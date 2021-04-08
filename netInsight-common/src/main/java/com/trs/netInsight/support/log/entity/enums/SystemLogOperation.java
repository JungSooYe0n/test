/*
 * Project: netInsight
 * 
 * File Created at 2018年7月25日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.log.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 具体操作类
 * 
 * @Type SystemLogOperation.java
 * @author 谷泽昊
 * @date 2018年7月25日 下午3:24:18
 * @version
 */
@Getter
@AllArgsConstructor
public enum SystemLogOperation {

	// ------------api--------------
	/**
	 * 获取栏目组数据
	 */
    INDEX_PAGE("获取栏目组数据","查询"),
	/**
	 * 获取栏目组数据（包含导航栏）
	 */
	GET_INDEX_PAGE("获取栏目组数据（包含导航栏）","查询"),
	/**
	 * 获取栏目组数据（包含导航栏）
	 */
	INDEX_PAGE_INFO("获取栏目详情","查询"),
	/**
	 * 获取栏目组下栏目列表数据
	 */
	INDEX_TABLE("获取栏目详情","查询"),
	/**
	 * 获取栏目组下栏目列表数据（包含父级）
	 */
	INDEX_TABLES("获取栏目详情（包含父级）","查询"),
	/**
	 * 获取栏目数据
	 */
	INDEX_TAB_DATA("获取栏目数据","查询"),
	/**
	 * 各舆论场趋势分析
	 */
	STATISTICAL_CHART("各舆论场趋势分析","查询"),
	/**
	 * 获取日常监测地域分布数据
	 */
	COLUMN_AREA("获取日常监测地域分布数据","查询"),
	/**
	 * 获取栏目列表数据
	 */
	INDEX_TAB_LIST_DATA("获取栏目列表数据","查询"),
	/**
	 * 获取专题分析数据
	 */
	SPECIAL_ALL("获取专题分析数据","查询"),
	/**
	 * 获取专题监测统计表格数据
	 */
	SPECIAL_STAT_TOTAL("获取专题监测统计表格数据","查询"),
	/**
	 * 获取专题信息列表
	 */
	SPECIAL_LIST_INFO("获取专题信息列表","查询"),
	/**
	 * 获取来源类型统计数据
	 */
	SPECIAL_WEB_COUNT("获取来源类型统计数据","查询"),
	/**
	 * 获取态势评估数据
	 */
	SITUATION_ASSESSMENT("获取态势评估数据","查询"),
	/**
	 * 获取各舆论场趋势分析数据
	 */
	WEB_COUNT_LINE("获取各舆论场趋势分析数据","查询"),
	/**
	 * 获取各舆论场发布统计数据
	 */
	WEB_COMMIT_COUNT("获取各舆论场发布统计数据","查询"),
	/**
	 * 获取观点分析数据
	 */
	SENTIMENT_ANALYSIS("获取观点分析数据","查询"),
	/**
	 * 获取情绪统计数据
	 */
	MOOD_STATISTICS("获取情绪统计数据","查询"),
	/**
	 * 获取热点信息数据
	 */
	HOT_MESSAGE("获取热点信息数据","查询"),
	/**
	 * 获取传播分析站点数据
	 */
	SPREAD_ANALYSIS_SITE_NAME("获取传播分析站点数据","查询"),
	/**
	 * 获取专题微博top5数据
	 */
	SPECIAL_STATUS_TOP5("获取专题微博top5数据","查询"),
	/**
	 * 获取专题地域分布数据
	 */
	SPECIAL_AREA("获取专题地域分布数据","查询"),
	/**
	 * 获取专题媒体活跃等级数据
	 */
	SPECIAL_ACTIVE_LEVEL("获取专题媒体活跃等级数据","查询"),
	/**
	 * 获取情感分析数据
	 */
	SPECIAL_STATUS_OPTION("获取情感分析数据","查询"),
	/**
	 * 获取事件溯源
	 */
	SPECIAL_TREND_TIME("获取事件溯源","查询"),
	/**
	 * 获取信息走势图数据
	 */
	SPECIAL_TREND_MESSAGE("获取信息走势图数据","查询"),
	/**
	 * 获取专题引爆点数据
	 */
	SPECIAL_TIPPING_POINT("获取专题引爆点数据","查询"),
	/**
	 * 获取情感走势数据
	 */
	SPECIAL_VOLUME("获取情感走势数据","查询"),
	/**
	 * 获取专题新闻传播分析数据
	 */
	SPECIAL_NEWS_SITE_ANALYSIS("获取专题新闻传播分析数据","查询"),
	/**
	 * 获取活跃账号数据
	 */
	ACTIVE_ACCOUNT("获取活跃账号数据","查询"),
	/**
	 * 获取网友情绪数据
	 */
	SPECIAL_USER_VIEWS("获取网友情绪数据","查询"),
	/**
	 * 获取专题词云数据
	 */
	SPECIAL_WORD_CLOUD("获取专题词云数据","查询"),
	/**
	 * 获取专题热词探索数据
	 */
	SPECIAL_TOPIC_EVO_EXPLOR("获取专题热词探索数据","查询"),
	/**
	 * 列表搜索
	 */
	SELECT_DATA("列表搜索","查询"),
	/**
	 * 高级搜索
	 */
	ADVANCED_SEARCH("高级搜索","查询"),
	/**
	 * 表达式检索列表
	 */
	EXPERT_SEARCH("表达式检索列表","查询"),
	/**
	 * 数据详情api
	 */
	DOCUMENT_DETAIL("数据详情api","查询"),
	/**
	 * 获取单条微博分析列表
	 */
	MICRO_BLOG_LIST("获取单条微博分析列表","查询"),
	/**
	 * 获取当前微博的博主信息
	 */
	BLOGGER_INFO("获取当前微博的博主信息","查询"),
	/**
	 * 获取当前微博信息
	 */
	MICRO_BLOG_DETAIL("获取当前微博信息","查询"),
	/**
	 * 热门评论TOP5
	 */
	HOT_REVIEWS("热门评论TOP5","查询"),
	/**
	 * 传播分析
	 */
	SPREAD_ANALYSIS("传播分析","查询"),
	/**
	 * 被转发趋势
	 */
	FORWARDED_TREND("被转发趋势","查询"),
	/**
	 * 传播路径
	 */
	SPREAD_PATH("传播路径","查询"),
	/**
	 * 核心转发
	 */
	CORE_FORWARD("核心转发","查询"),
	/**
	 * 意见领袖
	 */
	OPINION_LEADERS("意见领袖","查询"),
	/**
	 * 转发博主地域分析
	 */
	AREAANALYSIS_OF_FORWARDERS("转发博主地域分析","查询"),
	/**
	 * 转发微博表情分析
	 */
	EMOJI_ANALYSIS_OF_FORWARD("转发微博表情分析","查询"),
	/**
	 * 男女占比
	 */
	GENDER_OF_RATIO("男女占比","查询"),
	/**
	 * 认证比例
	 */
	CERTIFIED_OF_RATIO("认证比例","查询"),
	/**
	 * 博主发文频率
	 */
	DISPATCH_FREQUENCY("博主发文频率","查询"),
	/**
	 * 参与话题统计
	 */
	TAKE_SUPER_LANGUAGE("参与话题统计","查询"),
	/**
	 * 发文情感统计
	 */
	EMOTION_STATISTICS("发文情感统计","查询"),
	/**
	 * 原发转发占比
	 */
	PRIMARY_FORWARD_RATIO("原发转发占比","查询"),
	// -------------搜索--------------
	/**
	 * 普通搜索
	 */
	ORDINARY_SEARCH("普通搜索","查询"),
	/**
	 * 高级搜索
	 */
	SEARCH_LIST("高级搜索","查询"),
	// ------------专题--------------
	/**
	 * 专项置顶
	 */
	SPECIAL_TOP_FLAG("专项置顶","修改"),
	/**
	 * 取消专项置顶
	 */
	SPECIAL_NO_TOP_FLAG("取消专项置顶","修改"),
	/**
	 * 添加专项
	 */
	SPECIAL_ADD("添加专项","添加"),
	/**
	 * 修改专项
	 */
	SPECIAL_UPDATE("修改专项","修改"),
	/**
	 * 删除专项
	 */
	SPECIAL_DELETE("删除专项","删除"),
	/**
	 * 添加主题
	 */
	SPECIAL_ADD_SUBJECT("添加主题","添加"),
	/**
	 * 添加专题
	 */
	SPECIAL_ADD_ZHUAN("添加专题","添加"),
	/**
	 * 专题重命名
	 */
	SPECIAL_RENAME_SPECIAL("专题重命名","修改"),
	/**
	 * 主题重命名
	 */
	SPECIAL_RENAME_SUBJECT("主题重命名","修改"),
	/**
	 * 删除主题
	 */
	SPECIAL_DELETE_SUBJECT("删除主题","删除"),
	/**
	 * 删除专题
	 */
	SPECIAL_DELETE_ZHUANTI("删除专题","删除"),

	/**
	 * 查询专题数据列表
	 */
	SPECIAL_SELECT_ZHUANTI_LIST("查询专题数据列表","查询"),
	/**
	 * 查询专题分析 / 内容统计 / 来源类型统计
	 */
	SPECIAL_SELECT_ZHUANTI_WEBCOUNT("查询专题分析 / 内容统计 / 来源类型统计","查询"),
	/**
	 * 查询专题分析 / 内容统计 / 微博top5
	 */
	SPECIAL_SELECT_ZHUANTI_TOP5("查询专题分析 / 内容统计 / 微博top5","查询"),
	/**
	 * 查询专题分析 / 内容统计 / 地域分布
	 */
	SPECIAL_SELECT_ZHUANTI_AREA("查询专题分析 / 内容统计 / 地域分布","查询"),
	/**
	 * 查询专题分析 / 内容统计 / 媒体活跃等级
	 */
	SPECIAL_SELECT_ZHUANTI_ACTIVE_LEVEL("查询专题分析 / 内容统计 / 媒体活跃等级","查询"),
	/**
	 * 查询专题分析 / 内容统计 / 微博情感分析
	 */
	SPECIAL_SELECT_ZHUANTI_EMOTIONOPTION("查询专题分析 / 内容统计 / 微博情感分析","查询"),
	/**
	 * 查询专题分析 / 事件趋势 / 事件溯源
	 */
	SPECIAL_SELECT_ZHUANTI_TRENDTIME("查询专题分析 / 事件趋势 / 事件溯源","查询"),
	/**
	 * 查询专题分析 / 事件趋势 / 信息走势图 / 信息走势
	 */
	SPECIAL_SELECT_ZHUANTI_TRENDMESSAGE("查询专题分析 / 事件趋势 / 信息走势图 / 信息走势","查询"),
	/**
	 * 查询专题分析 / 事件趋势 / 信息走势图 / 网民参与趋势
	 */
	SPECIAL_SELECT_ZHUANTI_NETTENDENCY("查询专题分析 / 事件趋势 / 信息走势图 / 网民参与趋势","查询"),
	/**
	 * 查询专题分析 / 事件趋势 / 信息走势图 / 媒体参与趋势
	 */
	SPECIAL_SELECT_ZHUANTI_METATENDENCY("查询专题分析 / 事件趋势 / 信息走势图 / 媒体参与趋势","查询"),
	/**
	 * 查询专题分析 / 事件趋势 / 新闻传播分析
	 */
	SPECIAL_SELECT_ZHUANTI_NEWSSITEANALYSIS("查询专题分析 / 事件趋势 / 新闻传播分析","查询"),
	/**
	 * 查询专题分析 / 事件趋势 / 引爆点
	 */
	SPECIAL_SELECT_ZHUANTI_TIPPINGPOINT("查询专题分析 / 事件趋势 / 引爆点","查询"),
	/**
	 * 查询专题分析 / 事件趋势 / 情感走势
	 */
	SPECIAL_SELECT_ZHUANTI_VOLUME("查询专题分析 / 事件趋势 / 情感走势","查询"),
	/**
	 * 查询专题分析 / 事件趋势 / 事件溯源2
	 */
	SPECIAL_SELECT_ZHUANTI_TRENDMD5("查询专题分析 / 事件趋势 / 事件溯源2","查询"),
	/**
	 * 查询专题分析 / 事件分析 / 热词探索
	 */
	SPECIAL_SELECT_ZHUANTI_TOPICEVOEXPLOR("查询专题分析 / 事件分析 / 热词探索","查询"),
	/**
	 * 查询专题分析 / 事件分析 / 网友观点
	 */
	SPECIAL_SELECT_ZHUANTI_USERVIEWS("查询专题分析 / 事件分析 / 网友观点","查询"),
	/**
	 * 查询专题分析 / 事件分析 / 词云
	 */
	SPECIAL_SELECT_ZHUANTI_WORDCLOUD("查询专题分析 / 事件分析 / 词云","查询"),
	// ------------日常监测--------------
	/**
	 * 添加自定义导航栏
	 */
	COLUMN_ADD_NAVIGATION("添加自定义导航栏","添加"),
	/**
	 * 删除自定义导航栏
	 */
	COLUMN_DELETE_NAVIGATION("删除自定义导航栏","删除"),
	/**
	 * 拖拽自定义导航栏
	 */
	COLUMN_MOVE_NAVIGATION("拖拽自定义导航栏","修改"),
	/**
	 * 修改自定义导航栏
	 */
	COLUMN_UPDATE_NAVIGATION("修改自定义导航栏","修改"),
	/**
	 * 隐藏显示自定义导航栏
	 */
	COLUMN_HIDE_OR_SHOW_NAVI("隐藏显示自定义导航栏","修改"),
	/**
	 * 添加一级栏目
	 */
	COLUMN_ADD_INDEX_PAGE("添加一级栏目","添加"),
	/**
	 * 修改一级栏目
	 */
	COLUMN_UPDATE_INDEX_PAGE("修改一级栏目","修改"),
	/**
	 * 删除一级栏目
	 */
	COLUMN_DELETE_INDEX_PAGE("删除一级栏目","删除"),
	/**
	 * 隐藏一级栏目
	 */
	COLUMN_HIDE_INDEX_PAGE("隐藏一级栏目","修改"),
	/**
	 * 显示一级栏目
	 */
	COLUMN_SHOW_INDEX_PAGE("显示一级栏目","查询"),
	/**
	 * 查询所有栏目
	 */
	SELECT_ALL_INDEX_PAGE("查询所有栏目","查询"),
	/**
	 * 拖拽一级栏目
	 */
	COLUMN_MOVE_ONE("拖拽一级栏目","修改"),
	/**
	 * 拖拽二级栏目
	 */
	COLUMN_MOVE_TWO("拖拽二级栏目（图表）","修改"),
	/**
	 * 添加二级栏目（图表）
	 */
	COLUMN_ADD_INDEX_TAB("添加二级栏目（图表）","添加"),
	/**
	 * 修改二级栏目（图表）
	 */
	COLUMN_UPDATE_INDEX_TAB("修改二级栏目（图表）","修改"),
	/**
	 * 删除二级栏目（图表）
	 */
	COLUMN_DELETE_INDEX_TAB("删除二级栏目（图表）","删除"),
	/**
	 * 隐藏二级栏目（图表）
	 */
	COLUMN_HIDE_INDEX_TAB("隐藏二级栏目（图表）","修改"),
	/**
	 * 查询图表数据
	 */
	COLUMN_SELECT_INDEX_TAB_DATA("查询图表数据","查询"),
	/**
	 * 查询栏目的信息列表数据
	 */
	COLUMN_SELECT_INDEX_TAB_INFO("查询栏目的信息列表数据","查询"),

	/**
	 * 添加自定义图表
	 */
	COLUMN_ADD_CUSTOM_CHART("添加自定义图表","添加"),
	/**
	 * 修改自定义图表
	 */
	COLUMN_UPDATE_CUSTOM_CHART("修改自定义图表","修改"),
	/**
	 * 删除自定义图表
	 */
	COLUMN_DELETE_CUSTOM_CHART("删除自定义图表","删除"),

	/**
	 * 查询栏目下对应的统计分析 + 自定义图表
	 */
	COLUMN_SELECT_TAB_CHART("查询栏目下对应的统计分析 + 自定义图表","查询"),
	/**
	 * 查询分类下的栏目和被置顶的统计分析 + 自定义图表
	 */
	COLUMN_SELECT_PAGE_TOP_CHART("查询分类下的栏目和被置顶的统计分析 + 自定义图表","查询"),
	/**
	 * 置顶图表
	 */
	COLUMN_TOP_CHART("置顶图表","修改"),

	// ------------预警--------------
	// ------------舆情报告--------------
	/**
	 * 新增和修改报告模板
	 */
	REPORT_SAVE_TEMPLATE("新增和修改报告模版","修改"),
	/**
	 * 专报：新增和修改报告模板
	 */
	REPORT_SAVE_SPECIAL_TEMPLATE("专报：新增和修改报告模版","修改"),
	/**
	 * 删除报告模板
	 */
	REPORT_DELETE_TEMPLATE("删除报告模版","删除"),
	/**
	 * 修改模板顺序
	 */
	REPORT_T_ORDERSET("修改模版顺序","修改"),
	/**
	 * 添加到我的资源池
	 */
	REPORT_ADD_REPORT_RESOURCE("添加到我的资源池","添加"),
	/**
	 * 删除资源池中的资源
	 */
	REPORT_DELETE_REPORT_RESOURCE("删除资源池中的资源","删除"),
	/**
	 * 生成报告：日报、周报、月报
	 */
	REPORT_CREATE("生成报告：日报、周报、月报","下载"),
	/**
	 * 生成报告：专报
	 */
	REPORT_CREATE_SPECIAL("生成报告：专报","下载"),
	/**
	 * 下载报告
	 */
	REPORT_DOWNLOAD("下载报告","下载"),
	/**
	 * 删除报告
	 */
	REPORT_DELETE_REPORT("删除报告","删除"),
	/**
	 * 添加专报分组
	 */
	REPORT_ADD_GROUP("添加专报分组","添加"),
	/**
	 * 删除专报列表数据中的1条数据
	 */
	REPORT_DEL_SPECIAL_RESOURCE("删除专报列表数据中的1条数据","删除"),
	/**
	 * 编辑专题报图表类模块下面的文字
	 */
	REPORT_UPDATE_SPECIAL_RESOURCE("编辑专题报图表类模块下面的文字","修改"),
	/**
	 * 创建日报、周报、月报时编辑概述统计
	 */
	REPORT_UPDATE_SPECIAL_OVER_VIEW("创建日报、周报、月报时编辑概述统计","修改"),
	/**
	 * 清空所有报告资源
	 */
	REPORT_DEL_ALL_RES("清空所有报告资源","删除"),
	/**
	 * 删除专报分组
	 */
	REPORT_DEL_SPECIAL_GROUP("删除专报分组","删除"),
	/**
	 * 编辑专报分组名称
	 */
	REPORT_EDIT_SPECIAL_GROUP("编辑专报分组名称","修改"),
	// ------------用户后台--------------
	/**
	 * 添加机构
	 */
	ORGANIZATION_ADD("添加机构","添加"),
	/**
	 * 删除机构
	 */
	ORGANIZATION_DELETE("删除机构","删除"),
	/**
	 * 修改机构
	 */
	ORGANIZATION_UPDATE("修改机构","修改"),
	/**
	 * 修改机构状态
	 */
	ORGANIZATION_UPDATE_STATUS("修改机构状态","修改"),
	/**
	 * 添加用户
	 */
	USER_ADD("添加用户","添加"),
	/**
	 * 修改用户
	 */
	USER_UPDATE("修改用户","修改"),
	/**
	 * 删除用户
	 */
	USER_DELETE("删除用户","删除"),
	/**
	 * 修改用户状态
	 */
	USER_UPDATE_STATUS("修改用户状态","修改"),
	/**
	 * 修改用户唯一登录
	 */
	USER_UPDATE_SAME_TIME_LOGIN("修改用户唯一登录","修改"),
	/**
	 * 修改密码
	 */
	USER_UPDATE_PASSWORD("修改密码","修改"),
	/**
	 * 重置密码
	 */
	USER_RESET_PASSWORD("重置密码","修改"),
	/**
	 * 添加运维管理的机构
	 */
	USER_ADD_HOLD_ORGANIZATION("添加运维管理的机构","添加"),
	/**
	 * 去掉运维管理的机构
	 */
	USER_DELETE_HOLD_ORGANIZATION("去掉运维管理的机构","删除"),

	// ------------登录--------------
	/**
	 * 账号密码登录
	 */
	USERNAME_LOGIN("账号密码登录","登录"),
	/**
	 * 表单登录
	 */
	FORM_LOGIN("第三方表单登录","登录"),
	/**
	 * 微信登录
	 */
	WECHAT_LOGIN("微信登录","登录"),
	/**
	 * 登出
	 */
	LOGOUT("退出","退出"),
	/**
	 * 强制下线
	 */
	KICKOUT("强制下线","强制下线"),
	/**
	 * 模拟登录获取token
	 */
	SIMULATEDLOGINTOKEN("模拟登录获取token","获取token"),
	// ========舆情报告 极简模式 自定义专题 操作 BEGIN===
	/**
	 * 添加自定义专题
	 */
	CUSTOM_SPECIAL_ADD("添加自定义专题","添加"),

	/**
	 * 修改自定义专题
	 */
	CUSTOM_SPECIAL_UPDATE("修改自定义专题","修改"),

	/**
	 * 查某一个自定义专题详情
	 */
	CUSTOM_SPECIAL_DETAIL("查某一个自定义专题详情","查询"),

	/**
	 * 删除某个自定义专题
	 */
	CUSTOM_SPECIAL_DELETE("删除某个自定义专题","删除"),

	// ========舆情报告 极简模式 自定义专题 操作 END===

	// ========舆情报告 极简模式 报告有关操作 BEGIN==========

	/**
	 * 极简报告 专题分析、素材库、模板、报告列表操作
	 */
	SIMPLER_REPORT_LIST("极简报告查询列表操作","查询"),

	/**
	 * 极简报告生成第一步
	 */
	SIMPLER_REPORT_CALCULATE("极简报告生成报告","下载"),

	/**
	 * 极简报告生成第二步，返回前端图表数据为处理成base64
	 */
	SIMPLER_REPORT_IMGDATA("极简报告生成报告","下载"),

	/**
	 * 简报告生成第三步，拿到前端处理的base64生成word文档
	 */
	SIMPLER_REPORT_GENERATION("极简报告生成报告","下载"),

	/**
	 * 极简报告某个报告详情
	 */
	SIMPLER_REPORT_DETAIL("极简报告查看某个报告详情操作","查询"),

	/**
	 * 极简报告删除历史报告中的资源
	 */
	SIMPLER_REPORT_DELRESOURCES("极简报告删除历史报告中的资源操作","删除"),

	/**
	 * 编辑历史报告中的资源
	 */
	SIMPLER_REPORT_UPDATERESOURCE("编辑历史报告中的资源","修改"),

	/**
	 * 极简报告修改
	 */
	SIMPLER_REPORT_UPDATE("极简报告修改操作","修改"),

	/**
	 * 极简报告完成修改后重新生成报告
	 */
	SIMPLER_REPORT_REBUILD("极简报告重新生成报告操作","下载"),

	/**
	 * 极简报告下载
	 */
	SIMPLER_REPORT_DOWNLOAD("极简报告下载操作","下载"),

	/**
	 * 极简报告删除报告
	 */
	SIMPLER_REPORT_DELETE("极简报告删除操作","删除"),

	/**
	 * 新增和修改报告
	 */
	SIMPLER_REPORT_TEMPLATE_ADD_UPDATE("极简报告新增和修改报告模版操作","修改"),

	/**
	 * 极简报告模板查询详情
	 */
	SIMPLER_REPORT_TEMPLATE_DETAIL("极简报告模版查询详情操作","查询"),

	/**
	 * 极简报告模板删除
	 */
	SIMPLER_REPORT_TEMPLATE_DELETE("极简报告模版删除操作","删除"),
	// ============舆情报告 极简模式 报告操作 END=========

	//  ----------将选中的数据导出到excel-START----------------
	EXPORT_EXCEL_SELECT_DATA("将选中的数据导出到excel","下载"),


	// ================舆情报告 极简模式 素材库操作 BEGIN======
	/**
	 * 极简报告素材库查询列表
	 */
	SIMPLER_MATERIAL_LIST("极简报告素材库查询列表操作","查询"),

	/**
	 * 极简报告删除某个素材库
	 */
	SIMPLER_MATERIAL_DELETE("极简报告删除某个素材库操作","删除"),

	/**
	 * 极简报告素材库添加/修改
	 */
	SIMPLER_MATERIAL_ADD_UPDATE("极简报告素材库添加/修改操作","修改"),

	/**
	 * 极简报告添加素材库资源
	 */
	SIMPLER_MATERIAL_ADDRESOURCE("极简报告添加素材库资源操作","修改"),

	/**
	 * 极简报告查询素材库资源列表
	 */
	SIMPLER_MATERIAL_LISTRESOURCE("极简报告查询素材库资源列表操作","查询"),

	/**
	 * 日常监测图表导出
	 */
	COLUMN_DATA_EXPORT("日常监测图表导出","下载"),

	/**
	 * 相似文章列表
	 */
	LIST_SIM("相似文章列表","查询"),

	/**
	 * 预警规则列表
	 */
	ALERT_RULE_LIST("预警规则列表","查询"),

	/**
	 * 新建预警规则
	 */
	ALERT_RULE_ADD("新建预警规则","新建"),

	/**
	 * 修改预警规则
	 */
	ALERT_RULE_UPDATE("修改预警规则","修改"),

	/**
	 * 删除预警规则
	 */
	ALERT_RULE_DELETE("删除预警规则","修改"),

	/**
	 * 手动发送预警
	 */
	ARTIFICIAL_ALERT("手动发送预警","预警"),

	/**
	 * 开启关闭预警
	 */
	ONOROFF_ALERT("开启关闭预警","修改"),

	/**
	 * 预警右侧信息列表
	 */
	ALERT_RULE_LIST_DOCUMENT("预警右侧信息列表","查询"),

	/**
	 * 查询所有报告
	 */
	LIST_ALL_REPORT("查询所有报告","查询"),

	/**
	 * 我的收藏列表
	 */
	DRAR_LIST("我的收藏列表","查询"),

	/**
	 * 新增收藏
	 */
	ADD_FAVOURITES("新增收藏","新增"),

	/**
	 * 取消收藏
	 */
	DEL_FAVOURITES("取消收藏","修改"),

	/**
	 * 极简报告删除素材库下资源
	 */
	SIMPLER_MATERIAL_DELRESOURCE("极简报告删除素材库下资源操作","删除极简报告");
	// ------------hybase操作--------------

	// 值
	private String value;
	private String operator;
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年7月25日 谷泽昊 creat
 */