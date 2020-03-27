package com.trs.netInsight.support.appApi.filter;

import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.support.appApi.entity.AppApiClient;
import com.trs.netInsight.support.appApi.exception.ApiException;
import com.trs.netInsight.support.appApi.handler.AppApi;
import com.trs.netInsight.support.appApi.result.ApiCommonResult;
import com.trs.netInsight.support.appApi.result.ApiResultType;
import com.trs.netInsight.support.appApi.service.IOAuthService;
import com.trs.netInsight.support.appApi.utils.constance.GrantRange;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
/**
 * api AOP拦截器,负责参数校验、粗细粒度权限范围验证、参数处理及二次绑定、异常处理、统一结果集返回
 * @author 北京拓尔思信息技术股份有限公司
 * @since duhq @ 2019年4月15日
 *
 */
@Aspect
@Order(-99)
@Component
@Slf4j
public class AppAccessControlFilter {

	@Autowired
	private IOAuthService authService;

	@Autowired
	private IUserService userService;

	/**
	 * 拦截被Api标注的方法
	 */
	@Around("@annotation(api)")
	public Object before(ProceedingJoinPoint point, AppApi api) throws Throwable {
		log.debug("----------------->before");
		ApiCommonResult result = null;
		try {
			// 获取参数列表及参数值
			Signature signature = point.getSignature();
			Object[] paramValues = point.getArgs();
			MethodSignature methodSignature = (MethodSignature) signature;
			String[] paramNames = methodSignature.getParameterNames();
			String accessToken = null;
			HttpServletRequest request = null;
			for (int i = 0; i < paramNames.length; i++) {
				String param = paramNames[i];
				if (param.equals("accessToken")) {
					accessToken = String.valueOf(paramValues[i]);
				} else if (param.equals("request")) {
					request = (HttpServletRequest) paramValues[i];
				}
			}
			// 获取accessToken,并对其进行权限校验
			if (StringUtils.isNotBlank(accessToken) && request != null) {
				AppApiAccessToken token = this.authService.checkAccessToken(accessToken);
				if (token == null || token.getClient() == null) {
					result = new ApiCommonResult(ApiResultType.GrantError);
				} else if(new Date().after(token.getExpireTime())){
					//验证token是否过期
					result = new ApiCommonResult(ApiResultType.Invalidate);
				}else {
					// 验证授权范围,超限抛异常
					grantRange(token.getGrantRange(), api);
					// 校验资源拥有者
					String ownerId = dealOwnerId(token.getGrantSourceOwnerId(), token.getClient());
					token.setGrantSourceOwnerId(ownerId);

					// 重新绑定参数
					request.setAttribute("token", token);
					for (int i = 0; i < paramNames.length; i++) {
						String param = paramNames[i];
						if (param.equals("request")) {
							paramValues[i] = request;
						}
					}
					Object data = point.proceed(paramValues);// 方法运行
					log.debug("------------->原方法执行后");
					result = new ApiCommonResult(ApiResultType.Success, data);
				}
			} else {
				result = new ApiCommonResult(ApiResultType.ParamError);
			}
		} catch (Exception e) {
			if (e instanceof ApiException) {
				result = new ApiCommonResult(((ApiException) e).getType());
			} else {
				result = new ApiCommonResult(ApiResultType.ServerError, e.getMessage());
			}
			log.error("Api调用失败,请返回重试或联系管理员!e=[" + e.getMessage() + "]", e);
		}
		return result;
	}

	/**
	 * 处理资源拥有者
	 * 
	 * @since changjiang @ 2018年7月17日
	 * @param ownerId
	 *            资源拥有者id
	 * @param client
	 *            apiClient
	 * @return
	 * @Return : String
	 */
	private String dealOwnerId(String ownerId, AppApiClient client) {
		if (StringUtils.isNotBlank(ownerId)) {
			if (ownerId.equals("orgAdmin")) {
				List<User> users = userService.findOrgAmdin(client.getGrantOrgId());
				if (users != null && users.size() > 0) {
					ownerId = users.get(0).getId();
				}
			}
			return ownerId;
		} else {
			throw new ApiException(ApiResultType.NotFindSource);
		}
	}
	/**
	 * 权限认证
	 * 
	 * @since changjiang @ 2018年7月4日
	 * @param grantRange
	 *            token中保存的权限范围
	 * @param api
	 * @throws ApiException
	 * @Return : void
	 */
	@SuppressWarnings("unchecked")
	private void grantRange(String grantRange, AppApi api) throws ApiException {
		if (StringUtils.isNotBlank(grantRange)) {
			String[] gangeArray = grantRange.split(";");
			List<String> list = Arrays.asList(gangeArray);
			log.debug("------->注解中该方法授权范围=="+api.method().getGrantTypeCode());
			log.debug("------->APP拥有的权限list=="+list);
			if (!list.contains(api.method().getGrantTypeCode()) && !list.contains(GrantRange.Max.getCode())) {
				throw new ApiException(ApiResultType.Forbidden);
			} else {
				// TODO 资源权限认证
				if (api.method().getGrantParams() != null) {

				}
			}
		}
	}


}
