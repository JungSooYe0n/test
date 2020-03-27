package com.trs.netInsight.widget.analysis.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 观点聚类结果表
 * Created by even on 2017/5/10.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "task_result")
public class TaskResult extends BaseEntity{


    /**
	 * 
	 */
	private static final long serialVersionUID = 8950434661996628007L;

	@Column(name = "task_id")
    private String taskId;

    /**
     * 数据ID
     */
    @Column(name = "sid")
    private String sid;

    /**
     * 热度值
     */
    @Column(name = "hot_num")
    private int hotNum;


}
