/*
 * Project: netInsight
 * 
 * File Created at 2018年8月29日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.user.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户和结构状态
 * 
 * @Type Status.java
 * @author 谷泽昊
 * @date 2018年8月29日 下午4:13:22
 * @version
 */
@Getter
@AllArgsConstructor
public enum Status {
	/**
	 * 正常-0
	 */
	normal("0"),
	/**
	 * 冻结-1
	 */
	frozen("1"),
	/**
	 * 异常-2
	 */
	abnormal("2");
	// 值
	private String value;
	
	/**
	 * 根据值获得状态
	 * @date Created at 2018年9月14日  下午4:25:07
	 * @Author 谷泽昊
	 * @param value
	 * @return
	 */
	public static Status getStatusByValue(String value){
		if(StringUtils.isBlank(value)){
			return null;
		}
		for (Status status : Status.values()) {
			if(StringUtils.equals(status.getValue(), value)){
				return status;
			}
		}
		return null;
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年8月29日 谷泽昊 creat
 */