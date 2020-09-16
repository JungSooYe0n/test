package com.trs.netInsight.widget.apply.entity;

import com.trs.netInsight.widget.apply.entity.enums.AccountStatus;
import com.trs.netInsight.widget.apply.entity.enums.MoveStatus;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 申请试用的信息
 */
@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`apply`")
public class Apply extends BaseEntity {

    //申请用户类型
    @Column(name = "`apply_user_type`")
    private String applyUserType;

    //单位名称
    @Column(name = "`unit_name`")
    private String unitName;
    //姓名
    @Column(name = "`name`")
    private String name;
    //原有账号
    @Column(name = "`original_account`")
    private String originalAccount;

    //用户账号类型 正式formal，试用trial
    @Column(name = "`account_type`")
    private String accountType;
    //手机号码
    @Column(name = "`phone`")
    private String phone;

    //工作电话
    @Column(name = "`work_phone`")
    private String workPhone;
    //邮箱
    @Column(name = "`email`")
    private String email;

    //来源渠道
    @Column(name = "`source_way`")
    private String sourceWay;

    //账号状态
    @Column(name = "`account_status`")
    private String accountStatus = AccountStatus.noopen.toString();
    //数据迁移状态
    @Column(name = "`move_status`")
    private String moveStatus = MoveStatus.nomove.toString();

    public Apply(String applyUserType, String unitName, String name, String phone, String workPhone,
                 String email, String sourceWay, String originalAccount, String accountType) {
        this.applyUserType = applyUserType;
        this.unitName = unitName;
        this.name = name;
        this.phone = phone;
        this.workPhone = workPhone;
        this.email = email;
        this.sourceWay = sourceWay;
        this.originalAccount = originalAccount;
        this.accountType = accountType;
        this.accountStatus = AccountStatus.noopen.toString();
        this.moveStatus = MoveStatus.nomove.toString();
    }


}
