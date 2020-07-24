package com.trs.netInsight.widget.report.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.IndexTabType;
import com.trs.netInsight.widget.column.entity.StatisticalChart;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.column.entity.emuns.StatisticalChartInfo;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.column.service.IColumnChartService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.service.impl.CommonChartServiceImpl;
import com.trs.netInsight.widget.common.service.impl.CommonListServiceImpl;
import com.trs.netInsight.widget.report.constant.ReportConst;
import com.trs.netInsight.widget.report.entity.ReportDataNew;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.repository.ReportDataNewRepository;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.report.util.SpecialReportUtil;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.wml.R;

import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;
import static com.trs.netInsight.widget.report.constant.ReportConst.CHAPTERS2METHODSETNEW;

@Slf4j
public class IndexTabReportTask implements Runnable {

    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(30);

    private IndexTabMapper mapper = null;

    private ReportDataNew reportData = null;

    private ReportDataNewRepository reportDataNewRepository;

    public IndexTabReportTask(IndexTabMapper mapper, ReportDataNew reportData, ReportDataNewRepository reportDataNewRepository) {
        this.mapper = mapper;
        this.reportData = reportData;
        this.reportDataNewRepository = reportDataNewRepository;
    }
    private IDistrictInfoService districtInfoService = (IDistrictInfoService) ObjectContainer.getBean(IDistrictInfoService.class);
    private ICommonListService commonListService = (ICommonListService) ObjectContainer.getBean(ICommonListService.class);
    private ICommonChartService commonChartService = (ICommonChartService) ObjectContainer.getBean(CommonChartServiceImpl.class);

    @Override
    public void run() {
        long startMillis;
        long endMillis;
        //要生成报告的那个日常监测栏目
        IndexTab majorIndexTab = mapper.getIndexTab();
        String timerange = majorIndexTab.getTimeRange();
        int threadCount = chapterInfoForIndexTab.size() +1;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount);
        //这个是要生成的报告的模板 - 包含要生成报告的所有模块
        for( HashMap.Entry<String, HashMap> chapterInfo :chapterInfoForIndexTab.entrySet()){

            String chapterName = chapterInfo.getKey();
            HashMap<String , String> item = chapterInfo.getValue();
            StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.valueOf(item.get("StatisticalChartInfo"));

            String showTypes = item.get("showType");
            log.info(String.format(INDEXTABREPORTLOG, chapterName));
            startMillis = System.currentTimeMillis();
            //通过复制 重新实例化一个栏目对象，跟上面的栏目对象指向了不同地址，但是是相同的参数
            IndexTab indexTab = majorIndexTab.tabCopy();
            indexTab.setType(statisticalChartInfo.getChartType());
            indexTab.setContrast(item.get("mapContrast"));

            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
                        ColumnConfig config = new ColumnConfig();

                        config.setChartPage(ChartPageInfo.StatisticalChart);
                        column.setDistrictInfoService(districtInfoService);
                        column.setCommonListService(commonListService);
                        column.setCommonChartService(commonChartService);
                        HashMap<String, Object> mapRet = new HashMap();
                        ReportResource dataTrendRR = new ReportResource();
                        //各舆论场趋势分析 根据 showType 进行区分
                        switch (item.get("key")) {
                            case OVERVIEWOFDATAkey:
                                IndexTab tabForOverView = mapper.getIndexTab();
                                tabForOverView.setContrast(ColumnConst.CONTRAST_TYPE_GROUP);
                                tabForOverView.setType(ColumnConst.CHART_PIE);
                                config.setShowType(showTypes);
                                config.initSection(tabForOverView, timerange, 0, 10, null, null, item.get("entityType"), "", "", "default", "", "",
                                        "", "", null, tabForOverView.getMediaLevel(), tabForOverView.getMediaIndustry(), tabForOverView.getContentIndustry(), tabForOverView.getFilterInfo(),
                                        tabForOverView.getContentArea(), tabForOverView.getMediaArea(), null,"");
                                column.setConfig(config);
                                Object overViewRtn = column.getColumnData(timerange);
                                dataTrendRR.setImgComment(ReportUtil.getOverviewOfData(JSON.toJSONString(overViewRtn)));
                                dataTrendRR.setImg_data(ReportUtil.getOverviewOfData(JSON.toJSONString(overViewRtn)));
                                reportData.setOverviewOfdata(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                reportDataNewRepository.saveOverviewOfdata(reportData.getOverviewOfdata(), reportData.getId());
                                break;
                            case DATATRENDANALYSISkey:
                                if (StringUtil.isNotEmpty(showTypes)) {
                                    Object dayTrendResult = null;
                                    String[] typeArr = showTypes.split(";");
                                    for (String type : typeArr) {
                                        config.setShowType(type);
                                        config.initSection(indexTab, timerange, 0, 10, null, null, item.get("entityType"), "", "", "default", "", "",
                                                "", "", null, indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
                                                indexTab.getContentArea(), indexTab.getMediaArea(), null,"");
                                        column.setConfig(config);
                                        Object object = column.getColumnData(timerange);
                                        dayTrendResult = object;
                                        if(ObjectUtil.isNotEmpty(object)){
                                            mapRet.put(type, object);
                                        }
                                    }
                                    if(ObjectUtil.isNotEmpty(mapRet)){
                                        dataTrendRR.setImg_data(JSON.toJSONString(mapRet));
                                        dataTrendRR.setImgType("brokenLineChart");
                                        dataTrendRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(dayTrendResult), "brokenLineChart", "各舆论场趋势分析"));
                                        dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                        reportData.setDataTrendAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                    }
                                }
                                reportDataNewRepository.saveDataTrendAnalysis(reportData.getDataTrendAnalysis(), reportData.getId());
                                break;
                            case DATASOURCEANALYSISkey:
                                config.setShowType(showTypes);
                                config.initSection(indexTab, timerange, 0, 10, null, null, item.get("entityType"), "", "", "default", "", "",
                                        "", "", null, indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
                                        indexTab.getContentArea(), indexTab.getMediaArea(), null,"");
                                column.setConfig(config);
                                Object object = column.getColumnData(timerange);
                                if(ObjectUtil.isNotEmpty(object)){
                                    dataTrendRR.setImg_data(JSON.toJSONString(object));
                                    dataTrendRR.setImgType("pieChart");
                                    dataTrendRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(object), "pieChart", null));
                                    dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setDataSourceAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                }
                                reportDataNewRepository.saveDataSourceAnalysis(reportData.getDataSourceAnalysis(), reportData.getId());
                                break;

                            case ACTIVEACCOUNTkey:
                                config.setShowType(showTypes);
                                config.initSection(indexTab, timerange, 0, 10, null, null, item.get("entityType"), "", "", "default", "", "",
                                        "", "", null, indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
                                        indexTab.getContentArea(), indexTab.getMediaArea(), null,"");
                                column.setConfig(config);
                                Object objectActiveAccount = column.getColumnData(timerange);
                                if(ObjectUtil.isNotEmpty(objectActiveAccount)) {
                                    dataTrendRR.setImg_data(JSON.toJSONString(objectActiveAccount));
                                    dataTrendRR.setImgType("activeAccount");
                                    dataTrendRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(objectActiveAccount), "activeAccount", null));
                                    dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setActiveAccount(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR)).replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "")));
                                }
                                reportDataNewRepository.saveActiveAccount(reportData.getActiveAccount(), reportData.getId());
                                break;
                            case EMOTIONANALYSISkey:
                                config.setShowType(showTypes);
                                config.initSection(indexTab, timerange, 0, 10, null, null, item.get("entityType"), "", "", "default", "", "",
                                        "", "", null, indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
                                        indexTab.getContentArea(), indexTab.getMediaArea(), null,"");
                                column.setConfig(config);
                                Object emotionObj = column.getColumnData(timerange);
                                if(ObjectUtil.isNotEmpty(emotionObj)) {
                                dataTrendRR.setImg_data(JSON.toJSONString(emotionObj));
                                dataTrendRR.setImgType("emotionPieChart");
                                dataTrendRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(emotionObj), "emotionPieChart", null));
                                dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                reportData.setEmotionAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                }
                                reportDataNewRepository.saveEmotionAnalysis(reportData.getEmotionAnalysis(), reportData.getId());
                                break;
                            case WEIBOHOTTOPICSkey:
                                config.setShowType(showTypes);
                                config.initSection(indexTab, timerange, 0, 10, null, null, item.get("entityType"), "", "", "default", "", "",
                                        "", "", null, indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
                                        indexTab.getContentArea(), indexTab.getMediaArea(), null,"");
                                column.setConfig(config);
                                Object weiboTopicObj = column.getColumnData(timerange);
                                if(ObjectUtil.isNotEmpty(weiboTopicObj)){
                                    reportData.setWeiboHotTopics(ReportUtil.replaceHtml(JSON.toJSONString(weiboTopicObj)));
                                }
                                reportDataNewRepository.saveWeiboHotTopics(reportData.getWeiboHotTopics(), reportData.getId());
                                break;

                            case WORDCLOUDSTATISTICSkey:
                                String entityTypes = item.get("entityType");
                                String[] typeArr = entityTypes.split(";");
                                HashMap<String, Object> wordCloudRtn = new HashMap<>();
                                config.setShowType(showTypes);
                                for (String type : typeArr) {
                                    config.initSection(indexTab, timerange, 0, 10, null, null, type, "", "", "default", "", "",
                                            "", "", null, indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
                                            indexTab.getContentArea(), indexTab.getMediaArea(), null,"");
                                    column.setConfig(config);
                                    Object itemObj = column.getColumnData(timerange);
                                    if(ObjectUtil.isNotEmpty(itemObj)){
                                        wordCloudRtn.put(type, itemObj);
                                    }
                                }
                                if(ObjectUtil.isNotEmpty(wordCloudRtn)){
                                    dataTrendRR.setImg_data(JSON.toJSONString(wordCloudRtn));
                                    dataTrendRR.setImgType("wordCloud");
                                    dataTrendRR.setImgComment("暂定！");
                                    dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setWordCloudStatistics(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                }
                                reportDataNewRepository.saveWordCloudStatistics(reportData.getWordCloudStatistics(), reportData.getId());
                                break;
                            case AREAkey:
                                String mapContrasts = item.get("mapContrast");
                                String[] mapContrastArr = mapContrasts.split(";");
                                String keyStrs = item.get("keyStr");
                                String[] keyStrArr = keyStrs.split(";");
                                HashMap<String, Object> areaMap = new HashMap<>();
                                config.setShowType(showTypes);
                                Object comment = null;
                                int i = 0;
                                for (String mapContrast : mapContrastArr) {
                                    indexTab.setContrast(mapContrast);
                                    config.initSection(indexTab, timerange, 0, 10, null, null, item.get("entityType"), "", "", "default", "", "",
                                            "", "", null, indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
                                            indexTab.getContentArea(), indexTab.getMediaArea(), null,"");
                                    column.setConfig(config);
                                    Object areaRtn = column.getColumnData(timerange);
                                    if(i ==0){
                                        comment = areaRtn;
                                    }
                                    if(ObjectUtil.isNotEmpty(areaRtn)){
                                        areaMap.put(keyStrArr[i], areaRtn);
                                    }
                                    i++;
                                }
                                if(ObjectUtil.isNotEmpty(areaMap)){
                                    dataTrendRR.setImg_data(JSON.toJSONString(areaMap));
                                    dataTrendRR.setImgType("mapChart");
                                    dataTrendRR.setImgComment(SpecialReportUtil.getImgComment(JSON.toJSONString(comment), "mapChart", null));
                                    dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));
                                    reportData.setArea(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
                                }
                                reportDataNewRepository.saveArea(reportData.getArea(), reportData.getId());
                                break;
                            default:
                                break;

                        }
                    } catch (Exception e) {
                        //ReportUtil.setEmptyData(reportData, item.get("chapterType"), item.get("key"));
                        log.error(chapterName, e);
                    } finally {
                        try {
                            cyclicBarrier.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            endMillis = System.currentTimeMillis();
            log.info(String.format(INDEXTABREPORTLOG + SPECIALREPORTTIMELOG, item.get("name"), (endMillis - startMillis)));
        }
        try {
            cyclicBarrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        reportData.setDoneFlag(1);
        reportDataNewRepository.saveDoneFlag(1, reportData.getId());
    }

}
