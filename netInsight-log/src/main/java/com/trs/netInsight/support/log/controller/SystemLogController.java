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

import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.log.entity.ItemPer;
import com.trs.netInsight.support.log.entity.RequestTimeLog;
import com.trs.netInsight.support.log.entity.SystemLog;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.entity.enums.DepositPattern;
import com.trs.netInsight.support.log.repository.MysqlSystemLogRepository;
import com.trs.netInsight.support.log.repository.RequestTimeLogRepository;
import com.trs.netInsight.support.log.repository.SystemLogExceptionRepository;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.handler.result.ResultCode;
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

	@Autowired
	MysqlSystemLogRepository mysqlSystemLogRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	OrganizationRepository organizationRepository;

	@Autowired
	RequestTimeLogRepository requestTimeLogRepository;
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
				systemLogOperation.getValue(),systemLogOperation.getOperator(), systemLogTabId, NetworkUtil.getIpAddress(request),  request.getRequestURI(), new Date(), new Date(), timeConsumed,
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
			@ApiParam("排序:0不排序 1今日登录次数,3、昨日登录次数 4、周登录次数") @RequestParam(value = "sortBy",defaultValue = "0",required = false) Integer sortBy,
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
			@ApiParam("检索条件") @RequestParam(value = "retrievalCondition", required = false) String retrievalCondition,
			@ApiParam("根据时间排序") @RequestParam(value = "createTimeOrder",required = false) String createTimeOrder,
			@ApiParam("操作状态") @RequestParam(value = "simpleStatus",required = false) String simpleStatus,
			@ApiParam("步长") @RequestParam(value = "pageNum") Integer pageNum,
			@ApiParam("页码") @RequestParam(value = "pageSize") Integer pageSize) throws Exception {

		AbstractSystemLog abstractSystemLog = SystemLogFactory.createSystemLog(DepositPattern.MYSQL);
		return abstractSystemLog.findCurOrgLogs(organizationId, timeLimited, userId, operation,
				systemLogOperation,createTimeOrder,simpleStatus, pageNum, pageSize,retrievalCondition);
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
			@ApiParam("检索条件 根据ip查询传 requestIp ") @RequestParam(value = "retrievalCondition", required = false) String retrievalCondition,
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

	@ApiOperation("使用菜单占比")
	@RequestMapping(value = "/getItemPercent", method = RequestMethod.GET)
	@FormatResult
	public Object getItemPercent(@ApiParam("当前用户id ") @RequestParam(value = "currId") String currId,
								 @ApiParam("机构id ") @RequestParam(value = "orgId",required = false) String orgId,
								 @ApiParam("查询某个账户的 用户id") @RequestParam(value = "selectedId",required = false) String selectedId,
								 @ApiParam("查询时间范围 1h 1d 7d 1m 3m ") @RequestParam(value = "time",required = false,defaultValue = "1d") String time){
		String beginTime = DateUtil.getBeginTime(time)[0];
		List<String> orgIds = new ArrayList<>();
		List<String> userIds = new ArrayList<>();
		Object[] obj = null;
		if (StringUtil.isEmpty(orgId) && StringUtil.isEmpty(selectedId)){
			orgIds = getOrgIds(currId);
		}else if (StringUtil.isNotEmpty(orgId)){
			orgIds.add(orgId);
		}else if (StringUtil.isNotEmpty(selectedId)){
			userIds.add(selectedId);
		}
		if (ObjectUtil.isNotEmpty(userIds)){
			obj = mysqlSystemLogRepository.itemPerByUserId(userIds,beginTime);
		}else if (ObjectUtil.isNotEmpty(orgIds)){
			obj =  mysqlSystemLogRepository.itemPer(orgIds,beginTime);
		}
		return obj;
	}

	@ApiOperation("常用栏目top10")
	@RequestMapping(value = "/getTopTen", method = RequestMethod.GET)
	@FormatResult
	public Object getTopTen(@ApiParam("当前用户id ") @RequestParam(value = "currId") String currId,
								 @ApiParam("机构id ") @RequestParam(value = "orgId",required = false) String orgId,
								 @ApiParam("查询某个账户的 用户id") @RequestParam(value = "selectedId",required = false) String selectedId,
								 @ApiParam("菜单 ") @RequestParam(value = "item",required = false) String item,
								 @ApiParam("查询时间范围 1h 1d 7d 1m 3m ") @RequestParam(value = "time",required = false,defaultValue = "7d") String time){
		String beginTime = DateUtil.getBeginTime(time)[0];
		List<String> orgIds = new ArrayList<>();
		List<String> userIds = new ArrayList<>();
		Object[] obj = null;
		if (StringUtil.isEmpty(orgId) && StringUtil.isEmpty(selectedId)){
			orgIds = getOrgIds(currId);
		}else if (StringUtil.isNotEmpty(orgId)){
			orgIds.add(orgId);
		}else if (StringUtil.isNotEmpty(selectedId)){
			userIds.add(selectedId);
		}
		if (ObjectUtil.isNotEmpty(userIds)){
			if (StringUtil.isNotEmpty(item)){
				obj = requestTimeLogRepository.itemPerByUserId(userIds,beginTime,item+"%");
			}else {
				obj = requestTimeLogRepository.itemPerByUserId(userIds,beginTime);
			}
		}else if (ObjectUtil.isNotEmpty(orgIds)){
			if (StringUtil.isNotEmpty(item)){
				obj =  requestTimeLogRepository.itemPer(orgIds,beginTime,item+"%");
			}else {
				obj =  requestTimeLogRepository.itemPer(orgIds,beginTime);
			}
		}
		return obj;
	}

	@ApiOperation("常用模块top10")
	@RequestMapping(value = "/getMoudleTopTen", method = RequestMethod.GET)
	@FormatResult
	public Object getMoudleTopTen(@ApiParam("当前用户id ") @RequestParam(value = "currId") String currId,
							@ApiParam("机构id ") @RequestParam(value = "orgId",required = false) String orgId,
							@ApiParam("查询某个账户的 用户id") @RequestParam(value = "selectedId",required = false) String selectedId,
							@ApiParam("菜单 ") @RequestParam(value = "item",required = false) String item,
							@ApiParam("查询时间范围 1h 1d 7d 1m 3m ") @RequestParam(value = "time",required = false,defaultValue = "7d") String time){
		String beginTime = DateUtil.getBeginTime(time)[0];
		List<String> orgIds = new ArrayList<>();
		List<String> userIds = new ArrayList<>();
		List<RequestTimeLog> obj = null;
		if (StringUtil.isEmpty(orgId) && StringUtil.isEmpty(selectedId)){
			orgIds = getOrgIds(currId);
		}else if (StringUtil.isNotEmpty(orgId)){
			orgIds.add(orgId);
		}else if (StringUtil.isNotEmpty(selectedId)){
			userIds.add(selectedId);
		}
		if (ObjectUtil.isNotEmpty(userIds)){
			obj = requestTimeLogRepository.topTenMoudleUserId(userIds,beginTime,item+"%");
		}else if (ObjectUtil.isNotEmpty(orgIds)){
			obj =  requestTimeLogRepository.topTenMoudle(orgIds,beginTime,item+"%");
		}
		HashMap<String,Integer> rtnMap = new HashMap<>();
		for (RequestTimeLog log:obj){
			String op = log.getOperation();
			if (StringUtil.isNotEmpty(op)){
				String[] split = op.split("-");
				if (split.length == 3){
					if (rtnMap.get(split[2]) != null){
						rtnMap.put(split[2],rtnMap.get(split[2])+1);
					}else {
						rtnMap.put(split[2],1);
					}
				}
			}
		}
		return rtnMap;
	}

	@ApiOperation("账号登录频率统计")
	@RequestMapping(value = "/getLoginFre", method = RequestMethod.GET)
	@FormatResult
	public Object getLoginFre(@ApiParam("当前用户id ") @RequestParam(value = "currId") String currId,
								  @ApiParam("机构id ") @RequestParam(value = "orgId",required = false) String orgId,
								  @ApiParam("查询某个账户的 用户id") @RequestParam(value = "selectedId",required = false) String selectedId,
								  @ApiParam("排序 最近登录时间 last_login_time 总登录次数 totalTimes 7天内登录次数 sevenDays ") @RequestParam(value = "orderBy",required = false,defaultValue = "last_login_time") String orderBy,
							  @ApiParam("倒序 desc 正序 asc ") @RequestParam(value = "sort",required = false,defaultValue = "desc") String sort){
		List<String> orgIds = new ArrayList<>();
		List<String> userIds = new ArrayList<>();
		List<User> obj = null;
		if (StringUtil.isEmpty(orgId) && StringUtil.isEmpty(selectedId)){
			orgIds = getOrgIds(currId);
		}else if (StringUtil.isNotEmpty(orgId)){
			orgIds.add(orgId);
		}else if (StringUtil.isNotEmpty(selectedId)){
			userIds.add(selectedId);
		}
		String order = "sevenDays".equals(orderBy)?"last_login_time":orderBy;
		if (ObjectUtil.isNotEmpty(userIds)){
			obj = userRepository.topTenMoudleUserId(userIds,order,sort);
		}else if (ObjectUtil.isNotEmpty(orgIds)){
			obj = userRepository.topTenMoudle(orgIds,order,sort);
		}
		for (User user:obj){
			user.setLoginCount(UserUtils.getWeekLoginCount(user.getUserName()+user.getId()));
		}

		if ("sevenDays".equals(orderBy)){
			Collections.sort(obj, new Comparator<User>() {
				@Override
				public int compare(User o1, User o2) {
					int sortValue = -1;
					if ("desc".equals(sort)) sortValue = 1;
					if (o1.getLoginCount() > o2.getLoginCount()) {
						return sortValue;
					} else if (o1.getLoginCount() < o2.getLoginCount()) {
						return -sortValue;
					}
					return 0;
				}

			});
		}
		return obj;
	}

    @ApiOperation("账号登录统计")
    @RequestMapping(value = "/getLoginElastic", method = RequestMethod.GET)
    @FormatResult
    public Object getLoginElastic(@ApiParam("当前用户id ") @RequestParam(value = "currId") String currId,
                              @ApiParam("机构id ") @RequestParam(value = "orgId",required = false) String orgId,
                              @ApiParam("查询某个账户的 用户id") @RequestParam(value = "selectedId",required = false) String selectedId,
                              @ApiParam("查询时间范围 1h 1d 7d 1m 3m ") @RequestParam(value = "time",required = false,defaultValue = "7d") String time){
        List<String> orgIds = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        String[] beginAndEnd = DateUtil.getBeginTime(time);
        String beginTime = beginAndEnd[0];
		List<String[]> obj = null;
        HashMap<String,Object> rtnMap = new HashMap<>();
        if (StringUtil.isEmpty(orgId) && StringUtil.isEmpty(selectedId)){
            orgIds = getOrgIds(currId);
        }else if (StringUtil.isNotEmpty(orgId)){
            orgIds.add(orgId);
        }else if (StringUtil.isNotEmpty(selectedId)){
            userIds.add(selectedId);
        }
        if (ObjectUtil.isNotEmpty(userIds)){
            obj = mysqlSystemLogRepository.timeStatic(userIds,beginTime);
        }else if (ObjectUtil.isNotEmpty(orgIds)){
            obj = mysqlSystemLogRepository.timeStaticOrg(orgIds,beginTime);
        }
        List<Organization> orgs = findOrgs(currId);
        List<String> betweenDateString = DateUtil.getBetweenDateString(beginAndEnd[0], beginAndEnd[1], DateUtil.yyyyMMdd3);
        for (Organization org:orgs){
        	String id = org.getId();
        	String name = org.getOrganizationName();
            Map<String,Integer> item = new LinkedHashMap<>();
            for (String st:betweenDateString){
                item.put(st,0);
            }
            for (Object[] itemSta:obj){
            	if (id.equals(itemSta[0].toString())){
            		item.put(itemSta[1].toString(),Integer.parseInt(itemSta[2].toString()));
				}
			}
            rtnMap.put(name,item);
        }
        return rtnMap;
    }

	@ApiOperation("使用习惯分析")
	@RequestMapping(value = "/getHourElastic", method = RequestMethod.GET)
	@FormatResult
	public Object getHourElastic(@ApiParam("当前用户id ") @RequestParam(value = "currId") String currId,
								  @ApiParam("机构id ") @RequestParam(value = "orgId",required = false) String orgId,
								  @ApiParam("查询某个账户的 用户id") @RequestParam(value = "selectedId",required = false) String selectedId,
								  @ApiParam("查询时间范围 1h 1d 7d 1m 3m ") @RequestParam(value = "time",required = false,defaultValue = "1d") String time){
		List<String> orgIds = new ArrayList<>();
		List<String> userIds = new ArrayList<>();
		String[] beginAndEnd = DateUtil.getBeginTime(time);
		String beginTime = beginAndEnd[0];
		List<String[]> obj = null;
		HashMap<String,Integer> rtnMap = new LinkedHashMap<>();
		if (StringUtil.isEmpty(orgId) && StringUtil.isEmpty(selectedId)){
			orgIds = getOrgIds(currId);
		}else if (StringUtil.isNotEmpty(orgId)){
			orgIds.add(orgId);
		}else if (StringUtil.isNotEmpty(selectedId)){
			userIds.add(selectedId);
		}
		if (ObjectUtil.isNotEmpty(userIds)){
			obj = mysqlSystemLogRepository.hourStatic(userIds,beginTime);
		}else if (ObjectUtil.isNotEmpty(orgIds)){
			obj = mysqlSystemLogRepository.hourStaticOrg(orgIds,beginTime);
		}
		List<String> betweenDateHourString = DateUtil.getBetweenDateHourString3(beginAndEnd[0].replace("-","").replace(" ","").replace(":",""), beginAndEnd[1].replace("-","").replace(" ","").replace(":",""));
		for (String str:betweenDateHourString){
			rtnMap.put(str,0);
		}
		for (Object[] item:obj){
			if (item[0] != null){
				rtnMap.put(item[0].toString(),Integer.parseInt(item[1].toString()));
			}
		}
		return rtnMap;
	}

	@ApiOperation("获取该账户下的机构")
	@RequestMapping(value = "/getOrgs", method = RequestMethod.GET)
	@FormatResult
	public Object getOrgs(@ApiParam("当前用户id ") @RequestParam(value = "currId") String currId
							  ){
		List<Organization> orgs = findOrgs(currId);
		return orgs;
	}
	private List<Organization> findOrgs(String currId){
		Specification<Organization> criteria = new Specification<Organization>() {
			@Override
			public Predicate toPredicate(Root<Organization> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();

				if (UserUtils.isRolePlatform()) {
//					User user = userService.findById(id);
					User user = userRepository.findOne(currId);
					Collection<Organization> orgs1 = user.getOrganizations();
					List<Organization> orgs = getOperationsOrgsPages(orgs1);
					CriteriaBuilder.In<String> in = cb.in(root.get("id").as(String.class));
					boolean hasV=false;
					for (Organization organization : orgs) {
						if(organization!=null){
							hasV = true;
							in.value(organization.getId());
						}
					}
					if(hasV) predicate.add(in);
				}else if (UserUtils.isRoleAdmin()) {
					User user = UserUtils.getUser();
					Organization organization = organizationRepository.findOne(user.getOrganizationId());
					Collection<Organization> orgs1 = new ArrayList<>();
					orgs1.add(organization);
					List<Organization> orgs = getOperationsOrgsPages( orgs1);
					CriteriaBuilder.In<String> in = cb.in(root.get("id").as(String.class));
					boolean hasV=false;
					for (Organization o : orgs) {
						if(o!=null){
							hasV = true;
							in.value(o.getId());
						}
					}
					if(hasV) predicate.add(in);
				}
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};
		List<Organization> orgs = organizationRepository.findAll(criteria);
		return orgs;
	}



	private List<String> getOrgIds(String currId){
		List<String> rtn = new ArrayList<>();
		Specification<Organization> criteria = new Specification<Organization>() {
			@Override
			public Predicate toPredicate(Root<Organization> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();

				if (UserUtils.isRolePlatform()) {
//					User user = userService.findById(id);
					User user = userRepository.findOne(currId);
					Collection<Organization> orgs1 = user.getOrganizations();
					List<Organization> orgs = getOperationsOrgsPages(orgs1);
					CriteriaBuilder.In<String> in = cb.in(root.get("id").as(String.class));
					boolean hasV=false;
					for (Organization organization : orgs) {
						if(organization!=null){
							hasV = true;
							in.value(organization.getId());
						}
					}
					if(hasV) predicate.add(in);
				}else if (UserUtils.isRoleAdmin()) {
					User user = UserUtils.getUser();
					Organization organization = organizationRepository.findOne(user.getOrganizationId());
					Collection<Organization> orgs1 = new ArrayList<>();
					orgs1.add(organization);
					List<Organization> orgs = getOperationsOrgsPages( orgs1);
					CriteriaBuilder.In<String> in = cb.in(root.get("id").as(String.class));
					boolean hasV=false;
					for (Organization o : orgs) {
						if(o!=null){
							hasV = true;
							in.value(o.getId());
						}
					}
					if(hasV) predicate.add(in);
				}
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};
		List<Organization> orgs = organizationRepository.findAll(criteria);
		for (Organization org:orgs) {
			rtn.add(org.getId());
		}
		return rtn;
	}
	private List<Organization> getOperationsOrgsPages(Collection<Organization> orgs) {
		List<String> ids = new ArrayList<>();
		if (orgs != null && orgs.size() > 0) {
			for (Organization organization : orgs) {
				if(organization!=null){
					ids.add(organization.getId());
				}
			}
		}
//		return organizationRepository.findByIdIn(ids,new PageRequest(pageNum, pageSize, new Sort(listSort)));
		return organizationRepository.findByIdIn(ids);
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