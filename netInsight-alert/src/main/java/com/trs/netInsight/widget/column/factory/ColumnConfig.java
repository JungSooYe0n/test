package com.trs.netInsight.widget.column.factory;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.WordSpacingUtil;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.IndexTabType;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import edu.stanford.nlp.parser.dvparser.DVModelReranker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

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
	 * 栏目实体
	 */
	private ChartPageInfo chartPage;

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
	//private String area;
	/**
	 * 词云
	 */
	//private String irKeyword;

	/**
	 * 数据时间(折线图使用,x轴坐标)
	 */
	private String dataTime;

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
	private String imgOcr;
	/**
	 * 是否按照权重
	 */
	private boolean weight=false;

	private String read;//  阅读标记

	private String mediaLevel;//  媒体等级
	private String mediaIndustry;// 媒体行业
	private String contentIndustry;// 内容行业
	private String filterInfo;//信息过滤
	private String contentArea;//信息地域
	private String mediaArea;//媒体地域
	private String preciseFilter;//精准筛选





	/**
	 * 检索构造器
	 */
	private QueryBuilder queryBuilder;
	public QueryBuilder getQueryBuilder(){
		if(isFilterXyTrsl){
			appendXYTrslForSpecialTrsl();
		}
		return queryBuilder;
	}
	/**
	 * 联合查询通用构造器
	 */
	private QueryCommonBuilder commonBuilder;

	public QueryCommonBuilder getCommonBuilder(){
		if(isFilterXyTrsl){
			appendXYTrslForSpecialTrsl();
		}
		return commonBuilder;
	}

	/**
	 * 是否还需要拼接 xyTrsl 表达式
	 */
	private boolean isFilterXyTrsl = true;

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
		if (StringUtils.isNotEmpty(timeRange)) {
			timeArray = DateUtil.formatTimeRangeMinus1(timeRange);
		} else {
			if (StringUtils.isNotEmpty(timerange)) {
				timeArray = DateUtil.formatTimeRangeMinus1(timerange);
			}
		}
		this.entityType = entityType;

		String keyWords = indexTab.getKeyWord();
		String keyWordindex = indexTab.getKeyWordIndex();
		String excludeWords = indexTab.getExcludeWords();
		String excludeWordsIndex = indexTab.getExcludeWordIndex();
		weight = indexTab.isWeight();

		// 检索关键词转换为trsl
		createFilter(keyWords, keyWordindex, excludeWords, excludeWordsIndex,weight);
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
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND \"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				fuzzyBuilder.append(hybaseField).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			queryBuilder.filterByTRSL(fuzzyBuilder.toString());
		}
		// 构造检索条件
		// 时间
		String type = indexTab.getType();
		String dataStartTime = null;
		String dateEndTime = null;
		if (StringUtils.isNotEmpty(dataTime) && ColumnConst.CHART_LINE.equals(type)){
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
			queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
		}

		Date startToDate = DateUtil.stringToDate(timeArray[0], "yyyyMMddHHmmss");
		Date endToDate = DateUtil.stringToDate(timeArray[1], "yyyyMMddHHmmss");
		queryBuilder.setStartTime(startToDate);
		queryBuilder.setEndTime(endToDate);
		//查看OCR - 图片
		if(StringUtil.isNotEmpty(imgOcr) && !"ALL".equals(imgOcr)){
			if("img".equals(imgOcr)){ // 看有ocr的
				this.queryBuilder.filterByTRSL(Const.OCR_INCLUDE);
			}else if("noimg".equals(imgOcr)){  // 不看有ocr的
				this.queryBuilder.filterByTRSL(Const.OCR_NOT_INCLUDE);
			}
		}

		// 监测网站
		if (this.indexTab.getMonitorSite() != null && this.indexTab.getMonitorSite().split("[;|；]").length > 0) {
			String addMonitorSite = addMonitorSite(this.indexTab.getMonitorSite());
			if(StringUtil.isNotEmpty(addMonitorSite)){
				this.queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME,addMonitorSite, Operator.Equal);
			}
		}
		// 排除网站
		if (this.indexTab.getExcludeWeb() != null && this.indexTab.getExcludeWeb().split("[;|；]").length > 0) {
			addExcloudSite();
		}
		// 情感值
		if (StringUtils.isNoneBlank(emotion) && !"ALL".equals(emotion)) {
			addFieldFilter(FtsFieldConst.FIELD_APPRAISE,emotion,Const.ARRAY_APPRAISE);
		}

		//媒体等级
		if(StringUtil.isNotEmpty(mediaLevel)){
			addFieldFilter(FtsFieldConst.FIELD_MEDIA_LEVEL,mediaLevel,Const.MEDIA_LEVEL);
		}
		//媒体行业
		if(StringUtil.isNotEmpty(mediaIndustry )){
			addFieldFilter(FtsFieldConst.FIELD_MEDIA_INDUSTRY,mediaIndustry,Const.MEDIA_INDUSTRY);
		}
		//内容行业
		if(StringUtil.isNotEmpty(contentIndustry )){
			addFieldFilter(FtsFieldConst.FIELD_CONTENT_INDUSTRY,contentIndustry,Const.CONTENT_INDUSTRY);
		}
		//内容地域
		if(StringUtil.isNotEmpty(contentArea )){
			addAreaFilter(FtsFieldConst.FIELD_CATALOG_AREA,contentArea);
		}
		//媒体地域
		if(StringUtil.isNotEmpty(mediaArea )){
			addAreaFilter(FtsFieldConst.FIELD_MEDIA_AREA,mediaArea);
		}
		//信息过滤
		if(StringUtil.isNotEmpty(filterInfo) && !filterInfo.equals(Const.NOT_FILTER_INFO)){
			String trsl = queryBuilder.asTRSL();
			StringBuilder sb = new StringBuilder(trsl);
			String[] valueArr = filterInfo.split(";");
			Set<String> valueArrList = new HashSet<>();
			for(String v : valueArr){
				if(Const.FILTER_INFO.contains(v)){
					valueArrList.add(v);
				}
			}
			if (valueArrList.size() > 0 /*&& valueArrList.size() < Const.FILTER_INFO.size()*/) {
				sb.append(" NOT (").append(FtsFieldConst.FIELD_FILTER_INFO).append(":(").append(StringUtils.join(valueArrList," OR ")).append("))");
				queryBuilder = new QueryBuilder();
				queryBuilder.filterByTRSL(sb.toString());
			}
		}

		String source = indexTab.getGroupName();
		List<String> sourceList = CommonListChartUtil.formatGroupName(source);
		if(StringUtil.isNotEmpty(groupName)){
			if (!"ALL".equals(groupName) ) {
				List<String> checkSourceList = CommonListChartUtil.formatGroupName(groupName);
				Boolean isSearch = false;
				for (String group : checkSourceList) {
					if (sourceList.contains(group)) {
						isSearch = true;
					}
				}
				if (!isSearch) {
					throw new TRSSearchException("当前栏目无法查询该数据源");
				}
			} else {
				groupName = StringUtils.join(sourceList, ";");
			}

			List<String> searchSourceList = CommonListChartUtil.formatGroupName(groupName);

			//只在原转发和主回帖筛选时添加要查询数据源，因为底层方法通过参数中的groupName去添加了对应的数据源和数据库信息
			if ((searchSourceList.contains(Const.GROUPNAME_LUNTAN) && StringUtil.isNotEmpty(invitationCard)) ||
					(searchSourceList.contains(Const.GROUPNAME_WEIBO) && StringUtil.isNotEmpty(forwarPrimary))) {
				StringBuffer sb = new StringBuffer();
				if (searchSourceList.contains(Const.GROUPNAME_LUNTAN) && StringUtil.isNotEmpty(invitationCard)) {
					sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_LUNTAN + ")");
					if ("0".equals(invitationCard)) {// 主贴
						sb.append(" AND (").append(Const.NRESERVED1_LUNTAN).append(")");
					} else if ("1".equals(invitationCard)) {// 回帖
						sb.append(" AND (").append(FtsFieldConst.FIELD_NRESERVED1).append(":(1)").append(")");
					}
					sb.append(")");
					searchSourceList.remove(Const.GROUPNAME_LUNTAN);
				}
				if (searchSourceList.contains(Const.GROUPNAME_WEIBO) && StringUtil.isNotEmpty(forwarPrimary)) {
					if (sb.length() > 0) {
						sb.append(" OR ");
					}
					sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_WEIBO + ")");
					if ("primary".equals(forwarPrimary)) {
						// 原发
						sb.append(" AND ").append(Const.PRIMARY_WEIBO);
					} else if ("forward".equals(forwarPrimary)) {
						//转发
						sb.append(" NOT ").append(Const.PRIMARY_WEIBO);
					}
					sb.append(")");
					searchSourceList.remove(Const.GROUPNAME_WEIBO);
				}
				if (searchSourceList.size() > 0) {
					if (sb.length() > 0) {
						sb.append(" OR ");
					}
					sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME).append(":(").append(StringUtils.join(searchSourceList, " OR ")).append("))");
				}
				queryBuilder.filterByTRSL(sb.toString());
			}
		}
		//精准筛选 与上面论坛的主回帖和微博的原转发类似 ，都需要在数据源的基础上进行修改
		if (StringUtil.isNotEmpty(preciseFilter)) {
			if (sourceList.contains(Const.GROUPNAME_XINWEN) || sourceList.contains(Const.GROUPNAME_WEIBO) || sourceList.contains(Const.GROUPNAME_LUNTAN)) {
				String[] arr = preciseFilter.split(";");
				if (arr != null && arr.length > 0) {
					List<String> preciseFilterList = new ArrayList<>();
					for (String filter : arr) {
						preciseFilterList.add(filter);
					}
					StringBuffer buffer = new StringBuffer();
					// 新闻筛选  --- 屏蔽新闻转发 就是新闻 不要新闻不为空的时候，也就是要新闻原发
					if (sourceList.contains(Const.GROUPNAME_XINWEN) && preciseFilterList.contains("notNewsForward")) {

						buffer.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_XINWEN + ")");
						buffer.append(" AND (").append(Const.SRCNAME_XINWEN).append(")");
						buffer.append(")");
						sourceList.remove(Const.GROUPNAME_XINWEN);
					}

					//论坛筛选  ---  屏蔽论坛主贴  -  为回帖  、屏蔽论坛回帖为主贴
					if (sourceList.contains(Const.GROUPNAME_LUNTAN) && (preciseFilterList.contains("notLuntanForward") || preciseFilterList.contains("notLuntanPrimary"))) {
						//不同时包含 屏蔽主贴回帖
						// - 如果同时屏蔽主贴回帖，等同于不查论坛  能进来判断则代表一定选择了屏蔽中的一项
						if (!(preciseFilterList.contains("notLuntanForward") && preciseFilterList.contains("notLuntanPrimary"))) {
							if (buffer.length() > 0) {
								buffer.append(" OR ");
							}
							buffer.append("(");
							buffer.append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_LUNTAN + ")");
							if (preciseFilterList.contains("notLuntanForward")) { //屏蔽论坛回帖 -- 主贴
								buffer.append(" AND (").append(Const.NRESERVED1_LUNTAN).append(")");
							}
							if (preciseFilterList.contains("notLuntanPrimary")) { //屏蔽论坛主贴
								buffer.append(" NOT (").append(Const.NRESERVED1_LUNTAN).append(")");
							}
							buffer.append(")");
						}
						sourceList.remove(Const.GROUPNAME_LUNTAN);
					}

					//微博筛选  ----  微博筛选时 ，屏蔽微博原发 - 为转发、 屏蔽微博转发 - 为原发
					if (sourceList.contains(Const.GROUPNAME_WEIBO) && (preciseFilterList.contains("notWeiboForward") || preciseFilterList.contains("notWeiboPrimary")
						/*|| preciseFilterList.contains("notWeiboOrgAuthen") || preciseFilterList.contains("notWeiboPeopleAuthen")
						|| preciseFilterList.contains("notWeiboAuthen") || preciseFilterList.contains("notWeiboLocation")
						|| preciseFilterList.contains("notWeiboScreenName") || preciseFilterList.contains("notWeiboTopic")*/
					)) {
						//不同时包含 屏蔽原发转发
						// - 如果同时屏蔽原发转发，等同于不查微博  能进来判断则代表一定选择了屏蔽中的一项
						if (!(preciseFilterList.contains("notWeiboForward") && preciseFilterList.contains("notWeiboPrimary"))) {
							if (buffer.length() > 0) {
								buffer.append(" OR ");
							}
							buffer.append("(");
							buffer.append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_WEIBO + ")");
							if (preciseFilterList.contains("notWeiboForward")) {//屏蔽微博转发
								buffer.append(" AND (").append(Const.PRIMARY_WEIBO).append(")");
							}
							if (preciseFilterList.contains("notWeiboPrimary")) {//屏蔽微博原发
								buffer.append(" NOT (").append(Const.PRIMARY_WEIBO).append(")");
							}
							if (preciseFilterList.contains("notWeiboOrgAuthen")) {//屏蔽微博机构认证

							}
							if (preciseFilterList.contains("notWeiboPeopleAuthen")) {//屏蔽微博个人认证

							}
							if (preciseFilterList.contains("notWeiboAuthen")) {//屏蔽微博无认证

							}
							if (preciseFilterList.contains("notWeiboLocation")) {//屏蔽命中微博位置信息

							}
							if (preciseFilterList.contains("notWeiboScreenName")) {//忽略命中微博博主名

							}
							if (preciseFilterList.contains("notWeiboTopic")) {//屏蔽命中微博话题信息

							}
							buffer.append(")");
						}
						sourceList.remove(Const.GROUPNAME_WEIBO);
					}
					if (sourceList.size() > 0) {
						if (buffer.length() > 0) {
							buffer.append(" OR ");
						}
						buffer.append("(").append(FtsFieldConst.FIELD_GROUPNAME).append(":(").append(StringUtils.join(sourceList, " OR ")).append("))");
					}
					queryBuilder.filterByTRSL(buffer.toString());
				}
			}
		}
		String sidsTrsl = queryBuilder.asTRSL();
		StringBuilder sidsTrslNew = new StringBuilder(sidsTrsl);
		if (StringUtil.isNotEmpty(indexTab.getNotSids())) {
			sidsTrslNew.append(indexTab.getNotSids());
			queryBuilder.filterByTRSL(sidsTrslNew.toString());
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
					queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE);
				} else {
					queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME );
				}
				break;
		}
		// 构造通用检索构造器
		convertCommonBuilder();
		appendXYTrslForSpecialTrsl();
		// 分页
		queryBuilder.page(pageNo, pageSize);
	}

	private void addFieldFilter(String field,String value,List<String> allValue){
		if(StringUtil.isNotEmpty(value)){
			if(value.contains("其它")){
				value = value.replaceAll("其它","其他");
			}
			String[] valueArr = value.split(";");
			Set<String> valueArrList = new HashSet<>();
			for(String v : valueArr){
				if ("其他".equals(v) || "中性".equals(v)) {
					valueArrList.add("\"\"");
				}
				if (allValue.contains(v)) {
					valueArrList.add(v);
				}
			}
			//如果list中有其他，则其他为 其他+“”。依然是算两个
			if(valueArrList.size() >0  &&  valueArrList.size() < allValue.size() +1){
				this.queryBuilder.filterField(field,StringUtils.join(valueArrList," OR ") , Operator.Equal);
			}
		}
	}

	private void addAreaFilter(String field,String areas){
		Map<String,String> areaMap = null;
		if(FtsFieldConst.FIELD_MEDIA_AREA.equals(field)){
			areaMap = Const.MEDIA_PROVINCE_NAME;
		}else if(FtsFieldConst.FIELD_CATALOG_AREA.equals(field)){
			areaMap = Const.CONTTENT_PROVINCE_NAME;
		}
		if(StringUtil.isNotEmpty(areas) && areaMap!= null){
			if(areas.contains("其它")){
				areas = areas.replaceAll("其它","其他");
			}
			String[] areaArr = areas.split(";");
			Set<String> areaList = new HashSet<>();
			for(String area : areaArr){
				if ("其他".equals(area)) {
					areaList.add("\"\"");
				}
				if (areaMap.containsKey(area)) {
					areaList.add(areaMap.get(area));
				}
			}
			//如果list中有其他，则其他为 其他+“”。依然是算两个
			if(areaList.size() >0  &&  areaList.size() < areaMap.size() +1){
				this.queryBuilder.filterField(field,StringUtils.join(areaList," OR ") , Operator.Equal);
			}
		}
	}


	/**
	 * 添加筛选条件信息  ---  如果要添加筛选条件，需要写在initSection 方法前面,
	 * @param read  阅读标记
	 * @param mediaLevel  媒体等级
	 * @param mediaIndustry  媒体行业
	 * @param contentIndustry  内容行业
	 * @param filterInfo  过滤信息
	 * @param contentArea  内容行业
	 * @param mediaArea  媒体行业
	 * @param preciseFilter  精准筛选
	 */
	public void addFilterCondition(String read,String mediaLevel,String mediaIndustry,String contentIndustry,String filterInfo,
								   String contentArea,String mediaArea,String preciseFilter){
		//媒体等级
		if(StringUtil.isNotEmpty(mediaLevel)){
			if("ALL".equals(mediaLevel)){
				mediaLevel = org.thymeleaf.util.StringUtils.join(Const.MEDIA_LEVEL,";");
			}
		}
		//媒体行业
		if(StringUtil.isNotEmpty(mediaIndustry)){
			if("ALL".equals(mediaIndustry)){
				mediaIndustry = org.thymeleaf.util.StringUtils.join(Const.MEDIA_INDUSTRY,";");
			}
		}
		//内容行业
		if(StringUtil.isNotEmpty(contentIndustry)){
			if("ALL".equals(contentIndustry)){
				contentIndustry = org.thymeleaf.util.StringUtils.join(Const.CONTENT_INDUSTRY,";");
			}
		}
		//信息过滤
		if(StringUtil.isNotEmpty(filterInfo)){
			if("ALL".equals(filterInfo)){
				filterInfo = org.thymeleaf.util.StringUtils.join(Const.FILTER_INFO,";");
			}
		}
		this.read = read;
		this.mediaLevel = mediaLevel;
		this.mediaIndustry = mediaIndustry;
		this.contentIndustry = contentIndustry;
		this.filterInfo = filterInfo;
		this.contentArea = contentArea;
		this.mediaArea = mediaArea;
		this.preciseFilter = preciseFilter;
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
	//TODO  如果是日常监测筛选  因为普通模式添加了筛选字段，包括地域和行业等筛选字段需要添加
	public void initSection(IndexTab indexTab, String timeRange, int pageNo, int pageSize, String groupName,
							String emotion, String entityType, String dataTime, String key, String orderBy,
							String invitationCard, String fuzzyValue,String fuzzyValueScope, String forwarPrimary
							,String read, String mediaLevel,String mediaIndustry,String contentIndustry,String filterInfo,
							String contentArea,String mediaArea,String preciseFilter,String imgOcr
	) throws OperationException {
		this.orderBy = orderBy;
		this.dataTime = dataTime;
		this.key = key;
		this.groupName = groupName;
		this.emotion = emotion;
		//this.area = area;
		//this.irKeyword = irKeyword;
		this.invitationCard = invitationCard;
		this.fuzzyValue = fuzzyValue;
		this.fuzzyValueScope = fuzzyValueScope;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.forwarPrimary = forwarPrimary;
		this.imgOcr = imgOcr;
		addFilterCondition(read, mediaLevel, mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea, preciseFilter);
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
	private void createFilter(String keyWords, String keyWordindex, String excludeWords,String excludeWordsIndex, boolean weight) {
		if (StringUtil.isNotEmpty(keyWordindex) && (StringUtil.isNotEmpty(keyWords) || StringUtil.isNotEmpty(excludeWords))) {// 普通模式
			queryBuilder = WordSpacingUtil.handleKeyWords(keyWords, keyWordindex, this.weight);
			String excludeWordTrsl = WordSpacingUtil.appendExcludeWords(excludeWords,excludeWordsIndex);
			if(StringUtil.isNotEmpty(excludeWordTrsl)){
				this.queryBuilder.filterByTRSL(excludeWordTrsl);
			}
		} else {// 专家模式
			if(StringUtil.isNotEmpty(this.indexTab.getTrsl())){
				queryBuilder.filterByTRSL(this.indexTab.getTrsl());// 专家模式
			}
		}

	}

	/**
	 * @Desc : 为通用联合检索构造器赋值
	 * @since changjiang @ 2018年4月8日
	 * @Return : void
	 */
	private void convertCommonBuilder() {
		this.commonBuilder.filterByTRSL(this.queryBuilder.asTRSL());
		this.commonBuilder.page(pageNo, pageSize!= 0 ? pageSize: maxSize);
		switch (this.orderBy) { // 排序
			case "commtCount":// 评论tairele
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
					commonBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE );
				} else {
					commonBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME);
				}
				break;
		}
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
		String[] excludeWeb = this.indexTab.getExcludeWeb(true);
		String asTRSL = this.queryBuilder.asTRSL();
		if (excludeWeb != null && excludeWeb.length > 0) {
			String notSite = "";
			for (String site : excludeWeb) {
				notSite += site + " OR ";
			}
			if (notSite.endsWith(" OR ")) {
				notSite = notSite.substring(0, notSite.length() - 4);
			}
			asTRSL += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(notSite)
					.append(")").toString();
		}

		this.queryBuilder = new QueryBuilder();
		queryBuilder.filterByTRSL(asTRSL);
	}

	/**
	 * 增加监测站点
	 *
	 * @Return : void
	 */
	private String addMonitorSite(String monitorSite) {
		if(StringUtil.isNotEmpty(monitorSite)){
			String[] splitArr  = monitorSite.split("[;|；]");
			List<String> list = new ArrayList<>();
			for(String split:splitArr){
				if(StringUtil.isNotEmpty(split)){
					list.add(split);
				}
			}
			if(list.size()>0){
				return StringUtils.join(list," OR ");
			}else{
				return "";
			}
		}else{
			return "";
		}
	}


	/**
	 * 给专家模式的表达式拼接xytrsl
	 */
	private void appendXYTrslForSpecialTrsl(){
		//SpecialType specialType = this.indexTab.getSpecialType();

		String xyTrsl = this.indexTab.getXyTrsl();
		if (StringUtil.isNotEmpty(xyTrsl)) {
			// 专家模式，xytrsl不为空是饼、柱、折三种图
			//查信息列表（不是点击图跳转到的列表）时需要用xyTrsl去限制
			//如果是统计分析中的图表，也需要用xyTrsl去限制
			if (ColumnConst.LIST_NO_SIM.equals(this.indexTab.getType()) || (this.chartPage != null && this.chartPage.equals(ChartPageInfo.StatisticalChart))) {
				isFilterXyTrsl = false;
				//(IR_URLTIME:[20200709000000 TO 20200709015959]) AND
				//(
				//(IR_SITENAME:("河北新闻网")) OR  ( *:* -IR_SITENAME:("百度贴吧元搜索") )  OR  (*:* -IR_SITENAME:("百度贴吧")))
				List<CategoryBean> mediaType = CommonListChartUtil.getMediaType(xyTrsl);
				StringBuffer sb = new StringBuffer();
				for (CategoryBean categoryBean : mediaType) {
					if (StringUtil.isNotEmpty(categoryBean.getValue())) {
						String value = categoryBean.getValue().toLowerCase().trim();
						if (sb.length() > 0) {
							sb.append(" OR ");
						}
						if (value.startsWith("not")) {
							value = categoryBean.getValue().substring(3, categoryBean.getValue().length());
							while (value.startsWith("[ |	]")) {
								value = value.substring(1, categoryBean.getValue().length());
							}
							sb.append("(").append("*:* -").append(value).append(")");
						} else {
							sb.append("(").append(categoryBean.getValue()).append(")");
						}
					}
				}
				if (sb.length() > 0) {
					this.queryBuilder.filterByTRSL("(" + sb.toString() + ")");
					this.commonBuilder.filterByTRSL("(" + sb.toString() + ")");
				}
			}
		}
	}

}
