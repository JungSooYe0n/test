/*
 * Project: netInsight
 * 
 * File Created at 2018年7月25日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.log.handler;

import com.trs.netInsight.support.log.entity.enums.DepositPattern;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志注解
 * 
 * @Type Log.java
 * @author 谷泽昊
 * @date 2018年7月25日 下午2:59:48
 * @version
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

	/**
	 * 日志类型
	 * 
	 * @date Created at 2018年7月25日 下午3:17:11
	 * @Author 谷泽昊
	 * @return
	 */
	SystemLogType systemLogType();

	/**
	 * 具体操作
	 * 
	 * @date Created at 2018年7月25日 下午3:31:28
	 * @Author 谷泽昊
	 * @return
	 */
	SystemLogOperation systemLogOperation();

	/**
	 * 方法描述
	 * 
	 * @date Created at 2018年7月25日 下午3:19:19
	 * @Author 谷泽昊
	 * @return
	 */
	String methodDescription() default "";

	/**
	 * 存入方式 -默认为mysql
	 * 
	 * @date Created at 2018年7月25日 下午3:17:04
	 * @Author 谷泽昊
	 * @return
	 */
	DepositPattern depositPattern() default DepositPattern.MYSQL;

	/**
	 * 操作位置：需要以 ${id}的方式把id传过来，id为自定义变量，其他参数用@{name}，将会把参数直接替换
	 * 
	 * @date Created at 2018年11月7日 下午6:42:50
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @return
	 */
	String systemLogOperationPosition();

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年7月25日 谷泽昊 creat
 */