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
    REPORTINTRO(ReportConst.REPORTINTRO, ReportConst.SINGLERESOURCE) ,
    OVERVIEWOFDATA(ReportConst.OVERVIEWOFDATA, ReportConst.SINGLERESOURCE) ,
    NEWSTOP10(ReportConst.NEWSTOP10, ReportConst.LISTRESOURCES),
    WEIBOTOP10(ReportConst.WEIBOTOP10, ReportConst.LISTRESOURCES),
    WECHATTOP10(ReportConst.WECHATTOP10, ReportConst.LISTRESOURCES),
    DATATRENDANALYSIS(ReportConst.DATATRENDANALYSIS, ReportConst.CHART),
    DATASOURCEANALYSIS(ReportConst.DATASOURCEANALYSIS, ReportConst.CHART),
    WEBSITESOURCETOP10(ReportConst.WEBSITESOURCETOP10, ReportConst.CHART),
    AREA(ReportConst.AREA, ReportConst.CHART),
    EMOTIONANALYSIS(ReportConst.EMOTIONANALYSIS, ReportConst.CHART),
    NEWSHOTTOPICS(ReportConst.NEWSHOTTOPICS, ReportConst.LISTRESOURCES),
    WEIBOHOTTOPICS(ReportConst.WEIBOHOTTOPICS, ReportConst.LISTRESOURCES);

    String value;
    String valueType;

    Chapter(String value, String valueType) {
        this.value = value;
        this.valueType = valueType;
    }

    public String getValue(){
        return value;
    }

    public String getValueType(){
        return valueType;
    }

    public static void main(String[] args){
        Chapter[] values = Chapter.values();
        System.out.println(values[0].toString());
        System.out.println(values[0].getValue());
        System.out.println(values[0].getValueType());
        System.out.println(UUID.randomUUID().toString().replace("-",""));
    }

}
