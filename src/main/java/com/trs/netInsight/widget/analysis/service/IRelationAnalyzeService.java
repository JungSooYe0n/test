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
package com.trs.netInsight.widget.analysis.service;

import java.util.List;
import java.util.Map;

import com.trs.netInsight.handler.exception.TRSException;

/**
 * 任务关系挖掘相关接口服务
 *
 * Create by yan.changjiang on 2017年11月21日
 */
public interface IRelationAnalyzeService {
	
	/**
	 * 
	 * @param persons
	 * @param startTime
	 * @param endTime
	 * @param topNum
	 * @return
	 * @throws TRSException
	 */
	public List<Map<String, Object>> getPersonList(String[] persons, String startTime, String endTime, String topNum) throws TRSException;

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * 
 * When I wrote this, only God and I understood what I was doing. But now, God
 * only knows!
 * -------------------------------------------------------------------------
 * 2017年11月21日 Administrator creat
 */