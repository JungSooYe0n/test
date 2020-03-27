/*
 * Project: netInsight
 * 
 * File Created at 2018年7月25日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.log.factory.systemlog;

import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.log.entity.SystemLog;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.factory.AbstractSystemLog;
import com.trs.netInsight.util.IPUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 日志存入 mysql 实现类
 * 
 * @Type MysqlSystemLog.java
 * @author 谷泽昊
 * @date 2018年7月25日 下午4:26:45
 * @version
 */
@Slf4j
public class MysqlSystemLog extends AbstractSystemLog {

	@Override
	public SystemLog add(String requestParams, String methodDescription, SystemLogType systemLogType,
			SystemLogOperation systemLogOperation, String systemLogTabId, String requestIp, String requestUri,
			Date startTime, Date endTime, int status, String exceptionDetail, String osInfo, String browserInfo,
			String sessionId, String operationPosition, String operationUserName, String trsl,Integer num) {
		String simpleStatus = 500 == status ? "失败" : "成功";
		long timeConsumed = endTime.getTime() - startTime.getTime();
		SystemLog systemLog = new SystemLog(requestParams, methodDescription, systemLogType.getValue(),
				systemLogOperation.getValue(), systemLogTabId, requestIp, requestUri, startTime, endTime, timeConsumed,
				status, simpleStatus, exceptionDetail, osInfo, browserInfo, sessionId, operationPosition,
				operationUserName == null ? operationUserName = UserUtils.getUser().getUserName():operationUserName, trsl,num,0);
		return mysqlSystemLogRepository.save(systemLog);
	}

	@Override
	public Map<String, Object> findorgList(String retrievalCondition, String retrievalInformation, String id,
			Integer pageNum, Integer pageSize,Integer sortBy,String ascDesc,String organizationType) throws Exception {
		//多个字段排序
		List<Sort.Order> listSort = new ArrayList();
		Sort.Order orderCreateTime = new Sort.Order(Sort.Direction.DESC, "createdTime");
		listSort.add(orderCreateTime);
		PageRequest pageable = new PageRequest(pageNum, pageSize, new Sort(listSort));
		//定义查询条件
		Specification<Organization> criteria = new Specification<Organization>() {
			@Override
			public Predicate toPredicate(Root<Organization> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "organizationName")) {
					predicate.add(cb.like(root.get("organizationName"), "%" + retrievalInformation + "%"));
				}else if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "userName")) {
					Set<String> orgIds = userService.findOrgIdByUserNameAndOrgIds(retrievalInformation, null);
					In<String> in = cb.in(root.get("id").as(String.class));
					boolean hasV=false;
					for (String str : orgIds) {
						in.value(str);
						hasV = true;
					}
					if(hasV) predicate.add(in);
				}
				if (UserUtils.isRolePlatform()) {
					User user = userService.findById(id);
					Collection<Organization> orgs = user.getOrganizations();
					Page<Organization> orgsPages = getOperationsOrgsPages(retrievalCondition, retrievalInformation, orgs, pageNum, pageSize,listSort);
					orgs = orgsPages.getContent();
					In<String> in = cb.in(root.get("id").as(String.class));
					boolean hasV=false;
					for (Organization organization : orgs) {
						if(organization!=null){
							hasV = true;
							in.value(organization.getId());
						}
					}
					if(hasV) predicate.add(in);
				}else if (UserUtils.isRoleAdmin()) {
					User user =UserUtils.getUser();
					Organization organization = organizationService.findById(user.getOrganizationId());
					Collection<Organization> orgs = new ArrayList<>();
					orgs.add(organization);
					Page<Organization> orgsPages = getOperationsOrgsPages(retrievalCondition, retrievalInformation, orgs, pageNum, pageSize,listSort);
					orgs = orgsPages.getContent();
					In<String> in = cb.in(root.get("id").as(String.class));
					boolean hasV=false;
					for (Organization o : orgs) {
						if(o!=null){
							hasV = true;
							in.value(o.getId());
						}
					}
					if(hasV) predicate.add(in);
				}
				if (StringUtils.isNotBlank(organizationType)) {
					predicate.add(cb.equal(root.get("organizationType"), organizationType));
				}
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};

		Page<Organization> organizations;
		Collection<Organization> orgs;
		//排序走此方法 1在线人数,2登录次数
		if(sortBy==1 || sortBy==2){
			pageable = new PageRequest(0, 2000, new Sort(listSort));
			organizations = organizationService.findByCriteria(criteria, pageable);
			orgs = organizations.getContent();
			return sortOrgByNumber(orgs,pageNum,pageSize,sortBy,ascDesc,listSort);
		}else{
			organizations = organizationService.findByCriteria(criteria, pageable);
			orgs = organizations.getContent();
		}

		ArrayList<HashMap<String, Object>> resultDataList = new ArrayList<>();
		AtomicInteger i = new AtomicInteger(0);
		Collection<Session> activeSessions = sessionDAO.getActiveSessions();
		for (Organization e : orgs) {
			i.getAndIncrement();// 以原子方式+1
			HashMap<String, Object> each = new HashMap<>();
			each.put("order", i.intValue());
			each.put("organizationName", e.getOrganizationName());
			//each.put("systemName", e.getSystemName());
			each.put("organizationType", "formal".equals(e.getOrganizationType()) ? "正式" : "试用");
			each.put("onlineUserCount", getOnlineUserCount(activeSessions, e));
			each.put("loginCount", getLoginCount(e));
			each.put("organizationId", e.getId());
			resultDataList.add(each);
		}

		Map<String, Object> result = new HashMap<>();
		if (resultDataList == null || resultDataList.size() <= 0) {
			return null;
		}
		result.put("number", pageNum);
		result.put("size", pageSize);
		result.put("totalElements", organizations.getTotalElements());
		result.put("totalPages", organizations.getTotalPages());
		result.put("content", resultDataList);
		return result;

	}

	public Map<String, Object> sortOrgByNumber(Collection<Organization> orgs,
								Integer pageNum, Integer pageSize,Integer sortBy,String ascDesc,List<Sort.Order> listSort) throws Exception{
		log.info("---------->走排序方法:sortOrgByNumber");

		ArrayList<HashMap<String, Object>> resultDataList = new ArrayList<>();
		AtomicInteger iii = new AtomicInteger(0);
		Collection<Session> activeSessions = sessionDAO.getActiveSessions();
		List<Organization> orgList = new ArrayList<>();
		for (Organization e : orgs) {
			e.setLoginCount(getLoginCount(e));
			e.setOnlineUserCount(getOnlineUserCount(activeSessions, e));
			e.setAscDesc(ascDesc);
			e.setSortByCount(sortBy);
			orgList.add(e);
		}
		//数据排序
		Collections.sort(orgList);
		//计算页数
		int totalPages;
		int totalElements = orgList.size();
		if(totalElements%pageSize==0) totalPages = totalElements%pageSize;
		else totalPages = totalElements%pageSize + 1;
		//循环从list中取想要的数据
		int start = (pageSize-1)*pageNum;
		int limit = (start+pageSize)<totalElements?(start+pageSize):totalElements;
		for(int j=start;j<limit;j++){
			Organization e = orgList.get(j);
			iii.getAndIncrement();// 以原子方式+1
			HashMap<String, Object> each = new HashMap<>();
			each.put("order", iii.intValue());
			each.put("organizationName", e.getOrganizationName());
			//each.put("systemName", e.getSystemName());
			each.put("organizationType", "formal".equals(e.getOrganizationType()) ? "正式" : "试用");
			each.put("onlineUserCount", e.getOnlineUserCount());
			each.put("loginCount", e.getLoginCount());
			each.put("organizationId", e.getId());
			resultDataList.add(each);
		}

		Map<String, Object> result = new HashMap<>();
		if (resultDataList == null || resultDataList.size() <= 0) {
			return null;
		}
		result.put("number", pageNum);
		result.put("size", pageSize);
		result.put("totalElements", totalElements);
		result.put("totalPages", totalPages);
		result.put("content", resultDataList);
		return result;

	}

	/**
	 * 获取超管所有机构
	 * 
	 * @date Created at 2018年11月6日 下午4:59:32
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	private Page<Organization> getSuperAdminOrgsPages(String retrievalCondition, String retrievalInformation,
			Integer pageNum, Integer pageSize,List<Sort.Order> listSort) {
		Page<Organization> pages;

		PageRequest pageable = new PageRequest(pageNum, pageSize, new Sort(listSort));

		if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "organizationName")) {
			return organizationService.findByOrganizationNameLike("%" + retrievalInformation + "%",
					new PageRequest(pageNum, pageSize, new Sort(listSort)));
		} else if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "userName")) {
			Set<String> orgIds = userService.findOrgIdByUserNameAndOrgIds(retrievalInformation, null);
			return organizationService.findByIdIn(orgIds,
					new PageRequest(pageNum, pageSize, new Sort(listSort)));
		} else {
			pages = organizationService.pageList(pageNum, pageSize, null,listSort);
			return pages;
		}
	}

	/**
	 * 获取运维所有项目
	 * 
	 * @date Created at 2018年11月6日 下午4:59:42
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @param orgs
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	private Page<Organization> getOperationsOrgsPages(String retrievalCondition, String retrievalInformation,
			Collection<Organization> orgs, Integer pageNum, Integer pageSize,List<Sort.Order> listSort) {
		List<String> ids = new ArrayList<>();
		if (orgs != null && orgs.size() > 0) {
			for (Organization organization : orgs) {
				if(organization!=null){
					ids.add(organization.getId());
				}
			}
		}
		if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "organizationName")) {
			return organizationService.findByOrganizationNameLikeAndIdIn("%" + retrievalInformation + "%", ids,
					new PageRequest(pageNum, pageSize, new Sort(listSort)));
		} else if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "userName")) {
			Set<String> orgIds = userService.findOrgIdByUserNameAndOrgIds(retrievalInformation, ids);
			return organizationService.findByIdIn(orgIds,
					new PageRequest(pageNum, pageSize, new Sort(listSort)));
		} else {
			return organizationService.findByIdIn(ids,
					new PageRequest(pageNum, pageSize, new Sort(listSort)));
		}
	}

	@Override
	public Page<SystemLog> findCurOrgLogs(String organizationId, String timeLimited, String userId,
										  String operation,String operationDetail,String createTimeOrder,
										  String simpleStatus,Integer pageNum, Integer pageSize) {
		return getSysLogList(organizationId, timeLimited, userId, operation,operationDetail,
				createTimeOrder,simpleStatus, pageNum, pageSize);
	}

	@Override
	public List<Map<String, String>> curOrgUsers(String organizationId) {

		List<User> users = userService.findByOrganizationId(organizationId);
		List<Map<String, String>> result = new ArrayList<>();
		for (User user : users) {
			Map<String, String> each = new HashMap<>();
			each.put("id", user.getId());
			each.put("displayName", user.getDisplayName());
			each.put("username", user.getUserName());
			result.add(each);
		}
		return result;
	}

	@Override
	public Map<String, Object> logTimeStatistics(String id, String timeLimited, String operation, String timeConsumed,
			String retrievalCondition, String retrievalInformation,String createTimeOrder, Integer pageNum, Integer pageSize) {
		// 查system_log
		List<String> ids = new ArrayList<>();
		// 查systemlog表关联查organization表不太好实现，但又都需要in orgIds，所以有关org表的相关操作都挪到这里来
		if (UserUtils.isSuperAdmin()) {
			List<Organization> all = organizationService.findAll();
			for (Organization organization : all) {
				if(organization!=null){
					ids.add(organization.getId());
				}
			}
			if ("organizationName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
				Collection<Organization> orgs = organizationService
						.findByOrganizationNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
				ids = orgs.stream().map(e -> e.getId()).collect(Collectors.toList());
			}
			/*else if ("systemName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
				Collection<Organization> orgs = organizationService
						.findBySystemNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
				ids = orgs.stream().map(e -> e.getId()).collect(Collectors.toList());
			}*/
		} else if (UserUtils.isRolePlatform()) {
			// 当前运维人员
			User user = userService.findById(id);
			Set<Organization> organizations = user.getOrganizations();
			for (Organization organization : organizations) {
				if(organization!=null){
					ids.add(organization.getId());
				}
			}
			if ("organizationName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
				Collection<Organization> orgs = organizationService
						.findByOrganizationNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
				ids = orgs.stream().map(e -> e.getId()).collect(Collectors.toList());
			}
			/*else if ("systemName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
				Collection<Organization> orgs = organizationService
						.findBySystemNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
				ids = orgs.stream().map(e -> e.getId()).collect(Collectors.toList());
			}*/
		} else {
			// 机构管理员 UserUtils.is...
			User user = UserUtils.getUser();
			Organization one = organizationService.findById(user.getOrganizationId());
			if(one!=null){
				ids = Arrays.asList(one.getId());
			}
			if ("organizationName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
				Collection<Organization> orgs = organizationService
						.findByOrganizationNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
				ids = orgs.stream().map(e -> e.getId()).collect(Collectors.toList());
			}
			/*else if ("systemName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
				Collection<Organization> orgs = organizationService
						.findBySystemNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
				ids = orgs.stream().map(e -> e.getId()).collect(Collectors.toList());
			}*/
		}
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		List<String> finalIds = ids;
		// 原生 查询sql
		Specification<SystemLog> criteria = new Specification<SystemLog>() {
			@Override
			public Predicate toPredicate(Root<SystemLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				ArrayList<Predicate> allPredicates = new ArrayList<>();
				// 耗时的相关条件
				if (StringUtil.isNotEmpty(timeConsumed)) {
					String[] split = timeConsumed.split("-");
					if (StringUtils.equals("0-0", timeConsumed)) {
						Predicate timeConsumedPredicate = cb.greaterThan(root.get("timeConsumed").as(Integer.class),
								new Double(0).intValue());
						allPredicates.add(timeConsumedPredicate);
					} else if (split[0].equals("0")) {
						Predicate timeConsumedPredicate = cb.lessThan(root.get("timeConsumed").as(Integer.class),
								new Double(Double.valueOf(split[1]) * 1000).intValue());
						allPredicates.add(timeConsumedPredicate);
					} else if (split[1].equals("0")) {
						Predicate timeConsumedPredicate = cb.greaterThan(root.get("timeConsumed").as(Integer.class),
								new Double(Double.valueOf(split[0]) * 1000).intValue());
						allPredicates.add(timeConsumedPredicate);
					} else {
						Predicate timeConsumedPredicateStart = cb.greaterThan(
								root.get("timeConsumed").as(Integer.class),
								new Double(Double.valueOf(split[0]) * 1000).intValue());
						Predicate timeConsumedPredicateEnd = cb.lessThan(root.get("timeConsumed").as(Integer.class),
								new Double(Double.valueOf(split[1]) * 1000).intValue());
						allPredicates.add(timeConsumedPredicateStart);
						allPredicates.add(timeConsumedPredicateEnd);
					}
				}
				// 操作路径的相关条件
				if (StringUtil.isNotEmpty(operation)) {
					Predicate operationPredicate = cb.equal(root.get("systemLogType").as(String.class), operation);
					allPredicates.add(operationPredicate);

				}
				// 时间的相关条件
				if (StringUtil.isNotEmpty(timeLimited)) {
					String[] times = timeLimited.split(";");
					if (times.length > 1) {
						Date[] timeToDate = timeToDate(times);
						Predicate timeLimitedPredicate = cb.between(root.get("createdTime"), timeToDate[0],
								DateUtil.getTimeByMinute(timeToDate[1], 1));
						allPredicates.add(timeLimitedPredicate);
					} else {
						Predicate timeLimitedPredicate = cb.greaterThan(root.get("createdTime").as(Date.class),
								getGreaterThanNodeTime(timeLimited));
						allPredicates.add(timeLimitedPredicate);
					}
				}
				// 运维人员只看其负责的机构
				if (!CollectionUtils.isEmpty(finalIds)) {
					CriteriaBuilder.In<String> organizationIdIns = cb.in(root.get("organizationId").as(String.class));
					for (String eachId : finalIds) {
						organizationIdIns.value(eachId);
					}
					allPredicates.add(organizationIdIns);
				}
				// 账号条件。系统名称条件和机构名称条件已经在查询机构表的时候做过处理了
				if (retrievalCondition != null && "createdUserName".equals(retrievalCondition.trim())
						&& StringUtils.isNotBlank(retrievalInformation)) {
					Predicate predicateLike = cb.like(root.get(retrievalCondition.trim()),
							"%" + retrievalInformation + "%");
					allPredicates.add(predicateLike);
				}
				Predicate[] pre = new Predicate[allPredicates.size()];
				return query.where(allPredicates.toArray(pre)).getRestriction();

			}
		};
		Sort sort;
		if (StringUtil.isNotEmpty(createTimeOrder) && createTimeOrder.toLowerCase().contains("asc")) {
			sort = new Sort(Sort.Direction.ASC, "createdTime");
		}else sort = new Sort(Sort.Direction.DESC, "createdTime");

		Page<SystemLog> sysLogs = mysqlSystemLogRepository.findAll(criteria,
				new PageRequest(pageNum, pageSize, sort));

		return logTimeStatisticsResultHandle(sysLogs);
	}

	/***
	 * 给每一条记录加上机构名和系统名
	 * 
	 * @param sysLogs
	 * @return
	 */
	private Map<String, Object> logTimeStatisticsResultHandle(Page<SystemLog> sysLogs) {
		ArrayList<HashMap<String, Object>> resultDataList = new ArrayList<>();
		sysLogs.getContent().stream().forEach(e -> {
			HashMap<String, Object> each = new HashMap<>();
			Organization org = organizationService.findById(e.getOrganizationId());
			each.put("organizationName", org.getOrganizationName());
			//each.put("systemName", org.getSystemName());
			each.put("createdUserName", e.getCreatedUserName());
			each.put("createdTime", e.getCreatedTime());
			each.put("timeConsumed", e.getTimeConsumed());
			each.put("systemLogType", e.getSystemLogType());
			each.put("id", e.getId());
			resultDataList.add(each);
		});
		Map<String, Object> result = new HashMap<>();
		result.put("number", sysLogs.getNumber());
		result.put("size", sysLogs.getSize());
		result.put("totalElements", sysLogs.getTotalElements());
		result.put("totalPages", sysLogs.getTotalPages());
		result.put("content", resultDataList);
		if (resultDataList == null || resultDataList.size() <= 0) {
			return null;
		}
		return result;
	}

	@Override
	public SystemLog findById(String id) {
		return mysqlSystemLogRepository.findOne(id);
	}

	@Override
	public ArrayList<Map<String, Object>> browserUsagePer() {
		List<String> list = getOrganIds();
		Collection<SystemLog> logs = mysqlSystemLogRepository.findAllByBrowserInfoIsNotNullAndOrganizationIdIn(list);
		List<String> allBrowsers = Arrays.asList("Edge", "MSIE", "Safari", "Opera", "Chrome", "Netscape", "Firefox",
				"IE");
		ArrayList<Map<String, Object>> result = new ArrayList<>();
		for (String eachBrowser : allBrowsers) {
			Map<String, Object> each = new HashMap<>();
			each.put("name", eachBrowser);
			long count = logs.stream()
					.filter(e -> StringUtils.isNotEmpty(e.getBrowserInfo()) && e.getBrowserInfo().contains(eachBrowser))
					.count();
			each.put("num", count);
			if (count != 0) {
				result.add(each);
			}
		}
		return result;
	}
	private final String ipPre = "ipPre_";
	@Override
	public List<Map<String, Object>> ipPer(){
		String ids = getOrganIdsStr();
		List<Object[]> logs = mysqlSystemLogRepository.findRequestIP(ids);
		System.out.println(logs.get(0)[0]+"----->"+logs.get(0)[1]);
		List<Map<String, Object>> listmap = new ArrayList<>();
        int otherNum = 0;
        //前9显示,以后显示 其他
		for(int i=0;i<logs.size();i++){
			Integer num = Integer.parseInt(logs.get(i)[1].toString());
			String ip = (String) logs.get(i)[0];
		    if(num==0) break;
            if(i<10){
            	String ipCity = "未知";
            	//去redis缓存查找
            	if(RedisUtil.getString(ipPre+ip)==null){
					ipCity = IPUtil.getArea(ip);
				}
				RedisUtil.setString(ipPre+ip,ipCity,7,TimeUnit.DAYS);
				ip = ipCity+":"+ip;
                Map<String, Object> infoMap = new HashMap<>();
                infoMap.put(ip,num);
                listmap.add(infoMap);
            }else{
                otherNum += num;
            }
		}
		if(otherNum>0){
            Map<String, Object> otherMap = new HashMap<>();
            otherMap.put("其他",otherNum);
            listmap.add(otherMap);
        }
		return listmap;
	}
	/**
	 * 获得当前用户机构ids
	 * @return
	 */
	public List<String> getOrganIds(){
		List<String> ids = new ArrayList<>();
		if (UserUtils.isSuperAdmin()) {
			List<Organization> all = organizationService.findAll();
			for (Organization organization : all) {
				if (organization != null) {
					ids.add(organization.getId());
				}
			}
		}else {
			// 机构管理员 UserUtils.is...
			User user = UserUtils.getUser();
			Organization one = organizationService.findById(user.getOrganizationId());
			if(one!=null){
				ids = Arrays.asList(one.getId());
			}
		}
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		return ids;
	}

	public String getOrganIdsStr(){
        List<String> list = getOrganIds();
        StringBuffer sb = new StringBuffer();
        for(String s: list){
            sb.append(s);
            sb.append(",");
        }
        return sb.toString().substring(0,sb.toString().length()-1);
    }

	/**
	 * 获取系统日志
	 * 
	 * @date Created at 2018年11月6日 下午5:00:13
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param organizationId
	 * @param timeLimited
	 * @param userId
	 * @param operation
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	private Page<SystemLog> getSysLogList(String organizationId, String timeLimited, String userId,
			String operation,String operationDetail,
			String createTimeOrder,String simpleStatus,
			Integer pageNum, Integer pageSize) {

		// 原生 查询sql
		Specification<SystemLog> criteria = new Specification<SystemLog>() {
			@Override
			public Predicate toPredicate(Root<SystemLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> allPredicates = new ArrayList<>();
				Predicate organizationIdPredicate = cb.equal(root.get("organizationId").as(String.class),
						organizationId);
				allPredicates.add(organizationIdPredicate);
				if (StringUtil.isNotEmpty(userId)) {
					Predicate userIdPredicate = cb.equal(root.get("createdUserId").as(String.class), userId);
					allPredicates.add(userIdPredicate);
				}
				if (StringUtil.isNotEmpty(operationDetail)) {
					Predicate userIdPredicate = cb.like(root.get("systemLogOperation").as(String.class), "%"+operationDetail+"%");
					allPredicates.add(userIdPredicate);
				}
				if (StringUtil.isNotEmpty(simpleStatus)) {
					Predicate userIdPredicate = cb.equal(root.get("simpleStatus").as(String.class), simpleStatus);
					allPredicates.add(userIdPredicate);
				}
				if (StringUtil.isNotEmpty(operation)) {
					Predicate operationPredicate = cb.equal(root.get("systemLogType").as(String.class), operation);
					allPredicates.add(operationPredicate);

				}
				if (StringUtil.isNotEmpty(timeLimited)) {
					String[] times = timeLimited.split(";");
					if (times.length > 1) {
						Date[] timeToDate = timeToDate(times);
						Predicate timeLimitedPredicate = cb.between(root.get("createdTime").as(Date.class),
								timeToDate[0], timeToDate[1]);
						allPredicates.add(timeLimitedPredicate);
					} else {

						Predicate timeLimitedPredicate = cb.greaterThan(root.get("createdTime").as(Date.class),
								getGreaterThanNodeTime(timeLimited));
						allPredicates.add(timeLimitedPredicate);
					}
					//
				}

				return cb.and(allPredicates.toArray(new Predicate[allPredicates.size()]));
			}
		};
		Sort sort;
		if(createTimeOrder != null && createTimeOrder.toLowerCase().contains("asc")) sort = new Sort(Sort.Direction.ASC, "createdTime");
		else sort = new Sort(Sort.Direction.DESC, "createdTime");

		PageRequest pageRequest = new PageRequest(pageNum, pageSize, sort);
		Page<SystemLog> sysLogs = mysqlSystemLogRepository.findAll(criteria,pageRequest);

		if (sysLogs == null || sysLogs.getContent() == null || sysLogs.getContent().size() <= 0) {
			return null;
		}
		return sysLogs;
	}

	@SuppressWarnings("unused")
	@Deprecated
	private Object sysLogResultHandle(Page<SystemLog> sysLogs, long sysLogCount) {
		HashMap<String, Object> result = new HashMap<>();
		result.put("sysLogCount", sysLogCount);
		result.put("sysLogs", sysLogs);
		return result;
	}

	@SuppressWarnings("unused")
	@Deprecated
	private long getSysLogCount(String organizationId, String timeLimited, String userId, String operation) {
		return mysqlSystemLogRepository.count((Root<SystemLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
			ArrayList<Predicate> allPredicates = new ArrayList<>();
			Predicate organizationIdPredicate = cb.equal(root.get("organizationId").as(String.class), organizationId);
			allPredicates.add(organizationIdPredicate);
			if (StringUtil.isNotEmpty(userId)) {
				Predicate userIdPredicate = cb.equal(root.get("createdUserId").as(String.class), userId);
				allPredicates.add(userIdPredicate);
			}
			if (StringUtil.isNotEmpty(operation)) {
				Predicate operationPredicate = cb.equal(root.get("systemLogType").as(String.class), operation);
				allPredicates.add(operationPredicate);

			}
			if (StringUtil.isNotEmpty(timeLimited)) {
				Predicate timeLimitedPredicate = cb.greaterThan(root.get("createdTime").as(Date.class),
						getGreaterThanNodeTime(timeLimited));
				allPredicates.add(timeLimitedPredicate);
			}

			return cb.and(allPredicates.toArray(new Predicate[allPredicates.size()]));
		});
	}

	/***
	 * 转换时间
	 * 
	 * @param timeNode
	 *            1h 近1小时 or 15min 近15分钟
	 * @return
	 */
	private Date getGreaterThanNodeTime(String timeNode) {
		Calendar calendar = Calendar.getInstance();
		if (timeNode.contains("h")) {
			calendar.add(Calendar.HOUR_OF_DAY, new Integer("-" + timeNode.replaceAll("h", "")));
		} else {
			calendar.add(Calendar.MINUTE, new Integer("-" + timeNode.replaceAll("min", "")));
		}
		return calendar.getTime();
	}

	/***
	 * 获取当前机构的同时在线人数
	 * 
	 * @param activeSessions
	 * @param org
	 * @return
	 * @throws Exception
	 */
	private int getOnlineUserCount(Collection<Session> activeSessions, Organization org) throws Exception {
		int onlineUserCount = 0;
		for (Session session : activeSessions) {
			// System.out.println("最后操作日期:" + session.getLastAccessTime());
			// SimplePrincipalCollection attribute = (SimplePrincipalCollection)
			// session
			// .getAttribute("org.apache.shiro.subject.support.DefaultSubjectContext_PRINCIPALS_SESSION_KEY");
			// if (attribute == null)
			// continue;
			// Class<? extends SimplePrincipalCollection> classPrin =
			// attribute.getClass();
			// Field[] declaredFields = classPrin.getDeclaredFields();
			// Field field = Arrays.stream(declaredFields).map(e -> {
			// e.setAccessible(true);
			// return e;
			// }).filter(e ->
			// "realmPrincipals".equals(e.getName())).collect(Collectors.toList()).get(0);
			// if (field.get(attribute) instanceof Map) {
			// Map<?, ?> map = (Map<?, ?>) field.get(attribute);
			// Set<?> set = (Set<?>)
			// map.get("com.trs.netInsight.shiro.MyShiroRealm_0");
			// User user = (User) set.iterator().next();
			// String organizationId = user.getOrganizationId();
			// if (org.getId().equals(organizationId))
			// onlineUserCount++;
			// }
			// 更新于20181212
			if (session != null) {
				Object obj = session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
				if (obj != null) {
					SimplePrincipalCollection spc = (SimplePrincipalCollection) obj;
					if (spc.getPrimaryPrincipal() != null) {
						User user = (User) spc.getPrimaryPrincipal();// 转成User
						String organizationId = user.getOrganizationId();
						if (org.getId().equals(organizationId)) {
							onlineUserCount++;
						}
					}
				}
			}
		}
		return onlineUserCount;
	}

	/***
	 * 获取当前机构当日总登陆次数
	 * 
	 * @return
	 */
	private int getLoginCount(Organization org) {
		List<User> users = userService.findByOrganizationId(org.getId());
		AtomicInteger loginCount = new AtomicInteger(0);
		for (User user : users) {
			int currentUserLoginCount = UserUtils.getLoginCount(user.getUserName() + user.getId());
			loginCount.addAndGet(currentUserLoginCount);
		}
		return loginCount.intValue();
	}

	/**
	 * 将字符串转换为时间
	 * 
	 * @date Created at 2018年11月6日 下午3:19:17
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param times
	 * @return
	 */
	private Date[] timeToDate(String[] times) {
		String now = DateUtil.date2String(new Date(), DateUtil.yyyyMMdd3);
		String benginTime = times[0];
		String endTime = times[1];
		int benginTime0 = Integer.valueOf(benginTime.split(":")[0]);
		int endTime0 = Integer.valueOf(endTime.split(":")[0]);
		if (benginTime0 > endTime0) {
			benginTime = DateUtil.formatDateAfter(now, DateUtil.yyyyMMdd3, -1) + " " + benginTime;
			endTime = now + " " + endTime;
		} else {
			benginTime = now + " " + benginTime;
			endTime = now + " " + endTime;
		}
		Date beginDate = DateUtil.stringToDate(benginTime, DateUtil.yyyyMMddHHmm);
		Date endDate = DateUtil.stringToDate(endTime, DateUtil.yyyyMMddHHmm);
		return new Date[] { beginDate, endDate };
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年7月25日 谷泽昊 creat
 */