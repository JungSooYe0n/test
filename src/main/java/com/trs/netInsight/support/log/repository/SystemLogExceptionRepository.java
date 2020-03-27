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

import com.trs.netInsight.support.log.entity.BigScreenLog;
import com.trs.netInsight.support.log.entity.SystemLogException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 日志存入 mysql 实现类
 * @Type BigScreenLogRepository.java
 * @author lilei
 * @date 2020年02月20日 中午11:27:52
 * @version 
 */
@Repository
public interface SystemLogExceptionRepository extends JpaRepository<SystemLogException, String>,JpaSpecificationExecutor<SystemLogException> {
    SystemLogException findBySystemLogId(String systemLogId);
}
