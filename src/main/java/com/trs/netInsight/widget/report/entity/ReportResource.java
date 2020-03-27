package com.trs.netInsight.widget.report.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 报告资源表
 *
 * Create by shao.guangze on 2018年5月17日
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "report_resource")
public class ReportResource extends BaseEntity {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3525513008518315850L;

	/**
	 * 报告资源表的主键id
	 */
	/*@Column(name = "resource_id")
	private String resourceId;*/

	/**
	 * 为拖拽设计，记录每条信息在该模块中的位置
	 */
	@Column(name = "doc_position" ,columnDefinition = "INT default 0")
	private Integer docPosition = 0;
	
	/**
	 * 收藏的文章唯一标识id
	 */
	@Column(name = "sid")
	private String sid;

	/**
	 * 文章内容md5值
	 */
	@Column(name = "md5_tag")
	private String md5Tag;

	/**
	 * 素材库ID
	 */
	@Column(name = "library_id")
	private String libraryId;
	
	/**
	 * 来源 查库用
	 */
	@Column(name = "groupName")
	private String groupName;
	/**
	 * 一级标题，该资源所属章节
	 * */
	@Column(name = "chapter")
	private String chapter;

	/***
	 * 现章节可重复，但添加报告资源时做了排重处理，故此加入"章节位置"锁定章节
	 */
	@Column(name = "chapter_position")
	private Integer chapterPosition;
	/**
	 * 二级标题
	 * */
	@Column(name = "SecondaryChapter")
	private String SecondaryChapter;
	/**
	 * 图片资源
	 * */
	@Column(name = "img_data", columnDefinition="TEXT")
	private String img_data;
	
	@Column(name = "img_comment", columnDefinition="TEXT")
	private String imgComment;
	
	@Column(name = "img_type")
	private String imgType;

	/**
	 * 针对新闻类 报告下载 标题+摘要类
	 */
	@Column(name = "news_abstract", columnDefinition="TEXT")
	private String newsAbstract;

	/**
	 * 文章地址
	 */
	@Column(name = "urlName")
	private String urlName;

	/***
	 * 0:单条文字
	 * 1:列表资源
	 * 2:图表资源
	 */
	/*@Column(name = "resource_type")
	private Integer resourceType;*/

	/***
	 * 使用途径，报告列表页面预览 或者 查询所有报告资源准备生成报告
	 */
	@Column(name = "useage")
	private Integer useage;
	/**
	 * 标题或微博正文
	 * */
	@Column(name = "title" , columnDefinition="TEXT")
	private String title;
	/**
	 * 正文或摘要
	 * */
	@Column(name = "content" , columnDefinition="TEXT")
	private String content;
	/**
	 * 来源网站或微博来源账号
	 * */
	@Column(name = "site_name")
	private String siteName;
	/**
	 * 传统媒体的原发
	 * */
	@Column(name = "src_name")
	private String srcName;
	/**
	 * 信息发布时间
	 * */
	@Column(name = "url_date")
	private Date urlDate;
	/**
	 * 转发数
	 * */
	@Column(name = "rtt_count")
	private Long rttCount;
	/**
	 * 媒体类型
	 */
	@Column(name = "media_type")
	private String mediaType;
	/**
	 * 原发媒体
	 * */
	/*@Column(name = "origin_media")
	private String originMedia;*/
	/**
	 * 报告类型，日报周报月报
	 * */
	@Column(name = "report_type")
	private String reportType;
	
	/**
	 * 锁定哪一个报告，供二次编辑或预览的时候使用
	 * */
	@Column(name = "report_id")
	private String reportId;
	
	@Column(name = "template_id")
	private String templateId;
	
	@Column(name = "time_ago")
	private String timeAgo;

	/***
	 * 0 报告资源预览页显示内容
	 * 1 报告列表点击预览显示内容
	 */
	@Column(name = "resource_status")
	private Integer resourceStatus;

	/**
	 *   为热点微博、热点新闻的热度值
	 */
	@Column(name = "sim_count")
	private String simCount;
	/***
	 * 0 正在查
	 * 1 已完成
	 */
	//@Column(name = "done_flag")
	//private Integer doneFlag;
	/**
	 * 构造方法
	 *
	 * @param sid
	 * @param userId
	 */
	public ReportResource(String sid, String userId, String groupName,String chapter,String img_data) {
		/*this.resourceId = GUIDGenerator.generate(ReportResource.class);*/
		this.sid = sid;
		this.groupName=groupName;
		this.chapter = chapter;
		this.img_data = img_data;
		super.setUserId(userId);
	}
	/**
	 * 构造方法
	 *
	 * @param sid
	 * @param userId
	 */
	public ReportResource(String sid, String userId, String groupName,String chapter) {
		this.sid = sid;
		this.groupName=groupName;
		this.chapter = chapter;
		super.setUserId(userId);
	}
}
