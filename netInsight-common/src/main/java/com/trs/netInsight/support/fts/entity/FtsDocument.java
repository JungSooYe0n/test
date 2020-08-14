package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.enums.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 传统媒体对应实体
 *
 * Created by xiaoying on 2017年11月23日
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient
public class FtsDocument extends IDocument implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3380660854481832945L;

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
	 * 摘要
	 */
	@FtsField(value = "IR_ABSTRACT", highLight = true)
	private String abstracts;

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
	 * 地址层级
	 */
	@FtsField("IR_URLLEVEL")
	private int urlLevel;

	/**
	 * 关键词
	 */
	@FtsField("IR_KEYWORDS")
	private List<String> keywords;

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
	 * 图片标记
	 */
	@FtsField("IR_IMAGEFLAG")
	private int imageFlag;
	
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
	
	@FtsField("IR_SRCNAME")
	private String srcName;

	/**
	 * 主贴 0 /回帖 1
	 */
	@FtsField("IR_NRESERVED1")
	private String nreserved1;

	/**
	 * 主贴和回帖的关联关系
	 */
	@FtsField("IR_HKEY")
	private String hKey;

	/**
	 * 论坛 楼层
	 */
	@FtsField("IR_BBSNUM")
	private int bbsNum;
	/**
	 * 微博 原发  转发
	 */
	@FtsField("IR_RETWEETED_MID")
	private String retweetedMid;
	
	/**
	 * 频道
	 */
	@FtsField("IR_CHANNEL_INDUSTRY")
	private String channelIndustry;

	/**
	 * 舆情分类==敏感分类
	 */
	@FtsField("IR_CATALOG2")
	private String sensitiveType;
	
	/**
	 * wxb白名单
	 */
	@FtsField("IR_WXB_LIST")
	private String wxbList;

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
	 * 数据入hybase库的时间
	 */
	@FtsField("HYBASE_LOADTIME")
	private Date HyLoadTime;

	/**
	 * 地域
	 */
	@FtsField("CATALOG_AREA")
	private String catalogArea;

//	@FtsField("IR_VRESERVED1")
//	private String vreserved1;
//	@FtsField("IR_VRESERVED2")
//	private String vreserved2;
//	private String vreserved;
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
	private int  replyCount;
	
	/**
	 * 是否发送  为预警加的
	 */
	private boolean send;
	
	/**
	 * 文章中图片路径
	 */
	private String imgSrc;

	/**
	 * 命中句，包含关键词的语句
	 */
	private String hit;

	/**
	 * 命中词，被标红的字
	 */
	private String hitWord;

	/**
	 * 版面位置1  电子报专属
	 */
	@FtsField("IR_VRESERVED1")
	private String vreserved1;

	/**
	 * 版面位置2  电子报专属
	 */
	@FtsField("IR_VRESERVED2")
	private String vreserved2;

	/**
	 * 电子报专属  vreserved1 + vreserved2 确定一个位置
	 */
	private String vreserved;

	public String getVreserved(){
		if(StringUtil.isEmpty(this.vreserved1) || StringUtil.isEmpty(this.vreserved2)){
			return null;
		}
		return this.vreserved1+" "+this.vreserved2;
	}

	public String getSrcName() {
		if (StringUtil.isNotEmpty(srcName) && srcName.length() > 30) {
			return  srcName.substring(0,30);
		}else {
			return srcName;
		}
	}
	/**
	 * 已读字段(多值---针对用户)
	 */
	@FtsField("IR_READ")
	private String read;

	//    前端识别已读/未读标
	private boolean readFlag;

	public boolean isReadFlag() {
		String userId = UserUtils.getUser().getId();
		if (StringUtil.isNotEmpty(read) && read.contains(userId)){
			return true;
		}
		return false;
	}
}
