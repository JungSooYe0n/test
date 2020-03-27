package com.trs.netInsight.support.autowork.task;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

import com.trs.netInsight.support.autowork.exception.JobException;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试任务类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月19日
 *
 */
@Slf4j
@Service
public class TestTask extends AbstractTask {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		before();
		testMethod();
		after();
	}

	@Override
	protected void before() {
		log.error("开始执行!");
	}

	@Override
	protected void after() {
		log.error("执行结束!");
	}

	private void testMethod() throws JobException {
		throw new JobException("哈哈哈哈,错了吧!");
	}

}
