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
package com.trs.netInsight.widget.tool.service;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.TRSException;
import com.trs.netInsight.support.fts.builder.HomecomingBuilder;
import com.trs.netInsight.widget.tool.entity.HomecomingDocument;

import java.util.Map;

/**
 * 返乡日记分析服务接口
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since yan.changjiang @ 2019/2/22
 */
public interface IHomecomingService {

    /**
     * 获取返乡日记分析数据
     *
     * @return
     */
    Object getAnalysisData() throws TRSException;

    /**
     * 根据检索构造器分页检索
     *
     * @param builder
     * @return
     */
    PagedList<HomecomingDocument> pageList(HomecomingBuilder builder) throws TRSException;

    /**
     * 获取数据中的年份
     *
     * @return
     * @throws TRSException
     */
    Map<String, Long> getYears() throws TRSException;

    /**
     * 获取筛选条件
     *
     * @return
     * @throws TRSException
     */
    Map<String, Object> getConditions() throws TRSException;
}
