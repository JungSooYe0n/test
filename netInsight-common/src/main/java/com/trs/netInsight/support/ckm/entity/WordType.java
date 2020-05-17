/*
 * Project: netInsight
 * 
 * File Created at 2018年3月5日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.ckm.entity;

import lombok.Getter;

/**
 * @Desc 实体词类型枚举类
 * @author yan.changjiang
 * @date 2018年3月5日 上午11:24:39
 * @version
 */
@Getter
public enum WordType {
	
	PEOPLE_NAME			(1001,"人名"),
	REGIONAL_NAME		(1002,"地名"),
	ORGANIZATION_NAME	(1003,"机构名称"),
	
	DATE				(2001,"时间"),
	MSN					(2002,"MSN"),
	EMAIL				(2003,"邮箱"),
	QQ					(2004,"QQ"),
	CAR_LICENSE			(2005,"汽车牌照"),
	PASSPORT			(2006,"护照"),
	BANK_CARD			(2007,"银行卡号"),
	ID_CARD				(2008,"身份证号"),
	PHONE_NUMBER		(2009,"电话号码"),
	IP_ADDR				(2010,"ip地址"),
	WEBSITE				(2011,"网址"),
	
	CASE_NAME			(3000,"案件名称");
	
	/**
	 * 实体词类型码
	 */
	private int TYPE_CODE;
	
	/**
	 * 实体词说明
	 */
	private String DESC;

	private WordType(int type, String desc) {
		this.TYPE_CODE = type;
		this.DESC = desc;
	}
	
	
	
	

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 房屋出租相关等其他类型网察暂时未收录,详细实体词类型参见《TRS CKM SOAP Java API V6.0 用户手册》-TRS 实体识别应用编程接口介绍章节，并可依据其规定对本枚举类进行相应扩展。
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月5日 yan.changjiang create
 */