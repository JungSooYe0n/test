package com.trs.netInsight.widget.column.service;

import com.trs.netInsight.widget.column.entity.CustomChart;
import com.trs.netInsight.widget.column.entity.StatisticalChart;

import java.util.List;

public interface IColumnChartService {

    /**
     * 获取当前栏目对应的所有图表，包括统计分析图表和自定义图表
     *
     * @param id 栏目id
     * @return
     */
    Object getColumnChart(String id);

    /**
     * 获取当前分组对应的所有指定的栏目和图表
     *
     * @param pageId 分组id
     * @return
     */
    Object getTopColumnChartForPage(String pageId);

    /**
     * 初始化统计图表信息
     *
     * @param id
     * @return
     */
    List<StatisticalChart> initStatisticalChart(String id);

    /**
     * 获取当前栏目id下的自定义图表个数
     * @param tabId
     * @return
     */
    Integer getCustomChartSize(String tabId);

    /**
     * 获取当前栏目下置顶id的最大序号值
     *
     * @param id
     * @return
     */
    Integer getMaxTopSequence(String id);

    StatisticalChart findOneStatisticalChart(String id);

    CustomChart findOneCustomChart(String id);

    Long countForTabid(String id);

    CustomChart saveCustomChart(CustomChart customChart);

    StatisticalChart saveStatisticalChart(StatisticalChart statisticalChart);

    void deleteCustomChart(String id);

    Integer deleteCustomChartForTabMapper(String id);


}
