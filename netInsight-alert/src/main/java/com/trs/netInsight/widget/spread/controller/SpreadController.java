
/**
 * 传播路径分析
 * <p>
 * Created by even on 2017/5/3.
 */
package com.trs.netInsight.widget.spread.controller;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.FullTextSearch;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.handler.exception.NullException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.entity.SpreadEntity;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.spread.entity.Document;
import com.trs.netInsight.widget.spread.search.SpreadService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 传播路径分析
 * <p>
 * Created by even on 2017/5/3.
 */
@Slf4j
@RestController
@RequestMapping("/spread")
@Api(description = "传播路径分析")
public class SpreadController {

	@Autowired
	private SpecialProjectRepository specialProjectRepository;

	@Autowired
	private SpecialProjectRepository specialProjectNewRepository;

	@Autowired
	private FullTextSearch esSearchService;

	@Autowired
	private RestTemplate initRestTemplate;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private SpreadService spreadService;

	@ApiOperation("传播路径分析接口")
	@FormatResult
	@RequestMapping(value = "/analyseNew", method = RequestMethod.GET)
	public Object analyseNew(@ApiParam(value = "专项id") @RequestParam("special_id") String specialId,
			@ApiParam(value = "时间间隔") @RequestParam("time_range") String tRange,
			@RequestParam(value = "area", required = false) String area,
			@RequestParam(value = "industry", required = false) String industry) throws Exception {

		// String urlGet =
		// "http://192.168.200.5:30786/url?url=%s&level=5&num=100";
		// String urlGet = "http://localhost:30786/url?url=%s&level=5&num=100";
		// 获得url
		String[] timeRange = DateUtil.formatTimeRange(tRange);
		// 不要缓存 防止出来引爆点信息
		String spreadKey = DateUtil.startOfWeek() + specialId;
		// String url = RedisFactory.getValueFromRedis(spreadKey);
		// if (StringUtil.isNotEmpty(url))
		// return initRestTemplate.getForObject(String.format(urlGet, url),
		// Object.class);

		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilder();
		if (StringUtil.isNotEmpty(industry)) {
			searchBuilder.filterField(ESFieldConst.IR_SRESERVED2, industry.split(";"), Operator.Equal);
		}
		if (StringUtil.isNotEmpty(area)) {
			searchBuilder.filterField(ESFieldConst.CATALOG_AREA, area.split(";"), Operator.Equal);
		}

		searchBuilder.filterField(ESFieldConst.IR_URLTIME, timeRange, Operator.Between);
		searchBuilder.page(0, 1);
		searchBuilder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
		searchBuilder.setDatabase(Const.WEIBO);
		log.info(searchBuilder.asTRSL());
		List<SpreadEntity> list = hybase8SearchService.ftsQuery(searchBuilder, SpreadEntity.class,true,false,false,null);
		if (list.size() <= 0) {
			throw new NullException("获取url为空");
		}
		String url = list.get(0).baseUrl();
		RedisFactory.setValueToRedis(spreadKey, url);
		return spreadService.url(url, 5, 100);
		// http请求spread工程接口
		// return initRestTemplate.getForObject(String.format(urlGet, url),
		// String.class);
	}

	@ApiOperation("传播路径分析接口")
	@FormatResult
	@EnableRedis
	@RequestMapping(value = "/analyse", method = RequestMethod.GET)
	public Object analyse(@ApiParam(value = "专项id") @RequestParam("specialId") String specialId,
			@ApiParam(value = "时间间隔") @RequestParam("timeRange") String tRange,
			@RequestParam(value = "area", required = false) String area,
			@RequestParam(value = "industry", required = false) String industry) throws Exception {

		String urlGet = "http://192.168.200.5:30786/url?url=%s&level=3&num=100";
//		 String urlGet = "http://127.0.0.1:30786/url?url=%s&level=3&num=100";
		// 获得url
		String[] timeRange = DateUtil.formatTimeRange(tRange);
		// 不要缓存 防止出来引爆点信息
		String spreadKey = DateUtil.startOfWeek() + specialId;
		String url = RedisFactory.getValueFromRedis(spreadKey);
		if (StringUtil.isNotEmpty(url)) {
			return initRestTemplate.getForObject(String.format(urlGet, url), Object.class);
		}

		SpecialProject specialProject = specialProjectNewRepository.findOne(specialId);
		QueryBuilder searchBuilder = specialProject.toNoPagedAndTimeBuilderWeiBo();
		if (!"ALL".equals(industry)) {
			searchBuilder.filterField(ESFieldConst.IR_SRESERVED2, industry.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(area)) {
			searchBuilder.filterField(ESFieldConst.CATALOG_AREA, area.split(";"), Operator.Equal);
		}

		searchBuilder.filterField(ESFieldConst.IR_CREATED_AT, timeRange, Operator.Between);
		searchBuilder.page(0, 1);
		searchBuilder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
		searchBuilder.setDatabase(Const.WEIBO);
		List<SpreadEntity> list = hybase8SearchService.ftsQuery(searchBuilder, SpreadEntity.class,true,false,false,null);
		log.info(searchBuilder.asTRSL());
		if (list.size() <= 0) {
			throw new NullException("获取url为空");
		}
		url = list.get(0).baseUrl();
		RedisFactory.setValueToRedis(spreadKey, url);
		// http请求spread工程接口
		Object forObject = initRestTemplate.getForObject(String.format(urlGet, url), Object.class);
		return forObject;
	}

	@ApiOperation("新闻传播路径")
	@RequestMapping(value = "/news", method = RequestMethod.GET)
	public Object newsSpread(@ApiParam("专项id") @RequestParam("special_id") String specialId,
			@ApiParam("时间范围") @RequestParam("timerange") String timerange,
			@RequestParam(value = "area", defaultValue = "ALL") String area,
			@RequestParam(value = "industry", defaultValue = "ALL") String industry) throws Exception {
		// 获取时间范围
		String[] timeRange = DateUtil.formatTimeRange(timerange);
		SpecialProject specialProject = specialProjectRepository.findOne(specialId);
		if (specialProject != null) {

			QueryBuilder searchBuilder = new QueryBuilder();
			searchBuilder.filterByTRSL(searchBuilder.asTRSL()
					+ " AND IR_GROUPNAME:((国内新闻)or (国内新闻_手机客户端) or (国内新闻_电子报) or (港澳台新闻)) AND IR_URLTIME:["
					+ timeRange[0] + " TO " + timeRange[1] + "]");
			searchBuilder.setPageSize(1);
			if (!"all".equals(industry)) {
				searchBuilder.filterField(ESFieldConst.IR_SRESERVED2, industry.split(";"), Operator.Equal);
			}
			if (!"all".equals(area)) {
				searchBuilder.filterField(ESFieldConst.CATALOG_AREA, area.split(";"), Operator.Equal);
			}
			// 分类统计取md5最多的那条
			GroupResult list = esSearchService.categoryQuery(searchBuilder,true,false,false, ESFieldConst.MD5TAG,
					Const.ES_INDEX_CHUANTONG);
			if (list.size() <= 0) {
				throw new NullException("获取md5为空");
			}
			Iterator<GroupInfo> iterator = list.iterator();
			String md5tag = "";
			while (iterator.hasNext()) {
				GroupInfo info = iterator.next();
				md5tag = info.getFieldValue();
			}
			String urlGet = "http://localhost:30786/news?md5tag=" + md5tag;
			String result = initRestTemplate.getForObject(urlGet, String.class);

			return result;
		}
		return null;
	}

	/**
	 * 获取下一层转发关系 递归取值，每算一层level-1 ,直至为0
	 *
	 * @param list
	 *            文章列表
	 * @param maxLength
	 *            限定的长度
	 * @param level
	 *            层数
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	private void getChildren(List<Document> list, int maxLength, int level) throws Exception {
		List<Map<String, String>> linksList = new ArrayList<>();
		Map map = new HashMap<>();
		map.put("list", list);
		if (list.size() > 0) {
			for (Document document : list) {
				String rUrl = document.getRootUrl();
				String screenName = document.getScreenName();
				// ITRSSearchBuilder builder = SearchBuilderFactory.create(0,
				// maxLength);
				QueryBuilder builder = new QueryBuilder();
				builder.page(0, maxLength);
				String startTime = DateUtil.date2String(document.getCreatedAt(), DateUtil.yyyyMMddHHmmss);
				String endTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss);
				Map linkNode = new HashMap<>();
				linkNode.put("source", document.getRetFrom());
				linkNode.put("target", document.getScreenName());
				linksList.add(linkNode);
				builder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
				builder.filterField(ESFieldConst.IR_CREATED_AT, new String[] { startTime, endTime }, Operator.Between);
				builder.filterField(ESFieldConst.IR_RETWEETED_URL, "\"" + rUrl + "\"", Operator.Equal);
				builder.filterField(ESFieldConst.IR_RETWEETED_FROM, "\"" + screenName + "\"", Operator.Equal);
				// PagedList<Document> pagedList =
				// documentRepository.pageList(builder);
				List<Document> pagedList = hybase8SearchService.ftsQuery(builder, Document.class,true,false,false,null);
				/*
				 * if (pagedList.size() > 0) {
				 * document.setChildren(pagedList.getPageItems()); if (level >
				 * 0) getChildren(document.getChildren(), maxLength, level - 1);
				 * }
				 */
				if (pagedList.size() > 0) {
					document.setChildren(pagedList);
					if (level > 0) {
						getChildren(document.getChildren(), maxLength, level - 1);
					}
				}
			}
		}
	}

	/**
	 * 通过URL获取微博原文信息
	 *
	 * @param url
	 *            需要分析的url
	 * @return Document
	 */
	@SuppressWarnings("unused")
	private Document getDocument(String url) throws Exception {
		// ITRSSearchBuilder builder = SearchBuilderFactory.create(0, 1);
		QueryBuilder builder = new QueryBuilder();
		builder.page(0, 1);
		builder.filterField(ESFieldConst.IR_URLNAME, "\"" + url + "\"", Operator.Equal);
		// PagedList<Document> pagedList = documentRepository.pageList(builder);
		List<Document> pagedList = hybase8SearchService.ftsQuery(builder, Document.class,true,false,false,null);
		/*
		 * if (pagedList.getThisPageTotal() > 0) { Document document =
		 * pagedList.get(0);
		 * document.setRUrl(StringUtil.isEmpty(document.getRUrl()) ?
		 * document.getUrl() : document.getRUrl()); return document; }
		 */
		if (pagedList.size() > 0) {
			Document document = pagedList.get(0);
			document.setRootUrl(
					StringUtil.isEmpty(document.getRootUrl()) ? document.getCurrentUrl() : document.getRootUrl());
			return document;
		} else {
			throw new NullException(String.format("can not find document by url[%s]", url));
		}
	}

}
