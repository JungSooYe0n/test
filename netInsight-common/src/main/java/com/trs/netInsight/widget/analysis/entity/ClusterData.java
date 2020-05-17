package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 数据聚类实体
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "cluster_data")
public class ClusterData extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6572087828553073704L;

	@Column(name = "task_id")
	private String taskId;

	@Column(name = "sid")
	private String sid;
}
