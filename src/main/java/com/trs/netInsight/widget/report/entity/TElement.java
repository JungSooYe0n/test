package com.trs.netInsight.widget.report.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TElement implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2613970713499926090L;

	/**
	 * 对应id
	 */
	private String id;
	
	/**
	 * 元素标题
	 */
	private String title;
	
	/**
	 * 元素类型，文本，表格或者图片
	 */
	private String eleType;
	
	/**
	 * 检索来源（全部、微博、微信、论坛...）
	 */
	private String source;
	
	/**
	 * 图类型（情感分析图，地域分布图....）
	 */
	private String imgType;
	
	/**
	 * 图表展示类型
	 */
	private String imgStyle;
	
	/**
	 * 文本内容
	 */
	private String textContent;
	
	
	
}
