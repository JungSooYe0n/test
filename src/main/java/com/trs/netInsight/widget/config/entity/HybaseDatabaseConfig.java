package com.trs.netInsight.widget.config.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @Type HybaseDatabaseConfig.java
 * @author 张娅
 * @date 2020年1月6日
 * @version
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "hd_config")
public class HybaseDatabaseConfig implements Serializable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "`id`", length = 100)
    private String id;

    /**
     * 对象创建时间
     */
    @Column(name = "`created_time`", updatable = true)
    private Date createdTime;

    /**
     * 微博库
     */
    @Column(name = "weibo", columnDefinition="varchar(100) COMMENT '微博库'")
    private String weibo;
    /**
     * 微博用户库
     */
    @Column(name = "sinaweibo_users", columnDefinition="varchar(100) COMMENT '微博用户库'")
    private String sinaweiboUsers;
    /**
     * 传统媒体库
     */
    @Column(name = "traditional", columnDefinition="varchar(100) COMMENT '传统库'")
    private String traditional;
    /**
     * 微信库
     */
    @Column(name = "weixin", columnDefinition="varchar(100) COMMENT '微信库'")
    private String weixin;
    /**
     * 海外数据库  TF
     */
    @Column(name = "overseas", columnDefinition="varchar(100) COMMENT 'TF库'")
    private String overseas;
    /**
     * 手工录入库
     */
    @Column(name = "insert_entry", columnDefinition="varchar(100) COMMENT '手工录入库'")
    private String insert;
}
