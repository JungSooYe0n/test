package com.trs.netInsight.widget.report.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
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
import com.trs.netInsight.widget.report.constant.ReportConst;
import com.trs.netInsight.widget.report.entity.ReportDataNew;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.repository.ReportDataNewRepository;
import com.trs.netInsight.widget.report.util.ReportUtil;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.wml.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;
import static com.trs.netInsight.widget.report.constant.ReportConst.CHAPTERS2METHODSETNEW;

@Slf4j
public class IndexTabReportTask implements Runnable{

    private ArrayList<HashMap<String,Object>> statistics = null;

    private ReportDataNew reportData = null;

    private ReportDataNewRepository reportDataNewRepository;
    public IndexTabReportTask(ArrayList<HashMap<String,Object>> statistics,ReportDataNew reportData,ReportDataNewRepository reportDataNewRepository){
        this.statistics = statistics;
        this.reportData = reportData;
        this.reportDataNewRepository = reportDataNewRepository;
    }

    private IColumnChartService columnChartService = (IColumnChartService) ObjectContainer.getBean(IColumnChartService.class);
    private IIndexTabMapperService indexTabMapperService = (IIndexTabMapperService) ObjectContainer.getBean(IIndexTabMapperService.class);

    public static HashMap<String,HashMap> chapterInfoForIndexTab ;

    static {
        HashMap<String,String> item = new HashMap<>();

        item.put("key",DATATRENDANALYSISkey);
        item.put("name",DATATRENDANALYSIS);
        item.put("mapContrast","contrastGroup");
        chapterInfoForIndexTab.put("各舆论场趋势分析",item);

        item.put("key",DATATRENDANALYSISkey);
        item.put("name",DATATRENDANALYSIS);
        item.put("mapContrast","contrastGroup");
        chapterInfoForIndexTab.put("媒体来源对比",item);

        item.put("key",EMOTIONANALYSISkey);
        item.put("name",EMOTIONANALYSIS);
        item.put("mapContrast","contrastEmotion");
        chapterInfoForIndexTab.put("正负面占比",item);
        item.put("key",ACTIVEACCOUNTkey);
        item.put("name",ACTIVEACCOUNT);
        item.put("mapContrast","contrastSite");
        chapterInfoForIndexTab.put("活跃账号",item);

        item.put("key",WEIBOHOTTOPICSkey);
        item.put("name",WEIBOHOTTOPICS);
        item.put("mapContrast","contrastTopic");
        chapterInfoForIndexTab.put("微博热点话题排行",item);
        item.put("key",WORDCLOUDSTATISTICSkey);
        item.put("name",WORDCLOUDSTATISTICS);
        item.put("mapContrast","hitArticle");
        chapterInfoForIndexTab.put("词云统计",item);
        item.put("key",AREAkey);
        item.put("name",AREA);
        item.put("mapContrast","hitArticle;mediaArea");
        chapterInfoForIndexTab.put("地域统计",item);
    }

    @Override
    public void run() {
        long startMillis;
        long endMillis;
        for (HashMap<String,Object> map:statistics) {

            String id = (String) map.get("id");
            String chapterName = (String) map.get("name");

            StatisticalChart statisticalChart = columnChartService.findOneStatisticalChart(id);
            IndexTabMapper mapper = indexTabMapperService.findOne(statisticalChart.getParentId());
            IndexTab indexTab = mapper.getIndexTab();
            indexTab.setType(statisticalChart.getChartType());
            StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.getStatisticalChartInfo(statisticalChart.getChartType());
            indexTab.setContrast(statisticalChartInfo.getContrast());
            if(StatisticalChartInfo.WORD_CLOUD.equals(statisticalChartInfo)){
                indexTab.setTabWidth(100);
            }
            // urlRemove
            indexTab.setSimilar(false);
            indexTab.setIrSimflag(true);
            indexTab.setIrSimflagAll(false);
            indexTab.setKeyWordIndex("1");

            AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
            ColumnConfig config = new ColumnConfig();
            String timerange = indexTab.getTimeRange();
            config.setChartPage(ChartPageInfo.StatisticalChart);

//            column.setDistrictInfoService(districtInfoService);
//            column.setCommonListService(commonListService);
//            column.setCommonChartService(commonChartService);
            column.setConfig(config);
            int pageSize = 10;
            IndexTabType indexTabType = ColumnFactory.chooseType(indexTab.getType());
//            config.setShowType(showType);
//            if(StringUtil.isNotEmpty(mapContrast) && IndexTabType.MAP.equals(indexTabType)){
//                indexTab.setContrast(mapContrast);
//            }




            log.info(String.format(INDEXTABREPORTLOG, chapterName));
            startMillis = System.currentTimeMillis();

            try {




                ReportResource situationAccess = new ReportResource();
//                situationAccess.setImg_data(object.toString());
                situationAccess.setImgType("gaugeChart");
                situationAccess.setImgComment("暂定");
                situationAccess.setId(UUID.randomUUID().toString().replace("-", ""));
                reportData.setSituationAccessment(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(situationAccess))));
            }catch (Exception e){
                setEmptyData(reportData,"","");
                log.error(chapterName, e);
            }

        }
    }

    /***
     * 意思是该模块的数据已经被计算过，但没计算出数据来
     * 如果不做处理直接是null的话，你不能知道该章节是没计算还是计算出异常了
     * @param reportData
     * @param chapterType
     * @param chapterDetail
     */
    private void setEmptyData(ReportDataNew reportData, String chapterType, String chapterDetail) {
        if (ReportConst.SINGLERESOURCE.equals(chapterType) && OVERVIEWOFDATAkey.equals(chapterDetail)) {
            ReportResource overviewRR = new ReportResource();
            overviewRR.setImgComment("暂无数据！");
            overviewRR.setImg_data("暂无数据！");
            reportData.setOverviewOfdata(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(overviewRR))));
        } else {
            ReportResource emptyResource = new ReportResource();
            ArrayList<ReportResource> resources = new ArrayList<>();
            resources.add(emptyResource);
            String data = JSONArray.toJSONString(resources);
            try {
                reportData.getClass().getDeclaredMethod(CHAPTERS2METHODSETNEW.get(chapterDetail), String.class).invoke(reportData, data);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("存储");
            }
        }
    }
}
