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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.entity.enums.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.Set;

/**
 * 用户类
 * 
 * @Type User.java
 * @Desc
 * @author 谷泽昊
 * @date 2017年11月17日 下午3:49:25
 * @version
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "`user`")
//@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public class User extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 账号 -BINARY支持大小写
	 */
	@Column(name = "`user_name`",columnDefinition="VARCHAR(100) BINARY ")
	private String userName;

	/**
	 * 用户名
	 */
	@Column(name = "`display_name`")
	private String displayName;

	/**
	 * 用户密码,MD5加密
	 */
	@JsonIgnore
	@Column(name = "`password`")
	private String password;

	/**
	 * 用户密码,MD5加密的salt
	 */
	@JsonIgnore
	@Column(name = "`salt`")
	private String salt;

	/**
	 * 邮件地址
	 */
	@Column(name = "`email`")
	private String email;

	/**
	 * 手机号
	 */
	@Column(name = "`phone`")
	private String phone;

	/**
	 * 最后登录时间
	 */
	@Column(name = "`last_login_time`")
	private String lastLoginTime;

	/**
	 * 有效期，永久为0
	 */
	@Column(name = "`expireat`")
	private String expireAt;

	/**
	 * 最后登录IP
	 */
	@Column(name = "`last_login_ip`")
	private String lastLoginIp;

	/**
	 * 账号状态 1为冻结 0为正常
	 */
	@Column(name = "`status`")
	private String status = Status.frozen.getValue();

	/**
	 * status的set方法
	 * 
	 * @date Created at 2018年8月29日 下午4:39:05
	 * @Author 谷泽昊
	 * @param status
	 */
	public void setStatus(Status status) {
		this.status = status.getValue();
	}

	/**
	 * 用户备注
	 */
	@Column(name = "`descriptions`")
	private String descriptions;

	/**
	 * 用户判断，管理员还是普通用户
	 */
	@Column(name = "`check_role`")
	private String checkRole;

	/**
	 * checkRole的set方法
	 * 
	 * @date Created at 2018年8月29日 下午4:39:12
	 * @Author 谷泽昊
	 * @param checkRole
	 */
	public void setCheckRole(CheckRole checkRole) {
		this.checkRole = checkRole.toString();
	}

	/**
	 * 是否能同时登陆
	 */
	@Column(name = "`same_time_login`")
	private boolean sameTimeLogin = false;

	/**
	 * tenantId 天目云租户id
	 */
	@Column(name = "`tenantId`")
	private String tenantId = null;

	@Column(name = "`totalTimes`")
	private Integer totalTimes = 0;

	/**
	 * 角色
	 */

	@ManyToMany(fetch = FetchType.EAGER) // 多对多外键关联的配置
	@JoinTable(name = "users_roles", // 中间表的表名
			joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") }, // 本表的主键
			inverseJoinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "id") }) // 所映射表的主键
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Role> roles;

	/**
	 * 机构(运维账号下的机构)
	 */
	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER) // 多对多外键关联的配置
	@JoinTable(name = "role_platform_organization", // 中间表的表名
			joinColumns = { @JoinColumn(name = "role_platform_id", referencedColumnName = "id") }, // 本表的主键
			inverseJoinColumns = { @JoinColumn(name = "organization_id", referencedColumnName = "id") }) // 所映射表的主键
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<Organization> organizations;

	/**
	 * 临时属性 机构名称
	 */
	@Transient
	private String organizationName;
	/**
	 * 临时属性 运维管理的机构次数
	 */
	@Transient 
	private int organizationSize = 0;

	/**
	 * 临时属性 系统名称
	 */
	@Transient
	private String systemName;
	/**
	 * 临时属性 登录次数
	 */
	@Transient
	private int loginCount;
	@Column(name = "`exclusive_hybase`")
	private Boolean exclusiveHybase = false;
	public Boolean isExclusiveHybase(){
		if(this.exclusiveHybase != null && this.exclusiveHybase){
			return this.exclusiveHybase;
		}else{
			return false;
		}
	}

	/**
	 * 到期提示是否标红
	 */
//	@Transient
//	private boolean red;

	/**
	 * 账号剩余时间(账户到期提醒，30天内就开始提醒)  2019-11-26 改造
	 */
	@Transient
	private int remainingTime;
	/**
	 * 临时属性-- 用户上限 -1为不限
	 */
	@Transient
	private int userLimit;
	/**
	 * 临时属性-- 机构数据源
	 */
	@Transient
	private String dataSources;;

	/**
	 * 临时属性-- 数据时间
	 * 日常监测 默认近3个月，专题分析 默认近1年，高级搜索 默认近3个月 可支持检索时间
	 */
	@Transient
	private int columnDateLimit;

	@Transient
	private int specialDateLimit;

	@Transient
	private int aSearchDateLimit;

	/**
	 * 密码确认
	 */
	@Transient
	private String passwordAgain;
	/**
	 * 用户角色
	 */
	@Transient
	private String[] roleIds;

	/**
	 * 机构剩余时间（不入库）
	 */
	@Transient
	private String surplusDate;

    /**
     * 是否提示站内预警   - true提示  false不提示
     */
	@Transient
	private Boolean isAlert = true;

	/**
	 * 用来与 前端 创建分组时 添加用户时 做参数映射
	 * @param userName
	 * @param displayName
	 * @param password
	 * @param email
	 * @param phone
	 * @param passwordAgain
	 * @param roleIds
	 */
	public User(String userName, String displayName, String password, String email, String phone, String passwordAgain, String[] roleIds) {
		this.userName = userName;
		this.displayName = displayName;
		this.password = password;
		this.email = email;
		this.phone = phone;
		this.passwordAgain = passwordAgain;
		this.roleIds = roleIds;
	}

	@Override
	public String toString() {
		return "User [userName=" + userName + ", displayName=" + displayName + ", password=" + password + ", salt="
				+ salt +", email=" + email + ", phone=" + phone + ", lastLoginTime=" + lastLoginTime + ", expireAt="
				+ expireAt + ", lastLoginIp=" + lastLoginIp + ", status=" + status + ", descriptions=" + descriptions
				+ ", checkRole=" + checkRole + ", organizationName=" + organizationName + ", systemName=" + systemName 
				+ ", remainingTime=" + remainingTime + "]";
	}

}
