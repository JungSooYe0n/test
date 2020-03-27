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
package com.trs.netInsight.widget.user.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.widget.user.entity.Permission;
import com.trs.netInsight.widget.user.repository.PermissionRepository;
import com.trs.netInsight.widget.user.service.IPermissionService;

/**
 * 权限Service实现类
 * 
 * @Type PermissionServiceImpl.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:15:11
 * @version
 */
@Service
public class PermissionServiceImpl implements IPermissionService {

	@Autowired
	private PermissionRepository permissionRepository;

	@Override
	public List<Permission> addList(Collection<Permission> list) {
		return permissionRepository.save(list);
	}

	@Override
	public Permission addPermission(Permission permission) {
		return permissionRepository.save(permission);
	}

	@Override
	public List<Permission> findAll() {
		return permissionRepository.findAll();
	}

	@Override
	public List<Permission> findByIds(String[] permissionIds) {
		return findByIds(Arrays.asList(permissionIds));
	}

	@Override
	public List<Permission> findByIds(Collection<String> permissionIds) {
		return permissionRepository.findByIdIn(permissionIds);
	}

	@Override
	public Permission findById(String permissionId) {
		return permissionRepository.findOne(permissionId);
	}

	@Override
	public void deletePermission(Permission permission) {
		permissionRepository.delete(permission);
	}

	@Override
	public Permission updatePermission(Permission permission) {
		return permissionRepository.saveAndFlush(permission);
	}

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