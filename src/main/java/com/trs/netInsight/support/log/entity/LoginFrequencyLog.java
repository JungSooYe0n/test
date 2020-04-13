package com.trs.netInsight.support.log.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 记录登录次数信息
 *  -------
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "login_frequency_log")
public class LoginFrequencyLog extends SystemLogBaseEntity{

    /**
     * 操作人
     */
    @Column(name = "`operation_user_name`")
    private String operationUserName;


    /**
     * 登录次数
     */
    @Column(name = "`login_num`")
    private Integer loginNum;



}
