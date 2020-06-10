package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Created by shao.guangze on 2018年5月30日 上午9:52:53
 *
 * Changed by shao.guangze on 2018年7月12日 下午4:52:53
 * 该实体现只有专报使用，除此之外，旧版的已生成的日报周报月报也还仍使用该实体
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "report_data_new")
public class ReportDataNew extends BaseEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 报告简介
	 * */
	@Column(name = "report_intro")
	private String reportIntro;
	/**
	 * 数据统计概述
	 * */
	@Column(name = "overview_ofdata", columnDefinition = "text")
	private String overviewOfdata;
	/**
	 * 新闻网站TOP10
	 * */
	@Column(name = "news_top10", columnDefinition = "mediumtext")
	private String newsTop10;
	/**
	 * 微博TOP10
	 * */
	@Column(name = "weibo_top10", columnDefinition = "mediumtext")
	private String weiboTop10;
	/**
	 * 微信TOP10
	 * */
	@Column(name = "wechat_top10", columnDefinition = "mediumtext")
	private String wechatTop10;
	/**
	 * 数据趋势分析
	 * */
	@Column(name = "data_trend_analysis", columnDefinition = "mediumtext")
	private String dataTrendAnalysis; 
	/**
	 * 数据来源对比
	 * */
	@Column(name = "data_source_analysis", columnDefinition = "mediumtext")
	private String dataSourceAnalysis;
	/**
	 * 网站来源TOP10
	 * */
	@Column(name = "website_source_top10", columnDefinition = "mediumtext")
	private String websiteSourceTop10;
	/**
	 * 微博活跃用户TOP10
	 * */
	@Column(name = "weibo_active_top10", columnDefinition = "mediumtext")
	private String weiboActiveTop10;
	/**
	 * 微信活跃用户TOP10
	 * */
	@Column(name = "wechat_active_top10", columnDefinition = "mediumtext")
	private String wechatActiveTop10;

	/**
	 * 自媒体号
	 * */
	@Column(name = "we_media_hot", columnDefinition = "mediumtext")
	private String weMediaHot;
	/**
	 * 全国地域分布
	 * */
	@Column(name = "area", columnDefinition = "mediumtext")
	private String area;
	/**
	 * 情感分析
	 * */
	@Column(name = "emotion_analysis", columnDefinition = "mediumtext")
	private String emotionAnalysis;
	/**
	 * 新闻热点话题
	 * */
	@Column(name = "news_hot_topics", columnDefinition = "mediumtext")
	private String newsHotTopics;
	/**
	 * 微博热点话题
	 * */
	@Column(name = "weibo_hot_topics", columnDefinition = "mediumtext")
	private String weiboHotTopics;

	/**
	 * 微信热点TOP10（专题报 改造 20191121）
	 */
	@Column(name = "wechat_hot_top10", columnDefinition = "mediumtext")
	private String wechatHotTop10;

	/**
	 * 态势评估
	 */
	@Column(name = "situation_accessment", columnDefinition = "mediumtext")
	private String situationAccessment;

	/**
	 * 观点分析
	 */
	@Column(name = "opinion_analysis", columnDefinition = "mediumtext")
	private String opinionAnalysis;

	/**
	 *  情绪统计
	 */
	@Column(name = "mood_statistics", columnDefinition = "mediumtext")
	private String moodStatistics;

	/**
	 *  词云统计
	 */
	@Column(name = "word_cloud_statistics", columnDefinition = "mediumtext")
	private String wordCloudStatistics;

	/**
	 * 添加事件脉络 - 在word中走列表
	 */
	/**
	 * 微信事件脉络
	 */
	@Column(name = "wechat_event_context", columnDefinition = "mediumtext")
	private String wechatEventContext;

	/**
	 * 微博事件脉络
	 */
	@Column(name = "weibo_event_context", columnDefinition = "mediumtext")
	private String weiboEventContext;

	/**
	 * 新闻网站事件脉络
	 */
	@Column(name = "news_event_context", columnDefinition = "mediumtext")
	private String newsEventContext;

	/**
	 * 自媒体号事件脉络
	 */
	@Column(name = "wemedia_event_context", columnDefinition = "mediumtext")
	private String wemediaEventContext;

	/**
	 * 活跃账号
	 */
	@Column(name = "active_account", columnDefinition = "mediumtext")
	private String activeAccount;
	/**
	 * 极简模式  自定义模块
	 */
	@Transient
	private String customModule;
		/**
	 * 0表示正在查询数据；1表示数据全部查询完毕
	 * */
	@Column(name = "done_flag")
	private Integer doneFlag;
}
