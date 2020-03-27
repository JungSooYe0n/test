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

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Desc 文本(with id)key-value格式
 * @author yan.changjiang
 * @date 2018年3月5日 下午7:10:03
 * @version 
 */
@Data
@NoArgsConstructor
public class IdText implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4684698826971468140L;

	/**
	 * 文章id
	 */
	private String id;
	
	/**
	 * 内容
	 */
	private String text;

	public IdText(String id, String text) {
		super();
		this.id = id;
		this.text = text;
	}
	
}


/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月5日 yan.changjiang create
 */