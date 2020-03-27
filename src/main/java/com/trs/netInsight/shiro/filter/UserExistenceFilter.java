package com.trs.netInsight.shiro.filter;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户过期而 session还在
 * 
 * @Type IpFilter.java
 * @Desc
 * @author 谷泽昊
 * @date 2017年12月6日 上午10:11:11
 * @version
 */
@Getter
@Setter
public class UserExistenceFilter extends AccessControlFilter {

	private String kickoutUrl; // 踢出后到的地址

	private int sessionTimeout;

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		Subject subject = getSubject(request, response);
		if (!subject.isAuthenticated()) {
			// 会话被踢出了
			try {
				// 退出登录
				subject.logout();
			} catch (Exception e) { // ignore
			}
			saveRequest(request);
			// 判断是不是Ajax请求
			// 重定向
			WebUtils.issueRedirect(request, response, kickoutUrl);
			return false;
		}
		Object principal = subject.getPrincipal();
		if (principal instanceof User) {
			User user = (User) principal;
			Session session = subject.getSession();
			Serializable id = session.getId();
			// 将用户信息存入redis
			RedisUtil.setString(UserUtils.USERNAME_LOGIN_USER + user.getUserName(), id.toString(), sessionTimeout,
					TimeUnit.SECONDS);
		}

		return true;
	}

}
