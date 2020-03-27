package com.trs.netInsight.widget.alert.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alert_backups")
public class AlertBackups extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 文章 id
	 */
	@Column(name = "sid")
	private String sid;

	/**
	 * 文章标题
	 */
	@Column(name = "title", columnDefinition = "LONGTEXT")
	private String title;

	/**
	 * 文章正文 150字内
	 */
	@Column(name = "content", columnDefinition = "LONGTEXT")
	private String content;

	/**
	 * 文章链接
	 */
	@Column(name = "url_name",length=2550)
	private String urlName;

	/**
	 * 文章时间
	 */
	@Column(name = "time")
	private Date time;

	/**
	 * 文章站点
	 */
	@Column(name = "site_name")
	private String siteName;

	/**
	 * 来源
	 */
	@Column(name = "group_name")
	private String groupName;

	/**
	 * 规则id
	 */
	@Column(name = "alert_rule_backups_id")
	private String alertRuleBackupsId;

	/**
	 * 发送方式 自动 手动
	 */
	@Column(name = "alert_source")
	private AlertSource alertSource;
	/**
	 * 评论数
	 */
	@Column(name = "commt_count")
	private long commtCount;

	/**
	 * 转发数
	 */
	@Column(name = "rtt_count")
	private long rttCount;

	/**
	 * 用户昵称
	 */
	@Column(name = "screen_name")
	private String screenName;

	/**
	 * 正负面
	 */
	@Column(name = "appraise")
	private String appraise;

	/**
	 * 接收人
	 */
	@Column(name = "receiver")
	private String receiver;

	/**
	 * 发送方式 邮件 站内 微信
	 */
	@Column(name = "send_way")
	private SendWay sendWay;

	/**
	 * 主贴 0 回帖1
	 */
	@Column(name = "nreserved1")
	private String nreserved1;

	/**
	 * md5值 异步算相似文章数时用
	 */
	@Column(name = "md5tag")
	private String md5tag;

	/**
	 * APP页面展示使用
	 */
	@Column(name ="image_url")
	private String imageUrl;

	/**
	 * 作者
	 */
	@Column(name ="author")
	private String author;
}
