package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.enums.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 检索twitter and FaceBook实体
 * 
 * ps: 该类中注释不全,原因为库字段说明文档中注释不全,已告知,等待协商中...
 * 
 * Created by yan.changjiang on 2017年11月23日
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.OVERSEAS)
public class FtsDocumentTF extends IDocument {

	@FtsField("IR_SID")
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
	 * 发布时间
	 */
	@FtsField("IR_URLTIME")
	private Date createdAt;

	/**
	 * 发布时间
	 */
	@FtsField("IR_URLTIME")
	private Date urlTime;

	/**
	 * 发贴时间（年.月.日）
	 */
	@FtsField("IR_URLDATE")
	private Date createdDate;

	/**
	 * 发贴时间(时)
	 */
	@FtsField("IR_URLTIME_HOUR")
	private String createdHour;

	/**
	 * 发贴时间(年.月)
	 */
	@FtsField("IR_URLTIME_MONTH")
	private String createdMonth;

	/**
	 * 发贴时间(年)
	 */
	@FtsField("IR_URLTIME_YEAR")
	private String createdYear;

	/**
	 * 分组名称
	 */
	@FtsField("IR_GROUPNAME")
	private String groupName;

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

	/**
	 * 转发数
	 */
	@FtsField("IR_RTTCOUNT")
	private long rttCount;

	@FtsField("IR_RT_CONTENT")
	private String rtContent;

	/**
	 * 用户昵称
	 */
	@FtsField("IR_AUTHORS")
	private String screenName;

	/**
	 * 用户昵称
	 */
	@FtsField("IR_AUTHORS")
	private String authors;

	/**
	 * 点赞数
	 */
	@FtsField("IR_APPROVE_COUNT")
	private long approveCount;

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

	/**
	 * twitter and FaceBook正文,
	 */
	@FtsField(value = "IR_CONTENT", highLight = true)
	private String statusContent;

	@FtsField(value = "IR_CONTENT", highLight = true)
	private String content;

	/**
	 * 用户id
	 */
	@FtsField("IR_UID")
	private String uid;

	/**
	 * twitter and FaceBook地址
	 */
	@FtsField("IR_URLNAME")
	private String urlName;

	/**
	 * twitter and FaceBook来源:手机,客户端等
	 */
	@FtsField("IR_VIA")
	private String via;

	/**
	 * twitter and FaceBook入库时间
	 */
	@FtsField("HYBASE_LOADTIME")
	private Date HyLoadTime;

	/**
	 * twitter and FaceBook地域,ckm分类
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
	 * 是否发送 为预警加的
	 */
	private boolean send;

	/**
	 * 命中句，包含关键词的语句
	 */
	private String hit;

	/**
	 * 命中词，被标红的字
	 */
	private String hitWord;

	/**
	 * 已读字段(多值---针对用户)
	 */
	@FtsField("IR_READ")
	private String read;

	//    前端识别已读/未读标
	private boolean readFlag;

	public boolean isReadFlag() {
		String userId = UserUtils.getUser().getId();
		if (read.contains(userId)){
			return true;
		}
		return false;
	}

}
