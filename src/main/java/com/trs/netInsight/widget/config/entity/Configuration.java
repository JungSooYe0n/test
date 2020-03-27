/*
 * Project: netInsight
 * 
 * File Created at 2018年9月18日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.config.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.user.entity.Organization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @Type Configuration.java
 * @author 谷泽昊
 * @date 2018年9月18日 下午2:53:57
 * @version
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "configuration")
public class Configuration extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 配置项名
	 */
	@Column(name = "`key`")
	private String key;

	/**
	 * 配置项值
	 */
	@Column(name = "`value`", columnDefinition = "TEXT")
	private String value;

	/**
	 * 配置项显示名
	 */
	@Column(name = "`configuration_name`")
	private String configurationName;

	/**
	 * 机构
	 */
	@ManyToOne
	@JoinColumn(name = "config_organization")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Organization configOrganization;
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年9月18日 谷泽昊 creat
 */