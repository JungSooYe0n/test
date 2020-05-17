package com.trs.netInsight.widget.analysis.entity;

/**
 * key-value 格式数据对象
 *
 * Create by yan.changjiang on 2017年11月21日
 */
public class CategoryBean {

	private final String key;
	private final String value;

	public CategoryBean(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
