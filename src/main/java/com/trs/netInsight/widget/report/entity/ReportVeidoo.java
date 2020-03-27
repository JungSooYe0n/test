package com.trs.netInsight.widget.report.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 生成报告模板的模块
 * @Type ReportVeidoo.java
 * @Desc 
 * @author 谷泽昊
 * @date 2017年11月24日 下午4:33:13
 * @version
 */
@Getter
@Setter
@Entity
@Table(name = "report_veidoo")
public class ReportVeidoo extends BaseEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 报告id
	 */
	@Column(name = "report_id")
	private String reportId;
	
	/**
	 * 维度doc文件生成的路径
	 */
	@Column(name = "veidoo_path")
	private String veidooPath;
	
	/**
	 * 在报告中的顺序
	 */
	@Column(name = "sort")
	private int sort;

}
