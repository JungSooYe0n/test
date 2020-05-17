/*
 * Project: netInsight
 * 
 * File Created at 2018年11月7日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.log.factory.systemlogoperation;

import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.factory.AbstractSystemLogOperation;
import com.trs.netInsight.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 日常监测日志实现类
 * 
 * @Type ColumnSystemLogOperation.java
 * @author 谷泽昊
 * @date 2018年11月7日 下午5:53:40
 * @version
 */
public class LoginSystemLogOperation extends AbstractSystemLogOperation {

	@Override
	public String getOperationPosition(Map<String, String[]> parameterMap, String operationPosition,
			SystemLogOperation systemLogOperation) {
		// 获取key
		String oldStr = getKey(operationPosition);
		String newStr = null;
		StringBuffer buffer = new StringBuffer();
		newStr = buffer.toString();
		String replace = StringUtils.replace(operationPosition, oldStr, newStr);
		return StringUtil.getKeyNameFromParam(replace, parameterMap, "@");
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年11月7日 谷泽昊 creat
 */