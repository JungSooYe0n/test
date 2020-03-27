package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 传统媒体传播实体
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月9日
 *
 */
@Setter
@Getter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.TRADITIONAL)
public class SpreadNewsEntity extends IDocument {
	
	@FtsField("IR_SID")
	private String sid;

	/**
	 * 文章发布时间
	 */
	@FtsField("IR_URLTIME")
	private Date urlTime;

	/**
	 * 文章链接
	 */
	@FtsField("IR_URLNAME")
	private String urlName;
	
	/**
	 * 站点名
	 */
	@FtsField("IR_SITENAME")
	private String name;
	
	/**
	 * 转发自xx的网站名
	 */
	@FtsField("IR_SRCNAME")
	private String srcName;
	
	private List<SpreadNewsEntity> children;
	
}
