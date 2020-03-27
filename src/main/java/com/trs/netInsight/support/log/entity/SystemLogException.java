package com.trs.netInsight.support.log.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author lilyy
 * @date 2020/2/20 16:38
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "system_log_exception")
public class SystemLogException implements Serializable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "`id`")
    private String id;

    @Column(name = "`system_log_id`")
    private String systemLogId;

    @Lob
    @Column(name = "`system_log_exception`")
    private String systemLogException;

    /**
     * 对象创建时间
     */
    @Column(name = "`created_time`", updatable = true)
    private Date createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = new Date();
    }

    public SystemLogException(String systemLogId,String systemLogException){
        this.systemLogId = systemLogId;
        this.systemLogException = systemLogException;
    }


}
