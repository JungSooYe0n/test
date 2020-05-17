/*
 * Project: netInsight
 * 
 * File Created at 2017年11月17日
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

import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.user.entity.enums.PermissionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.Set;

/**
 * 权限表
 * 
 * @Type Permission.java
 * @author 谷泽昊
 * @date 2017年11月17日 下午5:45:43
 * @version
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "`permission`")
//@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
// @JsonIgnoreProperties(value = { "roles"})
public class Permission extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 权限名字
	 */
	@Column(name = "`permission_name`")
	private String permissionName;

	/**
	 * 拦截的url----唯一字段
	 */
	@Column(name = "`url`")
	private String url;
	/**
	 * 权限类型，比如专题分析
	 */
	@Column(name = "`type`")
	private String type;

	public void setType(PermissionType permissionType) {
		this.type = permissionType.getValue();
	}

	/**
	 * 需要的权限
	 */
	@Column(name = "`perms`")
	private String perms;

	/**
	 * 与前端交互的按钮
	 */
	@Column(name = "`button_name`")
	private String buttonName;

	/**
	 * 能否删除 true为能，false为不能
	 */
	@Column(name = "`is_delete`")
	private boolean isDelete = true;

	/**
	 * 父id
	 */
	@Column(name = "`parent_id`")
	private String parentId = "0";

	@ManyToMany(fetch = FetchType.EAGER, targetEntity = Role.class, mappedBy = "permissions") // 把主控方交给角色
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Role> roles;

	@Override
	public String toString() {
		return "Permission [permissionName=" + permissionName + ", url=" + url + ", type=" + type + ", perms=" + perms
				+ ", buttonName=" + buttonName + ", isDelete=" + isDelete + ", parentId=" + parentId + "]";
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月17日 谷泽昊 creat
 */