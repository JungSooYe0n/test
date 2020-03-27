package com.trs.netInsight.support.appApi.entity;

import com.trs.netInsight.support.appApi.utils.constance.ApiFrequencyConst;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * api 客户端实体
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月29日
 *
 */
@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@Table(name = "app_api_client")
public class AppApiClient extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6396283105648871816L;

	/**
	 * 授权机构id
	 */
	@Column(name = "grant_org_id")
	private String grantOrgId;

	/**
	 * 客户端名称
	 */
	@Column(name = "client_name")
	private String clientName;

	/**
	 * 客户端key
	 */
	@Column(name = "client_secret_key")
	private String clientSecretKey;

	/**
	 * api频率调用级别,默认普通级别
	 */
	@Column(name = "frequency_level")
	private String frequencyLevel = ApiFrequencyConst.LEVEL_COMMON;

}
