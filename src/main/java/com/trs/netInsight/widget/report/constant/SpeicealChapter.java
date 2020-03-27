package com.trs.netInsight.widget.report.constant;
/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/11/21.
 * @desc 专家模式专题报 报告章节
 *   1.value 默认章节名称
 *   2.valueType 供报告生成word的时候根据不同类型调用不同的方法
 *   3.this.toString 供页面不同章节，预览的时候出不同的图片
 */
public enum SpeicealChapter {
    REPORTINTRO(ReportConst.REPORTINTRO, ReportConst.SINGLERESOURCE) ,
    OVERVIEWOFDATA(ReportConst.OVERVIEWOFDATA, ReportConst.SINGLERESOURCE) ,
    NEWSHOTTOP10(ReportConst.NEWSHOTTOP10, ReportConst.LISTRESOURCES),
    WEIBOHOTTOP10(ReportConst.WEIBOHOTTOP10, ReportConst.LISTRESOURCES),
    WECHATHOTTOP10(ReportConst.WECHATHOTTOP10, ReportConst.LISTRESOURCES),
    DATATRENDANALYSIS(ReportConst.DATATRENDANALYSIS, ReportConst.CHART),
    DATASOURCEANALYSIS(ReportConst.DATASOURCEANALYSIS, ReportConst.CHART),
    WEBSITESOURCETOP10(ReportConst.WEBSITESOURCETOP10, ReportConst.CHART),
    WEIBOACTIVETOP10(ReportConst.WEIBOACTIVETOP10,ReportConst.CHART),
    WECHATACTIVETOP10(ReportConst.WECHATACTIVETOP10,ReportConst.CHART),
    AREA(ReportConst.AREA, ReportConst.CHART),
    EMOTIONANALYSIS(ReportConst.EMOTIONANALYSIS, ReportConst.CHART);


    String value;
    String valueType;

    SpeicealChapter(String value, String valueType) {
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

