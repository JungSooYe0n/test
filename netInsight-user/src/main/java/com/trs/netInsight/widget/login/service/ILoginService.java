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
package com.trs.netInsight.widget.login.service;

import com.trs.netInsight.handler.exception.TRSException;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 登录
 * @Type ILoginService.java 
 * @author 谷泽昊
 * @date 2018年8月9日 下午6:32:41
 * @version 
 */
public interface ILoginService {

	/**
	 * 登录
	 * @date Created at 2018年8月9日  下午6:46:29
	 * @Author 谷泽昊
	 * @param token
	 * @param userName
	 * @param ip
	 * @return
	 * @throws TRSException 
	 */
	public String login(UsernamePasswordToken token, String userName, String ip) throws TRSException;

	/**
	 * 模拟登录获取token
	 * @date Created at 2018年12月10日  下午2:37:34
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param loginId
	 * @param organizationId
	 * @param userId
	 * @return
	 */
	public String getSimulatedLoginToken(String loginId, String organizationId, String userGroupId, String userId);
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