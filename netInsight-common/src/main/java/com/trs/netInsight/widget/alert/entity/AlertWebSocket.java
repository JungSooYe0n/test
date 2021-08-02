package com.trs.netInsight.widget.alert.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="alert_websocket")
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AlertWebSocket{

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;//自增项Id

    @Column(name="receive_id")//接收用户
    private String receiveid;

    @Column(name="receive_message")//接收数据
    private String receivemessage;

    @Column(name="receive_time")//数据原时间-从查出数据中取出
    private String receivetime;

    @Column(name="receive_from")//数据来源
    private String receivefrom;

    @Column(name="receive_url")//数据原地址
    private String receiveurl;

    @CreatedDate
    @Column(name="create_date")//创建时间，用来排序
    private Date createdate;


}
