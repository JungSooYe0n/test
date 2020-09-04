
package com.trs.netInsight.widget.analysis.controller;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.NullException;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.entity.*;
import com.trs.netInsight.widget.analysis.enums.ChartType;
import com.trs.netInsight.widget.analysis.enums.Top5Tab;
import com.trs.netInsight.widget.analysis.service.IChartAnalyzeService;
import com.trs.netInsight.widget.base.enums.ESGroupName;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 图标分析模块 Controller
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @author songbinbin 2017年5月2日
 */

@Slf4j
@RestController
@RequestMapping("/analysis/chart")
@Api(description = "专项检测图表分析接口")
public class ChartAnalyzeController {

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private SpecialProjectRepository specialProjectNewRepository;

	@Autowired
	private SpecialSubjectRepository specialSubjectRepository;

	@Autowired
	private IChartAnalyzeService chartAnalyzeService;

	/**
	 * 网站统计
	 *
	 * @param timeRange
	 *            时间类型 今天，24小时，几天
	 * @param specialId
	 *            专项id
	 * @param area
	 *            检索开始时间
	 * @param industry
	 *            检索结束时间
	 * @return
	 * @throws TRSException
	 * @author mawen 2017年12月2日 Object
	 */
	@EnableRedis
	@FormatResult
	@RequestMapping(value = "/webCount", method = RequestMethod.GET)
	@ApiOperation("网站统计")
	public Object webCountnew(@RequestParam(value = "timeRange", defaultValue = "1d") String timeRange,
							  @RequestParam(value = "specialId", required = true) String specialId,
							  @RequestParam(value = "area", defaultValue = "ALL") String area,
							  @RequestParam(value = "industry", defaultValue = "ALL") String industry) throws TRSException {
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		long start = new Date().getTime();
		Map<String, Object> resultMap = chartAnalyzeService.getWebCountNew(timeRange, specialProject, area, industry);
		long end = new Date().getTime();
		long time = end - start;
		log.info("网站统计后台所需时间" + time);
		SpecialParam specParam = getSpecParam(specialProject);
		ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area, ChartType.WEBCOUNT.getType(),
				specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "");
		Map<String, Object> map = new HashMap<>();
		map.put("data", resultMap);
		map.put("param", chartParam);
		return map;
	}

	/**
	 * 地域分布
	 *
	 * @param special_id
	 * @return
	 * @throws TRSException
	 * @author songbinbin 2017年5月4日 Object
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("地域分布")
	@RequestMapping(value = "/area", method = RequestMethod.GET)
	public Object area(@RequestParam(value = "special_id", required = true) String special_id,
					   @RequestParam(value = "area", defaultValue = "ALL") String area,
					   @RequestParam(value = "time_range", defaultValue = "1d") String timeRange,
					   @RequestParam(value = "industry", defaultValue = "ALL") String industry) throws TRSException {
		long start = new Date().getTime();
		SpecialProject specialProject = specialProjectNewRepository.findOne(special_id);
		//单一媒体排重
		boolean isSimilar = specialProject.isSimilar();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
		if (!"ALL".equals(industry)) {
			searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(area)) {
			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			searchBuilder.filterByTRSL("CATALOG_AREA:" + contentArea);
		}
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		List<Map<String, Object>> resultMap = chartAnalyzeService.getAreaCount(searchBuilder, timeArray, isSimilar,irSimflag,irSimflagAll);
		List<Map<String, Object>> sortByValue = MapUtil.sortByValue(resultMap, "area_count");
		long end = new Date().getTime();
		long time = end - start;
		log.info("地域分布后台所需时间" + time);
		SpecialParam specParam = getSpecParam(specialProject);
		ChartParam chartParam = new ChartParam(special_id, timeRange, industry, area, ChartType.AREA.getType(),
				specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "");
		Map<String, Object> map = new HashMap<>();
		map.put("data", sortByValue);
		map.put("param", chartParam);
		return map;
	}

	/**
	 * 用户参与趋势图
	 *
	 * @return
	 * @throws TRSException
	 * @author songbinbin 2017年5月4日 Object
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("用户参与趋势图")
	@RequestMapping(value = "/userTendency", method = RequestMethod.GET)
	public Object userTendency(@ApiParam("时间区间") @RequestParam(value = "timeRange") String timerange,
							   @ApiParam("专项id") @RequestParam(value = "specialId", required = true) String specialId,
							   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
							   @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry)
			throws TRSException {
		long start = new Date().getTime();
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		QueryBuilder topicSearchBuilder = new QueryBuilder();
		if (!"ALL".equals(industry)) {
			topicSearchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(area)) {
			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			topicSearchBuilder.filterByTRSL("CATALOG_AREA:" + contentArea);
		}
		Map<String, Object> userTendencyNew2 = chartAnalyzeService.getTendencyNew2(topicSearchBuilder.asTRSL(),
				specialProject, "user", timerange, "");
		long end = new Date().getTime();
		long time = end - start;
		log.info("用户参与趋势图后台所需时间" + time);
		SpecialParam specParam = getSpecParam(specialProject);
		ChartParam chartParam = new ChartParam(specialId, timerange, industry, area, ChartType.USERTENDENCY.getType(),
				specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "");
		Map<String, Object> map = new HashMap<>();
		map.put("data", userTendencyNew2);
		map.put("param", chartParam);
		return map;
	}

	/**
	 * 词云分析
	 *
	 * @param specialId
	 *            专题ID
	 * @param timeRange
	 *            时间范围（eg:24h,1d,7d,1m 或 2016-05-26 22:00:00;2016-05-27
	 *            04:00:00）
	 * @param entityType
	 *            实体类型（通用：keywords；人名:people；地名:location；机构名:agency）
	 * @param articleType
	 *            文章类型（全部：all；新闻：news；微博：weibo；微信：weixin；客户端：app）
	 * @return Object
	 * @since mawen 2018/01/09
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("词云分析")
	@RequestMapping(value = "/wordCloud", method = RequestMethod.GET)
	public Object getWordYun(@ApiParam("专题ID") @RequestParam("specialId") String specialId,
							 @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
							 @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							 @ApiParam("时间区间") @RequestParam(value = "timeRange", defaultValue = "1d") String timeRange,
							 @ApiParam("实体类型（通用：keywords；人名:people；地名:location；机构名:agency）") @RequestParam(value = "entityType", defaultValue = "keywords") String entityType,
							 @ApiParam("文章类型（全部：all；新闻：news；微博：weibo；微信：weixin；客户端：app）") @RequestParam(value = "articleType", defaultValue = "all") String articleType)
			throws Exception {
		long start = new Date().getTime();
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		ObjectUtil.assertNull(specialProject, "专题ID");
		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
		// simflag排重1000
		boolean sim = specialProject.isSimilar();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		searchBuilder.setPageSize(50);
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		searchBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
		if (!"ALL".equals(industry)) {
			searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(area)) {
			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			searchBuilder.filterByTRSL(FtsFieldConst.CATALOG_AREANEW + ":" + contentArea);
		}
		if (!"all".equals(articleType)) {
			// 新闻里边排除贴吧
			if ("news".equals(articleType)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 ").append(" NOT ")
						.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
				searchBuilder.filterByTRSL(trsl);
			} else if (!StringUtils.equals("weixin", articleType) && !StringUtils.equals("weibo", articleType)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.PARAM_MAPPING.get(articleType),
						Operator.Equal);
			}
		}
		if (StringUtils.equals("weibo", articleType)) {
			searchBuilder.setDatabase(Const.WEIBO);
		} else if (StringUtils.equals("weixin", articleType)) {
			searchBuilder.setDatabase(Const.WECHAT);
		} else {
			searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		}
		Object wordCloud = chartAnalyzeService.getWordCloud(specialProject.isServer(), searchBuilder.asTRSL(), sim,
				irSimflag,irSimflagAll, entityType, searchBuilder.getPageSize(),"special", searchBuilder.getDatabase());

		long end = new Date().getTime();
		long time = end - start;
		log.info("词云分析后台所需时间" + time);
		SpecialParam specParam = getSpecParam(specialProject);
		ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area, ChartType.WORDCLOUD.getType(),
				specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "");
		Map<String, Object> map = new HashMap<>();
		map.put("data", wordCloud);
		map.put("param", chartParam);
		return map;
	}

	/**
	 * 等这个写好 就把老的干掉 根据地域 机构 人名分类统计 取md5最大的那个 这是三个球 然后每个打球旁边最多有5个小球
	 * 通过md5的量前端去区分大小
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("未知探索")
	@RequestMapping(value = "/getExplore", method = RequestMethod.GET)
	public Object getExploration(@ApiParam("专题ID") @RequestParam(value = "specialId") String specialId,
								 @ApiParam("时间范围") @RequestParam(value = "timeRange", defaultValue = "1d") String timeRange,
								 @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
								 @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry) throws Exception {
		long start = new Date().getTime();
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		ObjectUtil.assertNull(specialProject, "专题ID");
		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
		boolean sim = specialProject.isSimilar();
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
		if (!"ALL".equals(industry)) {
			searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(area)) {
			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			searchBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
		}
		// 第一个带数字的list
		List<Map<String, String>> listOne = new ArrayList<>();
		// 第二个带 只装source target的
		List<Map<String, String>> listTwo = new ArrayList<>();
		// 装最大的三个球
		List<Map<String, String>> listHead = new ArrayList<>();
		/// 一个大球旁边五个 也就是每次分类统计六个
		// 地域
		Map<String, String> mapArea = new HashMap<>();
		GroupResult areaTop6 = hybase8SearchService.categoryQuery(searchBuilder.isServer(), searchBuilder.asTRSL(), sim,
				irSimflag,irSimflagAll, FtsFieldConst.CATALOG_AREANEW, 6, Const.HYBASE_NI_INDEX);
		Map<String, String> areaMap = new HashMap<>();
		String[] areaOne = areaTop6.getFieldValue(0).split("\\\\");
		String areaNameHead = areaOne[areaOne.length - 1];
		for (GroupInfo info : areaTop6) {
			String[] array = info.getFieldValue().split("\\\\");
			String value = array[array.length - 1];
			areaMap.put(value, String.valueOf(info.getCount()));
		}
		// 不知道是不是取第一个值
		// String areaNameHead=areaMap.keySet().iterator().next();
		// String areaNameHead=areaMap.get;
		mapArea.put("name", areaNameHead);
		listHead.add(mapArea);
		Iterator<String> iterator = areaMap.keySet().iterator();
		while (iterator.hasNext()) {
			String next = iterator.next();
			String count = areaMap.get(next);
			Map<String, String> map = new HashMap<>();
			map.put("name", next);
			map.put("value", count);
			map.put("symbolSize", count);
			map.put("category", areaNameHead);
			listOne.add(map);
			Map<String, String> mapSource = new HashMap<>();
			mapSource.put("source", areaNameHead);
			mapSource.put("target", next);
			listTwo.add(mapSource);
		}
		// 机构
		Map<String, String> mapAgency = new HashMap<>();
		GroupResult agencyTop6 = hybase8SearchService.categoryQuery(searchBuilder.isServer(), searchBuilder.asTRSL(),
				sim, irSimflag,irSimflagAll, FtsFieldConst.FIELD_AGENCY, 6, Const.HYBASE_NI_INDEX);
		// 每个最大点的名字作为爸爸
		mapAgency.put("name", agencyTop6.getFieldValue(0));
		listHead.add(mapAgency);
		for (GroupInfo info : agencyTop6) {
			Map<String, String> map = new HashMap<>();
			map.put("name", info.getFieldValue());
			map.put("value", String.valueOf(info.getCount()));
			map.put("symbolSize", String.valueOf(info.getCount()));
			map.put("category", agencyTop6.getFieldValue(0));
			listOne.add(map);
			Map<String, String> mapSource = new HashMap<>();
			mapSource.put("source", agencyTop6.getFieldValue(0));
			mapSource.put("target", info.getFieldValue());
			listTwo.add(mapSource);
		}
		// 人名
		Map<String, String> mapPeople = new HashMap<>();
		GroupResult peopleTop6 = hybase8SearchService.categoryQuery(searchBuilder.isServer(), searchBuilder.asTRSL(),
				sim, irSimflag,irSimflagAll, FtsFieldConst.FIELD_PEOPLE, 6, Const.HYBASE_NI_INDEX);
		// String[] peopleNameArray=peopleTop6.getFieldValue(0).split(";");
		// String peopleName=peopleNameArray[peopleNameArray.length-1];
		mapPeople.put("name", peopleTop6.getFieldValue(0));
		listHead.add(mapPeople);
		for (GroupInfo info : peopleTop6) {
			Map<String, String> map = new HashMap<>();
			// String[] name=info.getFieldValue().split(";");
			map.put("name", info.getFieldValue());
			map.put("value", String.valueOf(info.getCount()));
			map.put("symbolSize", String.valueOf(info.getCount()));
			map.put("category", peopleTop6.getFieldValue(0));
			listOne.add(map);
			Map<String, String> mapSource = new HashMap<>();
			mapSource.put("source", peopleTop6.getFieldValue(0));
			mapSource.put("target", info.getFieldValue());
			listTwo.add(mapSource);
		}
		Map<String, List<Map<String, String>>> mapAll = new HashMap<>();
		mapAll.put("categories", listHead);
		// Map<String,List<Map<String,String>>> mapListOne=new HashMap<>();
		mapAll.put("data_Data", listOne);
		mapAll.put("data_links", listTwo);
		// listAll.add(mapHead);
		long end = new Date().getTime();
		long time = end - start;
		log.info("未知探索后台所需时间" + time);
		return mapAll;
	}

	/**
	 * 未知探索 1. 根据专题参数获取排名前10的 MD5List 2. 遍历 MD5List -> 获取该MD5下的最热的10个
	 * keyword(k0) -> 获取该MD5下的100 篇文章 -> 遍历每篇文章： -> 取该文章的 keyword(k1) -> 如果 k1
	 * 包含 k0 中多个,将被包含的 k0 连线
	 *
	 * @param specialId
	 *            专题ID
	 * @param timeRange
	 *            时间范围（eg:24h,1d,7d,1m 或 2016-05-26 22:00:00;2016-05-27
	 *            04:00:00）
	 * @since leeyao @ 2017/05/11
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("未知探索")
	@RequestMapping(value = "/explore", method = RequestMethod.GET)
	public Object getUnknownExploration(@ApiParam("专题ID") @RequestParam(value = "specialId") String specialId,
										@ApiParam("时间范围") @RequestParam(value = "timeRange", defaultValue = "1d") String timeRange,
										@ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
										@ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry) throws Exception {
		long start = new Date().getTime();
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		ObjectUtil.assertNull(specialProject, "专题ID");
		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
		boolean sim = specialProject.isSimilar();
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
		if (!"ALL".equals(industry)) {
			searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(area)) {
			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			searchBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
		}

		// 6个网
		GroupResult md5Top10List = hybase8SearchService.categoryQuery(searchBuilder.isServer(), searchBuilder.asTRSL(),
				sim, irSimflag,irSimflagAll, FtsFieldConst.FIELD_MD5TAG, 6, Const.HYBASE_NI_INDEX);
		ObjectUtil.assertNull(md5Top10List, "未知探索md5");
		// 构造返回结果集合
		List<Object> resultList = new ArrayList<>();
		AtomicInteger count = new AtomicInteger(); // 计数，原子操作
		for (GroupInfo categoryInfo : md5Top10List) {
			Map<String, Object> nodeMap = new HashMap<>();
			// 根据category 涂色
			nodeMap.put("category", categoryInfo.getFieldValue());
			QueryBuilder md5Builder = new QueryBuilder();
			String trsl = searchBuilder.asTRSL() + " AND MD5TAG:" + categoryInfo.getFieldValue();
			md5Builder.filterByTRSL(trsl);
			md5Builder.setPageSize(100);
			// 词频率统计，每个分类取12个词，去除首尾偏移词汇，默认最多只展示10个词
			log.info(md5Builder.asTRSL());
			GroupResult md5Category = hybase8SearchService.categoryQuery(md5Builder.isServer(), md5Builder.asTRSL(),
					sim, irSimflag,irSimflagAll, ESFieldConst.IR_KEYWORDS, 12, Const.HYBASE_NI_INDEX);

			// 封装每个分类下的实体
			List<UnknownBean> unknownBeanList = new ArrayList<>();
			Iterator<GroupInfo> iterator = md5Category.iterator();
			while (iterator.hasNext()) {
				GroupInfo next = iterator.next();
				if (next.getFieldValue().length() < 2) {
					continue;
				}
				UnknownBean unknownBean = new UnknownBean();
				unknownBean.setName(next.getFieldValue());
				unknownBean.setLinkId(count.incrementAndGet());
				unknownBean.setDfValue((int) next.getCount());
				unknownBeanList.add(unknownBean);
			}
			nodeMap.put("nodes", unknownBeanList);
			List<Map<String, Integer>> linksList = new ArrayList<>();
			// List<Map<String, String>> linksList = new ArrayList<>();
			// 连线分析
			// List<ChartAnalyzeEntity> analyzeEntities =
			// analyzeRepository.pageList(md5Builder).getPageItems();
			log.info(md5Builder.asTRSL());
			List<ChartAnalyzeEntity> analyzeEntities = hybase8SearchService.ftsQuery(md5Builder,
					ChartAnalyzeEntity.class, sim, irSimflag,irSimflagAll ,"special");
			if (analyzeEntities != null && analyzeEntities.size() > 0) {
				for (int i = 0; i < unknownBeanList.size() - 1; i++) {
					for (ChartAnalyzeEntity entity : analyzeEntities) {
						List<String> keywords = entity.getKeywords();
						if (keywords == null || keywords.size() == 0) {
							continue;
						}
						Map<String, Integer> linkNode = new HashMap<>();
						// 同一文档中出现的两个实体才构成连线
						if (keywords.contains(unknownBeanList.get(i).getName())
								&& keywords.contains(unknownBeanList.get(i + 1).getName())) {
							linkNode.put("source", unknownBeanList.get(i).getLinkId());
							linkNode.put("target", unknownBeanList.get(i + 1).getLinkId());
							linksList.add(linkNode);
							break;
						}
					}
				}
			}
			nodeMap.put("links", linksList);
			resultList.add(nodeMap);
		}
		long end = new Date().getTime();
		long time = end - start;
		log.info("未知探索后台所需时间" + time);
		return resultList;
	}

	/**
	 * 渠道偏好饼图
	 *
	 * @param specialId
	 *            专项ID
	 * @param area
	 *            地域
	 * @param industry
	 *            行业
	 * @param timeRange
	 *            时间范围
	 * @return Object
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("渠道偏好饼图")
	@RequestMapping(value = "/via", method = RequestMethod.GET)
	public Object getViaPreference(@RequestParam("specialId") String specialId,
								   @RequestParam(value = "area", defaultValue = "ALL") String area,
								   @RequestParam(value = "industry", defaultValue = "ALL") String industry,
								   @RequestParam(value = "timeRange", defaultValue = "0d") String timeRange) throws TRSException {
		long start = new Date().getTime();
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			QueryBuilder builder = specialProject.toNoPagedAndTimeBuilderWeiBo();
			boolean sim = specialProject.isSimilar();
			log.info(builder.asTRSL());
			builder.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(timeRange), Operator.Between);
			if (!"ALL".equals(area)) {
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, area.split(";"), Operator.Equal);
			}
			log.info("11111111" + builder.asTRSL());
			Object viaPreference = chartAnalyzeService.viaPreference(builder, sim, irSimflag,irSimflagAll );
			long end = new Date().getTime();
			long time = end - start;
			log.info("渠道偏好饼图后台所需时间" + time);
			SpecialParam specParam = getSpecParam(specialProject);
			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area, ChartType.VIA.getType(),
					specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "");
			Map<String, Object> map = new HashMap<>();
			map.put("data", viaPreference);
			map.put("param", chartParam);
			return map;
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("获取渠道偏好图出错，message:" + e);
		}
	}

	/**
	 * 媒体活跃等级图
	 *
	 * @param specialId
	 *            专项ID
	 * @param area
	 *            地域
	 * @param industry
	 *            行业
	 * @param timeRange
	 *            时间范围
	 * @return Object
	 */
	@FormatResult
	@ApiOperation("媒体活跃等级图")
	@RequestMapping(value = "/active_level", method = RequestMethod.GET)
	public Object getActiveLevel(@RequestParam("special_id") String specialId,
								 @RequestParam(value = "area", defaultValue = "ALL") String area,
								 @RequestParam(value = "industry", defaultValue = "ALL") String industry,
								 @RequestParam(value = "time_range", defaultValue = "3d") String timeRange) throws TRSException {
		long start = new Date().getTime();
		// long id = Thread.currentThread().getId();
		// LogPrintUtil loginpool = new LogPrintUtil();
		// RedisUtil.setLog(id, loginpool);
		// log.debug(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			QueryBuilder builder = specialProject.toNoPagedAndTimeBuilder();
			boolean sim = specialProject.isSimilar();
			if (!"ALL".equals(industry)) {
				builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
			}
			if (!"ALL".equals(area)) {
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterByTRSL("CATALOG_AREA:" + contentArea);
			}

			String[] range = DateUtil.formatTimeRange(timeRange);
			Object mediaActiveLevel = chartAnalyzeService.mediaActiveLevel(builder,"", range, sim, irSimflag,irSimflagAll);
			long end = new Date().getTime();
			long time = end - start;
			log.info("媒体活跃等级图后台所需时间" + time);
			SpecialParam specParam = getSpecParam(specialProject);
			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area,
					ChartType.ACTIVE_LEVEL.getType(), specParam.getFirstName(), specParam.getSecondName(),
					specParam.getThirdName(), "");
			Map<String, Object> map = new HashMap<>();
			map.put("data", mediaActiveLevel);
			map.put("param", chartParam);
			return map;
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("获取媒体活跃等级出错，message:" + e);
		}
		// inally {
		// long end = new Date().getTime();
		// int timeApi = (int) (end - start);
		// loginpool.setComeBak(start);
		// loginpool.setFinishBak(end);
		// loginpool.setFullBak(timeApi);
		// if(loginpool.getFullHybase()>FtsFieldConst.OVER_TIME){
		// loginpool.printTime(LogPrintUtil.INFO_LIST);
		// }
		// log.info("调用接口用了" + timeApi + "ms");
		// }
	}

	/**
	 * 微博top5
	 *
	 * @param specialId
	 *            专项ID
	 * @param area
	 *            地域
	 * @param sortType
	 *            排序方式
	 * @param industry
	 *            行业
	 * @param timeRange
	 *            时间范围
	 * @return Object
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("微博top5")
	@RequestMapping(value = "/top5", method = RequestMethod.GET)
	public Object top5(@ApiParam("专项ID") @RequestParam("specialId") String specialId,
					   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
					   @ApiParam("排序方式") @RequestParam(value = "sortType", defaultValue = "NEWEST") String sortType,
					   @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
					   @ApiParam("时间范围") @RequestParam(value = "timeRange", defaultValue = "0d") String timeRange)
			throws TRSException {
		try {
			long start = new Date().getTime();
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			QueryBuilder builder = specialProject.toNoPagedAndTimeBuilderWeiBo();
			boolean sim = specialProject.isSimilar();
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			builder.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(timeRange), Operator.Between);
			if (!"ALL".equals(industry)) {
				builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
			}
			if (!"ALL".equals(area)) {
				builder.filterField(ESFieldConst.CATALOG_AREA, area.split(";"), Operator.Equal);
			}
			List<MBlogAnalyzeEntity> mBlogTop5 = chartAnalyzeService.mBlogTop5(builder, Top5Tab.valueOf(sortType), sim,
					irSimflag,irSimflagAll);
			long end = new Date().getTime();
			long time = end - start;
			log.info("微博top5后台所需时间" + time);
			return mBlogTop5;
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("微博top5出错，message:" + e);
		}
	}

	/**
	 * 专题监测统计表格
	 *
	 * @param specialId
	 *            专项id
	 * @param days
	 *            时间
	 * @param industryType
	 *            行业
	 * @param area
	 *            地域
	 * @author mawen 2017年12月2日 Object
	 *
	 */
	//@EnableRedis(cacheMinutes = 5) //加快专题列表响应速度，暂时设置缓存5分钟
	@FormatResult
	@ApiOperation("专题监测统计表格")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialId", value = "专项ID", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "days", value = "时间", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "industryType", value = "行业类型", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "area", value = "内容地域", dataType = "String", paramType = "query", required = false) })
	@RequestMapping(value = "/stattotal", method = RequestMethod.GET)
	public Object getStatTotal(@ApiParam(value = "专项ID") @RequestParam(value = "specialId") String specialId,
							   @ApiParam(value = "时间 1. [0-9]*[hdwmy] \n 2.yyyy-MM-dd HH:mm:ss") @RequestParam(value = "days", required = false) String days,
							   @ApiParam(value = "行业类型") @RequestParam(value = "industryType", defaultValue = "ALL", required = false) String industryType,
							   @ApiParam(value = "内容地域") @RequestParam(value = "area", required = false) String area,
							   @RequestParam(value = "foreign", required = false) String foreign) throws TRSException {
		log.warn("图表分析统计表格stattotal接口开始调用");
		long start = new Date().getTime();
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(days)) {
				days = specialProject.getTimeRange();
				if (StringUtils.isBlank(days)) {
					days = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					days += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeRange = DateUtil.formatTimeRange(days);
			//List<ClassInfo> statToday = new ArrayList<>();
			Object total = chartAnalyzeService.stattotal(specialProject, timeRange[0], timeRange[1],
					industryType, area, foreign);
			//Map<String, Object> putValue = MapUtil.putValue(new String[] { "今日", "总量" }, statToday, total);
			long end = new Date().getTime();
			long time = end - start;
			log.info("专题监测统计表格后台所需时间" + time);
			return total;
		} catch (Exception e) {
			throw new OperationException("专题监测统计表格错误,message: " + e, e);
		}
	}

	/**
	 * 普通搜索统计表格
	 *
	 * @param days
	 *            时间
	 * @param keywords
	 *            关键字
	 * @author zhangya 2019年9月26日 Object
	 *
	 */
	//@EnableRedis(cacheMinutes = 5) //加快列表响应速度，暂时设置缓存5分钟  为了删除列表数据后统计的正常显示，去掉
	@FormatResult
	@ApiOperation("普通搜索统计表格")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "days", value = "时间", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keywords", value = "关键字", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "searchType", value = "查询类型：精准precise、模糊fuzzy", dataType = "String", paramType = "query", required = false) })
	@RequestMapping(value = "/ordinarySearchstatistics", method = RequestMethod.GET)
	public Object getOrdinarySearchstatistics(
			@ApiParam(value = "时间 1. [0-9]*[hdwmy] \n 2.yyyy-MM-dd HH:mm:ss") @RequestParam(value = "days", defaultValue = "7d") String days,
			@ApiParam("关键词") @RequestParam(value = "keywords", required = false) String keywords,
			@ApiParam("查询类型：精准precise、模糊fuzzy") @RequestParam(value = "searchType",defaultValue = "fuzzy",required = false) String searchType) throws TRSException {
		log.warn("普通搜索数据统计ordinarySearchstatistics接口开始调用");
		if (StringUtils.isBlank(keywords)) {
			throw new TRSException("关键词不能为空！", CodeUtils.FAIL);
		}

		long start = new Date().getTime();
		try {
			String source = "";
			User user = UserUtils.getUser();
			if(user != null && user.getId() != null){
				if ( !UserUtils.ROLE_PLATFORM_SUPER_LIST.contains(user.getCheckRole())){
					source = UserUtils.checkOrganization(user).getDataSources().replaceAll(",",";");
				}
			}

			if(StringUtil.isEmpty(source)){
				source = "ALL";
			}
			if(source.contains("境外媒体") || source.contains("境外网站")){
				source = source.replaceAll("境外媒体",Const.GROUPNAME_GUOWAIXINWEN).replaceAll("境外网站",Const.GROUPNAME_GUOWAIXINWEN);
			}
			if(StringUtil.isEmpty(searchType.trim())){
				searchType = "fuzzy";
			}
			if(StringUtil.isEmpty(days)){
				days = "7d";
			}
			String[] timeRange = DateUtil.formatTimeRange(days);
			keywords = keywords.trim();
			List<ClassInfo> statToday = new ArrayList<>();
			//普通搜索改造，统计采用站内排重，列表采用全网排重
			List<ClassInfo> total = chartAnalyzeService.ordinarySearchstatistics(false, true,false,keywords, timeRange,source, "positioCon",true,searchType);
			Map<String, Object> putValue = MapUtil.putValue(new String[] { "data" }, total);
			long end = new Date().getTime();
			long time = end - start;
			log.info("普通搜索统计表格后台所需时间" + time);
			return putValue;
		} catch (Exception e) {
			throw new OperationException("普通搜索统计表格错误,message: " + e, e);
		}
	}

	/**
	 * 事件走势、微博走势.综合走势
	 *
	 * @param specialId
	 *            专项id
	 * @param type
	 *            类型
	 * @param days
	 *            时间
	 * @param industryType
	 *            行业
	 * @param area
	 *            地域
	 */
	@SuppressWarnings("rawtypes")
	@EnableRedis
	@FormatResult
	@ApiOperation("事件走势、微博走势.综合走势")
	@RequestMapping(value = "/trend", method = RequestMethod.GET)
	public Object getTrend(@ApiParam("专项id") @RequestParam(value = "specialId") String specialId,
						   @ApiParam("") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
						   @ApiParam("类型") @RequestParam(value = "type", required = false) String type,
						   @ApiParam("时间") @RequestParam(value = "days", defaultValue = "3d") String days,
						   @ApiParam("行业") @RequestParam(value = "industryType", defaultValue = "ALL") String industryType,
						   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws TRSException {
		try {
			String[] timeRange = DateUtil.formatTimeRange(days);
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			// simflag排重1000
			boolean sim = specialProject.isSimilar();
			QueryBuilder statBuilder = null;
			if ("weibo".equals(type)) {
				statBuilder = specialProject.toNoTimeBuilderWeiBo(0, pageSize);
			} else {
				statBuilder = specialProject.toNoTimeBuilder(0, pageSize);
			}
			statBuilder.page(0, pageSize);
			if (!"ALL".equals(industryType)) {
				statBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
			}
			if (!"ALL".equals(area)) {
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				statBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			}
			List<Map> resultList = new ArrayList<>();
			switch (type) {
				case "weibo":
					statBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT, timeRange, Operator.Between);
					statBuilder.setPageSize(10);
					statBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
					log.info(statBuilder.asTRSL());
					List<FtsDocumentStatus> ftsQueryWeiBo = hybase8SearchService.ftsQuery(statBuilder,
							FtsDocumentStatus.class, sim, irSimflag,irSimflagAll,"special" );
					for (FtsDocumentStatus ftsStatus : ftsQueryWeiBo) {
						String md5 = ftsStatus.getMd5Tag();
						QueryBuilder builder = new QueryBuilder();
						builder.setDatabase(Const.WEIBO);
						builder.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
						long ftsCount = hybase8SearchService.ftsCount(builder, sim, irSimflag,irSimflagAll ,"special");
						if (ftsCount == 1L) {
							ftsCount = 0L;
						}
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsCount, ftsStatus));
					}
					return resultList;
				case "tradition":
					String trsl = FtsFieldConst.FIELD_GROUPNAME
							+ ":((国内新闻) OR (国内论坛) OR (国内新闻_手机客户端) OR (国内新闻_电子报) OR (国内博客))";
					statBuilder.filterByTRSL(trsl);
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
					statBuilder.setPageSize(10);
					statBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(statBuilder, FtsDocument.class, true,
							irSimflag,irSimflagAll ,"special");
					for (FtsDocument ftsDocument : ftsQuery) {
						String md5 = ftsDocument.getMd5Tag();
						QueryBuilder builder = new QueryBuilder();
						builder.setDatabase(Const.HYBASE_NI_INDEX);
						builder.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
						long ftsCount = hybase8SearchService.ftsCount(builder, sim, irSimflag,irSimflagAll,"special");
						if (ftsCount == 1L) {
							ftsCount = 0L;
						}
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsCount, ftsDocument));
					}
					return resultList;
				case "all":
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
				default:
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
			}
			return resultList;
		} catch (Exception e) {
			if (e.getMessage().contains("检索超时")){
				throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_TIMEOUT,
						e);
			}else if (e.getMessage().contains("表达式过长")){
				throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_EXCEPTION,
						e);
			}
			throw new OperationException("走势计算错误,message: " + e);
		}
	}

	@SuppressWarnings("rawtypes")
	@EnableRedis
	@FormatResult
	@ApiOperation("走势最新那条")
	@RequestMapping(value = "/trendTime", method = RequestMethod.GET)
	public Object trendTime(@ApiParam("专项id") @RequestParam(value = "specialId") String specialId,
							@ApiParam("") @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize,
							@ApiParam("类型") @RequestParam(value = "type", required = false) String type,
							@ApiParam("时间") @RequestParam(value = "days", defaultValue = "3d") String days,
							@ApiParam("行业") @RequestParam(value = "industryType", defaultValue = "ALL") String industryType,
							@ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws TRSException {
		long start = new Date().getTime();
		try {
			String[] timeRange = DateUtil.formatTimeRange(days);
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			// simflag排重1000
			boolean sim = specialProject.isSimilar();
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			QueryBuilder statBuilder = null;
			if ("weibo".equals(type)) {
				statBuilder = specialProject.toNoTimeBuilderWeiBo(0, 1);
			} else {
				statBuilder = specialProject.toNoTimeBuilder(0, 1);
			}
			statBuilder.page(0, pageSize);
			if (!"ALL".equals(industryType)) {
				statBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
			}
			if (!"ALL".equals(area)) {
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				statBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			}
			List<Map> resultList = new ArrayList<>();
			final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
			String trslk = "redisKey" + pageId;
			switch (type) {
				case "weibo":
					statBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT, timeRange, Operator.Between);
					statBuilder.page(0, 10);
					statBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					statBuilder.setDatabase(Const.WEIBO);

					RedisUtil.setString(trslk, statBuilder.asTRSL());

					List<FtsDocumentStatus> ftsQueryWeiBo = hybase8SearchService.ftsQuery(statBuilder,
							FtsDocumentStatus.class, sim, irSimflag,irSimflagAll,"special");
					log.info(statBuilder.asTRSL());
					FtsDocumentStatus ftsStatus = null;
					ftsStatus = ftsQueryWeiBo.get(0);
					// 上边过滤完了还是空就按照loadtime查
					if (null == ftsStatus) {
						statBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, false);

						RedisUtil.setString(trslk, statBuilder.asTRSL());

						List<FtsDocumentStatus> ftsQueryWeiBo2 = hybase8SearchService.ftsQuery(statBuilder,
								FtsDocumentStatus.class, sim, irSimflag,irSimflagAll,"special");
						ftsStatus = ftsQueryWeiBo2.get(0);
					}
					// 实在不行再取第一个
					// if(null==ftsStatus){
					// ftsStatus=ftsQueryWeiBo.get(0);
					// }
					String md5 = ftsStatus.getMd5Tag();
					QueryBuilder builder = new QueryBuilder();
					builder.setDatabase(Const.WEIBO);
					builder.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
					long ftsCount = hybase8SearchService.ftsCount(builder, sim, irSimflag,irSimflagAll,"special");
					if (ftsCount == 1L) {
						ftsCount = 0L;
					}
					ftsStatus.setSim((int) ftsCount);
					ftsStatus.setTrslk(trslk);
					return ftsStatus;
				case "tradition":
					String trsl = FtsFieldConst.FIELD_GROUPNAME
							+ ":((国内新闻) OR (国内论坛) OR (国内新闻_手机客户端) OR (国内新闻_电子报) OR (国内博客))";
					statBuilder.filterByTRSL(trsl);
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
					statBuilder.page(0, 10);
					statBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					statBuilder.setDatabase(Const.HYBASE_NI_INDEX);
					RedisUtil.setString(trslk, statBuilder.asTRSL());

					List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(statBuilder, FtsDocument.class, sim,
							irSimflag,irSimflagAll,"special");
					log.info(statBuilder.asTRSL());
					FtsDocument ftsStatus2 = null;
					ftsStatus2 = ftsQuery.get(0);
					if (ftsStatus2 == null) {
						statBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, false);
						RedisUtil.setString(trslk, statBuilder.asTRSL());
						List<FtsDocument> ftsQuery2 = hybase8SearchService.ftsQuery(statBuilder, FtsDocument.class, sim,
								irSimflag,irSimflagAll,"special");
						ftsStatus2 = ftsQuery2.get(0);
					}
					// if(ftsStatus2==null){
					// ftsStatus2=ftsQuery.get(0);
					// }
					// 算相似文章数
					String md52 = ftsStatus2.getMd5Tag();
					QueryBuilder builder2 = new QueryBuilder();
					builder2.setDatabase(Const.HYBASE_NI_INDEX);
					builder2.filterField(FtsFieldConst.FIELD_MD5TAG, md52, Operator.Equal);
					long ftsCount2 = hybase8SearchService.ftsCount(builder2, false, irSimflag,irSimflagAll,"special");
					if (ftsCount2 == 1L) {
						ftsCount2 = 0L;
					}
					ftsStatus2.setSim(ftsCount2);
					ftsStatus2.setTrslk(trslk);
					return ftsStatus2;
				case "all":
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
				default:
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
			}
			long end = new Date().getTime();
			long time = end - start;
			log.info("专题监测统计表格后台所需时间" + time);
			return resultList;
		} catch (Exception e) {
			if (e.getMessage().contains("检索超时")){
				throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_TIMEOUT,
						e);
			}else if (e.getMessage().contains("表达式过长")){
				throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_EXCEPTION,
						e);
			}
			throw new OperationException("走势计算错误,message: " + e);
		}
	}

	@SuppressWarnings("rawtypes")
	@EnableRedis
	@FormatResult
	@ApiOperation("走势后边的九条")
	@RequestMapping(value = "/trendMd5", method = RequestMethod.GET)
	public Object trendMd5(@ApiParam("专项id") @RequestParam(value = "specialId") String specialId,
						   @ApiParam("pageSize") @RequestParam(value = "pageSize", defaultValue = "9") Integer pageSize,
						   @ApiParam("类型") @RequestParam(value = "type", required = false) String type,
						   @ApiParam("时间") @RequestParam(value = "days", defaultValue = "3d") String days,
						   @ApiParam("行业") @RequestParam(value = "industryType", defaultValue = "ALL") String industryType,
						   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws TRSException {
		// 这个不用管它是否排重 肯定查md5的
		try {
			String[] timeRange = DateUtil.formatTimeRange(days);
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			QueryBuilder statBuilder = null;
			if ("weibo".equals(type)) {
				statBuilder = specialProject.toNoTimeBuilderWeiBo(0, pageSize);
			} else {
				statBuilder = specialProject.toNoTimeBuilder(0, pageSize);
			}
			statBuilder.page(0, pageSize);
			if (!"ALL".equals(industryType)) {
				statBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
			}
			if (!"ALL".equals(area)) {
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				statBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			}
			List<Map> resultList = new ArrayList<>();
			switch (type) {
				case "weibo":
					statBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT, timeRange, Operator.Between);
					statBuilder.setPageSize(10);
					statBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					statBuilder.setDatabase(Const.WEIBO);
					log.info(statBuilder.asTRSL());
					// md5分类统计
					GroupResult categoryQuery = hybase8SearchService.categoryQuery(statBuilder, false, irSimflag,irSimflagAll,
							FtsFieldConst.FIELD_MD5TAG, Const.WEIBO);
					// 然后拿着统计出来的md5去查
					List<FtsDocumentStatus> listMd5 = new ArrayList<>();
					for (GroupInfo groupInfo : categoryQuery) {
						QueryBuilder queryMd5 = new QueryBuilder();
						queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
						queryMd5.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
						queryMd5.filterField(FtsFieldConst.FIELD_CREATED_AT, timeRange, Operator.Between);
						queryMd5.setDatabase(Const.WEIBO);
						queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilderWeiBo().asTRSL());
						List<FtsDocumentStatus> ftsQueryWeiBo = hybase8SearchService.ftsQuery(queryMd5,
								FtsDocumentStatus.class, false, irSimflag,irSimflagAll,"special");
						ftsQueryWeiBo.get(0).setSim((int) groupInfo.getCount());
						listMd5.add(ftsQueryWeiBo.get(0));
					}

					SortListWeiBo sortListWeiBo = new SortListWeiBo();
					Collections.sort(listMd5, sortListWeiBo);
					// 防止这个的第一条和时间的那一条重复
					if (listMd5.size() > 0) {
						listMd5.remove(0);
					}
					for (FtsDocumentStatus ftsStatus : listMd5) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsStatus.getSim(), ftsStatus));
					}
					return resultList;
				case "tradition":
					String trsl = FtsFieldConst.FIELD_GROUPNAME
							+ ":((国内新闻) OR (国内论坛) OR (国内新闻_手机客户端) OR (国内新闻_电子报) OR (国内博客))";
					statBuilder.filterByTRSL(trsl);
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
					statBuilder.setPageSize(10);
					statBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					statBuilder.setDatabase(Const.HYBASE_NI_INDEX);
					log.info(statBuilder.asTRSL());
					GroupResult categoryChuan = hybase8SearchService.categoryQuery(statBuilder, false, irSimflag,irSimflagAll,
							FtsFieldConst.FIELD_MD5TAG, Const.HYBASE_NI_INDEX);
					log.info(statBuilder.asTRSL());
					List<FtsDocument> listChuan = new ArrayList<>();
					if (categoryChuan.size() > 0) {
						for (GroupInfo group : categoryChuan) {
							QueryBuilder queryMd5 = new QueryBuilder();
							queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, group.getFieldValue(), Operator.Equal);
							queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
							queryMd5.setDatabase(Const.HYBASE_NI_INDEX);
							queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
							log.info(queryMd5.asTRSL());
							List<FtsDocument> ftsQueryWeiBo = hybase8SearchService.ftsQuery(queryMd5, FtsDocument.class,
									false, irSimflag,irSimflagAll,"special");
							if (ftsQueryWeiBo.size() > 0) {
								ftsQueryWeiBo.get(0).setSim((int) group.getCount());
								listChuan.add(ftsQueryWeiBo.get(0));
							}
						}
					}

					// 按urltime降序
					SortList sort = new SortList();
					Collections.sort(listChuan, sort);
					// 防止这个的第一条和时间的那一条重复
					if (listChuan.size() > 0) {
						listChuan.remove(0);
					}
					for (FtsDocument ftsDocument : listChuan) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsDocument.getSim(), ftsDocument));
					}
					return resultList;
				case "all":
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
				default:
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
			}
			return resultList;
		} catch (Exception e) {
			if (e.getMessage().contains("检索超时")){
				throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_TIMEOUT,
						e);
			}else if (e.getMessage().contains("表达式过长")){
				throw new TRSException("处理controller结果出错,message:" + e, CodeUtils.HYBASE_EXCEPTION,
						e);
			}
			throw new OperationException("走势计算错误,message: " + e);
		}
	}

	/**
	 * 引爆点
	 *
	 * @param specialId
	 *            专项id
	 * @param days
	 *            时间范围
	 * @param industryType
	 *            行业
	 * @param area
	 *            地域
	 *//*
	@EnableRedis
	@FormatResult
	@ApiOperation("引爆点")
	@RequestMapping(value = "/tippingpoint", method = RequestMethod.GET)
	public Object getTippingPoint(@RequestParam(value = "specialId") String specialId,
			@RequestParam(value = "timeRange", required = false) String days,
			@RequestParam(value = "industryType", defaultValue = "ALL") String industryType,
			@RequestParam(value = "area", defaultValue = "ALL") String area) throws TRSException {
		try {
			String[] timeRange = DateUtil.formatTimeRange(days);
			String spreadKey = DateUtil.startOfWeek() + specialId;
			String url = RedisFactory.getValueFromRedis(spreadKey);
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
			boolean sim = specialProject.isSimilar();
			if (!"ALL".equals(industryType)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
			}
			if (!"ALL".equals(area)) {
				searchBuilder.filterField(ESFieldConst.CATALOG_AREA, area.split(";"), Operator.Equal);
			}
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.filterByTRSL(searchBuilder.asTRSL());
			queryBuilder.filterField(ESFieldConst.IR_URLTIME, timeRange, Operator.Between);
			queryBuilder.page(0, 1);
			queryBuilder.orderBy(ESFieldConst.IR_RTTCOUNT, true);

			List<FtsDocumentStatus> list = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentStatus.class, sim,
					irSimflag,irSimflagAll,"special");
			if (list.size() <= 0) {
				throw new NullException("获取url为空");
			}
			// url = list.get(0).baseUrl();
			FtsDocumentStatus status = list.get(0);
			RedisFactory.setValueToRedis(spreadKey, url, 7, TimeUnit.DAYS);
			return chartAnalyzeService.getTippingPoint(searchBuilder, status,
					DateUtil.stringToDate(timeRange[0], DateUtil.yyyyMMddHHmmss),sim, irSimflag,irSimflagAll);
		} catch (NullException n) {
			throw new NullException("获取url为空");
		} catch (Exception e) {
			throw new OperationException("引爆点计算错误,message: " + e);
		}
	}
*/
	/**
	 * 情感分析曲线趋势图
	 *
	 * @param industryType
	 * @param area
	 * @return
	 * @throws TRSException
	 * @author qinbin 情感分析曲线趋势图
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("情感分析曲线趋势图")
	@RequestMapping(value = "/volume", method = RequestMethod.GET)
	public Object getVolumeNew(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
							   @ApiParam("时间区间") @RequestParam(value = "timeRange") String timerange,
							   @ApiParam("行业") @RequestParam(value = "industryType", defaultValue = "ALL") String industryType,
							   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws TRSException {

		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
		boolean sim = specialProject.isSimilar();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		if (!"ALL".equals(industryType)) {
			searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(area)) {
			{
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				searchBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			}
		}
		Map<String, Object> resultMap = new HashMap<>();
		// String[] timeArray = DateUtil.formatTimeRange(timerange);
		try {
			// Map<String, Object> volumeNew =
			// chartAnalyzeService.getVolumeNew(searchBuilder,timeArray,timerange);
			Object volumeNew = chartAnalyzeService.getVolume(searchBuilder, timerange, sim, irSimflag,irSimflagAll,"");
			SpecialParam specParam = getSpecParam(specialProject);
			ChartParam chartParam = new ChartParam(specialId, timerange, industryType, area, ChartType.VOLUME.getType(),
					specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "");
			resultMap.put("data", volumeNew);
			resultMap.put("param", chartParam);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException("情感分析曲线趋势图计算错误,message: " + e);
		}
		return resultMap;
	}

	/**
	 * 情感分析瀑布图
	 *
	 * @param specialId
	 * @param dateType
	 * @param days
	 * @param industryType
	 * @param area
	 * @return
	 * @throws TRSException
	 *             曲线图的第三条线
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("情感分析瀑布图")
	@RequestMapping(value = "/emotional", method = RequestMethod.GET)
	public Object getEmotional(@RequestParam(value = "special_id") String specialId,
							   @RequestParam(value = "date_type", required = false) String dateType,
							   @RequestParam(value = "days", required = false) String days,
							   @RequestParam(value = "start_time", required = false) String startTime,
							   @RequestParam(value = "end_time", required = false) String endTime,
							   @RequestParam(value = "industry_type", required = false) String industryType,
							   @RequestParam(value = "area", required = false) String area) throws TRSException {

		// 如果为今天这new date() 到 今天凌晨所有的小时数量
		List<String> dateList = new ArrayList<String>();
		Map<String, Object> resultMap = new LinkedHashMap<>();
		try {
			if (StringUtil.isEmpty(dateType)) {
				dateType = "DIY";
			}
			if ("TODAY".equals(dateType)) {
				dateList = DateUtil.getCurrentDateHours();
				dateList.add(DateUtil.formatCurrentTime("yyyyMMddHHmmss"));
			}
			if ("HOUR".equals(dateType)) {
				dateList = DateUtil.get24Hours();
			}
			if ("DAY".equals(dateType)) {
				if (StringUtil.isEmpty(days)) {
					throw new OperationException("按天查询天数不能为空!");
				}
				dateList = DateUtil.getDataStinglist(Integer.parseInt(days));
			}
			if ("DIY".equals(dateType)) {
				if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
					throw new OperationException("自定义查询起止时间不能为空!");
				}
				dateList = DateUtil.getBetweenDateString(startTime, endTime, DateUtil.yyyyMMddHHmmss);
			}
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);

			for (String groupName : ESGroupName.MediaType.getAllMedias()) {
				List<String> countList = new LinkedList<>();
				for (int i = 0; i < dateList.size() - 1; i++) {
					String startGroup = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
					int value = chartAnalyzeService.getEmotionalValue(specialProject, groupName, dateList.get(i),
							dateList.get(i + 1), industryType, area);
					String endGroup = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
					log.warn("图表分析 情感分析瀑布图 查询hybase 本次耗时" + (Integer.parseInt(endGroup.substring(8, 17))
							- Integer.parseInt(startGroup.substring(8, 17))) + "ms");
					countList.add(String.valueOf(value));
				}
				resultMap.put(groupName, countList);
			}
			resultMap.put("Dates", dateList);
		} catch (Exception e) {
			throw new OperationException("情感瀑布图计算错误,message: " + e);
		}
		return resultMap;
	}

	/***
	 * 网友观点
	 *
	 * @param specialId
	 * @param industryType
	 * @param area
	 * @return
	 * @throws TRSException
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("网友观点")
	@RequestMapping(value = "/opinion", method = RequestMethod.GET)
	public Object getOpinion(@RequestParam(value = "specialId") String specialId,
							 @RequestParam(value = "timeRange", defaultValue = "1d") String timeRange,
							 @RequestParam(value = "industryType", defaultValue = "ALL") String industryType,
							 @RequestParam(value = "area", defaultValue = "ALL") String area) throws TRSException {

		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilderWeiBo();
			boolean sim = specialProject.isSimilar();
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (!"ALL".equals(industryType)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
			}
			if (!"ALL".equals(area)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, area.split(";"), Operator.Equal);
			}
			searchBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT, timeArray, Operator.Between);
			log.info(searchBuilder.asTRSL());
			GroupResult records = hybase8SearchService.categoryQuery(searchBuilder.isServer(), searchBuilder.asTRSL(),
					sim, irSimflag,irSimflagAll, FtsFieldConst.FIELD_TAG_TXT, 10, Const.WEIBO);
			return records.size() > 0 ? records : null;
		} catch (Exception e) {
			if (e.getMessage().contains("检索超时")){
				throw new TRSException("网友观点计算错误,message:" + e, CodeUtils.HYBASE_TIMEOUT,
						e);
			}else if (e.getMessage().contains("表达式过长")){
				throw new TRSException("网友观点计算错误,message:" + e, CodeUtils.HYBASE_EXCEPTION,
						e);
			}
			throw new OperationException("网友观点计算错误,message: " + e);
		}
	}

	// 微博情感分析
	@FormatResult
	@ApiOperation("微博情感分析")
	@EnableRedis
	@RequestMapping(value = "/weiboOption", method = RequestMethod.GET)
	public Object weiboOption(@RequestParam(value = "special_id") String specialId,
							  @RequestParam(value = "time_range", defaultValue = "3d") String timeRange,
							  @RequestParam(value = "industry_type", defaultValue = "ALL") String industryType,
							  @RequestParam(value = "area", defaultValue = "ALL") String area)
			throws OperationException, TRSSearchException {
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilderWeiBo();
		boolean sim = specialProject.isSimilar();
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		if (!"ALL".equals(industryType)) {
			searchBuilder.filterField(ESFieldConst.IR_VRESERVED5, industryType.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(area)) {
			searchBuilder.filterField(ESFieldConst.CATALOG_AREA, area.split(";"), Operator.Equal);
		}
		searchBuilder.filterField(ESFieldConst.IR_CREATED_AT, timeArray, Operator.Between);
		String trsl = searchBuilder.asTRSL();
		log.info(trsl);
		GroupResult records = hybase8SearchService.categoryQuery(specialProject.isServer(), trsl, sim, irSimflag,irSimflagAll,
				ESFieldConst.IR_APPRAISE, 3, Const.WEIBO);
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (GroupInfo group : records) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", group.getFieldValue());
			map.put("value", String.valueOf(group.getCount()));
			list.add(map);
		}
		SpecialParam specParam = getSpecParam(specialProject);
		ChartParam chartParam = new ChartParam(specialId, timeRange, industryType, area,
				ChartType.EMOTIONOPTION.getType(), specParam.getFirstName(), specParam.getSecondName(),
				specParam.getThirdName(), "");
		Map<String, Object> map = new HashMap<>();
		map.put("data", list);
		map.put("param", chartParam);
		return map;
	}

	/**
	 *
	 * @param specialId
	 *            专项id
	 * @param timeRange
	 *            高级筛选时间段
	 * @param industryType
	 *            行业类型
	 * @param area
	 *            地域分布
	 * @param chartType
	 *            图表类型
	 * @param dateTime
	 *            数据时间
	 * @param xType
	 *            数据参数
	 * @param source
	 *            数据来源
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            步长
	 * @return
	 * @throws Exception
	 */
	@FormatResult
	@ApiOperation("专题图表通用列表跳转方法")
	@RequestMapping(value = "/chart2list", method = RequestMethod.GET)
	public Object chart2list(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
							 @ApiParam("高级筛选时间段") @RequestParam(value = "timeRange", defaultValue = "3d") String timeRange,
							 @ApiParam("行业类型") @RequestParam(value = "industryType", defaultValue = "ALL") String industryType,
							 @ApiParam("地域分布") @RequestParam(value = "area", defaultValue = "ALL") String area,
							 @ApiParam("图表类型") @RequestParam(value = "chartType") String chartType,
							 @ApiParam("热词：用户点击的外圈词（共30个）")@RequestParam(value = "thirdWord", required = false)String thirdWord,
							 @ApiParam("数据时间") @RequestParam(value = "dateTime", required = false) String dateTime,
							 @ApiParam("数据参数") @RequestParam(value = "xType", required = false) String xType,
							 @ApiParam("数据来源") @RequestParam(value = "source", required = false) String source,
							 @ApiParam("通用：keywords；人物：people；地域：location；机构：agency") @RequestParam(value = "entityType", defaultValue = "keywords") String entityType,
							 @ApiParam("排序") @RequestParam(value = "sort", defaultValue = "default", required = false) String sort,
							 @ApiParam("情感") @RequestParam(value = "emotion", defaultValue = "ALL", required = false) String emotion,
							 @ApiParam("结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
							 @ApiParam("结果中搜索的范围")@RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope,
							 @ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
							 @ApiParam("微博原发/转发")@RequestParam(value = "forwarPrimary",defaultValue = "") String forwarPrimary,
							 @ApiParam("步长") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) throws Exception {

		SpecialProject specialProject = this.specialProjectNewRepository.findOne(specialId);
		if (specialProject != null) {
			// 初步高级删选时间范围
			specialProject.setTimeRange(timeRange);
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			return this.chartAnalyzeService.getDataByChart(specialProject, industryType, area, chartType, dateTime,
					xType, source, entityType, sort, emotion, fuzzyValue, fuzzyValueScope,pageNo, pageSize, forwarPrimary, null, false, thirdWord);
		}
		return null;
	}

	/**
	 * 为前端返回各级专题名字
	 *
	 * @param specialProject
	 * @return
	 */
	public SpecialParam getSpecParam(SpecialProject specialProject) {
		String firstName = "";
		String secondName = "";
		String thirdName = "";
		if (null != specialProject) {
			if (null != specialProject.getGroupName()) {
				SpecialSubject specialSubject = this.specialSubjectRepository.findOne(specialProject.getGroupId());
				if (null != specialSubject) {
					int flag = specialSubject.getFlag();
					if (flag == 0) {// 一级
						firstName = specialSubject.getName();
					} else if (flag == 1) {// 二级
						SpecialSubject subject = this.specialSubjectRepository.findOne(specialSubject.getSubjectId());// 一级
						firstName = subject.getName();
						secondName = specialSubject.getName();
					}
				}

			}
			thirdName = specialProject.getSpecialName();
		}
		SpecialParam specialParam = new SpecialParam(firstName, secondName, thirdName);
		return specialParam;
	}

	public static void main(String[] args) {
		List<Object> agencyList = new ArrayList<Object>();
		for (int j = 0; j < 300; j++) {
			List<Double> strings = new ArrayList<>();
			double min = 0.0001;//最小值
			double max = 1;//总和
			int scl = 4;//小数最大位数
			int pow = (int) Math.pow(10, scl);//指定小数位
			for (int i=0; i<2; i++) {
				double one = Math.floor((Math.random() * (max - min) + min) * pow) / pow;
				strings.add(one);
			}
			agencyList.add(strings);
		}


		JSONArray jsonObject = JSONArray.fromObject(agencyList);
		String jsonString = jsonObject.toString();

		// 拼接文件完整路径
		String fullPath = "D:\\jsonFile\\example.json";

		// 生成json格式文件
		try {
			// 保证创建一个新文件
			File file = new File(fullPath);
			if (!file.getParentFile().exists()) { // 如果父目录不存在，创建父目录
				file.getParentFile().mkdirs();
			}
			if (file.exists()) { // 如果已存在,删除旧文件
				file.delete();
			}
			file.createNewFile();

			if(jsonString.indexOf("'")!=-1){
				//将单引号转义一下，因为JSON串中的字符串类型可以单引号引起来的
				jsonString = jsonString.replaceAll("'", "\\'");
			}
			if(jsonString.indexOf("\"")!=-1){
				//将双引号转义一下，因为JSON串中的字符串类型可以单引号引起来的
				jsonString = jsonString.replaceAll("\"", "\\\"");
			}

			if(jsonString.indexOf("\r\n")!=-1){
				//将回车换行转换一下，因为JSON串中字符串不能出现显式的回车换行
				jsonString = jsonString.replaceAll("\r\n", "\\u000d\\u000a");
			}
			if(jsonString.indexOf("\n")!=-1){
				//将换行转换一下，因为JSON串中字符串不能出现显式的换行
				jsonString = jsonString.replaceAll("\n", "\\u000a");
			}

			// 格式化json字符串
			//	jsonString = JsonFormatTool.formatJson(jsonString);

			StringBuffer result = new StringBuffer();

			int length = jsonString.length();
			int number = 0;
			char key = 0;

			// 遍历输入字符串。
			for (int i = 0; i < length; i++) {
				// 1、获取当前字符。
				key = jsonString.charAt(i);

				// 2、如果当前字符是前方括号、前花括号做如下处理：
				if ((key == '[') || (key == '{')) {
					// （1）如果前面还有字符，并且字符为“：”，打印：换行和缩进字符字符串。
					if ((i - 1 > 0) && (jsonString.charAt(i - 1) == ':')) {
						result.append('\n');
						StringBuffer resulthh = new StringBuffer();
						for (int m = 0; m < number; m++) {
							resulthh.append("   ");
						}
						result.append(resulthh);
					}

					// （2）打印：当前字符。
					result.append(key);

					// （3）前方括号、前花括号，的后面必须换行。打印：换行。
					result.append('\n');

					// （4）每出现一次前方括号、前花括号；缩进次数增加一次。打印：新行缩进。
					number++;
					StringBuffer resulthh = new StringBuffer();
					for (int m = 0; m < number; m++) {
						resulthh.append("   ");
					}
					result.append(resulthh);

					// （5）进行下一次循环。
					continue;
				}

				// 3、如果当前字符是后方括号、后花括号做如下处理：
				if ((key == ']') || (key == '}')) {
					// （1）后方括号、后花括号，的前面必须换行。打印：换行。
					result.append('\n');

					// （2）每出现一次后方括号、后花括号；缩进次数减少一次。打印：缩进。
					number--;
					StringBuffer resulthh = new StringBuffer();
					for (int m = 0; m < number; m++) {
						resulthh.append("   ");
					}
					result.append(resulthh);

					// （3）打印：当前字符。
					result.append(key);

					// （4）如果当前字符后面还有字符，并且字符不为“，”，打印：换行。
					if (((i + 1) < length) && (jsonString.charAt(i + 1) != ',')) {
						result.append('\n');
					}

					// （5）继续下一次循环。
					continue;
				}

				// 4、如果当前字符是逗号。逗号后面换行，并缩进，不改变缩进次数。
            /*if ((key == ',')) {
                result.append(key);
                result.append('\n');
                result.append(indent(number));
                continue;
            }*/

				// 5、打印：当前字符。
				result.append(key);
			}


			// 将格式化后的字符串写入文件
			Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			write.write(jsonString);
			write.flush();
			write.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 返回是否成功的标记
	}
}
