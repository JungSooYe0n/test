/*
 * Project: netInsight
 * 
 * File Created at 2017年11月17日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.handler.result;

import com.trs.netInsight.util.ObjectUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 返回json类型
 * 
 * @Type Message.java
 * @author 谷泽昊
 * @date 2017年11月17日 下午6:33:08
 * @version
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message {

	/**
	 * 返回码
	 */
	private int code;

	/**
	 * 消息内容
	 */
	private String message;

	/**
	 * 数据
	 */
	private Object data;

	/**
	 * 获取数据
	 * 
	 * @param code
	 * @param messageStr
	 * @param data
	 * @return
	 */
	public static Message getMessage(int code, String message, Object data) {
//		if(ObjectUtil.isEmpty(data)){
//			code = 201;
//			message = "";
//		}
		return new Message(code, message, data);
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月17日 谷泽昊 creat
 */
