package com.trs.netInsight.support.log.entity;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "request_time_log")

public class RequestTimeLog {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "`id`")
    private String id;
    /**
     * 请求开始时间
     */
    @Column(name = "`start_time`")
    private Date startTime;
    /**
     * 请求结束时间
     */
    @Column(name = "`end_time`")
    private Date endTime;
    /**
     * 请求总时间
     */
    @Column(name = "`time_total`")
    private Long timeTotal;
    /**
     * 请求mysql开始时间
     */
    @Column(name = "`start_mysql_time`")
    private Date startMysqlTime;
    /**
     * 请求mysql 结束时间
     */
    @Column(name = "`end_mysql_time`")
    private Date endMsqlTime;
    /**
     * 请求mysql总时间
     */
    @Column(name = "`mysql_total`")
    private Long mysqlTotal;

    /**
     * 请求hybase开始时间
     */
    @Column(name = "`start_hybase_time`")
    private Date startHybaseTime;
    /**
     * 请求hybase结束时间
     */
    @Column(name = "`end_hybase_time`")
    private Date endHybaseTime;
    /**
     * 请求hybase总时间
     */
    @Column(name = "`hybase_total`")
    private Long hybaseTotal;

    /**
     * 随机数
     */
    @Column(name = "`random_num`")
    private String randomNum;

    /**
     * 操作名
     */
    @Column(name = "`operation`")
    private String operation;

    /**
     * 请求hybase总时间
     */
    @Column(name = "`other_time`")
    private Long otherTime;

    /**
     * 机构id
     */
    @Column(name = "`organization_id`", updatable = false)
    private String organizationId;

    /**
     * 授权用户分组id
     */
    @Column(name = "`sub_group_id`", updatable = false)
    private String subGroupId;

    /**
     * 对象创建时间
     */
    @Column(name = "`created_time`", updatable = false)
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
        if(timeTotal == null){
            if(startTime!= null && endTime!= null ){
                timeTotal = endTime.getTime() - startTime.getTime();
            }
        }
        if(mysqlTotal == null){
            if(startMysqlTime!= null && endMsqlTime!= null ){
                mysqlTotal = endMsqlTime.getTime() - startMysqlTime.getTime();
            }
        }
        if(hybaseTotal == null){
            if(startHybaseTime!= null && endHybaseTime!= null ){
                hybaseTotal = endHybaseTime.getTime() - startHybaseTime.getTime();
            }
        }
        if(timeTotal != null && hybaseTotal!=null){
            otherTime = timeTotal-hybaseTotal;
        }
        createdTime = new Date();
        // 用户
        User user = UserUtils.getUser();
        if(StringUtils.isBlank(createdUserId)){
            createdUserId = user.getId();
        }
        if(StringUtils.isBlank(createdUserName)){
            createdUserName = user.getUserName();
        }
        if(StringUtils.isBlank(displayName)){
            displayName = user.getDisplayName();
        }
        if (StringUtils.isBlank(organizationId)) {
            organizationId = user.getOrganizationId();
        }
        if (StringUtils.isBlank(subGroupId)){
            subGroupId = user.getSubGroupId();
        }
    }
}
