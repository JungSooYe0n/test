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
package com.trs.netInsight.support.log.factory;

import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trs.netInsight.support.log.entity.enums.DepositPattern;
import com.trs.netInsight.support.log.repository.MysqlSystemLogRepository;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.IUserService;

import lombok.extern.slf4j.Slf4j;

/**
 * 系统日志写入类
 * 
 * @Type SystemLogFactory.java
 * @author 谷泽昊
 * @date 2018年7月25日 下午4:21:06
 * @version
 */
@Slf4j
@Component
public class SystemLogFactory {

	private static MysqlSystemLogRepository mysqlSystemLogRepository;

	private static SessionDAO sessionDAO;

	private static IUserService userService;

	private static IOrganizationService organizationService;

	@Autowired
	public void setMysqlSystemLogRepository(MysqlSystemLogRepository mysqlSystemLogRepository) {
		SystemLogFactory.mysqlSystemLogRepository = mysqlSystemLogRepository;
	}

	@Autowired
	public void setSessionDAO(SessionDAO sessionDAO) {
		SystemLogFactory.sessionDAO = sessionDAO;
	}

	@Autowired
	public void setUserService(IUserService userService) {
		SystemLogFactory.userService = userService;
	}

	@Autowired
	public void setOrganizationService(IOrganizationService organizationService) {
		SystemLogFactory.organizationService = organizationService;
	}

	/**
	 * 根据反射创建 AbstractSystemLog
	 * 
	 * @date Created at 2018年11月7日 上午11:05:04
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param depositPattern
	 * @return
	 */
	public static AbstractSystemLog createSystemLog(DepositPattern depositPattern) {
		AbstractSystemLog abstractSystemLog = null;
		try {
			@SuppressWarnings("unchecked")
			Class<AbstractSystemLog> forName =(Class<AbstractSystemLog>) depositPattern.getName();
			abstractSystemLog = forName.newInstance();
			abstractSystemLog.init(mysqlSystemLogRepository, sessionDAO, userService, organizationService);
		} catch (Exception e) {
			log.error("created systemLog error", e);
		}
		return abstractSystemLog;
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