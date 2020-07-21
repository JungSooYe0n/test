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
     * 获取当前分组对应的所有置顶的栏目和图表 栏目+自定义图表+ 统计分析图表
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
    List<StatisticalChart> initStatisticalChart(List<StatisticalChart> statisticalChartList,String id);

    /**
     * 获取当前栏目id下的自定义图表的最大序号
     * @param tabId
     * @return
     */
    Integer getMaxCustomChartSeq(String tabId);

    /**
     * 获取当前栏目下置顶id的最大序号值
     *
     * @param id
     * @return
     */
    Integer getMaxTopSequence(String id);

    /**
     * 查找一个统计分析图表
     * @param id
     * @return
     */
    StatisticalChart findOneStatisticalChart(String id);

    /**
     * 查找一个自定义图表
     * @param id
     * @return
     */
    CustomChart findOneCustomChart(String id);

    /**
     * 获取当前栏目下自定义图表个数
     * @param id
     * @return
     */
    Long countForTabid(String id);

    /**
     * 保存自定义图表
     * @param customChart
     * @return
     */
    CustomChart saveCustomChart(CustomChart customChart);

    /**
     * 保存一个统计分析图表
     * @param statisticalChart
     * @return
     */
    StatisticalChart saveStatisticalChart(StatisticalChart statisticalChart);

    /**
     * 删除一个自定义图表
     * @param id
     */
    void deleteCustomChart(String id);

    /**
     * 删除一个栏目下的所有图表- 自定义+ 统计
     * @param id
     * @return
     */
    Integer deleteCustomChartForTabMapper(String id);

    /**
     * 拖动自定义图标排序
     * @param customChartList
     * @return
     */
    Object moveCustomChart(List<CustomChart> customChartList);

    /**
     * 修改错误数据用的
     * @return
     */
    Object addColumnType();
}
