package com.trs.netInsight.support.autowork.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import com.trs.netInsight.support.autowork.service.IQuartzScheduler;

import lombok.extern.slf4j.Slf4j;

/**
 * 通过监听spring加载完毕事件,进行任务初始化等操作<br>
 * <font color='red'>注:scheduler已经由application入口注入,此处不再管理</font>
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月19日
 *
 */
@Slf4j
@Configuration
public class ApplicationQuartzJobListener implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private IQuartzScheduler quartzScheduler;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			// 注册所有定时任务
			log.error("注册定时任务!");
			regist();
			quartzScheduler.startJob();
			log.error("任务启动!");
		} catch (Exception e) {
			log.error("task job onApplicationEvent error!", e);
		}
	}

	/**
	 * 注册定时任务
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @throws Exception
	 * @Return : void
	 */
	private void regist() throws Exception {
		// 获取所有定时任务
		quartzScheduler.regist();
	}

}
