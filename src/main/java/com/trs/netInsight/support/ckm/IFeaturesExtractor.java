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

import java.util.List;

/**
 * @Desc 文本特征提取接口服务
 * @author yan.changjiang
 * @date 2018年3月6日 下午5:38:29
 * @version
 */
public interface IFeaturesExtractor {

	/**
	 * 文本特征提取
	 * 
	 * @param documentWords
	 *            语料
	 * @return
	 */
	public double[] extractFeatures(List<String> documentWords);
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月6日 yan.changjiang creat
 */