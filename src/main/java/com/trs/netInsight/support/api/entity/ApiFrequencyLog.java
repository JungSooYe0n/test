package com.trs.netInsight.support.api.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * api接口请求频率日志
 *
 * @Type ApiFrequencyLog.java
 * @author zhangya
 * @date 2019年10月16日
 * @version
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "api_frequency_log")
public class ApiFrequencyLog implements Serializable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "`id`")
    private String id;

    /**
     * 对象创建时间
     */
    @Column(name = "`created_time`", updatable = true)
    private Date createdTime;

    /**
     * 最后修改时间
     */
    @Column(name = "`last_modified_time`")
    private Date lastModifiedTime;

    /**
     * clientID
     */
    @Column(name = "`quest_client_id`")
    private String questClientId;

    /**
     * 请求机构id  -- 日志分为两种，某机构的，或者全部的，按小时和天统计，全部的机构为null
     */
    @Column(name = "`quest_org_id`")
    private String questOrgId;

    /**
     * 请求api相对名称
     */
    @Column(name = "`quest_method`")
    private String questMethod;

    /**
     * 请求api接口的code
     */
    @Column(name = "`quest_method_code`")
    private String questMethodCode;

    /**
     * 请求api时间类型 - hour/day
     */
    @Column(name = "`time_type`")
    private String timeType;
    /**
     * 请求api频率
     */
    @Column(name = "`quest_frequency`")
    private Integer questFrequency;
    /**
     * 请求api时间
     */
    @Column(name = "`quest_time`")
    private Date questTime;
}
