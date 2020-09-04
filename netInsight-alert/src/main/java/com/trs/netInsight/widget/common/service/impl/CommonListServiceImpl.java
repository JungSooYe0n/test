package com.trs.netInsight.widget.common.service.impl;


import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.cache.TimingCachePool;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.fts.model.result.IQueryBuilder;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.hybaseRedis.HybaseRead;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.service.IFavouritesService;
import com.trs.netInsight.widget.special.entity.AsySiteNameDocument;
import com.trs.netInsight.widget.special.entity.AsyncDocument;
import com.trs.netInsight.widget.special.entity.AsyncInfo;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 公共信息查询 - 调用hybase的方法
 * 包括 信息列表、相似文章、各类图表统计的基础方法
 */
@Service
@Slf4j
//@EnableAspectJAutoProxy(exposeProxy = true)
public class CommonListServiceImpl implements ICommonListService {

    @Autowired
    private FullTextSearch hybase8SearchServiceNew;
    @Autowired
    private IFavouritesService favouritesService;
    @Autowired
    @Lazy
    private ICommonListService commonListService;

    /**
     * 线程池跑任务
     */
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);


    /**
     * 统一整理信息列表信息返回数据
     *
     * @param user              当前用户
     * @param pageId            当前页面id
     * @param nextPageId        下一页id
     * @param pagedList         当前页信息
     * @param database          列表页查询时用的数据库
     * @param type              查询类型，对应用户限制查询时间的模块相同
     * @param isCalculateSimNum 是否计算相似文章数
     * @return
     */
    private InfoListResult formatPageListResult(User user, String pageId, String nextPageId, PagedList<FtsDocumentCommonVO> pagedList, String database, String type, Boolean isCalculateSimNum) {
        List<FtsDocumentCommonVO> list = pagedList.getPageItems();

        List<Favourites> favouritesList = favouritesService.findAll(user);
        List<FtsDocumentCommonVO> ftsList = new ArrayList<>();
        List<String> md5List = new ArrayList<>();

        String trslk = pageId + "trslk";
        String trslkHot = pageId + "hot";


        if (list.size() > 0) {
            for (FtsDocumentCommonVO document : list) {
                String content = "";
                if (StringUtil.isNotEmpty(document.getContent())) {
                    List<String> imgSrcList = StringUtil.getImgStr(document.getContent());
                    if (imgSrcList != null && imgSrcList.size() > 0) {
                        document.setImgSrc(imgSrcList.get(0));
                    }
                    document.setExportContent(document.getContent());
                    content = StringUtil.replaceImg(document.getContent());
                    document.setContent(StringUtil.cutContentByFont(StringUtil.replaceImg(document.getContent()), Const.CONTENT_LENGTH));
                    document.setStatusContent(StringUtil.cutContentByFont(StringUtil.replaceImg(document.getContent()), Const.CONTENT_LENGTH));
                }
                if (StringUtil.isNotEmpty(document.getAbstracts())) {
                    document.setAbstracts(StringUtil.cutContentByFont(StringUtil.replaceImg(document.getAbstracts()), Const.CONTENT_LENGTH));
                }
                if (StringUtil.isNotEmpty(document.getTitle())) {
                    document.setTitle(StringUtil.replaceAnnotation(document.getTitle()).replace("&amp;nbsp;", ""));
                }
                if (Const.MEDIA_TYPE_WEIXIN.contains(document.getGroupName())) {
                    document.setSid(document.getHkey());
                    // 检验收藏
                    document.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(document.getHkey())));
                } else {
                    // 检验收藏
                    document.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(document.getSid())));
                }
                if (Const.MEDIA_TYPE_TF.contains(document.getGroupName())) {
                    document.setSiteName(document.getGroupName());
                    document.setTitle(content);
                }else if(Const.MEDIA_TYPE_WEIBO.contains(document.getGroupName())){
                    document.setTitle(content);
                }
                // 控制标题长度
                document.setId(document.getSid());
                document.setGroupName(CommonListChartUtil.formatPageShowGroupName(document.getGroupName()));
                document.setTrslk(trslk);
                md5List.add(document.getMd5Tag());
                ftsList.add(document);

            }
            final String trsSim = trslkHot;
            //根据参数计算相似文章数才计算相似文章数，基本的都需要计算相似文章数，但是存在个别不需要，例如API个别方法，详情页、导出
            if (isCalculateSimNum) {
                //最后一个参数为是否计算相似文章对应的发文网站，现在统一设置成true
                fixedThreadPool.execute(() -> calculateSimNum(String.join(";", database), pageId, ftsList, trsSim, type, true));
            }

        }
        return new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk);
    }


    /**
     * 分页查询信息列表数据  --  混合列表
     * 返回数据被处理
     * 要查询的数据源和数据库通过参数groupName控制，querybuilder中尽量少出现数据源
     *
     * @param queryBuilder      查询构造器
     * @param sim               单一媒体排重
     * @param irSimflag         站内排重
     * @param irSimflagAll      全网排重
     * @param groupName         要查询的数据源，用;分割，当前列表的可查询数据源的总的
     * @param type              查询类型，对应用户限制查询时间的模块相同
     * @param user              当前用户信息
     * @param isCalculateSimNum 是否计算相似文章数
     * @return
     * @throws TRSException
     */
    @Override
    public <T extends IQueryBuilder> InfoListResult queryPageList(T queryBuilder, boolean sim,
                                                                  boolean irSimflag, boolean irSimflagAll, String groupName, String type, User user, Boolean isCalculateSimNum) throws TRSException {

        final String pageId = GUIDGenerator.generate(CommonListServiceImpl.class);
        final String nextPageId = GUIDGenerator.generate(CommonListServiceImpl.class);
        try {
            QueryCommonBuilder builder = null;
            if (StringUtil.isNotEmpty(groupName)) {
                builder = (QueryCommonBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(queryBuilder, groupName, 1);
            } else {
                builder = CommonListChartUtil.formatQueryCommonBuilder(queryBuilder);
            }
            if (builder == null) {
                return null;
            }
            builder.setPageSize(builder.getPageSize() >= 1 ? builder.getPageSize() : 10);
            builder.setPageNo(builder.getPageNo() >= 0 ? builder.getPageNo() : 0);

            String trslk = pageId + "trslk";
            RedisUtil.setString(trslk, builder.asTRSL());
            builder.setKeyRedis(trslk);
            log.info("正式列表查询表达式：" + builder.asTRSL());
            String trslkHot = pageId + "hot";
            RedisUtil.setString(trslkHot, builder.asTRSL());
            PagedList<FtsDocumentCommonVO> pagedList = queryPageListBase(builder, sim, irSimflag, irSimflagAll, type);
            if (pagedList != null && pagedList.getPageItems() != null && pagedList.getPageItems().size() >0) {
                //统一处理返回数据的格式
                return formatPageListResult(user, pageId, nextPageId, pagedList, StringUtil.join(builder.getDatabase(), ";"), type, isCalculateSimNum);
            } else {
                return null;
            }
        } catch (Exception e) {
            if (e.getMessage().contains("检索超时")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_TIMEOUT,
                        e);
            }else if (e.getMessage().contains("表达式过长")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_EXCEPTION,
                        e);
            }
            throw new OperationException("检索异常：", e);
        }

    }

    /**
     * 普通信息列表
     * 调用hybase查询数据，直接查询，不做处理  --  混合列表
     * 返回数据未被处理
     * 当查询数据的条数超过10000条时，查询的数据必定为无序的
     * 要查询的数据源和数据库通过参数groupName控制，querybuilder中尽量少出现数据源
     *
     * @param queryBuilder 查询构造器
     * @param sim          单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @param groupName    要查询的数据源，用;分割，当前列表的可查询数据源的总的
     * @return
     * @throws TRSException
     */
    @Override
    public <T extends IQueryBuilder> PagedList<FtsDocumentCommonVO> queryPageListNoFormat(T queryBuilder, boolean sim,
                                                                                          boolean irSimflag, boolean irSimflagAll, String type, String groupName) throws TRSException {
        QueryCommonBuilder builder = null;
        if (StringUtil.isNotEmpty(groupName)) {
            builder = (QueryCommonBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(queryBuilder, groupName, 1);
        } else {
            builder = CommonListChartUtil.formatQueryCommonBuilder(queryBuilder);
        }
        if (builder == null) {
            return null;
        }
        PagedList<FtsDocumentCommonVO> pagedList =  queryPageListBase(builder, sim, irSimflag, irSimflagAll, type);
        if (pagedList == null || pagedList.getPageItems() == null || pagedList.getPageItems().size() == 0) {
            return null;
        }else{
            return pagedList;
        }
    }

    /**
     * 普通信息列表
     * 调用hybase查询数据，直接查询，不做处理  --  混合列表（需要标注要查询的数据源）
     * 返回数据未被处理
     * 当查询数据的条数超过10000条时，查询的数据必定为无序的
     *
     * @param builder      查询构造器
     * @param sim          单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSException
     */

    private PagedList<FtsDocumentCommonVO> queryPageListBase(QueryCommonBuilder builder, boolean sim,
                                                             boolean irSimflag, boolean irSimflagAll, String type) throws TRSException {
        try {
            if (builder == null) {
                return null;
            }
            long startTime = System.currentTimeMillis();
            PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchServiceNew.pageListCommon(builder, sim, irSimflag, irSimflagAll, type);
            long endTime = System.currentTimeMillis();
            log.error("间隔HY时间：" + (endTime - startTime));
            return pagedList;
        } catch (Exception e) {
            if (e.getMessage().contains("检索超时")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_TIMEOUT,
                        e);
            }else if (e.getMessage().contains("表达式过长")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_EXCEPTION,
                        e);
            }
            throw new OperationException("检索异常：", e);
        }
    }


    /**
     * 分页查询信息列表数据  --  混合列表
     * -- 列表无序，从hybase拿到的数据为：符合条件的随机数据，混合库可能只拿其中某个库
     * 当查询数据的条数超过10000条时，查询的数据必定为无序的
     * 返回数据未被处理
     * <p>
     * 要查询的数据源和数据库通过参数groupName控制，querybuilder中尽量少出现数据源
     *
     * @param queryBuilder 查询构造器
     * @param sim          单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @param groupName    要查询的数据源，用;分割，当前列表的可查询数据源的总的
     * @return
     * @throws TRSException
     */
    @Override
    public <T extends IQueryBuilder> PagedList<FtsDocumentCommonVO> queryPageListNoSort(T queryBuilder, boolean sim,
                                                                                        boolean irSimflag, boolean irSimflagAll, String type, String groupName) throws TRSException {
        try {
            QueryCommonBuilder builder = null;
            if (StringUtil.isNotEmpty(groupName)) {
                builder = (QueryCommonBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(queryBuilder, groupName, 1);
            } else {
                builder = CommonListChartUtil.formatQueryCommonBuilder(queryBuilder);
            }
            if (builder == null) {
                return null;
            }
            long startTime = System.currentTimeMillis();
            PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchServiceNew.pageListCommonForExport(builder, sim, irSimflag, irSimflagAll, type);
            long endTime = System.currentTimeMillis();
            log.error("间隔HY时间：" + (endTime - startTime));
            if (pagedList == null || pagedList.getPageItems() == null || pagedList.getPageItems().size() == 0) {
                return null;
            }else{
                return pagedList;
            }
        } catch (Exception e) {
            if (e.getMessage().contains("检索超时")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_TIMEOUT,
                        e);
            }else if (e.getMessage().contains("表达式过长")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_EXCEPTION,
                        e);
            }
            throw new OperationException("检索异常：", e);
        }

    }

    /**
     * 分页查询热度信息列表  --  混合列表
     * 返回数据被处理 - 主要是前端页面展示
     * 列表按热度进行排序
     * <p>
     * *要查询的数据源和数据库通过参数groupName控制，querybuilder中尽量少出现数据源
     *
     * @param queryBuilder      查询构造器
     * @param groupName         要查询的数据源，用;分割，当前列表的可查询数据源的总的  -- 统一处理数据源
     * @param user              当前用户信息
     * @param type              查询类型，对应用户限制查询时间的模块相同
     * @param isCalculateSimNum 是否计算相似文章数
     * @return
     * @throws TRSException
     */
    @Override
    public <T extends IQueryBuilder> InfoListResult queryPageListForHot(T queryBuilder, String groupName, User user, String type, Boolean isCalculateSimNum) throws TRSException {
        final String pageId = GUIDGenerator.generate(CommonListServiceImpl.class);
        final String nextPageId = GUIDGenerator.generate(CommonListServiceImpl.class);
        try {
            QueryBuilder builder = null;
            if (StringUtil.isNotEmpty(groupName)) {
                builder = (QueryBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(queryBuilder, groupName, 0);

            } else {
                builder = CommonListChartUtil.formatQueryBuilder(queryBuilder);
            }
            if (builder == null) {
                return null;
            }
            String database = builder.getDatabase();
            if (database == null) {
                builder.setDatabase(Const.MIX_DATABASE);
            }
            int pageSize = builder.getPageSize();
            if (pageSize > 50) {
                builder.setPageSize(50);
            } else if (pageSize <= 0) {
                builder.setPageSize(10);
            }
            builder.setPageNo(builder.getPageNo() >= 0 ? builder.getPageNo() : 0);

            String trslk = pageId + "trslk";
            RedisUtil.setString(trslk, builder.asTRSL());
            builder.setKeyRedis(trslk);
            log.info("正式列表查询表达式：" + builder.asTRSL());
            String trslkHot = pageId + "hot";
            RedisUtil.setString(trslkHot, builder.asTRSL());
            //在方法开始时拼接过数据源，则这个不再拼接
            PagedList<FtsDocumentCommonVO> pagedList = queryPageListForHotBase(builder, type);
            if (pagedList == null || pagedList.getPageItems() == null || pagedList.getPageItems().size() == 0) {
                return null;
            }
            //统一处理列表返回数据
            return formatPageListResult(user, pageId, nextPageId, pagedList, builder.getDatabase(), type, isCalculateSimNum);
        } catch (Exception e) {
            if (e.getMessage().contains("检索超时")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_TIMEOUT,
                        e);
            }else if (e.getMessage().contains("表达式过长")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_EXCEPTION,
                        e);
            }
            throw new OperationException("listByHot error:" + e);
        }
    }

    /**
     * 热点信息列表  -- 按热度排序时，默认为站内排重
     * 调用hybase查询数据，直接查询，不做处理  --  混合列表
     * 返回数据未被处理
     * 列表按热度进行排序
     * 要查询的数据源和数据库通过参数groupName控制，querybuilder中尽量少出现数据源
     *
     * @param queryBuilder 查询构造器
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @param groupName    要查询的数据源，用;分割，当前列表的可查询数据源的总的  -- 统一处理数据源*
     * @return
     * @throws TRSException
     */
    @Override
    public <T extends IQueryBuilder> PagedList<FtsDocumentCommonVO> queryPageListForHotNoFormat(T queryBuilder, String type, String groupName) throws TRSException {
        QueryBuilder builder = null;
        if (StringUtil.isNotEmpty(groupName)) {
            builder = (QueryBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(queryBuilder, groupName, 0);

        } else {
            builder = CommonListChartUtil.formatQueryBuilder(queryBuilder);
        }
        if (builder == null) {
            return null;
        }
        PagedList<FtsDocumentCommonVO> pagedList =  queryPageListForHotBase(builder, type);
        if (pagedList == null || pagedList.getPageItems() == null || pagedList.getPageItems().size() == 0) {
            return null;
        }else{
            return pagedList;
        }
    }


    /**
     * 热点信息列表  -- 按热度排序时，默认为站内排重
     * 调用hybase查询数据，直接查询，不做处理  --  混合列表（需要标注要查询的数据源）
     * 返回数据未被处理
     * 列表按热度进行排序
     *
     * @param builder 查询构造器
     * @param type    查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSException
     */
    private <T extends IQueryBuilder> PagedList<FtsDocumentCommonVO> queryPageListForHotBase(QueryBuilder builder, String type) throws TRSException {
        try {
            //热度排序按站内排重查询
            Boolean sim = false;
            Boolean irSimflag = true;
            Boolean irSimflagAll = false;
            if (builder == null) {
                return null;
            }
            //页面的 页数和条数
            long pageNo = builder.getPageNo();
            int pageSize = builder.getPageSize();

            // 返回给前端总页数
            int pageListNo = 0;
            // 返回给前端总条数
            int pageListSize = 0;

            List<FtsDocumentCommonVO> ftsDocumentCommonVOS = new ArrayList<>();
            // hybase不能直接分页 每次都统计出50条 然后再取
            long startTime = System.currentTimeMillis();
            List<GroupInfo> groupList = new ArrayList();
            builder.page(0, 50);
            //单独算法,已修改
            GroupResult md5TAG = commonListService.categoryQuery(builder, sim, irSimflag,
                    irSimflagAll, "MD5TAG", type);
            groupList = md5TAG.getGroupList();

            int start = (int) (pageSize * pageNo);
            int end = (int) (pageSize * pageNo + pageSize - 1);
            if (start >= groupList.size()) {
                return null;
            }
            if (groupList.size() <= end) {
                end = groupList.size() - 1;
            }
            // 返回前端总页数
            pageListNo = groupList.size() % pageSize == 0 ? groupList.size() / pageSize
                    : groupList.size() / pageSize + 1;
            pageListSize = groupList.size();
            for (int i = start; i <= end; i++) {
                GroupInfo info = groupList.get(i);
                QueryCommonBuilder builder1 = new QueryCommonBuilder();
                builder1.filterByTRSL(builder.asTRSL());
                builder1.page(0, 1);
                builder1.filterField(FtsFieldConst.FIELD_MD5TAG, info.getFieldValue(), Operator.Equal);
                builder1.orderBy(FtsFieldConst.FIELD_URLTIME, true);
                builder1.setDatabase(builder.getDatabase().split(";"));
                PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchServiceNew.pageListCommon(builder1, sim, irSimflag, irSimflagAll, type);

                if (ObjectUtil.isNotEmpty(pagedList) && pagedList.getPageItems() != null && pagedList.size() > 0) {
                    FtsDocumentCommonVO vo = pagedList.getPageItems().get(0);
                    vo.setSimCount(info.getCount());
                    ftsDocumentCommonVOS.add(vo);
                }
            }
            long endTime = System.currentTimeMillis();
            log.error("间隔HY时间：" + (endTime - startTime));
            if (0 == ftsDocumentCommonVOS.size()) {
                return null;
            }
            PagedList<FtsDocumentCommonVO> pagedList = new PagedList<FtsDocumentCommonVO>(pageListNo,
                    (int) (pageSize < 0 ? 15 : pageSize), pageListSize, ftsDocumentCommonVOS, 1);
            return pagedList;
        } catch (Exception e) {
            if (e.getMessage().contains("检索超时")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_TIMEOUT,
                        e);
            }else if (e.getMessage().contains("表达式过长")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_EXCEPTION,
                        e);
            }
            throw new OperationException("热点列表计算失败:" + e);
        }
    }

    /**
     *
     * 查询特定库，返回数据类型不同于信息列表类
     * 例如微博用户库，话题榜等库，但仍需要继承IDocument
     * 返回数据未被处理
     *
     *
     * @param builder      查询构造器  需要制定数据类型和要查询数据库
     * @param resultClass  返回对象数据类型类
     * @param sim          单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSException
     */
    @Override
    public <T extends IDocument> PagedList<T> queryPageListForClass(QueryBuilder builder, Class<T> resultClass, boolean sim,
                                                                    boolean irSimflag, boolean irSimflagAll, String type) throws TRSException {
        try {
            if (builder == null) {
                return null;
            }
            long startTime = System.currentTimeMillis();
            PagedList<T> pagedList = hybase8SearchServiceNew.ftsPageList(builder,resultClass, sim, irSimflag, irSimflagAll, type);
            long endTime = System.currentTimeMillis();
            log.error("间隔HY时间：" + (endTime - startTime));
            return pagedList;
        } catch (Exception e) {
            if (e.getMessage().contains("检索超时")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_TIMEOUT,
                        e);
            }else if (e.getMessage().contains("表达式过长")){
                throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_EXCEPTION,
                        e);
            }
            throw new OperationException("检索异常：", e);
        }
    }

    /**
     * 列表数据统计分析  统计各个数据源所占数据条数
     * 要查询的数据源和数据库通过参数groupName控制，querybuilder中尽量少出现数据源
     *
     * @param builder      查询表达式
     * @param sim          单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param groupName    数据源，用;分号分割
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @param resultField  这个是返回数据中对应的数据存储的key名
     * @param <T>
     * @return
     * @throws TRSException
     */
    @Override
    @HybaseRead
    public <T extends IQueryBuilder> Object queryListGroupNameStattotal(T builder, boolean sim,
                                                                        boolean irSimflag, boolean irSimflagAll, String groupName, String type, ChartResultField resultField) throws TRSException {

        //列表统计只会出现一组数据源，所以只写一个数据源即可
        QueryBuilder queryBuilder = (QueryBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(builder, groupName, 0);
        if (builder == null) {
            return null;
        }
        queryBuilder.setPageSize(queryBuilder.getPageSize() >= 1 ? queryBuilder.getPageSize() : 15);
        GroupResult groupResult = commonListService.categoryQuery(queryBuilder, sim, irSimflag, irSimflagAll, FtsFieldConst.FIELD_GROUPNAME, type);

        if(groupResult == null || groupResult.getGroupList()==null || groupResult.getGroupList().size() ==0){
            return null;
        }
        Map<String, Long> map = new LinkedHashMap<>();
        if(groupResult != null){
            for (GroupInfo groupInfo : groupResult.getGroupList()) {
                map.put(Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(groupInfo.getFieldValue()), groupInfo.getCount());
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        List<String> showGroup = Const.PAGE_SHOW_DATASOURCE_SORT;
        for (String group : showGroup) {
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put(resultField.getContrastField(), group);
            if (map.containsKey(group)) {
                groupMap.put(resultField.getCountField(), map.get(group));
            } else {
                groupMap.put(resultField.getCountField(), 0L);
            }
            list.add(groupMap);
        }
        return list;
    }


    /**
     * 相似文章计算   ---  基于列表页查询
     *
     * @param database            查询的数据库
     * @param pageId              当前页的标识
     * @param documentList        当前列表页要展示的数据
     * @param trslk               trsl表达式在redis存储的key
     * @param type                模块类型，主要对应的是用户查询时间的权限限制
     * @param isCalculateSitename 是否计算相似文章对应的发文网站信息
     */
    private void calculateSimNum(String database, String pageId, final List<FtsDocumentCommonVO> documentList,
                                 String trslk, String type, Boolean isCalculateSitename) {
        try {
            //log.info("相似文章数计算：" + "async:" + pageId + "开始");
            //热度排序按站内排重查询 - 热度为按相似文章数计算，相似文章数代表热度值
            Boolean sim = false;
            Boolean irSimflag = true;
            Boolean irSimflagAll = false;

            String trsl = RedisUtil.getString(trslk);
            List<AsyncDocument> asyncList = new ArrayList<>();
            List<AsyncInfo> asyncInfoList = new ArrayList<>();
            for (FtsDocumentCommonVO document : documentList) {
                String id = document.getSid();
                String md5Tag = document.getMd5Tag();
                AsyncDocument asyncDocument = new AsyncDocument();
                asyncDocument.setId(id);

                if (StringUtil.isNotEmpty(md5Tag)) { // 获取相似文章数
                    QueryBuilder searchBuilder = new QueryBuilder();
                    searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);

                    String trslpro = TrslUtil.removeSimflag(trsl);
                    searchBuilder.filterByTRSL(trslpro);
                    // 根据文章的groupName选择对应库检索
                    if (StringUtil.isEmpty(database)) {
                        searchBuilder.setDatabase(TrslUtil.chooseDatabases(document.getGroupName()));
                    } else {
                        searchBuilder.setDatabase(database);
                    }
                    if (StringUtil.isNotEmpty(id)) {
                        //id不为空，则去掉当前文章
                        StringBuffer idBuffer = new StringBuffer();
                        if (Const.MEDIA_TYPE_WEIBO.contains(document.getGroupName())) {
                            idBuffer.append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
                        } else if (Const.MEDIA_TYPE_WEIXIN.contains(document.getGroupName())) {
                            idBuffer.append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
                        } else {
                            idBuffer.append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
                        }
                        searchBuilder.filterByTRSL_NOT(idBuffer.toString());
                    }

                    searchBuilder.setPageSize(1);

                    Long ftsCount = commonListService.ftsCount(searchBuilder, sim, irSimflag, irSimflagAll, type);
                    //现在计算相似文章数，默认按照减去自身
                    asyncDocument.setSimNum(ftsCount);
                } else {
                    asyncDocument.setSimNum(0L);
                }
                asyncList.add(asyncDocument);
                AsyncInfo asyncInfo = new AsyncInfo();
                asyncInfo.setAsyncDocument(asyncDocument);
                asyncInfo.setMd5(md5Tag);
                asyncInfo.setDatabase(database);
                asyncInfo.setGroupName(document.getGroupName());
                asyncInfoList.add(asyncInfo);
            }
            //log.info("相似文章数计算：" + "async:" + pageId + "完成，数据为：" + asyncList.size());
            TimingCachePool.put("async:" + pageId, asyncList);
            RedisFactory.setValueToRedis("async:" + pageId, asyncList);
            //现在只针对普通搜索页面做了相似文章发文网站的检索
            if (isCalculateSitename) {
                fixedThreadPool.execute(
                        () -> querySiteNameForSimMd5(pageId, trslk, type, asyncInfoList));
            }

        } catch (Exception e) {
            log.error("setSimNumAndPicUrl error", e);
        }
    }

    /**
     * 在相同文章的基础上求发表相似文章的对应媒体网站
     *
     * @param pageId        页面id
     * @param trslk         表达式key
     * @param type          类型
     * @param asyncInfoList 相似文章数数据
     */
    private void querySiteNameForSimMd5(String pageId, String trslk, String type, List<AsyncInfo> asyncInfoList) {
        //五大商业媒体 ：IR_INDUSTRY    =1 时
        //网信办白名单 ： IR_WXB_LIST =0
        try {
            //log.info("相似文章的发文网站计算：" + "asySiteName:" + pageId + "开始");
            String trsl = RedisUtil.getString(trslk);
            if (StringUtil.isNotEmpty(trsl)) {
                trsl = TrslUtil.removeSimflag(trsl);
            }
		/*
			infoList中的字段：
			id：sid --用处不大
			md5：MD5
			database：查询数据库
			simNum：相似文章数
		 */
            //因为是求相似文章数的发文网站，所以遵守相似文章计算的排重规则
            Boolean sim = false;
            Boolean irSimflag = true;
            Boolean irSimflagAll = false;
            List<AsySiteNameDocument> resultList = new ArrayList<>();
            for (AsyncInfo asyncInfo : asyncInfoList) {
                AsyncDocument asy = asyncInfo.getAsyncDocument();
                String id = asy.getId();
                String md5 = asyncInfo.getMd5();
                Long simNum = asy.getSimNum();
                String database = asyncInfo.getDatabase();
                String groupName = asyncInfo.getGroupName();
                AsySiteNameDocument asySiteNameDocument = new AsySiteNameDocument();
                asySiteNameDocument.setId(id);
                asySiteNameDocument.setMd5(md5);
                asySiteNameDocument.setSimNum(simNum);
                if (simNum > 0) {
                    //需要计算发文网站信息
                    //如果最多展示3个。网站类型分3类，网信办白名单、五大商业媒体、其他普通
                    if (StringUtil.isNotEmpty(md5)) {
                        List<Object> siteNames = new ArrayList<>();

                        QueryBuilder searchBuilder = new QueryBuilder();

                        searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
                        StringBuffer idBuffer = new StringBuffer();
                        if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
                            idBuffer.append(" NOT ").append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
                        } else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
                            idBuffer.append(" NOT ").append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
                        } else {
                            idBuffer.append(" NOT ").append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
                        }
                        String trslFilter = trsl + idBuffer.toString();
                        searchBuilder.filterByTRSL(trslFilter);
                        //网信办白名单
                        QueryBuilder searchBuilder_wxb = new QueryBuilder();
                        searchBuilder_wxb.filterByTRSL("(" + FtsFieldConst.FIELD_WXB_LIST + ":(0))");
                        searchBuilder_wxb.filterByTRSL(searchBuilder.asTRSL());
                        searchBuilder_wxb.setDatabase(database);
                        searchBuilder_wxb.page(0, 3);
                        List<GroupInfo> list = new ArrayList<>();
                        GroupResult categoryInfos = null;
                        try {
                            //计算网信办白名单的
                            categoryInfos = commonListService.categoryQuery(searchBuilder_wxb, sim, irSimflag, irSimflagAll,
                                    FtsFieldConst.FIELD_SITENAME, type);
                        } catch (TRSSearchException e) {
                            throw new TRSSearchException(e);
                        }
                        if (categoryInfos != null && categoryInfos.getGroupList() != null && categoryInfos.getGroupList().size() > 0) {
                            list.addAll(categoryInfos.getGroupList());
                        }
                        if (list.size() < 3) {
                            //五大商业媒体
                            QueryBuilder searchBuilder_industry = new QueryBuilder();
                            searchBuilder_industry.filterByTRSL("(" + FtsFieldConst.FIELD_INDUSTRY + ":(1))");
                            searchBuilder_industry.filterByTRSL(searchBuilder.asTRSL());
                            searchBuilder_industry.filterByTRSL_NOT(FtsFieldConst.FIELD_WXB_LIST + ":(0)");
                            searchBuilder_industry.setDatabase(database);
                            searchBuilder_industry.page(0, 3);
                            try {
                                //计算五大商业媒体
                                categoryInfos = commonListService.categoryQuery(searchBuilder_industry, sim, irSimflag, irSimflagAll,
                                        FtsFieldConst.FIELD_SITENAME, type);
                            } catch (TRSSearchException e) {
                                throw new TRSSearchException(e);
                            }
                            if (categoryInfos != null && categoryInfos.getGroupList() != null && categoryInfos.getGroupList().size() > 0) {
                                list.addAll(categoryInfos.getGroupList());
                            }
                        }
                        if (list.size() < 3) {
                            searchBuilder.filterByTRSL_NOT(FtsFieldConst.FIELD_WXB_LIST + ":(0)");
                            searchBuilder.filterByTRSL_NOT(FtsFieldConst.FIELD_INDUSTRY + ":(1)");
                            searchBuilder.setDatabase(database);
                            searchBuilder.page(0, 3);
                            try {
                                //计算普通媒体的
                                categoryInfos = commonListService.categoryQuery(searchBuilder, sim, irSimflag, irSimflagAll,
                                        FtsFieldConst.FIELD_SITENAME, type);
                            } catch (TRSSearchException e) {
                                throw new TRSSearchException(e);
                            }
                            if (categoryInfos != null && categoryInfos.getGroupList() != null && categoryInfos.getGroupList().size() > 0) {
                                list.addAll(categoryInfos.getGroupList());
                            }
                        }
                        if (list.size() > 0) {
                            int size = 3;
                            if (list.size() < 3) {
                                size = list.size();
                            }
                            for (int i = 0; i < size; i++) {
                                GroupInfo groupInfo = list.get(i);
                                String siteName = groupInfo.getFieldValue();
                                Long num = groupInfo.getCount();
                                Map<String, Object> map = new HashMap<>();
                                map.put("sitename", siteName);
                                map.put("num", num);
                                siteNames.add(map);
                            }
                            asySiteNameDocument.setSitenames(siteNames);
                        } else {
                            asySiteNameDocument.setSitenames("");
                        }
                    } else {
                        asySiteNameDocument.setSitenames("");
                    }
                } else {
                    asySiteNameDocument.setSitenames("");
                }
                resultList.add(asySiteNameDocument);
            }
            //log.info("相似文章的发文网站计算：" + "asySiteName:" + pageId + "完成，数据为：" + resultList.size());
            TimingCachePool.put("asySiteName:" + pageId, resultList);
            RedisFactory.setValueToRedis("asySiteName:" + pageId, resultList);
        } catch (Exception e) {
            log.error("相似文章的发文网站计算信息失败", e);
        }
    }

    /**
     * 统计 - 获取到当前条件符合数量的总数
     * 要查询的数据源和数据库通过参数groupName控制，querybuilder中尽量少出现数据源
     *
     * @param builder      查询构造器
     * @param isSimilar    单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @param groupName    要查询的数据源，用;分割，当前列表的可查询数据源的总的-- 统一处理数据源
     * @return
     * @throws TRSSearchException
     */
    @Override
    @HybaseRead
    public <T extends IQueryBuilder> Long ftsCount(T builder, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type, String groupName) throws TRSException {
        QueryBuilder queryBuilder = null;
        if (StringUtil.isNotEmpty(groupName)) {
            queryBuilder = (QueryBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(builder, groupName, 0);
        } else {
            queryBuilder = CommonListChartUtil.formatQueryBuilder(builder);
        }
        if (queryBuilder == null) {
            return null;
        }
        queryBuilder.page(0,1);
        return ftsCount(queryBuilder, isSimilar, irSimflag, irSimflagAll, type);

    }

    /**
     * 统计 - 获取到当前条件符合数量的总数
     * 需要在builder中拼接要查询的数据源和数据库
     *
     * @param builder      查询构造器
     * @param isSimilar    单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSSearchException
     */
    @Override
    @HybaseRead
    public <T extends IQueryBuilder> Long ftsCount(T builder, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String type) throws TRSSearchException {
        Long count = 0L;
        try {
            QueryBuilder queryBuilder = CommonListChartUtil.formatQueryBuilder(builder);
            if (queryBuilder == null) {
                return null;
            }
            queryBuilder.page(0,1);
            count = hybase8SearchServiceNew.ftsCount(queryBuilder, isSimilar, irSimflag, irSimflagAll, type);
            return count;
        } catch (Exception e) {
            throw new TRSSearchException("计算总数失败:" + e);
        }
    }

    /**
     * 分类统计  --- 根据条件进行分类统计
     * 要查询的数据源和数据库通过参数groupName控制，querybuilder中尽量少出现数据源
     *
     * @param builder      查询构造器
     * @param isSimilar    单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param groupField   分类字段
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @param groupName    要查询的数据源，用;分割，当前列表的可查询数据源的总的  -- 统一处理数据源
     * @return
     * @throws TRSSearchException
     */
    @Override
    @HybaseRead
    public <T extends IQueryBuilder> GroupResult categoryQuery(T builder, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String groupField, String type, String groupName) throws TRSException {
        QueryBuilder queryBuilder = null;
        if (StringUtil.isNotEmpty(groupName)) {
            queryBuilder = (QueryBuilder) CommonListChartUtil.addGroupNameForQueryBuilder(builder, groupName, 0);
        } else {
            queryBuilder = CommonListChartUtil.formatQueryBuilder(builder);
        }
        if (queryBuilder == null) {
            return null;
        }
        return categoryQuery(queryBuilder, isSimilar, irSimflag, irSimflagAll, groupField, type);
    }

    /**
     * 分类统计  --- 根据条件进行分类统计
     * 需要拼接好表达式，拼接要查询的数据源类型
     * 需要在builder中写好要查询数据库
     *
     * @param builder      查询构造器
     * @param isSimilar    单一媒体排重
     * @param irSimflag    站内排重
     * @param irSimflagAll 全网排重
     * @param groupField   分类字段
     * @param type         查询类型，对应用户限制查询时间的模块相同
     * @return
     * @throws TRSSearchException
     */
    @Override
    @HybaseRead
    public <T extends IQueryBuilder> GroupResult categoryQuery(T builder, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String groupField, String type) throws TRSException {
        try {
            QueryBuilder queryBuilder = CommonListChartUtil.formatQueryBuilder(builder);

            if (queryBuilder == null) {
                return null;
            }
            GroupResult groupInfos = hybase8SearchServiceNew.categoryQuery(queryBuilder, isSimilar, irSimflag, irSimflagAll, groupField, type, queryBuilder.getDatabase());
            return groupInfos;
        } catch (Exception e) {
            throw new TRSSearchException("分类统计失败:" + e);
        }
    }
}
