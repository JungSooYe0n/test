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
package com.trs.netInsight.widget.login.service.impl;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.login.service.ILoginService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @Type LoginServiceImpl.java
 * @author 谷泽昊
 * @date 2018年8月9日 下午6:33:05
 * @version
 */
@Slf4j
@Service
public class LoginServiceImpl implements ILoginService {

	@Autowired
	private IUserService userService;

	@Value("${spring.session.timeout}")
	private int sessionTimeout;

	@Override
	public String login(UsernamePasswordToken token, String userName, String ip) throws TRSException {
		// 调用接口获取access_token
		//log.error("用户[" + userName + "]登录认证通过(这里可以进行一些认证通过后的一些系统参数初始化操作)");
		Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		try {
			// 在调用了login方法后,SecurityManager会收到AuthenticationToken,并将其发送给已配置的Realm执行必须的认证检查
			// 每个Realm都能在必要时对提交的AuthenticationTokens作出反应
			// 所以这一步在调用login(token)方法时,它会走到MyShiroRealm.doGetAuthenticationInfo()方法中,具体验证方式详见此方法
			log.error("对用户[" + userName + "]进行登录验证..验证开始");
			currentUser.login(token);
		} catch (UnknownAccountException uae) {
			log.error("对用户[" + userName + "]进行登录验证..验证未通过,未知账户");
			throw new TRSException(CodeUtils.FAIL, "账号或密码错误！");
		} catch (IncorrectCredentialsException ice) {

			log.error("对用户[" + userName + "]进行登录验证..验证未通过,错误的凭证");
			// 计算次数
			// Long increment =
			// RedisUtil.increment(UserUtils.REDIS_SHIRO_ACCOUNT + userName,
			// 1L);
			// log.error("increment:" + increment);
			// if (increment >= 5) {
			// RedisUtil.setString(UserUtils.REDIS_SHIRO_ACCOUNT + userName,
			// "LOCK", 1, TimeUnit.HOURS);
			// }
			throw new TRSException(CodeUtils.FAIL, "账号或密码错误！", ice);

		} catch (LockedAccountException lae) {
			log.error("对用户[" + userName + "]进行登录验证..验证未通过,账户已锁定");
			throw new TRSException(CodeUtils.ACCOUNT_LOCKOUT, "[" + userName + "]账户已锁定！", lae);
		} catch (ExcessiveAttemptsException eae) {
			log.error("对用户[" + userName + "]进行登录验证..验证未通过,错误次数大于5次,账户已锁定");
			throw new TRSException(CodeUtils.ACCOUNT_LOCKOUT, "[" + userName + "]错误次数大于5次，账户已锁定！", eae);
		} catch (DisabledAccountException sae) {
			log.error("对用户[" + userName + "]进行登录验证..验证未通过,帐号已经禁止登录");
			throw new TRSException(CodeUtils.UNKNOWN_ACCOUNT, "[" + userName + "]账号已经禁用！", sae);
		} catch (ExpiredCredentialsException ee) {
			log.error("对用户[" + userName + "]进行登录验证..验证未通过,帐号已经过期");
			throw new TRSException(CodeUtils.ACCOUNT_ERROR, "[" + userName + "]机构或账号已经过期！", ee);
		} catch (AuthenticationException ae) {
			// 通过处理Shiro的运行时AuthenticationException就可以控制用户登录失败或密码错误时的情景
			log.error("对用户[" + userName + "]进行登录验证..验证未通过,堆栈轨迹如下:" + ae);
			throw new TRSException(CodeUtils.UNKNOWN_FAIL, "[" + userName + "]未知错误！", ae);

		}
		// 验证是否登录成功
		if (currentUser.isAuthenticated()) {
			log.error("用户[" + userName + "]登录认证通过(这里可以进行一些认证通过后的一些系统参数初始化操作)");
			User user = userService.findByUserName(userName);
			user = UserUtils.checkOrganization(user);
			// 将用户信息存入redis
			RedisUtil.setString(UserUtils.USERNAME_LOGIN_USER + user.getUserName(), session.getId().toString(),
					sessionTimeout, TimeUnit.SECONDS);
			// 登录成功，更改ip和时间
			user.setLastLoginIp(ip);
			user.setLastLoginTime(DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
			user.setIsAlert(true);
			try {
				userService.update(user, false);

			} catch (Exception e) {
				log.error("修改失败!", e);
				currentUser.logout();
				RedisUtil.deleteKey(UserUtils.SESSION_LOGIN_USER + session.getId());
				RedisUtil.deleteKey(UserUtils.USERNAME_LOGIN_USER + user.getUserName());
				throw new TRSException(CodeUtils.UNKNOWN_FAIL, "[" + userName + "]未知错误！", e);
			}
			// 将登录次数存入redis（按用户）
			Integer count = UserUtils.setLoginCount(userName+user.getId());
			if (user.getTotalTimes() != null){
				if (count>user.getTotalTimes()){
					user.setTotalTimes(count);
				}else {
					user.setTotalTimes(user.getTotalTimes()+1);
				}

			}else {
				user.setTotalTimes(count);
			}
			userService.update(user,false);
			//loginFrequencyLogService.save(count,user.getId());
			// 将登录次数存入redis（按分组）
			String subGroupId = user.getSubGroupId();
			UserUtils.setLoginCount(subGroupId);
			return "登录成功！";
		} else {
			token.clear();
		}
		throw new TRSException(CodeUtils.UNKNOWN_FAIL, "[" + userName + "]未知错误！");
	}

	@Override
	public String getSimulatedLoginToken(String loginId, String organizationId, String userGroupId,String userId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("loginId", loginId);
		claims.put("organizationId", organizationId);
		claims.put("userGroupId", userGroupId);
		claims.put("userId", userId);
		String ip = NetworkUtil.getIpAddress(SpringUtil.getRequest());
		claims.put("ip", ip);
		return JwtUtil.getJwtToken(claims);
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