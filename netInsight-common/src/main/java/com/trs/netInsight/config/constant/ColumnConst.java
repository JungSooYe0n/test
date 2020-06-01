/*
 * Project: netInsight
 * 
 * File Created at 2017年11月27日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.config.constant;

import com.trs.netInsight.widget.column.entity.Columns;
import com.trs.netInsight.widget.home.entity.enums.ColumnType;

/**
 * @Type ColumnConst.java
 * @Desc 栏目相关常量
 * @author yan.changjiang
 * @date 2017年11月27日 下午5:02:06
 * @version
 *  @author 北京拓尔思信息技术股份有限公司
 */
public class ColumnConst {


	/**
	 * *****************************************************************************
	 * 栏目类型
	 */
	/**
	 * 栏目类型:无相似文章列表
	 */
	public static final String LIST_NO_SIM = "timeListInfo";

	/**
	 * 栏目类型:有相似文章列表
	 */
	public static final String LIST_SIM = "md5ListInfo";
	/**
	 * 微博热点话题榜
	 */
	public static final String HOT_TOPIC_SORT = "hotTopicSort";

	/**
	 * 栏目类型:图表-词云图
	 */
	public static final String CHART_WORD_CLOUD = "wordCloudChart";

	/**
	 * 栏目类型:图表-地图
	 */
	public static final String CHART_MAP = "mapChart";

	/**
	 * 栏目类型:图表-柱状图  默认的图
	 */
	public static final String CHART_BAR = "barGraphChart";
	/**
	 * 栏目类型:图表-柱状图横向的柱状图 - 日常监测特殊的图
	 */
	public static final String CHART_BAR_CROSS = "crossBarGraphChart";

	/**
	 * 栏目类型:图表-折线图
	 */
	public static final String CHART_LINE = "brokenLineChart";

	/**
	 * 栏目类型:图表-饼图 - 环状  默认的图
	 */
	public static final String CHART_PIE = "pieChart";

	/**
	 * 栏目类型:图表-饼图 - 实心 前端页面展示不一样的图，后端无用
	 */
	public static final String CHART_PIE_EMOTION = "emotionPieChart";

	/**
	 * *****************************************************************************
	 * 对比类型
	 */
	/**
	 * 分类对比图类型-按来源分类
	 */
	public static final String CONTRAST_TYPE_GROUP = "contrastGroup";

	/**
	 * 分类对比图类型-按站点分类
	 */
	public static final String CONTRAST_TYPE_SITE = "contrastSite";
	/**
	 * 分类对比图类型-微信公众号对比
	 */
	public static final String CONTRAST_TYPE_WECHAT = "contrastWeChat";

	/**
	 * 分类对比图类型- 地图 - 文章命中
	 */
	public static final String CONTRAST_TYPE_HIT_ARTICLE = "hitArticle";
	/**
	 * 分类对比图类型- 地图 - 媒体属地
	 */
	public static final String CONTRAST_TYPE_MEDIA_AREA = "mediaArea";

	/**
	 * 分类对比图类型-按情感分类
	 */
	public static final String CONTRAST_TYPE_EMOTION = "contrastEmotion";

	/**
	 * 分类对比图类型-按来源分类
	 */
	public static final String CONTRAST_TYPE_TOPIC = "contrastTopic";


















	/**
	 * 预警列表栏目
	 */
	static Columns CO_ALERT_LIST = new Columns("co_alert_list_id", "预警信息", ColumnType.ALERT_LIST);

	/**
	 * 预警渠道分布
	 */
	static Columns CO_ALERT_CHART = new Columns("co_alert_chart_id", "预警渠道分布", ColumnType.ALERT_CHART);

	/**
	 * 专项信息趋势
	 */
	static Columns CO_SPECIAL_TREND = new Columns("co_special_trend_id", "专项信息趋势", ColumnType.COUNT_AND_TREND);

	/**
	 * 专项监测展示
	 */
	static Columns CO_SPECIAL_CAROUSEL = new Columns("co_special_carousel_id", "专项监测展示", ColumnType.INFO_CAROUSEL);

	/**
	 * 全网地域热力图
	 */
	static Columns CO_DISTRICT = new Columns("co_district_id", "全网地域热力图", ColumnType.DISTRICT);

	/**
	 * 全网微博热点事件
	 */
	static Columns CO_HOT_MBLOG = new Columns("co_hot_mblog_id", "全网微博热点事件", ColumnType.HOT_MBLOG);

	/**
	 * 新闻综合榜单
	 */
	static Columns CO_NEWS_FOCUS = new Columns("co_news_focus_id", "新闻综合榜单", ColumnType.NEWS_FOCUS);

	/**
	 * 热搜关键词
	 */
	static Columns CO_SEARCH_W = new Columns("co_search_w_id", "热搜关键词", ColumnType.SEARCH_FOCUS_W);

	/**
	 * 热搜人物
	 */
	static Columns CO_SEARCH_P = new Columns("co_search_p_id", "热搜人物", ColumnType.SEARCH_FOCUS_P);

	/**
	 * 默认展示栏目：
	 */
	public static Columns[] DEFAULT_COLUMNS = { CO_ALERT_CHART, CO_ALERT_LIST, CO_SPECIAL_CAROUSEL, CO_SPECIAL_TREND,
			CO_DISTRICT, CO_HOT_MBLOG, CO_NEWS_FOCUS, CO_SEARCH_P, CO_SEARCH_W };

	/**
	 * 没有专题信息的展示栏目：
	 */
	public static Columns[] NO_SPECIAL_COLUMNS = { CO_ALERT_CHART, CO_ALERT_LIST, CO_DISTRICT, CO_HOT_MBLOG,
			CO_NEWS_FOCUS, CO_SEARCH_P, CO_SEARCH_W };

	/**
	 * 没有预警信息的展示栏目：
	 */
	public static Columns[] NO_ALERT_COLUMNS = { CO_SPECIAL_CAROUSEL, CO_SPECIAL_TREND, CO_DISTRICT, CO_HOT_MBLOG,
			CO_NEWS_FOCUS, CO_SEARCH_P, CO_SEARCH_W };

	/**
	 * 没有专题和预警信息的展示栏目：
	 */
	public static Columns[] BLANK_COLUMNS = { CO_DISTRICT, CO_HOT_MBLOG, CO_NEWS_FOCUS, CO_SEARCH_P, CO_SEARCH_W };


	/**
	 * 要删除没用的字段
	 */
	/**
	 * 栏目类型: 普通微博列表
	 */
	public static final String LIST_STATUS_COMMON = "listStatusCommon";

	/**
	 * 栏目类型: 普通微信列表
	 */
	public static final String LIST_WECHAT_COMMON = "listWeChatCommon";

	/**
	 * 栏目类型: Twitter
	 */
	public static final String LIST_TWITTER = "Twitter";

	/**
	 * 栏目类型: FaceBook
	 */
	public static final String LIST_FaceBook = "FaceBook";


	/**barGraphChartMeta
	 * 栏目类型：传统，微信，微博三库 联查列表
	 */
	public static final String LIST_CHAOS_DOCUMENT = "listDocumentChaos";


}

/**
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月27日 Administrator creat
 */