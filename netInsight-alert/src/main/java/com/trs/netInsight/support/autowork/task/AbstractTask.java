package com.trs.netInsight.support.autowork.task;

import org.quartz.Job;

/**
 * 定时任务业务类基类,所有业务实现均须继承该类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月19日
 *
 */
public abstract class AbstractTask implements Job {

	/**
	 * 定时任务准备方法
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @Return : void
	 */
	protected abstract void before();

	/**
	 * 定时任务结束方法
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @Return : void
	 */
	protected abstract void after();

}
