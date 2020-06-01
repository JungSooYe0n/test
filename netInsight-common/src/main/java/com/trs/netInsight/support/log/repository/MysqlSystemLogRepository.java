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
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

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