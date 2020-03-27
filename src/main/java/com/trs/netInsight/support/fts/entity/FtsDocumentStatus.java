package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.alert.entity.enums.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 检索微博实体
 * 
 * ps: 该类中注释不全,原因为库字段说明文档中注释不全,已告知,等待协商中...
 * 
 * Created by yan.changjiang on 2017年11月23日
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.WEIBO)
public class FtsDocumentStatus extends IDocument {

	@FtsField("IR_MID")
	private String mid;

	@FtsField("IR_ANALYZETIME")
	private Date analyzeTime;

	@FtsField("IR_ATS")
	private String ats;

	/**
	 * 栏目
	 */
	@FtsField("IR_CHANNEL")
	private String channel;

	/**
	 * 评论数
	 */
	@FtsField("IR_COMMTCOUNT")
	private long commtCount;

	/**
	 * 点赞数
	 */
	@FtsField("IR_APPROVE_COUNT")
	private long approveCount;

	/**
	 * 发布时间
	 */
	@FtsField("IR_URLTIME")
	private Date createdAt;
	/**
	 * 原文时间-日期
	 */
	@FtsField("IR_URLDATE")
	private Date urlDate;

	/**
	 * 原文时间-时间
	 */
	@FtsField("IR_URLTIME")
	private Date urlTime;
	/**
	 * 发贴时间（年.月.日）
	 */
	@FtsField("IR_CREATED_DATE")
	private Date createdDate;

	/**
	 * 发贴时间(时)
	 */
	@FtsField("IR_CREATED_HOUR")
	private String createdHour;

	/**
	 * 发贴时间(年.月)
	 */
	@FtsField("IR_CREATED_MONTH")
	private String createdMonth;

	/**
	 * 发贴时间(年)
	 */
	@FtsField("IR_CREATED_YEAR")
	private String createdYear;

	/**
	 * 分组名称
	 */
	@FtsField("IR_GROUPNAME")
	private String groupName;

	@FtsField("IR_HASHTAGS")
	private String hashTags;

	/**
	 * 数据中心使用标记
	 */
	@FtsField("IR_HKEY")
	private String hkey;

	/**
	 * 采集时间
	 */
	@FtsField("IR_LASTTIME")
	private Date lastTime;

	@FtsField("IR_LAT")
	private String lat;

	@FtsField("IR_LBSTITLE")
	private String lbsTitle;

	@FtsField("IR_LBSTYPE")
	private String lbsType;

	/**
	 * 入库时间
	 */
	@FtsField("IR_LOADTIME")
	private Date loadTime;

	/**
	 * 地域信息
	 */
	@FtsField("IR_LOCATION")
	private String location;

	@FtsField("IR_LON")
	private String lon;

	@FtsField("IR_POIID")
	private String poiId;

	/**
	 * 转发消息的ID
	 */
	@FtsField("IR_RETWEETED_MID")
	private String retweetedMid;

	/**
	 * 转发消息的用户昵称
	 */
	@FtsField("IR_RETWEETED_SCREEN_NAME")
	private String retweetedScreenName;

	/**
	 * 转发消息的用户ID
	 */
	@FtsField("IR_RETWEETED_UID")
	private String retweetedUid;

	/**
	 * 转发消息的url
	 */
	@FtsField("IR_RETWEETED_URL")
	private String retweetedUrl;

	@FtsField("IR_RTATS")
	private String rtats;

	@FtsField("IR_RTHASHTAGS")
	private String rtHashTags;

	/**
	 * 转发数
	 */
	@FtsField("IR_RTTCOUNT")
	private long rttCount;

	@FtsField("IR_RTVURL")
	private String rtvUrl;

	@FtsField("IR_RT_CONTENT")
	private String rtContent;

	@FtsField("IR_RT_IMG")
	private String rtImg;

	@FtsField("IR_RT_V_CLASS")
	private String rtVClass;

	/**
	 * 用户昵称
	 */
	@FtsField("IR_SCREEN_NAME")
	private String screenName;

	/**
	 * 自增长值
	 */
	@FtsField("IR_SID")
	private String sid;

	/**
	 * 网站名称,站点名称
	 */
	@FtsField("IR_SITENAME")
	private String siteName;

	@FtsField("IR_SOURCE")
	private String source;

	@FtsField("IR_SRCCMNUM")
	private String srcCmNum;

	@FtsField("IR_SRCRTNUM")
	private String srcRtNum;

	/**
	 * 
	 */
	@FtsField("IR_STATUS_BODY")
	private String statusBody;

	/**
	 * 微博正文
	 */
	@FtsField(value="IR_CONTENT",highLight = true)
	private String statusContent;

	@FtsField(value="IR_CONTENT",highLight = true)
	private String content;

	/**
	 * 标题
	 */
	@FtsField(value = "IR_URLTITLE", highLight = true)
	private String title;
	/**
	 * 原创帖子图像地址,只有原创帖子才有值
	 */
	@FtsField("IR_THUMBNAIL_PIC")
	private String thumbnailPic;

	/**
	 * 用户id
	 */
	@FtsField("IR_UID")
	private String uid;

	/**
	 * 微博地址
	 */
	@FtsField("IR_URLNAME")
	private String urlName;

	/**
	 * 微博来源:手机,客户端等
	 */
	@FtsField("IR_VIA")
	private String via;

	/**
	 * 
	 */
	@FtsField("IR_VURL")
	private String vUrl;

	/**
	 * 数据入hybase库的时间
	 */
	@FtsField("HYBASE_LOADTIME")
	private Date HyLoadTime;

	/**
	 * 微博入库时间
	 */
	@FtsField("HYBASE_LOADTIME_A")
	private Date HyloadTimeA;

	/**
	 * 微博地域,ckm分类
	 */
	@FtsField("CATALOG_AREA")
	private String catalogArea;
	
	/**
	 * 排重md5值
	 */
	@FtsField("MD5TAG")
	private String md5Tag;
	
	/**
	 * 情感值
	 */
	@FtsField("IR_APPRAISE")
	private String appraise;
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
	 * 微博参与话题
	 */
	@FtsField("IR_TAG")
	private String tag;

	/**
	 * 作者
	 */
	@FtsField(value = "IR_AUTHORS")
	private String authors;
	/**
	* 收藏状态
	 */
	private Store store;
	
	/**
	 * 收藏Boolean型
	 */
	private Boolean favourite;
	
	/**
	 * 相似文章数
	 */
	private int sim;
	/**
	 * 表达式
	 */
	private String trslk;
	
	/**
	 * 是否发送  为预警加的
	 */
	private boolean send;
	public String baseUrl() {
		return StringUtil.isEmpty(this.retweetedUrl) ? this.urlName : this.retweetedUrl;
	}

	/**
	 * 用于引爆点
	 * @return
	 */
	public String beforeUrl() {
		return StringUtil.isEmpty(this.urlName) ? this.retweetedUrl : this.urlName;
	}

	public String getMid(){
		if(StringUtil.isEmpty(this.mid)){
			return this.sid;
		}
		return this.mid;
	}
	/**
	 * 粉丝数
	 */
	private long followersCount;

	/**
	 * 命中句，包含关键词的语句
	 */
	private String hit;

	/**
	 * 命中词，被标红的字
	 */
	private String hitWord;
}
