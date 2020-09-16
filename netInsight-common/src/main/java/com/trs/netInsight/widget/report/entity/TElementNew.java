package com.trs.netInsight.widget.report.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by shao.guangze on 2018年5月24日 下午5:43:09
 */
@Getter
@Setter
public class TElementNew {

	/**
	 * 当前模块的中文名字
	 */
	private String chapterName;
	/**
	 * 模块位置，也是顺序
	 */
	private Integer chapterPosition;
	//0未选中，1选中
	private Integer selected;
	//0未选中，1选中
	private Boolean active = true;
	public Boolean getActive(){
		if(selected != null && selected ==1){
			active = true;
		}else{
			active = false;
		}
		return active;
	}


	//详细见Enum Chapter valueType	==> SingleResource,ListResources,chart
	private String chapterType;
	//供查询所有模板时，不同章节预览时显示不同内容，
	/**
	 * 当前模块的英文名字
	 */
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
