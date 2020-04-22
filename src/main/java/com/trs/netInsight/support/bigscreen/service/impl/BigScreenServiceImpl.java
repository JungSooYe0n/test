package com.trs.netInsight.support.bigscreen.service.impl;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.bigscreen.service.IBigScreenService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.MapUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.entity.BigScreenDistrictInfo;
import com.trs.netInsight.widget.analysis.service.IBigScreenDistrictInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 *  大屏业务实现类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/12/18.
 * @desc
 */
@Service
public class BigScreenServiceImpl implements IBigScreenService {
    @Autowired
    private FullTextSearch hybase8SearchService;

    @Autowired
    private IBigScreenDistrictInfoService bigScreenDistrictInfoService;


    @Override
    public Object dataCount(String keyWords) throws OperationException {
//        String keyWordsNew = "";
//        if (StringUtil.isNotEmpty(keyWords)){
//            keyWordsNew = keyWords.replaceAll(";", "");
//        }
//        String redisId = "dataCount"+keyWordsNew;
//        List<Map<String, Object>> listMap = RedisUtil.getListMap(redisId);
//        if (ObjectUtil.isEmpty(listMap)){
            List<Map<String, Object>> returnList = new ArrayList<>();
            //当天
            Map<String, Object> tday = new HashMap<>();
            QueryCommonBuilder tdayBuilder = new QueryCommonBuilder();
            String trsl = "";
            if (StringUtil.isNotEmpty(keyWords)){
                trsl = new StringBuffer(FtsFieldConst.FIELD_URLTITLE).append(":(").append(handleKeyWords(keyWords)).append(") OR ")
                        .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(handleKeyWords(keyWords)).append(")").toString();
                tdayBuilder.filterByTRSL(trsl);
            }
            tdayBuilder.setDatabase(Const.MIX_DATABASE.split(";"));

            tdayBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange("0d"),
                    Operator.Between);
            long tdayCount = hybase8SearchService.ftsCountCommon(tdayBuilder, false, false,false,null);
            tday.put("type","tday");
            tday.put("value",tdayCount);

            //本周
            Map<String, Object> tweek = new HashMap<>();
            QueryCommonBuilder tweekBuilder = new QueryCommonBuilder();
            tweekBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
            tweekBuilder.filterByTRSL(trsl);
            String startWeek = DateUtil.startOfWeek()+"000000";
            // String endWeek = DateUtil.endOfWeek();
            String endWeek = DateUtil.formatDateAfterNow("yyyyMMddHHmmss", 0);
            String[] timesWeek = new String[2];
            timesWeek[0] = startWeek;
            timesWeek[1] = endWeek;
            tweekBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timesWeek,
                    Operator.Between);
            long tweekCount = hybase8SearchService.ftsCountCommon(tweekBuilder, false, false,false,null);
            tweek.put("type","tweek");
            tweek.put("value",tweekCount);

            //本月
            Map<String, Object> tmonth = new HashMap<>();
            QueryCommonBuilder tmonthBuilder = new QueryCommonBuilder();
            tmonthBuilder.filterByTRSL(trsl);
            tmonthBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
            String startMonth = DateUtil.startOfMonth()+"000000";
            // String endMonth = DateUtil.endOfMonth();
            String endMonth = DateUtil.formatDateAfterNow("yyyyMMddHHmmss", 0);
            String[] timesMonth = new String[2];
            timesMonth[0] = startMonth;
            timesMonth[1] = endMonth;
            tmonthBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timesMonth,
                    Operator.Between);
            long tmonthCount = hybase8SearchService.ftsCountCommon(tmonthBuilder, false, false,false,null);
            tmonth.put("type","tmonth");
            tmonth.put("value",tmonthCount);

            returnList.add(tday);
            returnList.add(tweek);
            returnList.add(tmonth);


           // RedisUtil.setListMap(redisId,returnList);
            return returnList;
       // }
      // return listMap;
    }

    @Override
    public Object dataTypeAnalysis(String keyWords,String groupName,String timeRange) throws OperationException {

            List<Map<String, Object>> returnList = new ArrayList<>();
            QueryBuilder queryBuilder = new QueryBuilder();
            String[] databases = null;
            if (StringUtil.isEmpty(groupName)){
                databases = Const.MIX_DATABASE.split(";");
                groupName = "(\"微博\" OR \"国内微信\" OR \"国内论坛\" OR \"国内新闻\" OR \"国内博客\" OR \"国内新闻_手机客户端\")";
            }else {
                databases = TrslUtil.chooseDatabases(groupName.split(";"));
                groupName = groupName.replaceAll(";"," OR ");
                if (groupName.endsWith(" OR ")){
                    groupName = groupName.substring(0,groupName.length()-4);
                }
            }
            queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName,Operator.Equal);
            queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
                    Operator.Between);
            if (StringUtil.isNotEmpty(keyWords)){
                String trsl = new StringBuffer(FtsFieldConst.FIELD_URLTITLE).append(":(").append(handleKeyWords(keyWords)).append(") OR ")
                        .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(handleKeyWords(keyWords)).append(")").toString();
                queryBuilder.filterByTRSL(trsl);
            }
           // System.err.println("数据类型对比："+queryBuilder.asTRSL());
            GroupResult categoryQuery = hybase8SearchService.categoryQuery(false, queryBuilder.asTRSL(), false,false,
                    false, FtsFieldConst.FIELD_GROUPNAME, 25,null,databases );
            if (ObjectUtil.isNotEmpty(categoryQuery)){
                List<GroupInfo> groupList = categoryQuery.getGroupList();
                if (ObjectUtil.isNotEmpty(groupList)){
                    for (GroupInfo groupInfo : groupList) {
                        Map<String, Object> map = new HashMap<>();
                        String key = groupInfo.getFieldValue().replaceAll("国内微信","微信").replaceAll("国内新闻_手机客户端","客户端").replaceAll("国内新闻_电子报","电子报").replaceAll("国内新闻","新闻").replaceAll("国内论坛","论坛").replaceAll("国内博客","博客").replaceAll("国外新闻","境外媒体");
                        map.put("name",key);
                        map.put("value",groupInfo.getCount());
                        returnList.add(map);
                    }
                }
            }
            return returnList;
        }
    @Override
    public Object industryVoice(String keyWords,String timeRange) throws OperationException {
            QueryBuilder queryBuilder = new QueryBuilder();
            Map<String, Object> dataMap = new HashMap<>();
            queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
                    Operator.Between);
            String trsl = "";
            if (StringUtil.isNotEmpty(keyWords)){
                trsl = new StringBuffer(FtsFieldConst.FIELD_URLTITLE).append(":(").append(handleKeyWords(keyWords)).append(") OR ")
                        .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(handleKeyWords(keyWords)).append(")").toString();
                queryBuilder.filterByTRSL(trsl);
            }
            queryBuilder.page(0,100);
            GroupResult groupInfos = hybase8SearchService.categoryQuery(queryBuilder, false, false,false, FtsFieldConst.FIELD_CHANNEL_INDUSTRY, Const.HYBASE_NI_INDEX);
            Set<String> keys = new HashSet<>();
            if (ObjectUtil.isNotEmpty(groupInfos)){
                List<GroupInfo> groupList = groupInfos.getGroupList();
                if (ObjectUtil.isNotEmpty(groupList)){
                    List<Object> values = new ArrayList<>();
                    for (GroupInfo groupInfo : groupList) {
                        if (ObjectUtil.isNotEmpty(groupInfo) && 0L != groupInfo.getCount()){
                            String[] split = groupInfo.getFieldValue().split("\\\\");
                            keys.add(split[0]);
                            if (keys.size() > 11){
                                break;
                            }
                        }
                    }
                    for (String key : keys) {
                        int num = 0;
                        for (GroupInfo groupInfo : groupList) {
                            String[] split = groupInfo.getFieldValue().split("\\\\");
                            if (key.equals(split[0])){
                                num += groupInfo.getCount();
                            }
                        }
                        values.add(num);
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("industry",keys);
                    map.put("vdata",values);
                    dataMap.put("total",map);
                }
            }

            QueryBuilder todayBuilder = new QueryBuilder();
            todayBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange("0d"),
                    Operator.Between);
            todayBuilder.filterByTRSL(trsl);
            todayBuilder.page(0,100);
            GroupResult todayGroupInfos = hybase8SearchService.categoryQuery(todayBuilder, false,false, false, FtsFieldConst.FIELD_CHANNEL_INDUSTRY, Const.HYBASE_NI_INDEX);
            if (ObjectUtil.isNotEmpty(todayGroupInfos)){
                List<GroupInfo> groupList = todayGroupInfos.getGroupList();
                if (ObjectUtil.isNotEmpty(groupList)){
                    List<Object> values = new ArrayList<>();
                    for (String key : keys) {
                        int num = 0;
                        for (GroupInfo groupInfo : groupList) {
                            String[] split = groupInfo.getFieldValue().split("\\\\");
                            if (key.equals(split[0])){
                                num += groupInfo.getCount();
                            }
                        }
                        values.add(num);
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("industry",keys);
                    map.put("vdata",values);
                    dataMap.put("today",map);
                }
            }
            return dataMap;
    }

    @Override
    public Object messageOfWeek(String keyWords,String flag) throws OperationException {
        Map<String, Object> returnMap = new HashMap<>();
        //System.err.println("flag："+flag);
        int dataCount = 1;
        if ("0".equals(flag)){//首次访问   返回7个
            dataCount = 7;
        }
        List<String[]> times = new ArrayList<>();
        List<Object> dateResult = new ArrayList<>();

        int count = dataCount;
        for (int i = 0; i < dataCount; i++) {
            String startTime = DateUtil.getTimeByMinute(-3 * count);
            String endTime = DateUtil.getTimeByMinute(-3 * (count-1));
            count--;
            String[] timeArray = {startTime,endTime};
            times.add(timeArray);
        }
        List<Object> matesCounts = new ArrayList<>();
        List<Object> netUsersCounts = new ArrayList<>();
        List<Object> overseasCounts = new ArrayList<>();
        for (String[] time : times) {
            long timeStamp = DateUtil.stringToDate(time[1], "yyyyMMddHHmmss").getTime();
            dateResult.add(timeStamp);
            //媒体
            String mates = "(\"国内新闻\")";
            QueryBuilder matesBuilder = new QueryBuilder();
            matesBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,mates,Operator.Equal);
            matesBuilder.filterField(FtsFieldConst.FIELD_LOADTIME, time,
                    Operator.Between);
            matesBuilder.setDatabase(Const.HYBASE_NI_INDEX);
            long matesCount = hybase8SearchService.ftsCount(matesBuilder, false, false,false,null);
            //
            String netUsers = "(\"国内新闻_手机客户端\")";
            QueryBuilder netUsersBuilder = new QueryBuilder();
            netUsersBuilder.setDatabase(Const.HYBASE_NI_INDEX);
            netUsersBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,netUsers,Operator.Equal);
            netUsersBuilder.filterField(FtsFieldConst.FIELD_LOADTIME, time,
                    Operator.Between);
            System.err.println("国内新闻手机客户端："+netUsersBuilder.asTRSL());
            long netUsersCount = hybase8SearchService.ftsCount(netUsersBuilder, false, false,false,null);
            //
            String overseas = "(\"国内微信\")";
            QueryBuilder overseasBuilder = new QueryBuilder();
           // String[] dataBase = {Const.HYBASE_NI_INDEX,Const.WECHAT};
           // overseasBuilder.setDatabase(dataBase);
           // overseasBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,overseas,Operator.Equal);
            overseasBuilder.filterField(FtsFieldConst.FIELD_LOADTIME, time,
                    Operator.Between);
            overseasBuilder.setDatabase(Const.WECHAT);
            long overseasCount = hybase8SearchService.ftsCount(overseasBuilder, false,false,false,null);
            matesCounts.add(matesCount);
            netUsersCounts.add(netUsersCount);
            overseasCounts.add(overseasCount);
        }

        returnMap.put("date",dateResult);
        returnMap.put("mates",matesCounts);
        returnMap.put("netUsers",netUsersCounts);
        returnMap.put("overseas",overseasCounts);





        return  returnMap;

    }

    @Override
    public Object areaThermogram(String keyWords,String timeRange) throws OperationException {
//        String keyWordsNew = "";
//        if (StringUtil.isNotEmpty(keyWords)){
//            keyWordsNew = keyWords.replaceAll(";", "");
//        }
//        String redisId = "areaThermogram"+keyWordsNew;
//        List<Map<String, Object>> listMap = RedisUtil.getListMap(redisId);
//        if (ObjectUtil.isEmpty(listMap)){
            QueryBuilder queryBuilder = new QueryBuilder();
            List<Map<String, Object>> returnList = new ArrayList<>();

            if (StringUtil.isNotEmpty(keyWords)){
                String trsl = new StringBuffer(FtsFieldConst.FIELD_URLTITLE).append(":(").append(handleKeyWords(keyWords)).append(") OR ")
                        .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(handleKeyWords(keyWords)).append(")").toString();
                queryBuilder.filterByTRSL(trsl);
            }
            queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
                Operator.Between);
          //  System.err.println("地图："+queryBuilder.asTRSL());
            List<BigScreenDistrictInfo> bigScreenDistrictInfos = bigScreenDistrictInfoService.findAll();
            GroupResult categoryQuery = hybase8SearchService.categoryQuery(false, queryBuilder.asTRSL(), false,false,
                    false, FtsFieldConst.FIELD_CATALOG_AREA, 100,Const.HYBASE_NI_INDEX );
            //只取省 不取下属城市
//        if (ObjectUtil.isNotEmpty(categoryQuery)){
//            List<GroupInfo> groupList = categoryQuery.getGroupList();
//            for (BigScreenDistrictInfo bigScreenDistrictInfo :bigScreenDistrictInfos) {
//                    if (ObjectUtil.isNotEmpty(groupList)){
//                        int num = 0;
//                        for (GroupInfo groupInfo : groupList) {
//                            String city = groupInfo.getFieldValue().replaceAll("市", "");
//                            if (city.contains(bigScreenDistrictInfo.getAreaName())){
//                                num += groupInfo.getCount();
//                            }
//                        }
//                        Map<String, Object> dataMap = new HashMap<>();
//                        dataMap.put("name",bigScreenDistrictInfo.getAreaName());
//                        dataMap.put("value",num);
//                        if (0 != num ){
//                            returnList.add(dataMap);
//                        }
//
//                        if (returnList.size() > 5){
//                            break;
//                        }
//                    }
//            }
//
//        }


        if (ObjectUtil.isNotEmpty(categoryQuery)){
            List<GroupInfo> groupList = categoryQuery.getGroupList();
            if (ObjectUtil.isNotEmpty(groupList)){
                for (GroupInfo groupInfo : groupList) {
                    String city = groupInfo.getFieldValue().replaceAll("市", "");
                    for (BigScreenDistrictInfo bigScreenDistrictInfo :bigScreenDistrictInfos) {
                        if (city.contains(bigScreenDistrictInfo.getAreaName())){
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("name",bigScreenDistrictInfo.getAreaName());
                            dataMap.put("value",groupInfo.getCount());
                            if (0 != groupInfo.getCount() ){
                                returnList.add(dataMap);
                            }
                        }

                        if (returnList.size() > 5){
                            break;
                        }
                    }

                }
            }

        }


          //  RedisUtil.setListMap(redisId,returnList);
            return returnList;
//        }
//       return listMap;
    }

    @Override
    public Object topicType(String keyWords,String timeRange) throws OperationException {
        QueryBuilder queryBuilder = new QueryBuilder();
        List<Map<String, Object>> returnList = new ArrayList<>();
        queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
                Operator.Between);
        String trsl = "";
        if (StringUtil.isNotEmpty(keyWords)){
            trsl = new StringBuffer(FtsFieldConst.FIELD_URLTITLE).append(":(").append(handleKeyWords(keyWords)).append(") OR ")
                    .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(handleKeyWords(keyWords)).append(")").toString();
            queryBuilder.filterByTRSL(trsl);
        }
        queryBuilder.page(0,100);
        GroupResult groupInfos = hybase8SearchService.categoryQuery(queryBuilder, false, false,false, FtsFieldConst.FIELD_CHANNEL_INDUSTRY, Const.HYBASE_NI_INDEX);
        Set<String> keys = new HashSet<>();
        if (ObjectUtil.isNotEmpty(groupInfos)){
            List<GroupInfo> groupList = groupInfos.getGroupList();
            if (ObjectUtil.isNotEmpty(groupList)){
                for (GroupInfo groupInfo : groupList) {
                    if (ObjectUtil.isNotEmpty(groupInfo) && 0L != groupInfo.getCount()){
                        String[] split = groupInfo.getFieldValue().split("\\\\");
                        keys.add(split[0]);
                        if (keys.size() > 10){
                            break;
                        }
                    }
                }
                for (String key : keys) {
                    int num = 0;
                    for (GroupInfo groupInfo : groupList) {
                        String[] split = groupInfo.getFieldValue().split("\\\\");
                        if (key.equals(split[0])){
                            num += groupInfo.getCount();
                        }
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("name",key);
                    map.put("value",num);
                    returnList.add(map);
                }
            }
        }
        return returnList;
    }

    @Override
    public Object emotionAnalysis(String keyWords, String timeRange) throws OperationException {
        QueryBuilder queryBuilder = new QueryBuilder();
        List<Map<String, Object>> returnList = new ArrayList<>();
        queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
                Operator.Between);
        String trsl = "";
        if (StringUtil.isNotEmpty(keyWords)){
            if (keyWords.endsWith(";")){
                keyWords += Const.BIGSCREEN_EMTION;
            }else {
                keyWords += ";"+Const.BIGSCREEN_EMTION;
            }
            trsl = new StringBuffer(FtsFieldConst.FIELD_URLTITLE).append(":(").append(handleKeyWords(keyWords)).append(") OR ")
                    .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(handleKeyWords(keyWords)).append(")").toString();
        }else {
            trsl = new StringBuffer(FtsFieldConst.FIELD_URLTITLE).append(":(").append(handleKeyWords(Const.BIGSCREEN_EMTION)).append(") OR ")
                    .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(handleKeyWords(Const.BIGSCREEN_EMTION)).append(")").toString();
        }
        queryBuilder.filterByTRSL(trsl);
        queryBuilder.filterByTRSL(" (IR_EMOTION:(\"怒\" OR \"恶\" OR \"惧\" OR \"喜\" OR \"哀\"))");
        GroupResult emtionResult = hybase8SearchService.categoryQuery(false, queryBuilder.asTRSL(), false, false,false,
                FtsFieldConst.FIELD_EMOTION, 5, Const.HYBASE_NI_INDEX);

        if (ObjectUtil.isNotEmpty(emtionResult)){
            List<GroupInfo> groupList = emtionResult.getGroupList();
            if (ObjectUtil.isNotEmpty(groupList)){
                for (GroupInfo groupInfo : groupList) {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("name",groupInfo.getFieldValue());
                    dataMap.put("value",groupInfo.getCount());
                    returnList.add(dataMap);
                }
            }
        }
        return returnList;
    }

    @Override
    public Object hotWordCloud(String keyWords,String timeRange) throws OperationException {
//        String keyWordsNew = "";
//        if (StringUtil.isNotEmpty(keyWords)){
//            keyWordsNew = keyWords.replaceAll(";", "");
//        }
//        String redisId = "hotWordCloud"+keyWordsNew;
//        Map<Object, Object> listMap = RedisUtil.getMapper(redisId);
//        if (ObjectUtil.isEmpty(listMap)){
            QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
                    Operator.Between);
            queryBuilder.page(0,20);
            String[] dataBase = {Const.HYBASE_NI_INDEX,Const.WEIBO};
            if (StringUtil.isNotEmpty(keyWords)){
                String trsl = new StringBuffer(FtsFieldConst.FIELD_URLTITLE).append(":(").append(handleKeyWords(keyWords)).append(") OR ")
                        .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(handleKeyWords(keyWords)).append(")").toString();
                queryBuilder.filterByTRSL(trsl);
            }
           // System.err.println("热点词云："+queryBuilder.asTRSL());
            // 通用
            GroupResult categoryQuery = hybase8SearchService.categoryQuery(false, queryBuilder.asTRSL(), false, false,false,
                    Const.PARAM_MAPPING.get("keywords"), 20,null,dataBase);

            // 人物、地域、机构
            GroupResult people = hybase8SearchService.categoryQuery(false, queryBuilder.asTRSL(), false, false,false,
                    Const.PARAM_MAPPING.get("people"), 20,null, dataBase);
            GroupResult location = hybase8SearchService.categoryQuery(false, queryBuilder.asTRSL(), false, false,false,
                    Const.PARAM_MAPPING.get("location"), 20,null, dataBase);
            GroupResult agency = hybase8SearchService.categoryQuery(false, queryBuilder.asTRSL(), false, false,false,
                    Const.PARAM_MAPPING.get("agency"),  20,null, dataBase);

            List<Map<String, Object>> keyWordsMap = handleWordClould(categoryQuery);
            List<Map<String, Object>> peopleMap = handleWordClould(people);
            List<Map<String, Object>> locationMap = handleWordClould(location);
            List<Map<String, Object>> agencyMap = handleWordClould(agency);

            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("keywords",keyWordsMap);
            returnMap.put("people",peopleMap);
            returnMap.put("location",locationMap);
            returnMap.put("agency",agencyMap);
        //    RedisUtil.setMapper(redisId,returnMap);
            return returnMap;
//        }
//        return listMap;
    }



    private String handleKeyWords(String keyWords){
        if (StringUtil.isNotEmpty(keyWords)){
            keyWords = keyWords.replaceAll(";"," OR ");

            if (keyWords.endsWith(" OR ")){
              keyWords = keyWords.substring(0,keyWords.length()-4) ;
            }
            return keyWords;
        }
        return "";
    }

    private List<Map<String, Object>> handleWordClould(GroupResult categoryQuery){
        List<Map<String, Object>> returnList = new ArrayList<>();
        // 数据清理
        if (ObjectUtil.isNotEmpty(categoryQuery)){
            List<GroupInfo> categoryList = categoryQuery.getGroupList();
            if (ObjectUtil.isNotEmpty(categoryList)){
                for (int j = 0; j < categoryList.size(); j++) {
                    GroupInfo groupInfo = categoryList.get(j);
                    if (Const.NOT_KEYWORD.contains(groupInfo.getFieldValue())) {
                        categoryList.remove(j);
                        j--;
                    }
                }
                // 数据清理
                for (int i = 0; i < categoryList.size(); i++) {
                    String name = categoryList.get(i).getFieldValue();
                    if (name.endsWith("html")) {
                        categoryList.remove(i);
                        break;
                    }
                    if (name.contains(";")) {
                        String[] split = name.split(";");
                        name = split[split.length - 1];
                    }
                    if (name.contains("\\")) {
                        String[] split = name.split("\\\\");
                        name = split[split.length - 1];
                    }
                    if (name.contains(".")) {
                        String[] split = name.split("\\.");
                        name = split[split.length - 1];
                    }
                    categoryList.get(i).setFieldValue(name);
                }

                for (GroupInfo groupInfo : categoryList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name",groupInfo.getFieldValue());
                    map.put("value",groupInfo.getCount());
                    returnList.add(map);
                }

            }

        }
        return returnList;
    }
}
