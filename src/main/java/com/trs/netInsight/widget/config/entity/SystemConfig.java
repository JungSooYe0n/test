package com.trs.netInsight.widget.config.entity;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @Type SystemConfig.java
 * @author 张娅
 * @date 2020年1月6日
 * @version
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_config")
public class SystemConfig implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 删除机构是否需要邮件确认  需要邮件 true  不需要 false
     */
    @Column(name = "delete_org")
    private Boolean deleteOrg = true;
    /**
     * 新建机构是否需要运维账号  需要 true  不需要 false
     */
    @Column(name = "need_operation")
    private Boolean needOperation = true;

    /**
     * 超管的机构名
     */
    @Column(name = "organization_name")
    private String organizationName = "网察大数据分析平台";
    /**
     * 超管的机构图片
     */
    @Column(name = "logo_pic_name")
    private String logoPicName = "无logo";

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "`id`", length = 100)
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
