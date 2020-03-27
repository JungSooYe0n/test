package com.trs.netInsight.widget.analysis.entity;

import java.util.Date;
import java.util.List;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.StringUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author songbinbin 2017年5月3日
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FtsClient(indices="ni_weibo")
public class ChartAnalyzeEntityNew extends IDocument{

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


	public String baseUrl() {
		return StringUtil.isEmpty(this.rUrl) ? this.url : this.rUrl;
	}

}