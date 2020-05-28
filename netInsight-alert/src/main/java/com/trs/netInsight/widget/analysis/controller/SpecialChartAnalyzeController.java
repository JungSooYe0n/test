package com.trs.netInsight.widget.analysis.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
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
import com.trs.netInsight.support.ckm.ICkmService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.entity.*;
import com.trs.netInsight.widget.analysis.enums.ChartType;
import com.trs.netInsight.widget.analysis.enums.Top5Tab;
import com.trs.netInsight.widget.analysis.service.IChartAnalyzeService;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.spread.entity.SinaUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * 无锡图表分析模块 Controller Created by yangyanyan on 2018/5/3.
 */
@Slf4j
@RestController
@RequestMapping("/special/chart")
@Api(description = "专项检测图表分析接口")
public class SpecialChartAnalyzeController {
	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private SpecialProjectRepository specialProjectNewRepository;

	@Autowired
	private SpecialSubjectRepository specialSubjectRepository;

	@Autowired
	private IChartAnalyzeService specialChartAnalyzeService;

	@Autowired
	private ICkmService ckmService;

	@Autowired
	private ICommonListService commonListService;
	@Autowired
	private ICommonChartService commonChartService;

	// @Autowired
	// private LogPrintUtil loginpool;
	@FormatResult
	@EnableRedis
	@ApiOperation("舆论场趋势分析折线")
	@RequestMapping(value = "/webCountLine", method = RequestMethod.GET)
	public Object webCountLine(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							 @ApiParam("专项id") @RequestParam(value = "specialId", required = true) String specialId,
							   @ApiParam("hour/day") @RequestParam(value = "showType", required = true) String showType)
			throws TRSException, ParseException {
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		long start = new Date().getTime();
		ObjectUtil.assertNull(specialProject, "专题ID");
		if (StringUtils.isBlank(timeRange)) {
			timeRange = specialProject.getTimeRange();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
				timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
			}
		}
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		if (timeArray != null && timeArray.length == 2) {
			specialProject.setStart(timeArray[0]);
			specialProject.setEnd(timeArray[1]);
		}
		return specialChartAnalyzeService.getWebCountLine(specialProject,timeRange,showType);

	}
	@EnableRedis
	@FormatResult
	@ApiOperation("态势评估")
	@RequestMapping(value = "/situationAssessment", method = RequestMethod.GET)
	public Object situationAssessment(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
									@ApiParam("专项id") @RequestParam(value = "specialId", required = true) String specialId)
			throws TRSException, ParseException {
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		long start = new Date().getTime();
		ObjectUtil.assertNull(specialProject, "专题ID");
		if (StringUtils.isBlank(timeRange)) {
			timeRange = specialProject.getTimeRange();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
				timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
			}
		}
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		if (timeArray != null && timeArray.length == 2) {
			specialProject.setStart(timeArray[0]);
			specialProject.setEnd(timeArray[1]);
		}
		int max=100,min=50;
	    int ran = (int) (Math.random()*(max-min)+min);
		return ran;

	}
	@FormatResult
	@ApiOperation("观点分析")
	@RequestMapping(value = "/sentimentAnalysis", method = RequestMethod.GET)
	public Object sentimentAnalysis(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							 @ApiParam("专项id") @RequestParam(value = "specialId", required = true) String specialId,
							 @RequestParam(value = "viewType", required = false, defaultValue = "OFFICIAL_VIEW") String viewType)
			throws TRSException, ParseException {
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		long start = new Date().getTime();
		ObjectUtil.assertNull(specialProject, "专题ID");
		if (StringUtils.isBlank(timeRange)) {
			timeRange = specialProject.getTimeRange();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
				timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
			}
		}
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		if (timeArray != null && timeArray.length == 2) {
			specialProject.setStart(timeArray[0]);
			specialProject.setEnd(timeArray[1]);
		}
		return specialChartAnalyzeService.getSentimentAnalysis(specialProject,timeRange,viewType);

	}
	/**
	 * 来源类型统计
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
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_WEBCOUNT, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "专题分析/${specialId}/内容统计/来源类型统计")
	@EnableRedis
	@FormatResult
	@RequestMapping(value = "/webCount", method = RequestMethod.GET)
	@ApiOperation("来源类型统计")
	public Object webCountnew(@RequestParam(value = "timeRange", required = false) String timeRange,
							  @RequestParam(value = "specialId", required = true) String specialId,
							  @RequestParam(value = "area", defaultValue = "ALL") String area,
							  @RequestParam(value = "industry", defaultValue = "ALL") String industry) throws Exception {
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		long start = new Date().getTime();
		ObjectUtil.assertNull(specialProject, "专题ID");
		if (StringUtils.isBlank(timeRange)) {
			timeRange = specialProject.getTimeRange();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
				timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
			}
		}
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		if (timeArray != null && timeArray.length == 2) {
			specialProject.setStart(timeArray[0]);
			specialProject.setEnd(timeArray[1]);
		}

		// 根据时间升序,只要第一条
		// QueryBuilder searchBuilder = specialProject.toBuilder(0, 1, false);
		Map<String, Object> resultMap = specialChartAnalyzeService.getWebCountNew(timeRange, specialProject, area,
				industry);
		long end = new Date().getTime();
		long time = end - start;
		log.info("网站统计后台所需时间" + time);
		SpecialParam specParam = getSpecParam(specialProject);
		ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area, ChartType.WEBCOUNT.getType(),
				specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "无锡");
		Map<String, Object> map = new HashMap<>();
		map.put("data", resultMap);
		map.put("param", chartParam);
		return map;
	}
	/**
	 *
	 *各舆论发布统计
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
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_WEBCOUNT, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "专题分析/${specialId}/内容统计/来源类型统计")
	@EnableRedis
	@FormatResult
	@RequestMapping(value = "/webCommitCount", method = RequestMethod.GET)
	@ApiOperation("舆论场发布统计")
	public Object webCommitCount(@RequestParam(value = "timeRange", required = false) String timeRange,
							  @RequestParam(value = "specialId", required = true) String specialId,
							  @RequestParam(value = "area", defaultValue = "ALL") String area,
							  @RequestParam(value = "industry", defaultValue = "ALL") String industry) throws Exception {
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		long start = new Date().getTime();
		ObjectUtil.assertNull(specialProject, "专题ID");
		if (StringUtils.isBlank(timeRange)) {
			timeRange = specialProject.getTimeRange();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
				timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
			}
		}
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		if (timeArray != null && timeArray.length == 2) {
			specialProject.setStart(timeArray[0]);
			specialProject.setEnd(timeArray[1]);
		}

		// 根据时间升序,只要第一条
		// QueryBuilder searchBuilder = specialProject.toBuilder(0, 1, false);
		// 排重
		boolean sim = specialProject.isSimilar();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
	 QueryBuilder builder = specialProject.toNoPagedBuilder();
		builder.filterField(FtsFieldConst.FIELD_GROUPNAME,specialProject.getSource().replace(";", " OR ")
				.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
		String contrastField = FtsFieldConst.FIELD_GROUPNAME;
		builder.setPageSize(20);
		ChartResultField resultField = new ChartResultField("name", "value");
		List<Map<String, Object>> list = new ArrayList<>();
		list = (List<Map<String, Object>>)commonChartService.getPieColumnData(builder,sim,irSimflag,irSimflagAll,specialProject.getSource(),null,contrastField,"special",resultField);
//		Map<String, Object> resultMap = specialChartAnalyzeService.getWebCountNew(timeRange, specialProject, area,
//				industry);
		return list;
	}
	/**
	 * 信息走势图
	 *
	 * @param timeRange
	 *            时间类型 今天，24小时，几天
	 * @param specialId
	 *            专项id
	 * @param area
	 *            检索开始时间
	 * @param industry
	 *            检索结束时间
	 * @param showType
	 * 			  走势图的展示形式：天、小时
	 * @return
	 * @throws TRSException
	 * @author mawen 2017年12月2日 Object
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_TRENDMESSAGE, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件趋势/信息走势图/信息走势")
	@EnableRedis
	@FormatResult
	@RequestMapping(value = "/trendMessage", method = RequestMethod.GET)
	@ApiOperation("信息走势图")
	public Object trendMessage(@RequestParam(value = "timeRange", required = false) String timeRange,
							   @RequestParam(value = "specialId", required = true) String specialId,
							   @RequestParam(value = "area", defaultValue = "ALL") String area,
							   @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							   @RequestParam(value = "showType", required = false,defaultValue = "") String showType) throws Exception {
		long start = new Date().getTime();
		LogPrintUtil loginpool = new LogPrintUtil();
		long id = Thread.currentThread().getId();
		RedisUtil.setLog(id, loginpool);
		log.debug(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			// QueryBuilder searchBuilder = new QueryBuilder();
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
				searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
			}
			// searchBuilder = specialProject.toNoPagedAndTimeBuilder();
			// 服务类排重按专题来的
			Map<String, Object> resultMap = specialChartAnalyzeService.getTendencyMessage(searchBuilder.asTRSL(),
					specialProject, timeRange,showType);
			long end = new Date().getTime();
			long time = end - start;
			log.info("网站统计后台所需时间" + time);
			SpecialParam specParam = getSpecParam(specialProject);
			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area,
					ChartType.TRENDMESSAGE.getType(), specParam.getFirstName(), specParam.getSecondName(),
					specParam.getThirdName(), "无锡");
			Map<String, Object> map = new HashMap<>();
			map.put("data", resultMap);
			map.put("param", chartParam);
			return map;
		} catch (TRSException e) {
			throw e;
		} finally {
			LogPrintUtil logRedis = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logRedis.setComeBak(start);
			logRedis.setFinishBak(end);
			logRedis.setFullBak(timeApi);
			if (logRedis.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logRedis.printTime(LogPrintUtil.TREND_MESSAGE);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}

	}

	/**
	 *  微博top5
	 * @param specialId  专题id
	 * @param sortType   排序方式：最新、评论、转发
	 * @param timeRange  时间段
	 * @return
	 * @throws Exception
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_TOP5, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/内容统计/微博top5")
	@EnableRedis
	@FormatResult
	@ApiOperation("微博top5")
	@RequestMapping(value = "/top5", method = RequestMethod.GET)
	public Object top5(@ApiParam("专项ID") @RequestParam("specialId") String specialId,
					   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
					   @ApiParam("排序方式") @RequestParam(value = "sortType", defaultValue = "NEWEST") String sortType,
					   @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
					   @ApiParam("时间范围") @RequestParam(value = "timeRange", required = false) String timeRange) throws Exception {
		try {
			long start = new Date().getTime();
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			String groupName = specialProject.getSource();//多个以;隔开
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			QueryBuilder builder = specialProject.toNoPagedAndTimeBuilderWeiBo();
			builder.setGroupName(groupName);
			//因列表页日期与详情页不一致，所以改回按URLTIME查询
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);
			//builder.filterField(FtsFieldConst.FIELD_LOADTIME,DateUtil.formatTimeRange(timeRange), Operator.Between);
			//行业
//			if (!"ALL".equals(industry)) {
//				builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
//			}
			//地域
//			if (!"ALL".equals(area)) {
//				String[] areaSplit = area.split(";");
//				String contentArea = "";
//				for (int i = 0; i < areaSplit.length; i++) {
//					areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
//					if (i != areaSplit.length - 1) {
//						areaSplit[i] += " OR ";
//					}
//					contentArea += areaSplit[i];
//				}
//				builder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
//			}
//
			List<MBlogAnalyzeEntity> mBlogTop5 = specialChartAnalyzeService.mBlogTop5(builder,
					Top5Tab.valueOf(sortType), specialProject.isSimilar(), specialProject.isIrSimflag(),specialProject.isIrSimflagAll());

//			if ("NEWEST".equals(sortType)){//按 最新 排序时  需要手动排重
//				//MD5代码排重
//				List<MBlogAnalyzeEntity> returnDataList = new ArrayList<>();
//				List<MBlogAnalyzeEntity> repeatedDataList = new ArrayList<>();//此集合里的值为MD5重复的值 为防止帅选后 最终数据达不到5条 而备用
//				LinkedHashMap<String, MBlogAnalyzeEntity> distinctMap = Maps.newLinkedHashMap();//为保证顺序 采用linkedhashmap
//				int flag = 0;
//				for (int i = 0; i < mBlogTop5.size(); i++) {//若key值已存在 则不放里面  保证存入的是不同的MD5值对应的最新的一条数据
//					if (!distinctMap.containsKey(mBlogTop5.get(i).getMd5Tag()) && distinctMap.size() < 5){
//						distinctMap.put(mBlogTop5.get(i).getMd5Tag(),mBlogTop5.get(i));
//						if (distinctMap.size()-1 != i){
//							flag=i; //排重剩余不到5条 需要从repeatedDataList里取值  为避免时间错乱 需要从最后一个入distinctMap里的数据之后取 flag就是标识最后一个入distinctMap的数据的角标的
//						}
//					}else if (!distinctMap.containsKey(mBlogTop5.get(i).getMd5Tag())){
//						repeatedDataList.add(mBlogTop5.get(i));
//					}
//				}
//				if (distinctMap.size() < 5){
//
//				}
//				//为保证返回格式相同 作如下操作
//				for (Map.Entry<String, MBlogAnalyzeEntity> entityEntry : distinctMap.entrySet()) {
//					returnDataList.add(entityEntry.getValue());
//				}
//
//				if (returnDataList.size() < 5){
//					if (ObjectUtil.isNotEmpty(repeatedDataList) && repeatedDataList.size()>flag){
//						if (repeatedDataList.size()>(flag+5-returnDataList.size())){
//							returnDataList.addAll(repeatedDataList.subList(flag,flag+5-returnDataList.size()));
//						}else {
//							returnDataList.addAll(repeatedDataList.subList(flag,returnDataList.size()));
//						}
//					}
//				}
//				return returnDataList;
//			}

			long end = new Date().getTime();
			long time = end - start;
			log.info("yanyan微博top5后台总共所需时间" + time);
			return mBlogTop5;
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("微博top5出错，message:" + e);
		}
	}

	/**
	 * 地域分布
	 *
	 * @param specialId
	 * @param area
	 * @param timeRange
	 * @param industry
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_AREA, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/内容统计/地域分布")
	@EnableRedis
	@FormatResult
	@ApiOperation("地域分布")
	@RequestMapping(value = "/area", method = RequestMethod.GET)
	public Object area(@RequestParam(value = "specialId", required = true) String specialId,
					   @RequestParam(value = "area", defaultValue = "ALL") String area,
					   @RequestParam(value = "timeRange", required = false) String timeRange,
					   @RequestParam(value = "industry", defaultValue = "ALL") String industry) throws Exception {
		long start = new Date().getTime();
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		if (specialProject != null) {
			//单一媒体排重
			boolean isSimilar = specialProject.isSimilar();
			// url排重,站内排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重,全网排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String groupName = specialProject.getSource();//多个以;隔开
			if(groupName.contains("微信") && !groupName.contains("国内微信")){
				groupName = groupName.replaceAll("微信","国内微信");
			}
			groupName = groupName.replaceAll("境外网站","国外新闻");
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();

			searchBuilder.setGroupName(groupName);
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
				searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
			}
			// String[] timeArray = DateUtil.formatTimeRange(timeRange);
			List<Map<String, Object>> resultMap = specialChartAnalyzeService.getAreaCount(searchBuilder, timeArray,isSimilar,
					irSimflag,irSimflagAll);
			List<Map<String, Object>> sortByValue = MapUtil.sortByValue(resultMap, "area_count");
			long end = new Date().getTime();
			long time = end - start;
			log.info("地域分布后台所需时间" + time);
			SpecialParam specParam = getSpecParam(specialProject);
			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area, ChartType.AREA.getType(),
					specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "无锡");
			Map<String, Object> map = new HashMap<>();
			map.put("data", sortByValue);
			map.put("param", chartParam);
			return map;
		}
		return null;
	}
	/**
	 * 地域分布
	 *
	 * @param specialId
	 * @param area
	 * @param timeRange
	 * @param industry
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_AREA, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/内容统计/地域统计")
	@EnableRedis
	@FormatResult
	@ApiOperation("地域统计")
	@RequestMapping(value = "/areaStatistics", method = RequestMethod.GET)
	public Object area(@RequestParam(value = "specialId", required = true) String specialId,
					   @RequestParam(value = "area", defaultValue = "ALL") String area,
					   @RequestParam(value = "timeRange", required = false) String timeRange,
					   @RequestParam(value = "industry", defaultValue = "ALL") String industry,@RequestParam(value = "areaType", defaultValue = "catalogArea") String areaType) throws Exception {
		long start = new Date().getTime();
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		if (specialProject != null) {
			//单一媒体排重
			boolean isSimilar = specialProject.isSimilar();
			// url排重,站内排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重,全网排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String groupName = specialProject.getSource();//多个以;隔开
			if(groupName.contains("微信") && !groupName.contains("国内微信")){
				groupName = groupName.replaceAll("微信","国内微信");
			}
			groupName = groupName.replaceAll("境外网站","国外新闻");
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();

			searchBuilder.setGroupName(groupName);
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
				searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
			}
			// String[] timeArray = DateUtil.formatTimeRange(timeRange);
			List<Map<String, Object>> resultMap = specialChartAnalyzeService.getAreaCount(searchBuilder, timeArray,isSimilar,
					irSimflag,irSimflagAll,areaType);
//			List<Map<String, Object>> sortByValue = MapUtil.sortByValue(resultMap, "value");
//			long end = new Date().getTime();
//			long time = end - start;
//			log.info("地域分布后台所需时间" + time);
//			SpecialParam specParam = getSpecParam(specialProject);
//			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area, ChartType.AREA.getType(),
//					specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "无锡");
//			Map<String, Object> map = new HashMap<>();
//			map.put("data", sortByValue);
//			map.put("param", chartParam);
			return resultMap;
		}
		return null;
	}
	/**
	 * 情感分析
	 *
	 * @param specialId
	 * @param timeRange
	 * @param industry
	 * @param area
	 * @return
	 * @throws OperationException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_EMOTIONOPTION, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/内容统计/微博情感分析")
	@FormatResult
	@ApiOperation("正负面占比")
	@EnableRedis
	@RequestMapping(value = "/emotionOption", method = RequestMethod.GET)
	public Object weiboOption(@RequestParam(value = "specialId") String specialId,
							  @RequestParam(value = "timeRange", required = false) String timeRange,
							  @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							  @RequestParam(value = "area", defaultValue = "ALL") String area) throws Exception {
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		String groupName = specialProject.getSource();//多个以;隔开
		if (StringUtils.isBlank(timeRange)) {
			timeRange = specialProject.getTimeRange();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
				timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
			}
		}
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		if (timeArray != null && timeArray.length == 2) {
			specialProject.setStart(timeArray[0]);
			specialProject.setEnd(timeArray[1]);
		}
		QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
		searchBuilder.setGroupName(groupName);

		if (!"ALL".equals(industry)) {
			searchBuilder.filterField(ESFieldConst.IR_VRESERVED5, industry.split(";"), Operator.Equal);
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
			searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
		}

		List<Map<String, String>> list =specialChartAnalyzeService.emotionOption(searchBuilder,specialProject);

//		SpecialParam specParam = getSpecParam(specialProject);
//		ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area, ChartType.EMOTIONOPTION.getType(),
//				specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "网察");
//		Map<String, Object> map = new HashMap<>();
//		map.put("data", list);
//		map.put("param", chartParam);
		return list;
	}

	/**
	 * 活跃账号
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
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_ACTIVE_LEVEL, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/内容统计/媒体活跃等级")
	@FormatResult
	@ApiOperation("活跃账号")
	@RequestMapping(value = "/active_account", method = RequestMethod.GET)
	public Object getActiveAccount(@RequestParam("specialId") String specialId,
								 @RequestParam(value = "area", defaultValue = "ALL") String area,
								 @RequestParam(value = "industry", defaultValue = "ALL") String industry,
								 @RequestParam(value = "timeRange", required = false) String timeRange) throws TRSException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			String groupName = specialProject.getSource();//多个以;隔开
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			QueryBuilder builder = specialProject.toNoPagedBuilder();
			builder.setGroupName(groupName);
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
				builder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
			}

			String[] range = DateUtil.formatTimeRange(timeRange);
			String source = specialProject.getSource();
			Object mediaActiveAccount = specialChartAnalyzeService.mediaActiveAccount(builder,source, range, sim,
					irSimflag,irSimflagAll);
			long end = new Date().getTime();
			long time = end - start;
//			log.info("媒体活跃等级图后台所需时间" + time);
//			SpecialParam specParam = getSpecParam(specialProject);
//			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area,
//					ChartType.ACTIVE_LEVEL.getType(), specParam.getFirstName(), specParam.getSecondName(),
//					specParam.getThirdName(), "无锡");
//			Map<String, Object> map = new HashMap<>();
//			map.put("data", mediaActiveLevel);
//			map.put("param", chartParam);
			return mediaActiveAccount;
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("获取媒体活跃等级出错，message:" + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.ACTIVE_LEVEL);
			}
			log.info("调用接口用了" + timeApi + "ms");
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
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_ACTIVE_LEVEL, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/内容统计/媒体活跃等级")
	@EnableRedis
	@FormatResult
	@ApiOperation("媒体活跃等级图")
	@RequestMapping(value = "/active_level", method = RequestMethod.GET)
	public Object getActiveLevel(@RequestParam("specialId") String specialId,
								 @RequestParam(value = "area", defaultValue = "ALL") String area,
								 @RequestParam(value = "industry", defaultValue = "ALL") String industry,
								 @RequestParam(value = "source", required = true) String source,
								 @RequestParam(value = "timeRange", required = false) String timeRange) throws TRSException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			String groupName = specialProject.getSource();//多个以;隔开
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			QueryBuilder builder = specialProject.toNoPagedBuilder();
			builder.setGroupName(groupName);
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
				builder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
			}

			String[] range = DateUtil.formatTimeRange(timeRange);
			Object mediaActiveLevel = specialChartAnalyzeService.mediaActiveLevel(builder,source, range, sim,
					irSimflag,irSimflagAll);
			long end = new Date().getTime();
			long time = end - start;
			log.info("媒体活跃等级图后台所需时间" + time);
			SpecialParam specParam = getSpecParam(specialProject);
			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area,
					ChartType.ACTIVE_LEVEL.getType(), specParam.getFirstName(), specParam.getSecondName(),
					specParam.getThirdName(), "无锡");
			Map<String, Object> map = new HashMap<>();
			map.put("data", mediaActiveLevel);
			map.put("param", chartParam);
			return map;
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("获取媒体活跃等级出错，message:" + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.ACTIVE_LEVEL);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}
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
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_WORDCLOUD, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件分析/@{entityType}")
	@FormatResult
	@EnableRedis
	@ApiOperation("词云分析")
	@RequestMapping(value = "/wordCloud", method = RequestMethod.GET)
	public Object getWordYun(@ApiParam("专题ID") @RequestParam("specialId") String specialId,
							 @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
							 @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							 @ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							 @ApiParam("实体类型（通用：keywords；人名:people；地名:location；机构名:agency；）") @RequestParam(value = "entityType", defaultValue = "keywords") String entityType,
							 @ApiParam("文章类型（全部：all；新闻：news；微博：weibo；微信：weixin；客户端：app 目前前端都传了all）") @RequestParam(value = "articleType", defaultValue = "all") String articleType)
			throws Exception {
		long start = new Date().getTime();
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		ObjectUtil.assertNull(specialProject, "专题ID");
		if (StringUtils.isBlank(timeRange)) {
			timeRange = specialProject.getTimeRange();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
				timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
			}
		}
		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
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
			searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
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
		Object wordCloud = null;
		String[] timeArr = com.trs.netInsight.util.DateUtil.formatTimeRange(timeRange);
		String time0 = timeArr[0];
		if (!com.trs.netInsight.util.DateUtil.isExpire("2019-10-01 00:00:00",time0)){
			String metas = specialProject.getSource();
			String[] data = null;
			if (!"ALL".equals(metas)){
				String[] split = metas.split(";");
				data = TrslUtil.chooseDatabases(split);
				metas = metas.replaceAll("境外媒体", "国外新闻");
				metas = metas.replaceAll("微信", "国内微信");

				// 统一来源
				metas = metas.trim();
				if (metas.endsWith(";")) {
					metas = metas.substring(0, metas.length() - 2);
				}
				metas = "(" + metas.replaceAll(";", " OR ") + ")";
				if (!metas.equals("(传统媒体)")) {
					searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, metas, Operator.Equal);
				}
			}else {
				data = Const.MIX_DATABASE.split(";");
			}

//			wordCloud = specialChartAnalyzeService.getWordCloud(specialProject.isServer(), searchBuilder.asTRSL(),
//					sim, irSimflag,irSimflagAll, entityType, searchBuilder.getPageSize(),"special", data);
			wordCloud = commonChartService.getWordCloudColumnData(searchBuilder,sim, irSimflag,irSimflagAll,specialProject.getSource(),entityType,"special");
		}else {
			wordCloud = specialChartAnalyzeService.getWordCloudNew(searchBuilder,sim, irSimflag,irSimflagAll, entityType,"special");
//			wordCloud = specialChartAnalyzeService.getWordCloud(specialProject.isServer(), searchBuilder.asTRSL(),
//					sim, irSimflag,irSimflagAll, entityType, searchBuilder.getPageSize(),"special", searchBuilder.getDatabase());

		}

//
//		long end = new Date().getTime();
//		long time = end - start;
//		log.info("词云分析后台所需时间" + time);
//		SpecialParam specParam = getSpecParam(specialProject);
//		ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area, ChartType.WORDCLOUD.getType(),
//				specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "无锡");
//		Map<String, Object> map = new HashMap<>();
		if(ObjectUtil.isEmpty(wordCloud)){
			return null;
		}
//		map.put("data", wordCloud);
//		map.put("param", chartParam);
		return wordCloud;
	}

	/**
	 * 事件溯源
	 *
	 * @param specialId
	 * @param pageSize
	 * @param type
	 * @param timeRange
	 * @param industry
	 * @param area
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_TRENDTIME, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件趋势/事件溯源")
	@SuppressWarnings("rawtypes")
	@EnableRedis
	@FormatResult
	@ApiOperation("走势最新那条")
	@RequestMapping(value = "/trendTime", method = RequestMethod.GET)
	public Object trendTime(@ApiParam("专项id") @RequestParam(value = "specialId") String specialId,
							@ApiParam("页") @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize,
							@ApiParam("类型") @RequestParam(value = "type", required = false) String type,
							@ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
							@ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							@ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws TRSException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {

			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			String groupName = specialProject.getSource();//多个以;隔开
			// 单一数据源排重
			boolean irSimflag = specialProject.isIrSimflag();
			//全网排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			//站内排重
			boolean sim = specialProject.isSimilar();
			QueryBuilder statBuilder = null;
			if ("weibo".equals(type)) {
				statBuilder = specialProject.toNoTimeBuilderWeiBo(0, 1);
			} else {
				statBuilder = specialProject.toNoTimeBuilder(0, 1);
			}
			statBuilder.page(0, pageSize);
			if (!"ALL".equals(industry)) {
				statBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
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
				statBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
			}
			List<Map> resultList = new ArrayList<>();
			final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
			String trslk = "redisKey" + pageId;
			switch (type) {
				case "weibo":
					statBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT, timeArray, Operator.Between);
					statBuilder.page(0, 10);
					statBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					statBuilder.setDatabase(Const.WEIBO);

					RedisUtil.setString(trslk, statBuilder.asTRSL());
					if(StringUtil.isNotEmpty(groupName)){
						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
								.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
					}
//				List<FtsDocumentStatus> ftsQueryWeiBo = hybase8SearchService.ftsQuery(statBuilder,
//						FtsDocumentStatus.class, sim, irSimflag,irSimflagAll,"special");
					InfoListResult infoListResult = commonListService.queryPageList(statBuilder,sim,irSimflag,irSimflagAll,Const.GROUPNAME_WEIBO,type,UserUtils.getUser(),true);
					PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
					List<FtsDocumentCommonVO> ftsQueryWeiBo = content.getPageItems();
					log.info(statBuilder.asTRSL());
					FtsDocumentCommonVO ftsStatus = null;
					if (ftsQueryWeiBo != null && ftsQueryWeiBo.size() > 0) {
//					ftsStatus = ftsQueryWeiBo.get(0);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hhmmss");
						for(FtsDocumentCommonVO fts:ftsQueryWeiBo){
							//取时分秒不完全为0的数据
							String m = sdf.format(fts.getUrlTime());
							if(!m.endsWith("000000") && !m.endsWith("120000")){
								ftsStatus = fts;
								break;
							}
						}
						// 上边过滤完了还是空就按照loadtime查
						if (null == ftsStatus) {
							statBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, false);
						/*
						 * final String chartId =
						 * GUIDGenerator.generate(SpecialChartAnalyzeController.
						 * class); String trslKey = "redisKey" + chartId;
						 */
							RedisUtil.setString(trslk, statBuilder.asTRSL());
							InfoListResult infoListResult2 = commonListService.queryPageList(statBuilder,sim,irSimflag,irSimflagAll,Const.GROUPNAME_WEIBO,type,UserUtils.getUser(),true);
							PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
							List<FtsDocumentCommonVO> ftsQueryWeiBo2 = content2.getPageItems();
//						List<FtsDocumentStatus> ftsQueryWeiBo2 = hybase8SearchService.ftsQuery(statBuilder,
//								FtsDocumentStatus.class, sim, irSimflag,irSimflagAll,"special");
							ftsStatus = ftsQueryWeiBo2.get(0);
						}

						String md5 = ftsStatus.getMd5Tag();
						QueryBuilder builder = new QueryBuilder();
						builder.setDatabase(Const.WEIBO);
						builder.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
						if(StringUtil.isNotEmpty(groupName)){
							builder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
									.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
						}
//					long ftsCount = hybase8SearchService.ftsCount(builder, sim, irSimflag,irSimflagAll,"special");
						long ftsCount = commonListService.ftsCount(builder, sim, irSimflag,irSimflagAll,"special");
						if (ftsCount == 1L) {
							ftsCount = 0L;
						}
						ftsStatus.setSim((int) ftsCount);
						ftsStatus.setTrslk(trslk);
						return ftsStatus;
					}

				case "tradition":
					// 1.传统走势 分三步：
					// ①查关媒（重点媒体） 后期会有数据支持 ②groupname 只按国内新闻*查，不关注创建专题时来源的选择
					// ③urltime loadtime 排序 最早的
					// 以上三条 决定事件溯源的root 节点
					// 关媒 还没加入条件 等数据推入再加
					if(StringUtil.isNotEmpty(groupName)){
						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
								.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
					}
					if (statBuilder.isServer()) {
						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻%", Operator.Equal);
					} else {
						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻*", Operator.Equal);
					}
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
//				statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, "%000000", Operator.NotLike);//*:*-作者:""
					statBuilder.page(0, 10);
					statBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, false);
					statBuilder.setDatabase(Const.HYBASE_NI_INDEX);

					RedisUtil.setString(trslk, statBuilder.asTRSL());
					log.info("最新走势：" + statBuilder.asTRSL());
//				List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(statBuilder, FtsDocument.class, sim,
//						irSimflag,irSimflagAll,"special");
					InfoListResult infoListResultD = commonListService.queryPageList(statBuilder,sim,irSimflag,irSimflagAll,Const.TYPE_NEWS,type,UserUtils.getUser(),true);
					PagedList<FtsDocumentCommonVO> contentD = (PagedList<FtsDocumentCommonVO>) infoListResultD.getContent();
					List<FtsDocumentCommonVO> ftsQuery = contentD.getPageItems();
					log.info(statBuilder.asTRSL());
					FtsDocumentCommonVO ftsDocument = null;
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hhmmss");
					for(FtsDocumentCommonVO fts:ftsQuery){
						//取时分秒不完全为0的数据
						String m = sdf.format(fts.getUrlTime());
						log.info(m);
						if(!m.endsWith("000000") && !m.endsWith("120000")){
							ftsDocument = fts;
							break;
						}
					}
					if (ftsQuery != null && ftsQuery.size() > 0) {

						// 算相似文章数
						String md52 = ftsDocument.getMd5Tag();
						QueryBuilder builder2 = new QueryBuilder();
						builder2.setDatabase(Const.HYBASE_NI_INDEX);
						builder2.filterField(FtsFieldConst.FIELD_MD5TAG, md52, Operator.Equal);
						if(StringUtil.isNotEmpty(groupName)){
							builder2.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
									.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
						}
//					long ftsCount2 = hybase8SearchService.ftsCount(builder2, false, false,false,"special");
						long ftsCount2 = commonListService.ftsCount(builder2, sim, irSimflag,irSimflagAll,"special");
						if (ftsCount2 == 1L) {
							ftsCount2 = 0L;
						}
						ftsDocument.setSim((int)ftsCount2);
						ftsDocument.setTrslk(trslk);
						return ftsDocument;
					}

				case "all":
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
				default:
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
			}
			long end = new Date().getTime();
			long time = end - start;
			log.info("专题监测统计表格后台所需时间" + time);
			return resultList;
		} catch (Exception e) {
			throw new OperationException("走势计算错误,message: ", e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.TREND_TIME);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}
	}

	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_TRENDMD5, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件趋势/情感走势")
	@SuppressWarnings("rawtypes")
	@EnableRedis
	@FormatResult
	@ApiOperation("走势后边的九条")
	@RequestMapping(value = "/trendMd5", method = RequestMethod.GET)
	public Object trendMd5(@ApiParam("专项id") @RequestParam(value = "specialId") String specialId,
						   @ApiParam("pageSize") @RequestParam(value = "pageSize", defaultValue = "9") Integer pageSize,
						   @ApiParam("类型") @RequestParam(value = "type", required = false) String type,
						   @ApiParam("md5Tag") @RequestParam(value = "md5Tag", required = false) String md5,
						   @ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
						   @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
						   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws TRSException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		// 这个不用管它是否排重 肯定查md5的
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			String groupName = specialProject.getSource();//多个以;隔开
			// 单一数据源排重
			boolean irSimflag = specialProject.isIrSimflag();
			//全网排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			//站内排重
			boolean sim = specialProject.isSimilar();
			// url排重
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeArray = DateUtil.formatTimeRange(timeRange);

			QueryBuilder statBuilder = null;
			if ("weibo".equals(type)) {
				statBuilder = specialProject.toNoTimeBuilderWeiBo(0, pageSize);
			} else {
				statBuilder = specialProject.toNoTimeBuilder(0, pageSize);
			}
			statBuilder.page(0, pageSize);
			if (!"ALL".equals(industry)) {
				statBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
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
				statBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
			}
			List<Map> resultList = new ArrayList<>();

			switch (type) {
				case "weibo":
					//找十个转发数量最多的原发 按时间排序
					statBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT, timeArray, Operator.Between);
//				statBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_MID, "", Operator.Equal);
					statBuilder.filterByTRSL(FtsFieldConst.FIELD_RETWEETED_MID+":(0 OR \"\")");
					statBuilder.setPageSize(10);
					statBuilder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
					statBuilder.setDatabase(Const.WEIBO);
					log.info(statBuilder.asTRSL());
					statBuilder.setServer(specialProject.isServer());
					if(StringUtil.isNotEmpty(groupName)){
						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
								.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
					}
					InfoListResult infoListResult = commonListService.queryPageList(statBuilder,sim,irSimflag,irSimflagAll,Const.GROUPNAME_WEIBO,type,UserUtils.getUser(),true);
					PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
					List<FtsDocumentCommonVO> ftsQueryWeiBo = content.getPageItems();

//				List<FtsDocumentStatus> ftsQueryWeiBo = hybase8SearchService.ftsQuery(statBuilder,
//						FtsDocumentStatus.class, sim, irSimflag,irSimflagAll,"special");
					SortListAll sortListWeiBo = new SortListAll();
					//按时间排序
					Collections.sort(ftsQueryWeiBo, sortListWeiBo);
					// 防止这个的第一条和时间的那一条重复
					//微博走势 不走special/chart/trendTime接口，不需要去掉第一条数据
					for (FtsDocumentCommonVO ftsStatus : ftsQueryWeiBo) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsStatus.getSimCount(), ftsStatus));
					}
//				for (FtsDocumentStatus ftsStatus : ftsQueryWeiBo) {
//					resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsStatus.getSim(), ftsStatus));
//				}
					return resultList;
				case "tradition":
				/*
				 * String trsl = FtsFieldConst.FIELD_GROUPNAME +
				 * ":((国内新闻) OR (国内论坛) OR (国内新闻_手机客户端) OR (国内新闻_电子报) OR (国内博客))"
				 * ; statBuilder.filterByTRSL(trsl);
				 */
					List<FtsDocumentCommonVO> listChuan = new ArrayList<>();
					// statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,
					// "国内新闻*", Operator.Equal);
					if (statBuilder.isServer()) {
						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻%", Operator.Equal);
					} else {
						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻*", Operator.Equal);
					}
					// for (String[] time : timeList) {

					QueryBuilder queryFts = new QueryBuilder();
					String trsl = statBuilder.asTRSL();
					queryFts.filterByTRSL(trsl);
					// 不同小时间段
					// queryFts.filterField(FtsFieldConst.FIELD_URLTIME, time,
					// Operator.Between);
					queryFts.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
					queryFts.setPageSize(10);
					queryFts.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					queryFts.setDatabase(Const.HYBASE_NI_INDEX);
					log.info(queryFts.asTRSL());
					queryFts.setServer(specialProject.isServer());
					if(StringUtil.isNotEmpty(md5)){
						queryFts.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.NotEqual);
					}
					//找原发
//				if(StringUtil.isNotEmpty(groupName)){
//					queryFts.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
//							.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
//				}
					GroupResult categoryChuan = commonListService.categoryQuery(queryFts,sim,irSimflag,irSimflagAll,FtsFieldConst.FIELD_MD5TAG,"special",Const.TYPE_NEWS);
//				GroupResult categoryChuan = hybase8SearchService.categoryQuery(queryFts, sim, irSimflag,irSimflagAll,
//						FtsFieldConst.FIELD_MD5TAG,"special", Const.HYBASE_NI_INDEX);
					log.info(queryFts.asTRSL());
					List<GroupInfo> groupList = categoryChuan.getGroupList();
					if (groupList != null && groupList.size() > 0) {
						for (GroupInfo groupInfo : groupList) {
							QueryBuilder queryMd5 = new QueryBuilder();
							// 小时间段里MD5分类统计 时间排序取第一个结果 去查
							queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
							// queryMd5.filterField(FtsFieldConst.FIELD_URLTIME,
							// time, Operator.Between);
							queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
							queryMd5.orderBy(FtsFieldConst.FIELD_URLTIME, false);
							if (specialProject.isServer()) {
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻%", Operator.Equal);
							} else {
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻*", Operator.Equal);
							}
							queryMd5.setDatabase(Const.HYBASE_NI_INDEX);
							queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
							log.info(queryMd5.asTRSL());

							final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
							String trslk = "redisKey" + pageId;
							RedisUtil.setString(trslk, queryMd5.asTRSL());
							if(StringUtil.isNotEmpty(groupName)){
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
										.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
							}
							InfoListResult infoListResult2 = commonListService.queryPageList(queryMd5,sim,irSimflag,irSimflagAll,Const.TYPE_NEWS,"special",UserUtils.getUser(),true);
							PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
							List<FtsDocumentCommonVO> ftsQueryChuan = content2.getPageItems();
							//找原发
//						List<FtsDocument> ftsQueryChuan = hybase8SearchService.ftsQuery(queryMd5, FtsDocument.class,
//								sim, irSimflag,irSimflagAll,"special");
							// 再取第一个MD5结果集的第一个数据
							if (ftsQueryChuan != null && ftsQueryChuan.size() > 0) {
								ftsQueryChuan.get(0).setSimCount((int) groupInfo.getCount());
								ftsQueryChuan.get(0).setTrslk(trslk);
								listChuan.add(ftsQueryChuan.get(0));
							}
						}
					}
					// }

					// 按urltime降序
					SortListAll sort = new SortListAll();
					// Collections.sort(listChuan, sort);
					Collections.sort(listChuan, sort);
					// 防止这个的第一条和时间的那一条重复
					if (listChuan.size() > 0) {
						listChuan.remove(0);
					}
					for (FtsDocumentCommonVO ftsDocument : listChuan) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsDocument.getSimCount(), ftsDocument));
					}
//				for (FtsDocument ftsDocument : listChuan) {
//					resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsDocument.getSim(), ftsDocument));
//				}
					return resultList;
				case "weixin":
					List<FtsDocumentCommonVO> listweixin = new ArrayList<>();
					QueryBuilder queryFts2 = new QueryBuilder();
					String trsl2 = statBuilder.asTRSL();
					queryFts2.filterByTRSL(trsl2);
					// 不同小时间段
					queryFts2.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
					queryFts2.setPageSize(10);
					queryFts2.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					log.info(queryFts2.asTRSL());
					queryFts2.setServer(specialProject.isServer());
					if(StringUtil.isNotEmpty(md5)){
						queryFts2.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.NotEqual);
					}
					GroupResult categoryweixin = commonListService.categoryQuery(queryFts2,sim,irSimflag,irSimflagAll,FtsFieldConst.FIELD_MD5TAG,"special",Const.GROUPNAME_WEIXIN);
					log.info(queryFts2.asTRSL());
					List<GroupInfo> groupListweixin = categoryweixin.getGroupList();
					if (groupListweixin != null && groupListweixin.size() > 0) {
						for (GroupInfo groupInfo : groupListweixin) {
							QueryBuilder queryMd5 = new QueryBuilder();
							// 小时间段里MD5分类统计 时间排序取第一个结果 去查
							queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
							queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
							queryMd5.orderBy(FtsFieldConst.FIELD_URLTIME, false);
//							queryMd5.setDatabase(Const.WECHAT_COMMON);
							queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
							log.info(queryMd5.asTRSL());

							final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
							String trslk = "redisKey" + pageId;
							RedisUtil.setString(trslk, queryMd5.asTRSL());
							if(StringUtil.isNotEmpty(groupName)){
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
										.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
							}
							InfoListResult infoListResult2 = commonListService.queryPageList(queryMd5,sim,irSimflag,irSimflagAll,Const.TYPE_WEIXIN_GROUP,"special",UserUtils.getUser(),true);
							PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
							List<FtsDocumentCommonVO> ftsQueryChuan = content2.getPageItems();
							// 再取第一个MD5结果集的第一个数据
							if (ftsQueryChuan != null && ftsQueryChuan.size() > 0) {
								ftsQueryChuan.get(0).setSimCount((int) groupInfo.getCount());
								ftsQueryChuan.get(0).setTrslk(trslk);
								listweixin.add(ftsQueryChuan.get(0));
							}
						}
					}
					// }

					// 按urltime降序
					SortListAll sort2 = new SortListAll();
					// Collections.sort(listChuan, sort);
					Collections.sort(listweixin, sort2);
					// 防止这个的第一条和时间的那一条重复
					if (listweixin.size() > 0) {
						listweixin.remove(0);
					}
					for (FtsDocumentCommonVO ftsDocument : listweixin) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsDocument.getSimCount(), ftsDocument));
					}
					return resultList;
				case "zimeiti":
					List<FtsDocumentCommonVO> listzimeiti = new ArrayList<>();
					QueryBuilder queryFts3 = new QueryBuilder();
					String trsl3 = statBuilder.asTRSL();
					queryFts3.filterByTRSL(trsl3);
					// 不同小时间段
					queryFts3.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
					queryFts3.setPageSize(10);
					queryFts3.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					queryFts3.setServer(specialProject.isServer());
					if(StringUtil.isNotEmpty(md5)){
						queryFts3.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.NotEqual);
					}
					GroupResult category = commonListService.categoryQuery(queryFts3,sim,irSimflag,irSimflagAll,FtsFieldConst.FIELD_MD5TAG,"special",Const.GROUPNAME_ZIMEITI);
					log.info(queryFts3.asTRSL());
					List<GroupInfo> groupListzi = category.getGroupList();
					if (groupListzi != null && groupListzi.size() > 0) {
						for (GroupInfo groupInfo : groupListzi) {
							QueryBuilder queryMd5 = new QueryBuilder();
							// 小时间段里MD5分类统计 时间排序取第一个结果 去查
							queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
							queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
							queryMd5.orderBy(FtsFieldConst.FIELD_URLTIME, false);
//							queryMd5.setDatabase(Const.WECHAT_COMMON);
							queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
							log.info(queryMd5.asTRSL());

							final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
							String trslk = "redisKey" + pageId;
							RedisUtil.setString(trslk, queryMd5.asTRSL());
							if(StringUtil.isNotEmpty(groupName)){
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
										.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
							}
							InfoListResult infoListResult2 = commonListService.queryPageList(queryMd5,sim,irSimflag,irSimflagAll,Const.GROUPNAME_ZIMEITI,"special",UserUtils.getUser(),true);
							PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
							List<FtsDocumentCommonVO> ftsQueryChuan = content2.getPageItems();
							// 再取第一个MD5结果集的第一个数据
							if (ftsQueryChuan != null && ftsQueryChuan.size() > 0) {
								ftsQueryChuan.get(0).setSimCount((int) groupInfo.getCount());
								ftsQueryChuan.get(0).setTrslk(trslk);
								listzimeiti.add(ftsQueryChuan.get(0));
							}
						}
					}
					// }

					// 按urltime降序
					SortListAll sort3 = new SortListAll();
					// Collections.sort(listChuan, sort);
					Collections.sort(listzimeiti, sort3);
					// 防止这个的第一条和时间的那一条重复
					if (listzimeiti.size() > 0) {
						listzimeiti.remove(0);
					}
					for (FtsDocumentCommonVO ftsDocument : listzimeiti) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsDocument.getSimCount(), ftsDocument));
					}
					return resultList;
					case "all":
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
				default:
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
			}
			return resultList;
		} catch (Exception e) {
			throw new OperationException("走势计算错误,message: " + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			int fullHybase = logReids.getFullHybase();
			// String link = loginpool.getLink();
			if (fullHybase > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.TREND_MD5);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}
	}
	@SuppressWarnings("rawtypes")
	@EnableRedis
	@FormatResult
	@ApiOperation("事件脉络")
	@RequestMapping(value = "/affairVenation", method = RequestMethod.GET)
	public Object affairVenation(@ApiParam("专项id") @RequestParam(value = "specialId") String specialId,
						   @ApiParam("pageSize") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
						   @ApiParam("类型：新闻/微博/微信/自媒体号") @RequestParam(value = "groupName", required = false) String type,
						   @ApiParam("md5Tag") @RequestParam(value = "md5Tag", required = false) String md5,
						   @ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange) throws TRSException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		// 这个不用管它是否排重 肯定查md5的
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			String groupName = specialProject.getSource();//多个以;隔开
			// 单一数据源排重
			boolean irSimflag = specialProject.isIrSimflag();
			//全网排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			//站内排重
			boolean sim = specialProject.isSimilar();
			// url排重
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeArray = DateUtil.formatTimeRange(timeRange);

			QueryBuilder statBuilder = null;
			if (Const.GROUPNAME_WEIBO.equals(type)) {
				statBuilder = specialProject.toNoTimeBuilderWeiBo(0, pageSize);
			} else {
				statBuilder = specialProject.toNoTimeBuilder(0, pageSize);
			}
			statBuilder.page(0, pageSize);
			List<Map> resultList = new ArrayList<>();

			switch (type) {
				case Const.GROUPNAME_WEIBO:
					//找十个转发数量最多的原发 按时间排序
					statBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT, timeArray, Operator.Between);
//				statBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_MID, "", Operator.Equal);
					statBuilder.filterByTRSL(FtsFieldConst.FIELD_RETWEETED_MID+":(0 OR \"\")");
					statBuilder.setPageSize(10);
					statBuilder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
					statBuilder.setDatabase(Const.WEIBO);
					log.info(statBuilder.asTRSL());
					statBuilder.setServer(specialProject.isServer());
//					if(StringUtil.isNotEmpty(groupName)){
//						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
//								.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
//					}
					InfoListResult infoListResult = commonListService.queryPageList(statBuilder,sim,irSimflag,irSimflagAll,Const.GROUPNAME_WEIBO,type,UserUtils.getUser(),true);
					PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
					List<FtsDocumentCommonVO> ftsQueryWeiBo = content.getPageItems();

//				List<FtsDocumentStatus> ftsQueryWeiBo = hybase8SearchService.ftsQuery(statBuilder,
//						FtsDocumentStatus.class, sim, irSimflag,irSimflagAll,"special");
					SortListAll sortListWeiBo = new SortListAll();
					//按时间排序
					Collections.sort(ftsQueryWeiBo, sortListWeiBo);
					// 防止这个的第一条和时间的那一条重复
					//微博走势 不走special/chart/trendTime接口，不需要去掉第一条数据
					for (FtsDocumentCommonVO ftsStatus : ftsQueryWeiBo) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsStatus.getSimCount(), ftsStatus));
					}
//				for (FtsDocumentStatus ftsStatus : ftsQueryWeiBo) {
//					resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsStatus.getSim(), ftsStatus));
//				}
					return ftsQueryWeiBo;
				case "新闻":
					/*
					 * String trsl = FtsFieldConst.FIELD_GROUPNAME +
					 * ":((国内新闻) OR (国内论坛) OR (国内新闻_手机客户端) OR (国内新闻_电子报) OR (国内博客))"
					 * ; statBuilder.filterByTRSL(trsl);
					 */
					List<FtsDocumentCommonVO> listChuan = new ArrayList<>();
					// statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,
					// "国内新闻*", Operator.Equal);
					if (statBuilder.isServer()) {
						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻%", Operator.Equal);
					} else {
						statBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻*", Operator.Equal);
					}
					// for (String[] time : timeList) {

					QueryBuilder queryFts = new QueryBuilder();
					String trsl = statBuilder.asTRSL();
					queryFts.filterByTRSL(trsl);
					// 不同小时间段
					// queryFts.filterField(FtsFieldConst.FIELD_URLTIME, time,
					// Operator.Between);
					queryFts.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
					queryFts.setPageSize(10);
					queryFts.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					queryFts.setDatabase(Const.HYBASE_NI_INDEX);
					log.info(queryFts.asTRSL());
					queryFts.setServer(specialProject.isServer());
					if(StringUtil.isNotEmpty(md5)){
						queryFts.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.NotEqual);
					}
					//找原发
//				if(StringUtil.isNotEmpty(groupName)){
//					queryFts.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
//							.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
//				}
					GroupResult categoryChuan = commonListService.categoryQuery(queryFts,sim,irSimflag,irSimflagAll,FtsFieldConst.FIELD_MD5TAG,"special",Const.TYPE_NEWS);
//				GroupResult categoryChuan = hybase8SearchService.categoryQuery(queryFts, sim, irSimflag,irSimflagAll,
//						FtsFieldConst.FIELD_MD5TAG,"special", Const.HYBASE_NI_INDEX);
					log.info(queryFts.asTRSL());
					List<GroupInfo> groupList = categoryChuan.getGroupList();
					if (groupList != null && groupList.size() > 0) {
						for (GroupInfo groupInfo : groupList) {
							QueryBuilder queryMd5 = new QueryBuilder();
							// 小时间段里MD5分类统计 时间排序取第一个结果 去查
							queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
							// queryMd5.filterField(FtsFieldConst.FIELD_URLTIME,
							// time, Operator.Between);
							queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
							queryMd5.orderBy(FtsFieldConst.FIELD_URLTIME, false);
							if (specialProject.isServer()) {
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻%", Operator.Equal);
							} else {
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻*", Operator.Equal);
							}
							queryMd5.setDatabase(Const.HYBASE_NI_INDEX);
							queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
							log.info(queryMd5.asTRSL());

							final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
							String trslk = "redisKey" + pageId;
							RedisUtil.setString(trslk, queryMd5.asTRSL());
							if(StringUtil.isNotEmpty(groupName)){
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
										.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
							}
							InfoListResult infoListResult2 = commonListService.queryPageList(queryMd5,sim,irSimflag,irSimflagAll,Const.TYPE_NEWS,"special",UserUtils.getUser(),true);
							PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
							List<FtsDocumentCommonVO> ftsQueryChuan = content2.getPageItems();
							//找原发
//						List<FtsDocument> ftsQueryChuan = hybase8SearchService.ftsQuery(queryMd5, FtsDocument.class,
//								sim, irSimflag,irSimflagAll,"special");
							// 再取第一个MD5结果集的第一个数据
							if (ftsQueryChuan != null && ftsQueryChuan.size() > 0) {
								ftsQueryChuan.get(0).setSimCount((int) groupInfo.getCount());
								ftsQueryChuan.get(0).setTrslk(trslk);
								listChuan.add(ftsQueryChuan.get(0));
							}
						}
					}
					// }

					// 按urltime降序
					SortListAll sort = new SortListAll();
					// Collections.sort(listChuan, sort);
					Collections.sort(listChuan, sort);
					// 防止这个的第一条和时间的那一条重复
					if (listChuan.size() > 0) {
						listChuan.remove(0);
					}
					for (FtsDocumentCommonVO ftsDocument : listChuan) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsDocument.getSimCount(), ftsDocument));
					}
//				for (FtsDocument ftsDocument : listChuan) {
//					resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsDocument.getSim(), ftsDocument));
//				}
					return listChuan;
				case "微信":
					List<FtsDocumentCommonVO> listweixin = new ArrayList<>();
					QueryBuilder queryFts2 = new QueryBuilder();
					String trsl2 = statBuilder.asTRSL();
					queryFts2.filterByTRSL(trsl2);
					// 不同小时间段
					queryFts2.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
					queryFts2.setPageSize(10);
					queryFts2.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					log.info(queryFts2.asTRSL());
					queryFts2.setServer(specialProject.isServer());
					if(StringUtil.isNotEmpty(md5)){
						queryFts2.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.NotEqual);
					}
					GroupResult categoryweixin = commonListService.categoryQuery(queryFts2,sim,irSimflag,irSimflagAll,FtsFieldConst.FIELD_MD5TAG,"special",Const.GROUPNAME_WEIXIN);
					log.info(queryFts2.asTRSL());
					List<GroupInfo> groupListweixin = categoryweixin.getGroupList();
					if (groupListweixin != null && groupListweixin.size() > 0) {
						for (GroupInfo groupInfo : groupListweixin) {
							QueryBuilder queryMd5 = new QueryBuilder();
							// 小时间段里MD5分类统计 时间排序取第一个结果 去查
							queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
							queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
							queryMd5.orderBy(FtsFieldConst.FIELD_URLTIME, false);
//							queryMd5.setDatabase(Const.WECHAT_COMMON);
							queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
							log.info(queryMd5.asTRSL());

							final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
							String trslk = "redisKey" + pageId;
							RedisUtil.setString(trslk, queryMd5.asTRSL());
							if(StringUtil.isNotEmpty(groupName)){
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
										.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
							}
							InfoListResult infoListResult2 = commonListService.queryPageList(queryMd5,sim,irSimflag,irSimflagAll,Const.TYPE_WEIXIN_GROUP,"special",UserUtils.getUser(),true);
							PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
							List<FtsDocumentCommonVO> ftsQueryChuan = content2.getPageItems();
							// 再取第一个MD5结果集的第一个数据
							if (ftsQueryChuan != null && ftsQueryChuan.size() > 0) {
								ftsQueryChuan.get(0).setSimCount((int) groupInfo.getCount());
								ftsQueryChuan.get(0).setTrslk(trslk);
								listweixin.add(ftsQueryChuan.get(0));
							}
						}
					}
					// }

					// 按urltime降序
					SortListAll sort2 = new SortListAll();
					// Collections.sort(listChuan, sort);
					Collections.sort(listweixin, sort2);
					// 防止这个的第一条和时间的那一条重复
					if (listweixin.size() > 0) {
						listweixin.remove(0);
					}
					for (FtsDocumentCommonVO ftsDocument : listweixin) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsDocument.getSimCount(), ftsDocument));
					}
					return listweixin;
				case "自媒体号":
					List<FtsDocumentCommonVO> listzimeiti = new ArrayList<>();
					QueryBuilder queryFts3 = new QueryBuilder();
					String trsl3 = statBuilder.asTRSL();
					queryFts3.filterByTRSL(trsl3);
					// 不同小时间段
					queryFts3.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
					queryFts3.setPageSize(10);
					queryFts3.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					queryFts3.setServer(specialProject.isServer());
					if(StringUtil.isNotEmpty(md5)){
						queryFts3.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.NotEqual);
					}
					GroupResult category = commonListService.categoryQuery(queryFts3,sim,irSimflag,irSimflagAll,FtsFieldConst.FIELD_MD5TAG,"special",Const.GROUPNAME_ZIMEITI);
					log.info(queryFts3.asTRSL());
					List<GroupInfo> groupListzi = category.getGroupList();
					if (groupListzi != null && groupListzi.size() > 0) {
						for (GroupInfo groupInfo : groupListzi) {
							QueryBuilder queryMd5 = new QueryBuilder();
							// 小时间段里MD5分类统计 时间排序取第一个结果 去查
							queryMd5.filterField(FtsFieldConst.FIELD_MD5TAG, groupInfo.getFieldValue(), Operator.Equal);
							queryMd5.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
							queryMd5.orderBy(FtsFieldConst.FIELD_URLTIME, false);
//							queryMd5.setDatabase(Const.WECHAT_COMMON);
							queryMd5.filterByTRSL(specialProject.toNoPagedAndTimeBuilder().asTRSL());
							log.info(queryMd5.asTRSL());

							final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
							String trslk = "redisKey" + pageId;
							RedisUtil.setString(trslk, queryMd5.asTRSL());
							if(StringUtil.isNotEmpty(groupName)){
								queryMd5.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
										.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
							}
							InfoListResult infoListResult2 = commonListService.queryPageList(queryMd5,sim,irSimflag,irSimflagAll,Const.GROUPNAME_ZIMEITI,"special",UserUtils.getUser(),true);
							PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
							List<FtsDocumentCommonVO> ftsQueryChuan = content2.getPageItems();
							// 再取第一个MD5结果集的第一个数据
							if (ftsQueryChuan != null && ftsQueryChuan.size() > 0) {
								ftsQueryChuan.get(0).setSimCount((int) groupInfo.getCount());
								ftsQueryChuan.get(0).setTrslk(trslk);
								listzimeiti.add(ftsQueryChuan.get(0));
							}
						}
					}
					// }

					// 按urltime降序
					SortListAll sort3 = new SortListAll();
					// Collections.sort(listChuan, sort);
					Collections.sort(listzimeiti, sort3);
					// 防止这个的第一条和时间的那一条重复
					if (listzimeiti.size() > 0) {
						listzimeiti.remove(0);
					}
					for (FtsDocumentCommonVO ftsDocument : listzimeiti) {
						resultList.add(MapUtil.putValue(new String[] { "num", "list" }, ftsDocument.getSimCount(), ftsDocument));
					}
					return listzimeiti;
				case "all":
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
				default:
					statBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
			}
			return resultList;
		} catch (Exception e) {
			throw new OperationException("走势计算错误,message: " + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			int fullHybase = logReids.getFullHybase();
			// String link = loginpool.getLink();
			if (fullHybase > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.TREND_MD5);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}
	}
	@EnableRedis
	@FormatResult
	@ApiOperation("热点信息")
	@RequestMapping(value = "/hotMessage", method = RequestMethod.GET)
	public Object hotMessage(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							  @ApiParam("专项id") @RequestParam(value = "specialId", required = true) String specialId,
							  @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
							  @RequestParam(value = "source", defaultValue = "ALL", required = false) String source)
			throws TRSException {
		pageSize = pageSize>=1?pageSize:10;
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		String userName = UserUtils.getUser().getUserName();
		long startTime = System.currentTimeMillis();
		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		ObjectUtil.assertNull(specialProject, "专题ID");
		if (StringUtils.isBlank(timeRange)) {
			timeRange = specialProject.getTimeRange();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
				timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
			}
		}
		// 跟统计表格一样 如果来源没选 就不查数据
		source = CommonListChartUtil.changeGroupName(source);
		List<Map<String, Object>> result = specialChartAnalyzeService.getHotListMessage(source,
				specialProject, timeRange,pageSize);
		return result;
	}
	/**
	 * 网民参与趋势图
	 *
	 * @param timeRange
	 * @param specialId
	 * @param area
	 * @param industry
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_NETTENDENCY, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件趋势/信息走势图/网民参与趋势")
	@EnableRedis
	@FormatResult
	@ApiOperation("网民参与趋势图")
	@RequestMapping(value = "/netTendency", method = RequestMethod.GET)
	public Object netTendency(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							  @ApiParam("专项id") @RequestParam(value = "specialId", required = true) String specialId,
							  @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
							  @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							  @ApiParam("数据展示类型") @RequestParam(value = "showType", required = false,defaultValue = "") String showType)
			throws TRSException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			// long start = new Date().getTime();
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
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
				topicSearchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
			}
			Map<String, Object> userTendencyNew2 = specialChartAnalyzeService.getTendencyNew2(
					topicSearchBuilder.asTRSL(), specialProject, ChartType.NETTENDENCY.getType(), timeRange,showType);
			long end = new Date().getTime();
			long time = end - start;
			log.info("用户参与趋势图后台所需时间" + time);
			SpecialParam specParam = getSpecParam(specialProject);
			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area,
					ChartType.NETTENDENCY.getType(), specParam.getFirstName(), specParam.getSecondName(),
					specParam.getThirdName(), "无锡");
			Map<String, Object> map = new HashMap<>();
			map.put("data", userTendencyNew2);
			map.put("param", chartParam);
			return map;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long endFinally = new Date().getTime();
			int timeApi = (int) (endFinally - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(endFinally);
			logReids.setFullBak(timeApi);
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.NET_TENDENCY);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}

	}

	/**
	 * 媒体参与趋势图
	 *
	 * @param timeRange
	 * @param specialId
	 * @param area
	 * @param industry
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_METATENDENCY, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件趋势/信息走势图/媒体参与趋势")
	@EnableRedis
	@FormatResult
	@ApiOperation("媒体参与趋势图")
	@RequestMapping(value = "/metaTendency", method = RequestMethod.GET)
	public Object metaTendency(@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							   @ApiParam("专项id") @RequestParam(value = "specialId", required = true) String specialId,
							   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
							   @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							   @ApiParam("数据展示类型") @RequestParam(value = "showType", required = false,defaultValue = "") String showType)
			throws TRSException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
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
				topicSearchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
			}
			Map<String, Object> userTendencyNew2 = specialChartAnalyzeService.getTendencyNew2(
					topicSearchBuilder.asTRSL(), specialProject, ChartType.METATENDENCY.getType(), timeRange,showType);
			long end = new Date().getTime();
			long time = end - start;
			log.info("媒体参与趋势图后台所需时间" + time);
			SpecialParam specParam = getSpecParam(specialProject);
			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area,
					ChartType.METATENDENCY.getType(), specParam.getFirstName(), specParam.getSecondName(),
					specParam.getThirdName(), "无锡");
			Map<String, Object> map = new HashMap<>();
			map.put("data", userTendencyNew2);
			map.put("param", chartParam);
			return map;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long endFinally = new Date().getTime();
			int timeApi = (int) (endFinally - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(endFinally);
			logReids.setFullBak(timeApi);
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.META_TENDENCY);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}
	}

	/**
	 * 情感分析曲线趋势图
	 *
	 * @param specialId
	 * @param timeRange
	 * @param industry
	 * @param area
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_VOLUME, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件趋势/情感走势")
	@EnableRedis
	@FormatResult
	@ApiOperation("情感分析曲线趋势图")
	@RequestMapping(value = "/volume", method = RequestMethod.GET)
	public Object getVolumeNew(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
							   @ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							   @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
							   @ApiParam("数据展示类型") @RequestParam(value = "showType", defaultValue = "") String showType) throws TRSException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			String groupName = specialProject.getSource();
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
			boolean sim = specialProject.isSimilar();
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			if(StringUtil.isNotEmpty(groupName)){
				searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
						.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
			}
			if (!"ALL".equals(industry)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
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
					searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
					// searchBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA
					// + ":" + contentArea);
				}
			}
			Map<String, Object> resultMap = new HashMap<>();
			try {
				Object volumeNew = specialChartAnalyzeService.getVolume(searchBuilder, timeRange, sim, irSimflag,irSimflagAll,showType);
				SpecialParam specParam = getSpecParam(specialProject);
				ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area, ChartType.VOLUME.getType(),
						specParam.getFirstName(), specParam.getSecondName(), specParam.getThirdName(), "无锡");
				resultMap.put("data", volumeNew);
				resultMap.put("param", chartParam);
			} catch (Exception e) {
				e.printStackTrace();
				throw new OperationException("情感分析曲线趋势图计算错误,message: " + e);
			}
			return resultMap;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.VOLUME);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}

	}

	/**
	 * 引爆点
	 *
	 * @param specialId
	 *            专项id
	 * @param timeRange
	 *            时间范围
	 * @param industry
	 *            行业
	 * @param area
	 *            地域
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_TIPPINGPOINT, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件趋势/引爆点")
	@EnableRedis
	@FormatResult
	@ApiOperation("引爆点")
	@RequestMapping(value = "/tippingpoint", method = RequestMethod.GET)
	public Object getTippingPoint(@RequestParam(value = "specialId") String specialId,
								  @RequestParam(value = "timeRange", required = false) String timeRange,
								  @RequestParam(value = "industry", defaultValue = "ALL") String industry,
								  @RequestParam(value = "area", defaultValue = "ALL") String area) throws TRSException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {

			String spreadKey = DateUtil.startOfWeek() + specialId;
			String url = RedisFactory.getValueFromRedis(spreadKey);
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			ObjectUtil.assertNull(specialProject, "专题ID");
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();

			String groupName = specialProject.getSource();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
			boolean sim = specialProject.isSimilar();
			if (!"ALL".equals(industry)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
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
					searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
					// searchBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA
					// + ":" + contentArea);
				}
			}
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.filterByTRSL(searchBuilder.asTRSL());
			queryBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
			queryBuilder.page(0, 50);
			queryBuilder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
			queryBuilder.setServer(specialProject.isServer());
			if(StringUtil.isNotEmpty(groupName)){
				queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
						.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
			}
			String trl = queryBuilder.asTRSL();
			InfoListResult infoListResult2 = commonListService.queryPageList(queryBuilder,sim,irSimflag,irSimflagAll,Const.GROUPNAME_WEIBO,"special",UserUtils.getUser(),false);
			PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
			List<FtsDocumentCommonVO> list = content2.getPageItems();
//			List<FtsDocumentStatus> list = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentStatus.class, sim,
//					irSimflag,irSimflagAll,"special");
			QueryBuilder queryCommonBuilder = new QueryBuilder();
			queryCommonBuilder.filterByTRSL(searchBuilder.asTRSL());
			queryCommonBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
			queryCommonBuilder.page(0, 50);
			queryCommonBuilder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
			if(StringUtil.isNotEmpty(groupName)){
				queryCommonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
						.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
			}
//			String trl = queryCommonBuilder.asTRSL();
			//todo
			String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
			String trslk = "redisKey" + pageId;
			RedisUtil.setString(trslk, queryCommonBuilder.asTRSL());
			if (list.size() <= 0) {
				throw new NullException("获取url为空");
			}
			// url = list.get(0).baseUrl();
			FtsDocumentCommonVO status = list.get(0);
			RedisFactory.setValueToRedis(spreadKey, url, 7, TimeUnit.DAYS);
			queryCommonBuilder.setServer(specialProject.isServer());
			List<TippingPoint> tippingPoint = specialChartAnalyzeService.getTippingPoint(queryCommonBuilder, status,
					DateUtil.stringToDate(timeArray[0], DateUtil.yyyyMMddHHmmss),sim,irSimflag,irSimflagAll);
			if (tippingPoint != null && tippingPoint.size() > 0) {
				for (TippingPoint point : tippingPoint) {
					point.setTrslk(trslk);
				}
			}

			Map<String, Object> result = new HashMap<>();
			result.put("specialName", specialProject.getSpecialName());
			result.put("data", tippingPoint);
			return result;
			// return tippingPoint;
		} catch (NullException n) {
			log.error("获取url为空", n);
			return null;
		} catch (Exception e) {
			throw new OperationException("引爆点计算错误,message: ", e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.TIPPING_POINT);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}
	}

	/**
	 * 微博传播路径
	 *
	 * @param specialId
	 * @param timeRange
	 * @param industry
	 * @param area
	 * @return
	 * @throws Exception
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("微博传播路径")
	@RequestMapping(value = "/weiboPath", method = RequestMethod.GET)
	public Object weiboPath(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
							@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							@ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							@ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws Exception {

		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		ObjectUtil.assertNull(specialProject, "专题ID");
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		if (StringUtils.isBlank(timeRange)) {
			timeRange = specialProject.getTimeRange();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
				timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
			}
		}
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		boolean sim = specialProject.isSimilar();
		String source = specialProject.getSource();
		if (!source.contains("微博") && !"ALL".equals(source)) {
			return null;
		}

		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();

		if (!"ALL".equals(industry)) {
			searchBuilder.filterField(ESFieldConst.IR_SRESERVED2, industry.split(";"), Operator.Equal);
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
				searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
				// searchBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA +
				// ":" + contentArea);
			}
		}
		searchBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
		searchBuilder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
		searchBuilder.setDatabase(Const.WEIBO);
		log.info(searchBuilder.asTRSL());

		searchBuilder.setPageSize(9999);
		List<SinaUser> list = hybase8SearchService.ftsQuery(searchBuilder, SinaUser.class, true, irSimflag,irSimflagAll,"special");
		String pageId = GUIDGenerator.generate(SpecialChartAnalyzeController.class);
		String trslk = "redisKey" + pageId;
		RedisUtil.setString(trslk, searchBuilder.asTRSL());
		if (list.size() <= 0) {
			return null;
		}
		// String url = list.get(0).baseUrl();
		SinaUser sinaUser = list.get(0);
		SinaUser user = specialChartAnalyzeService.url(searchBuilder.asTRSL(), sinaUser, timeArray, sim, irSimflag,irSimflagAll);

		Map<String, Object> result = new HashMap<>();
		result.put("trslk", trslk);
		result.put("data", user);
		return result;
	}
	/**
	 * 情绪统计
	 *
	 * @param specialId
	 * @param timeRange
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_USERVIEWS, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件分析/网友观点")
	@EnableRedis
	@FormatResult
	@ApiOperation("情绪统计")
	@RequestMapping(value = "/moodStatistics")
	public Object moodStatistics(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
							@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange) throws Exception {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			SpecialParam specParam = getSpecParam(specialProject);
			return specialChartAnalyzeService.getMoodStatistics(specialProject, timeRange,specParam);
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.USER_VIEWS);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}

	}
	/**
	 * 网友观点
	 *
	 * @param specialId
	 * @param timeRange
	 * @param industry
	 * @param area
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_USERVIEWS, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件分析/网友观点")
	@EnableRedis
	@FormatResult
	@ApiOperation("网友观点")
	@RequestMapping(value = "/userViews")
	public Object userViews(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
							@ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							@ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							@ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws Exception {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			SpecialParam specParam = getSpecParam(specialProject);
			HashMap<String, Object> result = specialChartAnalyzeService.getUserViewsData(specialProject, timeRange,
					industry, area, specParam);
			return result;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.USER_VIEWS);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}

	}

	@Deprecated
	public Object userViewsDeprecated(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
									  @ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
									  @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
									  @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws Exception {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			QueryBuilder searchBuilder = specialProject.toNoPagedBuilderWeiBo();
			boolean sim = specialProject.isSimilar();
			if (!"ALL".equals(industry)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
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
					searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
				}
			}
			List<ViewEntity> userViewsData = null;
			Map<String, List<ViewEntity>> returnMap = new HashMap<>();
			List<ViewEntity> zhengmian = new ArrayList<>();
			List<ViewEntity> fumian = new ArrayList<>();
			List<ViewEntity> zhongxing = new ArrayList<>();
			try {
				userViewsData = specialChartAnalyzeService.getUserViewsData(specialProject, searchBuilder, timeArray,
						sim);
				for (ViewEntity userViewsDatum : userViewsData) {
					if (userViewsDatum.getAppraise().equals("正面")) {
						zhengmian.add(userViewsDatum);
					} else if (userViewsDatum.getAppraise().equals("负面")) {
						fumian.add(userViewsDatum);
					} else {
						zhongxing.add(userViewsDatum);
					}
				}
				returnMap.put("zhengMian", zhengmian);
				returnMap.put("fuMian", fumian);
				returnMap.put("zhongXing", zhongxing);
				if (zhengmian.size() < 1 && fumian.size() < 1 && zhongxing.size() < 1) {
					return null;
				}
			} catch (Exception e) {
				throw new OperationException("网友观点计算错误,message: ", e);
			}
			return returnMap;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.USER_VIEWS);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}

	}

	/**
	 * 热词探索 （目前仅做传统库）
	 *
	 * @param specialId
	 * @param timeRange
	 * @param industry
	 * @param area
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_TOPICEVOEXPLOR, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件趋势/热词探索")
	@EnableRedis
	@FormatResult
	@ApiOperation("热词探索")
	@RequestMapping(value = "/topicEvoExplor", method = RequestMethod.GET)
	public Object topicEvoExplor(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
								 @ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
								 @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
								 @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
								 @ApiParam("文章类型（全部：all；新闻：news；微博：weibo；微信：weixin；客户端：app）") @RequestParam(value = "articleType", defaultValue = "all") String articleType)
			throws TRSException, TRSSearchException {
		LogPrintUtil loginpool = new LogPrintUtil();
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			ObjectUtil.assertNull(specialProject, "专题ID");
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//跨数据源排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
					timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
				}
			}
			QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
			boolean sim = specialProject.isSimilar();
			searchBuilder.setPageSize(50);
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			searchBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
			//处理来源
			String groupName = specialProject.getSource();
			if (!"ALL".equals(groupName)){
				String[] split = groupName.split(";");
				List<String> choices = Arrays.asList(split);
				List<String> whole = Arrays.asList(Const.TYPE_NEWS.split(";"));
				//取交集
				List<String> result = whole.stream().filter(item -> choices.contains(item)).collect(toList());
				if (ObjectUtil.isEmpty(result)){
					//未选中传统类来源
					return null;
				}
				split = result.toArray(new String[result.size()]);
				groupName = StringUtil.join(split,";");
			}else {
				groupName = Const.TYPE_NEWS;
			}
			searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace("境外媒体","国外新闻").split(";"),Operator.Equal);
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
				searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
				// searchBuilder.filterByTRSL(FtsFieldConst.CATALOG_AREANEW +
				// ":" + contentArea);
			}
			searchBuilder.page(0, 100);
			GroupResult categoryQuery = commonListService.categoryQuery(searchBuilder,sim,irSimflag,irSimflagAll,FtsFieldConst.FIELD_KEYWORDS,"special",Const.TYPE_NEWS);
//			GroupResult categoryQuery = hybase8SearchService.categoryQuery(searchBuilder, sim, irSimflag,irSimflagAll,
//					FtsFieldConst.FIELD_KEYWORDS,"special", Const.HYBASE_NI_INDEX);
			// 数据清理
			List<Topic> returnList = new ArrayList<>();
			List<GroupInfo> categoryList = categoryQuery.getGroupList();
			for (int j = 0; j < categoryList.size(); j++) {
				GroupInfo groupInfo = categoryList.get(j);
				String fieldValue = groupInfo.getFieldValue();
				//过滤key为空的
				if (Const.NOT_KEYWORD.contains(fieldValue) ||StringUtils.isBlank(fieldValue)) {
					categoryList.remove(j);
					j--;
				}
			}
			// 初始化集合，用于全部的演变词做排重
			List<String> totalWords = new ArrayList<String>();
			// 演变 一级 5个词
			List<GroupInfo> list = null;
			if (categoryList.size() > 5) {
				list = categoryList.subList(0, 5);
			} else {
				list = categoryList;
			}
			for (GroupInfo groupInfo : list) {
				if (StringUtil.isNotEmpty(groupInfo.getFieldValue())) {
					totalWords.add(groupInfo.getFieldValue());
				}
			}
			// 创建专题时的排除词也放里边
			String excludeWords = specialProject.getExcludeWords();
			if (StringUtil.isNotEmpty(excludeWords)) {
				String[] split = excludeWords.split(";");
				for (String word : split) {
					if (StringUtil.isNotEmpty(word)) {
						totalWords.add(word);
					}
				}
			}

			for (int i = 0; i < list.size(); i++) {
				Topic topic = new Topic();
				String value = list.get(i).getFieldValue();
				if (value.endsWith("html")) {
					list.remove(i);
					break;
				}
				if (value.contains(";")) {
					String[] split = value.split(";");
					list.get(i).setFieldValue(split[split.length - 1]);
				}
				if (value.contains("\\")) {
					String[] split = value.split("\\\\");
					list.get(i).setFieldValue(split[split.length - 1]);
				}
				if (value.contains(".")) {
					String[] split = value.split("\\.");
					list.get(i).setFieldValue(split[split.length - 1]);
				}

				String fieldValue = list.get(i).getFieldValue();

				QueryBuilder builder = new QueryBuilder();
				String startTime = null;
				String endTime = null;
				String startSpe = specialProject.getStart();
				String end = specialProject.getEnd();
				Date startSpecTime = specialProject.getStartTime();
				Date endSpecTime = specialProject.getEndTime();
				if (StringUtils.isNotBlank(startSpe) && StringUtils.isNotBlank(end) && (!"0".equals(start))
						&& (!"0".equals(end))) {
					startTime = startSpe;
					endTime = end;
				} else {
					startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(startSpecTime);
					endTime = new SimpleDateFormat("yyyyMMddHHmmss").format(endSpecTime);
				}
				// 利用演变 一级5个词 分别放进内容 加上 专题时间 再次分类统计
				builder.filterField(FtsFieldConst.FIELD_CONTENT, fieldValue, Operator.Equal);
				builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { startTime, endTime }, Operator.Between);
				builder.page(0, 100);

				// 排除建专题的关键词
				// 拦截专项检测加号空格 start
				String anyKeywords = specialProject.getAnyKeywords();
				if (StringUtils.isNotBlank(anyKeywords)) {
					String[] split = anyKeywords.split(",");
					String splitNode = "";
					for (int j = 0; j < split.length; j++) {
						if (StringUtil.isNotEmpty(split[j])) {
							if (split[j].endsWith(";")) {
								split[j] = split[j].substring(0, split[j].length() - 1);
							}
							splitNode += split[j] + ",";
						}
					}
					anyKeywords = splitNode.substring(0, splitNode.length() - 1);
					// System.out.println("话题演变关键词1："+anyKeywords);

					if (anyKeywords.contains(fieldValue)) {
						anyKeywords = anyKeywords.replaceAll(fieldValue, " ");
					}
					// System.out.println("话题演变关键词2："+anyKeywords);
					// 防止全部关键词结尾为;报错
					StringBuilder childBuilder = new StringBuilder();
					String replaceAnyKey = "";
					if (anyKeywords.endsWith(";")) {
						replaceAnyKey = anyKeywords.substring(0, anyKeywords.length() - 1);
						childBuilder.append("((\"")
								.append(replaceAnyKey.replaceAll(",", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
								.append("\"))");
					} else {
						childBuilder.append("((\"")
								.append(anyKeywords.replaceAll(",", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
								.append("\"))");
					}

					builder.filterField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.NotEqual);
				} else {
					builder.filterByTRSL(specialProject.getTrsl());
				}
				// System.out.println("更改逻辑后话题演变表达式："+builder.asTRSL());
//				GroupResult groupInfos = hybase8SearchService.categoryQuery(builder, sim, irSimflag,irSimflagAll,
//						FtsFieldConst.FIELD_KEYWORDS,"special",Const.HYBASE_NI_INDEX);
				GroupResult groupInfos = commonListService.categoryQuery(builder,sim,irSimflag,irSimflagAll,FtsFieldConst.FIELD_KEYWORDS,"special",Const.TYPE_NEWS);
				List<GroupInfo> groupList = groupInfos.getGroupList();

				for (int j = 0; j < groupList.size(); j++) {
					GroupInfo groupInfo = groupList.get(j);
					Iterator<String> iterator = totalWords.iterator();
					boolean Exist = false;
					while (iterator.hasNext()) {
						if (iterator.next().contains(groupInfo.getFieldValue())) {
							Exist = true;
						}
					}
					if (Const.NOT_KEYWORD.contains(groupInfo.getFieldValue())
							|| fieldValue.equals(groupInfo.getFieldValue()) || Exist) {
						groupList.remove(j);
						j--;
					}
				}
				// 演变二级 取出6个词
				List<GroupInfo> groupInfoList = null;
				if (groupList.size() > 6) {
					groupInfoList = groupList.subList(0, 6);
				} else {
					groupInfoList = groupList;
				}
				List<Topic> childrens = new ArrayList<>();
				for (GroupInfo groupInfo : groupInfoList) {
					Topic tc = new Topic();
					tc.setName(groupInfo.getFieldValue());
					tc.setCount(groupInfo.getCount());
					childrens.add(tc);
					totalWords.add(groupInfo.getFieldValue());
				}
				topic.setName(fieldValue);
				topic.setCount(list.get(i).getCount());
				topic.setChildren(childrens);
				returnList.add(topic);
			}

			long end = new Date().getTime();
			long time = end - start;
			log.info("话题演变所需时间" + time);
			SpecialParam specParam = getSpecParam(specialProject);
			ChartParam chartParam = new ChartParam(specialId, timeRange, industry, area,
					ChartType.TOPICEVOEXPLOR.getType(), specParam.getFirstName(), specParam.getSecondName(),
					specParam.getThirdName(), "无锡");
			Map<String, Object> map = new HashMap<>();
			map.put("specialName", specialProject.getSpecialName());
			map.put("data", returnList);
			map.put("param", chartParam);
			return map;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.TOPIC_EVO_EXPLOR);
			}
			log.info("调用接口用了" + timeApi + "ms");
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
  /*  @FormatResult
    @ApiOperation("专题监测统计表格")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "specialId", value = "专项ID", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "days", value = "时间", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "industryType", value = "行业类型", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "area", value = "内容地域", dataType = "String", paramType = "query", required = false) })
    @RequestMapping(value = "/stattotal", method = RequestMethod.GET)
    public Object getStatTotal(@ApiParam(value = "专项ID") @RequestParam(value = "specialId") String specialId,
                               @ApiParam(value = "时间 1. [0-9]*[hdwmy] \n 2.yyyy-MM-dd HH:mm:ss") @RequestParam(value = "days") String days,
                               @ApiParam(value = "行业类型") @RequestParam(value = "industryType", defaultValue = "ALL") String industryType,
                               @ApiParam(value = "内容地域") @RequestParam(value = "area", defaultValue = "ALL") String area)
            throws TRSException {
        log.warn("图表分析统计表格stattotal接口开始调用");
        long start = new Date().getTime();
        try {
            SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
            ObjectUtil.assertNull(specialProject, "专题ID");
            if (StringUtils.isBlank(days)){
                days = specialProject.getTimeRange();
                if (StringUtils.isBlank(days)){
                    days = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
                    days += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
                }
            }
            String[] timeRange = DateUtil.formatTimeRange(days);
            List<ClassInfo> statToday = new ArrayList<>();

            List<ClassInfo> total = specialChartAnalyzeService.stattotal(specialProject, timeRange[0], timeRange[1],
                    industryType, area);
            Map<String, Object> putValue = MapUtil.putValue(new String[] { "今日", "总量" }, statToday, total);
            long end = new Date().getTime();
            long time = end - start;
            log.info("专题监测统计表格后台所需时间"+time);
            return putValue;
        } catch (Exception e) {
            throw new OperationException("专题监测统计表格错误,message: " + e);
        }
    }*/
	/**
	 * 图表跳列表
	 * @param specialId
	 * 			专项id
	 * @param timeRange
	 * 			高级筛选时间段
	 * @param industryType
	 * 			行业类型
	 * @param area
	 * 			地域分布
	 * @param chartType
	 * 			图表类型
	 * @param dateTime
	 * 			数据时间
	 * @param xType
	 * 			数据参数
	 * @param source
	 * 			数据来源
	 * @param pageNo
	 * 			页码
	 * @param pageSize
	 * 			步长
	 * @return
	 * @throws Exception
	 */
	//@EnableRedis 有文章删除的时候不能用,否则删除无效
	@FormatResult
	@ApiOperation("专题图表通用列表跳转方法")
	@RequestMapping(value = "/chart2list", method = RequestMethod.GET)
	public Object chart2list(@ApiParam("专题id")@RequestParam(value = "specialId") String specialId,
							 @ApiParam("高级筛选时间段") @RequestParam(value = "timeRange", defaultValue = "3d") String timeRange,
							 @ApiParam("行业类型")@RequestParam(value = "industryType", defaultValue = "ALL") String industryType,
							 @ApiParam("地域分布")@RequestParam(value = "area", defaultValue = "ALL") String area,
							 @ApiParam("图表类型")@RequestParam(value = "chartType") String chartType,
							 @ApiParam("热词：用户点击的外圈词（共30个）")@RequestParam(value = "thirdWord", required = false)String thirdWord,
							 @ApiParam("数据时间")@RequestParam(value = "dateTime", required = false) String dateTime,
							 @ApiParam("数据参数")@RequestParam(value = "xType",required = false ) String xType,
							 @ApiParam("数据来源")@RequestParam(value = "source",required = false ) String source,
							 @ApiParam("通用：keywords；人物：people；地域：location；机构：agency") @RequestParam(value = "entityType", defaultValue = "keywords") String entityType,
							 @ApiParam("排序")@RequestParam(value = "sort", defaultValue = "default", required = false) String sort,
							 @ApiParam("情感")@RequestParam(value = "emotion", defaultValue = "ALL", required = false) String emotion,
							 @ApiParam("结果中搜索") @RequestParam(value="fuzzyValue",required=false) String fuzzyValue,
							 @ApiParam("结果中搜索的范围")@RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope,
							 @ApiParam("页码")@RequestParam(value = "pageNo",defaultValue = "0") int pageNo,
							 @ApiParam("步长")@RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
							 @ApiParam("微博原发/转发")@RequestParam(value = "forwarPrimary",defaultValue = "") String forwarPrimary,
							 @ApiParam("是否导出") @RequestParam(value = "isExport", defaultValue = "false") boolean isExport,
							 @ApiParam("主回帖")@RequestParam(value = "invitationCard",defaultValue = "") String invitationCard
	) throws Exception{
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		SpecialProject specialProject = this.specialProjectNewRepository.findOne(specialId);
		//页面展示 国外新闻 变为 境外网站  2019-12-10
		if (StringUtil.isNotEmpty(source)){
			if ("境外网站".equals(source) || "境外媒体".equals(source)){
				source = "国外新闻";
			}else if ("微信".equals(source)){
				source = "国内微信";
			}else if ("新闻".equals(source)){
				source = "国内新闻";
			}else if ("客户端".equals(source)){
				source = "国内新闻_手机客户端";
			}else if ("论坛".equals(source)){
				source = "国内论坛";
			}else if ("博客".equals(source)){
				source = "国内博客";
			}else if ("电子报".equals(source)){
				source = "国内新闻_电子报";
			}
		}
		if (specialProject != null) {
			// 初步高级删选时间范围
			specialProject.setTimeRange(timeRange);
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			if (timeArray != null && timeArray.length == 2) {
				specialProject.setStart(timeArray[0]);
				specialProject.setEnd(timeArray[1]);
			}
			return this.specialChartAnalyzeService.getDataByChart(specialProject, industryType, area, chartType, dateTime, xType, source, entityType,sort,emotion,fuzzyValue,fuzzyValueScope,pageNo, pageSize,forwarPrimary,invitationCard,isExport,thirdWord);
		}
		return null;
	}
	/**
	 *  为前端返回各级专题名字
	 * @param specialProject
	 * @return
	 */
	public SpecialParam getSpecParam(SpecialProject specialProject){
		String firstName = "";
		String secondName = "";
		String thirdName = "";
		if (null != specialProject){
			if (null != specialProject.getGroupName()){
				SpecialSubject specialSubject = this.specialSubjectRepository.findOne(specialProject.getGroupId());
				if (null != specialSubject){
					int flag = specialSubject.getFlag();
					if (flag == 0){//一级
						firstName = specialSubject.getName();
					}else if (flag == 1){//二级
						SpecialSubject subject = this.specialSubjectRepository.findOne(specialSubject.getSubjectId());//一级
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

	/**
	 * 传统媒体传播路径分析图
	 * @since changjiang @ 2018年5月9日
	 * @param specialId
	 * @param timeRange
	 * @param industry
	 * @param area
	 * @param articleType
	 * @return
	 * @throws Exception
	 * @Return : Object
	 */
	@EnableRedis
	@FormatResult
	@ApiOperation("传统媒体传播路径分析图")
	@RequestMapping(value = "/pathByNews", method = RequestMethod.GET)
	public Object pathByNews(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
							 @ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
							 @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
							 @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area,
							 @ApiParam("文章类型（全部：all；新闻：news；微博：weibo；微信：weixin；客户端：app）") @RequestParam(value = "articleType", defaultValue = "all") String articleType)
			throws Exception {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			if (specialProject != null) {
				// url排重
				boolean irSimflag = specialProject.isIrSimflag();
				//跨数据源排重
				boolean irSimflagAll = specialProject.isIrSimflagAll();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = specialProject.getTimeRange();
					if (StringUtils.isBlank(timeRange)) {
						timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
						timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
					}
				}
				String[] timeArray = DateUtil.formatTimeRange(timeRange);
				if (timeArray != null && timeArray.length == 2) {
					specialProject.setStart(timeArray[0]);
					specialProject.setEnd(timeArray[1]);
				}

				// 根据时间升序,只要第一条
				QueryBuilder searchBuilder = specialProject.toBuilder(0, 1, true);
				if (!"ALL".equals(industry)) {
					searchBuilder.filterField(ESFieldConst.IR_SRESERVED2, industry.split(";"), Operator.Equal);
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
						// searchBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA
						// + ":" + contentArea);
						searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
					}
				}
				if (specialProject.isServer()) {
					searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻%", Operator.Equal);
				} else {
					searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻*", Operator.Equal);
				}
				searchBuilder.orderBy(ESFieldConst.IR_URLTIME, false);
				searchBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, false);
				searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);

				log.info("传统传播路径：" + searchBuilder.asTRSL());
				List<SpreadNewsEntity> list = hybase8SearchService.ftsQuery(searchBuilder, SpreadNewsEntity.class,
						specialProject.isSimilar(), irSimflag,irSimflagAll,"special");

				// 初步确定root节点
				SpreadNewsEntity root = null;
				if (list != null && list.size() > 0) {
					root = list.get(0);
					SpreadNewsEntity spreadNewsEntity = specialChartAnalyzeService.pathByNews(specialProject,
							searchBuilder, root, timeArray, irSimflag,irSimflagAll);
					SpreadNewsEntity spread = new SpreadNewsEntity();
					List<SpreadNewsEntity> entities = new ArrayList<>();
					entities.add(spreadNewsEntity);
					spread.setChildren(entities);
					spread.setName(specialProject.getSpecialName());
					return spread;
				}
			}
			return null;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.PATH_BY_NEWS);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}

	}
	@EnableRedis
	@FormatResult
	@ApiOperation("传播分析")
	@RequestMapping(value = "/spreadAnalysis", method = RequestMethod.GET)
	public Object spreadAnalysis(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
								   @ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
								 @ApiParam("类型") @RequestParam(value = "groupName", required = true) String groupName,
								   @ApiParam("每日最多展示网站个数，主要配合api，网察页面不需要理会此参数") @RequestParam(value = "isApi", defaultValue = "false",required = false) boolean isApi) throws OperationException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			if (specialProject != null) {
				// url排重
				boolean irSimflag = specialProject.isIrSimflag();
				boolean similar = specialProject.isSimilar();
				//跨数据源排重
				boolean irSimflagAll = specialProject.isIrSimflagAll();

				if (StringUtils.isBlank(timeRange)) {
					timeRange = specialProject.getTimeRange();
					if (StringUtils.isBlank(timeRange)) {
						timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
						timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
					}
				}
				String[] timeArray = DateUtil.formatTimeRange(timeRange);
				if (timeArray != null && timeArray.length == 2) {
					specialProject.setStart(timeArray[0]);
					specialProject.setEnd(timeArray[1]);
				}

				// 根据时间升序,只要第一条
				QueryBuilder searchBuilder = specialProject.toBuilder(0, 1, true);
//				if (specialProject.isServer()) {
//					searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻%", Operator.Equal);
//				} else {
//					searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻*", Operator.Equal);
//				}
				searchBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				searchBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, false);
//				searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
				return specialChartAnalyzeService.spreadAnalysis(searchBuilder, timeArray, similar, irSimflag,irSimflagAll,false,groupName);
			}
			return null;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.NEWS_SITE_ANALYSIS);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}
	}

	/**
	 * 新闻传播站点分析
	 *
	 * @date Created at 2018年9月5日 上午9:53:56
	 * @Author 谷泽昊
	 * @param specialId
	 * @param timeRange
	 * @param industry
	 * @param area
	 * @return
	 * @throws OperationException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_SELECT_ZHUANTI_NEWSSITEANALYSIS, systemLogType = SystemLogType.SPECIAL,systemLogOperationPosition="专题分析/${specialId}/事件趋势/新闻传播分析")
	@EnableRedis
	@FormatResult
	@ApiOperation("新闻传播站点分析")
	@RequestMapping(value = "/newsSiteAnalysis", method = RequestMethod.GET)
	public Object newsSiteAnalysis(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId,
								   @ApiParam("时间区间") @RequestParam(value = "timeRange", required = false) String timeRange,
								   @ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
								   @ApiParam("每日最多展示网站个数，主要配合api，网察页面不需要理会此参数") @RequestParam(value = "isApi", defaultValue = "false",required = false) boolean isApi,
								   @ApiParam("地域") @RequestParam(value = "area", defaultValue = "ALL") String area) throws OperationException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		try {
			SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
			if (specialProject != null) {
				// url排重
				boolean irSimflag = specialProject.isIrSimflag();
				boolean similar = specialProject.isSimilar();
				//跨数据源排重
				boolean irSimflagAll = specialProject.isIrSimflagAll();

				if (StringUtils.isBlank(timeRange)) {
					timeRange = specialProject.getTimeRange();
					if (StringUtils.isBlank(timeRange)) {
						timeRange = DateUtil.format2String(specialProject.getStartTime(), DateUtil.yyyyMMdd) + ";";
						timeRange += DateUtil.format2String(specialProject.getEndTime(), DateUtil.yyyyMMdd);
					}
				}
				String[] timeArray = DateUtil.formatTimeRange(timeRange);
				if (timeArray != null && timeArray.length == 2) {
					specialProject.setStart(timeArray[0]);
					specialProject.setEnd(timeArray[1]);
				}

				// 根据时间升序,只要第一条
				QueryBuilder searchBuilder = specialProject.toBuilder(0, 1, true);
				if (!"ALL".equals(industry)) {
					searchBuilder.filterField(ESFieldConst.IR_SRESERVED2, industry.split(";"), Operator.Equal);
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
						searchBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
					}
				}
				if (specialProject.isServer()) {
					searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻%", Operator.Equal);
				} else {
					searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻*", Operator.Equal);
				}
				searchBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				searchBuilder.orderBy(FtsFieldConst.FIELD_LOADTIME, false);
				searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
				return specialChartAnalyzeService.newsSiteAnalysis(searchBuilder, timeArray, similar, irSimflag,irSimflagAll,false);
			}
			return null;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			// String link = loginpool.getLink();
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.NEWS_SITE_ANALYSIS);
			}
			log.info("调用接口用了" + timeApi + "ms");
		}
	}

	@ApiOperation("饼图和柱状图数据导出接口")
	@PostMapping("/exportBarOrPieData")
	public void exportData(HttpServletResponse response,
						   @ApiParam("表头内容") @RequestParam(value = "dataType",required = true) String dataType,
						   @ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data) {
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			JSONArray array = JSONObject.parseArray(data);
			specialChartAnalyzeService.exportBarOrPieData(dataType,array).writeTo(outputStream);

		} catch (Exception e) {
			log.error("导出excel出错！", e);
		}

	}

	@ApiOperation("折线图数据导出接口")
	@PostMapping("/exportChartLineData")
	public void exportChartLine(HttpServletResponse response,
								@ApiParam("表头内容") @RequestParam(value = "dataType",required = true) String dataType,
								@ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data) {
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			JSONArray array = JSONObject.parseArray(data);
			specialChartAnalyzeService.exportChartLineData(dataType,array).writeTo(outputStream);

		} catch (Exception e) {
			log.error("导出excel出错！", e);
		}

	}

	@ApiOperation("词云图数据导出接口")
	@PostMapping("/exportWordCloudData")
	public void exportWordCloud(HttpServletResponse response,
								@ApiParam("导出数据分类") @RequestParam(value = "dataType", required = true) String dataType,
								@ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data) {
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			JSONArray array = JSONObject.parseArray(data);
			specialChartAnalyzeService.exportWordCloudData(dataType,array).writeTo(outputStream);

		} catch (Exception e) {
			log.error("导出excel出错！", e);
		}

	}

	@ApiOperation("地域图数据导出接口")
	@PostMapping("/exportMapData")
	public void exportMap(HttpServletResponse response,
						  @ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data) {
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			JSONArray array = JSONObject.parseArray(data);
			specialChartAnalyzeService.exportMapData(array).writeTo(outputStream);

		} catch (Exception e) {
			log.error("导出excel出错！", e);
		}

	}


}
