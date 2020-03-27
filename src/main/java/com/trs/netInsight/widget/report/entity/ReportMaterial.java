package com.trs.netInsight.widget.report.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 添加到素材库
 * @Type ReportMaterial.java
 * @Desc 
 * @author 谷泽昊
 * @date 2017年11月24日 下午4:25:16
 * @version
 */
@Getter
@Setter
@Entity
@Table(name = "report_material")
public class ReportMaterial extends BaseEntity{


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 素材id
	 */
	private String libraryId;

	/**
	 * 文章id
	 */
    private String sid;

    /**
     * 0：删除状态  1：正常状态
     */
    private String status;
    
    /**
     * 存储来源 以便查询时知道查哪个库
     */
    private String groupName;



}
