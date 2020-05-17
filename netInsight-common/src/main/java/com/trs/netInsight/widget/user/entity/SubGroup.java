package com.trs.netInsight.widget.user.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.user.entity.enums.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 *  分组管理
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/6/24 15:43.
 * 1`@desc
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "`sub_group`")
public class SubGroup extends BaseEntity {

    /**
     * 分组名称
     */
    @Column(name = "`name`")
    private String name;

    /**
     * logo图片
     */
    @Column(name = "`logo_pic_name`")
    private String logoPicName;



    //账号权限

    //---------------数据数量-------------

    /**
     * 用户上限 -1为不限，默认是5
     */
    @Column(name = "`user_limit`")
    private int userLimit = 5;

    /**
     * 日常监测栏目数  默认50
     */
    @Column(name = "`column_num`")
    private int columnNum = 50;

    /**
     * 专题事件数    默认10
     */
    @Column(name = "`special_num`")
    private int specialNum = 10;

    /**
     * 预警主题数   默认10
     */
    @Column(name = "`alert_num`")
    private int alertNum = 10;

    /**
     * 可绑定预警账号数  默认5
     */
    @Column(name = "`alert_account_num`")
    private int alertAccountNum = 5;

    /**
     * 有效期，永久为2050-01-01 00:00:00
     */
    @Column(name = "`expireat`")
    private String expireAt;

    /**
     * 账号状态 1为冻结 0为正常
     */
    @Column(name = "`status`")
    private String status = Status.frozen.getValue();

    /**
     *
     * 重写 set方法
     * @param status
     */
    public void setStatus(Status status) {
        this.status = status.getValue();
    }

       /**
     * 临时属性 今日登录次数
     */
    @Transient
    private int loginCount;

    /**
     * 机构剩余时间（不入库）
     */
    @Transient
    private String surplusDate;

    /**
     * 临时属性--用户
     */
    @Transient
    private User user;
    /**
     * 临时属性 机构名称
     */
    @Transient
    private String organizationName;

    /**
     * 用户
     */
    @Transient
    private List<User> users;

    //分组角色
    @ManyToMany(fetch = FetchType.EAGER) // 多对多外键关联的配置
    @JoinTable(name = "subgroup_roles", // 中间表的表名
            joinColumns = { @JoinColumn(name = "subgroup_id", referencedColumnName = "id") }, // 本表的主键
            inverseJoinColumns = { @JoinColumn(name = "subrole_id", referencedColumnName = "id") }) // 所映射表的主键
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Set<Role> roles;
}
