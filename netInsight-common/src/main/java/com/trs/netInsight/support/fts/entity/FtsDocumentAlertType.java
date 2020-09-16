package com.trs.netInsight.support.fts.entity;


import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Transient;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 已发预警 针对微信、APP
 * @Author:拓尔思信息股份有限公司
 * @Description:
 * @Date:Created in  2020/6/30 15:18
 * @Created By yangyanyan
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.ALERT_TYPE)
public class FtsDocumentAlertType extends IDocument {
//    private String id;

    /*
     * 唯一 id
     * */
    @FtsField("IR_ALERT_TYPE_ID")
    private String alertTypeId;

    @FtsField("IR_ALERT_IDS")
    private String ids;
    /**
     * 预警规则名称 || 预警名称
     */
    @FtsField("IR_RULE_NAME")
    private String ruleName;

    /**
     * 预警时间
     */
    @FtsField("IR_ALERT_TIME")
    private String alertTime;

    /**
     * 预警方式
     */
    @FtsField("IR_SEND_WAY")
    private String sendWay;

//    预警类型
//    @FtsField("IR_SEND_TYPE")
//    private String sendType;
    /**
     * 自动 auto、手动 ARTIFICIAL
     */
    @FtsField("IR_ALERT_SOURCE")
    private String alertSource;

    /**
     * 接收人id
     */
//    @Column(name = "`recevier_id`")
//    private String recevierId;
    /**
     * 接收人
     */
    @FtsField("IR_RECEIVER")
    private String receiver;

    /**
     * 数量
     */
    @FtsField("IR_SIZE")
    private int size;


    /**
     * 入库时间(即创建时间)
     */
    @FtsField("IR_LOADTIME")
    private Date loadTime;

    @FtsField("IR_USER_ID")
    private String userId;
    //    用户分组id
    @FtsField("IR_SubGroup_ID")
    private String subGroupId;

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

    public String getId() {
        return this.alertTypeId;
    }
    //    //    机构id
//    @FtsField("IR_ORGANIZATION_ID")
//    private String organizationId;

    public FtsDocumentAlertType(String alertTypeId, String ids, String ruleName, String alertTime, String sendWay, int size) {
        this.alertTypeId = alertTypeId;
        this.ids = ids;
        this.ruleName = ruleName;
        this.alertTime = alertTime;
        this.sendWay = sendWay;
        this.size = size;
    }
}
