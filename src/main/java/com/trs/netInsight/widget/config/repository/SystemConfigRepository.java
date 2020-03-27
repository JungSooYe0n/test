/*
 * Project: netInsight
 * 
 * File Created at 2018年9月18日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.config.repository;

import com.trs.netInsight.widget.config.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * hybase库配置类Repository
 * 
 * @Type HybaseDatabaseConfigRepository.java
 * @author 张娅
 * @date 2020年1月6日
 * @version
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {


}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年9月18日 谷泽昊 creat
 */