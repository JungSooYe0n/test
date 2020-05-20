/*
 * Project: netInsight
 *
 * File Created at 2017年11月24日
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

/**
 * @Type ExcelConst.java
 * @Desc excel相关常量类
 * @author yan.changjiang
 * @date 2017年11月24日 下午4:45:30
 * @version
 */
public class ExcelConst {

	/**
	 * sheet页名称
	 */
	public static final String EXCEL_SHEET = "NETINSIGHT";

	/**
	 * 导出头
	 */
//	public static final String[] HEAD_VALUE = { "序号", "标题", "地址", "媒体类型", "媒体名称", "发布时间" };
	public static final String[] HEAD_VALUE = { "序号", "标题", "地址", "媒体名称", "发布时间" };

	public static final String[] HEAD_VALUE_MIX = { "序号", "标题","地址", "来源", "站点", "发布时间" };
	//序号  标题  正文  链接  媒体类型  媒体名称  发布时间     

	//柱状图和饼图共用的表格头
	public static final String[] HEAD_PIE_BAR = { "媒体来源", "信息数量"};
	public static final String[] WEIBO_HOT_TOPIC = { "微博话题", "涉及量"};
	public static final String[] EMOTION_DATA = { "情感", "数量"};
	public static final String[] DETONATE_POINT = { "引爆用户", "二次转发数"};
	public static final String[] WEBSITE = { "网站", "信息数量"};
	public static final String[] XINWEN = { "新闻", "信息数量"};
	public static final String[] WEIBO = { "微博", "信息数量"};
	public static final String[] WEIXIN = { "微信", "信息数量"};
	public static final String[] KEHUDUAN = { "客户端", "信息数量"};
	public static final String[] LUNTAN = { "论坛", "信息数量"};
	public static final String[] BOKE = { "博客", "信息数量"};
	public static final String[] DIANZIBAO = { "电子报", "信息数量"};
	public static final String[] JINGWAIWANGZHAN = { "境外网站", "信息数量"};
	public static final String[] TWITTER = { "Twitter", "信息数量"};
	public static final String[] FACEBOOK = { "FaceBook", "信息数量"};
	//地域图的表格头
	public static final String[] HEAD_MAP = { "地名", "信息数量"};

	//词云图的表格头
	public static final String[] HEAD_WORDCLOUD = { "词语", "所属分组", "信息数量"};
	//折线图随着所选时间是变动的，所以代码中做动态设置

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月24日 Administrator creat
 */