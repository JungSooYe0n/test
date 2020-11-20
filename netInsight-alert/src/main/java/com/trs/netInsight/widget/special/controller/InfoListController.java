package com.trs.netInsight.widget.special.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.TRSInputRecord;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.hybaseShard.entity.HybaseShard;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.support.log.entity.RequestTimeLog;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.support.log.repository.RequestTimeLogRepository;
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
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.entity.JunkData;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.enums.SearchPage;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.special.service.IJunkDataService;
import com.trs.netInsight.widget.special.service.ISearchRecordService;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.util.VideoRedisUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.text.SimpleDateFormat;
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
	private IHybaseShardService hybaseShardService;

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
	@Autowired
	private RequestTimeLogRepository requestTimeLogRepository;

	@Autowired
	private OrganizationRepository organizationRepository;
	/**
	 * 是否走独立预警服务
	 */
	@Value("${http.client}")
	private boolean httpClient;

	/**
	 * 获取短视频连接服务地址
	 */
	@Value("${http.getdouyin.url}")
	private String douyinUrl;

	@Value("${http.getkuaishou.url}")
	private String kuaishouUrl;

	@Value("${video.play.dy.prefix}")
	private String videoPlaydyPrefix;

	@Value("${video.play.ks.prefix}")
	private String videoPlayksPrefix;

	/**
	 * 独立预警服务地址
	 */
	@Value("${http.alert.netinsight.url}")
	private String alertNetinsightUrl;
	@Autowired
	private FullTextSearch hybase8SearchServiceNew;
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
	@RequestMapping(value = "/info", method = RequestMethod.POST)
	public Object dataList(@ApiParam("专题id")@RequestParam(value = "specialId") String specialId,
						   @ApiParam("页码")@RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
						   @ApiParam("当前页显示条数")@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
						   @ApiParam("数据源 -当前列表的")@RequestParam(value = "source", defaultValue = "ALL", required = false) String source,
						   @ApiParam("排序")@RequestParam(value = "sort", defaultValue = "", required = false) String sort,
						   @ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
						   @ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
						   @ApiParam("结果中搜索") @RequestParam(value = "keywords", required = false) String keywords,
						   @ApiParam("结果中搜索的范围") @RequestParam(value = "fuzzyValueScope", defaultValue = "fullText", required = false) String fuzzyValueScope,

						   @ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
						   @ApiParam("排重规则  -  替换栏目条件") @RequestParam(value = "simflag", required = false) String simflag,
						   @ApiParam("关键词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "wordIndex", required = false) String wordIndex,
						   @ApiParam("情感倾向") @RequestParam(value = "emotion", required = false) String emotion,
						   @ApiParam("阅读标记") @RequestParam(value = "read", required = false) String read,
						   @ApiParam("排除网站  替换栏目条件") @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
						   @ApiParam("监测网站  替换栏目条件") @RequestParam(value = "monitorSite", required = false) String monitorSite,
						   @ApiParam("排除关键词  替换栏目条件") @RequestParam(value = "excludeWords", required = false) String excludeWords,
						   @ApiParam("排除词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "excludeWordsIndex", defaultValue = "1", required = false) String excludeWordsIndex,
						   @ApiParam("修改词距标记 替换栏目条件") @RequestParam(value = "updateWordForm", defaultValue = "false", required = false) Boolean updateWordForm,
						   @ApiParam("词距间隔字符 替换栏目条件") @RequestParam(value = "wordFromNum", required = false) Integer wordFromNum,
						   @ApiParam("词距是否排序  替换栏目条件") @RequestParam(value = "wordFromSort", required = false) Boolean wordFromSort,
						   @ApiParam("媒体等级") @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
						   @ApiParam("数据源  替换栏目条件") @RequestParam(value = "groupName", required = false) String groupName,
						   @ApiParam("媒体行业") @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
						   @ApiParam("内容行业") @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
						   @ApiParam("信息过滤") @RequestParam(value = "filterInfo", required = false) String filterInfo,
						   @ApiParam("信息地域") @RequestParam(value = "contentArea", required = false) String contentArea,
						   @ApiParam("媒体地域") @RequestParam(value = "mediaArea", required = false) String mediaArea,
						   @ApiParam("精准筛选") @RequestParam(value = "preciseFilter", required = false) String preciseFilter,
						   @ApiParam("随机数") @RequestParam(value = "randomNum", required = false) String randomNum,
						   @ApiParam("OCR筛选，对图片的筛选：全部：ALL、仅看图片img、屏蔽图片noimg") @RequestParam(value = "imgOcr", defaultValue = "ALL",required = false) String imgOcr) throws TRSException {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		Date startDate = new Date();
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
			specialProject.formatSpecialProject(simflag,wordIndex,excludeWeb,monitorSite,excludeWords,excludeWordsIndex,updateWordForm,wordFromNum,wordFromSort,
					mediaLevel,groupName,mediaIndustry,contentIndustry,filterInfo,contentArea,mediaArea);
			// 跟统计表格一样 如果来源没选 就不查数据
			List<String> specialSource = CommonListChartUtil.formatGroupName(groupName);
			if(!"ALL".equals(source)){
				source = CommonListChartUtil.changeGroupName(source);
				if(!specialSource.contains(source)){
					return null;
				}
			}else{
				source = StringUtils.join(specialSource,";");
			}
			Date hyStartDate = new Date();
			Object documentCommonSearch =  infoListService.documentCommonSearch(specialProject, pageNo, pageSize, source,
						timeRange, emotion, sort, invitationCard,forwarPrimary, keywords, fuzzyValueScope,
						"special", read, preciseFilter,imgOcr);
				long endTime = System.currentTimeMillis();
				log.warn("间隔时间："+(endTime - startTime));
			RequestTimeLog requestTimeLog = new RequestTimeLog();
			requestTimeLog.setTabId(specialId);
			requestTimeLog.setTabName(specialProject.getSpecialName());
			requestTimeLog.setStartHybaseTime(hyStartDate);
			requestTimeLog.setEndHybaseTime(new Date());
			requestTimeLog.setStartTime(startDate);
			requestTimeLog.setEndTime(new Date());
			requestTimeLog.setRandomNum(randomNum);
			requestTimeLog.setOperation("专题分析-信息列表-列表查询");
			requestTimeLogRepository.save(requestTimeLog);
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
	@RequestMapping(value = "/searchStattotal", method = RequestMethod.POST)
	public Object searchStattotal(
			@ApiParam("关键词组") @RequestParam(value = "keywords", required = false) String keywords,
			@ApiParam("查询类型：精准precise、模糊fuzzy") @RequestParam(value = "searchType",defaultValue = "fuzzy",required = false) String searchType,
			@ApiParam("时间设置") @RequestParam(value = "time", required = false, defaultValue = "7d") String time,
			@ApiParam("排重选择") @RequestParam(value = "simflag", required = false) String simflag,
			@ApiParam("关键词位置") @RequestParam(value = "keyWordIndex", defaultValue = "1", required = false) String keyWordIndex,
			@ApiParam("排序规则 - 是时间排序，还是优先标题命中") @RequestParam(value = "weight", defaultValue = "false", required = false) boolean weight,
			@ApiParam("来源网站") @RequestParam(value = "monitorSite", required = false) String monitorSite,
			@ApiParam("排除网站") @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@ApiParam("情绪选择") @RequestParam(value = "emotion", defaultValue = "ALL", required = false) String emotion,
			@ApiParam("词距范围") @RequestParam(value = "wordFromNum", defaultValue = "0", required = false) Integer wordFromNum,
			@ApiParam("词距是否排序") @RequestParam(value = "wordFromSort", defaultValue = "false", required = false) Boolean wordFromSort,
			@ApiParam("阅读标记") @RequestParam(value = "read", defaultValue = "ALL") String read,
			@ApiParam("排除关键词") @RequestParam(value = "excludeWords", required = false) String excludeWords,
			@ApiParam("排除关键词命中位置") @RequestParam(value = "excludeWordsIndex", required = false) String excludeWordsIndex,

			@ApiParam("媒体类型") @RequestParam(value = "source", defaultValue = "ALL") String source,
			@ApiParam("媒体等级") @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
			@ApiParam("媒体行业") @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
			@ApiParam("内容行业") @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
			@ApiParam("信息过滤") @RequestParam(value = "filterInfo", required = false) String filterInfo,
			@ApiParam("信息地域") @RequestParam(value = "contentArea", required = false) String contentArea,
			@ApiParam("媒体地域") @RequestParam(value = "mediaArea", required = false) String mediaArea,
			@ApiParam("精准筛选") @RequestParam(value = "preciseFilter", required = false) String preciseFilter,
			@ApiParam("OCR筛选，对图片的筛选：全部：ALL、仅看图片img、屏蔽图片noimg") @RequestParam(value = "imgOcr", defaultValue = "ALL",required = false) String imgOcr


			/*@ApiParam("新闻信息资质") @RequestParam(value = "newsInformation", required = false) String newsInformation,
			@ApiParam("可供转载网站 - 与门户类型是同一个字段") @RequestParam(value = "reprintPortal", required = false) String reprintPortal,
			@ApiParam("门户类型 - 与可供转载网站是一个字段") @RequestParam(value = "portalType", required = false) String portalType,
			@ApiParam("网站类型") @RequestParam(value = "siteType", required = false) String siteType,@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwardPrimary", required = false) String forwardPrimary*/
	) throws TRSException {
		log.warn("图表分析统计表格stattotal接口开始调用");
		try {
			//高级搜索只有一个关键词组
			JSONArray jsonArray = JSONArray.parseArray(keywords);
			String searchKey = "";
			if (jsonArray != null && jsonArray.size() == 1) {
				Object o = jsonArray.get(0);
				JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
				searchKey = jsonObject.getString("keyWords");
				searchKey = searchKey.replaceAll("\\s+", "");
				jsonObject.put("keyWords", searchKey);
				if(wordFromNum == null ){
					wordFromNum = 0;
				}
				if(wordFromNum == 0){
					wordFromSort = false;
				}
				jsonObject.put("wordSpace", wordFromNum);
				jsonObject.put("wordOrder", wordFromSort);
				jsonArray.set(0, jsonObject);
				keywords = jsonArray.toJSONString();
			}
			//只有 关键词、排除网站、监测网站、排除词 这四个都为空时才返回空
			if(StringUtil.isEmpty(searchKey) && StringUtil.isEmpty(excludeWeb) && StringUtil.isEmpty(excludeWords) && StringUtil.isEmpty(monitorSite)){
				return null;
			}
			// 默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			boolean irSimflagAll = false;
			if ("netRemove".equals(simflag)) {
				isSimilar = true;
			} else if ("urlRemove".equals(simflag)) {
				irSimflag = true;
			} else if ("sourceRemove".equals(simflag)) {
				irSimflagAll = true;
			}
			if("positioCon".equals(keyWordIndex)){
				keyWordIndex = "1";
			}else if("positionKey".equals(keyWordIndex)){
				keyWordIndex= "0";
			}
			if(excludeWordsIndex == null){
				excludeWordsIndex = keyWordIndex;
			}
			Object total = infoListService.searchstattotal(isSimilar, irSimflag, irSimflagAll, source,searchType,
					keywords,keyWordIndex,time,weight,monitorSite,excludeWeb,emotion,read,excludeWords,excludeWordsIndex,
					mediaLevel,mediaIndustry,contentIndustry,filterInfo,contentArea,mediaArea,preciseFilter, imgOcr,"advance");
			return total;
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
			@ApiImplicitParam(name = "sort", value = "排序", dataType = "boolean", paramType = "query", required = false),
            @ApiImplicitParam(name = "keywords", value = "关键词组", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "searchType", value = "搜索类型 - 模糊、精准", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "time", value = "时间", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排,url排,跨数据源排", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "keyWordIndex", value = "关键词位置", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "weight", value = "标题权重-排序规则", dataType = "boolean", paramType = "query", required = false),
            @ApiImplicitParam(name = "monitorSite", value = "监测网站", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "emotion", value = "情绪选择", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWords", value = "排除关键词", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "excludeWordsIndex", value = "排除关键词位置", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "wordFromNum", value = "词距", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "wordFromSort", value = "词组是否排序", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "read", value = "阅读标记", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "source", value = "媒体类型", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "mediaLevel", value = "媒体等级", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "mediaIndustry", value = "媒体行业", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "contentIndustry", value = "内容行业", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "filterInfo", value = "信息过滤", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "contentArea", value = "内容地域", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "mediaArea", value = "媒体地域", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "preciseFilter", value = "精准筛选", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "imgOcr", value = "OCR筛选，对图片的筛选：全部：ALL、仅看图片img、屏蔽图片noimg", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "fuzzyValueScope", value = "结果中搜索de范围", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "fuzzyValue", value = "结果中搜索", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "invitationCard", value = "论坛主贴 0 /回帖 1 所有=主贴+回帖+转帖", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "forwardPrimary", value = "微博 原发 primary / 转发 forward", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "checkedSource", value = "当前列表页对应的的媒体类型", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "svmTest", value = "测试选项", dataType = "String", paramType = "query", required = false)})
	@RequestMapping(value = "/searchList", method = RequestMethod.POST)
    public Object searchList(@RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
                             @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                             @RequestParam(value = "sort", defaultValue = "relevance", required = false) String sort,

                             @RequestParam(value = "keywords", required = false) String keywords,
                             @RequestParam(value = "searchType", required = false) String searchType,
                             @RequestParam(value = "time", required = false, defaultValue = "7d") String time,
                             @RequestParam(value = "simflag", required = false) String simflag,
                             @RequestParam(value = "keyWordIndex", defaultValue = "positioCon", required = false) String keyWordIndex,
                             @RequestParam(value = "weight", defaultValue = "false", required = false) boolean weight,
                             @RequestParam(value = "monitorSite", required = false) String monitorSite,
                             @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
                             @RequestParam(value = "emotion", defaultValue = "ALL", required = false) String emotion,

                             @RequestParam(value = "wordFromNum", defaultValue = "0", required = false) Integer wordFromNum,
                             @RequestParam(value = "wordFromSort", defaultValue = "false", required = false) Boolean wordFromSort,
                             @RequestParam(value = "read", defaultValue = "ALL") String read,
                             @RequestParam(value = "excludeWords", required = false) String excludeWords,
                             @RequestParam(value = "excludeWordsIndex", required = false) String excludeWordsIndex,

                             @RequestParam(value = "source", defaultValue = "ALL") String source,
                             @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
                             @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
                             @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
                             @RequestParam(value = "filterInfo", required = false) String filterInfo,
                             @RequestParam(value = "contentArea", required = false) String contentArea,
                             @RequestParam(value = "mediaArea", required = false) String mediaArea,
                             @RequestParam(value = "preciseFilter", required = false) String preciseFilter,
							 @RequestParam(value = "imgOcr", defaultValue = "ALL",required = false) String imgOcr,

                             @RequestParam(value = "invitationCard", required = false) String invitationCard,
                             @RequestParam(value = "forwardPrimary", required = false) String forwardPrimary,
                             @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
                             @RequestParam(value = "fuzzyValueScope", defaultValue = "fullText", required = false) String fuzzyValueScope,
                             @RequestParam(value = "checkedSource", defaultValue = "ALL") String checkedSource,
							 @RequestParam(value = "svmTest", required = false) String svmTest
    ) throws TRSException {
		log.warn("专项检测信息列表  开始调用接口");
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;


		try {
			//判断当前选择数据源和该次搜索选择的全部数据源是否是包含关系，不包含则无法查询
			if (!"ALL".equals(checkedSource)) {
				List<String> sourceList = CommonListChartUtil.formatGroupName(source);
				checkedSource = CommonListChartUtil.changeGroupName(checkedSource);
				if (!sourceList.contains(checkedSource)) {
					return null;
				} else {
					source = checkedSource;
				}
			}

            //高级搜索只有一个关键词组
            JSONArray jsonArray = JSONArray.parseArray(keywords);
			String searchKey = "";
            if (jsonArray != null && jsonArray.size() == 1) {
                Object o = jsonArray.get(0);
                JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
				searchKey = jsonObject.getString("keyWords");

                searchKey = searchKey.replaceAll("\\s+", "");
                jsonObject.put("keyWords", searchKey);
                if(wordFromNum == null ){
                    wordFromNum = 0;
                }
                if(wordFromNum == 0){
                    wordFromSort = false;
                }
                jsonObject.put("wordSpace", wordFromNum);
                jsonObject.put("wordOrder", wordFromSort);
                jsonArray.set(0, jsonObject);
                keywords = jsonArray.toJSONString();
            }
			//只有 关键词、排除网站、监测网站、排除词 这四个都为空时才返回空
			if(StringUtil.isEmpty(searchKey) && StringUtil.isEmpty(excludeWeb) && StringUtil.isEmpty(excludeWords) && StringUtil.isEmpty(monitorSite)){
				return null;
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

			if("positioCon".equals(keyWordIndex)){
				keyWordIndex = "1";
			}else if("positionKey".equals(keyWordIndex)){
				keyWordIndex= "0";
			}
            if(excludeWordsIndex == null){
                excludeWordsIndex = keyWordIndex;
            }
			return infoListService.advancedSearchList(isSimilar, irSimflag, irSimflagAll, pageNo, pageSize, sort,
					keywords, searchType, time, keyWordIndex, weight, monitorSite, excludeWeb, emotion, read, excludeWords, excludeWordsIndex, source,
					mediaLevel, mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea,
					preciseFilter, invitationCard, forwardPrimary, fuzzyValue, fuzzyValueScope, imgOcr,"advance",svmTest);

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
	public Object simList(
			@ApiParam("来源，当前列表页的数据源") @RequestParam("source") String source,
			@ApiParam("md5标示-点击的信息的id") @RequestParam(value = "md5Tag", required = false) String md5Tag,
			@ApiParam("推荐文章排除自己-点击的信息的id") @RequestParam(value = "id", required = false) String id,
			@ApiParam("表达式-当前列表的查询的表达式") @RequestParam("trslk") String trslk,
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
		String typeSim = "detail";//默认时间一年，用到的情况：①trslk失效；②trlk对应的trsl表达式没有时间范围控制
		boolean sim = false;// 默认走相似文章列表
		Boolean irsimflag = true; // 相似文章计算  要先站内排重之后再筛选  为true  要站内排重
		trslk = RedisUtil.getString(trslk);
		if(StringUtil.isNotEmpty(trslk)){
			//相似文章计算要先去掉排重
			trslk = removeSimflag(trslk);
			trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
		}
		log.info("redis" + trslk);
		if(!"ALL".equals(source)){
			source = CommonListChartUtil.changeGroupName(source);
		}else{
			if(StringUtil.isNotEmpty(trslk)) {
				String[] groupNameArr = TrslUtil.getGroupNameByTrsl(trslk);
				if(groupNameArr != null && groupNameArr.length >0){
					source = StringUtil.join(groupNameArr,";");
				}
			}
			//TrslUtil.chooseDatabases(groupNameSet.toArray(new String[] {}));
		}
		// 时间倒叙
		QueryBuilder builder = new QueryBuilder();
		builder.page(pageNo, pageSize);
		builder.filterByTRSL(trslk);
		if (StringUtil.isNotEmpty(md5Tag)) {
			builder.filterChildField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
		} else {// 若MD5为空 则走推荐文章列表
			sim = true;
		}
		User loginUser = UserUtils.getUser();
		if (!"ALL".equals(emotion)) { // 情感
			builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
		}

		if (Const.GROUPNAME_LUNTAN.equals(source)) {
			//可以加主回帖筛选
			StringBuffer sb = new StringBuffer();
			if ("0".equals(invitationCard)) {// 主贴
				sb.append(Const.NRESERVED1_LUNTAN);
			} else if ("1".equals(invitationCard)) {// 回帖
				sb.append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
			}
			if (StringUtil.isNotEmpty(sb.toString())) {
				builder.filterByTRSL(sb.toString());
			}
		}
		if (Const.GROUPNAME_WEIBO.equals(source)) {
			if ("primary".equals(forwarPrimary)) {
				// 原发
				builder.filterByTRSL(Const.PRIMARY_WEIBO);
			} else if ("forward".equals(forwarPrimary)) {
				// 转发
				builder.filterByTRSL_NOT(Const.PRIMARY_WEIBO);
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
		}

		if(StringUtil.isNotEmpty(id)){
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

		InfoListResult infoListResult = null;
		if("hot".equals(sort)){
			infoListResult= commonListService.queryPageListForHot(builder, source, loginUser, typeSim, false);
		}else{
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				default:
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			infoListResult =  commonListService.queryPageList(builder, sim, irsimflag, false, source, typeSim, loginUser, false);
		}
		if (infoListResult != null) {
			if (infoListResult.getContent() != null) {
				String trslkForPage = infoListResult.getTrslk();
				PagedList<Object> resultContent = CommonListChartUtil.formatListData(infoListResult,trslkForPage,null);
				infoListResult.setContent(resultContent);
			}
		}
		return infoListResult;

		/*switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotList(builder, null, loginUser,typeSim);
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
		}


		if (trslk != null && trslk.contains(
				"(IR_SITENAME:(\"新华网\" OR \"中国网\" OR \"央视网\"  OR \"中国新闻网\" OR  \"新浪网\" OR  \"网易\" OR \"搜狐网\" OR  \"凤凰网\") "
						+ "NOT IR_CHANNEL:(游戏)" + "AND IR_GROUPNAME:国内新闻 )NOT IR_URLTITLE:(吴君如 OR 汽车 OR 新车 OR 优惠)")) {
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotList(builder, null, loginUser,typeSim);
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
				return infoListService.getHotListWeChat(builder, null, loginUser,typeSim);
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}

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
				return infoListService.getHotListStatus(builder, null, loginUser,typeSim);
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}

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
				return infoListService.getHotList(builder, null, loginUser,typeSim);
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			if (!builder.asTRSL().contains("IR_GROUPNAME") && !source.equals("传统媒体")) {
				if ("国内新闻".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 ").toString();
					builder.filterByTRSL(trsl);
				} else if ("国内论坛".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 ").toString();
					builder.filterByTRSL(trsl);
				} else {
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				}
			}
			return infoListService.getDocList(builder, loginUser, sim, irsimflag,false,false,typeSim);
		}
		return null;*/
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
	@FormatResult
	@ApiOperation("设置已读接口")
	@RequestMapping(value = "/setReadArticle",method = RequestMethod.GET)
	public Object setReadArticle(@ApiParam("sid") @RequestParam(value = "sid",required = true) String sids,
							 @ApiParam("类型") @RequestParam(value = "groupName", required = true) String groupName,
							 @ApiParam("文章的urltime") @RequestParam(value = "urltime", required = false) String urltime,
							 @ApiParam("trslk") @RequestParam(value = "trslk",required = false) String trslk)throws TRSException{
		try {
			String[] groupNames = groupName.split(";");
			String[] sidArray = sids.split(";");
			if(groupNames.length != sidArray.length){
				return new TRSException("所传sid和groupName的个数不相同");
			}
			QueryBuilder builderTime = DateUtil.timeBuilder(urltime);
			Date start = builderTime.getStartTime();
			Date end = builderTime.getEndTime();
			SimpleDateFormat format = new SimpleDateFormat(DateUtil.yyyyMMdd2);
			String startString = format.format(start);
			String endString = format.format(end);
			String trsl = null;
			if (StringUtil.isNotEmpty(trslk)) {
				 trsl = RedisUtil.getString(trslk);
			}

			List<String> idList = new ArrayList<>();
			List<String> weixinList = new ArrayList<>();
			List<String> weiboList = new ArrayList<>();
			List<String> groupName_other = new ArrayList<>();
			List<FtsDocumentCommonVO> result = new ArrayList<>();
			for (int i = 0; i < sidArray.length; i++) {
				String tgroupName = Const.SOURCE_GROUPNAME_CONTRAST.get(groupNames[i]);
				if (Const.MEDIA_TYPE_WEIXIN.contains(tgroupName)) {
					weixinList.add(sidArray[i]);
				} else if (Const.PAGE_SHOW_WEIBO.contains(tgroupName)){
					weiboList.add(sidArray[i]);
				}else {
					idList.add(sidArray[i]);
					if (!groupName_other.contains(groupNames[i])) {
						groupName_other.add(groupNames[i]);
					}
				}
			}
			if (idList.size() > 0){
				QueryBuilder builder = new QueryBuilder();
				if (StringUtil.isNotEmpty(trsl)) {
					builder.filterByTRSL(trsl);
				}
				if (StringUtil.isNotEmpty(startString) && StringUtil.isNotEmpty(endString)) {
					builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString + "000000", endString + "235959"}, Operator.Between);
				}
				builder.filterField(FtsFieldConst.FIELD_SID, StringUtils.join(idList, " OR "), Operator.Equal);
				builder.page(0, idList.size() * 2);
				String searchGroupName = StringUtils.join(groupName_other, ";");
				log.info("选中查询数据表达式 - 全部：" + builder.asTRSL());
				InfoListResult infoListResult = commonListService.queryPageList(builder,false,false,false,searchGroupName,null,UserUtils.getUser(),false);
				PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
				if (content.getPageItems() != null && content.getPageItems().size() > 0) {
					result.addAll(content.getPageItems());
				}
			}
			if (weixinList.size() > 0) {
				QueryBuilder builderWeiXin = new QueryBuilder();//微信的表达式
				if (StringUtil.isNotEmpty(trsl)) {
					builderWeiXin.filterByTRSL(trsl);
				}
				if (StringUtil.isNotEmpty(startString) && StringUtil.isNotEmpty(endString)) {
					builderWeiXin.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString + "000000", endString + "235959"}, Operator.Between);
				}
				String weixinids = StringUtils.join(weixinList, " OR ");
				builderWeiXin.filterField(FtsFieldConst.FIELD_HKEY, weixinids, Operator.Equal);
				builderWeiXin.page(0, weixinList.size() * 2);
				log.info("预警查询数据表达式 - 微信：" + builderWeiXin.asTRSL());
				InfoListResult infoListResult = commonListService.queryPageList(builderWeiXin,false,false,false,Const.TYPE_WEIXIN,null,UserUtils.getUser(),false);
				PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
				if (content.getPageItems() != null && content.getPageItems().size() > 0) {
					result.addAll(content.getPageItems());
				}
			}
			if (weiboList.size() > 0) {
				QueryBuilder builderWeiBo = new QueryBuilder();//微信的表达式
				if (StringUtil.isNotEmpty(trsl)) {
					builderWeiBo.filterByTRSL(trsl);
				}
				if (StringUtil.isNotEmpty(startString) && StringUtil.isNotEmpty(endString)) {
					builderWeiBo.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString + "000000", endString + "235959"}, Operator.Between);
				}
				String weiboids = StringUtils.join(weiboList, " OR ");
				builderWeiBo.filterField(FtsFieldConst.FIELD_MID, weiboids, Operator.Equal);
				builderWeiBo.page(0, weiboList.size() * 2);
				log.info("预警查询数据表达式 - 微信：" + builderWeiBo.asTRSL());
				InfoListResult infoListResult = commonListService.queryPageList(builderWeiBo,false,false,false,Const.GROUPNAME_WEIBO,null,UserUtils.getUser(),false);
				PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
				if (content.getPageItems() != null && content.getPageItems().size() > 0) {
					result.addAll(content.getPageItems());
				}
			}
			if (null != result && result.size() > 0) {
				for (FtsDocumentCommonVO ftsDocumentCommonVO : result) {
					readArticle(ftsDocumentCommonVO);
				}
			}
//			if (Const.PAGE_SHOW_WEIBO.contains(groupName)){
//				queryBuilder.filterField(FtsFieldConst.FIELD_MID, sid, Operator.Equal);
//			}else if(Const.PAGE_SHOW_WEIXIN.equals(groupName)){
//				queryBuilder.filterField(FtsFieldConst.FIELD_HKEY, sid, Operator.Equal);
//			}else {
//				queryBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
//			}

		} catch (Exception e) {
			throw new TRSException("设置已读失败,message" + e);
		}
		return "success";
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
            throws TRSSearchException, TRSException, com.trs.hybase.client.TRSException{
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

//        	加上已读 / 未读 标
			for (FtsDocumentCommonVO ftsDocumentCommonVO : ftsQuery) {
				readArticle(ftsDocumentCommonVO);
			}

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
            if(StringUtil.isEmpty(ftsDocument.getAppraise())){
            	ftsDocument.setAppraise("中性");
			}

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
                    builder.setPageSize(20);
//                    builder.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
					builder.filterField(FtsFieldConst.FIELD_HKEY, ftsDocument.getHkey(), Operator.Equal);
                    if ("1".equals(nreserved1)) {
                        //直接查回帖的详情 加trsl
//                        builder.filterByTRSL(trsl);//不加trsl,导致前端直接取主贴标题，关键词不描红问题
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
						List list = new ArrayList<>();
						if (null != ftsDocuments && ftsDocuments.size() > 0) {
							for (FtsDocumentCommonVO document : ftsDocuments) {
								log.info(document.getNreserved1() + "主回帖");
								if ("0".equals(document.getNreserved1())) {// 说明这个是回帖对应的主贴
									document.setReplyCount(content2.getTotalItemCount() - 1);// 回帖个数
									// 把主贴刨去
									returnMap.put("mainCard", document);
								}else {
									list.add(document);
								}
							}
						}

						returnMap.put("replyCard", list);
						return returnMap;
//                        ftsDocument.setReplyCount(content2.getTotalItemCount() - 1);// 回帖个数
//                        // 把主贴刨去
//                        returnMap.put("mainCard", ftsDocument);
//                        return returnMap;
                    }
                }
            }
        }
        List all = new ArrayList<>();
        all.add(ftsQuery);
        System.err.println("方法结束" + System.currentTimeMillis());
        return all;
    }

	/**
	 * 详情页获得视频源链接
	 * @Author lilei
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@ApiOperation("视频/短视频进入详情页获得原链接")
	@FormatResult
	@RequestMapping(value = "/getVideoAddress", method = RequestMethod.GET)
	public Object getVideoAddress(@ApiParam("get/send get获得原链接,send发送请求") @RequestParam(value = "getSend",defaultValue = "send") String getSend,
								  @ApiParam("来源") @RequestParam(value = "siteName", required = false) String siteName,
								  @ApiParam("视频id") @RequestParam(value = "sid", required = false) String sid) throws Exception{
		Map<String, String> insertParam = new HashMap<>();
		insertParam.put("video_id",sid);
		String result = null;
		Object url = null;
		//去获得原链接
		//返回原链接
		if(siteName!=null&&"抖音".equals(siteName)){
			result = HttpUtil.doPost(douyinUrl, insertParam, "utf-8");
			url = VideoRedisUtil.getOneDataForString(videoPlaydyPrefix+sid);
		}
		if(siteName!=null&&"快手".equals(siteName)){
			result = HttpUtil.doPost(kuaishouUrl, insertParam, "utf-8");
			url = VideoRedisUtil.getOneDataForString(videoPlayksPrefix+sid);
		}
		log.info(result);
		return url;
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
	public Object hasAlert(@ApiParam("文章sid/hkey/mid") @RequestParam("sid") String sid) throws TRSException {
//          List<AlertEntity> alert = AlertUtil.getAlerts(UserUtils.getUser().getId(),sid,alertNetinsightUrl);
		User user = UserUtils.getUser();
		String userId = user.getId();
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder.filterField(FtsFieldConst.FIELD_USER_ID,userId,Operator.Equal);
		queryBuilder.setPageNo(0);
		queryBuilder.setPageSize(1);
		queryBuilder.setDatabase(Const.ALERT);
		queryBuilder.filterField(FtsFieldConst.FIELD_SEND_RECEIVE,"send",Operator.Equal);
		queryBuilder.filterField(FtsFieldConst.FIELD_SID,sid,Operator.Equal);
		PagedList<FtsDocumentAlert> ftsDocumentAlertPagedList = hybase8SearchServiceNew.ftsAlertList(queryBuilder, FtsDocumentAlert.class);
		Map<String,Boolean> map = new HashMap<>();
		if (ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList) && ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList.getPageItems())) {
			List<FtsDocumentAlert> pageItems = ftsDocumentAlertPagedList.getPageItems();
			if (ObjectUtil.isNotEmpty(pageItems)){
				map.put("send", true);
			}else {
				map.put("send", false);
			}
		}else {
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
		if (ObjectUtil.isNotEmpty(infoListResult)) {
			List<StatusUser> statusUsers = infoListResult.getPageItems();
			if (ObjectUtil.isEmpty(statusUsers)) {
				QueryBuilder queryStatusUser1 = new QueryBuilder();
				queryStatusUser1.filterField(FtsFieldConst.FIELD_UID, "\"" + uid + "\"", Operator.Equal);
				queryStatusUser1.setDatabase(Const.SINAUSERS);
				//查询微博用户信息
//			statusUsers = hybase8SearchService.ftsQuery(queryStatusUser1, StatusUser.class, false, false,false,null);
				PagedList<StatusUser> infoListResult1 = commonListService.queryPageListForClass(queryStatusUser1, StatusUser.class, false, false, false, null);
				statusUsers = infoListResult1.getPageItems();
			}
			if (ObjectUtil.isNotEmpty(statusUsers)) {
				//放入该条微博对应的 发布人信息
				return statusUsers.get(0);
			}
		}
		return null;

	}
//	加入已读 标 针对用户
	private void readArticle(FtsDocumentCommonVO ftsDocumentCommonVO) throws com.trs.hybase.client.TRSException,TRSException{
		User user = UserUtils.getUser();
		//     该功能针对 机构管理员 或 普通用户
		if (ObjectUtil.isNotEmpty(user) && (UserUtils.isRoleAdmin() || UserUtils.isRoleOrdinary(user))){
			Organization organization = organizationRepository.findOne(user.getOrganizationId());
			if (ObjectUtil.isNotEmpty(organization) && organization.isExclusiveHybase()){
				//			有小库情况下  才能使用已读标
				if (ObjectUtil.isNotEmpty(ftsDocumentCommonVO)){
					String groupName = ftsDocumentCommonVO.getGroupName();
					String userId = user.getId();

					TRSInputRecord trsInputRecord = new TRSInputRecord();
					trsInputRecord.setUid(ftsDocumentCommonVO.getSysUid());
					String read = ftsDocumentCommonVO.getRead();
					if (StringUtils.isNotBlank(read)) {
						String[] split = read.split(";");
						List<String> asList = Arrays.asList(split);
						if (!asList.contains(userId)) {
							asList = new ArrayList<>(asList);
							asList.add(userId);
						}
						String join = String.join(";", asList);
						trsInputRecord.addColumn(FtsFieldConst.FIELD_READ, join);
					} else {
						trsInputRecord.addColumn(FtsFieldConst.FIELD_READ, userId);
					}

					if (StringUtil.isNotEmpty(user.getOrganizationId())){
						HybaseShard trsHybaseShard = hybaseShardService.findByOrganizationId(user.getOrganizationId());
						if(ObjectUtil.isNotEmpty(trsHybaseShard)){

							String database = trsHybaseShard.getTradition();
							if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
								database = trsHybaseShard.getWeiBo();
							} else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
								database = trsHybaseShard.getWeiXin();
							} else if (Const.MEDIA_TYPE_TF.contains(groupName)) {
								database = trsHybaseShard.getOverseas();
							} else if (Const.MEDIA_TYPE_VIDEO.contains(groupName)){
								database = trsHybaseShard.getVideo();
							}
							hybase8SearchService.updateRecords(database, trsInputRecord);
						}
					}
				}
			}
		}

	}

	public static void main(String[] args) {
		Map<String, String> insertParam = new HashMap<>();
		insertParam.put("video_id","6893667536206875912");
		String result = HttpUtil.doPost("http://119.254.92.53:39000/api/v1/douyin/video",insertParam,"utf-8");
		String url = VideoRedisUtil.getOneDataForList("DYVideo:mp4_urls:"+"6893667536206875912").toString();
		System.out.println(url);
	}
}
