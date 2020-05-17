package com.trs.netInsight.widget.report.task;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.handler.exception.NullException;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.impl.Hybase8SearchImpl;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.entity.ChartAnalyzeEntity;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.report.entity.MaterialLibrary;
import com.trs.netInsight.widget.report.entity.Report;
import com.trs.netInsight.widget.report.entity.ReportData;
import com.trs.netInsight.widget.report.entity.TElement;
import com.trs.netInsight.widget.report.service.IReportService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Created by xiaoying on 2017年12月14日
 */
@Slf4j
public class BuildReportTask implements Runnable {

	private ChartAnalyzeService chartAnalyzeService = (ChartAnalyzeService) ObjectContainer
			.getBean(ChartAnalyzeService.class);

	private IReportService reportService = (IReportService) ObjectContainer.getBean(IReportService.class);

	private Hybase8SearchImpl hybase8SearchService = (Hybase8SearchImpl) ObjectContainer
			.getBean(Hybase8SearchImpl.class);

	private static String space = " ";

	private List<Map<String, TElement>> dataList;

	private static String TRS_SEPARATOR = Const.TRS_SEPARATOR;

	private Report report;

	private ReportData reportData;

	private String reportEsSql;

	private MaterialLibrary materialLibrary;

	private Double buildRat = 0.0;

	private Double subBuildRat = 0.0;

	private Double buildOver = 99.0;

	public BuildReportTask(MaterialLibrary materialLibrary, List<Map<String, TElement>> dataList, Report report) {
		this.dataList = dataList;
		this.report = report;
		this.materialLibrary = materialLibrary;
		this.reportData = new ReportData();
	}

	@Override
	public void run() {
		log.info("开始拼装数据！");
		double dataSize = dataList.size();
		preparationData();
		if (dataList == null || dataSize == 0) {
			gameOver();
		} else {
			subBuildRat = ((1 / dataSize) * 100);
			loadData();
		}
		reportService.saveReportData(reportData);
		// 报告数据生成完毕
		int success = Const.SUCCESS_INT;
		report.setStatus(success);
		report.setBulidRat(buildOver);
		report.setDataId(reportData.getId());
		reportService.saveReport(report);
		/*try {
			reportService.bulidReportDoc(report,reportData);
		} catch (Exception e) {
			log.error("生成报告错误！"+e);
			e.printStackTrace();
		}*/
	}

	public void preparationData() {
		reportEsSql = materialLibrary.getRealTrsl();
	}

	public void gameOver() {
		// 返回线程的上次的中断状态，并清除中断状态
		Thread.interrupted();
	}

	public void loadData() {
		for (int i = 0; i < dataList.size(); i++) {
			Map<String, TElement> mapData = dataList.get(i);
			Set<String> tempids = mapData.keySet();
			Iterator<String> iterator = tempids.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				switch (key) {
				// 报告名称
				case Const.TOP_1:
					fillReportTop(i, mapData.get(key));
					break;
				// 地域分布图
				case Const.AREA_2:
					areaCount(i, mapData.get(key));
					break;
				// 来源类型分析
				case Const.SOURCE_3:
					sourceTypeAnalyze(i, mapData.get(key));
					break;
				// 媒体活跃度
				case Const.ACTIVE_4:
					mediaActive(i, mapData.get(key));
					break;
				// 媒体扩散分析
				case Const.DIFFUSE_5:
					// log.error("暂无媒体扩散分析图");
					mediaExpend(i, mapData.get(key));
					break;
				// 情感分析
				case Const.EMOTION_6:
					emotionAnalysis(i, mapData.get(key));
					break;
				// 热词分布
				case Const.HOTWORD_7:
					log.error("暂无热词分布图");
					break;
				// 热点地名分布
				case Const.HOTPLACE_8:
					log.error("暂无热点地名分布图");
					break;
				// 热点机构分布
				case Const.HOTORGAN_9:
					log.error("暂无热点机构分布图");
					break;
				// 热点人名分布
				case Const.HOTNAME_10:
					log.error("暂无热点人名分布图");
					break;
				// 声量趋势图
				case Const.VOLUME_11:
					volumeTrend(i, mapData.get(key));
					break;
				// 引爆点 去掉
//				case Const.BOOM_12:
//					boomPoint(i, mapData.get(key));
//					break;
				// 舆情指数刻画
				case Const.EXPONENT_13:
					log.error("暂无舆情指数刻画图");
					break;
				// 最热新闻列表
				case Const.HOTTEST_14:
					log.error("暂无最热新闻列表图");
					break;
				// 最新新闻列表
				case Const.NEWEST_15:
					fillLatestNews(i, mapData.get(key));
					break;
				// 报告简介
				case Const.INTRO_16:
					fillReportIntro(i, mapData.get(key));
					break;
				// 监测概述
				case Const.SUMMARIZE_17:
					fillReportSumm(i, mapData.get(key));
					break;
				default:
					break;

				}
			}

		}

	}

	/**
	 * 报告概述
	 * 
	 * @param position
	 * @param tElement
	 */
	private void fillReportSumm(int position, TElement tElement) {
		try {
			Map<String, Object> datas = new HashMap<String, Object>();
			Map<String, Object> SummDatas = chartAnalyzeService.reportProcess(reportEsSql, "SUMM");
			SummDatas.put("sumTitle", tElement.getTitle());
			datas.put("data", SummDatas);
			datas.put("position", position);
			datas.put("tElement", tElement);
			String SummData;
			if (ObjectUtil.isNotEmpty(reportData.getMonitorSummarize())) {
				SummData = reportData.getMonitorSummarize() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				SummData = ObjectUtil.toJson(datas);
			}
			reportData.setMonitorSummarize(SummData);
			report.setBulidRat(buildRat += subBuildRat);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("报告概述数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充报告概述数据出错！", e);
			reportData.setMonitorSummarize("");
		}
	}

	/**
	 * 报告简介数据生成
	 * 
	 * @param position
	 * @param tElement
	 */
	private void fillReportIntro(int position, TElement tElement) {
		try {
			Map<String, Object> introDatas = new HashMap<String, Object>();
			Map<String, Object> datas = new HashMap<String, Object>();
			// 素材名称
			String libraryName = materialLibrary.getLibraryName();
			// 关键词
			String allWords = materialLibrary.getAllKeyword();
			String anyWords = materialLibrary.getAnyKeyword();
			String extWords = materialLibrary.getExcludeKeyword();
			String keyWords = "";
			if (StringUtil.isNotEmpty(allWords)) {
				allWords = allWords.replaceAll(";", "、");
				keyWords += "、" + allWords;
			}
			if (StringUtil.isNotEmpty(anyWords)) {
				anyWords = anyWords.replaceAll("[,;]", "、");
				keyWords += "、" + anyWords;
			}
			if (StringUtil.isNotEmpty(extWords)) {
				extWords = extWords.replaceAll(";", "、");
				keyWords += "、" + extWords;
			}
			keyWords = keyWords.replaceFirst("、", "");
			String startTime = DateUtil.date2String(materialLibrary.getSearchBeginTime(), DateUtil.yyyyMMdd3);
			String endTime = DateUtil.date2String(new Date(), DateUtil.yyyyMMdd3);
			String time = startTime + "至" + endTime;
			Map<String, Object> data = chartAnalyzeService.reportProcess(reportEsSql, "INTRO");
			Object count = data.get("count");
			Object siteName = data.get("siteName");
			introDatas.put("libraryName", libraryName);
			introDatas.put("keyWords", keyWords);
			introDatas.put("time", time);
			introDatas.put("count", count);
			introDatas.put("siteName", siteName);
			introDatas.put("introTitle", tElement.getTitle());
			datas.put("data", introDatas);
			datas.put("position", position);
			datas.put("tElement", tElement);
			String IntroDatas;
			if (ObjectUtil.isNotEmpty(reportData.getReportIntro())) {
				IntroDatas = reportData.getReportIntro() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				IntroDatas = ObjectUtil.toJson(datas);
			}
			reportData.setReportIntro(IntroDatas);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("报告简介数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充报告简介数据出错！", e);
			reportData.setReportIntro("");
		}
	}

	/**
	 * 填充报告顶部数据
	 * 
	 */
	private void fillReportTop(int position, TElement tElement) {
		try {
			Map<String, String> topMap = new HashMap<String, String>();
			Map<String, Object> datas = new HashMap<String, Object>();
			topMap.put("organName", report.getReportName());
			String dataTime = DateUtil.date2String(new Date(), DateUtil.DEFAULT_TIME_PATTERN);
			String[] dataArr = dataTime.split(space);
			topMap.put("createYear", dataArr[0]);
			topMap.put("createDate", dataArr[1]);
			datas.put("data", topMap);
			datas.put("position", position);
			datas.put("telement", tElement);
			String topDatas;
			if (ObjectUtil.isNotEmpty(reportData.getReportTopData())) {
				topDatas = reportData.getReportTopData() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				topDatas = ObjectUtil.toJson(datas);
			}
			reportData.setReportTopData(topDatas);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("报告顶部数据加载成功！ID:" + report.getId());
		} catch (OperationException e) {
			log.error("填充报告顶部数据出错！", e);
			reportData.setReportTopData("");
		}
	}

	/**
	 * 填充地图数据
	 * 
	 * @author songbinbin 2017年5月11日 void
	 */
	private void areaCount(int position, TElement tElement) {
		String startTime = DateUtil.date2String(materialLibrary.getSearchBeginTime(), DateUtil.yyyyMMddHHmmss);
		String endTime = DateUtil.date2String(materialLibrary.getSearchEndTime(), DateUtil.yyyyMMddHHmmss);
		Map<String, Object> datas = new HashMap<>();
		try {
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.filterByTRSL(reportEsSql);
			String[] timeArray = { startTime, endTime };
			List<Map<String, Object>> areaDate = chartAnalyzeService.getAreaCount(queryBuilder, timeArray,false,false,false);
			datas.put("data", areaDate);
			datas.put("position", position);
			datas.put("telement", tElement);
			datas.put("path", "drawImage" + Const.AREA_2);
			String areaData;
			if (ObjectUtil.isNotEmpty(reportData.getArealDistributionData())) {
				areaData = reportData.getArealDistributionData() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				areaData = ObjectUtil.toJson(datas);
			}
			reportData.setArealDistributionData(areaData);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("地域分布图数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充地图数据出错! sql--->:" + reportEsSql, e);
			reportData.setArealDistributionData("");
		}
	}

	/**
	 * 最新新闻列表
	 */
	private void fillLatestNews(int position, TElement tElement) {
		Map<String, Object> datas = new HashMap<>();
		try {
			String startTime = DateUtil.date2String(materialLibrary.getSearchBeginTime(), DateUtil.yyyyMMddHHmmss);
			String endTime = DateUtil.date2String(materialLibrary.getSearchEndTime(), DateUtil.yyyyMMddHHmmss);
			Map<String, Object> newsDatas = chartAnalyzeService.getLatestNews(reportEsSql, startTime, endTime, 1L, 10);
			newsDatas.put("newsTitle", tElement.getTitle());
			datas.put("data", newsDatas);
			datas.put("position", position);
			datas.put("telement", tElement);
			datas.put("path", "drawImage" + Const.NEWEST_15);
			String topDatas;
			if (ObjectUtil.isNotEmpty(reportData.getNewestData())) {
				topDatas = reportData.getNewestData() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				topDatas = ObjectUtil.toJson(datas);
			}
			if(topDatas!=null){
				topDatas = topDatas.replace("&nbsp;", " ");
				topDatas = topDatas.replace("<font color=red>", "");
				topDatas = topDatas.replace("</font>", "");
				topDatas = topDatas.replace("//", " ");
				topDatas = topDatas.replace("&amp;quot;", " ");
				topDatas = topDatas.replace("&quot;", " ");
				topDatas = topDatas.replace("&amp;", " ");
				topDatas = topDatas.replace("&lt;", "<");
				topDatas = topDatas.replace("&gt;", ">");
			}
			reportData.setNewestData(topDatas);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("最新新闻数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充最新新闻数据出错！", e);
			reportData.setNewestData("");
		}
	}

	/**
	 * 填充数据来源分析数据
	 * 
	 */
	public void sourceTypeAnalyze(int position, TElement tElement) {
		String startTime = DateUtil.date2String(materialLibrary.getSearchBeginTime(), DateUtil.yyyyMMdd);
		String endTime = DateUtil.date2String(materialLibrary.getSearchEndTime(), DateUtil.yyyyMMdd);
		Map<String, Object> datas = new HashMap<String, Object>();
		String timeRange = startTime + ";" + endTime;
		try {
			Map<String, Object> typeAnalyze = chartAnalyzeService.getWebCountNew2(reportEsSql, timeRange, null);
			datas.put("data", typeAnalyze);
			datas.put("position", position);
			datas.put("telement", tElement);
			datas.put("path", "drawImage" + Const.SOURCE_3);
			String areaData;
			if (ObjectUtil.isNotEmpty(reportData.getSourceTypeData())) {
				areaData = reportData.getSourceTypeData() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				areaData = ObjectUtil.toJson(datas);
			}
			reportData.setSourceTypeData(areaData);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("来源分析图数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充数据来源分析数据出错! sql--->:" + reportEsSql, e);
			reportData.setSourceTypeData("");
		}

	}

	/**
	 * 填充媒体活跃数据
	 * 
	 * @param position
	 * @param tElement
	 */
	private void mediaActive(int position, TElement tElement) {
		// 媒体活跃度
		String startTime = DateUtil.date2String(materialLibrary.getSearchBeginTime(), DateUtil.yyyyMMddHHmmss);
		String endTime = DateUtil.date2String(materialLibrary.getSearchEndTime(), DateUtil.yyyyMMddHHmmss);
		Map<String, Object> datas = new HashMap<String, Object>();
		try {
			String[] TimeRange = new String[] { startTime, endTime };
			// source 国内新闻 OR 国内新闻_手机客户端
			Map<String, Object> mediaActiveData = chartAnalyzeService.mediaAct(reportEsSql, TimeRange,
					tElement.getSource());
			datas.put("data", mediaActiveData);
			datas.put("position", position);
			datas.put("telement", tElement);
			datas.put("path", "drawImage" + Const.ACTIVE_4);
			String mediaData;
			if (ObjectUtil.isNotEmpty(reportData.getMediaActivityData())) {
				mediaData = reportData.getMediaActivityData() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				mediaData = ObjectUtil.toJson(datas);
			}
			reportData.setMediaActivityData(mediaData);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("媒体活跃图数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充媒体活跃数据出错! sql--->:" + reportEsSql, e);
		}
	}

	/**
	 * 填充媒体扩展分析
	 * 
	 * @param position
	 * @param tElement
	 */
	private void mediaExpend(int position, TElement tElement) {
		String startTime = DateUtil.date2String(materialLibrary.getSearchBeginTime(), DateUtil.yyyyMMddHHmmss);
		String endTime = DateUtil.date2String(materialLibrary.getSearchEndTime(), DateUtil.yyyyMMddHHmmss);
		Map<String, Object> datas = new HashMap<String, Object>();
		try {
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.filterByTRSL(reportEsSql);
			String[] range = new String[] { startTime, endTime };
			Object mediaActiveData = chartAnalyzeService.mediaActiveLevel(queryBuilder,"",range,true,false,false);
			datas.put("data", mediaActiveData);
			datas.put("position", position);
			datas.put("telement", tElement);
			datas.put("path", "drawImage" + Const.DIFFUSE_5);
			String mediaData;
			if (ObjectUtil.isNotEmpty(reportData.getMediaDiffuseData())) {
				mediaData = reportData.getMediaActivityData() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				mediaData = ObjectUtil.toJson(datas);
			}
			reportData.setMediaDiffuseData(mediaData);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("媒体活跃图数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充媒体活跃数据出错! sql--->:" + reportEsSql, e);
			reportData.setMediaDiffuseData("");
		}
	}

	/**
	 * 填充情感分析数据 既 图表分析中的 情感提及量
	 * 
	 * @param position
	 * @param tElement
	 */
	public void emotionAnalysis(int position, TElement tElement) {
		String startTime = DateUtil.date2String(materialLibrary.getSearchBeginTime(), DateUtil.yyyyMMddHHmmss);
		String endTime = DateUtil.date2String(materialLibrary.getSearchEndTime(), DateUtil.yyyyMMddHHmmss);
		//显示用
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
		String start=sdf.format(materialLibrary.getSearchBeginTime());  
		String end=sdf.format(materialLibrary.getSearchEndTime()); 
		Map<String, Object> datas = new HashMap<String, Object>();
		try {
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.filterByTRSL(reportEsSql);
			Map<String, Object> emotionAnaData = chartAnalyzeService.getVolumeNew(queryBuilder,
					new String[] { startTime, endTime }, start+";"+end);
			datas.put("data", emotionAnaData);
			datas.put("position", position);
			datas.put("telement", tElement);
			datas.put("path", "drawImage" + Const.EMOTION_6);
			String emotion;
			if (ObjectUtil.isNotEmpty(reportData.getEmotionAnalysis())) {
				emotion = reportData.getEmotionAnalysis() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				emotion = ObjectUtil.toJson(datas);
			}
			reportData.setEmotionAnalysis(emotion);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("情感分析图数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充情感分析数据出错! sql--->:" + reportEsSql, e);
			reportData.setEmotionAnalysis("");
		}
	}

	/**
	 * 填充声量趋势图数据
	 * 
	 * @param position
	 * @param tElement
	 */
	public void volumeTrend(int position, TElement tElement) {
		String startTime = DateUtil.date2String(materialLibrary.getSearchBeginTime(), DateUtil.yyyyMMdd);
		String endTime = DateUtil.date2String(materialLibrary.getSearchEndTime(), DateUtil.yyyyMMdd);
		Map<String, Object> datas = new HashMap<String, Object>();
		String timeRange = startTime + ";" + endTime;
		try {
			Map<String, Object> volume = chartAnalyzeService.getWebCountNew2(reportEsSql, timeRange, null);
			datas.put("data", volume);
			datas.put("position", position);
			datas.put("telement", tElement);
			datas.put("path", "drawImage" + Const.VOLUME_11);
			String volumeData;
			if (ObjectUtil.isNotEmpty(reportData.getVolumeData())) {
				volumeData = reportData.getVolumeData() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				volumeData = ObjectUtil.toJson(datas);
			}
			reportData.setVolumeData(volumeData);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			df.setRoundingMode(RoundingMode.DOWN);
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("声量趋势图数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充声量趋势图数据出错! sql--->:" + reportEsSql, e);
			reportData.setVolumeData("");
		}
	}

	/**
	 * 填充引爆点图数据
	 * 
	 * @param position
	 * @param tElement
	 */
	@SuppressWarnings("rawtypes")
	public void boomPoint(int position, TElement tElement) {
		log.info("引爆点数据开始加载！");
		String startTime = DateUtil.date2String(materialLibrary.getSearchBeginTime(), DateUtil.yyyyMMddHHmmss);
		Map<String, Object> datas = new HashMap<String, Object>();
		try {
			QueryBuilder builder = new QueryBuilder();
			builder.filterByTRSL(reportEsSql);
			builder.page(0, 1);
			builder.orderBy(ESFieldConst.IR_RTTCOUNT, true);
			builder.filterField(ESFieldConst.IR_GROUP_NAME, "微博", Operator.Equal);
			builder.filterField(ESFieldConst.IR_RETWEETED_URL, "", Operator.NotEqual);
			List<ChartAnalyzeEntity> list = hybase8SearchService.ftsQuery(builder, ChartAnalyzeEntity.class,true,false,false,null);
			if (list.size() <= 0) {
				throw new NullException("获取url为空");
			}
			String url = list.get(0).baseUrl();
			List boomPointList = chartAnalyzeService.getReportTippingPoint(url,
					DateUtil.stringToDate(startTime, DateUtil.yyyyMMddHHmmss));
			datas.put("data", boomPointList);
			datas.put("position", position);
			datas.put("telement", tElement);
			datas.put("path", "drawImage" + Const.BOOM_12);
			String boomPointStr;
			if (ObjectUtil.isNotEmpty(reportData.getBoomData())) {
				boomPointStr = reportData.getBoomData() + TRS_SEPARATOR + ObjectUtil.toJson(datas);
			} else {
				boomPointStr = ObjectUtil.toJson(datas);
			}
			reportData.setBoomData(boomPointStr);
			double d = buildRat += subBuildRat;
			DecimalFormat df = new DecimalFormat("#0.0");
			Double buildRat = Double.valueOf(df.format(d));
			report.setBulidRat(buildRat);
			reportService.saveReport(report);
			log.info("引爆点图数据加载成功！ID:" + report.getId());
		} catch (Exception e) {
			log.error("填充引爆点图数据出错! sql--->:" + reportEsSql, e);
			reportData.setBoomData("");
		}
	}
}
