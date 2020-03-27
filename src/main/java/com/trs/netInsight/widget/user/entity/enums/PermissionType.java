/*
 * Project: netInsight
 * 
 * File Created at 2018年9月11日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.user.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * @Type PermissionType.java 
 * @author 谷泽昊
 * @date 2018年9月11日 下午4:56:39
 * @version 
 */
@Getter
@AllArgsConstructor
public enum PermissionType {
	/**
	 *  API 监控
	 */
	API("API"),
	/**
	 *  登录监控
	 */
	LOGIN("登录相关"),
	/**
	 *  专题
	 */
	SPECIAL("专题分析"),
	/**
	 *  日常监测
	 */
	COLUMN("日常监测"),
	/**
	 *  舆情报告
	 */
	REPORT("舆情报告"),
	/**
	 *  用户后台管理
	 */
	USER("后台管理"),
	/**
	 *  hybase文章管理
	 */
	HYBASE_ARTICLE("hybase文章管理"),
	/**
	 *  预警
	 */
	ALERT("预警中心"),
	/**
	 * 搜索
	 */
	SEARCH("搜索"),
	/**
	 *  其他
	 */
	OTHER("其他");
	private String value;
}


/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年9月11日 谷泽昊 creat
 */