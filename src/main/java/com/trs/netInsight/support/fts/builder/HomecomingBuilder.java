/*
 * Project: netInsight
 *
 * File Created at 2019/2/25
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.fts.builder;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.builder.condition.SearchCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.trs.netInsight.util.StringUtil.*;

/**
 * 返乡日记数据检索构造器
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since yan.changjiang @ 2019/2/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class HomecomingBuilder {

    private String database = Const.HYBASE_HOMECOMING;

    private Date startTime;
    private Date endTime;

    private List<SearchCondition> conditions = new ArrayList<>();

    private SearchCondition childSearchCondition = new SearchCondition();

    protected StringBuilder appendTRSL = new StringBuilder();

    private boolean server;//是否转换为server表达式

    private String keyRedis;//把表达式存入redis

    /**
     * 排序表达式
     */
    protected String orderBy;

    /**
     * 分页号
     */
    protected long pageNo = 0;

    /**
     * 分页长
     */
    protected int pageSize = 10;

    public HomecomingBuilder(long pageNo, int pageSize) {
        super();
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }


    /**
     * 添加检索条件,忽略重复的条件
     *
     * @param field    字段
     * @param operator Operator
     * @param value    值
     */
    private void addCondition(String field, Operator operator, String value) {
        SearchCondition c = new SearchCondition(field, value, operator);
        addCondition(c);
    }

    /**
     * 添加检索条件,忽略重复的条件
     *
     * @param field    字段
     * @param operator Operator
     * @param values   值
     */
    private void addCondition(String field, Operator operator, String[] values) {
        SearchCondition c = new SearchCondition(field, values, operator);
        addCondition(c);
    }

    /**
     * 添加检索条件,忽略重复的条件
     *
     * @param c SearchCondition
     */
    private void addCondition(SearchCondition c) {
        if (!conditions.contains(c)) {
            conditions.add(c);
        }
    }

    public String asTRSL() {
        Collections.sort(conditions);
        String trsl = join(conditions.toArray(), " AND ");
        String result = isEmpty(trsl) ? appendTRSL.toString()
                : appendTRSL.length() > 0 ? trsl + " AND " + appendTRSL.toString() : trsl;
//        if (log.isDebugEnabled()) {
//            log.debug("trs search build result : [" + result + "]");
//        }
//		return fmtTrsl(result);
        return result;
    }

    public HomecomingBuilder filterField(String fieldName, String fieldValue, Operator op) {
        if (!isEmpty(fieldName) && !isEmpty(fieldValue)) {
            addCondition(fieldName, op, fieldValue);
        }
        return this;
    }

    public HomecomingBuilder filterField(String fieldName, String[] fieldValues, Operator op) {
        if (!isEmpty(fieldName) && fieldValues != null && fieldValues.length > 0) {
            addCondition(fieldName, op, fieldValues);
        }
        return this;
    }

    public HomecomingBuilder filterFieldsByWeight(LinkedHashMap<String, Integer> fieldsWeightMap, String keyword,
                                                  boolean escape) {
        StringBuilder buffer = new StringBuilder();
        if (fieldsWeightMap != null && fieldsWeightMap.size() > 0 && isNotEmpty(keyword)) {
            List<Map.Entry<String, Integer>> entrys = new ArrayList<>();
            entrys.addAll(fieldsWeightMap.entrySet());
            for (int i = 0; i < entrys.size(); i++) {
                buffer.append("(").append(entrys.get(i).getKey()).append(":(").append(keyword).append(")^")
                        .append(entrys.get(i).getValue()).append(")");
                if (i != entrys.size() - 1) {
                    buffer.append(" OR ");
                }
            }
            this.filterByTRSL(buffer.toString());
        }
        return this;
    }

    public HomecomingBuilder filterByTRSL(String trsl) {
        if (isEmpty(trsl)) {
            return this;
        }
        if (appendTRSL.length() > 0) {
            appendTRSL.append(" AND ");
        }
        this.appendTRSL.append("(").append(trsl).append(")");
        return this;
    }

    public HomecomingBuilder filterChildField(String fieldName, String fieldValue, Operator op) {
        if (!isEmpty(fieldName)) {
            addCondition(childSearchCondition.addChildCondition(new SearchCondition(fieldName, fieldValue, op)));
        }
        return this;
    }

    public HomecomingBuilder orderBy(String field, boolean desc) {
        if (!isEmpty(field)) {
            this.orderBy = (desc ? "-" : "+") + field;
        }
        return this;
    }

    public HomecomingBuilder page(long pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        return this;
    }
}

