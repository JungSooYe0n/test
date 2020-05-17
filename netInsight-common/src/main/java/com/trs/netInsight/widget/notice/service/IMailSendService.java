/*
 * Project: netInsight
 * 
 * File Created at 2018年1月30日
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

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.Message;

import java.util.Map;

/**
 * 
 * @Type IMailSendService.java
 * @author 谷泽昊
 * @date 2018年1月30日 下午3:11:08
 * @version
 */
public interface IMailSendService {
	/**
	 * 发送邮件
	 * @date Created at 2018年1月30日  下午3:23:05
	 * @Author 谷泽昊
	 * @param template
	 * @param subject
	 * @param json
	 * @param receivers
	 * @return
	 * @throws TRSException
	 */
//	public Message sendEmail(String template, String subject, String json, String receivers) ;
	public Message sendEmail(String template, String subject, Map<String, Object> map, String receivers) ;
	
	boolean sendMail(String subject, String text, String[] receivers) throws Exception;
	boolean sendOneMail(String subject, String text, String fromAddress) throws Exception;
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月30日 谷泽昊 creat
 */