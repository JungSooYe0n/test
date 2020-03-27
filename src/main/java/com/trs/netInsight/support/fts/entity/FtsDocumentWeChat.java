/*
 * Project: trssmas4.0
 * 
 * File Created at 2017年11月23日
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

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.widget.alert.entity.enums.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

/**
 * 微信库---实体类
 * 
 * @Type FtsDocumentWeChat.java
 * @author 谷泽昊
 * @date 2017年11月23日 上午11:47:40
 * @version
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.WEIXIN)
public class FtsDocumentWeChat extends IDocument {

	/**
	 * 1.记录标识，海贝自动生成
	 */
	@FtsField("IR_SID")
	private String sid;

	/**
	 * 2.记录唯一标识，可用做排重
	 */
	@FtsField("IR_HKEY")
	private String hkey;

	/**
	 * 3.入库时间，海贝设置缺省值
	 */
	@FtsField("IR_LOADTIME")
	private String loadTime;

	/**
	 * 4.公众账号名称
	 */
	@FtsField("IR_AUTHORS")
	private String authors;

	/**
	 * 5.公众账号微信号；新加
	 */
	@FtsField("IR_WEIXINID")
	private String weixinId;

	/**
	 * 6.文章作者（允许为空）；新加
	 */
	@FtsField("IR_WRITER")
	private String writer;

	/**
	 * 7.文章链接
	 */
	@FtsField("IR_URLNAME")
	private String urlName;

	/**
	 * 8.文章标题
	 */
	@FtsField(value = "IR_URLTITLE", highLight = true)
	private String urlTitle;

	/**
	 * 9.文章发布时间（含年月日时分秒）新调整
	 */
	@FtsField("IR_URLTIME")
	private Date urlTime;

	/**
	 * 10.文章内容
	 */
	@FtsField(value = "IR_CONTENT", highLight = true)
	private String content;

	/**
	 * 11.阅读数 （设置列存储）
	 */
	@FtsField("IR_RDCOUNT")
	private long rdcount;

	/**
	 * 12.赞数 （设置列存储）
	 */
	@FtsField("IR_PRCOUNT")
	private long prcount;

	/**
	 * 13.文章中包含的图片数
	 */
	@FtsField("IR_IMAGEFLAG")
	private long imageFlag;

	/**
	 * 14.文章中包含的图片，多值
	 */
	@FtsField("IR_URLIMAGE")
	private String urlImage;

	/**
	 * 15.采集时间（含年月日时分秒）
	 */
	@FtsField("IR_LASTTIME")
	private String lastTime;

	/**
	 * 16.文章发布的年份（含年）
	 */
	@FtsField("IR_CREATED_YEAR")
	private String createdYear;

	/**
	 * 17.文章发布的月份（含年月）
	 */
	@FtsField("IR_CREATED_MONTH")
	private String createdMonth;

	/**
	 * 18.文章发布的日期（含年月日）
	 */
	@FtsField("IR_CREATED_DATE")
	private String createdDate;

	/**
	 * 19.文章发布的日期（含年月日）
	 */
	@FtsField("IR_URLDATE")
	private Date urlDate;

	/**
	 * 20.对客户特殊需求的数据进行标记
	 */
	@FtsField("CUSTOMERS")
	private String customers;

	/**
	 * 21.CNML_RULE规则模板分类
	 */
	@FtsField("IR_SRESERVED1")
	private String sreserved1;

	/**
	 * 22.行业分类微信规则模板分类
	 */
	@FtsField("IR_SRESERVED2")
	private String sreserved2;

	/**
	 * 23.站点类型分类规则模板分类
	 */
	@FtsField("IR_VRESERVED3")
	private String vreserved3;

	/**
	 * 24.舆情分类规则模板分类
	 */
	@FtsField("IR_CATALOG2")
	private String catalog2;

	/**
	 * 25.标记字段：（三部采集置为0，梁斌采集置为1）
	 */
	@FtsField("SOURCE")
	private int source;

	/**
	 * 26.真是阅读数：10万以内和rdcount一样，超过10万为真实阅读数
	 */
	@FtsField("IR_REALRDCOUNT")
	private long realrdcount;

	/**
	 * 27.文章在同一批发布中的序号
	 */
	@FtsField("IR_IDX")
	private long idx;

	/**
	 * 28.标记文章是否头条：0：表示非头条；1：表示头条
	 */
	@FtsField("IR_RANK")
	private String rank;

	/**
	 * 29.文章的推送编号
	 */
	@FtsField("IR_MID")
	private String mid;

	/**
	 * 30.文章所属公众号信息
	 */
	@FtsField("IR_BIZ")
	private String biz;

	/**
	 * 微信地域,ckm分类
	 */
	@FtsField("CATALOG_AREA")
	private String catalogArea;

	/**
	 * 情感
	 */
	@FtsField("IR_APPRAISE")
	private String appraise;

	/**
	 * 排重md5值
	 */
	@FtsField("MD5TAG")
	private String md5Tag;
	/**
	 * SITENAME
	 */
	@FtsField("IR_SITENAME")
	private String siteName;
	/**
	 * 新闻信息服务资质
	 */
	@FtsField("IR_WXB_GRADE")
	private String wxbGrade;
	/**
	 * 新闻可供转载网站/门户类型
	 */
	@FtsField("IR_SITE_APTUTIDE")
	private String siteAptutide;
	/**
	 * 网站类型
	 */
	@FtsField("IR_SITE_PROPERTY")
	private String siteProperty;

	/**
	 * 媒体行业/频道
	 */
	@FtsField("IR_CHANNEL_INDUSTRY")
	private String channelIndustry;

	/**
	 * 数据入hybase库的时间
	 */
	@FtsField("HYBASE_LOADTIME")
	private Date HyLoadTime;

	/**
	 * 摘要
	 */
	@FtsField(value = "IR_ABSTRACT", highLight = true)
	private String abstracts;
	/**
	 * 收藏状态
	 */
	private Store store;

	/**
	 * 收藏Boolean型( true 表示已收藏)
	 */
	private Boolean favourite;
	
	/**
	 * 相似文章数
	 */
	private int sim;

	/**
	 * 来源
	 */
	@FtsField("IR_GROUPNAME")
	private String groupName;
	/**
	 * 表达式
	 */
	private String trslk;
	
	/**
	 * 是否发送  为预警加的
	 */
	private boolean send;

	/**
	 * 命中词，包含关键词的语句
	 */
	private String hit;

	/**
	 * 关键词
	 */
	@FtsField("IR_KEYWORDS")
	private List<String> keywords;

	/**
	 * 命中词，被标红的字
	 */
	private String hitWord;
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月23日 谷泽昊 creat
 */