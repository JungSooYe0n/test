package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
	 * 论坛回帖hkey
	 */
	@Column(name = "hkey")
	private String hkey;

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
	@Column(name = "url_name", columnDefinition="TEXT")
	private String urlName;
	/**
	 * 作者
	 */
	@Column(name = "authors")
	private String authors;

	@Column(name = "author")
	private String author;

	/**
	 * 标题
	 */
	@Column(name = "title", columnDefinition="TEXT")
	private String title;
	/**
	 * 正文
	 */
	@Column(name = "content", columnDefinition="TEXT")
	private String content;
	/**
	 * 全部的正文（去掉标签和特殊符号的）
	 */
	@Column(name = "full_content", columnDefinition="TEXT")
	private String fullContent;
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
	 * 原发媒体 - 传统媒体或微信时为原发媒体srcname，如果是微博和TF则是原发人的昵称
	 */
	@Column(name = "src_name")
	private String srcName;
	/**
	 * 摘要
	 */
	@Column(name = "abstracts", columnDefinition="TEXT")
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
	@Column(name = "status_content", columnDefinition="TEXT")
	private String statusContent;
	/**
	 * 文章标题
	 */
	@Column(name = "url_title", columnDefinition="TEXT")
	private String urlTitle;

	/**
	 * 正负面
	 */
	@Column(name = "appraise")
	private String appraise;

	/**
	 * 关键词
	 */
	@Column(name = "keywords")
	private String keywords;
	public void setKeywords(List<String> list){
		if(list!= null && list.size() >0){
			keywords = StringUtils.join(list,";");
		}
	}
	public List<String> getKeywords(){
		if(StringUtil.isNotEmpty(keywords)){
			return Arrays.asList(keywords.split(";"));
		}else{
			return null;
		}
	}

	/**
	 * 频道
	 */
	@Column(name = "channel")
	private String channel;

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
	@Column(name = "group_name")
	private String groupName;
	
	/**
	 * 查询列表时拼builder
	 */
	@Column(name = "urltime")
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
		this.title = StringUtil.filterEmoji(StringUtil.replaceImg(title));
		this.content=StringUtil.filterEmoji(StringUtil.replaceImg(content));
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
		this.statusContent = StringUtil.filterEmoji(StringUtil.replaceImg(statusContent));
		this.urlTitle = StringUtil.filterEmoji(StringUtil.replaceImg(urlTitle));


		super.setUserId(userId);
		super.setSubGroupId(subGroupId);
	}
	/**
	 * 构造方法
	 *
	 * @param sid
	 * @param userId
	 */
	public Favourites(String sid,String userId,String subGroupId) {
		this.favouritesId = GUIDGenerator.generate(Favourites.class);
		this.sid = sid;
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
