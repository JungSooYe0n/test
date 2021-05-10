package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *  @author 北京拓尔思信息技术股份有限公司
 * Created by shao.guangze on 2018年5月29日 下午4:06:57
 * 日报周报月报放弃使用report_data,故对于日报周报月报而言，使用report_new中的template_new字段锁定展示内容
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "report_new")
public class ReportNew extends BaseEntity{
	
	private static final long serialVersionUID = 1L;
	
	@Column(name = "report_name")
	private String reportName;
	
	@Column(name = "total_issue")
	private String totalIssue;
	
	@Column(name = "this_issue")
	private String thisIssue;
	
	@Column(name = "preparation_units")
	private String preparationUnits;
	
	@Column(name = "preparation_authors")
	private String preparationAuthors;
	
	@Column(name = "template_id")
	private String templateId;
	
	/**
	 * 统计时间
	 * */
	@Column(name = "statistics_time")
	private String statisticsTime;
	
	@Column(name = "report_data", columnDefinition="TEXT")
	private String reportDataId;
	
	/**
	 * 是否删除资源
	 * 0删除资源；1保留资源
	 * */
	@Column(name = "resource_deleted")
	private Integer resourceDeleted;
	
	@Column(name = "report_type")
	private String reportType;
	
	@Column(name = "doc_path")
	private String docPath;

	@Column(name = "temp_doc_path")
	private String tempDocPath;

	//专报分组
	@Column(name = "group_name")
	private String groupName;
	
	/**
	 * 专报关键词或检索语句
	 * */
	@Column(name = "special_trsl")
	private String specialTrsl;

	/**
	 * 0  正在查询
	 * 1  查询完成
	 * */
	@Column(name = "done_flag")
	private Integer doneFlag;

	/**
	 * 模板列表，解决报告列表预览时，因为模板后来遭到更改，导致预览数据有误
	 * */
	@Column(name = "template_list", columnDefinition="TEXT")
	private String templateList;

	/**
	 * 当前报告是否可以预览，因为老报告模板改变了，不让预览了
	 * 老报告没有这个字段是空
	 * */
	@Column(name = "preview_flag")
	private Boolean previewFlag = true;
	public Boolean getPreviewFlag(){
		if(previewFlag != null ){
			return previewFlag;
		}else{
			return false;
		}
	}


	
	public ReportNew(Builder builder){
		this.reportName = builder.reportName;
		this.totalIssue = builder.totalIssue;
		this.thisIssue = builder.thisIssue;
		this.preparationUnits = builder.preparationUnits;
		this.preparationAuthors = builder.preparationAuthors;
		this.templateId = builder.templateId;
		this.statisticsTime = builder.statisticsTime;
		this.reportDataId = builder.reportDataId;
		this.resourceDeleted = builder.resourceDeleted;
		this.reportType = builder.reportType;
		this.groupName = builder.groupName;
		this.specialTrsl = builder.specialTrsl;
		this.templateList = builder.templateList;
	}
	
	public static class Builder{
		private String reportName;
		
		private String totalIssue;
		
		private String thisIssue;
		
		private String preparationUnits;
		
		private String preparationAuthors;
		
		private String templateId;
		
		private String statisticsTime;
		
		private String reportDataId;
		
		private String reportType;
		
		private String groupName;
		
		private String specialTrsl;
		
		private String templateList;
		/**
		 * 是否删除资源
		 * 0删除资源；1保留资源
		 * */
		private Integer resourceDeleted;
		
		public Builder withReportName(String reportName){
			this.reportName = reportName;
			return this;
		}
		public Builder withTotalIssue(String totalIssue){
			this.totalIssue = totalIssue;
			return this;
		}
		public Builder withThisIssue(String thisIssue){
			this.thisIssue = thisIssue;
			return this;
		}
		public Builder withPreparationUnits(String preparationUnits){
			this.preparationUnits = preparationUnits;
			return this;
		}
		public Builder withPreparationAuthors(String preparationAuthors){
			this.preparationAuthors = preparationAuthors;
			return this;
		}
		public Builder withTemplateId(String templateId){
			this.templateId = templateId;
			return this;
		}
		public Builder withStatisticsTime(String statisticsTime){
			this.statisticsTime = statisticsTime;
			return this;
		}
		public Builder withReportDataId(String reportDataId){
			this.reportDataId = reportDataId;
			return this;
		}
		public Builder withResourceDeleted(Integer resourceDeleted){
			this.resourceDeleted = resourceDeleted;
			return this;
		}
		public Builder withReportType(String reportType){
			this.reportType = reportType;
			return this;
		}
		public Builder withGroupName(String groupName){
			this.groupName = groupName;
			return this;
		}
		public Builder withSpecialTrsl(String specialTrsl){
			this.specialTrsl = specialTrsl;
			return this;
		}
		public Builder withTemplateList(String templateList){
			this.templateList = templateList;
			return this;
		}
		public ReportNew build(){
			return new ReportNew(this);
		}
		
	}
}