package com.trs.netInsight.support.autowork.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 调度任务实体
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月4日
 *
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task")
public class Task extends BaseEntity {
	/**
	* 
	*/
	private static final long serialVersionUID = -5264864955926933306L;

	/**
	 * 任务名称
	 */
	@Column(name = "task_name")
	private String taskName;

	/**
	 * 任务业务类绝对路径
	 */
	@Column(name = "source")
	private String source;

	/**
	 * 调度频率,使用cron表达式
	 */
	@Column(name = "`cron`")
	private String cron;

	/**
	 * 描述
	 */
	@Column(name = "`remark`")
	private String remark;

	/**
	 * 是否开启
	 */
	@Column(name = "state")
	private boolean state = false;

}
