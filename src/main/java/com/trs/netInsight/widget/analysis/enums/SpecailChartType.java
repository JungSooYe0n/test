/*
 * Project: netInsight
 * 
 * File Created at 2018年2月28日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.analysis.enums;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.FtsFieldConst;

import lombok.Getter;

/**
 * @Desc 无锡图表类型枚举类
 * @author yan.changjiang
 * @date 2018年2月28日 下午1:04:21
 * @version
 */
@Getter
public enum SpecailChartType {

	STATTOTAL	  ("stattotal", 		"相关文章统计", 			   "ALL", 						new String[0]),
	
	WEBCOUNT	  ("webCount", 			"网站统计", 				   "ALL", 						new String[]{FtsFieldConst.FIELD_GROUPNAME}),
	
	AREA		  ("area", 				"地域分布", 				   Const.HYBASE_NI_INDEX, 		new String[]{FtsFieldConst.FIELD_CATALOG_AREA}),
	
	VIA			  ("via", 				"渠道偏好", 				   Const.WEIBO, 				new String[]{FtsFieldConst.FIELD_VIA}),
	
	ACTIVE_LEVEL  ("activeLevel", 		"媒体活跃等级", 			   Const.HYBASE_NI_INDEX, 		new String[]{FtsFieldConst.FIELD_SITENAME}),
	
	WEIBOOPTION	  ("weiboOption", 		"微博情感分析", 			   Const.WEIBO, 				new String[]{ESFieldConst.IR_APPRAISE}),
	
	TENDENCY      ("tendency", 	    	"网民/媒体参与趋势分布", 	   "ALL", 						new String[0]),

	TRENDMESSAGE  ("trendMessage", 	    "信息走势图", 		       "ALL", 						new String[]{FtsFieldConst.FIELD_GROUPNAME}),

	VOLUME		  ("volume", 			"情感走势", 				   Const.HYBASE_NI_INDEX, 		new String[]{ESFieldConst.IR_APPRAISE}),
	
	WORDCLOUD	  ("wordCloud", 		"词云", 					   "ALL", 						new String[]{FtsFieldConst.FIELD_TITLE,FtsFieldConst.FIELD_CONTENT}),

	USERVIEWS	  ("userViews", 		"网友观点", 				   Const.WEIBO,				    new String[0]),

	TOPICEVOEXPLOR("topicEvoExplor", 	"话题演变探索", 			    Const.WEIBO, 				new String[0]);

	/**
	 * 图表类型
	 */
	private String type;

	/**
	 * 图表类型名称
	 */
	private String name;

	/**
	 * 数据来源(多数据来源使用'ALL',指定来源直接使用对应库名)
	 */
	private String source;
	
	/**
	 * 图表数据参数对应字段
	 */
	private String[] xTpyeField;

	private SpecailChartType(String type, String name, String source, String[] xTpyeField) {
		this.type = type;
		this.name = name;
		this.source = source;
		this.xTpyeField = xTpyeField;
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年2月28日 Administrator creat
 */