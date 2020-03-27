package com.trs.netInsight.widget.report.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 报告
 * @Type ReportData.java
 * @Desc 
 * @author 谷泽昊
 * @date 2017年11月24日 下午4:42:43
 * @version
 */
@Getter
@Setter
@Entity
@Table(name = "report_data")
public class ReportData extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 模板id
	 */
	@Column(name = "template_id")
	private String templateId;

	/**
	 * 报告id
	 */
	@Column(name = "report_id")
	private String reportId;

	/**
	 * 模板路径
	 */
	@Column(name = "template_path")
	private String templatePath;

	/**
	 * 报告头（名称，时间）
	 */
	@Column(name = "report_top_data", columnDefinition = "TEXT")
	private String reportTopData;

	/**
	 * 报告简介
	 */
	@Column(name = "report_intro", columnDefinition = "TEXT")
	private String reportIntro;

	/**
	 * 监测概述
	 */
	@Column(name = "monitor_summarize", columnDefinition = "TEXT")
	private String monitorSummarize;

	/**
	 * 地域分布图数据
	 */
	@Column(name = "areal_distribution_data", columnDefinition = "TEXT")
	private String arealDistributionData;

	/**
	 * 来源类型分析数据
	 */
	@Column(name = "source_type_data", columnDefinition = "TEXT")
	private String sourceTypeData;

	/**
	 * 媒体活跃度数据
	 */
	@Column(name = "media_activity_data", columnDefinition = "TEXT")
	private String mediaActivityData;

	/**
	 * 媒体扩散图数据
	 */
	@Column(name = "media_diffuse_data", columnDefinition = "TEXT")
	private String mediaDiffuseData;

	/**
	 * 情感分析图数据
	 */
	@Column(name = "emotion_analysis", columnDefinition = "TEXT")
	private String emotionAnalysis;

	/**
	 * 热词分布数据
	 */
	@Column(name = "hot_word_data", columnDefinition = "TEXT")
	private String hotWordData;

	/**
	 * 热点地名分布数据
	 */
	@Column(name = "hotPlace_data", columnDefinition = "TEXT")
	private String hotPlaceData;

	/**
	 * 热点机构分布数据
	 */
	@Column(name = "hotOrgan_data", columnDefinition = "TEXT")
	private String hotOrganData;

	/**
	 * 热点人名分布
	 */
	@Column(name = "hotName_data", columnDefinition = "TEXT")
	private String hotNameData;

	/**
	 * 声量趋势图数据
	 */
	@Column(name = "volume_data", columnDefinition = "TEXT")
	private String volumeData;

	/**
	 * 引爆点数据
	 */
	@Column(name = "boom_data", columnDefinition = "TEXT")
	private String boomData;

	/**
	 * 舆情指数客户图数据
	 */
	@Column(name = "exponent_data", columnDefinition = "TEXT")
	private String exponentData;

	/**
	 * 最热新闻列表数据
	 */
	@Column(name = "hottest_data", columnDefinition = "TEXT")
	private String hottestData;

	/**
	 * 最新新闻列表数据
	 */
	@Column(name = "newest_data", columnDefinition = "TEXT")
	private String newestData;

}
