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

import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.factory.systemlogoperation.ColumnSystemLogOperation;
import com.trs.netInsight.support.log.factory.systemlogoperation.LoginSystemLogOperation;
import com.trs.netInsight.support.log.factory.systemlogoperation.SpecialSystemLogOperation;
import com.trs.netInsight.widget.column.service.IIndexPageService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.column.service.INavigationService;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.special.service.ISpecialSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 日志操作工厂
 * 
 * @Type OperationPositionFactory.java
 * @Desc
 * @author 北京拓尔思信息技术股份有限公司
 * @author 谷泽昊
 * @date 2018年11月7日 下午1:57:43
 * @version
 */
@Component
public class SystemLogOperationFactory {

	private static ISpecialProjectService specialProjectService;
	private static IIndexTabMapperService indexTabMapperService;
	private static IIndexPageService indexPageService;
	private static INavigationService navigationService;
	private static ISpecialSubjectService specialSubjectService;

	@Autowired
	public void setSpecialSubjectRepository(ISpecialSubjectService specialSubjectService) {
		SystemLogOperationFactory.specialSubjectService = specialSubjectService;
	}

	@Autowired
	public void setSpecialProjectService(ISpecialProjectService specialProjectService) {
		SystemLogOperationFactory.specialProjectService = specialProjectService;
	}

	@Autowired
	public void setIndexTabMapperService(IIndexTabMapperService indexTabMapperService) {
		SystemLogOperationFactory.indexTabMapperService = indexTabMapperService;
	}

	@Autowired
	public void setIndexPageService(IIndexPageService indexPageService) {
		SystemLogOperationFactory.indexPageService = indexPageService;
	}

	@Autowired
	public void setNavigationService(INavigationService navigationService) {
		SystemLogOperationFactory.navigationService = navigationService;
	}

	/**
	 * 根据反射创建 AbstractSystemLog
	 * 
	 * @date Created at 2018年11月7日 上午11:05:04
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param
	 * @return
	 */
	public static AbstractSystemLogOperation createSystemLogOperation(SystemLogType systemLogType) {

		AbstractSystemLogOperation abstractSystemLogOperation = null;
		switch (systemLogType) {
		case SPECIAL:
			abstractSystemLogOperation = new SpecialSystemLogOperation();
			break;
		case COLUMN:
			abstractSystemLogOperation = new ColumnSystemLogOperation();
			break;
		case LOGIN:
			abstractSystemLogOperation = new LoginSystemLogOperation();
			break;
		default:
			break;
		}

		if (abstractSystemLogOperation != null) {
			abstractSystemLogOperation.init(specialProjectService, indexTabMapperService, indexPageService,
					navigationService,specialSubjectService);
		}
		return abstractSystemLogOperation;
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