package com.trs.netInsight.widget.common.service;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IQueryBuilder;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.user.entity.User;

/**
 * 为列表查询和数据源条数统计的公共查询方法
 */
public interface ICommonListService {

    /**
     * 分页查询信息列表数据  --  混合列表（需要标注要查询的数据源）
     * 返回数据被处理 - 列表展示用这个方法
     *
     * 需要拼接好表达式，拼接要查询的数据源类型
     * 需要在builder中写好要查询数据库
     * 需要在builder中放入页码和当前页显示条数
     *
     * @param builder           查询构造器 - 拼接完表达式
     * @param sim               单一媒体排重
     * @param irSimflag         站内排重
     * @param irSimflagAll      全网排重
     * @param type              查询类型，对应用户限制查询时间的模块相同
     * @param user              当前用户信息
     * @param isCalculateSimNum 是否计算相似文章数
     * @return
     * @throws TRSException
     */
    public InfoListResult queryPageList(QueryCommonBuilder builder, boolean sim,
                                        boolean irSimflag, boolean irSimflagAll,String groupName, String type, User user, Boolean isCalculateSimNum) throws TRSException;

    /**
     * 普通信息列表  - 导出时使用
     * 调用hybase查询数据，直接查询，不做处理  --  混合列表（需要标注要查询的数据源）
     * 返回数据未被处理
     * 当查询数据的条数超过10000条时，查询的数据必定为无序的
     *
     * 需要拼接好表达式，拼接要查询的数据源类型
     * 需要在builder中写好要查询数据库
     * 需要在builder中放入页码和当前页显示条数
     * @param builder      查询构造器
     * @param sim          单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSException
     */
    public PagedList<FtsDocumentCommonVO> queryPageListBase(QueryCommonBuilder builder, boolean sim,
                                                            boolean irSimflag, boolean irSimflagAll, String type) throws TRSException;


    /**
     * 导出时使用
     * 分页查询信息列表数据  --  混合列表（需要标注要查询的数据源）
     * -- 列表无序，从hybase拿到的数据为：符合条件的随机数据，混合库可能只拿其中某个库
     * 当查询数据的条数超过10000条时，查询的数据必定为无序的
     * 返回数据未被处理
     *
     * 需要拼接好表达式，拼接要查询的数据源类型
     * 需要在builder中写好要查询数据库
     * 需要在builder中放入页码和当前页显示条数
     *
     * @param builder      查询构造器
     * @param sim          单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSException
     */
    public PagedList<FtsDocumentCommonVO> queryPageListNoSort(QueryCommonBuilder builder, boolean sim,
                                                              boolean irSimflag, boolean irSimflagAll, String type) throws TRSException;

    /**
     * 分页查询热度信息列表  --  混合列表（需要标注要查询的数据源）
     * 返回数据被处理 - 主要是前端页面展示  - 列表展示用这个方法
     * 列表按热度进行排序
     *
     * 需要拼接好表达式，拼接要查询的数据源类型
     * 需要在builder中写好要查询数据库
     * 需要在builder中放入页码和当前页显示条数
     *
     * @param builder           查询构造器
     * @param user              当前用户信息
     * @param type              查询类型，对应用户限制查询时间的模块相同
     * @param isCalculateSimNum 是否计算相似文章数
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> InfoListResult queryPageListForHot(T builder,String groupName, User user, String type, Boolean isCalculateSimNum) throws TRSException;

    /**
     * 导出时使用
     * 热点信息列表  -- 按热度排序时，默认为站内排重
     * 调用hybase查询数据，直接查询，不做处理  --  混合列表（需要标注要查询的数据源）
     * 返回数据未被处理
     * 列表按热度进行排序
     *
     *需要拼接好表达式，拼接要查询的数据源类型
     * 需要在builder中写好要查询数据库
     * 需要在builder中放入页码和当前页显示条数
     * @param builder 查询构造器
     * @param type    查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> PagedList<FtsDocumentCommonVO> queryPageListForHotBase(T builder, String type) throws TRSException;

    /**
     * 列表数据统计分析  统计各个数据源所占数据条数
     * @param builder  查询表达式
     * @param sim   单一媒体排重
     * @param irSimflag  站内排重
     * @param irSimflagAll  全网排重
     * @param groupName  数据源，用;分号分割
     * @param type  查询类型，对应用户限制查询时间的模块相同
     * @param <T>
     * @return
     * @throws TRSException
     */
    public <T extends IQueryBuilder> Object queryListGroupNameStattotal(T builder, boolean sim,
                                                                        boolean irSimflag, boolean irSimflagAll,String groupName, String type) throws TRSException;


    /**
     * 统计 - 获取到当前条件符合数量的总数
     *需要拼接好表达式，拼接要查询的数据源类型
     * 需要在builder中写好要查询数据库
     * @param builder      查询构造器
     * @param isSimilar    单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSSearchException
     */
    public <T extends IQueryBuilder> Long ftsCount(T builder, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) throws TRSSearchException;

    /**
     * 分类统计  --- 根据条件进行分类统计
     * 需要拼接好表达式，拼接要查询的数据源类型
     * 需要在builder中写好要查询数据库
     * 需要在builder中放入页码和当前页显示条数
     * @param builder      查询构造器
     * @param isSimilar    单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param groupField   分类字段
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSSearchException
     */
    public <T extends IQueryBuilder> GroupResult categoryQuery(T builder, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String groupField, String type) throws TRSSearchException;


}
