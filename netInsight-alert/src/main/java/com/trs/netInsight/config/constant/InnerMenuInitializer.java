/*
 * Project: netInsight
 * 
 * File Created at 2017年11月20日
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

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 启动时 生成公众号菜单
 * 
 * @Type InnerUserInitializer.java
 * @author 谷泽昊
 * @date 2017年11月20日 下午6:11:42
 * @version
 */
@Component
public class InnerMenuInitializer {


	@PostConstruct
	public void initialize() throws Exception {
		
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月20日 谷泽昊 creat
 */