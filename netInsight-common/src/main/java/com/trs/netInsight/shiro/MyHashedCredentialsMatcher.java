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

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

/**
 * 重写HashedCredentialsMatcher的doCredentialsMatch方法，使其可以面密码登录
 * 
 * @Type MyHashedCredentialsMatcher.java
 * @author 谷泽昊
 * @date 2018年8月9日 下午5:12:09
 * @version
 */
public class MyHashedCredentialsMatcher extends HashedCredentialsMatcher {
	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		if (token instanceof EasyTypeToken) {
			EasyTypeToken tk = (EasyTypeToken) token;
			if (tk.getType().equals(LoginType.NOPASSWD)) {
				return true;
			}
		}
        Object tokenHashedCredentials = hashProvidedCredentials(token, info);
        Object accountCredentials = getCredentials(info);
        return equals(tokenHashedCredentials, accountCredentials);
    
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