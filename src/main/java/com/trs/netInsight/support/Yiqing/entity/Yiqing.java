package com.trs.netInsight.support.Yiqing.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author lilyy
 * @date 2020/4/2 18:38
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "yiqing_data")
public class Yiqing implements Serializable {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "`id`")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "`value`", columnDefinition = "LONGTEXT")
    private String value;

    /**
     * 对象创建时间
     */
    @Column(name = "`created_time`", updatable = true)
    private Date createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = new Date();
    }
}
