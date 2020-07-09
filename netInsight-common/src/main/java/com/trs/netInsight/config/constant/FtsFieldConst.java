/*
 * Project: netInsight
 *
 * File Created at 2017年11月29日
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.config.constant;

/**
 * 海贝库字段常量
 *
 * @Type FtsFieldConst.java
 * @author 谷泽昊
 * @date 2017年11月29日 下午5:21:14
 * @version
 */
public class FtsFieldConst {

	public final static String WEIGHT = "^50";
	//有词序
	public final static String PRE = "#PRE";
	//无词序
	public final static String POS = "#POS";

	/**
	 * 相似度，至少命中多少词
	 */
	public final static String INCLUDE = "#INCLUDE";
	/**
	 * 超过这个时间开始打日志
	 */
	public final static int OVER_TIME = 2000;
	/**
	 * 传统-微信 sid
	 */
	public final static String FIELD_SID = "IR_SID";
	/**
	 * 微博 mid
	 */
	public final static String FIELD_MID = "IR_MID";

	/**
	 * 相关性排序
	 */
	public final static String FIELD_RELEVANCE = "RELEVANCE";

	/**
	 * hybase排重标记(1000/10000标)
	 */
	public final static String FIELD_SIMFLAG = "SIMFLAG";

	/**
	 * URLNAME重复标记 (0/1标)
	 */
	public final static String FIELD_IR_SIMFLAG = "IR_SIMFLAG";
	/**
	 * 跨数据源打标 （0/1）
	 */
	public final static String FIELD_IR_SIMFLAGALL = "IR_SIMFLAGALL";

	// -------------传统-------------------------------

	/**
	 * 标题
	 */
	public final static String FIELD_TITLE = "IR_URLTITLE";

	/**
	 * 正文
	 */
	public final static String FIELD_CONTENT = "IR_CONTENT";

	/**
	 * 摘要
	 */
	public final static String FIELD_ABSTRACTS = "IR_ABSTRACT";

	/**
	 * 发布时间
	 */
	public final static String FIELD_URLTIME = "IR_URLTIME";

	/**
	 * 发布日期
	 */
	public final static String FIELD_URLDATE = "IR_URLDATE";

	/**
	 * 地址层级
	 */
	public final static String FIELD_URLLEVEL = "IR_URLLEVEL";

	/**
	 * 关键词
	 */
	public final static String FIELD_KEYWORDS = "IR_KEYWORDS";

	/**
	 * 排重md5值
	 */
	public final static String FIELD_MD5TAG = "MD5TAG";

	/**
	 * 地址
	 */
	public final static String FIELD_URLNAME = "IR_URLNAME";

	/**
	 * 站点名称
	 */
	public final static String FIELD_SITENAME = "IR_SITENAME";

	/**
	 * 情感值
	 */
	public final static String FIELD_APPRAISE = "IR_APPRAISE";

	/**
	 * 分组名
	 */
	public final static String FIELD_GROUPNAME = "IR_GROUPNAME";

	/**
	 * 图片标记
	 */
	public final static String FIELD_IMAGEFLAG = "IR_IMAGEFLAG";

	/**
	 * wxb白名单
	 */
	public final static String FIELD_WXB_LIST = "IR_WXB_LIST";

	// -------------微博-------------------------------
	public final static String FIELD_ANALYZETIME = "IR_ANALYZETIME";
	public final static String FIELD_ATS = "IR_ATS";

	/**
	 * 栏目
	 */
	public final static String FIELD_CHANNEL = "IR_CHANNEL";

	/**
	 * 评论数
	 */
	public final static String FIELD_COMMTCOUNT = "IR_COMMTCOUNT";

	/**
	 * 发布时间
	 */
	public final static String FIELD_CREATED_AT = "IR_URLTIME";

	/**
	 * 发贴时间（年.月.日）
	 */
	public final static String FIELD_CREATED_DATE = "IR_CREATED_DATE";

	/**
	 * 发贴时间(时)
	 */
	public final static String FIELD_CREATED_HOUR = "IR_CREATED_HOUR";

	/**
	 * 发贴时间(年.月)
	 */
	public final static String FIELD_CREATED_MONTH = "IR_CREATED_MONTH";

	/**
	 * 发贴时间(年)
	 */
	public final static String FIELD_CREATED_YEAR = "IR_CREATED_YEAR";
	public final static String FIELD_HASHTAGS = "IR_HASHTAGS";

	/**
	 * 数据中心使用标记
	 */
	public final static String FIELD_HKEY = "IR_HKEY";

	/**
	 * 采集时间
	 */
	public final static String FIELD_LASTTIME = "IR_LASTTIME";
	public final static String FIELD_LAT = "IR_LAT";
	public final static String FIELD_LBSTITLE = "IR_LBSTITLE";
	public final static String FIELD_LBSTYPE = "IR_LBSTYPE";

	/**
	 * 入库时间
	 */
	public final static String FIELD_LOADTIME = "IR_LOADTIME";

	/**
	 * 地域信息
	 */
	public final static String FIELD_LOCATION = "IR_LOCATION";
	public final static String FIELD_LON = "IR_LON";
	public final static String FIELD_POIID = "IR_POIID";

	/**
	 * 转发消息的ID 微博判断转发 / 原发
	 */
	public final static String FIELD_RETWEETED_MID = "IR_RETWEETED_MID";

	/**
	 * 转发消息的用户昵称
	 */
	public final static String FIELD_RETWEETED_SCREEN_NAME = "IR_RETWEETED_SCREEN_NAME";

	/**
	 * 转发消息的用户ID
	 */
	public final static String FIELD_RETWEETED_UID = "IR_RETWEETED_UID";

	/**
	 * 转发消息的url
	 */
	public final static String FIELD_RETWEETED_URL = "IR_RETWEETED_URL";

	/**
	 * 转发消息的用户名  不支持分类统计
	 */
	public final static String FIELD_RETWEETED_FROM = "IR_RETWEETED_FROM";
	/**
	 * 转发消息的用户名  支持分类统计
	 */
	public final static String FIELD_RETWEETED_FROM_ALL = "IR_RETWEETED_FROM_ALL";
	public final static String FIELD_RTATS = "IR_RTATS";
	public final static String FIELD_RTHASHTAGS = "IR_RTHASHTAGS";

	/**
	 * 贴吧楼层
	 */
	public final static String FIELD_BBSNUM = "IR_BBSNUM";
	/**
	 * 主贴 0 /回帖 1
	 */
	public final static String FIELD_NRESERVED1 = "IR_NRESERVED1";
	/**
	 * 转发数
	 */
	public final static String FIELD_RTTCOUNT = "IR_RTTCOUNT";
	public final static String FIELD_RTVURL = "IR_RTVURL";
	public final static String FIELD_RTCONTENT = "IR_RTCONTENT";
	public final static String FIELD_RTIMG = "IR_RTIMG";
	public final static String FIELD_RTVCLASS = "IR_RTVCLASS";

	/**
	 * 用户昵称
	 */
	public final static String FIELD_SCREEN_NAME = "IR_SCREEN_NAME";
	public final static String FIELD_SOURCE = "IR_SOURCE";
	public final static String FIELD_SRCCMNUM = "IR_SRCCMNUM";
	public final static String FIELD_SRCRTNUM = "IR_SRCRTNUM";
	public final static String FIELD_STATUS_BODY = "IR_STATUS_BODY";

	/**
	 * 微博正文
	 */
	public final static String FIELD_STATUS_CONTENT = "IR_CONTENT";

	/**
	 * 原创帖子图像地址,只有原创帖子才有值
	 */
	public final static String FIELD_THUMBNAIL_PIC = "IR_THUMBNAIL_PIC";

	/**
	 * 用户id
	 */
	public final static String FIELD_UID = "IR_UID";

	/**
	 * 微博来源:手机,客户端等
	 */
	public final static String FIELD_VIA = "IR_VIA";
	public final static String FIELD_VURL = "IR_VURL";

	/**
	 * 微博入库时间
	 */
	public final static String FIELD_HYLOAD_TIME = "HYBASE_LOADTIME";

	/**
	 * 微博入库时间
	 */
	public final static String FIELD_HYLOAD_TIME_A = "HYBASE_LOADTIME_A";

	/**
	 * 内容地域信息 - 信息中出现的地域信息
	 */
	public final static String FIELD_CATALOG_AREA = "CATALOG_AREA";

	/**
	 * 媒体地域 - 发布信息方对应的地址
	 */
	public final static String FIELD_MEDIA_AREA = "IR_MEDIA_AREA";
	/**
	 * 信息过滤 - 这个值对信息的性质进行打标，如游戏广告、转发抽奖
	 */
	public final static String FIELD_FILTER_INFO = "IR_NOISEMESSAGE";
	/**
	 * 内容行业 - 信息中出现的行业信息
	 */
	public final static String FIELD_CONTENT_INDUSTRY = "IR_INDUSTRY_TYPE";
	/**
	 * 媒体行业 - 发布信息方对应的行业类型
	 */
	public final static String FIELD_MEDIA_INDUSTRY = "IR_INDUSTRY_MEDIA";
	/**
	 * 媒体等级
	 */
	public final static String FIELD_MEDIA_LEVEL = "IR_MEDIA_RANK";


	/**
	 * 微博判断转发 / 原发
	 */
	public static final String IR_RETWEETED_MID = "IR_RETWEETED_MID";

	/**
	 * 微博内容情绪标识
	 */
	public static final String FIELD_EMOJI = "IR_EMOJI";

	//--------------微博用户---------------------------
	/**
	 * 微博用户地域
	 */
	public final static String FIELD_USER_LOCATION = "IR_LOCATION";

	/**
	 * 微博用户性别
	 */
	public final static String FIELD_GENDER = "IR_GENDER";

	/**
	 * 微博认证
	 */
	public final static String FIELD_VERIFIED = "IR_VERIFIED";

	// -------------微信-------------------------------

	/**
	 * 4.公众账号名称
	 */
	public final static String FIELD_AUTHORS = "IR_AUTHORS";

	/**
	 * 5.公众账号微信号；新加
	 */
	public final static String FIELD_WEIXINID = "IR_WEIXINID";

	/**
	 * 6.文章作者（允许为空）；新加
	 */
	public final static String FIELD_WRITER = "IR_WRITER";

	/**
	 * 8.文章标题
	 */
	public final static String FIELD_URLTITLE = "IR_URLTITLE";

	/**
	 * 11.阅读数 （设置列存储）
	 */
	public final static String FIELD_RDCOUNT = "IR_RDCOUNT";

	/**
	 * 12.赞数 （设置列存储）
	 */
	public final static String FIELD_PRCOUNT = "IR_PRCOUNT";

	/**
	 * 14.文章中包含的图片，多值
	 */
	public final static String FIELD_URLIMAGE = "IR_URLIMAGE";

	/**
	 * 20.对客户特殊需求的数据进行标记
	 */
	public final static String FIELD_CUSTOMERS = "CUSTOMERS";

	/**
	 * 21.CNML_RULE规则模板分类
	 */
	public final static String FIELD_SRESERVED1 = "IR_SRESERVED1";

	/**
	 * 22.行业分类微信规则模板分类
	 */
	public final static String FIELD_SRESERVED2 = "IR_SRESERVED2";

	/**
	 * 23.站点类型分类规则模板分类
	 */
	public final static String FIELD_VRESERVED3 = "IR_VRESERVED3";

	/**
	 * 24.舆情分类规则模板分类
	 */
	public final static String FIELD_CATALOG2 = "IR_CATALOG2";

	/**
	 * 26.真是阅读数：10万以内和RDCOUNT一样，超过10万为真实阅读数
	 */
	public final static String FIELD_REALRDCOUNT = "IR_REALRDCOUNT";

	/**
	 * 27.文章在同一批发布中的序号
	 */
	public final static String FIELD_IDX = "IR_IDX";

	/**
	 * 28.标记文章是否头条：0：表示非头条；1：表示头条
	 */
	public final static String FIELD_RANK = "IR_RANK";

	/**
	 * 30.文章所属公众号信息
	 */
	public final static String FIELD_BIZ = "IR_BIZ";
	/**
	 * 行业
	 */
	public final static String FIELD_INDUSTRY = "IR_INDUSTRY";

	/**
	 * 排除
	 */
	public final static String FIELD_NOT = "NOT";

	// ---------------------------------------------
	/**
	 * 小时 --传统
	 */
	public final static String FIELD_URLTIME_HOUR = "IR_URLTIME_HOUR";

	/**
	 * 网友观点
	 */
	public final static String FIELD_TAG_TXT = "IR_TAG_TXT";

	/**
	 * 超话
	 */
	public final static String FIELD_TAG = "IR_TAG";

	/**
	 * 机构
	 */
	public final static String FIELD_AGENCY = "CQ_AGENCY";

	/**
	 * 人物
	 */
	public final static String FIELD_PEOPLE = "CQ_PEOPLE";

	/**
	 * 地域分类
	 */
	public final static String CATALOG_AREANEW = "CATALOG_AREA";//

	/**
	 * 关键词
	 */
	public final static String IR_KEYWORDSNEW = "IR_KEYWORDS";//

	/**
	 * 身份证
	 */
	public final static String CQ_IDCARDNEW = "CQ_IDCARD";//

	/**
	 * 电话号
	 */
	public final static String CQ_PHONENEW = "CQ_PHONE";//

	/**
	 * 邮箱
	 */
	public final static String CQ_EMAILNEW = "CQ_EMAIL";//

	/**
	 * qq
	 */
	public final static String CQ_QQNEW = "CQ_QQ";//
	/**
	 * 地域
	 */
	public final static String CQ_LOCATIONSNEW = "	CQ_LOCATIONS";//

	/**
	 * 字符集
	 */
	public final static String FIELD_CHARSET = "IR_CHARSET";//

	/**
	 * 文章长度
	 */
	public final static String FIELD_DOCLENGTH = "IR_DOCLENGTH";//

	/**
	 * 年
	 */
	public final static String FIELD_URLTIME_YEAR = "IR_URLTIME_YEAR";//

	/**
	 * 月
	 */
	public final static String FIELD_URLTIME_MONTH = "IR_URLTIME_MONTH";//

	/**
	 * 来源
	 */
	public static final String FIELD_SRCNAME = "IR_SRCNAME";
	/**
	 * 机构id
	 */
	public static final String FIELD_ORGANIZATIONID = "ORGANIZATIONID";

	/**
	 * 媒体行业
	 */
	public static final String FIELD_CHANNEL_INDUSTRY = "IR_CHANNEL_INDUSTRY";

	/**
	 * 新闻信息资质
	 */
	public static final String FIELD_WXB_GRADE = "IR_WXB_GRADE";

	/**
	 *新闻可供转载网站/门户类型
	 */
	public static final String FIELD_SITE_APTUTIDE = "IR_SITE_APTUTIDE";

	/**
	 * 网站类型
	 */
	public static final String FIELD_SITE_PROPERTY = "IR_SITE_PROPERTY";

	public static final String FIELD_EMOTION = "IR_EMOTION";
	public static final String FIELD_EMOTION_2 = "IR_EMOTION2";

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月29日 谷泽昊 creat
 */