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
package com.trs.netInsight.widget.config.repository;

import com.trs.netInsight.widget.config.entity.Configuration;
import com.trs.netInsight.widget.user.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 配置类Repository
 * 
 * @Type ConfigurationRepository.java
 * @author 谷泽昊
 * @date 2018年9月18日 下午4:12:22
 * @version
 */
@Repository("configurationRepository")
public interface ConfigurationRepository extends JpaRepository<Configuration, String> {

	/**
	 * 根据机构id和key查询机构配置
	 * 
	 * @date Created at 2018年9月18日 下午4:26:10
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public Configuration findByKeyAndConfigOrganization(String key, Organization organization);

	/**
	 * 根据机构id查询机构下全部配置
	 * 
	 * @date Created at 2018年9月18日 下午4:26:10
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public List<Configuration> findByConfigOrganization(Organization organization);

	/**
	 * 根据key 查询全局配置
	 * @date Created at 2018年9月18日  下午4:55:28
	 * @Author 谷泽昊
	 * @param key
	 * @return
	 */
	public List<Configuration> findByKeyAndConfigOrganizationIsNull(String key);
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