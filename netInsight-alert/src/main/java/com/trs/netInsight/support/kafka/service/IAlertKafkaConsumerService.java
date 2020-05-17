/*
 * Project: alertnetinsight
 * 
 * File Created at 2018年11月15日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.kafka.service;

import java.util.List;

import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertKafkaSend;
import com.trs.netInsight.widget.alert.entity.AlertRuleBackups;

/**
 * 预警kakfa类
 * @Type IAlertKafkaConsumer.java 
 * @author 谷泽昊
 * @date 2018年11月15日 下午3:18:35
 * @version 
 */
public interface IAlertKafkaConsumerService {

	/**
	 * 发送预警
	 * @date Created at 2018年11月15日  下午4:15:48
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param alertKafkaSend
	 */
	public void send(AlertKafkaSend alertKafkaSend);
	
	/**
	 * 发送
	 * @date Created at 2018年11月15日  下午4:22:43
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param list
	 * @param alertRuleBackups
	 */
	public void send(List<AlertEntity> list, AlertRuleBackups alertRuleBackups);
}


/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年11月15日 谷泽昊 creat
 */