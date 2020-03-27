package com.trs.netInsight.widget.column.entity;

import com.trs.netInsight.config.constant.ColumnConst;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 栏目类型枚举类
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月4日
 */
@Getter
@AllArgsConstructor
public enum IndexTabType {

    /**
     * 列表栏目
     */
    LIST_NO_SIM(ColumnConst.LIST_NO_SIM, "列表栏目", "com.trs.netInsight.widget.column.service.column.CommonListColumn"),
    /**
     * 热点栏目
     */
    HOT_LIST(ColumnConst.LIST_SIM, "传统媒体热点栏目", "com.trs.netInsight.widget.column.service.column.HostListColumn"),
    /**
     * 词云栏目
     */
    WORD_CLOUD(ColumnConst.CHART_WORD_CLOUD, "传统媒体词云栏目", "com.trs.netInsight.widget.column.service.column.WordCloudColumn"),
    /**
     * 地域热力图
     */
    MAP(ColumnConst.CHART_MAP, "传统媒体地域热力图栏目", "com.trs.netInsight.widget.column.service.column.MapColumn"),
    /**
     * 柱状图
     */
    CHART_BAR(ColumnConst.CHART_BAR, "柱状图", "com.trs.netInsight.widget.column.service.column.BarColumn"),
    /**
     * 分类对比柱状图
     */
    CHART_BAR_BY_META(ColumnConst.CHART_BAR_BY_META, "分类对比柱状图", "com.trs.netInsight.widget.column.service.column.BarColumn"),
    /**
     * 折线图
     */
    CHART_LINE(ColumnConst.CHART_LINE, "折线图", "com.trs.netInsight.widget.column.service.column.ChartLineColumn"),

    /**
     * 饼状图
     */
    CHART_PIE(ColumnConst.CHART_PIE, "饼状图", "com.trs.netInsight.widget.column.service.column.BarColumn"),
    /**
     * 饼状对比图
     */
    CHART_PIE_BY_META(ColumnConst.CHART_PIE_BY_META, "饼状对比图", "com.trs.netInsight.widget.column.service.column.PieColumn"),


    /**
     * common
     */
    _LIST("", "", "");

    /**
     *
     */
    private String typeCode;

    private String typeName;

    private String resource;

}
