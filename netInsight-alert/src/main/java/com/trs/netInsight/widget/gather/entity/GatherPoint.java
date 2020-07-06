package com.trs.netInsight.widget.gather.entity;

import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "gather_point")
public class GatherPoint extends BaseEntity {
    /**
     * 任务名称
     */
    @Column(name = "task_name")
    private String taskName;
    /**
     * 任务id
     */
    @Column(name = "task_id")
    private String taskId;
    /**
     * 数据类型
     */
    @Column(name = "data_type")
    private String dataType;
    /**
     * 站点名称
     */
    @Column(name = "site_name")
    private String siteName;
    /**
     * 频道名称
     */
    @Column(name = "channel_name")
    private String channelName;
    /**
     * url地址
     */
    @Column(name = "url_name")
    private String urlName;
    /**
     * 账号名称
     */
    @Column(name = "account_name")
    private String accountName;
    /**
     * 账号id
     */
    @Column(name = "account_id")
    private String accountId;
    /**
     * 提交时间
     */
    @Column(name = "commit_time")
    private Date commitTime;
    @Column(name = "key_word")
    private String keyWord;
    /**
     * 优先级
     */
    @Column(name = "level")
    private String level;
    /**
     * 状态 草稿 已提交 采集中
     */
    @Column(name = "status")
    private String status;
    /**
     * 审核状态  未审核 审核中 已审核
     */
    @Column(name = "audit_status")
    private String auditStatus;
    /**
     * 备注
     */
    @Column(name = "remarks")
    private String remarks;
    /**
     * 采集点
     */
    @Column(name = "gather_point_name")
    private String gatherPointName;
    /**
     * 审核人
     */
    @Column(name = "audit_user_name")
    private String auditUserName;
    /**
     * 审核时间
     */
    @Column(name = "audit_time")
    private Date auditTime;
    @Column(name = "organization_name")
    private String organizationName;
public GatherPoint(String userId,String dataType,String taskName,String taskId,String siteName,String channelName,String urlName,String accountName,String accountId,Date commitTime,String keyWord,String level){
//    this.favouritesId = GUIDGenerator.generate(GatherPoint.class);

    this.dataType = dataType;
    this.taskName = taskName;
    this.taskId = taskId;
    this.siteName = siteName;
    this.channelName = channelName;
    this.urlName = urlName;
    this.accountName = accountName;
    this.accountId = accountId;
    this.commitTime = commitTime;
    this.keyWord = keyWord;
    this.level = level;
    super.setUserId(userId);
}
public GatherPoint(String userId,String dataType,String siteName,String channelName,String urlName,Date commitTime,String level){
    this.dataType = dataType;
    this.siteName = siteName;
    this.channelName = channelName;
    this.urlName = urlName;
    this.commitTime = commitTime;
    this.level = level;
    super.setUserId(userId);
}
    public GatherPoint(String userId,String dataType,String siteName,String accountName,String accountId,String urlName,Date commitTime,String level){
        this.dataType = dataType;
        this.siteName = siteName;
        this.accountName = accountName;
        this.accountId = accountId;
        this.urlName = urlName;
        this.commitTime = commitTime;
        this.level = level;
        super.setUserId(userId);
    }


}
