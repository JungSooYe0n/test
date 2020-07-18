package com.trs.netInsight.support.appApi.controller;

import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.support.appApi.handler.AppApi;
import com.trs.netInsight.support.appApi.service.IApiService;
import com.trs.netInsight.support.appApi.service.IOAuthService;
import com.trs.netInsight.support.appApi.utils.constance.ApiMethod;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.util.EncodeString;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.PageAlertSend;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.column.service.IColumnService;
import com.trs.netInsight.widget.column.service.IIndexPageService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.report.service.IReportService;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.thinkTank.entity.ThinkTankData;
import com.trs.netInsight.widget.thinkTank.service.IThinkTankDataService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.mail.search.SearchException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Api(description = "APP端")
@RestController
@RequestMapping("/app/api")
@Slf4j
public class AppApiController {
    @Autowired
    private IIndexPageService indexPageService;

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
    private IIndexTabMapperService indexTabMapperService;

    @Autowired
    private IApiService apiService;

    @Autowired
    private IReportService reportService;

    @Autowired
    private IOAuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IColumnService columnService;

    @Autowired
    private IThinkTankDataService thinkTankDataService;

    @Autowired
    private ICommonListService commonListService;
    @Autowired
    private ICommonChartService commonChartService;

    // @Autowired
    // private LogPrintUtil loginpool;
    /**
     * 线程池跑任务
     */
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

    /**
     * app登录
     * duhq 2019/4/12
     * @return list
     */
    @GetMapping("/applogin")
    public Object appLogin(@RequestParam(value = "userAccount") String userAccount,
                           @RequestParam(value = "passWord") String passWord) throws TRSException{
        return apiService.loginAndGetToken(userAccount,passWord);
    }

    /**
     * 日常监测获取一级菜单
     * duhq 2019/4/9
     * @return list
     */
    @AppApi(value = "selectAppColumnOne",method = ApiMethod.selectAppColumnOne)
    @PostMapping("/appcolumnone")
    public Object appColumnOne(@RequestParam(value = "accessToken") String accessToken,HttpServletRequest request) {
        log.debug("------------------------>");
        AppApiAccessToken token = getToken(request);
        String userId = token.getGrantSourceOwnerId();
        return indexPageService.findByUserId(userId);
    }

    /**
     * 日常监测获取二级菜单
     * duhq 2019/4/9
     * @return list
     */
    @AppApi(value = "selectAppColumnTwo",method = ApiMethod.selectAppColumnTwo)
    @PostMapping("/appcolumntwo")
    public Object appColumnTwo(@RequestParam(value = "accessToken") String accessToken,
                                  @RequestParam(value = "typeId",required = false) String typeId,
                                  HttpServletRequest request) {
        AppApiAccessToken token = getToken(request);
//        String userId = token.getGrantSourceOwnerId();
//        String subGoupId = token.getSubGroupId();

//        try {
//            columnService.selectColumn(token.getUser(), typeId);
//            List<Map<String, Object>> result = new ArrayList<>();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return indexPageService.findByUserIdAndTypeId(token,typeId);
    }

    /**
     * 日常监测获取三级菜单
     * duhq 2019/4/9
     * @return list
     */
    @AppApi(value = "selectAppColumnThr",method = ApiMethod.selectAppColumnThr)
    @PostMapping("/appcolumnthr")
    public Object appColumnThr(@RequestParam(value = "accessToken") String accessToken,
                               @RequestParam(value = "parentId") String parentId,
                               HttpServletRequest request) {
        IndexPage indexPage = indexPageService.findOne(parentId);
        List<IndexTabMapper> list = indexTabMapperService.findByIndexPageOfApp(indexPage);
        return list;
    }

    /**
     *  获取日常检测chart数据
     * @param accessToken      验证身份的token值
     * @param indexTabMapperId 栏目映射表某id
     * @param timeRange        查询时间范围 （传值说明：24h,0d,3d 或 按时间查询 (2017-10-01 00:00:00;2017-10-20 00:00:00) ）
     * @param pageSize
     * @param pageNo
     * @param entityType       词云栏目类型（传值说明：通用：keywords；人物：people；地域：location；机构：agency）
     * @param request
     * @return list
     * @throws SearchException
     * @throws TRSException
     */
    @AppApi(value = "getChartData",method = ApiMethod.getChartData)
    @PostMapping("/getchartdata")
    public Object getChartData(@RequestParam(value = "accessToken") String accessToken,
                               @RequestParam(value = "indexTabMapperId") String indexTabMapperId,
                               @RequestParam(value = "timeRange", required = false) String timeRange,
                               @RequestParam(value = "pageSize",defaultValue = "15") int pageSize,
                               @RequestParam(value = "pageNo",defaultValue = "0") int pageNo,
                               @RequestParam(value = "entityType", defaultValue = "keywords") String entityType,
                                           HttpServletRequest request)
            throws SearchException, TRSException {
        log.error("getchartdata接口参数接收：accessToken:"+accessToken+",indexTabMapperId:"+indexTabMapperId);
        //防止前端乱输入
        pageSize = pageSize>=1?pageSize:15;
        IndexTabMapper tabMapper = indexTabMapperService.findOne(indexTabMapperId);
        if (ObjectUtil.isEmpty(tabMapper)){
            log.error("mapper查为空");
        }
        IndexTab indexTab = tabMapper.getIndexTab();
        //md5listinfo不可以 timeListInfo;listStatusCommon;listWeChatCommon
//        indexTab.setType("listStatusCommon;Twitter;FaceBook;listWeChatCommon;timeListInfo");
        if (ObjectUtil.isEmpty(indexTab)){
            log.error("indexTab为空");
        }
        String timeRan = indexTab.getTimeRange();
        if(StringUtil.isNotEmpty(timeRange)){
            timeRan = timeRange;
        }
        AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
        ColumnConfig config = new ColumnConfig();
        config.initSection(indexTab, timeRan, pageNo, pageSize ,"ALL","ALL", entityType,null,null,"default",
                 "","","","","", indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
                indexTab.getContentArea(), indexTab.getMediaArea(), "","");
        column.setCommonListService(commonListService);
        column.setCommonChartService(commonChartService);
        column.setDistrictInfoService(districtInfoService);
        column.setConfig(config);
        String userId = authService.findByAccessToken(accessToken).getGrantSourceOwnerId();
        log.error("userId为："+userId);
        InfoListResult infoListResult = null;
        if (indexTab.getType().contains("List")){
            log.info("查询方法走了：getAppSectionList");
            User user = userRepository.findOne(userId);
            infoListResult = (InfoListResult)column.getAppSectionList(user);
        }else if (indexTab.getType().contains("Chart")){
            log.info("柱状图-->查询方法走了：getColumnData");
            return column.getColumnData(timeRan);
        }else {
            log.info("列表信息-->查询方法走了：getSectionList");
            infoListResult = (InfoListResult)column.getSectionList();
        }
        if (infoListResult == null){
            return infoListResult;
        }
        infoListResult = isFav(userId,infoListResult);//判断数据是否收藏
        HashMap map = getImaUrl(infoListResult);//根据查询结果获取图片地址
        map.put("appDetail",detailsUrl(request));//获取app详情页面
        return map;
    }

    //获取app详情页面
    String detailsUrl(HttpServletRequest request){
        StringBuffer requestUrl = request.getRequestURL();
        String url = requestUrl.toString();
        int index = url.indexOf("/netInsight/");
        String mainUrl = url.substring(0,index);
        String Url = mainUrl+"/netInsight/app/details.html";
        return Url;
    }

    /**a'p
     * 显示和隐藏操作
     * @param accessToken
     * @param indexMapperIds 三级菜单id，多个id，使用；隔开
     * @param showOrHides 操作类型，使用；隔开。
     */
    @AppApi(value = "showOrHideOp",method = ApiMethod.showOrHideOp)
    @PostMapping("/showorhideop")
    public Object showOrHideOp(@RequestParam(value = "accessToken") String accessToken,
                               @RequestParam(value = "indexMapperIds") String indexMapperIds,
                               @RequestParam(value = "showOrHides") String showOrHides,
                               HttpServletRequest request) throws TRSException{
        //id用;分割
        indexTabMapperService.hide(indexMapperIds, showOrHides);
        return "success";
    }

    /**
     * 对三级菜单重新排序操作
     * @param accessToken
     * @param indexMapperIds 已排号序三级菜单id，多个id，使用；隔开
     */
    @AppApi(value = "sortColumnOp",method = ApiMethod.sortColumnOp)
    @PostMapping("/sortcolumnop")
    public Object sortColumnOp(@RequestParam(value = "accessToken") String accessToken,
                               @RequestParam(value = "indexMapperIds") String indexMapperIds,
                               HttpServletRequest request) throws TRSException{
        //id用;分割
        String[] split = indexMapperIds.split(";");
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            IndexTabMapper findOne = indexTabMapperService.findOne(s);
            if (ObjectUtil.isNotEmpty(findOne)) {
                findOne.setSequence(i + 1);
                indexTabMapperService.save(findOne);
            }
        }
        return "success";
    }

    /**
     * app收藏接口
     *
     */
    @AppApi(value = "addfavourites",method = ApiMethod.addfavourites)
    @PostMapping("/addfavourites")
    public Object addFavourites(@RequestParam(value = "accessToken") String accessToken,
                                @RequestParam(value = "sids") String sids,
                                @RequestParam(value = "urltime", required = false) String urltime,
                                @RequestParam(value = "md5tag", required = false) String md5tag,
                                @RequestParam(value = "groupName") String groupName,
                                HttpServletRequest request) throws OperationException {
        try {
            String[] split = groupName.split(";");
            for (int i = 0; i < split.length; i++) {
                if(StringUtil.isEmpty(split[i])){
                    return new OperationException("the field of groupName cannot be empty, please check carefully!");
                }
                if (split[i].equals("国内新闻_手机客户端")
                        || split[i].equals("国内新闻_电子报")) {
                    split[i] = split[i].substring(split[i].length() - 3,
                            split[i].length());
                }else if("境外媒体".equals(split[i])){
                    split[i] = "国外新闻";
                }
            }
            groupName = StringUtils.join(split, ";");
            AppApiAccessToken apiAccessToken = authService.findByAccessToken(accessToken);
            if (md5tag == null){
                md5tag = "";
            }
            String result = reportService.saveFavourites(sids, apiAccessToken.getGrantSourceOwnerId(),apiAccessToken.getSubGroupId(), md5tag,
                    groupName,urltime);
            return result;
        } catch (Exception e) {
            throw new OperationException("增加收藏失败,message" + e);
        }
    }
    /**
     * 取消收藏
     */
    @AppApi(value = "deladdfavourites",method = ApiMethod.deladdfavourites)
    @PostMapping("/deladdfavourites")
    public Object deleteFavourites(@RequestParam(value = "accessToken") String accessToken,
                                   @RequestParam(value = "sids") String sids,
                                   HttpServletRequest request) throws OperationException {
        try {
            AppApiAccessToken token = authService.findByAccessToken(accessToken);
            String userId = token.getGrantSourceOwnerId();
            return reportService.delFavourites(sids, userId);
        } catch (Exception e) {
            throw new OperationException("删除收藏失败,message" + e);
        }
    }
    @ApiOperation("修改token表")
    @RequestMapping(value = "/changeToken", method = RequestMethod.POST)
public Object changeAllToken() throws OperationException{
        return reportService.changeToken();

}
    /**
     * 获取收藏列表
     */
    @AppApi(value = "favouritesList",method = ApiMethod.favouritesList)
    @PostMapping("/favouritesList")
    public Object favouritesList(
            @RequestParam(value = "accessToken") String accessToken,
            @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "groupName", required = false) String groupName,
            HttpServletRequest request) throws TRSException {
            AppApiAccessToken token = authService.findByAccessToken(accessToken);
            String userId = token.getGrantSourceOwnerId();
            InfoListResult infoListResult = (InfoListResult)apiService.getAllFavourites(userId,token.getSubGroupId(),pageNo,pageSize);
            if (infoListResult == null){
                return infoListResult;
            }
            HashMap map = getImaUrl(infoListResult);
            map.put("appDetail",detailsUrl(request));
            map.put("totalPages",infoListResult.getTotalList());
            return map;
        }
    /**
     * 对三级菜单重新排序操作
     * @param accessToken
     * @param showIds 需要显示的id，以；隔开
     * @param hideIds 需要隐藏的id，以；隔开
     */
    @AppApi(value = "thirdColumnOp",method = ApiMethod.thirdColumnOp)
    @PostMapping("/thirdcolumnop")
    public Object thirdColumnOp(@RequestParam(value = "accessToken") String accessToken,
                               @RequestParam(value = "showIds") String showIds,
                                @RequestParam(value = "hideIds") String hideIds,
                               @RequestParam(value = "parentId") String parentId,
                               HttpServletRequest request) throws TRSException{
        return apiService.thirdColumnOp(showIds,hideIds,parentId);
    }

    /**
     * 点击单条文章进入详情页---传统媒体
     * @param sid
     * @param md5
     * @param trslk
     * @param nreserved1
     * @return
     * @throws TRSSearchException
     * @throws TRSException
     */
    @AppApi(value = "oneInfo",method = ApiMethod.oneInfo)
    @PostMapping("/oneInfo")
    public Object oneInfo(@RequestParam(value = "accessToken") String accessToken,
                          @RequestParam("sid") String sid,
                          @RequestParam(value = "md5", required = false) String md5,
                          @RequestParam(value = "trslk", required = false) String trslk,
                          @ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "nreserved1", required = false) String nreserved1,
                          HttpServletRequest request)
            throws TRSSearchException, TRSException {
        AppApiAccessToken token = authService.findByAccessToken(accessToken);
        String userId = token.getGrantSourceOwnerId();
        String trsl = RedisUtil.getString(trslk);
        fixedThreadPool.execute(() -> infoListService.simCount(sid, md5,null));
        QueryBuilder queryBuilder = new QueryBuilder();
        if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
            queryBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
        }
        queryBuilder.filterByTRSL(trsl);
        queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
        queryBuilder.page(0, 1);
        List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, false, false,false,null);
        if (null != ftsQuery && ftsQuery.size() > 0) {
            FtsDocument ftsDocument = ftsQuery.get(0);
            // 判断是否收藏
            //原生sql
            Specification<Favourites> criteriaFav = new Specification<Favourites>() {

                @Override
                public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Object> predicates = new ArrayList<>();
                    predicates.add(cb.equal(root.get("userId"),userId));
                    predicates.add(cb.isNull(root.get("libraryId")));
                    predicates.add(cb.equal(root.get("sid"), sid));
                    Predicate[] pre = new Predicate[predicates.size()];

                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            Favourites favourites = favouritesRepository.findOne(criteriaFav);
            if (ObjectUtil.isNotEmpty(favourites) && StringUtil.isEmpty(favourites.getLibraryId())) {
                ftsDocument.setFavourite(true);
            } else {
                ftsDocument.setFavourite(false);
            }
            // 判断是否预警
            List<String> sids = new ArrayList<>();
            sids.add(ftsDocument.getSid());
            List<AlertEntity> alert = null;

            ftsDocument.setTrslk(trslk);
            if ("百度贴吧".equals(ftsDocument.getSiteName())) {
                if (StringUtil.isNotEmpty(ftsDocument.getChannel()) && !ftsDocument.getChannel().endsWith("吧")) {
                    ftsDocument.setChannel(ftsDocument.getChannel() + "吧");
                }
            }
            if (null != nreserved1) {
                QueryBuilder builder = new QueryBuilder();
                HashMap<String, Object> returnMap = new HashMap<>();
                PagedList<FtsDocument> ftsDocuments = null;
                builder.filterField(FtsFieldConst.FIELD_HKEY, ftsDocument.getHKey(), Operator.Equal);
                builder.filterByTRSL(trsl);//不加trsl,导致前端直接取主贴标题，关键词不描红问题
                ftsDocuments = hybase8SearchService.ftsPageList(builder, FtsDocument.class, false, false,false,null);
                if ("1".equals(nreserved1)) {// 回帖
                    List list = new ArrayList<>();
                    if (null != ftsDocuments && ftsDocuments.size() > 0) {
                        for (FtsDocument document : ftsDocuments.getPageItems()) {
                            if ("0".equals(document.getNreserved1())) {// 说明这个是回帖对应的主贴
                                document.setReplyCount(ftsDocuments.getTotalItemCount() - 1);// 回帖个数
                                // 把主贴刨去
                                returnMap.put("mainCard", document);
                            }
                        }
                    }
                    list.add(ftsDocument);
                    returnMap.put("replyCard", list);
                    return returnMap;
                } else if ("0".equals(nreserved1)) {// 主贴
                    ftsDocument.setReplyCount(ftsDocuments.getTotalItemCount() - 1);// 回帖个数
                    // 把主贴刨去
                    returnMap.put("mainCard", ftsDocument);
                    return returnMap;
                }
            }

        }
        List all = new ArrayList<>();
        all.add(ftsQuery);
        System.err.println("方法结束" + System.currentTimeMillis());
        return all;
    }

    /**
     * 信息列表页单条的详情---微博
     * @throws TRSException
     * @throws TRSSearchException
     */
    @AppApi(value = "oneInfoStatus",method = ApiMethod.oneInfoStatus)
    @PostMapping("/oneInfoStatus")
    public Object oneInfoStatus(@RequestParam(value = "accessToken") String accessToken,
                                @ApiParam("文章sid") @RequestParam("" + "") String sid,
                                @ApiParam("md5值") @RequestParam(value = "md5", required = false) String md5,
                                @ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk,
                                HttpServletRequest request)
            throws TRSSearchException, TRSException {
        AppApiAccessToken token = authService.findByAccessToken(accessToken);
        String userId = token.getGrantSourceOwnerId();
        String trsl = RedisUtil.getString(trslk);
        fixedThreadPool.execute(() -> infoListService.simCountStatus(sid, md5,null));
        QueryBuilder queryBuilder = new QueryBuilder();
        if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
            queryBuilder.filterField(FtsFieldConst.FIELD_MID, sid, Operator.Equal);
        }
        queryBuilder.filterByTRSL(trsl);
        queryBuilder.page(0, 1);
        List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentStatus.class, false,
                false,false,null);
        // 查预警
        if (ObjectUtil.isNotEmpty(ftsQuery)) {
            List<String> sids = new ArrayList<>();
            sids.add(ftsQuery.get(0).getMid());
            //判断是否收藏
            //原生sql
            Specification<Favourites> criteria = new Specification<Favourites>() {
                @Override
                public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Object> predicates = new ArrayList<>();
                    predicates.add(cb.equal(root.get("userId"),userId));
                    predicates.add(cb.isNull(root.get("libraryId")));
                    predicates.add(cb.equal(root.get("sid"), sid));
                    Predicate[] pre = new Predicate[predicates.size()];

                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            Favourites favourites = favouritesRepository.findOne(criteria);
            if (ObjectUtil.isNotEmpty(favourites) && StringUtil.isEmpty(favourites.getLibraryId())) {
                ftsQuery.get(0).setFavourite(true);
            } else {
                ftsQuery.get(0).setFavourite(false);
            }
        }
        List all = new ArrayList<>();
        all.add(ftsQuery);
        return all;
    }


    /**
     * 信息列表页单条的详情---微信
     * @throws TRSException
     * @throws TRSSearchException
     */
    @AppApi(value = "oneInfoWeChat",method = ApiMethod.oneInfoWeChat)
    @PostMapping("/oneInfoWeChat")
    public Object oneInfoWeChat(@RequestParam(value = "accessToken") String accessToken,
                                @ApiParam("文章sid") @RequestParam("sid") String sid,
                                @ApiParam("md5值") @RequestParam(value = "md5", required = false) String md5,
                                @ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk,
                                HttpServletRequest request)
            throws TRSSearchException, TRSException {
        String userId = authService.findByAccessToken(accessToken).getGrantSourceOwnerId();
        String trsl = RedisUtil.getString(trslk);
        fixedThreadPool.execute(() -> infoListService.simCountWeChat(sid, md5,null));
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.filterByTRSL(trsl);
        if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
            queryBuilder.filterField(FtsFieldConst.FIELD_HKEY, sid, Operator.Equal);
        }
        queryBuilder.page(0, 1);
        List<FtsDocumentWeChat> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentWeChat.class, false,
                false,false,null);
        // 查预警
        if (ObjectUtil.isNotEmpty(ftsQuery)) {
            List<String> sids = new ArrayList<>();
            sids.add(ftsQuery.get(0).getHkey());
            //判断是否收藏
            //原生sql
            Specification<Favourites> criteria = new Specification<Favourites>() {

                @Override
                public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Object> predicates = new ArrayList<>();
                    predicates.add(cb.equal(root.get("userId"),userId));
                    predicates.add(cb.isNull(root.get("libraryId")));
                    predicates.add(cb.equal(root.get("sid"), sid));
                    Predicate[] pre = new Predicate[predicates.size()];

                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            Favourites favourites = favouritesRepository.findOne(criteria);
            if (ObjectUtil.isNotEmpty(favourites) && StringUtil.isEmpty(favourites.getLibraryId())) {
                ftsQuery.get(0).setFavourite(true);
            } else {
                ftsQuery.get(0).setFavourite(false);
            }
        }
        List all = new ArrayList<>();
        all.add(ftsQuery);
        return all;
    }

    /**
     * 推特及脸书
     * @param sid
     * @param md5
     * @param trslk
     * @return
     * @throws TRSSearchException
     * @throws TRSException
     */
    @AppApi(value = "oneInfoTF",method = ApiMethod.oneInfoTF)
    @PostMapping("/oneInfoTF")
    public Object oneInfoTF(@RequestParam(value = "accessToken") String accessToken,
                            @ApiParam("文章sid") @RequestParam("sid") String sid,
                            @ApiParam("md5值") @RequestParam(value = "md5", required = false) String md5,
                            @ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk,
                            HttpServletRequest request)
            throws TRSSearchException, TRSException {
        String userId = authService.findByAccessToken(accessToken).getGrantSourceOwnerId();
        String trsl = RedisUtil.getString(trslk);
        QueryBuilder queryBuilder = new QueryBuilder();
        if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
            queryBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
        }
        queryBuilder.filterByTRSL(trsl);
        queryBuilder.page(0, 1);
        List<FtsDocumentTF> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentTF.class, false, false,false,null);
        // 查预警
        if (ObjectUtil.isNotEmpty(ftsQuery)) {
            List<String> sids = new ArrayList<>();
            sids.add(ftsQuery.get(0).getSid());
            //判断是否收藏
            //原生sql
            Specification<Favourites> criteria = new Specification<Favourites>() {
                @Override
                public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Object> predicates = new ArrayList<>();
                    predicates.add(cb.equal(root.get("userId"),userId));
                    predicates.add(cb.isNull(root.get("libraryId")));
                    predicates.add(cb.equal(root.get("sid"), sid));
                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };

            Favourites favourites = favouritesRepository.findOne(criteria);
            //Favourites favourite = favouritesRepository.findByUserIdAndSid(userId, sid);
            if (ObjectUtil.isNotEmpty(favourites) && StringUtil.isEmpty(favourites.getLibraryId())) {
                ftsQuery.get(0).setFavourite(true);
            } else {
                ftsQuery.get(0).setFavourite(false);
            }
        }
        List all = new ArrayList<>();
        all.add(ftsQuery);
        return all;
    }
    /**
     * 获取token
     * @param request
     * @return
     * @Return : AppApiAccessToken
     */
    private AppApiAccessToken getToken(HttpServletRequest request) {
        return (AppApiAccessToken) request.getAttribute("token");
    }

    /**
     * 根据查询结果获取图片地址
     */
    private HashMap getImaUrl(InfoListResult infoListResult){
        PagedList list = (PagedList) infoListResult.getContent();
        ArrayList items = (ArrayList)list.getPageItems();
        ArrayList listImaUrl = new ArrayList();
        HashMap hashMap = new HashMap();
        if (items.size()>0){
            for (int index = 0; index < items.size();index++) {
                FtsDocumentCommonVO commonVO = (FtsDocumentCommonVO)items.get(index);
                String content = commonVO.getContent();
                String expertContent = commonVO.getExportContent();
                String[] imaUrls = null;
                String[] expertIma = null;
                if (content != null && expertContent != null){
                    imaUrls = content.split("IMAGE&nbsp;SRC=&quot;");
                    expertIma = expertContent.split("IMAGE&nbsp;SRC=&quot;");
                    if (imaUrls.length>1){
                        String imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
                        listImaUrl.add(imaUrl);
                    }else if (expertIma.length>1){
                        String imaUrl = expertIma[1].substring(0,expertIma[1].indexOf("&quot;"));
                        listImaUrl.add(imaUrl);
                    }else {
                        listImaUrl.add("");
                    }
                }
            }
        }
        hashMap.put("listImaUrl",listImaUrl);
        hashMap.put("infoListResult",infoListResult);
        //hashMap.put("appDetail","http://119.254.92.55:8089/netInsight/app/details.html");
        return hashMap;
    }

    /**
     * 判断数据是否收藏
     */
    private InfoListResult isFav(String userId,InfoListResult infoListResult){
        //原生sql
        Specification<Favourites> criteria = new Specification<Favourites>() {
            @Override
            public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("userId"),userId));
                predicates.add(cb.isNull(root.get("libraryId")));
                Predicate[] pre = new Predicate[predicates.size()];
                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };
        List<Favourites> favouritesList = favouritesRepository.findAll(criteria, new Sort(Sort.Direction.DESC, "createdTime"));
        List<String> sidFavourites = new ArrayList<>();
        for (Favourites favourites : favouritesList) {
            sidFavourites.add(favourites.getSid());
        }
        PagedList list = (PagedList) infoListResult.getContent();
        ArrayList items = (ArrayList)list.getPageItems();
        for (int i=0;i<items.size();i++){
            FtsDocumentCommonVO commonVO = (FtsDocumentCommonVO)items.get(i);
            if (sidFavourites.indexOf(commonVO.getSid())<0){
                commonVO.setFavourite(false);
            }else{
                commonVO.setFavourite(true);
            }
        }
        return infoListResult;
    }
    /**
     * app 查询预警列表
     * @param accessToken
     * @param alertSource
     * @param pageNo
     * @param pageSize
     * @return
     */
    @AppApi(value = "selectAppAlert",method = ApiMethod.selectAppAlert)
    @ApiOperation("查询预警列表 app")
    @GetMapping(value = "/selectAppAlert")
    public Object selectAppAlert(@ApiParam("token值") @RequestParam(value = "accessToken", required = true) String accessToken,
                                 @ApiParam("预警类型 自动 AUTO | 手动 ARTIFICIAL | 全部 不传值") @RequestParam("sendType") String alertSource,
                                 @ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
                                 @ApiParam("页长") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                 HttpServletRequest request) throws Exception {
        //防止前端乱输入
        pageSize = pageSize>=1?pageSize:10;
         String userId = authService.findByAccessToken(accessToken).getGrantSourceOwnerId();
        PageAlertSend pageAlertSend = new PageAlertSend();
        if (StringUtil.isNotEmpty(alertSource)){
            //AlertSource value = AlertSource.valueOf(alertSource);
            pageAlertSend = apiService.getAppAlertData(userId,alertSource,pageNo,pageSize);

        }else {
            pageAlertSend = apiService.getAppAlertData(userId,null,pageNo,pageSize);
        }
        if (null != pageAlertSend){
            pageAlertSend.setAppDetail(detailsUrl(request));
            return pageAlertSend;
        }
        return null;
    }
    /**
     * app 查询单条预警信息
     * @param accessToken
     * @param alertId
     * @return
     */
    @AppApi(value = "selectOneAppAlert",method = ApiMethod.selectOneAppAlert)
    @ApiOperation("查询单条预警信息 app")
    @GetMapping(value = "/selectOneAppAlert")
    public Object selectOneAppAlert(@ApiParam("token值") @RequestParam(value = "accessToken", required = true) String accessToken,
                                 @ApiParam("预警id") @RequestParam(value = "alertId",required = true) String alertId,
                                 HttpServletRequest request) throws Exception {
        String userId = authService.findByAccessToken(accessToken).getGrantSourceOwnerId();

        if (StringUtil.isNotEmpty(alertId)){
            PageAlertSend oneAlertData = apiService.getOneAlertData(alertId, userId);
            oneAlertData.setAppDetail(detailsUrl(request));
            return apiService.getOneAlertData(alertId,userId);
        }
        return null;
    }


//    @Value("${pdfpathVirtualPath}")
    @Value("${AppPageTitle}")
    private String AppPageTitle;
    @Value("${AppPageDisplay}")
    private String AppPageDisplay;
    /**
     * http://localhost:28088/netInsight/app/api/selectYiQing
     * http://localhost:28088/thinkTank/png/1.png
     */
    @ApiOperation("查询网察app疫情信息 app")
    @FormatResult
    @GetMapping(value = "/selectYiQing")
    public Object selectYiQing(@ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
                               @ApiParam("页长") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        //防止前端乱输入
//        thinkTankDataService.findByPdfNameNot(pageNo,pageSize,"");
        List<ThinkTankData> list = thinkTankDataService.findByPicDetailNameNotAndPicDetailNameIsNotNull(pageNo,pageSize,"");
        for(ThinkTankData l: list){
            if(l.getPicDetailName()!=null && l.getPicDetailName().length()>0)  l.setPicDetailName("thinkTank/png/"+l.getPicDetailName());
            else l.setPicDetailName("thinkTank/png/"+l.getPdfToPngName());
            l.setPictureName("thinkTank/picture/"+l.getPictureName());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("AppPageTitle", EncodeString.decodeString(AppPageTitle));
        jsonObject.put("AppPageDisplay",AppPageDisplay);
        jsonObject.put("list",list);
        return jsonObject;
    }


}
