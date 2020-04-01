package com.trs.netInsight.shiro.filter;

import com.trs.netInsight.util.JwtUtil;
import com.trs.netInsight.util.NetworkUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.IUserService;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 拦截token
 * 
 * @Type SimulatedLoginFilter.java
 * @Desc
 * @author 北京拓尔思信息技术股份有限公司
 * @author 谷泽昊
 * @date 2018年12月11日 下午2:30:28
 * @version
 */
@Setter
@Component
public class SimulatedLoginFilter extends AccessControlFilter {
	private IUserService userService;
	private IOrganizationService organizationService;

	private String url;

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
		////请求第二步
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String ip = NetworkUtil.getIpAddress(request);
		String token = request.getHeader("Simulated-Login-Token");
		// 如果token为空，直接放行
		if (StringUtils.isNotBlank(token)&&!StringUtils.equals("null", token) ) {
			try {
				// 放行
				Map<String, Object> map = JwtUtil.parseJwtToken(token);
				String mapIp = String.valueOf(map.get("ip"));
				if (!StringUtils.equals(ip, mapIp)) {
					WebUtils.issueRedirect(servletRequest, servletResponse, url);
					return false;
				}
				String loginId = String.valueOf(map.get("loginId"));
				//需要模拟登录的机构id
				String organizationId = String.valueOf(map.get("organizationId"));
				//需要模拟登录的用户id
				String userId = String.valueOf(map.get("userId"));
				User user = UserUtils.getUser();

				// 判断账号权限
				if (!UserUtils.ROLE_LIST.contains(user.getCheckRole())) {
					WebUtils.issueRedirect(servletRequest, servletResponse, url);
					return false;
				}
				// 如果是运维，判断这个机构是否为运维所管理的
				if (UserUtils.isRolePlatform() && !userService.isPlatformHoldOrganization(loginId, organizationId)) {
					WebUtils.issueRedirect(servletRequest, servletResponse, url);
					return false;
				}
				
				//如果是管理员，判断机构id是否一致
				if(UserUtils.isRoleAdmin() && !user.getOrganizationId().equals(organizationId)){
					WebUtils.issueRedirect(servletRequest, servletResponse, url);
					return false;
				}
				
				// 判断这个用户是不是这个机构的
				if (!organizationService.isOrganizationExistUser(organizationId, userId)) {
					WebUtils.issueRedirect(servletRequest, servletResponse, url);
					return false;
				}
				String loginUserId = user.getId();
				user = userService.findById(userId);
				HttpSession session = request.getSession();
				Object isAlert = session.getAttribute(UserUtils.SIMULATED_LOGIN_USER+loginUserId+user.getId());
				if(ObjectUtil.isNotEmpty(isAlert)){
					user.setIsAlert((Boolean)isAlert);
				}
				request.setAttribute(UserUtils.SIMULATED_LOGIN_USER, user);
			} catch (Exception e) {
				e.printStackTrace();
				WebUtils.issueRedirect(servletRequest, servletResponse, url);
				return false;
			}

		}
		// 放行
		return true;
	}

}
