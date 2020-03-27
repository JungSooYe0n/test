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
package com.trs.netInsight.widget.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.result.Message;

/**
 * 权限Controller
 * @Type PermissionController.java 
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:42:53
 * @version 
 */
@RestController
@RequestMapping(value={"/permission"})
public class PermissionController {

	public Message pageList(){
		
		return null;
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