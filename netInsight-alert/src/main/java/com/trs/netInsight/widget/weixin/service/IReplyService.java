/*
 * Project: netInsight
 * 
 * File Created at 2018年1月25日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.weixin.service;

/**
 * 微信回复服务接口
 * 
 * @Type IReplyService.java
 * @author 谷泽昊
 * @date 2018年1月25日 下午4:03:48
 * @version
 */
public interface IReplyService {

	/**
	 * 解析微信发来的消息
	 * 
	 * @date Created at 2018年1月25日 下午4:04:25
	 * @Author 谷泽昊
	 * @param request
	 * @return
	 */
	public String processRequest(String messageBody);
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月25日 谷泽昊 creat
 */