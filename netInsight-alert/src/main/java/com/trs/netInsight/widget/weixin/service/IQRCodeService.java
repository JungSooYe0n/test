/*
 * Project: netInsight
 * 
 * File Created at 2018年1月25日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.weixin.service;

/**
 * 二维码图片类
 * @Type IQRCodeService.java 
 * @author 谷泽昊
 * @date 2018年1月25日 下午3:42:18
 * @version 
 */
public interface IQRCodeService {

	/**
	 * 获取指定验证码状态
	 * @date Created at 2018年1月25日  下午3:43:24
	 * @Author 谷泽昊
	 * @param ticket
	 * @return
	 */
	public int getStatus(String ticket);
}


/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月25日 谷泽昊 creat
 */