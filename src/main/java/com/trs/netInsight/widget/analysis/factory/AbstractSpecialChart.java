package com.trs.netInsight.widget.analysis.factory;

import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.service.IInfoListService;
import lombok.Setter;

/**
 *  专题分析图表抽象列
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2020/1/7 17:25.
 * @desc
 */
@Setter
public abstract class AbstractSpecialChart {
    /**
     * 栏目配置
     */
    protected SpecialChartConfig config;

    /**
     * 检索服务
     */
    protected IInfoListService infoListService;
    protected FullTextSearch hybase8SearchService;
//    protected IDistrictInfoService districtInfoService;

    /**
     * 预警 收藏
     */
    protected FavouritesRepository favouritesRepository;
    protected AlertRepository alertRepository;

    /**
     * 获取图表列表页数据
     * @param timeRange
     * @return
     * @throws TRSSearchException
     */
    public abstract Object getChartList(String timeRange) throws TRSSearchException;


    public abstract QueryBuilder createQueryBuilder();


    public abstract QueryCommonBuilder createQueryCommonBuilder();
}
