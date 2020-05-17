package com.trs.netInsight.widget.special.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

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
	 * 主题名/专题名
	 */
	@Column(name = "`name`")
	private String name;
	
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
	 * 所属主题id
	 */
	@Column(name = "`subject_id`")
	private String subjectId;
	
	/**
	 * 主题0 专题1 方案2
	 */
	@Column(name = "`flag`")
	private int flag;
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

	/**
	 * 新建专题
	 * @param name
	 * @param subjectId
	 */
	public SpecialSubject(String name, String subjectId) {
		this.name = name;
		this.flag = 1;
		this.subjectId = subjectId;//所属主题id
		this.currentTheme = true;
		super.setLastModifiedTime(new Date());
	}

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
