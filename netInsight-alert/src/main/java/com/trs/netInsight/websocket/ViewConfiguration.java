package com.trs.netInsight.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class ViewConfiguration extends WebMvcConfigurerAdapter{
	//直接访问对应名字的html文件
	 @Override  
	    public void addViewControllers(ViewControllerRegistry registry){  
	        registry.addViewController("/chat");  
	    }  
}
