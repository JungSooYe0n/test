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
package com.trs.netInsight.widget.config.service.impl;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.config.entity.Configuration;
import com.trs.netInsight.widget.config.repository.ConfigurationRepository;
import com.trs.netInsight.widget.config.service.IConfigurationService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配置类 service
 * 
 * @Type ConfigurationServiceImpl.java
 * @author 谷泽昊
 * @date 2018年9月18日 下午4:27:11
 * @version
 */
@Service
public class ConfigurationServiceImpl implements IConfigurationService {
	@Autowired
	private ConfigurationRepository configurationRepository;
	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private Environment env;

	/**
	 * 获取第一个
	 * 
	 * @date Created at 2018年9月25日 下午10:20:43
	 * @Author 谷泽昊
	 * @param key
	 * @return
	 */
	private Configuration getConfig(final String key) {
		List<Configuration> configOrganizationList = configurationRepository.findByKeyAndConfigOrganizationIsNull(key);
		if (configOrganizationList != null && configOrganizationList.size() > 0) {
			return configOrganizationList.get(0);
		}
		return null;
	}

	/**
	 * 获取数据库中配置项
	 * 
	 * @date Created at 2018年10月25日 上午11:09:32
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param key
	 * @param organizationId
	 * @return
	 */
	private Configuration getConfigByDataBase(final String key, final String organizationId) {
		if (StringUtils.isNotBlank(organizationId)) {
			Organization organization = organizationRepository.findOne(organizationId);
			if (organization != null) {
				Configuration configuration = configurationRepository.findByKeyAndConfigOrganization(key, organization);
				if (configuration == null) {
					configuration = getConfig(key);
				}
				return configuration;
			}
			return getConfig(key);

		}
		if (!UserUtils.isSuperAdmin()) {
			String loginOrganizationId = UserUtils.getUser().getOrganizationId();
			if (StringUtils.isNotBlank(loginOrganizationId)) {
				Organization organization = organizationRepository.findOne(loginOrganizationId);
				if (organization != null) {
					Configuration configuration = configurationRepository.findByKeyAndConfigOrganization(key,
							organization);
					if (configuration == null) {
						configuration = getConfig(key);
					}
					return configuration;
				}
				return getConfig(key);
			}
			return null;
		}
		return getConfig(key);

	}

	@Override
	public Configuration getConfig(final String key, final String organizationId) {
		Configuration configuration = getConfigByDataBase(key, organizationId);
		//如果数据库没有，查询配置文件里的
		if (configuration == null|| StringUtils.isBlank(configuration.getValue())) {
			String property = env.getProperty(key);
			if (StringUtils.isNotBlank(property)) {
				configuration = new Configuration();
				configuration.setKey(key);
				configuration.setValue(property);
				return configuration;
			}
		}
		return configuration;
	}

	@Override
	public String getConfigValue(String key, String defaultValue, String organizationId) {
		Configuration configuration = getConfig(key, organizationId);
		if (configuration == null || StringUtils.isBlank(configuration.getValue())) {
			return defaultValue;
		}
		return configuration.getValue();
	}

	@Override
	public String getConfigValueByKey(String key) {
		List<Configuration> configOrganizationList = configurationRepository.findByKeyAndConfigOrganizationIsNull(key);
		if (configOrganizationList != null && configOrganizationList.size() > 0) {
			return configOrganizationList.get(0).getValue();
		}
		return null;
	}

	@Override
	public int getConfigValueAsInt(String key, int defaultValue, String organizationId) {
		Configuration configuration = getConfig(key, organizationId);
		if (configuration == null || StringUtils.isBlank(configuration.getValue())) {
			return defaultValue;
		}
		return Integer.valueOf(configuration.getValue());
	}

	@Override
	public long getConfigValueAsLong(String key, long defaultValue, String organizationId) {
		Configuration configuration = getConfig(key, organizationId);
		if (configuration == null || StringUtils.isBlank(configuration.getValue())) {
			return defaultValue;
		}
		return Long.valueOf(configuration.getValue());
	}

	@Override
	public boolean getConfigValueAsBoolean(String key, boolean defaultValue, String organizationId) {
		Configuration configuration = getConfig(key, organizationId);
		if (configuration == null || StringUtils.isBlank(configuration.getValue())) {
			return defaultValue;
		}
		return Boolean.valueOf(configuration.getValue());
	}

	@Override
	public boolean updateConfig(String key, String value) {
		if (UserUtils.isSuperAdmin()) {
			Configuration configuration = getConfig(key);
			if (configuration != null) {
				configuration.setValue(value);
				configurationRepository.save(configuration);
				return true;
			}

			configuration = new Configuration();
			configuration.setKey(key);
			configuration.setValue(value);
			configurationRepository.save(configuration);
			return true;
		}
		String organizationId = UserUtils.getUser().getOrganizationId();
		return updateConfig(key, value, organizationId);

	}

	@Override
	public boolean updateConfig(String key, String value, String organizationId) {
		if (StringUtils.isNotBlank(organizationId)) {
			Organization organization = organizationRepository.findOne(organizationId);
			if (organization == null) {
				return false;
			}
			Configuration configuration = configurationRepository.findByKeyAndConfigOrganization(key, organization);
			if (configuration != null) {
				configuration.setValue(value);
				configurationRepository.save(configuration);
				return true;
			}
			configuration = new Configuration();
			configuration.setKey(key);
			configuration.setValue(value);
			configuration.setConfigOrganization(organization);
			configurationRepository.save(configuration);
			return true;
		}
		return false;
	}

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