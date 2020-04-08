/*
 * Project: netInsight
 * 
 * File Created at 2018年1月29日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.weixin.entity;

import com.trs.netInsight.widget.config.helper.ConfigConst;
import com.trs.netInsight.widget.config.helper.ConfigHelper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @Type AlertTemplateMsg.java
 * @author 谷泽昊
 * @date 2018年1月29日 下午5:34:59
 * @version
 */
@Getter
@Setter
@ToString
public class AlertTemplateMsg {

	//private String templateId = "WRJUZbLj2DuRlwYzQXSNlV2G2saewC-NW09P2CiXxKQ";
	//新预警模板
	private String templateId = "DfpAMIGgCwsteO4tFUXxw_2cIg3pj5PF8ooMbh72dPg";
	/**
	 * 
	 * @param touser
	 *            发送人
	 * @param url
	 *            跳转连接
	 * @param first
	 *            第一行
	 * @param content
	 *            内容
	 * @param occurtime
	 *            时间
	 * @param remark
	 *            备注
	 */
	public AlertTemplateMsg(String touser, String url, String first, String content, String occurtime, String remark) {
		super();
		this.touser = touser;
		this.url = url;
		this.first = first;
		this.content = content;
		this.occurtime = occurtime;
		this.remark = remark;
	}

	/**
	 * 
	 * @param touser
	 *            发送人
	 * @param first
	 *            第一行
	 * @param content
	 *            内容
	 * @param occurtime
	 *            时间
	 * @param remark
	 *            备注
	 */
	public AlertTemplateMsg(String touser, String first, String content, String occurtime, String remark) {
		super();
		this.touser = touser;
		this.first = first;
		this.content = content;
		this.occurtime = occurtime;
		this.remark = remark;
	}

	/**
	 * 发送人
	 */
	private String touser;

	/**
	 * 跳转连接
	 */
	private String url;

	/**
	 * 第一行
	 */
	private String first;
	/**
	 * 内容
	 */
	private String content; // 内容

	/**
	 * 时间
	 */
	private String occurtime; // 时间

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 变成json
	 * @date Created at 2018年1月31日  下午2:37:09
	 * @Author 谷泽昊
	 * @return
	 */
	public String toJson() {
		return "{\"touser\":\"" + touser + "\",\"template_id\":\"" + ConfigHelper.getConfigValue(ConfigConst.TRS_WEIXIN_TEMPLATEID, templateId) + "\","
				+ " \"url\":\""+url+"\"," + " \"data\":{ \"first\": { \"value\":\"" + first
				+ "\",\"color\":\"#0000FF\"}," + " \"keyword1\":{ \"value\":\"" + content + "\", \"color\":\"#0000FF\"},"
				+ " \"keyword2\":{ \"value\":\"" + occurtime + "\", \"color\":\"#0000FF\"},"
				+ " \"remark\":{ \"value\":\"" + remark + "\",\"color\":\"#0000FF\"}} }";
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月29日 谷泽昊 creat
 */