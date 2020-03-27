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

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trs.netInsight.widget.user.entity.Permission;

/**
 * 权限repository
 * @Type PermissionRepository.java 
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:14:25
 * @version 
 */
public interface PermissionRepository extends JpaRepository<Permission, String>{

	/**
	 * 根据ids 批量查询
	 * @date Created at 2018年8月29日  下午4:52:55
	 * @Author 谷泽昊
	 * @param permissionIds
	 * @return
	 */
	public List<Permission> findByIdIn(Collection<String> permissionIds);

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