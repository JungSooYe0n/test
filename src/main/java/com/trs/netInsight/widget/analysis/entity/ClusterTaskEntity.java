package com.trs.netInsight.widget.analysis.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 聚类任务表
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "cluster_task")
public class ClusterTaskEntity extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9214268902938228417L;

	@Column(name = "type")
	private String type;

	@Column(length = 2000)
	private String trsl;

	@Column(name = "link_id")
	private String linkId;

	@Column(name = "submit_time")
	private Date submitTime;

}
