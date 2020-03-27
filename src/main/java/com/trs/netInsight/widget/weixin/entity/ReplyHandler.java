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
package com.trs.netInsight.widget.weixin.entity;

import org.dom4j.DocumentException;

/**
 * 回复处理器接口
 * 
 * @Type ReplyHandler.java
 * @author 谷泽昊
 * @date 2018年1月25日 下午4:08:08
 * @version
 */
public abstract class ReplyHandler {
	/**
	 * 执行方法
	 * 
	 * @date Created at 2018年1月25日 下午4:08:42
	 * @Author 谷泽昊
	 * @param messageBody
	 * @return
	 * @throws DocumentException 
	 */
	public abstract String reply(String messageBody) throws DocumentException;
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