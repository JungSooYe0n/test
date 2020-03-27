package com.trs.netInsight.widget.report.entity;

import java.util.List;

import lombok.Getter;
import lombok.Setter;


/**
 * Created by shao.guangze on 2018年5月31日 下午7:07:20
 */
@Getter
@Setter
public class TemplateNew4Page {
	
	private String templateId;
	
	private String userId;

	/**
	 * 模板名称
	 */
	private String templateName;
	
	/**
	 * 模板组成
	 */
	private List<TElementNew> templateListData;

	/***
	 * 原有报告头的内容现移到了模板头中。
	 */
	private ReportNew templateHeader;
	/**
	 * 模板类型，日报，周报，月报，专题报
	 */
	private String templateType;
	
	private Integer templatePosition;
	
	/**
	 * 前端高亮显示需要
	 * */
	private Boolean templateActive;
	
	private String groupName;
	
	private Integer isDefault;
}
