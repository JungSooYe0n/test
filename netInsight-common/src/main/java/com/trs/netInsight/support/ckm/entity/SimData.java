/*
 * Project: netInsight
 * 
 * File Created at 2018年3月6日
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

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点相似度语料实体
 * 
 * @author yan.changjiang
 * @date 2018年3月6日 下午4:51:52
 * @version
 */
@Data
@NoArgsConstructor
public class SimData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 185967839655454376L;

	/**
	 * 数据内容id
	 */
	private String msgId;

	/**
	 * 相关地域
	 */
	private String area;

	/**
	 * 相关人物
	 */
	private String people;

	/**
	 * 相关机构
	 */
	private String unit;

	/**
	 * 关键字
	 */
	private String keyword;

	/**
	 * 正文
	 */
	private String content;

	/**
	 * 权重
	 */
	private Double weights;

	public SimData(String msgId, String area, String people, String unit, String keyword, String content,
			Double weights) {
		super();
		this.msgId = msgId;
		this.area = area;
		this.people = people;
		this.unit = unit;
		this.keyword = keyword;
		this.content = content;
		this.weights = weights;
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月6日 yan.changjiang creat
 */