package com.trs.netInsight.support.autowork.service;

import java.util.Date;
import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trs.netInsight.support.autowork.entity.Task;

/**
 * 定时任务服务接口
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月19日
 *
 */
public interface ITaskService {

	/**
	 * 获取全部任务
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @return
	 * @Return : List<Task>
	 */
	public List<Task> listAll();
	
	/**
	 * 根据业务类路径名获取任务
	 * @param source 路径
	 * @return
	 */
	public List<Task> findBySource(String source);

	/**
	 * 获取某一状态下所有任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param stats
	 * @return
	 * @Return : List<Task>
	 */
	public List<Task> listAll(boolean stats);

	/**
	 * 根据id检索指定任务
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @param id
	 * @return
	 * @Return : Task
	 */
	public Task findOne(String id);

	/**
	 * 分页检索
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param pageable
	 * @return
	 * @Return : Page<Task>
	 */
	public Page<Task> pageList(Pageable pageable);

	/**
	 * 根据条件分页检索
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param orgId
	 *            机构id
	 * @param taskName
	 *            任务名称
	 * @param stats
	 *            任务状态
	 * @param begin
	 *            任务创建时间范围-开始
	 * @param end
	 *            任务创建时间范围-结束
	 * @param pageNo
	 *            当前页
	 * @param pageSize
	 *            每页展示条数
	 * @param orderBy
	 *            排序字段
	 * @param sort
	 *            正逆序, true为正序,false为逆序
	 * @return
	 * @Return : Page<Task>
	 */
	public Page<Task> pageList(String orgId, String taskName, boolean stats, Date begin, Date end, int pageNo,
			int pageSize, String orderBy, boolean sort);

	/**
	 * 保存
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param entity
	 * @return
	 * @Return : Task
	 */
	public Task save(Task entity);

	/**
	 * 批量保存
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param list
	 * @return
	 * @Return : List<Task>
	 */
	public List<Task> save(List<Task> list);

	/**
	 * 修改
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param entity
	 * @return
	 * @Return : Task
	 */
	public Task update(Task entity);

	/**
	 * 修改任务
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @param entity
	 *            任务实体
	 * @param reRegist
	 *            是否需要重新注册
	 * @return
	 * @Return : Task
	 */
	public Task update(Task entity, boolean reRegist) throws Exception;

	/**
	 * 更改任务状态
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param taskId
	 *            任务id
	 * @param status
	 *            状态
	 * @return
	 */
	public void openOrClose(String taskId, boolean status) throws Exception;

	/**
	 * 批量修改任务状态
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param taskIds
	 * @param status
	 * @return
	 */
	public void openOrClose(List<String> taskIds, boolean status) throws Exception;

	/**
	 * 根据id删除
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param taskId
	 * @Return : void
	 */
	public void delete(String taskId) throws SchedulerException;

	/***
	 * 根据实体删除
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param task
	 * @Return : void
	 */
	public void delete(Task task) throws SchedulerException;

	/**
	 * 批量删除
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param taskIds
	 * @Return : void
	 */
	public void delete(List<String> taskIds);

	/**
	 * 手动触发Scheduler中所有任务
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @Return : void
	 */
	public void startAll() throws SchedulerException;

	/**
	 * 手动停止Scheduler中所有任务
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @Return : void
	 */
	public void stopAll() throws SchedulerException;

}
