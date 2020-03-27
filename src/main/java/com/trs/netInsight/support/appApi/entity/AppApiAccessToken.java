package com.trs.netInsight.support.appApi.entity;

import com.trs.netInsight.support.appApi.utils.constance.GrantRange;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_api_access_token")
public class AppApiAccessToken extends BaseEntity {

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

	@Transient
	private AppApiClient client;

	/**
	 * 多对一关联关系
	 */
	@ManyToOne
	@JoinColumn(name = "grant_source_owner_id",insertable=false,updatable=false,foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
	private User user;

	public AppApiAccessToken(String clientId, String grantSourceOwnerId,String subGroupId, Date expireTime, String grantRange) {
		super();
		super.setSubGroupId(subGroupId);
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
	}
	

}
