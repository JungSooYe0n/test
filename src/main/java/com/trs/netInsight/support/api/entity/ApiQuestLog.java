package com.trs.netInsight.support.api.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * api调用日志实体
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月9日
 *
 */
@Getter
@Setter
public class ApiQuestLog {

	/**
	 * clientID
	 */
	private String questClientId;

	/**
	 * 请求机构id
	 */
	private String questOrgId;

	/**
	 * 请求client ip地址
	 */
	private String questIp;

	/**
	 * 请求api相对名称
	 */
	private String questMethod;

	/**
	 * 请求是否成功
	 */
	private boolean success;
}
