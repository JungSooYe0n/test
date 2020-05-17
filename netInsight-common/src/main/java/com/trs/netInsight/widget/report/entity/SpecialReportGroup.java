package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by shao.guangze on 2018年6月12日 下午7:32:08
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "special_report_group")
public class SpecialReportGroup extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name = "group_name")
	private String groupName;

}
