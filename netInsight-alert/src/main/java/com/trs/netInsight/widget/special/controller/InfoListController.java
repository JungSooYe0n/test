package com.trs.netInsight.widget.special.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.*;
import com.trs.netInsight.util.alert.AlertUtil;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.analysis.entity.ClassInfo;
import com.trs.netInsight.widget.analysis.service.IChartAnalyzeService;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.microblog.constant.MicroblogConst;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.microblog.repository.SingleMicroblogDataRepository;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogDataService;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogService;
import com.trs.netInsight.widget.microblog.task.CoreForwardWeiboTask;
import com.trs.netInsight.widget.microblog.task.HotReviewsTask;
import com.trs.netInsight.widget.microblog.task.HotReviewsWeiboTask;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.entity.JunkData;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.enums.SearchPage;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.special.service.IJunkDataService;
import com.trs.netInsight.widget.special.service.ISearchRecordService;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 信息列表页controller
 * <p>
 * Created by ChangXiaoyang on 2017/5/5.
 */
@RestController
@RequestMapping("/list")
@Api(description = "专项检测信息列表接口")
@Slf4j
public class InfoListController {

	@Autowired
	private IJunkDataService junkDataService;

	@Autowired
	private IInfoListService infoListService;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private ISpecialProjectService specialProjectService;

	@Autowired
	private AlertRepository alertRepository;

	@Autowired
	private FavouritesRepository favouritesRepository;

	@Autowired
	private IChartAnalyzeService chartAnalyzeService;
	
	@Autowired
	private ISearchRecordService searchRecordService;

	@Autowired
	private ICommonListService commonListService;
	@Autowired
	private SingleMicroblogDataRepository singleMicroblogDataRepository;
	/**
	 * 是否走独立预警服务
	 */
	@Value("${http.client}")
	private boolean httpClient;
	/**
	 * 独立预警服务地址
	 */
	@Value("${http.alert.netinsight.url}")
	private String alertNetinsightUrl;

	// @Autowired
	// private LogPrintUtil loginpool;
	/**
	 * 线程池跑任务
	 */
	private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

	/**
	 * 线程池跑任务
	 */
	private static ExecutorService realInfoThreadPool = Executors.newFixedThreadPool(10);
	/**
	 * 线程池跑任务
	 */
	private static ExecutorService weiboDetailThreadPool = Executors.newFixedThreadPool(10);


	// @Autowired
	// private LogPrintUtil loginpool;
	/**
	 * 线程池跑任务
	 */
	private static ExecutorService nowInfoThreadPool = Executors.newFixedThreadPool(10);

	/**
	 * 专项检测信息列表
	 *
	 * @param specialId
	 *            专项ID
	 * @param source
	 *            来源
	 *            行业类型
	 * @param emotion
	 *            情感
	 * @param sort
	 *            排序方式
	 * @param keywords
	 *            在结果查询
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_LIST, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "查询专题数据列表：${specialId}")
	@FormatResult
	@ApiOperation("专项检测信息列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialId", value = "专项ID", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "pageNo", value = "页码", dataType = "int", paramType = "query", required = false),
			@ApiImplicitParam(name = "pageSize", value = "步长", dataType = "int", paramType = "query", required = false),
			@ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "time", value = "时间", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "area", value = "内容地域", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "industry", value = "行业类型", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "emotion", value = "情感", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "sort", value = " 排序方式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keywords", value = " 在结果查询", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "fuzzyValueScope", value = "结果中搜索de范围", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "foreign", value = "欧洲  美国", dataType = "String", paramType = "query", required = false)

	})
	@RequestMapping(value = "/info", method = RequestMethod.POST)
	public Object dataList(@RequestParam(value = "specialId") String specialId,
			@RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
			@RequestParam(value = "source", defaultValue = "ALL", required = false) String source,
			@RequestParam(value = "sort", defaultValue = "", required = false) String sort,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@ApiParam("结果中搜索")@RequestParam(value = "keywords", required = false) String keywords,
			@ApiParam("结果中搜索的范围")@RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope,

						   @ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
						   @ApiParam("排重规则  -  替换栏目条件") @RequestParam(value = "simflag", required = false) String simflag,
						   @ApiParam("关键词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "wordIndex", required = false) String wordIndex,
						   @ApiParam("情感倾向") @RequestParam(value = "emotion", required = false) String emotion,
						   @ApiParam("阅读标记") @RequestParam(value = "read", required = false) String read,
						   @ApiParam("排除网站  替换栏目条件") @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
						   @ApiParam("监测网站  替换栏目条件") @RequestParam(value = "monitorSite", required = false) String monitorSite,
						   @ApiParam("排除关键词  替换栏目条件") @RequestParam(value = "excludeWord", required = false) String excludeWord,
						   @ApiParam("排除词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "excludeWordIndex",defaultValue ="1",required = false) String excludeWordIndex,
						   @ApiParam("修改词距标记 替换栏目条件") @RequestParam(value = "updateWordForm",defaultValue = "false",required = false) Boolean updateWordForm,
						   @ApiParam("词距间隔字符 替换栏目条件") @RequestParam(value = "wordFromNum", required = false) Integer wordFromNum,
						   @ApiParam("词距是否排序  替换栏目条件") @RequestParam(value = "wordFromSort", required = false) Boolean wordFromSort,
						   @ApiParam("媒体等级") @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
						   @ApiParam("数据源  替换栏目条件") @RequestParam(value = "groupName", required = false) String groupName,
						   @ApiParam("媒体行业") @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
						   @ApiParam("内容行业") @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
						   @ApiParam("信息过滤") @RequestParam(value = "filterInfo", required = false) String filterInfo,
						   @ApiParam("信息地域") @RequestParam(value = "contentArea", required = false) String contentArea,
						   @ApiParam("媒体地域") @RequestParam(value = "mediaArea", required = false) String mediaArea,
						   @ApiParam("精准筛选") @RequestParam(value = "preciseFilter", required = false) String preciseFilter) throws TRSException {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		log.warn("专项检测信息列表  开始调用接口"+DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
		String userName = UserUtils.getUser().getUserName();
		long startTime = System.currentTimeMillis();

		try {
			SpecialProject specialProject = specialProjectService.findOne(specialId);
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			//排重
			if ("netRemove".equals(simflag)) { //单一媒体排重
				specialProject.setSimilar(true);
				specialProject.setIrSimflag(false);
				specialProject.setIrSimflagAll(false);
			} else if ("urlRemove".equals(simflag)) { //站内排重
				specialProject.setSimilar(false);
				specialProject.setIrSimflag(true);
				specialProject.setIrSimflagAll(false);
			} else if ("sourceRemove".equals(simflag)) { //全网排重
				specialProject.setSimilar(false);
				specialProject.setIrSimflag(false);
				specialProject.setIrSimflagAll(true);
			}
			//命中规则
			if (StringUtil.isNotEmpty(wordIndex) && StringUtil.isEmpty(specialProject.getTrsl())) {
//				specialProject.setKeyWordIndex(wordIndex);
				if (SearchScope.TITLE.equals(wordIndex)){
					specialProject.setSearchScope(SearchScope.TITLE);
				}
				if (SearchScope.TITLE_CONTENT.equals(wordIndex)){
					specialProject.setSearchScope(SearchScope.TITLE_CONTENT);
				}
				if (SearchScope.TITLE_ABSTRACT.equals(wordIndex)){
					specialProject.setSearchScope(SearchScope.TITLE_ABSTRACT);
				}

			}
			specialProject.setMonitorSite(monitorSite);
			specialProject.setExcludeWeb(excludeWeb);
			//排除关键词
			specialProject.setExcludeWordIndex(excludeWordIndex);
			specialProject.setExcludeWords(excludeWord);

			//修改词距 选择修改词距时，才能修改词距
			if (updateWordForm != null && updateWordForm && StringUtil.isEmpty(specialProject.getTrsl()) && wordFromNum >= 0) {
				String keywordJson = specialProject.getAnyKeywords();
				JSONArray jsonArray = JSONArray.parseArray(keywordJson);
				//现在词距修改情况为：只有一个关键词组时，可以修改词距等，多个时不允许
				if (jsonArray != null && jsonArray.size() == 1) {
					Object o = jsonArray.get(0);
					JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
					jsonObject.put("wordSpace", wordFromNum);
					jsonObject.put("wordOrder", wordFromSort);
					jsonArray.set(0, jsonObject);
					specialProject.setAnyKeywords(jsonArray.toJSONString());
				}
			}
			// 跟统计表格一样 如果来源没选 就不查数据
			List<String> specialSource = CommonListChartUtil.formatGroupName(specialProject.getSource());
			if(!"ALL".equals(source)){
				source = CommonListChartUtil.changeGroupName(source);
				if(!specialSource.contains(source)){
					return null;
				}
			}else{
				source = StringUtils.join(specialSource,";");
			}
//			String keyWordIndex = "positioCon";// 标题加正文 与日常监测统一

				Object documentCommonSearch = infoListService.documentCommonSearch(specialProject, pageNo, pageSize, source,
						timeRange, emotion, sort, invitationCard,forwarPrimary, keywords, fuzzyValueScope,null,
						"special", read, mediaLevel, mediaIndustry,
						contentIndustry, filterInfo, contentArea, mediaArea, preciseFilter);
				long endTime = System.currentTimeMillis();
				log.warn("间隔时间："+(endTime - startTime));
				return documentCommonSearch;


		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e,e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.INFO_LIST);
			}
			log.info("调用接口用了" + timeApi + "ms"+DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
		}
	}

	/**
	 * 普通搜索
	 * 
	 * @date Created at 2018年7月19日 下午2:02:41
	 * @Author 谷泽昊
	 * @param keywords
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@ApiOperation("普通搜索")
	@Log(systemLogOperation = SystemLogOperation.ORDINARY_SEARCH, systemLogType = SystemLogType.SEARCH, systemLogOperationPosition = "普通搜索：@{keywords}")
	@RequestMapping(value = "/ordinarySearch", method = RequestMethod.GET)
	public Object ordinarySearch(
			@ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@ApiParam("步长") @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
			@ApiParam("关键词") @RequestParam(value = "keywords", required = false) String keywords,
			@ApiParam("来源") @RequestParam(value = "source", defaultValue = "ALL") String source,
			@ApiParam("排序") @RequestParam(value = "sort", defaultValue = "default", required = false) String sort,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("情感") @RequestParam(value = "emotion", defaultValue = "ALL", required = false) String emotion,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@ApiParam("在结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
			@ApiParam("在结果中搜索de范围") @RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope,
			@ApiParam("查询类型：精准precise、模糊fuzzy") @RequestParam(value = "searchType",defaultValue = "fuzzy",required = false) String searchType,
			@ApiParam("是否导出 主要是普通搜索全部数据源时使用") @RequestParam(value = "isExport", defaultValue = "false", required = false) Boolean isExport)
			throws TRSException {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		if (StringUtils.isBlank(keywords)) {
			throw new TRSException("关键词不能为空！", CodeUtils.FAIL);
		}
		//当pageNo时才记录   每次翻页关键词都是一样的就 不记录了
		if(pageNo == 0){
			searchRecordService.createRecord(keywords);
		}
		String time = "7d";
		keywords = keywords.trim();
		//keywords = keywords.replaceAll("\\s+", "\" AND \"");
		//keywords = keywords.replaceAll("\\s+", ",");
		String searchPage = "ordinarySearch";
		//20200324改，普通搜索列表页采用全网排重，统计用站内排重
		Boolean sim = false;
		Boolean irSimflag = false;
		Boolean irSimflagAll = true;
		if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
			return infoListService.weChatForSearchList(sim,  irSimflag,irSimflagAll,pageNo, pageSize, source, "",time, "ALL", "ALL",
					emotion, sort, keywords, "", "positioCon", true, fuzzyValue,fuzzyValueScope,null,null,null,searchPage,searchType);
		} else if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
			return infoListService.statusForSearchList(sim,  irSimflag,irSimflagAll,pageNo, pageSize, source, "",time, "ALL", "ALL",
					emotion, sort, keywords, "", "positioCon", forwarPrimary, "",true, fuzzyValue,fuzzyValueScope,null,null,null,searchPage,searchType);
		} else if (Const.MEDIA_TYPE_FINAL_NEWS.contains(source)) {
			//return infoListService.documentForOrdinarySearch(false,true,pageNo,pageSize,source,"7d",emotion,sort,invitationCard,keywords,"positioCon",true,fuzzyValue);
			return infoListService.documentForSearchList(sim,  irSimflag,irSimflagAll, pageNo, pageSize, source, "",time, "ALL", "ALL",
					emotion, sort, invitationCard, "",keywords, "", "positioCon", true, fuzzyValue,fuzzyValueScope,null,null,null,null,null,null,searchPage,searchType);
		}else if (Const.MEDIA_TYPE_TF.contains(source)) {
			return infoListService.documentTFForSearchList(sim,  irSimflag,irSimflagAll, pageNo, pageSize, source, "",time, "ALL", "ALL",
					emotion, sort, keywords, "", "positioCon", true, fuzzyValue,fuzzyValueScope,null,null,null,null,null,null,searchPage,searchType);
		}
		else{
			//AdvancedSearch
			//普通搜索全部情况下的原发转发筛选就是在结果中筛选，相当于高级搜索中的二级筛选
			return infoListService.documentCommonVOForSearchList(sim,  irSimflag,irSimflagAll, pageNo, pageSize, source, time, "ALL", "ALL",
					emotion, sort,  "",invitationCard, "",forwarPrimary,keywords, "", "positioCon", true, fuzzyValue,fuzzyValueScope,
					null,null,null,null,null,isExport,null,UserUtils.getUser().getId()+"OrdinarySearch",searchPage,searchType);
		}
	}
	
	@FormatResult
	@ApiOperation("搜索历史记录")
	@RequestMapping(value = "/searchRecordList", method = RequestMethod.GET)
	public Object recordList(@ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@ApiParam("步长") @RequestParam(value = "pageSize", defaultValue = "5", required = false) int pageSize){
		String userId = UserUtils.getUser().getId();
		//全都返回给前端  前端自己模糊匹配
		return searchRecordService.findByUserId(userId);
	}
	@FormatResult
	@RequestMapping(value = "/searchStattotal", method = RequestMethod.GET)
	public Object searchStattotal(
							   @RequestParam(value = "source", defaultValue = "ALL") String source,
							   @RequestParam(value = "time", required = false, defaultValue = "7d") String time,
							   @RequestParam(value = "emotion", defaultValue = "ALL", required = false) String emotion,
							   @RequestParam(value = "sort", defaultValue = "default", required = false) String sort,
							   @ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
							   @ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwardPrimary", required = false) String forwardPrimary,
							   @RequestParam(value = "area", defaultValue = "ALL", required = false) String area,
							   //@RequestParam(value = "industry", defaultValue = "ALL", required = false) String industry,
							   @RequestParam(value = "notKeyWords", required = false) String notKeyWords,
							   @RequestParam(value = "fromWebSite", required = false) String fromWebSite,
							   @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
							   @RequestParam(value = "keyWordIndex", defaultValue = "positioCon", required = false) String keyWordIndex,
							   @RequestParam(value = "simflag", required = false) String simflag,
							   @RequestParam(value = "keywords", required = false) String keywords,
							   @RequestParam(value = "weight", defaultValue = "false", required = false) boolean weight,
							   @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
							   @RequestParam(value = "newsInformation", required = false) String newsInformation,
							   @RequestParam(value = "reprintPortal", required = false) String reprintPortal,
							   @RequestParam(value = "siteType", required = false) String siteType,
							   @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
							   @RequestParam(value = "isExport", required = false) boolean isExport
	) throws TRSException {
		log.warn("图表分析统计表格stattotal接口开始调用");
		long start = new Date().getTime();
		try {
			if(source.contains("境外媒体")){
				source = source.replaceAll("境外媒体","国外新闻");
			}
			// 默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			boolean irSimflagAll = false;
			if ("netRemove".equals(simflag)) {
				isSimilar = true;
			} else if ("urlRemove".equals(simflag)) {
				irSimflag = true;
			}else if ("sourceRemove".equals(simflag)){
				irSimflagAll = true;
			}

			List<ClassInfo> total = infoListService.searchstattotal(isSimilar, irSimflag,irSimflagAll, 0, 20, source, time, area,
					mediaIndustry, emotion, sort, invitationCard, forwardPrimary,keywords, notKeyWords, keyWordIndex, weight,
					fuzzyValue,fromWebSite,excludeWeb,newsInformation,reprintPortal,siteType,isExport,"advance");
			Map<String, Object> putValue = MapUtil.putValue(new String[] { "总量" }, total);
			return putValue;
		} catch (Exception e) {
			throw new OperationException("统计表格错误,message: " + e, e);
		}
	}
	//@EnableRedis
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.SEARCH_LIST, systemLogType = SystemLogType.SEARCH, systemLogOperationPosition = "高级搜索")
	@ApiOperation("高级搜索列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageNo", value = "页码", dataType = "int", paramType = "query", required = false),
			@ApiImplicitParam(name = "pageSize", value = "步长", dataType = "int", paramType = "query", required = false),
			@ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "time", value = "时间", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "area", value = "内容地域", dataType = "String", paramType = "query", required = false),
			//@ApiImplicitParam(name = "industry", value = "行业类型", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "notKeyWords", value = "排除词", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "fromWebSite", value = "来源网站", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keyWordIndex", value = "关键词位置", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排,url排,跨数据源排", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keywords", value = "关键词", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weight", value = "标题权重", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "fuzzyValueScope", value = "结果中搜索de范围", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "fuzzyValue", value = "结果中搜索", dataType = "String", paramType = "query", required = false) ,
			@ApiImplicitParam(name = "newsInformation", value = "新闻信息资质", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "reprintPortal", value = "可供转载网站/门户类型", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "siteType", value = "网站类型", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "mediaIndustry", value = "媒体行业", dataType = "String", paramType = "query", required = false)})
	@RequestMapping(value = "/searchList", method = RequestMethod.GET)
	public Object searchList(@RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
			@RequestParam(value = "source", defaultValue = "ALL") String source,
			@RequestParam(value = "checkedSource", defaultValue = "ALL") String checkedSource,
			@RequestParam(value = "time", required = false, defaultValue = "7d") String time,
			@RequestParam(value = "emotion", defaultValue = "ALL", required = false) String emotion,
			@RequestParam(value = "sort", defaultValue = "default", required = false) String sort,
			@ApiParam("论坛主贴 0 /回帖 1 所有=主贴+回帖+转帖(嘿嘿)") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwardPrimary", required = false) String forwardPrimary,
			@ApiParam("二级筛选论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard1", required = false) String invitationCard1,
			@ApiParam("二级筛选微博 原发 primary / 转发 forward ") @RequestParam(value = "forwardPrimary1", required = false) String forwardPrimary1,
			@RequestParam(value = "area", defaultValue = "ALL", required = false) String area,
			//@RequestParam(value = "industry", defaultValue = "ALL", required = false) String industry,
			@RequestParam(value = "notKeyWords", required = false) String notKeyWords,
			@RequestParam(value = "fromWebSite", required = false) String fromWebSite,
			@RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@RequestParam(value = "keyWordIndex", defaultValue = "positioCon", required = false) String keyWordIndex,
			@RequestParam(value = "simflag", required = false) String simflag,
			@RequestParam(value = "keywords", required = false) String keywords,
			@RequestParam(value = "weight", defaultValue = "false", required = false) boolean weight,
			@RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
			@RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope,
			@RequestParam(value = "newsInformation", required = false) String newsInformation,
			@RequestParam(value = "reprintPortal", required = false) String reprintPortal,
			@RequestParam(value = "siteType", required = false) String siteType,
			@RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
							 @RequestParam(value = "isExport", required = false) boolean isExport
							 ) throws TRSException {
		log.warn("专项检测信息列表  开始调用接口");
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		try {
			// 默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			boolean irSimflagAll = false;
			if ("netRemove".equals(simflag)) {
				isSimilar = true;
			} else if ("urlRemove".equals(simflag)) {
				irSimflag = true;
			}else if ("sourceRemove".equals(simflag)){
				irSimflagAll = true;
			}
			String searchPage = "advancedSearch";
			String searchType = "precise";
			if (Const.MEDIA_TYPE_WEIXIN.contains(source) && !(source.contains(";") || source.contains("；"))) {
				return infoListService.weChatForSearchList(isSimilar, irSimflag,irSimflagAll, pageNo, pageSize, source, checkedSource,time, area,
						mediaIndustry, emotion, sort, keywords, notKeyWords, keyWordIndex, weight, fuzzyValue,fuzzyValueScope,fromWebSite,excludeWeb,"advance",searchPage,searchType);
			} else if (Const.MEDIA_TYPE_WEIBO.contains(source) && !(source.contains(";") || source.contains("；"))) {
				return infoListService.statusForSearchList(isSimilar, irSimflag,irSimflagAll, pageNo, pageSize, source,checkedSource, time, area,
						mediaIndustry, emotion, sort, keywords, notKeyWords, keyWordIndex, forwardPrimary,forwardPrimary1, weight,
						fuzzyValue,fuzzyValueScope,fromWebSite,excludeWeb,"advance",searchPage,searchType);
			} else if (Const.MEDIA_TYPE_FINAL_NEWS.contains(source) && !(source.contains(";") || source.contains("；"))) {
				return infoListService.documentForSearchList(isSimilar, irSimflag,irSimflagAll, pageNo, pageSize, source, checkedSource,time, area,
						mediaIndustry, emotion, sort, invitationCard,invitationCard1, keywords, notKeyWords, keyWordIndex, weight,
						fuzzyValue,fuzzyValueScope,fromWebSite,excludeWeb,newsInformation,reprintPortal,siteType,"advance",searchPage,searchType);
			}else if (Const.MEDIA_TYPE_TF.contains(source) && !(source.contains(";") || source.contains("；"))) {
				return infoListService.documentTFForSearchList(isSimilar, irSimflag,irSimflagAll, pageNo, pageSize, source, checkedSource,time, area,
						mediaIndustry, emotion, sort,  keywords, notKeyWords, keyWordIndex, weight,
						fuzzyValue,fuzzyValueScope,fromWebSite,excludeWeb,newsInformation,reprintPortal,siteType,"advance",searchPage,searchType);
			}else {
				return infoListService.documentCommonVOForSearchList(isSimilar, irSimflag,irSimflagAll, pageNo, pageSize, source, time, area,
						mediaIndustry, emotion, sort, invitationCard,invitationCard1,forwardPrimary,forwardPrimary1, keywords, notKeyWords, keyWordIndex, weight,
						fuzzyValue,fuzzyValueScope,fromWebSite,excludeWeb,newsInformation,reprintPortal,siteType,isExport,"advance",UserUtils.getUser().getId()+"AdvancedSearch",searchPage,searchType);
			}
			//return null;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		}
	}
	private String removeSimflag(String trslk) {
		if(trslk.indexOf("AND SIMFLAG:(1000 OR \"\")") != -1){
			trslk = trslk.replace(" AND SIMFLAG:(1000 OR \"\")","");
		}else if (trslk.indexOf("AND (IR_SIMFLAGALL:(\"0\" OR \"\"))") != -1){
			trslk = trslk.replace(" AND (IR_SIMFLAGALL:(\"0\" OR \"\"))","");
		}
		trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
		return trslk;
	}
	//@EnableRedis 这块不能加缓存，页面删除会失效  20191107
	@FormatResult                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
	@ApiOperation("相似文章列表 做统一列表  返回格式和日常监测跳的列表页一样")
	@RequestMapping(value = "/listsim", method = RequestMethod.GET)
	public Object simList(@ApiParam("当前搜索页面，普通搜索、其他") @RequestParam(value = "searchPage", required = false,defaultValue = "COMMON_SEARCH") String searchPage,
			@ApiParam("来源") @RequestParam("source") String source,
			@ApiParam("md5标示") @RequestParam(value = "md5Tag", required = false) String md5Tag,
			@ApiParam("推荐文章排除自己") @RequestParam(value = "id", required = false) String id,
			@ApiParam("表达式") @RequestParam("trslk") String trslk,
			@ApiParam("排序方式") @RequestParam(value = "sort", defaultValue = "default") String sort,
			@ApiParam("情感") @RequestParam(value = "emotion", defaultValue = "ALL") String emotion,
			@ApiParam("结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
			@ApiParam("在结果中搜索de范围") @RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope,
			@ApiParam("针对论坛  主贴 0/回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@ApiParam("页数") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@ApiParam("一页多少条") @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize)
			throws TRSException {
		//Twitter和Facebook没有MD5TAG标，所以不计算相似文章数
		String typeSim = "detail";//默认时间一年，用到的情况：①trslk失效；②trlk对应的trsl表达式没有时间范围控制 20191107
		boolean sim = false;// 默认走相似文章列表
		Boolean irsimflag = true; // 相似文章计算  要先站内排重之后再筛选  为true  要站内排重
		// 为了防止前台的来源名和数据库里边不对应
		if ("电子报".equals(source)) {
			source = "国内新闻_电子报";
		} else if ("论坛".equals(source)) {
			source = "国内论坛";
		} else if ("新闻".equals(source)) {
			source = "国内新闻";
		} else if ("博客".equals(source)) {
			source = "国内博客";
		} else if ("微信".equals(source)) {
			source = "国内微信";
		}
		trslk = RedisUtil.getString(trslk);
		if(StringUtil.isNotEmpty(trslk)){
			trslk = removeSimflag(trslk);
			trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
		}
		log.info("redis" + trslk);
		// 时间倒叙
		QueryBuilder builder = new QueryBuilder();
		builder.page(pageNo, pageSize);
		builder.filterByTRSL(trslk);
		if (StringUtil.isNotEmpty(md5Tag)) {
			builder.filterChildField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
		} else {// 若MD5为空 则走推荐文章列表
			sim = true;
		}
		QueryBuilder countBuilder = new QueryBuilder();// 算数的
		countBuilder.filterByTRSL(trslk);
		if (StringUtil.isNotEmpty(md5Tag)) {
			countBuilder.filterChildField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
		}
		User loginUser = UserUtils.getUser();
		if (!"ALL".equals(emotion)) { // 情感
			builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
		}

		if ("国内论坛".equals(source)) {
			//可以加主回帖筛选
			StringBuffer sb = new StringBuffer();
			if ("0".equals(invitationCard)) {// 主贴
				sb.append(Const.NRESERVED1_LUNTAN);
			} else if ("1".equals(invitationCard)) {// 回帖
				sb.append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
			}
			if (StringUtil.isNotEmpty(sb.toString())) {
				builder.filterByTRSL(sb.toString());
				countBuilder.filterByTRSL(sb.toString());
			}
		}
		if ("微博".equals(source)) {
			if ("primary".equals(forwarPrimary)) {
				// 原发
				builder.filterByTRSL(Const.PRIMARY_WEIBO);
				countBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
			} else if ("forward".equals(forwarPrimary)) {
				// 转发
				builder.filterByTRSL_NOT(Const.PRIMARY_WEIBO);
				countBuilder.filterByTRSL_NOT(Const.PRIMARY_WEIBO);
			}
		}
		// 结果中搜索
		if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {
			StringBuffer trsl = new StringBuffer();
			switch (fuzzyValueScope){
				case "title":
					trsl.append(FtsFieldConst.FIELD_TITLE).append(":").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \""));
					break;
				case "source":
					trsl.append(FtsFieldConst.FIELD_SITENAME).append(":").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \""));
					break;
				case "author":
					trsl.append(FtsFieldConst.FIELD_AUTHORS).append(":").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \""));
					break;
				case "fullText":
					trsl.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
					break;
			}
			builder.filterByTRSL(trsl.toString());
			countBuilder.filterByTRSL(trsl.toString());
		}

		//20200326 单独修改的普通搜索相似文章数去掉自身，如果之后全部列表页做了修改，去掉即可
		if(StringUtil.isNotEmpty(id) && searchPage.equals(SearchPage.ORDINARY_SEARCH.toString())){
			//id不为空，则去掉当前文章
			StringBuffer idBuffer = new StringBuffer();
			if(Const.MEDIA_TYPE_WEIBO.contains(source)){
				idBuffer.append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
			}else if(Const.MEDIA_TYPE_WEIXIN.contains(source)){
				idBuffer.append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
			}else{
				idBuffer.append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
			}
			builder.filterByTRSL_NOT(idBuffer.toString());
		}
		if (trslk != null && trslk.contains(
				"(IR_SITENAME:(\"新华网\" OR \"中国网\" OR \"央视网\"  OR \"中国新闻网\" OR  \"新浪网\" OR  \"网易\" OR \"搜狐网\" OR  \"凤凰网\") "
						+ "NOT IR_CHANNEL:(游戏)" + "AND IR_GROUPNAME:国内新闻 )NOT IR_URLTITLE:(吴君如 OR 汽车 OR 新车 OR 优惠)")) {
			// 结果中搜索
			/*
			if (StringUtil.isNotEmpty(fuzzyValue)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(fuzzyValue)
						.append(" OR ").append(FtsFieldConst.FIELD_ABSTRACTS).append(":").append(fuzzyValue)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(fuzzyValue)
						.append(" OR ").append(FtsFieldConst.FIELD_KEYWORDS).append(":").append(fuzzyValue).toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			}*/
			// 来源首页
			// 已经有来源了
			// 如果在去拼来源
			// 查不出东西
			// builder.filterField(FtsFieldConst.FIELD_SID, id,
			// Operator.NotEqual);
			log.info(builder.asTRSL());
			// builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			// countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotList(builder, countBuilder, loginUser,typeSim);
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}

			return infoListService.getDocList(builder, loginUser, sim, irsimflag,false,false,typeSim);
		} else if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotListWeChat(builder, countBuilder, loginUser,typeSim);
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			// builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			// countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			return infoListService.getWeChatList(builder, loginUser, false, irsimflag,false,false,typeSim);
		} else if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
				break;
			case "hot":
				return infoListService.getHotListStatus(builder, countBuilder, loginUser,typeSim);
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			log.info(builder.asTRSL());
			// builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
			// countBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
			return infoListService.getStatusList(builder, loginUser, false, irsimflag,false,false,typeSim);
		} else if (Const.MEDIA_TYPE_NEWS.contains(source)) {
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotList(builder, countBuilder, loginUser,typeSim);
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			log.info(builder.asTRSL());
			if (!builder.asTRSL().contains("IR_GROUPNAME") && !source.equals("传统媒体")) {
				if ("国内新闻".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 ").toString();
					builder.filterByTRSL(trsl);
					countBuilder.filterByTRSL(trsl);
				} else if ("国内论坛".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 ").toString();
					builder.filterByTRSL(trsl);
					countBuilder.filterByTRSL(trsl);
				} else {
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				}
			}
			// builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			// countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			return infoListService.getDocList(builder, loginUser, sim, irsimflag,false,false,typeSim);
		}
		return null;
	}


	/**
	 * 详情页左侧词云图
	 *
	 * @Author mawen
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@ApiOperation("详情页左侧词云图")
	@FormatResult
	@RequestMapping(value = "/getWordYun", method = RequestMethod.GET)
	public Object getWordYun(@ApiParam("source判断数据源") @RequestParam(value = "source", required = false) String source,
			@ApiParam("md5值") @RequestParam(value = "md5", required = false) String md5,
			@ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk,
			@ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
			@ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
			@ApiParam("时间区间") @RequestParam(value = "timeRange", defaultValue = "1d") String timeRange,
			@ApiParam("实体类型（通用：keywords；人名:people；地名:location；机构名:agency）") @RequestParam(value = "entityType", defaultValue = "location") String entityType,
			@ApiParam("文章类型（全部：all；新闻：news；微博：weibo；微信：weixin；客户端：app）") @RequestParam(value = "articleType", defaultValue = "all") String articleType)
			throws Exception {
		String trsl = RedisUtil.getString(trslk);
		if (null != trsl) {
			trsl += " AND MD5TAG:(" + md5 + ")";
		} else {
			trsl = " MD5TAG:(" + md5 + ")";
		}
		QueryBuilder searchBuilder = new QueryBuilder();
		searchBuilder.filterByTRSL(trsl);
		searchBuilder.page(0, 50);
		Object categoryQuery = null;
		boolean sim = true;
		if (isTraditionalMedia(source)) {
			searchBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
			searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		} else if (source.endsWith("微信")) {
			sim = false;
			entityType = "weixinlocation";
			searchBuilder.setDatabase(Const.WECHAT);
		} else if (source.endsWith("微博")) {
			searchBuilder.setDatabase(Const.WEIBO);
			entityType = "weibolocation";
			sim = false;
		} else {
			return "参数错误";
		}
		// 这个参数待确定
		categoryQuery = chartAnalyzeService.getWordCloud(false, searchBuilder.asTRSL(), sim, false,false, entityType,
				searchBuilder.getPageSize(),"special", searchBuilder.getDatabase());
		return categoryQuery;
	}

	/**
	 * 详情页相似文章接口 通过sid获取keyworld再根据语句查询
	 * 
	 * @Author mawen
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@ApiOperation("详情页相似文章接口")
	@FormatResult
	@RequestMapping(value = "/simList", method = RequestMethod.GET)
	public Object simList(@ApiParam("source判断数据源") @RequestParam(value = "source", required = false) String source,
			@ApiParam("sid") @RequestParam(value = "sid", required = false) String sid)
			throws TRSSearchException, TRSException {
		return infoListService.simlist(sid, source);
	}

	public String keyWords(List<String> keyWords) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < keyWords.size(); i++) {
			if (i < keyWords.size() - 1) {
				buffer.append("\"" + keyWords.get(i) + "\" AND ");
			} else {
				buffer.append("\"" + keyWords.get(i) + "\"");
			}
		}
		return buffer.toString();
	}

    /**
     * 信息列表页单条的详情
     *
     * @Author mawen
     * @throws TRSException
     * @throws TRSSearchException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@ApiOperation("首页和列表页点击单条文章进入详情页---全部")
	@FormatResult
	@RequestMapping(value = "/oneInfoAll", method = RequestMethod.GET)
	public Object oneInfoAll(@ApiParam("文章sid") @RequestParam("sid") String sid,
							 @ApiParam("md5值") @RequestParam(value = "md5", required = false) String md5,
							 @ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk,
							 @ApiParam("类型") @RequestParam(value = "groupName", required = true) String groupName,
							 @ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "nreserved1", required = false) String nreserved1)
            throws TRSSearchException, TRSException {
    	User user = UserUtils.getUser();
        String userId = user.getId();
        String trsl = RedisUtil.getString(trslk);
        fixedThreadPool.execute(() -> infoListService.simCount(sid, md5,null));
        QueryBuilder queryBuilder = new QueryBuilder();
        if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
            if (Const.PAGE_SHOW_WEIBO.contains(groupName)){
                queryBuilder.filterField(FtsFieldConst.FIELD_MID, sid, Operator.Equal);
            }else if(Const.PAGE_SHOW_WEIXIN.equals(groupName)){
                queryBuilder.filterField(FtsFieldConst.FIELD_HKEY, sid, Operator.Equal);
            }else {
                queryBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
            }
        }

        queryBuilder.filterByTRSL(trsl);
        queryBuilder.page(0, 1);
        InfoListResult infoListResult = commonListService.queryPageList(queryBuilder,false,false,false,groupName,"detail",UserUtils.getUser(),false);
        PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
        List<FtsDocumentCommonVO> ftsQuery = content.getPageItems();
        if (null != ftsQuery && ftsQuery.size() > 0) {
            FtsDocumentCommonVO ftsDocument = ftsQuery.get(0);

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
            ftsDocument.setTrslk(trslk);

            if (Const.GROUPNAME_WEIBO.equals(groupName)){
				realInfoThreadPool.execute(()->infoListService.getRealTimeInfoOfStatus(ftsDocument.getUrlName(),ftsDocument.getSid()));
				String urlName = ftsDocument.getUrlName();
				if (StringUtil.isNotEmpty(urlName)){
					urlName = urlName.replace("https","http");
					if (urlName.indexOf("?") != -1){
						//有问号
						urlName = urlName.split("\\?")[0];
					}
					String random = UUID.randomUUID().toString().replace("-", "");
					String currentUrl = urlName+random;
					weiboDetailThreadPool.execute(new HotReviewsWeiboTask(urlName,currentUrl,user,random));
					weiboDetailThreadPool.execute(new CoreForwardWeiboTask(urlName,currentUrl,user,random));
				}

				StatusUser statusUser = queryStatusUser(ftsDocument.getScreenName(), ftsDocument.getUid());
				if (ObjectUtil.isNotEmpty(statusUser)) {
					ftsDocument.setFollowersCount(statusUser.getFollowersCount());
				}else {
					ftsDocument.setFollowersCount(0);
				}
			}
            if (Const.TYPE_NEWS.contains(groupName)) {
                // 站点名是百度贴吧的 要是频道名不以吧结尾 就加上吧
                if ("百度贴吧".equals(ftsDocument.getSiteName())) {
                    if (StringUtil.isNotEmpty(ftsDocument.getChannel()) && !ftsDocument.getChannel().endsWith("吧")) {
                        ftsDocument.setChannel(ftsDocument.getChannel() + "吧");
                    }
                }
                if (null != nreserved1) {
                    QueryBuilder builder = new QueryBuilder();
                    HashMap<String, Object> returnMap = new HashMap<>();
                    builder.filterField(FtsFieldConst.FIELD_HKEY, ftsDocument.getHkey(), Operator.Equal);
                    if ("1".equals(nreserved1)) {
                        //直接查回帖的详情 加trsl
                        builder.filterByTRSL(trsl);//不加trsl,导致前端直接取主贴标题，关键词不描红问题
                        //若查主贴的详情，下面仅用来查询该主贴的回帖数，为和/getreplyCards保持一致，也与实际值保持一致，不许加trsl
                    }
//                ftsDocuments = hybase8SearchService.ftsPageList(builder, FtsDocument.class, false, false,false,null);
                    InfoListResult infoListResult2 = commonListService.queryPageList(builder, false, false, false, groupName, null, UserUtils.getUser(), false);
                    PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
                    List<FtsDocumentCommonVO> ftsDocuments = content2.getPageItems();
                    if ("1".equals(nreserved1)) {// 回帖
                        List list = new ArrayList<>();
                        if (null != ftsDocuments && ftsDocuments.size() > 0) {
                            for (FtsDocumentCommonVO document : ftsDocuments) {
                                log.info(document.getNreserved1() + "主回帖");
                                if ("0".equals(document.getNreserved1())) {// 说明这个是回帖对应的主贴
                                    document.setReplyCount(content2.getTotalItemCount() - 1);// 回帖个数
                                    // 把主贴刨去
                                    returnMap.put("mainCard", document);
                                }
                            }
                        }
                        list.add(ftsDocument);
                        returnMap.put("replyCard", list);
                        return returnMap;
                    } else if ("0".equals(nreserved1)) {// 主贴
                        ftsDocument.setReplyCount(content2.getTotalItemCount() - 1);// 回帖个数
                        // 把主贴刨去
                        returnMap.put("mainCard", ftsDocument);
                        return returnMap;
                    }
                }
            }
        }
        List all = new ArrayList<>();
        all.add(ftsQuery);
        System.err.println("方法结束" + System.currentTimeMillis());
        return all;
    }

	@ApiOperation("实时获取微博信息")
	@FormatResult
	@RequestMapping(value = "/getRealTimeInfo", method = RequestMethod.GET)
	public Object getRealTimeInfo(@ApiParam("文章sid") @RequestParam("sid") String sid){
    	return RedisUtil.getString(sid+"_present_data_info");
	}
	@ApiOperation("获取热评信息")
	@FormatResult
	@RequestMapping(value = "/getHotReviews", method = RequestMethod.GET)
	public Object getHotReviews(@ApiParam("urlName") @RequestParam("urlName") String urlName) throws TRSException {
		long start = new Date().getTime();
		log.info(urlName);
		urlName = urlName.replace("https","http");
		if (urlName.indexOf("?") != -1){
			//有问号
			urlName = urlName.split("\\?")[0];
		}
		List<SingleMicroblogData> singleMicroblogData = null;
			singleMicroblogData = singleMicroblogDataRepository.findByOriginalUrlAndName(urlName, MicroblogConst.HOTREVIEWS);
		if (ObjectUtil.isEmpty(singleMicroblogData)){
			return null;
		}
		return singleMicroblogData.get(0).getData();
	}
	@ApiOperation("获取核心转发")
	@FormatResult
	@RequestMapping(value = "/getCoreForward", method = RequestMethod.GET)
	public Object getCoreForward(@ApiParam("urlName") @RequestParam("urlName") String urlName) throws TRSException {
		long start = new Date().getTime();
		log.info(urlName);
		urlName = urlName.replace("https","http");
		if (urlName.indexOf("?") != -1){
			//有问号
			urlName = urlName.split("\\?")[0];
		}
		List<SingleMicroblogData> singleMicroblogData = null;
		singleMicroblogData = singleMicroblogDataRepository.findByOriginalUrlAndName(urlName, MicroblogConst.COREFORWARD);
		if (ObjectUtil.isEmpty(singleMicroblogData)){
			return null;
		}
		return singleMicroblogData.get(0).getData();
	}
	/**
	 * 信息列表页单条的详情
	 *
	 * @Author mawen
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ApiOperation("首页和列表页点击单条文章进入详情页---传统媒体")
	@FormatResult
	@RequestMapping(value = "/oneInfo", method = RequestMethod.GET)
	public Object oneInfo(@ApiParam("文章sid") @RequestParam("sid") String sid,
			@ApiParam("md5值") @RequestParam(value = "md5", required = false) String md5,
			@ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "nreserved1", required = false) String nreserved1)
			throws TRSSearchException, TRSException {
		String userId = UserUtils.getUser().getId();
		String trsl = RedisUtil.getString(trslk);
		fixedThreadPool.execute(() -> infoListService.simCount(sid, md5,null));
		QueryBuilder queryBuilder = new QueryBuilder();
		// if (null != trsl && !trsl.contains(sid)) {
		// // trsl += "AND IR_SID:(" + sid + ")" + "AND SIMFLAG:1000";
		// trsl += "AND IR_SID:(" + sid + ")";
		// } else {
		// // trsl = "IR_SID:(" + sid + ")" + "AND SIMFLAG:1000";
		// trsl = "IR_SID:(" + sid + ")";
		// }
		if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
			queryBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
		}

		queryBuilder.filterByTRSL(trsl);
		queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		queryBuilder.page(0, 1);
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, false, false,false,"detail");
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
			//Favourites favourite = favouritesRepository.findByUserIdAndSid(userId, sid);
			if (ObjectUtil.isNotEmpty(favourites) && StringUtil.isEmpty(favourites.getLibraryId())) {
				ftsDocument.setFavourite(true);
			} else {
				ftsDocument.setFavourite(false);
			}
			// 判断是否预警
			List<String> sids = new ArrayList<>();
			sids.add(ftsDocument.getSid());
			List<AlertEntity> alert = null;
			/*if (httpClient){
				alert = AlertUtil.getAlerts(userId,ftsDocument.getSid(),alertNetinsightUrl);
			}else {
				alert = alertRepository.findByUserIdAndSidIn(userId, sids);
			}

			if (ObjectUtil.isNotEmpty(alert)) {
				ftsDocument.setSend(true);
			} else {
				ftsDocument.setSend(false);
			}*/
			ftsDocument.setTrslk(trslk);
			// 站点名是百度贴吧的 要是频道名不以吧结尾 就加上吧
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
				if ("1".equals(nreserved1)){
					//直接查回帖的详情 加trsl
					builder.filterByTRSL(trsl);//不加trsl,导致前端直接取主贴标题，关键词不描红问题
					//若查主贴的详情，下面仅用来查询该主贴的回帖数，为和/getreplyCards保持一致，也与实际值保持一致，不许加trsl
				}
				ftsDocuments = hybase8SearchService.ftsPageList(builder, FtsDocument.class, false, false,false,null);
				if ("1".equals(nreserved1)) {// 回帖
					List list = new ArrayList<>();
					if (null != ftsDocuments && ftsDocuments.size() > 0) {
						for (FtsDocument document : ftsDocuments.getPageItems()) {
							log.info(document.getNreserved1() + "主回帖");
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
	@ApiOperation("文章详情查看是否预警的单独接口")
	@FormatResult
	@RequestMapping(value = "/hasAlert", method = RequestMethod.GET)
	public Object hasAlert(@ApiParam("文章sid/hkey/mid") @RequestParam("sid") String sid) throws OperationException{
          List<AlertEntity> alert = AlertUtil.getAlerts(UserUtils.getUser().getId(),sid,alertNetinsightUrl);
		  Map<String,Boolean> map = new HashMap<>();
		  if (ObjectUtil.isNotEmpty(alert)) {
			  map.put("send", true);
//				ftsDocument.setSend(true);
			} else {
//				ftsDocument.setSend(false);
				map.put("send", false);
			}
		return map;
	}
	/**
	 * 信息列表页单条的详情---微博
	 *
	 * @Author mawen
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ApiOperation("首页和列表页点击单条文章进入详情页----微博")
	@FormatResult
	@RequestMapping(value = "/oneInfoStatus", method = RequestMethod.GET)
	public Object oneInfoStatus(@ApiParam("文章sid") @RequestParam("" + "") String sid,
			@ApiParam("md5值") @RequestParam(value = "md5", required = false) String md5,
			@ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk)
			throws TRSSearchException, TRSException {
		String userId = UserUtils.getUser().getId();
		String trsl = RedisUtil.getString(trslk);
		fixedThreadPool.execute(() -> infoListService.simCountStatus(sid, md5,null));
		QueryBuilder queryBuilder = new QueryBuilder();
		if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
			queryBuilder.filterField(FtsFieldConst.FIELD_MID, sid, Operator.Equal);
		}
		// if (null != trsl && !trsl.contains(sid)) {
		// trsl += "AND IR_MID:(" + sid + ")";
		// } else {
		// trsl = "IR_MID:(" + sid + ")";
		// }
		queryBuilder.filterByTRSL(trsl);
		queryBuilder.page(0, 1);
		List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentStatus.class, false,
				false,false,"detail");
		// 查预警
		if (ObjectUtil.isNotEmpty(ftsQuery)) {
			List<String> sids = new ArrayList<>();
			sids.add(ftsQuery.get(0).getMid());
//			List<AlertEntity> alert = null;
			/*if (httpClient){
				alert = AlertUtil.getAlerts(userId,ftsQuery.get(0).getMid(),alertNetinsightUrl);
			}else {
				alert = alertRepository.findByUserIdAndSidIn(userId, sids);
			}

			if (ObjectUtil.isNotEmpty(alert)) {
				ftsQuery.get(0).setSend(true);
			} else {
				ftsQuery.get(0).setSend(false);
			}*/
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ApiOperation("首页和列表页点击单条文章进入详情页----推特及脸书等")
	@FormatResult
	@RequestMapping(value = "/oneInfoTF", method = RequestMethod.GET)
	public Object oneInfoTF(@ApiParam("文章sid") @RequestParam("sid") String sid,
			@ApiParam("md5值") @RequestParam(value = "md5", required = false) String md5,
			@ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk)
			throws TRSSearchException, TRSException {
		String userId = UserUtils.getUser().getId();
		String trsl = RedisUtil.getString(trslk);
		QueryBuilder queryBuilder = new QueryBuilder();
		if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
			queryBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
		}
		// if (StringUtils.isNotBlank(trsl) && !trsl.contains(sid)) {
		// trsl += "IR_SID:(" + sid + ")";
		// }else {
		// trsl = "IR_SID:(" + sid + ")";
		// }
		queryBuilder.filterByTRSL(trsl);
		queryBuilder.page(0, 1);
		List<FtsDocumentTF> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentTF.class, false, false,false,"detail");
		// 查预警
		if (ObjectUtil.isNotEmpty(ftsQuery)) {
			List<String> sids = new ArrayList<>();
			sids.add(ftsQuery.get(0).getSid());
			/*List<AlertEntity> alert = null;
			if (httpClient){
				alert = AlertUtil.getAlerts(userId,ftsQuery.get(0).getSid(),alertNetinsightUrl);
			}else {
				alert = alertRepository.findByUserIdAndSidIn(userId, sids);
			}
			if (ObjectUtil.isNotEmpty(alert)) {
				ftsQuery.get(0).setSend(true);
			} else {
				ftsQuery.get(0).setSend(false);
			}*/
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
	 * 信息列表页单条的详情---微信
	 *
	 * @param
	 * @return
	 * @date Created at 2017年11月21日 下午3:49:36
	 * @Author 马文
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ApiOperation("首页和列表页点击单条文章进入详情页---微信")
	@FormatResult
	@RequestMapping(value = "/oneInfoWeChat", method = RequestMethod.GET)
	public Object oneInfoWeChat(@ApiParam("文章sid") @RequestParam("sid") String sid,
			@ApiParam("md5值") @RequestParam(value = "md5", required = false) String md5,
			@ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk)
			throws TRSSearchException, TRSException {
		String userId = UserUtils.getUser().getId();
		String trsl = RedisUtil.getString(trslk);
		fixedThreadPool.execute(() -> infoListService.simCountWeChat(sid, md5,null));
		QueryBuilder queryBuilder = new QueryBuilder();
		// if (null != trsl) {
		// trsl += " AND IR_HKEY:(" + sid + ")";
		// } else {
		// trsl = " IR_HKEY:(" + sid + ")";
		// }
		// 新库以sid为主键
		queryBuilder.filterByTRSL(trsl);
		if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
			queryBuilder.filterField(FtsFieldConst.FIELD_HKEY, sid, Operator.Equal);
		}
	 	queryBuilder.page(0, 1);
		List<FtsDocumentWeChat> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentWeChat.class, false,
				false,false,"detail");
		// 查预警
		if (ObjectUtil.isNotEmpty(ftsQuery)) {
			List<String> sids = new ArrayList<>();
			sids.add(ftsQuery.get(0).getHkey());
			/*List<AlertEntity> alert = null;
			if (httpClient){
				alert = AlertUtil.getAlerts(userId,ftsQuery.get(0).getHkey(),alertNetinsightUrl);
			}else {
				alert = alertRepository.findByUserIdAndSidIn(userId, sids);
			}
			if (ObjectUtil.isNotEmpty(alert)) {
				ftsQuery.get(0).setSend(true);
			} else {
				ftsQuery.get(0).setSend(false);
			}*/
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

	@ApiOperation("进入文章详情页的异步")
	@FormatResult
	@RequestMapping(value = "/oneAsy", method = RequestMethod.GET)
	public Object oneAsy(@ApiParam("文章sid") @RequestParam("sid") String sid) throws TRSException {
		try {
			return infoListService.getOneAsy(sid);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 滚动查看下一页
	 *
	 * @param pageId
	 *            滚动页ID
	 */
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageId", value = "滚动页ID", dataType = "String", paramType = "query", required = true) })
	@RequestMapping(value = "/next", method = RequestMethod.GET)
	public Object nextList(@RequestParam("pageId") String pageId) throws TRSException {
		ObjectUtil.assertNull(pageId, "PAGE_ID");
		try {
			return infoListService.getNextList(pageId);
		} catch (InterruptedException e) {
			throw new OperationException("线程等待出错：" + e);
		}
	}

	/**
	 * 异步加载的数据
	 *
	 * @param pageId
	 *            滚动页ID
	 */
	@RequestMapping(value = "/asy", method = RequestMethod.GET)
	@FormatResult
	@ApiOperation("异步加载相似文章数")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageId", value = "滚动页ID", dataType = "String", paramType = "query", required = true) })
	public Object asyncList(@RequestParam("pageId") String pageId) throws TRSException {
		ObjectUtil.assertNull(pageId, "PAGE_ID");
		try {
			return infoListService.getAsyncList(pageId);
		} catch (InterruptedException e) {
			throw new OperationException("线程等待出错：" + e);
		}
	}

	/**
	 * 异步加载的数据
	 *
	 * @param pageId
	 *            滚动页ID
	 */
	@RequestMapping(value = "/asySiteName", method = RequestMethod.GET)
	@FormatResult
	@ApiOperation("异步加载相似文章数对应的发文网站信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageId", value = "滚动页ID", dataType = "String", paramType = "query", required = true) })
	public Object asySiteName(@RequestParam("pageId") String pageId) throws TRSException {
		ObjectUtil.assertNull(pageId, "PAGE_ID");
		try {
			return infoListService.getAsySiteNameList(pageId);
		} catch (InterruptedException e) {
			throw new OperationException("线程等待出错：" + e);
		}
	}


	/**
	 * 删除列表数据
	 * 
	 * @date Created at 2017年11月27日 下午5:25:02
	 * @Author 谷泽昊
	 * @param sid
	 * @param specialId
	 *            专题id
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialId", value = "专题id", dataType = "String", paramType = "query", required = true) })
	public Object delete(@RequestParam("sid") String sid, @RequestParam("specialId") String specialId)
			throws TRSException {
		ObjectUtil.assertNull(sid, "sid");
		List<JunkData> datas = new ArrayList<>();
		try {
			for (String str : sid.split(";")) {
				JunkData junkData = new JunkData();
				junkData.setId(str);
				junkData.setSpecialId(specialId);
				datas.add(junkData);
			}
			junkDataService.save(datas);
			return Const.SUCCESS;
		} catch (Exception e) {
			throw new OperationException("添加数据到删除库失败：" + e);
		}
	}

	boolean isTraditionalMedia(String source) {
		return "国内新闻".equals(source) || "国内论坛".equals(source) || "国内博客".equals(source) || "国内新闻_电子报".equals(source)
				|| "国内新闻_客户端".equals(source);
	}

	/**
	 * 查主贴对应的回帖
	 * 
	 * @param sid
	 * @param trslk
	 * @param nreserved1
	 * @return 回帖
	 * @throws TRSSearchException
	 * @throws TRSException
	 */
	@ApiOperation("论坛列表页 主贴进详情 主贴对应的回帖")
	@FormatResult
	@RequestMapping(value = "/getreplyCards", method = RequestMethod.GET)
	public Object getreplyCards(@ApiParam("文章sid") @RequestParam("sid") String sid,
			@ApiParam("trslk") @RequestParam(value = "trslk", required = false) String trslk,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "nreserved1", required = false) String nreserved1,
			@ApiParam("页码 ") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@ApiParam("步长 ") @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize)
			throws TRSSearchException, TRSException {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		String trsl = RedisUtil.getString(trslk);
		QueryBuilder queryBuilder = new QueryBuilder();
		// if (null != trsl) {
		// // trsl += "AND IR_SID:(" + sid + ")" + "AND SIMFLAG:1000";
		// trsl += "AND IR_SID:(" + sid + ")";
		// } else {
		// // trsl = "IR_SID:(" + sid + ")" + "AND SIMFLAG:1000";
		// trsl = "IR_SID:(" + sid + ")";
		// }
		if (StringUtil.isEmpty(trsl) || (StringUtil.isNotEmpty(trsl) && !trsl.contains(sid))) {
			queryBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
		}

		queryBuilder.filterByTRSL(trsl);
		queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		queryBuilder.page(0, 1);
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, false, false,false,null);
		if (null != ftsQuery && ftsQuery.size() > 0) {
			FtsDocument ftsDocument = ftsQuery.get(0);
			ftsDocument.setTrslk(trslk);
			// 主贴 ？ 回帖 ？
			if (null != nreserved1) {
				QueryBuilder builder = new QueryBuilder();
				builder.filterField(FtsFieldConst.FIELD_HKEY, ftsDocument.getHKey(), Operator.Equal);
				builder.page(pageNo, pageSize);
				PagedList<FtsDocument> ftsDocuments = null;
				if ("0".equals(nreserved1)) {// 主贴
					// 排除自身 只查它的回帖
					builder.filterField(FtsFieldConst.FIELD_SID, ftsDocument.getSid(), Operator.NotEqual);
					// 对贴吧数据getBbsNum楼层进行排序
					builder.orderBy(FtsFieldConst.FIELD_BBSNUM, true);
					ftsDocuments = hybase8SearchService.ftsPageList(builder, FtsDocument.class, false, false,false,null);
				}
				Map<String, Object> returnMap = new HashMap<>();
				returnMap.put("replyCard", ftsDocuments);
				return returnMap;
			}
		}
		return null;
	}

	@FormatResult
	@RequestMapping(value = "/testCount", method = RequestMethod.GET)
	public Object testCount() throws TRSException {
		QueryCommonBuilder builder = new QueryCommonBuilder();
		String trsl = "((IR_URLTITLE:(((\"俄罗斯\" OR \"政府\" OR \"辞职\"))))) AND ((IR_GROUPNAME:(国内新闻 OR 微博 OR 国内微信 OR 国内新闻_手机客户端 OR 国内论坛 OR 国内博客 OR 国内新闻_电子报 OR 国外新闻 OR Twitter OR FaceBook)))";
		builder.filterByTRSL(trsl);
		builder.page(0,10);
		//String[] strings = DateUtil.formatTimeRange("7d");
		String[] strings = {"20200112175730","20200119175730"};
		builder.filterField(FtsFieldConst.FIELD_URLTIME,strings,Operator.Between);
		builder.setDatabase(Const.MIX_DATABASE.split(";"));
		String[] split = Const.WEIBO.split(";");
		PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchService.pageListCommon(builder, false, false,true,"detail");
		//GroupResult groupInfos = hybase8SearchService.categoryQuery(builder, false, false, true, FtsFieldConst.FIELD_GROUPNAME, "detail",split);
			System.err.println("count："+pagedList.getTotalItemCount());
		return pagedList;
	}
	@FormatResult
	@RequestMapping(value = "/testCateCount", method = RequestMethod.GET)
	public Object testCateCount() throws TRSException {
		QueryBuilder builder = new QueryBuilder();
		String trsl = "((IR_URLTITLE:(((\"俄罗斯\" OR \"政府\" OR \"辞职\"))))) AND ((IR_GROUPNAME:(微博)))";
		builder.filterByTRSL(trsl);
		builder.page(0,10);
		//String[] strings = {"20200112175730","20200119175730"};
		String[] strings = DateUtil.formatTimeRange("7d");
		builder.filterField(FtsFieldConst.FIELD_URLTIME,strings,Operator.Between);
		builder.setDatabase(Const.WEIBO);
		String[] split = Const.WEIBO.split(";");
		//PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchService.pageListCommon(builder, false, false,true,"detail");
		System.err.println(builder.asTRSL());
		GroupResult groupInfos = hybase8SearchService.categoryQuery(builder, false, false, true, FtsFieldConst.FIELD_GROUPNAME, "detail",split);
		//	System.err.println("count："+pagedList.getTotalItemCount());
		return groupInfos;
	}
	/**
	 * 微博评论人信息
	 * @param ftsDocumentReviews
	 * @throws TRSException
	 */
	private void queryStatusUser(FtsDocumentReviews ftsDocumentReviews) throws TRSException{
		QueryBuilder queryStatusUser = new QueryBuilder();
		queryStatusUser.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+ftsDocumentReviews.getAuthors()+"\"",Operator.Equal);
		queryStatusUser.setDatabase(Const.SINAUSERS);
		//查询微博用户信息
		List<StatusUser> statusUsers = hybase8SearchService.ftsQuery(queryStatusUser, StatusUser.class, false, false,false,null);
//        if (ObjectUtil.isEmpty(statusUsers)){
//            QueryBuilder queryStatusUser1 = new QueryBuilder();
//            queryStatusUser1.filterField(FtsFieldConst.FIELD_UID,"\""+spreadObject.getUid()+"\"",Operator.Equal);
//            queryStatusUser1.setDatabase(Const.SINAUSERS);
//            //查询微博用户信息
//            statusUsers = hybase8SearchService.ftsQuery(queryStatusUser1, StatusUser.class, false, false);
//        }
		if (ObjectUtil.isNotEmpty(statusUsers)){
			//放入该条微博对应的 发布人信息
			ftsDocumentReviews.setStatusUser(statusUsers.get(0));
		}
	}
	/**
	 * 当前文章对应的用户信息
	 * @param screenName,uid
	 * @throws TRSException
	 */
	private StatusUser queryStatusUser(String screenName,String uid) throws TRSException{
		QueryBuilder queryStatusUser = new QueryBuilder();
		queryStatusUser.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+screenName+"\"",Operator.Equal);
		queryStatusUser.setDatabase(Const.SINAUSERS);
		//查询微博用户信息
//		List<StatusUser> statusUsers = hybase8SearchService.ftsQuery(queryStatusUser, StatusUser.class, false, false,false,null);
		PagedList<StatusUser> infoListResult = commonListService.queryPageListForClass(queryStatusUser, StatusUser.class, false, false,false,null);
		List<StatusUser> statusUsers = infoListResult.getPageItems();
		if (ObjectUtil.isEmpty(statusUsers)){
			QueryBuilder queryStatusUser1 = new QueryBuilder();
			queryStatusUser1.filterField(FtsFieldConst.FIELD_UID,"\""+uid+"\"",Operator.Equal);
			queryStatusUser1.setDatabase(Const.SINAUSERS);
			//查询微博用户信息
//			statusUsers = hybase8SearchService.ftsQuery(queryStatusUser1, StatusUser.class, false, false,false,null);
			PagedList<StatusUser> infoListResult1 = commonListService.queryPageListForClass(queryStatusUser1, StatusUser.class, false, false,false,null);
			statusUsers = infoListResult1.getPageItems();
		}
		if (ObjectUtil.isNotEmpty(statusUsers)){
			//放入该条微博对应的 发布人信息
			return statusUsers.get(0);
		}
		return null;

	}
}
