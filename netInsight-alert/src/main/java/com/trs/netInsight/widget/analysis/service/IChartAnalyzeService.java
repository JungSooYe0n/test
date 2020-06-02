/*
 * Project: netInsight
 * 
 * File Created at 2017年11月21日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.analysis.service;

import com.alibaba.fastjson.JSONArray;
import com.trs.ckm.soap.AbsTheme;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.widget.analysis.entity.ClassInfo;
import com.trs.netInsight.widget.analysis.entity.MBlogAnalyzeEntity;
import com.trs.netInsight.widget.analysis.entity.ReportTipping;
import com.trs.netInsight.widget.analysis.entity.SpecialParam;
import com.trs.netInsight.widget.analysis.entity.SpreadNewsEntity;
import com.trs.netInsight.widget.analysis.entity.TippingPoint;
import com.trs.netInsight.widget.analysis.entity.ViewEntity;
import com.trs.netInsight.widget.analysis.enums.Top5Tab;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.spread.entity.SinaUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author yan.changjiang
 * @Type IChartAnalyzeService.java
 * @Desc 图表分析相关服务接口
 * @date 2017年11月21日 下午5:18:38
 */
public interface IChartAnalyzeService {

	/**
	 * 媒体等级分布图
	 *
	 * @param builder
	 * @return
	 * @throws TRSException
	 */
	public Object mediaLevel(QueryBuilder builder) throws TRSException;

	/**
	 * 渠道偏好饼图
	 *
	 * @param builder
	 * @return
	 * @throws TRSException
	 */
	public Object viaPreference(QueryBuilder builder, boolean sim, boolean irSimflag,boolean irSimflagAll ) throws TRSException;

	/**
	 * 生成报告时需要的数据
	 *
	 * @param hySql
	 *            检索表达式
	 * @param model
	 *            简介（INTRO） 概述（SUMM）
	 * @return
	 * @throws TRSSearchException
	 */
	public Map<String, Object> reportProcess(String hySql, String model) throws TRSSearchException;

	/**
	 * 媒体活跃等级雷达图
	 *
	 * @param builder
	 * @param timeArray
	 *            时间数组
	 * @return
	 * @throws TRSException
	 */
	public Object mediaActiveLevel(QueryBuilder builder, String source,String[] timeArray, boolean sim,
								   boolean irSimflag,boolean irSimflagAll) throws TRSException;
	/**
	 * 媒体活跃等级雷达图
	 *
	 * @param builder
	 * @param timeArray
	 *            时间数组
	 * @return
	 * @throws TRSException
	 */
	public Object mediaActiveAccount(QueryBuilder builder, String source,String[] timeArray, boolean sim,
								   boolean irSimflag,boolean irSimflagAll) throws TRSException;

	/**
	 * 微博top检索
	 *
	 * @param builder
	 * @param sort
	 *            排序方式
	 * @return
	 * @throws TRSException
	 */
	public List<MBlogAnalyzeEntity> mBlogTop5(QueryBuilder builder, Top5Tab sort, boolean sim, boolean irSimflag,boolean irSimflagAll)
			throws TRSException;

	/**
	 * 网站统计 各数据来源时间曲线图
	 *
	 * @param timeRange
	 *            时间范围
	 * @param specialProject
	 *            专项内容
	 * @param area
	 *            地域选项
	 * @param industry
	 *            行业
	 * @return
	 * @throws TRSException
	 */
	public Map<String, Object> getWebCountNew(String timeRange, SpecialProject specialProject, String area,
											  String industry) throws TRSException;

	/**
	 * 地域分布检索
	 *
	 * @param searchBuilder
	 * @param timeArray
	 *            时间数组
	 * @return
	 * @throws TRSException
	 */
	public List<Map<String, Object>> getAreaCountForHome(QueryBuilder searchBuilder, String[] timeArray,
														 String groupName) throws TRSException;

	/**
	 * 地域分布检索
	 *
	 * @param searchBuilder
	 * @param timeArray
	 *            时间数组
	 * @return
	 * @throws TRSException
	 */
	public List<Map<String, Object>> getAreaCount(QueryBuilder searchBuilder, String[] timeArray,boolean isSimilar, boolean irSimflag,boolean irSimflagAll)
			throws TRSException;
	/**
	 * 地域分布检索
	 *
	 * @param searchBuilder
	 * @param timeArray
	 *            时间数组
	 * @return
	 * @throws TRSException
	 */
	public List<Map<String, Object>> getAreaCount(QueryBuilder searchBuilder, String[] timeArray,boolean isSimilar, boolean irSimflag,boolean irSimflagAll,String areaType)
			throws TRSException;

	/**
	 * 根据时间范围检索
	 *
	 * @param esSql
	 * @param timerange
	 * @return
	 * @throws TRSException
	 */
	public Map<String, Object> getTendencyNew2(String esSql, SpecialProject specialProject, String type,
											   String timerange, String showType) throws TRSException;

	/**
	 * 根据时间范围检索
	 *
	 * @param esSql
	 * @param timerange
	 * @return
	 * @throws TRSException
	 */
	public Map<String, Object> getTendencyMessage(String esSql, SpecialProject specialProject, String timerange, String showType)
			throws TRSException;
	/**
	 * 根据时间范围检索
	 *
	 * @param timerange
	 * @return
	 * @throws TRSException
	 */
	public List<Map<String, Object>> getHotListMessage(String source, SpecialProject specialProject, String timerange,int pageSize)
			throws TRSException;
	/**
	 * 舆论趋势折线
	 *
	 * @param timerange
	 * @return
	 * @throws TRSException
	 */
	public Map<String, Object> getWebCountLine(SpecialProject specialProject, String timerange,String showType)
			throws TRSException;
	/**
	 * 观点分析
	 *
	 * @param timerange
	 * @return
	 * @throws TRSException
	 */
	public List<Map<String, Object>> getSentimentAnalysis(SpecialProject specialProject, String timerange,String viewType)
			throws TRSException;
	/**
	 * 专题分类统计表格
	 *
	 * @param specialProject
	 *            专题
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @param industryType
	 *            行业类型
	 * @param area
	 *            地域
	 * @return
	 * @throws TRSException
	 */
	public List<ClassInfo> statByClassification(SpecialProject specialProject, String start, String end,
												String industryType, String area) throws TRSException;

	/**
	 * 专题分类统计表格 选几个来源统计几个来源
	 *
	 * @param specialProject
	 *            专题
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @param industryType
	 *            行业类型
	 * @param area
	 *            地域
	 * @return
	 * @throws TRSException
	 */
	public Object stattotal(SpecialProject specialProject, String start, String end, String industryType,
									 String area, String foreign) throws TRSException;

	/**
	 * 普通搜索统计表格 选几个来源统计几个来源
	 *
	 * @param keywords
	 *            关键字
	 * @param timeRange
	 * 			  时间  - 开始和结束
	 * @param source
	 * 			  数据来源
	 *  @param keyWordIndex
	 * 	  		  关键词位置
	 * @return
	 * @throws TRSException
	 */
	public List<ClassInfo> ordinarySearchstatistics(boolean sim, boolean irSimflag,boolean irSimflagAll,String keywords,String[] timeRange, String source,String keyWordIndex,Boolean weight,String searchType) throws TRSException;

	/**
	 * 获取指定时间的情感平均值
	 *
	 * @param specialProject
	 *            专题
	 * @param groupName
	 *            分组名称
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param industryType
	 *            行业类型
	 * @param area
	 *            地域
	 * @return
	 * @throws Exception
	 */
	public int getEmotionalValue(SpecialProject specialProject, String groupName, String startTime, String endTime,
								 String industryType, String area) throws Exception;

	/**
	 * 获取引爆点信息
	 *
	 * @param documentStatus
	 *            baseurl
	 * @param beginDate
	 *            开始时间
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public List<TippingPoint> getTippingPoint(QueryBuilder queryBuilder, FtsDocumentCommonVO documentStatus,
											  Date beginDate, boolean sim,boolean irSimflag,boolean irSimflagAll) throws TRSException, TRSSearchException;

	/**
	 * 获取引爆点信息
	 *
	 * @param baseUrl
	 *            baseUrl
	 * @param beginDate
	 *            开始时间
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public List<ReportTipping> getReportTippingPoint(String baseUrl, Date beginDate)
			throws TRSException, TRSSearchException;

	/**
	 * 微博传播路径分析入口
	 *
	 *            要分析的Url
	 * @param timeArray
	 *            时间段
	 * @return
	 * @throws Exception
	 */
	public SinaUser url(String trsl, SinaUser sinaUser, String[] timeArray, boolean sim, boolean irSimflag,boolean irSimflagAll)
			throws Exception;

	/**
	 * 传统媒体传播路径分析
	 *
	 * @since changjiang @ 2018年5月9日
	 * @param root
	 * @param timeArray
	 * @return
	 * @throws Exception
	 * @Return : Object
	 */
	public SpreadNewsEntity pathByNews(SpecialProject project, QueryBuilder builder, SpreadNewsEntity root,
									   String[] timeArray, boolean irSimflag,boolean irSimflagAll) throws Exception;

	/**
	 * 获取最新新闻列表
	 *
	 * @param trsl
	 *            检索表达式
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param pageNo
	 *            页码
	 * @param pageSie
	 *            分页长
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getLatestNews(String trsl, String startTime, String endTime, Long pageNo, int pageSie)
			throws TRSSearchException, TRSException;

	/**
	 * 获取媒体活跃度
	 *
	 * @param trsl
	 *            检索表达式
	 * @param time
	 *            时间数组
	 * @param source
	 *            来源-->IR_GROUPNAME
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> mediaAct(String trsl, String[] time, String source) throws Exception;

	/**
	 * 声量趋势计算
	 *
	 * @param searchBuilder
	 * @param timeArray
	 *            时间数组
	 * @param timerange
	 *            时间范围
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getVolumeNew(QueryBuilder searchBuilder, String[] timeArray, String timerange)
			throws Exception;

	/**
	 * 不按照数据源 直接按分类统计时间
	 *
	 * @param searchBuilder
	 * @param timerange
	 * @return
	 */
	public Object getVolume(QueryBuilder searchBuilder, String timerange, boolean sim, boolean irSimflag,boolean irSimflagAll,String showType)
			throws TRSException, TRSSearchException;

	/**
	 * 获取话题演变来源
	 *
	 * @param query
	 * @param groupName
	 * @return
	 */
	public List<AbsTheme> getTopicData(QueryBuilder query, String groupName, boolean sim)
			throws TRSException, TRSSearchException;

	/**
	 * 获取专题内图表列表数据
	 *
	 * @param specialProject
	 *            专题对象
	 * @param industryType
	 *            行业类型
	 * @param area
	 *            地域
	 * @param chartType
	 *            图表类型
	 * @param dateTime
	 *            数据时间
	 * @param xType
	 *            数据参数
	 * @param source
	 *            数据来源
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            步长
	 * @return
	 * @throws Exception
	 */
	public Object getDataByChart(SpecialProject specialProject, String industryType,
								 String area, String chartType, String dateTime, String xType, String source, String entityType,
								 String sort,String emotion,String fuzzyValue,String fuzzyValueScope, int pageNo, int pageSize, String forwarPrimary,
								 String invitationCard,boolean isExport, String thirdWord) throws Exception;

	/**
	 * 不按照数据源 直接按分类统计时间
	 *
	 * @param searchBuilder
	 * @return
	 */
	public Map<String, Object> getNodesData(QueryBuilder searchBuilder, String mid)
			throws TRSException, TRSSearchException;

	/**
	 * 话题演变探索
	 *
	 * @param searchBuilder
	 * @param timeRange
	 * @param sim
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public Object getTopicEvoExplorData(QueryBuilder searchBuilder, String timeRange, boolean sim)
			throws TRSException, TRSSearchException;

	/**
	 * 网友观点
	 *
	 * @param searchBuilder
	 * @param timeArray
	 * @param sim
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public List<ViewEntity> getUserViewsData(SpecialProject specialProject, QueryBuilder searchBuilder,
											 String[] timeArray, boolean sim) throws TRSException, TRSSearchException;

	/**
	 * 获取 词云图 数据
	 *
	 * @param trsl
	 *            查询条件
	 * @param sim
	 *            是否排重
	 * @param entityType
	 *            分类统计字段
	 * @param limit
	 *            结果返回个数
	 * @param data
	 *            所查数据库
	 * @return
	 * @throws
	 * @throws TRSSearchException
	 */
	public Object getWordCloud(boolean server, String trsl, boolean sim, boolean irSimflag,boolean irSimflagAll, String entityType,
							   int limit,String type, String... data) throws TRSSearchException;
	public Object getWordCloudNew(QueryBuilder builder, boolean sim, boolean irSimflag,boolean irSimflagAll,String entityType,String type) throws TRSSearchException;

	/**
	 * 新闻传播站点分析
	 *
	 * @date Created at 2018年9月5日 上午10:12:39
	 * @Author 谷泽昊
	 * @param searchBuilder
	 * @param irSimflag
	 * @param timeArray
	 * @throws TRSSearchException
	 */
	public List<Map<String, Object>> newsSiteAnalysis(QueryBuilder searchBuilder, String[] timeArray, boolean similar,
													  boolean irSimflag,boolean irSimflagAll,boolean isApi) throws TRSSearchException;
	public List<Map<String, Object>> spreadAnalysis(QueryBuilder searchBuilder, String[] timeArray, boolean similar,
													  boolean irSimflag,boolean irSimflagAll,boolean isApi,String groupName) throws TRSSearchException;
	HashMap<String, Object> getUserViewsData(SpecialProject specialProject, String timeRange, String industry, String area, SpecialParam specParam) throws Exception;

	ArrayList<HashMap<String, Object>> getMoodStatistics(SpecialProject specialProject, String timeRange, SpecialParam specParam) throws Exception;
	/**
	 * 专题分析饼图和柱状图数据导出
	 * @param dataType
	 * @param array
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportBarOrPieData(String dataType,JSONArray array) throws IOException;

	/**
	 * 专题分析折线图数据导出
	 * @param dataType
	 * @param array
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportChartLineData(String dataType,JSONArray array) throws IOException;

	/**
	 * 专题分析词云图数据导出
	 * @param dataType
	 * @param array
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportWordCloudData(String dataType,JSONArray array) throws IOException;

	/**
	 * 地域图数据导出
	 * @param array
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportMapData(JSONArray array) throws IOException;

	/**
	 * 情感分析
	 * @date Created at 2018年12月18日  下午3:00:21
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param searchBuilder
	 * @param specialProject
	 * @return
	 */
	public List<Map<String, String>> emotionOption(QueryBuilder searchBuilder, SpecialProject specialProject);
	public int getSituationAssessment(QueryBuilder searchBuilder, SpecialProject specialProject) throws TRSException;
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月21日 Administrator creat
 */