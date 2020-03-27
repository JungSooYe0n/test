package com.trs.netInsight.widget.analysis.enums;

import lombok.Getter;

/**
 * 枚举地域类型国家/ 省，直辖市，自治区 / 地级市
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Getter
public enum DistrictType {
	COUNTRY(0), PRIVIENCE(1), CITY(2);

	private int code;

	/**
	 * 构造函数
	 * 
	 * @param code
	 */
	DistrictType(int code) {
		this.code = code;
	}
}
