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

import com.trs.netInsight.support.log.factory.systemlog.MysqlSystemLog;
import lombok.Getter;

/**
 * 日志存入模式
 * 
 * @Type DepositPattern.java
 * @author 谷泽昊
 * @date 2018年7月25日 下午3:06:00
 * @version
 */
@Getter
public enum DepositPattern {

	// redis
	REDIS(MysqlSystemLog.class),
	// 文件
	FILE(MysqlSystemLog.class),
	// mysql
	MYSQL(MysqlSystemLog.class);

	private Class<?> name;

	private DepositPattern(Class<?> clazz) {
		this.name = clazz;
	}

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