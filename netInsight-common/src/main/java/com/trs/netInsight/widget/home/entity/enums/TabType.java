package com.trs.netInsight.widget.home.entity.enums;

import lombok.Getter;

/**
 * 栏目页签类型
 *
 * Created by trs on 2017/6/19.
 */
public enum TabType {

    /**
     * 时间
     */
    TIME("IR_URLTIME"),

    /**
     * 地域
     */
    REGION("CATALOG_AREA"),

    /**
     * 来源
     */
    SOURCE("IR_GROUPNAME"),

    /**
     * 行业
     */
    INDUSTRY("IR_VRESERVED5"),

    /**
     * 情感
     */
    EMOTION("IR_APPRAISE"),

    /**
     * 普通的  测试微博热点信息    信息列表
     */
    INFO("keyword");
    @Getter
    String field;

    TabType(String field) {
        this.field = field;
    }
}
