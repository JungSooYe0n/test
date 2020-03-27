/*
 * Project: netInsight
 * 
 * File Created at 2018年3月6日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.ckm;

import com.trs.netInsight.support.ckm.entity.ClusterInfo;

/**
 * @Desc 聚类接口服务
 * @author yan.changjiang
 * @date 2018年3月6日 下午5:59:33
 * @version
 */
public interface ICluster {

	/**
	 * 聚类
	 * 
	 * @return
	 */
	public ClusterInfo[] cluster();

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月6日 yan.changjiang creat
 */