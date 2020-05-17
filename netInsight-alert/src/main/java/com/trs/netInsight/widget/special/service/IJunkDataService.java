/*
 * Project: netInsight
 * 
 * File Created at 2017年11月27日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.special.service;

import java.util.List;

import com.trs.netInsight.widget.special.entity.JunkData;

/**
 * 垃圾数据
 * @Type IJunkDataService.java 
 * @author 谷泽昊
 * @date 2017年11月27日 下午6:00:00
 * @version 
 */
public interface IJunkDataService {

	/**
	 * 保存垃圾数据
	 * @date Created at 2017年11月27日  下午6:03:45
	 * @Author 谷泽昊
	 * @param datas
	 */
	public void save(List<JunkData> datas);

}


/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月27日 谷泽昊 creat
 */