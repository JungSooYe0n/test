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


import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.entity.enums.Status;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.IUserService;

/**
 * 初始化机构
 * 
 * @Type InnerUserInitializer.java
 * @author 谷泽昊
 * @date 2017年11月20日 下午6:11:42
 * @version
 */
@Order(10)
@Component
public class InnerOrganizationInitializer {

	@Autowired
	private IOrganizationService organizationService;
	
	@Autowired
	private IUserService userService;

	/**
	 * 初始化
	 * @date Created at 2017年11月22日  上午10:25:08
	 * @Author 谷泽昊
	 */
	//@PostConstruct
	public void initialize() {
		List<Organization> organization = organizationService.findByOrganizationName(UserUtils.RETAIL_ORGANIZATION_NAME);
		if(organization==null){
			User user = userService.findByUserName(UserUtils.RETAIL_USER_NAME);
			if(user==null){
				this.add(UserUtils.RETAIL_USER_NAME, UserUtils.RETAIL_USER_NAME, "trsadmin", UserUtils.RETAIL_ORGANIZATION_NAME, "", -1, UserUtils.FOREVER_DATE);
			}
			
		}
	}

	private void add(String userName, String displayName, String password, String organizationName, String email,
			int userLimit, String expireAt){

		//保存机构
		Organization organization=new Organization();
		organization.setOrganizationName(organizationName);
		organization.setUserLimit(userLimit);
		organization.setExpireAt(expireAt);
		organization.setStatus(Status.normal);
		String organizationId = organizationService.add(organization);
		//保存用户
		User user=new User();
		user.setUserName(userName);
		user.setDisplayName(displayName);
		
		//加密
		String salt = UUID.randomUUID().toString();//加密的salt
		//加密后的密码
		String encryptPsw = UserUtils.getEncryptPsw(password, salt);
		user.setSalt(salt);
		user.setPassword(encryptPsw);
		user.setEmail(email);
		user.setExpireAt(expireAt);
		user.setCheckRole(CheckRole.ROLE_ADMIN);
		user.setOrganizationId(organizationId);
		user.setStatus(Status.normal);
		String add = userService.add(user,false);
		
		//将机构存入管理员账号
		organization=null;
		organization=organizationService.findById(organizationId);
		if(organization!=null){
			organization.setAdminUserId(add);
			organizationService.update(organization);
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