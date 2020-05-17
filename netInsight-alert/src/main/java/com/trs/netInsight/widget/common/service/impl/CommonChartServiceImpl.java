package com.trs.netInsight.widget.common.service.impl;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.model.result.*;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.MapUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
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
        QueryBuilder queryBuilder = CommonListChartUtil.formatQueryBuilder(builder);
        Set<String> sourceList = CommonListChartUtil.formatGroupName(groupName);
        String[] groupArray = sourceList.toArray(new String[sourceList.size()]);
        String groupTrsl = "(" + StringUtil.join(groupArray, " OR ") + ")";
        queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupTrsl, Operator.Equal);
        String[] database = TrslUtil.chooseDatabases(groupArray);
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
                specialBuilder.setDatabase(StringUtil.join(database, ";"));

                Long count = commonListService.ftsCount(specialBuilder, sim, irSimflag, irSimflagAll, type);
                groupResult.addGroup(categoryBean.getKey(), count);
            }
        } else {
            QueryBuilder ordinaryBuilder = new QueryBuilder();
            ordinaryBuilder.filterByTRSL(trsl);
            ordinaryBuilder.setPageSize(queryBuilder.getPageSize());
            ordinaryBuilder.setDatabase(StringUtil.join(database, ";"));
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
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getBarColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName,
                                                              String xyTrsl, String contrastField, String type) throws TRSException {
        if (StringUtil.isEmpty(xyTrsl)) {
            if(StringUtil.isEmpty(contrastField)){
                throw new OperationException("柱状图普通模式下没有对比字段");
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        if (StringUtil.isNotEmpty(groupName)) {
            Set<String> sourceList = CommonListChartUtil.formatGroupName(groupName);
            if (sourceList.size() > 0) {
                try {
                    //用统一方法进行统计
                    GroupResult groupInfos = this.getCategoryQueryData(builder, sim, irSimflag, irSimflagAll, groupName, xyTrsl, contrastField, type);
                    //专家模式
                    //直接统计每个对比类型的数据
                    if (StringUtil.isNotEmpty(xyTrsl)) {

                        if (groupInfos != null && groupInfos.getGroupList().size() > 0) {
                            List<GroupInfo> groupList = groupInfos.getGroupList();
                            for (GroupInfo groupInfo : groupList) {
                                Map<String, Object> putValue = MapUtil.putValue(new String[]{"groupName", "group", "num"},
                                        groupInfo.getFieldValue(), groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
                                list.add(putValue);
                            }
                        }
                    } else {
                        if (groupInfos != null && groupInfos.getGroupList().size() > 0) {
                            List<GroupInfo> groupList = groupInfos.getGroupList();
                            if (contrastField.equals(FtsFieldConst.FIELD_GROUPNAME)) {
                                for (String group : sourceList) {
                                    Map<String, Object> putValue = new HashMap<>();
                                    String pageGroupName = Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(group);
                                    putValue.put("groupName", pageGroupName);
                                    putValue.put("group", pageGroupName);
                                    for (GroupInfo groupInfo : groupList) {
                                        if (group.equals(groupInfo.getFieldValue())) {
                                            putValue.put("num", groupInfo.getCount());
                                            break;
                                        } else {
                                            putValue.put("num", 0);
                                        }
                                    }
                                    list.add(putValue);
                                }
                            } else {
                                for (GroupInfo groupInfo : groupList) {
                                    Map<String, Object> putValue = MapUtil.putValue(new String[]{"groupName", "group", "num"},
                                            groupInfo.getFieldValue(), groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
                                    list.add(putValue);
                                }
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
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getMapColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName,
                                                             String contrastField, String type) throws TRSException {

        if (StringUtil.isEmpty(contrastField)) {
            throw new OperationException("没有准确填写对比字段");
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (StringUtil.isNotEmpty(groupName)) {
            try {
                //地图部分专家或者普通，因为没有分类对比表达式
                GroupResult categoryInfos = this.getCategoryQueryData(builder, sim, irSimflag, irSimflagAll, groupName,  "", contrastField, type);
                Map<String, List<String>> areaMap = districtInfoService.allAreas();
                for (Map.Entry<String, List<String>> entry : areaMap.entrySet()) {
                    Map<String, Object> reMap = new HashMap<String, Object>();
                    int num = 0;
                    // 查询结果之间相互对比 所以把城市放开也不耽误查询速度
                    for (GroupInfo classEntry : categoryInfos) {
                        String area = classEntry.getFieldValue();
                        if (area.contains(";")) {
                            continue;
                        }
                        //因为该查询字段形式类似数组，文章命中访问的是这个字段中的每个值的个数，例如一条数据的这个字段的值为：中国\北京市\朝阳区;中国\北京市\海淀区
                        //按注释方法算 - 这样同一条数据北京市被计算2次，因为朝阳与海淀都是北京下属地域，2019-12该字段修改为在上面基础上增加当前条所属市，为：中国\北京市\朝阳区;中国\北京市\海淀区;中国\北京市
                        //如果继续计算下属市则北京被计算3次，所以只计算到省，则需要数据库中改字段的值定义不变，为：中国\北京市
                        String[] areaArr = area.split("\\\\");
                        if (areaArr.length == 2) {
                            if (areaArr[1].contains(entry.getKey())) {
                                num += classEntry.getCount();
                            }
                        }

                    }
                    reMap.put("areaName", entry.getKey());
                    reMap.put("areaCount", num);
                    list.add(reMap);
                }
            } catch (Exception e) {
                throw new TRSSearchException(e);
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
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getPieColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName, String xyTrsl, String contrastField, String type) throws TRSException {

        if (StringUtil.isEmpty(xyTrsl)) {
            if(StringUtil.isEmpty(contrastField)){
                throw new OperationException("柱状图普通模式下没有对比字段");
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        if (StringUtil.isNotEmpty(groupName)) {
            Set<String> sourceList = CommonListChartUtil.formatGroupName(groupName);
            if (sourceList.size() > 0) {
                try {
                    //用统一方法进行统计
                    GroupResult groupInfos = this.getCategoryQueryData(builder, sim, irSimflag, irSimflagAll, groupName, xyTrsl, contrastField, type);
                    //专家模式
                    //直接统计每个对比类型的数据
                    if (StringUtil.isNotEmpty(xyTrsl)) {
                        if (groupInfos != null && groupInfos.getGroupList().size() > 0) {
                            List<GroupInfo> groupList = groupInfos.getGroupList();
                            for (GroupInfo groupInfo : groupList) {
                                Map<String, Object> putValue = MapUtil.putValue(new String[]{"groupName", "group", "num"},
                                        groupInfo.getFieldValue(), groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
                                list.add(putValue);
                            }
                        }
                    } else {
                        if (groupInfos != null && groupInfos.getGroupList().size() > 0) {
                            List<GroupInfo> groupList = groupInfos.getGroupList();
                            if (contrastField.equals(FtsFieldConst.FIELD_GROUPNAME)) {
                                for (String group : sourceList) {
                                    Map<String, Object> putValue = new HashMap<>();
                                    String pageGroupName = Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(group);
                                    putValue.put("groupName", pageGroupName);
                                    putValue.put("group", pageGroupName);
                                    for (GroupInfo groupInfo : groupList) {
                                        if (group.equals(groupInfo.getFieldValue())) {
                                            putValue.put("num", groupInfo.getCount());
                                            break;
                                        } else {
                                            putValue.put("num", 0);
                                        }
                                    }
                                    list.add(putValue);
                                }
                            } else {
                                for (GroupInfo groupInfo : groupList) {
                                    Map<String, Object> putValue = MapUtil.putValue(new String[]{"groupName", "group", "num"},
                                            groupInfo.getFieldValue(), groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
                                    list.add(putValue);
                                }
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
                                                             String groupName, String entityType, String type) throws TRSException {
        try {
            GroupWordResult wordInfos = new GroupWordResult();
            QueryBuilder queryBuilder = CommonListChartUtil.formatQueryBuilder(builder);

            Set<String> sourceList = CommonListChartUtil.formatGroupName(groupName);
            String[] groupArray = sourceList.toArray(new String[sourceList.size()]);
            String groupTrsl = "(" + StringUtil.join(groupArray, " OR ") + ")";
            queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupTrsl, Operator.Equal);
            String[] database = TrslUtil.chooseDatabases(groupArray);
            queryBuilder.setDatabase(StringUtil.join(database, ";"));


            if ("keywords".equals(entityType)) {
                // 人物、地域、机构
                GroupResult people = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, Const.PARAM_MAPPING.get("people"), type);
                GroupResult location = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, Const.PARAM_MAPPING.get("location"), type);
                GroupResult agency = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, Const.PARAM_MAPPING.get("agency"), type);
                List<GroupInfo> peopleList = people.getGroupList();
                List<GroupInfo> locationList = location.getGroupList();
                List<GroupInfo> agencyList = agency.getGroupList();

                for (GroupInfo groupInfo : peopleList) {
                    wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), "people");
                }

                for (GroupInfo groupInfo : locationList) {
                    wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), "location");
                }

                for (GroupInfo groupInfo : agencyList) {
                    wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), "agency");
                }

            } else {
                GroupResult result = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, Const.PARAM_MAPPING.get(entityType), type);
                List<GroupInfo> groupList = result.getGroupList();
                for (GroupInfo groupInfo : groupList) {
                    wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), entityType);
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
                wordInfos.setGroupList(newGroupWordList);
                return wordInfos;
            } else {
                return null;
            }
        } catch (TRSSearchException e) {
            throw new TRSSearchException(e);
        }
    }


    //折线图






    //热点列表

    /**
     * 热点信息查询
     * @param builder  查询构造器  需要拼接好要查询数据源，需要将可查询数据库放入对应字段
     * @param source 要查询的数据源类型，用;分割
     * @param pageSize  查询条数
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getHotListColumnData(T builder,String source,Integer pageSize, String type) throws TRSException {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            QueryBuilder queryBuilder = CommonListChartUtil.formatQueryBuilder(builder);
            Set<String> sourceList = CommonListChartUtil.formatGroupName(source);
            String[] groupArray = sourceList.toArray(new String[sourceList.size()]);
            String groupTrsl = "(" + StringUtil.join(groupArray, " OR ") + ")";
            queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupTrsl, Operator.Equal);
            String[] database = TrslUtil.chooseDatabases(groupArray);
            queryBuilder.setDatabase(StringUtil.join(database,";"));
            if (database == null) {
                queryBuilder.setDatabase(Const.MIX_DATABASE);
            }

            queryBuilder.setPageSize(pageSize);
            String uid = UUID.randomUUID().toString();
            RedisUtil.setString(uid, queryBuilder.asTRSL());
            queryBuilder.setKeyRedis(uid);
            PagedList<FtsDocumentCommonVO> pagedList = commonListService.queryPageListForHotBase(queryBuilder, type);
            Map<String, Object> map = null;
            if (pagedList == null || pagedList.getPageItems() == null || pagedList.getPageItems().size() == 0) {
                return list;
            }
            List<FtsDocumentCommonVO> voList = pagedList.getPageItems();
            for (FtsDocumentCommonVO vo : voList) {
                map = new HashMap<>();
                String groupName = vo.getGroupName();
                if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {

                }
                map.put("sid", vo.getSid());
                map.put("title", StringUtil.replacePartOfHtml(vo.getTitle()));
                if ("微博".equals(groupName)) {
                    map.put("siteName", vo.getScreenName());
                } else {
                    map.put("siteName", vo.getSiteName());
                }
                map.put("urlName", vo.getUrlName());
                map.put("hkey", vo.getHkey());
                map.put("simCount", String.valueOf(vo.getSimCount()));
                map.put("trslk", uid);
                map.put("groupName", groupName);
                map.put("nreserved1", vo.getNreserved1());
                map.put("catalogArea", vo.getCatalogArea());
                map.put("location", vo.getLocation());
                // 获得时间差
                Map<String, String> timeDifference = DateUtil.timeDifference(vo);
                boolean isNew = false;
                if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
                    isNew = true;
                    map.put("timeAgo", timeDifference.get("timeAgo"));
                } else {
                    map.put("timeAgo", timeDifference.get("urlTime"));
                }
                map.put("isNew", isNew);
                map.put("md5Tag", vo.getMd5Tag());
                map.put("urlTime", vo.getUrlTime());
                list.add(map);

            }
        } catch (Exception e) {
            throw new TRSSearchException("HotColumn error:" + e);
        }
        return list;

    }


    //列表数据源统计

}
