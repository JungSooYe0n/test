package com.trs.netInsight.widget.special.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "hot_rating")
public class HotRating extends BaseEntity {
    @Column(name = "weibo_low")
    private int weiboLow = 10000;
    @Column(name = "weibo_middle")
    private int weiboMiddle = 15000;
    @Column(name = "weibo_high")
    private int weiboHigh = 20000;
    @Column(name = "weibo_vip_low")
    private int weiboVipLow = 100;
    @Column(name = "weibo_vip_middle")
    private int weiboVipMiddle = 150;
    @Column(name = "weibo_vip_high")
    private int weiboVipHigh = 200;
    @Column(name = "weixin_low")
    private int weixinLow = 200;
    @Column(name = "weixin_middle")
    private int weixinMiddle = 300;
    @Column(name = "weixin_high")
    private int weixinHigh = 500;
    @Column(name = "news_low")
    private int newsLow = 300;
    @Column(name = "news_middle")
    private int newsMiddle = 500;
    @Column(name = "news_high")
    private int newsHigh = 800;
    @Column(name = "app_low")
    private int appLow = 300;
    @Column(name = "app_middle")
    private int appMiddle = 500;
    @Column(name = "app_high")
    private int appHigh = 800;
    @Column(name = "zimeiti_low")
    private int zimeitiLow = 200;
    @Column(name = "zimeiti_middle")
    private int zimeitiMiddle = 300;
    @Column(name = "zimeiti_high")
    private int zimeitiHigh = 500;
    @Column(name = "luntan_low")
    private int luntanLow = 100;
    @Column(name = "luntan_middle")
    private int luntanMiddle = 150;
    @Column(name = "luntan_high")
    private int luntanHigh = 200;
    public HotRating(){

    }
//    public HotRating(int weiboLow,int weiboMiddle,int weiboHigh,int weixinLow,int weixinMiddle,int weixinHigh,int newsLow,int newsMiddle,int newsHigh,int appLow,int appMiddle,int appHigh,int zimeitiLow,int zimeitiMiddle,int zimeitiHigh,int luntanLow,int luntanMiddle,int luantanHigh){
//
//    }

}
