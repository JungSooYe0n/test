package com.trs.netInsight.widget.special.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 专题指数实体
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月3日
 *
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "special_exponent")
public class SpecialExponent extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5373932795606106604L;
	
	/**
	 * 专题id
	 */
	@Column(name = "special_id")
	private String specialId;
	
	/**
	 * 专题名称
	 */
	@Column(name = "special_name")
	private String specialName;
	
	/**
	 * 热度值
	 */
	@Column(name = "hot_degree")
	private long hotDegree;
	
	/**
	 * 网民参与度
	 */
	@Column(name = "netizen_degree")
	private long netizenDegree;
	
	/**
	 * 媒体参与度
	 */
	@Column(name = "meta_degree")
	private long metaDegree;
	
	/**
	 * 指数计算时间(formate : yyyy-MM-dd)
	 */
	@Column(name = "compute_time")
	private Date computeTime;
	
	@Temporal(TemporalType.DATE)
	public Date getComputeTime(){
		return computeTime;
	}

	public SpecialExponent(String specialId, String specialName, long hotDegree, long netizenDegree, long metaDegree,
			Date computeTime) {
		super();
		this.specialId = specialId;
		this.specialName = specialName;
		this.hotDegree = hotDegree;
		this.netizenDegree = netizenDegree;
		this.metaDegree = metaDegree;
		this.computeTime = computeTime;
	}

	@Override
	public String toString() {
		return "SpecialExponent [specialId=" + specialId + ", specialName=" + specialName + ", hotDegree=" + hotDegree
				+ ", netizenDegree=" + netizenDegree + ", metaDegree=" + metaDegree + ", computeTime=" + computeTime
				+ "]";
	}
	
	
	
}
