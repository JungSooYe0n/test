package com.trs.netInsight.widget.report.constant;


/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/19.
 * @desc 极简模式报告章节
 *   1.value 默认章节名称
 *   2.valueType 供报告生成word的时候根据不同类型调用不同的方法
 *   3.this.toString 供页面不同章节，预览的时候出不同的图片
 */
public enum SimplerChapter {

    REPORTINTRO(SimplerReportConst.REPORTINTRO, ReportConst.SINGLERESOURCE) ,
    OVERVIEWOFDATA(SimplerReportConst.OVERVIEWOFDATA, ReportConst.SINGLERESOURCE) ,
//    NEWSTOP10(SimplerReportConst.NEWSTOP10, ReportConst.LISTRESOURCES),
//    WEIBOTOP10(SimplerReportConst.WEIBOTOP10, ReportConst.LISTRESOURCES),
//    WECHATTOP10(SimplerReportConst.WECHATTOP10, ReportConst.LISTRESOURCES),
    DATASOURCEANALYSIS(SimplerReportConst.DATASOURCEANALYSIS, ReportConst.CHART),
    DATATRENDANALYSIS(SimplerReportConst.DATATRENDANALYSIS, ReportConst.CHART),
    AREA(SimplerReportConst.AREA, ReportConst.CHART),
    EMOTIONANALYSIS(SimplerReportConst.EMOTIONANALYSIS, ReportConst.CHART),
    NEWSHOTTOPICS(SimplerReportConst.NEWSHOTTOPICS, ReportConst.LISTRESOURCES),
    WEBSITESOURCETOP10(SimplerReportConst.WEBSITESOURCETOP10, ReportConst.CHART),
    WEIBOHOTTOPICS(SimplerReportConst.WEIBOHOTTOPICS, ReportConst.LISTRESOURCES),
    WEIBOACTIVETOP10(SimplerReportConst.WEIBOACTIVETOP10,ReportConst.CHART),
    WECHATACTIVETOP10(SimplerReportConst.WECHATACTIVETOP10,ReportConst.CHART);
    //CUSTOMMODULE(SimplerReportConst.CUSTOMMODULE,ReportConst.LISTRESOURCES);

    String value;
    String valueType;

    SimplerChapter(String value, String valueType) {
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
