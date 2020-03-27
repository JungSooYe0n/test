package com.trs.netInsight.support.autowork.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.support.autowork.entity.Task;

/**
 * 定时任务运行记录日志持久层
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月3日
 *
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, String>, JpaSpecificationExecutor<Task> {

	/**
	 * 修改指定任务的状态
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param taskId
	 * @param status
	 * @Return : void
	 */
	@Query(value = "UPDATE `TASK` SET `STATE`= (:state) WHERE ID= (:taskId)", nativeQuery = true)
	@Modifying
	public void updateStatus(String taskId, boolean state);

	/**
	 * 批量修改任务状态
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param taskIds
	 * @param status
	 * @Return : void
	 */
	@Query(value = "UPDATE `TASK` SET `STATE`= (:state) WHERE ID IN (:taskIds)", nativeQuery = true)
	@Modifying
	public void updateStatus(List<String> taskIds, boolean state);

	/**
	 * 根据id批量删除
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param taskIds
	 * @Return : void
	 */
	@Query(value = "DELETE FROM `TASK` WHERE ID IN (:taskIds)", nativeQuery = true)
	@Modifying
	public void batchDelete(List<String> taskIds);

	/**
	 * 获取某一状态下所有任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param state
	 * @return
	 * @Return : List<Task>
	 */
	public List<Task> findAllByStateOrderByCreatedTime(boolean state);
	
	/**
	 * 根据业务类路径名获取任务
	 * @param source 路径
	 * @return
	 */
	public List<Task> findBySource(String source);

	/**
	 * 根据id结合检索任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param taskIds
	 * @return
	 * @Return : List<Task>
	 */
	@Query(value = "SELECT * FROM `TASK` WHERE ID IN (:taskIds)", nativeQuery = true)
	public List<Task> findAll(List<String> taskIds);

}
