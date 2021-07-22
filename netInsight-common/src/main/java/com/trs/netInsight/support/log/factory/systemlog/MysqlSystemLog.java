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
import com.trs.netInsight.support.kafka.util.KafkaUtil;
import com.trs.netInsight.support.log.entity.SystemLog;
import com.trs.netInsight.support.log.factory.AbstractSystemLog;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 日志存入 mysql 实现类
 *
 * @author 谷泽昊
 * @Type MysqlSystemLog.java
 * @date 2018年7月25日 下午4:26:45
 */
@Slf4j
public class MysqlSystemLog extends AbstractSystemLog {

    @Override
    public SystemLog add(SystemLog systemLog, User user) {
        KafkaUtil.send(systemLog);
        return null;
    }

    @Override
    public Map<String, Object> findorgList(String retrievalCondition, String retrievalInformation, String id,
                                           Integer pageNum, Integer pageSize, Integer onLine, Integer sortBy, String ascDesc, String organizationType) throws Exception {
        //多个字段排序
        List<Sort.Order> listSort = new ArrayList();
        Sort.Order order = null;
        if (sortBy == 1 || sortBy == 3 || sortBy == 4) {
            String orderStr = null;
            if (sortBy == 1) {
                orderStr = "loginCount";
            } else if (sortBy == 3) {
                orderStr = "lastDayCount";
            } else {
                orderStr = "weekCount";
            }
            if ("asc".equals(ascDesc)) {
                order = new Sort.Order(Sort.Direction.ASC, orderStr);
            } else if ("desc".equals(ascDesc)) {
                order = new Sort.Order(Sort.Direction.DESC, orderStr);
            } else {
                order = new Sort.Order(Sort.Direction.DESC, "createdTime");
            }
        } else {
            order = new Sort.Order(Sort.Direction.DESC, "createdTime");
        }

        listSort.add(order);
        PageRequest pageable = new PageRequest(pageNum, pageSize, new Sort(listSort));
        //定义查询条件
        Specification<Organization> criteria = new Specification<Organization>() {
            @Override
            public Predicate toPredicate(Root<Organization> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicate = new ArrayList<>();
                if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "organizationName")) {
                    predicate.add(cb.like(root.get("organizationName"), "%" + retrievalInformation + "%"));
                } else if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "userName")) {
//					Set<String> orgIds = userService.findOrgIdByUserNameAndOrgIds(retrievalInformation, null);
                    Set<String> orgIds = userRepository.findOrgIdByUserName(retrievalInformation);
                    In<String> in = cb.in(root.get("id").as(String.class));
                    boolean hasV = false;
                    for (String str : orgIds) {
                        in.value(str);
                        hasV = true;
                    }
                    if (hasV) predicate.add(in);
                }
                if (UserUtils.isRolePlatform()) {
//					User user = userService.findById(id);
                    User user = userRepository.findOne(id);
                    Collection<Organization> orgs = user.getOrganizations();
                    Page<Organization> orgsPages = getOperationsOrgsPages(retrievalCondition, retrievalInformation, orgs, pageNum, pageSize, listSort);
                    orgs = orgsPages.getContent();
                    In<String> in = cb.in(root.get("id").as(String.class));
                    boolean hasV = false;
                    for (Organization organization : orgs) {
                        if (organization != null) {
                            hasV = true;
                            in.value(organization.getId());
                        }
                    }
                    if (hasV) predicate.add(in);
                } else if (UserUtils.isRoleAdmin()) {
                    User user = UserUtils.getUser();
                    Organization organization = organizationRepository.findOne(user.getOrganizationId());
                    Collection<Organization> orgs = new ArrayList<>();
                    orgs.add(organization);
                    Page<Organization> orgsPages = getOperationsOrgsPages(retrievalCondition, retrievalInformation, orgs, pageNum, pageSize, listSort);
                    orgs = orgsPages.getContent();
                    In<String> in = cb.in(root.get("id").as(String.class));
                    boolean hasV = false;
                    for (Organization o : orgs) {
                        if (o != null) {
                            hasV = true;
                            in.value(o.getId());
                        }
                    }
                    if (hasV) predicate.add(in);
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
        //排序走此方法 1登录次数
        if (onLine == 1) {
            pageable = new PageRequest(0, 2000, new Sort(listSort));
            organizations = organizationRepository.findAll(criteria, pageable);
            orgs = organizations.getContent();
            return sortOrgByNumber(orgs, pageNum, pageSize, ascDesc);
        } else {
            organizations = organizationRepository.findAll(criteria, pageable);
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
            each.put("totalTimes", getTotalCount(e));
            //each.put("systemName", e.getSystemName());
            each.put("organizationType", "formal".equals(e.getOrganizationType()) ? "正式" : "试用");
            each.put("onlineUserCount", getOnlineUserCount(activeSessions, e));
            each.put("loginCount", getLoginCount(e));
            each.put("lastDayCount", getLastDayCount(e));
            each.put("weekCount", getWeekCount(e));
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
        if (ObjectUtil.isNotEmpty(orgs)) {
            List<String> orgList = new ArrayList<>();
            for (Organization org : orgs) {
                orgList.add(org.getId());
            }
            int count = mysqlSystemLogRepository.findNumber(orgList, DateUtil.formatCurrentTime("yyyy-MM-dd"));
            result.put("incomeCount", count);
        } else {
            result.put("incomeCount", 0);
        }
        return result;

    }

    public Map<String, Object> sortOrgByNumber(Collection<Organization> orgs,
                                               Integer pageNum, Integer pageSize, String ascDesc) throws Exception {
        log.info("---------->走排序方法:sortOrgByNumber");
        //原本 在线人数和登录次数都不存Mysql，所以查询所有单独排序，现在 - 登录次数存库，则在线人数筛选单独筛选
        ArrayList<HashMap<String, Object>> resultDataList = new ArrayList<>();
        AtomicInteger iii = new AtomicInteger(0);
        Collection<Session> activeSessions = sessionDAO.getActiveSessions();
        List<Organization> orgList = new ArrayList<>();
        for (Organization e : orgs) {
            e.setLoginCount(getLoginCount(e));
            e.setOnlineUserCount(getOnlineUserCount(activeSessions, e));
            e.setAscDesc(ascDesc);
            if (e.getOnlineUserCount() > 0) {
                orgList.add(e);
            }
        }
        //计算页数
        int totalPages;
        int totalElements = orgList.size();
        if (totalElements % pageSize == 0) totalPages = totalElements % pageSize;
        else totalPages = totalElements % pageSize + 1;

        //循环从list中取想要的数据
        int start = (pageSize - 1) * pageNum;
        if (start < totalElements) {
            int limit = (start + pageSize) < totalElements ? (start + pageSize) : totalElements;
            for (int j = start; j < limit; j++) {
                Organization e = orgList.get(j);
                iii.getAndIncrement();// 以原子方式+1
                HashMap<String, Object> each = new HashMap<>();
                each.put("order", iii.intValue());
                each.put("organizationName", e.getOrganizationName());
                //each.put("systemName", e.getSystemName());
                each.put("organizationType", "formal".equals(e.getOrganizationType()) ? "正式" : "试用");
                each.put("onlineUserCount", e.getOnlineUserCount());
                each.put("loginCount", e.getLoginCount());
                each.put("lastDayCount", getLastDayCount(e));
                each.put("weekCount", getWeekCount(e));
                each.put("organizationId", e.getId());
                resultDataList.add(each);
            }
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
     * 获取运维所有项目
     *
     * @param retrievalCondition
     * @param retrievalInformation
     * @param orgs
     * @param pageNum
     * @param pageSize
     * @return
     * @date Created at 2018年11月6日 下午4:59:42
     * @author 北京拓尔思信息技术股份有限公司
     * @author 谷泽昊
     */
    private Page<Organization> getOperationsOrgsPages(String retrievalCondition, String retrievalInformation,
                                                      Collection<Organization> orgs, Integer pageNum, Integer pageSize, List<Sort.Order> listSort) {
        List<String> ids = new ArrayList<>();
        if (orgs != null && orgs.size() > 0) {
            for (Organization organization : orgs) {
                if (organization != null) {
                    ids.add(organization.getId());
                }
            }
        }
        if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "organizationName")) {
            return organizationRepository.findByOrganizationNameLikeAndIdIn("%" + retrievalInformation + "%", ids,
                    new PageRequest(pageNum, pageSize, new Sort(listSort)));
        } else if (StringUtil.isNotEmpty(retrievalInformation) && StringUtils.equals(retrievalCondition, "userName")) {
//			Set<String> orgIds = userService.findOrgIdByUserNameAndOrgIds(retrievalInformation, ids);
            Set<String> orgIds = userRepository.findOrgIdByUserName(retrievalInformation);
            return organizationRepository.findByIdIn(orgIds,
                    new PageRequest(pageNum, pageSize, new Sort(listSort)));
        } else {
            return organizationRepository.findByIdIn(ids,
                    new PageRequest(pageNum, pageSize, new Sort(listSort)));
        }
    }

    @Override
    public Page<SystemLog> findCurOrgLogs(String organizationId, String timeLimited, String userId,
                                          String operation, String operationDetail, String systemLogOperationType, String createTimeOrder,
                                          String simpleStatus, Integer pageNum, Integer pageSize, String retrievalInformation) {
        //获取系统日志
        return getSysLogList(organizationId, timeLimited, userId, operation, operationDetail, systemLogOperationType,
                createTimeOrder, simpleStatus, pageNum, pageSize, retrievalInformation);
    }

    @Override
    public List<Map<String, String>> curOrgUsers(String organizationId) {
        //根据机构id查询机构用户
        List<User> users = userRepository.findByOrganizationId(organizationId);
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
                                                 String retrievalCondition, String retrievalInformation, String createTimeOrder, Integer pageNum, Integer pageSize) {
        // 查system_log
        List<String> ids = new ArrayList<>();
        // 查systemlog表关联查organization表不太好实现，但又都需要in orgIds，所以有关org表的相关操作都挪到这里来
        if (UserUtils.isSuperAdmin()) {
            List<Organization> all = organizationRepository.findAll();
            for (Organization organization : all) {
                if (organization != null) {
                    ids.add(organization.getId());
                }
            }
            if ("organizationName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
                Collection<Organization> orgs = organizationRepository.findByOrganizationNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
                ids = orgs.stream().map(e -> e.getId()).collect(Collectors.toList());
            }
			/*else if ("systemName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
				Collection<Organization> orgs = organizationService
						.findBySystemNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
				ids = orgs.stream().map(e -> e.getId()).collect(Collectors.toList());
			}*/
        } else if (UserUtils.isRolePlatform()) {
            // 当前运维人员
            User user = userRepository.findOne(id);
            Set<Organization> organizations = user.getOrganizations();
            for (Organization organization : organizations) {
                if (organization != null) {
                    ids.add(organization.getId());
                }
            }
            if ("organizationName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
                Collection<Organization> orgs = organizationRepository.findByOrganizationNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
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
            Organization one = organizationRepository.findOne(user.getOrganizationId());
            if (one != null) {
                ids = Arrays.asList(one.getId());
            }
            if ("organizationName".equals(retrievalCondition) && StringUtils.isNotBlank(retrievalCondition)) {
                Collection<Organization> orgs = organizationRepository.findByOrganizationNameLikeAndIdIn("%" + retrievalInformation + "%", ids);
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
                    In<String> organizationIdIns = cb.in(root.get("organizationId").as(String.class));
                    for (String eachId : finalIds) {
                        organizationIdIns.value(eachId);
                    }
                    allPredicates.add(organizationIdIns);
                }
                // 账号条件。系统名称条件和机构名称条件已经在查询机构表的时候做过处理了
                if (retrievalCondition != null && ("createdUserName".equals(retrievalCondition.trim()) || "requestIp".equals(retrievalCondition.trim()))
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
        } else sort = new Sort(Sort.Direction.DESC, "createdTime");

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
            Organization org = organizationRepository.findOne(e.getOrganizationId());
            each.put("organizationName", org.getOrganizationName());
            //each.put("systemName", org.getSystemName());
            each.put("createdUserName", e.getCreatedUserName());
            each.put("createdTime", e.getCreatedTime());
            each.put("timeConsumed", e.getTimeConsumed());
            each.put("systemLogType", e.getSystemLogType());
            each.put("id", e.getId());
            each.put("requestIp", e.getRequestIp());
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
    public List<Map<String, Object>> ipPer() {
        String ids = getOrganIdsStr();
        List<Object[]> logs = mysqlSystemLogRepository.findRequestIP(ids);
        System.out.println(logs.get(0)[0] + "----->" + logs.get(0)[1]);
        List<Map<String, Object>> listmap = new ArrayList<>();
        int otherNum = 0;
        //前9显示,以后显示 其他
        for (int i = 0; i < logs.size(); i++) {
            Integer num = Integer.parseInt(logs.get(i)[1].toString());
            String ip = (String) logs.get(i)[0];
            if (num == 0) break;
            if (i < 10) {
                String ipCity = "未知";
                //去redis缓存查找
                if (RedisUtil.getString(ipPre + ip) == null) {
                    ipCity = IPUtil.getArea(ip);
                }
                RedisUtil.setString(ipPre + ip, ipCity, 7, TimeUnit.DAYS);
                ip = ipCity + ":" + ip;
                Map<String, Object> infoMap = new HashMap<>();
                infoMap.put(ip, num);
                listmap.add(infoMap);
            } else {
                otherNum += num;
            }
        }
        if (otherNum > 0) {
            Map<String, Object> otherMap = new HashMap<>();
            otherMap.put("其他", otherNum);
            listmap.add(otherMap);
        }
        return listmap;
    }

    /**
     * 获得当前用户机构ids
     *
     * @return
     */
    public List<String> getOrganIds() {
        List<String> ids = new ArrayList<>();
        if (UserUtils.isSuperAdmin()) {
            List<Organization> all = organizationRepository.findAll();
            for (Organization organization : all) {
                if (organization != null) {
                    ids.add(organization.getId());
                }
            }
        } else {
            // 机构管理员 UserUtils.is...
            User user = UserUtils.getUser();
            Organization one = organizationRepository.findOne(user.getOrganizationId());
            if (one != null) {
                ids = Arrays.asList(one.getId());
            }
        }
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        return ids;
    }

    public String getOrganIdsStr() {
        List<String> list = getOrganIds();
        StringBuffer sb = new StringBuffer();
        for (String s : list) {
            sb.append(s);
            sb.append(",");
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    /**
     * 获取系统日志
     *
     * @param organizationId
     * @param timeLimited
     * @param userId
     * @param operation
     * @param pageNum
     * @param pageSize
     * @return
     * @date Created at 2018年11月6日 下午5:00:13
     * @author 北京拓尔思信息技术股份有限公司
     * @author 谷泽昊
     */
    //前端只发了这些数据：organizationId、operation、timeLimited、pageNum、pageNum
    private Page<SystemLog> getSysLogList(String organizationId, String timeLimited, String userId,
                                          String operation, String operationDetail, String systemLogOperationType,
                                          String createTimeOrder, String simpleStatus,
                                          Integer pageNum, Integer pageSize, String retrievalInformation) {

        // 原生 查询sql
        Specification<SystemLog> criteria = new Specification<SystemLog>() {
            @Override
            public Predicate toPredicate(Root<SystemLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                ArrayList<Predicate> allPredicates = new ArrayList<>();
                //加入organizationId作为查询条件
                Predicate organizationIdPredicate = cb.equal(root.get("organizationId").as(String.class),
                        organizationId);
                allPredicates.add(organizationIdPredicate);
                if (StringUtil.isNotEmpty(userId)) {
                    //加入createdUserId作为查询条件
                    Predicate userIdPredicate = cb.equal(root.get("createdUserId").as(String.class), userId);
                    allPredicates.add(userIdPredicate);
                }
                if (StringUtil.isNotEmpty(operationDetail)) {
                    //like （operationDetail——操作明细）
                    Predicate userIdPredicate = cb.like(root.get("systemLogOperation").as(String.class), "%" + operationDetail + "%");
                    allPredicates.add(userIdPredicate);
                }
                if (StringUtil.isNotEmpty(systemLogOperationType)) {
                    //具体操作：查询 或 登录——systemLogOperationType
                    Predicate userIdPredicate = cb.like(root.get("systemLogOperationType").as(String.class), "%" + systemLogOperationType + "%");
                    allPredicates.add(userIdPredicate);
                }
                if (StringUtil.isNotEmpty(simpleStatus)) {
                    //simpleStatus——操作状态
                    Predicate userIdPredicate = cb.equal(root.get("simpleStatus").as(String.class), simpleStatus);
                    allPredicates.add(userIdPredicate);
                }
                // 账号条件。系统名称条件和机构名称条件已经在查询机构表的时候做过处理了
                if (StringUtil.isNotEmpty(retrievalInformation)) {
                    Predicate predicateLike = cb.like(root.get("requestIp"),
                            "%" + retrievalInformation + "%");
                    allPredicates.add(predicateLike);
                }
                if (StringUtil.isNotEmpty(operation)) {
                    //operation——操作
                    Predicate operationPredicate = cb.equal(root.get("systemLogType").as(String.class), operation);
                    allPredicates.add(operationPredicate);

                }
                //timeLimited——时间
                if (StringUtil.isNotEmpty(timeLimited)) {
                    String[] times = timeLimited.split(";");
                    if (times.length > 1) {
                        Date[] timeToDate = timeToDate(times);
                        Predicate timeLimitedPredicate = cb.between(root.get("createdTime").as(Date.class),
                                timeToDate[0], timeToDate[1]);
                        allPredicates.add(timeLimitedPredicate);
                        //times:7d，长度是1：问题是createdTime是什么时候存入mysql的
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
        //createTimeOrder——根据时间排序 现在前端发的是空的——>所以按照createdTime降序排列
        if (createTimeOrder != null && createTimeOrder.toLowerCase().contains("asc"))
            sort = new Sort(Sort.Direction.ASC, "createdTime");
        else sort = new Sort(Sort.Direction.DESC, "createdTime");

        PageRequest pageRequest = new PageRequest(pageNum, pageSize, sort);
        //JpaSpecificationExecutor可以实现带查询条件的分页查询
        //criteria：封装了JPA Criteria查询的查询条件
        //pageRequest：封装了请求分页的信息 eg：pageNum、pageSize、sort
        //SystemLog：系统日志
        Page<SystemLog> sysLogs = mysqlSystemLogRepository.findAll(criteria, pageRequest);

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
            calendar.add(Calendar.HOUR_OF_DAY, new Integer(("-" + timeNode.replaceAll("h", "")).toString().trim()));
        } else if (timeNode.contains("d")) {
            calendar.add(Calendar.DAY_OF_MONTH, new Integer(("-" + timeNode.replaceAll("d", "")).toString().trim()));
        } else {
            calendar.add(Calendar.MINUTE, new Integer(("-" + timeNode.replaceAll("min", "")).toString().trim()));
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
        List<User> users = userRepository.findByOrganizationId(org.getOrganizationId());
        AtomicInteger loginCount = new AtomicInteger(0);
        for (User user : users) {
            int currentUserLoginCount = UserUtils.getLoginCount(user.getUserName() + user.getId());
            loginCount.addAndGet(currentUserLoginCount);
        }
        return loginCount.intValue();
    }

    /***
     * 获取当前机构总登陆次数
     *
     * @return
     */
    private int getTotalCount(Organization org) {
        List<User> users = userRepository.findByOrganizationId(org.getOrganizationId());
        AtomicInteger loginCount = new AtomicInteger(0);
        for (User user : users) {
            int currentUserLoginCount = user.getTotalTimes() == null ? 0 : user.getTotalTimes();
            loginCount.addAndGet(currentUserLoginCount);
        }
        return loginCount.intValue();
    }

    private int getLastDayCount(Organization org) {
        List<User> users = userRepository.findByOrganizationId(org.getOrganizationId());
        AtomicInteger loginCount = new AtomicInteger(0);
        for (User user : users) {
            int currentUserLoginCount = UserUtils.getDaysBeforeLoginCount(user.getUserName() + user.getId(), 1);
            loginCount.addAndGet(currentUserLoginCount);
        }
        return loginCount.intValue();
    }

    private int getWeekCount(Organization org) {
        List<User> users = userRepository.findByOrganizationId(org.getOrganizationId());
        AtomicInteger loginCount = new AtomicInteger(0);
        for (User user : users) {
            int currentUserLoginCount = UserUtils.getWeekLoginCount(user.getUserName() + user.getId());
            loginCount.addAndGet(currentUserLoginCount);
        }
        return loginCount.intValue();
    }

    /**
     * 将字符串转换为时间
     *
     * @param times
     * @return
     * @date Created at 2018年11月6日 下午3:19:17
     * @author 北京拓尔思信息技术股份有限公司
     * @author 谷泽昊
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
        return new Date[]{beginDate, endDate};
    }
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年7月25日 谷泽昊 creat
 */
