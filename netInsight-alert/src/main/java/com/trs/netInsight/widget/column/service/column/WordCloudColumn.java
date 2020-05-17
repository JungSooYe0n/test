package com.trs.netInsight.widget.column.service.column;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.model.result.GroupWordInfo;
import com.trs.netInsight.support.fts.model.result.GroupWordResult;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        GroupWordResult wordCloud = null;
        //用queryCommonBuilder和QueryBuilder 是一样的的
        QueryCommonBuilder queryBuilder = super.config.getCommonBuilder();
        queryBuilder.page(0, 50);
        String metas = indexTab.getGroupName();
        try {
            wordCloud = (GroupWordResult) commonChartService.getWordCloudColumnData(queryBuilder, sim, irSimflag, irSimflagAll, metas, config.getEntityType(), "column");

            if (wordCloud == null || wordCloud.getGroupList() == null || wordCloud.getGroupList().size() == 0) {
                return null;
            }
            List<GroupWordInfo> groupWordInfos = wordCloud.getGroupList();
            List<Object> result = new ArrayList<>();
            Map<String, Object> map = null;
            for (GroupWordInfo wordInfo : wordCloud) {
                map = new HashMap<>();
                map.put("name", wordInfo.getFieldValue());
                map.put("value", wordInfo.getCount());
                map.put("entityType", wordInfo.getEntityType());
                result.add(map);
            }
            return result;
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
            String irKeyword = this.config.getIrKeyword();
            if (StringUtil.isNotEmpty(irKeyword)) {
                String entityType = this.config.getEntityType();
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
            String emotion = super.config.getEmotion();
            if (StringUtil.isNotEmpty(emotion) && !"ALL".equals(emotion)) {
                queryBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
            }
            String area = super.config.getArea();
            // 取地域名
            if (!"ALL".equals(area)) { // 地域
                String[] areaSplit = area.split(";");
                String contentArea = "";
                for (int i = 0; i < areaSplit.length; i++) {
                    areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
                    if (i != areaSplit.length - 1) {
                        areaSplit[i] += " OR ";
                    }
                    contentArea += areaSplit[i];
                }
                queryBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
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
