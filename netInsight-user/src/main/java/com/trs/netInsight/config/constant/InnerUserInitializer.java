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

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.entity.enums.Status;
import com.trs.netInsight.widget.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * 启动时 生成admin账号
 * 
 * @Type InnerUserInitializer.java
 * @author 谷泽昊
 * @date 2017年11月20日 下午6:11:42
 * @version
 */
@Order(10)
@Component
public class InnerUserInitializer {

	@Autowired
	private IUserService userService;

	@PostConstruct
	public void initialize() throws Exception {
		String salt = UUID.randomUUID().toString();
		String password = UserUtils.getEncryptPsw("trsadmin", salt);
		createUser("admin", password, salt);
	}

	/**
	 * 创建超管用户
	 * 
	 * @param userName
	 * @param password
	 * @param salt
	 */
	private void createUser(String userName, String password, String salt) {
		if (userService.findByUserName(userName) == null) {
			User user = new User();
			user.setUserName(userName);
			user.setDisplayName(userName);
			user.setPassword(password);
			user.setExpireAt(UserUtils.FOREVER_DATE);
			user.setSalt(salt);
			user.setCheckRole(CheckRole.SUPER_ADMIN);
			user.setSameTimeLogin(true);
			user.setStatus(Status.normal);
			user.setOrganizationId("trsadminorganizationid");
			userService.add(user,false);
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