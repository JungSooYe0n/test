package com.trs.netInsight.widget.alert.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 预警频率实体
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Getter
@Setter
@Entity
@Table(name = "alert_frequency")
public class Frequency extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2790505721466144105L;
	/**
	 * cron表达式
	 */
	private String cron;
	/**
	 * 要执行的类
	 */
	private String className;

}
