/*
 * Project: netInsight
 * 
 * File Created at 2017年11月21日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.springframework.stereotype.Service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.widget.analysis.service.IRelationAnalyzeService;

/**
 * @Type RelationAnalyzeServiceImpl.java
 * @Desc 
 * @author yan.changjiang
 * @date 2017年11月21日 下午5:13:55
 * @version
 */
@Service
public class RelationAnalyzeServiceImpl implements IRelationAnalyzeService {

	@Override
	public List<Map<String, Object>> getPersonList(String[] persons, String startTime, String endTime, String topNum)
			throws TRSException {
		final CountDownLatch mDoneSignal = new CountDownLatch(persons.length);
		// ExecutorService executor =
		// Executors.newFixedThreadPool(persons.length);
		List<Map<String, Object>> resultsMap = new ArrayList<>();
		// IEsSearchOpenService esSearchService =
		// (IEsSearchOpenService)ESServiceFactory.getBean("esSearchService");
		for (String name : persons) {
			String[] dates = { startTime, endTime };
			// ITRSSearchBuilder searchBuilder =
			// SearchBuilderFactory.createNoPagedBuilder();
			QueryBuilder searchBuilder = new QueryBuilder();
			searchBuilder.filterField("IR_URLTIME", dates, Operator.Between);
			searchBuilder.filterField("IR_ME_TXT", name, Operator.Equal);
			searchBuilder.filterField("IR_MENTION", name, Operator.Equal);
			// executor.execute(new PersonRelationAnalyzeTask(esSearchService,
			// mDoneSignal, name, searchBuilder.asTRSL(), topNum, resultsMap));
			// executor.execute(new
			// PersonRelationAnalyzeTask(hybase8SearchService, mDoneSignal,
			// name, searchBuilder.asTRSL(), topNum, resultsMap));
		}
		try {
			mDoneSignal.await();
		} catch (InterruptedException e) {
			throw new TRSException("任务关系挖掘，线程出错:" + e);
		}
		return resultsMap;
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月21日 Administrator creat
 */