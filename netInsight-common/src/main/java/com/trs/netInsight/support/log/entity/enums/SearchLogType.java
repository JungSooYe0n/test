package com.trs.netInsight.support.log.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 检索时间超长模块类型
 * @author 马加鹏
 * @date 2021/4/21 16:11
 */
@AllArgsConstructor
@Getter
public enum SearchLogType {

    /**
     *  专题分析
     */
    SPECIAL("专题分析"),
    /**
     *  日常监测
     */
    COLUMN("日常监测");

    private String value;

}

