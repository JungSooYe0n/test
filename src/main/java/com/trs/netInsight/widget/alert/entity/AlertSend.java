package com.trs.netInsight.widget.alert.entity;

import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;
import java.util.Map;

/**
 * app(目前针对app)预警推送提示对应实体类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/22 10:09.
 * @desc
 */
@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`alert_send`")
public class AlertSend extends BaseEntity {

    private static final long serialVersionUID = -6659365422199362456L;
    /**
     *
     */
    @Column(name = "`ids`", columnDefinition = "LONGTEXT")
    private String ids;

    /**
     * 预警规则名称 || 预警名称
     */
    @Column(name = "`rule_name`")
    private String ruleName;

    /**
     * 预警时间
     */
    @Column(name = "`alert_time`")
    private String alertTime;

    /**
     * 预警方式
     */
    @Column(name = "`send_way`")
    private SendWay sendWay;

    /**
     * 自动 auto、手动 manual
     */
    @Column(name = "`alert_source`")
    private AlertSource alertSource;

    /**
     * 接收人id
     */
    @Column(name = "`recevier_id`")
    private String recevierId;
    /**
     * 数量
     */
    @Column(name = "`size`")
    private int size;

    /**
     * 推送人
     */
    @Transient
    private String pushHuman;

    /**
     * 预警具体数据内容
     */
    @Transient
    private List<Map<String, Object>> alertData;

    public AlertSend(String ids, String ruleName, String alertTime, SendWay sendWay, AlertSource alertSource, String recevierId, int size) {
        this.ids = ids;
        this.ruleName = ruleName;
        this.alertTime = alertTime;
        this.sendWay = sendWay;
        this.alertSource = alertSource;
        this.recevierId = recevierId;
        this.size = size;
    }
}
