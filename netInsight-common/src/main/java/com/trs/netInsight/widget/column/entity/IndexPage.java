package com.trs.netInsight.widget.column.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * 一二级栏目实体类
 * 
 * @author xiaoying
 *
 */
@Entity(name = "index_page")
@Table
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IndexPage extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3511154541081924351L;

	/**
	 * 一级栏目ID
	 */
	@Column(name = "parent_id")
	private String parentId;

	/**
	 * 一级栏目名
	 */
	@Column(name = "parent_name")
	private String parentName;

	/**
	 * 是否隐藏
	 */
	@Column(name = "hide")
	private boolean hide;

	/**
	 * 自定义类型导航栏的id
	 */
	@Column(name = "type_id")
	private String typeId;

	/**
	 * 排序
	 */
	@Column(name = "sequence", columnDefinition = "INT default 0")
	private int sequence;

	/**
	 * 机构内共享
	 */
	@Column(name = "share")
	private boolean share = false;
	
	/**
	 * 是不是排序过的数据  after代表排序过的或者是新数据  否则是老数据
	 */
	@Column(name = "order_before")
	private String  orderBefore = "after";

	/**
	 * api返回使用,无须持久化
	 */
	@Transient
	private List<IndexTab> indexTabs;
	
	/**
	 * 截断关联关系
	 */
	@Transient
	private List<IndexTabMapper> indexTabMappers;
	
	/**
	 * 有参构造函数
	 * 
	 * @param parentName
	 */
	public IndexPage(String parentName, String parentId) {
		this.parentName = parentName;
		// 默认不隐藏
		this.hide = false;
		// this.sonId=sonId;
		// this.sonName=sonName;
	}

	/**
	 * 复制page
	 * 
	 * @date Created at 2018年3月22日 上午11:12:27
	 * @Author 谷泽昊
	 * @return
	 */
	public IndexPage pageCopy() {
		return new IndexPage(parentId, parentName, hide, typeId, sequence);
	}

	public IndexPage(String parentId, String parentName, boolean hide, String typeId, int sequence) {
		super();
		this.parentId = parentId;
		this.parentName = parentName;
		this.hide = hide;
		this.typeId = typeId;
		this.sequence = sequence;
	}
}
