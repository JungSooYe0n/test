/*
 * Project: netInsight
 * 
 * File Created at 2018年4月11日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.fts.entity;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 插入文章类
 * 
 * @Type FtsDocumentInsert.java
 * @author 谷泽昊
 * @date 2018年4月11日 下午3:11:34
 * @version
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FtsDocumentInsert {

	/**
	 * 作者
	 */
	private String authors;

	/**
	 * 频道
	 */
	private String channel;

	/**
	 * 正文
	 */
	private String content;

	/**
	 * 站点
	 */
	private String sitename;

	/**
	 * 文章来源
	 */
	private String urlname;

	/**
	 * 时间 年月日时分秒
	 */
	private Date urltime;

	/**
	 * 标题
	 */
	private String urltitle;

	/**
	 * 正负面
	 */
	private String appraise;
	/**
	 * 行业
	 */
	private String industry;
	/**
	 * 分组
	 * */
	private String groupname;

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年4月11日 谷泽昊 creat
 */