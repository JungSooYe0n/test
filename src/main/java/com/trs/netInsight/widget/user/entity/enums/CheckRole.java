/*
 * Project: netInsight
 * 
 * File Created at 2018年8月29日
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

/**
 * 用户权限
 * @Type CheckRole.java 
 * @author 谷泽昊
 * @date 2018年8月29日 下午4:19:28
 * @version 
 */
public enum CheckRole {
	/**
	 * 超级管理员
	 */
	SUPER_ADMIN,
	/**
	 * 机构管理员
	 */
	ROLE_ADMIN,

	/**
	 * 普通用户
	 */
	ROLE_ORDINARY,

	/**
	 * 平台管理员
	 */
	ROLE_PLATFORM,

	/**
	 * 访客（主要针对大屏跳转网察页面，需要手动更改MySQL字段值）
	 * 2020-02-11
	 */
	ROLE_VISITOR
}


/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年8月29日 谷泽昊 creat
 */