package com.trs.netInsight.support.log.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
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

}
