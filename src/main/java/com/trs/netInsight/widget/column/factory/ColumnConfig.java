package com.trs.netInsight.widget.column.factory;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.WordSpacingUtil;
import com.trs.netInsight.widget.column.entity.IndexTab;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 栏目配置类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月4日
 *
 */
@Data
@Slf4j
public class ColumnConfig {

	/**
	 * 栏目实体
	 */
	private IndexTab indexTab;

	/**
	 * 页码
	 */
	private int pageNo;

	/**
	 * 每页展示条数
	 */
	private int pageSize;

	/**
	 * 最大查询条数,默认10条
	 */
	private int maxSize = 10;

	/**
	 * 时间段
	 */
	private String[] timeArray;

	/**
	 * 情感值筛选
	 */
	private String emotion;

	/**
	 * 排序字段
	 */
	private String orderBy;

	/**
	 * 折线图展示类型：按小时，按天    针对折线图
	 */
	private String showType;

	/**
	 * 来源筛选
	 */
	private String groupName;

	/**
	 * 词云图-实体词类型
	 */
	private String entityType;

	/**
	 * 坐标点(柱状图,折线图等分类对比使用)
	 */
	private String key;

	/**
	 * 地域分布图
	 */
	private String area;

	/**
	 * 数据时间(折线图使用,x轴坐标)
	 */
	private String dataTime;

	/**
	 * 词云
	 */
	private String irKeyword;

	/**
	 * 主回帖标记
	 */
	private String invitationCard;

	/**
	 * 结果中搜索
	 */
	private String fuzzyValue;

	/**
	 * 结果中搜索de范围
	 */
	private String fuzzyValueScope;

	/**
	 * 原发primary 转发forward
	 */
	private String forwarPrimary;
	/**
	 * 是否按照权重
	 */
	private boolean weight=false;

	/**
	 * 检索构造器
	 */
	private QueryBuilder queryBuilder;

	/**
	 * 联合查询通用构造器
	 */
	private QueryCommonBuilder commonBuilder;

	/**
	 * 初始化配置(不对hybase数据源负责)
	 * 
	 * @since changjiang @ 2018年4月4日
	 * @param indexTab
	 * @param timeRange
	 * @param pageNo
	 * @param pageSize
	 * @throws OperationException
	 * @Return : void
	 */
	public void init(IndexTab indexTab, String timeRange, int pageNo, int pageSize, String entityType, String orderBy,
			String fuzzyValue) throws OperationException {
		queryBuilder = new QueryBuilder();
		commonBuilder = new QueryCommonBuilder();

		this.indexTab = indexTab;
		String timerange = indexTab.getTimeRange();
		String timeRecent = indexTab.getTimeRecent();
		if (StringUtils.isNotEmpty(timeRange)) {
			timeArray = DateUtil.formatTimeRangeMinus1(timeRange);
		} else {
			if (StringUtils.isNotEmpty(timerange)) {
				timeArray = DateUtil.formatTimeRangeMinus1(timerange);
			} else if (StringUtils.isNotEmpty(timeRecent)) {
				timeArray = DateUtil.formatTimeRangeMinus1(timeRecent);
			}
		}
		this.entityType = entityType;

		String keyWords = indexTab.getKeyWord();
		String keyWordindex = indexTab.getKeyWordIndex();
		String excludeWords = indexTab.getExcludeWords();
		weight = indexTab.isWeight();

		// 检索关键词转换为trsl
		createFilter(keyWords, keyWordindex, excludeWords, weight);
		/*// 结果中搜索
		if (StringUtil.isNotEmpty(fuzzyValue)) {
			String trsl = new StringBuffer().append( FtsFieldConst.FIELD_TITLE).append(":(").append(fuzzyValue)
					.append(")").append(" OR (").append(FtsFieldConst.FIELD_CONTENT)
					.append(":(").append(fuzzyValue).append(")) ").toString();
			queryBuilder.filterByTRSL(trsl);
		}*/
		// 结果中搜索
		if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {//在结果中搜索,范围为全文的时候
			String[] split = fuzzyValue.split(",");
			String splitNode = "";
			for (int i = 0; i < split.length; i++) {
				if (StringUtil.isNotEmpty(split[i])) {
					splitNode += split[i] + ",";
				}
			}
			fuzzyValue = splitNode.substring(0, splitNode.length() - 1);
			if (fuzzyValue.endsWith(";") || fuzzyValue.endsWith(",") || fuzzyValue.endsWith("；")
					|| fuzzyValue.endsWith("，")) {
				fuzzyValue = fuzzyValue.substring(0, fuzzyValue.length() - 1);

			}
			StringBuilder fuzzyBuilder  = new StringBuilder();
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
				fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(") OR ("+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND \"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				fuzzyBuilder.append(hybaseField).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			queryBuilder.filterByTRSL(fuzzyBuilder.toString());
		}
		// 构造检索条件
		// 时间
		String[] type = indexTab.getType();
		List<String> strings = Arrays.asList(type);
		String dataStartTime = null;
		String dateEndTime = null;
		if (StringUtils.isNotEmpty(dataTime) && strings.size()==1 && strings.contains(ColumnConst.CHART_LINE)){
			if(dataTime.length() == 16 ){
				dataTime = dataTime.replace("-", "").replace("/", "")
						.replace(":", "").replace(" ", "").trim();
				dataTime = dataTime+"00";
				dateEndTime = dataTime.substring(0,10)+"5959";
				queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { dataTime, dateEndTime },
						Operator.Between);
			}else{
				if (StringUtils.equals(timeRange, "24h") || StringUtils.equals(timeRange, "0d")) {
					if (dataTime.length() == 1){
						dataStartTime = "0"+dataTime;
					}else {
						dataStartTime = dataTime;
					}
					if("24h".equals(timeRange)){
						//当前小时
						String formatCurrentTime = DateUtil.formatCurrentTime("HH");
						Integer paramTime = Integer.valueOf(dataStartTime);
						Integer currentTime = Integer.valueOf(formatCurrentTime);
						if (paramTime >= currentTime ){
							//用前一天
							dataTime = (DateUtil.formatDateAfter(new Date(), DateUtil.yyyyMMdd2, -1)) +dataStartTime+"0000";//间隔 1 天  代表默认查 1 天
							dateEndTime = (DateUtil.formatDateAfter(new Date(), DateUtil.yyyyMMdd2,-1)) + dataStartTime + "5959";
						}else {
							//当天
							dataTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2)) +dataStartTime+"0000";
							dateEndTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2)) + dataStartTime + "5959";
						}
					}else if ("0d".equals(timeRange)){
						//当天  24h 当前时间也采用 当天日期
						dataTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2)) +dataStartTime+"0000";
						dateEndTime = (DateUtil.format2String(new Date(), DateUtil.yyyyMMdd2)) + dataStartTime + "5959";
					}
					queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{dataTime,dateEndTime}, Operator.Between);
				} else if ( (dataTime.length() == 2 || dataTime.length() == 1) && timeRange.length() > 3) {
					if (dataTime.length() == 1){
						dataTime = "0"+dataTime;
					}
					Integer day_hour =Integer.valueOf(dataTime);
					Integer start_hour = Integer.valueOf(timeRange.substring(11,13));
					String day = "";
					if(start_hour <= day_hour){
						day = timeRange.replace("-", "").replace("/", "")
								.replace(":", "").replace(" ", "").trim().substring(0, 8);
					}else{
						day = timeRange.replace("-", "").replace("/", "")
								.replace(":", "").replace(" ", "").trim().substring(15, 23);
					}
					dataTime = day + dataTime + "0000";
					dateEndTime = dataTime.substring(0, 10) + "5959";
					queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{dataTime, dateEndTime},
							Operator.Between);
				} else {
					dataTime.replace("-", "");
					//间隔 1 天  代表默认查 1 天
					dateEndTime = DateUtil.formatDateAfter(dataTime, DateUtil.yyyyMMdd2, 1);
					dataTime = dataTime.replace("-", "").replace(":", "").replace("/", "").replaceAll("\r|\n|\t", "").trim().replace("/", "").substring(0, 8);
					dateEndTime = dataTime.replace("-", "").replace(":", "").replace("/", "").replaceAll("\r|\n|\t", "").trim();
					//按urltime查 分类统计结果与列表结果不符 所以按urldate查
					queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { dataTime+"000000", dateEndTime+"235959" },Operator.Between);
				}
			}
		}else {
			//折线图 24h 当前时间点 分类统计 数据 与 跳列表 时间段不一致问题
		/*	if (StringUtils.equals(timeRange, "24h") && strings.size()==1 && strings.contains(ColumnConst.CHART_LINE)){
				timeArray[0] = timeArray[0].substring(0,10)+"0000";
				int i = Integer.parseInt(timeArray[1].substring(8,10))-1;
				String repalceHour = String.valueOf(i);
				if (i < 10){
					repalceHour = "0" + String.valueOf(i);
				}
				timeArray[1] = new StringBuilder(timeArray[1].substring(0,10)+"5959").replace(8,10,repalceHour).toString();

			}else if(!StringUtils.equals(timeRange, "24h") && strings.size()==1 && strings.contains(ColumnConst.CHART_LINE)){
				timeArray[0] = timeArray[0].substring(0,8)+"000000";
			}*/
			queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
		}

		Date startToDate = DateUtil.stringToDate(timeArray[0], "yyyyMMddHHmmss");
		Date endToDate = DateUtil.stringToDate(timeArray[1], "yyyyMMddHHmmss");
		queryBuilder.setStartTime(startToDate);
		queryBuilder.setEndTime(endToDate);
		// 排除网站
		if (this.indexTab.getExcludeWeb() != null && this.indexTab.getExcludeWeb().length > 0) {
			addExcloudSite();
		}
		//主回帖
		if ("0".equals(invitationCard)){
			//主贴
			queryBuilder.filterField(FtsFieldConst.FIELD_NRESERVED1, "(0 OR \"\")", Operator.Equal);
		}else if ("1".equals(invitationCard)){//回帖
			queryBuilder.filterField(FtsFieldConst.FIELD_NRESERVED1, invitationCard, Operator.Equal);
		}
		// 情感值
		if (StringUtils.isNoneBlank(emotion) && !"ALL".equals(emotion)) {
			// 处理中性问题
			if("中性".equals(emotion)){
				queryBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, "(\"正面\" OR \"负面\")", Operator.NotEqual);
			}else{
				queryBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}
		}

		// 转发 / 原发

		String builderTRSL = queryBuilder.asTRSL();
		StringBuilder builderTrsl = new StringBuilder(builderTRSL);
		if ("primary".equals(forwarPrimary)) {
			// 原发
			queryBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
		} else if ("forward".equals(forwarPrimary)) {
			// 转发
			queryBuilder = new QueryBuilder();
			builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
			queryBuilder.filterByTRSL(builderTrsl.toString());

		}


		String sidsTrsl = queryBuilder.asTRSL();
		StringBuilder sidsTrslNew = new StringBuilder(sidsTrsl);
		if (StringUtil.isNotEmpty(indexTab.getNotSids())){
			sidsTrslNew.append(indexTab.getNotSids());
			queryBuilder.filterByTRSL(sidsTrslNew.toString());
		}
		// 构造通用检索构造器
		convertCommonBuilder();
		// 分页
		queryBuilder.page(pageNo, pageSize);
	}

	/**
	 * 列表查询初始化配置
	 * 
	 * @since changjiang @ 2018年4月11日
	 * @param indexTab
	 * @param timeRange
	 *            时间段
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页显示条数
	 * @param groupName
	 *            来源分组
	 * @param entityType
	 *            实体词类型(词云使用)
	 * @param dataTime
	 *            数据时间
	 * @param key
	 *            坐标点
	 * @param orderBy
	 *            排序
	 * @throws OperationException
	 * @Return : void
	 */
	public void initSection(IndexTab indexTab, String timeRange, int pageNo, int pageSize, String groupName,
			String emotion, String entityType, String dataTime, String key, String orderBy, String area,
			String irKeyword, String invitationCard, String fuzzyValue,String fuzzyValueScope, String forwarPrimary)
			throws OperationException {
		this.orderBy = orderBy;
		this.dataTime = dataTime;
		this.key = key;
		this.groupName = groupName;
		this.emotion = emotion;
		this.area = area;
		this.irKeyword = irKeyword;
		this.invitationCard = invitationCard;
		this.fuzzyValue = fuzzyValue;
		this.fuzzyValueScope = fuzzyValueScope;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.forwarPrimary = forwarPrimary;
		this.init(indexTab, timeRange, pageNo, pageSize, entityType, orderBy, fuzzyValue);
	}

	/**
	 * @Desc : 构造检索条件
	 * @since changjiang @ 2018年4月8日
	 * @param keyWords
	 * @param keyWordindex
	 * @param excludeWords
	 * @Return : void
	 */
	private void createFilter(String keyWords, String keyWordindex, String excludeWords, boolean weight) {
		if (StringUtil.isNotEmpty(keyWordindex) && (StringUtil.isNotEmpty(keyWords) || StringUtil.isNotEmpty(excludeWords))) {// 普通模式

			queryBuilder = WordSpacingUtil.handleKeyWords(keyWords, keyWordindex, this.weight);
			//拼接排除词
			if (keyWordindex.trim().equals("1")) {// 标题加正文
				if (StringUtil.isNotEmpty(excludeWords)) {
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
							.append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					this.queryBuilder.filterByTRSL(exbuilder.toString());
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					this.queryBuilder.filterByTRSL(exbuilder2.toString());
				}
			}else {
				if (StringUtil.isNotEmpty(excludeWords)) {
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
							.append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					this.queryBuilder.filterByTRSL(exbuilder.toString());
				}
			}
		//	}
		} else {// 专家模式
			queryBuilder.filterByTRSL(this.indexTab.getTrsl());// 专家模式

			if(this.indexTab.isServer()){
				queryBuilder.setServer(true);
			}
		}
		switch (this.orderBy) { // 排序
		case "commtCount":// 评论
			queryBuilder.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
			break;
		case "rttCount":// 转发
			queryBuilder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
			break;
		case "asc":
			queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			break;
		case "desc":
			queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			break;
		case "relevance":// 相关性排序
			queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
			break;
		default:
			if (weight) {
				queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
			} else {
				queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
			}
			break;
		}
	}

	/**
	 * @Desc : 为通用联合检索构造器赋值
	 * @since changjiang @ 2018年4月8日
	 * @Return : void
	 */
	private void convertCommonBuilder() {
		this.commonBuilder.filterByTRSL(this.queryBuilder.asTRSL());
		this.commonBuilder.page(queryBuilder.getPageNo(), queryBuilder.getPageSize());
		// this.commonBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
		switch (this.orderBy) { // 排序
		case "commtCount":// 评论
			commonBuilder.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
			break;
		case "rttCount":// 转发
			commonBuilder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
			break;
		case "asc":
			commonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			break;
		case "desc":
			commonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			break;
		case "relevance":// 相关性排序
			commonBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
			break;
		default:
			if (weight) {
				commonBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
			} else {
				commonBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
			}
			break;
		}
		commonBuilder.setServer(queryBuilder.isServer());
		this.commonBuilder.setStartTime(this.queryBuilder.getStartTime());
		this.commonBuilder.setEndTime(this.queryBuilder.getEndTime());
	}

	/**
	 * 增加排除站点
	 * 
	 * @since changjiang @ 2018年4月9日
	 * @Return : void
	 */
	private void addExcloudSite() {
		String[] excludeWeb = this.indexTab.getExcludeWeb();
		String asTRSL = this.queryBuilder.asTRSL();
		String notSite = "";
		for (String site : excludeWeb) {
			notSite += site + " OR ";
		}
		if (notSite.endsWith(" OR ")) {
			notSite = notSite.substring(0, notSite.length() - 4);
		}
		asTRSL += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(notSite)
				.append(")").toString();

		this.queryBuilder = new QueryBuilder();
		queryBuilder.filterByTRSL(asTRSL);
		if(this.indexTab.isServer()){
			queryBuilder.setServer(true);
		}
		switch (this.orderBy) { // 排序
			case "commtCount":// 评论
				queryBuilder.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
				break;
			case "rttCount":// 转发
				queryBuilder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
				break;
			case "asc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "desc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "relevance":// 相关性排序
				queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			default:
				if (weight) {
					queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
				} else {
					queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
				}
				break;
		}
	}

}
