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
package com.trs.netInsight.widget.alert.service.impl;

import java.util.List;

import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.trs.netInsight.widget.alert.entity.AlertRuleBackups;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleBackupsRepository;
import com.trs.netInsight.widget.alert.service.IAlertRuleBackupsService;

/**
 * 规则备份类
 * 
 * @Type AlertRuleBackupsServiceImpl.java
 * @author 谷泽昊
 * @date 2018年3月2日 上午9:54:35
 * @version
 */
@Service
public class AlertRuleBackupsServiceImpl implements IAlertRuleBackupsService {

	@Autowired
	private AlertRuleBackupsRepository alertRuleBackupsRepository;

	@Override
	public List<AlertRuleBackups> list(String alertRuleId, String userId) {
		Sort sort=new Sort(Direction.DESC, "createdTime");
		return alertRuleBackupsRepository.findByAlertRuleIdAndUserId(alertRuleId,userId,sort);
	}

	@Override
	public AlertRuleBackups add(AlertRuleBackups alertRuleBackups) {
		return alertRuleBackupsRepository.save(alertRuleBackups);
	}

	@Override
	public void delete(AlertRuleBackups alertRuleBackups) {
		alertRuleBackupsRepository.delete(alertRuleBackups);
	}

	@Override
	public void deleteByAlertRuleId(String alertRuleId) {
		List<AlertRuleBackups> alertRuleBackups = alertRuleBackupsRepository.findByAlertRuleId(alertRuleId);
		if (ObjectUtil.isNotEmpty(alertRuleBackups)){
			alertRuleBackupsRepository.delete(alertRuleBackups);
		}

	}

	@Override
	public void delete(List<AlertRuleBackups> alertRuleBackupsList) {
		alertRuleBackupsRepository.delete(alertRuleBackupsList);
	}

	@Override
	public void update(AlertRuleBackups alertRuleBackups) {
		alertRuleBackupsRepository.saveAndFlush(alertRuleBackups);
//		alertRuleBackupsRepository.save(alertRuleBackups);
	}

	@Override
	public AlertRuleBackups findOne(String alertRuleBackupsId) {
		return alertRuleBackupsRepository.findOne(alertRuleBackupsId);
	}

	@Override
	public List<AlertRuleBackups> findSimple() {
		return alertRuleBackupsRepository.findBySpecialType(SpecialType.COMMON);
	}

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