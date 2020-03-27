package com.trs.netInsight.widget.analysis.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 地域热点聚类结果表
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "area_result")
public class AreaResult extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4041463911044766942L;

	/**
	 * 地域
	 */
	@Column(name = "location")
	private String location;

	/**
	 * 数据id
	 */
	@Column(name = "sid")
	private String sid;

	/**
	 * 热度值
	 */
	@Column(name = "hot_num")
	private int hotNum;

	/**
	 * 时间戳
	 */
	@Column(name = "timstamp")
	private String timstamp;

}
