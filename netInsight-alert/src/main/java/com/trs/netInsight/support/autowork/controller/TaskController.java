package com.trs.netInsight.support.autowork.controller;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.autowork.entity.Task;
import com.trs.netInsight.support.autowork.service.ITaskService;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * 定时任务控制接口
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月19日
 *
 */
@RestController
@RequestMapping("/task")
@Slf4j
@Api(description = "定时任务控制接口")
public class TaskController {

	@Autowired
	private ITaskService taskService;

	/**
	 * 注册任务,注册完成默认不开启
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @param taskName
	 *            任务名称
	 * @param source
	 *            业务类全类名
	 * @param cron
	 *            cron表达式
	 * @param remark
	 *            备注
	 * @param state
	 *            开启状态
	 * @return
	 * @Return : Object
	 */
	@PostMapping("/regist")
	@FormatResult
	public Object regist(@ApiParam("taskName") @RequestParam(value = "taskName") String taskName,
			@ApiParam("source") @RequestParam(value = "source") String source,
			@ApiParam("cron") @RequestParam(value = "cron") String cron,
			@ApiParam("remark") @RequestParam(value = "remark", required = false) String remark,
			@ApiParam("state") @RequestParam(value = "state", required = false, defaultValue = "false") boolean state) {

		Task task = new Task(taskName, source, cron, remark, state);
		try {
			task = taskService.save(task);
		} catch (Exception e) {
			log.error("save task error, taskName=" + taskName + ",cron=" + cron + ",e.message=" + e.getMessage(), e);
		}
		return task;
	}

	/**
	 * 手动触发所有任务
	 * 
	 * @since changjiang @ 2018年6月19日
	 * @Return : void
	 */
	@GetMapping("/startAll")
	public void startAll() {
		try {
			this.taskService.startAll();
		} catch (SchedulerException e) {
			log.error("task startAll error: e.message=" + e.getMessage(), e);
		}
	}

	/**
	 * 手动停止所有任务
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @Return : void
	 */
	@GetMapping("/stopAll")
	public void stopAll() {
		try {
			this.taskService.stopAll();
		} catch (SchedulerException e) {
			log.error("task stopAll error : e.message=" + e.getMessage(), e);
		}
	}

	/**
	 * 启动或关闭指定任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param taskId
	 *            任务id
	 * @param stats
	 *            任务状态
	 * @Return : void
	 */
	@GetMapping("/openOrClose")
	public void openOrClose(@ApiParam("taskId") @RequestParam(value = "taskId") String taskId,
			@ApiParam("stats") @RequestParam(value = "stats") boolean stats) {
		try {
			this.taskService.openOrClose(taskId, stats);
		} catch (Exception e) {
			log.error("openOrClose task error, taskId=" + taskId + ",stats=" + stats + ",e.message" + e.getMessage(),
					e);
		}
	}

	/**
	 * 批量启动或关闭任务
	 * 
	 * @since changjiang @ 2018年6月20日
	 * @param taskIds
	 *            任务id集
	 * @param stats
	 *            任务状态
	 * @Return : void
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/batchOpenOrClose")
	public void openOrClose(@ApiParam("taskIds") @RequestParam(value = "taskIds") String[] taskIds,
			@ApiParam("stats") @RequestParam(value = "stats") boolean stats) {
		try {
			this.taskService.openOrClose(Arrays.asList(taskIds), stats);
		} catch (Exception e) {
			log.error("batch openOrClose tasks error, taskIds=" + taskIds + ",stats=" + stats + ",e.message"
					+ e.getMessage(), e);
		}
	}

	/**
	 * 修改任务
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @param taskId
	 *            任务id
	 * @param taskName
	 *            任务名称
	 * @param source
	 *            全类名
	 * @param cron
	 *            cron表达式
	 * @param remark
	 *            描述
	 * @return
	 * @Return : Object
	 */
	@PostMapping("/modify")
	@FormatResult
	public Object modify(@ApiParam("taskId") @RequestParam(value = "taskId") String taskId,
			@ApiParam("taskName") @RequestParam(value = "taskName", required = false) String taskName,
			@ApiParam("source") @RequestParam(value = "source", required = false) String source,
			@ApiParam("cron") @RequestParam(value = "cron", required = false) String cron,
			@ApiParam("remark") @RequestParam(value = "remark", required = false) String remark) {
		Task oldTask = this.taskService.findOne(taskId);
		boolean reRegist = false;
		// 参数校验
		if (StringUtils.isNotBlank(taskName)) {
			oldTask.setTaskName(taskName);
		}
		if (StringUtils.isNotBlank(source)) {
			if (oldTask.getSource().equals(source)) {
				reRegist = true;
			}
			oldTask.setSource(source);
		}
		if (StringUtils.isNotBlank(cron)) {
			oldTask.setCron(cron);
		}
		if (StringUtils.isNotBlank(remark)) {
			oldTask.setRemark(remark);
		}
		try {
			oldTask = this.taskService.update(oldTask, reRegist);
		} catch (Exception e) {
			log.error("task modify error : taskId=" + taskId + ",taskName=" + taskName + ",source=" + source + ",cron="
					+ cron + ",remark=" + remark + ",e.message=" + e.getMessage(), e);
		}
		return oldTask;
	}

	/**
	 * 根据taskId获取任务详情
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @param taskId
	 * @return
	 * @Return : Object
	 */
	@GetMapping("/detail")
	@FormatResult
	public Object detail(@ApiParam("taskId") @RequestParam(value = "taskId") String taskId) {
		Task task = null;
		try {
			task = this.taskService.findOne(taskId);
		} catch (Exception e) {
			log.error("task detail error : taskId=" + taskId + ",e.message=" + e.getMessage(), e);
		}
		return task;
	}

	/**
	 * 删除任务,默认Scheduler移除Job信息
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @param taskId
	 * @Return : void
	 */
	@GetMapping("/delete")
	public void delete(@ApiParam("taskId") @RequestParam(value = "taskId") String taskId) {
		try {
			this.taskService.delete(taskId);
		} catch (SchedulerException e) {
			log.error("task delete error : taskId=" + taskId + ",e.message=" + e.getMessage(), e);
		}
	}

	/**
	 * 根据条件分页检索任务,只对本机构可见
	 * 
	 * @since changjiang @ 2018年6月21日
	 * @param taskName
	 *            任务名称,模糊匹配
	 * @param createdTimeRange
	 *            任务注册时间范围
	 * @param stats
	 *            状态,开始OR关闭
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页展示条数
	 * @param orderBy
	 *            排序字段,默认注册时间
	 * @param sort
	 *            正逆序,默认false逆序
	 * @return
	 * @Return : Object
	 */
	@PostMapping("/search")
	@FormatResult
	public Object search(@ApiParam("taskName") @RequestParam(value = "taskName", required = false) String taskName,
			@ApiParam(value = "createdTimeRange", example = "[yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss]") @RequestParam(value = "createdTimeRange", required = false) String[] createdTimeRange,
			@ApiParam("stats") @RequestParam(value = "stats", required = false) boolean stats,
			@ApiParam("pageNo") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
			@ApiParam("pageSize") @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@ApiParam("orderBy") @RequestParam(value = "orderBy", required = false, defaultValue = "createdTime") String orderBy,
			@ApiParam("sort") @RequestParam(value = "sort", required = false, defaultValue = "false") boolean sort) {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		// 获取当前用户
		User user = UserUtils.getUser();
		Date begin = null;
		Date end = null;
		Page<Task> pageList = null;
		// 处理时间范围
		if (createdTimeRange != null && createdTimeRange.length == 2) {
			begin = DateUtil.stringToDate(createdTimeRange[0], DateUtil.yyyyMMdd);
			end = DateUtil.stringToDate(createdTimeRange[1], DateUtil.yyyyMMdd);
		}
		try {
			pageList = this.taskService.pageList(user.getOrganizationId(), taskName, stats, begin, end, pageNo,
					pageSize, orderBy, sort);
		} catch (Exception e) {
			log.error("task search error : e.message = " + e.getMessage(), e);
		}
		return pageList;
	}
}
