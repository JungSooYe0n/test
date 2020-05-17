package com.trs.netInsight.support.api.utils.constance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 授权模式
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 *
 */
@Getter
@AllArgsConstructor
public enum GrantType {

	/**
	 * 密码模式,支持refresh token
	 */
	Password("Password"),
	/**
	 * 认证模式-推荐使用(OAuth2标准授权模式),支持refresh token
	 */
	Code("Code"),
	/**
	 * 客户端模式,可供超管对指定机构进行授权
	 */
	Client("Client"),
	/**
	 * 简易模式-不推荐使用
	 */
	Implicit("Implicit");

	/**
	 * 授权模式
	 */
	private String type;
	
}
