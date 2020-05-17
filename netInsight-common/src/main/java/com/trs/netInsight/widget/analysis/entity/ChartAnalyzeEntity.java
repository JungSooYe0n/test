package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.StringUtil;
import lombok.*;

import java.util.Date;
import java.util.List;

/**
 * 图表分析实体
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FtsClient
public class ChartAnalyzeEntity extends IDocument {

	@FtsField("IR_SID")
	private String sid;

	@FtsField("IR_UID")
	private String uid;

	@FtsField(value = "IR_URLTITLE", highLight = true)
	private String title;

	@FtsField(value = "IR_CONTENT", highLight = true)
	private String content;

	@FtsField(value = "IR_ABSTRACT", highLight = true)
	private String abstracts;

	@FtsField("IR_URLTIME")
	private Date urltime;

	@FtsField("IR_URLDATE")
	private Date urldate;

	@FtsField("IR_URLLEVEL")
	private int urlLevel;

	@FtsField("IR_RETWEETED_URL")
	private String rUrl;

	@FtsField("IR_URLNAME")
	private String url;

	@FtsField("MD5TAG")
	private String md5Tag;

	@FtsField("IR_KEYWORDS")
	private List<String> keywords;

	@FtsField("CATALOG_AREA")
	private String catalogArea;

	@FtsField("IR_GROUPNAME")
	private String groupName;

	/**
	 * 主贴 0 /回帖 1
	 */
	@FtsField("IR_NRESERVED1")
	private String nreserved1;

	/**
	 * 相似文章数
	 */
	private int simNum;

	/**
	 * 站点
	 */
	@FtsField("IR_SITENAME")
	private String siteName;

	@FtsField("IR_PROFILE_IMAGE_URL")
	private String imageUrl;
	
	/**
	 * 情感值
	 */
	@FtsField("IR_APPRAISE")
	private String appraise;
	
	/**
	 * 是否发送  为预警加的
	 */
	private boolean send;

	public String baseUrl() {
		return StringUtil.isEmpty(this.rUrl) ? this.url : this.rUrl;
	}

}