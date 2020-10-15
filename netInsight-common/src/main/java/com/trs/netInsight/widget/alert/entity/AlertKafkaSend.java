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
 *//*

package com.trs.netInsight.widget.alert.entity;

import lombok.*;

import java.io.Serializable;

*/
/**
 * 定时预警时发送到kafka的实体类
 * 
 * @Type AlertKafkaSend.java
 * @Desc
 * @author 北京拓尔思信息技术股份有限公司
 * @author 谷泽昊
 * @date 2018年11月15日 下午4:00:06
 * @version
 *//*

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AlertKafkaSend implements Serializable {

	*/
/**
	 * 
	 *//*

	private static final long serialVersionUID = 1L;

	*/
/**
	 * 备份规则
	 *//*

	private String alertRuleBackupsId;

	*/
/**
	 * 预警规则
	 *//*

	private String alertRuleId;

}

*/
/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月31日 谷泽昊 creat
 */
