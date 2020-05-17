package com.trs.netInsight.widget.report.task;

import static com.trs.netInsight.widget.report.constant.SimplerReportConst.AREA;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.AREAkey;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.DATASOURCEANALYSIS;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.DATASOURCEANALYSISkey;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.DATATRENDANALYSIS;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.DATATRENDANALYSISkey;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.EMOTIONANALYSIS;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.EMOTIONANALYSISkey;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.NEWSHOTTOPICS;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.NEWSHOTTOPICSkey;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.OVERVIEWOFDATA;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.OVERVIEWOFDATAkey;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.REPORTINTROkey;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.WEBSITESOURCETOP10;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.WEBSITESOURCETOP10key;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.WECHATACTIVETOP10;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.WECHATACTIVETOP10key;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.WEIBOACTIVETOP10;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.WEIBOACTIVETOP10key;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.WEIBOHOTTOPICS;
import static com.trs.netInsight.widget.report.constant.SimplerReportConst.WEIBOHOTTOPICSkey;

import java.text.SimpleDateFormat;
import java.util.*;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.WordSpacingUtil;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.impl.Hybase8SearchImpl;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.util.MapUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.TElementNew;
import com.trs.netInsight.widget.report.entity.repository.ReportNewRepository;
import com.trs.netInsight.widget.report.entity.repository.ReportResourceRepository;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.service.IInfoListService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 北京拓尔思信息技术股份有限公司 Created by yangyanyan on 2018/10/18.
 * @desc 舆情报告 极简模式 任务类
 */
@Slf4j
public class SimpleReportTask implements Runnable {
	// 地域服务
	private IDistrictInfoService districtInfoService = (IDistrictInfoService) ObjectContainer
			.getBean(IDistrictInfoService.class);
	// hybase服务
	private FullTextSearch hybase8SearchService = (Hybase8SearchImpl) ObjectContainer.getBean(Hybase8SearchImpl.class);
	// 报告资源 持久层
	private ReportResourceRepository reportResourceRepository = (ReportResourceRepository) ObjectContainer
			.getBean(ReportResourceRepository.class);
	private IInfoListService infoListService = (IInfoListService) ObjectContainer.getBean(IInfoListService.class);
	private ChartAnalyzeService chartAnalyzeService = (ChartAnalyzeService) ObjectContainer
			.getBean(ChartAnalyzeService.class);
	private ReportNewRepository reportNewRepository = (ReportNewRepository) ObjectContainer
			.getBean(ReportNewRepository.class);
	/**
	 * 任意关键词
	 */
	private String keyWords;
	/**
	 * 排除词
	 */
	private String excludeWords;
	/**
	 * 关键词 位置 标题 | 标题+正文
	 */
	private String searchScope;
	/**
	 * 专家模式 表达式
	 */
	private String trsl;

	private String timeRange;

	private boolean similar;
	private boolean irSimflag;
	private boolean irSimflagAll;

	private boolean weight;

	private String userId;

	private String subGroupId;
	/**
	 * 排除网站
	 */
	private String excludeWebs;


	/**
	 * 报告
	 */
	private ReportNew reportNew;

	public SimpleReportTask(String keyWords, String excludeWords, String searchScope, String trsl, String timeRange,
			boolean similar, boolean irSimflag,boolean irSimflagAll, boolean weight, ReportNew reportNew, String userId,String subGroupId,String excludeWebs) {
		this.keyWords = keyWords;
		this.excludeWords = excludeWords;
		this.searchScope = searchScope;
		this.trsl = trsl;
		this.timeRange = timeRange;
		this.similar = similar;
		this.irSimflag = irSimflag;
		this.irSimflagAll = irSimflagAll;
		this.weight = weight;
		this.reportNew = reportNew;
		this.userId = userId;
		this.subGroupId = subGroupId;
		this.excludeWebs =excludeWebs;
	}

	@Override
	public void run() {
		// 因为最终查询表达式用的是日常监测 所以在此要与日常监测保持一致
		if ("TITLE_CONTENT".equals(searchScope)) {
			searchScope = "1";
		}
		List<TElementNew> elementList = JSONArray.parseArray(reportNew.getTemplateList(), TElementNew.class);

		List<String> chapters = new ArrayList<>();
		if (elementList != null && elementList.size() > 0) {
			for (TElementNew tElementNew : elementList) {
				chapters.add(tElementNew.getChapterDetail());
			}
		}
		List<ReportResource> reportResourcesList = null;
		Object overviewOfDataResult = null;
		try {
			for (String simChapter : chapters) {
				switch (simChapter) {
				case REPORTINTROkey:// 报告简介
					break;
				case OVERVIEWOFDATAkey:// 数据统计
					IndexTab overviewOfDataIT = StringUtils.isEmpty(trsl) ? createIndexTab(keyWords, excludeWords,excludeWebs,
							searchScope, similar, irSimflag,irSimflagAll, timeRange, weight, 10)
							: createIndexTab(trsl, similar, irSimflag,irSimflagAll,timeRange, weight, 10);
					overviewOfDataIT.setGroupName("国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
					overviewOfDataIT.setType(ColumnConst.CHART_PIE_BY_META);
					overviewOfDataIT.setContrast(ColumnConst.CONTRAST_TYPE_GROUP);
					reportResourcesList = new ArrayList<>();

					try {
						overviewOfDataResult = columnSearch(overviewOfDataIT, 10,
								"国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
						insertImgDataIntoResources(userId, OVERVIEWOFDATA, JSON.toJSONString(overviewOfDataResult),
								"barGraphChartMeta", 2, reportNew.getId(), reportResourcesList);
					} catch (OperationException e) {
						// setEmptyData(reportData, ReportConst.SINGLERESOURCE,
						// ReportConst.OVERVIEWOFDATAkey);
						insertImgDataIntoResources(userId, OVERVIEWOFDATA, "暂无数据", "barGraphChartMeta", 2,
								reportNew.getId(), reportResourcesList);
						log.error(OVERVIEWOFDATA, e);
					}
					reportResourceRepository.save(reportResourcesList);
					break;

				// case NEWSTOP10key:
				// IndexTab webSiteResource = StringUtils.isEmpty(trsl) ?
				// createIndexTab(keyWords,excludeWords, searchScope,
				// similar,irSimflag, timeRange, weight,25) :
				// createIndexTab(trsl,similar,irSimflag, timeRange, weight
				// ,10);
				// webSiteResource.setGroupName("传统媒体");
				// webSiteResource.setTradition("国内新闻");
				// webSiteResource.setType(ColumnConst.LIST_NO_SIM);
				// reportResourcesList = new ArrayList<>();
				// try {
				// Object newTOP10Result = columnSearch(webSiteResource, 10);
				// saveListResources(newTOP10Result,NEWSTOP10,reportResourcesList,3);
				// } catch (OperationException e) {
				// insertEmpatyDataIntoResources(NEWSTOP10,null,3,reportResourcesList);
				// log.error(NEWSTOP10 , e);
				// }
				// reportResourceRepository.save(reportResourcesList);
				// break;
				// case WEIBOTOP10key:
				// IndexTab weiboTop10 = StringUtils.isEmpty(trsl) ?
				// createIndexTab(keyWords,excludeWords, searchScope,
				// similar,irSimflag, timeRange, weight,25) :
				// createIndexTab(trsl,similar,irSimflag, timeRange, weight
				// ,10);
				// weiboTop10.setGroupName("微博");
				// weiboTop10.setTradition("");
				// weiboTop10.setType(ColumnConst.LIST_STATUS_COMMON);
				// reportResourcesList = new ArrayList<>();
				//
				// try {
				// Object weiboTOP10Result = columnSearch(weiboTop10, 10);
				// saveListResources(weiboTOP10Result,WEIBOTOP10,reportResourcesList,4);
				// } catch (OperationException e) {
				// insertEmpatyDataIntoResources(WEIBOTOP10,null,4,reportResourcesList);
				// log.error(WEIBOTOP10 , e);
				// }
				// reportResourceRepository.save(reportResourcesList);
				// break;
				// case WECHATTOP10key:
				// IndexTab weChatTop10 = StringUtils.isEmpty(trsl) ?
				// createIndexTab(keyWords,excludeWords, searchScope, similar,
				// irSimflag,timeRange, weight,25) :
				// createIndexTab(trsl,similar, irSimflag,timeRange, weight
				// ,10);
				// weChatTop10.setGroupName("微信");
				// weChatTop10.setTradition("");
				// weChatTop10.setType(ColumnConst.LIST_WECHAT_COMMON);
				// reportResourcesList = new ArrayList<>();
				//
				// try {
				// Object weChatTOP10Result = columnSearch(weChatTop10, 10);
				// saveListResources(weChatTOP10Result,WECHATTOP10,reportResourcesList,5);
				// } catch (OperationException e) {
				// insertEmpatyDataIntoResources(WECHATTOP10,null,5,reportResourcesList);
				// log.error(WECHATTOP10 , e);
				// }
				// reportResourceRepository.save(reportResourcesList);
				// break;

				case DATASOURCEANALYSISkey:
					reportResourcesList = new ArrayList<>();
					IndexTab dataSourceOfDataIT = StringUtils.isEmpty(trsl) ? createIndexTab(keyWords, excludeWords,excludeWebs,
							searchScope, similar, irSimflag,irSimflagAll, timeRange, weight, 10)
							: createIndexTab(trsl, similar, irSimflag, irSimflagAll,timeRange, weight, 10);
					dataSourceOfDataIT.setGroupName("国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
					dataSourceOfDataIT.setType(ColumnConst.CHART_PIE_BY_META);
					dataSourceOfDataIT.setContrast(ColumnConst.CONTRAST_TYPE_GROUP);
					try {
						Object dataSourceResult = columnSearch(dataSourceOfDataIT, 10,
								"国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
						insertImgDataIntoResources(userId, DATASOURCEANALYSIS, JSON.toJSONString(dataSourceResult),
								"pieGraphChartMeta", 3, reportNew.getId(), reportResourcesList);
					} catch (OperationException e) {
						insertEmpatyDataIntoResources(DATASOURCEANALYSIS, "pieGraphChartMeta", 3, reportResourcesList);
						log.error(DATASOURCEANALYSIS, e);
					}

					reportResourceRepository.save(reportResourcesList);
					break;
				case DATATRENDANALYSISkey:
					IndexTab dataTrend = StringUtils.isEmpty(trsl) ? createIndexTab(keyWords, excludeWords, excludeWebs,searchScope,
							similar, irSimflag,irSimflagAll, timeRange, weight, 25)
							: createIndexTab(trsl, similar, irSimflag,irSimflagAll, timeRange, weight, 10);
					dataTrend.setGroupName("国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
					dataTrend.setType(ColumnConst.CHART_LINE);
					dataTrend.setContrast(ColumnConst.CONTRAST_TYPE_GROUP);
					reportResourcesList = new ArrayList<>();

					try {
						Object dataTrendResult = columnSearch(dataTrend, 10,
								"国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");

						insertImgDataIntoResources(userId, DATATRENDANALYSIS, JSON.toJSONString(dataTrendResult),
								"brokenLineChart", 4, reportNew.getId(), reportResourcesList);

					} catch (OperationException e) {
						insertEmpatyDataIntoResources(DATATRENDANALYSIS, "brokenLineChart", 4, reportResourcesList);
						log.error(DATATRENDANALYSIS, e);
					}
					reportResourceRepository.save(reportResourcesList);
					break;
				case AREAkey:
					IndexTab areaSource = StringUtils.isEmpty(trsl) ? createIndexTab(keyWords, excludeWords,excludeWebs,
							searchScope, false, irSimflag,irSimflagAll, timeRange, weight, 25)
							: createIndexTab(trsl, false, irSimflag,irSimflagAll, timeRange, weight, 25);
					//areaSource.setGroupName("传统媒体");
					areaSource.setGroupName("国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
					areaSource.setTradition("");
					areaSource.setType(ColumnConst.CHART_MAP);
					reportResourcesList = new ArrayList<>();

					try {
						Object areaResult = columnSearch(areaSource, 10, "传统媒体");

						insertImgDataIntoResources(userId, AREA, JSON.toJSONString(areaResult), "mapChart", 5,
								reportNew.getId(), reportResourcesList);
					} catch (OperationException e) {
						insertEmpatyDataIntoResources(AREA, "mapChart", 5, reportResourcesList);
						log.error(AREA, e);
					}
					reportResourceRepository.save(reportResourcesList);
					break;
				case EMOTIONANALYSISkey:
					String emotionTrsl = "";
					if (StringUtils.isEmpty(trsl)) {// 普通
						emotionTrsl = createFilter(keyWords, searchScope, excludeWords, excludeWebs,false).asTRSL();
						// if (StringUtils.isNotEmpty(excludeWords)){ 不考虑排除网站
						// emotionTrsl += createFilter(excludeWords);
						// }
					}
					IndexTab emotionIT = StringUtils.isEmpty(trsl) ? createIndexTab(keyWords, excludeWords,excludeWebs, searchScope,
							similar, irSimflag,irSimflagAll, timeRange, weight, 10)
							: createIndexTab(trsl, similar, irSimflag,irSimflagAll, timeRange, weight, 10);
					emotionIT.setGroupName("国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
					emotionIT.setType(ColumnConst.CHART_PIE_BY_META);
					emotionIT.setTrsl("0".equals(searchScope) ? emotionTrsl : trsl);
					emotionIT.setXyTrsl("正面=IR_APPRAISE:正面;\n中性=IR_APPRAISE:中性;\n负面=IR_APPRAISE:负面");
					reportResourcesList = new ArrayList<>();

					try {
						Object emotionResult = columnSearch(emotionIT, 10,
								"国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");

						insertImgDataIntoResources(userId, EMOTIONANALYSIS, JSON.toJSONString(emotionResult),
								"pieGraphChartMeta", 6, reportNew.getId(), reportResourcesList);

					} catch (OperationException e) {
						insertEmpatyDataIntoResources(EMOTIONANALYSIS, "pieGraphChartMeta", 6, reportResourcesList);

						log.error(EMOTIONANALYSIS, e);
					}
					reportResourceRepository.save(reportResourcesList);
					break;
				case NEWSHOTTOPICSkey:
					//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-2
					IndexTab newsHot = StringUtils.isEmpty(trsl) ? createIndexTab(keyWords, excludeWords,excludeWebs, searchScope,
							similar, true,irSimflagAll, timeRange, weight, 10)
							: createIndexTab(trsl, similar, true,irSimflagAll, timeRange, weight, 10);
					newsHot.setIrSimflag(true);
					// 日常监测 默认irsimflag排重 和日常监测保持一致
					newsHot.setIrSimflagAll(false);
					newsHot.setGroupName("传统媒体");
					newsHot.setTradition("国内新闻");
					newsHot.setType(ColumnConst.LIST_SIM);
					reportResourcesList = new ArrayList<>();

					try {
						Object newsHotResult = columnSearch(newsHot, 10, "国内新闻");

						saveListResources(newsHotResult, NEWSHOTTOPICS, reportResourcesList, 7);
					} catch (OperationException e) {
						insertEmpatyDataIntoResources(NEWSHOTTOPICS, null, 7, reportResourcesList);
						log.error(NEWSHOTTOPICS, e);
					}

					reportResourceRepository.save(reportResourcesList);
					break;
				case WEBSITESOURCETOP10key:
					IndexTab websiteSource = StringUtils.isEmpty(trsl) ? createIndexTab(keyWords, excludeWords,excludeWebs,
							searchScope, similar, irSimflag,irSimflagAll, timeRange, weight, 10)
							: createIndexTab(trsl, similar, irSimflag,irSimflagAll, timeRange, weight, 10);
					websiteSource.setGroupName("传统媒体");
					websiteSource.setTradition("国内新闻");
					websiteSource.setType(ColumnConst.CHART_BAR_BY_META);
					websiteSource.setContrast(ColumnConst.CONTRAST_TYPE_SITE);
					reportResourcesList = new ArrayList<>();

					try {
						Object websiteSourceResult = columnSearch(websiteSource, 10, "国内新闻");

						insertImgDataIntoResources(userId, WEBSITESOURCETOP10, JSON.toJSONString(websiteSourceResult),
								"barGraphChartMeta", 8, reportNew.getId(), reportResourcesList);

					} catch (OperationException e) {
						insertEmpatyDataIntoResources(WEBSITESOURCETOP10, "barGraphChartMeta", 8, reportResourcesList);
						log.error(WEBSITESOURCETOP10, e);
					}
					reportResourceRepository.save(reportResourcesList);
					break;
				case WEIBOHOTTOPICSkey:
					//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-2
					IndexTab weiboHot = StringUtils.isEmpty(trsl) ? createIndexTab(keyWords, excludeWords,excludeWebs, searchScope,
							similar, true,irSimflagAll, timeRange, weight, 10)
							: createIndexTab(trsl, similar, true,irSimflagAll, timeRange, weight, 10);
					// 不排重
					weiboHot.setIrSimflagAll(false);
					weiboHot.setIrSimflag(false);
					weiboHot.setGroupName("传统媒体");
					weiboHot.setTradition("微博");
					weiboHot.setType(ColumnConst.LIST_SIM);
					reportResourcesList = new ArrayList<>();

					try {
						Object weiboHotResult = columnSearch(weiboHot, 10, "微博");
						saveListResources(weiboHotResult, WEIBOHOTTOPICS, reportResourcesList, 9);
					} catch (OperationException e) {
						insertEmpatyDataIntoResources(WEIBOHOTTOPICS, null, 9, reportResourcesList);
						log.error(WEIBOHOTTOPICS, e);
					}
					reportResourceRepository.save(reportResourcesList);
					break;
				case WEIBOACTIVETOP10key:
					reportResourcesList = new ArrayList<>();

					try {
						String weiboActiceTrsl = StringUtils.isEmpty(trsl) ? generateActiveTrsl(WEIBOACTIVETOP10key,
								timeRange, keyWords, excludeWords,excludeWebs, searchScope, weight)
								: generateActiveTrsl(WEIBOACTIVETOP10key, trsl, timeRange);
						GroupResult weiboActiceGroupResult = hybase8SearchService.categoryQuery(false, weiboActiceTrsl,
								similar, irSimflag,irSimflagAll, FtsFieldConst.FIELD_UID, 10,null, Const.WEIBO);
						List<Map<String, Object>> weiboActiceList = new ArrayList<>();
						groupResult2MapList(weiboActiceGroupResult, weiboActiceList, "微博");

						insertImgDataIntoResources(userId, WEIBOACTIVETOP10, JSON.toJSONString(weiboActiceList),
								"barGraphChartMeta", 10, reportNew.getId(), reportResourcesList);

					} catch (Exception e) {
						insertEmpatyDataIntoResources(WEIBOACTIVETOP10, "barGraphChartMeta", 10, reportResourcesList);
						log.error(WEIBOACTIVETOP10, e);
					}
					reportResourceRepository.save(reportResourcesList);
					break;
				case WECHATACTIVETOP10key:
					reportResourcesList = new ArrayList<>();

					try {
						String weChatActiceTrsl = StringUtils.isEmpty(trsl)
								? generateActiveTrsl(WECHATACTIVETOP10key, timeRange, keyWords, excludeWords,excludeWebs,
										searchScope, weight)
								: generateActiveTrsl(WECHATACTIVETOP10key, trsl, timeRange);
						GroupResult weChatActiveGroupResult = hybase8SearchService.categoryQuery(false,
								weChatActiceTrsl, similar, irSimflag,irSimflagAll, FtsFieldConst.FIELD_SITENAME, 10, null, Const.WECHAT_COMMON);
						List<Map<String, Object>> wechatList = new ArrayList<>();
						groupResult2MapList(weChatActiveGroupResult, wechatList, "国内微信");

						insertImgDataIntoResources(userId, WECHATACTIVETOP10, JSON.toJSONString(wechatList),
								"barGraphChartMeta", 11, reportNew.getId(), reportResourcesList);

					} catch (Exception e) {
						insertEmpatyDataIntoResources(WECHATACTIVETOP10, "barGraphChartMeta", 11, reportResourcesList);
						log.error(WECHATACTIVETOP10, e);
					}
					reportResourceRepository.save(reportResourcesList);
					break;

				default:
					break;
				}
			}
			reportNew.setDoneFlag(1);
			reportNewRepository.save(reportNew);
		} catch (Exception e) {
			log.error("生成简报出错！",e);
			reportNew.setDoneFlag(1);
			reportNewRepository.save(reportNew);
		}
	}

	private IndexTab createIndexTab(String keyWords, String excludeWords, String excludeWebs,String keyWordsIndex, boolean similar,
			boolean simflag,boolean irSimflagAll, String timeRange, boolean weight, Integer maxSize) {
		IndexTab indexTab = new IndexTab();
		indexTab.setKeyWord(keyWords);
		indexTab.setKeyWordIndex(keyWordsIndex);// 0 或 1
		indexTab.setExcludeWords(excludeWords);
		indexTab.setExcludeWeb(excludeWebs);
		indexTab.setExcludeWeb(null);
		indexTab.setTimeRange(timeRange);
		indexTab.setHide(false);
		indexTab.setMaxSize(maxSize);
		indexTab.setWeight(weight);
		indexTab.setSimilar(similar);
		indexTab.setIrSimflag(simflag);
		indexTab.setIrSimflagAll(irSimflagAll);
		return indexTab;
	}

	/**
	 * 高级模式模拟生成indexTab
	 * 
	 * @since changjiang @ 2018年6月14日
	 */
	private IndexTab createIndexTab(String trsl, boolean similar, boolean simflag,boolean irSimflagAll, String timeRange, boolean weightm,
			Integer maxSize) {
		IndexTab indexTab = new IndexTab();
		indexTab.setTrsl(trsl);
		indexTab.setSimilar(similar);
		indexTab.setIrSimflagAll(irSimflagAll);
		indexTab.setIrSimflag(simflag);
		indexTab.setTimeRange(timeRange);
		indexTab.setHide(false);
		indexTab.setMaxSize(maxSize);
		indexTab.setWeight(weightm);
		return indexTab;
	}

	private Object columnSearch(IndexTab indexTab, Integer maxSize, String groupName) throws OperationException {
		AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
		ColumnConfig config = new ColumnConfig();
		config.setMaxSize(maxSize);
		String[] databases = TrslUtil.chooseDatabases(groupName.split(";"));

		config.initSection(indexTab, indexTab.getTimeRange(), 0, maxSize, null, null, "keywords", "", "", "desc", "",
				"", "", "", "","");
		column.setHybase8SearchService(hybase8SearchService);
		column.setChartAnalyzeService(chartAnalyzeService);
		column.setInfoListService(infoListService);
		column.setDistrictInfoService(districtInfoService);
		column.setConfig(config);
		return column.getColumnData(indexTab.getTimeRange());
	}

	/**
	 * 资源中添加空数据
	 * 
	 * @param chapter
	 * @param imgType
	 * @param chapterPosition
	 * @param reportResourcesList
	 */
	private void insertEmpatyDataIntoResources(String chapter, String imgType, Integer chapterPosition,
			List<ReportResource> reportResourcesList) {

		ReportResource newAdd = new ReportResource();
		newAdd.setUserId(userId);
		newAdd.setSubGroupId(subGroupId);
		newAdd.setChapter(chapter);
		newAdd.setDocPosition(-1);
		newAdd.setReportType(reportNew.getReportType());
		newAdd.setTemplateId(reportNew.getTemplateId());
		newAdd.setChapterPosition(chapterPosition);
		newAdd.setResourceStatus(0);
		newAdd.setReportId(reportNew.getId());
		newAdd.setImgType(imgType);
		newAdd.setChapterPosition(chapterPosition);
		reportResourcesList.add(newAdd);

	}

	/**
	 * 资源池或预览资源中添加Img Type Resource Data
	 * 
	 * @param userId
	 * @param chapter
	 * @param img_data
	 * @param imgType
	 * @param chapterPosition
	 * @param reportId
	 * @param reportResourcesList
	 */
	private void insertImgDataIntoResources(String userId, String chapter, String img_data, String imgType,
			Integer chapterPosition, String reportId, List<ReportResource> reportResourcesList) {
		ReportResource newAdd = new ReportResource();
		newAdd.setChapter(chapter);
		newAdd.setReportType(reportNew.getReportType());
		newAdd.setTemplateId(reportNew.getTemplateId());
		newAdd.setImg_data(img_data);
		newAdd.setImgType(imgType);
		newAdd.setUserId(userId);
		newAdd.setSubGroupId(subGroupId);
		newAdd.setChapterPosition(chapterPosition);
		newAdd.setReportId(reportId);
		newAdd.setResourceStatus(0);
		if (OVERVIEWOFDATA.equals(chapter)) {
			newAdd.setImgComment(ReportUtil.getOverviewOfData(img_data.replace("\\n", "")));

		} else {
			if (StringUtils.isNotEmpty(img_data)) {
				newAdd.setImgComment(ReportUtil.getImgComment(img_data, imgType, chapter));
			}
		}
		reportResourcesList.add(newAdd);

	}

	/**
	 * 合并数据，将hybase查询出来的结果(title、content、srcname等)与原有对象(templateId、reportType等)
	 * 进行合并
	 * 
	 * @param docPosition
	 * @param userId
	 * @param chapter
	 * @param chapterPosition
	 * @param reportId
	 * @param reportResourcesList
	 * @param reportResource
	 */
	private void insertListDataIntoResources(Integer docPosition, String userId, String chapter,
			Integer chapterPosition, String reportId, List<ReportResource> reportResourcesList,
			ReportResource reportResource) {

		ReportResource newAdd = new ReportResource();
		newAdd.setSid(reportResource.getSid());
		newAdd.setUserId(userId);
		newAdd.setSubGroupId(subGroupId);
		newAdd.setGroupName(reportResource.getGroupName());
		newAdd.setChapter(chapter);
		newAdd.setDocPosition(docPosition);
		newAdd.setReportType(reportNew.getReportType());
		newAdd.setTemplateId(reportNew.getTemplateId());
		newAdd.setChapterPosition(chapterPosition);
		newAdd.setResourceStatus(0);
		newAdd.setReportId(reportId);
		newAdd.setUrlDate(reportResource.getUrlDate());
		newAdd.setRttCount(reportResource.getRttCount());
		newAdd.setMd5Tag(reportResource.getMd5Tag());
		newAdd.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(reportResource.getTitle())));
		newAdd.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(reportResource.getContent())));
		newAdd.setSiteName(reportResource.getSiteName());
		newAdd.setSrcName(reportResource.getSrcName());
		newAdd.setNewsAbstract(ReportUtil.replaceHtml(StringUtil.removeFourChar(reportResource.getNewsAbstract())));
		newAdd.setUrlName(reportResource.getUrlName());
		newAdd.setSimCount(reportResource.getSimCount());

		reportResourcesList.add(newAdd);
	}

	/**
	 * 将TOP10类型的列表json数据转换为List<ReportResource>并且给位置信息赋值
	 * 
	 * @author shao.guangze
	 */
	private List<ReportResource> top10list2RR(String jsonData, String chapter) {
		if (StringUtils.isEmpty(jsonData)) {
			return null;
		}
		jsonData = StringUtil.removeFourChar(jsonData);
		List<Map<String, String>> lists = JSONObject.parseObject(jsonData,
				new TypeReference<List<Map<String, String>>>() {
				});
		List<ReportResource> listResult = new ArrayList<>();
		int position = 0;
		for (Map<String, String> map : lists) {
			ReportResource reportResource = new ReportResource();
			reportResource.setMd5Tag(map.get("md5Tag"));
			if (StringUtil.isNotEmpty(map.get("urlTime")) && (new Long(map.get("urlTime")) > 345398400000L)) {
				reportResource.setUrlDate(new Date(new Long(map.get("urlTime"))));
			} else {
				reportResource.setUrlDate(timeAgo2urlDate(map.get("timeAgo")));
			}
			// 位置信息赋值
			// if("新闻网站TOP10".equals(chapter) || "微博TOP10".equals(chapter) ||
			// "微信TOP10".equals(chapter) || "新闻热点话题".equals(chapter) ||
			// "微博热点话题".equals(chapter)){
			// reportResource.setDocPosition( ++position );
			// }
			if ("热点新闻".equals(chapter) || "热点微博".equals(chapter)) {
				reportResource.setDocPosition(++position);
			}
			reportResource.setTimeAgo(map.get("timeAgo"));
			reportResource.setSiteName(map.get("siteName"));
			reportResource.setSrcName(map.get("siteName"));
			reportResource.setTitle(map.get("title"));
			reportResource.setContent(StringUtil.isEmpty(map.get("content")) ? map.get("title") : map.get("content"));
			reportResource.setSid(map.get("sid"));
			reportResource.setGroupName(map.get("groupName"));
			reportResource.setSimCount(map.get("simCount"));
			reportResource.setReportType(reportNew.getReportType());
			reportResource.setChapter(chapter);
			reportResource.setId(UUID.randomUUID().toString().replace("-", ""));
			// 重新截取微博title
			if ("微博TOP10".equals(chapter) || "微博热点话题".equals(chapter)) {
				String content = ReportUtil.replaceHtml(map.get("content"));
				String subStr = "";
				if (content == null)
					content = ReportUtil.replaceHtml(map.get("title"));
				if (content.length() > 160) {
					subStr = content.substring(0, 160);
				} else {
					subStr = content.substring(0, content.length());
				}
				reportResource.setTitle(subStr);
			}
			listResult.add(reportResource);
		}
		return listResult;
	}

	private Date timeAgo2urlDate(String timeAgo) {
		Calendar cal = Calendar.getInstance();
		if (StringUtils.isNotEmpty(timeAgo) && timeAgo.contains("分钟")) {
			cal.add(Calendar.MINUTE, new Integer("-" + timeAgo.replace("分钟前", "")));
		} else if (StringUtils.isNotEmpty(timeAgo) && timeAgo.contains("小时")) {
			cal.add(Calendar.HOUR_OF_DAY, new Integer("-" + timeAgo.replace("小时前", "")));
		} else if (StringUtils.isNotEmpty(timeAgo) && timeAgo.contains("天")) {
			cal.add(Calendar.DAY_OF_MONTH, new Integer("-" + timeAgo.replace("天前", "")));
		} else if (StringUtils.isNotEmpty(timeAgo) && timeAgo.contains(".")) {
			try {
				return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(timeAgo);
			} catch (Exception e) {
				return cal.getTime();
			}
		}
		// Date -> 时间戳
		return cal.getTime();
	}

	private List<ReportResource> saveListResources(Object resources, String chapter,
			List<ReportResource> reportResourcesList, Integer chapterPosition) {
		if (resources != null) {
			List<ReportResource> reportResources = top10list2RR(JSON.toJSONString(resources), chapter);
			if (reportResources != null && reportResources.size() > 0) {
				for (int i = 0; i < reportResources.size(); i++) {
					insertListDataIntoResources(-1, userId, chapter, chapterPosition, reportNew.getId(),
							reportResourcesList, reportResources.get(i));
				}

				// 资源池中完全没有数据，新加入数据时走这里
				if (reportResourcesList != null && reportResourcesList.size() > 0) {
					for (int i = 0; i < reportResourcesList.size(); i++) {
						reportResourcesList.get(i).setDocPosition(i + 1);
					}
				}

			}
		}
		// 计算转载数
		// produceRttCount(reportResourcesList);
		return reportResourcesList;
	}

	/**
	 * 计算转载数
	 * 
	 * @author shao.guangze
	 * @param reportResourcePool
	 */
	// private void produceRttCount(List<ReportResource> reportResourcePool) {
	// for (int i = 0; i < reportResourcePool.size(); i++) {
	// ReportResource reportResource = reportResourcePool.get(i);
	// if (reportResource.getMd5Tag() == null) {
	// reportResource.setRttCount(new Long(0));
	// reportResourcePool.remove(i);
	// reportResourcePool.add(i, reportResource);
	// continue;
	// }
	// String md5queryTRSL = FtsFieldConst.FIELD_MD5TAG + ":"+
	// reportResource.getMd5Tag();
	// QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
	// queryBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
	// queryBuilder.filterByTRSL(md5queryTRSL);
	// // 昨天0点到当前时间
	// queryBuilder.filterByTRSL(FtsFieldConst.FIELD_URLTIME + ":"
	// + getFormatDateTRSL());
	// long ftsCount = hybase8SearchService.ftsCountCommon(
	// queryBuilder, false,false);
	// reportResource.setRttCount(ftsCount);
	// reportResourcePool.remove(i);
	// reportResourcePool.add(i, reportResource);
	// }
	// }

	/**
	 * 获取昨天0点到当前时间的 时间数组， 24h+
	 *
	 * @author shao.guangze
	 * @return
	 */
	// private String getFormatDateTRSL() {
	// Calendar calendar = Calendar.getInstance();
	// String currentDate = new SimpleDateFormat("yyyyMMddHHmmss")
	// .format(calendar.getTime());
	// calendar.add(Calendar.DAY_OF_MONTH, -1);
	// String yesterdayDate = new SimpleDateFormat("yyyyMMddHHmmss")
	// .format(calendar.getTime());
	// // 获取到昨天
	// yesterdayDate = yesterdayDate.substring(0, 8) + "000000";
	// return "[" + yesterdayDate + " TO " + currentDate + "]";
	// }

	private String generateActiveTrsl(String key, String timeRange, String keyWords, String excludeWords,String excludeWebs,
			String keyWordsIndex, boolean weight) throws OperationException {

		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		StringBuffer trsl = new StringBuffer();
		if (key.equals(WEIBOACTIVETOP10key)) {
			String trslTemp = createFilter(keyWords, "0", excludeWords,excludeWebs, weight).asTRSL();
			if (StringUtil.isEmpty(trslTemp)) {
				trsl.append("(IR_GROUPNAME:(微博)");
			} else {
				trsl.append(trslTemp.substring(0, trslTemp.length() - 1));// 把最后1个
																			// )
																			// 去掉
				trsl.append(" AND (IR_GROUPNAME:(微博)) ");
			}
		} else if (key.equals(WECHATACTIVETOP10key)) {
			String trslTemp = createFilter(keyWords, keyWordsIndex.toString(), excludeWords,excludeWebs, weight).asTRSL();
			if (StringUtil.isEmpty(trslTemp)) {
				trsl.append("(IR_GROUPNAME:(国内微信)");
			} else {
				trsl.append(trslTemp.substring(0, trslTemp.length() - 1));// 把最后1个
																			// )
																			// 去掉
				trsl.append(" AND (IR_GROUPNAME:(国内微信)) ");
			}
		}
		trsl.append(" AND (IR_URLTIME:[ " + timeArray[0] + " TO " + timeArray[1] + "])");
		trsl.append(")");// 把最后1个 ) 加上

		return trsl.toString();
	}

	/**
	 * 根据类型生成对应表达式
	 * 
	 * @since changjiang @ 2018年6月14日
	 */
	private String generateActiveTrsl(String key, String trsl, String timeRange) throws OperationException {

		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		StringBuilder trslSb = new StringBuilder(trsl);
		if (key.equals(WEIBOACTIVETOP10key)) {
			trslSb.append(" AND (IR_GROUPNAME:(微博)) ");
		} else if (key.equals(WECHATACTIVETOP10key)) {
			trslSb.append(" AND (IR_GROUPNAME:(国内微信)) ");
		}
		trslSb.append(" AND (IR_URLTIME:[ " + timeArray[0] + " TO " + timeArray[1] + "])");

		return trslSb.toString();
	}

	private QueryBuilder createFilter(String keyWords, String keyWordindex, String excludeWords,String excludeWebs, boolean weight) {
		QueryBuilder queryBuilder = WordSpacingUtil.handleKeyWords(keyWords, keyWordindex, weight);
		//拼接排除词
		if (keyWordindex.trim().equals("1")) {// 标题加正文
			if (StringUtil.isNotEmpty(excludeWords)) {
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
						.append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				queryBuilder.filterByTRSL(exbuilder.toString());
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				queryBuilder.filterByTRSL(exbuilder2.toString());
			}
		}else {//仅标题
			if (StringUtil.isNotEmpty(excludeWords)) {
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
						.append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				queryBuilder.filterByTRSL(exbuilder.toString());
			}
		}
		//排除网站
		if (StringUtil.isNotEmpty(excludeWebs)) {
			queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, excludeWebs.replaceAll(";|；", " OR "), Operator.NotEqual);
		}
		return queryBuilder;
	}


	private void groupResult2MapList(GroupResult groupResult, List<Map<String, Object>> mapList, String groupName)
			throws Exception {
		if (groupResult != null && groupResult.getGroupList() != null && groupResult.getGroupList().size() > 0) {
			List<GroupInfo> groupList = groupResult.getGroupList();
			// 根据Uid再去查SCREEN_NAME
			for (GroupInfo groupInfo : groupList) {
				if ("微博".equals(groupName)) {
					String uidTrsl = "IR_UID:(?)";
					uidTrsl = uidTrsl.replace("?", groupInfo.getFieldValue());
					QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
					queryBuilder.setDatabase(new String[] { Const.WEIBO });
					queryBuilder.setAppendTRSL(new java.lang.StringBuilder().append(uidTrsl));
					PagedList<FtsDocumentCommonVO> queryList = hybase8SearchService.pageListCommon(queryBuilder, false,
							false,false,"detail");
					groupInfo.setFieldValue(queryList.getPageItems().get(0).getScreenName());
					queryBuilder.setAppendTRSL(new java.lang.StringBuilder().append(uidTrsl));
				}
				Map<String, Object> putValue = MapUtil.putValue(new String[] { "groupName", "group", "num" },
						groupInfo.getFieldValue(), groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
				mapList.add(putValue);
			}
		}
	}


}
