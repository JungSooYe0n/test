package com.trs.netInsight.support.log.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 检索时间超长日志
 * @author 马加鹏
 * @date 2021/4/21 15:34
 */
@Table(name = "search_time_long_log")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SearchTimeLongLog extends SystemLogBaseEntity {

    /**
     * 机构名称
     */
    @Column(name = "`organization_name`")
    private String organizationName;

    /**
     * 功能模块
     */
    @Column(name = "`model_type`")
    private String modelType;

    /**
     * 模块名称
     */
    @Column(name = "`model_name`")
    private String modelName;

    /***
     * 最终查询hybase的表达式
     */
    @Column(name = "`trsl`", columnDefinition = "TEXT")
    private String trsl;

    /**
     * 检索时间范围
     */
    @Column(name = "`time_range`")
    private String timeRange;

    /**
     * 检索时间范围差（单位：天）
     */
    @Column(name = "`serach_time`")
    private int searchTime;

}

