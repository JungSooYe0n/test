/*
 * Project: netInsight
 * 
 * File Created at 2018年3月2日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.alert.entity.repository;

import com.trs.netInsight.widget.alert.entity.AlertRuleBackups;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 规则备份表
 * @Type AlertRuleBackupsRepository.java 
 * @author 谷泽昊
 * @date 2018年3月2日 上午9:52:23
 * @version 
 */
@Repository
public interface AlertRuleBackupsRepository extends JpaRepository<AlertRuleBackups, String>{

	/**
	 * 根据规则和用户id查询
	 * @date Created at 2018年3月6日  下午12:16:34
	 * @Author 谷泽昊
	 * @param alertRuleId
	 * @param userId
	 * @param sort 
	 * @return
	 */
	List<AlertRuleBackups> findByAlertRuleIdAndUserId(String alertRuleId, String userId, Sort sort);

	List<AlertRuleBackups> findByAlertRuleId(String alertRuleId);

	List<AlertRuleBackups> findBySpecialType(SpecialType specialType);

}


/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月2日 谷泽昊 creat
 */