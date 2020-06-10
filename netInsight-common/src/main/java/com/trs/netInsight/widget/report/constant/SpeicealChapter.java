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

    OVERVIEWOFDATA(ReportConst.OVERVIEWOFDATA, ReportConst.SINGLERESOURCE) ,
    SITUATIONACCESSMENT(ReportConst.SITUATIONACCESSMENT, ReportConst.CHART),
    DATATRENDANALYSIS(ReportConst.DATATRENDANALYSIS, ReportConst.CHART),
    DATASOURCEANALYSIS(ReportConst.DATASOURCEANALYSIS, ReportConst.CHART),
    OPININOANALYSIS(ReportConst.OPININOANALYSIS, ReportConst.CHART),
    EMOTIONANALYSIS(ReportConst.EMOTIONANALYSIS, ReportConst.CHART),
    MOODSTATISTICS(ReportConst.MOODSTATISTICS, ReportConst.CHART),
    WORDCLOUDSTATISTICS(ReportConst.WORDCLOUDSTATISTICS, ReportConst.CHART),
    AREA(ReportConst.AREA, ReportConst.CHART),

    //热点信息
    NEWSHOTTOP10(ReportConst.NEWSHOTTOP10,ReportConst.SINGLERESOURCE),
    WEIBOHOTTOP10(ReportConst.WEIBOHOTTOP10,ReportConst.SINGLERESOURCE),
    WECHATHOTTOP10(ReportConst.WECHATHOTTOP10,ReportConst.SINGLERESOURCE),
    WEMEDIA(ReportConst.WEMEDIA,ReportConst.SINGLERESOURCE),

    //事件脉络
    NEWSEVENTCONTEXT(ReportConst.NEWSEVENTCONTEXT,ReportConst.SINGLERESOURCE),
    WEIBOEVENTCONTEXT(ReportConst.WEIBOEVENTCONTEXT,ReportConst.SINGLERESOURCE),
    WECHATEVENTCONTEXT(ReportConst.WECHATEVENTCONTEXT,ReportConst.SINGLERESOURCE),
    WEMEDIAEVENTCONTEXT(ReportConst.WEMEDIAEVENTCONTEXT,ReportConst.SINGLERESOURCE),

    //传播分析
    PROPAFATIONANALYSIS(ReportConst.PROPAFATIONANALYSIS,ReportConst.CHART),
    //活跃账号
    ACTIVEACCOUNT(ReportConst.ACTIVEACCOUNT,ReportConst.CHART);



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

