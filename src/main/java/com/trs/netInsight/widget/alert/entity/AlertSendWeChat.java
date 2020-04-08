/*
 * Project: netInsight
 * 
 * File Created at 2018年1月31日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.alert.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 微信发送后把sid保存到数据库里
 * 
 * @Type WeixinAlert.java
 * @author 谷泽昊
 * @date 2018年1月31日 下午2:47:52
 * @version
 */
@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`alert_send_wechat`")
public class AlertSendWeChat extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@Column(name = "`ids`", columnDefinition = "LONGTEXT")
	private String ids;
	/**
	 * 
	 */
	@Column(name = "`rule_name`")
	private String ruleName;
	
	/**
	 * 预警时间
	 */
	@Column(name = "`alert_time`")
	private String alertTime;
	
	/**
	 * 预警方式
	 */
	@Column(name = "`send_way`")
	private SendWay sendWay;
	/**
	 * 数量
	 */
	@Column(name = "`size`")
	private int size;


}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月31日 谷泽昊 creat
 */