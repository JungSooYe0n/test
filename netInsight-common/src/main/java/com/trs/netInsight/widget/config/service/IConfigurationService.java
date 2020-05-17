/*
 * Project: netInsight
 * 
 * File Created at 2018年9月18日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.config.service;

import com.trs.netInsight.widget.config.entity.Configuration;

/**
 * 
 * @Type IConfigurationService.java
 * @author 谷泽昊
 * @date 2018年9月18日 下午4:26:51
 * @version
 */
public interface IConfigurationService {
	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项,如果organizationId为空，则自定义查询，否则 则查询这个机构的
	 * 
	 * @date Created at 2018年9月18日 下午4:31:01
	 * @Author 谷泽昊
	 * @param key
	 * @return
	 */
	public Configuration getConfig(String key, String organizationId);

	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值<br>
	 * 
	 * @date Created at 2018年9月18日 下午4:30:56
	 * @Author 谷泽昊
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getConfigValue(String key, String defaultValue, String organizationId);

	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值 不考虑机构 如申请试用 是不存在机构和用户的
	 *
	 * @param key
	 * @return
	 */
	public String getConfigValueByKey(String key);

	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值,并转换为<code>int</code>
	 * 
	 * @date Created at 2018年9月18日 下午4:30:52
	 * @Author 谷泽昊
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getConfigValueAsInt(String key, int defaultValue, String organizationId);

	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值,并转换为<code>long</code>
	 * 
	 * @date Created at 2018年9月18日 下午4:30:45
	 * @Author 谷泽昊
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public long getConfigValueAsLong(String key, long defaultValue, String organizationId);

	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值,并转换为 boolean
	 * 
	 * @date Created at 2018年9月18日 下午4:30:10
	 * @Author 谷泽昊
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public boolean getConfigValueAsBoolean(String key, boolean defaultValue, String organizationId);

	/**
	 * 修改当前机构的配置,如果当前登录用户为超管,则修改全局配置
	 * 
	 * @date Created at 2018年9月18日 下午4:29:44
	 * @Author 谷泽昊
	 * @param key
	 * @param value
	 */
	public boolean updateConfig(String key, String value);

	/**
	 * 修改指定机构的配置
	 * 
	 * @date Created at 2018年9月18日 下午4:29:14
	 * @Author 谷泽昊
	 * @param key
	 * @param value
	 * @param organizationId
	 */
	public boolean updateConfig(String key, String value, String organizationId);
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年9月18日 谷泽昊 creat
 */