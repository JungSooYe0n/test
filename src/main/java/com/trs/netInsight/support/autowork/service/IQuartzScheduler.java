package com.trs.netInsight.support.autowork.service;

import java.util.List;

import org.quartz.Job;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.trs.netInsight.support.autowork.entity.Task;

/**
 * 定时任务调度服务接口
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月20日
 *
 */
public interface IQuartzScheduler {

	/**
	 * 启动所有定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @throws SchedulerException
	 * @Return : void
	 */
	public void startJob() throws SchedulerException;

	/**
	 * 获取指定任务的Job信息
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param task
	 *            定时任务实体
	 * @return
	 * @throws SchedulerException
	 * @Return : String
	 */
	public String getJobInfo(Task task) throws SchedulerException;

	/**
	 * 获取指定任务的Job信息
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param jobKey
	 *            定时任务key
	 * @param group
	 * @return
	 * @throws SchedulerException
	 * @Return : String
	 */
	public String getJobInfo(String jobKey, String group) throws SchedulerException;

	/**
	 * 修改指定任务的执行时间
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param task
	 *            定时任务实体
	 * @return
	 * @throws SchedulerException
	 * @Return : boolean
	 */
	public boolean modifyJob(Task task) throws SchedulerException;

	/**
	 * 修改指定任务的执行时间
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param jobKey
	 *            定时任务key
	 * @param group
	 *            分组
	 * @param cron
	 *            cron表达式
	 * @return
	 * @throws SchedulerException
	 * @Return : boolean
	 */
	public boolean modifyJob(String jobKey, String group, String cron) throws SchedulerException;

	/**
	 * 暂停所有定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @throws SchedulerException
	 * @Return : void
	 */
	public void pauseJob() throws SchedulerException;

	/**
	 * 暂停指定定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param task
	 *            定时任务实体
	 * @throws SchedulerException
	 * @Return : void
	 */
	public void pauseJob(Task task) throws SchedulerException;

	/**
	 * 暂停指定定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param jobKey
	 * @param group
	 * @throws SchedulerException
	 * @Return : void
	 */
	public void pauseJob(String jobKey, String group) throws SchedulerException;

	/**
	 * 恢复所有定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @throws SchedulerException
	 * @Return : void
	 */
	public void resumeJob() throws SchedulerException;

	/**
	 * 恢复指定定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param task
	 * @throws SchedulerException
	 * @Return : void
	 */
	public void resumeJob(Task task) throws SchedulerException;

	/**
	 * 恢复指定定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param jobKey
	 * @param group
	 * @throws SchedulerException
	 * @Return : void
	 */
	public void resumeJob(String jobKey, String group) throws SchedulerException;

	/**
	 * 删除所有定时任务<br>
	 * <font color='red'>(deletes!) all scheduling data - all {@link Job}s,
	 * {@link Trigger}s</font>
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @throws SchedulerException
	 * @Return : void
	 */
	@Deprecated
	public void deleteJob() throws SchedulerException;

	/**
	 * 删除指定定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param task
	 *            定时任务实体
	 * @throws SchedulerException
	 * @Return : void
	 */
	public void deleteJob(Task task) throws SchedulerException;

	/**
	 * 删除指定定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param jobKey
	 *            定时任务key
	 * @param group
	 * @throws SchedulerException
	 * @Return : void
	 */
	public void deleteJob(String jobKey, String group) throws SchedulerException;

	/**
	 * 判断任务是否已经存在于调度服务中
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param task
	 * @return
	 * @throws SchedulerException
	 * @Return : boolean
	 */
	public boolean exits(Task task) throws SchedulerException;

	/**
	 * 判断任务是否已经存在于调度服务中
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param jobKey
	 * @param group
	 * @return
	 * @throws SchedulerException
	 * @Return : boolean
	 */
	public boolean exits(String jobKey, String group) throws SchedulerException;

	/**
	 * 注册所有已知开启任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @throws Exception
	 * @Return : void
	 */
	public void regist() throws Exception;

	/**
	 * 注册指定定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param tasks
	 *            定时任务集
	 * @throws Exception
	 * @Return : void
	 */
	public void regist(List<Task> tasks) throws Exception;

	/**
	 * 注册指定定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param task
	 *            定时任务实体
	 * @throws Exception
	 * @Return : void
	 */
	public void regist(Task task) throws Exception;

	/**
	 * 注册指定定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param jobKey
	 *            任务id
	 * @param group
	 *            分组id
	 * @param cron
	 *            cron表达式
	 * @param source
	 *            全类名
	 * @throws Exception
	 * @Return : void
	 */
	public void regist(String jobKey, String group, String cron, String source) throws Exception;

}
