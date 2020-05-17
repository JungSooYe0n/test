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

import com.trs.netInsight.widget.user.entity.Role;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.repository.RoleRepository;
import com.trs.netInsight.widget.user.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 角色实现类
 * 
 * @Type RoleServiceImpl.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:40:32
 * @version
 */
@Service
public class RoleServiceImpl implements IRoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public Role add(Role role) {
		return roleRepository.save(role);
	}

	@Override
	public List<Role> addAll(Collection<Role> role) {
		return roleRepository.save(role);
	}

	@Override
	public Role findOne(String id) {
		return roleRepository.findOne(id);
	}

	@Override
	public void delete(String id) {
		roleRepository.delete(id);
	}

	@Override
	public Page<Role> pageList(int pageNo, int pageSize) {
		Pageable pageable = new PageRequest(pageNo, pageSize);

		// String organizationId = UserUtils.getUser().getOrganizationId();

		Page<Role> findAll = roleRepository.findAll(pageable);
		// Page<Role> findAll =
		// roleRepository.findByOrganizationId(organizationId,pageable);
		return findAll;
	}

	@Override
	public List<Role> findAll() {
		return roleRepository.findAll();
	}

	@Override
	public Role update(Role role) {
		return roleRepository.saveAndFlush(role);
	}

	@Override
	public List<Role> updateAll(Collection<Role> roles) {
		return roleRepository.save(roles);
	}

	@Override
	public List<Role> findByRoleTypeAndDes(CheckRole roleAdmin,String des) {
		return roleRepository.findByRoleTypeAndDescriptions(roleAdmin.toString(),des);
	}

	@Override
	public List<Role> findByIds(Collection<String> roleIds) {
		return roleRepository.findAll(roleIds);
	}

	@Override
	public List<Role> findByIds(String[] roleIds) {
		return findByIds(Arrays.asList(roleIds));
	}

	@Override
	public List<Role> findByDescriptions(String descriptions) {
		return roleRepository.findByDescriptions(descriptions);
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