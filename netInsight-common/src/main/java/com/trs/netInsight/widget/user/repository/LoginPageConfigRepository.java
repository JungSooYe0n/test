/*
 * Project: netInsight
 * 
 * File Created at 2017年11月20日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.user.repository;

import com.trs.netInsight.widget.user.entity.LoginPageConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 机构登录页配置Repository
 * @Type LoginPageConfigRepository.java
 * @author
 * @date
 * @version 
 */
@Repository
public interface LoginPageConfigRepository extends JpaRepository<LoginPageConfig, String>,JpaSpecificationExecutor<LoginPageConfig>{

	public LoginPageConfig findBySuffix(String suffix);

	@Query(value = "SELECT l FROM LoginPageConfig l WHERE  l.relevanceOrganizationId = ?1")
	public LoginPageConfig findByOrgId(String orgId);

}

