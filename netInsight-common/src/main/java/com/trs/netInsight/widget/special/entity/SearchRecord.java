package com.trs.netInsight.widget.special.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 记录普通搜索关键词
 * Created by Xiaoying on 2018/11/26.
 *
 */
@Entity
@Setter
@Getter
@Table(name = "search_record")
public class SearchRecord extends BaseEntity{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5099687624751672216L;
	
	public SearchRecord(String keywords){
		this.keywords = keywords;
	}
	public SearchRecord(){}
	
	/**
	 * 关键词
	 */
	@Column(name = "`keywords`")
	private String keywords;

}
