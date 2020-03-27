package com.trs.netInsight.widget.base.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

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
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = -2955330485748809219L;
	
	protected interface SaveView {}
	public interface DisplayView {}

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
	 * 授权用户id
	 */
	@Column(name = "`user_id`")
	private String userId;

	/**
	 * 授权用户分组id
	 */
	@Column(name = "`sub_group_id`")
	private String subGroupId;
	
	/**
	 * 授权用户账号
	 */
	@Column(name = "`user_account`", updatable = false)
	private String userAccount;

	/**
	 * 对象创建时间
	 */
	@Column(name = "`created_time`", updatable = true)
	private Date createdTime;

	/**
	 * 创建用户
	 */
	@Column(name = "`createduser_id`", updatable = false)
	private String createdUserId;

	/**
	 * 最后修改时间
	 */
	@Column(name = "`last_modified_time`")
	private Date lastModifiedTime;

	/**
	 * 最后修改用户
	 */
	@Column(name = "`last_modifieduser_id`")
	private String lastModifiedUserId;

	@PrePersist
	protected void onCreate() {
		createdTime = new Date();
		// 用户
		User user = UserUtils.getUser();
		createdUserId = user.getId();
		if(StringUtils.isBlank(userId)){
			userId = user.getId();
		}
		
		if(StringUtils.isBlank(organizationId)){
			organizationId = user.getOrganizationId();
		}
		if (StringUtils.isBlank(subGroupId)){
			subGroupId = user.getSubGroupId();
		}
		if(StringUtils.isBlank(userAccount)){
			userAccount=user.getUserName(); 
		}

	}

	@PreUpdate
	protected void onUpdate() {
		lastModifiedTime = new Date();
//		System.out.println(UserUtils.getUser());
		lastModifiedUserId = UserUtils.getUser().getId();
//		RedisFactory.deleteAllKey(id);
	}

}
