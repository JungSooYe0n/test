package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.widget.alert.entity.enums.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 传统媒体对应实体
 *
 * Created by xiaoying on 2017年11月23日
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.INSERT)
public class FtsDocumentInsertShow extends IDocument {

	/**
	 * 用于修改
	 */
	@FtsField("uid")
	private String uid;
	
	/**
	 * 文章sid
	 */
	@FtsField("IR_SID")
	private String sid;

	/**
	 * 标题
	 */
	@FtsField(value = "IR_URLTITLE", highLight = true)
	private String title;
	public String getUrlTitle() {
		return title;
	}
	/**
	 * 正文
	 */
	@FtsField(value = "IR_CONTENT", highLight = true)
	private String content;

	/**
	 * 发布时间
	 */
	@FtsField("IR_URLTIME")
	private Date urlTime;

	/**
	 * 发布日期
	 */
	@FtsField("IR_URLDATE")
	private Date urlDate;

	/**
	 * 排重md5值
	 */
	@FtsField("MD5TAG")
	private String md5Tag;

	/**
	 * 地址
	 */
	@FtsField("IR_URLNAME")
	private String urlName;

	/**
	 * 站点名称
	 */
	@FtsField("IR_SITENAME")
	private String siteName;

	/**
	 * 情感值
	 */
	@FtsField("IR_APPRAISE")
	private String appraise;

	/**
	 * 分组名
	 */
	@FtsField("IR_GROUPNAME")
	private String groupName;

	/**
	 * 入库时间
	 */
	@FtsField("IR_LOADTIME")
	private Date loadTime;

	/**
	 * 频道
	 */
	@FtsField(value = "IR_CHANNEL", highLight = true)
	private String channel;

	/**
	 * 发帖人
	 */
	@FtsField("IR_AUTHORS")
	private String authors;

	/**
	 * 机构id
	 */
	@FtsField("ORGANIZATIONID")
	private String organizationId;

	/**
	 * 相似文章数
	 */
	private long sim;

	/**
	 * 相似文章数
	 */
	private int count;

	/**
	 * 收藏
	 */
	private Store store;

	/**
	 * 收藏Boolean型
	 */
	private Boolean favourite;

	/**
	 * 表达式
	 */
	private String trslk;

	/**
	 * 回帖数量
	 */
	private int replyCount;

	/**
	 * 是否发送 为预警加的
	 */
	private boolean send;
}
