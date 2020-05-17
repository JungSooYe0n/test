/*
 * Project: netInsight
 * 
 * File Created at 2018年8月9日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 自定义shiro登录配置，可以免密码登录
 * 
 * @Type EasyTypeToken.java
 * @author 谷泽昊
 * @date 2018年8月9日 下午5:03:14
 * @version
 */
public class EasyTypeToken extends UsernamePasswordToken {

	private static final long serialVersionUID = -2564928913725078138L;

	private LoginType type;

	public EasyTypeToken() {
		super();
	}

	public EasyTypeToken(String username, String password, LoginType type, boolean rememberMe, String host) {
		super(username, password, rememberMe, host);
		this.type = type;
	}

	public EasyTypeToken(String username, String password, LoginType type, boolean rememberMe) {
		super(username, password, rememberMe, null);
		this.type = type;
	}

	public EasyTypeToken(String username, String password, boolean rememberMe) {
		super(username, password, rememberMe, null);
		this.type = LoginType.PASSWORD;
	}

	public EasyTypeToken(String username, boolean rememberMe) {
		super(username, "", rememberMe, null);
		this.type = LoginType.NOPASSWD;
	}

	/** 免密登录 */
	public EasyTypeToken(String username) {
		super(username, "", false, null);
		this.type = LoginType.NOPASSWD;
	}

	/** 账号密码登录 */
	public EasyTypeToken(String username, String password) {
		super(username, password, false, null);
		this.type = LoginType.PASSWORD;
	}

	public LoginType getType() {
		return type;
	}

	public void setType(LoginType type) {
		this.type = type;
	}
}

enum LoginType {
	PASSWORD("password"), // 密码登录
	NOPASSWD("nopassword"); // 免密登录

	private String code;// 状态值

	private LoginType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年8月9日 谷泽昊 creat
 */