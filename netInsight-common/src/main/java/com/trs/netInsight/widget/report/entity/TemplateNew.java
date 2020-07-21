package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 增加或删除字段应与TemplateNew4Page 同步
 * Created by shao.guangze on 2018年5月24日 下午5:41:10
 */
@Getter
@Setter
@Entity
@Table(name = "report_new_template")
public class TemplateNew extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6282468819528391098L;

	/**
	 * 模板名称
	 */
	@Column(name = "template_name")
	private String templateName;
	
	/**
	 * 模板组成
	 */
	@Column(name = "template_list",columnDefinition = "TEXT")
	private String templateList;
	
	/**
	 * 模板类型，日报，周报，月报，专题报
	 */
	@Column(name = "template_type")
	private String templateType;

	/***
	 * 模板头，原来报告头的内容，例如：本期期号、总期号、编制单位、编制作者、统计时间
	 */
	@Column(name = "template_header")
	private String templateHeader;
	
	/**
	 * 模板位置，供模板上下拖动改变位置使用
	 * 第一个位置是 1
	 * */
	@Column(name = "template_position")
	private Integer templatePosition;

	/**
	 * 专报分组
	 * */
	@Column(name = "group_name")
	private String groupName;
	
	//1 为默认模板,页面控制默认模板不可删除
	@Column(name = "is_default")
	private Integer isDefault = 0;
}
