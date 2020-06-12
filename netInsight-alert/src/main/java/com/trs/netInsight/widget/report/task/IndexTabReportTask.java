package com.trs.netInsight.widget.report.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.trs.netInsight.widget.report.constant.ReportConst;
import com.trs.netInsight.widget.report.entity.ReportDataNew;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.util.ReportUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;
import static com.trs.netInsight.widget.report.constant.ReportConst.CHAPTERS2METHODSETNEW;

@Slf4j
public class IndexTabReportTask implements Runnable{

    private ArrayList<HashMap<String,Object>> statistics = null;

    public IndexTabReportTask(ArrayList<HashMap<String,Object>> statistics){
        this.statistics = statistics;
    }

    @Override
    public void run() {
        long startMillis;
        long endMillis;
        for (HashMap<String,Object> map:statistics) {

            String id = (String) map.get("id");
            String chapterName = (String) map.get("name");


            log.info(String.format(INDEXTABREPORTLOG, chapterName));
            startMillis = System.currentTimeMillis();

            try {


                ReportResource situationAccess = new ReportResource();
//                situationAccess.setImg_data(object.toString());
                situationAccess.setImgType("gaugeChart");
                situationAccess.setImgComment("暂定");
                situationAccess.setId(UUID.randomUUID().toString().replace("-", ""));
//                reportData.setSituationAccessment(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(situationAccess))));
            }catch (Exception e){
//                setEmptyData();
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
