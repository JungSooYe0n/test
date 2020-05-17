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
package com.trs.netInsight.widget.alert.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.util.HttpUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.AlertSendWeChat;
import com.trs.netInsight.widget.alert.entity.repository.SendAlertRepository;
import com.trs.netInsight.widget.alert.service.ISendAlertService;
import com.trs.netInsight.widget.user.entity.User;

/**
 * 微信预警保存类
 * @Type SendAlertServiceImpl.java
 * @author 谷泽昊
 * @date 2018年1月26日 下午2:30:55
 * @version
 */
@Service
public class SendAlertServiceImpl implements ISendAlertService {

	@Value("${http.client}")
	private boolean httpClient;
	
	//这里边增删改查都要跨工程
	@Autowired
	private SendAlertRepository sendAlertRepository;
	
	@Value("${http.alert.netinsight.url}")
	private String alertNetinsightUrl;

	@Override
	public AlertSendWeChat add(AlertSendWeChat sendAlert) throws OperationException {
		if(httpClient){
			String url = alertNetinsightUrl+"/sendAlert/add";
//			sendAlert.setId(GUIDGenerator.generate(AlertSendWeChat.class));
			String doPost = HttpUtil.doPost(url, wechatToMap(sendAlert), "utf-8");
			ObjectMapper om = new ObjectMapper();
			AlertSendWeChat readValue = null;
			try {
				 //json转实体
				 readValue = om.readValue(doPost, AlertSendWeChat.class);
			} catch (IOException e) {
				throw new OperationException("微信结果保存报错", e);
			}
			return readValue;
		}else{
			return sendAlertRepository.save(sendAlert);
		}
	}

	@Override
	public void delete(AlertSendWeChat sendAlert) {
		if(httpClient){
			String url = alertNetinsightUrl+"/sendAlert/delete";
			String doPost = HttpUtil.doPost(url, wechatToMap(sendAlert), "utf-8");
		}else{
			sendAlertRepository.delete(sendAlert);
		}
	}

	@Override
	public AlertSendWeChat findOne(String id) throws OperationException {
		if(httpClient){
			String url = alertNetinsightUrl+"/sendAlert/findOne?id="+id;
			String doGet = HttpUtil.doGet(url, null);
			ObjectMapper om = new ObjectMapper();
			AlertSendWeChat readValue = null;;
			try {
				 //json转实体
				 readValue = om.readValue(doGet, AlertSendWeChat.class);
			} catch (IOException e) {
				throw new OperationException("微信预警查找报错", e);
			}
			return readValue;
		}else{
			return sendAlertRepository.findOne(id);
		}
	}

	@Override
	public Page<AlertSendWeChat> pageList(int pageNo, int pageSize, String name) {
		return null;
	}

	@Override
	public void edit(AlertSendWeChat findOne) {
		if(httpClient){
			String url = alertNetinsightUrl+"/sendAlert/edit";
			String doPost = HttpUtil.doPost(url, wechatToMap(findOne), "utf-8");
			System.err.println("edit");
		}else{
			sendAlertRepository.saveAndFlush(findOne);
		}
		
	}

	/**
	 * post请求前实体转Map
	 * @param sendAlert
	 * @return
	 */
	private Map<String,String> wechatToMap(AlertSendWeChat sendAlert){
		User user = UserUtils.getUser();
		Map<String,String> map = new HashMap<String,String>();
		map.put("id", sendAlert.getId());
		map.put("organizationId", user.getOrganizationId());
		map.put("userId", user.getId());
		map.put("userAccount", user.getUserName());
		map.put("createdUserId", sendAlert.getCreatedUserId());
		map.put("ids", sendAlert.getIds());
		map.put("ruleName", sendAlert.getRuleName());
		map.put("alertTime", sendAlert.getAlertTime());
		map.put("size", String.valueOf(sendAlert.getSize()));
		return map;
	}

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