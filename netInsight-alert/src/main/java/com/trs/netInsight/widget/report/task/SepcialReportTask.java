package com.trs.netInsight.widget.report.task;

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
import com.trs.netInsight.widget.report.constant.ReportConst;
import com.trs.netInsight.widget.report.entity.ReportDataNew;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.repository.ReportDataNewRepository;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.service.IInfoListService;

import lombok.extern.slf4j.Slf4j;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;

/**
 *  专报
 *  @author 北京拓尔思信息技术股份有限公司
 * Created by shao.guangze on 2018年6月21日 下午9:11:31
 */
@Slf4j
public class SepcialReportTask implements Runnable{

	private ChartAnalyzeService chartAnalyzeService = (ChartAnalyzeService) ObjectContainer.getBean(ChartAnalyzeService.class);

	private IInfoListService infoListService = (IInfoListService) ObjectContainer.getBean(IInfoListService.class);

	private IDistrictInfoService districtInfoService = (IDistrictInfoService) ObjectContainer.getBean(IDistrictInfoService.class);

	private ReportDataNewRepository reportDataNewRepository = (ReportDataNewRepository) ObjectContainer.getBean(ReportDataNewRepository.class);

	private FullTextSearch hybase8SearchService = (Hybase8SearchImpl) ObjectContainer.getBean(Hybase8SearchImpl.class);

	private String keyWords;
	private String excludeWords;
	private Integer keyWordsIndex;
	private String excludeWebs;
	private String simflag;
	private String timeRange;
	private String trsl;
	private Integer searchType;
	private ReportDataNew reportData;
	private boolean server;
	private boolean weight;
	private String userId;

	public SepcialReportTask(boolean server,boolean weight, String keyWords,
			String excludeWords, Integer keyWordsIndex, String excludeWebs,
			String simflag, String timeRange, String trsl,Integer searchType,ReportDataNew reportData,String userId){
		this.keyWords = keyWords;
		this.excludeWords = excludeWords;
		this.keyWordsIndex = keyWordsIndex;
		this.excludeWebs = excludeWebs;
		this.simflag = simflag;
		this.timeRange = timeRange;
		this.trsl = trsl;
		this.searchType = searchType;
		this.reportData = reportData;
		this.server = server;
		this.weight = weight;
        this.userId = userId;
	}

	public void run(){
		long startMillis ;
		long endMillis ;
		long allSearchStartMillis;
		long allSearchEndMillis;
		Object overviewOfDataResult = null;
		allSearchStartMillis = System.currentTimeMillis();
		log.info("专报查询开始：  " + allSearchStartMillis);
		//TODO 专报容错处理，现计算阶段已经完成，大致思路已经清晰(见setEmptyData)，需要前端对stPreview和specialPreview两个接口做联调，listPreview测试，word处理。
		for(String chapter : CHAPTERS){
			switch (chapter) {
			case REPORTINTROkey:
				break;
			case OVERVIEWOFDATAkey:
				//饼图
				log.info(String.format(SPECILAREPORTLOG , OVERVIEWOFDATA));
				startMillis = System.currentTimeMillis();
				IndexTab overviewOfDataIT = searchType == 0 ? createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight,REPORTCHARTDATASIZE) : createIndexTab(trsl,simflag, timeRange, weight ,REPORTCHARTDATASIZE);
				String groupName = "国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook";
				overviewOfDataIT.setGroupName(groupName);
				overviewOfDataIT.setType(ColumnConst.CHART_PIE_BY_META);
				overviewOfDataIT.setContrast(ColumnConst.CONTRAST_TYPE_GROUP);
				try {
					overviewOfDataResult = columnSearch(overviewOfDataIT, REPORTCHARTDATASIZE,groupName);
					endMillis = System.currentTimeMillis();
					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, OVERVIEWOFDATA , (endMillis - startMillis)));

					ReportResource overviewRR = new ReportResource();
					overviewRR.setImgComment(ReportUtil.getOverviewOfData(JSON.toJSONString(overviewOfDataResult)));
					reportData.setOverviewOfdata(overviewRR.getImgComment());
				} catch (Exception e) {
					setEmptyData(reportData, ReportConst.SINGLERESOURCE,OVERVIEWOFDATAkey);
					log.error(OVERVIEWOFDATA , e);
				}
				reportDataNewRepository.saveOverviewOfdata(reportData.getOverviewOfdata(), reportData.getId());
				break;
//			case NEWSHOTTOP10key://新闻热点TOP10
//				//列表数据
//				log.info(String.format(SPECILAREPORTLOG , NEWSTOP10));
//				startMillis = System.currentTimeMillis();
//				IndexTab newsTOP10IT = searchType == 0 ?  createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight,REPORTLISTDATASIZE) :createIndexTab(trsl,simflag, timeRange, weight,REPORTLISTDATASIZE);
//				newsTOP10IT.setGroupName("传统媒体");
//				newsTOP10IT.setTradition("国内新闻");
//				newsTOP10IT.setType(ColumnConst.LIST_NO_SIM);
//				try {
//					//throw new Exception("gggg");
//					Object newTOP10Result = columnSearch(newsTOP10IT, REPORTLISTDATASIZE,"国内新闻");
//					endMillis = System.currentTimeMillis();4
//					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, NEWSTOP10 , (endMillis - startMillis)));
//					//把日常监测出来的结果处理成List<ReportResource> 然后再转为jsonStr，编码问题已解决
//					reportData.setNewsTop10(ReportUtil.replaceHtml(JSON.toJSONString(top10list2RR(JSON.toJSONString(newTOP10Result),NEWSTOP10))));
//				} catch (Exception e) {
//					setEmptyData(reportData, ReportConst.LISTRESOURCES,NEWSTOP10key);
//					log.error(NEWSTOP10 , e);
//				}
//				reportDataNewRepository.saveNewsTop10(reportData.getNewsTop10(), reportData.getId());
//				break;
//			case WEIBOTOP10key:
//				//列表数据
//				log.info(String.format(SPECILAREPORTLOG , WEIBOTOP10));
//				startMillis = System.currentTimeMillis();
//				IndexTab weiboTOP10IT = searchType == 0 ?  createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight,REPORTLISTDATASIZE) :createIndexTab(trsl,simflag, timeRange, weight,REPORTLISTDATASIZE);
//				weiboTOP10IT.setGroupName("微博");
//				weiboTOP10IT.setTradition("");
//				weiboTOP10IT.setType(ColumnConst.LIST_STATUS_COMMON);
//				try {
//					Object weiboTOP10Result = columnSearch(weiboTOP10IT, REPORTLISTDATASIZE,"微博");
//					endMillis = System.currentTimeMillis();
//					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEIBOTOP10 , (endMillis - startMillis)));
//
//					reportData.setWeiboTop10(ReportUtil.replaceHtml(JSON.toJSONString(top10list2RR(JSON.toJSONString(weiboTOP10Result),WEIBOTOP10))));
//				} catch (Exception e) {
//					setEmptyData(reportData, ReportConst.LISTRESOURCES,WEIBOTOP10key);
//					log.error(WEIBOTOP10 , e);
//				}
//				reportDataNewRepository.saveWeiboTop10(reportData.getWeiboTop10(), reportData.getId());
//				break;
//			case WECHATTOP10key:
//				//列表数据
//				log.info(String.format(SPECILAREPORTLOG , WECHATTOP10));
//				startMillis = System.currentTimeMillis();
//				IndexTab wechatTOP10IT = searchType == 0 ?  createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight,REPORTLISTDATASIZE) :createIndexTab(trsl,simflag, timeRange, weight,REPORTLISTDATASIZE);
//				wechatTOP10IT.setGroupName("微信");
//				wechatTOP10IT.setTradition("");
//				wechatTOP10IT.setType(ColumnConst.LIST_WECHAT_COMMON);
//				try {
//					Object wechatTOP10Result = columnSearch(wechatTOP10IT, REPORTLISTDATASIZE,"微信");
//					endMillis = System.currentTimeMillis();
//					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WECHATTOP10 , (endMillis - startMillis)));
//
//					reportData.setWechatTop10(ReportUtil.replaceHtml(JSON.toJSONString(top10list2RR(JSON.toJSONString(wechatTOP10Result),WECHATTOP10))));
//				} catch (Exception e) {
//					setEmptyData(reportData, ReportConst.LISTRESOURCES,WECHATTOP10key);
//					log.error(WECHATTOP10 , e);
//				}
//				reportDataNewRepository.saveWechatTop10(reportData.getWechatTop10(), reportData.getId());
//				break;
				case NEWSHOTTOP10key://（专题报 改造 20191121）
					//列表，新闻热点TOP10
					log.info(String.format(SPECILAREPORTLOG , NEWSHOTTOP10));
					startMillis = System.currentTimeMillis();
					IndexTab newsHotIT = searchType == 0 ? createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight, REPORTLISTDATASIZE) : createIndexTab(trsl, simflag, timeRange, weight,REPORTLISTDATASIZE);
					//不排重
					newsHotIT.setIrSimflagAll(false);
					//日常监测 默认irsimflag排重  和日常监测保持一致
					newsHotIT.setIrSimflag(false);
					newsHotIT.setSimilar(false);
					newsHotIT.setGroupName("传统媒体");
					newsHotIT.setTradition("国内新闻");
					newsHotIT.setType(ColumnConst.LIST_SIM);
					try {
						Object newsHotResult = columnSearch(newsHotIT, REPORTLISTDATASIZE,"国内新闻");
						endMillis = System.currentTimeMillis();
						log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, NEWSHOTTOP10 , (endMillis - startMillis)));

						reportData.setNewsHotTopics(ReportUtil.replaceHtml(JSON.toJSONString(top10list2RR(JSON.toJSONString(newsHotResult),NEWSHOTTOP10))));
					} catch (Exception e) {
						setEmptyData(reportData, ReportConst.LISTRESOURCES,NEWSHOTTOP10key);
						log.error(NEWSHOTTOP10 , e);
					}
					reportDataNewRepository.saveNewsHotTopics(reportData.getNewsHotTopics(), reportData.getId());
					break;
				case WEIBOHOTTOP10key://（专题报 改造 20191121）
					//列表，微博热点TOP10
					log.info(String.format(SPECILAREPORTLOG , WEIBOHOTTOPICS));
					startMillis = System.currentTimeMillis();
					IndexTab weiboHotIT = searchType == 0 ?  createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight, REPORTLISTDATASIZE) : createIndexTab(trsl, simflag, timeRange, weight, REPORTLISTDATASIZE);
					//不排重
					weiboHotIT.setIrSimflagAll(false);
					weiboHotIT.setIrSimflag(false);
					weiboHotIT.setSimilar(false);
					weiboHotIT.setGroupName("微博");
					weiboHotIT.setTradition("微博");
					weiboHotIT.setType(ColumnConst.LIST_SIM);
					try {
						Object weiboHotResult = columnSearch(weiboHotIT, REPORTLISTDATASIZE,"微博");
						endMillis = System.currentTimeMillis();
						log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEIBOHOTTOP10 , (endMillis - startMillis)));

						reportData.setWeiboHotTopics(ReportUtil.replaceHtml(JSON.toJSONString(top10list2RR(JSON.toJSONString(weiboHotResult),WEIBOHOTTOP10))));
					} catch (Exception e) {
						setEmptyData(reportData, ReportConst.LISTRESOURCES,WEIBOHOTTOP10key);
						log.error(WEIBOHOTTOP10 , e);
					}
					reportDataNewRepository.saveWeiboHotTopics(reportData.getWeiboHotTopics(), reportData.getId());
					break;
				case WECHATHOTTOP10key://（专题报 改造 20191121）
					//列表，微博热点TOP10
					log.info(String.format(SPECILAREPORTLOG , WECHATHOTTOP10));
					startMillis = System.currentTimeMillis();
					IndexTab weChatHotIT = searchType == 0 ?  createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight, REPORTLISTDATASIZE) : createIndexTab(trsl, simflag, timeRange, weight, REPORTLISTDATASIZE);
					//不排重
					weChatHotIT.setIrSimflagAll(false);
					weChatHotIT.setIrSimflag(false);
					weChatHotIT.setSimilar(false);
					weChatHotIT.setGroupName("国内微信");
					weChatHotIT.setTradition("国内微信");
					weChatHotIT.setType(ColumnConst.LIST_SIM);
					try {
						Object weChatHotResult = columnSearch(weChatHotIT, REPORTLISTDATASIZE,"国内微信");
						endMillis = System.currentTimeMillis();
						log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WECHATHOTTOP10 , (endMillis - startMillis)));

						reportData.setWechatHotTop10(ReportUtil.replaceHtml(JSON.toJSONString(top10list2RR(JSON.toJSONString(weChatHotResult),WECHATHOTTOP10))));
					} catch (Exception e) {
						setEmptyData(reportData, ReportConst.LISTRESOURCES,WECHATHOTTOP10key);
						log.error(WECHATHOTTOP10 , e);
					}
					reportDataNewRepository.saveWechatHotTop10(reportData.getWechatHotTop10(), reportData.getId());
					break;
			case DATATRENDANALYSISkey:
				//折线图
				log.info(String.format(SPECILAREPORTLOG , DATATRENDANALYSIS));
				startMillis = System.currentTimeMillis();
				IndexTab dataTrendIT = searchType == 0 ?  createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight,REPORTCHARTDATASIZE) :createIndexTab(trsl,simflag, timeRange, weight,REPORTCHARTDATASIZE);
				dataTrendIT.setGroupName("国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
				dataTrendIT.setType(ColumnConst.CHART_LINE);
				dataTrendIT.setContrast(ColumnConst.CONTRAST_TYPE_GROUP);
				try {
					Object dataTrendResult = columnSearch(dataTrendIT, REPORTCHARTDATASIZE,"国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
					endMillis = System.currentTimeMillis();
					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, DATATRENDANALYSIS , (endMillis - startMillis)));

					ReportResource dataTrendRR = new ReportResource();
					dataTrendRR.setImg_data(JSON.toJSONString(dataTrendResult));
					dataTrendRR.setImgType("brokenLineChart");
					dataTrendRR.setImgComment(ReportUtil.getImgComment(dataTrendRR.getImg_data(),dataTrendRR.getImgType(),DATATRENDANALYSIS));
					dataTrendRR.setId(UUID.randomUUID().toString().replace("-", ""));

					reportData.setDataTrendAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataTrendRR))));
				} catch (Exception e) {
					setEmptyData(reportData, ReportConst.CHART,DATATRENDANALYSISkey);
					log.error(DATATRENDANALYSIS , e);
				}
				reportDataNewRepository.saveDataTrendAnalysis(reportData.getDataTrendAnalysis(), reportData.getId());
				break;
			case DATASOURCEANALYSISkey:
				log.info(String.format(SPECILAREPORTLOG , DATASOURCEANALYSIS));
				startMillis = System.currentTimeMillis();
				Object dataSourceResult = overviewOfDataResult;
				if(reportData.getOverviewOfdata().equals("暂无数据")){
					dataSourceResult = "暂无数据";
				}
				endMillis = System.currentTimeMillis();
				log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, DATASOURCEANALYSIS , (endMillis - startMillis)));

				ReportResource dataSourceRR = new ReportResource();
				dataSourceRR.setImg_data(JSON.toJSONString(dataSourceResult));
				dataSourceRR.setImgType("pieGraphChartMeta");
				dataSourceRR.setImgComment(ReportUtil.getImgComment(dataSourceRR.getImg_data(),dataSourceRR.getImgType(),DATASOURCEANALYSIS));
				dataSourceRR.setId(UUID.randomUUID().toString().replace("-", ""));

				reportData.setDataSourceAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(dataSourceRR))));
				try {
					reportDataNewRepository.saveDataSourceAnalysis(reportData.getDataSourceAnalysis(), reportData.getId());
				} catch (Exception e) {
					setEmptyData(reportData, ReportConst.CHART,DATASOURCEANALYSISkey);
					log.error(DATASOURCEANALYSIS , e);
				}
				break;
			case WEBSITESOURCETOP10key:
				//柱状图
				log.info(String.format(SPECILAREPORTLOG , WEBSITESOURCETOP10));
				startMillis = System.currentTimeMillis();
				IndexTab websiteSourceIT =  searchType == 0 ?  createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight,REPORTCHARTDATASIZE) :createIndexTab(trsl,simflag, timeRange, weight,REPORTCHARTDATASIZE);
				websiteSourceIT.setGroupName("传统媒体");
				websiteSourceIT.setTradition("国内新闻");
				websiteSourceIT.setContrast(ColumnConst.CONTRAST_TYPE_SITE);
				websiteSourceIT.setType(ColumnConst.CHART_BAR_BY_META);
				try {
					Object websiteSourceResult = columnSearch(websiteSourceIT, REPORTCHARTDATASIZE,"国内新闻");
					endMillis = System.currentTimeMillis();
					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEBSITESOURCETOP10 , (endMillis - startMillis)));

					ReportResource websiteSourceRR = new ReportResource();
					websiteSourceRR.setImg_data(JSON.toJSONString(websiteSourceResult));
					websiteSourceRR.setImgType("barGraphChartMeta");
					websiteSourceRR.setImgComment(ReportUtil.getImgComment(websiteSourceRR.getImg_data(),websiteSourceRR.getImgType(),WEBSITESOURCETOP10));
					websiteSourceRR.setId(UUID.randomUUID().toString().replace("-", ""));

					reportData.setWebsiteSourceTop10(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(websiteSourceRR))));
				} catch (Exception e) {
					setEmptyData(reportData, ReportConst.CHART,WEBSITESOURCETOP10key);
					log.error(WEBSITESOURCETOP10 , e);
				}
				reportDataNewRepository.saveWebsiteSourceTop10(reportData.getWebsiteSourceTop10(), reportData.getId());
				break;
			case WEIBOACTIVETOP10key:
				log.info(String.format(SPECILAREPORTLOG , WEIBOACTIVETOP10));

				startMillis = System.currentTimeMillis();
				try {
					String weiboActiceTrsl = searchType == 0 ? generateActiveTrsl(WEIBOACTIVETOP10key, timeRange, keyWords, excludeWords,excludeWebs, keyWordsIndex, weight) : generateActiveTrsl(WEIBOACTIVETOP10key, trsl, timeRange);
					GroupResult weiboActiceGroupResult = hybase8SearchService.categoryQuery(server,weiboActiceTrsl, false, false, false,FtsFieldConst.FIELD_UID, REPORTCHARTDATASIZE,
							null,Const.WEIBO);
					List<Map<String, Object>> weiboActiceList = new ArrayList<>();
					groupResult2MapList(weiboActiceGroupResult, weiboActiceList, "微博");
					endMillis = System.currentTimeMillis();
					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WEIBOACTIVETOP10 , (endMillis - startMillis)));

					ReportResource weiboActiveRR = new ReportResource();
					weiboActiveRR.setImg_data(JSON.toJSONString(weiboActiceList));
					weiboActiveRR.setImgType("pieGraphChartMeta");
					weiboActiveRR.setImgComment(ReportUtil.getImgComment(weiboActiveRR.getImg_data(),weiboActiveRR.getImgType(),WEIBOACTIVETOP10));
					weiboActiveRR.setId(UUID.randomUUID().toString().replace("-", ""));

					reportData.setWeiboActiveTop10(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(weiboActiveRR))));
				} catch (Exception e) {
					setEmptyData(reportData, ReportConst.CHART,WEIBOACTIVETOP10key);
					log.error(WEIBOACTIVETOP10 , e);
				}
				reportDataNewRepository.saveWeiboActiveTop10(reportData.getWeiboActiveTop10(), reportData.getId());
				break;
			case WECHATACTIVETOP10key:
				log.info(String.format(SPECILAREPORTLOG , WECHATACTIVETOP10));

				startMillis = System.currentTimeMillis();
				try {
					String wechatActiveTrsl = searchType == 0 ? generateActiveTrsl(WECHATACTIVETOP10key, timeRange, keyWords, excludeWords,excludeWebs, keyWordsIndex, weight) : generateActiveTrsl(WECHATACTIVETOP10key, trsl, timeRange);
					GroupResult wechatActiveGroupResult = hybase8SearchService.categoryQuery(server,wechatActiveTrsl, false, false, false,FtsFieldConst.FIELD_SITENAME, REPORTCHARTDATASIZE,
							null, Const.WECHAT_COMMON);
					List<Map<String, Object>> wechatList = new ArrayList<>();
					groupResult2MapList(wechatActiveGroupResult, wechatList, "国内微信");
					endMillis = System.currentTimeMillis();
					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, WECHATACTIVETOP10 , (endMillis - startMillis)));

					ReportResource wechatActiveRR = new ReportResource();
					wechatActiveRR.setImg_data(JSON.toJSONString(wechatList));
					wechatActiveRR.setImgType("pieGraphChartMeta");
					wechatActiveRR.setImgComment(ReportUtil.getImgComment(wechatActiveRR.getImg_data(),wechatActiveRR.getImgType(), WECHATACTIVETOP10));
					wechatActiveRR.setId(UUID.randomUUID().toString().replace("-", ""));

					reportData.setWechatActiveTop10(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(wechatActiveRR))));
				} catch (Exception e) {
					setEmptyData(reportData, ReportConst.CHART,WECHATACTIVETOP10key);
					log.error(WECHATACTIVETOP10 , e);
				}
				reportDataNewRepository.saveWechatActiveTop10(reportData.getWechatActiveTop10(), reportData.getId());
				break;
			case AREAkey:
				//地图
				log.info(String.format(SPECILAREPORTLOG , AREA));
				startMillis = System.currentTimeMillis();
				IndexTab areaIT = searchType == 0 ? createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight,REPORTCHARTDATASIZE) : createIndexTab(trsl, simflag, timeRange, weight,REPORTCHARTDATASIZE);
				//areaIT.setGroupName("传统媒体");
				areaIT.setGroupName("国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
				areaIT.setTradition("");
				areaIT.setType(ColumnConst.CHART_MAP);
				try {
					Object areaResult = columnSearch(areaIT, REPORTLISTDATASIZE,"传统媒体");
					endMillis = System.currentTimeMillis();
					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, AREA , (endMillis - startMillis)));

					ReportResource areaRR = new ReportResource();
					areaRR.setImg_data(JSON.toJSONString(areaResult));
					areaRR.setImgType("mapChart");
					areaRR.setImgComment(ReportUtil.getImgComment(areaRR.getImg_data(),areaRR.getImgType(),AREA));
					areaRR.setId(UUID.randomUUID().toString().replace("-", ""));

					reportData.setArea(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(areaRR))));
				} catch (Exception e) {
					setEmptyData(reportData, ReportConst.CHART,AREAkey);
					log.error(AREA , e);
				}
				reportDataNewRepository.saveArea(reportData.getArea(), reportData.getId());
				break;
			case EMOTIONANALYSISkey:
				//情感分析,走专家模式, 饼图
				log.info(String.format(SPECILAREPORTLOG , EMOTIONANALYSIS));
				startMillis = System.currentTimeMillis();
				String emotionTrsl = "";
				if(searchType == 0){
					emotionTrsl = createFilter(keyWords, keyWordsIndex.toString(), excludeWords,excludeWebs, false).asTRSL();

				}
				IndexTab emotionIT = searchType == 0 ? createIndexTab(keyWords,excludeWords, keyWordsIndex, excludeWebs,simflag, timeRange, weight,REPORTCHARTDATASIZE) : createIndexTab(trsl,simflag, timeRange, weight,REPORTCHARTDATASIZE);
				emotionIT.setKeyWord(null);
				emotionIT.setKeyWordIndex(null);
				emotionIT.setExcludeWeb(null);
				emotionIT.setExcludeWords(null);
				emotionIT.setGroupName("国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
				emotionIT.setType(ColumnConst.CHART_PIE_BY_META);
				emotionIT.setTrsl(searchType == 0 ?emotionTrsl : trsl);
				emotionIT.setXyTrsl("正面=IR_APPRAISE:正面;\n中性=IR_APPRAISE:中性;\n负面=IR_APPRAISE:负面");
				try {
					Object emotionResult = columnSearch(emotionIT, REPORTCHARTDATASIZE,"国内新闻;微博;微信;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体;Twitter;FaceBook");
					endMillis = System.currentTimeMillis();
					log.info(String.format(SPECILAREPORTLOG + SPECIALREPORTTIMELOG, EMOTIONANALYSIS , (endMillis - startMillis)));

					ReportResource emotionRR = new ReportResource();
					emotionRR.setImg_data(JSON.toJSONString(emotionResult));
					emotionRR.setImgType("pieGraphChartMeta");
					emotionRR.setImgComment(ReportUtil.getImgComment(emotionRR.getImg_data(),emotionRR.getImgType(),EMOTIONANALYSIS));
					emotionRR.setId(UUID.randomUUID().toString().replace("-", ""));

					reportData.setEmotionAnalysis(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(emotionRR))));
				} catch (Exception e) {
					setEmptyData(reportData, ReportConst.CHART,EMOTIONANALYSISkey);
					log.error(EMOTIONANALYSIS , e);
				}
				reportDataNewRepository.saveEmotionAnalysis(reportData.getEmotionAnalysis(), reportData.getId());
				break;

			default:
				break;
			}
		}
		allSearchEndMillis = System.currentTimeMillis();
		log.info("专报查询结束：  " + allSearchEndMillis);
		log.info("专报查询耗时：  " + (allSearchEndMillis - allSearchStartMillis));

		reportData.setDoneFlag(1);
		reportDataNewRepository.saveDoneFlag(1, reportData.getId());
	}

	/***
	 * 意思是该模块的数据已经被计算过，但没计算出数据来
	 * 如果不做处理直接是null的话，你不能知道该章节是没计算还是计算出异常了
	 * @param reportData
	 * @param chapterType
	 * @param chapterDetail
	 */
	private void setEmptyData(ReportDataNew reportData, String chapterType, String chapterDetail) {
		if(ReportConst.SINGLERESOURCE.equals(chapterType)){
			reportData.setOverviewOfdata("暂无数据");
		}else{
			ReportResource emptyResource = new ReportResource();
			ArrayList<ReportResource> resources = new ArrayList<>();
			resources.add(emptyResource);
			String data = JSONArray.toJSONString(resources);
			try{
				reportData.getClass().getDeclaredMethod(CHAPTERS2METHODSETNEW.get(chapterDetail),String.class).invoke(reportData,data);
			}catch (Exception e){
				e.printStackTrace();
				log.error("存储");
			}
		}
	}

	private void groupResult2MapList(GroupResult groupResult, List<Map<String,Object>> mapList, String groupName) throws Exception {
		if (groupResult != null && groupResult.getGroupList()!=null && groupResult.getGroupList().size()>0) {
			List<GroupInfo> groupList = groupResult.getGroupList();
			//根据Uid再去查SCREEN_NAME
			for (GroupInfo groupInfo : groupList) {
				if("微博".equals(groupName)){
					String uidTrsl = "IR_UID:(?)";
					uidTrsl = uidTrsl.replace("?", groupInfo.getFieldValue());
					QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
					queryBuilder.setDatabase(new String[]{Const.WEIBO});
					queryBuilder.setAppendTRSL(new java.lang.StringBuilder().append(uidTrsl));
					PagedList<FtsDocumentCommonVO> queryList = hybase8SearchService.pageListCommon(queryBuilder, false, false,false,null);
					groupInfo.setFieldValue(queryList.getPageItems().get(0).getScreenName());queryBuilder.setAppendTRSL(new java.lang.StringBuilder().append(uidTrsl));
				}
				Map<String, Object> putValue = MapUtil.putValue(new String[] { "groupName", "group", "num" },
						groupInfo.getFieldValue(), groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
				mapList.add(putValue);
			}
		}
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

	private IndexTab createIndexTab(String keyWords,
			String excludeWords, Integer keyWordsIndex, String excludeWebs,
			String simflag, String timeRange,boolean weight,Integer maxSize){
		IndexTab indexTab = new IndexTab();
		indexTab.setKeyWord(keyWords);
		indexTab.setKeyWordIndex(keyWordsIndex.toString());// 0 或 1
		indexTab.setExcludeWords(excludeWords);
		indexTab.setExcludeWeb(excludeWebs);
		indexTab.setTimeRange(timeRange);
		indexTab.setHide(false);
		indexTab.setMaxSize(maxSize);
		indexTab.setWeight(weight);
		setIndexTabSimilar(indexTab, simflag);
		return indexTab;
	}

	private void setIndexTabSimilar(IndexTab indexTab, String simflag) {
		if("no".equals(simflag)){
			//不排重
			indexTab.setIrSimflagAll(false);
			indexTab.setIrSimflag(false);
			indexTab.setSimilar(false);
		}else if("netRemove".equals(simflag)){
			//全网排重，现改名为单一网站排重
			indexTab.setIrSimflagAll(false);
			indexTab.setIrSimflag(false);
			indexTab.setSimilar(true);
		}else if("urlRemove".equals(simflag)){
			//url排重，现改名为站内排重
			indexTab.setIrSimflagAll(false);
			indexTab.setIrSimflag(true);
			indexTab.setSimilar(false);
		}else if("sourceRemove".equals(simflag)){
			//跨数据源排重，现改名为全网排重
			indexTab.setIrSimflagAll(true);
			indexTab.setIrSimflag(false);
			indexTab.setSimilar(false);

		}
	}

	/**
	 * 高级模式模拟生成indexTab
	 * @since changjiang @ 2018年6月14日
	 */
	private IndexTab createIndexTab(String trsl, String simflag, String timeRange, boolean weightm,Integer maxSize){
		IndexTab indexTab = new IndexTab();
		indexTab.setTrsl(trsl);
		setIndexTabSimilar(indexTab, simflag);
		indexTab.setTimeRange(timeRange);
		indexTab.setHide(false);
		indexTab.setMaxSize(maxSize);
		indexTab.setWeight(weight);
		return indexTab;
	}

	/**
	 * 将TOP10类型的列表json数据转换为List<ReportResource>并且给位置信息赋值
	 * @author shao.guangze
	 */
	private List<ReportResource> top10list2RR(String jsonData, String chapter){
		if(StringUtils.isEmpty(jsonData)){
			return null;
		}
		jsonData = StringUtil.removeFourChar(jsonData);
		List<Map<String, String>> lists = JSONObject.parseObject(jsonData,new TypeReference<List<Map<String, String>>>(){});
		List<ReportResource> listResult = new ArrayList<>();
		int position = 0;
		for(Map<String, String> map : lists){
			ReportResource reportResource = new ReportResource();
			reportResource.setMd5Tag(map.get("md5Tag"));
			if(StringUtil.isNotEmpty(map.get("urlTime")) && (new Long(map.get("urlTime")) > 345398400000L)){
				reportResource.setUrlDate(new Date(new Long(map.get("urlTime"))));
			}else{
				reportResource.setUrlDate(timeAgo2urlDate(map.get("timeAgo")));
			}
			//位置信息赋值
			if("新闻网站TOP10".equals(chapter) || "微博TOP10".equals(chapter) || "微信TOP10".equals(chapter) || "新闻热点话题".equals(chapter) || "微博热点话题".equals(chapter)|| "新闻热点TOP10".equals(chapter) || "微博热点TOP10".equals(chapter) || "微信热点TOP10".equals(chapter)){
				reportResource.setDocPosition( ++position );
			}
			//专题报 改造  20191121
			if ("新闻热点TOP10".equals(chapter) || "微博热点TOP10".equals(chapter) || "微信热点TOP10".equals(chapter)){
				reportResource.setSimCount(map.get("simCount"));
			}
			reportResource.setTimeAgo(map.get("timeAgo"));
			reportResource.setSiteName(map.get("siteName"));
			reportResource.setSrcName(map.get("siteName"));
			reportResource.setTitle(map.get("title"));
			reportResource.setContent(StringUtil.isEmpty(map.get("content")) ? map.get("title") : map.get("content"));
			reportResource.setSid(map.get("sid"));
			reportResource.setGroupName(map.get("groupName"));
			reportResource.setReportType("专报");
			reportResource.setChapter(chapter);
			reportResource.setId(UUID.randomUUID().toString().replace("-", ""));
			//重新截取微博title
			if(chapter.contains("微博")){
				String content = ReportUtil.replaceHtml(map.get("content"));
				String subStr = "";
				if(content == null)
					content = ReportUtil.replaceHtml(map.get("title"));
				if(content.length() > 160){
					subStr = content.substring(0,160);
				}else{
					subStr = content.substring(0,content.length());
				}
				reportResource.setTitle(subStr);
			}
			listResult.add(reportResource);
		}
		return listResult;
	}
	private Date timeAgo2urlDate(String timeAgo){
		Calendar cal = Calendar.getInstance();
		if(StringUtils.isNotEmpty(timeAgo) && timeAgo.contains("分钟")){
			cal.add(Calendar.MINUTE, new Integer("-"+timeAgo.replace("分钟前", "")));
		}else if(StringUtils.isNotEmpty(timeAgo) && timeAgo.contains("小时")){
			cal.add(Calendar.HOUR_OF_DAY, new Integer("-"+timeAgo.replace("小时前", "")));
		}else if(StringUtils.isNotEmpty(timeAgo) && timeAgo.contains("天")){
			cal.add(Calendar.DAY_OF_MONTH, new Integer("-"+timeAgo.replace("天前", "")));
		}else if(StringUtils.isNotEmpty(timeAgo) && timeAgo.contains(".")){
			try{
				return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(timeAgo);
			}catch (Exception e){
				return cal.getTime();
			}
		}
		// Date -> 时间戳
		return cal.getTime();
	}
	/**
	 *  根据类型生成对应表达式
	 * @since changjiang @ 2018年6月14日
	 */
	private String generateActiveTrsl(String key, String trsl, String timeRange) throws OperationException {

		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		StringBuilder trslSb = new StringBuilder(trsl);
		String[] databases = null;
		if(key.equals(WEIBOACTIVETOP10key)){
			trslSb.append(" AND (IR_GROUPNAME:(微博)) ");
			databases = TrslUtil.chooseDatabases("微博".split(";"));

		}else if(key.equals(WECHATACTIVETOP10key)){
			trslSb.append(" AND (IR_GROUPNAME:(国内微信)) ");
			databases = TrslUtil.chooseDatabases("国内微信".split(";"));
		}
		trslSb.append(" AND (IR_URLTIME:[ " + timeArray[0] + " TO "+ timeArray[1] +"])");

		return trslSb.toString();
	}

	private Object columnSearch(IndexTab indexTab, Integer maxSize,String groupName) throws OperationException{
		AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
		ColumnConfig config = new ColumnConfig();
		config.setMaxSize(maxSize);
		config.initSection(indexTab, indexTab.getTimeRange(), 0, maxSize, null, null, "keywords", "", "", "desc", "", "", "", "", "","");
		column.setHybase8SearchService(hybase8SearchService);
		column.setChartAnalyzeService(chartAnalyzeService);
		column.setInfoListService(infoListService);
		column.setDistrictInfoService(districtInfoService);
		boolean mix = false ;
		column.setConfig(config);
		if(mix){
			return column.getSectionList();
		}else{
			return column.getColumnData(indexTab.getTimeRange());
		}

	}

	private String generateActiveTrsl(String key,
			String timeRange, String keyWords, String excludeWords,String excludeWebs, Integer keyWordsIndex, boolean weight) throws OperationException {

		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		StringBuffer trsl = new StringBuffer();
		String[] databases = null;
		if(key.equals(WEIBOACTIVETOP10key)){
			String trslTemp = createFilter(keyWords, "0", excludeWords,excludeWebs, weight).asTRSL();
			if(StringUtil.isEmpty(trslTemp)){
				trsl.append("(IR_GROUPNAME:(微博)");
			}else {
				trsl.append(trslTemp.substring(0,trslTemp.length()-1));//把最后1个 ) 去掉
				trsl.append(" AND (IR_GROUPNAME:(微博)) ");
			}
			databases = TrslUtil.chooseDatabases("微博".split(";"));
		}else if(key.equals(WECHATACTIVETOP10key)){
			String trslTemp = createFilter(keyWords, keyWordsIndex.toString(), excludeWords,excludeWebs, weight).asTRSL();
			if(StringUtil.isEmpty(trslTemp)){
				trsl.append("(IR_GROUPNAME:(国内微信)");
			}else {
				trsl.append(trslTemp.substring(0,trslTemp.length()-1));//把最后1个 ) 去掉
				trsl.append(" AND (IR_GROUPNAME:(国内微信)) ");
			}
			databases = TrslUtil.chooseDatabases("微博".split(";"));
		}
		trsl.append(" AND (IR_URLTIME:[ " + timeArray[0] + " TO "+ timeArray[1] +"])");
		trsl.append(")");//把最后1个 ) 加上


		return trsl.toString();
	}


}
