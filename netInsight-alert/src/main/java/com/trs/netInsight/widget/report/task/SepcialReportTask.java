package com.trs.netInsight.widget.report.task;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.controller.SpecialChartAnalyzeController;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.analysis.service.impl.SpecialChartAnalyzeService;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.service.impl.CommonChartServiceImpl;
import com.trs.netInsight.widget.common.service.impl.CommonListServiceImpl;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.report.util.SpecialReportUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.impl.Hybase8SearchImpl;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.report.constant.ReportConst;
import com.trs.netInsight.widget.report.entity.ReportDataNew;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.repository.ReportDataNewRepository;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.service.IInfoListService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;

/**
 * 专报
 *
 * @author 北京拓尔思信息技术股份有限公司
 * Created by shao.guangze on 2018年6月21日 下午9:11:31
 */
@Slf4j
public class SepcialReportTask implements Runnable {
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(30);

    private ChartAnalyzeService chartAnalyzeService = (ChartAnalyzeService) ObjectContainer.getBean(ChartAnalyzeService.class);

    private SpecialChartAnalyzeService specialChartAnalyzeService = (SpecialChartAnalyzeService) ObjectContainer.getBean(SpecialChartAnalyzeService.class);

    private IInfoListService infoListService = (IInfoListService) ObjectContainer.getBean(IInfoListService.class);

    private IDistrictInfoService districtInfoService = (IDistrictInfoService) ObjectContainer.getBean(IDistrictInfoService.class);

    private ICommonListService commonListService = (ICommonListService) ObjectContainer.getBean(ICommonListService.class);

    private ICommonChartService commonChartService = (ICommonChartService) ObjectContainer.getBean(CommonChartServiceImpl.class);

    private ReportDataNewRepository reportDataNewRepository = (ReportDataNewRepository) ObjectContainer.getBean(ReportDataNewRepository.class);

    private FullTextSearch hybase8SearchService = (Hybase8SearchImpl) ObjectContainer.getBean(Hybase8SearchImpl.class);

    private String keyWords;
    private String excludeWords;
    private Integer keyWordsIndex;
    private String excludeWebs;
    private String simflag;
    private String timeRange;
    private String trsl;
    private Integer searchType;
    private ReportDataNew reportData;
    private boolean server;
    private boolean weight;
    private String userId;
    private SpecialProject specialProject;

    public SepcialReportTask(boolean server, boolean weight, String keyWords,
                             String excludeWords, Integer keyWordsIndex, String excludeWebs,
                             String simflag, String timeRange, String trsl, Integer searchType, ReportDataNew reportData, String userId, SpecialProject specialProject) {
        this.keyWords = keyWords;
        this.excludeWords = excludeWords;
        this.keyWordsIndex = keyWordsIndex;
        this.excludeWebs = excludeWebs;
        this.simflag = simflag;
        this.timeRange = timeRange;
        this.trsl = trsl;
        this.searchType = searchType;
        this.reportData = reportData;
        this.server = server;
        this.weight = weight;
        this.userId = userId;
        this.specialProject = specialProject;
    }

    public void run() {

        long allSearchStartMillis;

        allSearchStartMillis = System.currentTimeMillis();
        log.info("专报查询开始：  " + allSearchStartMillis);
        String majorRangeTime = null;
        String[] majorTimeArray = new String[2];
        try {
            if (timeRange != null) {
                String[] timeArray = DateUtil.formatTimeRange(timeRange);
                if (timeArray != null && timeArray.length == 2) {
                    specialProject.setStart(timeArray[0]);
                    specialProject.setEnd(timeArray[1]);
                }
            }
            majorRangeTime = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
            majorRangeTime += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
            majorTimeArray = DateUtil.formatTimeRange(majorRangeTime);
            if (majorTimeArray != null && majorTimeArray.length == 2) {
                specialProject.setStart(majorTimeArray[0]);
                specialProject.setEnd(majorTimeArray[1]);
            }
        } catch (Exception e) {
            log.error("专题分析专报生成 时间格式转换失败", e);
        }
        final String rangeTime = majorRangeTime;
        final String[] timeArray = majorTimeArray;
        // 单一数据源排重
        boolean irSimflag = specialProject.isIrSimflag();
        //全网排重
        boolean irSimflagAll = specialProject.isIrSimflagAll();
        //站内排重
        boolean isSimilar = specialProject.isSimilar();

        String groName = specialProject.getSource();
        specialProject.setConditionScreen(true);
        QueryBuilder statBuilder = specialProject.toNoTimeBuilder(0, 10);
        int threadCount = CHAPTERS.size() + 1;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount);
        //TODO 专报容错处理，现计算阶段已经完成，大致思路已经清晰(见setEmptyData)，需要前端对stPreview和specialPreview两个接口做联调，listPreview测试，word处理。
        for (String chapter : CHAPTERS) {
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        long startMillis;
                        Object overviewOfDataResult = null;
                        long endMillis;
                        switch (chapter) {
                            case REPORTINTROkey:
                                break;
                            case OVERVIEWOFDATAkey:
                                log.info(String.format(SPECILAREPORTLOG, OVERVIEWOFDATA));
                                startMillis = System.currentTimeMillis();
                                IndexTab overviewOfDataIT = searchType == 0 ? createIndexTab(keyWords, excludeWords, keyWordsIndex, excludeWebs, simflag, timeRange, weight, REPORTCHARTDATASIZE) : createIndexTab(trsl, simflag, timeRange, weight, REPORTCHARTDATASIZE);
                                String groupName = "国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;Facebook;视频;短视频;自媒体号";
                                overviewOfDataIT.setGroupName(groupName);
                                overviewOfDataIT.setType(ColumnConst.CHART_PIE);
                                overviewOfDataIT.setContrast(ColumnConst.CONTRAST_TYPE_GROUP);
                                ReportResource overviewRR = new ReportResource();
                                try {
//                                    overviewOfDataResult = columnSearch(overviewOfDataIT, REPORTCHARTDATASIZE, groupName);
                                    QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
//                                    searchBuilder.setGroupName(specialProject.getSource());
                                    searchBuilder.setPageSize(20);
                                    ChartResultField resultField = new ChartResultField("name", "value");
                                    overviewOfDataResult = commonListService.queryListGroupNameStattotal(searchBuilder,specialProject.isSimilar(),specialProject.isIrSimflag(),specialProject.isIrSimflagAll(),specialProject.getSource(),"special",resultField);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, OVERVIEWOFDATA, (endMillis - startMillis)));
                                    overviewRR.setImgComment(ReportUtil.getOverviewOfData(JSON.toJSONString(overviewOfDataResult)));
                                    overviewRR.setImg_data(ReportUtil.getOverviewOfData(JSON.toJSONString(overviewOfDataResult)));
                                    reportData.setOverviewOfdata(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(overviewRR))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.SINGLERESOURCE, OVERVIEWOFDATAkey);
                                    log.error(OVERVIEWOFDATA, e);
                                }
                                reportDataNewRepository.saveOverviewOfdata(reportData.getOverviewOfdata(), reportData.getId());
                                break;
                            //态势评估
                            case SITUATIONACCESSMENTkey:
                                log.info(String.format(SPECILAREPORTLOG, SITUATIONACCESSMENT));
                                startMillis = System.currentTimeMillis();
                                try {
                                    QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
                                    searchBuilder.setGroupName(specialProject.getSource());
                                    Object object = specialChartAnalyzeService.getSituationAssessment(searchBuilder, specialProject);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, OVERVIEWOFDATA, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(object)) {
                                        ReportResource situationAccess = new ReportResource();
                                        situationAccess.setImg_data(object.toString());
                                        situationAccess.setImgType("gaugeChart");
                                        // 数据、图类型、图名字
                                        situationAccess.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(object), "gaugeChart", "态势评估"));
                                        situationAccess.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setSituationAccessment(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(situationAccess))));
                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, SITUATIONACCESSMENTkey);
                                    log.error(SITUATIONACCESSMENTkey, e);
                                }
                                reportDataNewRepository.saveSituationAccessment(reportData.getSituationAccessment(), reportData.getId());
                                break;
                            case DATATRENDANALYSISkey:
                                //各舆论场趋势分析
                                log.info(String.format(SPECILAREPORTLOG, DATATRENDANALYSIS));
                                startMillis = System.currentTimeMillis();

                                try {
                                    specialProject.setSource(groName);
                                    Object dayTrendResult = specialChartAnalyzeService.getWebCountLine(specialProject, rangeTime, "day");
                                    Object hourTrendResult = specialChartAnalyzeService.getWebCountLine(specialProject, rangeTime, "hour");
                                    if (ObjectUtil.isNotEmpty(dayTrendResult) || ObjectUtil.isNotEmpty(hourTrendResult)) {
                                        Map<String, Object> dataTrendResult = new HashMap<>();
                                        dataTrendResult.put("day", dayTrendResult);
                                        dataTrendResult.put("hour", hourTrendResult);
                                        ReportResource dataTrendRR = new ReportResource();
                                        dataTrendRR.setImg_data(JSON.toJSONString(dataTrendResult));
                                        dataTrendRR.setImgType("brokenLineChart");
                                        Object imgComment = dayTrendResult;
                                        if(DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "50") <= 1){
                                            imgComment = hourTrendResult;
                                        }
                                        // 数据、图类型、图名字
                                        dataTrendRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(imgComment), "brokenLineChart", "各舆论场趋势分析"));
                                        dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));

                                        reportData.setDataTrendAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                    }
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, DATATRENDANALYSIS, (endMillis - startMillis)));
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, DATATRENDANALYSISkey);
                                    log.error(DATATRENDANALYSIS, e);
                                }
                                reportDataNewRepository.saveDataTrendAnalysis(reportData.getDataTrendAnalysis(), reportData.getId());
                                break;
                            case DATASOURCEANALYSISkey:
                                //各舆论场发布统计
                                log.info(String.format(SPECILAREPORTLOG, DATASOURCEANALYSIS));
                                startMillis = System.currentTimeMillis();
                                try {
                                    QueryBuilder builder = specialProject.toNoPagedBuilder();
                                    String contrastField = FtsFieldConst.FIELD_GROUPNAME;
                                    builder.setPageSize(20);
                                    ChartResultField resultField = new ChartResultField("name", "value");
                                    List<Map<String, Object>> list = new ArrayList<>();
                                    list = (List<Map<String, Object>>) commonChartService.getPieColumnData(builder, isSimilar, irSimflag, irSimflagAll, CommonListChartUtil.changeGroupName(specialProject.getSource()), null, contrastField, "special", resultField);

                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, DATASOURCEANALYSIS, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(list)) {
                                        ReportResource dataSourceRR = new ReportResource();
                                        dataSourceRR.setImg_data(JSON.toJSONString(list));
                                        dataSourceRR.setImgType("pieChart");
                                        // 数据、图类型、图名字
                                        dataSourceRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(list), "pieChart", null));
                                        dataSourceRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setDataSourceAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataSourceRR))));
                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, DATASOURCEANALYSISkey);
                                    log.error(DATASOURCEANALYSIS, e);
                                }
                                reportDataNewRepository.saveDataSourceAnalysis(reportData.getDataSourceAnalysis(), reportData.getId());
                                break;
                            case OPINIONANALYSISkey:
                                //观点分析
                                log.info(String.format(SPECILAREPORTLOG, OPINIONANALYSISkey));
                                startMillis = System.currentTimeMillis();
                                try {
                                    //官方观点
                                    Object officialResult = specialChartAnalyzeService.getSentimentAnalysis(specialProject, rangeTime, "OFFICIAL_VIEW");
                                    //媒体观点
                                    Object mediaResult = specialChartAnalyzeService.getSentimentAnalysis(specialProject, rangeTime, "MEDIA_VIEW");
                                    //专家观点
                                    Object exportResult = specialChartAnalyzeService.getSentimentAnalysis(specialProject, rangeTime, "EXPORT_VIEW");
                                    //网民观点
                                    Object netizenResult = specialChartAnalyzeService.getSentimentAnalysis(specialProject, rangeTime, "NETIZEN_VIEW");
                                    if (ObjectUtil.isNotEmpty(officialResult) || ObjectUtil.isNotEmpty(mediaResult)
                                           || ObjectUtil.isNotEmpty(exportResult)|| ObjectUtil.isNotEmpty(netizenResult)) {

                                        Map<String, Object> opinionResult = new HashMap<>();
                                        opinionResult.put("OFFICIAL_VIEW", officialResult);
                                        opinionResult.put("MEDIA_VIEW", mediaResult);
                                        //opinionResult.put("EXPORT_VIEW", exportResult);
                                        opinionResult.put("NETIZEN_VIEW", netizenResult);
                                        endMillis = System.currentTimeMillis();
                                        log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, OPININOANALYSIS, (endMillis - startMillis)));

                                        ReportResource dataTrendRR = new ReportResource();
                                        dataTrendRR.setImg_data(JSON.toJSONString(opinionResult));
                                        dataTrendRR.setImgType("statisticBox");
                                        dataTrendRR.setImgComment("暂定！");
                                        dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));

                                        reportData.setOpinionAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, OPINIONANALYSISkey);
                                    log.error(OPININOANALYSIS, e);
                                }
                                reportDataNewRepository.saveOpinionAnalysis(StringUtil.filterEmoji(reportData.getOpinionAnalysis().replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "")), reportData.getId());
                                break;

                            case EMOTIONANALYSISkey:
                                //情感分析 饼图
                                log.info(String.format(SPECILAREPORTLOG, EMOTIONANALYSIS));
                                startMillis = System.currentTimeMillis();
                                try {
                                    QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
                                    searchBuilder.setGroupName(specialProject.getSource());
                                    List<Map<String, String>> list = specialChartAnalyzeService.emotionOption(searchBuilder, specialProject);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, EMOTIONANALYSIS, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(list)) {
                                        ReportResource emotionRR = new ReportResource();
                                        emotionRR.setImg_data(JSON.toJSONString(list));
                                        emotionRR.setImgType("emotionPieChart");
                                        // 数据、图类型、图名字
                                        emotionRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(list), "emotionPieChart", null));
                                        emotionRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setEmotionAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(emotionRR))));
                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, EMOTIONANALYSISkey);
                                    log.error(EMOTIONANALYSIS, e);
                                }
                                reportDataNewRepository.saveEmotionAnalysis(reportData.getEmotionAnalysis(), reportData.getId());
                                break;
                            case MOODSTATISTICSkey:
                                //情绪分析 饼图
                                log.info(String.format(SPECILAREPORTLOG, MOODSTATISTICS));
                                startMillis = System.currentTimeMillis();
                                try {
                                    Object list = specialChartAnalyzeService.getMoodStatistics(specialProject, rangeTime);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, MOODSTATISTICS, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(list)) {
                                        ReportResource moodRR = new ReportResource();
                                        moodRR.setImg_data(JSON.toJSONString(list));
                                        moodRR.setImgType("moodStatistics");
                                        // 数据、图类型、图名字
                                        moodRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(list), "moodStatistics", null));
                                        moodRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setMoodStatistics(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(moodRR))));
                                    }

                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, MOODSTATISTICSkey);
                                    log.error(MOODSTATISTICS, e);
                                }
                                reportDataNewRepository.saveMoodStatistics(reportData.getMoodStatistics(), reportData.getId());
                                break;

                            case WORDCLOUDSTATISTICSkey:
                                //词云统计
                                log.info(String.format(SPECILAREPORTLOG, WORDCLOUDSTATISTICS));
                                startMillis = System.currentTimeMillis();
                                try {
                                    QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
                                    searchBuilder.setPageSize(50);
                                    String[] timeArr = DateUtil.formatTimeRange(rangeTime);
                                    searchBuilder.filterField(ESFieldConst.IR_URLTIME, timeArr, Operator.Between);
                                    searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
                                    //通用
                                    Object keywordWordCloud = null;
                                    //人物
                                    Object peopleWordCloud = null;
                                    //地域
                                    Object locationWordCloud = null;
                                    //机构
                                    Object agencyWordCloud = null;
                                    String[] timeArr1 = com.trs.netInsight.util.DateUtil.formatTimeRange(rangeTime);
                                    String time0 = timeArr1[0];
                                    //keywords；人名:people；地名:location；机构名:agency
                                    if (!com.trs.netInsight.util.DateUtil.isExpire("2019-10-01 00:00:00", time0)) {
                                        ChartResultField resultField = new ChartResultField("name", "value", "entityType");
                                        keywordWordCloud = commonChartService.getWordCloudColumnData(searchBuilder, isSimilar, irSimflag, irSimflagAll, specialProject.getSource(), "keywords", "special", resultField);
                                        peopleWordCloud = commonChartService.getWordCloudColumnData(searchBuilder, isSimilar, irSimflag, irSimflagAll, specialProject.getSource(), "people", "special", resultField);
                                        locationWordCloud = commonChartService.getWordCloudColumnData(searchBuilder, isSimilar, irSimflag, irSimflagAll, specialProject.getSource(), "location", "special", resultField);
                                        agencyWordCloud = commonChartService.getWordCloudColumnData(searchBuilder, isSimilar, irSimflag, irSimflagAll, specialProject.getSource(), "agency", "special", resultField);
                                    } else {
                                        keywordWordCloud = specialChartAnalyzeService.getWordCloudNew(searchBuilder, isSimilar, irSimflag, irSimflagAll, "keywords", "special");
                                        peopleWordCloud = specialChartAnalyzeService.getWordCloudNew(searchBuilder, isSimilar, irSimflag, irSimflagAll, "people", "special");
                                        locationWordCloud = specialChartAnalyzeService.getWordCloudNew(searchBuilder, isSimilar, irSimflag, irSimflagAll, "location", "special");
                                        agencyWordCloud = specialChartAnalyzeService.getWordCloudNew(searchBuilder, isSimilar, irSimflag, irSimflagAll, "agency", "special");
                                    }
                                    if (ObjectUtil.isNotEmpty(peopleWordCloud) || ObjectUtil.isNotEmpty(locationWordCloud)
                                            || ObjectUtil.isNotEmpty(locationWordCloud ) || ObjectUtil.isNotEmpty(agencyWordCloud)) {
                                        Map<String, Object> wordCloudResult = new HashMap<>();
                                        wordCloudResult.put("people", peopleWordCloud);
                                        wordCloudResult.put("keywords", keywordWordCloud);
                                        wordCloudResult.put("location", locationWordCloud);
                                        wordCloudResult.put("agency", agencyWordCloud);
                                        endMillis = System.currentTimeMillis();
                                        log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, MOODSTATISTICS, (endMillis - startMillis)));
                                        ReportResource wordCloudRR = new ReportResource();
                                        wordCloudRR.setImg_data(JSON.toJSONString(wordCloudResult));
                                        wordCloudRR.setImgType("wordCloud");
                                        wordCloudRR.setImgComment("");
                                        wordCloudRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setWordCloudStatistics(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(wordCloudRR))));
                                    }

                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, WORDCLOUDSTATISTICSkey);
                                    log.error(WORDCLOUDSTATISTICS, e);
                                }
                                reportDataNewRepository.saveWordCloudStatistics(reportData.getWordCloudStatistics(), reportData.getId());
                                break;
                            case AREAkey:
                                //地图
                                log.info(String.format(SPECILAREPORTLOG, AREA));
                                startMillis = System.currentTimeMillis();
                                try {
                                    String groupNames = groName;//多个以;隔开
                                    if (groupNames.contains("微信") && !groupNames.contains("国内微信")) {
                                        groupNames = groupNames.replaceAll("微信", "国内微信");
                                    }
                                    groupNames = groupNames.replaceAll("境外网站", "国外新闻");
                                    String[] timeArr = DateUtil.formatTimeRange(rangeTime);
                                    if (timeArr != null && timeArr.length == 2) {
                                        specialProject.setStart(timeArr[0]);
                                        specialProject.setEnd(timeArr[1]);
                                    }
//                                    QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
                                    QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
                                    searchBuilder.setGroupName(groupNames);
//                                    List<Map<String, Object>> catalogResult = specialChartAnalyzeService.getAreaCount(searchBuilder, timeArr, isSimilar,
//                                            irSimflag, irSimflagAll, "catalogArea");
//                                    List<Map<String, Object>> mediaResult = specialChartAnalyzeService.getAreaCount(searchBuilder, timeArr, isSimilar,
//                                            irSimflag, irSimflagAll, "mediaArea");
                                    Object catalogResult = specialChartAnalyzeService.getAreaCount(searchBuilder, timeArr, isSimilar,
                                            irSimflag, irSimflagAll, "catalogArea");
                                    Object mediaResult = specialChartAnalyzeService.getAreaCount(searchBuilder, timeArr, isSimilar,
                                            irSimflag, irSimflagAll, "mediaArea");

                                    if (ObjectUtil.isNotEmpty(catalogResult) ||ObjectUtil.isNotEmpty(catalogResult)) {
//                                        catalogResult = MapUtil.sortByValue(catalogResult, "value");
//
//                                        mediaResult = MapUtil.sortByValue(mediaResult, "value");

                                        Map<String, Object> result = new HashMap<>();
                                        result.put("catalogArea", catalogResult);
                                        result.put("mediaArea", mediaResult);
                                        endMillis = System.currentTimeMillis();
                                        log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, AREA, (endMillis - startMillis)));

                                        ReportResource areaRR = new ReportResource();
//                                        areaRR.setImg_data(JSON.toJSONString(result));
                                        areaRR.setImg_data(JSON.toJSON(result).toString());
                                        areaRR.setImgType("mapChart");
                                        // 数据、图类型、图名字
                                        Map<String, Object> objectMap = (Map<String, Object>) mediaResult;
                                        List<Map<String, Object>> areaData = (List<Map<String, Object>>) objectMap.get("areaData");
                                        areaRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(areaData), "mapChart", null));
                                        areaRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setArea(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(areaRR))));
//                                        reportData.setArea(ReportUtil.replaceHtml(JSON.toJSON(areaRR).toString()));
                                    }

                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, AREAkey);
                                    log.error(AREA, e);
                                }
                                reportDataNewRepository.saveArea(reportData.getArea(), reportData.getId());
                                break;

                            case NEWSHOTTOP10key://（专题报 改造 20191121）
                                //列表，新闻热点TOP10
                                log.info(String.format(SPECILAREPORTLOG, NEWSHOTTOP10));
                                startMillis = System.currentTimeMillis();
                                try {
                                    String source = CommonListChartUtil.changeGroupName("新闻网站");
                                    specialProject.setSource(source);
                                    List<Map<String, Object>> result = specialChartAnalyzeService.getHotListMessage(source,
                                            specialProject, rangeTime, 8);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, NEWSHOTTOP10, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(result)) {
                                        reportData.setNewsHotTopics(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(result), NEWSHOTTOP10))));

                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, NEWSHOTTOP10key);
                                    log.error(NEWSHOTTOP10, e);
                                }
                                reportDataNewRepository.saveNewsHotTopics(StringUtil.filterEmoji(reportData.getNewsHotTopics()), reportData.getId());
                                break;
                            case WEIBOHOTTOP10key://（专题报 改造 20191121）
                                //列表，微博热点TOP10
                                log.info(String.format(SPECILAREPORTLOG, WEIBOHOTTOPICS));
                                startMillis = System.currentTimeMillis();
                                try {
                                    String source = CommonListChartUtil.changeGroupName("微博");
                                    List<Map<String, Object>> result = specialChartAnalyzeService.getHotListMessage(source,
                                            specialProject, rangeTime, 8);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEIBOHOTTOP10, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(result)) {
                                        reportData.setWeiboTop10(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(result), WEIBOHOTTOP10))));
                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WEIBOHOTTOP10key);
                                    log.error(WEIBOHOTTOP10, e);
                                }
                                reportDataNewRepository.saveWeiboTop10(StringUtil.filterEmoji(reportData.getWeiboTop10()), reportData.getId());
                                break;
                            case WECHATHOTTOP10key://（专题报 改造 20191121）
                                //列表，微信热点TOP10
                                log.info(String.format(SPECILAREPORTLOG, WECHATHOTTOP10));
                                startMillis = System.currentTimeMillis();
                                try {
                                    String source = CommonListChartUtil.changeGroupName("微信");
                                    List<Map<String, Object>> result = specialChartAnalyzeService.getHotListMessage(source,
                                            specialProject, rangeTime, 8);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WECHATHOTTOP10, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(result)) {
                                        reportData.setWechatHotTop10(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(result), WECHATHOTTOP10))));
                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WECHATHOTTOP10key);
                                    log.error(WECHATHOTTOP10, e);
                                }
                                reportDataNewRepository.saveWechatHotTop10(StringUtil.filterEmoji(reportData.getWechatHotTop10()), reportData.getId());
                                break;
                            case WEMEDIAkey:
                                //列表，自媒体报热点
                                log.info(String.format(SPECILAREPORTLOG, WEMEDIA));
                                startMillis = System.currentTimeMillis();
                                try {
                                    String source = CommonListChartUtil.changeGroupName("自媒体号");
                                    List<Map<String, Object>> result = specialChartAnalyzeService.getHotListMessage(source,
                                            specialProject, rangeTime, 8);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEMEDIA, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(result)) {
                                        reportData.setWeMediaHot(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(result), WEMEDIA))));

                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WEMEDIAkey);
                                    log.error(WEMEDIA, e);
                                }
                                reportDataNewRepository.saveWeMediaHot(StringUtil.filterEmoji(reportData.getWeMediaHot()), reportData.getId());
                                break;
                            case NEWSEVENTCONTEXTkey:
                                //列表，新闻网站事件脉络
                                log.info(String.format(SPECILAREPORTLOG, NEWSEVENTCONTEXT));
                                startMillis = System.currentTimeMillis();
                                try {
//                                    QueryBuilder queryFts = new QueryBuilder();
//                                    String trsl = statBuilder.asTRSL();
//                                    queryFts.filterByTRSL(trsl);
//                                    queryFts.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
//                                    queryFts.setPageSize(11);
//                                    queryFts.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                                    QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
                                    searchBuilder.setPageSize(11);
                                    InfoListResult infoListResult = null;
                                    infoListResult = commonListService.queryPageListForHot(searchBuilder,CommonListChartUtil.changeGroupName(Const.PAGE_SHOW_XINWEN),UserUtils.getUser(),"special",false);
                                    String trslkall = null;
                                    if (ObjectUtil.isNotEmpty(infoListResult) && ObjectUtil.isNotEmpty(infoListResult.getContent())) {
                                        trslkall = infoListResult.getTrslk();
                                        PagedList<FtsDocumentCommonVO> pagedList = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
                                        if (pagedList != null && pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0) {
                                            List<FtsDocumentCommonVO> voList = pagedList.getPageItems();
                                            SortListAll sortList = new SortListAll();
                                            //按时间排序
                                            Collections.sort(voList, sortList);
                                            for (FtsDocumentCommonVO ftsDocument : voList) {
                                                ftsDocument.setTrslk(trslkall);
                                                ftsDocument.setSimCount(ftsDocument.getSimCount() - 1 > 0 ? ftsDocument.getSimCount() - 1 : 0);
                                            }
                                            reportData.setNewsEventContext(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(voList), NEWSEVENTCONTEXT))));
                                        }
                                    }

                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, NEWSEVENTCONTEXTkey);
                                    log.error(NEWSEVENTCONTEXT, e);
                                }
                                reportDataNewRepository.saveNewsEventContex(StringUtil.filterEmoji(reportData.getNewsEventContext()), reportData.getId());
                                break;

                            case WEIBOEVENTCONTEXTkey:
                                //列表，微博事件脉络
                                log.info(String.format(SPECILAREPORTLOG, WEIBOEVENTCONTEXT));
                                startMillis = System.currentTimeMillis();
                                try {
                                    //找十个转发数量最多的原发 按时间排序
                                    QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
                                    searchBuilder.setPageSize(11);
                                    InfoListResult infoListResult = null;
                                    infoListResult = commonListService.queryPageListForHot(searchBuilder,CommonListChartUtil.changeGroupName(Const.GROUPNAME_WEIBO),UserUtils.getUser(),"special",false);
                                    String trslkall = null;
                                    if (ObjectUtil.isNotEmpty(infoListResult) && ObjectUtil.isNotEmpty(infoListResult.getContent())) {
                                        trslkall = infoListResult.getTrslk();
                                        PagedList<FtsDocumentCommonVO> pagedList = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
                                        if (pagedList != null && pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0) {
                                            List<FtsDocumentCommonVO> voList = pagedList.getPageItems();
                                            SortListAll sortList = new SortListAll();
                                            //按时间排序
                                            Collections.sort(voList, sortList);
                                            for (FtsDocumentCommonVO ftsDocument : voList) {
                                                ftsDocument.setTrslk(trslkall);
                                                ftsDocument.setSimCount(ftsDocument.getSimCount() - 1 > 0 ? ftsDocument.getSimCount() - 1 : 0);
                                            }
                                            reportData.setWeiboEventContext(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(voList), WEIBOEVENTCONTEXT))));
                                        }
                                    }
//                                    PagedList<FtsDocumentCommonVO> content = commonListService.queryPageListForHotNoFormat(statBuilder, "special", Const.GROUPNAME_WEIBO);
//                                    if (ObjectUtil.isNotEmpty(content)) {
//                                        List<FtsDocumentCommonVO> ftsQueryWeiBo = content.getPageItems();
//                                        SortListAll sortListWeiBo = new SortListAll();
//                                        //按时间排序
//                                        Collections.sort(ftsQueryWeiBo, sortListWeiBo);
//                                        // 防止这个的第一条和时间的那一条重复
//                                        //微博走势 不走special/chart/trendTime接口，不需要去掉第一条数据
//                                        for (FtsDocumentCommonVO ftsStatus : ftsQueryWeiBo) {
//                                            ftsStatus.setSiteName(ftsStatus.getScreenName());
//                                        }
//                                        endMillis = System.currentTimeMillis();
//                                        log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEIBOEVENTCONTEXT, (endMillis - startMillis)));
//                                        reportData.setWeiboEventContext(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(ftsQueryWeiBo), WEIBOEVENTCONTEXT))));
//
//                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WEIBOEVENTCONTEXTkey);
                                    log.error(WEIBOEVENTCONTEXT, e);
                                }
                                reportDataNewRepository.saveWeiboEventContex(StringUtil.filterEmoji(reportData.getWeiboEventContext()), reportData.getId());
                                break;

                            case WECHATEVENTCONTEXTkey:
                                //列表，微信事件脉络
                                log.info(String.format(SPECILAREPORTLOG, WECHATEVENTCONTEXT));
                                startMillis = System.currentTimeMillis();
                                try {
                                    QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
                                    searchBuilder.setPageSize(11);
                                    InfoListResult infoListResult = null;
                                    infoListResult = commonListService.queryPageListForHot(searchBuilder,CommonListChartUtil.changeGroupName(Const.GROUPNAME_WEIXIN),UserUtils.getUser(),"special",false);
                                    String trslkall = null;
                                    if (ObjectUtil.isNotEmpty(infoListResult) && ObjectUtil.isNotEmpty(infoListResult.getContent())) {
                                        trslkall = infoListResult.getTrslk();
                                        PagedList<FtsDocumentCommonVO> pagedList = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
                                        if (pagedList != null && pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0) {
                                            List<FtsDocumentCommonVO> voList = pagedList.getPageItems();
                                            SortListAll sortList = new SortListAll();
                                            //按时间排序
                                            Collections.sort(voList, sortList);
                                            for (FtsDocumentCommonVO ftsDocument : voList) {
                                                ftsDocument.setTrslk(trslkall);
                                                ftsDocument.setSimCount(ftsDocument.getSimCount() - 1 > 0 ? ftsDocument.getSimCount() - 1 : 0);
                                            }
                                            reportData.setWechatEventContext(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(voList), WECHATEVENTCONTEXT))));
                                        }
                                    }
//

                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WECHATEVENTCONTEXTkey);
                                    log.error(WECHATEVENTCONTEXT, e);
                                }
                                reportDataNewRepository.saveWechatEventContex(StringUtil.filterEmoji(reportData.getWechatEventContext()), reportData.getId());
                                break;

                            case WEMEDIAEVENTCONTEXTkey:
                                //列表，自媒体号事件脉络
                                log.info(String.format(SPECILAREPORTLOG, WEMEDIAEVENTCONTEXT));
                                startMillis = System.currentTimeMillis();
                                try {
                                    QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
                                    searchBuilder.setPageSize(11);
                                    InfoListResult infoListResult = null;
                                    infoListResult = commonListService.queryPageListForHot(searchBuilder,CommonListChartUtil.changeGroupName(Const.GROUPNAME_ZIMEITI),UserUtils.getUser(),"special",false);
                                    String trslkall = null;
                                    if (ObjectUtil.isNotEmpty(infoListResult) && ObjectUtil.isNotEmpty(infoListResult.getContent())) {
                                        trslkall = infoListResult.getTrslk();
                                        PagedList<FtsDocumentCommonVO> pagedList = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
                                        if (pagedList != null && pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0) {
                                            List<FtsDocumentCommonVO> voList = pagedList.getPageItems();
                                            SortListAll sortList = new SortListAll();
                                            //按时间排序
                                            Collections.sort(voList, sortList);
                                            for (FtsDocumentCommonVO ftsDocument : voList) {
                                                ftsDocument.setTrslk(trslkall);
                                                ftsDocument.setSimCount(ftsDocument.getSimCount() - 1 > 0 ? ftsDocument.getSimCount() - 1 : 0);
                                            }
                                            reportData.setWemediaEventContext(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(voList), WEMEDIAEVENTCONTEXT))));
                                        }
                                    }

                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WEMEDIAEVENTCONTEXTkey);
                                    log.error(WEMEDIAEVENTCONTEXT, e);
                                }
                                reportDataNewRepository.saveWemediaEventContex(StringUtil.filterEmoji(reportData.getWemediaEventContext()), reportData.getId());
                                break;

                            case ACTIVEACCOUNTkey:
                                //活跃账号 新
                                log.info(String.format(SPECILAREPORTLOG, ACTIVEACCOUNT));
                                startMillis = System.currentTimeMillis();
                                try {
                                    QueryBuilder builder = specialProject.toNoPagedBuilder();
                                    builder.setGroupName(groName);
                                    String[] range = DateUtil.formatTimeRange(rangeTime);
                                    Object mediaActiveAccount = specialChartAnalyzeService.mediaActiveAccount(builder, groName, range, isSimilar,
                                            irSimflag, irSimflagAll);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, AREA, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(mediaActiveAccount)) {
                                        ReportResource accountRR = new ReportResource();
                                        accountRR.setImg_data(JSON.toJSONString(mediaActiveAccount));
                                        accountRR.setImgType("activeAccount");
                                        // 数据、图类型、图名字

                                        accountRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(mediaActiveAccount), "activeAccount", null));
                                        accountRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setActiveAccount(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(accountRR)).replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "")));
                                    }

                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, ACTIVEACCOUNTkey);
                                    log.error(ACTIVEACCOUNT, e);
                                }
                                reportDataNewRepository.saveActiveAccount(StringUtil.filterEmoji(reportData.getActiveAccount()), reportData.getId());
                                break;
                            case PROPAFATIONANALYSISkey:
                                //传播分析
                                log.info(String.format(SPECILAREPORTLOG, PROPAFATIONANALYSIS));
                                startMillis = System.currentTimeMillis();
                                try {
                                    QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
                                    searchBuilder.setGroupName(groName);
                                    Object spreadAnalysisSiteName = specialChartAnalyzeService.spreadAnalysisSiteName(searchBuilder);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, PROPAFATIONANALYSIS, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(spreadAnalysisSiteName )) {
                                        ReportResource spreadAnalysisRR = new ReportResource();
                                        spreadAnalysisRR.setImg_data(JSON.toJSONString(spreadAnalysisSiteName));
                                        spreadAnalysisRR.setImgType("newsSiteAnalysisSiteName");
                                        spreadAnalysisRR.setImgComment("暂定！");
                                        spreadAnalysisRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setSpreadAnalysisSiteName(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(spreadAnalysisRR)).replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "")));

                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, PROPAFATIONANALYSISkey);
                                    log.error(PROPAFATIONANALYSIS, e);
                                }
                                reportDataNewRepository.saveSpreadAnalysisSiteName(StringUtil.filterEmoji(reportData.getSpreadAnalysisSiteName()), reportData.getId());
                                break;
                            case NEWSPROPAFATIONANALYSISTIMELISTkey:
                                //新闻传播分析时间轴
                                log.info(String.format(SPECILAREPORTLOG, NEWSPROPAFATIONANALYSISTIMELIST));
                                startMillis = System.currentTimeMillis();
                                try {
                                    // 根据时间升序,只要第一条
                                    QueryBuilder searchBuilder = specialProject.toBuilder(0, 1, true);
                                    searchBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                                    searchBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, false);
                                    Object newsTimeList = specialChartAnalyzeService.spreadAnalysis(searchBuilder, timeArray, isSimilar, irSimflag, irSimflagAll, false, Const.PAGE_SHOW_XINWEN);

                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, NEWSPROPAFATIONANALYSISTIMELIST, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(newsTimeList)) {
                                        ReportResource newsTimeListRR = new ReportResource();
                                        newsTimeListRR.setImg_data(JSON.toJSONString(newsTimeList));
                                        newsTimeListRR.setImgType("newsSiteAnalysis");
                                        newsTimeListRR.setImgComment("暂定！");
                                        newsTimeListRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setNewsSpreadAnalysisTimeList(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(newsTimeListRR)).replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "")));

                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, NEWSPROPAFATIONANALYSISTIMELISTkey);
                                    log.error(NEWSPROPAFATIONANALYSISTIMELIST, e);
                                }
                                reportDataNewRepository.saveNewsSpreadAnalysisTimeList(StringUtil.filterEmoji(reportData.getNewsSpreadAnalysisTimeList()), reportData.getId());
                                break;
                            case WEMEDIAPROPAFATIONANALYSISTIMELISTkey:
                                //自媒体传播分析时间轴
                                log.info(String.format(SPECILAREPORTLOG, WEMEDIAPROPAFATIONANALYSISTIMELIST));
                                startMillis = System.currentTimeMillis();
                                try {
                                    // 根据时间升序,只要第一条
                                    QueryBuilder searchBuilder = specialProject.toBuilder(0, 1, true);
                                    searchBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                                    searchBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, false);
                                    Object wemediaTimeList = specialChartAnalyzeService.spreadAnalysis(searchBuilder, timeArray, isSimilar, irSimflag, irSimflagAll, false, Const.PAGE_SHOW_ZIMEITI);

                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEMEDIAPROPAFATIONANALYSISTIMELIST, (endMillis - startMillis)));
                                    if (ObjectUtil.isNotEmpty(wemediaTimeList)) {
                                        ReportResource wemediaTimeListRR = new ReportResource();
                                        wemediaTimeListRR.setImg_data(JSON.toJSONString(wemediaTimeList));
                                        wemediaTimeListRR.setImgType("newsSiteAnalysis");
                                        wemediaTimeListRR.setImgComment("暂定！");
                                        wemediaTimeListRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setWemediaSpreadAnalysisTimeList(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(wemediaTimeListRR)).replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "")));

                                    }
                                } catch (Exception e) {
                                    //ReportUtil.setEmptyData(reportData, ReportConst.CHART, WEMEDIAPROPAFATIONANALYSISTIMELISTkey);
                                    log.error(WEMEDIAPROPAFATIONANALYSISTIMELIST, e);
                                }
                                reportDataNewRepository.saveWemediaSpreadAnalysisTimeList(StringUtil.filterEmoji(reportData.getWemediaSpreadAnalysisTimeList()), reportData.getId());
                                break;
                            default:
                                break;
                        }

                    } finally {
                        try {
                            cyclicBarrier.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        long allSearchEndMillis = System.currentTimeMillis();
        log.info("专报查询结束：  " + allSearchEndMillis);
        log.info("专报查询耗时：  " + (allSearchEndMillis - allSearchStartMillis));
        try {
            cyclicBarrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        reportData.setDoneFlag(1);
        reportDataNewRepository.saveDoneFlag(1, reportData.getId());
    }

    private void groupResult2MapList(GroupResult groupResult, List<Map<String, Object>> mapList, String groupName) throws Exception {
        if (groupResult != null && groupResult.getGroupList() != null && groupResult.getGroupList().size() > 0) {
            List<GroupInfo> groupList = groupResult.getGroupList();
            //根据Uid再去查SCREEN_NAME
            for (GroupInfo groupInfo : groupList) {
                if ("微博".equals(groupName)) {
                    String uidTrsl = "IR_UID:(?)";
                    uidTrsl = uidTrsl.replace("?", groupInfo.getFieldValue());
                    QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
                    queryBuilder.setDatabase(new String[]{Const.WEIBO});
                    queryBuilder.setAppendTRSL(new java.lang.StringBuilder().append(uidTrsl));
                    PagedList<FtsDocumentCommonVO> queryList = hybase8SearchService.pageListCommon(queryBuilder, false, false, false, null);
                    groupInfo.setFieldValue(queryList.getPageItems().get(0).getScreenName());
                    queryBuilder.setAppendTRSL(new java.lang.StringBuilder().append(uidTrsl));
                }
                Map<String, Object> putValue = MapUtil.putValue(new String[]{"groupName", "group", "num"},
                        groupInfo.getFieldValue(), groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
                mapList.add(putValue);
            }
        }
    }

    private QueryBuilder createFilter(String keyWords, String keyWordindex, String excludeWords, String excludeWebs, boolean weight) {

        QueryBuilder queryBuilder = WordSpacingUtil.handleKeyWords(keyWords, keyWordindex, weight);
        //拼接排除词
        if (keyWordindex.trim().equals("1")) {// 标题加正文
            if (StringUtil.isNotEmpty(excludeWords)) {
                StringBuilder exbuilder = new StringBuilder();
                exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
                        .append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
                queryBuilder.filterByTRSL(exbuilder.toString());
                StringBuilder exbuilder2 = new StringBuilder();
                exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
                        .append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
                ;
                queryBuilder.filterByTRSL(exbuilder2.toString());
            }
        } else {//仅标题
            if (StringUtil.isNotEmpty(excludeWords)) {
                StringBuilder exbuilder = new StringBuilder();
                exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
                        .append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
                queryBuilder.filterByTRSL(exbuilder.toString());
            }
        }
        //排除网站
        if (StringUtil.isNotEmpty(excludeWebs)) {
            queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, excludeWebs.replaceAll(";|；", " OR "), Operator.NotEqual);
        }
        return queryBuilder;
    }

    private IndexTab createIndexTab(String keyWords,
                                    String excludeWords, Integer keyWordsIndex, String excludeWebs,
                                    String simflag, String timeRange, boolean weight, Integer maxSize) {
//        IndexTab indexTab=new IndexTab(name,specialType, trsl, xyTrsl, type,contrast, tradition, excludeWeb,monitorSite, parentId,typeId, sequence,
//                maxSize, timeRange, hide, statusTrsl, weChatTrsl, keyWord,
//                excludeWords,excludeWordIndex, keyWordIndex, xyKeyWord, xyKeyWordIndex, groupName,similar,irSimflag,irSimflagAll,weight,sort,tabWidth,oneName,notSids,
//                mediaLevel, mediaIndustry, contentIndustry, filterInfo, preciseFilter, contentArea, mediaArea);
        IndexTab indexTab = new IndexTab();
        indexTab.setKeyWord(keyWords);
        indexTab.setKeyWordIndex(keyWordsIndex.toString());// 0 或 1
        indexTab.setExcludeWords(excludeWords);
        indexTab.setExcludeWeb(excludeWebs);
        indexTab.setTimeRange(timeRange);
        indexTab.setHide(false);
        indexTab.setMaxSize(maxSize);
        indexTab.setWeight(weight);
        setIndexTabSimilar(indexTab, simflag);
        return indexTab;
    }

    private void setIndexTabSimilar(IndexTab indexTab, String simflag) {
        if ("no".equals(simflag)) {
            //不排重
            indexTab.setIrSimflagAll(false);
            indexTab.setIrSimflag(false);
            indexTab.setSimilar(false);
        } else if ("netRemove".equals(simflag)) {
            //全网排重，现改名为单一网站排重
            indexTab.setIrSimflagAll(false);
            indexTab.setIrSimflag(false);
            indexTab.setSimilar(true);
        } else if ("urlRemove".equals(simflag)) {
            //url排重，现改名为站内排重
            indexTab.setIrSimflagAll(false);
            indexTab.setIrSimflag(true);
            indexTab.setSimilar(false);
        } else if ("sourceRemove".equals(simflag)) {
            //跨数据源排重，现改名为全网排重
            indexTab.setIrSimflagAll(true);
            indexTab.setIrSimflag(false);
            indexTab.setSimilar(false);

        }
    }

    /**
     * 高级模式模拟生成indexTab
     *
     * @since changjiang @ 2018年6月14日
     */
    private IndexTab createIndexTab(String trsl, String simflag, String timeRange, boolean weightm, Integer maxSize) {
        IndexTab indexTab = new IndexTab();
        indexTab.setTrsl(trsl);
        setIndexTabSimilar(indexTab, simflag);
        indexTab.setTimeRange(timeRange);
        indexTab.setHide(false);
        indexTab.setMaxSize(maxSize);
        indexTab.setWeight(weight);
        return indexTab;
    }


    /**
     * 根据类型生成对应表达式
     *
     * @since changjiang @ 2018年6月14日
     */
    private String generateActiveTrsl(String key, String trsl, String timeRange) throws OperationException {

        String[] timeArray = DateUtil.formatTimeRange(timeRange);
        StringBuilder trslSb = new StringBuilder(trsl);
        String[] databases = null;
        if (key.equals(WEIBOACTIVETOP10key)) {
            trslSb.append(" AND (IR_GROUPNAME:(微博)) ");
            databases = TrslUtil.chooseDatabases("微博".split(";"));

        } else if (key.equals(WECHATACTIVETOP10key)) {
            trslSb.append(" AND (IR_GROUPNAME:(国内微信)) ");
            databases = TrslUtil.chooseDatabases("国内微信".split(";"));
        }
        trslSb.append(" AND (IR_URLTIME:[ " + timeArray[0] + " TO " + timeArray[1] + "])");

        return trslSb.toString();
    }

    private Object columnSearch(IndexTab indexTab, Integer maxSize, String groupName) throws OperationException {
        AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
        ColumnConfig config = new ColumnConfig();
        config.setMaxSize(maxSize);
        //TODO read preciseFilter 为空字符串 两个字段之间 传 tab.get 对应字段
        config.initSection(indexTab, indexTab.getTimeRange(), 0, maxSize, null, null, "keywords", "", "", "desc",
                "", "", "", "", "", indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(), indexTab.getContentArea(),
                indexTab.getMediaArea(), indexTab.getPreciseFilter(), "");
        column.setDistrictInfoService(districtInfoService);
        column.setCommonChartService(commonChartService);
        column.setCommonListService(commonListService);
        column.setConfig(config);
        return column.getColumnData(indexTab.getTimeRange());
    }

    private String generateActiveTrsl(String key,
                                      String timeRange, String keyWords, String excludeWords, String excludeWebs, Integer keyWordsIndex, boolean weight) throws OperationException {

        String[] timeArray = DateUtil.formatTimeRange(timeRange);
        StringBuffer trsl = new StringBuffer();
        String[] databases = null;
        if (key.equals(WEIBOACTIVETOP10key)) {
            String trslTemp = createFilter(keyWords, "0", excludeWords, excludeWebs, weight).asTRSL();
            if (StringUtil.isEmpty(trslTemp)) {
                trsl.append("(IR_GROUPNAME:(微博)");
            } else {
                trsl.append(trslTemp.substring(0, trslTemp.length() - 1));//把最后1个 ) 去掉
                trsl.append(" AND (IR_GROUPNAME:(微博)) ");
            }
            databases = TrslUtil.chooseDatabases("微博".split(";"));
        } else if (key.equals(WECHATACTIVETOP10key)) {
            String trslTemp = createFilter(keyWords, keyWordsIndex.toString(), excludeWords, excludeWebs, weight).asTRSL();
            if (StringUtil.isEmpty(trslTemp)) {
                trsl.append("(IR_GROUPNAME:(国内微信)");
            } else {
                trsl.append(trslTemp.substring(0, trslTemp.length() - 1));//把最后1个 ) 去掉
                trsl.append(" AND (IR_GROUPNAME:(国内微信)) ");
            }
            databases = TrslUtil.chooseDatabases("微博".split(";"));
        }
        trsl.append(" AND (IR_URLTIME:[ " + timeArray[0] + " TO " + timeArray[1] + "])");
        trsl.append(")");//把最后1个 ) 加上


        return trsl.toString();
    }


}
