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

    /*
    饼图
        pieChart
        emotionPieChart
    柱状图
        barGraphChart
        crossBarGraphChart
    折线图
        brokenLineChart
    词云
        wordCloudChart
    地图
        mapChart
    微博热点
        hotTopicSort
    热点列表
        md5ListInfo
    普通信息列表
        timeListInfo
     */

    /**
     * 列表栏目
     */
    LIST_NO_SIM(ColumnConst.LIST_NO_SIM, "列表栏目", "com.trs.netInsight.widget.column.service.column.CommonListColumn"),
    /**
     * 热点栏目
     */
    HOT_LIST(ColumnConst.LIST_SIM, "热点栏目", "com.trs.netInsight.widget.column.service.column.HostListColumn"),
    /**
     * 词云栏目
     */
    WORD_CLOUD(ColumnConst.CHART_WORD_CLOUD, "词云图", "com.trs.netInsight.widget.column.service.column.WordCloudColumn"),
    /**
     * 地域热力图
     */
    MAP(ColumnConst.CHART_MAP, "地域热力图", "com.trs.netInsight.widget.column.service.column.MapColumn"),
    /**
     * 微博热点话题榜  ---  统计方式与统计柱状图的方式一样，所以用同一个方法
     */
    HOT_TOPIC_SORT(ColumnConst.HOT_TOPIC_SORT, "微博热点话题榜", "com.trs.netInsight.widget.column.service.column.BarColumn"),

    /**
     * 柱状图
     */
    CHART_BAR(ColumnConst.CHART_BAR, "柱状图", "com.trs.netInsight.widget.column.service.column.BarColumn"),
    /**
     * 柱状图  横向柱状图，主要为展示效果不同，则
     */
    CHART_BAR_CROSS(ColumnConst.CHART_BAR_CROSS, "活跃账号图", "com.trs.netInsight.widget.column.service.column.BarColumn"),

    /**
     * 折线图
     */
    CHART_LINE(ColumnConst.CHART_LINE, "折线图", "com.trs.netInsight.widget.column.service.column.ChartLineColumn"),

    /**
     * 饼状图
     */
    CHART_PIE(ColumnConst.CHART_PIE, "饼状图", "com.trs.netInsight.widget.column.service.column.PieColumn"),

    /**
     * 饼状图
     */
    CHART_PIE_EMOTION(ColumnConst.CHART_PIE_EMOTION, "情感分析饼状图", "com.trs.netInsight.widget.column.service.column.PieColumn"),


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
