package com.trs.netInsight.widget.home.entity.enums;

import lombok.Getter;

/**
 * 栏目类型枚举
 *
 * Created by trs on 2017/6/19.
 */
public enum  ColumnType {

    /**
     * 预警列表，图表
     */
    ALERT_LIST(false), ALERT_CHART(false),

    /**
     * 热点信息， 地域分布
     */
    HOT_INFO(true), DISTRICT(true),

    /**
     * 微博热点图
     */
    HOT_MBLOG(false),

    /**
     * 新闻榜单, 热搜关键词，热搜人物
     */
    NEWS_FOCUS(false), SEARCH_FOCUS_W(false),SEARCH_FOCUS_P(false),

    /**
     * 信息轮播
     */
    INFO_CAROUSEL(true),

    /**
     * 专项趋势
     */
    COUNT_AND_TREND(false),

    /**
     * 分类信心列表
     */
    CLASSIFY_LIST(true),

    WORD_CLOUD(false),

    /**
     * 专项推荐
     */
    SPECIAL_RECMD(false);


    /**
     * 是否可重复添加
     */
    @Getter
    boolean repeatable;

    ColumnType(boolean repeatable) {
        this.repeatable = repeatable;
    }

}
