package com.trs.netInsight.support.autowork.service.impl;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.support.autowork.entity.Task;
import com.trs.netInsight.support.autowork.entity.repository.TaskRepository;
import com.trs.netInsight.support.autowork.service.IQuartzScheduler;
import com.trs.netInsight.support.autowork.service.ITaskService;

/**
 * 定时任务服务接口实现类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月19日
 *
 */
@Service
public class TaskServiceImpl implements ITaskService {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private IQuartzScheduler quartzScheduler;

	@Override
	public List<Task> listAll() {
		return taskRepository.findAll();
	}

	@Override
	public List<Task> listAll(boolean stats) {
		return taskRepository.findAllByStateOrderByCreatedTime(stats);
	}

	@Override
	public Task findOne(String id) {
		return taskRepository.findOne(id);
	}

	@Override
	public Page<Task> pageList(Pageable pageable) {
		return taskRepository.findAll(pageable);
	}

	@Override
	@Transactional
	public Task save(Task entity) {
		return taskRepository.save(entity);
	}

	@Override
	public List<Task> save(List<Task> list) {
		return taskRepository.save(list);
	}

	@Override
	@Transactional
	public Task update(Task entity) {
		return taskRepository.saveAndFlush(entity);
	}

	@Override
	@Transactional
	public Task update(Task entity, boolean reRegist) throws Exception {
		// 重新注册
		if (reRegist) {
			// remove old job
			if (this.quartzScheduler.exits(entity)) {
				this.quartzScheduler.deleteJob(entity);
			}
			// reGegist
			quartzScheduler.regist(entity);
		} else {
			// 修改Scheduler内job时间
			this.quartzScheduler.modifyJob(entity);
		}
		// 联动修改mysqlTask记录
		return this.taskRepository.saveAndFlush(entity);
	}

	@Override
	@Transactional
	public void openOrClose(String taskId, boolean status) throws Exception {
		// 修改状态
		taskRepository.updateStatus(taskId, status);
		// 动态运行或停止该定时任务
		Task task = taskRepository.findOne(taskId);
		if (status) {
			if (quartzScheduler.exits(task)) {
				// 已经存在于调度服务中,重新启动即可
				quartzScheduler.resumeJob(task);
			} else {
				// 未存在于调度服务中,需注册
				quartzScheduler.regist(task);
			}
		} else {
			// 暂停任务
			quartzScheduler.pauseJob(task);
		}

	}

	@Override
	@Transactional
	public void openOrClose(List<String> taskIds, boolean status) throws Exception {
		// 修改状态
		taskRepository.updateStatus(taskIds, status);
		// 动态运行或停止运行
		List<Task> all = this.taskRepository.findAll(taskIds);
		if (all != null && all.size() > 0) {
			for (Task task : all) {
				if (status) {
					// 如果该任务已被Scheduler移除,需重新注册
					if (!this.quartzScheduler.exits(task)) {
						this.quartzScheduler.regist(task);
					} else {
						quartzScheduler.resumeJob(task);
					}
				} else {
					quartzScheduler.pauseJob(task);
				}
			}
		}
	}

	@Override
	@Transactional
	public void delete(String taskId) throws SchedulerException {
		Task task = this.findOne(taskId);
		this.remove(task);
	}

	@Override
	@Transactional
	public void delete(Task task) throws SchedulerException {
		this.remove(task);
	}

	/**
	 * Scheduler与Mysql同时移除任务信息
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @param task
	 * @throws SchedulerException
	 * @Return : void
	 */
	private void remove(Task task) throws SchedulerException {
		// Scheduler移除Job信息
		this.quartzScheduler.deleteJob(task);
		// mysql移除对应task信息
		this.delete(task);
	}

	@Override
	@Transactional
	public void delete(List<String> taskIds) {
		taskRepository.batchDelete(taskIds);
	}

	@Override
	public Page<Task> pageList(String orgId, String taskName, boolean stats, Date begin, Date end, int pageNo,
			int pageSize, String orderBy, boolean sort) {
		return this.searchCondition(orgId, taskName, stats, begin, end, pageNo, pageSize, orderBy, sort);
	}

	/**
	 * 根据条件分页检索实现
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param orgId
	 *            机构id
	 * @param taskName
	 *            任务名称,模糊检索
	 * @param stats
	 *            任务状态,默认false,未开启
	 * @param begin
	 *            任务创建时间范围-开始
	 * @param end
	 *            任务创建时间范围-结束
	 * @param pageNo
	 *            分页
	 * @param pageSize
	 *            每页展示条数
	 * @param orderBy
	 *            排序字段
	 * @param sort
	 *            正逆序,true为正序,反之为逆序
	 * @return
	 * @Return : Page<Task>
	 */
	private Page<Task> searchCondition(String orgId, String taskName, boolean stats, Date begin, Date end, int pageNo,
			int pageSize, String orderBy, boolean sort) {

		Criteria<Task> criteria = new Criteria<>();

		// 机构id
		if (StringUtils.isNotBlank(orgId)) {
			criteria.add(Restrictions.eq("organizationId", orgId));
		}

		// 任务名称
		if (StringUtils.isNotBlank(taskName)) {
			criteria.add(Restrictions.like("taskName", "%" + taskName + "%"));
		}

		// 开启状态,默认为false
		criteria.add(Restrictions.eq("state", stats));

		// 创建时间范围
		if (begin != null && end != null) {
			criteria.add(Restrictions.between("createdTime", begin, end));
		}

		// 排序及分页
		Pageable pageable = new PageRequest(pageNo, pageSize);
		if (StringUtils.isNotBlank(orderBy)) {
			if (sort) {
				pageable = new PageRequest(pageNo, pageSize, Sort.Direction.ASC, orderBy);
			} else {
				pageable = new PageRequest(pageNo, pageSize, Sort.Direction.DESC, orderBy);
			}
		}
		return taskRepository.findAll(criteria, pageable);
	}

	@Override
	public void startAll() throws SchedulerException {
		this.quartzScheduler.resumeJob();
	}

	@Override
	public void stopAll() throws SchedulerException {
		this.quartzScheduler.pauseJob();
	}

	@Override
	public List<Task> findBySource(String source) {
		return taskRepository.findBySource(source);
	}

}
