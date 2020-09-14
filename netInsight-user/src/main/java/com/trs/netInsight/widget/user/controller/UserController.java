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
package com.trs.netInsight.widget.user.controller;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.model.factory.HybaseFactory;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.Role;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.entity.enums.Status;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.IRoleService;
import com.trs.netInsight.widget.user.service.ISubGroupService;
import com.trs.netInsight.widget.user.service.IUserService;
import com.trs.netInsight.widget.weixin.entity.login.Weixinlogin;
import com.trs.netInsight.widget.weixin.entity.login.repository.WeixinLoginRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 用户管理类
 *
 * @Type UserController.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:09:09
 * @version
 */
@Slf4j
@RestController
@Api(description = "用户接口")
@RequestMapping(value = { "/user", "/admin/user" })
public class UserController {

	@Autowired
	private IUserService userService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IOrganizationService organizationService;
	@Autowired
	private WeixinLoginRepository weixinLoginRepository;

	@Autowired
	private ISubGroupService subGroupService;
	@Autowired
	private IHybaseShardService hybaseShardService;

	/**
	 * 查询用户
	 * 
	 * @date Created at 2017年12月15日 下午3:25:07
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@ApiOperation("机构管理查询用户")
	@FormatResult
	@GetMapping("/pageListByOrganization")
	public Object pageListByOrganization(
			@ApiParam("检索条件:登录账号userName，账号昵称displayName") @RequestParam(value = "retrievalCondition", required = false) String retrievalCondition,
			@ApiParam("检索信息") @RequestParam(value = "retrievalInformation", required = false) String retrievalInformation,
			@ApiParam("pageNo") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
			@ApiParam("pageSize") @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@ApiParam("机构id") @RequestParam(value = "organizationId", required = false) String organizationId)
			throws TRSException {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		// 判断权限
		User user = UserUtils.getUser();
		String checkRole = user.getCheckRole();
		if (!UserUtils.ROLE_LIST.contains(checkRole)) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限查看！");
		}
		if (UserUtils.ROLE_PLATFORM.equals(checkRole)) {
			String id = user.getId();
			User findById = userService.findById(id);
			Set<Organization> organizations = findById.getOrganizations();
			boolean isCheckRole = false;
			for (Organization organization : organizations) {
				if (StringUtils.equals(organizationId, organization.getId())) {
					isCheckRole = true;
				}
			}
			if (!isCheckRole) {
				throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限查看！");
			}
		}

		if (UserUtils.ROLE_ADMIN.equals(checkRole)) {
			organizationId = null;
		}

		if (StringUtils.isBlank(organizationId)) {
			organizationId = UserUtils.getUser().getOrganizationId();
		}
		Map<String, Object> mapData = new HashMap<>();

		long userCount = userService.countByOrganizationId(organizationId);
		Page<User> pageList = userService.findByOrganizationId(retrievalCondition, retrievalInformation, pageNo,
				pageSize, organizationId);

		mapData.put("userCount", userCount);
		mapData.put("users", pageList);
		return mapData;
	}

	/**
	 * 查询所有运维
	 * 
	 * @date Created at 2018年9月17日 上午11:02:38
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("查询所有运维")
	@FormatResult
	@GetMapping("/pageListByPlatform")
	public Object pageListByPlatform(
			@ApiParam("检索条件:登录账号userName，账号昵称displayName") @RequestParam(value = "retrievalCondition", required = false) String retrievalCondition,
			@ApiParam("检索信息") @RequestParam(value = "retrievalInformation", required = false) String retrievalInformation,
			@ApiParam("pageNo") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
			@ApiParam("pageSize") @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize)
			throws TRSException {
		if (!UserUtils.isSuperAdmin()) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户没有权限查询！");
		}
		Page<User> pageList = userService.pageListByPlatform(retrievalCondition, retrievalInformation, pageNo,
				pageSize);
		return pageList;
	}

	/**
	 * 修改密码
	 * 
	 * @date Created at 2017年11月20日 下午6:02:18
	 * @Author 谷泽昊
	 * @param id
	 *            用户id
	 * @param passwordOld
	 *            旧密码
	 * @param password
	 *            新密码
	 * @param passwordAgain
	 *            第二次密码
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("修改用户密码")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.USER_UPDATE_PASSWORD, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@PostMapping("/updatePassword/{id}")
	public Object updatePassword(@ApiParam("用户id") @PathVariable(value = "id") String id,
			@ApiParam("旧密码") @RequestParam(value = "passwordOld") String passwordOld,
			@ApiParam("新密码") @RequestParam(value = "password") String password,
			@ApiParam("第二次密码") @RequestParam(value = "passwordAgain") String passwordAgain) throws TRSException {

		if (StringUtils.isBlank(password) || StringUtils.isBlank(passwordOld) || StringUtils.isBlank(passwordAgain)) {
			throw new TRSException(CodeUtils.PASSWORD_NULL, "密码不能为空！");
		}

		if (!password.equals(passwordAgain)) {
			throw new TRSException(CodeUtils.PASSWORD_NOT_SAME, "两次密码不一致！");
		}
		// 判断密码强度
		if (!RegexUtils.isLowSafely(password)) {
			throw new TRSException(CodeUtils.PASSWORD_LOW, "密码需要在8-16位，并且有大写、小写、数字和特殊字符至少三种！");
		}

		User user = userService.findById(id);
		if (user != null) {
			// 用户为用户本身 才能修改密码
			User loginUser = UserUtils.getUser();
			if (StringUtils.equals(loginUser.getId(), user.getId())) {
				String psw = UserUtils.getEncryptPsw(passwordOld, user.getSalt());
				if (!user.getPassword().equals(psw)) {
					throw new TRSException(CodeUtils.PASSWORD_NOT_SAME, "旧密码输入错误！");
				}

				String salt = UUID.randomUUID().toString();
				password = UserUtils.getEncryptPsw(password, salt);
				user.setPassword(password);
				user.setSalt(salt);
				userService.update(user, false);
				return "修改密码成功！";
			}

		}
		throw new TRSException(CodeUtils.FAIL, "修改密码失败！");
	}

	/**
	 * 重置密码
	 * 
	 * @date Created at 2017年12月15日 上午10:49:04
	 * @Author 谷泽昊
	 * @param id
	 * @param password
	 * @param passwordAgain
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("重置用户密码")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.USER_RESET_PASSWORD, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@PostMapping("/resetPassword/{id}")
	public Object resetPassword(@ApiParam("用户id") @PathVariable(value = "id") String id,
			@ApiParam("新密码") @RequestParam(value = "password") String password,
			@ApiParam("第二次密码") @RequestParam(value = "passwordAgain") String passwordAgain) throws TRSException {

		if (StringUtils.isBlank(password) || StringUtils.isBlank(passwordAgain)) {
			throw new TRSException(CodeUtils.PASSWORD_NULL, "密码不能为空！");
		}

		if (!password.equals(passwordAgain)) {
			throw new TRSException(CodeUtils.PASSWORD_NOT_SAME, "两次密码不一致！");
		}
		// 判断密码强度
		if (!RegexUtils.isLowSafely(password)) {
			throw new TRSException(CodeUtils.PASSWORD_LOW, "密码需要在8-16位，并且有大写、小写、数字和特殊字符至少三种！");
		}

		User user = userService.findById(id);
		if (user != null) {
			// 用户为超管--用户为机构管理员---用户为用户本身 才能修改密码
			if (UserUtils.checkRole(user)) {
				String salt = UUID.randomUUID().toString();
				password = UserUtils.getEncryptPsw(password, salt);
				user.setPassword(password);
				user.setSalt(salt);
				userService.update(user, true);
				return "重置密码成功！";
			} else {
				throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户没有权限重置密码！");
			}

		}
		throw new TRSException(CodeUtils.FAIL, "重置密码失败！");

	}

	/**
	 * 添加用户
	 * 
	 * @date Created at 2017年12月4日 下午8:15:51
	 * @Author 谷泽昊
	 * @param userName
	 *            账号
	 * @param displayName
	 *            用户名
	 * @param password
	 *            密码
	 * @param passwordAgain
	 *            再次输入密码
	 * @param expireAt
	 *            有效期
	 * @param email
	 *            邮件
	 * @param phone
	 *            电话
	 * @param descriptions
	 *            备注
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("添加用户")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.USER_ADD, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@PostMapping("/addUser")
	public Object addUser(@ApiParam("账号") @RequestParam(value = "userName") String userName,
			@ApiParam("用户名") @RequestParam(value = "displayName") String displayName,
			@ApiParam("密码") @RequestParam(value = "password") String password,
			@ApiParam("再次输入密码") @RequestParam(value = "passwordAgain") String passwordAgain,
			@ApiParam("有效期") @RequestParam(value = "expireAt",defaultValue = "2050-01-01 00:00:00") String expireAt,
			@ApiParam("普通账号权限，多个用逗号隔开") @RequestParam(value = "roleIds", required = false) String[] roleIds,
			@ApiParam("用户权限：普通用户ROLE_ORDSINARY,运维用户ROLE_PLATFORM") @RequestParam(value = "checkRole", required = false, defaultValue = "ROLE_ORDINARY") CheckRole checkRole,
			@ApiParam("邮件") @RequestParam(value = "email", required = false) String email,
			@ApiParam("电话") @RequestParam(value = "phone", required = false) String phone,
			@ApiParam("是否能同时登录") @RequestParam(value = "sameTimeLogin", required = false, defaultValue = "false") boolean sameTimeLogin,
			@ApiParam("备注") @RequestParam(value = "descriptions", required = false) String descriptions,
			@ApiParam("是否复制栏目") @RequestParam(value = "copyFlag", required = false, defaultValue = "false") boolean copyFlag,
			@ApiParam("机构id") @RequestParam(value = "organizationId", required = false) String organizationId,
						  @ApiParam("传统库表名") @RequestParam(value = "tradition",required = false)String tradition,
						  @ApiParam("微博库表名") @RequestParam(value = "weiBo",required = false)String weiBo,
						  @ApiParam("微信库表名") @RequestParam(value = "weiXin",required = false)String weiXin,
						  @ApiParam("海外库表名") @RequestParam(value = "overseas",required = false)String overseas,
						  @ApiParam("视频库表名") @RequestParam(value = "video",required = false)String video)
			throws TRSException {

		// 判断账号是否为空
		if (StringUtils.isBlank(userName)) {
			throw new TRSException(CodeUtils.ACCOUNT_NULL, "账号不能为空！");
		}
		// 判断账号是否有空格
		if (RegexUtils.checkBlankSpace(userName)) {
			throw new TRSException(CodeUtils.ACCOUNT_SPACE, "账号中不能含有空格！");
		}

		// 判断用户名是否为空
		if (StringUtils.isBlank(displayName)) {
			throw new TRSException(CodeUtils.DISPLAYNAME_NULL, "用户名不能为空！");
		}

		// 判断密码是否为空
		if (StringUtils.isBlank(password)) {
			throw new TRSException(CodeUtils.PASSWORD_NULL, "密码不能为空！");
		}
		// 判断密码强度
		if (!RegexUtils.isLowSafely(password)) {
			throw new TRSException(CodeUtils.PASSWORD_LOW, "密码必须符合以下需求：长度为8~16位，英文字母大、小写、符号、数字，任选三种及三种以上进行组合");
		}

		// 判断邮件是否为空
		if (StringUtils.isNotBlank(email)) {
			// 判断邮件是否正确
			if (!RegexUtils.checkEmail(email)) {
				throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
			}
		}

		// 判断两次密码是否一致
		if (!password.equals(passwordAgain)) {
			throw new TRSException(CodeUtils.PASSWORD_NOT_SAME, "两次密码不一样！");
		}
		// 验证手机号
		if (StringUtils.isNotBlank(phone) && !RegexUtils.checkMobile(phone)) {
			throw new TRSException(CodeUtils.PHONE_FAIL, "手机号填写错误！");
		}
		// 验证时间
		if (StringUtils.isBlank(expireAt)) {
			throw new TRSException(CodeUtils.FAIL, "时间不能为空！");
		} else {
			if (!DateUtil.isValidDate(expireAt, DateUtil.yyyyMMdd)) {
				throw new TRSException(CodeUtils.FAIL, "日期格式不正确，应为" + DateUtil.yyyyMMdd + "！");
			}
		}
		// 判断用户账号是否存在
		if (userService.findByUserName(userName) != null) {
			throw new TRSException(CodeUtils.USERNAME_EXISTED, "用户账号已存在！");
		}
		User loginUser = UserUtils.getUser();
		// 判断用户是否有权限新建
		if (!UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限新建用户！");
		}

		// 如果新建的用户为运维,并且登录账号不为超管
		if (CheckRole.ROLE_PLATFORM.equals(checkRole) && !UserUtils.isSuperAdmin()) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限新建运维！");

		}


		// 判断当前登录用户，如果是超管和运维，则需要机构id，如果为机构管理员，则为机构管理员id
		if (!UserUtils.isSuperAdmin() && !UserUtils.isRolePlatform()) {
			organizationId = loginUser.getOrganizationId();
		}
		if (!CheckRole.ROLE_PLATFORM.equals(checkRole)) {
			if (StringUtils.isNotBlank(organizationId)) {
				Organization organization = organizationService.findById(organizationId);
				if (organization == null) {
					throw new TRSException(CodeUtils.FAIL, "新建用户失败，查无此机构！");
				}
				List<User> userList = userService.findByOrganizationId(organizationId);
				if (organization.getUserLimit() <= userList.size() && organization.getUserLimit() != -1) {
					throw new TRSException(CodeUtils.USER_LIMIT, "已经建够[" + organization.getUserLimit() + "]个用户！");
				}
			} else {
				throw new TRSException(CodeUtils.FAIL, "新建用户失败，查无此机构！！");
			}
		} else {
			organizationId = "platformId";
		}

		// 盐
		String salt = UUID.randomUUID().toString();
		// 加密的盐
		String encryptPsw = UserUtils.getEncryptPsw(password, salt);
		User user = new User();

		user.setUserName(userName);
		user.setDisplayName(displayName);
		user.setPassword(encryptPsw);
		user.setSalt(salt);
		user.setEmail(email);
		user.setPhone(phone);
		user.setLastLoginTime(null);
		user.setExpireAt(expireAt);
		user.setLastLoginIp(null);
		user.setStatus(Status.normal);
		user.setDescriptions(descriptions);
		if (CheckRole.ROLE_PLATFORM.equals(checkRole)){
			user.setSameTimeLogin(sameTimeLogin);
		}else {
			user.setSameTimeLogin(false);
		}
		if (roleIds != null && roleIds.length > 0) {
			List<Role> roles = roleService.findByIds(roleIds);
			if (roles != null && roles.size() > 0) {
				user.setRoles(new HashSet<>(roles));
			}
		} else {
			user.setRoles(null);
		}
		user.setCheckRole(checkRole);
		user.setOrganizationId(organizationId);
		try {
			userService.add(user, copyFlag);
			String ownerId = user.getId();
			if (StringUtil.isNotEmpty(user.getSubGroupId())){
				ownerId = user.getSubGroupId();
			}
			if (CheckRole.ROLE_PLATFORM.equals(checkRole) && UserUtils.isSuperAdmin()) {
				hybaseShardService.save(HybaseFactory.getServer(),HybaseFactory.getUserName(),HybaseFactory.getPassword(),tradition,weiBo,weiXin,overseas,video,ownerId,organizationId);
			}
			return "添加用户成功！";
		} catch (Exception e) {
			log.error("添加用户失败：", e);
		}
		throw new TRSException(CodeUtils.FAIL, "添加用户失败！");
	}

	/**
	 * 修改用户信息
	 * 
	 * @date Created at 2017年12月15日 下午3:48:10
	 * @Author 谷泽昊
	 * @param id
	 * @param displayName
	 * @param expireAt
	 * @param email
	 * @param phone
	 * @param descriptions
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("修改用户")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.USER_UPDATE, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@PostMapping("/updateUser/{id}")
	public Object updateUser(@ApiParam("id") @PathVariable(value = "id") String id,
			@ApiParam("用户名") @RequestParam(value = "displayName") String displayName,
			@ApiParam("有效期") @RequestParam(value = "expireAt") String expireAt,
			@ApiParam("邮件") @RequestParam(value = "email", required = false) String email,
			@ApiParam("电话") @RequestParam(value = "phone", required = false) String phone,
			@ApiParam("是否能同时登录") @RequestParam(value = "sameTimeLogin", required = false, defaultValue = "false") boolean sameTimeLogin,
			@ApiParam("备注") @RequestParam(value = "descriptions", required = false) String descriptions,
			@ApiParam("普通账号权限，多个用逗号隔开") @RequestParam(value = "roleIds", required = false) String[] roleIds,
			@ApiParam("是否复制栏目") @RequestParam(value = "copyFlag", required = false, defaultValue = "false") boolean copyFlag,
							 @ApiParam("传统库表名") @RequestParam(value = "tradition",required = false)String tradition,
							 @ApiParam("微博库表名") @RequestParam(value = "weiBo",required = false)String weiBo,
							 @ApiParam("微信库表名") @RequestParam(value = "weiXin",required = false)String weiXin,
							 @ApiParam("海外库表名") @RequestParam(value = "overseas",required = false)String overseas,
							 @ApiParam("视频库表名") @RequestParam(value = "video",required = false)String video)
			throws TRSException {

		// 判断用户名是否为空
		if (StringUtils.isBlank(displayName)) {
			throw new TRSException(CodeUtils.DISPLAYNAME_NULL, "用户名不能为空！");
		}
		// 判断邮件是否为空// 判断邮件是否正确
		if (StringUtils.isNotBlank(email) && !RegexUtils.checkEmail(email)) {
			throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
		}

		// 验证手机号
		if (StringUtils.isNotBlank(phone) && !RegexUtils.checkMobile(phone)) {
			throw new TRSException(CodeUtils.PHONE_FAIL, "手机号填写错误！");
		}
		// 验证时间
		if (StringUtils.isBlank(expireAt)) {
			throw new TRSException(CodeUtils.FAIL, "时间不能为空！");
		} else {
			if (!expireAt.equals(UserUtils.FOREVER_DATE) && !DateUtil.isValidDate(expireAt, DateUtil.yyyyMMdd)) {
				throw new TRSException(CodeUtils.FAIL, "日期格式不正确，应为" + DateUtil.yyyyMMdd + "！");
			}
		}
		User LoginUser = UserUtils.getUser();
		// 判断用户是否有权限修改
		if (!UserUtils.ROLE_LIST.contains(LoginUser.getCheckRole())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改用户！");
		}

		User user = null;
		try {
			user = userService.findById(id);
		} catch (Exception e) {
			log.error("修改用户失败：", e);
			throw new TRSException(CodeUtils.FAIL, "修改用户失败！");
		}
		if (user == null) {
			throw new TRSException(CodeUtils.FAIL, "修改用户失败！");
		}
		// 如果当前用户为运维，只能超管修改
		if (StringUtils.contains(UserUtils.ROLE_PLATFORM, user.getCheckRole())) {
			if (!UserUtils.isSuperAdmin()) {
				throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改用户！");
			}
		}
		// 如果当前用户为超管，则不能修改
		if (StringUtils.contains(UserUtils.SUPER_ADMIN, user.getCheckRole())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改用户！");
		}
		user.setDisplayName(displayName);
		user.setExpireAt(expireAt);
		user.setEmail(email);
		user.setPhone(phone);
		if (UserUtils.isRolePlatform() || UserUtils.isSuperAdmin()){
			//平台运维人员
			user.setSameTimeLogin(sameTimeLogin);
		}else {
			//机构管理员、普通用户无同时登录权限、超管默认可同时登录
			user.setSameTimeLogin(false);
		}
		if (roleIds != null && roleIds.length > 0) {
			List<Role> roles = roleService.findByIds(roleIds);
			if (roles != null && roles.size() > 0) {
				user.setRoles(new HashSet<>(roles));
			}
		} else {
			user.setRoles(null);
		}
		user.setDescriptions(descriptions);
		if (StringUtils.contains(UserUtils.ROLE_PLATFORM, user.getCheckRole()) && UserUtils.isSuperAdmin()) {
			String ownerId = user.getId();
			if (StringUtil.isNotEmpty(user.getSubGroupId())){
				ownerId = user.getSubGroupId();
			}
			hybaseShardService.save(HybaseFactory.getServer(),HybaseFactory.getUserName(),HybaseFactory.getPassword(),tradition,weiBo,weiXin,overseas,video,ownerId,user.getOrganizationId());
		}
		userService.update(user, false);
		return "修改用户成功！";
	}

	/**
	 * 删除用户
	 * 
	 * @date Created at 2017年12月15日 下午2:51:30
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation(" 删除用户")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.USER_DELETE, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@GetMapping("/deleteUser/{id}")
	public Object deleteUser(@ApiParam("用户id") @PathVariable(value = "id") String id) throws TRSException {
		User user = userService.findById(id);
		if (user != null) {
			User loginUser = UserUtils.getUser();
			// 如果是运维，则需要超管权限才能删除
			if (StringUtils.equals(UserUtils.ROLE_PLATFORM, user.getCheckRole())) {
				// 用户为超管--用户为机构管理员---用户为运维 才能删除用户
				if (UserUtils.isSuperAdmin()) {
					userService.deleteByUser(user);
					return "删除用户成功！";
				} else {
					throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "删除用户失败！权限不够");
				}
				// 如果是超管和管理员泽不能删除
			} else if (StringUtils.equals(UserUtils.ROLE_ADMIN, user.getCheckRole())
					|| StringUtils.equals(UserUtils.SUPER_ADMIN, user.getCheckRole())) {
				throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "删除用户失败！权限不够");
			} else {
				// 用户为超管--用户为机构管理员---用户为运维 才能删除用户
				if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())) {
					//判断该用户是否为 其用户分组下的最后一个用户，若为最后一个，则不能被删除
					List<User> userList = userService.findBySubGroupId(user.getSubGroupId());
					if (ObjectUtil.isNotEmpty(userList)){
						if (userList.size() < 2){
							throw new TRSException(CodeUtils.FAIL, "删除用户失败！该用户为所属用户分组下的最后一个用户！");
						}
					}else {
						throw new TRSException(CodeUtils.FAIL, "删除用户失败！该用户所属用户分组下已无登录账号！");
					}
					userService.deleteByUser(user);
					return "删除用户成功！";
				} else {
					throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "删除用户失败！权限不够");
				}
			}

		}
		throw new TRSException(CodeUtils.FAIL, "删除用户失败！");
	}

	/**
	 * 修改状态--启用、禁用
	 * 
	 * @date Created at 2017年11月20日 下午3:35:28
	 * @Author 谷泽昊
	 * @param id
	 * @param status
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("修改用户状态")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.USER_UPDATE_STATUS, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@GetMapping(value = "/updateStatus/{id}")
	public Object updateStatus(@ApiParam("用户id") @PathVariable(value = "id") String id,
			@ApiParam("状态  1为冻结 0为正常") @RequestParam(value = "status") String status) throws TRSException {
		// 判断权限
		if (!UserUtils.ROLE_LIST.contains(UserUtils.getUser().getCheckRole())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改状态！");
		}

		User user = userService.findById(id);
		if (UserUtils.isRoleOrdinary(user)){
			SubGroup subGroup = subGroupService.findOne(user.getSubGroupId());
			if (UserUtils.isRoleOrdinary(user)){
				if ("1".equals(status) && subGroup.getStatus().equals(status)){
					throw new TRSException(CodeUtils.FAIL,"当前用户所属分组呈冻结状态，所以其状态无法开启！");
				}
			}
		}

		user.setStatus(Status.getStatusByValue(status));
		userService.update(user, false);
		return "修改状态成功！";
	}

	/**
	 * 修改用户是否唯一登录
	 * 
	 * @date Created at 2018年9月19日 下午4:11:35
	 * @Author 谷泽昊
	 * @param id
	 * @param sameTimeLogin
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("修改用户是否唯一登录")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.USER_UPDATE_SAME_TIME_LOGIN, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@GetMapping(value = "/updateSameTimeLogin/{id}")
	public Object updateSameTimeLogin(@ApiParam("用户id") @PathVariable(value = "id") String id,
			@ApiParam("状态  true唯一,false不唯一") @RequestParam(value = "sameTimeLogin") boolean sameTimeLogin)
			throws TRSException {
		// 判断权限
		if (!UserUtils.ROLE_LIST.contains(UserUtils.getUser().getCheckRole())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改用户是否唯一登录！");
		}

		User user = userService.findById(id);
		if (!UserUtils.ROLE_PLATFORM.equals(user.getCheckRole())){
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限被修改是否唯一登录！");
		}
		user.setSameTimeLogin(sameTimeLogin);
		userService.update(user, false);
		return "修改成功！";
	}

	@ApiOperation("个人中心")
	@FormatResult
	@GetMapping(value = "/selectUserinfo")
	public Object selectUserinfo() throws TRSException {
		try {
			Map<String, Object> map = new HashMap<>();
			Subject currentUser = SecurityUtils.getSubject();
			// 删除微信登录信息
			String username = null;
			User user = null;
			try {
				user = (User) currentUser.getPrincipal();
				username = user.getUserName();
				RedisUtil.deleteString(username + String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN));
				log.info(username + "微信登录");
			} catch (Exception e) {
				username = (String) currentUser.getPrincipal();
				log.info(username + "非微信登录");
			}
			user = userService.findByUserName(username);
			String phone = user.getPhone();
			map.put("phone", phone);// 手机号
			String orgId = user.getOrganizationId();
			Organization organization = organizationService.findById(orgId);
			if (organization != null) {
				map.put("organizationName", organization.getOrganizationName());
			}
			boolean authentication = false;
			Date createdTime = null;
			List<Weixinlogin> weixinUser = weixinLoginRepository.findByUserAccount(username);
			if (ObjectUtil.isNotEmpty(weixinUser) && weixinUser.size() > 0) {
				authentication = true;
				createdTime = weixinUser.get(0).getCreatedTime();
			}
			map.put("createdTime", createdTime);
			map.put("authentication", authentication);// 是否绑定微信
			return map;
		} catch (Exception e) {
			log.error("修改机构状态失败：", e);
			throw new TRSException(CodeUtils.FAIL, "查询个人信息失败");
		}

	}

	/**
	 * 运维赋管理机构
	 * 
	 * @date Created at 2018年9月20日 下午6:37:57
	 * @Author 谷泽昊
	 * @param id
	 * @param organizationIds
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("运维赋管理机构")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.USER_ADD_HOLD_ORGANIZATION, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@GetMapping(value = "/addHoldOrganization/{id}")
	public Object addHoldOrganization(@ApiParam("用户id") @PathVariable(value = "id") String id,
			@ApiParam("机构Id,多个用逗号隔开") @RequestParam(value = "organizationIds") String[] organizationIds)
			throws TRSException {
		if (!UserUtils.isSuperAdmin()) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "赋权失败！权限不够");
		}

		userService.addHoldOrganization(id, organizationIds);

		return "设置成功！";
	}

	/**
	 * 去掉运维管理机构
	 * 
	 * @date Created at 2018年9月20日 下午6:37:53
	 * @Author 谷泽昊
	 * @param id
	 * @param organizationIds
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("去掉运维管理的机构")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.USER_DELETE_HOLD_ORGANIZATION, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@GetMapping(value = "/deleteHoldOrganization/{id}")
	public Object deleteHoldOrganization(@ApiParam("用户id") @PathVariable(value = "id") String id,
			@ApiParam("机构Id,多个用逗号隔开") @RequestParam(value = "organizationIds") String[] organizationIds)
			throws TRSException {
		if (!UserUtils.isSuperAdmin()) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "赋权失败！权限不够");
		}

		userService.deleteHoldOrganization(id, organizationIds);

		return "设置成功！";
	}

	/**
	 * 获取所有url链接
	 * 
	 * @date Created at 2018年9月12日 下午4:07:46
	 * @Author 谷泽昊
	 * @param request
	 * @return
	 */
	@GetMapping("/getAllUrl")
	public Set<String> getAllUrl(HttpServletRequest request) {
		Set<String> result = new HashSet<String>();
		WebApplicationContext wc = (WebApplicationContext) request
				.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		RequestMappingHandlerMapping bean = wc.getBean(RequestMappingHandlerMapping.class);
		Map<RequestMappingInfo, HandlerMethod> handlerMethods = bean.getHandlerMethods();
		for (RequestMappingInfo rmi : handlerMethods.keySet()) {
			PatternsRequestCondition pc = rmi.getPatternsCondition();
			Set<String> pSet = pc.getPatterns();
			result.addAll(pSet);
		}
		return result;
	}

	/**
	 * 在用户分组下添加用户
	 * @param subGroupId
	 * @param userName
	 * @param displayName
	 * @param password
	 * @param passwordAgain
	 * @param roleIds
	 * @param email
	 * @param phone
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("在用户分组下添加用户")
	@FormatResult
	@PostMapping("/addUserForGroup")
	public Object addUserForGroup(@ApiParam("用户分组id") @RequestParam(value = "subGroupId") String subGroupId,
								  @ApiParam("账号") @RequestParam(value = "userName") String userName,
						  @ApiParam("用户名") @RequestParam(value = "displayName") String displayName,
						  @ApiParam("密码") @RequestParam(value = "password") String password,
						  @ApiParam("再次输入密码") @RequestParam(value = "passwordAgain") String passwordAgain,
						  @ApiParam("普通账号权限，多个用逗号隔开") @RequestParam(value = "roleIds", required = false) String[] roleIds,
						  @ApiParam("邮件") @RequestParam(value = "email", required = false) String email,
						  @ApiParam("电话") @RequestParam(value = "phone", required = false) String phone)
			throws TRSException {

		// 判断账号是否为空
		if (StringUtils.isBlank(userName)) {
			throw new TRSException(CodeUtils.ACCOUNT_NULL, "账号不能为空！");
		}
		// 判断账号是否有空格
		if (RegexUtils.checkBlankSpace(userName)) {
			throw new TRSException(CodeUtils.ACCOUNT_SPACE, "账号中不能含有空格！");
		}

		// 判断用户名是否为空
		if (StringUtils.isBlank(displayName)) {
			throw new TRSException(CodeUtils.DISPLAYNAME_NULL, "用户名不能为空！");
		}

		// 判断密码是否为空
		if (StringUtils.isBlank(password)) {
			throw new TRSException(CodeUtils.PASSWORD_NULL, "密码不能为空！");
		}
		// 判断密码强度
		if (!RegexUtils.isLowSafely(password)) {
			throw new TRSException(CodeUtils.PASSWORD_LOW, "密码需要在8-16位，并且有大写、小写、数字和特殊字符至少三种！");
		}

		// 判断邮件是否为空
		if (StringUtils.isNotBlank(email)) {
			// 判断邮件是否正确
			if (!RegexUtils.checkEmail(email)) {
				throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
			}
		}

		// 判断两次密码是否一致
		if (!password.equals(passwordAgain)) {
			throw new TRSException(CodeUtils.PASSWORD_NOT_SAME, "两次密码不一样！");
		}
		// 验证手机号
		if (StringUtils.isNotBlank(phone) && !RegexUtils.checkMobile(phone)) {
			throw new TRSException(CodeUtils.PHONE_FAIL, "手机号填写错误！");
		}

		// 判断用户账号是否存在
		if (userService.findByUserName(userName) != null) {
			throw new TRSException(CodeUtils.USERNAME_EXISTED, "用户账号已存在！");
		}
		User loginUser = UserUtils.getUser();
		// 判断用户是否有权限新建
		if (!UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限新建用户！");
		}
		SubGroup subGroup = subGroupService.findOne(subGroupId);
		if (ObjectUtil.isEmpty(subGroup)){
			throw new TRSException(CodeUtils.FAIL,"找不到新建用户所属的用户分组！");
		}
		List<User> users = userService.findBySubGroupId(subGroupId);
		if (subGroup.getUserLimit() <= users.size() && subGroup.getUserLimit() != -1) {
			throw new TRSException(CodeUtils.USER_LIMIT, "已经建够[" + subGroup.getUserLimit() + "]个用户！");
		}

		// 判断当前登录用户，如果是超管和运维，则需要机构id，如果为机构管理员，则为机构管理员id
		String organizationId = subGroup.getOrganizationId();


		// 盐
		String salt = UUID.randomUUID().toString();
		// 加密的盐
		String encryptPsw = UserUtils.getEncryptPsw(password, salt);
		User user = new User();

		user.setUserName(userName);
		user.setDisplayName(displayName);
		user.setPassword(encryptPsw);
		user.setSalt(salt);
		user.setEmail(email);
		user.setPhone(phone);
		user.setLastLoginTime(null);
		user.setExpireAt(subGroup.getExpireAt());
		user.setLastLoginIp(null);
		user.setStatus(Status.normal);
		user.setSubGroupId(subGroup.getId());
		//user.setDescriptions(descriptions);
		user.setSameTimeLogin(false);
		Set<Role> roleSet = subGroup.getRoles();
		if (ObjectUtil.isNotEmpty(roleSet)){
			List<String> subGroupRoles = new ArrayList<>();
			for (Role role : roleSet) {
				subGroupRoles.add(role.getId());
			}

			if (ObjectUtil.isNotEmpty(roleIds)){
				for (int i = 0; i < roleIds.length; i++) {
					if ( ! subGroupRoles.contains(roleIds[i])){
						throw new TRSException(CodeUtils.FAIL,"当前用户添加了无效权限");
					}
				}

				List<Role> roles = roleService.findByIds(roleIds);
				user.setRoles(new HashSet<>(roles));
			}else {
				user.setRoleIds(null);
			}
		}else {
			throw new TRSException(CodeUtils.FAIL,"账号超越用户分组权限，无法创建用户！");
		}

		//普通用户
		user.setCheckRole(CheckRole.ROLE_ORDINARY);
		user.setOrganizationId(organizationId);
		try {
			userService.add(user, false);
			return "添加用户成功！";
		} catch (Exception e) {
			log.error("添加用户失败：", e);
		}
		throw new TRSException(CodeUtils.FAIL, "添加用户失败！");
	}
	/**
	 * 超管修改机构管理员（用户分组 设置）
	 * @param id
	 * @param userName
	 * @param displayName
	 * @param email
	 * @param phone
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("超管修改机构管理员")
	@FormatResult
	@PostMapping(value = "/updateOrgAdmin/{id}")
	public Object updateOrgAdmin(@ApiParam("用户id") @PathVariable(value = "id") String id,
							   @ApiParam("管理员账号") @RequestParam(value = "userName",required = false) String userName,
								 @ApiParam("用户名") @RequestParam(value = "displayName",required = false) String displayName,
								 @ApiParam("联系邮箱") @RequestParam(value = "email",required = false) String email,
								 @ApiParam("联系电话") @RequestParam(value = "phone", required = false) String phone) throws TRSException {
		// 判断权限
		User loginUser = UserUtils.getUser();
		//(登录账号不为 超管或运维或机构管理员)
		if (! UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改机构管理员信息！");
		}
		User user = userService.findById(id);
		//超管或运维有权限修改机构管理员的账号
		if (UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(loginUser.getCheckRole())){
			// 判断账号是否为空
			if (StringUtils.isBlank(userName)) {
				throw new TRSException(CodeUtils.ACCOUNT_NULL, "账号不能为空！");
			}
			// 判断账号是否有空格
			if (RegexUtils.checkBlankSpace(userName)) {
				throw new TRSException(CodeUtils.ACCOUNT_SPACE, "账号中不能含有空格！");
			}

			if (userService.findByUserName(userName) != null && !userService.findById(id).getUserName().equals(userName)){
				throw new TRSException(CodeUtils.USERNAME_EXISTED, "管理员账号已经被使用，请重新输入管理员账号！");
			}
		}else if (UserUtils.ROLE_ADMIN.equals(loginUser.getCheckRole()) && StringUtils.isNotEmpty(userName)){
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改机构管理员账号！");
		}


		// 判断邮件是否为空,是否正确
		if (StringUtils.isNotBlank(email) && !RegexUtils.checkEmail(email)) {
			throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
		}
		// 判断手机是否为空,是否正确
		if (StringUtils.isNotBlank(phone) && !RegexUtils.checkMobile(phone)) {
			throw new TRSException(CodeUtils.PHONE_FAIL, "手机号格式不正确！");
		}
//		防止机构管理员修改设置 时，  userName为空，导致该机构管理员无法登录问题
		if (!UserUtils.ROLE_ADMIN.equals(loginUser.getCheckRole())){
			user.setUserName(userName);
		}
		user.setDisplayName(displayName);
		user.setEmail(email);
		user.setPhone(phone);
		userService.update(user, false);
		return "修改机构管理员成功！";
	}

	/**
	 * 查询用户分组下的用户(弹框展示 不分页)
	 * @param subGroupId
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("查询某分组下的用户列表")
	@FormatResult
	@GetMapping("/pageListByGroup")
	public Object pageListByGroup(@ApiParam("用户分组 id") @RequestParam(value = "subGroupId") String subGroupId)
			throws TRSException {
		// 判断权限
		User user = UserUtils.getUser();
		String checkRole = user.getCheckRole();
		if (!UserUtils.ROLE_LIST.contains(checkRole)) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限查看！");
		}

		List<User> pageList = userService.findBySubGroupId(subGroupId);

		return pageList;
	}

	/**
	 * 查询某机构的机构管理员账号信息
	 * @param orgId
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("查询某机构的机构管理员账号信息")
	@FormatResult
	@GetMapping("/selectOrgAdmin")
	public Object selectOrgAdmin(@ApiParam("机构 id") @RequestParam(value = "orgId",required = false) String orgId)
			throws TRSException {
		// 判断权限
		User user = UserUtils.getUser();
		String checkRole = user.getCheckRole();
		if (!UserUtils.ROLE_LIST.contains(checkRole)) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限查看！");
		}

		if (UserUtils.isRoleAdmin()){
			return userService.findCurrentUser();
		}
		if (UserUtils.isSuperAdmin() || UserUtils.isRolePlatform()){
			//若为 超管或者运维 同样 同步 机构管理员的数据
			if (StringUtils.isBlank(orgId)){
				throw new TRSException(CodeUtils.FAIL,"请告诉我要查询哪个机构管理员的信息！");
			}
		}

		List<User> pageList = userService.findOrgAmdin(orgId);
		if (ObjectUtil.isNotEmpty(pageList)){
			User userOrg = pageList.get(0);
			//保证 新建、编辑的顺序展示
			if (ObjectUtil.isNotEmpty(userOrg)){
				if (userOrg.getRoles().size() > 1){
					Set<Role> roles = userOrg.getRoles();
					Set<Role> roleTreeSet = new TreeSet<>();

					for (Role role : roles) {
						if ("新建".equals(role.getRoleName())){
							roleTreeSet.add(role);
						}else {
							roleTreeSet.add(role);
						}
					}

					user.setRoles(roleTreeSet);
				}
			}
			return pageList.get(0);
		}

		return null;
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