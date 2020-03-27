package com.trs.netInsight.widget.alert.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 预警账号
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Entity
@Table(name = "alert_account ")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AlertAccount extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4438510032753508038L;

	/**
	 * 账号名称
	 */
	@Column(name = "name")
	private String name;
	
	/**
	 * 自定义名字
	 */
	@Column(name="defined_name")
	private String definedName;
	/**
	 * 类型（枚举） 短信，邮件，微信，客户端
	 */
	@Column(name = "type")
	private SendWay type;
	/**
	 * 账号
	 */
	@Column(name = "account")
	private String account;
	
	/**
	 * 是否可以删除
	 */
	@Column(name = "del_flag")
	private boolean delFlag;
	
	/**
	 * 是否激活预警
	 */
	@Column(name = "`active`")
	private boolean active;
	
	/**
	 * 头像
	 */
	@Column(name = "`head_img_url`")
	private String headImgUrl;
	
	
	/**
	 * email flag wechat应前端要求加  都为true
	 */
	@Transient
	private boolean email;
	
	@Transient
	private boolean flag;
	
	@Transient
	private boolean wechat;
	
	@Transient
	private boolean sms;

	@Transient
	private boolean app;
	/**
	 * 20181012  因前端渲染需要  将这四个字段的值 由默认true 改为 false
	 * @return
	 */
	public boolean isSms(){
		return false;
	}

	public boolean isEmail() {
		return false;
	}

	public boolean isFlag() {
		return false;
	}

	public boolean isWechat() {
		return false;
	}

	public boolean isApp() {
		return false;
	}

}
