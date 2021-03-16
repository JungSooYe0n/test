package com.trs.netInsight.handler;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.util.CodeUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.trs.netInsight.handler.exception.LoginException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.CommonResult;
import com.trs.netInsight.handler.result.ResultCode;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.support.cache.PerpetualPool;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 利用aop统一处理controller返回数据格式
 *
 * Created by yan.changjiang on 2017/4/10.
 */
@Aspect
@Configuration
@Order(9999)
@Slf4j
public class GlobalResultHandler {

	/**
	 * 定义切点Pointcut,拦截相应方法 此方法不能有返回值，该方法只是一个标示
	 */
	@Pointcut("@annotation(com.trs.netInsight.handler.result.FormatResult)")
	public void excludeService() {
	}

	/**
	 * @Around，环绕通知，放在方法头上，这个方法要决定真实的方法是否执行，而且必须有返回值。 简单理解，环绕通知=前置+目标方法执行+后置通知
	 * @param pjp
	 * @return
	 * @throws TRSException
	 */
	@Around("excludeService()")
	public Object doAround(ProceedingJoinPoint pjp) throws TRSException {
		try {
			// 获取当前方法
			Signature sig = pjp.getSignature();
			MethodSignature signature;
			if (!(sig instanceof MethodSignature)) {
				throw new TRSException("该注解");
			}
			signature = (MethodSignature) sig;
			Object target = pjp.getTarget();
			Method currentMethod = target.getClass().getMethod(signature.getName(), signature.getParameterTypes());

			if (currentMethod.isAnnotationPresent(EnableRedis.class)) {
				RequestAttributes ras = RequestContextHolder.getRequestAttributes();
				HttpServletRequest request = ((ServletRequestAttributes) ras).getRequest();
				EnableRedis annotation = currentMethod.getAnnotation(EnableRedis.class);
				Object[] args = pjp.getArgs();
				String parameter = request.getParameter(annotation.poolId());

				// if ("userId".equals(annotation.poolId())) {
				// HttpSession session = request.getSession();
				// SecurityContextImpl security = (SecurityContextImpl) session
				// .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
				// Map principal = (Map)
				// security.getAuthentication().getPrincipal();
				// parameter = principal.get("id").toString();
				// }
				String pool = PerpetualPool.get(parameter);
				// 方法名+参数 组成key
				StringBuilder redisKey = new StringBuilder(currentMethod.getName() + parameter + pool);
				for (Object arg : args) {
					if (ObjectUtil.isNotEmpty(arg)) {
						redisKey.append(arg.toString());
					}
				}
				String valueFromRedis = RedisFactory.getValueFromRedis(redisKey.toString());
				// 直接从redis中获取运行结果
				if (StringUtil.isNotEmpty(valueFromRedis)) {
					log.debug("------------>从redis中获得运行结果: key-->"+redisKey.toString());
					return ObjectUtil.toObject(valueFromRedis, CommonResult.class);
				}
				// 如果key不存在 把新运行的结果放到redis里边
				CommonResult commonResult = CommonResult.build(pjp.proceed());
				if (commonResult.getCode() == 200) {
					RedisFactory.setValueToRedis(redisKey.toString(), commonResult, annotation.cacheMinutes(),
							TimeUnit.MINUTES);
				}
				// 如果内容为空 也返回空 这个还没测 先不提交
				// if(commonResult.getData() instanceof Page){
				// Page<?> p= (Page<?>) commonResult.getData();
				// if(ObjectUtil.isEmpty(p.getContent())){
				//// commonResult = null;
				// return new CommonResult(204,"没有符合条件的数据！",null);
				// }
				// }
				return commonResult;
			}
			// proceed方法的作用是让目标方法执行
			return CommonResult.build(pjp.proceed());
		} catch (Throwable throwable) {
			log.error(throwable.getMessage());
			if (throwable instanceof LoginException) {
				return new CommonResult(((LoginException) throwable).getCode(), throwable.getMessage(), null);
			} else if (throwable instanceof TRSException) {
				if (throwable.getMessage().contains("检索超时")){
					throw new TRSException("处理controller结果出错,message:" + throwable, CodeUtils.HYBASE_TIMEOUT,
							throwable);
				}else if (throwable.getMessage().contains("表达式过长")){
					throw new TRSException("处理controller结果出错,message:" + throwable, CodeUtils.HYBASE_EXCEPTION,
							throwable);
				}
				throw (TRSException) throwable;
			} else {
				if (throwable.getMessage().contains("检索超时")){
					throw new TRSException("处理controller结果出错,message:" + throwable, CodeUtils.HYBASE_TIMEOUT,
							throwable);
				}else if (throwable.getMessage().contains("表达式过长")){
					throw new TRSException("处理controller结果出错,message:" + throwable, CodeUtils.HYBASE_EXCEPTION,
							throwable);
				}
				throw new TRSException("处理controller结果出错,message:" + throwable, ResultCode.OPERATION_EXCEPTION,
						throwable);
			}
		}
	}
}
