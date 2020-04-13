package com.trs.netInsight.support.log.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;

import lombok.Getter;
import lombok.Setter;

/**
 * 系统实体对象抽象类<br>
 * 包含主键id，子类不需要定义主键
 * 
 *
 */
@Setter
@Getter
@MappedSuperclass
public abstract class SystemLogBaseEntity implements Serializable {

	private static final long serialVersionUID = -2955330485748809219L;

	protected interface SaveView {
	}

	public interface DisplayView {
	}

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	@Column(name = "`id`")
	private String id;

	/**
	 * 机构id
	 */
	@Column(name = "`organization_id`", updatable = false)
	private String organizationId;

	/**
	 * 授权用户分组id
	 */
	@Column(name = "`sub_group_id`")
	private String subGroupId;

	/**
	 * 对象创建时间
	 */
	@Column(name = "`created_time`", updatable = true)
	private Date createdTime;

	/**
	 * 创建用户id
	 */
	@Column(name = "`createduser_id`", updatable = false)
	private String createdUserId;

	/***
	 * 创建用户name
	 */
	@Column(name = "`createduser_name`", updatable = false)
	private String createdUserName;

	@Column(name = "`display_name`", updatable = false)
	private String displayName;

	@PrePersist
	protected void onCreate() {
		createdTime = new Date();
		// 用户
		User user = UserUtils.getUser();
		createdUserId = user.getId();
		if (StringUtils.isBlank(createdUserId)) {
			createdUserId = user.getId();
		}
		createdUserName = user.getUserName();
		displayName = user.getDisplayName();
		if (StringUtils.isBlank(organizationId)) {
			organizationId = user.getOrganizationId();
		}
		if (StringUtils.isBlank(subGroupId)){
			subGroupId = user.getSubGroupId();
		}
	}

}
