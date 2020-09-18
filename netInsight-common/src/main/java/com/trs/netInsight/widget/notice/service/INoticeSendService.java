/*
 * Project: netInsight
 * 
 * File Created at 2018年2月1日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.notice.service;

import com.trs.netInsight.handler.result.Message;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;

import java.util.Map;

/**
 * 发送通知类
 * 
 * @Type INoticeSendService.java
 * @author 谷泽昊
 * @date 2018年2月1日 下午1:56:40
 * @version
 */
public interface INoticeSendService {
	/**
	 * 发送预警
	 * @date Created at 2018年2月1日 下午1:58:32
	 * @Author 谷泽昊
	 * @param send
	 * @param template
	 * @param subject
	 * @param map
	 * @param receivers
	 * @param userId
	 * @param sendType 发送类型 自动 auto、手动 manual
	 * @return
	 */
	public Message sendAll(SendWay send, String template, String subject, Map<String, Object> map, String receivers,
                           String userId, AlertSource sendType);


	String sendAlert(AlertSource sendType,String subject,String userId,
			  String sendWays, String receivers,Map<String, Object> map,Boolean cutSendData);
	String sendAlert(AlertRule alertRule, Map<String, Object> sendMap);
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年2月1日 谷泽昊 creat
 */