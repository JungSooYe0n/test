package com.trs.netInsight.support.api.handler;

import java.util.ArrayList;
import java.util.List;

import com.trs.netInsight.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import com.trs.netInsight.support.api.entity.ApiFrequency;
import com.trs.netInsight.support.api.service.IApiFrequencyService;
import com.trs.netInsight.support.api.utils.constance.ApiMethod;

/**
 * 监听spring加载完毕事件,并校验api初始化妆台
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月3日
 *
 */
@Configuration
public class ApplicationApiListener implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private IApiFrequencyService frequencyService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//long countAll = frequencyService.countAll();
		//if (countAll <= 0l) {
			// 执行初始化操作
			init();
		//}
	}

	/**
	 * 初始化api频率表
	 * 
	 * @since changjiang @ 2018年7月3日
	 * @Return : void
	 */
	private void init() {
		List<ApiFrequency> list = new ArrayList<>();
		ApiFrequency frequency = null;
		for (ApiMethod method : ApiMethod.values()) {
			ApiFrequency apiFrequency = frequencyService.findByCodeWithInit(method.getCode());
			if (ObjectUtil.isEmpty(apiFrequency)){
				frequency = new ApiFrequency();
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
		}
		this.frequencyService.save(list);
	}

}
