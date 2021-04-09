/*
 * Project: netInsight
 * 
 * File Created at 2017年11月20日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.util;

/**
 * 状态码util
 * 
 * @Type CodeUtils.java
 * @author 谷泽昊
 * @date 2017年11月20日 下午5:23:02
 * @version
 */
public class CodeUtils {
	/**
	 * 成功
	 */
	public static final int SUCCESS = 200;

	/**
	 * 失败
	 */
	public static final int FAIL = 201;

	/**
	 * 没有权限
	 */
	public static final int FORBIDDEN_FAIL = 403;

	/**
	 * 未知账号
	 */
	public static final int UNKNOWN_ACCOUNT = 202;

	/**
	 * 账号错误
	 */
	public static final int ACCOUNT_ERROR = 203;

	/**
	 * 账号已被锁定
	 */
	public static final int ACCOUNT_LOCKOUT = 204;
	/**
	 * 没有登录
	 */
	public static final int NO_LOGIN = 205;
	/**
	 * 未知错误
	 */
	public static final int UNKNOWN_FAIL = 206;
	/**
	 * 验证码错误
	 */
	public static final int VCODE_FAIL = 207;

	/**
	 * 强制下线
	 */
	public static final int COMPULSORY_OFFLINE = 208;

	/**
	 * 账号为空
	 */
	public static final int ACCOUNT_NULL = 209;

	/**
	 * 用户名为空
	 */
	public static final int DISPLAYNAME_NULL = 210;

	/**
	 * 密码为空
	 */
	public static final int PASSWORD_NULL = 211;

	/**
	 * 密码强度低
	 */
	public static final int PASSWORD_LOW = 212;

	/**
	 * 机构名为空
	 */
	public static final int ORGANIZATIONNAME_NULL = 213;

	/**
	 * 邮箱为空
	 */
	public static final int EMAIL_NULL = 214;

	/**
	 * 邮件格式不正确
	 */
	public static final int EMAIL_FALSE = 215;

	/**
	 * 两次密码不一致
	 */
	public static final int PASSWORD_NOT_SAME = 216;

	/**
	 * 机构名字存在
	 */
	public static final int ORGANIZATIONNAME_EXISTED = 217;

	/**
	 * 用户账号存在
	 */
	public static final int USERNAME_EXISTED = 218;

	/**
	 * 账号中有空格
	 */
	public static final int ACCOUNT_SPACE = 219;

	/**
	 * 机构用户满了
	 */
	public static final int USER_LIMIT = 220;
	/**
	 * 手机号错误
	 */
	public static final int PHONE_FAIL = 221;

	/**
	 * 账号是否过期
	 */
	public static final int IS_EXPIRED = 222;

	/**
	 * 登录账号个数无法删除
	 */
	public static final int USER_DELETE = 223;

	/**
	 * 数据为空
	 */
	public static final int DATA_IS_NULL = 224;

	/**
	 * 服务器运算出错
	 */
	public static final int OPERATION_EXCEPTION = 500;

	/**
	 * 请输入要分析的微博地址
	 */
	public static final int  STATUS_URLNAME = 301;

	/*
	*  hybase出错 （运行超时）
	*/
	public static final int HYBASE_TIMEOUT = 302;
	/*
	 *  hybase出错 （表达式超长）
	 */
	public static final int HYBASE_EXCEPTION = 303;

	/**
	 * 验证码储存到session的 key
	 */
	public static final String CODE_SESSION_KEY = "CODE_SESSION_KEY";
	/**
	 * 删除机构时获取验证码的key
	 */
	public static final String SUPER_CODE_KEY = "SUPER_CODE_KEY_";


}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月20日 谷泽昊 creat
 */