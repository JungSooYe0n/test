package com.trs.netInsight.support.hybaseShard.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author:拓尔思信息股份有限公司
 *
 * @Description: hybase小库实体类
 *
 * @Date:Created in 15:03 2020/3/3
 *
 * @Created By yangyanyan
 */
@Table(name = "HYBASE_SHARD")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HybaseShard extends BaseEntity {

    /**
     * hybase节点连接
     */
    @Column(name = "hybase_server")
    private String hybaseServer;

    /**
     * hybase登录账号
     */
    @Column(name = "hybase_user")
    private String hybaseUser;

    /**
     * hybase登录密码
     */
    @Column(name = "hybase_pwd")
    private String hybasePwd;

    /**
     * 传统库表名
     */
    @Column(name = "tradition")
    private String tradition;

    /**
     * 微博库表名
     */
    @Column(name = "wei_bo")
    private String weiBo;

    /**
     * 微信库表名
     */
    @Column(name = "wei_xin")
    private String weiXin;

    /**
     * 海外库表名（推特与脸书数据）
     */
    @Column(name = "overseas")
    private String overseas;
    /**
     * 视频库表名 - 默认为"system2.media_200402"
     */
    @Column(name = "video")
    private String video;

    /**
     * 节点所属用户id或用户分组id
     */
    @Column(name = "owner_id")
    private String ownerId;
    /**
     * 机构id
     */
    @Column(name = "organization_id")
    private String organizationId;


    //保留字段1 ---- 小库使用范围：某用户、某用户分组、某机构、或某用户的某模块

    //保留字段2 ---- 是否开启使用小库

}

