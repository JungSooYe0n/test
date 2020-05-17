package com.trs.netInsight.widget.common.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.model.result.IQueryBuilder;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;

import java.util.List;

/**
 * 为图表统计的公共查询方法
 */
public interface ICommonChartService {

    /**
     * 柱状图数据查询
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param xyTrsl  分类检索表达式  -- 通过这个字段判断是否是专家模式  专家模式单独分类统计每个分类的数据，与直接通过表达式统计不同
     * @param contrastField  分类统计字段  -- 普通模式采用这个字段进行统计
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param resultKey   这个是返回数据中对应的名字
     * @param <T>
     * @return
     * @throws TRSException
     */
    <T extends IQueryBuilder> Object getBarColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName, String xyTrsl, String contrastField, String type, ChartResultField resultKey) throws TRSException;

    /**
     * 地图数据查询
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param contrastField  分类统计字段，-普通模式用这个字段进行统计
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param resultKey   这个是返回数据中对应的名字
     * @param <T>
     * @return
     * @throws TRSException
     */
    <T extends IQueryBuilder> Object getMapColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName,
                                                      String contrastField, String type, ChartResultField resultKey) throws TRSException;
    /**
     *饼图数据查询
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param xyTrsl  分类检索表达式  -- 通过这个字段判断是否是专家模式  专家模式单独分类统计每个分类的数据，与直接通过表达式统计不同
     * @param contrastField  分类统计字段  -- 普通模式采用这个字段进行统计
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param resultKey   这个是返回数据中对应的名字
     * @param <T>
     * @return
     * @throws TRSException
     */
    <T extends IQueryBuilder> Object getPieColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName, String xyTrsl, String contrastField, String type, ChartResultField resultKey) throws TRSException;

    /**
     *词云图数据查询
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param entityType  词云图的筛选类型
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param <T>
     * @return
     * @throws TRSException
     */
    <T extends IQueryBuilder> Object getWordCloudColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll,
                                                            String groupName, String entityType, String type) throws TRSException;

    //折线图
    //groupField 为折线图x轴字段   如果为按小时显示，因为Hybase查询时，按小时只能显示小时数，所以按小时查询时，一次最多显示一天的数据，方法内不对返回的时间做处理，所以在按小时查询时，传进来的groupData的值完全与hybase查询结果一样

    /**
     *
     * @param builder  检索构造器
     * @param sim  单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  要查询的数据源，用;分号分割
     * @param type   查询类型，对应用户限制查询时间的模块相同
     * @param xyTrsl  分类对比查询表达式 -- 专家模式下，用户填写的分类对比表达式，这个字段有值时，不要写下面的contrastField和  contrastData
     * @param contrastField   对比字段
     * @param contrastData  对比字段中要对比的数据集合，例如对比字段是groupName，那么这个字段的值应该为对应的要对比的数据源如新闻等。
     * @param groupField   这个为x轴对应的字段，例如日常监测中的折线图x轴为时间，则这个字段为时间字段，一条线为一个按时间进行统计分析的结果
     * @param groupData  这个为x轴对应的展示数据，这个值需要与hybase查询到的统计结果的key值相同，分则无效
     * @param resultKey   这个是返回数据中对应的名字
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object getChartLineColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName, String type, String xyTrsl,
                                                                   String contrastField, List<String> contrastData, String groupField, List<String> groupData, ChartResultField resultKey) throws TRSException;


}