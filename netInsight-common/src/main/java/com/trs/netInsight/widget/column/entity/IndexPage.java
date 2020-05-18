package com.trs.netInsight.widget.column.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

/**
 * 栏目分组类
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
	 * 子类
	 */
	@JsonIgnore
	@OneToMany(cascade={CascadeType.REFRESH, CascadeType.REMOVE}, fetch = FetchType.EAGER /*fetch = FetchType.LAZY*/)
	@JoinColumn(name = "parent_id")
	@OrderBy("sequence asc")
	private List<IndexPage> childrenPage = new ArrayList<>();

	@Column(name = "parent_id")
	private String parentId;

	/**
	 * 栏目名
	 */
	@Column(name = "name")
	private String name;

	@Column(name = "parent_name")
	private String parentName;

	/**
	 * 是否隐藏
	 */
	@Column(name = "hide")
	private boolean hide;

	/**
	 * 自定义类型导航栏的id，如果为默认为“”
	 */
	@Column(name = "type_id")
	private String typeId;

	/**
	 * 排序
	 */
	@Column(name = "sequence", columnDefinition = "INT default 0")
	private Integer sequence;

	/**
	 * 机构内共享
	 */
	@Column(name = "share")
	private boolean share = false;


	@Column(name = "order_before")
	private String  orderBefore = "after";

	/**
	 * api返回使用,无须持久化
	 */
	@Transient
	private List<IndexTab> indexTabs;

	/**
	 * 标识是分组还是栏目  -  前端使用
	 */
	@Transient
	private Integer flag = ColumnFlag.IndexPageFlag.ordinal();

	/**
	 * 日常监测分组和栏目列表展示用
	 */
	@Transient
	private List<Object> columnList = new ArrayList<>();

	/**
	 * 截断关联关系  - 向前端返回值时，忽略该属性
	 */
	@JsonIgnore
	@OneToMany(mappedBy = "indexPage", cascade = CascadeType.DETACH)
	@OrderBy("sequence asc")
	private List<IndexTabMapper> indexTabMappers = new ArrayList<>();

	/**
	 * 有参构造函数
	 *
	 * @param name
	 */
	public IndexPage(String name, String parentId) {
		this.name = name;
		// 默认不隐藏
		this.hide = false;
	}

	/**
	 * 复制page
	 *
	 * @date Created at 2018年3月22日 上午11:12:27
	 * @Author 谷泽昊
	 * @return
	 */
	public IndexPage pageCopy() {
		return new IndexPage(name, hide, typeId, sequence);
	}

	public IndexPage(String name, boolean hide, String typeId, int sequence) {
		super();
		this.name = name;
		this.hide = hide;
		this.typeId = typeId;
		this.sequence = sequence;
	}
}
