/*
 * Project: netInsight
 * 
 * File Created at 2018年1月26日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.alert.service;

import org.springframework.data.domain.Page;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.widget.alert.entity.AlertSendWeChat;

/**
 * 预警存入类service
 * 
 * @Type IWeixinAlertService.java
 * @author 谷泽昊
 * @date 2018年1月26日 下午2:30:13
 * @version
 */
public interface ISendAlertService {

	/**
	 * 添加
	 * 
	 * @date Created at 2018年1月26日 下午2:36:12
	 * @Author 谷泽昊
	 * @param weixinAlert
	 * @return
	 */
	public AlertSendWeChat add(AlertSendWeChat weixinAlert) throws OperationException;

	/**
	 * 删除
	 * 
	 * @date Created at 2018年1月26日 下午2:37:01
	 * @Author 谷泽昊
	 * @param weixinAlert
	 */
	public void delete(AlertSendWeChat weixinAlert);

	/**
	 * 查询
	 * 
	 * @date Created at 2018年1月26日 下午2:37:26
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 */
	public AlertSendWeChat findOne(String id) throws OperationException;

	/**
	 * 查询
	 * 
	 * @date Created at 2018年1月26日 下午2:41:22
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @param name
	 * @return
	 */
	public Page<AlertSendWeChat> pageList(int pageNo, int pageSize, String name);

	/**
	 * 修改
	 * 
	 * @date Created at 2018年3月14日 下午4:09:14
	 * @Author 谷泽昊
	 * @param findOne
	 */
	public void edit(AlertSendWeChat findOne);
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月26日 谷泽昊 creat
 */