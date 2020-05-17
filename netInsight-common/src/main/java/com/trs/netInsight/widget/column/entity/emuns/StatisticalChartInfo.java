package com.trs.netInsight.widget.column.entity.emuns;

import com.trs.netInsight.config.constant.ColumnConst;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日常监测统计分析图表的图表信息
 *
 */
@Getter
@AllArgsConstructor
public enum StatisticalChartInfo {

    /*
    饼图
        pieChart
        solidPieChart
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
     * 折线图
     */
    CHART_LINE(ColumnConst.CHART_LINE, "网络发布渠道分析", 1),
    /**
     * 饼状图
     */
    CHART_PIE(ColumnConst.CHART_PIE, "媒体来源占比图", 2),

    /**
     * 饼状图
     */
    CHART_PIE_EMOTION(ColumnConst.CHART_PIE_EMOTION, "正负面占比图", 3),
    /**
     * 柱状图  横向柱状图，主要为展示效果不同，则
     */
    CHART_BAR_CROSS(ColumnConst.CHART_BAR_CROSS, "活跃账号", 4),

    /**
     * 微博热点话题榜  ---  统计方式与统计柱状图的方式一样，所以用同一个方法
     */
    HOT_TOPIC_SORT(ColumnConst.HOT_TOPIC_SORT, "微博热点话题排行", 5),

    /**
     * 词云栏目
     */
    WORD_CLOUD(ColumnConst.CHART_WORD_CLOUD, "热点词云", 6),
    /**
     * 地域热力图
     */
    MAP(ColumnConst.CHART_MAP, "热力图", 7);

    private String chartType;

    private String chartName;

    private Integer sequence;

}
