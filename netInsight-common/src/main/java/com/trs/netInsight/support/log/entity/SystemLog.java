/*
 * Project: netInsight
 * 
 * File Created at 2018年7月25日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.log.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * 系统日志
 * 
 * @Type SystemLog.java
 * @author 谷泽昊
 * @date 2018年7月25日 上午10:45:01
 * @version
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "system_log",indexes = {
		@Index(columnList = "organization_id"),
		@Index(columnList = "createduser_id")})

public class SystemLog extends SystemLogBaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 请求参数
	 */
	@Column(name = "`request_params`", columnDefinition = "LONGTEXT")
	private String requestParams;
	/**
	 * 方法描述
	 */
	@Column(name = "`method_description`", columnDefinition = "TEXT")
	private String methodDescription;
	/**
	 * 模块
	 */
	@Column(name = "`system_log_type`")
	private String systemLogType;
	
	/**
	 * 具体操作
	 */
	@Column(name = "`system_log_operation`")
	private String systemLogOperation;

	/**
	 * 具体操作类型
	 */
	@Column(name = "`system_log_operation_type`")
	private String systemLogOperationType;
	
	
	/**
	 * 存入栏目id
	 * 
	 * 日常监测为：栏目分组id
	 * 
	 * 专题分析为：专题id
	 */
	@Column(name = "`system_log_tab_id`")
	private String systemLogTabId;
	/**
	 * 访问者ip
	 */
	@Column(name = "`request_ip`")
	private String requestIp;
	/**
	 * 请求uri
	 */
	@Column(name = "`request_uri`")
	private String requestUri;
	/**
	 * 请求开始时间
	 */
	@Column(name = "`start_time`")
	private Date startTime;
	/**
	 * 操作结束时间
	 */
	@Column(name = "`end_time`")
	private Date endTime;

	/***
	 * 请求耗时(毫秒值)
	 */
	@Column(name = "`time_consumed`")
	private Long timeConsumed;
	/**
	 * 状态码
	 */
	@Column(name = "`status`")
	private int status;

	/***
	 * 简单状态：成功 or 失败
	 */
	@Column(name = "`simple_status`")
	private String simpleStatus;
	
	/**
	 * 异常详情
	 */
	@Column(name = "`exception_detail`")
	private String exceptionDetail;

	/***
	 * 操作客户端信息(operation system infomation)
	 */
	@Column(name = "`os_info`")
	private String osInfo;

	/***
	 * 操作浏览器信息
	 */
	@Column(name = "`browser_info`")
	private String browserInfo;

	/***
	 * sessionId
	 */
	@Column(name = "`session_id`")
	private String sessionId;

	/***
	 * 操作位置	eg： 日常监测 / 环境 / 污染源新闻数据监控
	 */
	@Column(name = "`operation_position`")
	private String operationPosition;
	

	/**
	 * 操作人
	 */
	@Column(name = "`operation_user_name`")
	private String operationUserName;

	/***
	 * 操作位置	eg： 日常监测 / 环境 / 污染源新闻数据监控
	 */
	@Column(name = "`trsl`", columnDefinition = "TEXT")
	private String trsl;
	/***
	 * 导出的时候记录 条数
	 */
	@Column(name = "`num`")
	private Integer num;

	@Transient
	private String orgName = "";

	/**
	 * 按分组统计
	 */
	@Transient
	private Integer ipCount;

	public SystemLog(String requestIp,Integer ipCount){
		this.requestIp = requestIp;
		this.ipCount = ipCount;
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年7月25日 谷泽昊 creat
 */