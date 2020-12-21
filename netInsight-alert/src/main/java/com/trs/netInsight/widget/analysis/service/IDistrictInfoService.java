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

import com.trs.netInsight.widget.analysis.entity.DistrictInfo;

import java.util.List;
import java.util.Map;

/**
 * 地域检索服务接口实现
 *
 * Create by yan.changjiang on 2017年11月21日
 */
public interface IDistrictInfoService {

	/**
	 * 获取所有的地域信息
	 *
	 * @return
	 */
	public Map<String, List<String>> allAreas();

	/**
	 * 根据市获取省
	 *
	 * @param city
	 * @return
	 */
	public String province(String city);

	/**
	 * 根据市获得编码信息
	 * @param city
	 * @return
	 */
	DistrictInfo getCodeBy(String city);

	List<DistrictInfo> getAreasByCode(String city);
	DistrictInfo getCityByCode(String city);

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月21日 yan.changjiang creat
 */
