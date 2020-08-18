
package com.trs.netInsight;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.client.RestTemplate;

import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.widget.alert.quartz.AdaptableJobFactoryExtends;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * SpringBoot工程启动入口
 *
 * Created by ChangXiaoyang on 2017/2/15.
 */
//@ComponentScan(basePackages = "com.trs.netInsight")
//@EnableJpaRepositories(basePackages ="com.trs.netInsight.com.trs.netInsight.support.hybaseShard.repository")
//@EntityScan(basePackages = "com.trs.netInsight.com.trs.netInsight.support.hybaseShard.entity")
@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class NetInsightApplication implements CommandLineRunner {

	@Value("${trs.discover.address}")
	private String discoverHost;

	@Autowired
	private Environment environment;

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(NetInsightApplication.class, args);
		ObjectContainer.setWebApplicationContext(applicationContext);
	}

	/**
	 * 将RestTemplate注册为spring bean，使其可以被其他module直接自动注入使用
	 *
	 * @return RestTemplate
	 */
	@Bean
	public RestTemplate initRestTemplate() {
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setConnectionRequestTimeout(160000);
		httpRequestFactory.setConnectTimeout(160000);
		httpRequestFactory.setReadTimeout(160000);
		return new RestTemplate(httpRequestFactory);
	}

	@Autowired
	private RestTemplate initRestTemplate;

	/**
	 * 装载数据库中保存的ES索引，字段等信息
	 */
	@Override
	public void run(String... args) throws Exception {
		try {
			String port = environment.getProperty("server.port");
			String object = initRestTemplate.getForObject(discoverHost, String.class, port);
			log.info(object.contains("200") ? "success" : "monitor error");
		} catch (Exception e) {
			log.error("monitor error");
		}
	}

	/**
	 * 定时调度
	 * 
	 * @return Created By xiao.ying
	 */
	@Bean
	public AdaptableJobFactoryExtends adaptableJobFactory() {
		return new AdaptableJobFactoryExtends();
	}

	/**
	 * 定时调度
	 * 
	 * @return Created By xiao.ying
	 */
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() {
		Properties properties = new Properties();
		properties.setProperty("org.quartz.threadPool.threadCount", "50");
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		schedulerFactoryBean.setQuartzProperties(properties);
		schedulerFactoryBean.setJobFactory(adaptableJobFactory());
		return schedulerFactoryBean;

	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("*")
						.allowedMethods("PUT", "DELETE","GET","POST")
						.allowedHeaders("*")
						.exposedHeaders("access-control-allow-headers",
								"access-control-allow-methods",
								"access-control-allow-origin",
								"access-control-max-age",
								"X-Frame-Options")
						.allowCredentials(false).maxAge(3600);
			}
		};
	}
}
