package com.trs.netInsight.widget.report.constant;
/**
 * 日常监测生成报告
 */
public enum IndexTabChapter {

    OVERVIEWOFDATA(ReportConst.OVERVIEWOFDATA, ReportConst.SINGLERESOURCE) ,
    DATATRENDANALYSIS(ReportConst.DATATRENDANALYSIS, ReportConst.CHART),
    DATASOURCEANALYSIS(ReportConst.DATASOURCEANALYSIS, ReportConst.CHART),
    EMOTIONANALYSIS(ReportConst.EMOTIONANALYSIS, ReportConst.CHART),
    MOODSTATISTICS(ReportConst.MOODSTATISTICS, ReportConst.CHART),

    //活跃账号
    ACTIVEACCOUNT(ReportConst.ACTIVEACCOUNT,ReportConst.CHART),

    //微博热点话题
    WEIBOHOTTOPICS(ReportConst.WEIBOHOTTOPICS, ReportConst.CHART) ,

    WORDCLOUDSTATISTICS(ReportConst.WORDCLOUDSTATISTICS, ReportConst.CHART),
    AREA(ReportConst.AREA, ReportConst.CHART);

    String value;
    String valueType;

    IndexTabChapter(String value, String valueType) {
        this.value = value;
        this.valueType = valueType;
    }

    public String getValue(){
        return value;
    }

    public String getValueType(){
        return valueType;
    }
}

