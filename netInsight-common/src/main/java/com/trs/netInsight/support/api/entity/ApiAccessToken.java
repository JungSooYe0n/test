package com.trs.netInsight.support.api.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.trs.netInsight.support.api.utils.constance.GrantRange;
import com.trs.netInsight.widget.base.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "api_access_token")
public class ApiAccessToken extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1493082440235229915L;

	/**
	 * api客户端id
	 */
	@Column(name = "client_id")
	private String clientId;

	/**
	 * 授权资源拥有者id
	 */
	@Column(name = "grant_source_owner_id")
	private String grantSourceOwnerId;

	/**
	 * 到期时间
	 */
	@Column(name = "expire_time")
	private Date expireTime;

	/**
	 * 权限范围
	 */
	@Column(name = "grant_range")
	private String grantRange;

	/**
	 * token
	 */
	@Column(name = "access_token")
	private String accessToken;

	/**
	 * 列表每页最多展示条数
	 */
	@Column(name = "max_page_size")
	private int maxPageSize = 1000;

	@Transient
	private ApiClient client;

	public ApiAccessToken(String clientId, String grantSourceOwnerId, Date expireTime, String grantRange,int maxPageSize) {
		super();
		this.clientId = clientId;
		this.grantSourceOwnerId = grantSourceOwnerId;
		this.expireTime = expireTime;
		if (StringUtils.isBlank(grantRange)) {
			this.grantRange = GrantRange.Column.getCode();
		}else{
			grantRange = grantRange.replace(GrantRange.Max.getParam(), GrantRange.Max.getCode());
			grantRange = grantRange.replace(GrantRange.Column.getParam(), GrantRange.Column.getCode());
			grantRange = grantRange.replace(GrantRange.Project.getParam(), GrantRange.Project.getCode());
			grantRange = grantRange.replace(GrantRange.Report.getParam(), GrantRange.Report.getCode());
			grantRange = grantRange.replace(GrantRange.Alert.getParam(), GrantRange.Alert.getCode());
			this.grantRange = grantRange.replace(GrantRange.PlatForm.getParam(), GrantRange.PlatForm.getCode());
		}
		this.maxPageSize = maxPageSize;
	}
	

}
