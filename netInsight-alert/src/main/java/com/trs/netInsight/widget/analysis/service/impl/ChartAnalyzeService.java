
package com.trs.netInsight.widget.analysis.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.trs.ckm.soap.AbsTheme;
import com.trs.ckm.soap.CkmSoapException;
import com.trs.dc.entity.TRSStatisticParams;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.ckm.ICkmService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.GroupWordInfo;
import com.trs.netInsight.support.fts.model.result.GroupWordResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.entity.*;
import com.trs.netInsight.widget.analysis.enums.ChartType;
import com.trs.netInsight.widget.analysis.enums.Top5Tab;
import com.trs.netInsight.widget.analysis.service.IChartAnalyzeService;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.base.enums.ESGroupName;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.spread.entity.GraphMap;
import com.trs.netInsight.widget.spread.entity.SinaUser;
import com.trs.netInsight.widget.spread.util.MultiKVMap;
import com.trs.netInsight.widget.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.trs.netInsight.config.constant.ChartConst.*;

/**
 * 图表分析服务
 * <p>
 * Created by mawen on 2017/12/5.
 */
@Service
@Slf4j
public class ChartAnalyzeService implements IChartAnalyzeService {

	@Autowired
	private IDistrictInfoService districtInfoService;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private FullTextSearch esSearchService;

	@Autowired
	private IInfoListService infoListService;

	@Autowired
	private ICkmService ckmService;

	@Autowired
	private ICommonListService commonListService;

	@Override
	public Object mediaLevel(QueryBuilder builder) throws TRSException {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object viaPreference(QueryBuilder builder, boolean sim, boolean irSimflag,boolean irSimflagAll ) throws TRSException {
		try {
			List<String> ticks = new ArrayList<>();
			List<Integer> value = new ArrayList<>();
			List<String> list = new ArrayList();
			List data = new ArrayList();
			int sum = 0;
			builder.setDatabase(Const.WEIBO);
			GroupResult via = hybase8SearchService.categoryQuery(builder.isServer(), builder.asTRSL(), sim, irSimflag,irSimflagAll,
					"IR_VIA", 6, Const.WEIBO);
			for (GroupInfo info : via) {
				if ("微博 weibo.com".equals(info.getFieldValue())) {
					info.setFieldValue("微博PC端");
				}
				// 尾巴处理
				ticks.add(info.getFieldValue());
				value.add((int) info.getCount());
				sum += info.getCount();
			}

			Map map = MapUtil.putValue(new String[] { "ticks", "value" }, ticks, value);
			data.add(map);
			String result = "";
			for (GroupInfo info : via) {
				// 尾巴处理
				// 创建一个数值格式化对象
				NumberFormat numberFormat = NumberFormat.getInstance();
				// 设置精确到小数点后2位
				numberFormat.setMaximumFractionDigits(2);
				result = numberFormat.format((float) (info.getCount()) / (float) sum * 100);
				list.add(result);
			}
			data.add(list);
			return data;
		} catch (Exception e) {
			throw new OperationException("查询出错");
		}
	}

	@Override
	public Map<String, Object> reportProcess(String hySql, String model) throws TRSSearchException {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		if ("INTRO".equals(model)) {
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.filterByTRSL(hySql);
			queryBuilder.filterField("IR_GROUPNAME", new String[] { "微博", "国内微信" }, Operator.NotEqual);
			queryBuilder.setPageSize(3);
			queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			long count = hybase8SearchService.ftsCount(queryBuilder, true, false,false,"special");
			GroupResult infoList = hybase8SearchService.categoryQuery(queryBuilder, true, false,false, "IR_SITENAME",
					Const.HYBASE_NI_INDEX);
			String resultStr = "";
			for (GroupInfo info : infoList) {
				resultStr += "、" + info.getFieldValue();
			}
			resultStr = resultStr.replaceFirst("、", "");
			dataMap.put("count", count);
			dataMap.put("siteName", resultStr);
		} else if ("SUMM".equals(model)) {
			QueryBuilder posQueryBuilder = new QueryBuilder();
			posQueryBuilder.filterByTRSL(hySql);
			posQueryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			// 总数
			long totalNum = hybase8SearchService.ftsCount(posQueryBuilder, true, false,false,"special");
			posQueryBuilder.filterField("IR_APPRAISE", "正面", Operator.Equal);
			// 正面条数
			long posNum = hybase8SearchService.ftsCount(posQueryBuilder, true, false,false,"special");
			QueryBuilder neQueryBuilder = new QueryBuilder();
			neQueryBuilder.filterByTRSL(hySql);
			neQueryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			neQueryBuilder.filterField("IR_APPRAISE", "负面", Operator.Equal);
			// 负面条数
			long neNum = hybase8SearchService.ftsCount(neQueryBuilder, true, false,false,"special");
			DecimalFormat df = new DecimalFormat("0.00");
			String posRat = df.format(((double) posNum / (double) totalNum) * 100) + "%";
			String neRat = df.format(((double) neNum / (double) totalNum) * 100) + "%";
			dataMap.put("count", totalNum);
			dataMap.put("posNum", posNum);
			dataMap.put("neNum", neNum);
			dataMap.put("posRat", posRat);
			dataMap.put("neRat", neRat);
		}
		return dataMap;
	}

	@Override
	public Object mediaActiveLevel(QueryBuilder builder,String source, String[] timeArray, boolean sim,
								   boolean irSimflag,boolean irSimflagAll) throws TRSException {
		try {

			Map<String, Object> map = new HashMap<>();
			QueryBuilder app = new QueryBuilder();
			app.filterByTRSL(builder.asTRSL());
			app.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
			app.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻_手机客户端", Operator.Equal);
			//跨数据源排重
			if (irSimflagAll){
				builder.filterField(FtsFieldConst.FIELD_IR_SIMFLAGALL,"0 OR \"\"",Operator.Equal);
			}
			log.error("builder:" + builder.asTRSL());
			log.error("app:" + app.asTRSL());

			GroupResult groupResultApp = hybase8SearchService.categoryQuery(builder.isServer(), app.asTRSL(), sim,
					irSimflag,irSimflagAll, FtsFieldConst.FIELD_SITENAME, 12, Const.HYBASE_NI_INDEX);
			QueryBuilder news = new QueryBuilder();
			news.filterByTRSL(builder.asTRSL());
			news.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻", Operator.Equal);
			log.error("builder:" + builder.asTRSL());
			log.error("news:" + news.asTRSL());
			GroupResult groupResultNews = hybase8SearchService.categoryQuery(builder.isServer(), news.asTRSL(), sim,
					irSimflag,irSimflagAll , FtsFieldConst.FIELD_SITENAME, 12, Const.HYBASE_NI_INDEX);

			map.put("news", groupResultNews);
			map.put("app", groupResultApp);

			return map;
		} catch (Exception e) {
			throw new OperationException("查询出错" + e);
		}
	}

	@Override
	public List<Map<String, Object>> getHotListMessage(String source, SpecialProject specialProject, String timerange, int pageSize) {
		return null;
	}

    @Override
    public Map<String, Object> getWebCountLine(SpecialProject specialProject, String timerange, String showType) throws TRSException {
        return null;
    }

	@Override
	public List<Map<String, Object>> getSentimentAnalysis(SpecialProject specialProject, String timerange, String viewType) throws TRSException {
		return null;
	}

	@Override
	public Object mediaActiveAccount(QueryBuilder builder, String source, String[] timeArray, boolean sim, boolean irSimflag, boolean irSimflagAll) throws TRSException {
		return null;
	}

	@Override
	public List<MBlogAnalyzeEntity> mBlogTop5(QueryBuilder builder, Top5Tab sort, boolean sim, boolean irSimflag,boolean irSimflagAll)
			throws TRSException {
		try {
			builder.orderBy(sort.getField(), true);
			// top5
			builder.page(0, 5);
			builder.setDatabase(Const.WEIBO);
			//跨数据源排重
			if (irSimflagAll){
				builder.filterField(FtsFieldConst.FIELD_IR_SIMFLAGALL,"0 OR \"\"",Operator.Equal);
			}
			log.info(builder.asTRSL());
			List<MBlogAnalyzeEntity> pagedList = hybase8SearchService.ftsQuery(builder, MBlogAnalyzeEntity.class, sim,
					irSimflag,irSimflagAll,"special");
			for (MBlogAnalyzeEntity mBlogAnalyzeEntity : pagedList) {
				QueryBuilder builderMd5 = new QueryBuilder();
				builderMd5.filterField(FtsFieldConst.FIELD_MD5TAG, mBlogAnalyzeEntity.getMd5Tag(), Operator.Equal);
				long ftsCount = hybase8SearchService.ftsCount(builder, MBlogAnalyzeEntity.class, sim, irSimflag,irSimflagAll,"special");
				mBlogAnalyzeEntity.setCount(ftsCount);

				String content = mBlogAnalyzeEntity.getContent() != null
						? mBlogAnalyzeEntity.getContent().replaceFirst("　　", "").replaceAll("\\?{4,25}", "") : null;
				mBlogAnalyzeEntity.setContent(content);
			}

			return pagedList;
		} catch (Exception e) {
			throw new OperationException("微博top5检索失败:" + e);
		}
	}

	@Override
	public Map<String, Object> getWebCountNew(String days, SpecialProject specialProject, String area, String industry)
			throws TRSException {
		String[] timeRange = DateUtil.formatTimeRange(days);
		String beginDate = timeRange[0];
		String endDate = timeRange[1];
		try {
			specialProject.setStart(beginDate);
			specialProject.setEnd(endDate);
		} catch (ParseException e) {
			throw new TRSException(e);
		}
		boolean flag = true;
		if (!"0d".equals(days)) {
			flag = false;
		}
		List<String> dateList = DateUtil.getBetweenDateString(beginDate, endDate, "yyyyMMddHHmmss");
		List<String> date = new ArrayList<>();

		Iterator<String> i = dateList.iterator();
		String s = "";
		while (i.hasNext()) {
			s = String.valueOf(i.next());
			s = s.replaceAll("[-/.: ]", "").trim();
			s = s.substring(0, 8);
			Date stringToDate = DateUtil.stringToDate(s, "yyyyMMdd");
			s = DateUtil.date2String(stringToDate, "yyyy-MM-dd");
			date.add(s);
		}

		List<ClassInfo> classInfoNews = ClassInfoUtil.timeChange(getWebCountByGroupName("国内新闻", specialProject, flag),
				date, flag);

		List<ClassInfo> classInfoWeiBo = ClassInfoUtil.timeChange(getWebCountByGroupName("微博", specialProject, flag),
				date, flag);

		List<ClassInfo> classInfoWeiXin = ClassInfoUtil.timeChange(getWebCountByGroupName("微信", specialProject, flag),
				date, flag);

		List<ClassInfo> classInfoLuntan = ClassInfoUtil.timeChange(getWebCountByGroupName("国内论坛", specialProject, flag),
				date, flag);

		List<ClassInfo> classInfoBlog = ClassInfoUtil.timeChange(getWebCountByGroupName("国内博客", specialProject, flag),
				date, flag);

		List<ClassInfo> classInfoApps = ClassInfoUtil
				.timeChange(getWebCountByGroupName("国内新闻_电子报", specialProject, flag), date, flag);

		List<ClassInfo> classInfoKeHuDuan = ClassInfoUtil
				.timeChange(getWebCountByGroupName("国内新闻_客户端", specialProject, flag), date, flag);

		return MapUtil.putValue(new String[] { "news", "status", "wechat", "luntan", "blog", "read", "app" },
				classInfoNews, classInfoWeiBo, classInfoWeiXin, classInfoLuntan, classInfoBlog, classInfoApps,
				classInfoKeHuDuan);
	}

	/**
	 * 数据源/日期统计
	 *
	 * @param groupName
	 *            数据源
	 * @param specialProject
	 *            专项pojo类
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<ClassInfo> getWebCountByGroupName(String groupName, SpecialProject specialProject, boolean flag) {
		List<ClassInfo> classInfo = new ArrayList<>();
		QueryBuilder builder = null;
		// 排重
		boolean sim = specialProject.isSimilar();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		// 数据源判断
		if ("微信".equals(groupName)) {
			builder = specialProject.toNoPagedBuilderWeiXin();
			builder.filterField(FtsFieldConst.FIELD_URLTIME,
					new String[] { specialProject.getStart(), specialProject.getEnd() }, Operator.Between);
			builder.setDatabase(Const.WECHAT);
		} else if ("微博".equals(groupName)) {
			builder = specialProject.toNoPagedAndTimeBuilderWeiBo();
			builder.filterField(FtsFieldConst.FIELD_CREATED_AT,
					new String[] { specialProject.getStart(), specialProject.getEnd() }, Operator.Between);
			builder.setDatabase(Const.WEIBO);
		} else {
			builder = specialProject.toNoPagedBuilder();
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupName, Operator.Equal);
			builder.setDatabase(Const.HYBASE_NI_INDEX);
		}
		//跨数据源排重
		if (irSimflagAll){
			builder.filterField(FtsFieldConst.FIELD_IR_SIMFLAGALL,"0 OR \"\"",Operator.Equal);
		}
		Iterator<Map.Entry<String, Long>> iter = null;
		// 按小时统计或者按天统计
		try {
			log.error("groupName:" + groupName);
			log.error(builder.asTRSL());
			if (flag) {
				if ("微博".equals(groupName) || "微信".equals(groupName)) {
					iter = hybase8SearchService.groupCount(builder, FtsFieldConst.FIELD_CREATED_HOUR, sim, irSimflag,irSimflagAll,"special");
				} else {
					iter = hybase8SearchService.groupCount(builder, FtsFieldConst.FIELD_URLTIME_HOUR, sim, irSimflag,irSimflagAll,"special");
				}
			} else {
				if ("微博".equals(groupName)) {
					iter = hybase8SearchService.groupCount(builder, FtsFieldConst.FIELD_CREATED_AT, sim, irSimflag,irSimflagAll,"special");
				} else {
					iter = hybase8SearchService.groupCount(builder, FtsFieldConst.FIELD_URLTIME, sim, irSimflag,irSimflagAll,"special");
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		if (null != iter) {
			while (iter.hasNext()) {
				Map.Entry<String, Long> entry = iter.next();
				if (flag) {
					classInfo.add(new ClassInfo(entry.getKey() + ":00", entry.getValue().intValue()));
				} else {
					classInfo.add(new ClassInfo(entry.getKey(), entry.getValue().intValue()));
				}

			}
		}
		return classInfo;
	}

	@Override
	public List<Map<String, Object>> getAreaCount(QueryBuilder searchBuilder, String[] timeArray,boolean isSimilar, boolean irSimflag,boolean irSimflagAll)
			throws TRSException {
		ObjectUtil.assertNull(searchBuilder.asTRSL(), "地域分布检索表达式");
		List<Map<String, Object>> resultMap = new ArrayList<>();
		if (timeArray != null) {
			searchBuilder.filterField("IR_URLTIME", timeArray, Operator.Between);
		}
		//跨数据源排重
		if (irSimflagAll){
			searchBuilder.filterField(FtsFieldConst.FIELD_IR_SIMFLAGALL,"0 OR \"\"",Operator.Equal);
		}
		try {
			Map<String, List<String>> areaMap = districtInfoService.allAreas();
			GroupResult categoryInfos = hybase8SearchService.categoryQuery(searchBuilder.isServer(),
					searchBuilder.asTRSL(), isSimilar, irSimflag,irSimflagAll, ESFieldConst.CATALOG_AREA, 1000, Const.HYBASE_NI_INDEX);
			for (Map.Entry<String, List<String>> entry : areaMap.entrySet()) {
				Map<String, Object> reMap = new HashMap<String, Object>();
				int num = 0;
				// 查询结果之间相互对比 所以把城市放开也不耽误查询速度
				for (GroupInfo classEntry : categoryInfos) {
					if (classEntry.getFieldValue().contains(entry.getKey())) {
						num += classEntry.getCount();
					}
				}
				List<Map<String, String>> citys = new ArrayList<Map<String, String>>();
				for (String city : entry.getValue()) {
					Map<String, String> cityMap = new HashMap<String, String>();
					int num2 = 0;
					int numJiLin = 0;
					for (GroupInfo classEntry : categoryInfos) {
						// 因为吉林省市同名,单独拿出,防止按区域名称分类统计错误
						if (classEntry.getFieldValue().contains(city)
								&& !classEntry.getFieldValue().contains("吉林省\\吉林市")) {
							num2 += classEntry.getCount();
						} else if (classEntry.getFieldValue().contains("吉林省\\吉林市")) {
							numJiLin += classEntry.getCount();
						}
					}
					// 把.之前的去掉
					String[] citySplit = city.split(".");
					if (citySplit.length > 1) {
						city = citySplit[citySplit.length - 1];
					}
					cityMap.put("area_name", city);
					cityMap.put("area_count", String.valueOf(num2));
					if ("吉林".equals(city)) {
						cityMap.put("area_count", String.valueOf(numJiLin));
					}
					citys.add(cityMap);
				}
				reMap.put("area_name", entry.getKey());
				reMap.put("area_count", num);
				reMap.put("citys", citys);
				resultMap.add(reMap);
			}

		} catch (Exception e) {
			throw new OperationException("地域分布查询失败" + e);
		}
		return resultMap;
	}

	@Override
	public List<Map<String, Object>> getAreaCount(QueryBuilder searchBuilder, String[] timeArray, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String areaType) throws TRSException {
		return null;
	}

	@Override
	public List<Map<String, Object>> getAreaCountForHome(QueryBuilder searchBuilder, String[] timeArray,
														 String groupName) throws TRSException {
		List<Map<String, Object>> resultMap = new ArrayList<>();
		try {
			Map<String, List<String>> areaMap = districtInfoService.allAreas();
			// GroupResult all = new GroupResult();
			GroupResult categoryInfos = new GroupResult();
			if ("ALL".equals(groupName) || StringUtil.isEmpty(groupName) || Const.MEDIA_TYPE_NEWS.contains(groupName)) {
				searchBuilder.filterField("IR_URLTIME", timeArray, Operator.Between);
				categoryInfos = hybase8SearchService.categoryQuery(searchBuilder.isServer(), searchBuilder.asTRSL(),
						true, true, true,FtsFieldConst.FIELD_CATALOG_AREA, 1000, Const.HYBASE_NI_INDEX);
				log.info(searchBuilder.asTRSL());
				// all.addAll(categoryInfos);
			}
			if (Const.MEDIA_TYPE_WEIXIN.contains(groupName) || "ALL".equals(groupName)
					|| StringUtil.isEmpty(groupName)) {
				QueryBuilder weiboBuilder = new QueryBuilder();
				weiboBuilder.filterField("IR_URLTIME", timeArray, Operator.Between);
				log.info(weiboBuilder.asTRSL());
				categoryInfos = hybase8SearchService.categoryQuery(weiboBuilder.isServer(), weiboBuilder.asTRSL(),
						false, false,false,FtsFieldConst.FIELD_CATALOG_AREA, 1000, Const.WECHAT);
				// all.addAll(categoryInfosWeiBo);
			}
			if (Const.MEDIA_TYPE_WEIBO.contains(groupName) || "ALL".equals(groupName)
					|| StringUtil.isEmpty(groupName)) {
				QueryBuilder weiboBuilder = new QueryBuilder();
				weiboBuilder.filterField("IR_URLTIME", timeArray, Operator.Between);
				weiboBuilder.setDatabase(Const.WEIBO);
				log.info(weiboBuilder.asTRSL());
				categoryInfos = hybase8SearchService.categoryQuery(weiboBuilder.isServer(), weiboBuilder.asTRSL(),
						false, false,false,FtsFieldConst.FIELD_CATALOG_AREA, 1000, Const.WEIBO);
				// all.addAll(categoryInfosWeiXin);
			}
			for (Map.Entry<String, List<String>> entry : areaMap.entrySet()) {
				Map<String, Object> reMap = new HashMap<String, Object>();
				int num = 0;
				// 查询结果之间相互对比 所以把城市放开也不耽误查询速度
				for (GroupInfo classEntry : categoryInfos) {
					if (classEntry.getFieldValue().contains(entry.getKey())) {
						num += classEntry.getCount();
					}
				}
				List<Map<String, String>> citys = new ArrayList<Map<String, String>>();
				for (String city : entry.getValue()) {
					Map<String, String> cityMap = new HashMap<String, String>();
					int num2 = 0;
					int numJiLin = 0;
					for (GroupInfo classEntry : categoryInfos) {
						// 因为吉林省市同名,单独拿出,防止按区域名称分类统计错误
						if (classEntry.getFieldValue().contains(city)
								&& !classEntry.getFieldValue().contains("吉林省\\吉林市")) {
							num2 += classEntry.getCount();
						} else if (classEntry.getFieldValue().contains("吉林省\\吉林市")) {
							numJiLin += classEntry.getCount();
						}
					}
					cityMap.put("area_name", city);
					cityMap.put("area_count", String.valueOf(num2));
					if ("吉林".equals(city)) {
						cityMap.put("area_count", String.valueOf(numJiLin));
					}
					citys.add(cityMap);
				}
				reMap.put("area_name", entry.getKey());
				reMap.put("area_count", num);
				reMap.put("citys", citys);
				resultMap.add(reMap);
			}

		} catch (Exception e) {
			throw new OperationException("地域分布查询失败" + e);
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> getTendencyNew2(String esSql, SpecialProject specialProject, String type,
											   String timerange, String showType) throws TRSException {
		boolean sim = specialProject.isSimilar();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		String dateType = "";
		String days = "";
		String startTime = "";
		String endTime = "";
		boolean flag = true;
		if (timerange.contains("d")) {
			dateType = "DAY";
			// days=timerange.substring(0, 1);
			days = timerange.substring(0, timerange.length() - 1);
			if ("0".equals(days)) {
				dateType = "TODAY";
				flag = false;
			}
		} else if (timerange.contains("h")) {
			dateType = "HOUR";
			String[] timeArray = DateUtil.formatRange(timerange);
			startTime = timeArray[0];
			endTime = timeArray[1];
		} else {
			dateType = "DIY";
			String[] timeArray = DateUtil.formatRange(timerange);
			startTime = timeArray[0];
			endTime = timeArray[1];
		}

		Map<String, Object> result = new HashMap<>();
		if (StringUtil.isEmpty(dateType)) {
			dateType = "DIY";
		}
		List<String> dateList = new ArrayList<String>();
		List<String> date = new ArrayList<String>();
		List<String> dateResult = new ArrayList<String>();

		if ("DAY".equals(String.valueOf(dateType))) {
			if (StringUtil.isEmpty(days)) {
				throw new OperationException("按天查询天数不能为空!");
			}
			dateList = DateUtil.getDataStinglist2(Integer.parseInt(days) - 1);
		}
		if ("DIY".equals(String.valueOf(dateType))) {
			if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
				throw new OperationException("自定义查询起止时间不能为空!");
			}
			dateList = DateUtil.getBetweenDateString(startTime, endTime, "yyyy-MM-dd HH:mm:ss");
		}
		if ("TODAY".equals(String.valueOf(dateType))) {
			dateList = DateUtil.getCurrentDateHours();
			dateList.add(DateUtil.formatCurrentTime("yyyyMMddHHmmss"));
		}
		if ("HOUR".equals(String.valueOf(dateType))) {
			dateList = DateUtil.get24Hours();
		}

		Iterator<String> i = dateList.iterator();
		String s = "";
		if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
			while (i.hasNext()) {
				s = String.valueOf(i.next());
				s = s.replaceAll("[-/.: ]", "").trim();
				s = s.substring(0, 8);
				date.add(s);
				Date stringToDate = DateUtil.stringToDate(s, "yyyyMMdd");
				s = DateUtil.date2String(stringToDate, "yyyy-MM-dd");
				dateResult.add(s);
			}
		}
		if ("TODAY".equals(String.valueOf(dateType)) || "HOUR".equals(String.valueOf(dateType))) {
			while (i.hasNext()) {
				s = String.valueOf(i.next());
				s = s.replaceAll("[-/.: ]", "").trim();
				s = s.substring(0, 10);
				date.add(s);

			}
			String formatCurrentTime = DateUtil.formatCurrentTime("HH");
			for (int j = 0; j <= Integer.valueOf(formatCurrentTime); j++) {
				if (j < 10) {
					dateResult.add("0" + j + "：00");
				} else {
					dateResult.add(j + "" + "：00");
				}
			}
		}
		try {
			int size = date.size();
			List<Map<String, Object>> list = new ArrayList<>();

			result.put("date", dateResult);
			String groupField = null;
			String groupFieldWeibo = null;
			String groupFieldWeixin = null;
			String start = null;
			String end = null;
			if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
				start = date.get(0) + DateUtil.DAY_START;
				end = date.get(date.size() - 1) + DateUtil.DAY_END;
				groupField = FtsFieldConst.FIELD_URLTIME;
				groupFieldWeixin = FtsFieldConst.FIELD_URLTIME;
				groupFieldWeibo = FtsFieldConst.FIELD_CREATED_AT;
			}
			if ("TODAY".equals(String.valueOf(dateType)) || "HOUR".equals(String.valueOf(dateType))) {
				start = date.get(0) + DateUtil.HOUR_START;
				end = date.get(date.size() - 1) + DateUtil.HOUR_END;
				groupField = FtsFieldConst.FIELD_URLTIME_HOUR;
				groupFieldWeixin = FtsFieldConst.FIELD_CREATED_HOUR;
				groupFieldWeibo = FtsFieldConst.FIELD_CREATED_HOUR;
			}

			QueryBuilder chuantong = specialProject.toNoPagedAndTimeBuilder();
			chuantong.filterByTRSL(esSql);
			String chuantongTrsl = chuantong.asTRSL();

			QueryBuilder weibo = specialProject.toNoPagedAndTimeBuilderWeiBo();
			weibo.filterByTRSL(esSql);
			String weiboTrsl = weibo.asTRSL();

			QueryBuilder weixin = specialProject.toNoPagedAndTimeBuilderWeiXin();
			weixin.filterByTRSL(esSql);
			String weixinTrsl = weixin.asTRSL();

			/*
			 * // 1. 媒体 String sqlNews = FtsFieldConst.FIELD_URLTIME + ":[" +
			 * start + " TO " + end + "] AND " + FtsFieldConst.FIELD_GROUPNAME +
			 * ":((国内新闻) OR (国内新闻_手机客户端) OR (国内新闻_电子报) OR (国外新闻) OR (港澳台新闻)) NOT "
			 * + FtsFieldConst.FIELD_SITENAME + ":百度贴吧 AND " + chuantongTrsl;
			 * GroupResult categoryQueryNews =
			 * hybase8SearchService.categoryQuery(sqlNews, sim, groupField,
			 * size, Const.HYBASE_NI_INDEX); Map<String, Object> mapNews = new
			 * HashMap<>(); mapNews.put("groupName", "媒体"); mapNews.put("list",
			 * MapUtil.sortAndChangeList(categoryQueryNews, dateResult,
			 * "yyyy-MM-dd", flag)); list.add(mapNews);
			 */
			if ("user".equals(type)) {
				// 2.微博用户
				String sqlStatus = FtsFieldConst.FIELD_CREATED_AT + ":[" + start + " TO " + end + "]  AND " + weiboTrsl;
				log.error("微博：" + sqlStatus);
				GroupResult categoryQueryStatus = hybase8SearchService.categoryQuery(chuantong.isServer(), sqlStatus,
						sim, irSimflag,irSimflagAll, groupFieldWeibo, size, Const.WEIBO);
				Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("groupName", "微博用户");
				mapStatus.put("list", MapUtil.sortAndChangeList(categoryQueryStatus, dateResult, "yyyy-MM-dd", flag));
				list.add(mapStatus);

				// 3.博客用户
				String sqlBlog = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] " + "AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":" + ESGroupName.BLOG_IN.getName() + " AND " + chuantongTrsl;
				log.error("博客：" + sqlBlog);
				GroupResult categoryQueryBlog = hybase8SearchService.categoryQuery(chuantong.isServer(), sqlBlog, sim,
						irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
				Map<String, Object> mapBlog = new HashMap<>();
				mapBlog.put("groupName", "博客用户");
				mapBlog.put("list", MapUtil.sortAndChangeList(categoryQueryBlog, dateResult, "yyyy-MM-dd", flag));
				list.add(mapBlog);

				// 4.微信用户
				String sqlWeChat = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]  AND " + weixinTrsl;
				GroupResult categoryQueryWeChat = hybase8SearchService.categoryQuery(specialProject.isServer(),
						sqlWeChat, sim, irSimflag,irSimflagAll, groupFieldWeixin, size, Const.WECHAT);
				Map<String, Object> mapWeChat = new HashMap<>();
				mapWeChat.put("groupName", "微信用户");
				mapWeChat.put("list", MapUtil.sortAndChangeList(categoryQueryWeChat, dateResult, "yyyy-MM-dd", flag));
				list.add(mapWeChat);

				// 5.论坛用户
				String sqlLunTan = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] " + "AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":" + ESGroupName.FORUM_IN.getName() + " OR "
						+ FtsFieldConst.FIELD_SITENAME + ":百度贴吧  AND " + chuantongTrsl;
				GroupResult categoryQueryLunTan = hybase8SearchService.categoryQuery(specialProject.isServer(),
						sqlLunTan, sim, irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
				Map<String, Object> mapLunTan = new HashMap<>();
				mapLunTan.put("groupName", "论坛用户");
				mapLunTan.put("list", MapUtil.sortAndChangeList(categoryQueryLunTan, dateResult, "yyyy-MM-dd", flag));
				list.add(mapLunTan);

				// 6.TWitter
				/*
				 * String sqlTWitter = FtsFieldConst.FIELD_URLTIME + ":[" +
				 * start + " TO " + end + "] AND " +
				 * FtsFieldConst.FIELD_GROUPNAME + ":(TWitter) " +
				 * chuantongTrsl; log.info("TWitteryangyanyan"+sqlTWitter);
				 * GroupResult categoryQueryTWitter =
				 * hybase8SearchService.categoryQuery(sqlTWitter, sim,
				 * groupField, size, Const.HYBASE_OVERSEAS); Map<String, Object>
				 * mapTWitter = new HashMap<>(); mapTWitter.put("groupName",
				 * "TWitter"); mapTWitter.put("list",
				 * MapUtil.sortAndChangeList(categoryQueryTWitter, dateResult,
				 * "yyyy-MM-dd", flag)); list.add(mapTWitter);
				 *
				 * // 7.FaceBook String sqlFaceBook =
				 * FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end +
				 * "] AND " + FtsFieldConst.FIELD_GROUPNAME + ":(FaceBook) " +
				 * chuantongTrsl; log.info("FaceBookyangyanyan"+sqlTWitter);
				 * GroupResult categoryQueryFaceBook =
				 * hybase8SearchService.categoryQuery(sqlFaceBook, sim,
				 * groupField, size, Const.HYBASE_OVERSEAS); Map<String, Object>
				 * mapFaceBook = new HashMap<>(); mapFaceBook.put("groupName",
				 * "FaceBook"); mapFaceBook.put("list",
				 * MapUtil.sortAndChangeList(categoryQueryFaceBook, dateResult,
				 * "yyyy-MM-dd", flag)); list.add(mapFaceBook);
				 */
			} else if ("media".equals(type)) {
				// 1.新闻
				String sqlNews = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻) NOT " + FtsFieldConst.FIELD_SITENAME + ":百度贴吧 AND "
						+ chuantongTrsl;
				GroupResult categoryQueryNews = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlNews,
						sim, irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
				Map<String, Object> mapNews = new HashMap<>();
				mapNews.put("groupName", "新闻");
				mapNews.put("list", MapUtil.sortAndChangeList(categoryQueryNews, dateResult, "yyyy-MM-dd", flag));
				list.add(mapNews);

				// 2.客户端
				String sqlApp = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻_手机客户端) NOT " + FtsFieldConst.FIELD_SITENAME
						+ ":百度贴吧 AND " + chuantongTrsl;
				GroupResult categoryQueryApps = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlApp,
						sim, irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
				Map<String, Object> mapApps = new HashMap<>();
				mapApps.put("groupName", "客户端");
				mapApps.put("list", MapUtil.sortAndChangeList(categoryQueryApps, dateResult, "yyyy-MM-dd", flag));
				list.add(mapApps);

				// 3.电子报
				String sqlEpaper = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻_电子报) NOT " + FtsFieldConst.FIELD_SITENAME
						+ ":百度贴吧 AND " + chuantongTrsl;
				GroupResult categoryQueryEpapers = hybase8SearchService.categoryQuery(specialProject.isServer(),
						sqlEpaper, sim, irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
				Map<String, Object> mapEpapers = new HashMap<>();
				mapEpapers.put("groupName", "电子报");
				mapEpapers.put("list", MapUtil.sortAndChangeList(categoryQueryEpapers, dateResult, "yyyy-MM-dd", flag));
				list.add(mapEpapers);

				// 4.境外媒体
				String sqlForeign = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":((国外新闻) OR (港澳台新闻)) NOT " + FtsFieldConst.FIELD_SITENAME
						+ ":百度贴吧 AND " + chuantongTrsl;
				GroupResult categoryQueryForeigns = hybase8SearchService.categoryQuery(specialProject.isServer(),
						sqlForeign, sim, irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
				Map<String, Object> mapForeigns = new HashMap<>();
				mapForeigns.put("groupName", "境外媒体");
				mapForeigns.put("list",
						MapUtil.sortAndChangeList(categoryQueryForeigns, dateResult, "yyyy-MM-dd", flag));
				list.add(mapForeigns);
			}
			result.put("arrayList", list);

		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException("网站统计失败" + e);
		}
		return result;
	}

	@Override
	public Map<String, Object> getTendencyMessage(String esSql, SpecialProject specialProject, String timerange, String showType)
			throws TRSException {
		boolean sim = specialProject.isSimilar();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		String dateType = "";
		String days = "";
		String startTime = "";
		String endTime = "";
		boolean flag = true;
		if (timerange.contains("d")) {
			dateType = "DAY";
			// days=timerange.substring(0, 1);
			days = timerange.substring(0, timerange.length() - 1);
			if ("0".equals(days)) {
				dateType = "TODAY";
				flag = false;
			}
		} else if (timerange.contains("h")) {
			dateType = "HOUR";
			String[] timeArray = DateUtil.formatRange(timerange);
			startTime = timeArray[0];
			endTime = timeArray[1];
		} else {
			dateType = "DIY";
			String[] timeArray = DateUtil.formatRange(timerange);
			startTime = timeArray[0];
			endTime = timeArray[1];
		}

		Map<String, Object> result = new HashMap<>();
		if (StringUtil.isEmpty(dateType)) {
			dateType = "DIY";
		}
		List<String> dateList = new ArrayList<String>();
		List<String> date = new ArrayList<String>();
		List<String> dateResult = new ArrayList<String>();

		if ("DAY".equals(String.valueOf(dateType))) {
			if (StringUtil.isEmpty(days)) {
				throw new OperationException("按天查询天数不能为空!");
			}
			dateList = DateUtil.getDataStinglist2(Integer.parseInt(days) - 1);
		}
		if ("DIY".equals(String.valueOf(dateType))) {
			if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
				throw new OperationException("自定义查询起止时间不能为空!");
			}
			dateList = DateUtil.getBetweenDateString(startTime, endTime, "yyyy-MM-dd HH:mm:ss");
		}
		if ("TODAY".equals(String.valueOf(dateType))) {
			dateList = DateUtil.getCurrentDateHours();
			dateList.add(DateUtil.formatCurrentTime("yyyyMMddHHmmss"));
		}
		if ("HOUR".equals(String.valueOf(dateType))) {
			dateList = DateUtil.get24Hours();
		}

		Iterator<String> i = dateList.iterator();
		String s = "";
		if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
			while (i.hasNext()) {
				s = String.valueOf(i.next());
				s = s.replaceAll("[-/.: ]", "").trim();
				s = s.substring(0, 8);
				date.add(s);
				Date stringToDate = DateUtil.stringToDate(s, "yyyyMMdd");
				s = DateUtil.date2String(stringToDate, "yyyy-MM-dd");
				dateResult.add(s);
			}
		}
		if ("TODAY".equals(String.valueOf(dateType)) || "HOUR".equals(String.valueOf(dateType))) {
			while (i.hasNext()) {
				s = String.valueOf(i.next());
				s = s.replaceAll("[-/.: ]", "").trim();
				s = s.substring(0, 10);
				date.add(s);

			}
			String formatCurrentTime = DateUtil.formatCurrentTime("HH");
			for (int j = 0; j <= Integer.valueOf(formatCurrentTime); j++) {
				if (j < 10) {
					dateResult.add("0" + j);
				} else {
					dateResult.add(j + "");
				}
			}
		}
		try {
			int size = date.size();
			List<Map<String, Object>> list = new ArrayList<>();

			result.put("date", dateResult);
			String groupField = null;
			String groupFieldWeibo = null;
			String groupFieldWeixin = null;
			String start = null;
			String end = null;
			if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
				start = date.get(0) + DateUtil.DAY_START;
				end = date.get(date.size() - 1) + DateUtil.DAY_END;
				groupField = FtsFieldConst.FIELD_URLTIME;
				groupFieldWeixin = FtsFieldConst.FIELD_URLTIME;
				groupFieldWeibo = FtsFieldConst.FIELD_CREATED_AT;
			}
			if ("TODAY".equals(String.valueOf(dateType)) || "HOUR".equals(String.valueOf(dateType))) {
				start = date.get(0) + DateUtil.HOUR_START;
				end = date.get(date.size() - 1) + DateUtil.HOUR_END;
				groupField = FtsFieldConst.FIELD_URLTIME_HOUR;
				groupFieldWeixin = FtsFieldConst.FIELD_CREATED_HOUR;
				groupFieldWeibo = FtsFieldConst.FIELD_CREATED_HOUR;
			}

			QueryBuilder chuantong = specialProject.toNoPagedAndTimeBuilder();
			chuantong.filterByTRSL(esSql);
			String chuantongTrsl = chuantong.asTRSL();

			QueryBuilder weibo = specialProject.toNoPagedAndTimeBuilderWeiBo();
			weibo.filterByTRSL(esSql);
			String weiboTrsl = weibo.asTRSL();

			QueryBuilder weixin = specialProject.toNoPagedAndTimeBuilderWeiXin();
			weixin.filterByTRSL(esSql);
			String weixinTrsl = weixin.asTRSL();

			// 1.新闻
			String sqlNews = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
					+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻) NOT " + FtsFieldConst.FIELD_SITENAME + ":百度贴吧 AND "
					+ chuantongTrsl;
			GroupResult categoryQueryNews = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlNews, sim,
					irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
			Map<String, Object> mapNews = new HashMap<>();
			mapNews.put("groupName", "新闻");
			mapNews.put("list", MapUtil.sortAndChangeList(categoryQueryNews, dateResult, "yyyy-MM-dd", flag));
			list.add(mapNews);

			// 2.微博
			String sqlStatus = FtsFieldConst.FIELD_CREATED_AT + ":[" + start + " TO " + end + "]  AND " + weiboTrsl;
			log.error("微博：" + sqlStatus);
			GroupResult categoryQueryStatus = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlStatus,
					sim, irSimflag,irSimflagAll, groupFieldWeibo, size, Const.WEIBO);
			Map<String, Object> mapStatus = new HashMap<>();
			mapStatus.put("groupName", "微博");
			mapStatus.put("list", MapUtil.sortAndChangeList(categoryQueryStatus, dateResult, "yyyy-MM-dd", flag));
			list.add(mapStatus);

			// 3.微信用户
			String sqlWeChat = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]  AND " + weixinTrsl;
			GroupResult categoryQueryWeChat = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlWeChat,
					sim, irSimflag,irSimflagAll, groupFieldWeixin, size, Const.WECHAT);
			Map<String, Object> mapWeChat = new HashMap<>();
			mapWeChat.put("groupName", "微信");
			mapWeChat.put("list", MapUtil.sortAndChangeList(categoryQueryWeChat, dateResult, "yyyy-MM-dd", flag));
			list.add(mapWeChat);

			// 4.论坛用户
			String sqlLunTan = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] " + "AND "
					+ FtsFieldConst.FIELD_GROUPNAME + ":" + ESGroupName.FORUM_IN.getName() + " OR "
					+ FtsFieldConst.FIELD_SITENAME + ":百度贴吧  AND " + chuantongTrsl;
			GroupResult categoryQueryLunTan = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlLunTan,
					sim, irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
			Map<String, Object> mapLunTan = new HashMap<>();
			mapLunTan.put("groupName", "论坛");
			mapLunTan.put("list", MapUtil.sortAndChangeList(categoryQueryLunTan, dateResult, "yyyy-MM-dd", flag));
			list.add(mapLunTan);

			// 5.博客用户
			String sqlBlog = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] " + "AND "
					+ FtsFieldConst.FIELD_GROUPNAME + ":" + ESGroupName.BLOG_IN.getName() + " AND " + chuantongTrsl;
			log.error("博客：" + sqlBlog);
			GroupResult categoryQueryBlog = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlBlog, sim,
					irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
			Map<String, Object> mapBlog = new HashMap<>();
			mapBlog.put("groupName", "博客");
			mapBlog.put("list", MapUtil.sortAndChangeList(categoryQueryBlog, dateResult, "yyyy-MM-dd", flag));
			list.add(mapBlog);

			// 6.客户端
			String sqlApp = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
					+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻_手机客户端) NOT " + FtsFieldConst.FIELD_SITENAME + ":百度贴吧 AND "
					+ chuantongTrsl;
			GroupResult categoryQueryApps = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlApp, sim,
					irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
			Map<String, Object> mapApps = new HashMap<>();
			mapApps.put("groupName", "客户端");
			mapApps.put("list", MapUtil.sortAndChangeList(categoryQueryApps, dateResult, "yyyy-MM-dd", flag));
			list.add(mapApps);

			// 7.电子报
			String sqlEpaper = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
					+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻_电子报) NOT " + FtsFieldConst.FIELD_SITENAME + ":百度贴吧 AND "
					+ chuantongTrsl;
			GroupResult categoryQueryEpapers = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlEpaper,
					sim, irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
			Map<String, Object> mapEpapers = new HashMap<>();
			mapEpapers.put("groupName", "电子报");
			mapEpapers.put("list", MapUtil.sortAndChangeList(categoryQueryEpapers, dateResult, "yyyy-MM-dd", flag));
			list.add(mapEpapers);

			// 8.境外媒体
			String sqlForeign = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
					+ FtsFieldConst.FIELD_GROUPNAME + ":((国外新闻) OR (港澳台新闻)) NOT " + FtsFieldConst.FIELD_SITENAME
					+ ":百度贴吧 AND " + chuantongTrsl;
			GroupResult categoryQueryForeigns = hybase8SearchService.categoryQuery(specialProject.isServer(),
					sqlForeign, sim, irSimflag,irSimflagAll, groupField, size, Const.HYBASE_NI_INDEX);
			Map<String, Object> mapForeigns = new HashMap<>();
			mapForeigns.put("groupName", "境外媒体");
			mapForeigns.put("list", MapUtil.sortAndChangeList(categoryQueryForeigns, dateResult, "yyyy-MM-dd", flag));
			list.add(mapForeigns);
			result.put("arrayList", list);

		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException("网站统计失败" + e);
		}
		return result;
	}

	/**
	 * @param specialProject
	 *            专题
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @param industryType
	 *            行业类型
	 * @param area
	 *            地域
	 * @return
	 * @throws TRSException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ClassInfo> statByClassification(SpecialProject specialProject, String start, String end,
												String industryType, String area) throws TRSException {
		List<ClassInfo> classInfo = new ArrayList<>();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		try {
			// 传统媒体分类统计结果
			Date startToDate = DateUtil.stringToDate(start, "yyyyMMddHHmmss");
			Date endToDate = DateUtil.stringToDate(end, "yyyyMMddHHmmss");
			specialProject.setStartTime(startToDate);
			specialProject.setEndTime(endToDate);
			QueryBuilder builder = specialProject.toNoPagedBuilder();
			builder.setDatabase(Const.HYBASE_NI_INDEX);
			log.warn(builder.asTRSL());
			Iterator<Map.Entry<String, Long>> iter = hybase8SearchService.groupCount(builder,
					FtsFieldConst.FIELD_GROUPNAME, true, irSimflag,irSimflagAll,"special");
			Map<String, Long> map = new LinkedHashMap<>();
			map.put("国内新闻", 0L);
			map.put("国内博客", 0L);
			map.put("国内论坛", 0L);
			map.put("微博", 0L);
			map.put("国内视频", 0L);
			map.put("微信", 0L);
			map.put("国内新闻_手机客户端", 0L);
			map.put("国内新闻_电子报", 0L);
			map.put("国外新闻", 0L);
			while (iter.hasNext()) {
				Map.Entry<String, Long> entry = iter.next();
				map.put(entry.getKey(), entry.getValue());
			}
			log.info(builder.asTRSL());
			// 微博总数统计结果
			QueryBuilder builderWeiBo = specialProject.toNoPagedBuilderWeiBo();// new
			// QueryBuilder();
			// log.error(builderWeiBo.asTRSL());
			builderWeiBo.setDatabase(Const.WEIBO);
			long weibo = hybase8SearchService.ftsCount(builderWeiBo, false, irSimflag,irSimflagAll,"special");
			map.put("微博", weibo);
			log.info(builderWeiBo.asTRSL());
			// 微信总数统计结果
			QueryBuilder builderWeChart = specialProject.toNoPagedBuilderWeiXin();// new
			builderWeChart.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内微信", Operator.Equal);
			builderWeChart.setDatabase(Const.WECHAT);
			log.info(builderWeChart.asTRSL());
			long wechart = hybase8SearchService.ftsCount(builderWeChart, false, irSimflag,irSimflagAll,"special");
			map.put("微信", wechart);
			Iterator<Map.Entry<String, Long>> iterMap = map.entrySet().iterator();
			while (iterMap.hasNext()) {
				Map.Entry<String, Long> entry = iterMap.next();
				classInfo.add(new ClassInfo(entry.getKey(), entry.getValue().intValue()));
			}
			log.info(builderWeChart.asTRSL());
		} catch (Exception e) {
			log.error("statByClassification error ", e);
		}
		return classInfo;
	}

	@Override
	public Object stattotal(SpecialProject specialProject, String start, String end, String industryType,
									 String area, String foreign) throws TRSException {

		try {
			// 传统媒体分类统计结果
			Date startToDate = DateUtil.stringToDate(start, "yyyyMMddHHmmss");
			Date endToDate = DateUtil.stringToDate(end, "yyyyMMddHHmmss");
			specialProject.setStartTime(startToDate);
			specialProject.setEndTime(endToDate);
			specialProject.setStart(new SimpleDateFormat("yyyyMMddHHmmss").format(startToDate));
			specialProject.setEnd(new SimpleDateFormat("yyyyMMddHHmmss").format(endToDate));
			QueryBuilder builder = specialProject.toNoPagedBuilder();
			//单一媒体排重
			boolean sim = specialProject.isSimilar();
			// url排重
			boolean irSimflag = specialProject.isIrSimflag();
			//全网排重
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			builder.page(0, 20);
			// 根据已选择的来源不同 查的group不同
			String groupName = specialProject.getSource();
			/*if ((null != foreign && !"".equals(foreign)) && (null == area || "".equals(area))) {
				groupName = "境外媒体";
			}*/
			if (null != foreign && !"".equals(foreign) && !"ALL".equals(foreign)) {
				infoListService.setForeignData(foreign, builder, null, null, null);
			}
			// 增加地域分析
			if (StringUtils.isNotBlank(area) && !area.equals("ALL")) {
				area = "中国\\\\" + area;
				area = area.replaceAll(";", "* OR 中国\\\\\\\\");
				if (area.endsWith("OR 中国\\\\")) {
					area = area.substring(0, area.lastIndexOf("OR"));
				}
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, area, Operator.Equal);
			}

			log.warn("stattal接口：" + builder.asTRSL());
			builder.setDatabase(Const.MIX_DATABASE);
			ChartResultField resultField = new ChartResultField("name", "value");
			List<Map<String, Object>> cateqoryQuery = (List<Map<String, Object>>) commonListService.queryListGroupNameStattotal(builder,sim,irSimflag,irSimflagAll,groupName,"special",resultField);
			Long count = 0L;
			for(Map<String, Object> map :cateqoryQuery){
				count += (Long)map.get(resultField.getCountField());
			}
			Map<String, Object> total = new HashMap<>();
			total.put(resultField.getContrastField(),"全部");
			total.put(resultField.getCountField(),count);
			cateqoryQuery.add(0,total);
			return cateqoryQuery;
		} catch (Exception e) {
			log.error("statByClassification error ", e);
			throw new OperationException("statByClassification error: " + e, e);
		}
	}

	@Override
	public int getEmotionalValue(SpecialProject specialProject, String groupName, String startTime, String endTime,
								 String industryType, String area) throws Exception {
		return 0;
	}

	@Override
	public List<TippingPoint> getTippingPoint(QueryBuilder queryBuilder, FtsDocumentCommonVO documentStatus, Date beginDate, boolean sim, boolean irSimflag, boolean irSimflagAll) throws TRSException, TRSSearchException {
		return null;
	}

/*
	@Override
	public List<TippingPoint> getTippingPoint(QueryBuilder queryBuilder, FtsDocumentStatus documentStatus,
			Date beginDate, boolean sim, boolean irSimflag,boolean irSimflagAll) throws TRSException, TRSSearchException {
		// String startTime = DateUtil.date2String(beginDate,
		// DateUtil.yyyyMMddHHmmss);
		// String endTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss);

		QueryBuilder builder = new QueryBuilder();
		String trsl = queryBuilder.asTRSL();
		builder.filterByTRSL(trsl);
		String baseUrl = documentStatus.beforeUrl();
		builder.filterField(ESFieldConst.IR_RETWEETED_URL, "\"" + baseUrl + "\"", Operator.Equal);
		builder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
		builder.setPageSize(20);
		List<TippingPoint> points = hybase8SearchService.ftsQuery(builder, TippingPoint.class, true, irSimflag,irSimflagAll,"special");
		List<String> scName = new ArrayList<>();
		List<TippingPoint> result = new ArrayList<>();
		for (TippingPoint point : points) {
			if (!scName.contains(point.getScreenName()) && result.size() < 10) {
				scName.add(point.getScreenName());
				result.add(point);
			}
		}

		if (result.size() < 10) {
			// 按MD5 查出前10条
			String md5Tag = documentStatus.getMd5Tag();
			String asTRSL = queryBuilder.asTRSL();
			QueryBuilder md5Builder = new QueryBuilder();
			// builder.filterField(ESFieldConst.IR_CREATED_AT, new
			// String[]{startTime, endTime}, Operator.Between);
			md5Builder.filterByTRSL(asTRSL);
			md5Builder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
			md5Builder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
			md5Builder.setPageSize(20);
			List<TippingPoint> md5Points = hybase8SearchService.ftsQuery(md5Builder, TippingPoint.class, true,
					irSimflag,irSimflagAll,"special");
			List<String> srcName = new ArrayList<>();
			List<TippingPoint> returnList = new ArrayList<>();
			for (TippingPoint point : md5Points) {
				if (point.getScreenName() != null) {
					if (!srcName.contains(point.getScreenName()) && returnList.size() < 10) {
						srcName.add(point.getScreenName());
						returnList.add(point);
					}
				}
			}
			return returnList;
		}
		return result;
	}
*/

	@Override
	public List<ReportTipping> getReportTippingPoint(String baseUrl, Date beginDate)
			throws TRSException, TRSSearchException {
		String startTime = DateUtil.date2String(beginDate, DateUtil.yyyyMMddHHmmss);
		String endTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss);

		QueryBuilder builder = new QueryBuilder();
		builder.filterField(ESFieldConst.IR_CREATED_AT, new String[] { startTime, endTime }, Operator.Between);
		builder.filterField(ESFieldConst.IR_RETWEETED_URL, "\"" + baseUrl + "\"", Operator.Equal);
		builder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
		builder.setPageSize(20);
		List<ReportTipping> points = esSearchService.ftsQuery(builder, ReportTipping.class, true, false,false,"special");
		List<String> scName = new ArrayList<>();
		List<ReportTipping> result = new ArrayList<>();
		for (ReportTipping point : points) {
			if (!scName.contains(point.getScreenName()) && result.size() < 10) {
				scName.add(point.getScreenName());
				result.add(point);
			}
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public SinaUser url(String trsl, SinaUser sinaUser, String[] timeArray, boolean sim, boolean irSimflag,boolean irSimflagAll)
			throws Exception {

		// 通过url获取原文
		SinaUser doc = getDocNew(sinaUser.baseUrl(), sim, irSimflag,irSimflagAll);
		if (doc == null) {
			doc = sinaUser;
		}
		getAllRTTUserNew(doc, timeArray, sim, irSimflag,irSimflagAll);
		String subContent = subContent(doc.getContent(), 25);
		doc.setContent(subContent);
		return doc;
	}

	private SinaUser getDocNew(String url, boolean sim, boolean irSimflag,boolean irSimflagAll) throws Exception {
		QueryBuilder builder = new QueryBuilder();
		String trsl = "IR_URLNAME:\"" + url + "\"";
		builder.filterByTRSL(trsl);

		builder.setDatabase(Const.WEIBO);
		List<SinaUser> documentList = hybase8SearchService.ftsQuery(builder, SinaUser.class, sim, irSimflag,irSimflagAll,"special");
		if (documentList == null || documentList.size() == 0) {
			return null;
		}
		SinaUser document = documentList.get(0);
		document.setRUrl(StringUtil.isEmpty(document.getRUrl()) ? document.getUrl() : document.getRUrl());
		document.setId(GUIDGenerator.generateName());

		return document;
	}

	private void getAllRTTUserNew(SinaUser document, String[] timeArray, boolean sim, boolean irSimflag,boolean irSimflagAll)
			throws Exception {
		QueryBuilder builder = new QueryBuilder();
		// builder.page(0, 5000);
		builder.page(0, 6);
		String trsl = "IR_RETWEETED_URL:\"" + document.getUrl() + "\"";
		builder.filterByTRSL(trsl);

		builder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
		builder.filterField(FtsFieldConst.FIELD_RETWEETED_SCREEN_NAME, document.getName(), Operator.Equal);
		builder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
		scrollForSpreadNew(document, builder, Const.WEIBO, timeArray, sim, irSimflag,irSimflagAll);
	}

	private QueryBuilder setReUrl(SinaUser document) throws Exception {
		QueryBuilder builder = new QueryBuilder();
		builder.page(0, 6);
		String trsl = "IR_RETWEETED_URL:\"" + document.getUrl() + "\"";
		builder.filterByTRSL(trsl);

		builder.filterField(FtsFieldConst.FIELD_RETWEETED_SCREEN_NAME, document.getName(), Operator.Equal);
		builder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
		builder.setDatabase(Const.WEIBO);
		return builder;
	}

	/**
	 * 检索获取传播用户 被转发的是同一人
	 *
	 * @param builder
	 * @param indices
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 */
	public void scrollForSpreadNew(SinaUser document, QueryBuilder builder, String indices, String[] timeArray,
								   boolean sim, boolean irSimflag,boolean irSimflagAll) throws Exception {
		builder.setDatabase(indices);
		List<SinaUser> ftsQuery = hybase8SearchService.ftsQuery(builder, SinaUser.class, sim, irSimflag,irSimflagAll,"special");
		log.info(builder.asTRSL());
		while (true) {
			if (ftsQuery != null) {
				List<SinaUser> children = new ArrayList<>();
				for (SinaUser sinaUser : ftsQuery) {
					if (sinaUser.getMid() != document.getMid()) {
						String subContent = subContent(sinaUser.getContent(), 25);
						sinaUser.setContent(subContent);
						children.add(sinaUser);
						QueryBuilder queryBuilder = setReUrl(sinaUser);
						queryBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
						scrollForSpreadNew(sinaUser, queryBuilder, indices, timeArray, sim, irSimflag,irSimflagAll);
					}
				}
			}
			break;
		}
		document.setChildren(ftsQuery);
	}

	/**
	 * 逐层取数据，直到 null
	 */
	private void generateMap(GraphMap graphMap, MultiKVMap<String, SinaUser> allUser, SinaUser fromUser, boolean first,
							 int level, int num) throws Exception {

		if (level == 0) {
			return;
		}
		log.info(fromUser.getName());
		// 获取转发 sinaUser的所有用户 用户就是那些点
		String fromUserName = fromUser.getName();
		List<SinaUser> userList = allUser.remove(fromUserName);// remove方法就是把remove掉的东西返回去
		// 把remove掉的东西存起来
		// 为空直接返回
		if (first) {
			if (userList == null) {
				userList = new ArrayList<>();
			}
			List<SinaUser> remove = allUser.remove(null);
			if (remove != null) {
				userList.addAll(remove);
			}
		}
		if (userList == null || userList.size() == 0) {
			return;
		}
		int n = 0;
		for (int i = 0; i < userList.size(); i++) {
			SinaUser user = userList.remove(i);

			graphMap.addGraph(user.getId(), fromUser.getId(), user.getName(), fromUser.getName());
			if (allUser.containsKey(user.getName())) {
				// 递归取下层数据
				generateMap(graphMap, allUser, user, false, level - 1, num);
			}
			if (++n == num) {
				break;
			}
		}

	}

	@Override
	public Map<String, Object> getLatestNews(String sql, String startTime, String endTime, Long pageNo, int pageSie)
			throws TRSSearchException, TRSException {
		Map<String, Object> resultMap = new HashMap<>();
		QueryBuilder searchBuilder = new QueryBuilder();
		searchBuilder.filterByTRSL(sql);
		searchBuilder.page(pageNo, pageSie);
		searchBuilder.orderBy("IR_URLTIME", true);
		searchBuilder.filterField("IR_URLTIME", new String[] { startTime, endTime }, Operator.Between);
		searchBuilder.filterField("IR_GROUPNAME", "国内新闻", Operator.Equal);
		log.info(searchBuilder.asTRSL());
		List<ChartAnalyzeEntity> newList = hybase8SearchService.ftsQuery(searchBuilder, ChartAnalyzeEntity.class, true,
				false,false,"special");
		ObjectUtil.assertNull(newList, "最新新闻列表数据");
		List<String> titleList = new ArrayList<String>();
		List<String> timeList = new ArrayList<String>();
		newList.forEach(item -> {
			titleList.add(item.getTitle());
			timeList.add(DateUtil.date2String(item.getUrltime(), DateUtil.yyyyMMdd));
		});
		resultMap.put("titleList", titleList);
		resultMap.put("timeList", timeList);
		return resultMap;
	}

	@Override
	public Map<String, Object> mediaAct(String sql, String[] time, String source) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		QueryBuilder searchBuilder = new QueryBuilder();
		searchBuilder.filterByTRSL(sql);
		searchBuilder.filterField("IR_URLTIME", time, Operator.Between);
		searchBuilder.filterField("IR_GROUPNAME", source, Operator.Equal);
		searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		long count = hybase8SearchService.ftsCount(searchBuilder, true, false,false,"special");
		GroupResult infoList = hybase8SearchService.categoryQuery(false, searchBuilder.asTRSL(), true, false,false,
				"IR_SITENAME", 9, Const.HYBASE_NI_INDEX);
		List<String> value = new ArrayList<>();
		List<String> ticks = new ArrayList<>();
		for (GroupInfo info : infoList) {
			ticks.add(info.getFieldValue());
			value.add(String.valueOf(info.getCount()));
		}
		ticks.add("总数");
		value.add(String.valueOf(count));
		resultMap.put("siteName", ticks);
		resultMap.put("rats", value);
		return resultMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	@Override
	public Map<String, Object> getVolumeNew(QueryBuilder searchBuilder, String[] timeArray, String timerange)
			throws Exception {
		String dateType = "";
		String days = "";
		String startTime = "";
		String endTime = "";
		// String[] timeArray = DateUtil.formatTimeRange(timerange);
		// 留着返回显示
		List<String> betweenDateString = new ArrayList();
		if (timerange.contains("d")) {
			dateType = "DAY";
			days = timerange.substring(0, timerange.length() - 1);
			if ("0".equals(days)) {
				dateType = "TODAY";

			}
		} else if (timerange.contains("h")) {
			dateType = "HOUR";
			startTime = timeArray[0];
			endTime = timeArray[1];
		} else {
			dateType = "DIY";
			startTime = timeArray[0];
			endTime = timeArray[1];
		}
		Map<String, Object> resultMap = new LinkedHashMap<>();
		try {
			// 如果为今天这new date() 到 今天凌晨所有的小时数量
			List<String> dateList = new ArrayList<>();
			List<String> list = new ArrayList<>();

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
				betweenDateString = DateUtil.getBetweenDateString(timerange.split(";")[0], timerange.split(";")[1],
						"yyyy-MM-dd");
			}
			String[] appraise = { "正面", "中性", "负面" };
			// 得到所有GROUPNAME
			for (String groupName : ESGroupName.MediaType.getAllMedias()) {
				List<Object> countList = new ArrayList<>();
				for (int i = 0; i < dateList.size() - 1; i++) {
					List<Map<String, String>> appraiseList = new ArrayList<>();
					for (int j = 0; j < appraise.length; j++) {
						// 为检索表达式正确又要改回中文
						if ("xinwen".equals(groupName)) {
							groupName = "国内新闻";

						}
						if ("luntan".equals(groupName)) {
							groupName = "国内论坛";
						}
						if ("kehuduan".equals(groupName)) {
							groupName = "国内新闻_手机客户端";
						}
						if ("dianzibao".equals(groupName)) {
							groupName = "国内新闻_电子报";
						}
						if ("boke".equals(groupName)) {
							groupName = "国内博客";
						}
						if ("weibo".equals(groupName)) {
							groupName = "微博";
						}
						if ("weixin".equals(groupName)) {
							groupName = "国内微信";
						}
						if ("all".equals(groupName)) {
							groupName = "((国内新闻) OR (国内论坛) OR (国内新闻_手机客户端) OR (国内新闻_电子报) OR (国内博客) OR (微博) OR (国内微信))";
						}
						if ("zhengmian".equals(appraise[j])) {
							appraise[j] = "正面";
						}
						if ("zhongxing".equals(appraise[j])) {
							appraise[j] = "中性";
						}
						if ("fumian".equals(appraise[j])) {
							appraise[j] = "负面";
						}
						Map<String, String> appraiseMap = new LinkedHashMap<>();
						QueryBuilder queryBuilder = new QueryBuilder();
						boolean isSimilar = false;
						if ("微博".equals(groupName)) {
							queryBuilder.setDatabase(Const.WEIBO);
							String esSql = FtsFieldConst.FIELD_CREATED_AT + ":[" + dateList.get(i) + " TO "
									+ dateList.get(i + 1) + "] AND (" + tieba(groupName) + ") AND IR_APPRAISE:"
									+ appraise[j] + " AND " + searchBuilder.asTRSL();
							queryBuilder.filterByTRSL(esSql);
						} else if ("微信".equals(groupName)) {
							queryBuilder.setDatabase(Const.WECHAT);
							String esSql = "IR_URLTIME:[" + dateList.get(i) + " TO " + dateList.get(i + 1) + "] AND ("
									+ tieba(groupName) + ") AND IR_APPRAISE:" + appraise[j] + " AND "
									+ searchBuilder.asTRSL();
							queryBuilder.filterByTRSL(esSql);
						} else {
							isSimilar = true;
							String esSql = "IR_URLTIME:[" + dateList.get(i) + " TO " + dateList.get(i + 1) + "] AND ("
									+ tieba(groupName) + ") AND IR_APPRAISE:" + appraise[j] + " AND "
									+ searchBuilder.asTRSL();
							queryBuilder.filterByTRSL(esSql);
							queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
						}
						Long l = hybase8SearchService.ftsCount(queryBuilder, isSimilar, false,false,"special");
						String count = String.valueOf(l);
						// 为方便前端取值 改成英文
						if ("正面".equals(appraise[j])) {
							appraise[j] = "zhengmian";
						}
						if ("中性".equals(appraise[j])) {
							appraise[j] = "zhongxing";
						}
						if ("负面".equals(appraise[j])) {
							appraise[j] = "fumian";
						}
						appraiseMap.put(appraise[j], count);
						appraiseList.add(appraiseMap);
					}
					Map map = new HashMap();
					map.put("date", appraiseList);
					countList.add(map);
				}
				if ("国内新闻".equals(groupName)) {
					groupName = "xinwen";
				}
				if ("国内论坛".equals(groupName)) {
					groupName = "luntan";
				}
				if ("国内新闻_手机客户端".equals(groupName)) {
					groupName = "kehuduan";
				}
				if ("国内新闻_电子报".equals(groupName)) {
					groupName = "dianzibao";
				}
				if ("国内博客".equals(groupName)) {
					groupName = "boke";
				}
				if ("微博".equals(groupName)) {
					groupName = "weibo";
				}
				if ("国内微信".equals(groupName)) {
					groupName = "weixin";
				}
				if ("((国内新闻) OR (国内论坛) OR (国内新闻_手机客户端) OR (国内新闻_电子报) OR (国内博客) OR (微博) OR (国内微信))".equals(groupName)) {
					groupName = "all";
				}
				resultMap.put(groupName, countList);
			}
			// 为了展示
			if ("DAY".equals(dateType)) {
				if (StringUtil.isEmpty(days)) {
					throw new OperationException("按天查询天数不能为空!");
				}
				dateList = DateUtil.getDataStinglist2(Integer.parseInt(days));
				for (int d = 0; d < dateList.size(); d++) {
					dateList.set(d, dateList.get(d).length() > 10 ? dateList.get(d).substring(0, 10) : dateList.get(d));
				}
			} else if ("DIY".equals(dateType)) {
				dateList = betweenDateString;
			}
			dateList.remove(0);
			resultMap.put("Dates", dateList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException("声量趋势计算错误,message: " + e);
		}
		return resultMap;
	}

	/**
	 * 雷达图时间自适应
	 *
	 * @param beginTime,endTime
	 *            时间范围
	 * @return Date[]
	 */
	@SuppressWarnings("unused")
	private List<DateRange> timeSelfAdaption(String beginTime, String endTime)
			throws ParseException, OperationException {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Date begin = format.parse(beginTime);
		Date end = format.parse(endTime);

		if (begin.after(end)) {
			throw new OperationException("时间自适应失败，时间范围有误");
		}
		Date yesterday = DateUtil.getMorningBefore(1);
		// 全部是历史
		if (end.before(yesterday)) {
			return Collections.singletonList(new DateRange(begin, end, LEGEND_HISTORY));
		}

		// 今天凌晨
		Date today = DateUtil.getMorningBefore(0);

		// 最新数据在昨天
		if (end.before(today)) {
			// 没有历史
			if (begin.after(yesterday)) {
				return Collections.singletonList(new DateRange(begin, end, LEGEND_YESTERDAY));
			}
			return Arrays.asList(new DateRange(begin, yesterday, LEGEND_HISTORY),
					new DateRange(yesterday, end, LEGEND_YESTERDAY));
		}

		// 最新数据在今天
		// 只有今天
		if (begin.after(today)) {
			return Collections.singletonList(new DateRange(begin, end, LEGEND_TODAY));
		}

		// 有今天和昨天
		if (begin.after(yesterday)) {
			return Arrays.asList(new DateRange(begin, today, LEGEND_YESTERDAY),
					new DateRange(today, end, LEGEND_TODAY));
		}
		return Arrays.asList(new DateRange(begin, yesterday, LEGEND_BEGIN),
				new DateRange(yesterday, yesterday, LEGEND_BEGIN_TODAY),
				new DateRange(yesterday, DateUtil.getMorningBefore(-1), LEGEND_TODAY));
	}

	/**
	 * 时间范围
	 */
	@AllArgsConstructor
	private class DateRange {

		private Date begin;

		private Date end;

		@SuppressWarnings("unused")
		private String legend;

		@SuppressWarnings("unused")
		private String[] getTimeRange() {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			return new String[] { format.format(begin), format.format(end) };
		}
	}

	/**
	 * 国内新闻排除贴吧 贴吧放到论坛里
	 *
	 * @param groupname
	 * @return
	 */
	public String tieba(String groupname) {
		if ("国内新闻".equals(groupname)) {
			groupname = "IR_GROUPNAME:国内新闻 NOT IR_SITENAME:百度贴吧";
		} else if ("国内论坛".equals(groupname)) {
			groupname = "IR_GROUPNAME:国内论坛 OR IR_SITENAME:百度贴吧";
		} else {
			groupname = "IR_GROUPNAME:" + groupname;
		}
		return groupname;
	}

	@SuppressWarnings("unused")
	private QueryBuilder generateFilter(SpecialProject specialProject, String beginDate, String endDate,
										String industryType, String area) {
		QueryBuilder builder = specialProject.esNoPagedAndTimeBuilder();
		if (!"ALL".equals(industryType)) {
			builder.filterField("", industryType, Operator.Equal);
		}
		if (!"ALL".equals(area)) {
			builder.filterField("", area, Operator.Equal);
		}
		builder.filterField("IR_URLTIME", new String[] { beginDate, endDate }, Operator.Between);
		return builder;
	}

	/**
	 * 重载 为栏目模块的xy轴弄的
	 *
	 * @param trsl
	 * @return
	 */
	public List<CategoryBean> getMediaType(String trsl) {
		return getMediaType(trsl, true);
	}

	public List<CategoryBean> getMediaType(String trsl,Boolean changeKey) {
		List<CategoryBean> list = new ArrayList<>();
		String newXyTrsl = trsl.replaceAll("\n", "");
		String[] medias = newXyTrsl.split("[;|；]");
		if (medias.length > 0) {
			for (String str : medias) {
				if (StringUtils.isNotBlank(str)) {
					String key = str.substring(0, str.indexOf("="));
					if(changeKey){
						if ("手机客户端".equals(key)) {
							key = "国内新闻_手机客户端";
						}
					}
					String val = str.substring(str.indexOf("=") + 1, str.length());
					list.add(new CategoryBean(key, val));
				}
			}
		}
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public Map<String, Object> getWebCountNew2(String esSql, String timeRange, String special_id) throws TRSException {
		String dateType = "";
		String days = "";
		String startTime = "";
		String endTime = "";
		// 如果是取当天的小时时返回用
		List<String> dateHourString2 = new ArrayList<>();
		if (timeRange.contains("d")) {
			dateType = "DAY";
			days = timeRange.substring(0, timeRange.length() - 1);
			if (0 == Integer.valueOf(days)) {
				dateType = "TODAY";
			}
		} else {
			dateType = "DIY";
			String[] timeArray = timeRange.split(";");
			startTime = timeArray[0];
			endTime = timeArray[1];
		}
		if (StringUtil.isEmpty(esSql)) {
			throw new TRSException("检索表达式不能为空！");
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List arrayList = new ArrayList<Map>();
		if (StringUtil.isEmpty(dateType)) {
			dateType = "DIY";
		}
		List<String> dateList = new ArrayList<String>();
		List<String> date = new ArrayList<String>();

		if ("DAY".equals(dateType)) {
			if (StringUtil.isEmpty(days)) {
				throw new OperationException("按天查询天数不能为空!");
			}
			dateList = DateUtil.getDataStinglist2(Integer.parseInt(days) - 1);
			Iterator i = dateList.iterator();
			String s1 = "";
			String s2 = "";
			String s3 = "";
			String s4 = "";
			String s5 = "";
			String s6 = "";
			List<String> date2 = new ArrayList<String>();
			List<String> date3 = new ArrayList<String>();
			while (i.hasNext()) {
				s1 = String.valueOf(i.next());
				s6 = s1.substring(0, 10);
				date3.add(s6);
				s2 = s1.replaceAll("[/.: ]", "").trim();
				s5 = s2.substring(0, 10);
				date2.add(s5);
				s4 = s5.replaceAll("[-/.: ]", "").trim();
				s3 = s4.substring(0, 8);
				date.add(s3);
			}

			dateList = date3;

			for (String groupName : ESGroupName.MediaType.getAllMedias()) {
				List result = new ArrayList();
				for (int j = 0; j < date.size(); j++) {
					String start = null;
					String end = null;
					if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
						start = date.get(j) + "000000";
						end = date.get(j) + "235959";
					}
					String newSql = null;
					TRSStatisticParams params = new TRSStatisticParams(); // 统计参数类
					GroupResult recordSet = null;
					try {
						newSql = "IR_URLDATE:[" + start + " TO " + end + "]  AND " + esSql + "AND " + tieba(groupName);
						QueryBuilder query = new QueryBuilder();
						query.filterByTRSL(newSql);
						boolean isSimilar = false;
						if ("微博".equals(groupName)) {
							query.setDatabase(Const.WEIBO);
						} else {
							isSimilar = true;
							query.setDatabase(Const.HYBASE_NI_INDEX);
						}
						long list = hybase8SearchService.ftsCount(query, isSimilar, false,false,"special");
						result.add(list);
					} catch (Exception e) {
						throw new OperationException("网站统计失败" + e);
					}
				}
				Map map = MapUtil.putValue(new String[] { "groupname", "result" }, groupName, result);
				arrayList.add(map);
			}

		} else {
			if ("DIY".equals(dateType)) {
				if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
					throw new OperationException("自定义查询起止时间不能为空!");
				}
				dateList = DateUtil.getBetweenDateString(startTime, endTime, "yyyy-MM-dd");
				Iterator i = dateList.iterator();
				String s = "";
				List<String> date3 = new ArrayList<String>();
				while (i.hasNext()) {
					s = String.valueOf(i.next());
					s = s.replaceAll("[-/.: ]", "").trim();
					s = s.substring(0, 8);
					date.add(s);
				}
				for (String groupName : ESGroupName.MediaType.getAllMedias()) {
					List result = new ArrayList();
					for (int j = 0; j < date.size(); j++) {
						String start = null;
						String end = null;
						if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
							start = date.get(j) + "000000";
							end = date.get(j) + "235959";
						}
						String newSql = null;
						TRSStatisticParams params = new TRSStatisticParams(); // 统计参数类
						GroupResult recordSet = null;
						try {
							newSql = "IR_URLDATE:[" + start + " TO " + end + "] AND " + tieba(groupName) + " AND "
									+ esSql;
							QueryBuilder query = new QueryBuilder();
							query.filterByTRSL(newSql);
							boolean isSimilar = false;
							if ("微博".equals(groupName)) {
								query.setDatabase(Const.WEIBO);
							} else {
								isSimilar = true;
								query.setDatabase(Const.HYBASE_NI_INDEX);
							}
							long list = hybase8SearchService.ftsCount(query, isSimilar, false,false,"special");
							result.add(list);
							log.info("esSql---" + newSql);
						} catch (Exception e) {
							throw new OperationException("网站统计失败" + e);
						}
					}
					Map map = MapUtil.putValue(new String[] { "groupname", "result" }, groupName, result);
					arrayList.add(map);
				}
			} else {
				if ("TODAY".equals(dateType)) {
					dateList = DateUtil.getCurrentDateHours();
					dateHourString2 = DateUtil.getDateHourString2(dateList.get(dateList.size() - 1).substring(4, 8),
							Integer.parseInt(dateList.get(dateList.size() - 1).substring(8, 10)));
				}
				if ("HOUR".equals(dateType)) {
					dateList = DateUtil.get24Hours();
				}
				Iterator i = dateList.iterator();
				String s = "";
				while (i.hasNext()) {
					s = String.valueOf(i.next());
					s = s.replaceAll("[-/.: ]", "").trim();
					s = s.substring(0, 10);
					date.add(s);
				}
				for (String groupName : ESGroupName.MediaType.getAllMedias()) {
					List result = new ArrayList();
					for (int j = 0; j < date.size(); j++) {
						String start = null;
						String end = null;
						start = date.get(j) + "0000";
						end = date.get(j) + "5959";
						String newSql = null;
						long list;
						try {
							newSql = "IR_URLTIME:[" + start + " TO " + end + "] AND " + tieba(groupName) + " AND "
									+ esSql;
							QueryBuilder query = new QueryBuilder();
							query.filterByTRSL(newSql);
							boolean isSimilar = false;
							if ("微博".equals(groupName)) {
								query.setDatabase(Const.WEIBO);
							} else {
								isSimilar = true;
								query.setDatabase(Const.HYBASE_NI_INDEX);
							}
							list = hybase8SearchService.ftsCount(query, isSimilar, false,false,"special");
							result.add(list);
							log.info("esSql---" + newSql);
						} catch (Exception e) {
							throw new OperationException("网站统计失败" + e);
						}
					}
					Map map = MapUtil.putValue(new String[] { "groupname", "result" }, groupName, result);
					arrayList.add(map);
				}
			}
		}
		if ("TODAY".equals(dateType)) {
			dateList = dateHourString2;
		}
		resultMap = MapUtil.putValue(new String[] { "arrayList", "date" }, arrayList, dateList);
		return resultMap;
	}

	@Override
	public Object getDataByChart(SpecialProject specialProject, String industryType, String area, String chartType,
								 String dateTime, String xType, String source, String entityType, String sort, String emotion,
								 String fuzzyValue,String fuzzyValueScope,int pageNo, int pageSize, String forwarPrimary, String invitationCard, boolean isExport, String thirdWord)
			throws Exception {
		QueryBuilder builder = new QueryBuilder();
		QueryBuilder countBuilder = new QueryBuilder();
		User loginUser = UserUtils.getUser();
		boolean sim = specialProject.isSimilar();
		// 拼接检索条件
		for (ChartType type : ChartType.values()) {
			// 匹配数据源,并且拼接数据时间
			if (type.getType().equals(chartType)) {
				// 转换数据时间
				String dataEndTime = null;
				String dataStartTime = null;
				if (StringUtil.isNotEmpty(dateTime)) {
					if (type.getType().equals(ChartType.WEBCOUNT.getType())
							|| type.getType().equals(ChartType.NETTENDENCY.getType())
							|| type.getType().equals(ChartType.VOLUME.getType())
							|| type.getType().equals(ChartType.TRENDMESSAGE.getType())) {
						if(dateTime.length() == 16){
							dateTime = dateTime.replace("-", "").replace("/", "")
									.replace(":", "").replace(" ", "").trim();
							dateTime = dateTime+"00";
							dataEndTime = dateTime.substring(0,10)+"5959";
						}else {
							if (dateTime.length() <= 2) {
								if (dateTime.length() == 1) {
									dataStartTime = "0" + dateTime;
								} else {
									dataStartTime = dateTime;
								}
								dateTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2)) + "000000";
								dataEndTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2)) + dataStartTime
										+ "0000";
							} else {
								dateTime.replace("-", "");
								dataEndTime = DateUtil.formatDateAfter(dateTime, DateUtil.yyyyMMdd2, 1);// 间隔
								// 1
								// 天
								// 代表默认查
								// 1
								// 天
								dateTime = dateTime.replace("-", "").replace(":", "").replaceAll("\r|\n|\t", "").trim();
								dataEndTime = dateTime.replace("-", "").replace(":", "").replaceAll("\r|\n|\t", "").trim();
							}
						}
						if (type.getSource().equals("ALL")) { // 多数据来源,根据source匹配数据源
							if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
								builder = specialProject.toBuilderWeiXin(pageNo, pageSize, false);
								countBuilder = specialProject.toBuilderWeiXin(pageNo, pageSize, false);
								builder.setDatabase(Const.WECHAT);
								countBuilder.setDatabase(Const.WECHAT);
								builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { dateTime, dataEndTime },
										Operator.Between);
								countBuilder.filterField(FtsFieldConst.FIELD_URLTIME,
										new String[] { dateTime, dataEndTime }, Operator.Between);
							} else if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
								builder = specialProject.toBuilderWeiBo(pageNo, pageSize, false);
								countBuilder = specialProject.toBuilderWeiBo(pageNo, pageSize, false);
								builder.setDatabase(Const.WEIBO);
								countBuilder.setDatabase(Const.WEIBO);
								builder.filterField(FtsFieldConst.FIELD_CREATED_AT,
										new String[] { dateTime, dataEndTime }, Operator.Between);
								countBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT,
										new String[] { dateTime, dataEndTime }, Operator.Between);
							} else {
								builder = specialProject.toBuilder(pageNo, pageSize, false);
								countBuilder = specialProject.toBuilderWeiXin(pageNo, pageSize, false);
								builder.setDatabase(Const.HYBASE_NI_INDEX);
								countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
								builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { dateTime, dataEndTime },
										Operator.Between);
								countBuilder.filterField(FtsFieldConst.FIELD_URLTIME,
										new String[] { dateTime, dataEndTime }, Operator.Between);
							}
						} else { // 指定图表数据,使用指定数据源
							if (Const.WEIBO.equals(type.getSource())) {
								builder = specialProject.toBuilderWeiBo(pageNo, pageSize, false);
								countBuilder = specialProject.toBuilderWeiBo(pageNo, pageSize, false);
								builder.setDatabase(Const.WEIBO);
								countBuilder.setDatabase(Const.WEIBO);
								builder.filterField(FtsFieldConst.FIELD_CREATED_AT,
										new String[] { dateTime, dataEndTime }, Operator.Between);
								countBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT,
										new String[] { dateTime, dataEndTime }, Operator.Between);
							} else if (Const.WECHAT.equals(type.getSource())) {
								builder = specialProject.toBuilderWeiXin(pageNo, pageSize, false);
								countBuilder = specialProject.toBuilderWeiXin(pageNo, pageSize, false);
								builder.setDatabase(Const.WECHAT);
								countBuilder.setDatabase(Const.WECHAT);
								builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { dateTime, dataEndTime },
										Operator.Between);
								countBuilder.filterField(FtsFieldConst.FIELD_URLTIME,
										new String[] { dateTime, dataEndTime }, Operator.Between);
							} else {
								builder = specialProject.toBuilder(pageNo, pageSize, false);
								countBuilder = specialProject.toBuilderWeiXin(pageNo, pageSize, false);
								builder.setDatabase(Const.HYBASE_NI_INDEX);
								countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
								builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { dateTime, dataEndTime },
										Operator.Between);
								countBuilder.filterField(FtsFieldConst.FIELD_URLTIME,
										new String[] { dateTime, dataEndTime }, Operator.Between);
							}
						}
					}

				} else {
					if (type.getSource().equals("ALL")) { // 多数据来源,根据source匹配数据源
						if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
							builder = specialProject.toBuilderWeiXin(pageNo, pageSize);
							countBuilder = specialProject.toBuilderWeiXin(pageNo, pageSize);
							builder.setDatabase(Const.WECHAT);
							countBuilder.setDatabase(Const.WECHAT);
						} else if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
							builder = specialProject.toBuilderWeiBo(pageNo, pageSize);
							countBuilder = specialProject.toBuilderWeiBo(pageNo, pageSize);
							builder.setDatabase(Const.WEIBO);
							countBuilder.setDatabase(Const.WEIBO);
						} else {// 可以在里面判断往后的图表类型和数据来源
							builder = specialProject.toBuilder(pageNo, pageSize);
							countBuilder = specialProject.toBuilder(pageNo, pageSize);
							builder.setDatabase(Const.HYBASE_NI_INDEX);
							countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
						}
					} else { // 指定图表数据,使用指定数据源
						if (Const.WEIBO.equals(type.getSource())) {
							builder = specialProject.toBuilderWeiBo(pageNo, pageSize);
							countBuilder = specialProject.toBuilderWeiBo(pageNo, pageSize);
							builder.setDatabase(Const.WEIBO);
							countBuilder.setDatabase(Const.WEIBO);
						} else if (Const.WECHAT.equals(type.getSource())) {
							builder.setDatabase(Const.WECHAT);
							builder = specialProject.toBuilderWeiXin(pageNo, pageSize);
							countBuilder = specialProject.toBuilderWeiXin(pageNo, pageSize);
							builder.setDatabase(Const.WECHAT);
							countBuilder.setDatabase(Const.WECHAT);
						} else {// 可以在里面判断往后的图表类型和数据来源
							builder = specialProject.toBuilder(pageNo, pageSize);
							countBuilder = specialProject.toBuilder(pageNo, pageSize);
							builder.setDatabase(Const.HYBASE_NI_INDEX);
							countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
						}
					}
				}
				if (!"ALL".equals(emotion)) { // 情感
					if("中性".equals(emotion)){
						builder.filterField(FtsFieldConst.FIELD_APPRAISE, new String[]{"正面","负面"}, Operator.NotEqual);
						countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, new String[]{"正面","负面"}, Operator.NotEqual);
					}else{
						builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
						countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
					}
				}
				if (!"ALL".equals(area)) { // 拼接地域
					String[] areaSplit = area.split(";");
					String contentArea = "";
					for (int i = 0; i < areaSplit.length; i++) {
						areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
						if (i != areaSplit.length - 1) {
							areaSplit[i] += " OR ";
						}
						contentArea += areaSplit[i];
					}
					builder.filterByTRSL(FtsFieldConst.CATALOG_AREANEW + ":" + contentArea);
					countBuilder.filterByTRSL(FtsFieldConst.CATALOG_AREANEW + ":" + contentArea);
				}

				// 拼接数据参数
				if (StringUtil.isNotEmpty(xType)) {
					if (xType.equals("微博PC端")) {
						xType = "微博 weibo.com";
					}
					String[] xTpyeField = type.getXTpyeField();// 字段名
					if (chartType.equals(ChartType.AREA.getType())) {// 地图
						xType = "中国\\\\" + xType + "*";
						builder.filterByTRSL(FtsFieldConst.CATALOG_AREANEW + ":" + xType);
						countBuilder.filterByTRSL(FtsFieldConst.CATALOG_AREANEW + ":" + xType);
					} else if (chartType.equals(ChartType.WORDCLOUD.getType())) {
						StringBuffer sb = new StringBuffer();
						if ("location".equals(entityType)) {
							String xTypeNew = "";
							if ("省".equals(xType.substring(xType.length() - 1))
									|| "市".equals(xType.substring(xType.length() - 1))) {
								xTypeNew = xType.replace("省", "");
								xTypeNew = xType.replace("市", "");
							} else if ("区".equals(xType.substring(xType.length() - 1)) || !xType.isEmpty()) {
								xTypeNew = xType;
							}
							if (!xTypeNew.contains("\"")) {
								xTypeNew = "\"" + xTypeNew + "\"";
							}
							builder.filterField(FtsFieldConst.FIELD_CONTENT, xTypeNew, Operator.Equal);
							countBuilder.filterField(FtsFieldConst.FIELD_CONTENT, xTypeNew, Operator.Equal);
						} else {
							builder.filterField(Const.PARAM_MAPPING.get(entityType), xType, Operator.Equal);
							countBuilder.filterField(Const.PARAM_MAPPING.get(entityType), xType, Operator.Equal);
							sb.append(FtsFieldConst.FIELD_CONTENT).append(":").append("\"").append(xType).append("\"");
							sb.append(" OR ").append(FtsFieldConst.FIELD_URLTITLE).append(":").append("\"")
									.append(xType).append("\"");
							builder.filterByTRSL(sb.toString());
							countBuilder.filterByTRSL(sb.toString());
						}
					} else {// 其他普通图表
						if (xTpyeField.length > 0) {// 字段名不确定 长度
							StringBuffer sb = new StringBuffer();
							String trsl = "";
							for (int i = 0; i < xTpyeField.length; i++) {
								if (i != xTpyeField.length - 1) {// 不是最后一个
									trsl = sb.append(xTpyeField[i]).append(":").append("\"").append(xType).append("\"")
											.append(" OR ").toString();
								} else {
									trsl = sb.append(xTpyeField[i]).append(":").append("\"").append(xType).append("\"")
											.toString();
								}
							}
							builder.filterByTRSL(trsl);
							countBuilder.filterByTRSL(trsl);
						}
					}
				}
			}
		}
		Object obj = null;

		if (builder.getDatabase().equals(Const.WEIBO)) {
			// obj = this.hybase8SearchService.ftsPageList(builder,
			// FtsDocumentStatus.class, true);
			// 结果中搜索
			String trsl = null;
			if (StringUtil.isNotEmpty(fuzzyValue)) {
				trsl = new StringBuffer().append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":\"").append(fuzzyValue)
						.append("\"").toString();
			}
//			if (trsl != null) {
//				trsl = trsl + " AND IR_RETWEETED_MID:(0 OR \"\")";
//			} else {
//				trsl = " IR_RETWEETED_MID:(0 OR \"\")";
//			}
			if (StringUtils.isNotBlank(forwarPrimary)) {

				if (forwarPrimary.equals("primary")) {
					if (StringUtils.isNotBlank(trsl)) {
						trsl = trsl + " AND IR_RETWEETED_MID:(0 OR \"\")";
					} else {
						trsl = " IR_RETWEETED_MID:(0 OR \"\")";
					}
				} else if (forwarPrimary.equals("forward")) {
					if (StringUtils.isNotBlank(trsl)) {
						trsl = trsl + " NOT IR_RETWEETED_MID:(0 OR \"\")";
					} else {
						trsl = "IR_GROUPNAME:微博 NOT IR_RETWEETED_MID:(0 OR \"\")";
					}
				}
			}
			builder.filterByTRSL(trsl);
			countBuilder.filterByTRSL(trsl);
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					break;
				case "hot":
					return infoListService.getHotListStatus(builder, countBuilder, loginUser,"special");
				default:// 相关性
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			log.info("WEIBO:" + builder.asTRSL());
			obj = infoListService.getStatusList(builder, loginUser, sim, false, false,false,"special");
		} else if (builder.getDatabase().equals(Const.WECHAT)) {
			// obj = this.hybase8SearchService.ftsPageList(builder,
			// FtsDocumentWeChat.class, true);
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":\"").append(fuzzyValue)
						.append("\" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":\"").append(fuzzyValue)
						.append("\"").toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			}
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return infoListService.getHotListWeChat(builder, countBuilder, loginUser,"special");
				default:
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			log.info("WECHAT:" + builder.asTRSL());

			obj = infoListService.getWeChatList(builder, loginUser, sim, false, false,false,"special");
		} else {
			if ("国内新闻".equals(source)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
						.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧 NOT ").append(FtsFieldConst.FIELD_GROUPNAME)
						.append(":(国内博客 OR 国内新闻_手机客户端 OR 国内新闻_电子报)").toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			} else if ("国内论坛".equals(source)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
						.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			} else if ("国内博客".equals(source)) {
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
			} else if ("国内新闻_客户端".equals(source)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻_手机客户端").toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			} else if ("国内新闻_电子报".equals(source)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻_电子报").toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			} else if ("".equals(source) && !chartType.equals(ChartType.AREA.getType())
					&& !chartType.equals(ChartType.WORDCLOUD.getType())) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
						.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧 NOT ").append(FtsFieldConst.FIELD_GROUPNAME)
						.append(":国内博客").toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			}
			// obj = this.hybase8SearchService.ftsPageList(builder,
			// FtsDocument.class, true);
			if (!"ALL".equals(industryType)) {// 拼接行业类型
				builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":\"").append(fuzzyValue)
						.append("\"").append(" OR ").append(FtsFieldConst.FIELD_ABSTRACTS).append(":\"")
						.append(fuzzyValue).append("\"").append(" OR ").append(FtsFieldConst.FIELD_CONTENT)
						.append(":\"").append(fuzzyValue).append("\"").append(" OR ")
						.append(FtsFieldConst.FIELD_KEYWORDS).append(":\"").append(fuzzyValue).append("\"").toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			}
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return infoListService.getHotList(builder, countBuilder, loginUser,"special");
				default:
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
			}
			log.info("HYBASE_NI_INDEX:" + builder.asTRSL());

			obj = infoListService.getDocList(builder, loginUser, true, false, false,false,"special");

		}
		return obj;
	}

	@Override
	public Map<String, Object> getNodesData(QueryBuilder searchBuilder, String mid)
			throws TRSException, TRSSearchException {
		// 原发
		QueryBuilder builder = new QueryBuilder();
		builder.setDatabase(Const.WEIBO);
		builder.filterField(FtsFieldConst.FIELD_MID, mid, Operator.Equal);
		List<FtsDocumentStatus> ftsDocumentStatuses = hybase8SearchService.ftsQuery(builder, FtsDocumentStatus.class,
				false, false,false,"special");
		FtsDocumentStatus ftsDocumentStatus = null;
		ftsDocumentStatus = ftsDocumentStatuses.get(0);
		if (ftsDocumentStatus == null) {
			return null;
		}
		// 转发
		List<FtsDocumentStatus> ftsDocumentStatuses1 = hybase8SearchService.ftsQuery(searchBuilder,
				FtsDocumentStatus.class, false, false,false,"special");
		/*
		 * for (FtsDocumentStatus documentStatus : ftsDocumentStatuses1) {
		 * //如果被转发 则成为引爆点 }
		 */
		Map<String, Object> returnMap = new HashMap<>();
		returnMap.put("status", ftsDocumentStatus);
		returnMap.put("retweets", ftsDocumentStatuses1);
		return returnMap;
	}

	@Override
	public Object getTopicEvoExplorData(QueryBuilder searchBuilder, String timeRange, boolean sim)
			throws TRSException, TRSSearchException {
		/*
		 * PagedList<FtsDocumentStatus> dataWeiBo =
		 * this.getDataWeiBo(searchBuilder, sim); List<FtsDocumentStatus>
		 * pageItems = dataWeiBo.getPageItems(); for (FtsDocumentStatus status :
		 * pageItems) {
		 * 
		 * }
		 */
		return null;
	}

	@Override
	public List<ViewEntity> getUserViewsData(SpecialProject specialProject, QueryBuilder searchBuilder,
											 String[] timeArray, boolean sim) throws TRSException, TRSSearchException {
		String source = specialProject.getSource();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		searchBuilder.filterField("IR_URLTIME", timeArray, Operator.Between);
		List<FtsDocumentStatus> ftsDocumentStatuses = null;
		List<FtsDocumentWeChat> ftsDocumentWeChats = null;
		GroupResult groupLunTan = null;

		String trsl = searchBuilder.asTRSL();
		// 微博
		QueryBuilder builderWeiBo = new QueryBuilder();
		builderWeiBo.filterByTRSL(trsl);
		builderWeiBo.setDatabase(Const.WEIBO);
		builderWeiBo.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
		ftsDocumentStatuses = hybase8SearchService.ftsQuery(builderWeiBo, FtsDocumentStatus.class, sim, irSimflag,irSimflagAll,"special");

		// 微信
		QueryBuilder builderWeChat = new QueryBuilder();
		builderWeChat.filterByTRSL(trsl);
		builderWeChat.orderBy(FtsFieldConst.FIELD_PRCOUNT, true);
		builderWeChat.setDatabase(Const.WECHAT);
		ftsDocumentWeChats = hybase8SearchService.ftsQuery(builderWeChat, FtsDocumentWeChat.class, sim, irSimflag,irSimflagAll,"special");

		// 论坛

		StringBuffer sb = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
				.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").append(" AND ")
				.append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
		searchBuilder.filterByTRSL(sb.toString());
		List<GroupInfo> groupInfos = null;
		List<FtsDocumentStatus> ftsStatuses = null;
		List<FtsDocumentWeChat> ftsWeChats = null;
		// 根据创建专题所选来源 决定网友观点 取自哪里
		if (source.contains("国内论坛") && source.contains("微博") && !source.contains("微信")) {
			List<GroupInfo> groupList = groupLunTan.getGroupList();
			if (ftsDocumentStatuses != null) {
				if (ftsDocumentStatuses.size() >= 8) {

					ftsStatuses = ftsDocumentStatuses.subList(0, 8);

				} else {
					ftsStatuses = ftsDocumentStatuses;
				}
			}
			if (groupList != null) {
				if (groupList.size() >= 4) {
					groupInfos = groupList.subList(0, 4);
					if (8 - ftsStatuses.size() > 4) {
						if (groupList.size() >= 8 - ftsStatuses.size()) {
							groupInfos = groupList.subList(0, 8 - ftsStatuses.size());
						} else {
							groupInfos = groupList;
						}
					}
				} else {
					groupInfos = groupList;
				}
			}
		} else if (source.contains("国内论坛") && !source.contains("微博") && source.contains("微信")) {
			List<GroupInfo> groupList = groupLunTan.getGroupList();
			if (ftsDocumentWeChats != null) {
				if (ftsDocumentWeChats.size() >= 4) {
					if (groupList.size() > 4) {
						ftsWeChats = ftsDocumentWeChats.subList(0, 4);
					} else {
						ftsWeChats = ftsDocumentWeChats.subList(0, 8 - groupList.size());
					}
				} else {
					ftsWeChats = ftsDocumentWeChats;
				}
			}

			if (groupList != null) {
				if (groupList.size() >= 4) {
					groupInfos = groupList.subList(0, 4);
					if (8 - ftsWeChats.size() > 4 && groupList.size() >= 8 - ftsWeChats.size()) {
						groupInfos = groupList.subList(0, 8 - ftsWeChats.size());
					}
				} else {
					groupInfos = groupList;
				}

			}
		} else if (!source.contains("国内论坛") && source.contains("微博") && source.contains("微信")) {
			if (ftsDocumentStatuses != null) {
				if (ftsDocumentStatuses.size() > 8) {

					ftsStatuses = ftsDocumentStatuses.subList(0, 8);

				} else {
					ftsStatuses = ftsDocumentStatuses;
				}
			}

			if (ftsDocumentWeChats != null) {
				if (ftsDocumentWeChats.size() >= 4) {
					ftsWeChats = ftsDocumentWeChats.subList(0, 4);
					if (8 - ftsStatuses.size() > 4) {
						if (ftsDocumentWeChats.size() >= 8 - ftsStatuses.size()) {
							ftsWeChats = ftsDocumentWeChats.subList(0, 8 - ftsStatuses.size());
						} else {
							ftsWeChats = ftsDocumentWeChats;
						}
					}
				} else {
					ftsWeChats = ftsDocumentWeChats;
				}
			}
		} else if ("ALL".equals(source) || source.contains("国内论坛") && source.contains("微博") && source.contains("微信")) {
			List<GroupInfo> groupList = null;
			if (groupLunTan != null) {
				groupList = groupLunTan.getGroupList();
			}

			if (ftsDocumentStatuses != null) {
				if (ftsDocumentStatuses.size() > 8) {
					ftsStatuses = ftsDocumentStatuses.subList(0, 8);
				} else {
					ftsStatuses = ftsDocumentStatuses;
				}
			}
			if (groupList != null) {
				if (groupList.size() > 3) {
					groupInfos = groupList.subList(0, 3);
				} else {
					groupInfos = groupList;
				}
			}

			if (ftsDocumentWeChats != null) {
				if (ftsDocumentWeChats.size() > 2) {

					ftsWeChats = ftsDocumentWeChats.subList(0, 2);

				} else {
					ftsWeChats = ftsDocumentWeChats;
				}
			}
		}
		// 从这开始 仅仅是为了统计count 总数
		List<ViewEntity> viewEntities = new ArrayList<>();
		if (groupInfos != null) {
			for (GroupInfo groupInfo : groupInfos) {
				ViewEntity viewEntity = new ViewEntity();
				QueryBuilder builder = new QueryBuilder();
				builder.filterField(FtsFieldConst.FIELD_HKEY, groupInfo.getFieldValue(), Operator.Equal);
				List<FtsDocument> ftsDocuments = hybase8SearchService.ftsQuery(builder, FtsDocument.class, sim,
						irSimflag,irSimflagAll,"special");
				if (ftsDocuments != null) {
					viewEntity.setView(subView(ftsDocuments.get(0).getTitle(), 25));
				}
				if (groupInfo.getCount() == 0) {
					break;
				}
				viewEntity.setCount(groupInfo.getCount());
				if (ftsDocuments.get(0).getAppraise() == "" || ftsDocuments.get(0).getAppraise() == " "
						|| ftsDocuments.get(0).getAppraise() == null) {
					viewEntity.setAppraise("中性");
				} else {
					viewEntity.setAppraise(ftsDocuments.get(0).getAppraise());
				}
				viewEntities.add(viewEntity);
			}
		}

		if (ftsWeChats != null) {
			for (FtsDocumentWeChat ftsWeChat : ftsWeChats) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.setView(subView(ftsWeChat.getUrlTitle(), 25));
				if (ftsWeChat.getPrcount() == 0) {
					break;
				}
				viewEntity.setCount(ftsWeChat.getPrcount());
				if (ftsWeChat.getAppraise() == "" || ftsWeChat.getAppraise() == " "
						|| ftsWeChat.getAppraise() == null) {
					viewEntity.setAppraise("中性");
				} else {
					viewEntity.setAppraise(ftsWeChat.getAppraise());
				}
				viewEntities.add(viewEntity);
			}
		}

		if (ftsStatuses != null) {
			int size = viewEntities.size();
			for (int i = 0; i < ftsStatuses.size() - size; i++) {
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.setView(subView(ftsStatuses.get(i).getStatusContent(), 25));
				viewEntity.setCount(ftsStatuses.get(i).getCommtCount());
				if (ftsStatuses.get(i).getAppraise() == "" || ftsStatuses.get(i).getAppraise() == " "
						|| ftsStatuses.get(i).getAppraise() == null) {
					viewEntity.setAppraise("中性");
				} else {
					viewEntity.setAppraise(ftsStatuses.get(i).getAppraise());
				}
				viewEntities.add(viewEntity);

			}
		}

		// count 统计结束 做最后数据处理 算权重

		/*
		 * for (FtsDocumentStatus status : documents) { ViewEntity viewEntity =
		 * new ViewEntity(); float v = (float) status.getCommtCount() / count;
		 * DecimalFormat df = new DecimalFormat(".##%"); String format =
		 * df.format(v); //String str = String.valueOf((float)
		 * status.getCommtCount() / count * 100) + "%";
		 * viewEntity.setView(subView(status.getStatusContent(), 25));
		 * viewEntity.setCount(format);
		 * viewEntity.setAppraise(status.getAppraise());
		 * viewEntity.setId(status.getId()); viewEntities.add(viewEntity);
		 * 
		 * }
		 */
		return viewEntities;
	}

	@Override
	public Object getWordCloud(boolean server, String trsl, boolean sim, boolean irSimflag,boolean irSimflagAll, String entityType,
							   int limit,String type, String... data) throws  TRSSearchException {
		GroupResult result = new GroupResult();
		try {
			GroupWordResult wordInfos = new GroupWordResult();

			if ("keywords".equals(entityType)) {
				// 人物、地域、机构
				GroupResult people = hybase8SearchService.categoryQuery(server, trsl, sim, irSimflag,irSimflagAll,
						Const.PARAM_MAPPING.get("people"), limit, type,data);
				GroupResult location = hybase8SearchService.categoryQuery(server, trsl, sim, irSimflag,irSimflagAll,
						Const.PARAM_MAPPING.get("location"), limit,type, data);
				GroupResult agency = hybase8SearchService.categoryQuery(server, trsl, sim, irSimflag,irSimflagAll,
						Const.PARAM_MAPPING.get("agency"), limit,type, data);
				List<GroupInfo> peopleList = people.getGroupList();
				List<GroupInfo> locationList = location.getGroupList();
				List<GroupInfo> agencyList = agency.getGroupList();

				for (GroupInfo groupInfo : peopleList) {
					wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), "people");
				}

				for (GroupInfo groupInfo : locationList) {
					wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), "location");
				}

				for (GroupInfo groupInfo : agencyList) {
					wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), "agency");
				}

			} else {
				result = hybase8SearchService.categoryQuery(server, trsl, sim, irSimflag,irSimflagAll,
						Const.PARAM_MAPPING.get(entityType), limit,type, data);
				List<GroupInfo> groupList = result.getGroupList();
				for (GroupInfo groupInfo : groupList) {
					wordInfos.addGroup(groupInfo.getFieldValue(), groupInfo.getCount(), entityType);
				}
			}
			wordInfos.sort();

			List<GroupWordInfo> groupWordList = wordInfos.getGroupList();
			List<GroupWordInfo> newGroupWordList = new ArrayList<>();
			if (groupWordList.size() > 50) {
				newGroupWordList = groupWordList.subList(0, 50);
			} else {
				newGroupWordList.addAll(groupWordList);
			}
			// 数据清理
			for (int i = 0; i < groupWordList.size(); i++) {
				String name = groupWordList.get(i).getFieldValue();
				if (name.endsWith("html")) {
					groupWordList.remove(i);
				}
				if (name.contains(";")) {
					String[] split = name.split(";");
					name = split[split.length - 1];
				}
				if (name.contains("\\")) {
					String[] split = name.split("\\\\");
					name = split[split.length - 1];
				}
				if (name.contains(".")) {
					String[] split = name.split("\\.");
					name = split[split.length - 1];
				}
				groupWordList.get(i).setFieldValue(name);
			}
			if (ObjectUtil.isNotEmpty(newGroupWordList) && newGroupWordList.size() > 0){
				wordInfos.setGroupList(newGroupWordList);
				return wordInfos;
			}else {
				return null;
			}
		} catch (TRSSearchException e) {
			throw new TRSSearchException(e);
		}
	}

	@Override
	public Object getWordCloudNew(QueryBuilder builder, boolean sim, boolean irSimflag, boolean irSimflagAll, String entityType, String type) throws TRSSearchException {
		return null;
	}

	@Override
	public Object getVolume(QueryBuilder searchBuilder, String timerange, boolean sim, boolean irSimflag,boolean irSimflagAll,String showType)
			throws TRSException, TRSSearchException {
		String[] timeArray = DateUtil.formatTimeRange(timerange);
		String trsl = searchBuilder.asTRSL();
		GroupResult categoryQuery = null;
		Map<String, GroupResult> map = new HashMap<>();
		for (String appraise : Const.APPRAISE) {
			QueryBuilder builder = new QueryBuilder();
			builder.filterByTRSL(trsl);
			builder.setPageSize(1000);
			// 拼接情感值
			String appraiseSql = null;
			if ("".equals(appraise)) {
				appraiseSql = FtsFieldConst.FIELD_APPRAISE + ":" + "\"\"";
			} else {
				appraiseSql = FtsFieldConst.FIELD_APPRAISE + ":" + appraise;

			}
			builder.filterByTRSL(appraiseSql);
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			// 拼接时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
			// 根据时间 按照天分类统计 还是 小时分类统计
			log.info(builder.asTRSL());
			if (timerange.endsWith("h") || "0d".equals(timerange)) {
				categoryQuery = hybase8SearchService.categoryQuery(builder, sim, irSimflag,irSimflagAll,
						FtsFieldConst.FIELD_URLTIME_HOUR, Const.MIX_DATABASE);
			} else {// if(timerange.endsWith("d") && !"0d".equals(timerange))
				// 自定义时间也走这个
				categoryQuery = hybase8SearchService.categoryQuery(builder, sim, irSimflag,irSimflagAll, FtsFieldConst.FIELD_URLDATE,
						Const.MIX_DATABASE);
			}
			map.put(appraise, categoryQuery);
		}
		String start = null;
		String end = null;
		List<String> dateList = null;
		// 今天和24h都是小时
		if ("24h".equals(timerange)) {
			dateList = DateUtil.get24Hour();
			dateList.remove(0);// 24小时时间有重复 先查23小时
		} else if ("0d".equals(timerange)) {
			dateList = DateUtil.getCurrentDateHours();
			// 截取只留小时
			List<String> datelist = new ArrayList<>();
			for (String dateEvery : dateList) {
				datelist.add(dateEvery.substring(8, 10));
			}
			dateList = datelist;
		} else {
			start = timeArray[0].substring(0, 4) + "-" + timeArray[0].substring(4, 6) + "-"
					+ timeArray[0].substring(6, 8);
			end = timeArray[1].substring(0, 4) + "-" + timeArray[1].substring(4, 6) + "-"
					+ timeArray[1].substring(6, 8);
			dateList = DateUtil.getBetweenDateString(start, end, "yyyy-MM-dd");
		}
		List<Map<String, Object>> listAll = new ArrayList<>();
		for (String date : dateList) {
			Map<String, Object> mapDate = new HashMap<>();
			// 当前日期
			// List<Map<String,Long>> mapSumList = new ArrayList<>();
			Map<String, Long> mapSum = new HashMap<>();
			for (String key : map.keySet()) {
				// Map<String,Long> mapSum = new HashMap<>();
				long count = 0;
				// key是情感
				GroupResult groupResult = map.get(key);
				for (GroupInfo info : groupResult) {
					String fieldValue = info.getFieldValue().replaceAll("[-/.: ]", "").trim();
					String dateReplace = date.replaceAll("[-/.: ]", "").trim();
					if (dateReplace.equals(fieldValue)) {
						// 当前日期当前情感的数量
						count = info.getCount();
					}
				}
				if ("正面".equals(key)) {
					key = "zhengmian";
				} else if ("负面".equals(key)) {
					key = "fumian";
				} else if ("".equals(key)) {
					key = "zhongxing";
				}
				mapSum.put(key, count);
				// mapSumList.add(mapSum);
			}
			// date的格式要改
			mapDate.put("time", date);
			// mapDate.put("", value)
			mapDate.put("appraise", mapSum);
			listAll.add(mapDate);
		}
		return listAll;
	}

	@Override
	public List<AbsTheme> getTopicData(QueryBuilder query, String groupName, boolean sim)
			throws TRSException, TRSSearchException {
		List<FtsDocumentWeChat> ftsDocumentWeChats = hybase8SearchService.ftsQuery(query, FtsDocumentWeChat.class, sim,
				false,false,"special");
		List<String> contents = new ArrayList<>();

		List<AbsTheme> theme = null;
		if (ftsDocumentWeChats.size() > 50) {
			for (int i = 0; i < 50; i++) {
				String content = ftsDocumentWeChats.get(i).getContent();
				contents.add(content);
			}
			try {
				theme = ckmService.theme(contents);
			} catch (CkmSoapException e) {
				e.printStackTrace();
			}

		}
		// 先这样返回，等确定哪个是权重字段再决定
		return theme;
	}

	/**
	 * 根据专题信息 获取微博数据
	 *
	 * @param searchBuilder
	 * @param sim
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	public PagedList<FtsDocumentStatus> getDataWeiBo(QueryBuilder searchBuilder, boolean sim)
			throws TRSException, TRSSearchException {
		PagedList<FtsDocumentStatus> pagedList = hybase8SearchService.ftsPageList(searchBuilder,
				FtsDocumentStatus.class, sim, false,false,null);
		return pagedList;
	}

	@Override
	public SpreadNewsEntity pathByNews(SpecialProject project, QueryBuilder builder, SpreadNewsEntity root,
									   String[] timeArray, boolean irSimflag,boolean irSimflagAll) throws Exception {

		// 再次确认root节点
		root = checkRoot(project, builder, root, project.isSimilar());
		computeSpreadNewsNodes(builder.asTRSL(), root, project.isSimilar(), irSimflag,irSimflagAll);
		return root;
	}

	/**
	 * 递归遍历节点
	 *
	 * @since changjiang @ 2018年5月9日
	 * @param father
	 * @param isSimilar
	 * @throws TRSSearchException
	 * @throws TRSException
	 * @Return : void
	 */
	private void computeSpreadNewsNodes(String trsl, SpreadNewsEntity father, boolean isSimilar, boolean irSimflag,boolean irSimflagAll)
			throws TRSSearchException, TRSException {
		QueryBuilder change = new QueryBuilder();
		List<SpreadNewsEntity> list = null;
		while (true) {
			change.filterByTRSL(trsl);
			change.filterField(FtsFieldConst.FIELD_SRCNAME, father.getName(), Operator.Equal);
			String trsll = change.asTRSL();
			trsll += " NOT IR_SITENAME:" + father.getName();
			change = new QueryBuilder();
			change.filterByTRSL(trsll);
			change.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			change.filterField("IR_SIMFLAG", "0", Operator.Equal);
			list = hybase8SearchService.ftsQuery(change, SpreadNewsEntity.class, isSimilar, irSimflag,irSimflagAll,"special");
			System.out.println(change.asTRSL());
			if (list != null && list.size() > 0) {
				for (SpreadNewsEntity spreadNewsEntity : list) {
					computeSpreadNewsNodes(trsl, spreadNewsEntity, isSimilar, irSimflag,irSimflagAll);
				}
			}
			break;
		}
		// 同级节点排重
		List<SpreadNewsEntity> result = null;
		if (list != null && list.size() > 0) {
			result = new ArrayList<>();
			result.add(list.get(0));
			for (SpreadNewsEntity spreadNewsEntity : list) {
				boolean sim = false;
				for (SpreadNewsEntity entity : result) {
					if (entity.getName().equals(spreadNewsEntity.getName())) {
						sim = true;
						break;
					}
					if (!sim) {
						result.add(spreadNewsEntity);
						break;
					}
				}
			}
		}
		father.setChildren(result);
	}

	/**
	 * 确定发布时间最早且非转发新闻,定位root节点新闻
	 *
	 * @since changjiang @ 2018年5月9日
	 * @param builder
	 * @param root
	 * @param isSimilar
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 * @Return : SpreadNewsEntity
	 */
	private SpreadNewsEntity checkRoot(SpecialProject project, QueryBuilder builder, SpreadNewsEntity root,
									   boolean isSimilar) throws TRSSearchException, TRSException {
		// url排重
		boolean irSimflag = project.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = project.isIrSimflagAll();
		List<SpreadNewsEntity> list = null;
		if ((root.getName().equals(root.getSrcName()) || StringUtils.isBlank(root.getSrcName()))) {
			return root;
		} else {
			builder = project.toBuilder(0, 1, false);
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			Date endUrlTime = root.getUrlTime();
			String endUrlTimeStr = DateUtil.format2String(endUrlTime, DateUtil.yyyyMMddHHmmss);
			String beginUrlTimeStr = DateUtil.formatDateAfter(
					DateUtil.format2String(endUrlTime, DateUtil.yyyyMMddHHmmss), DateUtil.yyyyMMddHHmmss, -7);
			String[] between = { endUrlTimeStr, beginUrlTimeStr };
			builder.filterField(FtsFieldConst.FIELD_URLTIME, between, Operator.Between);
			// clean srcName
			String srcName = root.getSrcName();
			if (srcName.contains("\r")) {
				srcName = srcName.substring(0, srcName.indexOf("\r"));
			}
			builder.filterField(FtsFieldConst.FIELD_SITENAME, srcName, Operator.Equal);
			builder.filterField("IR_SIMFLAG", "0", Operator.Equal);
			System.out.println(builder.asTRSL());
			list = this.hybase8SearchService.ftsQuery(builder, SpreadNewsEntity.class, isSimilar, irSimflag,irSimflagAll,"special");
			if (list != null && list.size() > 0) {
				return list.get(0);
			}
		}

		return root;

	}

	/**
	 * 去掉内容中特殊符号 并截取指定长度（传播路径使用）
	 *
	 * @param content
	 * @param len
	 * @return
	 */
	public String subContent(String content, int len) {
		content = StringUtil.replaceImg(content);
		content = StringUtil.replaceFont(content);
		String[] split = content.split("@");
		if (split.length > 1) {
			content = split[split.length - 1];
		}
		if (content.length() > len) {
			String ss = content.substring(0, len);
			content = ss;
		}
		if (content.endsWith(",") || content.endsWith("，")) {
			content = content.replace(content.charAt(content.length() - 1) + "", " ");
		}
		return content;
	}

	/**
	 * 去掉内容中特殊符号 并截取指定长度(网友观点使用)
	 *
	 * @param content
	 * @return
	 */
	public String subView(String content, int len) {
		content = StringUtil.replaceImg(content);
		content = StringUtil.replaceFont(content);
		content = StringUtil.replacePeriod(content);
		if (content.contains("@")) {
			String[] split = content.split("@");
			if (split.length > 1) {
				content = split[split.length - 1];
			}
		}
		if (content.length() > len) {
			String ss = content.substring(0, len);
			content = ss;
		}

		if (content.endsWith(",") || content.endsWith("，")) {
			content = content.replace(content.charAt(content.length() - 1) + "", " ");
		}

		return content;
	}

	@Override
	public List<Map<String, Object>> newsSiteAnalysis(QueryBuilder searchBuilder, String[] timeArray, boolean similar,
													  boolean irSimflag,boolean irSimflagAll,boolean isApi) throws TRSSearchException {
		// 最多显示几个
		int maxLength = 5;
		List<String> list = DateUtil.getBetweenDateString(timeArray[0], timeArray[1], DateUtil.yyyyMMddHHmmss,
				DateUtil.yyyyMMdd3);
		if (list != null) {
			List<Map<String, Object>> listMapData = new ArrayList<>();
			int size = list.size();
			if (size > 30) {
				list = list.subList(size - 30, size);
			}
			for (String time : list) {
				Map<String, Object> mapData = new HashMap<>();
				List<GroupInfo> listData = new ArrayList<>();
				// wxb白名单的数据
				QueryBuilder builderWxb = new QueryBuilder(0, 100);
				builderWxb.filterByTRSL(searchBuilder.asTRSL());
				builderWxb.filterField(FtsFieldConst.FIELD_URLDATE, time, Operator.Equal);
				builderWxb.filterField(FtsFieldConst.FIELD_WXB_LIST, "0", Operator.Equal);
				GroupResult resultWxb = hybase8SearchService.categoryQuery(builderWxb, similar, irSimflag,irSimflagAll,
						FtsFieldConst.FIELD_SITENAME, searchBuilder.getDatabase());
				if (resultWxb != null) {
					listData.addAll(resultWxb.getGroupList());
				}
				// 如果wxb白名单的数量够了就不查非白名单的了
				if (listData.size() < maxLength) {
					// 非wxb白名单的数据
					QueryBuilder builderNotWxb = new QueryBuilder(0, 100);
					builderNotWxb.filterByTRSL(searchBuilder.asTRSL());
					builderNotWxb.filterField(FtsFieldConst.FIELD_URLDATE, time, Operator.Equal);
					builderNotWxb.filterField(FtsFieldConst.FIELD_WXB_LIST, "0", Operator.NotEqual);
					GroupResult resultNotWxb = hybase8SearchService.categoryQuery(builderNotWxb, similar, irSimflag,irSimflagAll,
							FtsFieldConst.FIELD_SITENAME, searchBuilder.getDatabase());
					if (resultNotWxb != null) {
						listData.addAll(resultNotWxb.getGroupList());
					}
				}
				// 取 maxLength 个
				if ( !isApi ){
					if (listData.size() > maxLength) {
						listData = listData.subList(0, maxLength);
					}
				}


				mapData.put("time", time);
				mapData.put("data", listData);
				listMapData.add(mapData);
			}
			return listMapData;
		}
		return null;
	}

    @Override
    public List<Map<String, Object>> spreadAnalysis(QueryBuilder searchBuilder, String[] timeArray, boolean similar, boolean irSimflag, boolean irSimflagAll, boolean isApi, String groupName) throws TRSSearchException {
        return null;
    }

    @Override
	public ArrayList<HashMap<String, Object>> getMoodStatistics(SpecialProject specialProject, String timeRange, SpecialParam specParam) {
		return null;
	}

	@Override
	public HashMap<String, Object> getUserViewsData(SpecialProject specialProject, String timeRange,
													String industry, String area, SpecialParam specParam)  throws Exception{
		return null;
	}

	@Override
	public ByteArrayOutputStream exportBarOrPieData(String dataType,JSONArray array) throws IOException {
		return null;
	}

	@Override
	public ByteArrayOutputStream exportChartLineData(String dataType,JSONArray array) throws IOException {
		return null;
	}

	@Override
	public ByteArrayOutputStream exportWordCloudData(String dataType,JSONArray array) throws IOException {
		return null;
	}

	@Override
	public ByteArrayOutputStream exportMapData(JSONArray array) throws IOException {
		return null;
	}

	@Override
	public List<Map<String, String>> emotionOption(QueryBuilder searchBuilder, SpecialProject specialProject) {

		List<Map<String, String>> list=new ArrayList<>();
		boolean sim = specialProject.isSimilar();
		boolean irSimflag = specialProject.isIrSimflag();
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		//跨数据源排重
		if (irSimflagAll){
			searchBuilder.filterField(FtsFieldConst.FIELD_IR_SIMFLAGALL,"0 OR \"\"",Operator.Equal);
		}
		String trsl = searchBuilder.asTRSL();
		log.info(trsl);
		GroupResult records = hybase8SearchService.categoryQuery(specialProject.isServer(), trsl, sim, irSimflag,irSimflagAll,
				ESFieldConst.IR_APPRAISE, 3, Const.WEIBO);
		trsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_APPRAISE).append(":(").append("正面")
				.append(" OR ").append("负面").append(")").toString();
		searchBuilder = new QueryBuilder();
		searchBuilder.filterByTRSL(trsl);
		searchBuilder.setDatabase(Const.WEIBO);
		long ftsCount = hybase8SearchService.ftsCount(searchBuilder, sim, irSimflag,irSimflagAll,"special");

		List<GroupInfo> groupList = records.getGroupList();
		for (GroupInfo group : groupList) {
			Map<String, String> map = new HashMap<String, String>();
			String fieldValue = group.getFieldValue();
			if ("负面".equals(fieldValue) || "正面".equals(fieldValue)) {
				map.put("name", group.getFieldValue());
				map.put("value", String.valueOf(group.getCount()));
			} else {
				map.put("name", group.getFieldValue());
				map.put("value", String.valueOf(ftsCount));
			}
			list.add(map);
		}

		List<String> keys = new ArrayList<>();
		for (Map<String, String> map : list) {
			Set<String> strings = map.keySet();
			for (String string : strings) {
				if ("name".equals(string)) {
					String value = map.get(string);
					keys.add(value);
				}
			}
		}
		if (!keys.contains("正面")) {
			Map<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("name", "正面");
			hashMap.put("value", "0");
			list.add(hashMap);
		} else if (!keys.contains("负面")) {
			Map<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("name", "负面");
			hashMap.put("value", "0");
			list.add(hashMap);
		}
		return list;

	}

	@Override
	public int getSituationAssessment(QueryBuilder searchBuilder, SpecialProject specialProject) throws TRSException {
		return 0;
	}

	@Override
	public List<ClassInfo> ordinarySearchstatistics(boolean sim, boolean irSimflag,boolean irSimflagAll,String keywords,String[] timeRange, String source,String keyWordIndex,Boolean weight,String searchType) throws TRSException{
		List<ClassInfo> list = new ArrayList<>();

		try {
			QueryBuilder builder = new QueryBuilder();
			builder.setPageNo(0);
			builder.setPageSize(20);
			String originKeyword = keywords;
			keywords = keywords.replaceAll("\\s+", ",");
			StringBuilder childBuilder = new StringBuilder();
			// 关键词中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				// 防止全部关键词结尾为;报错
				String replaceAnyKey = "";
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					replaceAnyKey = keywords.substring(0, keywords.length() - 1);
					childBuilder.append("((\"").append(
							replaceAnyKey.replaceAll("[,|，]+", "\")  AND  (\"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childBuilder.append("((\"")
							.append(keywords.replaceAll("[,|，]+", "\")  AND  (\"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
			}
			//普通搜索可以使用模糊查询
			Boolean isFuzzySearch = false;
			if("fuzzy".equals(searchType)){
				isFuzzySearch = true;
			}
			if (childBuilder.length() > 0) {
				/*if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					if (weight) {
						QueryBuilder weightBuilder = new QueryBuilder();
						weightBuilder.page(0, 20);
						QueryBuilder weightBuilder_wx = new QueryBuilder();
						weightBuilder_wx.page(0, 20);
						weightBuilder.filterByTRSL(builder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+"("+ FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString()+")");
						builder = weightBuilder;
					} else {
						builder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
					}
				} else if (keyWordIndex.equals("positionKey")) {
					// 仅标题
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
				}*/
				String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch,true);
				builder.filterByTRSL(filterTrsl);
				// 时间  这个位置不要在加权重之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
			}else {
				// 时间 只有排除词的情况要加在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, timeRange, Operator.Between);
			}
			source = replaceGroupName(source);
			if (StringUtil.isNotEmpty(source)) {
				if (!"ALL".equals(source)) {
					builder.filterField(
							FtsFieldConst.FIELD_GROUPNAME, source.replace(";", " OR ").replace("境外媒体", "国外新闻"),
							Operator.Equal);
				} else {
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME,
							Const.STATTOTAL_GROUP.replace(";", " OR ").replace("境外媒体", "国外新闻"), Operator.Equal);
				}
			}
			log.info(builder.asTRSL());
			GroupResult categoryQuery = commonListService.categoryQuery(builder,sim,irSimflag,irSimflagAll,FtsFieldConst.FIELD_GROUPNAME,null);
			/*GroupResult categoryQuery = hybase8SearchService.categoryQuery(builder, sim, irSimflag,irSimflagAll,
					FtsFieldConst.FIELD_GROUPNAME, null,Const.MIX_DATABASE);*/
			List<GroupInfo> groupList = categoryQuery.getGroupList();
			Map<String, Long> map = new LinkedHashMap<>();
			map.put("国内新闻", 0L);
			map.put("微博", 0L);
			map.put("微信", 0L);
			map.put("国内新闻_手机客户端", 0L);
			map.put("国内论坛", 0L);
			map.put("国内博客", 0L);
			map.put("国内新闻_电子报", 0L);
			map.put("国外新闻", 0L);
			map.put("Twitter", 0L);
			map.put("FaceBook", 0L);
			for (GroupInfo groupInfo : groupList) {
				String fieldValue = groupInfo.getFieldValue();
				if ("国内微信".equals(fieldValue)) {
					fieldValue = "微信";
				}
				if ("境外新闻".equals(fieldValue) || "境外媒体".equals(fieldValue)) {
					fieldValue = "国外新闻";
				}
				if ("Facebook".equals(fieldValue)) {
					fieldValue = "FaceBook";
				}
				map.put(fieldValue, groupInfo.getCount());
			}

			Iterator<Map.Entry<String, Long>> iterMap = map.entrySet().iterator();
			while (iterMap.hasNext()) {
				Map.Entry<String, Long> entry = iterMap.next();
				String key = entry.getKey();
				list.add(new ClassInfo(key, entry.getValue().intValue()));
			}

		} catch (Exception e) {
			throw new OperationException("普通搜索数据统计查询出错：" + e, e);
		}
		return  list;
	}

	public String replaceGroupName(String groupName){
		//"国内新闻;国内论坛;国内博客;国内新闻_电子报;国内新闻_手机客户端;国外新闻;国内微信;微博;Twitter;FaceBook";
		if(!"ALL".equals(groupName)){
			groupName = groupName.replaceAll("国外新闻","境外媒体");
			if(groupName.contains("新闻") && !groupName.contains("国内新闻")){
				groupName = groupName.replaceAll("新闻","国内新闻");
			}
			if(groupName.contains("电子报") && !groupName.contains("国内新闻_电子报")){
				groupName = groupName.replaceAll("电子报","国内新闻_电子报");
			}
			if(groupName.contains("论坛") && !groupName.contains("国内论坛")){
				groupName = groupName.replaceAll("论坛","国内论坛");
			}
			if(groupName.contains("博客") && !groupName.contains("国内博客")){
				groupName = groupName.replaceAll("博客","国内博客");
			}
			if(groupName.contains("客户端") && !groupName.contains("国内新闻_手机客户端")){
				groupName = groupName.replaceAll("客户端","国内新闻_手机客户端");
			}
			if(groupName.contains("微信") && !groupName.contains("国内微信")){
				groupName = groupName.replaceAll("微信","国内微信");
			}
			groupName = groupName.replaceAll("境外媒体","国外新闻");
		}
		return groupName;
	}

}
