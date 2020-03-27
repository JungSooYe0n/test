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
package com.trs.netInsight.widget.user.service;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;

import com.trs.netInsight.widget.user.entity.Role;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;

/**
 * 
 * @Type IRoleService.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:40:03
 * @version
 */
public interface IRoleService {

	/**
	 * 新建角色
	 * 
	 * @date Created at 2017年12月11日 下午5:30:47
	 * @Author 谷泽昊
	 * @param role
	 * @return
	 */
	public Role add(Role role);

	/**
	 * 批量添加
	 * 
	 * @date Created at 2018年9月12日 上午10:36:46
	 * @Author 谷泽昊
	 * @param role
	 * @return
	 */
	public List<Role> addAll(Collection<Role> role);

	/**
	 * 修改角色
	 * 
	 * @date Created at 2017年12月11日 下午5:30:47
	 * @Author 谷泽昊
	 * @param role
	 * @return
	 */
	public Role update(Role role);

	/**
	 * 批量修改角色
	 * 
	 * @date Created at 2017年12月11日 下午5:30:47
	 * @Author 谷泽昊
	 * @param role
	 * @return
	 */
	public List<Role> updateAll(Collection<Role> role);

	/**
	 * 根据id查找角色
	 * 
	 * @date Created at 2017年12月11日 下午5:37:51
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 */
	public Role findOne(String id);

	/**
	 * 删除角色
	 * 
	 * @date Created at 2017年12月11日 下午5:44:03
	 * @Author 谷泽昊
	 * @param id
	 */
	public void delete(String id);

	/**
	 * 分页查询
	 * 
	 * @date Created at 2017年12月11日 下午6:12:38
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Page<Role> pageList(int pageNo, int pageSize);

	/**
	 * 查询全部
	 * 
	 * @date Created at 2017年12月11日 下午6:12:38
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public List<Role> findAll();

	/**
	 * 根据权限类别查询所有
	 * 
	 * @date Created at 2018年9月12日 上午10:34:38
	 * @Author 谷泽昊
	 * @param roleAdmin
	 * @return
	 */
	public List<Role> findByRoleTypeAndDes(CheckRole roleAdmin,String des);
	
	/**
	 * 根据id批量查询角色
	 * @date Created at 2018年9月14日  下午2:42:03
	 * @Author 谷泽昊
	 * @param roleIds
	 * @return
	 */
	public List<Role> findByIds(Collection<String> roleIds);
	
	/**
	 * 根据id批量查询角色
	 * @date Created at 2018年9月14日  下午2:42:34
	 * @Author 谷泽昊
	 * @param roleIds
	 * @return
	 */
	public List<Role> findByIds(String[] roleIds);

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