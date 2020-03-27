package com.trs.netInsight.support.ckm.enums;

import lombok.Getter;

/**
 * 枚举实体抽取类型
 *
 * Create by liang.xin on 2018年7月12日
 */

public enum PloType {
	/**
	 * 抽取人名/地名/机构名
	 */
	NAME_AREA_ORGANIZATION(1),
	
	/**
	 * 抽取数字——时间、MSN、email、QQ、车牌、护照号、身份证号、电话号码等有意义的数字信息
	 */
	NUMBER(2),
	
	/**
	 * 抽取案件
	 */
	CASE(4),
	
	/**
	 * 抽取房屋相关
	 */
	HOUSE(8),
	
	/**
	 * 抽取所有实体信息
	 */
	ALL(65535),
	
//==============以下是抽取结果后分析的细分项============================
	/**
	 * 人名
	 */
	NAME(1001),
	
	/**
	 * 地名
	 */
	NETHERLANGS(1002),
	
	/**
	 * 机构名
	 */
	ORGANIZATION(1003),
	
	/**
	 * 时间
	 */
	TIME(2001),
	
	/**
	 * MSN
	 */
	MSN(2002),
	
	/**
	 * email
	 */
	EMAIL(2003);
	
	@Getter
	int code;

	PloType(int code) {
		this.code = code;
	}
	
	
}
