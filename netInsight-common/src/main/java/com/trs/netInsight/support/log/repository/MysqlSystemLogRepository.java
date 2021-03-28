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
package com.trs.netInsight.support.log.repository;

import com.trs.netInsight.support.log.entity.SystemLog;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 日志存入 mysql 实现类
 * @Type MysqlSystemLogRepository.java 
 * @author 谷泽昊
 * @date 2018年7月25日 下午4:27:52
 * @version 
 */
@Primary
@Repository("mysqlSystemLogRepository")
public interface MysqlSystemLogRepository extends JpaRepository<SystemLog, String>,JpaSpecificationExecutor<SystemLog> {

    Collection<SystemLog> findAllByBrowserInfoIsNotNullAndOrganizationIdIn(List<String> list);

    @Query(value = "SELECT " +
            "request_ip as requestIp,COUNT(*) as ipCount " +
            "FROM system_log " +
            "WHERE organization_id IN (?1)" +
            "GROUP BY request_ip ORDER BY ipCount DESC LIMIT 50", nativeQuery = true)
    List<Object[]> findRequestIP(String ids);

    //查询数量
    @Query(value = "SELECT count(a.id) total  " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM system_log a " +
            "WHERE `organization_id` IN (:orgIds)" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d')", nativeQuery = true)
    int findNumber(@Param("orgIds")List<String> orgIds, @Param("dateStr")String dateStr);

    //查询数量 根据机构查
    @Query(value = "SELECT system_log_type as item, count(a.id) num " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM system_log a " +
            "WHERE `organization_id` IN (:orgIds) AND `system_log_type` != '登录相关'" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') group by system_log_type", nativeQuery = true)
    Object[] itemPer(@Param("orgIds")List<String> orgIds, @Param("dateStr")String dateStr);

    //查询数量 根据机构查
    @Query(value = "SELECT system_log_type as item, count(a.id) num " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM system_log a " +
            "WHERE `createduser_id` IN (:userIds) AND `system_log_type` != '登录相关'" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') group by system_log_type", nativeQuery = true)
    Object[] itemPerByUserId(@Param("userIds")List<String> userIds, @Param("dateStr")String dateStr);

    //查询数量 根据机构查
    @Query(value = "SELECT organization_id,DATE(created_time) 'createdTime',COUNT(id) num ,createduser_id " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM system_log a " +
            "WHERE `createduser_id` IN (:userIds) AND `system_log_type` != '登录相关'" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') GROUP BY organization_id ,createduser_id, createdTime ORDER BY createdTime", nativeQuery = true)
    List<String[]> timeStatic(@Param("userIds")List<String> userIds, @Param("dateStr")String dateStr);

    //查询数量 根据机构查
    @Query(value = "SELECT organization_id,DATE(created_time) 'createdTime',COUNT(id) num ,createduser_id " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM system_log a " +
            "WHERE `organization_id` IN (:orgIds) AND `system_log_type` != '登录相关'" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') GROUP BY organization_id ,createduser_id, createdTime ORDER BY createdTime", nativeQuery = true)
    List<String[]> timeStaticOrg(@Param("orgIds")List<String> orgIds, @Param("dateStr")String dateStr);

    //查询数量 根据机构查
    @Query(value = "SELECT HOUR(created_time) 'hourStr',COUNT(id) num " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM system_log a " +
            "WHERE `createduser_id` IN (:userIds) AND `system_log_type` != '登录相关'" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') ORDER BY hourStr", nativeQuery = true)
    List<String[]> hourStatic(@Param("userIds")List<String> userIds, @Param("dateStr")String dateStr);

    //查询数量 根据机构查
    @Query(value = "SELECT HOUR(created_time) 'hourStr',COUNT(id) num " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM system_log a " +
            "WHERE `organization_id` IN (:orgIds) AND `system_log_type` != '登录相关'" +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') ORDER BY hourStr", nativeQuery = true)
    List<String[]> hourStaticOrg(@Param("orgIds")List<String> orgIds, @Param("dateStr")String dateStr);

    //查询数量 根据机构查
    @Query(value = "SELECT * " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
            "FROM system_log a " +
            "WHERE `organization_id` IN (:orgIds) " +
            "AND a.created_time >= date_format((:dateStr),'%Y-%m-%d %H:%M:%S') ORDER BY time_consumed desc limit 10 ", nativeQuery = true)
    List<SystemLog> getResponseTimeElastic(@Param("orgIds")List<String> orgIds, @Param("dateStr")String dateStr);

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