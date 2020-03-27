package com.trs.netInsight.widget.report.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.Setter;


/**
 * 舆情报告实体
 * Created by xiaoying on 2017年12月13日
 */
@SuppressWarnings("serial")
@Getter
@Setter
@Entity
@Table(name = "report_info")
public class Report extends BaseEntity {

	
	/**
	 * 对应的模板id
	 */
	@Column(name = "template_id")
	private String templateId;
	
	/**
	 * 对应的数据id
	 */
	@Column(name = "data_id")
	private String dataId;
	
	/**
	 * 报告名称
	 */
	@Column(name = "report_name")
	private String reportName;
	
	/**
	 * 报告对应得素材库id
	 */
	@Column(name = "library_id")
	private String libraryId;
	
	/**
	 * 报告状态（0：正在生成，1：已完成  2:正在下载）
	 */
	@Column(name = "status")
	private int status;
	
	/**
	 * word文档生成后的路径
	 */
	@Column(name = "doc_file_path")
	private String docFilePath;
	
	
	/**
	 * 报告生成百分比
	 */
	@Column(name = "bulid_rat")
	private Double bulidRat=0.0;
	
	/**
	 * word文档大小
	 */
	@Column(name = "doc_file_size")
	private String docFileSize;
	
}
