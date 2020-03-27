package com.trs.netInsight.support.appApi.handler;

import com.trs.netInsight.support.appApi.entity.AppApiFrequency;
import com.trs.netInsight.support.appApi.service.IApiFrequencyService;
import com.trs.netInsight.support.appApi.utils.constance.ApiMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听spring加载完毕事件,并校验api初始化妆台
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月3日
 *
 */
@Configuration
public class AppApplicationApiListener implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private IApiFrequencyService frequencyService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		long countAll = frequencyService.countAll();
		if (countAll <= 0l) {
			// 执行初始化操作
			init();
		}
	}

	/**
	 * 初始化api频率表
	 * 
	 * @since changjiang @ 2018年7月3日
	 * @Return : void
	 */
	private void init() {
		List<AppApiFrequency> list = new ArrayList<>();
		AppApiFrequency frequency = null;
		for (ApiMethod method : ApiMethod.values()) {
			frequency = new AppApiFrequency();
			frequency.setCode(method.getCode());
			frequency.setName(method.getName());
			if (StringUtils.isNotBlank(method.getFrequencyLow())) {
				frequency.setFrequencyLow(method.getFrequencyLow());
			}
			if (StringUtils.isNotBlank(method.getFrequencyCommon())) {
				frequency.setFrequencyCommon(method.getFrequencyCommon());
			}
			if (StringUtils.isNotBlank(method.getFrequencyHigh())) {
				frequency.setFrequencyHigh(method.getFrequencyHigh());
			}
			list.add(frequency);
		}
		this.frequencyService.save(list);
	}

}
