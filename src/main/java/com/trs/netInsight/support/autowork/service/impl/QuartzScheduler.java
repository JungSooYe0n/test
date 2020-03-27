package com.trs.netInsight.support.autowork.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.support.autowork.entity.Task;
import com.trs.netInsight.support.autowork.service.IQuartzScheduler;
import com.trs.netInsight.support.autowork.service.ITaskService;
import com.trs.netInsight.util.EmailSendTool;

/**
 * 定时任务调度服务
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月20日
 *
 */
@Service
public class QuartzScheduler implements IQuartzScheduler {

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private ITaskService taskService;

	@Override
	public void startJob() throws SchedulerException {
		this.scheduler.start();
	}

	@Override
	public String getJobInfo(Task task) throws SchedulerException {
		return this.getJob(task.getId(), task.getOrganizationId());
	}

	@Override
	public String getJobInfo(String jobKey, String group) throws SchedulerException {
		return this.getJob(jobKey, group);
	}

	/**
	 * 获取指定job信息
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param name
	 * @param group
	 * @return
	 * @throws SchedulerException
	 * @Return : String
	 */
	private String getJob(String name, String group) throws SchedulerException {
		TriggerKey triggerKey = new TriggerKey(name, group);
		CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
		return String.format("time:%s,state:%s", cronTrigger.getCronExpression(),
				scheduler.getTriggerState(triggerKey).name());
	}

	@Override
	public boolean modifyJob(Task task) throws SchedulerException {
		return this.modify(task.getId(), task.getOrganizationId(), task.getCron());
	}

	@Override
	public boolean modifyJob(String jobKey, String group, String cron) throws SchedulerException {
		return this.modify(jobKey, group, cron);
	}

	/**
	 * 修改指定任务的执行时间
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param name
	 * @param group
	 * @param time
	 * @return
	 * @throws SchedulerException
	 * @Return : boolean
	 */
	private boolean modify(String name, String group, String time) throws SchedulerException {
		Date date = null;
		TriggerKey triggerKey = new TriggerKey(name, group);
		CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
		String oldTime = cronTrigger.getCronExpression();
		if (!oldTime.equalsIgnoreCase(time)) {
			CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(time);
			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group)
					.withSchedule(cronScheduleBuilder).build();
			date = scheduler.rescheduleJob(triggerKey, trigger);
		}
		return date != null;
	}

	@Override
	public void pauseJob() throws SchedulerException {
		this.scheduler.pauseAll();
	}

	@Override
	public void pauseJob(Task task) throws SchedulerException {
		this.pause(task.getId(), task.getOrganizationId());
	}

	@Override
	public void pauseJob(String jobKey, String group) throws SchedulerException {
		this.pause(jobKey, group);
	}

	/**
	 * 暂停指定任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param name
	 * @param group
	 * @throws SchedulerException
	 * @Return : void
	 */
	private void pause(String name, String group) throws SchedulerException {
		JobKey jobKey = new JobKey(name, group);
		JobDetail jobDetail = scheduler.getJobDetail(jobKey);
		if (jobDetail == null)
			return;
		this.scheduler.pauseJob(jobKey);
	}

	@Override
	public void resumeJob() throws SchedulerException {
		this.scheduler.resumeAll();
	}

	@Override
	public void resumeJob(Task task) throws SchedulerException {
		this.resume(task.getId(), task.getOrganizationId());
	}

	@Override
	public void resumeJob(String jobKey, String group) throws SchedulerException {
		this.resume(jobKey, group);
	}

	/**
	 * 恢复指定任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param name
	 * @param group
	 * @throws SchedulerException
	 * @Return : void
	 */
	private void resume(String name, String group) throws SchedulerException {
		JobKey jobKey = new JobKey(name, group);
		JobDetail jobDetail = this.scheduler.getJobDetail(jobKey);
		if (jobDetail == null)
			return;
		this.scheduler.resumeJob(jobKey);
	}

	@Override
	@Deprecated
	public void deleteJob() throws SchedulerException {
		this.scheduler.clear();
	}

	@Override
	public void deleteJob(Task task) throws SchedulerException {
		this.delete(task.getId(), task.getOrganizationId());
	}

	@Override
	public void deleteJob(String jobKey, String group) throws SchedulerException {
		this.delete(jobKey, group);
	}

	/**
	 * 删除指定任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param name
	 * @param group
	 * @throws SchedulerException
	 * @Return : void
	 */
	private void delete(String name, String group) throws SchedulerException {
		JobKey jobKey = new JobKey(name, group);
		JobDetail jobDetail = scheduler.getJobDetail(jobKey);
		if (jobDetail == null)
			return;
		scheduler.deleteJob(jobKey);
	}

	@Override
	public void regist() throws Exception {
		this.registJob(null);
	}

	@Override
	public void regist(List<Task> tasks) throws Exception {
		this.registJob(tasks);
	}

	@Override
	public void regist(Task task) throws Exception {
		this.registJob(task.getId(), task.getOrganizationId(), task.getCron(), task.getSource());
	}

	@Override
	public void regist(String jobKey, String group, String cron, String source) throws Exception {
		this.registJob(jobKey, group, cron, source);
	}

	/**
	 * 注册单个定时任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param name
	 *            任务id
	 * @param group
	 *            分组id
	 * @param cron
	 *            表达式
	 * @throws Exception
	 * @Return : void
	 */
	@SuppressWarnings("unchecked")
	private void registJob(String name, String group, String cron, String source) throws Exception {

		// 初始化任务所需元素
		JobKey jobKey = new JobKey(name, group);

		// 判断是否存在该任务
		if (!this.scheduler.checkExists(jobKey)) {

			TriggerKey triggerKey = new TriggerKey(name, group);
			Class<? extends Job> clazz = (Class<? extends Job>) Class.forName(source);

			// 构建JobDetail实例
			JobDetail detail = JobBuilder.newJob(clazz).withIdentity(jobKey).build();
			// 根据cron构建触发器实例
			CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(cron);
			CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronSchedule)
					.build();

			// 注册
			this.scheduler.scheduleJob(detail, cronTrigger);
		}
	}

	/**
	 * 注册指定任务集
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param tasks
	 *            tasks为null,注册所有已知并开启的任务
	 * @throws Exception
	 * @Return : void
	 */
	@SuppressWarnings("unchecked")
	private void registJob(List<Task> tasks) throws Exception {
		//检查定时发邮件类是否存在
		expireatEmail();
		// null or empty
		if (tasks == null || tasks.size() <= 0) {
			tasks = this.taskService.listAll(true);
		}

		Map<JobDetail, Set<? extends Trigger>> map = new HashMap<>();
		JobDetail detail = null;
		Class<? extends Job> clazz = null;
		Set<CronTrigger> trigger = null;
		CronScheduleBuilder cronScheduleBuilder = null;
		CronTrigger cronTrigger = null;
		JobKey key = null; // 任务key
		TriggerKey triggerKey = null; // 触发器key
		for (Task task : tasks) {
			if (scheduler.checkExists(key)) {
				continue;
			}
			trigger = new HashSet<>();
			
			// 以任务和机构id生成任务key以及触发器key
			key = new JobKey(task.getId(), task.getOrganizationId());
			triggerKey = new TriggerKey(task.getId(), task.getOrganizationId());

			// 通过JobBuilder构建JobDetail实例，JobDetail规定只能是实现Job接口的实例
			clazz = (Class<? extends Job>) Class.forName(task.getSource());
			detail = JobBuilder.newJob(clazz).withIdentity(key).build();

			// 基于表达式构建触发器
			cronScheduleBuilder = CronScheduleBuilder.cronSchedule(task.getCron());
			cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder)
					.build();
			trigger.add(cronTrigger);
			map.put(detail, trigger);
		}
		scheduler.scheduleJobs(map, false);
	}

	/**
	 * 检查定时发邮件类是否存在
	 */
	public void expireatEmail(){
		List<Task> sourceList = taskService.findBySource(EmailSendTool.EXPIREAT_EMAIL);
		if(sourceList==null || sourceList.size()==0){
			taskService.save(new Task("到期邮件预警",EmailSendTool.EXPIREAT_EMAIL,"0 0 10 * * ?","还有五天到期时开始每天八点发送邮件",true));
//			taskService.save(new Task("到期邮件预警",EmailSendTool.EXPIREAT_EMAIL,"0 0/1 * * * ? ","还有五天到期时开始每天八点发送邮件",true));
		}
	}
	@Override
	public boolean exits(Task task) throws SchedulerException {
		return scheduler.checkExists(new JobKey(task.getId(), task.getOrganizationId()));
	}

	@Override
	public boolean exits(String jobKey, String group) throws SchedulerException {
		return scheduler.checkExists(new JobKey(jobKey, group));
	}
	
	

}
