/*
 * Project: netInsight
 * 
 * File Created at 2017年11月17日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.user.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.user.entity.enums.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.*;

/**
 * 机构表
 *
 * 2019/06/19重构
 * @Type Organization.java
 * @author 谷泽昊
 * @date 2017年11月17日 下午4:08:21
 * @version
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "`organization`")
public class Organization extends BaseEntity implements Comparable<Organization>{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// --------------基本信息------------------------

	/**
	 * 机构类型
	 */
	@Column(name = "`organization_type`")
	private String organizationType;

	/**
	 *
	 * organizationType的set方法
	 * 
	 * @date Created at 2018年8月29日 下午4:38:46
	 * @Author 谷泽昊
	 * @param organizationType
	 */
	public void setOrganizationType(OrganizationType organizationType) {
		this.organizationType = organizationType.toString();
	}

	/**
	 * 机构名称
	 */
	@Column(name = "`organization_name`")
	private String organizationName;
	//自动注册的,防止重复标识去掉
	public String getOrganizationName(){
		return this.organizationName;
	}

	/**
	 * 机构logo图片 名字
	 */
	@Column(name = "`logo_pic_name`")
	private String logoPicName;

	/**
	 * 管理员账号
	 */
	@Column(name = "`admin_user_id`")
	private String adminUserId;

	/**
	 * 临时属性--用户
	 */
	@Transient
	private User user;

	/**
	 * 有效期，永久为2050-01-01 00:00:00
	 */
	@Column(name = "`expireat`")
	private String expireAt;

	/**
	 * 客户来源
	 */
	@Column(name = "`customer_source`")
	private String customerSource;

	/**
	 * 销售负责人
	 */
	@Column(name = "`head_of_sales`")
	private String headOfSales;

	/**
	 * 运维负责人id
	 */
	@ManyToMany(fetch=FetchType.EAGER,targetEntity = User.class, mappedBy = "organizations") // 把主控方交给用户
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<User> rolePlatforms;

	/**
	 * 备注
	 */
	@Column(name = "`descriptions`")
	private String descriptions;

	/**
	 * 临时属性--登录页配置信息
	 */
	@Transient
	private LoginPageConfig loginPageConfig;

	// --------------配置信息(机构配置)------------------------
	/**
	 * 系统名称
	 */
	/*@Column(name = "`system_name`")
	private String systemName;*/


	/**
	 * 用户上限 -1为不限，默认是5
	 */
	@Column(name = "`user_limit`")
	private int userLimit;

	//--------------各种数量限制-----------------
    /**
     * 日常监测栏目数  默认50
     */
    @Column(name = "`column_num`")
    private int columnNum = 50;

    /**
     * 专题事件数    默认10
     */
    @Column(name = "`special_num`")
    private int specialNum = 10;

    /**
     * 预警主题数   默认10
     */
    @Column(name = "`alert_num`")
    private int alertNum = 10;

    /**
     * 可绑定预警账号数  默认5
     */
    @Column(name = "`alert_account_num`")
    private int alertAccountNum = 5;

    /**
     * 关键词数量      默认500
     */
    @Column(name = "`keywords_num`")
    private int keyWordsNum = 500;

    /**
     * 日常监测 默认近3个月，专题分析 默认近1年，高级搜索 默认近3个月 可支持检索时间
     */
    @Column(name = "`column_date_limit`")
    private int columnDateLimit = 90;

	@Column(name = "`special_date_limit`")
	private int specialDateLimit = 365;

	@Column(name = "`a_searche_date_limit`")
	private int aSearchDateLimit = 90;


	/**
	 * 是否能同时登陆
	 */
	/*@Column(name = "`same_time_login`")
	private boolean sameTimeLogin = false;*/

	/**
	 * 数据设置
	 */
	@Column(name = "`data_sources`")
	private String dataSources;
	/**
	 * 可检索数据时间(原有的 可检索时间限制  留着是为了迁移历史数据)
	 */
	@Column(name = "`data_date`")
	private int dataDate=6;


	/**
	 * 账号状态 1为冻结 0为正常
	 */
	@Column(name = "`status`")
	private String status = Status.frozen.getValue();

	/**
	 * 1自动添加的 0 正常添加
	 */
	@Column(name = "`autoAdd`")
	private String autoAdd = "0";

//	是否给机构开启专享库
	@Column(name = "`exclusive_hybase`")
	private Boolean exclusiveHybase = false;
	public Boolean isExclusiveHybase(){
		if(this.exclusiveHybase != null && this.exclusiveHybase){
			return this.exclusiveHybase;
		}else{
			return false;
		}
	}

	/**
	 * status的set方法
	 * 
	 * @date Created at 2018年8月29日 下午4:38:24
	 * @Author 谷泽昊
	 * @param status
	 */
	public void setStatus(Status status) {
		this.status = status.getValue();
	}

	/**
	 * 分组
	 */
	//@OneToMany(cascade = javax.persistence.CascadeType.REMOVE,fetch = FetchType.LAZY)//级联删除
	@Transient
    private List<SubGroup> subGroups;

	/**
	 * 机构剩余时间（不入库）
	 */
	@Transient
	private String surplusDate;

	/**
	 * app id, openApi使用
	 */
//	@Column(name = "`client_id`")
//	private String clientId;

	/**
	 * 机构类型
	 * 
	 * @Type Organization.java
	 * @Desc
	 * @author 谷泽昊
	 * @date 2018年8月29日 下午4:02:23
	 * @version
	 */
	public static enum OrganizationType {
		/**
		 * 正式
		 */
		formal,
		/**
		 * 试用
		 */
		trial
	}

	/**
	 * 登录次数（不入库）
	 */
	@Transient
	private Integer loginCount;
	/**
	 * 在线人数（不入库）
	 */
	@Transient
	private Integer onlineUserCount;
	/**
	 * 上面两个参数哪个排序 0不排序 1在线人数 ,2登录次数
	 */
	@Transient
	private Integer sortByCount;
	/**
	 * 上面两个参数正序还是降序
	 */
	@Transient
	private String ascDesc;

	/**
	 * this.o1、o2；
	 * 如果保持这个顺序就返回-1，交换顺序就返回1，什么都不做就返回0；
	 * 所以 升序的话 如果o1<o2，就返回-1
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Organization o){
		if(this.loginCount==null) this.loginCount=0;
		if(this.onlineUserCount==null) this.onlineUserCount=0;
		if(o.onlineUserCount==null) o.onlineUserCount=0;
		if(o.loginCount==null) o.loginCount=0;
		if(this.ascDesc==null) this.ascDesc="";

		if(this.ascDesc.equals("desc") && sortByCount==2){
			return o.loginCount - this.loginCount;
		}else if(this.ascDesc.equals("asc") && sortByCount==2){
			return this.loginCount - o.loginCount;
		}else if(this.ascDesc.equals("desc") && sortByCount==1){
			return o.onlineUserCount - this.onlineUserCount;
		}else if(this.ascDesc.equals("asc") && sortByCount==1){
			return this.onlineUserCount - o.onlineUserCount;
		}else{
			return 0;
		}
	}

	@Override
	public String toString() {
		return "Organization{" +
				"organizationType='" + organizationType + '\'' +
				", organizationName='" + organizationName + '\'' +
				", logoPicName='" + logoPicName + '\'' +
				", adminUserId='" + adminUserId + '\'' +
				", user=" + user +
				", expireAt='" + expireAt + '\'' +
				", customerSource='" + customerSource + '\'' +
				", headOfSales='" + headOfSales + '\'' +
				", rolePlatforms=" + rolePlatforms +
				", descriptions='" + descriptions + '\'' +
				", userLimit=" + userLimit +
				", columnNum=" + columnNum +
				", specialNum=" + specialNum +
				", alertNum=" + alertNum +
				", alertAccountNum=" + alertAccountNum +
				", keyWordsNum=" + keyWordsNum +
				", columnDateLimit=" + columnDateLimit +
				", specialDateLimit=" + specialDateLimit +
				", aSearchDateLimit=" + aSearchDateLimit +
				", dataSources='" + dataSources + '\'' +
				", status='" + status + '\'' +
				", autoAdd='" + autoAdd + '\'' +
				'}';
	}

//	@Override
//	public String toString() {
//		return "Organization [organizationType=" + organizationType + ", organizationName=" + organizationName
//				+ ", adminUserId=" + adminUserId + ", user=" + user + ", expireAt=" + expireAt + ", customerSource="
//				+ customerSource + ", headOfSales=" + headOfSales + ", logoPicName=" + logoPicName + ", userLimit=" +
//				userLimit + ", dataSources=" + dataSources + ", status=" + status
//				+ ", clientId=" + clientId + "]";
//	}

	public static void main(String af[]){
		List<Organization> orgs = new ArrayList<>();
		for(int i=0;i<10;i++){
			Organization o = new Organization();
			Random r = new Random();
			if(i<8) o.setOnlineUserCount(r.nextInt(100));
			o.setLoginCount(r.nextInt(50));
			o.setSortByCount(1);
			o.setAscDesc("asc");
			orgs.add(o);
		}
		for(Organization oo: orgs){
			System.out.println("--->"+oo.getOnlineUserCount());
		}
		Collections.sort(orgs);
		System.out.println("-------------------------------");
		for(Organization oo: orgs){
			System.out.println("--->"+oo.getOnlineUserCount());
		}
	}


}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月17日 谷泽昊 creat
 */