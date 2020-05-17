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
package com.trs.netInsight.widget.weixin.service.impl;

import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.WeixinMessageUtil;
import com.trs.netInsight.widget.weixin.service.IQRCodeService;
import org.springframework.stereotype.Service;

/**
 * 二维码实体类
 * @Type QRCodeServiceImpl.java 
 * @author 谷泽昊
 * @date 2018年1月25日 下午3:41:49
 * @version 
 */
@Service
public class QRCodeServiceImpl implements IQRCodeService {

	@Override
	public int getStatus(String ticket) {
		Integer code = RedisUtil.getInteger(WeixinMessageUtil.QRCODE_REDIS+ticket);
		if(code==null){
			return -1;
		}
		return code;
	}

	
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