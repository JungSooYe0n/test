package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 微博分析实体类
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.WEIBO)
public class MBlogAnalyzeEntity extends IDocument {

	@FtsField("IR_MID")
	private String sid;

	@FtsField("IR_PROFILE_IMAGE_URL")
	private String imageUrl;

	@FtsField("IR_UID")
	private String uid;

	@FtsField("IR_VIA")
	private String via;
//
	@FtsField("IR_URLTIME")
	private String createdAt;
//	@FtsField("IR_LOADTIME")
//	private String createdAt;

	@FtsField(value = "IR_CONTENT",highLight = true)
	private String content;

	@FtsField("IR_URLNAME")
	private String url;

	/**
	 * 用户名
	 */
	@FtsField("IR_SCREEN_NAME")
	private String author;

	/**
	 * 转发
	 */
	@FtsField("IR_RTTCOUNT")
	private Integer rttCount;

	/**
	 * 评论
	 */
	@FtsField("IR_COMMTCOUNT")
	private Integer commentCount;
	
	/**
	 * md5值
	 */
	@FtsField("MD5TAG")
	private String md5Tag;
	
	private long  count;

	/**
	 * 我的发言
	 *//*
		 * @SearchField(field = "IR_ME_TXT", searchFields = true, dataType =
		 * DataType.INT) private Integer myContent;
		 */
	/**
	 * 微博UID
	 */
	// @SearchField(field = "IR_UID", searchFields = true, dataType =
	// DataType.INT)
	// private Integer uid;

	/**
	 * 情感值
	 */
	// @SearchField(field = "IR_EMOTIONAL_DIGIT", searchFields = true, dataType
	// = DataType.STRING)
	// private String ir_emotional_digit;

	@FtsField("IR_RETWEETED_URL")
	private String rurl;

	@FtsField("IR_RETWEETED_SCREEN_NAME")
	private String screenName;

	@FtsField("IR_RETWEETED_FROM")
	private List<String> retFrom;

	@FtsField("IR_TAG_TXT")
	private List<String> tagTxt;

	@FtsField("IR_LOCATION")
	private List<String> location;

	private String trslk;
}
