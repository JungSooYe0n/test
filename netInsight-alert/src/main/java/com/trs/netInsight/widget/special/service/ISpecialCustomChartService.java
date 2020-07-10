package com.trs.netInsight.widget.special.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.special.entity.SpecialCustomChart;

public interface ISpecialCustomChartService {

    /**
     * 获取当前专题分析栏目下的自定义图表
     * @param id
     * @return
     */
    Object getCustomChart(String id);

    /**
     * 获取当前专题分析栏目id下的自定义图表个数
     * @param id
     * @return
     */
    Integer getCustomChartSize(String id);

    /**
     * 获取当前栏目下自定义图表的最大序号值
     *
     * @param id
     * @return
     */
    Integer getMaxChartSequence(String id);

    /**
     * 查找一个自定义图表
     * @param id
     * @return
     */
    SpecialCustomChart findOneSpecialCustomChart(String id);

    /**
     * 保存一个自定义图表
     * @return
     */
    SpecialCustomChart saveSpecialCustomChart(SpecialCustomChart specialCustomChart);

    /**
     * 删除一个自定义图表
     * @param id
     */
    void deleteSpecialCustomChart(String id);

    /**
     * 删除专题对应自定义图表
     * @param id
     * @return
     */
    Integer deleteCustomChart(String id);

    /**
     * 查询自定义图标的数据
     * @param customChart 自定义图表
     * @param timeRange 时间 - 页面上选择的
     * @param showType 折线图的展示方式
     * @param entityType  词云的类型
     * @param contrast  地图选择的对比类型
     * @return
     * @throws TRSException
     */
    Object selectChartData(SpecialCustomChart customChart,String timeRange,String showType,String entityType,String contrast)throws TRSException;

    Object selectChar2ListtData(SpecialCustomChart customChart,String source,String key,String dateTime,String entityType,String mapContrast,String sort,int pageNo,int pageSize,
                                String forwardPrimary,String invitationCard,String fuzzyValue,String fuzzyValueScope)throws TRSException;
}
