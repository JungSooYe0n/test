package com.trs.netInsight.widget.report.constant;

import java.util.UUID;

/***
 *  Created by shao.guangze on 2018/6/28
 *  报告章节
 *  1.value 默认章节名称
 *  2.valueType 供日报、周报、月报生成word的时候根据不同类型调用不同的方法
 *  3.this.toString 供页面不同章节，预览的时候出不同的图片
 */
public enum Chapter {
    /*
    报告简介
     */
    Report_Synopsis(ReportConst.Hand_Made_Report_Synopsis, ReportConst.SINGLERESOURCE,1) ,
    /*
    数据统计概述
     */
    Statistics_Summarize(ReportConst.Hand_Made_Statistics_Summarize, ReportConst.SINGLERESOURCE,2) ,
    /*
    数据来源对比
     */
    Source_contrast(ReportConst.Hand_Made_Source_contrast, ReportConst.CHART,3),
    /*
    数据统计
     */
    Data_Statistics(ReportConst.Hand_Made_Data_Statistics, ReportConst.CHART,4),
    /*
    全国地域分布
     */
    Area(ReportConst.Hand_Made_Area, ReportConst.CHART,5),
    /*
    情感分布
     */
    Emotion_Analyze(ReportConst.Hand_Made_Emotion_Analyze, ReportConst.CHART,6),
    /*
    热点新闻
     */
    Hot_News(ReportConst.Hand_Made_Hot_News, ReportConst.LISTRESOURCES,7),
    /*
    网站来源TOP10
     */
    //TOP10_News(ReportConst.Hand_Made_TOP10_News, ReportConst.CHART,8),
    /*
    热点微博
     */
    Hot_Weibo(ReportConst.Hand_Made_Hot_Weibo, ReportConst.LISTRESOURCES,8),
    /*
    微博活跃账号TOP10
     */
    //TOP10_Weibo(ReportConst.Hand_Made_TOP10_Weibo, ReportConst.CHART,10),
    /*
    微信活跃账号TOP10
     */
    //TOP10_Wechat(ReportConst.Hand_Made_TOP10_Wechat, ReportConst.CHART,11);
    /**
     * 活跃用户TOP10 ---------  因为 网站来源微信微博TOP10 融合为1个了，但是前端暂时不想改名字，所以 叫TOP10_New。，但是中文名字改了
     */
    TOP10_News(ReportConst.Hand_Made_TOP10_User, ReportConst.CHART,9);

    String value;
    String valueType;
    Integer sequence;

    Chapter(String value, String valueType,Integer sequence) {
        this.value = value;
        this.valueType = valueType;
        this.sequence = sequence;
    }

    public String getValue(){
        return value;
    }

    public String getValueType(){
        return valueType;
    }
    public Integer getSequence(){
        return sequence;
    }

}
