package com.trs.netInsight.widget.special.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.column.entity.emuns.SpecialFlag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by xiaoying on 2017/7/18. 专题主题表
 */
@Entity
@Getter
@Setter
@Table(name = "special_subject")
@NoArgsConstructor
public class SpecialSubject extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5544889422024785423L;

	/**
	 * 子类
	 */
	@JsonIgnore
	@OneToMany(cascade={CascadeType.REFRESH, CascadeType.REMOVE}, fetch = FetchType.EAGER )
	@JoinColumn(name = "subject_id")
	@OrderBy("sequence asc")
	private List<SpecialSubject> childrenPage = new ArrayList<>();
	@Column(name = "parent_id")
	private String parentId;
	/**
	 * 主题名/专题名
	 */
	@Column(name = "`name`")
	private String name;

	@Column(name = "parent_name")
	private String parentName;

	/**
	 * 专题id  也就是二级id
	 */
//	@Column
//	private String twoId;
	/**
	 * 主题名/专题名
	 */
//	@Column
//	private String subjectName;
	
	/**
	 * 所属分组id（父id）
	 */
	@Column(name = "subject_id")
	private String subjectId;
	
	/**
	 * 主题0 专题1 方案2
	 */
	/*@Column(name = "`flag`")
	private int flag;*/
	/**
	 * boolean类型 MYSQL保存BOOLEAN值时用1代表TRUE,0代表FALSE currentTheme 方案false 主题专题true
	 */
	@Column(name = "`current_theme`")
	private boolean currentTheme;
	
	 /**
     * 排序字段
     */
    @Column(name = "sequence", columnDefinition = "INT default 0")
	private int sequence;
	@Column(name = "order_before")
	private String  orderBefore = "after";

	/**
	 * api返回使用,无须持久化
	 */
	@Transient
	private List<SpecialProject> indexTabs;

	/**
	 * 标识是分组还是栏目  -  前端使用
	 */
	@Transient
	private Integer flag = SpecialFlag.SpecialSubjectFlag.ordinal();

	/**
	 * 日常监测分组和栏目列表展示用
	 */
	@Transient
	private List<Object> columnList = new ArrayList<>();

	/**
	 * 截断关联关系  - 向前端返回值时，忽略该属性
	 */
	@JsonIgnore
	@OneToMany(mappedBy = "groupId", cascade = CascadeType.DETACH)
	@OrderBy("sequence asc")
	private List<SpecialProject> indexTabMappers = new ArrayList<>();

	/**
	 * 有参构造函数
	 *
	 * @param name
	 */
	public SpecialSubject(String name, String subjectId) {
		this.name = name;
		this.subjectId = subjectId;//所属主题id
		// 默认不隐藏
	}

	/**
	 * 复制page
	 *
	 * @date Created at 2018年3月22日 上午11:12:27
	 * @Author 谷泽昊
	 * @return
	 */
	public SpecialSubject pageCopy() {
		return new SpecialSubject(name,sequence,subjectId);
	}
	public SpecialSubject(String name, int sequence) {
		super();
		this.name = name;
		this.sequence = sequence;
	}
	public SpecialSubject(String name, int sequence,String subjectId) {
		super();
		this.name = name;
		this.sequence = sequence;
		this.subjectId = subjectId;
	}
/*	*//**
	 * 新建专题
	 * @param name
	 * @param subjectId
	 *//*
	public SpecialSubject(String name, String subjectId) {
		this.name = name;
		this.flag = 1;
		this.subjectId = subjectId;//所属主题id
		this.currentTheme = true;
		super.setLastModifiedTime(new Date());
	}*/

	/**
	 * 新建主题
	 * @param name
	 */
	public SpecialSubject(String name) {
		this.name = name;
		this.flag = 0;
		this.currentTheme = true;
		super.setLastModifiedTime(new Date());
//		this.subjectId=UUID.randomUUID().toString();//主题id
	}
	
	/**
	 * 构造新实例
	 * @return
	 */
	public SpecialSubject newInstance(String subjectId, String commonUserId){
		SpecialSubject specialSubject = new SpecialSubject(name,subjectId);
		specialSubject.setFlag(flag);
		specialSubject.setCurrentTheme(currentTheme);
		specialSubject.setUserId(commonUserId);
		return specialSubject;
	}
	
	/**
	 * 构造新实例
	 * @return
	 */
	public SpecialSubject newInstance(String commonUserId){
		SpecialSubject specialSubject = new SpecialSubject(name,subjectId);
		specialSubject.setFlag(flag);
		specialSubject.setCurrentTheme(currentTheme);
		specialSubject.setUserId(commonUserId);
		return specialSubject;
	}
	/**
	 * 构造新实例
	 * @return
	 */
	public SpecialSubject newInstanceForSubGroup(String subjectId, String commonSubGroupId){
		SpecialSubject specialSubject = new SpecialSubject(name,subjectId);
		specialSubject.setFlag(flag);
		specialSubject.setCurrentTheme(currentTheme);
		specialSubject.setSubGroupId(commonSubGroupId);
		return specialSubject;
	}

	/**
	 * 构造新实例
	 * @return
	 */
	public SpecialSubject newInstanceForSubGroup(String commonSubGroupId){
		SpecialSubject specialSubject = new SpecialSubject(name,subjectId);
		specialSubject.setFlag(flag);
		specialSubject.setCurrentTheme(currentTheme);
		specialSubject.setSubGroupId(commonSubGroupId);
		return specialSubject;
	}
}
