package com.trs.netInsight.support.appApi.utils.constance;

/**
 * Api默认接口标准参数集,可扩展,但所有Api参数必须在此记录,以便进行统一参数校验
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月4日
 *
 */
public enum ApiParam {

	// ---------- 栏目资源相关 -------------
	/**
	 * 一级栏目id
	 */
	indexPageId,
	/**
	 * 栏目id
	 */
	indexTableId,

	// ---------- 专题资源相关 -------------
	/**
	 * 专项id
	 */
	specialSubjectId,
	/**
	 * 专题id
	 */
	specialProjectId,

	// ---------- 报告资源相关 -------------
	/**
	 * 报告id
	 */
	reportId,

	// ---------- 预警资源相关 -------------
	alertRuleId,

	// ---------- 检索资源相关 -------------
	/**
	 * 检索词
	 */
	selectKey,
	/**
	 * 排除词
	 */
	excludeKey,
	/**
	 * 排除网站
	 */
	excludeWeb,
	/**
	 * 检索关键词位置
	 */
	selectKeyIndex,
	/**
	 * 检索时间,exp:24h/7d,优先级高于selectTimeRange并与之互斥,
	 */
	selectTime,
	/**
	 * 检索时间范围,exp:2018-05-05 05:05:05;2018-06-06 06:06:06,优先级地域selectTime并与之互斥
	 */
	selectTimeRange,
	/**
	 * 排重选项
	 */
	similarType,
	/**
	 * 页码
	 */
	pageNo,
	/**
	 * 每页展示条数
	 */
	pageSize,

	// ---------- 平台资源相关 -------------
	/**
	 * 用户id
	 */
	userId,

	// ---------- 默认参数 -------------
	/**
	 * HttpSevletRequest
	 */
	request,
	/**
	 * HttpSevletResponse
	 */
	response;

}
