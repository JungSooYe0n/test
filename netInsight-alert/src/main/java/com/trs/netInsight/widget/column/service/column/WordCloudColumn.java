package com.trs.netInsight.widget.column.service.column;

import java.util.*;


import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.GroupWordInfo;
import com.trs.netInsight.support.fts.model.result.GroupWordResult;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;

/**
 * 词云列表
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
public class WordCloudColumn extends AbstractColumn {

    @Override
    public Object getColumnData(String timeRaneg) throws TRSSearchException {

        IndexTab indexTab = super.config.getIndexTab();
        //url排重
        boolean irSimflag = indexTab.isIrSimflag();
        boolean sim = indexTab.isSimilar();
        boolean irSimflagAll = indexTab.isIrSimflagAll();
        //记录searchTimeLongLog日志
        SearchTimeLongUtil.execute(indexTab.getName(), indexTab.getTimeRange());
        //用queryCommonBuilder和QueryBuilder 是一样的的
        QueryCommonBuilder queryBuilder = super.config.getCommonBuilder();
        Integer pagesize = 50;
        if ("100".equals(indexTab.getTabWidth())) {
            pagesize = 100;
        }
        queryBuilder.page(0, pagesize);
        String metas = indexTab.getGroupName();
        try {
            ChartResultField resultField = new ChartResultField("name", "value","entityType");
            Object wordCloud = commonChartService.getWordCloudColumnData(queryBuilder, sim, irSimflag, irSimflagAll, metas, config.getEntityType(), "column",resultField);
            return wordCloud;
        } catch (TRSException | TRSSearchException e) {
            throw new TRSSearchException(e);
        }
    }

    @Override
    public Object getColumnCount() throws TRSSearchException {
        return null;
    }

    @Override
    public Object getSectionList() throws TRSSearchException {
        // 取userId
        User loginUser = UserUtils.getUser();
        IndexTab indexTab = super.config.getIndexTab();
        boolean sim = indexTab.isSimilar();
        boolean irSimflag = indexTab.isIrSimflag();
        boolean irSimflagAll = indexTab.isIrSimflagAll();
        //当前列表选中的数据源
        String checkGroupName = super.config.getGroupName();
        try {
            //用queryCommonBuilder和QueryBuilder 是一样的的
            QueryCommonBuilder queryBuilder = super.config.getCommonBuilder();
            queryBuilder.setPageNo(config.getPageNo());
            queryBuilder.setPageSize(config.getPageSize());
            String irKeyword = this.config.getKey();
            if (StringUtil.isNotEmpty(irKeyword)) {
                String entityType = this.config.getEntityType();
                //queryBuilder.filterField(Const.PARAM_MAPPING.get(entityType), irKeyword, Operator.Equal);
                if ("location".equals(entityType) && !"省".equals(irKeyword.substring(irKeyword.length() - 1)) && !irKeyword.contains("自治区")) {
                    String irKeywordNew = "";
                    if ("市".equals(irKeyword.substring(irKeyword.length() - 1))) {
                        irKeywordNew = irKeyword.replace("市", "");
                    } else {
                        irKeywordNew = irKeyword;
                    }
                    if (!irKeywordNew.contains("\"")) {
                        irKeywordNew = "\"" + irKeywordNew + "\"";
                    }
                    String trsl = FtsFieldConst.FIELD_URLTITLE + ":(" + irKeywordNew + ") OR " + FtsFieldConst.FIELD_CONTENT + ":(" + irKeywordNew + ")";
                    queryBuilder.filterByTRSL(trsl);
                } else {
                    queryBuilder.filterField(Const.PARAM_MAPPING.get(entityType), irKeyword, Operator.Equal);
                }
            }
            if ("ALL".equals(checkGroupName)) {
                checkGroupName = indexTab.getGroupName();
            }
            //处理数据源
            checkGroupName = StringUtils.join(CommonListChartUtil.formatGroupName(checkGroupName), ";");

            if ("hot".equals(this.config.getOrderBy())) {
                return commonListService.queryPageListForHot(queryBuilder, checkGroupName, loginUser, "column", true);
            } else {
                return commonListService.queryPageList(queryBuilder, sim, irSimflag, irSimflagAll, checkGroupName, "column", loginUser, true);
            }
        } catch (TRSException e) {
            throw new TRSSearchException(e);
        }
    }

    /**
     * 信息列表统计 - 但是页面上的信息列表统计不受栏目类型影响，所以只需要用普通列表的这个方法即可
     * 对应为信息列表的数据源条数统计
     *
     * @return
     * @throws TRSSearchException
     */
    @Override
    public Object getListStattotal() throws TRSSearchException {
        return null;
    }

    @Override
    public Object getAppSectionList(User user) throws TRSSearchException {
        return null;
    }

    @Override
    public QueryBuilder createQueryBuilder() {
        QueryBuilder builder = super.config.getQueryBuilder();
        return builder;

    }

    @Override
    public QueryCommonBuilder createQueryCommonBuilder() {
        QueryCommonBuilder builder = super.config.getCommonBuilder();
        int pageNo = super.config.getPageNo();
        int pageSize = super.config.getPageSize();
        builder.setPageNo(pageNo);
        if (pageSize != 0) {
            builder.setPageSize(pageSize);
        }
        return builder;
    }

}
