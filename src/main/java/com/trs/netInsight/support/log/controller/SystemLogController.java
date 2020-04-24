/*
 * Project: netInsight
 * 
 * File Created at 2018年7月27日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.log.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.trs.netInsight.support.log.entity.SystemLog;
import com.trs.netInsight.support.log.repository.SystemLogExceptionRepository;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.handler.result.ResultCode;
import com.trs.netInsight.support.log.entity.enums.DepositPattern;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.factory.AbstractSystemLog;
import com.trs.netInsight.support.log.factory.SystemLogFactory;
import com.trs.netInsight.util.NetworkUtil;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 日志类
 * 
 * @Type SystemLogController.java
 * @author 谷泽昊
 * @date 2018年7月27日 下午5:18:02
 * @version add
 */
@RestController
@RequestMapping("/systemLog")
public class SystemLogController {

	@Autowired
	SystemLogExceptionRepository systemLogExceptionRepository;

	/**
	 * 添加点击日志
	 * 
	 * @date Created at 2018年7月27日 下午5:27:00
	 * @Author 谷泽昊
	 * @param depositPattern
	 * @param systemLogType
	 * @param systemLogOperation
	 * @param request
	 */
	@ApiOperation("添加系统日志")
	@RequestMapping(value = "/addSystemLog", method = RequestMethod.GET)
	public void addSystemLog(@ApiParam("存入模式") @RequestParam(value = "depositPattern") DepositPattern depositPattern,
			@ApiParam("日志类型") @RequestParam(value = "systemLogType") SystemLogType systemLogType,
			@ApiParam("具体操作类") @RequestParam(value = "systemLogOperation") SystemLogOperation systemLogOperation,
			@ApiParam("栏目组id-专题id") @RequestParam(value = "systemLogTabId") String systemLogTabId,
			HttpServletRequest request) {
		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(depositPattern);
		long timeConsumed = new Date().getTime() - new Date().getTime();
		String simpleStatus = 500 == ResultCode.SUCCESS ? "失败" : "成功";
		SystemLog systemLog = new SystemLog(null, null, systemLogType.getValue(),
				systemLogOperation.getValue(), systemLogTabId, NetworkUtil.getIpAddress(request),  request.getRequestURI(), new Date(), new Date(), timeConsumed,
				ResultCode.SUCCESS, null, null, null, null, null, null,
				UserUtils.getUser().getUserName(), null,null,0);
		User user = UserUtils.getUser();
		abstractSystemLog.add(systemLog,user);
	}

	/**
	 * 查看当前运维人员下的所有机构；超管看所有
	 * 
	 * @date Created at 2018年11月6日 下午4:57:58
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @param id
	 * @param pageNum
	 * @param pageSize
	 * @param sortBy 1在线人数,2登录次数,3机构类型 desc试用在前 asc正式在前
	 *               organizationType 结构类型
	 * @return
	 * @throws Exception
	 * #lltodo
	 */
	@ApiOperation("查看当前运维人员下的所有机构；超管看所有")
	@RequestMapping(value = "/orgList", method = RequestMethod.GET)
	@FormatResult
	public Object orgList(
			@ApiParam("检索条件,userName或者organizationName") @RequestParam(value = "retrievalCondition", required = false) String retrievalCondition,
			@ApiParam("检索内容") @RequestParam(value = "retrievalInformation", required = false) String retrievalInformation,
			@ApiParam("id") @RequestParam(value = "id") String id,
			@ApiParam("页码") @RequestParam(value = "pageNum") Integer pageNum,
			@ApiParam("步长") @RequestParam(value = "pageSize") Integer pageSize,
			@ApiParam("机构类型") @RequestParam(value = "organizationType",required = false) String organizationType,
			@ApiParam("对当前在线进行筛选，全部：0、仅在线：1  ") @RequestParam(value = "onLine",required = false,defaultValue = "0") Integer onLine,
			@ApiParam("排序:0不排序 1登录次数") @RequestParam(value = "sortBy",defaultValue = "0",required = false) Integer sortBy,
			@ApiParam("升序降序") @RequestParam(value = "ascDesc",required = false,defaultValue = "desc") String ascDesc) throws Exception {
		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(DepositPattern.MYSQL);
		return abstractSystemLog.findorgList(retrievalCondition, retrievalInformation, id, pageNum, pageSize,onLine,sortBy,ascDesc,organizationType);
	}

	/***
	 * 当前机构下所有用户的操作日志
	 * 
	 * @param organizationId
	 *            机构id
	 * @param timeLimited
	 *            时限
	 * @param userId
	 *            用户id
	 * @param operation
	 *            操作名称
	 * @param systemLogOperation 操作明细(添加,修改,查询,删除) lltodo
	 *
	 *                           createTimeOrder createTimeDesc createTimeAsc
	 *                           simpleStatus 成功 失败
	 * @return
	 * @throws Exception
	 */
	@ApiOperation("当前机构下所有用户的操作日志")
	@RequestMapping(value = "/curOrgLogs", method = RequestMethod.GET)
	@FormatResult
	public Object curOrgLogs(@ApiParam("机构id") @RequestParam(value = "organizationId") String organizationId,
			@ApiParam("时间") @RequestParam(value = "timeLimited") String timeLimited,
			@ApiParam("用户id") @RequestParam(value = "userId") String userId,
			@ApiParam("操作") @RequestParam(value = "operation") String operation,
			@ApiParam("操作明细(增删改查)") @RequestParam(value = "operationDetail",required = false) String systemLogOperation,
			@ApiParam("根据时间排序") @RequestParam(value = "createTimeOrder",required = false) String createTimeOrder,
			@ApiParam("操作状态") @RequestParam(value = "simpleStatus",required = false) String simpleStatus,
			@ApiParam("步长") @RequestParam(value = "pageNum") Integer pageNum,
			@ApiParam("页码") @RequestParam(value = "pageSize") Integer pageSize) throws Exception {

		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(DepositPattern.MYSQL);
		return abstractSystemLog.findCurOrgLogs(organizationId, timeLimited, userId, operation,
				systemLogOperation,createTimeOrder,simpleStatus, pageNum, pageSize);
	}

	/***
	 * 当前机构的所有用户
	 * 
	 * @param organizationId
	 *            机构id  查询速度慢的原因
	 * @return
	 */
	@ApiOperation("当前机构的所有用户")
	@RequestMapping(value = "/curOrgUsers", method = RequestMethod.GET)
	@FormatResult
	public Object curOrgUsers(@ApiParam("机构id") @RequestParam(value = "organizationId") String organizationId) {
		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(DepositPattern.MYSQL);
		return abstractSystemLog.curOrgUsers(organizationId);
	}

	/***
	 * 所有的操作名称 or 操作路径
	 * 
	 * @return
	 */
	@ApiOperation("所有的操作名称 or 操作路径")
	@RequestMapping(value = "/operations", method = RequestMethod.GET)
	@FormatResult
	public Object operations() {
		return Arrays.stream(SystemLogType.values()).map(e -> e.getValue()).collect(Collectors.toList());
	}

	/***
	 * 查看当前日志详细
	 * 
	 * @param id
	 *            system_log 表的id
	 * @return
	 */
	/*
	 * @RequestMapping(value = "/curLog", method = RequestMethod.GET) public
	 * Object curLog(String id){ //前端同志说这个接口不用写啦 return null; }
	 */

	//
	/**
	 * 日志统计分析
	 * 
	 * @date Created at 2018年11月2日 下午3:36:44
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param id
	 * @param timeLimited
	 * @param operation
	 * @param timeConsumed
	 *            空 or 10-20 or 0-20 or 20-0
	 * @param retrievalCondition
	 *            -> createdUserName、systemName、organizationName
	 * @param retrievalInformation
	 * @param createTimeOrder asc/desc
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ApiOperation("日志统计分析")
	@RequestMapping(value = "/logTimeStatistics", method = RequestMethod.GET)
	@FormatResult
	public Object logTimeStatistics(@ApiParam("用户id") @RequestParam(value = "id") String id,
			@ApiParam("时间") @RequestParam(value = "timeLimited") String timeLimited,
			@ApiParam("操作") @RequestParam(value = "operation") String operation,
			@ApiParam("timeConsumed") @RequestParam(value = "timeConsumed") String timeConsumed,
			@ApiParam("检索条件") @RequestParam(value = "retrievalCondition", required = false) String retrievalCondition,
			@ApiParam("检索内容") @RequestParam(value = "retrievalInformation", required = false) String retrievalInformation,
			@ApiParam("根据时间排序") @RequestParam(value = "createTimeOrder",required = false) String createTimeOrder,
			@ApiParam("页码") @RequestParam(value = "pageNum", required = false, defaultValue = "0") Integer pageNum,
			@ApiParam("步长") @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(DepositPattern.MYSQL);
		return abstractSystemLog.logTimeStatistics(id, timeLimited, operation, timeConsumed, retrievalCondition,
				retrievalInformation,createTimeOrder, pageNum, pageSize);
	}

	/**
	 * 查看详细日志
	 * 
	 * @date Created at 2018年11月5日 上午10:20:36
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param id
	 * @return
	 */
	@ApiOperation("查看详细日志")
	@RequestMapping(value = "/curLog", method = RequestMethod.GET)
	@FormatResult
	public Object curLog(@ApiParam("id") @RequestParam(value = "id") String id) {
		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(DepositPattern.MYSQL);
		return abstractSystemLog.findById(id);
	}

	/**
	 * 浏览器使用占比
	 * 
	 * @date Created at 2018年11月5日 上午10:20:22
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @return
	 */
	@ApiOperation("浏览器使用占比")
	@RequestMapping(value = "/browserUsagePer", method = RequestMethod.GET)
	@FormatResult
	public Object browserUsagePer() {
		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(DepositPattern.MYSQL);
		return abstractSystemLog.browserUsagePer();
	}

	/**
	 * ip占比
	 * @return
	 */
	@ApiOperation("ip使用占比")
	@RequestMapping(value = "/ipPer", method = RequestMethod.GET)
	@FormatResult
	public Object ipPer(){
		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(DepositPattern.MYSQL);
		return abstractSystemLog.ipPer();
	}

	/**
	 * 失败后查看错误信息
	 * @param systemLogId
	 * @return
	 */
	@ApiOperation("失败后查看错误信息")
	@RequestMapping(value = "/getExceptionByLogId", method = RequestMethod.GET)
	@FormatResult
	public Object getExceptionByLogId(@ApiParam("systemLogId") @RequestParam(value = "systemLogId") String systemLogId){
		return systemLogExceptionRepository.findBySystemLogId(systemLogId);
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年7月27日 谷泽昊 creat
 */