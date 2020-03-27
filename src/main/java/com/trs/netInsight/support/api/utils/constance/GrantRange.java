package com.trs.netInsight.support.api.utils.constance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Access Token 授权范围
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 *
 */
@Getter
@AllArgsConstructor
public enum GrantRange {

	/**
	 * 最大授权范围
	 */
	Max("0000", "Max"),

	/**
	 * 日常监测
	 */
	Column("1000", "Column"),

	/**
	 * 专题
	 */
	Project("2000", "Project"),

	/**
	 * 报告
	 */
	Report("3000", "Report"),

	/**
	 * 预警
	 */
	Alert("4000", "Alert"),

	/**
	 * 检索
	 */
	Select("5000", "Select"),

	/**
	 * 平台管理
	 */
	PlatForm("9000", "platForm");

	/**
	 * 授权范围码,持久化代码
	 */
	private String code;

	/**
	 * 授权码释义,用于匹配参数
	 */
	private String param;

}
