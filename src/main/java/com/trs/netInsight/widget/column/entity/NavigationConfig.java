package com.trs.netInsight.widget.column.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 导航栏配置类
 * @author xiaoying
 *
 */
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="navigation_config")
public class NavigationConfig extends BaseEntity implements Comparable<NavigationConfig>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3626315078418138361L;
	/**
	 * 类型  自定义还是原来的
	 */
	@Column(name="type")
	private NavigationEnum type; 
	/**
	 * 名字
	 */
	@Column(name="name")
	private String name;
	/**
	 * 顺序
	 */
	@Column(name = "sequence")
	private int sequence;
	
	/**
	 * 是否隐藏
	 */
	@Column(name = "hide")
	private boolean hide = false;

	@Transient
	private long shareNumber;
	
	@Transient
	private boolean edit = false;
	
	@Transient
	private boolean active = false;
	
	public NavigationConfig copyNavigation(){
		NavigationConfig navigationConfig = new NavigationConfig(type, name, sequence, hide, edit, active);
		return navigationConfig;
	}

	public NavigationConfig(NavigationEnum type, String name, int sequence, boolean hide, boolean edit,
			boolean active) {
		super();
		this.type = type;
		this.name = name;
		this.sequence = sequence;
		this.hide = hide;
		this.edit = edit;
		this.active = active;
	}


	@Override
	public int compareTo(NavigationConfig o) {
		if (o.getSequence() > sequence){
			return -1;
		}else if (o.getSequence() < sequence){
			return 1;
		}
		return 0;
	}
}
