package com.trs.netInsight.widget.report.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by shao.guangze on 2018年5月24日 下午5:43:09
 */
@Getter
@Setter
public class TElementNew {
	
	private String chapterName;
	private Integer chapterPosition;
	//0未选中，1选中
	private Integer selected;
	//详细见Enum Chapter valueType	==> SingleResource,ListResources,chart
	private String chapterType;
	//供查询所有模板时，不同章节预览时显示不同内容，
	private String chapterDetail;
	//额外的章节标志。页面上自定义模板时，对于新增的章节，是可以选择删除的，前端需要1个flag。
	//0 默认，1 额外的章节标志。
	private Integer extraChapterFlag = 0;
	
	private List<ReportResource> chapaterContent;

	/**
	 * 章节内容 列表类 是按列表展示 list 还是按表格展示 table
	 * 				列表 分 微博  标题（sitename）+正文（content)  list_1
	 * 			           微信  只有 标题+正文   list_1
	 * 			           新闻  标题+正文  list_1
	 * 			                标题+摘要  list_2
	 * 			                标题+摘要  并以附件形式 放到word list_3
	 * 		   图表类 是按 柱状图 bar、折线图line、还是饼图 pie
	 */
	private String elementNewType;

	public TElementNew(String chapterName, Integer selected ,Integer chapterPosition){
		this.chapterName = chapterName;
		this.selected = selected;
		this.chapterPosition = chapterPosition;
	}
	public TElementNew(){}
}
