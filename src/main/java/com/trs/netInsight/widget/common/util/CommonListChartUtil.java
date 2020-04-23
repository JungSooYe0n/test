package com.trs.netInsight.widget.common.util;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.model.result.IQueryBuilder;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonListChartUtil {

    /**
     * 统一转化querybuilder
     * @param builder
     * @return
     */
    public static  <T extends IQueryBuilder> QueryBuilder formatQueryBuilder(T builder){
        QueryBuilder queryBuilder = new QueryBuilder();
        if (builder instanceof QueryBuilder) {
            queryBuilder = (QueryBuilder) builder;
        } else if (builder instanceof QueryCommonBuilder) {
            QueryCommonBuilder commonBuilder = (QueryCommonBuilder) builder;
            queryBuilder.filterByTRSL(commonBuilder.asTRSL());
            queryBuilder.setDatabase(StringUtil.join(commonBuilder.getDatabase(), ";"));
            queryBuilder.setOrderBy(commonBuilder.getOrderBy());
            queryBuilder.setStartTime(commonBuilder.getStartTime());
            queryBuilder.setEndTime(commonBuilder.getEndTime());
            queryBuilder.page(commonBuilder.getPageNo(),commonBuilder.getPageSize());
        }
        queryBuilder.setPageSize(queryBuilder.getPageSize()>=1?queryBuilder.getPageSize():10);
        queryBuilder.setPageNo(queryBuilder.getPageNo()>=0?queryBuilder.getPageNo():0);
        String database = queryBuilder.getDatabase();
        if (database == null) {
            queryBuilder.setDatabase(Const.MIX_DATABASE);
        }
        return queryBuilder;
    }


    /**
     * 重载 为栏目模块的xy轴弄的
     *
     * @param trsl
     * @return
     */
    public static List<CategoryBean> getMediaType(String trsl) {
        List<CategoryBean> list = new ArrayList<>();
        String newXyTrsl = trsl.replaceAll("\n", "");
        String[] medias = newXyTrsl.split("[;|；]");
        if (medias.length > 0) {
            for (String str : medias) {
                if (StringUtils.isNotBlank(str)) {
                    String key = str.substring(0, str.indexOf("="));
                    String val = str.substring(str.indexOf("=") + 1, str.length());
                    list.add(new CategoryBean(key, val));
                }
            }
        }
        return list;
    }

    public static Set<String> formatGroupName(String groupName) throws TRSException{
        if("ALL".equals(groupName)){
            groupName = Const.STATTOTAL_GROUP;
        }
        String[] split = groupName.split("[;|；]");
        Set<String> sourceList = new HashSet<>();
        for (String str : split) {
            if (Const.SOURCE_GROUPNAME_CONTRAST.containsKey(str)) {
                sourceList.add(Const.SOURCE_GROUPNAME_CONTRAST.get(str));
            } else {
                throw new TRSException("传入了不能识别的groupName类型：" + str);
            }
        }
        return sourceList;
    }


}
