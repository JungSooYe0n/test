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

import com.trs.netInsight.widget.user.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 角色类repository
 * @Type RoleRepository.java 
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:19:47
 * @version 
 */
public interface RoleRepository extends JpaRepository<Role, String>{

	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月11日  下午6:17:03
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageable
	 * @return
	 */
	public Page<Role> findByOrganizationId(String organizationId, Pageable pageable);

	/**
	 * 根据角色类别查所有
	 * @date Created at 2018年9月12日  上午10:35:21
	 * @Author 谷泽昊
	 * @param string
	 * @return
	 */
	public List<Role> findByRoleTypeAndDescriptions(String string, String des);

	/**
	 * 为迁移历史数据
	 * @param descriptions
	 * @return
	 */
	public List<Role> findByDescriptions(String descriptions);

}


/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月20日 谷泽昊 creat
 */