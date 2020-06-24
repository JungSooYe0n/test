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
package com.trs.netInsight.config.constant;

import com.trs.netInsight.shiro.ShiroConfiguration;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.user.entity.Permission;
import com.trs.netInsight.widget.user.entity.Role;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.entity.enums.PermissionType;
import com.trs.netInsight.widget.user.service.IPermissionService;
import com.trs.netInsight.widget.user.service.IRoleService;
import com.trs.netInsight.widget.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 初始化权限
 * 20190723(yangyanyan 重构)
 * @Type InnerUserInitializer.java
 * @author 谷泽昊
 * @date 2017年11月20日 下午6:11:42
 * @version
 */
@Order(9)
@Component
public class InnerPermissionInitializer {

	@Autowired
	private IPermissionService permissionService;

	@Autowired
	private IUserService userService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private ShiroConfiguration shiroConfiguration;

	/**
	 * 初始化
	 * 
	 * @date Created at 2017年11月22日 上午10:25:08
	 * @Author 谷泽昊
	 */
	@PostConstruct
	public void initialize() {
		List<Permission> findAll = permissionService.findAll();
		if (findAll == null || findAll.size() < 22) {
			createPermission();
			shiroConfiguration.updatePermission();
			// 角色和用户关联
			List<User> users = userService.findByRolesIsNull();
			// 多余的判断
			if (users != null && users.size() > 0) {
				List<Role> roles = roleService.findByRoleTypeAndDes(CheckRole.ROLE_ADMIN,"日常监测、专题分析、预警中心");
				if (roles != null && roles.size() > 0) {
					for (User user : users) {
						user.setRoles(new HashSet<>(roles));
					}
					userService.updateAll(users);
				}
			}
		}

	}

	/**
	 * 添加权限
	 * 
	 * @date Created at 2017年11月22日 上午10:19:53
	 * @Author 谷泽昊
	 */
	private void createPermission() {

		Set<Role> list = new HashSet<>();
		// 日常监测
		/*Role roleColumn = addRole("新建栏目", true,"column:add", "日常监测", CheckRole.ROLE_ADMIN);
		Set<Permission> permissionsColumn = new HashSet<>();
		roleColumn.setPermissions(permissionsColumn);
		// 专项分析
		Role roleSpecial = addRole("新建专题", true,"special:add", "专项分析", CheckRole.ROLE_ADMIN);
		Set<Permission> permissionsSpecial = new HashSet<>();
		// 预警中心
		Role roleAlert = addRole("新建预警", true,"alert:add", "预警中心", CheckRole.ROLE_ADMIN);
		Set<Permission> permissionsAlert = new HashSet<>();*/

		//-----------------角色------------------
		//新建 与 编辑（两个角色，同时控制 日常监测，专题分析，预警中心）
		Role addRole = addRole("新建", true, "add", "日常监测、专题分析、预警中心", CheckRole.ROLE_ADMIN);
		Set<Permission> permissionsAdd = new HashSet<>();

		Role updateRole = addRole("编辑", true, "update", "日常监测、专题分析、预警中心", CheckRole.ROLE_ADMIN);
		Set<Permission> permissionsUpdate = new HashSet<>();


		// ----------------权限------------------
		//历史记录
		List<Permission> findAll = permissionService.findAll();
		if (ObjectUtil.isNotEmpty(findAll)){
			for (Permission permission : findAll) {
				permissionsAdd.add(permission);
			}
		}else {
			// 添加菜单
			permissionsAdd.add(addPermission("添加菜单", "/column/addNavigation/**", PermissionType.COLUMN,
					"column:addNavigation", null, false, "0", addRole));
			// 添加日常监测栏目组
			permissionsAdd.add(addPermission("添加日常监测栏目组", "/column/addIndexPage/**", PermissionType.COLUMN,
					"column:addIndexPage", null, false, "0", addRole));
			// 添加栏目
			permissionsAdd.add(addPermission("添加栏目", "/column/addIndexTab/**", PermissionType.COLUMN,
					"column:addIndexTab", null, false, "0", addRole));
			// 添加专题一级分类
			permissionsAdd.add(addPermission("添加一级分类", "/special/addSubject/**", PermissionType.SPECIAL,
					"special:addSubject", null, false, "0", addRole));
			// 添加专题二级分类
			permissionsAdd.add(addPermission("添加二级分类", "/special/addzhuan/**", PermissionType.SPECIAL,
					"special:addzhuan", null, false, "0", addRole));
			// 添加专题
			permissionsAdd.add(addPermission("添加专题", "/special/addProject/**", PermissionType.SPECIAL, "special:add", null,
					false, "0", addRole));
			// 添加预警
			permissionsAdd.add(
					addPermission("添加预警", "/rule/add/**", PermissionType.ALERT, "alert:add", null, false, "0", addRole));
		}

		//绑定预警账号
		permissionsAdd.add(
				addPermission("绑定预警账号", "/account/add/**", PermissionType.ALERT, "alertAccount:add", null, false, "0", addRole));

		// 编辑菜单
		permissionsUpdate.add(addPermission("编辑菜单", "/column/updateNavigation/**", PermissionType.COLUMN,
				"column:updateNavigation", null, false, "0", updateRole));
		// 编辑日常监测栏目组
		permissionsUpdate.add(addPermission("编辑日常监测栏目组", "/column/updateIndexPage/**", PermissionType.COLUMN,
				"column:updateIndexPage", null, false, "0", updateRole));
		//管理分组
		permissionsUpdate.add(addPermission("是否隐藏栏目", "/column/showIndexTab/**", PermissionType.COLUMN,
				"column:showIndexTab", null, false, "0", updateRole));
		permissionsUpdate.add(addPermission("修改栏目为半栏或通栏", "/column/changeTabWidth/**", PermissionType.COLUMN,
				"column:changeTabWidth", null, false, "0", updateRole));
		permissionsUpdate.add(addPermission("拖拽栏目位置", "/column/moveTwo/**", PermissionType.COLUMN,
				"column:moveTwo", null, false, "0", updateRole));
		//编辑权限 与删除在一起
		permissionsUpdate.add(addPermission("删除栏目","/column/deleteIndexTab/**",PermissionType.COLUMN,"column:deleteIndexTab",null,false,"0",updateRole));
		//删除栏目组
		permissionsUpdate.add(addPermission("删除栏目组","/column/deleteIndexPage/**",PermissionType.COLUMN,"column:deleteIndexPage",null,false,"0",updateRole));
		// 编辑栏目
		permissionsUpdate.add(addPermission("编辑栏目", "/column/updateIndexTab/**", PermissionType.COLUMN,
				"column:updateIndexTab", null, false, "0", updateRole));

		//共享栏目
		permissionsUpdate.add(addPermission("共享栏目","/column/share/share/**", PermissionType.COLUMN,
				"columnShare:share", null, false, "0", updateRole));
		// 编辑专题一级分类
		permissionsUpdate.add(addPermission("编辑一级或二级分类", "/special/renameSubject/**", PermissionType.SPECIAL,
				"special:renameSubject", null, false, "0", updateRole));
		// 编辑专题
		permissionsUpdate.add(addPermission("编辑专题", "/special/update/**", PermissionType.SPECIAL, "special:update", null,
				false, "0", updateRole));
		//置顶
		permissionsUpdate.add(addPermission("专题置顶", "/special/topFlag/**", PermissionType.SPECIAL, "special:topFlag", null,
				false, "0", updateRole));
		// 编辑预警
		permissionsUpdate.add(
				addPermission("编辑预警", "/rule/saveUpdate/**", PermissionType.ALERT, "alert:saveUpdate", null, false, "0", updateRole));
		//是否关闭预警
		permissionsUpdate.add(
				addPermission("是否关闭预警", "/rule/onOrOff/**", PermissionType.ALERT, "alert:onOrOff", null, false, "0", updateRole));

		//
		addRole.setPermissions(new HashSet<>(permissionService.addList(permissionsAdd)));
		updateRole.setPermissions(new HashSet<>(permissionService.addList(permissionsUpdate)));

		list.add(addRole);
		list.add(updateRole);

		roleService.addAll(list);
	}

	/**
	 * 添加权限
	 * 
	 * @date Created at 2018年9月11日 下午5:04:35
	 * @Author 谷泽昊
	 * @param permissionName
	 * @param url
	 * @param type
	 * @param perms
	 * @param buttonName
	 * @param isDelete
	 * @param parentId
	 * @param role
	 * @return
	 */
	private Permission addPermission(String permissionName, String url, PermissionType type, String perms,
			String buttonName, boolean isDelete, String parentId, Role role) {
		Permission permission = new Permission();
		permission.setPermissionName(permissionName);
		permission.setUrl(url);
		permission.setType(type);
		permission.setPerms(perms);
		permission.setButtonName(buttonName);
		permission.setDelete(isDelete);
		permission.setParentId(parentId);
		Set<Role> roles = new HashSet<>();
		roles.add(role);
		permission.setRoles(roles);
		return permission;
	}

	/**
	 * 添加角色
	 * 
	 * @date Created at 2018年9月11日 下午5:06:11
	 * @Author 谷泽昊
	 * @param roleName
	 * @param isDelete
	 * @param descriptions
	 * @param roleType
	 * @return
	 */
	private Role addRole(String roleName, boolean isDelete, String buttonName,String descriptions, CheckRole roleType) {
		Role role = new Role();
		role.setRoleName(roleName);
		role.setDelete(isDelete);
		role.setButtonName(buttonName);
		role.setDescriptions(descriptions);
		role.setRoleType(roleType);
		return role;
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