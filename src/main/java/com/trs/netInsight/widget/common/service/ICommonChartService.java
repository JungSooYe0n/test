package com.trs.netInsight.widget.common.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.model.result.IQueryBuilder;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;

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
     * @param <T>
     * @return
     * @throws TRSException
     */
    <T extends IQueryBuilder> Object getBarColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName, String xyTrsl, String contrastField, String type) throws TRSException;

    /**
     * 地图数据查询
     * @param builder  查询构造器 - 需要写明当前页条数和页码
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param contrastField  分类统计字段，-普通模式用这个字段进行统计
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param <T>
     * @return
     * @throws TRSException
     */
    <T extends IQueryBuilder> Object getMapColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName,
                                                      String contrastField, String type) throws TRSException;
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
     * @param <T>
     * @return
     * @throws TRSException
     */
    <T extends IQueryBuilder> Object getPieColumnData(T builder, Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String groupName, String xyTrsl, String contrastField, String type) throws TRSException;

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

    /**
     * 热点信息查询
     * @param builder  查询构造器  需要拼接好要查询数据源，需要将可查询数据库放入对应字段
     * @param groupName 要查询的数据源类型，用;分割
     * @param pageSize  查询条数
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param <T>
     * @return
     * @throws TRSException
     */
    <T extends IQueryBuilder> Object getHotListColumnData(T builder,String groupName, Integer pageSize, String type) throws TRSException;


}