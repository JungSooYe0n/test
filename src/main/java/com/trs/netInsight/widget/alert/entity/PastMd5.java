package com.trs.netInsight.widget.alert.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
/**
 * 热度值预警时  存储该规则已发送过的md5
 * @author xiaoying
 * 2018.8.27
 */
@Entity
@Table(name = "past_md5")
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PastMd5 extends BaseEntity{

	/**
	 * 备份规则的id
	 */
	@Column(name="rule_back_id")
	private String ruleBackId;
	
	/**
	 * 这个规则已经发送过的md5值
	 */
	@Column(name="md5")
	private String md5;
}
