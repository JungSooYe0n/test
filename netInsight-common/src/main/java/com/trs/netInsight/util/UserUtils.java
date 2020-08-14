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
package com.trs.netInsight.util;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.widget.config.entity.SystemConfig;
import com.trs.netInsight.widget.config.repository.SystemConfigRepository;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.SubGroupRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户工具类
 * 
 * @Type UserUtil.java
 * @author 谷泽昊
 * @date 2019年11月17日 下午4:39:37
 * @version
 */
public class UserUtils {

	/**
	 * 永久有效期
	 */
	public static final String FOREVER_DATE = "2050-01-01 00:00:00";
	/**
	 * 输入密码错误次数
	 */
	public static final String REDIS_SHIRO_ACCOUNT = "REDIS_SHIRO_ACCOUNT_";

	/**
	 * 登录用户
	 */
	public static final String SESSION_LOGIN_USER = "SESSION_LOGIN_USER_";
	/**
	 * 登录用户名
	 */
	public static final String USERNAME_LOGIN_USER = "USERNAME_LOGIN_USER_";

	/**
	 * 记录当前用户登录次数
	 */
	public static final String LOGIN_COUNT = "LOGIN_COUNT_";
	/**
	 * 模拟登录key
	 */
	public static final String SIMULATED_LOGIN_USER = "SIMULATED_LOGIN_USER";

	/**
	 * 超级管理员
	 */
	public static final String SUPER_ADMIN = CheckRole.SUPER_ADMIN.toString();
	/**
	 * 机构管理员
	 */
	public static final String ROLE_ADMIN = CheckRole.ROLE_ADMIN.toString();

	/**
	 * 普通用户
	 */
	public static final String ROLE_ORDINARY = CheckRole.ROLE_ORDINARY.toString();

	/**
	 * 平台管理员
	 */
	public static final String ROLE_PLATFORM = CheckRole.ROLE_PLATFORM.toString();

	/**
	 * 访客
	 */
	public static final String ROLE_VISITOR = CheckRole.ROLE_VISITOR.toString();
	/**
	 * 默认散户机构名
	 */
	public static final String RETAIL_ORGANIZATION_NAME = "TRS";
	/**
	 * 默认散户管理员账号
	 */
	public static final String RETAIL_USER_NAME = "trs_shadmin";

	/**
	 * 超管和机构管理员 和平台管理员
	 */
	public static final List<String> ROLE_LIST = Arrays.asList(SUPER_ADMIN, ROLE_ADMIN, ROLE_PLATFORM);

	/**
	 * 机构管理员 和平台管理员
	 */
	public static final List<String> ROLE_PLATFORM_ADMIN_LIST = Arrays.asList(ROLE_PLATFORM, ROLE_ADMIN);
	/**
	 * 超管 和平运维管理员
	 */
	public static final List<String> ROLE_PLATFORM_SUPER_LIST = Arrays.asList(ROLE_PLATFORM, SUPER_ADMIN);

	/**
	 * 获取当前登录用户
	 * 
	 * @date Created at 2017年11月17日 下午4:41:22
	 * @Author 谷泽昊
	 */
	public static User getUser() {
		try {
			HttpServletRequest request = SpringUtil.getRequest();
			Object attribute = request.getAttribute(UserUtils.SIMULATED_LOGIN_USER);
			if (attribute != null) {
				User user = (User) attribute;
				return user;
			}else{
				Subject currentUser = SecurityUtils.getSubject();
				if (currentUser.isAuthenticated()) {
					Object principal = currentUser.getPrincipal();
					if (principal instanceof User) {
						User user = (User) principal;
						// IUserService userService
						// =SpringUtil.getBean(UserServiceImpl.class);
						// user = userService.findByUserName(user.getUserName());
						return user;
					}
					return new User();
				}
			}

		} catch (Exception e) {
			// log.error("获取用户失败：" , e);
		}
		return new User();
	}

	/**
	 * 获取session——id
	 * 
	 * @date Created at 2018年3月26日 下午3:49:13
	 * @Author 谷泽昊
	 * @return
	 */
	public static String getSessionId(String userName) {
		if (StringUtils.isBlank(userName)) {
			return null;
		}
		return RedisUtil.getString(SESSION_LOGIN_USER + userName);
	}

	/**
	 * 密码加密
	 * 
	 * @date Created at 2017年11月17日 下午7:26:10
	 * @Author 谷泽昊
	 * @param password
	 *            密码
	 * @param salt
	 *            盐值
	 * @return
	 */
	public static String getEncryptPsw(String password, String salt) {
		return new SimpleHash("md5", password, ByteSource.Util.bytes(salt), 2).toHex();
	}

	/**
	 * 判断当前登录账户是否为超管
	 * 
	 * @date Created at 2017年12月11日 下午2:33:06
	 * @Author 谷泽昊
	 * @return
	 */
	public static boolean isSuperAdmin() {
		User user = getUser();
		if (user == null || !StringUtils.equals(UserUtils.SUPER_ADMIN, user.getCheckRole())) {
			return false;
		}
		return true;
	}

	/**
	 * 判断当前登录用户是否为机构管理员
	 * 
	 * @date Created at 2017年12月11日 下午2:33:14
	 * @Author 谷泽昊
	 * @return
	 */
	public static boolean isRoleAdmin() {
		User user = getUser();
		if (user == null || !StringUtils.equals(UserUtils.ROLE_ADMIN, user.getCheckRole())) {
			return false;
		}
		return true;
	}

	/**
	 * 判断用户是否为机构管理员
	 * @param user
	 * @return
	 */
	public static boolean isRoleAdmin(User user) {
		if (user == null || !StringUtils.equals(UserUtils.ROLE_ADMIN, user.getCheckRole())) {
			return false;
		}
		return true;
	}

	/**
	 * 判断当前登录用户是否为平台管理员
	 * 
	 * @date Created at 2017年12月11日 下午2:33:17
	 * @Author 谷泽昊
	 * @return
	 */
	public static boolean isRolePlatform() {
		User user = getUser();
		if (user == null || !StringUtils.equals(UserUtils.ROLE_PLATFORM, user.getCheckRole())) {
			return false;
		}
		return true;
	}

	/**
	 * 判断是否为访客
	 * @return
	 */
	public static boolean isRoleVisitor() {
		User user = getUser();
		if (user == null || !StringUtils.equals(UserUtils.ROLE_VISITOR, user.getCheckRole())) {
			return false;
		}
		return true;
	}
	/**
	 * 判断用户是否为平台管理员
	 * 
	 * @date Created at 2017年12月11日 下午2:33:17
	 * @Author 谷泽昊
	 * @return
	 */
	public static boolean isRolePlatform(User user) {
		if (user == null || !StringUtils.equals(UserUtils.ROLE_PLATFORM, user.getCheckRole())) {
			return false;
		}
		return true;
	}

	/**
	 * 判断当前用户是否为普通用户
	 * @param user
	 * @return
	 */
	public static boolean isRoleOrdinary(User user) {
		if (user == null || !StringUtils.equals(UserUtils.ROLE_ORDINARY, user.getCheckRole())) {
			return false;
		}
		return true;
	}
	/**
	 * 强行掉线
	 * 
	 * @date Created at 2018年3月26日 下午6:01:01
	 * @Author 谷泽昊
	 * @param userName
	 * @return
	 */
	public static boolean loginout(String userName) {
		// 判断是否登录
		String sessionId = RedisUtil.getString(UserUtils.USERNAME_LOGIN_USER + userName);
		if (StringUtils.isNotBlank(sessionId)) {// 如果登录修改redis中用户信息
			Subject currentUser = JSONObject.parseObject(sessionId, Subject.class);
			currentUser.logout();
		}
		return true;
	}

	/**
	 * 填写自定义机构名
	 * 
	 * @date Created at 2018年9月6日 下午3:11:02
	 * @Author 谷泽昊
	 * @param user
	 * @return
	 */
	public static User checkOrganization(User user) {
		SystemConfigRepository systemConfigRepository = (SystemConfigRepository)SpringUtil.getBean("systemConfigRepository");
		SystemConfig systemConfig = null;
		List<SystemConfig> systemConfigs = systemConfigRepository.findAll();
		if (systemConfigs.size()>0){
			systemConfig = systemConfigs.get(0);
		}else {
			systemConfig = new SystemConfig();
		}

		if (StringUtils.isNotBlank(user.getOrganizationId())) {
			OrganizationRepository repository = (OrganizationRepository)SpringUtil.getBean("organizationRepository");
			Organization organization = repository.findOne(user.getOrganizationId());
			if (ObjectUtil.isNotEmpty(organization)){
				user.setExclusiveHybase(organization.isExclusiveHybase());
			}
			SubGroupRepository subGroupRepository = (SubGroupRepository)SpringUtil.getBean("subGroupRepository");
			String subGroupId = user.getSubGroupId();
			SubGroup subGroup = null;
			if (StringUtil.isNotEmpty(subGroupId)){
				subGroup = subGroupRepository.findOne(user.getSubGroupId());
			}
			if (subGroup != null){
				user.setUserLimit(subGroup.getUserLimit());
				user.setSystemName(subGroup.getName());
				user.setColumnDateLimit(organization.getColumnDateLimit());
				user.setSpecialDateLimit(organization.getSpecialDateLimit());
				user.setASearchDateLimit(organization.getASearchDateLimit());
				if (organization != null){
					user.setDataSources(organization.getDataSources());
				}

			}else if (organization != null) {
				user.setColumnDateLimit(organization.getColumnDateLimit());
				user.setSpecialDateLimit(organization.getSpecialDateLimit());
				user.setASearchDateLimit(organization.getASearchDateLimit());
				user.setUserLimit(organization.getUserLimit());
				user.setDataSources(organization.getDataSources());
				user.setSystemName(organization.getOrganizationName());
			}else{
				user.setColumnDateLimit(1825);
				user.setSpecialDateLimit(1825);
				user.setASearchDateLimit(1825);
				user.setSystemName(systemConfig.getOrganizationName());
                //user.setSystemName("网察大数据分析平台");
				//平台运维 或者 超管
				user.setDataSources("ALL");
			}
		}
		return user;
	}

	/**
	 * 判断checkRole的权限 如果机构管理员和平台管理员密码忘掉 只能超管修改 普通用户忘掉密码 机构管理员重置
	 * 
	 * @date Created at 2017年12月15日 上午9:44:28
	 * @Author 谷泽昊
	 * @param user
	 * @return
	 */
	public static boolean checkRole(User user) {
		if (user == null) {
			return false;
		}

		boolean superAdmin = UserUtils.isSuperAdmin();
		boolean roleAdmin = UserUtils.isRoleAdmin();
		boolean rolePlatform = UserUtils.isRolePlatform();
		User loginUser = UserUtils.getUser();
		String checkRole = user.getCheckRole();
		// 判断checkRole的权限
		// 如果机构管理员和平台管理员密码忘掉 只能超管修改
		// 普通用户忘掉密码 机构管理员重置
		if (((StringUtils.equals(checkRole, UserUtils.ROLE_ADMIN)
				|| StringUtils.equals(checkRole, UserUtils.ROLE_PLATFORM)) && superAdmin)
				|| (StringUtils.equals(checkRole, UserUtils.ROLE_ORDINARY)
						&& (StringUtils.equals(user.getOrganizationId(), loginUser.getOrganizationId()) && roleAdmin)
						|| superAdmin || rolePlatform)) {
			return true;
		}

		return false;
	}

	/**
	 * 记录当前用户登录次数
	 * 
	 * @date Created at 2018年9月26日 下午3:21:12
	 * @Author 谷泽昊
	 * @param userName
	 */
	public static Integer setLoginCount(String userName) {
		String key = LOGIN_COUNT + userName + DateUtil.formatCurrentTime(DateUtil.yyyyMMdd3);
		Integer integer = RedisUtil.getInteger(key);
		Integer count = integer;
		if (integer == null) {
			RedisUtil.setInteger(key, 1);
			count = 1;
			RedisUtil.expire(key, 1, TimeUnit.DAYS);
		} else {
			RedisUtil.setInteger(key, ++integer);
			count = integer;
			RedisUtil.expire(key, 1, TimeUnit.DAYS);
		}
		return count;
	}

	/**
	 * 获取当前用户登录次数
	 * 
	 * @date Created at 2018年9月26日 下午3:21:12
	 * @Author 谷泽昊
	 * @param userName
	 * LOGIN_COUNT_TMY-yaokeee48a888084700f457a01700f512afc00242020-02-17
	 */
	public static int getLoginCount(String userName) {
		String key = LOGIN_COUNT + userName + DateUtil.formatCurrentTime(DateUtil.yyyyMMdd3);
		Integer integer = null;
		try {
			integer = RedisUtil.getInteger(key);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (integer == null) {
			return 0;
		}
		return integer;
	}
	/**
	 * 修改当前用户是否预警的信息
	 *
	 * @date
	 * @Author 张娅
	 */
	public static User updateIsAlert(Boolean isAlert) {
		try {
			HttpServletRequest request = SpringUtil.getRequest();
			Object attribute = request.getAttribute(UserUtils.SIMULATED_LOGIN_USER);
			String userId = "";
			User loginUser = null;
			Boolean isSimulate = false;
			if (attribute != null) {
				User user = (User) attribute;
				user.setIsAlert(isAlert);
				loginUser= user;
				isSimulate = true;
			}
			Subject currentUser = SecurityUtils.getSubject();
			if (currentUser.isAuthenticated()) {
				Object principal = currentUser.getPrincipal();
				if (principal instanceof User) {
					User user = (User) principal;
					userId = user.getId();
					if(!isSimulate){
						//false 不提醒
						user.setIsAlert(isAlert);
						PrincipalCollection principalCollection = currentUser.getPrincipals();
						String info = principalCollection.getRealmNames().iterator().next();
						PrincipalCollection principalCollectionNew = new SimplePrincipalCollection(user, info);
						currentUser.runAs(principalCollectionNew);
						loginUser = user;
					}
				}
			}
			if(isSimulate){
				HttpSession session = request.getSession();
				session.setAttribute(UserUtils.SIMULATED_LOGIN_USER+userId+loginUser.getId(),isAlert);
			}
			return loginUser;

		} catch (Exception e) {
			// log.error("获取用户失败：" , e);
		}
		return new User();
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