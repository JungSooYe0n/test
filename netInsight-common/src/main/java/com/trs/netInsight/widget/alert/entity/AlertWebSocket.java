package com.trs.netInsight.widget.alert.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="alert_websocket")
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AlertWebSocket{

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    private int id;

    @Column(name="receive_id")
    private String receiveid;

    @Column(name="receive_message")
    private String receivemessage;

    @Column(name="receive_time")
    private String receivetime;

    @Column(name="receive_from")
    private String receivefrom;

    @Column(name="receive_url")
    private String receiveurl;

    @Column(name="timestamp")
    private String timestamp;


}
