package com.trs.netInsight.widget.column.entity.mapper;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;

import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import lombok.Getter;
import lombok.Setter;

/**
 * 栏目映射表
 * <p> indexTab、indexPage两个属性在使用懒加载获取时，获取的对象均为持久态对象，<br>
 * 修改属性值时应注意，在未加载该属性时，可通过置null的方式，拒绝进行懒加载;在已加载<br>
 * 的情况下，可通过复制对象的方式（主键除外）进行进行状态转换
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年9月17日
 *
 */
@Getter
@Setter
@Entity
@Table(name = "index_tab_mapper")
public class IndexTabMapper extends BaseEntity {

	/**
	 *
	 */
	private static final long serialVersionUID = -2880083565994872996L;

	/**
	 * 多对一关联关系
	 */
	@ManyToOne
	@JoinColumn(name = "index_tab_id")
	private IndexTab indexTab;

	/**
	 * 断言是否为引用数据   为0 为引用，默认为1
	 */
	@Column(name = "isme")
	private boolean isMe;

	/**
	 * 是否共享
	 */
	@Column(name = "share")
	private boolean share;

	/**
	 * 多对一关联关系
	 */
	@ManyToOne()
	@JsonIgnore
	@JoinColumn(name = "index_page_id")
	private IndexPage indexPage;

	/**
	 * 排序字段
	 */
	@Column(name = "sequence")
	private Integer sequence;

	/**
	 * App排序字段
	 */
	@Column(name = "appsequence" , columnDefinition = "INT default 0")
	private int appsequence;

	/**
	 * 是否隐藏
	 */
	@Column(name = "hide")
	private boolean hide;

	/**
	 * 自定义宽度
	 */
	@Column(name = "tab_width")
	private int tabWidth = 50;

	public String getTabWidth(){
		return String.valueOf(this.tabWidth);
	}

	public boolean getShare(){
		return this.share;
	}

	/**
	 * 自定义类型导航栏的id
	 */
	@Column(name = "type_id")
	private String typeId;

	/**
	 * 标识是分组还是栏目  -  前端使用
	 */
	@Transient
	private Integer flag = ColumnFlag.IndexTabFlag.ordinal();
}
