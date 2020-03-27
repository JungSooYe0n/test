package com.trs.netInsight.widget.special.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 被删除的数据
 *
 * Created by ChangXiaoyang on 2017/5/5.
 */
@Entity
@Setter
@Getter
@Table(name = "junk_data")
public class JunkData extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1218762133967937969L;

	/**
	 * 专题id
	 */
	@Column(name = "`special_id`")
	private String specialId;
	
	/**
	 * 文章sid
	 */
	@Column(name = "`sid`")
	private String sid;


}
