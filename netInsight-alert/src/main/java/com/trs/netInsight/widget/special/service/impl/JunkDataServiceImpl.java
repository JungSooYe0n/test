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
package com.trs.netInsight.widget.special.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.widget.special.entity.JunkData;
import com.trs.netInsight.widget.special.entity.repository.JunkDataRepository;
import com.trs.netInsight.widget.special.service.IJunkDataService;

/**
 * 
 * @Type JunkDataServiceImpl.java 
 * @author 谷泽昊
 * @date 2017年11月27日 下午6:00:35
 * @version 
 */
@Service
@Transactional
public class JunkDataServiceImpl implements IJunkDataService{

	@Autowired
	private JunkDataRepository junkDataRepository;
	
	@Override
	public void save(List<JunkData> datas) {
		junkDataRepository.save(datas);
	}

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