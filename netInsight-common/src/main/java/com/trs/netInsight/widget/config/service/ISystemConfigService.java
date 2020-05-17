/*
 * Project: netInsight
 * 
 * File Created at 2018年9月18日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.config.service;

import com.trs.netInsight.widget.config.entity.HybaseDatabaseConfig;
import com.trs.netInsight.widget.config.entity.SystemConfig;

/**
 * 
 * @Type ISystemConfigService.java
 * @author 张娅
 * @date 2020年1月6日
 * @version
 */
public interface ISystemConfigService {

	String queryOneHybaseDatabase(String type);

	HybaseDatabaseConfig queryHybaseDatabases();

	SystemConfig findSystemConfig();

	void updateSystemConfig(String orgName, String logoName, Boolean deleteOrg, Boolean needOperation);
	void updateHybaseDatabaseConfig(String traditional, String weibo, String weixin, String overseas, String insert, String sinaweiboUsers);
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年9月18日 谷泽昊 creat
 */