package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * 模板表
 * Created by xiaoying on 2017年12月13日
 */
@Getter
@Setter
@Entity
@Table(name = "report_template")
public class Template extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8009850993329147128L;

	/**
	 * 模板名称
	 */
	@Column(name = "template_name")
	private String templateName;
	
	
	/**
	 * 模板所在路径
	 */
	@Column(name = "template_path")
	private String templatePath;
	
	/**
	 * 模板组成
	 */
	@Column(name = "template_list",columnDefinition = "TEXT")
	private String templateList;
	
	
}
