package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.model.result.IDocument;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 新闻传播实体
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@FtsClient
@Setter
@Getter
@NoArgsConstructor
public class NewsSpreadEntity extends IDocument {
	// 继承获得id
	@FtsField("MD5TAG")
	private String md5tag;

	@FtsField("IR_SRCNAME")
	private String srcname;

	@FtsField("IR_SITENAME")
	private String sitename;
}
