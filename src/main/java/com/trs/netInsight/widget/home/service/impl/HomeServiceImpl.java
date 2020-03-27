package com.trs.netInsight.widget.home.service.impl;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.trs.netInsight.widget.analysis.entity.ClassInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.dc.entity.TRSEsSearchParams;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.cache.TimingCachePool;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.ESDocumentNew;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.MapUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.chart.BrokenLineUtil;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.analysis.entity.ChartAnalyzeEntity;
import com.trs.netInsight.widget.analysis.entity.MBlogAnalyzeEntity;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.entity.Columns;
import com.trs.netInsight.widget.column.repository.ColumnRepository;
import com.trs.netInsight.widget.home.entity.enums.ColumnType;
import com.trs.netInsight.widget.home.entity.enums.TabType;
import com.trs.netInsight.widget.home.service.IHomeService;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 首页
 *
 * Created by trs on 2017/6/19.
 */
@Service
@Slf4j
public class HomeServiceImpl implements IHomeService {

	@Autowired
	private ColumnRepository columnRepository;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private FullTextSearch esSearchService;

	@Autowired
	private ChartAnalyzeService chartAnalyzeService;

	@Autowired
	private SpecialProjectRepository specialProjectRepository;

	@Autowired
	private AlertRepository alertRepository;

	@Autowired
	private IDistrictInfoService districtInfoService;

	@Autowired
	private IAlertService alertService;

	private static String CURRENT_WEEK = "本周";

	private static String CURRENT_MONTH = "本月";

	private static String CURRENT_DAY = "本日";

	private static String TIME_SUFFIX = "000000";

	@Override
	public Object hsInfo(String keywords, int start, int end, String[] timeArray) throws TRSSearchException, TRSException {
		QueryBuilder query = new QueryBuilder();
		String trsl = "IR_KEYWORDS:" + keywords;
		query.filterByTRSL(trsl);
		query.page(start, end);
		query.setDatabase(Const.HYBASE_NI_INDEX);
		query.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
		query.orderBy("IR_URLTIME", true);
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(query, FtsDocument.class, true,false,false,null);
		return ftsQuery;
	}

	@Override
	public Object save(String columnName, ColumnType type, String tabKeywords, TabType tabType, int position,
			String keywords) throws TRSException {
		Columns columns = new Columns(columnName, type, tabKeywords, tabType, position, keywords);
		columnRepository.save(columns);
		return "保存成功";
	}

	// 热搜防止查询结果为空
	@Override
	public List<FtsDocument> notNull(String trsl, String timeRange, int end) throws TRSSearchException, TRSException {
		timeRange = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1)) + 5 + "d";
		int n = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder.filterByTRSL(trsl);
		queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
		String starthybase = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,null);
		String endhybase = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		log.warn("热搜由于数据来源百度  可能要多次查询hybase 本次耗时"
				+ (Integer.parseInt(endhybase.substring(8, 17)) - Integer.parseInt(starthybase.substring(8, 17)))
				+ "ms");
		// 为防止死循环 加30天限制
		if (n < 30) {
			String[] timeArray = DateUtil.formatTimeRange(timeRange);
			queryBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
			String start2 = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
			ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,null);
			String end2 = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
			log.warn("热搜由于数据来源百度  可能要多次查询hybase 本次耗时"
					+ (Integer.parseInt(end2.substring(8, 17)) - Integer.parseInt(start2.substring(8, 17))) + "ms");
			// 排重
			List<String> md5List = new ArrayList<>();
			List<FtsDocument> noMd5List = new ArrayList<>();
			for (FtsDocument ftsDocument : ftsQuery) {
				if (!md5List.contains(ftsDocument.getMd5Tag())) {
					md5List.add(ftsDocument.getMd5Tag());
					noMd5List.add(ftsDocument);
				}
			}
			ftsQuery = noMd5List;
			if (ObjectUtil.isEmpty(ftsQuery) || ftsQuery.size() < end) {
				notNull(trsl, timeRange, end);
			} else {
				return noImg(ftsQuery, trsl);
			}
		}
		log.info(queryBuilder.asTRSL());
		return ftsQuery;
	}

	/**
	 * 首页热搜列表
	 * 
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@Override
	public Object homeHsInfo(String keyword, int end, int start, String timeRange, String resultKeyword)
			throws TRSSearchException, TRSException {
		QueryBuilder queryBuilder = new QueryBuilder();
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		queryBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
		queryBuilder.setPageNo(start);
		queryBuilder.setPageSize(end);
		queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		queryBuilder.orderBy("IR_URLTIME", true);
		String trsl = "IR_URLTITLE:" + keyword + " OR IR_CONTENT:" + keyword + " OR IR_ABSTRACT:" + keyword
				+ " OR IR_KEYWORDS:" + keyword;
		queryBuilder.filterByTRSL(trsl);
		if (StringUtil.isNotEmpty(resultKeyword)) {
			String trs = "IR_URLTITLE:" + resultKeyword + " OR IR_ABSTRACT:" + resultKeyword + " OR IR_CONTENT:"
					+ resultKeyword + " OR IR_KEYWORDS:" + resultKeyword;
			queryBuilder.filterByTRSL(trs);
		}
		String starthybase = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,null);
		String endhybase = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		log.warn("首页热搜进入列表页  耗时"
				+ (Integer.parseInt(endhybase.substring(8, 17)) - Integer.parseInt(starthybase.substring(8, 17)))
				+ "ms");
		// 排重
		List<String> md5List = new ArrayList<>();
		List<FtsDocument> noMd5List = new ArrayList<>();
		for (FtsDocument ftsDocument : ftsQuery) {
			if (!md5List.contains(ftsDocument.getMd5Tag())) {
				md5List.add(ftsDocument.getMd5Tag());
				noMd5List.add(ftsDocument);
			}
		}
		// 过滤Img标签
		ftsQuery = noImg(noMd5List, ESFieldConst.IR_ABSTRACT);
		// 热搜是从百度取得 热搜防止空就行了
		if (ObjectUtil.isEmpty(ftsQuery) || ftsQuery.size() < end) {
			ftsQuery = notNull(trsl, timeRange, end);
		}
		return simCount(ftsQuery, "");
	}

	/**
	 * 首页未知探索
	 * 
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@Override
	public Object homeKnowInfo(String timeRange, int start, int end, String keyword, String resultKeyword)
			throws TRSSearchException, TRSException {
		QueryBuilder queryBuilder = new QueryBuilder();
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		queryBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
		queryBuilder.setPageNo(start);
		queryBuilder.setPageSize(end);
		queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		queryBuilder.orderBy("IR_URLTIME", true);
		// 未知探索
		String trsl = "IR_KEYWORDS:" + keyword;
		queryBuilder.filterByTRSL(trsl);
		// 结果中搜索
		if (StringUtil.isNotEmpty(resultKeyword)) {
			String trs = "IR_URLTITLE:" + resultKeyword + " OR IR_ABSTRACT:" + resultKeyword + " OR IR_CONTENT:"
					+ resultKeyword + " OR IR_KEYWORDS:" + resultKeyword;
			queryBuilder.filterByTRSL(trs);
			// end=100;
		}
		String starthybase = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,null);
		String endhybase = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		log.warn("未知探索 进入列表页 耗时"
				+ (Integer.parseInt(endhybase.substring(8, 17)) - Integer.parseInt(starthybase.substring(8, 17)))
				+ "ms");
		// 排重
		List<String> md5List = new ArrayList<>();
		List<FtsDocument> noMd5List = new ArrayList<>();
		label: while (ftsQuery.size() > 0) {
			for (FtsDocument ftsDocument : ftsQuery) {
				if (!md5List.contains(ftsDocument.getMd5Tag())) {
					md5List.add(ftsDocument.getMd5Tag());
					noMd5List.add(ftsDocument);
				}
			}
			if (noMd5List.size() >= queryBuilder.getPageSize()) {
				if (noMd5List.size() > queryBuilder.getPageSize()) {
					int size = noMd5List.size() - queryBuilder.getPageSize();
					for (int i = 0; i < size; i++) {
						noMd5List.remove(noMd5List.size() - 1);
					}

				}
				break label;
			}
			queryBuilder.setPageNo(queryBuilder.getPageNo() + 1);
			String start2 = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
			ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,null);
			String end2 = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
			log.warn("未知探索进入列表页 排重后由于条数不够再次查询hybase  耗时"
					+ (Integer.parseInt(end2.substring(8, 17)) - Integer.parseInt(start2.substring(8, 17))) + "ms");
		}

		// 过滤Img标签
		ftsQuery = noImg(noMd5List, ESFieldConst.IR_ABSTRACT);
		return simCount(ftsQuery, "");
	}

	/**
	 * 相似文章列表页 不排重
	 * 
	 * @param md5
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 */
	@Override
	public Object simList(String md5, int start, int end, String resultKeyword) throws TRSSearchException, TRSException {
		String trsl = "MD5TAG:(" + md5 + ")";
		QueryBuilder querySim = new QueryBuilder();
		// 结果中搜索
		if (StringUtil.isNotEmpty(resultKeyword)) {
			String trs = "IR_URLTITLE:" + resultKeyword + " OR IR_ABSTRACT:" + resultKeyword + " OR IR_CONTENT:"
					+ resultKeyword + " OR IR_KEYWORDS:" + resultKeyword;
			querySim.filterByTRSL(trs);
			// end=100;
		}
		querySim.page(start, end);
		querySim.filterByTRSL(trsl);
		querySim.setDatabase(Const.HYBASE_NI_INDEX);
		querySim.orderBy(ESFieldConst.IR_URLTIME, true);
		String starthybase = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		List<FtsDocument> ftsQuerySim = hybase8SearchService.ftsQuery(querySim, FtsDocument.class, true,false,false,null);
		String endhybase = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		log.warn("相似文章列表页  耗时"
				+ (Integer.parseInt(endhybase.substring(8, 17)) - Integer.parseInt(starthybase.substring(8, 17)))
				+ "ms");
		// 找相似文章数
		String sql = "MD5TAG:(" + ftsQuerySim.get(0).getMd5Tag() + ")";
		QueryBuilder query = new QueryBuilder();
		query.filterByTRSL(sql);
		query.setDatabase(Const.HYBASE_NI_INDEX);
		String s = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		long ftsCount = hybase8SearchService.ftsCount(query, true,false,false,null);
		if (ftsCount == 1L){
			ftsCount = 0L;
		}
		String e = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		log.warn("相似文章列表页 查相似文章数 耗时" + (Integer.parseInt(e.substring(8, 17)) - Integer.parseInt(s.substring(8, 17)))
				+ "ms");
		List<FtsDocument> simList = new ArrayList<>();
		for (FtsDocument document : ftsQuerySim) {
			document.setSim(ftsCount);
			simList.add(document);
		}
		return simList;
	}

	/**
	 * 首页地域列表
	 * 
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@Override
	public Object homeAreaInfo(String area, String timeRange, int start, int end, String groupName,
			String resultKeyword) throws TRSSearchException, TRSException {
		QueryBuilder queryBuilder = new QueryBuilder();
		// 热搜 先当天 搜不到就三天
		// 地域 三天
		if (StringUtil.isNotEmpty(area)) {
			String province = districtInfoService.province(area);
			// 地域只传一个 所以不用分割了
			area = "中国\\\\" + province + "*";
			queryBuilder.filterByTRSL("CATALOG_AREA:" + area);
		}
		// 结果中搜索
		String trsl = "";
		if (StringUtil.isNotEmpty(resultKeyword)) {
			trsl = "IR_URLTITLE:" + resultKeyword + " OR IR_ABSTRACT:" + resultKeyword + " OR IR_CONTENT:"
					+ resultKeyword + " OR IR_KEYWORDS:" + resultKeyword;
			queryBuilder.filterByTRSL(trsl);
		}
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		queryBuilder.setPageNo(start);
		queryBuilder.setPageSize(end);
		// 首页地域找来源
		if ("全部".equals(groupName)) {

		} else if (StringUtil.isNotEmpty(groupName)) {
			String trslgroup = "";
			if ("国内新闻_电子报".equals(groupName)) {
				// 单独拼国内新闻电子报表达式会报错 不知道为毛线
				trslgroup = "IR_GROUPNAME:" + groupName;
			} else {
				String[] split = groupName.split(";");
				for (int i = 0; i < split.length; i++) {
					if ("国内新闻_电子报".equals(split[i])) {
						split[i] = split[0];
						split[0] = "国内新闻_电子报";
					}
				}
				for (int j = 0; j < split.length; j++) {
					trslgroup += "(IR_GROUPNAME:" + split[j] + ") OR";
				}
				trslgroup = trslgroup.substring(0, trslgroup.length() - 3);
			}
			queryBuilder.filterByTRSL(trsl);
		}
		if (Const.MEDIA_TYPE_NEWS.contains(groupName) || StringUtil.isEmpty(groupName) || "全部".equals(groupName)) {
			queryBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
			queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
			log.info(queryBuilder.asTRSL());
			List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,null);
			// 过滤Img标签
			ftsQuery = noImg(ftsQuery, ESFieldConst.IR_ABSTRACT);
			return simCount(ftsQuery, resultKeyword);
		} else if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
			QueryBuilder weiboBuilder = new QueryBuilder();
			weiboBuilder.filterByTRSL("CATALOG_AREA:" + area);
			weiboBuilder.filterByTRSL(trsl);
			weiboBuilder.filterField(ESFieldConst.IR_CREATED_AT, timeArray, Operator.Between);
			weiboBuilder.setDatabase(Const.WEIBO);
			weiboBuilder.orderBy(ESFieldConst.IR_CREATED_AT, true);
			log.info(weiboBuilder.asTRSL());
			List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(weiboBuilder, FtsDocumentStatus.class,
					false,false,false,null);
			// 过滤Img标签
			ftsQuery = noImgWeiBo(ftsQuery, ESFieldConst.IR_ABSTRACT);
			return simCountWeibo(ftsQuery, resultKeyword);
		} else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
			QueryBuilder weixinBuilder = new QueryBuilder();
			weixinBuilder.filterByTRSL("CATALOG_AREA:" + area);
			weixinBuilder.filterByTRSL(trsl);
			weixinBuilder.filterField(ESFieldConst.IR_URLTIME, timeArray, Operator.Between);
			weixinBuilder.setDatabase(Const.WECHAT);
			weixinBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
			log.info(weixinBuilder.asTRSL());
			List<FtsDocumentWeChat> ftsQuery = hybase8SearchService.ftsQuery(weixinBuilder, FtsDocumentWeChat.class,
					false,false,false,null);
			// 过滤Img标签
			ftsQuery = noImgWeiXin(ftsQuery, ESFieldConst.IR_ABSTRACT);
			return simCountWeiXin(ftsQuery, resultKeyword);
		}
		return null;
	}

	/**
	 * 查询相似文章数 这个方法还得加两个 微信微博的
	 */
	@Override
	public List<FtsDocument> simCount(List<FtsDocument> ftsQuery, String resultKeyword) {
		// 如果在结果中搜索不为空 先过滤
		if (StringUtil.isNotEmpty(resultKeyword)) {
			ftsQuery = findInResult(ftsQuery, resultKeyword);
		}
		List<FtsDocument> list = new ArrayList<>();
		for (FtsDocument documents : ftsQuery) {
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			String md5Tag = documents.getMd5Tag();
			String trsl = "MD5TAG:" + md5Tag;
			queryBuilder.filterByTRSL(trsl);
			long ftsCount = hybase8SearchService.ftsCount(queryBuilder, true,false,false,null);
			if (ftsCount == 1L){
				ftsCount = 0L;
			}
			documents.setSim(ftsCount);
			list.add(documents);
		}
		return list;
	}

	/**
	 * 微博查相似文章数
	 */
	@Override
	public List<FtsDocumentStatus> simCountWeibo(List<FtsDocumentStatus> ftsQuery, String resultKeyword) {
		// 如果在结果中搜索不为空 先过滤
		if (StringUtil.isNotEmpty(resultKeyword)) {
			ftsQuery = findInResultWeiBo(ftsQuery, resultKeyword);
		}
		List<FtsDocumentStatus> list = new ArrayList<>();
		for (FtsDocumentStatus documents : ftsQuery) {
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.setDatabase(Const.WEIBO);
			String md5Tag = documents.getMd5Tag();
			String trsl = "MD5TAG:" + md5Tag;
			queryBuilder.filterByTRSL(trsl);
			long ftsCount = hybase8SearchService.ftsCount(queryBuilder, false,false,false,null);
			if (ftsCount == 1L){
				ftsCount = 0L;
			}
			documents.setSim((int) ftsCount);
			list.add(documents);
		}
		return list;
	}

	/**
	 * 微信查相似文章数
	 */
	@Override
	public List<FtsDocumentWeChat> simCountWeiXin(List<FtsDocumentWeChat> ftsQuery, String resultKeyword) {
		// 如果在结果中搜索不为空 先过滤
		if (StringUtil.isNotEmpty(resultKeyword)) {
			ftsQuery = findInResultWeiXin(ftsQuery, resultKeyword);
		}
		List<FtsDocumentWeChat> list = new ArrayList<>();
		for (FtsDocumentWeChat documents : ftsQuery) {
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.setDatabase(Const.WECHAT);
			String md5Tag = documents.getMd5Tag();
			String trsl = "MD5TAG:" + md5Tag;
			queryBuilder.filterByTRSL(trsl);
			long ftsCount = hybase8SearchService.ftsCount(queryBuilder, false,false,false,null);
			if (ftsCount == 1L){
				ftsCount = 0L;
			}
			documents.setSim((int) ftsCount);
			list.add(documents);
		}
		return list;
	}


	/**
	 * 在结果中搜索 标题 摘要 正文 关键词
	 * 
	 * @return
	 */
	@Override
	public List<FtsDocument> findInResult(List<FtsDocument> list, String keyWord) {
		List<FtsDocument> resultList = new ArrayList<>();
		for (FtsDocument document : list) {
			if (StringUtil.isNotEmpty(document.getTitle()) && document.getTitle().contains(keyWord)
					|| StringUtil.isNotEmpty(document.getContent()) && document.getContent().contains(keyWord)
					|| StringUtil.isNotEmpty(document.getAbstracts()) && document.getAbstracts().contains(keyWord)
					|| StringUtil.isNotEmpty(document.getAbstracts()) && document.getAbstracts().contains(keyWord)
					|| findInKeyword(document.getKeywords(), keyWord)) {
				resultList.add(document);
			}
		}
		return resultList;
	}

	/**
	 * 在结果中搜索 标题 摘要 正文 关键词
	 * 
	 * @return
	 */
	@Override
	public List<FtsDocumentStatus> findInResultWeiBo(List<FtsDocumentStatus> list, String keyWord) {
		List<FtsDocumentStatus> resultList = new ArrayList<>();
		for (FtsDocumentStatus document : list) {
			if (StringUtil.isNotEmpty(document.getStatusContent()) && document.getStatusContent().contains(keyWord)) {
				resultList.add(document);
			}
		}
		return resultList;
	}

	/**
	 * 在结果中搜索 标题 摘要 正文 关键词
	 * 
	 * @return
	 */
	@Override
	public List<FtsDocumentWeChat> findInResultWeiXin(List<FtsDocumentWeChat> list, String keyWord) {
		List<FtsDocumentWeChat> resultList = new ArrayList<>();
		for (FtsDocumentWeChat document : list) {
			if (StringUtil.isNotEmpty(document.getUrlTitle()) && document.getUrlTitle().contains(keyWord)
					|| StringUtil.isNotEmpty(document.getContent()) && document.getContent().contains(keyWord)) {
				resultList.add(document);
			}
		}
		return resultList;
	}

	/**
	 * 关键词中搜索
	 * 
	 * @param keyWordList
	 * @param keyWord
	 * @return
	 */
	@Override
	public boolean findInKeyword(List<String> keyWordList, String keyWord) {
		if (ObjectUtil.isNotEmpty(keyWordList)) {
			for (String word : keyWordList) {
				if (word.contains(keyWord)) {
					return true;
				}
			}
		}
		return false;
	}

	// 过滤img标签
	@Override
	public List<FtsDocument> noImg(List<FtsDocument> ftsQuery, String trsl) {
		List<FtsDocument> noImgList = new ArrayList<>();
		for (FtsDocument ftsDocument : ftsQuery) {
			// 防止取摘要不标红
			if (trsl.contains(ESFieldConst.IR_ABSTRACT)) {
				String content = ftsDocument.getAbstracts();
				// 去掉img标签
				if (StringUtil.isNotEmpty(content)) {
					content = StringUtil.replaceImg(content);
					ftsDocument.setContent(content);
				}
			} else if (StringUtil.isNotEmpty(ftsDocument.getContent())) {
				String content = StringUtil.replaceImg(ftsDocument.getContent());
				ftsDocument.setContent(content);
			}
			noImgList.add(ftsDocument);
		}
		return noImgList;
	}

	public List<FtsDocumentStatus> noImgWeiBo(List<FtsDocumentStatus> ftsQuery, String trsl) {
		List<FtsDocumentStatus> noImgList = new ArrayList<>();
		for (FtsDocumentStatus ftsDocument : ftsQuery) {
			// 防止取摘要不标红
			if (StringUtil.isNotEmpty(ftsDocument.getStatusContent())) {
				String content = StringUtil.replaceImg(ftsDocument.getStatusContent());
				ftsDocument.setStatusContent(content);
			}
			noImgList.add(ftsDocument);
		}
		return noImgList;
	}

	public List<FtsDocumentWeChat> noImgWeiXin(List<FtsDocumentWeChat> ftsQuery, String trsl) {
		List<FtsDocumentWeChat> noImgList = new ArrayList<>();
		for (FtsDocumentWeChat ftsDocument : ftsQuery) {
			// 防止取摘要不标红
			if (StringUtil.isNotEmpty(ftsDocument.getContent())) {
				String content = StringUtil.replaceImg(ftsDocument.getContent());
				ftsDocument.setContent(content);
			}
			noImgList.add(ftsDocument);
		}
		return noImgList;
	}

	// 过滤gif的方法
	@Override
	public String noGif(int mid, String imageContent1, String[] split1, int responseCode, String group) {
		// 回调
		if (imageContent1.toLowerCase().contains("GIF".toLowerCase()) || 200 != responseCode
				|| (!imageContent1.toLowerCase().contains("JPG".toLowerCase())
						&& !imageContent1.toLowerCase().contains("JPEG".toLowerCase())
						&& !imageContent1.toLowerCase().contains("PNG".toLowerCase()))) {
			if (mid + 1 < split1.length) {
				imageContent1 = split1[mid + 1];
				// 正则截取
				String per = "(&nbsp;SRC=&quot;)";
				String endfi = "[&]";
				String re = per + ".*?" + endfi;
				// 转换为正则表达式
				Pattern regexPhone = Pattern.compile(re);
				// 用正则表达式去内容中匹配
				Matcher matcher = regexPhone.matcher(imageContent1);
				// matcher.find()为匹配结果 已匹配true 不匹配false
				if (matcher.find()) {
					// matcher.group返回第一个匹配的值
					String matcherGroup = matcher.group();
					if (imageContent1.contains("imageUrl")) {
						String perImg = "imageUrl=";
						String reImg = perImg + ".*?" + endfi;
						// 转换为正则表达式
						Pattern regexperImg = Pattern.compile(reImg);
						// 用正则表达式去内容中匹配
						Matcher matcherImg = regexperImg.matcher(imageContent1);
						if (matcherImg.find()) {
							group = matcherImg.group().replaceAll(endfi, "").replaceAll(perImg, "");
							if (StringUtil.isNotEmpty(group)) {
								// 判断返回码
								URL u = null;
								try {
									u = new URL(group);
								} catch (MalformedURLException e1) {
									e1.printStackTrace();
								}
								try {
									HttpURLConnection uConnection = (HttpURLConnection) u.openConnection();
									uConnection.setConnectTimeout(2000);
									uConnection.connect();
									responseCode = uConnection.getResponseCode();
									if (200 == responseCode) {
										return group;
									} else {
										noGif(mid + 1, imageContent1, split1, responseCode, group);
									}
								} catch (Exception e) {
									noGif(mid + 1, imageContent1, split1, responseCode, group);
								}
							} else {
								noGif(mid + 1, imageContent1, split1, responseCode, group);
							}
						}
					} else {
						group = matcherGroup.replaceAll(per, "").replaceAll(endfi, "");
						if (StringUtil.isNotEmpty(group)) {
							// 判断返回码
							URL u = null;
							try {
								u = new URL(group);
							} catch (MalformedURLException e1) {
								e1.printStackTrace();
							}
							try {
								HttpURLConnection uConnection = (HttpURLConnection) u.openConnection();
								uConnection.setConnectTimeout(2000);
								uConnection.connect();
								responseCode = uConnection.getResponseCode();
								noGif(mid + 1, imageContent1, split1, responseCode, group);
							} catch (Exception e) {
								noGif(mid + 1, imageContent1, split1, responseCode, group);
							}

						} else {
						}
						log.info(group);
					}
				}

			} else {

				return group;
			}
		} else {
			return group;
		}
		return group;
	}


	/**
	 * 获取新闻榜单
	 *
	 * @return Object
	 * @throws TRSException
	 *             TRSException
	 */
	@Override
	public Object newsFocus(int limit, String trsl) throws TRSException {
		try {
			Map<String,Object> map=new HashMap<>();
			List<ESDocumentNew> documents = new ArrayList<>();

			GroupResult infoList = hybase8SearchService.categoryQuery(false,trsl, false,false,false, ESFieldConst.MD5TAG, limit,null,
					Const.HYBASE_NI_INDEX);
			ObjectUtil.assertNull(infoList, "新闻榜单 MD5 List");
			log.info(trsl);
			for (GroupInfo info : infoList) {
				String key = info.getFieldValue();
				QueryBuilder queryBuilder = new QueryBuilder();
				queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
				queryBuilder.filterByTRSL(trsl);
				queryBuilder.filterChildField(ESFieldConst.MD5TAG, key, Operator.Equal);
				queryBuilder.page(0, 1);
				List<ESDocumentNew> pagedList = hybase8SearchService.ftsQuery(queryBuilder, ESDocumentNew.class, true,false,false,null);
				log.info(queryBuilder.asTRSL());
				if (ObjectUtil.isNotEmpty(pagedList)) {
					ESDocumentNew esDocumentNew = pagedList.get(0);
					esDocumentNew.setCount(info.getCount());
					documents.add(esDocumentNew);
				}
			}
			//保存表达式
			String uuid = UUID.randomUUID().toString();
			RedisUtil.setString(uuid, trsl);
			log.info("首页"+trsl);
			map.put("trslk", uuid);
			map.put("data", documents);
			return map;
		} catch (Exception e) {
			throw new OperationException("新闻榜单出错");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object trend(List<SpecialProject> findByUserId) throws TRSSearchException, OperationException {
		// 获取前24小时数据
		// 返回给前端的
		List<String> dateListBefore = DateUtil.getCurrentDateHours3();
		List<String> noSecondList = new ArrayList<>();
		// 只要时分不要秒
		for (String second : dateListBefore) {
			noSecondList.add(second.substring(0, 5));
		}
		List<String> dateList = DateUtil.getCurrentDateHours();
		Date today = new Date();
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
		String now = f.format(today);
		List<String> hourList = new ArrayList<>();
		for (String hour : dateList) {
			hourList.add(hour.substring(8, 10));
		}
		List<Map<String, Object>> listAll = new ArrayList<>();
		for (SpecialProject special : findByUserId) {
			QueryBuilder queryBuilder = special.toNoPagedAndTimeBuilder();
			String trsl = "IR_URLTIME:[" + dateList.get(0) + " TO " + now + "]";
			queryBuilder.filterByTRSL(trsl);
			queryBuilder.page(0, 1000);
			log.info(queryBuilder.asTRSL());
			String startGroup = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
			GroupResult categoryQuery = hybase8SearchService.categoryQuery(queryBuilder, true,false, false,"IR_URLTIME_HOUR",null,
					Const.HYBASE_NI);
			String endGroup = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
			log.warn("首页  专项趋势 本次分类统计 耗时"
					+ (Integer.parseInt(endGroup.substring(8, 17)) - Integer.parseInt(startGroup.substring(8, 17)))
					+ "ms");
			Map<String, Object> broken = BrokenLineUtil.broken(categoryQuery, special.getSpecialName(), hourList);
			listAll.add(broken);
		}
		Map<String, Object> putValue = MapUtil.putValue(new String[] { "hourlist", "datalist" }, noSecondList, listAll);
		return putValue;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object specialTrend(String start, String end) throws Exception {

		log.info("首页专项折线图");
		List<String> date = DateUtil.getBetweenDateString(start, end, "yyyyMMdd");
		List<SpecialProject> list = (List<SpecialProject>) specialProjectRepository.findAll();
		Map all = new HashMap();
		List arrayList = new ArrayList();
		for (SpecialProject l : list) {
			// 对象转换为字符串 toNoPagedAndTimeBuilder拼凑表达式
			String itrsSearchBuilder = l.toNoPagedAndTimeBuilder().asTRSL();
			String spacialName = l.getSpecialName();
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.filterByTRSL("IR_URLTIME:[" + start + " TO " + end + "] AND " + itrsSearchBuilder);
			GroupResult categoryQuery = hybase8SearchService.categoryQuery(false,queryBuilder.asTRSL(), true,false, false,"IR_URLDATE",
					1000,null, Const.HYBASE_NI_INDEX);
			Map result = BrokenLineUtil.brokenLineNew(categoryQuery, spacialName, date);
			arrayList.add(result);
		}
		all.put(date, arrayList);
		return all;
	}

	/**
	 * 首页（热点）信息列表
	 *
	 * @param columnId
	 * @param limit
	 * @return
	 * @throws OperationException
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object hotInfo(String columnId, int limit) throws OperationException {
		try {
			Columns columns = columnRepository.findOne(columnId);
			TabType tabType = columns.getTabType();
			List<Map> result = new ArrayList<>();
			String[] regions = columns.getTabKeywords().split(";");
			String start = "";
			String end = "";
			String keywords = columns.getKeywords();
			switch (tabType) {
			case TIME:// 国内新闻热点DOMESTIC
				for (String region : regions) {
					TRSEsSearchParams params = new TRSEsSearchParams();
					// 查es
					if (CURRENT_MONTH.equals(region)) {
						start = DateUtil.startOfMonth();
						end = DateUtil.endOfMonth();
					} else if (CURRENT_WEEK.equals(region)) {
						start = DateUtil.startOfWeek();
						end = DateUtil.endOfWeek();
					} else if (CURRENT_DAY.equals(region)) {
						SimpleDateFormat day = new SimpleDateFormat("yyyyMMddhhmmss");
						start = day.format(DateUtil.startOfTodDay()).substring(0, 8) + TIME_SUFFIX;
						end = day.format(DateUtil.endOfTodDay());
					} else {
						throw new OperationException("请确定时间标签正确");
					}
					QueryBuilder query = new QueryBuilder();
					params.setQuery("IR_CONTENT:(" + keywords
							+ ") AND  IR_GROUPNAME:((国内新闻)or (国内新闻_手机客户端) or (国内新闻_电子报)) AND IR_URLTIME:[" + start
							+ " TO " + end + "]");
					query.filterByTRSL("IR_CONTENT:(" + keywords
							+ ") AND  IR_GROUPNAME:((国内新闻) OR (国内新闻_手机客户端) OR (国内新闻_电子报)) AND IR_URLTIME:[" + start
							+ " TO " + end + "]");
					query.page(0, limit);
					query.setDatabase(Const.HYBASE_NI_INDEX);
					query.orderBy("IR_URLTIME", true);
					List<ChartAnalyzeEntity> recordSet = hybase8SearchService.ftsQuery(query, ChartAnalyzeEntity.class,
							true,false,false,null);
					Iterator<ChartAnalyzeEntity> iterator = recordSet.iterator();
					List list = new ArrayList();
					ChartAnalyzeEntity resultMap;
					while (iterator.hasNext()) {
						resultMap = iterator.next();
						list.add(resultMap);

					}
					result.add(MapUtil.putValue(new String[] { "name", "value" }, region, list));
				}
				break;
			case REGION:// 地区新闻热点
				for (String region : regions) {
					QueryBuilder query = new QueryBuilder();
					query.filterByTRSL("IR_CONTENT:" + keywords + " AND CQ_LOCATIONS:" + region
							+ " IR_GROUPNAME:((国内新闻) OR (国外新闻) OR (招投标新闻) OR (国外新闻_敏感) OR (国内新闻_手机客户端) OR (国内新闻_电子报) OR (港澳台新闻))");
					List<ChartAnalyzeEntity> recordSet = hybase8SearchService.ftsQuery(query, ChartAnalyzeEntity.class,
							true,false,false,null);
					Iterator<ChartAnalyzeEntity> iterator = recordSet.iterator();
					List list = new ArrayList();
					ChartAnalyzeEntity resultMap;
					while (iterator.hasNext()) {
						resultMap = iterator.next();
						list.add(resultMap);

					}
					result.add(MapUtil.putValue(new String[] { "name", "value" }, region, list));
				}
				break;
			case EMOTION:
				for (String region : regions) {
					QueryBuilder query = new QueryBuilder();
					query.filterByTRSL("IR_CONTENT:" + keywords + " AND IR_APPRAISE:" + region
							+ " IR_GROUPNAME:((国内新闻) or( 国外新闻) or (招投标新闻) or (国外新闻_敏感) or (国内新闻_手机客户端) or (国内新闻_电子报) or (港澳台新闻))");
					List<ChartAnalyzeEntity> recordSet = hybase8SearchService.ftsQuery(query, ChartAnalyzeEntity.class,
							true,false,false,null);
					Iterator<ChartAnalyzeEntity> iterator = recordSet.iterator();
					List list = new ArrayList();
					ChartAnalyzeEntity resultMap;
					while (iterator.hasNext()) {
						resultMap = iterator.next();
						list.add(resultMap);

					}
					result.add(MapUtil.putValue(new String[] { "name", "value" }, region, list));
				}
				break;
			case INDUSTRY:
				break;
			case SOURCE:
				break;
			case INFO:// 首页信息列表
				QueryBuilder query = new QueryBuilder();
				query.filterByTRSL("IR_CONTENT:" + keywords);
				List<ChartAnalyzeEntity> recordSet = hybase8SearchService.ftsQuery(query, ChartAnalyzeEntity.class,
						true,false,false,null);
				Iterator<ChartAnalyzeEntity> iterator = recordSet.iterator();
				List list = new ArrayList();
				ChartAnalyzeEntity resultMap;
				while (iterator.hasNext()) {
					resultMap = iterator.next();
					list.add(resultMap);

				}
				return list;
			default:
				break;
			}
			return result;
		} catch (Exception e) {
			throw new OperationException("热点信息列表出错");
		}

	}

	@Override
	public Object areaCount(String grouName, QueryBuilder queryBuilder, String[] timeArray) throws TRSException {
		if (StringUtil.isNotEmpty(grouName) && Const.MEDIA_TYPE_NEWS.contains(grouName)) {
			// 单选状态
			if ("国内新闻".equals(grouName)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
				queryBuilder.filterByTRSL(trsl);
			} else if ("国内论坛".equals(grouName)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ").append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
				queryBuilder.filterByTRSL(trsl);
			} else {
				queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, grouName, Operator.Equal);
			}
		}
		log.error("queryBuilder===" + queryBuilder.asTRSL());
		return chartAnalyzeService.getAreaCountForHome(queryBuilder, timeArray, grouName);
	}

	/**
	 * 首页微博热点列表
	 *
	 * @param columnId
	 * @return
	 * @throws com.trs.dc.entity.TRSException
	 *             库名要改
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object weibo(String columnId) throws com.trs.dc.entity.TRSException, TRSSearchException, TRSException {
		List<Map> result = new ArrayList<>();
		Columns columns = columnRepository.findOne(columnId);
		TabType tabType = columns.getTabType();
		String[] regions = columns.getTabKeywords().split(";");
		String start = "";
		String end = "";
		String keywords = columns.getKeywords();
		switch (tabType) {
		case TIME:
			for (String region : regions) {
				// 查es
				if (CURRENT_MONTH.equals(region)) {
					start = DateUtil.startOfMonth();
					end = DateUtil.endOfMonth();
				} else if (CURRENT_WEEK.equals(region)) {
					start = DateUtil.startOfWeek();
					end = DateUtil.endOfWeek();
				} else if (CURRENT_DAY.equals(region)) {
					SimpleDateFormat day = new SimpleDateFormat("yyyyMMddhhmmss");
					start = day.format(DateUtil.startOfTodDay()).substring(0, 8) + TIME_SUFFIX;
					end = day.format(DateUtil.endOfTodDay());
				}
				QueryBuilder query = new QueryBuilder();
				query.filterByTRSL(
						"IR_STATUS_CONTENT:(" + keywords + ") AND IR_LOADTIME:[" + start + " TO " + end + "]");
				List<MBlogAnalyzeEntity> recordSet = esSearchService.ftsQuery(query, MBlogAnalyzeEntity.class, false,false,false,null);
				Iterator<MBlogAnalyzeEntity> iterator = recordSet.iterator();
				List list = new ArrayList();
				MBlogAnalyzeEntity resultMap;
				String uid = "";
				// List<MBlogAnalyzeEntity> recordSetID;
				while (iterator.hasNext()) {
					List list2 = new ArrayList();
					String img = "";
					resultMap = iterator.next();
					uid = resultMap.getUid();
					QueryBuilder queryID = new QueryBuilder();
					queryID.filterByTRSL("IR_UID:" + uid);
					img = "http://slide.news.sina.com.cn/z/slide_1_64237_196213.html#p=1";
					list2.add(img);
					list2.add(resultMap);
					list.add(list2);
					resultMap = iterator.next();
					list.add(resultMap);

				}
				try {
					result.add(MapUtil.putValue(new String[] { "name", "value" }, region, list));
				} catch (OperationException e) {
					e.printStackTrace();
				}
			}
			break;
		case REGION:
			for (String region : regions) {
				QueryBuilder query = new QueryBuilder();
				query.filterByTRSL("IR_STATUS_CONTENT:(" + keywords + ") AND IR_LOCATION:" + region);
				// 应该去用户表查头像
				List<MBlogAnalyzeEntity> recordSet = esSearchService.ftsQuery(query, MBlogAnalyzeEntity.class, false,false,false,null);
				Iterator<MBlogAnalyzeEntity> iterator = recordSet.iterator();
				List list = new ArrayList();
				MBlogAnalyzeEntity resultMap;
				String uid = "";
				while (iterator.hasNext()) {
					List list2 = new ArrayList();
					String img = "";
					resultMap = iterator.next();
					uid = resultMap.getUid();
					QueryBuilder queryID = new QueryBuilder();
					queryID.filterByTRSL("IR_UID:" + uid);
					img = "http://slide.news.sina.com.cn/z/slide_1_64237_196213.html#p=1";
					list2.add(img);
					list2.add(resultMap);
					list.add(list2);
				}
				try {
					result.add(MapUtil.putValue(new String[] { "name", "value" }, region, list));
				} catch (OperationException e) {
					e.printStackTrace();
				}
			}
			break;
		case INFO:// 测试首页微博热点信息
			QueryBuilder query = new QueryBuilder();
			query.filterByTRSL("IR_STATUS_CONTENT:" + keywords);
			List<MBlogAnalyzeEntity> recordSet = esSearchService.ftsQuery(query, MBlogAnalyzeEntity.class, false,false,false,null);
			Iterator<MBlogAnalyzeEntity> iterator = recordSet.iterator();
			List list = new ArrayList();
			MBlogAnalyzeEntity resultMap;
			String uid = "";
			while (iterator.hasNext()) {
				List list2 = new ArrayList();
				String img = "";
				resultMap = iterator.next();
				uid = resultMap.getUid();
				QueryBuilder queryID = new QueryBuilder();
				queryID.filterByTRSL("IR_UID:" + uid);
				// 应该去用户表查头像
				img = "http://slide.news.sina.com.cn/z/slide_1_64237_196213.html#p=1";
				list2.add(img);
				list2.add(resultMap);
				list.add(list2);
			}
			return list;
		default:
			break;
		}
		return result;
	}

	// 查找图片
	@Override
	public List<SpecialProject> specialImg(List<SpecialProject> list2) throws TRSSearchException, TRSException {
		List<SpecialProject> imgList = new ArrayList<>();
		for (SpecialProject special : list2) {
			String key = special.getId();
			// 从缓存池里边取出来
			String value = TimingCachePool.get(key);
			if (ObjectUtil.isNotEmpty(value)) {
				special.setImgUrl(value);
			} else {
				// 通过内容截取图片
				QueryBuilder queryBuilder = special.toNoTimeBuilder(0, 1);
				// 过滤
				queryBuilder.filterField("IR_GROUPNAME", "国内微信", Operator.NotEqual);
				queryBuilder.orderBy("IR_IMAGEFLAG", true);
				queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
				log.info(queryBuilder.asTRSL());
				List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,null);
				if (ftsQuery != null && ftsQuery.size() > 0) {
					FtsDocument document = ftsQuery.get(0);
					String content = document.getContent();
					if (content.contains("IMAGE")) {
						String[] split1 = content.split("IMAGE");
						int mid = split1.length / 2;
						String imageContent1 = split1[mid];
						// 过滤掉git
						String group = noGif(mid + 1, imageContent1, split1, 203, "");
						if (StringUtil.isNotEmpty(group)) {
							special.setImgUrl(group);
						} else {
							imageContent1 = split1[0];
							String noGif = noGif(0, imageContent1, split1, 203, "");
							special.setImgUrl(noGif);
						}
					} else {
						special.setImgUrl("");
					}
				} else {
					special.setImgUrl("");
				}
			}
			imgList.add(special);
		}
		return imgList;
	}

	@Override
	public SpecialProject specialIdImg(SpecialProject special) throws TRSSearchException, TRSException {

		String key = special.getId();
		// 从缓存池里边取出来
		String value = TimingCachePool.get(key);
		if (ObjectUtil.isNotEmpty(value)) {
			special.setImgUrl(value);
		} else {
			// 通过内容截取图片
			QueryBuilder queryBuilder = special.toNoTimeBuilder(0, 1);
			// 过滤
			queryBuilder.filterField("IR_GROUPNAME", "国内微信", Operator.NotEqual);
			queryBuilder.orderBy("IR_IMAGEFLAG", true);
			queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,null);
			if (ftsQuery != null && ftsQuery.size() > 0) {
				FtsDocument document = ftsQuery.get(0);
				String content = document.getContent();
				if (content.contains("IMAGE")) {
					String[] split1 = content.split("IMAGE");
					// String imageContent1=split1[2];
					int mid = split1.length / 2;
					String imageContent1 = split1[mid];
					String group = noGif(mid + 1, imageContent1, split1, 203, "");
					if (StringUtil.isNotEmpty(group)) {
						special.setImgUrl(group);
						TimingCachePool.put(key, group, 60 * 24 * 7);
					} else {
						imageContent1 = split1[0];
						String noGif = noGif(0, imageContent1, split1, 203, "");
						special.setImgUrl(noGif);
						TimingCachePool.put(key, group, 60 * 24 * 7);
					}
				} else {
					special.setImgUrl("");
				}
			} else {
				special.setImgUrl("");
			}
		}
		return special;
	}

	@Override
	public List<ClassInfo> netAnalysis(String keyWords) throws OperationException,TRSSearchException{
		QueryBuilder queryBuilder = new QueryBuilder();
		List<ClassInfo> classInfo = new ArrayList<>();
		try {
			queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, com.trs.netInsight.support.fts.util.DateUtil.formatTimeRange("24h"), Operator.Between);
		} catch (OperationException e) {
			throw new OperationException("时间转换错误，message："+e,e);
		}
		queryBuilder.setPageSize(Integer.MAX_VALUE);
		String sb  = new StringBuffer().append( FtsFieldConst.FIELD_TITLE).append(":(\"")
				.append(keyWords).append("\")").append(" OR ")
				.append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
				.append(keyWords+"\")").toString();
		//queryBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT,keyWords,Operator.Equal);
		//queryBuilder.filterField(FtsFieldConst.FIELD_URLTITLE,keyWords,Operator.Equal);
		queryBuilder.filterByTRSL(sb);

		log.warn("netSearch接口："+queryBuilder.asTRSL());

		GroupResult categoryQuery = hybase8SearchService.categoryQuery(queryBuilder, false, false,false,FtsFieldConst.FIELD_GROUPNAME, null,Const.MIX_DATABASE);
		List<GroupInfo> groupList = categoryQuery.getGroupList();
		Map<String, Long> map = new LinkedHashMap<>();
		map.put("国内新闻",0L);
		map.put("国内博客",0L);
		map.put("国内论坛",0L);
		map.put("微博",0L);
		map.put("国内视频",0L);
		map.put("微信",0L);
		map.put("国内新闻_手机客户端",0L);
		map.put("国内新闻_电子报",0L);
		map.put("国外新闻",0L);
		map.put("Twitter",0L);
		map.put("FaceBook",0L);

		for (GroupInfo groupInfo : groupList) {
			String fieldValue = groupInfo.getFieldValue();
			if ("国内微信".equals(fieldValue)) {
				fieldValue = "微信";
			}
			map.put(fieldValue, groupInfo.getCount());
		}

		Iterator<Map.Entry<String, Long>> iterMap = map.entrySet().iterator();
		Long total = 0L;
		while (iterMap.hasNext()) {
			Map.Entry<String, Long> entry = iterMap.next();
			String key = entry.getKey();
			int count = entry.getValue().intValue();
			classInfo.add(new ClassInfo(key,count));
			total += count;
		}
		classInfo.add(new ClassInfo("搜索总量",total));
		return classInfo;
	}

}
