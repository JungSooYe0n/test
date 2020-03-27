package com.trs.netInsight.widget.special.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 专题分析指数映射对象
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月4日
 *
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SpecialExponentVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 391996005104590490L;

	/**
	 * 专题id
	 */
	private String specialId;

	/**
	 * 专题名称
	 */
	private String specialName;

	/**
	 * 热度
	 */
	private Long hotDegree;

	/**
	 * 媒体参与度
	 */
	private Long metaDegree;

	/**
	 * 网民参与度
	 */
	private Long netizenDegree;
	
	/**
	 * 开始时间
	 */
	private String beginTime;
	
	/**
	 * 结束时间
	 */
	private String endTime;
}
