package com.trs.netInsight.support.api.filter;

import com.trs.netInsight.support.api.entity.ApiAccessToken;
import com.trs.netInsight.support.api.entity.ApiClient;
import com.trs.netInsight.support.api.entity.ApiFrequency;
import com.trs.netInsight.support.api.exception.ApiException;
import com.trs.netInsight.support.api.handler.Api;
import com.trs.netInsight.support.api.result.ApiCommonResult;
import com.trs.netInsight.support.api.result.ApiResultType;
import com.trs.netInsight.support.api.service.IApiFrequencyService;
import com.trs.netInsight.support.api.service.IOAuthService;
import com.trs.netInsight.support.api.utils.constance.GrantRange;
import com.trs.netInsight.util.RedisUtil;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * api AOP拦截器,负责参数校验、粗细粒度权限范围验证、参数处理及二次绑定、异常处理、统一结果集返回
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月4日
 *
 */
@Aspect
@Order(-99)
@Component
@Slf4j
public class AccessControlFilter {

	@Autowired
	private IOAuthService authService;

	@Autowired
	private IApiFrequencyService frequencyService;

	@Autowired
	private IUserService userService;

	/**
	 * 拦截被Api标注的方法
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param point
	 * @param api
	 * @throws Throwable
	 * @Return : void
	 */
	@Around("@annotation(api)")
	public Object before(ProceedingJoinPoint point, Api api) throws Throwable {
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
				ApiAccessToken token = this.authService.checkAccessToken(accessToken);
				if (token == null || token.getClient() == null) {
					result = new ApiCommonResult(ApiResultType.GrantError);
				}else if(new Date().after(token.getExpireTime())){
					//验证token是否过期
					result = new ApiCommonResult(ApiResultType.Expire);
				} else {

					// 验证授权范围,超限抛异常
					grantRange(token.getGrantRange(), api);
					/*if(api.method().getCode() == 6001){
						frequencyLogService.recordFrequency(token.getClientId(),token.getClient().getGrantOrgId(),api.method().getCode(),api.method().getName());
					}*/
					// 计数,超限抛异常
					String key = api.method().getName() + accessToken;
					counter(token.getClient(), api.method().getCode(), key);

					// ip访问限制,五分钟内超过3个ip访问同一个接口,抛异常
					ipFilter(key, request);

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
	private String dealOwnerId(String ownerId, ApiClient client) {
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
	 * api调用计数器
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param code
	 * @param key
	 * @Return : void
	 */
	private void counter(ApiClient client, int code, String key) throws ApiException {
		if (code == 0 || StringUtils.isBlank(key)) {
			throw new ApiException(ApiResultType.NotFind);
		}
		log.info("ApiClient grantOrgId："+client.getGrantOrgId()+",对应clientName："+client.getClientName());
		// 获取该api调用频率
		ApiFrequency frequency = frequencyService.findByCodeWithClient(code, client);
		int maxH = 30;// 每小时最大调用数
		int maxD = 300;// 每天最大调用数
		String frequencyStr = frequency.getFrequencyCustom();
		if (StringUtils.isNotBlank(frequencyStr)) {// 自定义频率优先
			String[] range = frequencyStr.split(";");
			maxH = Integer.valueOf(range[0]);
			maxD = Integer.valueOf(range[1]);
		}

		// 每小时调用次数
		String _Hkey = "Hkey" + key;
		int maxStepH = 0;
		if (RedisUtil.getInteger(_Hkey) == null) {
			//maxStepH++;
			RedisUtil.setInteger(_Hkey, maxStepH);
			RedisUtil.expire(_Hkey, 60, TimeUnit.MINUTES);
			log.info("_Hkey new："+_Hkey+",对应maxStepH值："+maxStepH);
		} else {
			maxStepH = RedisUtil.getInteger(_Hkey);
			//maxStepH++;
			RedisUtil.increment(_Hkey, 1);
			Integer integer = RedisUtil.getInteger(_Hkey);
			log.info("_Hkey 未超出："+_Hkey+",对应maxStepH值："+maxStepH+",redis获取请求次数："+integer);
			if (maxStepH > maxH) {
				log.info("_Hkey 超出："+_Hkey+",对应maxStepH值："+maxStepH+",redis获取请求次数："+integer);
				throw new ApiException(ApiResultType.TooMany);
			}
			//RedisUtil.increment(_Hkey, 1);
		}

		// 每天调用次数
		String _Dkey = "Dkey" + key;
		int maxStepD = 0;
		if (RedisUtil.getInteger(_Dkey) == null) {
			//maxStepD++;
			RedisUtil.setInteger(_Dkey, maxStepD);
			RedisUtil.expire(_Dkey, 1, TimeUnit.DAYS);
			log.info("_Dkey new："+_Dkey+",对应maxStepD值："+maxStepD);
		} else {
			maxStepD = RedisUtil.getInteger(_Dkey);
			//maxStepD++;
			RedisUtil.increment(_Dkey, 1);
			Integer integer = RedisUtil.getInteger(_Dkey);
			log.info("_Dkey 未超出："+_Dkey+",对应maxStepD值："+maxStepD+",redis获取请求次数："+integer);
			if (maxStepD > maxD) {
				log.info("_Dkey 超出："+_Dkey+",对应maxStepD值："+maxStepD+",redis获取请求次数："+integer);
				throw new ApiException(ApiResultType.TooMany);
			}
			//RedisUtil.increment(_Dkey, 1);
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
	private void grantRange(String grantRange, Api api) throws ApiException {
		if (StringUtils.isNotBlank(grantRange)) {
			String[] gangeArray = grantRange.split(";");
			List<String> list = Arrays.asList(gangeArray);
			if (!list.contains(api.method().getGrantTypeCode()) && !list.contains(GrantRange.Max.getCode())) {
				throw new ApiException(ApiResultType.Forbidden);
			} else {
				// TODO 资源权限认证
				if (api.method().getGrantParams() != null) {

				}
			}
		}
	}

	/**
	 * 校验ip
	 * 
	 * @since changjiang @ 2018年7月9日
	 * @param key
	 * @param request
	 * @throws ApiException
	 * @Return : void
	 */
	private void ipFilter(String key, HttpServletRequest request) throws ApiException {
		String ipKey = "IP" + key;
		String ip = request.getRemoteAddr();
		List<String> ipList = RedisUtil.getList(ipKey, String.class);
		if (ipList != null && ipList.size() > 0) {
			if (ipList.contains(ip)) {
				return;
			} else {
				if (ipList.size() < 5) {
					ipList.add(ip);
					RedisUtil.setListForObject(ipKey, ipList, 5, TimeUnit.MINUTES);
				} else {
					throw new ApiException(ApiResultType.IpLimited);
				}
			}
		} else {
			ipList = new ArrayList<>();
			ipList.add(ip);
			RedisUtil.setListForObject(ipKey, ipList, 5, TimeUnit.MINUTES);
		}
	}

}
