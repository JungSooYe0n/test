package com.trs.netInsight.support.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.api.entity.ApiAccessToken;
import com.trs.netInsight.support.api.handler.Api;
import com.trs.netInsight.support.api.result.ApiCommonResult;
import com.trs.netInsight.support.api.result.ApiResultType;
import com.trs.netInsight.support.api.service.IApiService;
import com.trs.netInsight.support.api.utils.constance.ApiMethod;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.StatusUser;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.hybaseShard.entity.HybaseShard;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeBase;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeClassify;
import com.trs.netInsight.support.knowledgeBase.service.IKnowledgeBaseService;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.support.redis.RedisOperator;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.analysis.controller.ChartAnalyzeController;
import com.trs.netInsight.widget.analysis.controller.SpecialChartAnalyzeController;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.controller.ColumnChartController;
import com.trs.netInsight.widget.column.controller.ColumnController;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.column.service.*;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.microblog.constant.MicroblogConst;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.microblog.entity.SpreadObject;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogDataService;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogService;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.special.controller.InfoListController;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.special.service.ISpecialService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.UserRepository;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.*;

import javax.mail.search.SearchException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * openApi暴露接口,接口内部无须处理异常,直接抛出
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月17日
 */
@Slf4j
@RestController
@RequestMapping("/api/method")
public class ApiController {

    @Autowired
    private IIndexPageService indexPageService;

    @Autowired
    private IIndexTabService indexTabService;

    @Autowired
    private IIndexTabMapperService indexTabMapperService;

    @Autowired
    private IInfoListService infoListService;

    @Autowired
    private FullTextSearch hybase8SearchService;

    @Autowired
    private ChartAnalyzeService chartAnalyzeService;

    @Autowired
    private IDistrictInfoService districtInfoService;

    @Autowired
    private FavouritesRepository favouritesRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ISingleMicroblogDataService singleMicroblogDataService;

    @Autowired
    private ISingleMicroblogService singleMicroblogService;

    @Autowired
    private IHybaseShardService hybaseShardService;

    @Autowired
    private ISpecialService specialService;

    @Autowired
    private IApiService apiService;

    @Autowired
    private ChartAnalyzeController chartAnalyzeController;

    @Autowired
    private ColumnController columnController;
    @Autowired
    private ColumnChartController columnChartController;
    @Autowired
    private SpecialChartAnalyzeController specialChartAnalyzeController;
    @Autowired
    private InfoListController infoListController;

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IColumnService columnService;

    @Autowired
    private IKnowledgeBaseService knowledgeBaseService;

    private static final String createTime = "createdTime";

    @Autowired
    private ICommonListService commonListService;
    @Autowired
    private ICommonChartService commonChartService;

    /**
     * 获取pageList
     *
     * @param accessToken
     * @param request
     * @return
     * @Return : Object
     * @since changjiang @ 2018年7月17日
     */
    @Api(value = "indexPage list", method = ApiMethod.IndexPage)
    @GetMapping("/indexPage")
    @Log(systemLogOperation = SystemLogOperation.INDEX_PAGE, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getIndexPage(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request) {
        ApiAccessToken token = getToken(request);
        String grantSourceOwnerId = token.getGrantSourceOwnerId();
        User ownerUser = userRepository.findOne(grantSourceOwnerId);
        List<IndexPage> indexPages = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(ownerUser)){
            if (UserUtils.ROLE_LIST.contains(ownerUser.getCheckRole())){
                indexPages = indexPageService.findByUserId(token.getGrantSourceOwnerId(),
                        new Sort(Direction.DESC, createTime));
            }else {
                indexPages = indexPageService.findBySubGroupId(ownerUser.getSubGroupId(),new Sort(Direction.DESC, createTime));
            }

        }

        return indexPages;
    }
    /**
     * 获取栏目组，带着导航信息(新)
     *
     * @param accessToken
     * @param request
     * @return
     * @Return : Object
     */
    @Api(value = "indexPage list with navigation", method = ApiMethod.IndexPageNavigation)
    @GetMapping("/getIndexPage")
    @Log(systemLogOperation = SystemLogOperation.GET_INDEX_PAGE, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object queryIndexPage(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request) throws TRSException {
        ApiAccessToken token = getToken(request);
        String grantSourceOwnerId = token.getGrantSourceOwnerId();
        User ownerUser = userRepository.findOne(grantSourceOwnerId);
        if (ObjectUtil.isEmpty(ownerUser)){
            ApiCommonResult apiCommonResult = new ApiCommonResult(ApiResultType.NotFindSource, "Api调用失败,请返回重试或联系管理员!");
            return apiCommonResult;
        }
        return indexPageService.findIndexPageForApi(ownerUser);
    }
    /**
     * 根据pageId,获取indexPage详细信息
     *
     * @param accessToken
     * @param request
     * @param indexPageId
     * @return
     * @Return : Object
     * @since changjiang @ 2018年7月17日
     */
    @Api(value = "indexPage info", method = ApiMethod.IndexPageInfo)
    @GetMapping("/indexPageInfo")
    @Log(systemLogOperation = SystemLogOperation.INDEX_PAGE_INFO, systemLogType = SystemLogType.API, systemLogOperationPosition = "栏目id：@{indexPageId}")
    public Object getIndexPageInfo(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                   @RequestParam(value = "indexPageId") String indexPageId) {
        ApiAccessToken token = getToken(request);
        IndexPage indexPage = indexPageService.findOne(token.getGrantSourceOwnerId(), indexPageId);
        return indexPage;
    }

    /**
     * 根据pageId,获取indexTab列表
     *
     * @param accessToken
     * @param request
     * @param indexPageId
     * @return
     * @Return : Object
     * @since changjiang @ 2018年7月17日
     */
    @Api(value = "indexTab list", method = ApiMethod.IndexTable)
    @GetMapping("/indexTab")
    @Log(systemLogOperation = SystemLogOperation.INDEX_TABLE, systemLogType = SystemLogType.API, systemLogOperationPosition = "栏目id：@{indexPageId}")
    public Object getIndexTabList(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                  @RequestParam(value = "indexPageId") String indexPageId) {
        ApiAccessToken token = getToken(request);
        User user = userRepository.findOne(token.getGrantSourceOwnerId());
        if (ObjectUtil.isNotEmpty(user)){
            return indexTabService.findByParentIdAndUser(indexPageId, user,
                    new Sort(Direction.DESC, createTime));
        }
        return null;
    }

    /**
     *
     * 获取栏目组下的栏目信息，带父级(该账号所能展示的所有栏目信息)(新)
     * @param accessToken
     * @param request
     * @return
     */
    @Api(value = "indexTab list with pageName", method = ApiMethod.IndexTableWithPageName)
    @GetMapping("/getIndextabs")
    @Log(systemLogOperation = SystemLogOperation.INDEX_TABLES, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getIndextabs(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request) {
        ApiAccessToken token = getToken(request);
        User user = userRepository.findOne(token.getGrantSourceOwnerId());
        if (ObjectUtil.isNotEmpty(user)){
            try {
                return columnService.selectColumn(user, "");
            } catch (OperationException e) {
                ApiCommonResult apiCommonResult = new ApiCommonResult(ApiResultType.NotFindSource, e.getMessage());
                return apiCommonResult;
            }
        }
        return null;
    }
    /**
     * 获取栏目数据(豆腐块）
     *
     * @param accessToken
     * @param request
     * @param id
     * @param timeRange
     * @return
     * @throws OperationException
     * @Return : Object
     * @since changjiang @ 2018年7月17日
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "timeRange", value = "按时间查询 (2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "栏目id", dataType = "String", paramType = "query",required = true)
    })
    @Api(value = "indexTab data", method = ApiMethod.IndexTabData)
    @PostMapping("/indexTabData")
    @Log(systemLogOperation = SystemLogOperation.INDEX_TAB_DATA, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getIndexTabInfo(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                  @RequestParam(value = "timeRange", required = false) String timeRange,
                                  @RequestParam(value = "id") String id)
            throws TRSException, SearchException {
        ApiAccessToken token = getToken(request);
        User user = userRepository.findOne(token.getGrantSourceOwnerId());
        if (ObjectUtil.isNotEmpty(user)) {
            return columnController.columnStattotal(id, timeRange, "", "", "", "", "", "", "", "", false, 0, false, "", "", "", "", "", "", "", "", "", "");
        }
        return null;
    }

    /**
     * 查找当前栏目对应的图表
     *
     * @param accessToken
     * @param request
     * @param id
     * @return
     * @throws OperationException
     * @Return : Object
     * @since changjiang @ 2018年7月17日
     */
    @ApiImplicitParam(name = "id", value = "栏目id", dataType = "String", paramType = "query",required = true)
    @Api(value = "selectTabStatisticalChartList", method = ApiMethod.SelectTabStatisticalChartList)
    @PostMapping("/selectTabStatisticalChartList")
    @Log(systemLogOperation = SystemLogOperation.SELECT_TABSTATISTICAL_CHARTLIST, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object StatisticalChart(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                   @RequestParam(value = "id") String id)
            throws TRSException, SearchException {
        ApiAccessToken token = getToken(request);
        User user = userRepository.findOne(token.getGrantSourceOwnerId());
        if (ObjectUtil.isNotEmpty(user)) {
            return columnChartController.selectTabStatisticalChartList(request, id, null);
        }
        return null;
    }

    /**
     * 获取各舆论场趋势分析
     *
     * @param accessToken
     * @param request
     * @param id
     * @return
     * @throws OperationException
     * @Return : Object
     * @since changjiang @ 2018年7月17日
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "showType", value = "指定折线图的展示方式：按小时(hour)，按天数(day)", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "timeRange", value = "按时间查询 (2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "图表id", dataType = "String", paramType = "query",required = true)
    })
    @Api(value = "Statistical chart", method = ApiMethod.StatisticalChart)
    @PostMapping("/statisticalChart")
    @Log(systemLogOperation = SystemLogOperation.STATISTICAL_CHART, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object StatisticalChart(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                   @RequestParam(value = "showType", required = false, defaultValue = "day") String showType,
                                   @RequestParam(value = "timeRange", required = false) String timeRange,
                                   @RequestParam(value = "id") String id)
            throws TRSException, SearchException {
        ApiAccessToken token = getToken(request);
        User user = userRepository.findOne(token.getGrantSourceOwnerId());
        if (ObjectUtil.isNotEmpty(user)) {
            return columnController.selectChart(id, "StatisticalChart", showType, "", "", timeRange, false, "", "", "", "", "", "", "", "", false, 0, false, "", "", "", "", "", "", "", "", "", "", "");
        }
        return null;
    }

    /**
     * 获取日常监测地域分布数据
     *
     * @param accessToken
     * @param request
     * @param id
     * @return
     * @throws Exception
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "showType", value = "指定折线图的展示方式：按小时(hour)，按天数(day)", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "timeRange", value = "按时间查询 (2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "mapto", value = "地图下钻省", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "图表id", dataType = "String", paramType = "query",required = true)
    })
    @Api(value = "column area", method = ApiMethod.ColumnArea)
    @GetMapping("/getColumnArea")
    @Log(systemLogOperation = SystemLogOperation.COLUMN_AREA, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getColumnArea(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                @RequestParam(value = "showType", required = false, defaultValue = "day") String showType,
                                @RequestParam(value = "timeRange", required = false) String timeRange,
                                @RequestParam(value = "mapto", defaultValue = "", required = false) String mapto,
                                @RequestParam(value = "id") String id) throws Exception {
        ApiAccessToken token = getToken(request);
        User user = userRepository.findOne(token.getGrantSourceOwnerId());
        if (ObjectUtil.isNotEmpty(user)) {
            return columnController.selectChart(id, "StatisticalChart", showType, "", "hitArticle", timeRange, false, "", "", "", "", "", "", "", "", false, 0, false, "", "", "", "", "", "", "", "", "", mapto, "");
        }
        return null;
    }
    /**
     * 栏目列表-更多（热点栏目总共最多50条，暂定其余栏目每页最多1000条）
     * @param accessToken
     * @param request
     * @param indexTabId
     * @param entityType
     * @param pageSize
     * @return
     * @throws OperationException
     */
    @ApiImplicitParam(name = "indexTabId", value = "三级栏目id", dataType = "String", required = true,paramType = "query")
    @Api(value = "indexTab list data", method = ApiMethod.IndexTabListData)
    @PostMapping("/indexTabListData")
    @Log(systemLogOperation = SystemLogOperation.INDEX_TAB_LIST_DATA, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getIndexTabList(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                  @ApiParam("栏目id") @RequestParam(value = "indexTabId") String indexTabId,
                                  @ApiParam("来源") @RequestParam(value = "source", defaultValue = "ALL") String source,
                                  @ApiParam("第几页，默认第0页") @RequestParam(value = "pageNo", defaultValue = "0",required = false) int pageNo,
                                  @ApiParam("一页多少条,默认10条") @RequestParam(value = "pageSize", defaultValue = "10",required = false) int pageSize,
                                  @ApiParam("排序方式，默认时间降序") @RequestParam(value = "sort", defaultValue = "desc") String sort,
                                  @ApiParam("条件筛选-情感，默认全部情绪") @RequestParam(value = "emotion", defaultValue = "ALL") String emotion,
                                  @ApiParam("条件筛选-论坛主贴 0 /回帖 1，默认主贴+回帖") @RequestParam(value = "invitationCard", required = false) String invitationCard,
                                  @ApiParam("条件筛选-微博 原发 primary / 转发 forward ，默认原发+转发") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
                                  @ApiParam("折线图 时间点") @RequestParam(value = "dateTime", required = false) String dateTime,
                                  @ApiParam("图表类栏目（除舆论来源对比、词云图栏目和地图类栏目），对应所选择点击内容") @RequestParam(value = "key", required = false) String key,
                                  @ApiParam("词云点击进去所选择的词") @RequestParam(value = "irKeyword",required = false) String irKeyword,
                                  @ApiParam("地域名（点地图进去）") @RequestParam(value = "area", defaultValue = "ALL") String area,
                                  @ApiParam("词云图 类型") @RequestParam(value = "entityType", defaultValue = "keywords") String entityType)
            throws OperationException {
        ApiAccessToken token = getToken(request);
        User user = userRepository.findOne(token.getGrantSourceOwnerId());
        if (ObjectUtil.isNotEmpty(user)){
            IndexTab indexTab = indexTabService.findOne(indexTabId);
            IndexTabMapper mapper = indexTabMapperService.findOne(indexTabId);
            if(mapper!=null){
                 indexTab = mapper.getIndexTab();
            }
            //IndexTab indexTab = indexTabService.findOne(indexTabId);
            if(ObjectUtil.isNotEmpty(indexTab)){
                String timerange = indexTab.getTimeRange();
                AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
                ColumnConfig config = new ColumnConfig();
                int maxPageSize = token.getMaxPageSize();
                if (pageSize > maxPageSize){
                    pageSize = maxPageSize;
                }
                if(StringUtil.isNotEmpty(area)){
                    key = area;
                }
                if(StringUtil.isNotEmpty(irKeyword)){
                    key = irKeyword;
                }
                config.initSection(indexTab, timerange, pageNo, pageSize, source, emotion, entityType, dateTime, key, sort, invitationCard, "", "",forwarPrimary,
                        "", indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
                        indexTab.getContentArea(), indexTab.getMediaArea(), "","");
                column.setDistrictInfoService(districtInfoService);
                column.setCommonListService(commonListService);
                column.setCommonChartService(commonChartService);
                column.setConfig(config);
                return column.getSectionList();
            }
        }

        return null;
    }
    /**
     * 获取专题列表树
     *
     * @param accessToken
     * @param request
     * @return
     * @throws OperationException
     */
    @Api(value = "get all special", method = ApiMethod.SpecialAll)
    @GetMapping("/getAllSpecial")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_ALL, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getAllSpecial(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request)
            throws OperationException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        return specialService.selectSpecialReNew(user);
    }

    /**
     * 获取专题监测统计表格数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws TRSException
     */
    @Api(value = "specialProject statTotal", method = ApiMethod.SpecialStatTotal)
    @GetMapping("/getStatTotal")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_STAT_TOTAL, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getStatTotal(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                               @RequestParam(value = "specialId") String specialId) throws TRSException {
        ApiAccessToken token = getToken(request);
        User user = userRepository.findOne(token.getGrantSourceOwnerId());
        if (ObjectUtil.isNotEmpty(user)) {
            return chartAnalyzeController.getStatTotal(specialId, "", "ALL", "", "");
        }
        return null;
    }

    /**
     * 获取专题信息列表
     * @param accessToken
     * @param request
     * @param specialId
     * @param source
     * @param pageNo
     * @param pageSize
     * @param sort
     * @param emotion
     * @param invitationCard
     * @param forwarPrimary
     * @return
     * @throws TRSException
     */
    @Api(value = "specialProject list info", method = ApiMethod.SpecialListInfo)
    @GetMapping("/getListInfo")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_LIST_INFO, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getListInfo(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                              @ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
                              @ApiParam("来源") @RequestParam(value = "source", defaultValue = "ALL") String source,
                              @ApiParam("第几页，默认第0页") @RequestParam(value = "pageNo", defaultValue = "0",required = false) int pageNo,
                              @ApiParam("一页多少条,默认10条") @RequestParam(value = "pageSize", defaultValue = "10",required = false) int pageSize,
                              @ApiParam("排序方式，默认时间降序") @RequestParam(value = "sort", defaultValue = "desc") String sort,
                              @ApiParam("条件筛选-情感，默认全部情绪") @RequestParam(value = "emotion", defaultValue = "ALL") String emotion,
                              @ApiParam("条件筛选-论坛主贴 0 /回帖 1，默认主贴+回帖") @RequestParam(value = "invitationCard", required = false) String invitationCard,
                              @ApiParam("条件筛选-微博 原发 primary / 转发 forward ，默认原发+转发") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary) throws TRSException {
        ApiAccessToken token = getToken(request);
        int maxPageSize = token.getMaxPageSize();
        if (pageSize > maxPageSize) {
            pageSize = maxPageSize;
        }
        User user = userRepository.findOne(token.getGrantSourceOwnerId());
        if (ObjectUtil.isNotEmpty(user)) {
            return infoListController.dataList(specialId, pageNo, pageSize, source, sort, invitationCard, forwarPrimary, "", "", "", "", "", emotion, "",
                    "", "", "", "", false, 0, false, "", "ALL",
                    "", "", "", "", "", "", "", "");
        }
        return null;
    }

    /**
     * 获取来源类型统计数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject webCount", method = ApiMethod.SpecialWebCount)
    @GetMapping("/getWebCount")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_WEB_COUNT, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getWebCount(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                              @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.webCountnew(null, specialId, "ALL", "ALL");
    }

    /**
     * 获取态势评估数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject situationAssessment", method = ApiMethod.SituationAssessment)
    @GetMapping("/getSituationAssessment")
    @Log(systemLogOperation = SystemLogOperation.SITUATION_ASSESSMENT, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    @ApiImplicitParam(name = "timeRange", value = "按时间查询 (2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query")
    public Object getSituationAssessment(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                         @RequestParam(value = "timeRange", required = false) String timeRange,
                               @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.situationAssessment(timeRange, specialId, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * 获取各舆论场趋势分析数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @ApiImplicitParam(name = "timeRange", value = "按时间查询 (2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query")
    @Api(value = "specialProject webCountLine", method = ApiMethod.WebCountLine)
    @GetMapping("/getWebCountLine")
    @Log(systemLogOperation = SystemLogOperation.WEB_COUNT_LINE, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getWebCountLine(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                         @RequestParam(value = "specialId",required = true) String specialId,
                                  @RequestParam(value = "timeRange", required = false) String timeRange,
                                  @ApiParam("hour/day") @RequestParam(value = "showType",required = true) String showType) throws Exception {
        return specialChartAnalyzeController.webCountLine(null,specialId, showType, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * 获取各舆论场发布统计数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject webCommitCount", method = ApiMethod.WebCommitCount)
    @GetMapping("/getWebCommitCount")
    @Log(systemLogOperation = SystemLogOperation.WEB_COMMIT_COUNT, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getWebCommitCount(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                  @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.webCommitCount(null,specialId,false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * 获取观点分析数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject sentimentAnalysis", method = ApiMethod.SentimentAnalysis)
    @GetMapping("/getSentimentAnalysis")
    @Log(systemLogOperation = SystemLogOperation.SENTIMENT_ANALYSIS, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getSentimentAnalysis(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                       @ApiParam("观点分析范围") @RequestParam(value = "viewType", required = false, defaultValue = "OFFICIAL_VIEW") String viewType,
                                    @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.sentimentAnalysis(null,specialId,viewType,false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null);
    }

    /**
     * 获取情绪统计数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject moodStatistics", method = ApiMethod.MoodStatistics)
    @GetMapping("/getMoodStatistics")
    @Log(systemLogOperation = SystemLogOperation.MOOD_STATISTICS, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getMoodStatistics(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                       @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.moodStatistics(specialId,null,false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null);
    }

    /**
     * 获取热点信息数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject hotMessage", method = ApiMethod.HotMessage)
    @GetMapping("/getHotMessage")
    @Log(systemLogOperation = SystemLogOperation.HOT_MESSAGE, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getHotMessage(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                @ApiParam("类型：新闻网站/微博/微信/自媒体号") @RequestParam(value = "groupName", required = false ,defaultValue = "新闻网站") String groupName,
                                @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.hotMessage(null, specialId, 8, groupName, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * 获取传播分析站点数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject spreadAnalysisSiteName", method = ApiMethod.SpreadAnalysisSiteName)
    @GetMapping("/getSpreadAnalysisSiteName")
    @Log(systemLogOperation = SystemLogOperation.SPREAD_ANALYSIS_SITE_NAME, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getSpreadAnalysisSiteName(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.spreadAnalysisSiteName(specialId,null, false, null,null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }


    /**
     * 获取专题微博top5数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject statusTop5", method = ApiMethod.SpecialStatusTop5)
    @GetMapping("/getTop5")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_STATUS_TOP5, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getTop5(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                          @RequestParam(value = "specialId") String specialId,
                          @RequestParam(value = "sortType", defaultValue = "NEWEST") String sortType) throws Exception {
        return specialChartAnalyzeController.top5(specialId, "ALL", sortType, "ALL", "");
    }

    /**
     * 获取专题地域分布数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject area", method = ApiMethod.SpecialArea)
    @GetMapping("/getArea")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_AREA, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getArea(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                          @RequestParam(value = "specialId") String specialId) throws Exception {
        //return specialChartAnalyzeController.area(specialId, "ALL", null, "ALL");
        return specialChartAnalyzeController.area(specialId, null, "", true, "", "", "", "", "", "", "", "", false, null, false, "", "ALL", "", "", "", "", "", "", "", "", "");
    }

    /**
     * 获取专题媒体活跃等级数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject activeLevel", method = ApiMethod.SpecialActiveLevel)
    @GetMapping("/getActiveLevel")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_ACTIVE_LEVEL, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getActiveLevel(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                 @RequestParam(value = "specialId") String specialId,
                                 @RequestParam(value = "source", required = true) String source) throws Exception {
        return specialChartAnalyzeController.getActiveLevel(specialId, "ALL", "ALL", source,"65d");
    }

    /**
     * 获取情感分析数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject statusOption", method = ApiMethod.SpecialStatusOption)
    @GetMapping("/getStatusOption")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_STATUS_OPTION, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getStatusOption(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                  @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.weiboOption(specialId, "", false,null,null,null,null,
                null,null,null,null,null,null,
                null,null,null,null,null,null,null,null,null,null,null);
    }


    /**
     * 获取事件溯源
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @param type
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject trendTime", method = ApiMethod.SpecialTrendTime)
    @GetMapping("/getTrend")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_TREND_TIME, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getTrend(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                           @RequestParam(value = "specialId") String specialId,
                           @ApiParam("类型：新闻网站/微博/微信/自媒体号") @RequestParam(value = "type", required = false ,defaultValue = "新闻网站") String type) throws Exception {
//        Object first = specialChartAnalyzeController.trendTime(specialId, 1, type, "", "ALL", "ALL");
//        Object other = specialChartAnalyzeController.trendMd5(specialId, 9, type, "","", "ALL", "ALL");
        Object other = specialChartAnalyzeController.affairVenation(specialId, 10, type, "", "0d", true, "", "", "", "", "", "", "", "", false, null, null, "", "", "", "", "", "", "", "", "", "");
        Map<String, Object> data = new HashMap<>();
        data.put("other", other);
        return data;
    }

    /**
     * 获取信息走势图数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @param type
     * @param showType
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject trendMessage", method = ApiMethod.SpecialTrendMessage)
    @GetMapping("/getTrendMessage")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_TREND_MESSAGE, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getTrendMessage(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                  @RequestParam(value = "specialId") String specialId,
                                  @RequestParam(value = "type", defaultValue = "message") String type,
                                  @RequestParam(value = "showType", required = false,defaultValue = "day") String showType) throws Exception {
        if ("message".equals(type)){//信息走势
            return specialChartAnalyzeController.trendMessage("", specialId, "ALL", "ALL", showType);
        }else if ("netizen".equals(type)){//网民参与
            return specialChartAnalyzeController.netTendency("", specialId, "ALL", "ALL", showType);
        }else if ("media".equals(type)){//媒体参与
            return specialChartAnalyzeController.metaTendency("", specialId, "ALL", "ALL", showType);
        }
        return null;
    }

    /**
     * 获取专题引爆点数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject tippingPoint", method = ApiMethod.SpecialTippingPoint)
    @GetMapping("/getTippingPoint")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_TIPPING_POINT, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getTippingPoint(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                  @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.getTippingPoint(specialId, "", "ALL", "ALL");
    }

    /**
     * 获取情感走势数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject volume", method = ApiMethod.SpecialVolume)
    @GetMapping("/getVolume")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_VOLUME, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getVolume(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                            @RequestParam(value = "specialId") String specialId,
                            @RequestParam(value = "showType", defaultValue = "day") String showType) throws Exception {
        return specialChartAnalyzeController.getVolumeNew(specialId, "", "ALL", "ALL",showType);
    }

    /**
     * 获取专题新闻传播分析数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject NewsSiteAnalysis", method = ApiMethod.SpecialNewsSiteAnalysis)
    @GetMapping("/getNewsSiteAnalysis")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_NEWS_SITE_ANALYSIS, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getNewsSiteAnalysis(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                      @RequestParam(value = "specialId") String specialId) throws Exception {
        //return specialChartAnalyzeController.newsSiteAnalysis(specialId, "", "ALL", true,"ALL");
        return specialChartAnalyzeController.spreadAnalysis(specialId, "0d", "ALL", true, "", "", "", "", "", "", "", "", false, null, false, "", "", "", "", "", "", "", "", "");
    }

    /**
     * 获取活跃账号数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject ActiveAccount", method = ApiMethod.ActiveAccount)
    @GetMapping("/getActiveAccount")
    @Log(systemLogOperation = SystemLogOperation.ACTIVE_ACCOUNT, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getActiveAccount(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                      @RequestParam(value = "specialId") String specialId) throws Exception {
        //return specialChartAnalyzeController.newsSiteAnalysis(specialId, "", "ALL", true,"ALL");
        return specialChartAnalyzeController.getActiveAccount(specialId, null, false,null,null,null,null,
                null,null,null,null,null,null,
                null,null,null,null,null,null,null,null,null,null,null);
    }

    /**
     * 获取网友情绪数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject UserViews", method = ApiMethod.SpecialUserViews)
    @GetMapping("/getUserViews")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_USER_VIEWS, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getUserViews(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                               @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.userViews(specialId, "", "ALL", "ALL");
    }

    /**
     * 获取专题词云数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @param entityType
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject WordCloud", method = ApiMethod.SpecialWordCloud)
    @GetMapping("/getWordCloud")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_WORD_CLOUD, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getWordCloud(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                               @RequestParam(value = "specialId") String specialId,
                               @RequestParam(value = "entityType") String entityType) throws Exception {
        return specialChartAnalyzeController.getWordYun(specialId, "",entityType,"all",false,null,null,null,null,
                null,null,null,null,null,null,
                null,null,null,null,null,null,null,null,null,null,null);
    }

    /**
     * 获取专题热词探索数据
     *
     * @param accessToken
     * @param request
     * @param specialId
     * @return
     * @throws Exception
     */
    @Api(value = "specialProject TopicEvoExplor", method = ApiMethod.SpecialTopicEvoExplor)
    @GetMapping("/getTopicEvoExplor")
    @Log(systemLogOperation = SystemLogOperation.SPECIAL_TOPIC_EVO_EXPLOR, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getTopicEvoExplor(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request,
                                    @RequestParam(value = "specialId") String specialId) throws Exception {
        return specialChartAnalyzeController.topicEvoExplor(specialId, "", "ALL", "ALL", "all");
    }


    /**
     * 获取token
     *
     * @param request
     * @return
     * @Return : ApiAccessToken
     * @since changjiang @ 2018年7月17日
     */
    private ApiAccessToken getToken(HttpServletRequest request) {
        return (ApiAccessToken) request.getAttribute("token");
    }

    /**
     *   此接口存在问题：1、最好post请求  2、查询时间段做限制  3、数据来源 必填  国内微信，电子报，客户端类做处理
     *  根据 输入条件查询 数据列表
     * @param accessToken
     * @param request
     * @param keyWords  关键词
     * @param time      时间
     * @param groupName  来源
     * @param pageNo   页码
     * @param pageSize 页长
     * @return
     * @throws OperationException
     * @throws TRSException
     */
    @Api(value = "select data by keyWords",method = ApiMethod.SelectData)
    @GetMapping("/selectData")
    @Log(systemLogOperation = SystemLogOperation.SELECT_DATA, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object selectData( @RequestParam(value = "accessToken") String accessToken,HttpServletRequest request, @RequestParam(value = "keyWords") String keyWords,
                              @RequestParam(value = "time",defaultValue = "7d",required = false) String time, @RequestParam(value = "groupName",required = false) String groupName,
                              @RequestParam(value = "pageNo",defaultValue = "0") int pageNo, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize)throws OperationException,TRSException{
        //防止前端乱输入
        pageSize = pageSize>=1?pageSize:10;
        QueryCommonBuilder commonBuilder = new QueryCommonBuilder();
        commonBuilder.page(pageNo,pageSize);
        if (StringUtil.isNotEmpty(keyWords)){
            keyWords = keyWords.replaceAll(";"," OR ");

            if (keyWords.endsWith(" OR ")){
                keyWords = keyWords.substring(0,keyWords.length()-4) ;
            }
        }

        String[] databases = null;
        if (StringUtil.isEmpty(groupName)){
            databases = Const.MIX_DATABASE.split(";");
        }else {
            groupName = groupName.replaceAll("微信","国内微信").replaceAll("电子报","国内新闻_电子报").replaceAll("新闻","国内新闻").replaceAll("论坛","国内论坛").replaceAll("博客","国内博客");
            databases = TrslUtil.chooseDatabases(groupName.split(";"));
            groupName = groupName.replaceAll(";"," OR ");
            if (groupName.endsWith(" OR ")){
                groupName = groupName.substring(0,groupName.length()-4);
            }
        }
        commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName,Operator.Equal);
        commonBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time),
                Operator.Between);
        if (StringUtil.isNotEmpty(keyWords)){
            String trsl = new StringBuffer(FtsFieldConst.FIELD_URLTITLE).append(":(").append(keyWords).append(") OR ")
                    .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(keyWords).append(")").toString();
            commonBuilder.filterByTRSL(trsl);
        }
        commonBuilder.setDatabase(databases);
        PagedList<FtsDocumentCommonVO> ftsDocumentCommonVOPagedList = hybase8SearchService.pageListCommon(commonBuilder, false, false,false,null);
        return ftsDocumentCommonVOPagedList;
    }

    /**
     * 高级搜索
     * @param accessToken
     * @param request
     * @param keywords
     * @param groupName
     * @param sort
     * @param time
     * @param pageNo
     * @param pageSize
     * @param simflag
     * @param emotion
     * @param invitationCard
     * @param forwarPrimary
     * @return
     * @throws OperationException
     * @throws TRSException
     */
    @Api(value = "advanced search",method = ApiMethod.AdvancedSearch)
    @GetMapping("/advancedSearch")
    @Log(systemLogOperation = SystemLogOperation.ADVANCED_SEARCH, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object advancedSearch(@RequestParam(value = "accessToken") String accessToken,HttpServletRequest request,
                                 @RequestParam(value = "keywords", required = false) String keywords,
                                 @RequestParam(value = "groupName",defaultValue = "ALL",required = false) String groupName,
                                 @RequestParam(value = "sort", defaultValue = "desc", required = false) String sort,
                                 @RequestParam(value = "time",defaultValue = "7d",required = false) String time,
                                 @RequestParam(value = "pageNo",defaultValue = "0") int pageNo,
                                 @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                 @RequestParam(value = "simflag", required = false) String simflag,
                                 @ApiParam("条件筛选-情感，默认全部情绪") @RequestParam(value = "emotion", defaultValue = "ALL",required = false) String emotion,
                                 @ApiParam("条件筛选-论坛主贴 0 /回帖 1，默认主贴+回帖") @RequestParam(value = "invitationCard", required = false) String invitationCard,
                                 @ApiParam("条件筛选-微博 原发 primary / 转发 forward ，默认原发+转发") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary)throws OperationException,TRSException{

        pageSize = pageSize>=1?pageSize:10;
        ApiAccessToken token = getToken(request);
        int maxPageSize = token.getMaxPageSize();
        if (pageSize > maxPageSize){
            pageSize = maxPageSize;
        }
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("keyWords",keywords);
        jsonObject.put("wordSpace",0);
        jsonObject.put("wordOrder",false);
        jsonArray.add(jsonObject);
        keywords = jsonArray.toJSONString();
        return infoListController.searchList(pageNo,pageSize,sort,keywords,"precise",time,simflag,"1",false,"","",emotion,
                0,false,"","","1","ALL","","","",
                "","","","","",invitationCard,forwarPrimary,"","",groupName,"");
    }

    /**
     * 根据表达式检索数据 支持分页，支持自定义页长
     * @param accessToken   api接口token值
     * @param request
     * @param trsl          trsl检索表达式
     * @param groupName  来源
     * @param pageNo        页码
     * @param pageSize      页长
     * @return
     * @throws OperationException
     * @throws TRSException
     */
    @Api(value = "select data by trsl表达式",method = ApiMethod.ExpertSearch)
    @PostMapping("/expertSearch")
    @Log(systemLogOperation = SystemLogOperation.EXPERT_SEARCH, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object expertSearch( @RequestParam(value = "accessToken") String accessToken,HttpServletRequest request, @RequestParam(value = "trsl",required = false) String trsl,
                              @RequestParam(value = "groupName",defaultValue = "ALL",required = false) String groupName,@RequestParam(value = "sort", defaultValue = "default", required = false) String sort,
                                @RequestParam(value = "time",defaultValue = "7d",required = false) String time, @RequestParam(value = "pageNo",defaultValue = "0") int pageNo, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                @ApiParam("条件筛选-情感，默认全部情绪") @RequestParam(value = "emotion", defaultValue = "ALL",required = false) String emotion,
                                @ApiParam("条件筛选-论坛主贴 0 /回帖 1，默认主贴+回帖") @RequestParam(value = "invitationCard", required = false) String invitationCard,
                                @ApiParam("条件筛选-微博 原发 primary / 转发 forward ，默认原发+转发") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary)throws OperationException,TRSException{
        //防止前端乱输入
        pageSize = pageSize>=1?pageSize:10;
        ApiAccessToken token = getToken(request);
        int maxPageSize = token.getMaxPageSize();
        if (pageSize > maxPageSize){
            pageSize = maxPageSize;
        }
        return apiService.expertSearch(trsl,groupName,sort,time,emotion,invitationCard,forwarPrimary,pageNo,pageSize);
    }

    /**
     * 数据详情api
     * @param accessToken   api接口token值
     * @param request
     * @param groupName  来源
     * @param nreserved1
     * @param id            数据唯一值
     * @return
     * @throws OperationException
     * @throws TRSException
     */
    @Api(value = "select document detail",method = ApiMethod.DocumentDetail)
    @GetMapping("/documentDetail")
    @Log(systemLogOperation = SystemLogOperation.DOCUMENT_DETAIL, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object documentDetail( @RequestParam(value = "accessToken") String accessToken,HttpServletRequest request,
                                  //@ApiParam("表达式存入redis时的key值，用于关键字描红") @RequestParam(value = "trslk",required = false) String trslk,
                                  @ApiParam("当前需查看详情的文章的groupName值,必填参数") @RequestParam(value = "groupName",required = true) String groupName,
                                  @ApiParam("国内论坛主、回帖标识，当前文章groupName若为国内论坛，可将nreserved1值对应填入此参数") @RequestParam(value = "nreserved1",required = false) String nreserved1,
                                  @ApiParam("当前需查看详情的文章的唯一值，其中微信，即国内微信取hKey的值，其他均取sid的值，必填参数")@RequestParam(value = "id", required = true) String id)throws OperationException,TRSException{

        String trslk = null;
        if (Const.MEDIA_TYPE_NEWS.contains(groupName)){
            //传统
            return infoListController.oneInfo(id,null,trslk,nreserved1);
        }else if (Const.MEDIA_TYPE_WEIBO.contains(groupName)){
            //微博
            return infoListController.oneInfoStatus(id,null,trslk);
        }else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)){
            //微信
            return infoListController.oneInfoWeChat(id,null,trslk);
        }else if (Const.MEDIA_TYPE_TF.contains(groupName)){
            //twitter、facebook
            return infoListController.oneInfoTF(id,null,trslk);
        }

        throw new OperationException("请填写正确的groupName值");
    }

    /**
     * 获取网察原数据api
     * @param accessToken   api接口token值
     * @param request
     * @param //requestTime 请求时间
     * @param //period 时间间隔
     * @param //duplicateFlag 去重标识
     * @param //traditonExp 传统检索表达式
     * @param //weixinExp 微信检索表达式
     * @param //weiboExp 微博检索表达式
     * @return
     * @throws OperationException
     * @throws TRSException
     */
    @Api(value = "get original data",method = ApiMethod.getOriginalData)
    @RequestMapping(value="/getOriginalData" ,method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
   // @Log(systemLogOperation = SystemLogOperation.GET_INDEX_PAGE, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getOriginalData(
            @RequestParam(value = "accessToken") String accessToken,HttpServletRequest request,
            @ApiParam("参数") @RequestBody JSONObject jsonObject)throws OperationException,TRSException {

        log.info("平安金服预警接口访问");
        if(jsonObject == null ){
            log.info("接收JSONObject数据为：null");
            throw new TRSException("接收JSONObject数据为：null");
        }
        String requestTime = jsonObject.getString("requestTime");
        Integer period = jsonObject.getInteger("period");
        Integer duplicateFlag = jsonObject.getInteger("duplicateFlag");
        String traditonExp = jsonObject.getString("traditonExp");
        String weixinExp = jsonObject.getString("weixinExp");
        String weiboExp = jsonObject.getString("weiboExp");
        log.info("平安金服预警接口参数为："+jsonObject.toJSONString());
        ApiAccessToken token = getToken(request);
        if(!DateUtil.isTimeFormatter(requestTime)){
            throw new TRSException("时间格式不正确");
        }
        if(period <1){
            throw new TRSException("请求时间间隔不可用，为："+period);
        }
        boolean isSimilar = false;
        boolean irSimflag = false;
        boolean irSimflagAll = false;
        if(duplicateFlag == 1){
            irSimflagAll = true;
        }else if(duplicateFlag == 2){
            isSimilar = true;
        }else if(duplicateFlag == 3){
            irSimflag = true;
        }

        List<Object> list = infoListService.getOriginalData(traditonExp,weiboExp,weixinExp,requestTime,period,isSimilar,irSimflag,irSimflagAll);
       return list;
    }

    /**
     * 获取单条微博分析列表
     *
     * @param accessToken
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get all single microblog list", method = ApiMethod.MicroblogList)
    @GetMapping("/getMicroblogList")
    @Log(systemLogOperation = SystemLogOperation.MICRO_BLOG_LIST, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getMicroblogList(@RequestParam(value = "accessToken") String accessToken, HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "latelyTime");
        List<SingleMicroblogData> microblogDatas = singleMicroblogDataService.findAll(user, MicroblogConst.MICROBLOGLIST,sort);
        return microblogDatas;
    }
    /**
     * 获取当前微博的博主信息
     * @param accessToken
     * @param originalUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get blogger information", method = ApiMethod.BloggerInfo)
    @GetMapping("/getBloggerInfo")
    @Log(systemLogOperation = SystemLogOperation.BLOGGER_INFO, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getBloggerInfo(@RequestParam(value = "accessToken") String accessToken,
                                 @RequestParam(value = "originalUrl", required = true) String originalUrl, HttpServletRequest request)
            throws TRSException {
        if (StringUtil.isEmpty(originalUrl)){
            throw new TRSException(CodeUtils.STATUS_URLNAME, "请输入微博地址！");
        }
        if (originalUrl.indexOf("?") != -1){
            //有问号
            originalUrl = originalUrl.split("\\?")[0];
        }
        originalUrl = originalUrl.replace("https","http");
        QueryBuilder queryBuilder = new QueryBuilder();
        //根据urlName查询
        queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+originalUrl+"\"",Operator.Equal);
        List<SpreadObject> statuses = hybase8SearchService.ftsQuery(queryBuilder, SpreadObject.class, false, false,false,null);
        if (ObjectUtil.isEmpty(statuses)){
            return null;
        }
        SpreadObject spreadObject = statuses.get(0);

        QueryBuilder queryStatusUser = new QueryBuilder();
        queryStatusUser.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        queryStatusUser.setDatabase(Const.SINAUSERS);
        //查询微博用户信息
        List<StatusUser> statusUsers = hybase8SearchService.ftsQuery(queryStatusUser, StatusUser.class, false, false,false,null);
        if (ObjectUtil.isEmpty(statusUsers)){
            QueryBuilder queryStatusUser1 = new QueryBuilder();
            queryStatusUser1.filterField(FtsFieldConst.FIELD_UID,"\""+spreadObject.getUid()+"\"",Operator.Equal);
            queryStatusUser1.setDatabase(Const.SINAUSERS);
            //查询微博用户信息
            statusUsers = hybase8SearchService.ftsQuery(queryStatusUser1, StatusUser.class, false, false,false,null);
        }
        if (ObjectUtil.isNotEmpty(statusUsers)){
            //返回该条微博对应的 发布人信息
            return statusUsers.get(0);
        }
        return null;
    }

    /**
     * 获取当前微博信息
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get blog detail", method = ApiMethod.MicroBlogDetail)
    @GetMapping("/getMicroBlogDetail")
    @Log(systemLogOperation = SystemLogOperation.MICRO_BLOG_DETAIL, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getMicroBlogDetail(@RequestParam(value = "accessToken") String accessToken,
                                 @RequestParam(value = "currentUrl", required = true) String currentUrl, HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData microBlog = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.MICROBLOGDETAIL);
        if (ObjectUtil.isEmpty(microBlog)){
            return null;
        }
        Object data = microBlog.getData();

        return data;
    }

    /**
     * 添加微博分析
     * @param accessToken
     * @param urlName
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "save microblog", method = ApiMethod.SaveMicroblog)
    @GetMapping("/saveMicroblog")
    @Log(systemLogOperation = SystemLogOperation.SAVE_MICROBLOG, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object saveMicroblog(@RequestParam(value = "accessToken") String accessToken,
                                     @RequestParam(value = "urlName", required = true) String urlName, HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        if (urlName.indexOf("?") != -1){
            //有问号
            urlName = urlName.split("\\?")[0];
        }
        urlName = urlName.replace("https","http");
        String random = UUID.randomUUID().toString().replace("-", "");
        String currentUrl = urlName+random;
        //User loginUser = UserUtils.getUser();
        List<SingleMicroblogData> states = singleMicroblogDataService.findStates(user, MicroblogConst.MICROBLOGLIST,"分析中");
        //列表查询
        SingleMicroblogData microblogList = new SingleMicroblogData(MicroblogConst.MICROBLOGLIST,urlName,currentUrl);
        microblogList.setUserId(user.getId());
        microblogList.setSubGroupId(user.getSubGroupId());
        microblogList.setLatelyTime(new Date());
        if (ObjectUtil.isNotEmpty(states) && states.size() > 0){
            microblogList.setState("正在排队");
        }else {
            microblogList.setState("分析中");
        }

        try {
            SpreadObject spreadObject = singleMicroblogService.currentUrlMicroBlog(urlName);
            if (ObjectUtil.isEmpty(spreadObject)){
                return null;
            }
            microblogList.setData(spreadObject);
        } catch (TRSException e) {
            log.error(MicroblogConst.MICROBLOGLIST,e);
        }
        microblogList.setRandom(random);
        microblogList.setLastModifiedTime(new Date());
        SingleMicroblogData microblogData = singleMicroblogDataService.insert(microblogList);
        //查询hybase，并将结果放在mongodb中
        if (ObjectUtil.isNotEmpty(microblogData) && "分析中".equals(microblogData.getState())){
            singleMicroblogService.dataAnalysisApi(user,microblogData.getOriginalUrl(),microblogData.getCurrentUrl(),microblogData.getRandom());
        }

        return microblogData;

    }

    /**
     * 查看微博分析进度
     * @param accessToken
     * @param urlName
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "confirmStep", method = ApiMethod.StepMicroblog)
    @GetMapping("/confirmStep")
    //@Log(systemLogOperation = SystemLogOperation.SAVE_MICROBLOG, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object confirmStep(@RequestParam(value = "accessToken") String accessToken,
                                @RequestParam(value = "currentUrl", required = true) String urlName, HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        Object confirmStep = singleMicroblogService.confirmStepApi(user,urlName);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，查看查询进度共用时长："+(end-start));
        return confirmStep;

    }
    /**
     * 更新已完成的微博分析
     * @param accessToken
     * @param id
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "update microblog", method = ApiMethod.UpdateMicroblog)
    @GetMapping("/updateMicroblog")
    @Log(systemLogOperation = SystemLogOperation.UPDATE_MICROBLOG, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object updateMicroblog(@RequestParam(value = "accessToken") String accessToken,
                                  @ApiParam("已分析完成的微博id")@RequestParam(value = "id", required = true) String id, HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData singleMicroblogData = singleMicroblogDataService.findOne(id);
        if (ObjectUtil.isEmpty(singleMicroblogData)){
            throw new OperationException("无效id");
        }

        String random = UUID.randomUUID().toString().replace("-", "");
        singleMicroblogData.setRandom(random);
        //User loginUser = UserUtils.getUser();
        List<SingleMicroblogData> states = singleMicroblogDataService.findStates(user, MicroblogConst.MICROBLOGLIST,"分析中");
        //列表查询
        singleMicroblogData.setLatelyTime(new Date());
        if (ObjectUtil.isNotEmpty(states) && states.size() > 0){
            singleMicroblogData.setState("正在排队");

        }else {
            singleMicroblogData.setState("分析中");
        }

        try {
            SpreadObject spreadObject = singleMicroblogService.currentUrlMicroBlog(singleMicroblogData.getOriginalUrl());
            if (ObjectUtil.isEmpty(spreadObject)){
                return null;
            }
            singleMicroblogData.setData(spreadObject);
        } catch (TRSException e) {
            log.error(MicroblogConst.MICROBLOGLIST,e);
        }
        SingleMicroblogData microblogData = singleMicroblogDataService.save(singleMicroblogData);
        //查询hybase，并将结果放在mongodb中
        if (ObjectUtil.isNotEmpty(microblogData) && "分析中".equals(microblogData.getState())){
            singleMicroblogService.dataAnalysis(microblogData.getOriginalUrl(),microblogData.getCurrentUrl(),microblogData.getRandom());
        }


        return microblogData;

    }

    /**
     * 删除的微博分析
     * @param accessToken
     * @param id
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "delete microblog", method = ApiMethod.DeleteMicroblog)
    @GetMapping("/deleteMicroblog")
    @Log(systemLogOperation = SystemLogOperation.DELETE_MICROBLOG, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object deleteMicroblog(@RequestParam(value = "accessToken") String accessToken,
                                  @ApiParam("微博id")@RequestParam(value = "id", required = true) String id, HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        if(ObjectUtil.isNotEmpty(user)){
            singleMicroblogDataService.remove(id);
            return "删除成功";
        }
        return null;

    }

    /**
     * 热门评论TOP5
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get hot reviews TOP5", method = ApiMethod.HotReviews)
    @GetMapping("/getHotReviews")
    @Log(systemLogOperation = SystemLogOperation.HOT_REVIEWS, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getHotReviewsTop(@RequestParam(value = "accessToken") String accessToken,
                                 @RequestParam(value = "currentUrl", required = true) String currentUrl, HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData hotReviews = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.HOTREVIEWS);
        if (ObjectUtil.isEmpty(hotReviews)){
            return null;
        }
        Object hotReviewsData = hotReviews.getData();
        return hotReviewsData;
    }

    /**
     * 传播分析
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get spread analysis", method = ApiMethod.SpreadAnalysis)
    @GetMapping("/getSpreadAnalysis")
    @Log(systemLogOperation = SystemLogOperation.SPREAD_ANALYSIS, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getSpreadAnalysis(@RequestParam(value = "accessToken") String accessToken,
                                 @RequestParam(value = "currentUrl", required = true) String currentUrl, HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData spreadAnalysis = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.SPREADANALYSIS);
        if (ObjectUtil.isEmpty(spreadAnalysis)){
            return null;
        }
        Object spreadAnalysisData = spreadAnalysis.getData();
        return spreadAnalysisData;
    }

    /**
     * 被转发趋势
     * @param accessToken
     * @param currentUrl
     * @param urlTime
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get forwardedTrend data", method = ApiMethod.ForwardedTrend)
    @GetMapping("/getForwardedTrend")
    @Log(systemLogOperation = SystemLogOperation.FORWARDED_TREND, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getForwardedTrend(@RequestParam(value = "accessToken") String accessToken,
                                    @RequestParam(value = "currentUrl", required = true) String currentUrl,
                                    @ApiParam("微博发布时间,时间格式yyyy-MM-dd HH:mm:ss") @RequestParam(value = "urlTime",required = false)String urlTime,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData forwardedTrend = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.FORWARDEDTREND);
        if (ObjectUtil.isEmpty(forwardedTrend)){
            return null;
        }
        Map<String, Map<String, List<Object>>> forwardedTrendData = (Map<String, Map<String, List<Object>>>) forwardedTrend.getData();
        if (urlTime != null && urlTime.length() > 0) {
            if (urlTime.length() == 10) {
                urlTime = urlTime + " 00:00:00";
            }
            String start_day = urlTime.substring(0, 10);
            String start_hour = urlTime.substring(11, 13);
            Map<String, List<Object>> map_hour = (Map<String, List<Object>>) forwardedTrendData.get("hour");
            List<Object> hour_date = (List<Object>) map_hour.get("date");
            int hour_num = hour_date.indexOf(start_day);
            if (hour_num != -1) {
                for (int i = hour_num; i < hour_date.size(); i++) {
                    if (start_hour.equals((String) hour_date.get(i))) {
                        hour_num = i;
                        break;
                    }
                }
                List<Object> hour_count = (List<Object>) map_hour.get("count");
                for (int i = hour_num - 1; i >= 0; i--) {
                    hour_date.remove(i);
                    hour_count.remove(i);
                }
            }
            Map<String, List<Object>> map_day = (Map<String, List<Object>>) forwardedTrendData.get("day");
            List<Object> day_date = (List<Object>) map_day.get("date");
            int day_num = day_date.indexOf(start_day);
            if (day_num != -1) {
                List<Object> day_count = (List<Object>) map_day.get("count");
                for (int i = day_num - 1; i >= 0; i--) {
                    day_date.remove(i);
                    day_count.remove(i);
                }
            }
        }
        return forwardedTrendData;
    }

    /**
     * 传播路径
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get spreadPath data", method = ApiMethod.SpreadPath)
    @GetMapping("/getSpreadPath")
    @Log(systemLogOperation = SystemLogOperation.SPREAD_PATH, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getSpreadPath(@RequestParam(value = "accessToken") String accessToken,
                                    @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData spreadPath = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.SPREADPATH);
        if (ObjectUtil.isEmpty(spreadPath)){
            return null;
        }
        Object spreadPathData = spreadPath.getData();
        return spreadPathData;
    }

    /**
     * 核心转发
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get coreForward data", method = ApiMethod.CoreForward)
    @GetMapping("/getCoreForward")
    @Log(systemLogOperation = SystemLogOperation.CORE_FORWARD, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getCoreForward(@RequestParam(value = "accessToken") String accessToken,
                                @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData coreForward = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.COREFORWARD);
        if (ObjectUtil.isEmpty(coreForward)){
            return null;
        }
        Object coreForwardData = coreForward.getData();
        return coreForwardData;
    }

    /**
     * 意见领袖
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get opinionLeaders data", method = ApiMethod.OpinionLeaders)
    @GetMapping("/getOpinionLeaders")
    @Log(systemLogOperation = SystemLogOperation.OPINION_LEADERS, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getOpinionLeaders(@RequestParam(value = "accessToken") String accessToken,
                                 @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData opinionLeaders = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.OPINIONLEADERS);
        if (ObjectUtil.isEmpty(opinionLeaders)){
            return null;
        }
        Object opinionLeadersData = opinionLeaders.getData();
        return opinionLeadersData;
    }

    /**
     * 转发博主地域分析
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get areaAnalysisOfForWarders data", method = ApiMethod.AreaAnalysisOfForWarders)
    @GetMapping("/getAreaAnalysisOfForWarders")
    @Log(systemLogOperation = SystemLogOperation.AREAANALYSIS_OF_FORWARDERS, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getAreaAnalysisOfForWarders(@RequestParam(value = "accessToken") String accessToken,
                                    @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData areaAnalysisOfForWarders = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.AREAANALYSISOFFORWARDERS);
        if (ObjectUtil.isEmpty(areaAnalysisOfForWarders)){
            return null;
        }
        Object areaAnalysisOfForWardersData = areaAnalysisOfForWarders.getData();
        return areaAnalysisOfForWardersData;
    }

    /**
     * 转发微博表情分析
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get emojiAnalysisOfForward data", method = ApiMethod.EmojiAnalysisOfForward)
    @GetMapping("/getEmojiAnalysisOfForward")
    @Log(systemLogOperation = SystemLogOperation.EMOJI_ANALYSIS_OF_FORWARD, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getEmojiAnalysisOfForward(@RequestParam(value = "accessToken") String accessToken,
                                              @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData emojiAnalysisOfForward = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.EMOJIANALYSISOFFORWARD);
        if (ObjectUtil.isEmpty(emojiAnalysisOfForward)){
            return null;
        }
        List<GroupInfo> emojiAnalysisOfForwardData = (List<GroupInfo>)emojiAnalysisOfForward.getData();
        if (ObjectUtil.isNotEmpty(emojiAnalysisOfForwardData)){
            for (GroupInfo groupInfo : emojiAnalysisOfForwardData) {
                String pinyin = PinyinUtil.toPinyinWithPolyphone(groupInfo.getFieldValue());
                groupInfo.setFieldValue(pinyin);
            }
        }
        return emojiAnalysisOfForwardData;
    }

    /**
     * 男女占比
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get genderOfRatio data", method = ApiMethod.GenderOfRatio)
    @GetMapping("/getGenderOfRatio")
    @Log(systemLogOperation = SystemLogOperation.GENDER_OF_RATIO, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getGenderOfRatio(@RequestParam(value = "accessToken") String accessToken,
                                            @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData genderOfRatio = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.GENDEROFRATIO);
        if (ObjectUtil.isEmpty(genderOfRatio)){
            return null;
        }
        Object genderOfRatioData = genderOfRatio.getData();
        return genderOfRatioData;
    }

    /**
     * 认证比例
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get certifiedOfRatio data", method = ApiMethod.CertifiedOfRatio)
    @GetMapping("/getCertifiedOfRatio")
    @Log(systemLogOperation = SystemLogOperation.CERTIFIED_OF_RATIO, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getCertifiedOfRatio(@RequestParam(value = "accessToken") String accessToken,
                                   @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData certifiedOfRatio = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.CERTIFIEDOFRATIO);
        if (ObjectUtil.isEmpty(certifiedOfRatio)){
            return null;
        }
        Object certifiedOfRatioData = certifiedOfRatio.getData();
        return certifiedOfRatioData;
    }

    /**
     * 博主发文频率
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get dispatchFrequency data", method = ApiMethod.DispatchFrequency)
    @GetMapping("/getDispatchFrequency")
    @Log(systemLogOperation = SystemLogOperation.DISPATCH_FREQUENCY, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getDispatchFrequency(@RequestParam(value = "accessToken") String accessToken,
                                      @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData dispatchFrequency = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.DISPATCHFREQUENCY);
        if (ObjectUtil.isEmpty(dispatchFrequency)){
            return null;
        }
        Object dispatchFrequencyData = dispatchFrequency.getData();
        return dispatchFrequencyData;
    }

    /**
     * 参与话题统计
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get takeSuperLanguage data", method = ApiMethod.TakeSuperLanguage)
    @GetMapping("/getTakeSuperLanguage")
    @Log(systemLogOperation = SystemLogOperation.TAKE_SUPER_LANGUAGE, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getTakeSuperLanguage(@RequestParam(value = "accessToken") String accessToken,
                                       @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData takeSuperLanguage = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.TAKESUPERLANGUAGE);
        if (ObjectUtil.isEmpty(takeSuperLanguage)){
            return null;
        }
        Object takeSuperLanguageData = takeSuperLanguage.getData();
        return takeSuperLanguageData;
    }

    /**
     * 发文情感统计
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get emotionStatistics data", method = ApiMethod.EmotionStatistics)
    @GetMapping("/getEmotionStatistics")
    @Log(systemLogOperation = SystemLogOperation.EMOTION_STATISTICS, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getEmotionStatistics(@RequestParam(value = "accessToken") String accessToken,
                                       @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData emotionStatistics = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.EMOTIONSTATISTICS);
        if (ObjectUtil.isEmpty(emotionStatistics)){
            return null;
        }
        Object emotionStatisticsData = emotionStatistics.getData();
        return emotionStatisticsData;
    }

    /**
     * 原发转发占比
     * @param accessToken
     * @param currentUrl
     * @param request
     * @return
     * @throws TRSException
     */
    @Api(value = "get primaryForwardRatio data", method = ApiMethod.PrimaryForwardRatio)
    @GetMapping("/getPrimaryForwardRatio")
    @Log(systemLogOperation = SystemLogOperation.PRIMARY_FORWARD_RATIO, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object getPrimaryForwardRatio(@RequestParam(value = "accessToken") String accessToken,
                                       @RequestParam(value = "currentUrl", required = true) String currentUrl,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        SingleMicroblogData primaryForwardRatio = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.PRIMARYFORWARDRATIO);
        if (ObjectUtil.isEmpty(primaryForwardRatio)){
            return null;
        }
        Object primaryForwardRatioData = primaryForwardRatio.getData();
        return primaryForwardRatioData;
    }

    //大屏---网民情感趋势
    @Api(value = "Emotional trend of large screen Internet users", method = ApiMethod.NetizensFeelingsCount)
    @GetMapping("/netizensFeelingsCount")
    @EnableRedis(cacheMinutes=60)
    public Object getPrimaryForwardRatio(@RequestParam(value = "accessToken") String accessToken,HttpServletRequest request)
            throws TRSException {
        ApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        User user = userRepository.findOne(userId);
        String ownerId = userId;
        if (StringUtil.isNotEmpty(user.getSubGroupId())){
            ownerId = user.getSubGroupId();
        }
//        HybaseShard trsHybaseShard = hybaseShardService.findByOwnerUserId(ownerId);
        HybaseShard trsHybaseShard;
        if (UserUtils.isRolePlatform()){
            //运维
             trsHybaseShard = hybaseShardService.findByOwnerUserId(ownerId);
        }else {
             trsHybaseShard = hybaseShardService.findByOrganizationId(user.getOrganizationId());
        }

        String indices = Const.MIX_DATABASE;
        if (UserUtils.isRoleVisitor() && ObjectUtil.isNotEmpty(trsHybaseShard)){
            if (indices.contains(Const.HYBASE_NI_INDEX) && StringUtil.isNotEmpty(trsHybaseShard.getTradition())){
                indices = indices.replaceAll(Const.HYBASE_NI_INDEX,trsHybaseShard.getTradition());
            }
            if (indices.contains(Const.WEIBO) && StringUtil.isNotEmpty(trsHybaseShard.getWeiBo())){
                indices = indices.replaceAll(Const.WEIBO,trsHybaseShard.getWeiBo());
            }
            if (indices.contains(Const.WECHAT) && StringUtil.isNotEmpty(trsHybaseShard.getWeiXin())){
                indices = indices.replaceAll(Const.WECHAT,trsHybaseShard.getWeiXin());
            }
            if (indices.contains(Const.HYBASE_OVERSEAS) && StringUtil.isNotEmpty(trsHybaseShard.getOverseas())){
                indices = indices.replaceAll(Const.HYBASE_OVERSEAS,trsHybaseShard.getOverseas());
            }
        }

        String trsl = "IR_URLTITILE:((\"新型冠状病毒\" OR \"新型肺炎\" OR \"武汉肺炎\" OR \"新冠病毒\" OR \"新冠肺炎\" OR \"不明肺炎\" OR \"肺炎\" OR \"2019-nCoV\" OR \"钟南山\" OR \"火神山\" OR \"雷神山\" OR \"气溶胶\" OR \"飞沫\" OR \"NCP\" OR \"防疫\" OR \"战疫\" OR \"抗疫\" OR \"疫区\" OR \"小汤山\" OR \"菊头蝠\" OR \"new coronavirus pneumonia\" OR \"novel coronavirus pneumonia\" OR \"隔离\" OR \"口罩\" OR \"消毒水\" OR \"消毒液\" OR \"密切接触\" OR \"医学观察\" OR \"隔离\" OR \"复工\" OR \"复产\" OR \"恢复生产\" OR \"恢复工作\" OR \"封城\" OR \"防控\" OR \"防护用品\" OR \"疫情\" OR \"检疫\") OR ((\"确诊\" OR \"疑似\" OR \"死亡\" OR \"治愈\" OR \"新增\") AND (病例 OR \"案例\" OR 人数)) OR ((武汉 OR \"湖北\" OR \"疫区\" OR \"一线人员\" OR \"一线工作\" OR \"抗疫一线\") AND (\"支援\" OR \"驰援\" OR \"援助\" OR \"助力\" OR \"加油\" OR \"坚持\" OR \"捐赠\" OR \"捐助\" OR \"物资\" OR \"医疗\" OR \"救助\" OR \"医护\" OR \"奔赴\" OR \"出征\"))) OR IR_CONTENT:((\"新型冠状病毒\" OR \"新型肺炎\" OR \"武汉肺炎\" OR \"新冠病毒\" OR \"新冠肺炎\" OR \"不明肺炎\" OR \"肺炎\" OR \"2019-nCoV\" OR \"钟南山\" OR \"火神山\" OR \"雷神山\" OR \"气溶胶\" OR \"飞沫\" OR \"NCP\" OR \"防疫\" OR \"战疫\" OR \"抗疫\" OR \"疫区\" OR \"小汤山\" OR \"菊头蝠\" OR \"new coronavirus pneumonia\" OR \"novel coronavirus pneumonia\" OR \"隔离\" OR \"口罩\" OR \"消毒水\" OR \"消毒液\" OR \"密切接触\" OR \"医学观察\" OR \"隔离\" OR \"复工\" OR \"复产\" OR \"恢复生产\" OR \"恢复工作\" OR \"封城\" OR \"防控\" OR \"防护用品\" OR \"疫情\" OR \"检疫\") OR ((\"确诊\" OR \"疑似\" OR \"死亡\" OR \"治愈\" OR \"新增\") AND (病例 OR \"案例\" OR 人数)) OR ((武汉 OR \"湖北\" OR \"疫区\" OR \"一线人员\" OR \"一线工作\" OR \"抗疫一线\") AND (\"支援\" OR \"驰援\" OR \"援助\" OR \"助力\" OR \"加油\" OR \"坚持\" OR \"捐赠\" OR \"捐助\" OR \"物资\" OR \"医疗\" OR \"救助\" OR \"医护\" OR \"奔赴\" OR \"出征\")))";
        String[] timeRange = DateUtil.formatTimeRange("2020-02-03 00:00:00;至今");
        List<String> list = new ArrayList<>();
        list = DateUtil.getBetweenDateString(timeRange[0], timeRange[1], DateUtil.yyyyMMddHHmmss, DateUtil.yyyyMMdd4);
        //正面
        String posTrsl = "IR_APPRAISE:正面";
        QueryBuilder posQueryBuilder = new QueryBuilder();
        posQueryBuilder.filterByTRSL(trsl);
        posQueryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,timeRange,Operator.Between);
        posQueryBuilder.filterByTRSL(posTrsl);
        posQueryBuilder.setDatabase(indices);
        posQueryBuilder.page(0,list.size());
        GroupResult posResult = hybase8SearchService.categoryQuery(posQueryBuilder, false, false, false, FtsFieldConst.FIELD_URLTIME, "detail", indices.split(";"));
        //负面
        String negTrsl = "IR_APPRAISE_NEW:负面";
        QueryBuilder negQueryBuilder = new QueryBuilder();
        negQueryBuilder.filterByTRSL(trsl);
        negQueryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,timeRange,Operator.Between);
        negQueryBuilder.filterByTRSL(negTrsl);
        negQueryBuilder.setDatabase(indices);
        negQueryBuilder.page(0,list.size());
        GroupResult negResult = hybase8SearchService.categoryQuery(negQueryBuilder, false, false, false, FtsFieldConst.FIELD_URLTIME, "detail", indices.split(";"));

        //结果返回格式处理
        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("date",list);
        mapResult.put("positive", MapUtil.sortAndChangeList(posResult, list, "yyyy/MM/dd", true));
        mapResult.put("negative", MapUtil.sortAndChangeList(negResult, list, "yyyy/MM/dd", true));
        return mapResult;
    }

    //数据中心 --- 微博主贴热点
    @Api(value = "Micro-blog hotspots for data center", method = ApiMethod.NetizensFeelingsCount)
    @GetMapping("/hotSpotsOfWeiBo")
    @Log(systemLogOperation = SystemLogOperation.GET_INDEX_PAGE, systemLogType = SystemLogType.API, systemLogOperationPosition = "")
    public Object hotSpotsOfWeiBo(@RequestParam(value = "accessToken") String accessToken,HttpServletRequest request)
            throws TRSException {
        String yesToday = DateUtil.getYesToday();
        java.text.DateFormat format = new java.text.SimpleDateFormat(DateUtil.yyyyMMdd2);
        String start = format.format(new Date());

        String startTime = "";
        String endTime = "";
        Calendar calendar = Calendar.getInstance();
        int curHour = calendar.get(calendar.HOUR_OF_DAY);
        if (curHour > 0){
            startTime = String.valueOf(curHour - 1);
            startTime = startTime.length()==1?"0"+startTime:startTime;
            startTime = start + startTime;

        }else {
            startTime = yesToday+"23";
        }

        endTime = String.valueOf(curHour);
        endTime = endTime.length()==1?"0"+endTime:endTime;
        endTime = start + endTime;

        String[] timeArray = new String[]{startTime+"0000",endTime+"0000"};

        String redisKey = "dataCenterOfHotSpotsOfWeiBo"+timeArray[1];
        List<FtsDocumentStatus> documentStatuses = redisOperator.getObject(14, redisKey,FtsDocumentStatus.class);
       // List<FtsDocumentStatus> documentStatuses = RedisUtil.getList(redisKey, FtsDocumentStatus.class);
        if (ObjectUtil.isEmpty(documentStatuses)){
            QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.setDatabase(Const.WEIBO);
            queryBuilder.page(0,100);

            queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,timeArray,Operator.Between);
            String trsl = "";

            //加入排除词
            List<KnowledgeBase> byEffective = knowledgeBaseService.findByClassify(KnowledgeClassify.Exclude);
            if (ObjectUtil.isNotEmpty(byEffective) && byEffective.size() > 0){
                String keywordsTotal = "";
                for (KnowledgeBase knowledgeBase : byEffective) {
                    String keywords = knowledgeBase.getKeywords();
                    if (StringUtil.isNotEmpty(keywords)){
                        if (keywords.startsWith(";")){
                            keywords = keywords.substring(1,keywords.length());
                        }
                        if (keywords.endsWith(";")){
                            keywords = keywords.substring(0,keywords.length()-1);
                        }

                        keywordsTotal += ";"+keywords;

                    }
                }
                keywordsTotal = keywordsTotal + ";";
                if (StringUtil.isNotEmpty(keywordsTotal)){
                    if (keywordsTotal.startsWith(";")){
                        keywordsTotal = keywordsTotal.substring(1,keywordsTotal.length());
                    }
                    if (keywordsTotal.endsWith(";")){
                        keywordsTotal = keywordsTotal.substring(0,keywordsTotal.length()-1);
                    }
                    StringBuilder exbuilder = new StringBuilder();
                    exbuilder.append("(\"")
                            .append(keywordsTotal.replaceAll("[;|；]+", "\" OR \"")).append("\")");

                    trsl = "(*:* -"+ FtsFieldConst.FIELD_CONTENT + ":" + exbuilder.toString() +")";
                }

            }
            queryBuilder.filterByTRSL(trsl);
            queryBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
            GroupResult groupResult = hybase8SearchService.categoryQuery(queryBuilder, false, false, false, FtsFieldConst.FIELD_MD5TAG, "detail", Const.WEIBO);
            List<FtsDocumentStatus> ftsDocumentStatusses = new ArrayList<>();
            List<String> tags = new ArrayList<>();
            int totalCount = 0;
            if (ObjectUtil.isNotEmpty(groupResult) && ObjectUtil.isNotEmpty(groupResult.getGroupList()) && groupResult.getGroupList().size() > 0){
                List<GroupInfo> groupList = groupResult.getGroupList();
                for (GroupInfo groupInfo : groupList) {
                    if (totalCount == 50){
                        break;
                    }
                    QueryBuilder query = new QueryBuilder();
                    query.filterByTRSL(queryBuilder.asTRSL());
                    query.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
                    query.page(0, 1);
                    query.orderBy(FtsFieldConst.FIELD_URLTIME, true);
                    List<FtsDocumentStatus> docs = hybase8SearchService.ftsQuery(query, FtsDocumentStatus.class, false,false,
                            false,"detail");
                    if (null != docs && docs.size() > 0) {
                        FtsDocumentStatus doc = docs.get(0);
                        doc.setSim((int)groupInfo.getCount());

                        String content = doc.getContent();
                        if (StringUtil.isNotEmpty(content) && content.contains("#")){
                            String[] split = content.split("#");
                            if (split.length > 2){
                                String tag = split[1];
                                if (!tags.contains(tag)){
                                    tags.add(tag);
                                    ftsDocumentStatusses.add(doc);
                                    totalCount ++;
                                }
                            }
                        }else {
                            ftsDocumentStatusses.add(doc);
                            totalCount ++;
                        }
                    }
                }
            }
            if (ObjectUtil.isNotEmpty(ftsDocumentStatusses)){
                documentStatuses = ftsDocumentStatusses;
                //RedisUtil.setListForObject(redisKey,ftsDocumentStatusses,24, TimeUnit.HOURS);
                redisOperator.set(14,redisKey,ftsDocumentStatusses,24, TimeUnit.HOURS);
            }
        }
        return documentStatuses;
    }
}
