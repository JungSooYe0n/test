package com.trs.netInsight.widget.special.entity.enums;


/**
 * 页面枚举类型
 *
 * 20200324创建
 */
public enum SearchPage {
    /**
     * 共用搜索页面
     * 因为普通搜索页面需要单独对相似文章进行修改，所以把页面先分为两类，普通搜索页面，和其他公共页面
     */
    COMMON_SEARCH,

    /**
     * 普通搜索页面
     */
    ORDINARY_SEARCH
}
