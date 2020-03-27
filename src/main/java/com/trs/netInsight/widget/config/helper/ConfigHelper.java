/*
 * Project: netInsight
 * 
 * File Created at 2018年9月29日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.config.helper;

import com.trs.netInsight.util.SpringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.config.service.IConfigurationService;
import com.trs.netInsight.widget.config.service.impl.ConfigurationServiceImpl;

/**
 * 配置类 工具类
 * 
 * @Type ConfigHelper.java
 * @author 谷泽昊
 * @date 2018年9月29日 下午2:02:51
 * @version
 */
public class ConfigHelper {

	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值
	 * 
	 * @date Created at 2018年9月29日 下午2:06:11
	 * @Author 谷泽昊
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getConfigValue(String key, String defaultValue) {
		IConfigurationService configurationService = SpringUtil.getBean(ConfigurationServiceImpl.class);
		return configurationService.getConfigValue(key, defaultValue, UserUtils.getUser().getOrganizationId());
	}
	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值 不考虑机构 如申请试用 是不存在机构和用户的
	 *
	 * @param key
	 * @return
	 */
	public static String getConfigValueByKey(String key) {
		IConfigurationService configurationService = SpringUtil.getBean(ConfigurationServiceImpl.class);
		return configurationService.getConfigValueByKey(key );
}
	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值,并转换为<code>int</code>
	 * 
	 * @date Created at 2018年9月18日 下午4:30:52
	 * @Author 谷泽昊
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static int getConfigValueAsInt(String key, int defaultValue) {
		IConfigurationService configurationService = SpringUtil.getBean(ConfigurationServiceImpl.class);
		return configurationService.getConfigValueAsInt(key, defaultValue, UserUtils.getUser().getOrganizationId());
	}

	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值,并转换为<code>long</code>
	 * 
	 * @date Created at 2018年9月18日 下午4:30:45
	 * @Author 谷泽昊
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static long getConfigValueAsLong(String key, long defaultValue) {
		IConfigurationService configurationService = SpringUtil.getBean(ConfigurationServiceImpl.class);
		return configurationService.getConfigValueAsLong(key, defaultValue, UserUtils.getUser().getOrganizationId());
	}

	/**
	 * 从系统全局配置或机构内部配置中读取指定名称的配置项的值,并转换为 boolean
	 * 
	 * @date Created at 2018年9月18日 下午4:30:10
	 * @Author 谷泽昊
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean getConfigValueAsBoolean(String key, boolean defaultValue) {
		IConfigurationService configurationService = SpringUtil.getBean(ConfigurationServiceImpl.class);
		return configurationService.getConfigValueAsBoolean(key, defaultValue, UserUtils.getUser().getOrganizationId());
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年9月29日 谷泽昊 creat
 */