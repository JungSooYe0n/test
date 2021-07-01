/*
 * Project: netInsight
 * 
 * File Created at 2017年11月24日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.config.constant;


/**
 * @Type ChartConst.java
 * @Desc 分类统计常量
 * @author Administrator
 * @date 2017年11月24日 下午3:52:12
 * @version
 */
public class ChartConst {

	/**
	 * 渠道偏好分类统计返回的数量
	 */
	public static final int VIA_RESULT_SIZE = 6;

	/**
	 * 站点分类统计返回的数量
	 */
	public static final int ACTIVE_LEVEL_SIZE = 12;

	public static final String LEGEND_TODAY = "今天";
	public static final String LEGEND_YESTERDAY = "昨天";
	public static final String LEGEND_HISTORY = "历史";
	public static final String LEGEND_BEGIN = "开始时间到昨天";
	public static final String LEGEND_BEGIN_TODAY = "昨天到今天";

	/**
	 *根据用户名自动添加机构,结构用户前添加标识,防止和原有的机构,账号冲突--自动登录接口使用
	 * 这种方式也不能100%排重,加特殊字符的情况下修改什么都有影响,暂时这样使用
	 * 机构添加: auto login 分公司 Interface 第一个字母  ALFI 改成 天目云-
	 * 用户添加: AUTOLOGINUSERNAME
	 */
	public static final String AUTOLOGIN = "天目云-";
	public static final String AUTOLOGINUSERNAME = "TMY-";
	public static final String AUTOLOGIN_ZB = "浙报-";
	public static final String AUTOLOGINUSERNAME_ZB = "ZB-";
	public static final String AUTOLOGIN_SZ = "宿州-";
	public static final String AUTOLOGINUSERNAME_SZ = "SZ-";

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月24日 Administrator creat
 */