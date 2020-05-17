package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 地域信息实体
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "district")
public class DistrictInfo extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6225998731657562428L;

	/**
	 * 地域名称
	 */
	@Column(name = "area_name")
	private String areaName;

	/**
	 * 地域类型
	 */

	@Column(name = "area_Type")
	private String areaType;

	/**
	 * 有参构造
	 * 
	 * @param areaName
	 * @param areaType
	 */
	public DistrictInfo(String areaName, String areaType) {
		super();
		this.areaName = areaName;
		this.areaType = areaType;
	}

}
