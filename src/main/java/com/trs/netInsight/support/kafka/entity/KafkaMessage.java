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
package com.trs.netInsight.support.kafka.entity;

import java.io.Serializable;

import com.trs.netInsight.support.kafka.entity.enums.KafkaMessageHeaderEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @Type KafkaMessage.java
 * @author 谷泽昊
 * @date 2018年11月15日 下午3:08:50
 * @version
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaMessage implements Serializable, Cloneable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	/**
	 * 统一消息头
	 */
	protected KafkaMessageHeaderEnum messageHeaderEnum;

	/**
	 * 内容
	 */
	private Object data;
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