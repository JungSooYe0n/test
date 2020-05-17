/*
 * Project: netInsight
 *
 * File Created at 2019/2/22
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.tool.entity;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * @since yan.changjiang @ 2019/2/22
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(indices = Const.HYBASE_HOMECOMING)
public class HomecomingDocument extends IDocument implements Serializable {


    /**
     * 唯一主键
     */
    @FtsField(value = "IR_SID")
    private String sid;

    /**
     * 标题
     */
    @FtsField(value = "IR_URLTITLE", highLight = true)
    private String urlTitle;

    /**
     * 链接
     */
    @FtsField(value = "IR_URLNAME")
    private String urlName;

    /**
     * 年份
     */
    @FtsField("YEAR")
    private String year;

    /**
     * 省份
     */
    @FtsField("PROVINCE")
    private String province;

    /**
     * 城市
     */
    @FtsField("CITY")
    private String city;

    /**
     * 地域
     */
    @FtsField("AREA")
    private String area;

    /**
     * 标签
     */
    @FtsField("LABEL")
    private String label;

    /**
     * 热度
     */
    @FtsField("HOT")
    private String hot;

}

