/*
 * Project: netInsight
 * 
 * File Created at 2018年1月26日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.weixin.entity.qrcode;

import lombok.*;

/**
 * 
 * @Type QRCodeBind.java
 * @author 谷泽昊
 * @date 2018年1月26日 下午4:40:22
 * @version
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeBind {
	private String ticket;
	private String imgUrl;
	private String userId;
	private String userName;
	private String displayName;
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月26日 谷泽昊 creat
 */