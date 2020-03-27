package com.trs.netInsight.widget.report.entity.enums;

/**
 *  导出列表的类型
 *  主要是在导出列表页前多少条时使用
 *
 * Created by zhangya 2019/12/03
 */
public enum ExportListType {
    /**
     * 普通列表
     */
    COMMON,
    /**
     * 普通搜索列表页
     */
    ORDINARYSEARCH,
    /**
     * 高级搜索列表页
     */
    ADVANCEDSEARCH,

    /**
     * 相似文章列表  - > 默认排序方式不同
     */
    SIM,

    /**
     * 专题分析图表挑列表  - > 默认排序方式不同
     */
    CHART2LIST,

    /**
     * 普通预警列表  - > 检索时间字段不同
     */
    ALERT,

    /**
     * 已发预警列表
     */
    SENDALERT,

    /**
     * 站内预警列表
     */
    SMSALERT,

    /**
     * 我的收藏列表
     */
    COLLECT,

    /**
     * 素材库列表
     */
    LIBRARY

}
