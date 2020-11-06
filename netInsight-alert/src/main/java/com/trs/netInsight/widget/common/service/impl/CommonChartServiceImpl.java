package com.trs.netInsight.widget.common.service.impl;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.model.result.*;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.CollectionsUtil;
import com.trs.netInsight.util.MapUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.analysis.entity.DistrictInfo;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.gather.entity.GatherPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommonChartServiceImpl implements ICommonChartService {

    @Autowired
    private ICommonListService commonListService;

    @Autowired
    private IDistrictInfoService districtInfoService;


    /**
     * 分类统计数据 - 公共的部分，包括参数处理
     *
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param xyTrsl  分类检索表达式  -- 通过这个字段判断是否是专家模式  专家模式单独分类统计每个分类的数据，与直接通过表达式统计不同
     * @param contrastField  分类统计字段
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param <T>
     * @return
     * @throws TRSException
     */
    private <T extends IQueryBuilder> GroupResult getCategoryQueryData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName,
                                                                       String xyTrsl, String contrastField, String type) throws TRSException {
        T newBuilder = builder;
        QueryBuilder queryBuilder = (QueryBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(newBuilder,groupName,0);
        if(queryBuilder == null){
            //通过方法生成queryBuilder之后，判断是否存在可查询的数据源，无则返回null,则对应无可查询数据
            return null;
        }
        //获取基础的表达式
        String trsl = queryBuilder.asTRSL();
        GroupResult groupResult = null;
        //专家模式
        //直接统计每个对比类型的数据
        if (StringUtil.isNotEmpty(xyTrsl)) {
            groupResult = new GroupResult();
            List<CategoryBean> mediaType = CommonListChartUtil.getMediaType(xyTrsl);
            for (CategoryBean categoryBean : mediaType) {
                QueryBuilder specialBuilder = new QueryBuilder();
                specialBuilder.filterByTRSL(trsl);
                if (StringUtil.isNotEmpty(categoryBean.getValue())) {
                    String value = categoryBean.getValue().toLowerCase().trim();
                    if (value.startsWith("not")) {
                        specialBuilder.filterByTRSL_NOT(categoryBean.getValue().substring(3, categoryBean.getValue().length()));
                    } else {
                        specialBuilder.filterByTRSL(categoryBean.getValue());
                    }
                }
                specialBuilder.setDatabase(queryBuilder.getDatabase());

                Long count = commonListService.ftsCount(specialBuilder, sim, irSimflag, irSimflagAll, type);
                groupResult.addGroup(categoryBean.getKey(), count);
            }
          groupResult.sort();
        } else {
            QueryBuilder ordinaryBuilder = new QueryBuilder();
            ordinaryBuilder.filterByTRSL(trsl);
            ordinaryBuilder.setPageSize(queryBuilder.getPageSize());
            ordinaryBuilder.setDatabase(queryBuilder.getDatabase());
            groupResult = commonListService.categoryQuery(ordinaryBuilder, sim, irSimflag, irSimflagAll, contrastField, type);

        }
        return groupResult;

    }

    //柱状图

    /**
     * 柱状图数据查询
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param xyTrsl  分类检索表达式  -- 通过这个字段判断是否是专家模式  专家模式单独分类统计每个分类的数据，与直接通过表达式统计不同
     * @param contrastField  分类统计字段  -- 普通模式采用这个字段进行统计
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param resultKey   这个是返回数据中对应的名字
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getBarColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName,
                                                             String xyTrsl, String contrastField, String type, ChartResultField resultKey) throws TRSException {
        if (StringUtil.isEmpty(xyTrsl)) {
            if(StringUtil.isEmpty(contrastField)){
                throw new OperationException("柱状图普通模式下没有对比字段");
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        if (StringUtil.isNotEmpty(groupName)) {
            List<String> sourceList = CommonListChartUtil.formatGroupName(groupName);
            if (sourceList.size() > 0) {
                try {
                    //用统一方法进行统计
                    GroupResult groupInfos = this.getCategoryQueryData(builder, sim, irSimflag, irSimflagAll, groupName, xyTrsl, contrastField, type);
                    if(groupInfos == null){
                        return null;
                    }
                    if (groupInfos != null && groupInfos.getGroupList().size() > 0) {
                        List<GroupInfo> groupList = groupInfos.getGroupList();
                        if (FtsFieldConst.FIELD_GROUPNAME.equals(contrastField)) {

                            for (GroupInfo groupInfo : groupList) {
                                Map<String, Object> putValue = MapUtil.putValue(new String[]{resultKey.getContrastField(), resultKey.getCountField()},
                                        Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(groupInfo.getFieldValue()),  String.valueOf(groupInfo.getCount()));
                                list.add(putValue);
                            }
                            for (String oneGroupName:sourceList) {
                                int isIn = 0;
                                for (GroupInfo groupInfo : groupList) {
                                    if (oneGroupName.equals(groupInfo.getFieldValue())) {
                                        isIn = 1;
                                        break;
                                    }
                                }
                                if (isIn == 0){
                                    Map<String, Object> putValue = new HashMap<>();
                                    String pageGroupName = Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(oneGroupName);
                                    putValue.put(resultKey.getContrastField(), pageGroupName);
                                    putValue.put(resultKey.getCountField(), 0);
                                    list.add(putValue);
                                }
                            }
                        } else {
                            for (GroupInfo groupInfo : groupList) {
                                Map<String, Object> putValue = MapUtil.putValue(new String[]{resultKey.getContrastField(), resultKey.getCountField()},
                                        groupInfo.getFieldValue(),  String.valueOf(groupInfo.getCount()));
                                list.add(putValue);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new TRSSearchException(e);
                }
            }
        }
        return list;
    }
    //饼图

    /**
     *饼图数据查询
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param xyTrsl  分类检索表达式  -- 通过这个字段判断是否是专家模式  专家模式单独分类统计每个分类的数据，与直接通过表达式统计不同
     * @param contrastField  分类统计字段  -- 普通模式采用这个字段进行统计
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param resultKey   这个是返回数据中对应的名字
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getPieColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName, String xyTrsl, String contrastField, String type, ChartResultField resultKey) throws TRSException {

        if (StringUtil.isEmpty(xyTrsl)) {
            if(StringUtil.isEmpty(contrastField)){
                throw new OperationException("饼图普通模式下没有对比字段");
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        if (StringUtil.isNotEmpty(groupName)) {
            List<String> sourceList = CommonListChartUtil.formatGroupName(groupName);
            if (sourceList.size() > 0) {
                try {
                    //用统一方法进行统计
                    GroupResult groupInfos = this.getCategoryQueryData(builder, sim, irSimflag, irSimflagAll, groupName, xyTrsl, contrastField, type);
                    if(groupInfos == null){
                        return null;
                    }
                    if (groupInfos != null && groupInfos.getGroupList().size() > 0) {
                        List<GroupInfo> groupList = groupInfos.getGroupList();
                        if (FtsFieldConst.FIELD_GROUPNAME.equals(contrastField)) {

                            for (GroupInfo groupInfo : groupList) {
                                Map<String, Object> putValue = MapUtil.putValue(new String[]{resultKey.getContrastField(), resultKey.getCountField()},
                                        Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(groupInfo.getFieldValue()),  String.valueOf(groupInfo.getCount()));
                                list.add(putValue);
                            }
                            for (String oneGroupName:sourceList) {
                                int isIn = 0;
                                for (GroupInfo groupInfo : groupList) {
                                    if (oneGroupName.equals(groupInfo.getFieldValue())) {
                                        isIn = 1;
                                        break;
                                    }
                                }
                                if (isIn == 0){
                                    Map<String, Object> putValue = new HashMap<>();
                                    String pageGroupName = Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(oneGroupName);
                                    putValue.put(resultKey.getContrastField(), pageGroupName);
                                    putValue.put(resultKey.getCountField(), 0);
                                    list.add(putValue);
                                }
                            }
                        } else {
                            for (GroupInfo groupInfo : groupList) {
                                Map<String, Object> putValue = MapUtil.putValue(new String[]{resultKey.getContrastField(), resultKey.getCountField()},
                                        groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
                                list.add(putValue);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new TRSSearchException(e);
                }
            }
        }
        return list;

    }

    //地图

    /**
     * 地图数据查询
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param contrastField  分类统计字段，-普通模式用这个字段进行统计
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param resultKey   这个是返回数据中对应的名字
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getMapColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName,
                                                             String contrastField, String type, ChartResultField resultKey) throws TRSException {

        if (StringUtil.isEmpty(contrastField)) {
            throw new OperationException("没有准确填写对比字段");
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (StringUtil.isNotEmpty(groupName)) {
            try {
                //地图部分专家或者普通，因为没有分类对比表达式 地图下钻时:type=mapto_北京_column
                String typeTmp = type;
                String areaName = "";
                if(type.startsWith(Const.mapto)){
                    areaName = typeTmp.split("_")[1];
                    typeTmp = typeTmp.split("_")[2];
                }
                GroupResult categoryInfos = this.getCategoryQueryData(builder, sim, irSimflag, irSimflagAll, groupName,  "", contrastField, typeTmp);
                if(categoryInfos == null || categoryInfos.getGroupList().size() ==0){
                    return null;
                }
                //地图下钻走
                if(type.startsWith(Const.mapto)){
                    if(typeTmp.equals("special")) return getMaptoDataS(categoryInfos,areaName,resultKey);
                    else return getMaptoData(categoryInfos,areaName,resultKey);
                }
                Map<String, List<String>> areaMap = districtInfoService.allAreas();
                if ("special".equals(type)) {
//                    省（包含自治区）、市（包含直辖市）、行政区
                    Map<String, Object> provinceMap = new HashMap<String, Object>();
                    Map<String, Object> cityMap = new HashMap<String, Object>();
                    Map<String, Object> specialARMap = new HashMap<String, Object>();
                    for (Map.Entry<String, List<String>> entry : areaMap.entrySet()) {
                        Map<String, Object> reMap = new HashMap<String, Object>();
                        int num = 0;
                        // 查询结果之间相互对比 所以把城市放开也不耽误查询速度
                        for (GroupInfo classEntry : categoryInfos) {
                            String area = classEntry.getFieldValue();
                            //将从hybase查询到的地域 信息转化为页面中要显示的样子，也跟从mysql库中查询到的一样
                            area = Const.PAGE_SHOW_PROVINCE_NAME.get(area);
                            if(entry.getKey().equals(area)){
                                num = (int)classEntry.getCount();
                                break;
                            }
                        }
                        reMap.put(resultKey.getContrastField(), entry.getKey());
                        reMap.put(resultKey.getCountField(), num);
                        list.add(reMap);

                        if (num > 0){
                            if ("北京".equals(entry.getKey()) || "天津".equals(entry.getKey()) || "上海".equals(entry.getKey()) || "重庆".equals(entry.getKey())){
                                cityMap.put(entry.getKey(),num);
                            } else if ("香港".equals(entry.getKey()) || "澳门".equals(entry.getKey())) {
                                specialARMap.put(entry.getKey(),num);
                            } else {
                                provinceMap.put(entry.getKey(),num);
                            }
                        }

                        if (FtsFieldConst.FIELD_CATALOG_AREA.equals(contrastField)){  //  内容地域
                            for (String city : entry.getValue()) {
                                int num2 = 0;
                                int numJiLin = 0;
                                // 因为吉林省市同名,单独拿出,防止按区域名称分类统计错误
                                for (GroupInfo classEntry : categoryInfos) {
                                    if (classEntry.getFieldValue().contains(city) && !classEntry.getFieldValue().contains("吉林省\\吉林市")) {
                                        num2 += classEntry.getCount();
                                    } else if (classEntry.getFieldValue().contains("吉林省\\吉林市")) {
                                        numJiLin += classEntry.getCount();
                                    }
                                }
                                // 把.之前的去掉
                                String[] citySplit = city.split(".");
                                if (citySplit.length > 1) {
                                    city = citySplit[citySplit.length - 1];
                                }

                                if ("吉林".equals(city)) {
                                    if (numJiLin > 0){
                                        cityMap.put(city,numJiLin);
                                    }
                                }else {
                                    if (num2 > 0){
                                        if (Const.CITY_BEIJING.contains(city)){
                                            cityMap.put("北京",num2);
                                        } else if (Const.CITY_TIANJIN.contains(city)){
                                            cityMap.put("天津",num2);
                                        } else if (Const.CITY_SHANGHAI.contains(city)){
                                            cityMap.put("上海",num2);
                                        } else if (Const.CITY_CHONGQING.contains(city)){
                                            cityMap.put("重庆",num2);
                                        } else if (!Const.CITY_AOMEN.contains(city) && !Const.CITY_XIANGGANG.contains(city)){
                                            cityMap.put(city,num2);
                                        }
                                    }
                                }
                            }
                        }
//                        else { // 媒体地域
//
//                        }
                    }
                    Map<String, Object> returnMap = new HashMap<String, Object>();
                    returnMap.put("areaData",list);
                    returnMap.put("province",provinceMap.size());
                    returnMap.put("city",cityMap.size());
                    returnMap.put("specialAR",specialARMap.size());
                    return returnMap;
                } else {
                    for (Map.Entry<String, List<String>> entry : areaMap.entrySet()) {
                        Map<String, Object> reMap = new HashMap<String, Object>();
                        int num = 0;
                        // 查询结果之间相互对比 所以把城市放开也不耽误查询速度
                        for (GroupInfo classEntry : categoryInfos) {
                            String area = classEntry.getFieldValue();
                            //将从hybase查询到的地域 信息转化为页面中要显示的样子，也跟从mysql库中查询到的一样
                            area = Const.PAGE_SHOW_PROVINCE_NAME.get(area);
                            if(entry.getKey().equals(area)){
                                num = (int)classEntry.getCount();
                                break;
                            }
                        }
                        reMap.put(resultKey.getContrastField(), entry.getKey());
                        reMap.put(resultKey.getCountField(), num);
                        list.add(reMap);
                    }
                }
            } catch (Exception e) {
                throw new TRSSearchException(e);
            }
        }
        return list;
    }

    /**
     * 地图下钻 -- 日常监测
     * @param categoryInfos
     * @param areaName
     * @return
     */
    public List<Map<String,Object>> getMaptoData(GroupResult categoryInfos,String areaName,ChartResultField resultKey){
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String,Integer> bjmap = new HashMap<>();
        List<DistrictInfo> citys = districtInfoService.getAreasByCode(areaName);
        for (GroupInfo classEntry : categoryInfos) {
            String area = classEntry.getFieldValue();
            int num2 = (int)classEntry.getCount();
            for(DistrictInfo d: citys){
                String formatArea = d.getAreaName().replace("市","").replace("区","");
                if(area.indexOf(formatArea) >0){
                    if(bjmap.get(d.getAreaName())==null) bjmap.put(d.getAreaName(),num2);
                    else if(bjmap.get(d.getAreaName())<num2) bjmap.put(d.getAreaName(),num2);
                }
            }
        }

        for(DistrictInfo d: citys){
            Map<String,Object> mm = new HashMap<>();
            String mapKey = d.getAreaName();
            Integer mapValue = bjmap.get(mapKey)==null?0:bjmap.get(mapKey);
            mm.put(resultKey.getContrastField(), mapKey);
            mm.put(resultKey.getCountField(), mapValue);
            list.add(mm);
        }

        return list;
    }
    /**
     * 地图下钻 -- 专题分析
     * @param categoryInfos
     * @param areaName
     * @return
     */
    public Map<String,Object> getMaptoDataS(GroupResult categoryInfos,String areaName,ChartResultField resultKey){
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String,Integer> bjmap = new HashMap<>();
        List<DistrictInfo> citys = districtInfoService.getAreasByCode(areaName);
        for (GroupInfo classEntry : categoryInfos) {
            String area = classEntry.getFieldValue();
            int num2 = (int)classEntry.getCount();
            for(DistrictInfo d: citys){
                String formatArea = d.getAreaName().replace("市","").replace("区","");
                if(area.indexOf(formatArea) >0){
                    if(bjmap.get(d.getAreaName())==null) bjmap.put(d.getAreaName(),num2);
                    else if(bjmap.get(d.getAreaName())<num2) bjmap.put(d.getAreaName(),num2);
                }
            }
        }

        for(DistrictInfo d: citys){
            Map<String,Object> mm = new HashMap<>();
            String mapKey = d.getAreaName();
            Integer mapValue = bjmap.get(mapKey)==null?0:bjmap.get(mapKey);
            mm.put(resultKey.getContrastField(), mapKey);
            mm.put(resultKey.getCountField(), mapValue);
            list.add(mm);
        }

        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("areaData",list);
        returnMap.put("city",citys.size());
        return returnMap;

    }



    //词云图

    /**
     *词云图数据查询
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param entityType  词云图的筛选类型
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getWordCloudColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll,
                                                                   String groupName, String entityType, String type, ChartResultField resultKey) throws TRSException {
        try {
            GroupWordResult wordInfos = new GroupWordResult();
            QueryBuilder queryBuilder =(QueryBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(builder,groupName,0);

            if ("keywords".equals(entityType)) {
                // 人物、地域、机构
                GroupResult people = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, Const.PARAM_MAPPING.get("people"), type);
                GroupResult location = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, Const.PARAM_MAPPING.get("location"), type);
                GroupResult agency = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, Const.PARAM_MAPPING.get("agency"), type);

                if(people != null && people.getGroupList().size()>0){
                    List<GroupInfo> peopleList = people.getGroupList();
                    for (GroupInfo groupInfo : peopleList) {
                        wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), "people");
                    }
                }
                if(location != null && location.getGroupList().size()>0){
                    List<GroupInfo> locationList = location.getGroupList();
                    for (GroupInfo groupInfo : locationList) {
                        wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), "location");
                    }
                }
                if(agency != null && agency.getGroupList().size()>0){
                    List<GroupInfo> agencyList = agency.getGroupList();
                    for (GroupInfo groupInfo : agencyList) {
                        wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), "agency");
                    }
                }
            } else {
                GroupResult result = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, Const.PARAM_MAPPING.get(entityType), type);
                if(result != null && result.getGroupList().size()>0){
                    List<GroupInfo> groupList = result.getGroupList();
                    for (GroupInfo groupInfo : groupList) {
                        wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), entityType);
                    }
                }
            }
            wordInfos.sort();

            Integer pagesize = queryBuilder.getPageSize();
            List<GroupWordInfo> groupWordList = wordInfos.getGroupList();
            List<GroupWordInfo> newGroupWordList = new ArrayList<>();
            if (groupWordList.size() > pagesize) {
                newGroupWordList = groupWordList.subList(0, pagesize);
            } else {
                newGroupWordList.addAll(groupWordList);
            }

            // 数据清理
            for (int i = 0; i < newGroupWordList.size(); i++) {
                String name = newGroupWordList.get(i).getFieldValue();
                if (name.endsWith("html")) {
                    newGroupWordList.remove(i);
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
                newGroupWordList.get(i).setFieldValue(name);
            }
            if (ObjectUtil.isNotEmpty(newGroupWordList) && newGroupWordList.size() > 0) {

                List<Object> result = new ArrayList<>();
                Map<String, Object> map = null;
                for (GroupWordInfo wordInfo : newGroupWordList) {
                    map = new HashMap<>();
                    map.put(resultKey.getContrastField(), wordInfo.getFieldValue());
                    map.put(resultKey.getCountField(), wordInfo.getCount());
                    map.put(resultKey.getLineXField(), wordInfo.getEntityType());
                    result.add(map);
                }
                return result;
            } else {
                return null;
            }
        } catch (TRSSearchException e) {
            throw new TRSSearchException(e);
        }
    }


    //折线图
    //groupField 为折线图x轴字段   如果为按小时显示，因为Hybase查询时，按小时只能显示小时数，所以按小时查询时，一次最多显示一天的数据，方法内不对返回的时间做处理，所以在按小时查询时，传进来的groupData的值完全与hybase查询结果一样

    /**
     *
     * @param builder  检索构造器
     * @param sim  单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  要查询的数据源，用;分号分割
     * @param type   查询类型，对应用户限制查询时间的模块相同
     * @param xyTrsl  分类对比查询表达式 -- 专家模式下，用户填写的分类对比表达式，这个字段有值时，不要写下面的contrastField和  contrastData
     * @param contrastField   对比字段
     * @param contrastData  对比字段中要对比的数据集合，例如对比字段是groupName，那么这个字段的值应该为对应的要对比的数据源如新闻等。
     * @param groupField   这个为x轴对应的字段，例如日常监测中的折线图x轴为时间，则这个字段为时间字段，一条线为一个按时间进行统计分析的结果
     * @param groupData  这个为x轴对应的展示数据，这个值需要与hybase查询到的统计结果的key值相同，分则无效
     * @param resultKey   这个是返回数据中对应的名字
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getChartLineColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName, String type, String xyTrsl,
                                                                   String contrastField, List<String> contrastData, String groupField, List<String> groupData, ChartResultField resultKey) throws TRSException {
        try {
            QueryBuilder queryBuilder = (QueryBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(builder,groupName,0);

            //折线图特殊的地方，需要三个维度的参数
            //一条线代表一个分类统计结果
            if (StringUtil.isEmpty(xyTrsl)) {
                if (StringUtil.isEmpty(contrastField)) {
                    throw new OperationException("折线图普通模式下没有对比字段");
                } else {
                    if (contrastData == null || contrastData.size() == 0) {
                        contrastData = new ArrayList<>();
                        queryBuilder.setPageSize(8);
                        GroupResult result = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, contrastField, type);
                        if (result != null) {
                            List<GroupInfo> groupList = result.getGroupList();
                            if (groupList != null && groupList.size() > 0) {
                                for (GroupInfo groupInfo : groupList) {
                                    contrastData.add(groupInfo.getFieldValue());
                                }
                            }
                        }
                    }
                }
            }
            if (StringUtil.isEmpty(groupField)) {
                throw new OperationException("折线图没有标明x轴字段属性");
            }
            if (!CollectionsUtil.isNotEmpty(groupData)) {
                throw new OperationException("折线图x轴无数据");
            }
            List<CategoryBean> categoryBeans = new ArrayList<>();
            if(StringUtil.isNotEmpty(xyTrsl)){
                categoryBeans =  CommonListChartUtil.getMediaType(xyTrsl);
            }else{
                for(String contrast:contrastData){
                    String categoryBeanKey = contrast;
                    if(contrastField.equals(FtsFieldConst.FIELD_GROUPNAME)){
                        contrast = Const.SOURCE_GROUPNAME_CONTRAST.get(contrast);
                        categoryBeanKey = Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(contrast);
                    }
                    String value = contrastField+":("+contrast+")";
                    categoryBeans.add(new CategoryBean(categoryBeanKey,value));
                }
            }


            int pageSize = groupData.size() + 1;

            Map<String ,List<Object>> result = new HashMap<>();
            List<Object> contrastList = new ArrayList<>();
            List<Object> dataList = new ArrayList<>();
            List<Object> totalList = new ArrayList<>();
            for (CategoryBean categoryBean : categoryBeans) {
                Map<String, Long> dataMap = new LinkedHashMap<>();
                for (String xField : groupData) {
                    dataMap.put(xField, 0L);
                }
                String categoryBeanKey = categoryBean.getKey();
                QueryBuilder categoryBuilder = new QueryBuilder();
                categoryBuilder.filterByTRSL(queryBuilder.asTRSL());
                categoryBuilder.setDatabase(queryBuilder.getDatabase());
                categoryBuilder.setPageSize(pageSize);

                if(StringUtil.isEmpty(xyTrsl) && contrastField.equals(FtsFieldConst.FIELD_GROUPNAME)){
                    String group = Const.SOURCE_GROUPNAME_CONTRAST.get(categoryBeanKey);
                    String categoryDatabase = TrslUtil.chooseDatabases(group);
                    categoryBuilder.setDatabase(categoryDatabase);
                }
                if(StringUtil.isNotEmpty(categoryBean.getValue())){
                    String value = categoryBean.getValue().toLowerCase().trim();
                    if(value.startsWith("not")){
                        categoryBuilder.filterByTRSL_NOT(categoryBean.getValue().substring(3,categoryBean.getValue().length()));
                    }else {
                        categoryBuilder.filterByTRSL(categoryBean.getValue());
                    }
                }
                GroupResult groupInfos = null;
                try {
                    groupInfos = commonListService.categoryQuery(categoryBuilder,sim,irSimflag,irSimflagAll,groupField,type);
                } catch (TRSSearchException e) {
                    throw new TRSSearchException(e);
                }
                if(groupInfos != null){
                    List<GroupInfo> groupList = groupInfos.getGroupList();
                    if (groupInfos.getGroupList() != null && groupInfos.getGroupList().size() > 0) {
                        for (GroupInfo groupInfo : groupList) {
                            if(dataMap.containsKey(groupInfo.getFieldValue())){
                                dataMap.put(groupInfo.getFieldValue(),groupInfo.getCount());
                            }
                        }
                    }
                }

                List<Long> oneData = new ArrayList<>();
                int i =0;
                int totaolSize = totalList.size();
                for(Map.Entry<String,Long> entry :dataMap.entrySet()){
                    oneData.add(entry.getValue());
                    Long total =entry.getValue();
                    if(totaolSize > 0){
                        total=total + (Long)totalList.get(i);
                        totalList.set(i , total);
                    }else{
                        totalList.add(total);
                    }
                    i++;
                }
                dataList.add(oneData);
                contrastList.add(categoryBeanKey);
            }
            List<Object> lineXList = new ArrayList<>();
            lineXList.addAll(groupData);
            result.put(resultKey.getLineXField(),lineXList);
            result.put(resultKey.getCountField(),dataList);
            result.put(resultKey.getContrastField(),contrastList);
            result.put("total",totalList);

            return result;
        } catch (TRSSearchException e) {
            throw new TRSSearchException(e);
        }
    }


}
