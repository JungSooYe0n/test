package com.trs.netInsight.support.fts.annotation.enums;


/**
 * 数据库类型
 * 主要是指具体某个库
 *
 * Create by 张娅  2020.1.6
 */
public enum FtsHybaseType {
    /**
     * 传统媒体库
     */
    TRADITIONAL,
    /**
     * 微博库
     */
    WEIBO,
    /**
     * 微信库
     */
    WEIXIN,
    /**
     * 海外数据库  TF
     */
    OVERSEAS,
    /**
     * 视频库
     */
    VIDEO,
    /**
     * 手工录入库
     */
    INSERT,
    /**
     * 微博用户库
     */
    SINAUSER,
    /**
     * 混合库  包含微博 微信 传统 海外
     */
    MIX,
    /**
     * 已发预警详情库 - 默认的
     */
    ALERT,
    /**
     * 已发预警 类型 主要是 微信与APP  -- 默认的
     */
    ALERT_TYPE
}
