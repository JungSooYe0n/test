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
 * @Desc 相似度计算实体
 * @author yan.changjiang
 * @date 2018年3月6日 下午4:47:07
 * @version
 */
@Data
@NoArgsConstructor
public class AnalysisValue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2449066500472711088L;

	/**
	 * 数据内容id
	 */
	private String msgId;

	/**
	 * 相似度
	 */
	private Float simv;

	public AnalysisValue(String msgId, Float simv) {
		super();
		this.msgId = msgId;
		this.simv = simv;
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