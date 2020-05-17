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
package com.trs.netInsight.widget.tool.service.impl;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.TRSConnection;
import com.trs.hybase.client.TRSException;
import com.trs.hybase.client.TRSRecord;
import com.trs.hybase.client.TRSResultSet;
import com.trs.hybase.client.params.SearchParams;
import com.trs.netInsight.support.fts.annotation.parser.FtsParser;
import com.trs.netInsight.support.fts.builder.HomecomingBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.model.factory.HybaseFactory;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.tool.entity.HomecomingDocument;
import com.trs.netInsight.widget.tool.service.IHomecomingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 返乡日记分析服务接口实现类
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since yan.changjiang @ 2019/2/22
 */
@Service
@Slf4j
public class HomecomingServiceImpl implements IHomecomingService {

    /**
     * 最大检索页数
     */
    private static final int MAX_PAGE_SIZE = 512 * 2;

    @Override
    public Object getAnalysisData() throws TRSException {

        Map<String, Object> data = new HashMap<>();

        HomecomingBuilder builder = new HomecomingBuilder();
        builder.filterField("AREA", "", Operator.NotEqual);// 检索所有地域不为空的数据
        Map<String, Long> resultSet = categoryQuery(builder, "PROVINCE", Integer.MAX_VALUE);

        if (resultSet != null && resultSet.size() > 0) {
            // 地域图数据
            data.put("region", dealSimpleData(resultSet));
        }
        return data;
    }

    /**
     * 初步处理数据，
     *
     * @return
     */
    private List<Map<String, Object>> dealSimpleData(Map<String, Long> resultSet) throws TRSException {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> item;
        if (ObjectUtil.isNotEmpty(resultSet)) {
            HomecomingBuilder builder;
            PagedList<HomecomingDocument> page;
            HomecomingDocument document;
            for (Map.Entry<String, Long> set : resultSet.entrySet()) {
                item = new HashMap<>();
                item.put("province", set.getKey()); // 省份
                item.put("hot", set.getValue()); // 热度值

                // 检索相关文章
                builder = new HomecomingBuilder(0, 1);
                builder.filterField("PROVINCE", "\"" + set.getKey() + "\"", Operator.Equal);
                builder.orderBy("LABEL", true);//根据标签排序
                page = pageList(builder);
                if (page.size() > 0) {
                    document = page.getPageItems().get(0);
                    item.put("document", document);
                    item.put("label", document.getLabel().split(";"));
                    item.put("city", document.getCity());
                }
                data.add(item);
            }
        }
        return data;
    }

    /**
     * 根据检索构造器分类统计
     *
     * @param builder
     * @param groupField
     * @param top
     * @return
     */
    private Map<String, Long> categoryQuery(HomecomingBuilder builder, String groupField, int top) throws TRSException {
        TRSConnection connection = HybaseFactory.getClient();
        SearchParams searchParams = getParams();
        System.out.println("trsl:" + builder.asTRSL());
        TRSResultSet resultSet = connection.categoryQuery(builder.getDatabase(), builder.asTRSL(), "", groupField, top, searchParams);
        Map<String, Long> result = resultSet.getCategoryMap();
        // 按value由大到小排序
        return result;
    }

    /**
     * 根据检索构造器分页检索
     *
     * @param builder
     * @return
     */
    @Override
    public PagedList<HomecomingDocument> pageList(HomecomingBuilder builder) throws TRSException {
        TRSConnection connection = HybaseFactory.getClient();
        SearchParams searchParams = getParams();
        long from = (builder.getPageNo() < 0) ? 0 : builder.getPageNo() * builder.getPageSize();
        long recordNum = (builder.getPageSize() < 0) ? MAX_PAGE_SIZE : builder.getPageSize();
        System.out.println("trsl:" + builder.asTRSL());
        TRSResultSet resultSet = connection.executeSelect(builder.getDatabase(), builder.asTRSL(), from, recordNum, searchParams);
        List<HomecomingDocument> entities = new ArrayList<>();
        for (int i = 0; i < resultSet.size(); i++) {
            if (resultSet.moveTo(i)) {
                TRSRecord trsRecord = resultSet.get();
                entities.add(FtsParser.toEntity(trsRecord, HomecomingDocument.class));
            }
        }
        long count = resultSet.getNumFound();
        return new PagedList<>(builder.getPageSize() < 0 ? 0 : (int) builder.getPageNo(),
                (builder.getPageSize() < 0 ? MAX_PAGE_SIZE : builder.getPageSize()),
                (int) (count - Math.max(0, 0)), entities, 1);
    }


    /**
     * 获取检索参数
     *
     * @return
     */
    private SearchParams getParams() {
        SearchParams searchParams = new SearchParams();
        searchParams.setTimeOut(60);
        searchParams.setColorColumns(String.join(";", FtsParser.getSearchField(HomecomingDocument.class)));
        searchParams.setReadColumns(String.join(";", FtsParser.getSearchField(HomecomingDocument.class)));
        return searchParams;
    }

    @Override
    public Map<String, Long> getYears() throws TRSException {
        HomecomingBuilder builder = new HomecomingBuilder();
        builder.filterField("YEAR", "", Operator.NotEqual);
        Map<String, Long> yearMap = categoryQuery(builder, "YEAR", Integer.MAX_VALUE);
        return yearMap;
    }

    @Override
    public Map<String, Object> getConditions() throws TRSException {
        Map<String, Object> conditions = new HashMap<>();
        HomecomingBuilder builder = new HomecomingBuilder();
        builder.filterField("URLTITLE", "", Operator.NotEqual); // 检索所有数据

        // 获取年份数据
        Map<String, Long> yearsMap = categoryQuery(builder, "YEAR", Integer.MAX_VALUE);
        List<Map<String, String>> years = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(yearsMap)) {
            Map<String, String> item;
            for (Map.Entry<String, Long> year : yearsMap.entrySet()) {
                item = new HashMap<>();
                item.put("year", year.getKey());
                years.add(item);
            }
        }
        conditions.put("years", years);

        // 获取标签数据
        Map<String, Long> labelsMap = categoryQuery(builder, "LABEL", Integer.MAX_VALUE);
        List<Map<String, String>> labels = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(labelsMap)) {
            Map<String, String> item;
            for (Map.Entry<String, Long> label : labelsMap.entrySet()) {
                item = new HashMap<>();
                item.put("label", label.getKey());
                labels.add(item);
            }
        }
        conditions.put("labels", labels);
        return conditions;
    }
}

