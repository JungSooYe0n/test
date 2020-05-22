
package com.trs.netInsight.widget.analysis.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.trs.ckm.soap.AbsTheme;
import com.trs.ckm.soap.CkmSoapException;
import com.trs.dc.entity.TRSStatisticParams;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.ExcelConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.ckm.ICkmService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
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
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.report.excel.ExcelData;
import com.trs.netInsight.support.report.excel.ExcelFactory;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.entity.*;
import com.trs.netInsight.widget.analysis.enums.ChartType;
import com.trs.netInsight.widget.analysis.enums.Top5Tab;
import com.trs.netInsight.widget.analysis.service.IChartAnalyzeService;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.base.enums.ESGroupName;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.special.entity.InfoListResult;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.trs.netInsight.config.constant.ChartConst.*;
import static java.util.stream.Collectors.toList;

/**
 * 无锡 图表分析服务
 * <p>
 * Created by mawen on 2017/12/5.
 */
@Service
@Slf4j
public class SpecialChartAnalyzeService implements IChartAnalyzeService {

	@PersistenceContext
	private EntityManager entityManager;

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
	private ICommonChartService commonChartService;
	@Autowired
	private ICommonListService commonListService;

	@Override
	public Object mediaLevel(QueryBuilder builder) throws TRSException {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object viaPreference(QueryBuilder builder, boolean sim, boolean irSimflag,boolean irSimflagAll) throws TRSException {
		try {
			List<String> ticks = new ArrayList<>();
			List<Integer> value = new ArrayList<>();
			List<String> list = new ArrayList();
			List data = new ArrayList();
			int sum = 0;
			builder.setDatabase(Const.WEIBO);
			GroupResult via = hybase8SearchService.categoryQuery(builder.isServer(), builder.asTRSL(), sim, irSimflag,irSimflagAll,
					"IR_VIA", 6,"special", Const.WEIBO);
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
			GroupResult infoList = hybase8SearchService.categoryQuery(queryBuilder, true, false, false,"IR_SITENAME",
					"special",Const.HYBASE_NI_INDEX);
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
	public Object mediaActiveLevel(QueryBuilder builder, String source, String[] timeArray, boolean sim,
								   boolean irSimflag, boolean irSimflagAll) throws TRSException {
		try {
			QueryBuilder dataBuilder = new QueryBuilder();
			dataBuilder.filterByTRSL(builder.asTRSL());
			if (StringUtil.isNotEmpty(source)){
				if ("境外网站".equals(source)){
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
			dataBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,source, Operator.Equal);

			String groupField ="";
			String database = "";
			if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
				groupField = FtsFieldConst.FIELD_SITENAME;
				database = Const.WECHAT;
			} else if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
				groupField = FtsFieldConst.FIELD_AUTHORS;
				database = Const.WEIBO;
			} else if (Const.MEDIA_TYPE_TF.contains(source)) {
				groupField = FtsFieldConst.FIELD_AUTHORS;
				database = Const.HYBASE_OVERSEAS;
			}else {
				groupField = FtsFieldConst.FIELD_SITENAME;
				database = Const.HYBASE_NI_INDEX;
			}

			log.error("dataBuilder:" + dataBuilder.asTRSL());

			GroupResult groupResult = hybase8SearchService.categoryQuery(builder.isServer(), dataBuilder.asTRSL(), sim,
					irSimflag,irSimflagAll, groupField, 10, "special",database);
//			commonChartService.getPieColumnData(builder,sim,irSimflag,irSimflagAll,source,"",groupField,"special");

			return groupResult;
		} catch (Exception e) {
			throw new OperationException("查询出错" + e);
		}
	}

	@Override
	public List<MBlogAnalyzeEntity> mBlogTop5(QueryBuilder builder, Top5Tab sort, boolean sim, boolean irSimflag,boolean irSimflagAll)
			throws TRSException {
		try {
			builder.orderBy(sort.getField(), true);
			// top5
			if ("NEWEST".equals(sort.toString())){
				builder.page(0, 100);
			}else {
				builder.page(0, 5);
			}
			String groupName = builder.getGroupName();
			if (StringUtil.isNotEmpty(groupName) && !"ALL".equals(groupName) && !groupName.contains("微博")){
				return null;
			}
//			String[] groupNames = StringUtil.isNotEmpty(groupName)?groupName.split(";"):null;
//			if (StringUtil.isNotEmpty(groupName)) {
//				builder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
//						.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
//			}
			//全部只查原发微博
			builder.filterByTRSL(Const.PRIMARY_WEIBO);
			final String pageId = GUIDGenerator.generate(SpecialChartAnalyzeService.class);
			String trslk = pageId + "trslk";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			long start = new Date().getTime();
			log.info("微博top5前100条开始查询hybase："+new Date().getTime());
			InfoListResult infoListResult = commonListService.queryPageList(builder,sim,irSimflag,irSimflagAll,Const.GROUPNAME_WEIBO,"special",UserUtils.getUser(),false);
			PagedList<FtsDocumentCommonVO> pagecontent = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
			List<FtsDocumentCommonVO> docunmentPagedList = pagecontent.getPageItems();
			List<MBlogAnalyzeEntity> pagedList = new ArrayList<>();
//			List<MBlogAnalyzeEntity> pagedList = hybase8SearchService.ftsQuery(builder, MBlogAnalyzeEntity.class, sim,
//					irSimflag,irSimflagAll,"special");
			long end = new Date().getTime();
			log.info("微博top5前100条结束查询hybase："+new Date().getTime());
			log.info("微博top5前100条查询hybase累计时间："+(end - start));
			for (FtsDocumentCommonVO ftsDocumentCommonVO : docunmentPagedList) {
				MBlogAnalyzeEntity mBlogAnalyzeEntity = new MBlogAnalyzeEntity();
				mBlogAnalyzeEntity.setContent(ftsDocumentCommonVO.getContent());
				mBlogAnalyzeEntity.setCount(ftsDocumentCommonVO.getCount());
				mBlogAnalyzeEntity.setTrslk(ftsDocumentCommonVO.getTrslk());
				mBlogAnalyzeEntity.setAuthor(ftsDocumentCommonVO.getAuthors());
				mBlogAnalyzeEntity.setCommentCount(Integer.valueOf((int)ftsDocumentCommonVO.getCommtCount()));
				mBlogAnalyzeEntity.setCreatedAt(DateUtil.format2String(ftsDocumentCommonVO.getCreatedAt(),DateUtil.yyyyMMddHHmmss_Line));
				mBlogAnalyzeEntity.setImageUrl(ftsDocumentCommonVO.getImgSrc());
//						mBlogAnalyzeEntity.setLocation(ftsDocumentCommonVO.getLocation());
				mBlogAnalyzeEntity.setMd5Tag(ftsDocumentCommonVO.getMd5Tag());
				mBlogAnalyzeEntity.setRttCount(Integer.valueOf((int)ftsDocumentCommonVO.getRttCount()));
//						mBlogAnalyzeEntity.setRurl(ftsDocumentCommonVO.getR);
				mBlogAnalyzeEntity.setScreenName(ftsDocumentCommonVO.getScreenName());
				mBlogAnalyzeEntity.setSid(ftsDocumentCommonVO.getSid());
//						mBlogAnalyzeEntity.setTagTxt(ftsDocumentCommonVO.getT);
				mBlogAnalyzeEntity.setUid(ftsDocumentCommonVO.getUid());
				mBlogAnalyzeEntity.setUrl(ftsDocumentCommonVO.getUrlName());
				mBlogAnalyzeEntity.setVia(ftsDocumentCommonVO.getVia());
				mBlogAnalyzeEntity.setId(ftsDocumentCommonVO.getId());
//						mBlogAnalyzeEntity.setRetFrom(ftsDocumentCommonVO.getR);
				pagedList.add(mBlogAnalyzeEntity);

			}
			if ("NEWEST".equals(sort.toString())){//按 最新 排序时  需要手动排重
				//MD5代码排重
				List<MBlogAnalyzeEntity> returnDataList = new ArrayList<>();
				LinkedHashMap<String, MBlogAnalyzeEntity> distinctMap = Maps.newLinkedHashMap();//为保证顺序 采用linkedhashmap
				for (MBlogAnalyzeEntity mBlogAnalyzeEntity : pagedList) {//若key值已存在 则不放里面  保证存入的是不同的MD5值对应的最新的一条数据
					if (!distinctMap.containsKey(mBlogAnalyzeEntity.getMd5Tag())){
						distinctMap.put(mBlogAnalyzeEntity.getMd5Tag(),mBlogAnalyzeEntity);
					}
					if (distinctMap.size() == 5){
						break;
					}
				}
				if (distinctMap.size() < 5){
					builder.page(1,100);
//					List<MBlogAnalyzeEntity> secondPagedList = hybase8SearchService.ftsQuery(builder, MBlogAnalyzeEntity.class, sim,
//							irSimflag,irSimflagAll,"special");
					InfoListResult secondInfoListResult = commonListService.queryPageList(builder,sim,irSimflag,irSimflagAll,Const.GROUPNAME_WEIBO,"special",UserUtils.getUser(),false);
					PagedList<FtsDocumentCommonVO> secondPageContent = (PagedList<FtsDocumentCommonVO>) secondInfoListResult.getContent();
					List<FtsDocumentCommonVO> secondPagedList = secondPageContent.getPageItems();
					for (FtsDocumentCommonVO ftsDocumentCommonVO : secondPagedList) {
						if (!distinctMap.containsKey(ftsDocumentCommonVO.getMd5Tag())){
							MBlogAnalyzeEntity mBlogAnalyzeEntity = new MBlogAnalyzeEntity();
							mBlogAnalyzeEntity.setContent(ftsDocumentCommonVO.getContent());
							mBlogAnalyzeEntity.setCount(ftsDocumentCommonVO.getCount());
							mBlogAnalyzeEntity.setTrslk(ftsDocumentCommonVO.getTrslk());
							mBlogAnalyzeEntity.setAuthor(ftsDocumentCommonVO.getAuthors());
							mBlogAnalyzeEntity.setCommentCount(Integer.valueOf((int)ftsDocumentCommonVO.getCommtCount()));
							mBlogAnalyzeEntity.setCreatedAt(DateUtil.format2String(ftsDocumentCommonVO.getCreatedAt(),DateUtil.yyyyMMddHHmmss_Line));
							mBlogAnalyzeEntity.setImageUrl(ftsDocumentCommonVO.getImgSrc());
//						mBlogAnalyzeEntity.setLocation(ftsDocumentCommonVO.getLocation());
							mBlogAnalyzeEntity.setMd5Tag(ftsDocumentCommonVO.getMd5Tag());
							mBlogAnalyzeEntity.setRttCount(Integer.valueOf((int)ftsDocumentCommonVO.getRttCount()));
//						mBlogAnalyzeEntity.setRurl(ftsDocumentCommonVO.getR);
							mBlogAnalyzeEntity.setScreenName(ftsDocumentCommonVO.getScreenName());
							mBlogAnalyzeEntity.setSid(ftsDocumentCommonVO.getSid());
//						mBlogAnalyzeEntity.setTagTxt(ftsDocumentCommonVO.getT);
							mBlogAnalyzeEntity.setUid(ftsDocumentCommonVO.getUid());
							mBlogAnalyzeEntity.setUrl(ftsDocumentCommonVO.getUrlName());
							mBlogAnalyzeEntity.setVia(ftsDocumentCommonVO.getVia());
							mBlogAnalyzeEntity.setId(ftsDocumentCommonVO.getId());
//						mBlogAnalyzeEntity.setRetFrom(ftsDocumentCommonVO.getR);
							distinctMap.put(ftsDocumentCommonVO.getMd5Tag(),mBlogAnalyzeEntity);
						}
						if (distinctMap.size() == 5){
							break;
						}
					}
				}
				//为保证返回格式相同 作如下操作
				for (Map.Entry<String, MBlogAnalyzeEntity> entityEntry : distinctMap.entrySet()) {
					returnDataList.add(entityEntry.getValue());
				}
				pagedList = returnDataList;
			}

			long startCount = new Date().getTime();
			log.info("微博top5开始处理查询结果："+new Date().getTime());
			if (ObjectUtil.isNotEmpty(pagedList)){
				for (MBlogAnalyzeEntity mBlogAnalyzeEntity : pagedList) {
					QueryBuilder builderMd5 = new QueryBuilder();
					builderMd5.filterField(FtsFieldConst.FIELD_MD5TAG, mBlogAnalyzeEntity.getMd5Tag(), Operator.Equal);
//					long ftsCount = hybase8SearchService.ftsCount(builder, MBlogAnalyzeEntity.class, sim, irSimflag,irSimflagAll,"special");
					long ftsCount = commonListService.ftsCount(builder,sim, irSimflag,irSimflagAll,"special",groupName);
					mBlogAnalyzeEntity.setCount(ftsCount);

					String content = mBlogAnalyzeEntity.getContent() != null
							? mBlogAnalyzeEntity.getContent().replaceFirst("　　", "").replaceAll("\\?{4,25}", "") : null;
					// 此处对文章具体内容按第一个命中的词的前后截取共150个字
					Matcher matcher = Pattern.compile("font").matcher(content);
					if (content.length() > 150 && matcher.find()) {
						int targetIndex = matcher.start();
						if (75 > targetIndex) {
							String str1 = content.substring(0, 150);
							content = str1 + "...";
						} else if (75 < targetIndex && content.length() - targetIndex >= 75) {
							String str1 = content.substring(targetIndex - 75, targetIndex);
							String str2 = content.substring(targetIndex, targetIndex + 75);
							content = "..." + str1 + str2 + "...";
						} else if (75 < targetIndex && content.length() - targetIndex < 75) {
							String str1 = content.substring(content.length() - 150, content.length());
							content = "..." + str1;
						}
					}

					mBlogAnalyzeEntity.setContent(content);
					mBlogAnalyzeEntity.setTrslk(trslk);
				}
			}
			long endCount = new Date().getTime();
			log.info("微博top5结束处理查询结果："+new Date().getTime());
			log.info("微博top5处理查询结果累计时间，其中包括查询每条的count数据："+(endCount - startCount));
			return pagedList;
		} catch (Exception e) {
			throw new OperationException("微博top5检索失败:" + e,e);
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

		// 来源统计图和创建专题选的数据来源对应
		String source = specialProject.getSource();
		if(!"ALL".equals(source)){
			Map<String,Object> map = new HashMap<String, Object>();
			String[] groupNames = source.split(";");
			for (String groupName : groupNames) {
				switch(groupName){
					case "国内新闻":
						List<ClassInfo> classInfoNews = ClassInfoUtil
								.timeChange(getWebCountByGroupName("国内新闻", specialProject, area, flag), date, flag);
						map.put("news", classInfoNews);
						break;
					case "微博":
						List<ClassInfo> classInfoWeiBo = ClassInfoUtil
								.timeChange(getWebCountByGroupName("微博", specialProject, area, flag), date, flag);
						map.put("status", classInfoWeiBo);
						break;
					case "微信":
						List<ClassInfo> classInfoWeiXin = ClassInfoUtil
								.timeChange(getWebCountByGroupName("微信", specialProject, area, flag), date, flag);
						map.put("wechat", classInfoWeiXin);
						break;
					case "国内论坛":
						List<ClassInfo> classInfoLuntan = ClassInfoUtil
								.timeChange(getWebCountByGroupName("国内论坛", specialProject, area, flag), date, flag);
						map.put("luntan", classInfoLuntan);
						break;
					case "国内博客":
						List<ClassInfo> classInfoBlog = ClassInfoUtil
								.timeChange(getWebCountByGroupName("国内博客", specialProject, area, flag), date, flag);
						map.put("blog", classInfoBlog);
						break;
					case "国内新闻_电子报":
						List<ClassInfo> classInfoApps = ClassInfoUtil
								.timeChange(getWebCountByGroupName("国内新闻_电子报", specialProject, area, flag), date, flag);
						map.put("read", classInfoApps);
						break;
					case "国内新闻_手机客户端":
						List<ClassInfo> classInfoKeHuDuan = ClassInfoUtil
								.timeChange(getWebCountByGroupName("国内新闻_手机客户端", specialProject, area, flag), date, flag);
						map.put("app", classInfoKeHuDuan);
						break;
					case "Twitter":
						List<ClassInfo> classInfoTwitter = ClassInfoUtil
								.timeChange(getWebCountByGroupName("Twitter", specialProject, area, flag), date, flag);
						map.put("twitter", classInfoTwitter);
						break;
					case "FaceBook":
						List<ClassInfo> classInfoFaceBook = ClassInfoUtil
								.timeChange(getWebCountByGroupName("FaceBook", specialProject, area, flag), date, flag);
						map.put("facebook", classInfoFaceBook);
						break;
					case "境外媒体":
						List<ClassInfo> classInfoForeign = ClassInfoUtil
								.timeChange(getWebCountByGroupName("国外新闻", specialProject, area, flag), date, flag);
						map.put("foreign", classInfoForeign);
						break;
				}
			}
			return map;
		}else{
			List<ClassInfo> classInfoNews = ClassInfoUtil
					.timeChange(getWebCountByGroupName("国内新闻", specialProject, area, flag), date, flag);

			List<ClassInfo> classInfoWeiBo = ClassInfoUtil
					.timeChange(getWebCountByGroupName("微博", specialProject, area, flag), date, flag);

			List<ClassInfo> classInfoWeiXin = ClassInfoUtil
					.timeChange(getWebCountByGroupName("微信", specialProject, area, flag), date, flag);

			List<ClassInfo> classInfoLuntan = ClassInfoUtil
					.timeChange(getWebCountByGroupName("国内论坛", specialProject, area, flag), date, flag);

			List<ClassInfo> classInfoBlog = ClassInfoUtil
					.timeChange(getWebCountByGroupName("国内博客", specialProject, area, flag), date, flag);

			List<ClassInfo> classInfoApps = ClassInfoUtil
					.timeChange(getWebCountByGroupName("国内新闻_电子报", specialProject, area, flag), date, flag);

			List<ClassInfo> classInfoKeHuDuan = ClassInfoUtil
					.timeChange(getWebCountByGroupName("国内新闻_手机客户端", specialProject, area, flag), date, flag);
			List<ClassInfo> classInfoTwitter = ClassInfoUtil
					.timeChange(getWebCountByGroupName("Twitter", specialProject, area, flag), date, flag);

			List<ClassInfo> classInfoFaceBook = ClassInfoUtil
					.timeChange(getWebCountByGroupName("FaceBook", specialProject, area, flag), date, flag);

			List<ClassInfo> classInfoForeign = ClassInfoUtil
					.timeChange(getWebCountByGroupName("国外新闻", specialProject, area, flag), date, flag);

			return MapUtil
					.putValue(
							new String[] { "news", "status", "wechat", "luntan", "blog", "read", "app", "twitter",
									"facebook", "foreign" },
							classInfoNews, classInfoWeiBo, classInfoWeiXin, classInfoLuntan, classInfoBlog, classInfoApps,
							classInfoKeHuDuan, classInfoTwitter, classInfoFaceBook, classInfoForeign);
		}
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
	private List<ClassInfo> getWebCountByGroupName(String groupName, SpecialProject specialProject, String area,
												   boolean flag) {
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
			//hybase有存在来源字段值为空的情况  为与信息列表表格共计数据对应  需过滤掉来源字段值为空的数据
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME,"国内微信",Operator.Equal);
			builder.filterField(FtsFieldConst.FIELD_URLTIME,
					new String[] { specialProject.getStart(), specialProject.getEnd() }, Operator.Between);
			builder.setDatabase(Const.WECHAT);
		} else if ("微博".equals(groupName)) {
			builder = specialProject.toNoPagedAndTimeBuilderWeiBo();
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME,"微博",Operator.Equal);//防止微博数据出现微信数据来源值为空情况导致同条件下同来源数据量不一致
			builder.filterField(FtsFieldConst.FIELD_CREATED_AT,
					new String[] { specialProject.getStart(), specialProject.getEnd() }, Operator.Between);
			builder.setDatabase(Const.WEIBO);
		} else if ("Twitter".equals(groupName) || "FaceBook".equals(groupName)) {
			builder = specialProject.toNoPagedBuilder();
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupName, Operator.Equal);
			builder.setDatabase(Const.HYBASE_OVERSEAS);
		} else {
			builder = specialProject.toNoPagedBuilder();
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupName, Operator.Equal);
			builder.setDatabase(Const.HYBASE_NI_INDEX);
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
		Iterator<Map.Entry<String, Long>> iter = null;
		List<Map<String, Object>> list = null;
		// 按小时统计或者按天统计
		try {
			log.error("groupName:" + groupName);
			log.error(builder.asTRSL());
			if (flag) {
				if ("微博".equals(groupName) || "微信".equals(groupName)) {
					ChartResultField chartResultField = new ChartResultField("thekey","thevalue");
					list = (List<Map<String, Object>>) commonChartService.getPieColumnData(builder,sim, irSimflag,irSimflagAll,groupName,"",FtsFieldConst.FIELD_CREATED_HOUR,"special",chartResultField);
//					iter = hybase8SearchService.groupCount(builder, FtsFieldConst.FIELD_CREATED_HOUR, sim, irSimflag,irSimflagAll,"special");
				} else {
					ChartResultField chartResultField = new ChartResultField("thekey","thevalue");
					list = (List<Map<String, Object>>) commonChartService.getPieColumnData(builder,sim, irSimflag,irSimflagAll,groupName,"",FtsFieldConst.FIELD_URLTIME_HOUR,"special",chartResultField);
//					iter = hybase8SearchService.groupCount(builder, FtsFieldConst.FIELD_URLTIME_HOUR, sim, irSimflag,irSimflagAll,"special");
				}
			} else {
//				if ("微博".equals(groupName)) {
//					iter = hybase8SearchService.groupCount(builder, FtsFieldConst.FIELD_CREATED_AT, sim, irSimflag,irSimflagAll,"special");
//				} else {
//					iter = hybase8SearchService.groupCount(builder, FtsFieldConst.FIELD_URLTIME, sim, irSimflag,irSimflagAll,"special");
				ChartResultField chartResultField = new ChartResultField("thekey","thevalue");
				list = (List<Map<String, Object>>) commonChartService.getPieColumnData(builder,sim, irSimflag,irSimflagAll,groupName,"",FtsFieldConst.FIELD_URLTIME,"special",chartResultField);

//				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		if (null != list && list.size() > 0){
			for (int i = 0; i < list.size(); i++) {
				classInfo.add(new ClassInfo(list.get(i).get("thekey").toString(),Long.valueOf(String.valueOf(list.get(i).get("thevalue")))));
			}

		}
//		if (null != iter) {
//			while (iter.hasNext()) {
//				Map.Entry<String, Long> entry = iter.next();
//				classInfo.add(new ClassInfo(entry.getKey(), entry.getValue().intValue()));
//			}
//		}
		return classInfo;
	}

	@Override
	public List<Map<String, Object>> getAreaCount(QueryBuilder searchBuilder, String[] timeArray,boolean isSimilar,boolean irSimflag,boolean irSimflagAll)
			throws TRSException {
		ObjectUtil.assertNull(searchBuilder.asTRSL(), "地域分布检索表达式");
		List<Map<String, Object>> resultMap = new ArrayList<>();
		if (timeArray != null) {
			searchBuilder.filterField("IR_URLTIME", timeArray, Operator.Between);
		}
		String groupName = searchBuilder.getGroupName();
		String[] groupNames = StringUtil.isNotEmpty(groupName)?groupName.split(";"):null;
		String[] database = TrslUtil.chooseDatabases(groupNames);
		if (StringUtil.isNotEmpty(groupName)) {
			searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
					.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
		}
		ChartResultField chartResultField = new ChartResultField("area_name","area_count");
		searchBuilder.setPageSize(Integer.MAX_VALUE);
		resultMap = (List<Map<String, Object>>) commonChartService.getMapColumnData(searchBuilder,isSimilar,irSimflag,irSimflagAll,groupName, FtsFieldConst.FIELD_CATALOG_AREA,"special",chartResultField);
//		try {
//			Map<String, List<String>> areaMap = districtInfoService.allAreas();
//			GroupResult categoryInfos = hybase8SearchService.categoryQuery(searchBuilder.isServer(),
//					searchBuilder.asTRSL(), isSimilar, irSimflag,irSimflagAll, FtsFieldConst.FIELD_CATALOG_AREA, Integer.MAX_VALUE,"special", database);
//			for (Map.Entry<String, List<String>> entry : areaMap.entrySet()) {
//				Map<String, Object> reMap = new HashMap<String, Object>();
//				int num = 0;
//				// 查询结果之间相互对比 所以把城市放开也不耽误查询速度
//
//				for (GroupInfo classEntry : categoryInfos) {
//					String area = classEntry.getFieldValue();
//					if(area.contains(";")){
//						continue;
//					}
//					//因为该查询字段形式类似数组，文章命中访问的是这个字段中的每个值的个数，例如一条数据的这个字段的值为：中国\北京市\朝阳区;中国\北京市\海淀区
//					//按注释方法算 - 这样同一条数据北京市被计算2次，因为朝阳与海淀都是北京下属地域，2019-12该字段修改为在上面基础上增加当前条所属市，为：中国\北京市\朝阳区;中国\北京市\海淀区;中国\北京市
//					//如果继续计算下属市则北京被计算3次，所以只计算到省，则需要数据库中改字段的值定义不变，为：中国\北京市
//					String[] areaArr = area.split("\\\\");
//					if (areaArr.length == 2) {
//						if (areaArr[1].contains(entry.getKey())) {
//							num += classEntry.getCount();
//						}
//					}
//				}
//
//				/*
//
//				for (GroupInfo classEntry : categoryInfos) {
//					if (classEntry.getFieldValue().contains(entry.getKey())) {
//						num += classEntry.getCount();
//					}
//				}
//				List<Map<String, String>> citys = new ArrayList<Map<String, String>>();
//				for (String city : entry.getValue()) {
//					Map<String, String> cityMap = new HashMap<String, String>();
//					int num2 = 0;
//					int numJiLin = 0;
//					for (GroupInfo classEntry : categoryInfos) {
//						// 因为吉林省市同名,单独拿出,防止按区域名称分类统计错误
//						if (classEntry.getFieldValue().contains(city)
//								&& !classEntry.getFieldValue().contains("吉林省\\吉林市")) {
//							num2 += classEntry.getCount();
//						} else if (classEntry.getFieldValue().contains("吉林省\\吉林市")) {
//							numJiLin += classEntry.getCount();
//						}
//					}
//					// 把.之前的去掉
//					String[] citySplit = city.split(".");
//					if (citySplit.length > 1) {
//						city = citySplit[citySplit.length - 1];
//					}
//					cityMap.put("area_name", city);
//					cityMap.put("area_count", String.valueOf(num2));
//					if ("吉林".equals(city)) {
//						cityMap.put("area_count", String.valueOf(numJiLin));
//					}
//					citys.add(cityMap);
//				}
//
//				*/
//				reMap.put("area_name", entry.getKey());
//				reMap.put("area_count", num);
//				//reMap.put("citys", citys);
//				resultMap.add(reMap);
//			}
//
//		} catch (Exception e) {
//			throw new OperationException("地域分布查询失败" + e);
//		}
		return resultMap;
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
						true, false,false, FtsFieldConst.FIELD_CATALOG_AREA, 1000,"special", Const.HYBASE_NI_INDEX);
				log.info(searchBuilder.asTRSL());
				// all.addAll(categoryInfos);
			}
			if (Const.MEDIA_TYPE_WEIXIN.contains(groupName) || "ALL".equals(groupName)
					|| StringUtil.isEmpty(groupName)) {
				QueryBuilder weiboBuilder = new QueryBuilder();
				weiboBuilder.filterField("IR_URLTIME", timeArray, Operator.Between);
				log.info(weiboBuilder.asTRSL());
				categoryInfos = hybase8SearchService.categoryQuery(searchBuilder.isServer(), weiboBuilder.asTRSL(),
						false, false, false,FtsFieldConst.FIELD_CATALOG_AREA, 1000,"special", Const.WECHAT);
				// all.addAll(categoryInfosWeiBo);
			}
			if (Const.MEDIA_TYPE_WEIBO.contains(groupName) || "ALL".equals(groupName)
					|| StringUtil.isEmpty(groupName)) {
				QueryBuilder weiboBuilder = new QueryBuilder();
				weiboBuilder.filterField("IR_URLTIME", timeArray, Operator.Between);
				weiboBuilder.setDatabase(Const.WEIBO);
				log.info(weiboBuilder.asTRSL());
				categoryInfos = hybase8SearchService.categoryQuery(searchBuilder.isServer(), weiboBuilder.asTRSL(),
						false, false,false, FtsFieldConst.FIELD_CATALOG_AREA, 1000,"special", Const.WEIBO);
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
		String[] timeArray = DateUtil.formatTimeRange(timerange);
		String groupField = null;
		String groupFieldWeibo = null;
		String groupFieldWeixin = null;
		boolean flag = true;
		if(StringUtils.equals(showType,"day") && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3){
			groupField = FtsFieldConst.FIELD_URLTIME;
			groupFieldWeixin = FtsFieldConst.FIELD_URLTIME;
			groupFieldWeibo = FtsFieldConst.FIELD_CREATED_AT;
		}else{
			flag = false;
			groupField = FtsFieldConst.FIELD_URLTIME_HOUR;
			groupFieldWeixin = FtsFieldConst.FIELD_CREATED_HOUR;
			groupFieldWeibo = FtsFieldConst.FIELD_CREATED_HOUR;
		}
		boolean sim = specialProject.isSimilar();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		String[] groupName = specialProject.getSource().split(";");
		String dateType = "";
		String days = "";
		String startTime = "";
		String endTime = "";
		if (timerange.contains("d")) {
			dateType = "DAY";
			days = timerange.substring(0, timerange.length() - 1);
			if ("0".equals(days)) {
				dateType = "TODAY";
				flag = false;
			}
		} else if (timerange.contains("h")) {
			flag = false;
			dateType = "HOUR";
		} else {
			dateType = "DIY";
		}
		startTime = timeArray[0];
		endTime = timeArray[1];
		Map<String, Object> result = new HashMap<>();
		if (StringUtil.isEmpty(dateType)) {
			dateType = "DIY";
		}
		List<String[]> list_time = new ArrayList<>();
		if (StringUtils.equals(showType, "day") && StringUtils.equals(dateType, "DAY")) {
			list_time.add(timeArray);
		} else if(StringUtils.equals(showType, "day") && StringUtils.equals(dateType, "DIY") && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3){
			list_time.add(timeArray);
		}else {
			if (StringUtils.equals(timerange, "0d")) {
				list_time.add(timeArray);
			}  else {
				timeArray = DateUtil.getTimeToSevenDay(timeArray[0],timeArray[1]);
				list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]); //每天的起止时间  yyyyMMddHHmmss格式
			}
		}
		try {
			List<Map<String, Object>> list = new ArrayList<>();
			Boolean listEmpty = false;
			for (String[] arrays : list_time) {//循环查询，如果是天，只查一次，如果是小时查多次
				List<String> dateList = new ArrayList<String>();
				List<String> date = new ArrayList<String>();
				List<String> dateResult = new ArrayList<String>();

				if ("DAY".equals(String.valueOf(dateType)) && StringUtils.equals(showType, "day")) {
					if (StringUtil.isEmpty(days)) {
						throw new OperationException("按天查询天数不能为空!");
					}
					dateList = DateUtil.getDataStinglist2(Integer.parseInt(days) - 1);
				} else if ("DIY".equals(String.valueOf(dateType)) && StringUtils.equals(showType, "day") && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3) {
					if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
						throw new OperationException("自定义查询起止时间不能为空!");
					}
					// dateList = DateUtil.getBetweenDateString(startTime, endTime,
					// "yyyy-MM-dd");
					dateList = DateUtil.getBetweenDateString(startTime, endTime, "yyyyMMddHHmmss");
					int start = Integer.parseInt(startTime.substring(11, 13));
					int end = Integer.parseInt(endTime.substring(11, 13));
					if (end < start) {
						dateList.add(endTime.substring(0, 10));
					}
				}else{
					/*if ("TODAY".equals(String.valueOf(dateType))) {
						dateList = DateUtil.getCurrentDateHours();
						dateList.add(DateUtil.formatCurrentTime("yyyyMMddHHmmss"));
					}
					if ("HOUR".equals(String.valueOf(dateType))) {
						dateList = DateUtil.get24Hours();
						//同日常监测 最后一个小时不看
						dateList.remove(dateList.size() - 1);
					}*/
					dateList = DateUtil.getStartToEndOfHour(arrays[0], arrays[1]);
				}


				Iterator<String> i = dateList.iterator();
				String s = "";
				//if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
				if ("day".equals(showType) && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3) {
					while (i.hasNext()) {
						s = String.valueOf(i.next());
						s = s.replaceAll("[-/.: ]", "").trim();
						s = s.substring(0, 8);
						date.add(s);
						Date stringToDate = DateUtil.stringToDate(s, "yyyyMMdd");
						s = DateUtil.date2String(stringToDate, "yyyy-MM-dd");
						dateResult.add(s);
					}
				} else{
					dateResult.addAll(dateList);//一天之内的所有小时  ，yyyy/MM/dd HH:00格式
					date = DateUtil.getHourOfHH(arrays[0], arrays[1]);//一天之内的所有小时  ，HH格式 //下面的代码中主要为了取开始与结束时间
				}
				/*if ("TODAY".equals(String.valueOf(dateType)) || "HOUR".equals(String.valueOf(dateType))) {
					while (i.hasNext()) {
						s = String.valueOf(i.next());
						s = s.replaceAll("[-/.: ]", "").trim();
						s = s.substring(0, 10);
						date.add(s);

					}
					if ("TODAY".equals(String.valueOf(dateType))) {
						String formatCurrentTime = DateUtil.formatCurrentTime("HH");
						for (int j = 0; j <= Integer.valueOf(formatCurrentTime); j++) {
							if (j < 10) {
								dateResult.add("0" + j);
							} else {
								dateResult.add(j + "");
							}
						}
					} else if ("HOUR".equals(String.valueOf(dateType))) {
//				for (int j = 0; j < 24; j++) {
//					if (j < 10) {
//						dateResult.add("0" + j);
//					} else {
//						dateResult.add(j + "");
//					}
//				}
						//24h的结果类似201809101525 取整点
						for (String dateSubstring : date) {
							dateResult.add(dateSubstring.substring(8, 10));
						}
					}

				}*/
				List<String> date_all = (List<String> )result.get("date");
				if(date_all == null){
					date_all = new ArrayList<>();
				}
				date_all.addAll(dateResult);
				result.put("date", date_all);
				int size = date.size();
				String start = null;
				String end = null;
				//if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
				if ("day".equals(showType) && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3) {
					if ("DAY".equals(String.valueOf(dateType))) {
						start = date.get(0) + new SimpleDateFormat("HHmmss").format(new Date());
					} else {
						// 保证DIY的时间正确
						start = date.get(0) + DateUtil.DAY_START;
					}
					end = date.get(date.size() - 1) + DateUtil.DAY_END;

				}else{
					start = arrays[0];
					end = arrays[1];
				}
				/*if ("TODAY".equals(String.valueOf(dateType)) || "HOUR".equals(String.valueOf(dateType))) {
					start = date.get(0) + DateUtil.HOUR_START;
					end = date.get(date.size() - 1) + DateUtil.HOUR_END;
				}*/

				QueryBuilder chuantong = specialProject.toNoPagedAndTimeBuilder();
				chuantong.filterByTRSL(esSql);
				String groupNameTrl = "";
				if(groupName.length>0 && StringUtil.isNotEmpty(groupName[0])){
					groupNameTrl += "AND ";
					for(String g:groupName){
						groupNameTrl+= FtsFieldConst.FIELD_GROUPNAME+":"+g+" OR ";
					}
					groupNameTrl = groupNameTrl.substring(0,groupNameTrl.length()-4);
				}
				//String chuantongTrsl = chuantong.asTRSL()+groupNameTrl;
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

				if (ChartType.NETTENDENCY.getType().equals(type)) {
					int num = 0;
					// 2.微博用户
					String sqlStatus = FtsFieldConst.FIELD_CREATED_AT + ":[" + start + " TO " + end + "]  AND " + weiboTrsl;
					log.error("微博：" + sqlStatus);
					GroupResult categoryQueryStatus = hybase8SearchService.categoryQuery(specialProject.isServer(),
							sqlStatus, sim, irSimflag, irSimflagAll, groupFieldWeibo, size,"special", Const.WEIBO);
					Map<String, Object> mapStatus = new HashMap<>();
					mapStatus.put("groupName", "微博用户");
					//mapStatus.put("list", MapUtil.sortAndChangeList(categoryQueryStatus, dateResult, "yyyy-MM-dd", flag));
					//list.add(mapStatus);
					//flag是小时
					if (flag) {
						mapStatus.put("list", MapUtil.sortAndChangeList(categoryQueryStatus, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapStatus.put("list", MapUtil.sortAndChangeList(categoryQueryStatus, date, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapStatus);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapStatus.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
					num++;

					// 3.博客用户
					String sqlBlog = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] " + "AND ("
							+ FtsFieldConst.FIELD_GROUPNAME + ":" + ESGroupName.BLOG_IN.getName() + ") AND "
							+ chuantongTrsl;
					log.error("博客：" + sqlBlog);
					GroupResult categoryQueryBlog = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlBlog,
							sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
					Map<String, Object> mapBlog = new HashMap<>();
					mapBlog.put("groupName", "博客用户");
					//mapBlog.put("list", MapUtil.sortAndChangeList(categoryQueryBlog, dateResult, "yyyy-MM-dd", flag));
					//list.add(mapBlog);
					if (flag) {
						mapBlog.put("list", MapUtil.sortAndChangeList(categoryQueryBlog, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapBlog.put("list", MapUtil.sortAndChangeList(categoryQueryBlog, date, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapBlog);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapBlog.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
					num++;

					// 4.微信用户
					String sqlWeChat = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]  AND " + weixinTrsl;
					GroupResult categoryQueryWeChat = hybase8SearchService.categoryQuery(specialProject.isServer(),
							sqlWeChat, sim, irSimflag, irSimflagAll, groupFieldWeixin, size,"special", Const.WECHAT);
					Map<String, Object> mapWeChat = new HashMap<>();
					mapWeChat.put("groupName", "微信用户");
					//mapWeChat.put("list", MapUtil.sortAndChangeList(categoryQueryWeChat, dateResult, "yyyy-MM-dd", flag));
					if (flag) {
						mapWeChat.put("list", MapUtil.sortAndChangeList(categoryQueryWeChat, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapWeChat.put("list", MapUtil.sortAndChangeList(categoryQueryWeChat, date, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapWeChat);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapWeChat.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
					num++;

					// 5.论坛用户
					String sqlLunTan = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] " + " AND (("
							+ FtsFieldConst.FIELD_GROUPNAME + ":" + ESGroupName.FORUM_IN.getName() + ") OR ("
							+ FtsFieldConst.FIELD_SITENAME + ":百度贴吧 )) AND " + chuantongTrsl;
					GroupResult categoryQueryLunTan = hybase8SearchService.categoryQuery(specialProject.isServer(),
							sqlLunTan, sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
					Map<String, Object> mapLunTan = new HashMap<>();
					mapLunTan.put("groupName", "论坛用户");
					//mapLunTan.put("list", MapUtil.sortAndChangeList(categoryQueryLunTan, dateResult, "yyyy-MM-dd", flag));
					if (flag) {
						mapLunTan.put("list", MapUtil.sortAndChangeList(categoryQueryLunTan, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapLunTan.put("list", MapUtil.sortAndChangeList(categoryQueryLunTan, date, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapLunTan);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapLunTan.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
					num++;

					// 6.TWitter
					String sqlTWitter = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND ("
							+ FtsFieldConst.FIELD_GROUPNAME + ":(TWitter)) AND " + chuantongTrsl;
					log.info("TWitter" + sqlTWitter);
					GroupResult categoryQueryTWitter = hybase8SearchService.categoryQuery(specialProject.isServer(),
							sqlTWitter, sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_OVERSEAS);
					Map<String, Object> mapTWitter = new HashMap<>();
					mapTWitter.put("groupName", "Twitter");
					//mapTWitter.put("list", MapUtil.sortAndChangeList(categoryQueryTWitter, dateResult, "yyyy-MM-dd", flag));
					if (flag) {
						mapTWitter.put("list", MapUtil.sortAndChangeList(categoryQueryTWitter, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapTWitter.put("list", MapUtil.sortAndChangeList(categoryQueryTWitter, date, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapTWitter);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapTWitter.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
					num++;

					// 7.FaceBook
					String sqlFaceBook = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND ("
							+ FtsFieldConst.FIELD_GROUPNAME + ":(FaceBook)) AND " + chuantongTrsl;
					log.info("FaceBook" + sqlTWitter);
					GroupResult categoryQueryFaceBook = hybase8SearchService.categoryQuery(specialProject.isServer(),
							sqlFaceBook, sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_OVERSEAS);
					Map<String, Object> mapFaceBook = new HashMap<>();
					mapFaceBook.put("groupName", "FaceBook");
					//mapFaceBook.put("list", MapUtil.sortAndChangeList(categoryQueryFaceBook, dateResult, "yyyy-MM-dd", flag));
					if (flag) {
						mapFaceBook.put("list",
								MapUtil.sortAndChangeList(categoryQueryFaceBook, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapFaceBook.put("list",
								MapUtil.sortAndChangeList(categoryQueryFaceBook, date, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapFaceBook);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapFaceBook.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
				} else if (ChartType.METATENDENCY.getType().equals(type)) {
					int num = 0;
					// 1.新闻
					String sqlNews = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND ("
							+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻) NOT " + FtsFieldConst.FIELD_SITENAME + ":百度贴吧) AND "
							+ chuantongTrsl;
					GroupResult categoryQueryNews = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlNews,
							sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
					Map<String, Object> mapNews = new HashMap<>();
					mapNews.put("groupName", "新闻");
					//mapNews.put("list", MapUtil.sortAndChangeList(categoryQueryNews, dateResult, "yyyy-MM-dd", flag));
					if (flag) {
						mapNews.put("list", MapUtil.sortAndChangeList(categoryQueryNews, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapNews.put("list", MapUtil.sortAndChangeList(categoryQueryNews, date, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapNews);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapNews.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
					num++;

					// 2.客户端
					String sqlApp = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND ("
							+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻_手机客户端) NOT " + FtsFieldConst.FIELD_SITENAME
							+ ":百度贴吧 ) AND " + chuantongTrsl;
					GroupResult categoryQueryApps = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlApp,
							sim, irSimflag, irSimflagAll, groupField, size,"special",Const.HYBASE_NI_INDEX);
					Map<String, Object> mapApps = new HashMap<>();
					mapApps.put("groupName", "客户端");
					//mapApps.put("list", MapUtil.sortAndChangeList(categoryQueryApps, dateResult, "yyyy-MM-dd", flag));
					if (flag) {
						mapApps.put("list", MapUtil.sortAndChangeList(categoryQueryApps, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapApps.put("list", MapUtil.sortAndChangeList(categoryQueryApps, date, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapApps);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapApps.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
					num++;

					// 3.电子报
					String sqlEpaper = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND ("
							+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻_电子报) NOT " + FtsFieldConst.FIELD_SITENAME
							+ ":百度贴吧 ) AND " + chuantongTrsl;
					GroupResult categoryQueryEpapers = hybase8SearchService.categoryQuery(specialProject.isServer(),
							sqlEpaper, sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
					Map<String, Object> mapEpapers = new HashMap<>();
					mapEpapers.put("groupName", "电子报");
					//mapEpapers.put("list", MapUtil.sortAndChangeList(categoryQueryEpapers, dateResult, "yyyy-MM-dd", flag));
					if (flag) {
						mapEpapers.put("list", MapUtil.sortAndChangeList(categoryQueryEpapers, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapEpapers.put("list", MapUtil.sortAndChangeList(categoryQueryEpapers, date, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapEpapers);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapEpapers.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
					num++;

					// 4.境外媒体
					String sqlForeign = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND ("
							+ FtsFieldConst.FIELD_GROUPNAME + ":(国外新闻  OR 港澳台新闻)) AND " + chuantongTrsl;
					GroupResult categoryQueryForeigns = hybase8SearchService.categoryQuery(specialProject.isServer(),
							sqlForeign, sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
					Map<String, Object> mapForeigns = new HashMap<>();
					//页面展示 国外新闻 变为 境外网站  2019-12-10
					//mapForeigns.put("groupName", "境外媒体");
					mapForeigns.put("groupName", "境外网站");
					//mapForeigns.put("list", MapUtil.sortAndChangeList(categoryQueryForeigns, dateResult, "yyyy-MM-dd", flag));
					if (flag) {
						mapForeigns.put("list",
								MapUtil.sortAndChangeList(categoryQueryForeigns, dateResult, "yyyy-MM-dd", flag));
						//按天 查一次
					} else {
						mapForeigns.put("list",
								MapUtil.sortAndChangeList(categoryQueryForeigns, dateResult, "yyyy-MM-dd", flag));
						//按小时  查多次
					}
					if (!listEmpty) {
						list.add(mapForeigns);
					} else {
						Map<String, Object> map_all = list.get(num);
						List<Long> list_all = (List<Long>) map_all.get("list");
						List<Long> list_day = (List<Long>) mapForeigns.get("list");
						list_all.addAll(list_day);
						map_all.put("list", list_all);
						list.set(num, map_all);
					}
				}
				if(!listEmpty){
					listEmpty = true;
				}
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
		String[] timeArray = DateUtil.formatTimeRange(timerange);
		String groupField = null;
		String groupFieldWeibo = null;
		String groupFieldWeixin = null;
		boolean flag = true;
		if(StringUtils.equals(showType,"day") && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3){
			groupField = FtsFieldConst.FIELD_URLTIME;
			groupFieldWeixin = FtsFieldConst.FIELD_URLTIME;
			groupFieldWeibo = FtsFieldConst.FIELD_CREATED_AT;
		}else{
			flag = false;
			groupField = FtsFieldConst.FIELD_URLTIME_HOUR;
			groupFieldWeixin = FtsFieldConst.FIELD_CREATED_HOUR;
			groupFieldWeibo = FtsFieldConst.FIELD_CREATED_HOUR;
		}
		boolean sim = specialProject.isSimilar();
		// url排重
		boolean irSimflag = specialProject.isIrSimflag();
		//跨数据源排重
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		String dateType = "";
		String days = "";
		String startTime = "";
		String endTime = "";
		if (timerange.contains("d")) {
			dateType = "DAY";
			days = timerange.substring(0, timerange.length() - 1);
			if ("0".equals(days)) {
				dateType = "TODAY";
				flag = false;
			}
		} else if (timerange.contains("h")) {
			flag = false;
			dateType = "HOUR";
		} else {
			dateType = "DIY";
		}
		startTime = timeArray[0];
		endTime = timeArray[1];
		Map<String, Object> result = new HashMap<>();//最终结果
		if (StringUtil.isEmpty(dateType)) {
			dateType = "DIY";
		}
		List<String[]> list_time = new ArrayList<>();
		if (StringUtils.equals(showType, "day") && StringUtils.equals(dateType, "DAY")) {
			list_time.add(timeArray);
		} else if(StringUtils.equals(showType, "day") && StringUtils.equals(dateType, "DIY") && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3){
			list_time.add(timeArray);
		}else {
			if (StringUtils.equals(timerange, "0d")) {
				list_time.add(timeArray);
			}  else {
				timeArray = DateUtil.getTimeToSevenDay(timeArray[0],timeArray[1]);
				list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]); //每天的起止时间  yyyyMMddHHmmss格式
			}
		}
		try {
			List<Map<String, Object>> list = new ArrayList<>();
			Boolean listEmpty = false;
			for (String[] arrays : list_time) {//循环查询，如果是天，只查一次，如果是小时查多次
				List<String> dateList = new ArrayList<String>();
				List<String> date = new ArrayList<String>();
				List<String> dateResult = new ArrayList<String>();

				if ("DAY".equals(String.valueOf(dateType)) && StringUtils.equals(showType, "day")) {//如果按小时，这俩都不成立
					if (StringUtil.isEmpty(days)) {
						throw new OperationException("按天查询天数不能为空!");
					}
					dateList = DateUtil.getDataStinglist2(Integer.parseInt(days) - 1);
				} else if ("DIY".equals(String.valueOf(dateType)) && StringUtils.equals(showType, "day") && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3) {
					if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
						throw new OperationException("自定义查询起止时间不能为空!");
					}

					dateList = DateUtil.getBetweenDateString(startTime, endTime, "yyyyMMddHHmmss");
					int start = Integer.parseInt(startTime.substring(11, 13));
					int end = Integer.parseInt(endTime.substring(11, 13));
					if (end < start) {
						dateList.add(endTime.substring(0, 10));
					}
				} else{//按小时展示
					/*if ("TODAY".equals(String.valueOf(dateType))) {
						dateList = DateUtil.getCurrentDateHours();
						dateList.add(DateUtil.formatCurrentTime("yyyyMMddHHmmss"));
					}
					if ("HOUR".equals(String.valueOf(dateType))) {
						dateList = DateUtil.get24Hours();
						//同日常监测 最后一个小时不看
						dateList.remove(dateList.size() - 1);
					}*/
					dateList = DateUtil.getStartToEndOfHour(arrays[0], arrays[1]);
				}


				Iterator<String> i = dateList.iterator();
				String s = "";
				//if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
				if ("day".equals(showType) && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3) {
					while (i.hasNext()) {
						s = String.valueOf(i.next());
						s = s.replaceAll("[-/.: ]", "").trim();
						s = s.substring(0, 8);
						date.add(s);
						Date stringToDate = DateUtil.stringToDate(s, "yyyyMMdd");
						s = DateUtil.date2String(stringToDate, "yyyy-MM-dd");
						dateResult.add(s);
					}
				} else{
					dateResult.addAll(dateList);//一天之内的所有小时  ，yyyy/MM/dd HH:00格式
					date = DateUtil.getHourOfHH(arrays[0], arrays[1]);//一天之内的所有小时  ，HH格式 //下面的代码中主要为了取开始与结束时间
				}
				/*if ("TODAY".equals(String.valueOf(dateType)) || "HOUR".equals(String.valueOf(dateType))) {
					while (i.hasNext()) {
						s = String.valueOf(i.next());
						s = s.replaceAll("[-/.: ]", "").trim();
						s = s.substring(0, 10);
						date.add(s);

					}
					if ("TODAY".equals(String.valueOf(dateType))) {
						String formatCurrentTime = DateUtil.formatCurrentTime("HH");
						for (int j = 0; j <= Integer.valueOf(formatCurrentTime); j++) {
							if (j < 10) {
								dateResult.add("0" + j);
							} else {
								dateResult.add(j + "");
							}
						}
					} else if ("HOUR".equals(String.valueOf(dateType))) {
						//24h的结果类似201809101525 取整点
						for (String dateSubstring : date) {
							dateResult.add(dateSubstring.substring(8, 10));
						}
					}
				}*/
				List<String> date_all = (List<String> )result.get("date");
				if(date_all == null){
					date_all = new ArrayList<>();
				}
				date_all.addAll(dateResult);
				result.put("date", date_all);
				int size = date.size();
				String start = null;
				String end = null;
				//if ("DAY".equals(String.valueOf(dateType)) || "DIY".equals(String.valueOf(dateType))) {
				if ("day".equals(showType) && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") == 3) {
					if ("DAY".equals(String.valueOf(dateType))) {
						start = date.get(0) + new SimpleDateFormat("HHmmss").format(new Date());
					} else {
						// 保证DIY的时间正确
						start = date.get(0) + DateUtil.DAY_START;
					}
					end = date.get(date.size() - 1) + DateUtil.DAY_END;

				}else{
					start = arrays[0];
					end = arrays[1];
				}
				/*if ("TODAY".equals(String.valueOf(dateType)) || "HOUR".equals(String.valueOf(dateType))) {
					start = date.get(0) + DateUtil.HOUR_START;
					end = date.get(date.size() - 1) + DateUtil.HOUR_END;

				}*/

				// QueryBuilder chuantong =
				// specialProject.toNoPagedAndTimeBuilder();

				// QueryBuilder weibo =
				// specialProject.toNoPagedAndTimeBuilderWeiBo();


				// QueryBuilder weixin =
				// specialProject.toNoPagedAndTimeBuilderWeiXin();


				int num = 0;
				// 1.新闻
				QueryBuilder chuantong = new QueryBuilder();
				chuantong.filterByTRSL(esSql);
				String chuantongTrsl = chuantong.asTRSL();
				chuantong.setServer(specialProject.isServer());
				chuantong.setPageSize(size);

				String sqlNews = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND ("
						+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻) NOT " + FtsFieldConst.FIELD_SITENAME + ":百度贴吧 )AND "
						+ chuantongTrsl;
				chuantong.filterByTRSL(sqlNews);
				GroupResult categoryQueryNews = commonListService.categoryQuery(chuantong,sim,irSimflag,irSimflagAll,groupField,"special",Const.TYPE_NEWS);
//				GroupResult categoryQueryNews = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlNews, sim,
//						irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
				Map<String, Object> mapNews = new HashMap<>();
				mapNews.put("groupName", "新闻");
				//flag是小时
				if (flag) {
					mapNews.put("list", MapUtil.sortAndChangeList(categoryQueryNews, dateResult, "yyyy-MM-dd", flag));
					//按天 查一次
				} else {
					mapNews.put("list", MapUtil.sortAndChangeList(categoryQueryNews, date, "yyyy-MM-dd", flag));
					//按小时  查多次
				}
				if (!listEmpty) {
					list.add(mapNews);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapNews.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				num++;
				// 2.微博
				QueryBuilder weibo = new QueryBuilder();
				weibo.filterByTRSL(esSql);
				String weiboTrsl = weibo.asTRSL();
				weibo.setServer(specialProject.isServer());
				weibo.setPageSize(size);
				String sqlStatus = FtsFieldConst.FIELD_CREATED_AT + ":[" + start + " TO " + end + "]  AND " + weiboTrsl;
				weibo.filterByTRSL(sqlStatus);
				log.error("微博：" + sqlStatus);
				GroupResult categoryQueryStatus = commonListService.categoryQuery(weibo,sim,irSimflag,irSimflagAll,groupFieldWeibo,"special",Const.GROUPNAME_WEIBO);
//				GroupResult categoryQueryStatus = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlStatus,
//						sim, irSimflag, irSimflagAll, groupFieldWeibo, size,"special", Const.WEIBO);
				Map<String, Object> mapStatus = new HashMap<>();
				mapStatus.put("groupName", "微博");
				if (flag) {
					mapStatus.put("list", MapUtil.sortAndChangeList(categoryQueryStatus, dateResult, "yyyy-MM-dd", flag));
				} else {
					mapStatus.put("list", MapUtil.sortAndChangeList(categoryQueryStatus, date, "yyyy-MM-dd", flag));
				}
				if (!listEmpty) {
					list.add(mapStatus);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapStatus.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				num++;

				// 3.微信用户
				QueryBuilder weixin = new QueryBuilder();
				weixin.filterByTRSL(esSql);
				String weixinTrsl = weixin.asTRSL();
				weixin.setServer(specialProject.isServer());
				weixin.setPageSize(size);
				String sqlWeChat = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]  AND " + weixinTrsl;
				weixin.filterByTRSL(sqlWeChat);
				GroupResult categoryQueryWeChat = commonListService.categoryQuery(weibo,sim,irSimflag,irSimflagAll,groupFieldWeibo,"special",Const.GROUPNAME_WEIXIN);
//				GroupResult categoryQueryWeChat = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlWeChat,
//						sim, irSimflag, irSimflagAll, groupFieldWeixin, size,"special", Const.WECHAT);
				Map<String, Object> mapWeChat = new HashMap<>();
				mapWeChat.put("groupName", "微信");
				if (flag) {
					mapWeChat.put("list", MapUtil.sortAndChangeList(categoryQueryWeChat, dateResult, "yyyy-MM-dd", flag));
				} else {
					mapWeChat.put("list", MapUtil.sortAndChangeList(categoryQueryWeChat, date, "yyyy-MM-dd", flag));
				}
				if (!listEmpty) {
					list.add(mapWeChat);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapWeChat.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				num++;

				// 4.论坛用户
				String sqlLunTan = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] " + "AND ("
						+ FtsFieldConst.FIELD_GROUPNAME + ":" + ESGroupName.FORUM_IN.getName() + " OR "
						+ FtsFieldConst.FIELD_SITENAME + ":百度贴吧)  AND " + chuantongTrsl;
				QueryBuilder luantan = new QueryBuilder();
				luantan.filterByTRSL(esSql);
				luantan.setServer(specialProject.isServer());
				luantan.setPageSize(size);
				luantan.filterByTRSL(sqlLunTan);
				GroupResult categoryQueryLunTan = commonListService.categoryQuery(luantan,sim,irSimflag,irSimflagAll,groupField,"special",Const.TYPE_NEWS);
//				GroupResult categoryQueryLunTan = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlLunTan,
//						sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
				Map<String, Object> mapLunTan = new HashMap<>();
				mapLunTan.put("groupName", "论坛");
				if (flag) {
					mapLunTan.put("list", MapUtil.sortAndChangeList(categoryQueryLunTan, dateResult, "yyyy-MM-dd", flag));
				} else {
					mapLunTan.put("list", MapUtil.sortAndChangeList(categoryQueryLunTan, date, "yyyy-MM-dd", flag));
				}
				//list.add(mapLunTan);
				if (!listEmpty) {
					list.add(mapLunTan);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapLunTan.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				num++;

				// 5.博客用户
				String sqlBlog = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] " + "AND ("
						+ FtsFieldConst.FIELD_GROUPNAME + ":" + ESGroupName.BLOG_IN.getName() + ") AND " + chuantongTrsl;
				log.error("博客：" + sqlBlog);
				QueryBuilder boke = new QueryBuilder();
				boke.filterByTRSL(esSql);
				boke.setServer(specialProject.isServer());
				boke.setPageSize(size);
				boke.filterByTRSL(sqlBlog);
				GroupResult categoryQueryBlog = commonListService.categoryQuery(boke,sim,irSimflag,irSimflagAll,groupField,"special",Const.TYPE_NEWS);
//				GroupResult categoryQueryBlog = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlBlog, sim,
//						irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
				Map<String, Object> mapBlog = new HashMap<>();
				mapBlog.put("groupName", "博客");
				if (flag) {
					mapBlog.put("list", MapUtil.sortAndChangeList(categoryQueryBlog, dateResult, "yyyy-MM-dd", flag));
				} else {
					mapBlog.put("list", MapUtil.sortAndChangeList(categoryQueryBlog, date, "yyyy-MM-dd", flag));
				}
				//list.add(mapBlog);
				if (!listEmpty) {
					list.add(mapBlog);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapBlog.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				num++;

				// 6.客户端
				String sqlApp = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND ("
						+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻_手机客户端) NOT " + FtsFieldConst.FIELD_SITENAME
						+ ":百度贴吧) AND " + chuantongTrsl;
				QueryBuilder apps = new QueryBuilder();
				apps.filterByTRSL(esSql);
				apps.setServer(specialProject.isServer());
				apps.setPageSize(size);
				apps.filterByTRSL(sqlApp);
				GroupResult categoryQueryApps = commonListService.categoryQuery(apps,sim,irSimflag,irSimflagAll,groupField,"special",Const.TYPE_NEWS);
//				GroupResult categoryQueryApps = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlApp, sim,
//						irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
				Map<String, Object> mapApps = new HashMap<>();
				mapApps.put("groupName", "客户端");
				if (flag) {
					mapApps.put("list", MapUtil.sortAndChangeList(categoryQueryApps, dateResult, "yyyy-MM-dd", flag));
				} else {
					mapApps.put("list", MapUtil.sortAndChangeList(categoryQueryApps, date, "yyyy-MM-dd", flag));
				}
				//list.add(mapApps);
				if (!listEmpty) {
					list.add(mapApps);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapApps.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				num++;

				// 7.电子报
				// String sqlEpaper = FtsFieldConst.FIELD_URLTIME + ":[" + start + "
				// TO " + end + "] AND "
				// + FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻_电子报) " + chuantongTrsl;
				String sqlEpaper = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":(国内新闻_电子报) AND " + chuantongTrsl;
				QueryBuilder epapers = new QueryBuilder();
				epapers.filterByTRSL(esSql);
				epapers.setServer(specialProject.isServer());
				epapers.setPageSize(size);
				epapers.filterByTRSL(sqlEpaper);
				GroupResult categoryQueryEpapers = commonListService.categoryQuery(epapers,sim,irSimflag,irSimflagAll,groupField,"special",Const.TYPE_NEWS);
//				GroupResult categoryQueryEpapers = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlEpaper,
//						sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_NI_INDEX);
				Map<String, Object> mapEpapers = new HashMap<>();
				mapEpapers.put("groupName", "电子报");
				if (flag) {
					mapEpapers.put("list", MapUtil.sortAndChangeList(categoryQueryEpapers, dateResult, "yyyy-MM-dd", flag));
				} else {
					mapEpapers.put("list", MapUtil.sortAndChangeList(categoryQueryEpapers, date, "yyyy-MM-dd", flag));
				}
				//list.add(mapEpapers);
				if (!listEmpty) {
					list.add(mapEpapers);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapEpapers.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				num++;

				// 8.境外媒体
				String sqlForeign = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":((国外新闻) OR (港澳台新闻)) AND " + chuantongTrsl;
				QueryBuilder foreigns = new QueryBuilder();
				foreigns.filterByTRSL(esSql);
				foreigns.setServer(specialProject.isServer());
				foreigns.setPageSize(size);
				foreigns.filterByTRSL(sqlForeign);
				GroupResult categoryQueryForeigns = commonListService.categoryQuery(foreigns,sim,irSimflag,irSimflagAll,groupField,"special",Const.TYPE_NEWS);
//				GroupResult categoryQueryForeigns = hybase8SearchService.categoryQuery(specialProject.isServer(),
//						sqlForeign, sim, irSimflag, irSimflagAll, groupField, size,"special",Const.HYBASE_NI_INDEX);
				Map<String, Object> mapForeigns = new HashMap<>();
				//页面展示 国外新闻 变为 境外网站  2019-12-10
				//mapForeigns.put("groupName", "境外媒体");
				mapForeigns.put("groupName", "境外网站");
				if (flag) {
					mapForeigns.put("list", MapUtil.sortAndChangeList(categoryQueryForeigns, dateResult, "yyyy-MM-dd", flag));
				} else {
					mapForeigns.put("list", MapUtil.sortAndChangeList(categoryQueryForeigns, date, "yyyy-MM-dd", flag));
				}
				//list.add(mapForeigns);
				if (!listEmpty) {
					list.add(mapForeigns);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapForeigns.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				num++;

				// 9.TWitter
				String sqlTWitter = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":(TWitter) AND " + chuantongTrsl;
				QueryBuilder twitter = new QueryBuilder();
				twitter.filterByTRSL(esSql);
				twitter.setServer(specialProject.isServer());
				twitter.setPageSize(size);
				twitter.filterByTRSL(sqlTWitter);
				GroupResult categoryQueryTWitter = commonListService.categoryQuery(twitter,sim,irSimflag,irSimflagAll,groupField,"special",Const.GROUPNAME_TWITTER);
				log.info("TWitter" + sqlTWitter);
//				GroupResult categoryQueryTWitter = hybase8SearchService.categoryQuery(specialProject.isServer(), sqlTWitter,
//						sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_OVERSEAS);
				Map<String, Object> mapTWitter = new HashMap<>();
				mapTWitter.put("groupName", "Twitter");
				if (flag) {
					mapTWitter.put("list", MapUtil.sortAndChangeList(categoryQueryTWitter, dateResult, "yyyy-MM-dd", flag));
				} else {
					mapTWitter.put("list", MapUtil.sortAndChangeList(categoryQueryTWitter, date, "yyyy-MM-dd", flag));
				}
				//list.add(mapTWitter);
				if (!listEmpty) {
					list.add(mapTWitter);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapTWitter.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				num++;

				// 10.FaceBook
				String sqlFaceBook = FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "] AND "
						+ FtsFieldConst.FIELD_GROUPNAME + ":(FaceBook) AND " + chuantongTrsl;
				log.info("FaceBook" + sqlTWitter);
				QueryBuilder faceBook = new QueryBuilder();
				faceBook.filterByTRSL(esSql);
				faceBook.setServer(specialProject.isServer());
				faceBook.setPageSize(size);
				faceBook.filterByTRSL(sqlFaceBook);
				GroupResult categoryQueryFaceBook = commonListService.categoryQuery(faceBook,sim,irSimflag,irSimflagAll,groupField,"special",Const.GROUPNAME_FACEBOOK);
//				GroupResult categoryQueryFaceBook = hybase8SearchService.categoryQuery(specialProject.isServer(),
//						sqlFaceBook, sim, irSimflag, irSimflagAll, groupField, size,"special", Const.HYBASE_OVERSEAS);
				Map<String, Object> mapFaceBook = new HashMap<>();
				mapFaceBook.put("groupName", "FaceBook");
				if (flag) {
					mapFaceBook.put("list", MapUtil.sortAndChangeList(categoryQueryFaceBook, dateResult, "yyyy-MM-dd", flag));
				} else {
					mapFaceBook.put("list", MapUtil.sortAndChangeList(categoryQueryFaceBook, date, "yyyy-MM-dd", flag));
				}
				//list.add(mapFaceBook);
				if (!listEmpty) {
					list.add(mapFaceBook);
				} else {
					Map<String, Object> map_all = list.get(num);
					List<Long> list_all = (List<Long>) map_all.get("list");
					List<Long> list_day = (List<Long>) mapFaceBook.get("list");
					list_all.addAll(list_day);
					map_all.put("list", list_all);
					list.set(num, map_all);
				}
				if(!listEmpty){
					listEmpty = true;
				}
			}
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
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		try {
			// 传统媒体分类统计结果
			Date startToDate = DateUtil.stringToDate(start, "yyyyMMddHHmmss");
			Date endToDate = DateUtil.stringToDate(end, "yyyyMMddHHmmss");
			specialProject.setStartTime(startToDate);
			specialProject.setEndTime(endToDate);
			QueryBuilder builder = specialProject.toNoPagedBuilder();
			builder.setDatabase(Const.HYBASE_NI_INDEX);
			//跨数据源排重
			if (irSimflagAll){
				builder.filterField(FtsFieldConst.FIELD_IR_SIMFLAGALL,"0 OR \"\"",Operator.Equal);
			}
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
			long weibo = hybase8SearchService.ftsCount(builderWeiBo, false, irSimflag,irSimflagAll ,"special");
			map.put("微博", weibo);
			log.info(builderWeiBo.asTRSL());
			// 微信总数统计结果
			QueryBuilder builderWeChart = specialProject.toNoPagedBuilderWeiXin();// new
			builderWeChart.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内微信", Operator.Equal);
			builderWeChart.setDatabase(Const.WECHAT);
			log.info(builderWeChart.asTRSL());
			long wechart = hybase8SearchService.ftsCount(builderWeChart, false, irSimflag,irSimflagAll ,"special");
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
		List<ClassInfo> classInfo = new ArrayList<>();
		// url排重
		// boolean irSimflag = specialProject.isIrSimflag();
		try {
			// 传统媒体分类统计结果
			Date startToDate = DateUtil.stringToDate(start, "yyyyMMddHHmmss");
			Date endToDate = DateUtil.stringToDate(end, "yyyyMMddHHmmss");
			specialProject.setStartTime(startToDate);
			specialProject.setEndTime(endToDate);
			QueryBuilder builder = specialProject.toNoPagedBuilder();
			boolean sim = specialProject.isSimilar();
			builder.page(0, 20);
			// 根据已选择的来源不同 查的group不同
			String groupName = specialProject.getSource();
			if ((null != foreign && !"".equals(foreign)) && (null == area || "".equals(area))) {
				groupName = "境外媒体";
			}
			if (null != foreign && !"".equals(foreign) && !"ALL".equals(foreign)) {
				infoListService.setForeignData(foreign, builder, null, null, null);
			}
			String replace = groupName.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻");
			String[] split = replace.split(";");
			List<String> metas = new ArrayList<>();
			if (split != null && split.length > 0) {
				metas = Arrays.asList(split);
			}

			if (StringUtil.isNotEmpty(groupName)) {
				if (!"ALL".equals(groupName)) {
					builder.filterField(
							FtsFieldConst.FIELD_GROUPNAME, groupName.replace(";", " OR ")
									.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),
							Operator.Equal);
				} else {
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME,
							Const.STATTOTAL_GROUP.replace(";", " OR ").replace("境外媒体", "国外新闻"), Operator.Equal);
				}
			} else {
				String trsl = "";
				if (StringUtil.isNotEmpty(specialProject.getTrsl())) {
					trsl += Const.TYPE_NEWS.replace(";", " OR ");
				}
				if (StringUtil.isNotEmpty(specialProject.getStatusTrsl())) {
					if (!"".equals(trsl)) {
						trsl += " OR " + Const.TYPE_WEIBO;
					} else {
						trsl += Const.TYPE_WEIBO;
					}
				}
				if (StringUtil.isNotEmpty(specialProject.getWeChatTrsl())) {
					if (!"".equals(trsl)) {
						trsl += " OR " + Const.TYPE_WEIXIN_GROUP;
					} else {
						trsl += Const.TYPE_WEIXIN_GROUP;
					}
				}
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, trsl, Operator.Equal);
			}

			log.warn(builder.asTRSL());
			GroupResult categoryQuery = hybase8SearchService.categoryQuery(builder, sim, false,false,
					FtsFieldConst.FIELD_GROUPNAME,"special",Const.MIX_DATABASE);
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
			map.put("国内视频", 0L);
			for (GroupInfo groupInfo : groupList) {
				String fieldValue = groupInfo.getFieldValue();
				if ("国内微信".equals(fieldValue)) {
					fieldValue = "微信";
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
				classInfo.add(new ClassInfo(key, entry.getValue().intValue()));
			}
		} catch (Exception e) {
			log.error("statByClassification error ", e);
			throw new OperationException("statByClassification error: " + e, e);
		}
		return classInfo;
	}

	@Override
	public int getEmotionalValue(SpecialProject specialProject, String groupName, String startTime, String endTime,
								 String industryType, String area) throws Exception {
		return 0;
	}

	@Override
	public List<TippingPoint> getTippingPoint(QueryBuilder queryBuilder, FtsDocumentCommonVO documentStatus,
											  Date beginDate,boolean sim, boolean irSimflag,boolean  irSimflagAll) throws TRSException, TRSSearchException {
		// String startTime = DateUtil.date2String(beginDate,
		// DateUtil.yyyyMMddHHmmss);
		// String endTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss);
		String trsl = queryBuilder.asTRSL();
		QueryBuilder builder = new QueryBuilder();
		// builder.filterField(ESFieldConst.IR_CREATED_AT, new
		// String[]{startTime, endTime}, Operator.Between);
		builder.filterByTRSL(trsl);
		String baseUrl = documentStatus.beforeUrl();
		builder.filterField(ESFieldConst.IR_RETWEETED_URL, "\"" + baseUrl + "\"", Operator.Equal);
		builder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
		builder.setPageSize(20);
		builder.setServer(queryBuilder.isServer());
		String asTRSL1 = builder.asTRSL();
//		List<TippingPoint> pointsss = hybase8SearchService.ftsQuery(builder, TippingPoint.class, true, irSimflag,irSimflagAll,"special");
		InfoListResult infoListResult = commonListService.queryPageList(builder,sim,irSimflag,irSimflagAll,Const.GROUPNAME_WEIBO,"special",UserUtils.getUser(),false);
		List<TippingPoint> result = new ArrayList<>();
		if (ObjectUtil.isNotEmpty(infoListResult)) {
			PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
			List<FtsDocumentCommonVO> points = content.getPageItems();
			List<String> scName = new ArrayList<>();
			for (FtsDocumentCommonVO point : points) {
				if (point.getScreenName() != null) {
					if (!scName.contains(point.getScreenName()) && result.size() < 10) {
						scName.add(point.getScreenName());
						TippingPoint tippingPoint = new TippingPoint();
						tippingPoint.setScreenName(point.getScreenName());
						tippingPoint.setTrslk(point.getTrslk());
						tippingPoint.setMd5(point.getMd5Tag());
						tippingPoint.setRttCount(point.getRttCount());
						tippingPoint.setSid(point.getSid());
						tippingPoint.setId(point.getId());
						result.add(tippingPoint);
					}
				}
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
			String s = md5Builder.asTRSL();
//			List<TippingPoint> md5Points = hybase8SearchService.ftsQuery(md5Builder, TippingPoint.class, true,
//					irSimflag,irSimflagAll,"special");
			InfoListResult infoListResult2 = commonListService.queryPageList(md5Builder,sim,irSimflag,irSimflagAll,Const.GROUPNAME_WEIBO,"special",UserUtils.getUser(),false);
			PagedList<FtsDocumentCommonVO> content2 = (PagedList<FtsDocumentCommonVO>) infoListResult2.getContent();
			List<FtsDocumentCommonVO> md5Points = content2.getPageItems();
			List<String> srcName = new ArrayList<>();
			List<TippingPoint> returnList = new ArrayList<>();
			for (FtsDocumentCommonVO point : md5Points) {
				if (point.getScreenName() != null) {
					if (!srcName.contains(point.getScreenName()) && returnList.size() < 10) {
						srcName.add(point.getScreenName());
						TippingPoint tippingPoint = new TippingPoint();
						tippingPoint.setScreenName(point.getScreenName());
						tippingPoint.setTrslk(point.getTrslk());
						tippingPoint.setMd5(point.getMd5Tag());
						tippingPoint.setRttCount(point.getRttCount());
						tippingPoint.setSid(point.getSid());
						tippingPoint.setId(point.getId());
						returnList.add(tippingPoint);
					}
				}
			}
			return returnList;
		}
		return result;
	}

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

	/**
	 * 根据urltime获取对应数据
	 *
	 * @param url
	 * @param sim
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * 由父节点 获取子节点数据
	 *
	 * @param document
	 *            父节点
	 * @param timeArray
	 * @param sim
	 * @throws Exception
	 */
	private void getAllRTTUserNew(SinaUser document, String[] timeArray, boolean sim, boolean irSimflag,boolean irSimflagAll)
			throws Exception {
		QueryBuilder builder = new QueryBuilder();
		// builder.page(0, 5000);
		builder.page(0, 6);
		/*
		 * if (document == null){ return null; }
		 */
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
				"IR_SITENAME", 9,"special", Const.HYBASE_NI_INDEX);
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
		List<CategoryBean> list = new ArrayList<>();
		String[] medias = trsl.split(";");
		for (String str : medias) {
			String key = str.substring(0, str.indexOf("="));
			String val = str.substring(str.indexOf("=") + 1, str.length());
			list.add(new CategoryBean(key, val));
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
				dateList = DateUtil.getBetweenDateString(startTime, endTime, "yyyyMMddHHmmss");
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
								 String fuzzyValue, String fuzzyValueScope,int pageNo, int pageSize, String forwarPrimary, String invitationCard,
								 boolean isExport, String thirdWord) throws Exception {
		boolean weight = specialProject.isWeight();
		//因热词探索要排除掉原专题的一些信息，而此处 specialProject.toBuilder 会默认加上了原专题的标题信息，所以另起一套逻辑
		if("hotWordExplore".equals(chartType)){
			return getHotWordExploreList(specialProject, industryType, area, chartType, xType, source, sort, emotion, fuzzyValue, pageNo, pageSize, isExport, thirdWord);
		}
		User loginUser = UserUtils.getUser();
		boolean sim = specialProject.isSimilar();
		boolean irSimflag = specialProject.isIrSimflag();
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		String timeRange = specialProject.getTimeRange();
		boolean server = specialProject.isServer();

		QueryBuilder builder = specialProject.toBuilder(pageNo, pageSize, false);
		QueryBuilder countBuilder = specialProject.toBuilder(pageNo, pageSize, false);
		// 拼接检索条件
		for (ChartType type : ChartType.values()) {
			// 匹配数据源,并且拼接数据时间
			if (type.getType().equals(chartType)) {
				// 转换数据时间
				String dataEndTime = null;
				String dataStartTime = null;
				if (StringUtil.isNotEmpty(dateTime)) {
					if (type.getType().equals(ChartType.NEWSSITEANALYSIS.getType())
							|| type.getType().equals(ChartType.TRENDMESSAGE.getType())
							|| type.getType().equals(ChartType.NETTENDENCY.getType())
							|| type.getType().equals(ChartType.METATENDENCY.getType())) {
						if(dateTime.length() == 16){
							dateTime = dateTime.replace("-", "").replace("/", "")
									.replace(":", "").replace(" ", "").trim();
							dateTime = dateTime+"00";
							dataEndTime = dateTime.substring(0,10)+"5959";
						}else{
							if (dateTime.length() <= 2) {
								if (dateTime.length() == 1) {
									dataStartTime = "0" + dateTime;
								} else {
									dataStartTime = dateTime;
								}
								if ("24h".equals(timeRange)) {
									// 当前小时
									String formatCurrentTime = DateUtil.formatCurrentTime("HH");
									Integer paramTime = Integer.valueOf(dataStartTime);
									Integer currentTime = Integer.valueOf(formatCurrentTime);
									if (paramTime >= currentTime) {
										// 用前一天
										dateTime = (DateUtil.formatDateAfter(new Date(), DateUtil.yyyyMMdd2, -1))
												+ dataStartTime + "0000";// 间隔 1 天
										// 代表默认查
										// 1 天
										dataEndTime = (DateUtil.formatDateAfter(new Date(), DateUtil.yyyyMMdd2, -1))
												+ dataStartTime + "5959";
									} else {
										// 当天
										dateTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2)) + dataStartTime
												+ "0000";
										dataEndTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2))
												+ dataStartTime + "5959";
									}
								} else if ("0d".equals(timeRange)) {
									// 当天
									dateTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2)) + dataStartTime
											+ "0000";
									dataEndTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2)) + dataStartTime
											+ "5959";
								}

							} else {
								dateTime = dateTime.replace("-", "");
								dataEndTime = DateUtil.formatDateAfter(dateTime, DateUtil.yyyyMMdd2, 1);// 间隔1天代表默认查一天
								dateTime = dateTime.replace("-", "").replace(":", "").replaceAll("\r|\n|\t", "").trim();
								dataEndTime = dateTime.replace("-", "").replace(":", "").replaceAll("\r|\n|\t", "").trim();
								if (dateTime.length() == 8){
									dateTime = dateTime+"000000";
								}
								if (dataEndTime.length() == 8){
									dataEndTime = dataEndTime+"235959";
								}
							}
						}

					}

					if (type.getType().equals(ChartType.NEWSSITEANALYSIS.getType())){
						builder.filterField(FtsFieldConst.FIELD_URLDATE, dateTime, Operator.Equal);
						countBuilder.filterField(FtsFieldConst.FIELD_URLDATE, dateTime, Operator.Equal);
					}else {
						builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { dateTime, dataEndTime },
								Operator.Between);
						countBuilder.filterField(FtsFieldConst.FIELD_URLTIME,
								new String[] { dateTime, dataEndTime }, Operator.Between);
					}
				}
				if (!"ALL".equals(emotion)) { // 情感
					if ("中性".equals(emotion)) {
						// 因为hybase库里没有中性标，默认IR_APPRAISE字段值为""时 是中性
						// 直接为某字段赋空置，是不行的
						String trsl = builder.asTRSL();
						String trsl1 = countBuilder.asTRSL();
						String database = builder.getDatabase();
						String database1 = countBuilder.getDatabase();
						trsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_APPRAISE).append(":(")
								.append("正面").append(" OR ").append("负面").append(")").toString();
						trsl1 += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_APPRAISE).append(":(")
								.append("正面").append(" OR ").append("负面").append(")").toString();
						builder = new QueryBuilder();
						countBuilder = new QueryBuilder();
						builder.filterByTRSL(trsl);
						countBuilder.filterByTRSL(trsl1);
						builder.setDatabase(database);
						countBuilder.setDatabase(database1);
					} else {
						builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
						countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
					}

				}
				if (!"ALL".equals(area)) { // 拼接地域
					String[] areaSplit = area.split(";");
					String contentArea = "";
					for (int i = 0; i < areaSplit.length; i++) {
						if (server) {
							areaSplit[i] = "中国\\\\" + areaSplit[i] + "%";
						} else {
							areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
						}

						if (i != areaSplit.length - 1) {
							areaSplit[i] += " OR ";
						}
						contentArea += areaSplit[i];
					}
					builder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
					countBuilder.filterByTRSL("CATALOG_AREA:(" + contentArea + ")");
				}

				// 拼接数据参数
				if (StringUtil.isNotEmpty(xType)) {
					if (xType.equals("微博PC端")) {
						xType = "微博 weibo.com";
					}
					String[] xTpyeField = type.getXTpyeField();// 字段名
					if (chartType.equals(ChartType.AREA.getType())) {// 地图
						/*
						2019-12-30改
						//原有规则是查询这个字段包含这个省，但是统计方法改变，只统计到省，存在历史数据问题（没有单独追加省，造成很多数据没有到省数据），所以查询时不能模糊查询
						if (server) {
							xType = "中国\\\\" + xType + "%";
						} else {
							xType = "中国\\\\" + xType + "*";
						}*/
						// xType = "中国\\\\" + xType + "*";
						xType = Const.PROVINCE_FULL_NAME.get(xType);
						xType = "中国\\\\" + xType;
						builder.filterByTRSL(FtsFieldConst.CATALOG_AREANEW + ":(" + xType+")");
						countBuilder.filterByTRSL(FtsFieldConst.CATALOG_AREANEW + ":(" + xType+")");
					} else if (chartType.equals(ChartType.WORDCLOUD.getType())) {
						if ("location".equals(entityType) && !"省".equals(xType.substring(xType.length() - 1)) && !xType.contains("自治区")) {
							String xTypeNew = "";
							if ("市".equals(xType.substring(xType.length() - 1))) {
								xTypeNew = xType.replace("市", "");
							} else {
								xTypeNew = xType;
							}
							if (!xTypeNew.contains("\"")) {
								xTypeNew = "\"" + xTypeNew + "\"";
							}
							String trsl = FtsFieldConst.FIELD_URLTITLE+":("+xTypeNew +") OR "+ FtsFieldConst.FIELD_CONTENT +":(" +xTypeNew + ")";
							builder.filterByTRSL(trsl);
							countBuilder.filterByTRSL(trsl);

						} else {
							builder.filterField(Const.PARAM_MAPPING.get(entityType), xType, Operator.Equal);
							countBuilder.filterField(Const.PARAM_MAPPING.get(entityType), xType, Operator.Equal);
						}
					}else if (chartType.equals(ChartType.ACTIVE_LEVEL.getType())) {
						String trsl = "";
						StringBuffer sb = new StringBuffer();
						if (Const.ACTIVE_LEVEL_SITENAME.contains(source)){
							trsl = sb.append("IR_SITENAME").append(":").append("\"").append(xType).append("\"")
									.toString();
						}else if (Const.ACTIVE_LEVEL_AUTHORS.contains(source)){
							trsl = sb.append("IR_AUTHORS").append(":").append("\"").append(xType).append("\"")
									.toString();
						}
						builder.filterByTRSL(trsl);
						countBuilder.filterByTRSL(trsl);
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
		List<FtsDocumentCommonVO> voList = new ArrayList<>();
		// 是否查server
		builder.setServer(server);
		countBuilder.setServer(server);
		//处理来源
		if (!"ALL".equals(source)){
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME,source,Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,source,Operator.Equal);
		}
		//处理时间
		if (StringUtil.isEmpty(dateTime)){
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			builder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME,timeArray, Operator.Between);
		}
		if (!"ALL".equals(industryType)) {// 拼接行业类型
			builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
		}
		// 结果中搜索
		if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {
			StringBuilder trsl = new StringBuilder();
			String hybaseField = "fullText";
			switch (fuzzyValueScope){
				case "title":
					hybaseField = FtsFieldConst.FIELD_URLTITLE;
					break;
				case "source":
					hybaseField = FtsFieldConst.FIELD_SITENAME;
					break;
				case "author":
					hybaseField = FtsFieldConst.FIELD_AUTHORS;
					break;
			}
			if("fullText".equals(hybaseField)){
				trsl.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				trsl.append(hybaseField).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			builder.filterByTRSL(trsl.toString());
			countBuilder.filterByTRSL(trsl.toString());
		}
		if (Const.MEDIA_TYPE_WEIBO.contains(source)){

			String builderTRSL = builder.asTRSL();
			String countBuilderTRSL = countBuilder.asTRSL();
			StringBuilder builderTrsl = new StringBuilder(builderTRSL);
			StringBuilder countBuilderTrsl = new StringBuilder(countBuilderTRSL);
			if ("primary".equals(forwarPrimary)) {
				// 原发
				builder.filterByTRSL(Const.PRIMARY_WEIBO);
			} else if ("forward".equals(forwarPrimary)) {
				// 转发
				builder = new QueryBuilder();
				builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
				builder.filterByTRSL(builderTrsl.toString());

				countBuilder = new QueryBuilder();
				countBuilderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
				countBuilder.filterByTRSL(countBuilderTrsl.toString());
			}
			builder.setDatabase(Const.WEIBO);
			countBuilder.setDatabase(Const.WEIBO);
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
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			log.info("WEIBO:" + builder.asTRSL());
			obj = infoListService.getStatusList(builder, loginUser, sim, irSimflag,irSimflagAll, isExport,"special");
			if (isExport) {// 是导出 转为混合实体存入redis
				InfoListResult<FtsDocumentStatus> statusResult = (InfoListResult<FtsDocumentStatus>) obj;
				PagedList<FtsDocumentStatus> content = (PagedList<FtsDocumentStatus>) statusResult.getContent();
				List<FtsDocumentStatus> list = content.getPageItems();
				for (FtsDocumentStatus status : list) {
					FtsDocumentCommonVO vo = new FtsDocumentCommonVO();
					vo.setTitle(status.getStatusContent());
					vo.setContent(status.getStatusContent());
					vo.setUrlName(status.getUrlName());
					vo.setGroupName("微博");
					vo.setSiteName(status.getSiteName());
					vo.setUrlTime(status.getUrlTime());
					voList.add(vo);
				}
			}
		} else if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
			builder.setDatabase(Const.WECHAT);
			countBuilder.setDatabase(Const.WECHAT);
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
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			log.info("WECHAT:" + builder.asTRSL());

			obj = infoListService.getWeChatList(builder, loginUser, sim, irSimflag,irSimflagAll, isExport,"special");
			if (isExport) {// 是导出 转为混合实体存入redis
				InfoListResult<FtsDocumentWeChat> statusResult = (InfoListResult<FtsDocumentWeChat>) obj;
				PagedList<FtsDocumentWeChat> content = (PagedList<FtsDocumentWeChat>) statusResult.getContent();
				List<FtsDocumentWeChat> list = content.getPageItems();
				for (FtsDocumentWeChat wechat : list) {
					FtsDocumentCommonVO vo = new FtsDocumentCommonVO();
					vo.setTitle(wechat.getUrlTitle());
					vo.setContent(wechat.getContent());
					vo.setUrlName(wechat.getUrlName());
					vo.setGroupName("国内微信");
					vo.setSiteName(wechat.getSiteName());
					vo.setUrlTime(wechat.getUrlTime());
					voList.add(vo);
				}
			}
		} else if (Const.MEDIA_TYPE_FINAL_NEWS.contains(source)){
			//可以加主回帖筛选，但是不要加groupName字段，会限制查询条数
			StringBuffer sb = new StringBuffer();
			if ("0".equals(invitationCard)) {// 主贴
				sb.append(Const.NRESERVED1_LUNTAN);
			} else if ("1".equals(invitationCard)) {// 回帖
				sb.append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
			}
			if(StringUtil.isNotEmpty(sb.toString())){
				builder.filterByTRSL(sb.toString());
				countBuilder.filterByTRSL(sb.toString());
			}

			builder.setDatabase(Const.HYBASE_NI_INDEX);
			countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
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
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			log.info("HYBASE_NI_INDEX:" + builder.asTRSL());

			obj = infoListService.getDocList(builder, loginUser, sim, irSimflag,irSimflagAll, isExport,"special");
			if (isExport) {// 是导出 转为混合实体存入redis
				InfoListResult statusResult = (InfoListResult) obj;
				PagedList<FtsDocument> content = (PagedList<FtsDocument>) statusResult.getContent();
				List<FtsDocument> list = content.getPageItems();
				for (FtsDocument document : list) {
					FtsDocumentCommonVO vo = new FtsDocumentCommonVO();
					vo.setTitle(document.getUrlTitle());
					vo.setContent(document.getContent());
					vo.setUrlName(document.getUrlName());
					vo.setGroupName(document.getGroupName());
					vo.setSiteName(document.getSiteName());
					vo.setUrlTime(document.getUrlTime());
					voList.add(vo);
				}
			}
		}else if (Const.MEDIA_TYPE_TF.contains(source)){
			builder.setDatabase(Const.HYBASE_OVERSEAS);
			countBuilder.setDatabase(Const.HYBASE_OVERSEAS);
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
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			log.info("HYBASE_OVERSEAS:" + builder.asTRSL());

			obj = infoListService.getDocTFList(builder, loginUser, sim, irSimflag,irSimflagAll,"special");
			if (isExport) {// 是导出 转为混合实体存入redis
				InfoListResult statusResult = (InfoListResult) obj;
				PagedList<FtsDocument> content = (PagedList<FtsDocument>) statusResult.getContent();
				List<FtsDocument> list = content.getPageItems();
				for (FtsDocument document : list) {
					FtsDocumentCommonVO vo = new FtsDocumentCommonVO();
					vo.setTitle(document.getUrlTitle());
					vo.setContent(document.getContent());
					vo.setUrlName(document.getUrlName());
					vo.setGroupName(document.getGroupName());
					vo.setSiteName(document.getSiteName());
					vo.setUrlTime(document.getUrlTime());
					voList.add(vo);
				}
			}
		}else if ("ALL".equals(source)){
			QueryCommonBuilder queryCommonBuilder = new QueryCommonBuilder();
			QueryCommonBuilder countCommonBuilder = new QueryCommonBuilder();
			queryCommonBuilder.setPageNo(pageNo);
			countCommonBuilder.setPageNo(pageNo);
			queryCommonBuilder.setPageSize(pageSize);
			countCommonBuilder.setPageSize(pageSize);
			String queryTrsl = builder.asTRSL();
			String countTrsl = countBuilder.asTRSL();
			queryCommonBuilder.filterByTRSL(queryTrsl);
			countCommonBuilder.filterByTRSL(countTrsl);
			String metas = specialProject.getSource();
			String[] data = null;

			if(!"ALL".equals(metas)){

				String[] split = metas.split(";");
				//网友情绪
				if (chartType.equals(ChartType.USERVIEWS.getType())){
					List<String> choices = Arrays.asList(split);
					List<String> whole = Arrays.asList(Const.TYPE_NEWS.split(";"));
					//取交集
					List<String> result = whole.stream().filter(item -> choices.contains(item)).collect(toList());
					split = result.toArray(new String[result.size()]);
					metas = StringUtil.join(split,";");
				}else if (chartType.equals(ChartType.NEWSSITEANALYSIS.getType())){//新闻传播分析
					metas = "国内新闻*";
				}
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
					queryCommonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, metas, Operator.Equal);
					countCommonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, metas, Operator.Equal);
				}
			}else {
				data = Const.MIX_DATABASE.split(";");
			}
			String[] timeArr = com.trs.netInsight.util.DateUtil.formatTimeRange(timeRange);
			String time0 = timeArr[0];
			if ((chartType.equals(ChartType.WORDCLOUD.getType()) && com.trs.netInsight.util.DateUtil.isExpire("2019-10-01 00:00:00",time0))|| chartType.equals(ChartType.USERVIEWS.getType())|| chartType.equals(ChartType.NEWSSITEANALYSIS.getType())){
				queryCommonBuilder.setDatabase(new String[]{Const.HYBASE_NI_INDEX});
				countCommonBuilder.setDatabase(new String[]{Const.HYBASE_NI_INDEX});
			}else {
				queryCommonBuilder.setDatabase(data);
				countCommonBuilder.setDatabase(data);
			}

			switch (sort) { // 排序
				case "desc":
					queryCommonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countCommonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					queryCommonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countCommonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;

				default:
					if (weight) {
						queryCommonBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						countCommonBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						queryCommonBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						countCommonBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}

			obj = infoListService.getDocListContrast(queryCommonBuilder, loginUser, sim, irSimflag,irSimflagAll,"special");
			if (isExport) {// 是导出 转为混合实体存入redis
				InfoListResult statusResult = (InfoListResult) obj;
				PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) statusResult.getContent();
				List<FtsDocumentCommonVO> list = content.getPageItems();
				voList = list;
			}
		}
		if (isExport && voList.size() > 0) {
			// 存入缓存 以便混合列表导出时使用
			RedisUtil.setMix(specialProject.getId(), voList);
		}
		return obj;
	}
	private Object getHotWordExploreList(SpecialProject specialProject, String industryType, String area,String chartType, String xType,
										 String source, String sort, String emotion, String fuzzyValue, int pageNo, int pageSize, boolean isExport,
										 String thirdWord) throws TRSException {
		QueryBuilder builder = new QueryBuilder();
		QueryBuilder countBuilder = new QueryBuilder();
		builder.setPageNo(pageNo);
		builder.setPageSize(pageSize);
		countBuilder.setPageNo(pageNo);
		countBuilder.setPageSize(pageSize);
		User loginUser = UserUtils.getUser();
		boolean sim = specialProject.isSimilar();
		boolean irSimflag = specialProject.isIrSimflag();
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		boolean server = specialProject.isServer();
		boolean weight = specialProject.isWeight();
		Object obj = null;
		List<FtsDocumentCommonVO> voList = new ArrayList<>();
		// 是否查server
		builder.setServer(server);
		countBuilder.setServer(server);
		builder.setDatabase(Const.HYBASE_NI_INDEX);
		countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		//并上专题建时的排除词
		String excludeWords = specialProject.getExcludeWords();
		String[] excludeWordsArray = excludeWords.split(";");
		if(excludeWordsArray.length > 0 && StringUtil.isNotEmpty(excludeWords)){
			builder.filterField(FtsFieldConst.FIELD_CONTENT, excludeWordsArray, Operator.NotEqual);
			builder.filterField(FtsFieldConst.FIELD_TITLE, excludeWordsArray, Operator.NotEqual);
			countBuilder.filterField(FtsFieldConst.FIELD_CONTENT, excludeWordsArray, Operator.NotEqual);
			countBuilder.filterField(FtsFieldConst.FIELD_TITLE, excludeWordsArray, Operator.NotEqual);
		}
		//处理来源
		if ("ALL".equals(source)){
			source = specialProject.getSource();
			if (!"ALL".equals(source)){
				String[] split = source.split(";");
				List<String> choices = Arrays.asList(split);
				List<String> whole = Arrays.asList(Const.TYPE_NEWS.split(";"));
				//取交集
				List<String> result = whole.stream().filter(item -> choices.contains(item)).collect(toList());
				split = result.toArray(new String[result.size()]);
				source = StringUtil.join(split,";");
			}else {
				source = Const.TYPE_NEWS;
			}
		}
		builder.filterField(FtsFieldConst.FIELD_GROUPNAME,source.replace("境外媒体","国外新闻").split(";"),Operator.Equal);
		countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,source.replace("境外媒体","国外新闻").split(";"),Operator.Equal);

		//排除网站
		String excludeWeb = specialProject.getExcludeWeb();
		if (StringUtil.isNotEmpty(excludeWeb)) {
			builder.filterField(FtsFieldConst.FIELD_SITENAME, excludeWeb.replaceAll(";|；", " OR "), Operator.NotEqual);
			countBuilder.filterField(FtsFieldConst.FIELD_SITENAME, excludeWeb.replaceAll(";|；", " OR "), Operator.NotEqual);
		}
		if(StringUtil.isNotEmpty(emotion) && !"ALL".equals(emotion)){
			builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
		}

		String start = specialProject.getStart();
		String end = specialProject.getEnd();
		builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { start, end },
				Operator.Between);
		countBuilder.filterField(FtsFieldConst.FIELD_URLTIME,
				new String[] { start, end }, Operator.Between);
		if (!"ALL".equals(industryType)) {// 拼接行业类型
			builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industryType.split(";"), Operator.Equal);
		}

		for (ChartType type : ChartType.values()) {
			String[] xTpyeField = type.getXTpyeField();// 字段名
			if (type.getType().equals(chartType)) {
				if (xTpyeField.length > 0) {// 字段名不确定 长度
					StringBuffer sb = new StringBuffer();
					String trsl = "";
					for (int i = 0; i < xTpyeField.length; i++) {
						if ((i != xTpyeField.length - 1) && StringUtil.isNotEmpty(thirdWord)) {// 不是最后一个
							trsl = sb.append(xTpyeField[i]).append(":").append("(\"").append(xType).append("\" AND \"").append(thirdWord).append("\")")
									.append(" OR ").toString();
						} else {
							if(StringUtil.isNotEmpty(thirdWord)){
								trsl = sb.append(xTpyeField[i]).append(":").append("(\"").append(xType).append("\" AND \"").append(thirdWord).append("\")").toString();
							}else{
								trsl = sb.append(xTpyeField[i]).append(":").append("\"").append(xType).append("\"").toString();
							}
						}
					}
					builder.filterByTRSL(trsl);
					countBuilder.filterByTRSL(trsl);
				}
			}

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
			default:
				if (weight) {
					builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
				} else {
					builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					countBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
				}
				break;
		}
		log.info("HYBASE_NI_INDEX:" + builder.asTRSL());
		obj = infoListService.getDocList(builder, loginUser, sim, irSimflag,irSimflagAll,isExport,"special");
		if(isExport){//是导出  转为混合实体存入redis
			InfoListResult statusResult = (InfoListResult)obj;
			PagedList<FtsDocument> content = (PagedList<FtsDocument>)statusResult.getContent();
			List<FtsDocument> list = content.getPageItems();
			for(FtsDocument document:list){
				FtsDocumentCommonVO vo = new FtsDocumentCommonVO();
				vo.setTitle(document.getUrlTitle());
				vo.setContent(document.getContent());
				vo.setUrlName(document.getUrlName());
				vo.setGroupName(document.getGroupName());
				vo.setSiteName(document.getSiteName());
				vo.setUrlTime(document.getUrlTime());
				voList.add(vo);
			}
		}
		//InfoListResult
		if(isExport && voList.size()>0){
			//存入缓存  以便混合列表导出时使用
			RedisUtil.setMix(specialProject.getId(), voList);
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
		// 微博md5找钱10 原发
		searchBuilder.setPageSize(10);
		searchBuilder.setStartTime(specialProject.getStartTime());
		searchBuilder.setEndTime(specialProject.getEndTime());
		searchBuilder.filterByTRSL(FtsFieldConst.FIELD_RETWEETED_MID + ":(0 OR \"\")");
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);
		String formatStart = sdf.format(specialProject.getStartTime());
		String formatEnd = sdf.format(specialProject.getEndTime());
		searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { formatStart, formatEnd },
				Operator.Between);
		GroupResult categoryQuery = hybase8SearchService.categoryQuery(searchBuilder, sim, specialProject.isIrSimflag(),specialProject.isIrSimflagAll(),
				FtsFieldConst.FIELD_MD5TAG,"special", Const.WEIBO);
		List<GroupInfo> groupList = categoryQuery.getGroupList();
		List<ViewEntity> viewEntities = new ArrayList<>();
		for (GroupInfo groupInfo : groupList) {
			QueryBuilder builder = new QueryBuilder();
			builder.filterByTRSL(FtsFieldConst.FIELD_RETWEETED_MID + ":(0 OR \"\")");
			builder.filterByTRSL(FtsFieldConst.FIELD_MD5TAG + ":" + groupInfo.getFieldValue());
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			builder.setDatabase(Const.WEIBO);
			builder.page(0, 1);
			// 限制时间段微博最早入库时间 20170701
			builder.setStartTime(specialProject.getStartTime());
			builder.setEndTime(specialProject.getEndTime());
			builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { formatStart, formatEnd }, Operator.Between);
			List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(builder, FtsDocumentStatus.class, false,false,
					false,"special");
			if (ftsQuery != null && ftsQuery.size() > 0) {
				FtsDocumentStatus ftsStatuses = ftsQuery.get(0);
				ViewEntity viewEntity = new ViewEntity();
				viewEntity.setView(subView(ftsStatuses.getStatusContent(), 25));
				viewEntity.setCount(groupInfo.getCount());
				viewEntity.setId(ftsStatuses.getSid());
				if (ftsStatuses.getAppraise() == "" || ftsStatuses.getAppraise() == " "
						|| ftsStatuses.getAppraise() == null) {
					viewEntity.setAppraise("中性");
				} else {
					viewEntity.setAppraise(ftsStatuses.getAppraise());
				}
				viewEntities.add(viewEntity);
			}
		}
		return viewEntities;
	}

	@Override
	public Object getWordCloud(boolean server, String trsl, boolean sim, boolean irSimflag,boolean irSimflagAll, String entityType,
							   int limit,String type, String... data) throws  TRSSearchException {
		GroupResult result = new GroupResult();
		try {
			if ("keywords".equals(entityType)) {
				// 人物、地域、机构
				GroupResult people = hybase8SearchService.categoryQuery(server, trsl, sim, irSimflag,irSimflagAll,
						Const.PARAM_MAPPING.get("people"), limit,"special", data);
				GroupResult location = hybase8SearchService.categoryQuery(server, trsl, sim, irSimflag,irSimflagAll,
						Const.PARAM_MAPPING.get("location"), limit,"special", data);
				GroupResult agency = hybase8SearchService.categoryQuery(server, trsl, sim, irSimflag,irSimflagAll,
						Const.PARAM_MAPPING.get("agency"), limit,"special", data);
				GroupWordResult wordInfos = new GroupWordResult();
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

				wordInfos.sort();

				List<GroupWordInfo> groupWordList = wordInfos.getGroupList();
				List<GroupWordInfo> newGroupWordList = new ArrayList<>();
				if (groupWordList.size() > 50) {
					newGroupWordList = groupWordList.subList(0, 50);
				} else {
					newGroupWordList = groupWordList;
				}
				// 数据清理
				for (int i = 0; i < groupWordList.size(); i++) {
					String name = groupWordList.get(i).getFieldValue();
					if (name.endsWith("html")) {
						groupWordList.remove(i);
						break;
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
				wordInfos.setGroupList(newGroupWordList);
				return wordInfos;
			} else {
				result = hybase8SearchService.categoryQuery(server, trsl, sim, irSimflag,irSimflagAll,
						Const.PARAM_MAPPING.get(entityType), limit,"special",data);
			}

			List<GroupInfo> list = result.getGroupList();
			// 数据清理
			for (int i = 0; i < list.size(); i++) {
				String name = list.get(i).getFieldValue();
				if (name.endsWith("html")) {
					list.remove(i);
					break;
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
				list.get(i).setFieldValue(name);
			}
		} catch (TRSSearchException e) {
			throw new TRSSearchException(e);
		}
		return result;
	}

	@Override
	public Object getVolume(QueryBuilder searchBuilder, String timerange, boolean sim, boolean irSimflag, boolean irSimflagAll, String showType)
			throws TRSException, TRSSearchException {
		String[] timeArray = DateUtil.formatTimeRange(timerange);
		String trsl = searchBuilder.asTRSL();
		String groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
		if (StringUtils.equals(showType, "day") && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "") > 0) {
			groupBy = FtsFieldConst.FIELD_URLDATE;
		} else {
			groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
		}
		List<String[]> list_time = new ArrayList<>();
		//判断数据展示格式，按天展示需要满足按天+时间超过24h
		if (StringUtils.equals(showType, "day") && timerange.contains("d") && !timerange.equals("0d")) {
			list_time.add(timeArray);
		} else if (StringUtils.equals(showType, "day") && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], "100") > 0) {
			list_time.add(timeArray);
		} else {
			if (StringUtils.equals(timerange, "0d")) {
				list_time.add(timeArray);
			} else {
				timeArray = DateUtil.getTimeToSevenDay(timeArray[0],timeArray[1]);
				list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]); //每天的起止时间  yyyyMMddHHmmss格式
			}
		}
		List<Map<String, Object>> listAll = new ArrayList<>();//最终结果集

		for (int i = 0; i < list_time.size(); i++) {//有几个时间段就判断几次
			String[] arrays = list_time.get(i);
			Map<String, GroupResult> map = new HashMap<>();
			GroupResult categoryQuery = null;
			for (String appraise : Const.APPRAISE) {
				QueryBuilder builder = new QueryBuilder();
				builder.setServer(searchBuilder.isServer());
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
				builder.filterField(FtsFieldConst.FIELD_URLTIME, arrays, Operator.Between);
				// 根据时间 按照天分类统计 还是 小时分类统计
				log.info(builder.asTRSL());
				if (FtsFieldConst.FIELD_URLDATE.equals(groupBy)) {
//					categoryQuery = hybase8SearchService.categoryQuery(builder, sim, irSimflag, irSimflagAll,
//							groupBy,"special", Const.MIX_DATABASE);
					categoryQuery = commonListService.categoryQuery(builder, sim, irSimflag, irSimflagAll,groupBy,"special",Const.ALL_GROUP);
				} else {//区分按天还是按小时
					// 自定义时间也走这个
//					categoryQuery = hybase8SearchService.categoryQuery(builder, sim, irSimflag, irSimflagAll, groupBy,"special",
//							Const.MIX_DATABASE);
					categoryQuery = commonListService.categoryQuery(builder, sim, irSimflag, irSimflagAll,groupBy,"special",Const.ALL_GROUP);
				}
				map.put(appraise, categoryQuery);
			}
			String start = null;
			String end = null;
			List<String> dateList = null;
			List<String> dateResult = null;
			if (FtsFieldConst.FIELD_URLDATE.equals(groupBy)) {
				start = timeArray[0].substring(0, 4) + "/" + timeArray[0].substring(4, 6) + "/"
						+ timeArray[0].substring(6, 8);
				end = timeArray[1].substring(0, 4) + "/" + timeArray[1].substring(4, 6) + "/"
						+ timeArray[1].substring(6, 8);
				dateList = DateUtil.getBetweenDateString(start, end, "yyyy/MM/dd");
				dateResult = dateList;
			} else {//时间展示格式  和查询结果的格式类型
				dateResult = DateUtil.getStartToEndOfHour(arrays[0], arrays[1]);//一天之内的所有小时  ，yyyy/MM/dd HH:00格式
				dateList = DateUtil.getHourOfHH(arrays[0], arrays[1]); //一天之内的所有小时  ，HH格式 //下面的代码中主要为了取开始与结束时间
			}
			/*// 今天和24h都是小时
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
			}*/
			for (int j = 0; j < dateList.size(); j++) {
				String date = dateList.get(j);
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
						if (info.getFieldValue().equals(date)) {
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
				mapDate.put("time", dateResult.get(j));
				// mapDate.put("", value)
				mapDate.put("appraise", mapSum);
				listAll.add(mapDate);
			}
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
				FtsDocumentStatus.class, sim, false,false,"special");
		return pagedList;
	}

	@Override
	public SpreadNewsEntity pathByNews(SpecialProject project, QueryBuilder builder, SpreadNewsEntity root,
									   String[] timeArray, boolean irSimflag,boolean irSimflagAll) throws Exception {

		// 再次确认root节点
		root = checkRoot(project, builder, root, project.isSimilar());
		computeSpreadNewsNodes(builder.asTRSL(), root, project.isSimilar(), irSimflag,irSimflagAll, project.isServer());
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
	private void computeSpreadNewsNodes(String trsl, SpreadNewsEntity father, boolean isSimilar, boolean irSimflag,boolean irSimflagAll,
										boolean server) throws TRSSearchException, TRSException {
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
			change.setServer(server);
			list = hybase8SearchService.ftsQuery(change, SpreadNewsEntity.class, isSimilar, irSimflag,irSimflagAll,"special");
			System.out.println(change.asTRSL());
			if (list != null && list.size() > 0) {
				for (SpreadNewsEntity spreadNewsEntity : list) {
					computeSpreadNewsNodes(trsl, spreadNewsEntity, isSimilar, irSimflag,irSimflagAll, server);
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
			builder.setServer(project.isServer());
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
				int index = 0;
				for (int i = 0; i < split.length; i++) {
					if (split[i].length() > split[index].length()) {
						index = i;
					}
				}
				content = split[index];
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
			//超过8个点 ，只展示8个  2020-01-06
			if (size > 8) {
				list = list.subList(size - 8, size);
			}
			for (String time : list) {
				Map<String, Object> mapData = new HashMap<>();
				List<GroupInfo> listData = new ArrayList<>();
				// wxb白名单的数据
				QueryBuilder builderWxb = new QueryBuilder(0, 1000);
				builderWxb.filterByTRSL(searchBuilder.asTRSL());
				builderWxb.filterField(FtsFieldConst.FIELD_URLDATE, time, Operator.Equal);
				builderWxb.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻", Operator.Equal);
				builderWxb.filterField(FtsFieldConst.FIELD_WXB_LIST, "0", Operator.Equal);
//				GroupResult resultWxb = hybase8SearchService.categoryQuery(builderWxb, similar, irSimflag,irSimflagAll,
//						FtsFieldConst.FIELD_SITENAME,"special", searchBuilder.getDatabase());
				GroupResult resultWxb = null;
				try {
					resultWxb = commonListService.categoryQuery(builderWxb, similar, irSimflag,irSimflagAll,
							FtsFieldConst.FIELD_SITENAME,"special");
				} catch (TRSException e) {
					e.printStackTrace();
				}
				if (resultWxb != null) {
					listData.addAll(resultWxb.getGroupList());
				}
				// 非wxb白名单的数据
				QueryBuilder builderNotWxb = new QueryBuilder(0, 1000);
				builderNotWxb.filterByTRSL(searchBuilder.asTRSL());
				builderNotWxb.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内新闻", Operator.Equal);
				builderNotWxb.filterField(FtsFieldConst.FIELD_URLDATE, time, Operator.Equal);
				builderNotWxb.filterField(FtsFieldConst.FIELD_WXB_LIST, "0", Operator.NotEqual);
//				GroupResult resultNotWxb = hybase8SearchService.categoryQuery(builderNotWxb, similar, irSimflag,irSimflagAll,
//						FtsFieldConst.FIELD_SITENAME,"special" ,searchBuilder.getDatabase());
				GroupResult resultNotWxb = null;
				try {
					resultNotWxb = commonListService.categoryQuery(builderNotWxb, similar, irSimflag,irSimflagAll,
							FtsFieldConst.FIELD_SITENAME,"special");
				} catch (TRSException e) {
					e.printStackTrace();
				}
				if (resultNotWxb != null) {
					listData.addAll(resultNotWxb.getGroupList());
				}
				mapData.put("count", listData.size());
				// 取 maxLength 个
				if ( !isApi){
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
	public HashMap<String, Object> getUserViewsData(SpecialProject specialProject, String timeRange,
													String industry, String area, SpecialParam specParam) throws Exception {
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
		boolean sim = specialProject.isSimilar();
		String groupName = specialProject.getSource();
		//来源处理
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
		}

		HashMap<String, Object> result = new HashMap<>();
		HashMap<String, Object> resultData = new HashMap<>();
		try {
			searchBuilder.filterByTRSL(" (IR_EMOTION:(\"怒\" OR \"恶\" OR \"惧\" OR \"喜\" OR \"哀\"))");
			searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);

			String posCatTrsl = searchBuilder.asTRSL();
//			GroupResult posResult = hybase8SearchService.categoryQuery(specialProject.isServer(), posCatTrsl, sim, specialProject.isIrSimflag(),specialProject.isIrSimflagAll(),
//					FtsFieldConst.FIELD_EMOTION, 5, "special",Const.HYBASE_NI_INDEX);
			searchBuilder.setPageSize(5);
			GroupResult posResult = commonListService.categoryQuery(searchBuilder,sim, specialProject.isIrSimflag(),specialProject.isIrSimflagAll(),FtsFieldConst.FIELD_EMOTION,"special",Const.TYPE_NEWS);

			userViewResultEcapsulation(resultData, posResult);
			//还需构建param，供前端点击跳转使用
			ChartParam chartParam = new ChartParam(specialProject.getId(), timeRange, industry, area,
					ChartType.USERVIEWS.getType(), specParam.getFirstName(), specParam.getSecondName(),
					specParam.getThirdName(), "无锡");
			result.put("data", resultData);
			result.put("param", chartParam);
		} catch (Exception e) {
			throw new OperationException("网友观点计算错误,message: ", e);
		}
		return result;
	}

	@Override
	public ByteArrayOutputStream exportBarOrPieData(String dataType,JSONArray array) throws IOException {
		ExcelData content = new ExcelData();
		if ("情感分析".equals(dataType)){
			content.setHead(ExcelConst.EMOTION_DATA);  // { "情感", "数值"}
		}else if ("引爆点".equals(dataType)){
			content.setHead(ExcelConst.DETONATE_POINT); // { "引爆用户", "二次转发数"}
		}else if ("网站".equals(dataType)){
			content.setHead(ExcelConst.WEBSITE);   // { "网站", "信息数量"}
		}else if ("新闻".equals(dataType)){
			content.setHead(ExcelConst.XINWEN);   // { "网站", "信息数量"}
		}else if ("客户端".equals(dataType)){
			content.setHead(ExcelConst.KEHUDUAN);  // { "客户端", "信息数量"}
		}else if ("微博".equals(dataType)){
			content.setHead(ExcelConst.WEIBO);   // { "网站", "信息数量"}
		}else if ("微信".equals(dataType)){
			content.setHead(ExcelConst.WEIXIN);  // { "客户端", "信息数量"}
		}else if ("论坛".equals(dataType)){
			content.setHead(ExcelConst.LUNTAN);   // { "网站", "信息数量"}
		}else if ("博客".equals(dataType)){
			content.setHead(ExcelConst.BOKE);  // { "客户端", "信息数量"}
		}else if ("电子报".equals(dataType)){
			content.setHead(ExcelConst.DIANZIBAO);   // { "网站", "信息数量"}
		}else if ("境外网站".equals(dataType)){
			content.setHead(ExcelConst.JINGWAIWANGZHAN);  // { "客户端", "信息数量"}
		}else if ("Twitter".equals(dataType)){
			content.setHead(ExcelConst.TWITTER);   // { "网站", "信息数量"}
		}else if ("FaceBook".equals(dataType)){
			content.setHead(ExcelConst.FACEBOOK);  // { "客户端", "信息数量"}
		}else {
			content.setHead(ExcelConst.HEAD_PIE_BAR);  // { "媒体来源", "数值"}
		}
		for (Object object : array) {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
			String groupNameValue  = "";
			String numValue = "";
			if ("情感分析".equals(dataType)){
				groupNameValue = parseObject.get("name").toString();
				numValue = parseObject.get("value").toString();
			}else {
				groupNameValue = parseObject.get("fieldValue").toString();
				numValue = parseObject.get("count").toString();
			}


			if("国内新闻".equals(groupNameValue)){
				groupNameValue = "新闻";
			}else if("国内新闻_电子报".equals(groupNameValue)){
				groupNameValue = "电子报";
			}else if("国内新闻_手机客户端".equals(groupNameValue)){
				groupNameValue = "客户端";
			}else if("国内论坛".equals(groupNameValue)){
				groupNameValue = "论坛";
			}else if("国内微信".equals(groupNameValue)){
				groupNameValue = "微信";
			}else if("国内博客".equals(groupNameValue)){
				groupNameValue = "博客";
			}
			content.addRow(groupNameValue, numValue);
		}
		return ExcelFactory.getInstance().export(content);
	}

	@Override
	public ByteArrayOutputStream exportChartLineData(String dataType,JSONArray array) throws IOException {
		ExcelData data = new ExcelData();
		//单独循环设置表头
		String[] header = null;
		String headArray = dataType;
		for (Object object : array) {

			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
			String groupName = "";
			if ("情感走势".equals(dataType)){
				groupName = parseObject.get("appraise").toString();
			}else {
				groupName = parseObject.get("groupName").toString();
				if("国内新闻".equals(groupName)){
					groupName = "新闻";
				}else if("国内新闻_电子报".equals(groupName)){
					groupName = "电子报";
				}else if("国内新闻_手机客户端".equals(groupName)){
					groupName = "客户端";
				}else if("国内论坛".equals(groupName)){
					groupName = "论坛";
				}else if("国内微信".equals(groupName)){
					groupName = "微信";
				}else if("国内博客".equals(groupName)){
					groupName = "博客";
				}
			}
			if (StringUtil.isNotEmpty(groupName)){
				headArray += "," + groupName;
			}
		}
		header = headArray.split(",");
		data.setHead(header);

		List<String[]> arrayList = new ArrayList<>();
		for (int i = 0;i < array.size(); i++) {

			JSONObject parseObject = JSONObject.parseObject(String.valueOf(array.get(i)));

			String timeAndCount = parseObject.get("data").toString();
			JSONArray timeAndCountArray = JSONObject.parseArray(timeAndCount);
			for (int j = 0; j < timeAndCountArray.size(); j++) {
				String[] arr = null;
				if (0==i){
					arr = new String[array.size()+1];
				}else{
					arr = arrayList.get(j);
				}
				Object o = timeAndCountArray.get(j);
				JSONObject parseObject2 = JSONObject.parseObject(String.valueOf(o));
				if (0==i){
					arr[i] = parseObject2.get("fieldValue").toString();
					arr[i+1] = parseObject2.get("count").toString();
					arrayList.add(arr);
				}else {
					arr[i+1] = parseObject2.get("count").toString();
				}
			}

		}

		for (String[] strings : arrayList) {
			data.addRow(strings);
		}
		return ExcelFactory.getInstance().export(data);
	}

	@Override
	public ByteArrayOutputStream exportWordCloudData(String dataType,JSONArray array) throws IOException {
		ExcelData content = new ExcelData();
		content.setHead(ExcelConst.HEAD_WORDCLOUD); // {"词语", "所属分组", "信息数量"}
		for (Object object : array) {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));

			String word = parseObject.get("fieldValue").toString();
			String count = parseObject.get("count").toString();
			String group = "";
			if ("通用".equals(dataType)){
				group = parseObject.get("entityType").toString();
			}

			if("地域".equals(dataType) || "location".equals(group)){
				group = "地域";
			}else if("机构".equals(dataType) || "agency".equals(group)){
				group = "机构";
			}else{
				group = "人物";
			}

			content.addRow(word, group, count);

//			String groupList = parseObject.get("groupList").toString();
//			JSONArray dataArray = JSONObject.parseArray(groupList);
//			for (Object data : dataArray) {
//				JSONObject jSONObject = JSONObject.parseObject(String.valueOf(data));
//				String word = jSONObject.get("fieldValue").toString();
//				String count = jSONObject.get("count").toString();
//				String group = jSONObject.get("entityType").toString();
//				content.addRow(word, group, count);
//			}


		}
		return ExcelFactory.getInstance().export(content);
	}

	@Override
	public ByteArrayOutputStream exportMapData(JSONArray array) throws IOException {
		ExcelData content = new ExcelData();
		content.setHead(ExcelConst.HEAD_MAP); // { "地域", "信息数量"};
		for (Object object : array) {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));

			String areaName = parseObject.get("area_name").toString();
			String areaCount = parseObject.get("area_count").toString();
			content.addRow(areaName, areaCount);
		}
		return ExcelFactory.getInstance().export(content);
	}
	/***
	 * 网友观点结果集封装
	 * @param result	结果集
	 * @param posAndNegResult	情绪数据
	 */
	private void userViewResultEcapsulation(HashMap<String, Object> result, GroupResult posAndNegResult) {
		ArrayList<HashMap<String, Object>> nodes = new ArrayList<>();
		ArrayList<HashMap<String, Object>> links = new ArrayList<>();
		List<GroupInfo> posAndNegGroupList = posAndNegResult.getGroupList();
		long totalCount = posAndNegGroupList.stream().collect(Collectors.summingLong(GroupInfo::getCount));
		HashMap<String, Object> node1 = new HashMap<>();
		node1.put("name", "总量");
		node1.put("value", totalCount);
		nodes.add(node1);

		for(GroupInfo groupInfo : posAndNegGroupList){
			HashMap<String, Object> secTemp = new HashMap<>();
			secTemp.put("name", groupInfo.getFieldValue());
			secTemp.put("value", groupInfo.getCount());
			nodes.add(secTemp);

			HashMap<String, Object> posAndNegMap = new HashMap<>();
			posAndNegMap.put("source", "总量");
			posAndNegMap.put("target", groupInfo.getFieldValue());
			posAndNegMap.put("value", groupInfo.getCount());
			links.add(posAndNegMap);
		}
		List<String> emotions = Arrays.asList("喜", "怒", "哀", "惧", "恶");
//		for(String fieldValue : emotions){
//			HashMap<String, Object> emotionMap = new HashMap<>();
//			emotionMap.put("name", fieldValue);
//			long negCount = negResult.getGroupList().stream().filter(e -> fieldValue.equals(e.getFieldValue())).map(f -> f.getCount()).collect(Collectors.summingLong(value -> value));
//			long posCount = posResult.getGroupList().stream().filter(e -> fieldValue.equals(e.getFieldValue())).map(f -> f.getCount()).collect(Collectors.summingLong(value -> value));
//			long midCount = midResult.getGroupList().stream().filter(e -> fieldValue.equals(e.getFieldValue())).map(f -> f.getCount()).collect(Collectors.summingLong(value -> value));
//			emotionMap.put("value", posCount + negCount + midCount);
//			nodes.add(emotionMap);
//		}
		result.put("nodes", nodes);
		result.put("links", links);
	}
	/***
	 * 网友观点结果集封装
	 * @param result	结果集
	 * @param posAndNegResult	正负面数据
	 * @param posResult			情绪对应的正面数据
	 * @param negResult			情绪对应的负面数据
	 * @param midResult			情绪对应的中性数据
	 */
	private void userViewResultEcapsulation(HashMap<String, Object> result, GroupResult posAndNegResult, GroupResult posResult, GroupResult negResult, GroupResult midResult,long midMaxCount) {
		ArrayList<HashMap<String, Object>> nodes = new ArrayList<>();
		ArrayList<HashMap<String, Object>> links = new ArrayList<>();
		List<GroupInfo> posAndNegGroupList = posAndNegResult.getGroupList();
		long totalCount = posAndNegGroupList.stream().collect(Collectors.summingLong(GroupInfo::getCount));
		HashMap<String, Object> node1 = new HashMap<>();
		node1.put("name", "总量");
		node1.put("value", totalCount + midMaxCount);
		nodes.add(node1);
		if (midMaxCount > 0){
			GroupInfo groupInfo = new GroupInfo("中性",midMaxCount);
			int i = 0;
			for(GroupInfo g:posAndNegGroupList){
				if(!"中性".equals(g.getFieldValue())){
					i++;
				}
			}
			if(i == posAndNegGroupList.size()){//都不等于
				posAndNegGroupList.add(groupInfo);
			}
		}
		for(GroupInfo groupInfo : posAndNegGroupList){
			HashMap<String, Object> secTemp = new HashMap<>();
			secTemp.put("name", groupInfo.getFieldValue());
			secTemp.put("value", groupInfo.getCount());
			nodes.add(secTemp);

			HashMap<String, Object> posAndNegMap = new HashMap<>();
			posAndNegMap.put("source", "总量");
			posAndNegMap.put("target", groupInfo.getFieldValue());
			posAndNegMap.put("value", groupInfo.getCount());
			links.add(posAndNegMap);
		}
		for(GroupInfo posInfo : posResult.getGroupList()){
			String fieldValue = posInfo.getFieldValue();
			long fieldCount = posInfo.getCount();
			HashMap<String, Object> posMap= new HashMap<>();
			posMap.put("source", "正面");
			posMap.put("target", fieldValue);
			posMap.put("value", fieldCount);
			links.add(posMap);
		}
		for(GroupInfo negInfo : negResult.getGroupList()){
			HashMap<String, Object> negMap= new HashMap<>();
			negMap.put("source", "负面");
			negMap.put("target", negInfo.getFieldValue());
			negMap.put("value", negInfo.getCount());
			links.add(negMap);
		}
		for(GroupInfo midInfo : midResult.getGroupList()){
			HashMap<String, Object> midMap= new HashMap<>();
			midMap.put("source", "中性");
			midMap.put("target", midInfo.getFieldValue());
			midMap.put("value", midInfo.getCount());
			links.add(midMap);
		}
		List<String> emotions = Arrays.asList("喜", "怒", "哀", "惧", "恶");
		for(String fieldValue : emotions){
			HashMap<String, Object> emotionMap = new HashMap<>();
			emotionMap.put("name", fieldValue);
			long negCount = negResult.getGroupList().stream().filter(e -> fieldValue.equals(e.getFieldValue())).map(f -> f.getCount()).collect(Collectors.summingLong(value -> value));
			long posCount = posResult.getGroupList().stream().filter(e -> fieldValue.equals(e.getFieldValue())).map(f -> f.getCount()).collect(Collectors.summingLong(value -> value));
			long midCount = midResult.getGroupList().stream().filter(e -> fieldValue.equals(e.getFieldValue())).map(f -> f.getCount()).collect(Collectors.summingLong(value -> value));
			emotionMap.put("value", posCount + negCount + midCount);
			nodes.add(emotionMap);
		}
		result.put("nodes", nodes);
		result.put("links", links);
	}

	@Override
	public List<Map<String, String>> emotionOption(QueryBuilder searchBuilder, SpecialProject specialProject) {
		List<Map<String, String>> list=new ArrayList<>();
		boolean sim = specialProject.isSimilar();
		boolean irSimflag = specialProject.isIrSimflag();
		boolean irSimflagAll = specialProject.isIrSimflagAll();
		//微博情感分析 -->  情感分析 ！！！按专题分析创建的来源统计  20200107
		String groupName = specialProject.getSource();
		String[] groupNames = StringUtil.isNotEmpty(groupName)?groupName.split(";"):null;
		String[] database = TrslUtil.chooseDatabases(groupNames);
		if (ObjectUtil.isEmpty(database)){
			return null;
		}
		if (StringUtil.isNotEmpty(groupName)) {
			searchBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName.replace(";", " OR ")
					.replace(Const.TYPE_WEIXIN, Const.TYPE_WEIXIN_GROUP).replace("境外媒体", "国外新闻"),Operator.Equal);
		}
		String trsl = searchBuilder.asTRSL();
		log.info(trsl);
//		GroupResult records = hybase8SearchService.categoryQuery(specialProject.isServer(), trsl, sim, irSimflag,irSimflagAll,
//				ESFieldConst.IR_APPRAISE, 3, "special",database);
		searchBuilder.setPageSize(3);
		//todo
		ChartResultField chartResultField = new ChartResultField("name","num");
		List<Map<String, Object>> list1 = null;
		try {
			list1 = (List<Map<String, Object>>) commonChartService.getPieColumnData(searchBuilder,sim,irSimflag,irSimflagAll,groupName,"",ESFieldConst.IR_APPRAISE,"special",chartResultField);
		} catch (TRSException e) {
			e.printStackTrace();
		}
		trsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_APPRAISE).append(":(").append("正面")
				.append(" OR ").append("负面").append(")").toString();
		searchBuilder = new QueryBuilder();
		searchBuilder.filterByTRSL(trsl);

		String join = StringUtil.join(database, ";");
		searchBuilder.setDatabase(join);

//		long ftsCount = hybase8SearchService.ftsCount(searchBuilder, sim, irSimflag,irSimflagAll,"special" );
		long ftsCount = 0;
		try {
			ftsCount = commonListService.ftsCount(searchBuilder,sim,irSimflag,irSimflagAll,"special");
		} catch (TRSException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < list1.size(); i++) {
			Map<String, String> map = new HashMap<String, String>();
			String fieldValue = (String) list1.get(i).get("name");
			if ("负面".equals(fieldValue) || "正面".equals(fieldValue)) {
				map.put("name",(String) list1.get(i).get("name"));
				map.put("value", String.valueOf(list1.get(i).get("num")));
			} else {
				map.put("name", "中性");
				map.put("value", String.valueOf(ftsCount));
			}
			list.add(map);
		}
//		List<GroupInfo> groupList = records.getGroupList();
//		for (GroupInfo group : groupList) {
//			Map<String, String> map = new HashMap<String, String>();
//			String fieldValue = group.getFieldValue();
//			if ("负面".equals(fieldValue) || "正面".equals(fieldValue)) {
//				map.put("name", group.getFieldValue());
//				map.put("value", String.valueOf(group.getCount()));
//			} else {
//				map.put("name", "中性");
//				map.put("value", String.valueOf(ftsCount));
//			}
//			list.add(map);
//		}

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
		}
		if (!keys.contains("负面")) {
			Map<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("name", "负面");
			hashMap.put("value", "0");
			list.add(hashMap);
		}
		if(!keys.contains("中性")){
			Map<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("name", "中性");
			hashMap.put("value", String.valueOf(ftsCount));
			list.add(hashMap);

		}
		return list;
	}

	@Override
	public List<ClassInfo> ordinarySearchstatistics(boolean sim, boolean irSimflag,boolean irSimflagAll,String keywords,String[] timeRange, String source,String keyWordIndex,Boolean weight,String searchType) throws TRSException{
		List<ClassInfo> list = new ArrayList<>();

		try {
			QueryBuilder builder = new QueryBuilder();
			builder.setPageNo(0);
			builder.setPageSize(20);
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

			if (childBuilder.length() > 0) {
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
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
				}
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
			GroupResult categoryQuery = hybase8SearchService.categoryQuery(builder, sim, irSimflag,irSimflagAll,
					FtsFieldConst.FIELD_GROUPNAME, null,Const.MIX_DATABASE);
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
