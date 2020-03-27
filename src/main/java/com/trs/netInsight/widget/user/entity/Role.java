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
package com.trs.netInsight.widget.user.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 角色实体类
 * 
 * @Type Role.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:16:14
 * @version
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "`role`")
public class Role extends BaseEntity implements Comparable<Role>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 角色名称
	 */
	@Column(name = "`role_name`")
	private String roleName;

	/**
	 * 是否可以删除 true可以 false不可以
	 */
	@Column(name = "`is_delete`")
	private boolean isDelete;
	
	/**
	 * 与前端交互的按钮
	 */
	@Column(name = "`button_name`")
	private String buttonName;

	/**
	 * 角色描述
	 */
	@Column(name = "`descriptions`")
	private String descriptions;

	/**
	 * 拥有该角色所需用户权限，比如roleType为CheckRole.ROLE_ADMIN则表示管理员拥有带这个标记得角色
	 * 
	 * 该字段只针对管理员和运维账号
	 */
	@Column(name = "`role_type`")
	private String roleType;

	public CheckRole getRoleType() {
		return CheckRole.valueOf(roleType);
	}

	public void setRoleType(CheckRole roleType) {
		this.roleType = roleType.toString();
	}

	/**
	 * 权限
	 */
	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER) // 多对多外键关联的配置
	@JoinTable(name = "roles_permissions", // 中间表的表名
			joinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "id") }, // 本表的主键
			inverseJoinColumns = { @JoinColumn(name = "permission_id", referencedColumnName = "id") }) // 所映射表的主键
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Permission> permissions;

	/**
	 * 用户
	 */
	@JsonIgnore
	@ManyToMany(fetch=FetchType.LAZY,targetEntity = User.class, mappedBy = "roles") // 把主控方交给用户
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<User> users;

	/**
	 * 分组
	 */
	@JsonIgnore
	@ManyToMany(fetch=FetchType.LAZY,targetEntity = SubGroup.class, mappedBy = "roles") // 把主控方交给分组
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<SubGroup> subGroups;

	@Override
	public String toString() {
		return "Role [roleName=" + roleName + ", isDelete=" + isDelete + ", descriptions=" + descriptions
				+ ", roleType=" + roleType + "]";
	}

	@Override
	public int compareTo(Role o) {
		if (o.getRoleName().equals(roleName)){
			return 0;

		}else {
			return 1;
		}
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