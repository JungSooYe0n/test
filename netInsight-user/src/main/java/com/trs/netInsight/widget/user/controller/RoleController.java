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
package com.trs.netInsight.widget.user.controller;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.Permission;
import com.trs.netInsight.widget.user.entity.Role;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.service.IPermissionService;
import com.trs.netInsight.widget.user.service.IRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

/**
 * 角色Controller
 * 
 * @Type RoleController.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:42:32
 * @version
 */
@Slf4j
@RestController
@Api(description = "角色接口")
@RequestMapping(value = { "/role" })
public class RoleController {

	@Autowired
	private IRoleService roleService;
	@Autowired
	private IPermissionService permissionService;

	/**
	 * 查询角色
	 * 
	 * @date Created at 2017年12月11日 下午6:17:23
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("查询角色")
	@FormatResult
	@GetMapping(value = "/pageList")
	public Object pageList(
			@ApiParam("页码") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
			@ApiParam("size") @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize)
			throws TRSException {
		return roleService.pageList(pageNo, pageSize);
	}

	/**
	 * 查询角色
	 * 
	 * @date Created at 2017年12月11日 下午6:17:23
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("查询所有角色")
	@FormatResult
	@GetMapping(value = "/findAll")
	public Object findAll() throws TRSException {
		return roleService.findAll();
	}

	/**
	 * 根据角色类型查询角色
	 * 
	 * @date Created at 2017年12月11日 下午6:17:23
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("根据角色类型查询角色")
	@FormatResult
	@GetMapping(value = "/findAllRoleType")
	public Object findAllRoleType(@ApiParam("角色类型") @RequestParam(value = "roleType") CheckRole roleType)
			throws TRSException {
		return roleService.findByRoleTypeAndDes(roleType,"日常监测、专题分析、预警中心");
	}

	/**
	 * 添加角色
	 * 
	 * @date Created at 2017年12月11日 下午5:12:16
	 * @Author 谷泽昊
	 * @param roleName
	 *            角色名称
	 * @param descriptions
	 *            描述
	 * @param permissionId
	 *            角色id
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("添加角色")
	@FormatResult
	@PostMapping(value = "/addRole")
	public Object addRole(@ApiParam("角色名") @RequestParam(value = "roleName") String roleName,
			@ApiParam("描述") @RequestParam(value = "descriptions", required = false) String descriptions,
			@ApiParam("权限id，用,分割") @RequestParam(value = "permissionIds") String[] permissionIds) throws TRSException {
		// 判断名字
		if (StringUtils.isBlank(roleName)) {
			throw new TRSException(CodeUtils.FAIL, "角色名不能为空！");
		}

		// 判断权限id
		if (permissionIds == null || permissionIds.length <= 0) {
			throw new TRSException(CodeUtils.FAIL, "权限不能为空！");
		}

		// 判断用户是否有权限
		if (!UserUtils.isRoleAdmin()) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户没有权限新建角色！");
		}
		List<Permission> findByIds = permissionService.findByIds(permissionIds);
		Role role = new Role();
		role.setRoleName(roleName);
		role.setDelete(true);
		role.setDescriptions(descriptions);
		role.setRoleType(CheckRole.SUPER_ADMIN);
		role.setPermissions(new HashSet<>(findByIds));
		roleService.add(role);
		return "添加角色成功！";
	}

	/**
	 * 修改角色
	 * 
	 * @date Created at 2017年12月11日 下午5:40:24
	 * @Author 谷泽昊
	 * @param id
	 * @param roleName
	 * @param descriptions
	 * @param permissionId
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("修改角色")
	@FormatResult
	@PostMapping(value = "/editRole/{id}")
	public Object editRole(@ApiParam("角色id") @PathVariable(value = "id") String id,
			@ApiParam("角色名") @RequestParam(value = "roleName") String roleName,
			@ApiParam("描述") @RequestParam(value = "descriptions", required = false) String descriptions,
			@ApiParam("权限id，用,分割") @RequestParam(value = "permissionId") String[] permissionIds) throws TRSException {
		// 判断名字
		if (StringUtils.isBlank(roleName)) {
			throw new TRSException(CodeUtils.FAIL, "角色名不能为空！");
		}

		// 判断权限id
		if (permissionIds == null || permissionIds.length <= 0) {
			throw new TRSException(CodeUtils.FAIL, "权限不能为空！");
		}

		Role role = roleService.findOne(id);
		// 判断用户是否有权限
		if (!UserUtils.isRoleAdmin()
				|| !StringUtils.equals(UserUtils.getUser().getOrganizationId(), role.getOrganizationId())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户没有权限修改角色！");
		}

		role.setRoleName(roleName);
		role.setDescriptions(descriptions);
		role.setPermissions(new HashSet<>(permissionService.findByIds(permissionIds)));
		roleService.add(role);
		return "修改角色成功！";
	}

	/**
	 * 删除角色
	 * 
	 * @date Created at 2017年12月11日 下午5:45:31
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("删除角色")
	@FormatResult
	@PostMapping(value = "/deleteRole/{id}")
	public Object deleteRole(@ApiParam("角色id") @PathVariable(value = "id") String id) throws TRSException {

		Role role = roleService.findOne(id);
		// 判断用户是否有权限
		if (!UserUtils.isRoleAdmin()
				|| !StringUtils.equals(UserUtils.getUser().getOrganizationId(), role.getOrganizationId())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户没有权限删除角色！");
		}

		roleService.delete(id);
		return "删除角色成功！";
	}

	/**
	 * 修改角色状态
	 * 
	 * @date Created at 2017年12月11日 下午6:00:00
	 * @Author 谷泽昊
	 * @param id
	 * @param status
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("修改角色")
	@FormatResult
	@GetMapping(value = "/editStatus/{id}")
	public Object editStatus(@ApiParam("角色id") @PathVariable(value = "id") String id,
			@ApiParam("状态 1为冻结 0为正常 ") @RequestParam(value = "roleName") String status) throws TRSException {

		Role role = roleService.findOne(id);
		// 判断用户是否有权限
		if (!UserUtils.isRoleAdmin()
				|| !StringUtils.equals(UserUtils.getUser().getOrganizationId(), role.getOrganizationId())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户没有权限修改角色状态！");
		}
		roleService.add(role);
		return "修改角色状态成功！";
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