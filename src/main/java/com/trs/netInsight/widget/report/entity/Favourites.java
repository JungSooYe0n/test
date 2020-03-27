package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * 收藏表
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "favourites")
public class Favourites extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3375108782314184671L;

	/**
	 * 收藏表的主键id
	 */
	@Column(name = "favourites_id")
	private String favouritesId;

	/**
	 * 收藏的文章唯一标识id
	 */
	@Column(name = "sid")
	private String sid;

	/**
	 * 文章内容md5值
	 */
	@Column(name = "mds_tag")
	private String mdsTag;

	/**
	 * 素材库ID
	 */
	@Column(name = "library_id")
	private String libraryId;

	/**
	 * 链接
	 */
	@Column(name = "url_name")
	private String urlName;
	/**
	 * 作者
	 */
	@Column(name = "authors")
	private String authors;

	/**
	 * 标题
	 */
	@Column(name = "title")
	private String title;
	/**
	 * 正文
	 */
	@Column(name = "content")
	private String content;
	/**
	 * 微博用户昵称
	 */
	@Column(name = "screen_name")
	private String screenName;
	/**
	 * 原文时间-日期
	 */
	@Column(name = "url_date")
	private Date urlDate;

	/**
	 * 原文时间-时间
	 */
	@Column(name = "url_time")
	private Date urlTime;

	public Date getUrlTime() {
		return urlTime;
	}

	public void setUrlTime(Date urlTime) {
		this.urlTime = urlTime;
	}

	/**
	 * 站点
	 */
	@Column(name = "site_name")
	private String siteName;
	/**
	 * 原发媒体
	 */
	@Column(name = "src_name")
	private String srcName;
	/**
	 * 摘要
	 */
	@Column(name = "abstracts")
	private String abstracts;
	/**
	 * 区分论坛 主贴/回帖
	 */
	@Column(name = "nreserved1")
	private String nreserved1;
	/**
	 * 判断微博转发 / 原发
	 */
	@Column(name = "retweeted_mid")
	private String retweetedMid;
	/**
	 * 评论数
	 */
	@Column(name = "commt_count")
	private Long commtCount;
	/**
	 * 转发数
	 */
	@Column(name = "rtt_count")
	private Long rttCount;
	/**
	 * 是否发送过预警
	 */
	@Transient
	private boolean send;
	/**
	 * 是否已收藏
	 */
	@Transient
	private boolean favourite;
	/**
	 * 发布时间
	 */
	@Column(name = "create_at")
	private Date createdAt;
	/**
	 * 微博正文
	 */
	@Column(name = "status_content")
	private String statusContent;
	/**
	 * 文章标题
	 */
	@Column(name = "url_title")
	private String urlTitle;

	/**
	 * material 素材库
	 * collection  收藏
	 */
//	@Column(name = "data_type")
//	private String dataType;
//
	/**
	 * 来源 查库用
	 */
	private String groupName;
	
	/**
	 * 查询列表时拼builder
	 */
	private String urltime;

	public String getUrltime() {
		return urltime;
	}

	public void setUrltime(String urltime) {
		this.urltime = urltime;
	}

	/**
	 * 构造方法
	 *
	 * @param sid
	 * @param userId
	 */
	public Favourites(String sid, String userId,String subGroupId,String groupName,Date hyUrlTime,String urltime,String urlName,String md5,String authors,String title,String content,String screenName,Date urlDate
	,String siteName,String srcName,String abstracts,String retweetedMid,String nreserved1,long commtCount,long rttCount,boolean favourite,Date createdAt,String statusContent,String urlTitle) {
		this.favouritesId = GUIDGenerator.generate(Favourites.class);
		this.sid = sid;
		this.groupName=groupName;
		this.urlTime = hyUrlTime;
		this.urltime = urltime;
		this.urlName = urlName;
		this.mdsTag = md5;
		this.authors = authors;
		this.title = title;
		this.content=content;
		this.screenName = screenName;
		this.urlDate = urlDate;
		this.siteName = siteName;
		this.srcName = srcName;
		this.abstracts=abstracts;
		this.retweetedMid = retweetedMid;
		this.nreserved1 = nreserved1;
		this.commtCount = commtCount;
		this.rttCount = rttCount;
		this.send = send;
		this.favourite = favourite;
		this.createdAt = createdAt;
		this.statusContent = statusContent;
		this.urlTitle = urlTitle;


		super.setUserId(userId);
		super.setSubGroupId(subGroupId);
	}
	/**
	 * 构造方法
	 *
	 * @param sid
	 * @param userId
	 */
	public Favourites(String sid, String userId, String md5,String groupName) {
		this.favouritesId = GUIDGenerator.generate(Favourites.class);
		this.sid = sid;
		this.mdsTag = md5;
		this.groupName=groupName;
		super.setUserId(userId);
	}
	/**
	 * 构造方法
	 *
	 * @param sid
	 * @param userId
	 */
	public Favourites(String sid, String userId, String md5,String groupName,String urltime) {
		this.favouritesId = GUIDGenerator.generate(Favourites.class);
		this.sid = sid;
		this.mdsTag = md5;
		this.groupName=groupName;
		this.urltime = urltime;
		super.setUserId(userId);
	}
}
