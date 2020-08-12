package com.trs.netInsight.widget.alert.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Table(name = "alert_time")
@Setter
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AlertTime extends BaseEntity {
    /**
     * 用户“预警提醒”的最近一次提醒的预警发送时间
     * 避免重复发送使用
     */
    @Column(name = "alert_time")
    private Date alertTime;
}
