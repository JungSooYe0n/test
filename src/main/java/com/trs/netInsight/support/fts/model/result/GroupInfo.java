package com.trs.netInsight.support.fts.model.result;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 分类统计结果详情
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Data
@AllArgsConstructor
public class GroupInfo implements Comparable<GroupInfo>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1518394256476689597L;

	/**
	 * 字段值
	 */
	private String fieldValue;

	/**
	 * 数量
	 */
	private long count;

	@Override
	public int compareTo(GroupInfo o) {
		return o.getCount() > this.count ? 1 : (this.count == o.getCount() ? 0 : -1);
	}
}
