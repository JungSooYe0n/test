package com.trs.netInsight.widget.report.task;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.trs.netInsight.config.constant.ESFieldConst;
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
        majorRangeTime = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
        majorRangeTime += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
        String[] majorTimeArray = new String[2];
        try {
            majorTimeArray = DateUtil.formatTimeRange(majorRangeTime);
            if (majorTimeArray != null && majorTimeArray.length == 2) {
                specialProject.setStart(majorTimeArray[0]);
                specialProject.setEnd(majorTimeArray[1]);
            }
        } catch (Exception e) {
            log.error("专题分析专报生成 时间格式转换失败", e);
        }
        final String rangeTime= majorRangeTime;
        final String[] timeArray= majorTimeArray;
        // 单一数据源排重
        boolean irSimflag = specialProject.isIrSimflag();
        //全网排重
        boolean irSimflagAll = specialProject.isIrSimflagAll();
        //站内排重
        boolean isSimilar = specialProject.isSimilar();

        String groName = specialProject.getSource();

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
                                String groupName = "国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook";
                                overviewOfDataIT.setGroupName(groupName);
                                overviewOfDataIT.setType(ColumnConst.CHART_PIE);
                                overviewOfDataIT.setContrast(ColumnConst.CONTRAST_TYPE_GROUP);
                                ReportResource overviewRR = new ReportResource();
                                try {
                                    overviewOfDataResult = columnSearch(overviewOfDataIT, REPORTCHARTDATASIZE, groupName);
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
                                    ReportResource situationAccess = new ReportResource();
                                    situationAccess.setImg_data(object.toString());
                                    situationAccess.setImgType("gaugeChart");
                                    situationAccess.setImgComment("暂定");
                                    situationAccess.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setSituationAccessment(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(situationAccess))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.CHART, SITUATIONACCESSMENTkey);
                                    log.error(SITUATIONACCESSMENTkey, e);
                                }
                                reportDataNewRepository.saveSituationAccessment(reportData.getSituationAccessment(), reportData.getId());
                                break;
                            case DATATRENDANALYSISkey:
                                //各舆论场趋势分析
                                log.info(String.format(SPECILAREPORTLOG, DATATRENDANALYSIS));
                                startMillis = System.currentTimeMillis();

                                try {
                                    Object dayTrendResult = specialChartAnalyzeService.getWebCountLine(specialProject, rangeTime, "day");
                                    Object hourTrendResult = specialChartAnalyzeService.getWebCountLine(specialProject, rangeTime, "hour");
                                    Map<String, Object> dataTrendResult = new HashMap<>();
                                    dataTrendResult.put("day", dayTrendResult);
                                    dataTrendResult.put("hour", hourTrendResult);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, DATATRENDANALYSIS, (endMillis - startMillis)));

                                    ReportResource dataTrendRR = new ReportResource();
                                    dataTrendRR.setImg_data(JSON.toJSONString(dataTrendResult));
                                    dataTrendRR.setImgType("brokenLineChart");
                                    dataTrendRR.setImgComment("暂定！");
                                    dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));

                                    reportData.setDataTrendAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.CHART, DATATRENDANALYSISkey);
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

                                    ReportResource dataSourceRR = new ReportResource();
                                    dataSourceRR.setImg_data(JSON.toJSONString(list));
                                    dataSourceRR.setImgType("pieGraphChartMeta");
                                    dataSourceRR.setImgComment("暂定");
                                    dataSourceRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setDataSourceAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataSourceRR))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.CHART, DATASOURCEANALYSISkey);
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
                                    Map<String, Object> opinionResult = new HashMap<>();
                                    opinionResult.put("OFFICIAL_VIEW", officialResult);
                                    opinionResult.put("MEDIA_VIEW", mediaResult);
                                    opinionResult.put("EXPORT_VIEW", exportResult);
                                    opinionResult.put("NETIZEN_VIEW", netizenResult);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, OPININOANALYSIS, (endMillis - startMillis)));

                                    ReportResource dataTrendRR = new ReportResource();
                                    dataTrendRR.setImg_data(JSON.toJSONString(opinionResult));
                                    dataTrendRR.setImgType("statisticBox");
                                    dataTrendRR.setImgComment("暂定！");
                                    dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));

                                    reportData.setOpinionAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.CHART, OPINIONANALYSISkey);
                                    log.error(OPININOANALYSIS, e);
                                }
                                reportDataNewRepository.saveOpinionAnalysis(reportData.getOpinionAnalysis(), reportData.getId());
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

                                    ReportResource emotionRR = new ReportResource();
                                    emotionRR.setImg_data(JSON.toJSONString(list));
                                    emotionRR.setImgType("pieGraphChartMeta");
                                    emotionRR.setImgComment("暂定！");
                                    emotionRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setEmotionAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(emotionRR))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.CHART, EMOTIONANALYSISkey);
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

                                    ReportResource moodRR = new ReportResource();
                                    moodRR.setImg_data(JSON.toJSONString(list));
                                    moodRR.setImgType("pieGraphChartMeta");
                                    moodRR.setImgComment("暂定！");
                                    moodRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setMoodStatistics(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(moodRR))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.CHART, MOODSTATISTICSkey);
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
                                    wordCloudRR.setImgComment("暂定！");
                                    wordCloudRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setWordCloudStatistics(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(wordCloudRR))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.CHART, WORDCLOUDSTATISTICSkey);
                                    log.error(WORDCLOUDSTATISTICS, e);
                                }
                                reportDataNewRepository.saveWordCloudStatistics(reportData.getWordCloudStatistics(), reportData.getId());
                                break;
                            case AREAkey:
                                //地图
                                log.info(String.format(SPECILAREPORTLOG, AREA));
                                startMillis = System.currentTimeMillis();
                                try {
                                    String groupNames = specialProject.getSource();//多个以;隔开
                                    if (groupNames.contains("微信") && !groupNames.contains("国内微信")) {
                                        groupNames = groupNames.replaceAll("微信", "国内微信");
                                    }
                                    groupNames = groupNames.replaceAll("境外网站", "国外新闻");
                                    String[] timeArr = DateUtil.formatTimeRange(rangeTime);
                                    if (timeArr != null && timeArr.length == 2) {
                                        specialProject.setStart(timeArr[0]);
                                        specialProject.setEnd(timeArr[1]);
                                    }
                                    QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
                                    searchBuilder.setGroupName(groupNames);
                                    List<Map<String, Object>> catalogResult = specialChartAnalyzeService.getAreaCount(searchBuilder, timeArr, isSimilar,
                                            irSimflag, irSimflagAll, "catalogArea");
                                    List<Map<String, Object>> mediaResult = specialChartAnalyzeService.getAreaCount(searchBuilder, timeArr, isSimilar,
                                            irSimflag, irSimflagAll, "mediaArea");
                                    Map<String, Object> result = new HashMap<>();
                                    result.put("catalogArea", catalogResult);
                                    result.put("mediaArea", mediaResult);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, AREA, (endMillis - startMillis)));

                                    ReportResource areaRR = new ReportResource();
                                    areaRR.setImg_data(JSON.toJSONString(result));
                                    areaRR.setImgType("mapChart");
                                    areaRR.setImgComment("暂定！");
                                    areaRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setArea(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(areaRR))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.CHART, AREAkey);
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
                                    List<Map<String, Object>> result = specialChartAnalyzeService.getHotListMessage(source,
                                            specialProject, rangeTime, 8);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, NEWSHOTTOP10, (endMillis - startMillis)));

                                    reportData.setNewsHotTopics(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(result), NEWSHOTTOP10))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, NEWSHOTTOP10key);
                                    log.error(NEWSHOTTOP10, e);
                                }
                                reportDataNewRepository.saveNewsHotTopics(reportData.getNewsHotTopics(), reportData.getId());
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

                                    reportData.setWeiboTop10(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(result), WEIBOHOTTOP10))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WEIBOHOTTOP10key);
                                    log.error(WEIBOHOTTOP10, e);
                                }
                                reportDataNewRepository.saveWeiboTop10(reportData.getWeiboTop10(), reportData.getId());
                                break;
                            case WECHATHOTTOP10key://（专题报 改造 20191121）
                                //列表，微博热点TOP10
                                log.info(String.format(SPECILAREPORTLOG, WECHATHOTTOP10));
                                startMillis = System.currentTimeMillis();
                                try {
                                    String source = CommonListChartUtil.changeGroupName("微信");
                                    List<Map<String, Object>> result = specialChartAnalyzeService.getHotListMessage(source,
                                            specialProject, rangeTime, 8);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WECHATHOTTOP10, (endMillis - startMillis)));
                                    reportData.setWechatHotTop10(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(result), WECHATHOTTOP10))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WECHATHOTTOP10key);
                                    log.error(WECHATHOTTOP10, e);
                                }
                                reportDataNewRepository.saveWechatHotTop10(reportData.getWechatHotTop10(), reportData.getId());
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
                                    reportData.setWeMediaHot(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(result), WEMEDIA))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WEMEDIAkey);
                                    log.error(WEMEDIA, e);
                                }
                                reportDataNewRepository.saveWeMediaHot(reportData.getWeMediaHot(), reportData.getId());
                                break;
                            case NEWSEVENTCONTEXTkey:
                                //列表，新闻网站事件脉络
                                log.info(String.format(SPECILAREPORTLOG, NEWSEVENTCONTEXT));
                                startMillis = System.currentTimeMillis();
                                try {
                                    List<FtsDocumentCommonVO> listChuan = new ArrayList<>();
                                    QueryBuilder queryFts = new QueryBuilder();
                                    String trsl = statBuilder.asTRSL();
                                    queryFts.filterByTRSL(trsl);
                                    queryFts.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
                                    queryFts.setPageSize(10);
                                    queryFts.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                                    GroupResult categoryChuan = commonListService.categoryQuery(queryFts, isSimilar, irSimflag, irSimflagAll, FtsFieldConst.FIELD_MD5TAG, "special", Const.TYPE_NEWS);
                                    List<GroupInfo> groupList = categoryChuan.getGroupList();
                                    if (groupList != null && groupList.size() > 0) {
                                        for (GroupInfo groupInfo : groupList) {
                                            QueryBuilder queryMd5 = new QueryBuilder();
                                            // 小时间段里MD5分类统计 时间排序取第一个结果 去查
                                            queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
                                            queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
                                            queryMd5.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                                            queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
                                            log.info(queryMd5.asTRSL());
                                            final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
                                            String trslk = "redisKey" + pageId;
                                            RedisUtil.setString(trslk, queryMd5.asTRSL());
                                            InfoListResult infoListResult2 = commonListService.queryPageList(queryMd5, isSimilar, irSimflag, irSimflagAll, Const.TYPE_NEWS, "special", UserUtils.getUser(), true);
                                            PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
                                            List<FtsDocumentCommonVO> ftsQueryChuan = content2.getPageItems();
                                            // 再取第一个MD5结果集的第一个数据
                                            if (ftsQueryChuan != null && ftsQueryChuan.size() > 0) {
                                                ftsQueryChuan.get(0).setSimCount((int) groupInfo.getCount());
                                                ftsQueryChuan.get(0).setTrslk(trslk);
                                                listChuan.add(ftsQueryChuan.get(0));
                                            }
                                        }
                                    }
                                    SortListAll sort = new SortListAll();
                                    Collections.sort(listChuan, sort);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, NEWSEVENTCONTEXT, (endMillis - startMillis)));
                                    reportData.setNewsEventContext(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(listChuan), NEWSEVENTCONTEXT))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, NEWSEVENTCONTEXTkey);
                                    log.error(NEWSEVENTCONTEXT, e);
                                }
                                reportDataNewRepository.saveNewsEventContex(reportData.getNewsEventContext(), reportData.getId());
                                break;

                            case WEIBOEVENTCONTEXTkey:
                                //列表，微博事件脉络
                                log.info(String.format(SPECILAREPORTLOG, WEIBOEVENTCONTEXT));
                                startMillis = System.currentTimeMillis();
                                try {
                                    //找十个转发数量最多的原发 按时间排序
                                    statBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT, timeArray, Operator.Between);
                                    statBuilder.setPageSize(10);
                                    PagedList<FtsDocumentCommonVO> content = commonListService.queryPageListForHotNoFormat(statBuilder, "special", Const.GROUPNAME_WEIBO);
                                    List<FtsDocumentCommonVO> ftsQueryWeiBo = content.getPageItems();
                                    SortListAll sortListWeiBo = new SortListAll();
                                    //按时间排序
                                    Collections.sort(ftsQueryWeiBo, sortListWeiBo);
                                    // 防止这个的第一条和时间的那一条重复
                                    //微博走势 不走special/chart/trendTime接口，不需要去掉第一条数据
                                    for (FtsDocumentCommonVO ftsStatus : ftsQueryWeiBo) {
                                        ftsStatus.setSiteName(ftsStatus.getScreenName());
                                    }
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEIBOEVENTCONTEXT, (endMillis - startMillis)));
                                    reportData.setWeiboEventContext(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(ftsQueryWeiBo), WEIBOEVENTCONTEXT))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WEIBOEVENTCONTEXTkey);
                                    log.error(WEIBOEVENTCONTEXT, e);
                                }
                                reportDataNewRepository.saveWeiboEventContex(reportData.getWeiboEventContext(), reportData.getId());
                                break;

                            case WECHATEVENTCONTEXTkey:
                                //列表，微信事件脉络
                                log.info(String.format(SPECILAREPORTLOG, WECHATEVENTCONTEXT));
                                startMillis = System.currentTimeMillis();
                                try {
                                    List<FtsDocumentCommonVO> listweixin = new ArrayList<>();
                                    QueryBuilder queryFts2 = new QueryBuilder();
                                    String trsl2 = statBuilder.asTRSL();
                                    queryFts2.filterByTRSL(trsl2);
                                    // 不同小时间段
                                    queryFts2.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
                                    queryFts2.setPageSize(10);
                                    queryFts2.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                                    GroupResult categoryweixin = commonListService.categoryQuery(queryFts2, isSimilar, irSimflag, irSimflagAll, FtsFieldConst.FIELD_MD5TAG, "special", Const.GROUPNAME_WEIXIN);
                                    List<GroupInfo> groupListweixin = categoryweixin.getGroupList();
                                    if (groupListweixin != null && groupListweixin.size() > 0) {
                                        for (GroupInfo groupInfo : groupListweixin) {
                                            QueryBuilder queryMd5 = new QueryBuilder();
                                            // 小时间段里MD5分类统计 时间排序取第一个结果 去查
                                            queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
                                            queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
                                            queryMd5.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                                            queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
                                            if (StringUtil.isNotEmpty(groName)) {
                                                queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME, groName.replace(";", " OR ")
                                                        .replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"), Operator.Equal);
                                            }
                                            InfoListResult infoListResult2 = commonListService.queryPageList(queryMd5, isSimilar, irSimflag, irSimflagAll, Const.TYPE_WEIXIN_GROUP, "special", UserUtils.getUser(), true);
                                            PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
                                            List<FtsDocumentCommonVO> ftsQueryChuan = content2.getPageItems();
                                            // 再取第一个MD5结果集的第一个数据
                                            if (ftsQueryChuan != null && ftsQueryChuan.size() > 0) {
                                                ftsQueryChuan.get(0).setSimCount((int) groupInfo.getCount());
                                                listweixin.add(ftsQueryChuan.get(0));
                                            }
                                        }
                                    }

                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WECHATEVENTCONTEXT, (endMillis - startMillis)));
                                    reportData.setWechatEventContext(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(listweixin), WECHATEVENTCONTEXT))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WECHATEVENTCONTEXTkey);
                                    log.error(WECHATEVENTCONTEXT, e);
                                }
                                reportDataNewRepository.saveWechatEventContex(reportData.getWechatEventContext(), reportData.getId());
                                break;

                            case WEMEDIAEVENTCONTEXTkey:
                                //列表，自媒体号事件脉络
                                log.info(String.format(SPECILAREPORTLOG, WEMEDIAEVENTCONTEXT));
                                startMillis = System.currentTimeMillis();
                                try {
                                    List<FtsDocumentCommonVO> listzimeiti = new ArrayList<>();
                                    QueryBuilder queryFts3 = new QueryBuilder();
                                    String trsl3 = statBuilder.asTRSL();
                                    queryFts3.filterByTRSL(trsl3);
                                    // 不同小时间段
                                    queryFts3.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
                                    queryFts3.setPageSize(10);
                                    queryFts3.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                                    GroupResult category = commonListService.categoryQuery(queryFts3, isSimilar, irSimflag, irSimflagAll, FtsFieldConst.FIELD_MD5TAG, "special", Const.GROUPNAME_ZIMEITI);
                                    List<GroupInfo> groupListzi = category.getGroupList();
                                    if (groupListzi != null && groupListzi.size() > 0) {
                                        for (GroupInfo groupInfo : groupListzi) {
                                            QueryBuilder queryMd5 = new QueryBuilder();
                                            // 小时间段里MD5分类统计 时间排序取第一个结果 去查
                                            queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
                                            queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
                                            queryMd5.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                                            queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
                                            if (StringUtil.isNotEmpty(groName)) {
                                                queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME, groName.replace(";", " OR ")
                                                        .replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"), Operator.Equal);
                                            }
                                            InfoListResult infoListResult2 = commonListService.queryPageList(queryMd5, isSimilar, irSimflag, irSimflagAll, Const.GROUPNAME_ZIMEITI, "special", UserUtils.getUser(), true);
                                            PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
                                            List<FtsDocumentCommonVO> ftsQueryChuan = content2.getPageItems();
                                            // 再取第一个MD5结果集的第一个数据
                                            if (ftsQueryChuan != null && ftsQueryChuan.size() > 0) {
                                                ftsQueryChuan.get(0).setSimCount((int) groupInfo.getCount());
                                            }
                                        }
                                    }
                                    // 按urltime降序
                                    SortListAll sort3 = new SortListAll();
                                    Collections.sort(listzimeiti, sort3);
                                    endMillis = System.currentTimeMillis();
                                    log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEMEDIAEVENTCONTEXT, (endMillis - startMillis)));
                                    reportData.setWemediaEventContext(ReportUtil.replaceHtml(JSON.toJSONString(ReportUtil.top10list2RR(JSON.toJSONString(listzimeiti), WEMEDIAEVENTCONTEXT))));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.LISTRESOURCES, WEMEDIAEVENTCONTEXTkey);
                                    log.error(WEMEDIAEVENTCONTEXT, e);
                                }
                                reportDataNewRepository.saveWemediaEventContex(reportData.getWemediaEventContext(), reportData.getId());
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
                                    ReportResource accountRR = new ReportResource();
                                    accountRR.setImg_data(JSON.toJSONString(mediaActiveAccount));
                                    accountRR.setImgType("activeAccount");
                                    accountRR.setImgComment("暂定！");
                                    accountRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setActiveAccount(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(accountRR)).replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "")));
                                } catch (Exception e) {
                                    ReportUtil.setEmptyData(reportData, ReportConst.CHART, ACTIVEACCOUNTkey);
                                    log.error(ACTIVEACCOUNT, e);
                                }
                                reportDataNewRepository.saveActiveAccount(reportData.getActiveAccount(), reportData.getId());
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
                indexTab.getMediaArea(), "","");
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
