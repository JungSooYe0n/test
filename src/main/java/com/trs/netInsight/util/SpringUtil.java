/*
 * Project: netInsight
 *
 * File Created at 2017年11月21日
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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 获取spring全局变量
 *
 * @author 谷泽昊
 * @Type SpringUtil.java
 * @date 2017年11月21日 上午11:47:51
 */
@Component
public class SpringUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	/**
	 * 获取applicationContext
	 * 
	 * @date Created at 2018年7月25日 下午5:00:38
	 * @Author 谷泽昊
	 * @return
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * 通过name获取 Bean.
	 * 
	 * @date Created at 2018年7月25日 下午5:00:41
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 */
	public static Object getBean(String name) {
		return getApplicationContext().getBean(name);
	}

	/**
	 * 通过class获取Bean.
	 * 
	 * @date Created at 2018年7月25日 下午5:00:47
	 * @Author 谷泽昊
	 * @param clazz
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	/**
	 * 通过name,以及Clazz返回指定的Bean
	 * 
	 * @date Created at 2018年7月25日 下午5:00:50
	 * @Author 谷泽昊
	 * @param name
	 * @param clazz
	 * @return
	 */
	public static <T> T getBean(String name, Class<T> clazz) {
		return getApplicationContext().getBean(name, clazz);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (SpringUtil.applicationContext == null) {
			SpringUtil.applicationContext = applicationContext;
		}
	}

	/**
	 * 全局获取request
	 * 
	 * @date Created at 2018年11月6日 下午6:13:02
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = ((ServletRequestAttributes) ra).getRequest();
		return request;
	}
	/**
	 * 得到所有传递的参数以Map的形式保存
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getParameterMap() {
		Map<String, String[]> map = (Map<String, String[]>) getRequest().getParameterMap();
		Map<String, Object> result = new HashMap<String, Object>();
		Set<Map.Entry<String, String[]>> set = map.entrySet();
		StringBuffer params = new StringBuffer("params: ");
		System.out.println("url------>"+getRequest().getRequestURL());
		for (Map.Entry<String, String[]> entry : set) {
			String key = entry.getKey();
			String[] values = entry.getValue();
			String value = StringUtils.join(values, "-");
			result.put(key, value);
			params.append(key + "=" + value + " ");
			System.out.println(key + "==" + value + " ");
		}
		return result;
	}
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月21日 谷泽昊 creat
 */