package com.trs.netInsight.support.template;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 当自动注入无法使用时， 可采取本方法手动注入
 */


@Component
public class ObjectContainer implements ServletContextListener {

	private static ApplicationContext applicationContext;

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		ServletContext servletContext = sce.getServletContext();
		// 获取Spring容器对象
		applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
	}

	public static Object getBean(String name) {
		return applicationContext.getBean(name);
	}

	public static Object getBean(Class<?> clazz) {
		try {
			return applicationContext.getBean(clazz);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}

	public static void setWebApplicationContext(ApplicationContext webApplicationContext) {
		ObjectContainer.applicationContext = webApplicationContext;
	}

	public static ApplicationContext getWebApplicationContext() {
		return applicationContext;
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletcontextevent) {
		// TODO Auto-generated method stub
		
	}

}
