package com.trs.netInsight.widget.analysis.specialchart;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.analysis.factory.AbstractSpecialChart;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.user.entity.User;

/**
 * 来源类型统计
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2020/1/7 18:08.
 * @desc
 */
public class WebCountChart extends AbstractSpecialChart {
    @Override
    public Object getChartList(String timeRange) throws TRSSearchException {
        SpecialProject specialProject = this.config.getSpecialProject();
        int pageNo = this.config.getPageNo();
        int pageSize = this.config.getPageSize();
        String groupName = this.config.getGroupName();
        QueryBuilder builder = this.config.getQueryBuilder();
        //排序
        if ("hot".equals(this.config.getOrderBy())){
            User user = UserUtils.getUser();
            // 选择数据库
            String source = specialProject.getSource();
            String database = builder.getDatabase();
           if ("ALL".equals(groupName)){
               String[] databases = TrslUtil.chooseDatabases(specialProject.getSource().split(";"));
               database = StringUtil.join(databases,";");
           }
           builder.setDatabase(database);
            try {
                return super.infoListService.getHotList(builder, builder, user,"column");
            } catch (TRSException e) {
                throw new TRSSearchException(e);
            }
        }

        //分类统计字段值填入
        //来源

        //查询
        builder.setDatabase(Const.WEIBO);
        return null;
    }

    @Override
    public QueryBuilder createQueryBuilder() {
        return null;
    }

    @Override
    public QueryCommonBuilder createQueryCommonBuilder() {
        return null;
    }
}
