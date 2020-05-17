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

import com.trs.netInsight.widget.user.entity.Permission;

import java.util.Collection;
import java.util.List;

/**
 * 权限service
 * 
 * @Type IPermissionService.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:14:48
 * @version
 */
public interface IPermissionService {

	/**
	 * 批量添加
	 * @date Created at 2017年11月22日  上午10:22:01
	 * @Author 谷泽昊
	 * @param list
	 */
	public List<Permission> addList(Collection<Permission> list);
	/**
	 * 添加
	 * @date Created at 2017年11月22日  上午10:22:01
	 * @Author 谷泽昊
	 * @param list
	 */
	public Permission addPermission(Permission permission);

	/**
	 * 查询所有
	 * @date Created at 2017年11月22日  上午10:23:43
	 * @Author 谷泽昊
	 * @return
	 */
	public List<Permission> findAll();

	/**
	 * 根据id 批量查询权限
	 * @date Created at 2018年8月29日  下午4:50:30
	 * @Author 谷泽昊
	 * @param permissionIds
	 * @return
	 */
	public List<Permission> findByIds(String[] permissionIds);
	
	/**
	 * 根据id批量查询权限
	 * @date Created at 2018年8月29日  下午4:50:39
	 * @Author 谷泽昊
	 * @param permissionIds
	 * @return
	 */
	public List<Permission> findByIds(Collection<String> permissionIds);
	
	/**
	 * 根据id查询
	 * @date Created at 2018年9月10日  上午11:50:04
	 * @Author 谷泽昊
	 * @param permissionId
	 * @return
	 */
	public Permission findById(String permissionId);
	
	/**
	 * 根据实体类删除
	 * @date Created at 2018年9月10日  上午11:51:05
	 * @Author 谷泽昊
	 * @param permission
	 */
	public void deletePermission(Permission permission);
	/**
	 * 根据实体类修改
	 * @date Created at 2018年9月10日  上午11:51:05
	 * @Author 谷泽昊
	 * @param permission
	 */
	public Permission updatePermission(Permission permission);
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