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
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @Desc 文本聚类实例,与{@link IdText}联合使用
 * @author yan.changjiang
 * @date 2018年3月6日 下午5:26:35
 * @version
 */
@Getter
@ToString
@EqualsAndHashCode
public class TextInstance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2167109210221212779L;

	/**
	 * 聚类标签(key)
	 */
	public final String label;

	/**
	 * 聚类依据(means)
	 */
	public final List<String> tokens;

	public TextInstance(List<String> tokens) {
		super();
		this.label = null;
		this.tokens = tokens;
	}

	public TextInstance(String label, List<String> tokens) {
		super();
		this.label = label;
		this.tokens = tokens;
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