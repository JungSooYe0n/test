/*
 * Project: netInsight
 * 
 * File Created at 2018年3月7日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.support.ckm.entity.SimData;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @Desc 文章分析实体
 * @author yan.changjiang
 * @date 2018年3月7日 下午4:56:23
 * @version 
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient
public class FtsDocumentAnalyze extends IDocument{
	
	@FtsField("IR_SID")
	private String sid;

	/**
	 * 正文
	 */
	@FtsField(value = "IR_CONTENT", highLight = true)
	private String content;

	/**
	 * 摘要
	 */
	@FtsField(value = "IR_ABSTRACT", highLight = true)
	private String abstracts;

	/**
	 * 发布时间
	 */
	@FtsField("IR_URLTIME")
	private Date urlTime;


	/**
	 * 地域
	 */
	@FtsField("CQ_LOCATIONS")
	private String area;
	
	/**
	 * 机构
	 */
	@FtsField("CQ_AGENCY")
	private String unit;
	
	/**
	 * 人物
	 */
	@FtsField("CQ_PEOPLE")
	private String people;

	/**
	 * 关键词
	 */
	@FtsField("IR_KEYWORDS")
	private String keywords;

	/**
	 * 构造SimData
	 * @return
	 */
	public SimData toSimData(){
		SimData data = new SimData(sid,area,people,unit,keywords,content,0.5d);
		return data;
	}

}


/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月7日 yan.changjiang creat
 */