package com.trs.netInsight.support.bigscreen.service.impl;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.bigscreen.service.IBigScreenService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsRankList;
import com.trs.netInsight.support.fts.entity.FtsRankListHtb;
import com.trs.netInsight.support.fts.entity.FtsRankListRsb;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.entity.BigScreenDistrictInfo;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.analysis.service.IBigScreenDistrictInfoService;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
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
    @Autowired
    private ICommonListService commonListService;
    @Autowired
    private ICommonChartService commonChartService;

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

    @Override
    public Object mapInfo(String timeRange) throws OperationException {
        String contrastField = FtsFieldConst.FIELD_CATALOG_AREA;
        HashMap map = new HashMap();

        QueryBuilder builder = new QueryBuilder();
        builder.setPageSize(3);
        builder.filterByTRSL(contrastField + ":(" +"中国\\\\安徽省\\\\宿州市\\\\泗县" + ")"+" NOT (IR_NOISEMESSAGE:(采购招标 OR 游戏信息 OR 涉黄信息 OR 赌博彩票 OR 母婴广告 OR 招生招聘 OR 转发抽奖 OR 影视剧场 OR 婚恋交友 OR 明星娱乐 OR 股票信息 OR 假证假票))");
        builder.filterField(FtsFieldConst.FIELD_URLTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1(timeRange), Operator.Between);
        builder.filterField(FtsFieldConst.FIELD_APPRAISE,"负面",Operator.Equal);
        QueryBuilder builder2 = new QueryBuilder();
        builder2.setPageSize(3);
        builder2.filterByTRSL(contrastField + ":(" +"中国\\\\安徽省\\\\宿州市\\\\灵璧县" + ")"+" NOT (IR_NOISEMESSAGE:(采购招标 OR 游戏信息 OR 涉黄信息 OR 赌博彩票 OR 母婴广告 OR 招生招聘 OR 转发抽奖 OR 影视剧场 OR 婚恋交友 OR 明星娱乐 OR 股票信息 OR 假证假票))");
        builder2.filterField(FtsFieldConst.FIELD_URLTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1(timeRange), Operator.Between);
        builder2.filterField(FtsFieldConst.FIELD_APPRAISE,"负面",Operator.Equal);
        QueryBuilder builder3 = new QueryBuilder();
        builder3.setPageSize(3);
        builder3.filterByTRSL(contrastField + ":(" +"中国\\\\安徽省\\\\宿州市\\\\砀山县" + ") NOT (IR_NOISEMESSAGE:(采购招标 OR 游戏信息 OR 涉黄信息 OR 赌博彩票 OR 母婴广告 OR 招生招聘 OR 转发抽奖 OR 影视剧场 OR 婚恋交友 OR 明星娱乐 OR 股票信息 OR 假证假票))");
        builder3.filterField(FtsFieldConst.FIELD_URLTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1(timeRange), Operator.Between);
        builder3.filterField(FtsFieldConst.FIELD_APPRAISE,"负面",Operator.Equal);
        QueryBuilder builder4 = new QueryBuilder();
        builder4.setPageSize(3);
        builder4.filterByTRSL(contrastField + ":(" +"中国\\\\安徽省\\\\宿州市\\\\萧县" + ") NOT (IR_NOISEMESSAGE:(采购招标 OR 游戏信息 OR 涉黄信息 OR 赌博彩票 OR 母婴广告 OR 招生招聘 OR 转发抽奖 OR 影视剧场 OR 婚恋交友 OR 明星娱乐 OR 股票信息 OR 假证假票))");
        builder4.filterField(FtsFieldConst.FIELD_URLTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1(timeRange), Operator.Between);
        builder4.filterField(FtsFieldConst.FIELD_APPRAISE,"负面",Operator.Equal);
        QueryBuilder builder5 = new QueryBuilder();
        builder5.setPageSize(3);
        builder5.filterByTRSL(contrastField + ":(" +"中国\\\\安徽省\\\\宿州市\\\\埇桥区" + ") NOT (IR_NOISEMESSAGE:(采购招标 OR 游戏信息 OR 涉黄信息 OR 赌博彩票 OR 母婴广告 OR 招生招聘 OR 转发抽奖 OR 影视剧场 OR 婚恋交友 OR 明星娱乐 OR 股票信息 OR 假证假票))");
        builder5.filterField(FtsFieldConst.FIELD_URLTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1(timeRange), Operator.Between);
        builder5.filterField(FtsFieldConst.FIELD_APPRAISE,"负面",Operator.Equal);
        InfoListResult infoListResult = null;
        InfoListResult infoListResult2 = null;
        InfoListResult infoListResult3 = null;
        InfoListResult infoListResult4 = null;
        InfoListResult infoListResult5 = null;
        try {
            infoListResult = commonListService.queryPageListForHot(builder,"ALL",null,"special",false);
            infoListResult2 = commonListService.queryPageListForHot(builder2,"ALL",null,"special",false);
            infoListResult3 = commonListService.queryPageListForHot(builder3,"ALL",null,"special",false);
            infoListResult4 = commonListService.queryPageListForHot(builder4,"ALL",null,"special",false);
            infoListResult5 = commonListService.queryPageListForHot(builder5,"ALL",null,"special",false);
            map.put("SIXIAN",infoListResult);
            map.put("LINGBI",infoListResult2);
            map.put("TANGSHAN",infoListResult3);
            map.put("XIAOXIAN",infoListResult4);
            map.put("YONGQIAO",infoListResult5);
        } catch (TRSException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Object hotList(String timeRange, String siteName, String channelName, String keyword) throws TRSException {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.filterField(FtsFieldConst.FIELD_LASTTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRangeMinus1(timeRange), Operator.Between);
        queryBuilder.setPageNo(0);
        queryBuilder.setPageSize(50);
        queryBuilder.orderBy("IR_LASTTIME", true);
        if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName) || Const.HTB_SITENAME_PENGPAI.equals(siteName) || Const.HTB_SITENAME_TIANYA.equals(siteName) ||("微博".equals(siteName) && "要闻榜".equals(channelName))) {
            queryBuilder.orderBy("IR_RANK", false);
        }
        if (ObjectUtil.isNotEmpty(channelName)){
            queryBuilder.filterField(FtsFieldConst.FIELD_CHANNEL,channelName,Operator.Equal);
            if ("同城榜".equals(channelName)){
                //只有同城榜需要 IR_CITY 字段 新增字段需要做空判断 默认北京
            }
        }
        if (ObjectUtil.isNotEmpty(siteName)) queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME,siteName,Operator.Equal);
        if (StringUtil.isNotEmpty(keyword)) {
            String[] split = keyword.split("\\s+|,");
            String splitNode = "";
            for (int i = 0; i < split.length; i++) {
                if (StringUtil.isNotEmpty(split[i])) {
                    splitNode += split[i] + ",";
                }
            }
            keyword = splitNode.substring(0, splitNode.length() - 1);
            if (keyword.endsWith(";") || keyword.endsWith(",") || keyword.endsWith("；")
                    || keyword.endsWith("，")) {
                keyword = keyword.substring(0, keyword.length() - 1);

            }
            String hybaseField = "IR_URLTITLE";
            if(Const.GROUPNAME_WEIBO.equals(siteName) && "热搜榜".equals(channelName)){
                hybaseField = "IR_HOTWORD";
            }
            StringBuilder fuzzyBuilder = new StringBuilder();
            fuzzyBuilder.append(hybaseField).append(":((\"").append(keyword.replaceAll("[,|，]+", "\") AND (\"")
                    .replaceAll("[;|；]+", "\" OR \"")).append("\"))");
            queryBuilder.filterByTRSL(fuzzyBuilder.toString());
        }
        if (Const.GROUPNAME_WEIBO.equals(siteName) && !"要闻榜".equals(channelName)){
            queryBuilder.setDatabase(Const.DC_BANGDAN);
            if ("热搜榜".equals(channelName) || "娱乐榜".equals(channelName)){
                //微博热搜榜
                PagedList<FtsRankListRsb> ftsPageList = commonListService.queryPageListForClass(queryBuilder,FtsRankListRsb.class,false,false,false,"hotTop");
                List<FtsRankListRsb> list = ftsPageList.getPageItems();
                List<FtsRankListRsb> listTemp = new ArrayList();
                for(int i=0;i<list.size();i++){//排重取十条数据
                    boolean isAdd = true;
                    for (int j = 0; j < listTemp.size(); j++) {
                        if(listTemp.get(j).getHotWord().contains(list.get(i).getHotWord())){
                            isAdd = false;
                            break;
                        }
                    }
                    if (isAdd) listTemp.add(list.get(i));
                    if (listTemp.size() > 7) break;

                }
                SortListRsb sortList = new SortListRsb();
                //按时间排序
                Collections.sort(listTemp, sortList);
                List<Object> resultList = new ArrayList<>();
                for (FtsRankListRsb vo : listTemp) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title",vo.getHotWord());
                    map.put("heat",vo.getHeat());
                    resultList.add(map);
                }
                return resultList;
            }else {
                //微博话题榜
//                queryBuilder.orderBy("IR_READNUM", true);
                queryBuilder.filterField(FtsFieldConst.FENLEI_HOTTOP,"总榜",Operator.Equal);
                PagedList<FtsRankListHtb> ftsPageList = commonListService.queryPageListForClass(queryBuilder,FtsRankListHtb.class,false,false,false,"hotTop");
                List<FtsRankListHtb> list = ftsPageList.getPageItems();
                SortListHtb sortList = new SortListHtb();
                Collections.sort(list, sortList);
                List<FtsRankListHtb> listTemp = new ArrayList();
                for(int i=0;i<list.size();i++){//排重取十条数据
                    boolean isAdd = true;
                    for (int j = 0; j < listTemp.size(); j++) {
                        if(listTemp.get(j).getTitle().contains(list.get(i).getTitle())){
                            isAdd = false;
                            break;
                        }
                    }
                    if (isAdd) listTemp.add(list.get(i));
                    if (listTemp.size() > 7) break;

                }
                List<Object> resultList = new ArrayList<>();
                for (FtsRankListHtb vo : listTemp) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title",vo.getTitle());
                    map.put("heat",vo.getReadNum());
                    resultList.add(map);
                }
                return resultList;
            }
        }else {
            queryBuilder.setDatabase(Const.DC_BANGDAN);
            PagedList<FtsRankList> ftsPageList = commonListService.queryPageListForClass(queryBuilder,FtsRankList.class,false,false,false,"hotTop");
            List<FtsRankList> list = ftsPageList.getPageItems();
            List<FtsRankList> listTemp = new ArrayList();
            for(int i=0;i<list.size();i++){//排重取十条数据
                boolean isAdd = true;
                for (int j = 0; j < listTemp.size(); j++) {
                    if(listTemp.get(j).getTitle().contains(list.get(i).getTitle())){
                        isAdd = false;
                        break;
                    }
                }
                if (isAdd) listTemp.add(list.get(i));
                if (listTemp.size() > 7) break;

            }
            if (Const.HTB_SITENAME_BAIDU.equals(siteName) || Const.HTB_SITENAME_360.equals(siteName) || Const.HTB_SITENAME_soudog.equals(siteName)) {
                SortListBangdan sortList = new SortListBangdan();
                Collections.sort(listTemp, sortList);
            }else if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName) || Const.HTB_SITENAME_PENGPAI.equals(siteName) || Const.HTB_SITENAME_TIANYA.equals(siteName) ||("微博".equals(siteName) && "要闻榜".equals(channelName))){

            }else {
                SortListBangdanHeat sortList = new SortListBangdanHeat();
                //按时间排序
                Collections.sort(listTemp, sortList);
            }

            List<Object> resultList = new ArrayList<>();
            for (FtsRankList vo : listTemp) {
                Map<String, Object> map = new HashMap<>();
                map.put("title",vo.getTitle());
                if (Const.HTB_SITENAME_BAIDU.equals(siteName) || Const.HTB_SITENAME_360.equals(siteName) || Const.HTB_SITENAME_soudog.equals(siteName)) {
                    map.put("heat",vo.getSearchIndex());
                }else if (Const.HTB_SITENAME_ZHIHUHTB.equals(siteName) || Const.HTB_SITENAME_JINRI.equals(siteName) || Const.HTB_SITENAME_PENGPAI.equals(siteName) || Const.HTB_SITENAME_TIANYA.equals(siteName) || ("微博".equals(siteName) && "要闻榜".equals(channelName))){
//这些没有热度指数
                }else {
                    map.put("heat",vo.getHeat());
                }

                resultList.add(map);
            }
            return resultList;
        }

    }

    @Override
    public Object mediaActiveAccount(QueryBuilder builder, String source, String[] timeArray, boolean sim, boolean irSimflag, boolean irSimflagAll) throws TRSException {
        List<String> allList = Const.ALL_GROUPNAME_SORT;
        List<Object> result = new ArrayList<>();
        List<String> sourceList = CommonListChartUtil.formatGroupName(source);
        ChartResultField resultField = new ChartResultField("name", "value");
        Boolean resultFlag = true;
        Integer active = 0;
        for(String oneGroupName : allList){

            //只显示选择的数据源
            if(sourceList.contains(oneGroupName)){
                QueryBuilder queryBuilder = new QueryBuilder();
                queryBuilder.filterByTRSL(builder.asTRSL());
                String contrastField = FtsFieldConst.FIELD_SITENAME;
                if(Const.GROUPNAME_WEIBO.equals(oneGroupName)){
                    contrastField = FtsFieldConst.FIELD_SCREEN_NAME;
                } else if (Const.MEDIA_TYPE_TF.contains(oneGroupName) || Const.MEDIA_TYPE_VIDEO.contains(oneGroupName) || Const.MEDIA_TYPE_ZIMEITI_LUNTAN_BOKE.contains(oneGroupName)) {
                    //FaceBook、Twitter、视频、短视频、自媒体号、论坛、博客
                    contrastField = FtsFieldConst.FIELD_AUTHORS;
                }
                if (Const.GROUPNAME_XINWEN.equals(oneGroupName)){
                    queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME,Const.REMOVEMEDIAS,Operator.NotEqual);
                }
                Map<String,Object> oneInfo = new HashMap<>();
                Object list = commonChartService.getBarColumnData(queryBuilder,sim,irSimflag,irSimflagAll,oneGroupName,null,contrastField,"special",resultField);
                List<Map<String, Object>> changeList = new ArrayList<>();
                if (list != null) {
                    changeList = (List<Map<String, Object>>) list;
                    if (changeList.size() > 0) {
                        resultFlag = false;
                        active++;
                        oneInfo.put("name", Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(oneGroupName));
                        oneInfo.put("info", list);
                        oneInfo.put("active", active == 1 ? true : false);
                        result.add(oneInfo);
                    }

                }

            }
        }
        if (resultFlag) {
            return null;
        }
        return result;
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
