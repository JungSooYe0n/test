package com.trs.netInsight.widget.special.service;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.widget.special.entity.SpecialExponent;
import com.trs.netInsight.widget.special.entity.SpecialExponentVO;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 专题分析指数计算及其相关服务
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月3日
 *
 */
public interface ISpecialComputeService {

	/**
	 * 专题指数计算服务,只计算当天指数
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @return
	 * @throws ParseException
	 * @throws TRSSearchException 
	 * @throws OperationException 
	 * @Return : boolean
	 */
	public boolean compute() throws ParseException, TRSSearchException, OperationException;

	/**
	 * 专题指数计算服务,只对当天指数及范围内机构进行计算
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @param orgIds
	 *            机构id集
	 * @return
	 * @throws ParseException
	 * @throws TRSSearchException 
	 * @throws OperationException 
	 * @Return : boolean
	 */
	public boolean compute(String[] orgIds) throws ParseException, TRSSearchException, OperationException;

	/**
	 * 根据专题id删除专题指数集
	 * 
	 * @since changjiang @ 2018年5月5日
	 * @param specialIds
	 *            专题id集
	 * @Return : void
	 */
	public void delete(String[] specialIds);

	/**
	 * 获取全部指数
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAll();

	/**
	 * 根据机构id检索专题指数列表
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @param orgId
	 *            机构id
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAll(String orgId);

	/**
	 * 根据指定专题检索专题指数列表
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @param specialIds
	 *            专题id集
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAll(String[] specialIds);

	/**
	 * 检索所有专题指数列表并按照指定字段进行排序
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @param orderBy
	 *            排序字段
	 * @param sort
	 *            正/逆序,true为正序
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAll(String orderBy, boolean sort);

	/**
	 * 根据指定专题集检索专题指数列表并按照指定字段进行排序
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @param specialIds
	 *            专题id集
	 * @param orderBy
	 *            排序字段
	 * @param sort
	 *            正/逆序
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAll(String[] specialIds, String orderBy, boolean sort);

	/**
	 * 根据机构id检索专题指数列表并按照指定字段进行排序
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @param orgId
	 *            机构id
	 * @param orderBy
	 *            排序字段
	 * @param sort
	 *            正/逆序,true为正序
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAll(String orgId, String orderBy, boolean sort);

	/**
	 * 根据计算日期段检索专题指数列表
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @param begin
	 *            开始日期
	 * @param end
	 *            结束日期
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAllByComputeDate(Date begin, Date end);

	/**
	 * 根据指定专题及计算日期段检索专题指数列表
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @param specialIds
	 *            专题id集
	 * @param begin
	 *            开始日期
	 * @param end
	 *            结束日期
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAllByComputeDate(String[] specialIds, Date begin, Date end);

	/**
	 * 根据机构id及计算日期检索专题指数列表
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @param orgId
	 *            专题id
	 * @param begin
	 *            开始日期
	 * @param end
	 *            结束日期
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAllByComputeDate(String orgId, Date begin, Date end);

	/**
	 * 根据计算日期检索专题指数列表并按照指定字段进行排序
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @param begin
	 *            开始日期
	 * @param end
	 *            结束日期
	 * @param orderBy
	 *            排序字段
	 * @param sort
	 *            正/逆序,true为正序
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAllByComputeDate(Date begin, Date end, String orderBy, boolean sort);

	/**
	 * 根据指定专题id集以及计算日期段检索专题指数列表并按照指定字段进行排序
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @param specialIds
	 *            专题id集
	 * @param begin
	 *            开始日期
	 * @param end
	 *            结束日期
	 * @param orderBy
	 *            排序字段
	 * @param sort
	 *            正逆序
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAllByComputeDate(String[] specialIds, Date begin, Date end, String orderBy,
			boolean sort);

	/**
	 * 根据专题id及计算日期检索专题指数列表,并按照指定字段进行排序
	 * 
	 * @since changjiang @ 2018年5月3日
	 * @param orgId
	 *            专题id
	 * @param begin
	 *            开始日期
	 * @param end
	 *            结束日期
	 * @param orderBy
	 *            排序字段
	 * @param sort
	 *            正/逆序,true为正序
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> findAllByComputeDate(String orgId, Date begin, Date end, String orderBy, boolean sort);

	/**
	 * 计算指定专题在一定条件下的指数(按天计算,默认计算七天),并持久化
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @param specialId
	 *            专题id
	 * @param begin
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @return
	 * @throws TRSException
	 * @throws ParseException
	 * @throws TRSSearchException
	 * @Return : List<SpecialExponent>
	 */
	public List<SpecialExponent> computeBySpecialId(String specialId, Date begin, Date end)
			throws TRSException, ParseException, TRSSearchException;

	/**
	 * 根据专题集合以及时间范围计算各指数,并按照指定字段进行排序
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @param specialIds
	 * @param begin
	 * @param end
	 * @param orderBy
	 * @param sort
	 * @return
	 */
	public List<SpecialExponentVO> computeTotalByCondition(String[] specialIds, Date begin, Date end, String orderBy,
			boolean sort);

	/**
	 * 根据专题集合以及时间范围计算走势图所需数据
	 * 
	 * @since changjiang @ 2018年5月7日
	 * @param specialIds
	 * 				专题id集
	 * @param begin
	 * 				开始日期
	 * @param end
	 * '			结束日期
	 * @return
	 * @Return : Map<String,List<Map<String,Long>>>
	 */
	public Object computeTrendChart(String[] specialIds, Date begin, Date end);


}
