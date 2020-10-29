package com.trs.netInsight.widget.special.service.impl;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.column.entity.IndexTabType;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.column.repository.CustomChartRepository;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.entity.SpecialCustomChart;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.special.entity.repository.SpecialCustomChartRepository;
import com.trs.netInsight.widget.special.service.ISpecialCustomChartService;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Slf4j
@Transactional
public class SpecialCustomChartServiceImpl implements ISpecialCustomChartService {
    @Autowired
    private SpecialCustomChartRepository specialCustomChartRepository;
    @Autowired
    private ICommonChartService commonChartService;
    @Autowired
    private ICommonListService commonListService;
    @Autowired
    private CustomChartRepository customChartRepository;

    @Override
    public Object getCustomChart(String id,int pageNo,int pageSize) {
        if (StringUtil.isEmpty(id)) {
            return null;
        }
        Sort sort = new Sort(Sort.Direction.ASC, "sequence");
        Pageable pageable = new PageRequest(pageNo,pageSize,sort);
        Page<SpecialCustomChart> chartPage = specialCustomChartRepository.findByParentId(id, pageable);
        List<Object> result = null;
        if (chartPage != null &&chartPage.getContent() != null && chartPage.getContent().size() > 0) {
            result = new ArrayList<>();
            for (SpecialCustomChart chart : chartPage.getContent()) {
                if(chart.isWeight()&&chart.getSort()==null){
                    chart.setSort("hittitle");
                }
                if(!chart.isWeight()&&chart.getSort()==null){
                    chart.setSort("desc");
                }
                Map<String, Object> chartMap = new HashMap<>();
                chartMap.put("id", chart.getId());
                chartMap.put("name", chart.getName());
                chartMap.put("specialType", chart.getSpecialType());
                chartMap.put("chartType", chart.getType());
                chartMap.put("chartPage", ChartPageInfo.CustomChart);
                chartMap.put("isTop", chart.getIsTop());
                chartMap.put("sequence", chart.getSequence());
                chartMap.put("contrast", chart.getContrast());
                chartMap.put("groupName", CommonListChartUtil.formatPageShowGroupName(chart.getGroupName()));
                chartMap.put("keyWord", chart.getKeyWord());
                chartMap.put("keyWordIndex", chart.getKeyWordIndex());
                chartMap.put("weight", chart.isWeight());
                chartMap.put("sort",chart.getSort());
                chartMap.put("excludeWords", chart.getExcludeWords());
                chartMap.put("excludeWordsIndex", chart.getExcludeWordsIndex());
                chartMap.put("excludeWeb", chart.getExcludeWeb());
                chartMap.put("monitorSite", chart.getMonitorSite());
                //排重方式 不排 no，单一媒体排重 netRemove,站内排重 urlRemove,全网排重 sourceRemove
                if (chart.isSimilar()) {
                    chartMap.put("simflag", "netRemove");
                } else if (chart.isIrSimflag()) {
                    chartMap.put("simflag", "urlRemove");
                } else if (chart.isIrSimflagAll()) {
                    chartMap.put("simflag", "sourceRemove");
                } else {
                    chartMap.put("simflag", "no");
                }
                chartMap.put("tabWidth", chart.getTabWidth());
                chartMap.put("timeRange", chart.getTimeRange());
                chartMap.put("trsl", chart.getTrsl());
                chartMap.put("xyTrsl", chart.getXyTrsl());

                chartMap.put("mediaLevel", chart.getMediaLevel());
                chartMap.put("mediaIndustry", chart.getMediaIndustry());
                chartMap.put("contentIndustry", chart.getContentIndustry());
                chartMap.put("filterInfo", chart.getFilterInfo());
                chartMap.put("contentArea", chart.getContentArea());
                chartMap.put("mediaArea", chart.getMediaArea());
                chartMap.put("pointToId", chart.getParentId());
                result.add(chartMap);
            }
            return new PagedList<Object>(result,pageNo,
                    pageSize, (int)chartPage.getTotalElements());
        }
        return result;
    }

    @Override
    public Integer getCustomChartSize(String id) {
        Integer seq = 0;
        if (StringUtil.isEmpty(id)) {
            return seq;
        }
        Sort sort = new Sort(Sort.Direction.ASC, "sequence");
        List<SpecialCustomChart> chartList = specialCustomChartRepository.findByParentId(id, sort);
        if (chartList == null || chartList.size() == 0) {
            return seq;
        }
        return chartList.size();
    }

    @Override
    public Integer getMaxChartSequence(String id) {
        Integer seq = 0;
        if (StringUtil.isEmpty(id)) {
            return seq;
        }
        Sort sort = new Sort(Sort.Direction.ASC, "sequence");
        List<SpecialCustomChart> chartList = specialCustomChartRepository.findByParentId(id, sort);
        if (chartList == null || chartList.size() == 0) {
            return seq;
        }
        for (SpecialCustomChart chart : chartList) {
            if (chart.getSequence() > seq) {
                seq = chart.getSequence();
            }
        }
        return seq;
    }

    @Override
    public SpecialCustomChart findOneSpecialCustomChart(String id) {
        return specialCustomChartRepository.findOne(id);
    }

    @Override
    @Transactional
    public SpecialCustomChart saveSpecialCustomChart(SpecialCustomChart specialCustomChart) {
        return specialCustomChartRepository.save(specialCustomChart);
    }

    @Override
    @Transactional
    public void deleteSpecialCustomChart(String id) {
        specialCustomChartRepository.delete(id);
    }

    @Override
    public Integer deleteCustomChart(String id) {
        Integer deleteCount = 0;
        List<SpecialCustomChart> customChartList = specialCustomChartRepository.findByParentId(id);
        if(customChartList != null && customChartList.size() >0){
            specialCustomChartRepository.delete(customChartList);
            deleteCount += customChartList.size();
        }

        return deleteCount;
    }

    @Override
    public Object selectChar2ListtData(SpecialCustomChart customChart,String source,String key,String dateTime,String entityType,String mapContrast,String sort,int pageNo,int pageSize,
                                       String forwardPrimary,String invitationCard,String fuzzyValue,String fuzzyValueScope) throws TRSException {
        InfoListResult infoListResult = null;
        try {
            QueryBuilder queryBuilder =customChart.getQueryBuilder(false,pageNo,pageSize);
            IndexTabType indexTabType = ColumnFactory.chooseType(customChart.getType());
            String timeRange = customChart.getTimeRange();
            SpecialType specialType = customChart.getSpecialType();
            if (IndexTabType.WORD_CLOUD.equals(indexTabType)) {
                if ("location".equals(entityType) && !"省".equals(key.substring(key.length() - 1)) && !key.contains("自治区")) {
                    String irKeywordNew = "";
                    if ("市".equals(key.substring(key.length() - 1))) {
                        irKeywordNew = key.replace("市", "");
                    } else {
                        irKeywordNew = key;
                    }
                    if (!irKeywordNew.contains("\"")) {
                        irKeywordNew = "\"" + irKeywordNew + "\"";
                    }
                    String trsl = FtsFieldConst.FIELD_URLTITLE + ":(" + irKeywordNew + ") OR " + FtsFieldConst.FIELD_CONTENT + ":(" + irKeywordNew + ")";
                    queryBuilder.filterByTRSL(trsl);
                } else {
                    queryBuilder.filterField(Const.PARAM_MAPPING.get(entityType), key, Operator.Equal);
                }

            } else if (IndexTabType.MAP.equals(indexTabType)) {
                String contrastField = FtsFieldConst.FIELD_CATALOG_AREA;
                if(StringUtil.isNotEmpty(mapContrast) && mapContrast.equals(ColumnConst.CONTRAST_TYPE_MEDIA_AREA)){
                    contrastField = FtsFieldConst.FIELD_MEDIA_AREA;
                }
                Map<String,String> areaMap = null;
                if(FtsFieldConst.FIELD_MEDIA_AREA.equals(contrastField)){
                    areaMap = Const.MEDIA_PROVINCE_NAME;
                }else if(FtsFieldConst.FIELD_CATALOG_AREA.equals(contrastField)){
                    areaMap = Const.CONTTENT_PROVINCE_NAME;
                }
                queryBuilder.filterByTRSL(contrastField + ":(" + areaMap.get(key) +")");

            }else if (IndexTabType.CHART_LINE.equals(indexTabType) || IndexTabType.CHART_PIE.equals(indexTabType) || IndexTabType.CHART_BAR.equals(indexTabType)) {
                if(IndexTabType.CHART_LINE.equals(indexTabType)){
                    String dateEndTime = "";
                    if(dateTime.length() ==10 &&  DateUtil.isTimeFormatterYMD(dateTime)){
                        dateTime = dateTime.replaceAll("/","-");
                        dateTime = dateTime+" 00:00:00";
                        dateEndTime = dateTime.substring(0,dateTime.length()-9)+" 23:59:59";
                    }else if(dateTime.length() ==16 && DateUtil.isTimeFormatterYMDH(dateTime)){
                        dateTime = dateTime.replaceAll("/","-");
                        dateTime = dateTime+":00";
                        dateEndTime = dateTime.substring(0,dateTime.length()-6)+":59:59";
                    }
                    timeRange = dateTime+";"+dateEndTime;
                }

                if (SpecialType.COMMON.equals(specialType)) {
                    if (ColumnConst.CONTRAST_TYPE_GROUP.equals(customChart.getContrast())) {// 舆论来源对比
                        if("全部".equals(key)){
                            key = "ALL";
                        }
                        source = key;
                        /*key = StringUtils.join(CommonListChartUtil.formatGroupName(key), ";");
                        queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, key, Operator.Equal);*/
                    }else if (ColumnConst.CONTRAST_TYPE_SITE.equals(customChart.getContrast())) {//站点对比
                        queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, key, Operator.Equal);
                    }else if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(customChart.getContrast())) {//微信公众号对比
                        queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, key, Operator.Equal);
                    } else {
                        //除去专家模式，柱状图只有三种模式，+两种特殊的图，如果不是这5种，则无对比模式
                        throw new TRSSearchException("未获取到检索条件");
                    }
                }else{
                    List<CategoryBean> mediaType = CommonListChartUtil.getMediaType(customChart.getXyTrsl());
                    for (CategoryBean categoryBean : mediaType) {
                        if (key.equals(categoryBean.getKey())) {
                            if (StringUtil.isNotEmpty(categoryBean.getValue())) {
                                String value = categoryBean.getValue().toLowerCase().trim();
                                if (value.startsWith("not")) {
                                    queryBuilder.filterByTRSL_NOT(categoryBean.getValue().substring(3, categoryBean.getValue().length()));
                                } else {
                                    queryBuilder.filterByTRSL(categoryBean.getValue());
                                }
                            }
                            break;
                        }
                    }
                }
            }else if ( !IndexTabType.LIST_NO_SIM.equals(indexTabType) && !IndexTabType.HOT_LIST.equals(indexTabType)) {//判断到这则是上面的都不符合，所以再不符合这俩，就代表都不对了
                throw new TRSSearchException("未获取到检索条件");
            }
            queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);

            if("ALL".equals(source)){
                source = customChart.getGroupName();
            }
            List<String> searchSourceList = CommonListChartUtil.formatGroupName(source);
            //只在原转发和主回帖筛选时添加要查询数据源，因为底层方法通过参数中的groupName去添加了对应的数据源和数据库信息
            if ((searchSourceList.contains(Const.GROUPNAME_LUNTAN) && StringUtil.isNotEmpty(invitationCard)) ||
                    (searchSourceList.contains(Const.GROUPNAME_WEIBO) && StringUtil.isNotEmpty(forwardPrimary) )) {
                StringBuffer sb = new StringBuffer();
                if (searchSourceList.contains(Const.GROUPNAME_LUNTAN) && StringUtil.isNotEmpty(invitationCard)) {
                    sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_LUNTAN + ")");
                    if ("0".equals(invitationCard)) {// 主贴
                        sb.append(" AND (").append(Const.NRESERVED1_LUNTAN).append(")");
                    } else if ("1".equals(invitationCard)) {// 回帖
                        sb.append(" AND (").append(FtsFieldConst.FIELD_NRESERVED1).append(":(1)").append(")");
                    }

                    sb.append(")");
                    searchSourceList.remove(Const.GROUPNAME_LUNTAN);
                }
                if (searchSourceList.contains(Const.GROUPNAME_WEIBO) && StringUtil.isNotEmpty(forwardPrimary) ) {
                    if (sb.length() > 0) {
                        sb.append(" OR ");
                    }
                    sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_WEIBO + ")");
                    if ("primary".equals(forwardPrimary)) {
                        // 原发
                        sb.append(" AND ").append(Const.PRIMARY_WEIBO);
                    } else if ("forward".equals(forwardPrimary)) {
                        //转发
                        sb.append(" NOT ").append(Const.PRIMARY_WEIBO);
                    }

                    sb.append(")");
                    searchSourceList.remove(Const.GROUPNAME_WEIBO);
                }
                if (searchSourceList.size() > 0) {
                    if (sb.length() > 0) {
                        sb.append(" OR ");
                    }
                    sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME).append(":(").append(StringUtils.join(searchSourceList, " OR ")).append("))");
                }
                queryBuilder.filterByTRSL(sb.toString());
            }

            // 结果中搜索
            if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {//在结果中搜索,范围为全文的时候
                String[] split = fuzzyValue.split(",");
                String splitNode = "";
                for (int i = 0; i < split.length; i++) {
                    if (StringUtil.isNotEmpty(split[i])) {
                        splitNode += split[i] + ",";
                    }
                }
                fuzzyValue = splitNode.substring(0, splitNode.length() - 1);
                if (fuzzyValue.endsWith(";") || fuzzyValue.endsWith(",") || fuzzyValue.endsWith("；")
                        || fuzzyValue.endsWith("，")) {
                    fuzzyValue = fuzzyValue.substring(0, fuzzyValue.length() - 1);

                }
                StringBuilder fuzzyBuilder = new StringBuilder();
                String hybaseField = "fullText";
                switch (fuzzyValueScope) {
                    case "title":
                        hybaseField = FtsFieldConst.FIELD_URLTITLE;
                        break;
                    case "source":
                        hybaseField = FtsFieldConst.FIELD_SITENAME;
                        break;
                    case "author":
                        hybaseField = FtsFieldConst.FIELD_AUTHORS;
                        break;
                }
                if ("fullText".equals(hybaseField)) {
                    fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+", "\") AND (\"")
                            .replaceAll("[;|；]+", "\" OR \"")).append("\"))").append(" OR " + FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+", "\") AND \"")
                            .replaceAll("[;|；]+", "\" OR \"")).append("\"))");
                } else {
                    fuzzyBuilder.append(hybaseField).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+", "\") AND (\"")
                            .replaceAll("[;|；]+", "\" OR \"")).append("\"))");
                }
                queryBuilder.filterByTRSL(fuzzyBuilder.toString());
            }
            log.info("综合：" + queryBuilder.asTRSL());
            User user = UserUtils.getUser();
            String type = "special";

            Boolean sim = customChart.isSimilar();
            Boolean	irSimflag = customChart.isIrSimflag();
            Boolean irSimflagAll = customChart.isIrSimflagAll();

            if ("hot".equals(sort)) {
                infoListResult = commonListService.queryPageListForHot(queryBuilder, source, user, type, true);
            } else {
                switch (sort) { // 排序
                    case "desc":
                        queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
                        break;
                    case "asc":
                        queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                        break;
                    case "relevance":// 相关性排序
                        queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
                        break;
                    default:
                        if (customChart.isWeight()) {
                            queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
                        } else {
                            queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
                        }
                        break;
                }
                infoListResult = commonListService.queryPageList(queryBuilder, sim, irSimflag, irSimflagAll, source, type, user, true);
            }
            if (infoListResult != null) {
                String trslk = infoListResult.getTrslk();
                if (infoListResult.getContent() != null) {
                    PagedList<Object> resultContent = CommonListChartUtil.formatListData(infoListResult,trslk,customChart.getKeyWordIndex());
                    infoListResult.setContent(resultContent);
                }
            }
            return infoListResult;

        } catch (TRSException | TRSSearchException e) {
            throw new TRSSearchException(e);
        }

    }

        @Override
    public Object selectChartData(SpecialCustomChart customChart, String timeRange, String showType, String entityType, String contrast) throws TRSException {
        if (StringUtil.isNotEmpty(timeRange)) {
            customChart.setTimeRange(timeRange);
        }
        QueryBuilder commonBuilder = customChart.getQueryBuilder(true,-1,-1);
        Object result = null;
        IndexTabType indexTabType = ColumnFactory.chooseType(customChart.getType());
        if (IndexTabType.LIST_NO_SIM.equals(indexTabType) || IndexTabType.HOT_LIST.equals(indexTabType)) {
            result = getListData(customChart, commonBuilder);
        } else if (IndexTabType.WORD_CLOUD.equals(indexTabType)) {
            result = getWordCloudData(customChart, commonBuilder, entityType);
        } else if (IndexTabType.MAP.equals(indexTabType)) {
            result = getMapData(customChart, commonBuilder, contrast);
        } else if (IndexTabType.CHART_LINE.equals(indexTabType)) {
            result = getChartLineData(customChart, commonBuilder, showType);
        } else if (IndexTabType.CHART_PIE.equals(indexTabType) || IndexTabType.CHART_BAR.equals(indexTabType)) {
            result = getBarPieData(customChart, commonBuilder);
        }
        return result;
    }

    private Object getBarPieData(SpecialCustomChart customChart, QueryBuilder queryBuilder) throws TRSSearchException {
        Object result = null;
        try {
            boolean irSimflag = customChart.isIrSimflag();
            boolean sim = customChart.isSimilar();
            boolean irSimflagAll = customChart.isIrSimflagAll();
            String groupName = customChart.getGroupName();
            queryBuilder.setPageSize(20);
            ChartResultField resultField = new ChartResultField("name", "value");
            SpecialType specialType = customChart.getSpecialType();
            if (SpecialType.COMMON.equals(specialType)) {
                String contrastField = FtsFieldConst.FIELD_GROUPNAME;
                // 站点对比图
                if (ColumnConst.CONTRAST_TYPE_SITE.equals(customChart.getContrast())) {
                    contrastField = FtsFieldConst.FIELD_SITENAME;
                    queryBuilder.setPageSize(10);
                }

                //微信公众号对比
                if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(customChart.getContrast())) {
                    contrastField = FtsFieldConst.FIELD_SITENAME;
                    queryBuilder.setPageSize(10);
                }
                result = commonChartService.getBarColumnData(queryBuilder, sim, irSimflag, irSimflagAll, groupName, null, contrastField, "special", resultField);

            } else {
                result = commonChartService.getBarColumnData(queryBuilder, sim, irSimflag, irSimflagAll, groupName, customChart.getXyTrsl(), null, "special", resultField);
            }
            return result;
        } catch (TRSException | TRSSearchException e) {
            throw new TRSSearchException(e);
        }
    }

    private Object getChartLineData(SpecialCustomChart customChart, QueryBuilder queryBuilder, String showType) throws TRSSearchException {
        try {
            String trsl = queryBuilder.asTRSL();
            boolean irSimflag = customChart.isIrSimflag();
            boolean sim = customChart.isSimilar();
            boolean irSimflagAll = customChart.isIrSimflagAll();
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> singleMap = new HashMap<>();
            Map<String, Object> doubleMap = new HashMap<>();

            List<String> dateList = new ArrayList<>();
            List<Object> contrastList = new ArrayList<>();
            List<List<Long>> countList = new ArrayList<>();
            List<Object> doubleContrastList = new ArrayList<>();
            List<List<Long>> doubleCountList = new ArrayList<>();
            List<Object> totalList = new ArrayList<>();
            Map<String, Object> maxSourceMap = new HashMap<>();
            List<Long> maxSourceList = new ArrayList<>();


            String source = customChart.getGroupName();
            List<String> contrastData = new ArrayList<>();
            String contrast = customChart.getContrast();
            String xyTrsl = null;
            String contrastField = FtsFieldConst.FIELD_GROUPNAME;
            SpecialType specialType = customChart.getSpecialType();
            if (SpecialType.COMMON.equals(specialType)) {
                if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_GROUP)) {
                    List<String> sourceList = CommonListChartUtil.formatGroupName(source);
                    List<String> allList = Const.ALL_GROUPNAME_SORT;
                    for (String oneGroupName : allList) {
                        //只显示选择的数据源
                        if (sourceList.contains(oneGroupName)) {
                            contrastData.add(oneGroupName);
                        }
                    }
                } else if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_SITE)) {
                    contrastField = FtsFieldConst.FIELD_SITENAME;
                } else if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_WECHAT)) {
                    contrastField = FtsFieldConst.FIELD_SITENAME;
                }
            } else {
                xyTrsl = customChart.getXyTrsl();
                contrastField = null;
            }

		/*
		时间格式展示判断
				按小时展示时，最多显示7天的
		小于24小时 - 小时
		 大于24小时且小于48小时
		 		通栏 - 小时
		 		半栏 -天
		 其他
		 	天
		 */
            String[] timeArray = null;
            try {
                timeArray = DateUtil.formatTimeRangeMinus1(customChart.getTimeRange());//修改时间格式 时间戳
            } catch (OperationException e) {
                throw new TRSSearchException(e);
            }

            List<String[]> list_time = new ArrayList<>();
            String groupBy = FtsFieldConst.FIELD_URLTIME;
            //格式化时间，一个String[] 数据对应的是一次查询对应的时间范围  如果按天查，则list_time只有一个值，如果为按小时查询，则有多少天，list_time就有多长，一个元素为当天的起止时间
            //最后list_time 会进行裁剪，因为按小时查询最多查询7天，时间太长了页面无法显示
            if ("hour".equals(showType)) {
                groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
                list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]);
            } else if ("day".equals(showType)) {
                list_time.add(timeArray);
            } else {
                if (DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], String.valueOf(customChart.getTabWidth())) <= 1) {
                    showType = "hour";
                    groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
                    list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]);
                } else {
                    showType = "day";
                    list_time.add(timeArray);
                }
            }

            if (list_time.size() > 8) {
                list_time = list_time.subList(list_time.size() - 8, list_time.size());
            }
            ChartResultField resultField = new ChartResultField("groupName", "count", "date");
            for (String[] times : list_time) {
                // 获取开始时间和结束时间之间的小时
                //date = DateUtil.getHourOfHH(arrays[0], arrays[1]);
                //这个是按小时分一天之内的时间（开始结束时间都在一天之内），返回结果是带有当天日期的
                //DateUtil.getStartToEndOfHour(arrays[0], arrays[1]);//将时间分组，按天分，早晚
                //获取两个时间之间的所有时间字符串，第一个时间格式为传入的时间格式，第二个为返回的时间格式
                // DateUtil.getBetweenDateString(TimeArray()[0], TimeArray()[1], DateUtil.yyyyMMddHHmmss,DateUtil.yyyyMMdd4);
                String queryTrsl = trsl;
                List<String> groupDate = new ArrayList<>(); // 对比hybase查询结果的key值得时间格式
                List<String> showDate = new ArrayList<>();// 页面展示的时间格式

                //如果按小时展示，需要对表达式中的时间进行替换
                if ("hour".equals(showType)) {
                    int n = queryTrsl.indexOf("URLTIME");
                    if (n != -1) {
                        String timeTrsl = queryTrsl.substring(n + 9, n + 41);//替换查询条件
                        queryTrsl = queryTrsl.replace(timeTrsl, times[0] + " TO " + times[1]);
                    }

                    groupDate = DateUtil.getHourOfHH(times[0], times[1]);
                    showDate = DateUtil.getStartToEndOfHour(times[0], times[1]);

                } else {
                    groupDate = DateUtil.getBetweenDateString(times[0], times[1], DateUtil.yyyyMMddHHmmss, DateUtil.yyyyMMdd4);
                    showDate = groupDate;
                }
                //查询结果的返回时间是与hybase的查询结果一致的时间格式，不是前端页面展示的时间格式
                dateList.addAll(showDate);

                QueryBuilder builder = new QueryBuilder();
                builder.filterByTRSL(queryTrsl);
                Map<String, List<Object>> oneTimeResult = (Map<String, List<Object>>) commonChartService.getChartLineColumnData(builder, sim, irSimflag, irSimflagAll, source, "column", xyTrsl, contrastField, contrastData, groupBy, groupDate, resultField);
                if (contrastList.size() == 0) {
                    List<Object> oneTimeContrast = oneTimeResult.get(resultField.getContrastField());
                    contrastData.clear();
                    oneTimeContrast.stream().forEach(oneContrast -> contrastData.add((String) oneContrast));
                    contrastList.addAll(contrastData);
                }
                List<Object> oneTimeTotal = oneTimeResult.get("total");
                oneTimeTotal.stream().forEach(onetotal -> totalList.add(onetotal));
                List<Object> oneTimeCount = oneTimeResult.get(resultField.getCountField());
                //将查到的结果放入要展示的结果中
                if (countList.size() == 0) {
                    for (Object countOne : oneTimeCount) {
                        countList.add((List<Long>) countOne);
                    }
                } else {
                    for (int i = 0; i < countList.size(); i++) {
                        List<Long> countOne = (List<Long>) oneTimeCount.get(i);
                        countList.get(i).addAll(countOne);
                    }
                }
            }

            Long countTotal = 0L;
            for (Object num : totalList) {
                countTotal = countTotal + (Long) num;
            }
            //判断图上有没有点，如果没有点，则直接返回null，不画图
            if (countTotal == 0L) {
                return null;
            }

            int maxIndex = 0;
            Long maxSourceTotal = 0L;
            String maxSourceName = "";
            for (int i = 0; i < countList.size(); i++) {
                Long oneTotal = 0L;
                List<Long> oneCount = countList.get(i);
                for (Long one : oneCount) {
                    oneTotal += one;
                }
                if (oneTotal > maxSourceTotal) {
                    maxSourceTotal = oneTotal;
                    maxIndex = i;
                }
            }
            maxSourceName = (String) contrastList.get(maxIndex);
            maxSourceList = countList.get(maxIndex);
            for (int i = 0; i < contrastList.size(); i++) {
                if (maxIndex != i) {
                    doubleContrastList.add(contrastList.get(i));
                    doubleCountList.add(countList.get(i));
                }
            }
            //网察折线图统一的返回结果
            doubleMap.put("legendData", doubleContrastList);
            doubleMap.put("lineXdata", dateList);
            doubleMap.put("lineYdata", doubleCountList);
            doubleMap.put("total", totalList);
            maxSourceMap.put("name", maxSourceName);
            maxSourceMap.put("info", maxSourceList);
            doubleMap.put("maxSource", maxSourceMap);
            result.put("double", doubleMap);

            contrastList.add(0, "总量");
            List<Long> totalLong = new ArrayList<>();
            for (Object one : totalList) {
                totalLong.add((Long) one);
            }
            countList.add(0, totalLong);
            //网察折线图统一的返回结果
            singleMap.put("legendData", contrastList);
            singleMap.put("lineXdata", dateList);
            singleMap.put("lineYdata", countList);
            result.put("single", singleMap);

            return result;
        } catch (TRSException | TRSSearchException e) {
            throw new TRSSearchException(e);
        }
    }

    private Object getMapData(SpecialCustomChart customChart, QueryBuilder queryBuilder, String contrast) throws TRSSearchException {
        try {
            boolean irSimflag = customChart.isIrSimflag();
            boolean sim = customChart.isSimilar();
            boolean irSimflagAll = customChart.isIrSimflagAll();
            String groupName = customChart.getGroupName();
            List<Map<String, Object>> list = new ArrayList<>();
            queryBuilder.setPageSize(Integer.MAX_VALUE);
            ChartResultField resultField = new ChartResultField("name", "value");
            list = (List<Map<String, Object>>) commonChartService.getMapColumnData(queryBuilder, sim, irSimflag, irSimflagAll, groupName, FtsFieldConst.FIELD_CATALOG_AREA, "special", resultField);
            if (list == null) {
                return null;
            }
            if (list != null && list.size() > 0) {
                Collections.sort(list, (o1, o2) -> {
                    Integer seq1 = (Integer) o1.get("value");
                    Integer seq2 = (Integer) o2.get("value");
                    return seq2.compareTo(seq1);
                });
            }
            return list;
        } catch (TRSException | TRSSearchException e) {
            throw new TRSSearchException(e);
        }
    }

    private Object getWordCloudData(SpecialCustomChart customChart, QueryBuilder queryBuilder, String entityType) throws TRSSearchException {
        try {
            boolean irSimflag = customChart.isIrSimflag();
            boolean sim = customChart.isSimilar();
            boolean irSimflagAll = customChart.isIrSimflagAll();
            String groupName = customChart.getGroupName();
            Integer pagesize = 50;
            if ("100".equals(String.valueOf(customChart.getTabWidth()))) {
                pagesize = 200;
            }
            queryBuilder.page(0, pagesize);
            ChartResultField resultField = new ChartResultField("name", "value","entityType");
            Object wordCloud =  commonChartService.getWordCloudColumnData(queryBuilder, sim, irSimflag, irSimflagAll, groupName, entityType, "special",resultField);
            return wordCloud;
        } catch (TRSException | TRSSearchException e) {
            throw new TRSSearchException(e);
        }
    }

    private Object getListData(SpecialCustomChart customChart, QueryBuilder queryBuilder) throws TRSSearchException {
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map = null;

            queryBuilder.page(0, 10);
            String uid = UUID.randomUUID().toString();
            RedisUtil.setString(uid, queryBuilder.asTRSL());
            queryBuilder.setKeyRedis(uid);
            PagedList<FtsDocumentCommonVO> pagedList = null;

            boolean irSimflag = customChart.isIrSimflag();
            boolean sim = customChart.isSimilar();
            boolean irSimflagAll = customChart.isIrSimflagAll();
            String source = customChart.getGroupName();
            IndexTabType indexTabType = ColumnFactory.chooseType(customChart.getType());
            if (IndexTabType.LIST_NO_SIM.equals(indexTabType)) {
                pagedList = commonListService.queryPageListNoFormat(queryBuilder, sim, irSimflag, irSimflagAll, "special", source);
            } else if (IndexTabType.HOT_LIST.equals(indexTabType)) {
                pagedList = commonListService.queryPageListForHotNoFormat(queryBuilder, "special", source);

            }
            if (pagedList == null || pagedList.getPageItems() == null || pagedList.getPageItems().size() == 0) {
                return null;
            }
            List<FtsDocumentCommonVO> voList = pagedList.getPageItems();
            for (FtsDocumentCommonVO vo : voList) {
                map = new HashMap<>();
                map.put("trslk", uid);
                String groupName = CommonListChartUtil.formatPageShowGroupName(vo.getGroupName());
                map.put("id", vo.getSid());
                if (Const.PAGE_SHOW_WEIXIN.equals(groupName)) {
                    map.put("id", vo.getHkey());
                }
                map.put("groupName", groupName);
                map.put("time", vo.getUrlTime());
                map.put("md5", vo.getMd5Tag());
                String title = vo.getTitle();
                if (StringUtil.isNotEmpty(title)) {
                    title = StringUtil.replacePartOfHtml(StringUtil.cutContentByFont(StringUtil.replaceImg(title), Const.CONTENT_LENGTH));
                }
                map.put("title", title);
                String content = "";
                if (StringUtil.isNotEmpty(vo.getContent())) {
                    content = StringUtil.cutContentByFont(StringUtil.replaceImg(vo.getContent()), Const.CONTENT_LENGTH);
                }
                if (StringUtil.isNotEmpty(vo.getAbstracts())) {
                    vo.setAbstracts(StringUtil.cutContentByFont(StringUtil.replaceImg(vo.getAbstracts()), Const.CONTENT_LENGTH));
                }
                if("1".equals(customChart.getKeyWordIndex())){
                    //摘要
                    map.put("abstracts", vo.getContent());
                }else{
                    //摘要
                    map.put("abstracts", vo.getAbstracts());
                }

                map.put("nreserved1", null);
                map.put("hkey", null);
                if (Const.PAGE_SHOW_LUNTAN.equals(groupName)) {
                    map.put("nreserved1", vo.getNreserved1());
                    map.put("hkey", vo.getHkey());
                }
                map.put("urlName", vo.getUrlName());
                map.put("siteName", vo.getSiteName());
                map.put("author", vo.getAuthors());
                //微博、Facebook、Twitter、短视频等没有标题，应该用正文当标题
                if (Const.PAGE_SHOW_WEIBO.equals(groupName)) {
                    String setContent = vo.getContent();
                    String ocrContent = vo.getOcrContent();
                    if (StringUtil.isNotEmpty(ocrContent)) {
                        ocrContent = StringUtil.replaceImg(ocrContent);
                        setContent = StringUtil.cutContentByFont(ocrContent, Const.CONTENT_LENGTH);
                    }
                    map.put("title", StringUtil.replaceImg(setContent));
                    map.put("abstracts", StringUtil.replaceImg(setContent));

                    map.put("siteName", vo.getScreenName());
                } else if (Const.PAGE_SHOW_FACEBOOK.equals(groupName) || Const.PAGE_SHOW_TWITTER.equals(groupName)) {
                    map.put("title", content);
                    map.put("abstracts", content);
                    map.put("siteName", vo.getAuthors());
                } else if (Const.PAGE_SHOW_DUANSHIPIN.equals(groupName) || Const.PAGE_SHOW_CHANGSHIPIN.equals(groupName)) {
                    if(StringUtil.isEmpty(title)){
                        map.put("title", vo.getContent());
                    }
                    map.put("abstracts", content);
                    map.put("siteName", vo.getAuthors());
                }
                map.put("commtCount", vo.getCommtCount());
                map.put("rttCount", vo.getRttCount());
                long simNum = vo.getSimCount() == 0 ? 0:vo.getSimCount()-1;
                map.put("simNum", String.valueOf(simNum));
                // 获得时间差,三天内显示时间差,剩下消失urltime
                Map<String, String> timeDifference = DateUtil.timeDifference(vo);
                boolean isNew = false;
                if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
                    isNew = true;
                    map.put("timeAgo", timeDifference.get("timeAgo"));
                } else {
                    map.put("timeAgo", timeDifference.get("urlTime"));
                }
                map.put("isNew", isNew);
                list.add(map);
            }

            return list;
        } catch (TRSException | TRSSearchException e) {
            throw new TRSSearchException(e);
        }
    }


}
