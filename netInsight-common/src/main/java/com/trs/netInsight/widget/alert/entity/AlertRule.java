package com.trs.netInsight.widget.alert.entity;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.WordSpacingUtil;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 预警规则 新建预警
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Entity
@Table(name = "alert_rule")
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1314426345955460538L;

	/**
	 * 定时任务开启或关闭状态
	 */
	@Column(name = "status")
	private ScheduleStatus status = ScheduleStatus.OPEN;

	/**
	 * 预警规则标题
	 */
	@Column(name = "title")
	private String title;

	/**
	 * 定时推送时间间隔 即频率
	 */
	@Column(name = "time_interval")
	private int timeInterval;

	/**
	 * 增长量
	 */
	@Column(name = "growth")
	private int growth;

	/**
	 * 预警机制 true：全量 false：排重
	 */
	@Column(name = "repetition")
	private boolean repetition = false;
	
	/**
     * 按urlname排重  0排 1不排  true排 false不排
     */
    @Column(name = "ir_simflag")
    private boolean irSimflag = false;
    
    /**
	 * 跨数据源排重
	 */
	@Column(name = "ir_simflag_all")
	private boolean irSimflagAll = false;

	/**
	 * 数据源 用;分割，如果为""，或者为ALL，则是所有数据源
	 */
	@Column(name = "group_name")
	private String groupName;

	/**
	 * 任意关键词
	 */
	@Column(name = "any_keywords", columnDefinition = "TEXT")
	private String anyKeyword;

	/**
	 * 排除词
	 */
	@Column(name = "exclude_words", columnDefinition = "TEXT")
	private String excludeWords;
	/**
	 * 排除词检索位置 0：标题 1：标题+正文  2：标题+摘要
	 */
	@Column(name = "exclude_words_index", columnDefinition = "TEXT")
	private String excludeWordsIndex = "1";
	public String getExcludeWordsIndex(){
		if(StringUtil.isEmpty(this.excludeWordsIndex)){
			if(SearchScope.TITLE_CONTENT.equals(this.scope)){//标题 + 正文
				return "1";
			}
			if(SearchScope.TITLE.equals(this.scope)){//标题
				return "0";
			}
			if(SearchScope.TITLE_ABSTRACT.equals(this.scope)){//标题 + 摘要
				return "2";
			}
			return "1";
		}
		return this.excludeWordsIndex;
	}
	/**
	 * 排除网站
	 */
	@Column(name = "exclude_site_name", columnDefinition = "TEXT")
	private String excludeSiteName;
	@Column(name = "monitor_site", columnDefinition = "TEXT")
	private String monitorSite;// 监测网站
	/**
	 * 关键词位置
	 */
	@Column(name = "scope")
	private SearchScope scope;
	/**
	 * 发送方式,SMS--短信, EMAIL--邮件, WE_CHAT--微信, WEBSITE 站内，多个用英文分号隔开
	 */
	@Column(name = "send_way",length=2550)
	private String sendWay;

	/**
	 * 针对于站内用户,站内用户发送方式
	 */
	@Column(name = "website_send_way")
	private String websiteSendWay;
	/**
	 * 针对于站内用户,站内用户的id
	 */
	@Column(name = "website_id",length=2550)
	private String websiteId;

	/**
	 * 预警开始时间
	 */
	@Column(name = "alert_start_hour")
	private String alertStartHour;

	/**
	 * 预警结束时间
	 */
	@Column(name = "alert_end_hour")
	private String alertEndHour;

	/**
	 * 阈值达到后保存的时间，下次直接从这个时间开始
	 */
	@Column(name = "last_start_time")
	private String lastStartTime;
	/**
	 * 上次预警发送时间
	 */
	@Column(name = "last_execution_time")
	private long lastExecutionTime;
	/**
	 * 预警类型
	 */
	@Column(name = "alert_type")
	private AlertSource alertType;

	/**
	 * 发送时间，星期一;星期二;星期三;星期四;星期五;星期六;星期日---- 多个用分号隔开
	 */
	@Column(name = "week")
	private String week;

	/**
	 * 模式：专家模式；普通模式
	 */
	@Column(name = "special_type")
	private SpecialType specialType;

	/**
	 * 完整检索表达式 专家模式
	 */
	@Column(name = "trsl", columnDefinition = "TEXT")
	private String trsl;

	/**
	 * 微博检索表达式 专家模式
	 */
	@Column(name = "status_trsl", columnDefinition = "TEXT")
	private String statusTrsl;

	/**
	 * 微信检索表达式 专家模式
	 */
	@Column(name = "we_chat_trsl", columnDefinition = "TEXT")
	private String weChatTrsl;
	
	/**
	 * 是否按照权重查找
	 */
	@Column(name = "weight")
	private boolean weight = false;

	/**
	 * 排序方式
	 */
	@Column(name = "sort")
	private String sort;

	/**
	 * 上次发送时间
	 */
	private Date lastSendTime;

	/**
	 * 接受者
	 */
	private String receiver;
	
	/**
	 * 默认空按数量计算预警  md5按照热度值计算预警
	 */
	@Column(name = "`count_by`")
	private String countBy;
	
	/**
	 * 定时扫描的频率的id  对应frequency表的id
	 * 默认3  五分钟一次
	 */
	@Column(name = "`frequency_id`")
	private String frequencyId;
	
	/**
	 * 按热度值预警时 分类统计后md5数量大于等于这个数时发送预警
	 */
	@Column(name = "`md5_num`")
	private int md5Num;
	
	/**
	 * 按热度值预警时 拼builder时的时间段  按小时存储 
	 * 比如5就是当前时间往前推5小时
	 */
	@Column(name = "`md5_range`")
	private int md5Range;
	
	/**
	 * 应前端要求加的两个字段 flag  show
	 */
	@Transient
	@Column(name = "`show`")
	private boolean show = false;
	
	@Transient
	@Column(name = "`flag`")
	private boolean flag = false;

	/**
	 * 媒体等级
	 */
	@Column(name = "media_level")
	private String mediaLevel;
	public String getMediaLevel(){
		if(StringUtil.isNotEmpty(this.mediaLevel)){
			return this.mediaLevel.replaceAll("其他","其它");
		}else{
			return StringUtils.join(Const.MEDIA_LEVEL,";").replaceAll("其他","其它");
		}
	}
	/**
	 * 媒体行业
	 */
	@Column(name = "media_industry")
	private String mediaIndustry;
	public String getMediaIndustry(){
		if(StringUtil.isNotEmpty(this.mediaIndustry)){
			return this.mediaIndustry.replaceAll("其他","其它");
		}else{
			return StringUtils.join(Const.MEDIA_INDUSTRY,";").replaceAll("其他","其它");
		}
	}
	/**
	 * 内容行业
	 */
	@Column(name = "content_industry")
	private String contentIndustry;
	public String getContentIndustry(){
		if(StringUtil.isNotEmpty(this.contentIndustry)){
			return this.contentIndustry.replaceAll("其他","其它");
		}else{
			return StringUtils.join(Const.CONTENT_INDUSTRY,";").replaceAll("其他","其它");
		}
	}
	/**
	 * 信息过滤  -  信息性质打标，如抽奖
	 */
	@Column(name = "filter_info")
	private String filterInfo;
	public String getFilterInfo(){
		if(StringUtil.isNotEmpty(this.filterInfo)){
			return this.filterInfo;
		}else{
			if (SpecialType.SPECIAL.equals(this.getSpecialType())){
				//处理历史专家模式的信息过滤
				return Const.NOT_FILTER_INFO;
			}
			return StringUtils.join(Const.FILTER_INFO,";");
		}
	}

	/**
	 * 精准筛选
	 */
	@Column(name = "precise_filter")
	private String preciseFilter;


	/**
	 * 内容所属地域
	 */
	@Column(name = "content_area")
	private String contentArea;
	public String getContentArea(){
		if(StringUtil.isNotEmpty(this.contentArea)){
			return this.contentArea.replaceAll("其他","其它");
		}else{
			return StringUtils.join(Const.AREA_LIST,";").replaceAll("其他","其它");
		}
	}
	/**
	 * 媒体所属地域
	 */
	@Column(name = "media_area")
	private String mediaArea;
	public String getMediaArea(){
		if(StringUtil.isNotEmpty(this.mediaArea)){
			return this.mediaArea.replaceAll("其他","其它");
		}else{
			return StringUtils.join(Const.AREA_LIST,";").replaceAll("其他","其它");
		}
	}
	

	/**
	 * 拼凑检索表达式 传统
	 * 
	 * @param time
	 *            频率id
	 * @return 返回拼凑后的builder
	 */
	public QueryBuilder toSearchBuilder(String time) throws OperationException {
		QueryBuilder searchBuilder = new QueryBuilder();
		searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		// 获取当前时间
		SimpleDateFormat df = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);// 设置日期格式
		String now = df.format(new Date());// new Date()为获取当前系统时间
		String before = null;
		//是按照热度值预警还是五分钟一次预警
		if("md5".equals(countBy)){
			before = DateUtil.getTimeByHour(-md5Range);
		}else{
			if(StringUtils.isBlank(lastStartTime)){
				if(timeInterval == 1){
					before = DateUtil.getTimeByMinute(-5);
				}else{
					before = DateUtil.getTimeByMinute(-this.timeInterval);
				}
			}else{
				before=this.lastStartTime;
			}
		}
		
		switch (specialType) {
		case COMMON:
			// 根据关键词位置，任意关键词，排除词，拼凑表达式
			String keywordIndex = "0";//仅标题
			if(SearchScope.TITLE_CONTENT.equals(this.scope)){
				keywordIndex = "1";//标题+正文
			}
			if(SearchScope.TITLE_ABSTRACT.equals(this.scope)){
				keywordIndex = "2";//标题+摘要
			}
			//关键词
			searchBuilder = WordSpacingUtil.handleKeyWords(this.anyKeyword,keywordIndex,this.weight);
			searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);

			String excludeIndex = this.getExcludeWordsIndex();
			//拼凑排除词
			String excludeWordTrsl = WordSpacingUtil.appendExcludeWords(excludeWords,excludeIndex);
			if(StringUtil.isNotEmpty(excludeWordTrsl)){
				searchBuilder.filterByTRSL(excludeWordTrsl);
			}
			/*for (String field : this.scope.getField()) {
				if (StringUtil.isNotEmpty(this.excludeWords)) {
					StringBuilder exBuilder = new StringBuilder();
					exBuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					searchBuilder.filterByTRSL(exBuilder.toString());
				}
			}*/

			if (StringUtil.isNotEmpty(this.excludeSiteName)) {
				if (this.excludeSiteName.endsWith(";") || this.excludeSiteName.endsWith("；")){
					this.excludeSiteName = this.excludeSiteName.substring(0,this.excludeSiteName.length()-1);
				}
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeSiteName.replaceAll(";|；", " OR "), Operator.NotEqual);
			}
			//监测网站
			if (StringUtil.isNotEmpty(this.monitorSite)) {
				if (this.monitorSite.endsWith(";") || this.monitorSite.endsWith("；")){
					this.monitorSite = this.monitorSite.substring(0,this.monitorSite.length()-1);
				}
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.monitorSite.replaceAll("[;|；]", " OR "), Operator.Equal);
			}
			break;
		case SPECIAL:
			if (StringUtils.isBlank(this.trsl)) {
				return null;
			}
			searchBuilder.filterByTRSL(this.trsl);
			break;
		default:
			break;
		}
		if(StringUtils.isNotBlank(time)){
			searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, DateUtil.formatTimeRange(time), Operator.Between);
		}else{
			searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, new String[] { before, now }, Operator.Between);
		}
		return searchBuilder;
	}

	/**
	 * 拼凑检索表达式 微博
	 * 
	 * @param time
	 *            频率id
	 * @return 返回拼凑后的builder
	 * @throws OperationException 
	 */
	public QueryBuilder toSearchBuilderWeiBo(String time) throws OperationException {
		QueryBuilder searchBuilder = new QueryBuilder();
		searchBuilder.setDatabase(Const.WEIBO);
			// 获取当前时间
		SimpleDateFormat df = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);// 设置日期格式
		String now = df.format(new Date());// new Date()为获取当前系统时间
		String before = null;
		//是按照热度值预警还是五分钟一次预警
		if("md5".equals(countBy)){
			before = DateUtil.getTimeByHour(-md5Range);
		}else{
			if(StringUtils.isBlank(lastStartTime)){
				if(timeInterval == 1){
					before = DateUtil.getTimeByMinute(-5);
				}else{
					before = DateUtil.getTimeByMinute(-this.timeInterval);
				}
			}else{
				before=this.lastStartTime;
			}
		}
		switch (specialType) {
		case COMMON:
			// 根据关键词位置，任意关键词，排除词，拼凑表达式
			// 内容
			String field = FtsFieldConst.FIELD_STATUS_CONTENT;

			//关键词
			searchBuilder = WordSpacingUtil.handleKeyWordsToWeiboTF(this.anyKeyword,this.weight);
			searchBuilder.setDatabase(Const.WEIBO);
			//拼凑排除词
			String excludeIndex = this.getExcludeWordsIndex();

			String excludeWordTrsl = WordSpacingUtil.appendExcludeWords(excludeWords,excludeIndex);
			if(StringUtil.isNotEmpty(excludeWordTrsl)){
				searchBuilder.filterByTRSL(excludeWordTrsl);
			}
			/*//拼接排除词
			if (StringUtil.isNotEmpty(this.excludeWords)) {
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				searchBuilder.filterByTRSL(exbuilder.toString());
			}*/
			//excludeSiteName 这个字段是排除网站   不应该和 excludeWords一样都拼在 URLTITLE里 或者URLTITLE + CONTENT 里  应该拼在 IR_SITENAME字段里
			if (StringUtil.isNotEmpty(this.excludeSiteName)) {
				if (this.excludeSiteName.endsWith(";") || this.excludeSiteName.endsWith("；")){
					this.excludeSiteName = this.excludeSiteName.substring(0,this.excludeSiteName.length()-1);
				}
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeSiteName.replaceAll(";|；", " OR "), Operator.NotEqual);
			}
			//监测网站
			if (StringUtil.isNotEmpty(this.monitorSite)) {
				if (this.monitorSite.endsWith(";") || this.monitorSite.endsWith("；")){
					this.monitorSite = this.monitorSite.substring(0,this.monitorSite.length()-1);
				}
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.monitorSite.replaceAll("[;|；]", " OR "), Operator.Equal);
			}
			
			break;
		case SPECIAL:
			if (StringUtils.isBlank(this.statusTrsl)) {
				return null;
			}
			searchBuilder.filterByTRSL(this.statusTrsl);
			break;

		default:
			break;
		}
		if(StringUtils.isNotBlank(time)){
			searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, DateUtil.formatTimeRange(time), Operator.Between);
		}else{
			searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, new String[] { before, now }, Operator.Between);
		}
		return searchBuilder;
	}

	/**
	 * 拼凑检索表达式 微信
	 * 
	 * @param time
	 *            频率id
	 * @return 返回拼凑后的builder 现在介于微信没有摘要字段
	 */
	public QueryBuilder toSearchBuilderWeiXin(String time) throws OperationException {
		QueryBuilder searchBuilder = new QueryBuilder();
		searchBuilder.setDatabase(Const.WECHAT);
			// 获取当前时间
		SimpleDateFormat df = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);// 设置日期格式
		String now = df.format(new Date());// new Date()为获取当前系统时间
		String before = null;
		//是按照热度值预警还是五分钟一次预警
		if("md5".equals(countBy)){
			before = DateUtil.getTimeByHour(-md5Range);
		}else{
			if(StringUtils.isBlank(lastStartTime)){
				if(timeInterval == 1){
					before = DateUtil.getTimeByMinute(-5);
				}else{
					before = DateUtil.getTimeByMinute(-this.timeInterval);
				}
			}else{
				before=this.lastStartTime;
			}
		}
		switch (specialType) {
		case COMMON:
			// 根据关键词位置，任意关键词，排除词，拼凑表达式
			//关键词
			String keywordIndex = "0";//仅标题
			if(SearchScope.TITLE_CONTENT.equals(this.scope)){
				keywordIndex = "1";//标题+正文
			}
			if(SearchScope.TITLE_ABSTRACT.equals(this.scope)){
				keywordIndex = "2";//标题+摘要
			}
			searchBuilder = WordSpacingUtil.handleKeyWords(this.anyKeyword,keywordIndex,this.weight);
			searchBuilder.setDatabase(Const.WECHAT);
			//拼凑排除词
			String excludeIndex = this.getExcludeWordsIndex();

			String excludeWordTrsl = WordSpacingUtil.appendExcludeWords(excludeWords,excludeIndex);
			if(StringUtil.isNotEmpty(excludeWordTrsl)){
				searchBuilder.filterByTRSL(excludeWordTrsl);
			}
			/*//拼接排除词
			for (String field : this.scope.getField()) {
				if (StringUtil.isNotEmpty(this.excludeWords)) {
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					searchBuilder.filterByTRSL(exbuilder.toString());
				}
			}*/
			if (StringUtil.isNotEmpty(this.excludeSiteName)) {
				if (this.excludeSiteName.endsWith(";") || this.excludeSiteName.endsWith("；")){
					this.excludeSiteName = this.excludeSiteName.substring(0,this.excludeSiteName.length()-1);
				}
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeSiteName.replaceAll(";|；", " OR "), Operator.NotEqual);
			}
			//监测网站
			if (StringUtil.isNotEmpty(this.monitorSite)) {
				if (this.monitorSite.endsWith(";") || this.monitorSite.endsWith("；")){
					this.monitorSite = this.monitorSite.substring(0,this.monitorSite.length()-1);
				}
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.monitorSite.replaceAll("[;|；]", " OR "), Operator.Equal);
			}
			break;
		case SPECIAL:
			if (StringUtils.isBlank(this.weChatTrsl)) {
				return null;
			}
			searchBuilder.filterByTRSL(this.weChatTrsl);
			break;

		}
		if(StringUtils.isNotBlank(time)){
			searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, DateUtil.formatTimeRange(time), Operator.Between);
		}else{
			searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, new String[] { before, now }, Operator.Between);
		}
		return searchBuilder;
	}

	/**
	 * 拼凑检索表达式  全部数据源
	 *
	 * @param time
	 *            频率id
	 * @return 返回拼凑后的builder
	 */
	public QueryCommonBuilder toSearchBuilderCommon(String time) throws OperationException {
		QueryCommonBuilder searchBuilder = new QueryCommonBuilder();
		//searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		// 获取当前时间
		SimpleDateFormat df = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);// 设置日期格式
		String now = df.format(new Date());// new Date()为获取当前系统时间
		String before = null;
		//是按照热度值预警还是五分钟一次预警
		if("md5".equals(countBy)){
			before = DateUtil.getTimeByHour(-md5Range);
		}else{
			if(StringUtils.isBlank(lastStartTime)){
				if(timeInterval == 1){
					before = DateUtil.getTimeByMinute(-5);
				}else{
					before = DateUtil.getTimeByMinute(-this.timeInterval);
				}
			}else{
				before=this.lastStartTime;
			}
		}

		switch (specialType) {
			case COMMON:
				// 根据关键词位置，任意关键词，排除词，拼凑表达式
				String keywordIndex = "0";//仅标题
				if(SearchScope.TITLE_CONTENT.equals(this.scope)){
					keywordIndex = "1";//标题+正文
				}
				if(SearchScope.TITLE_ABSTRACT.equals(this.scope)){
					keywordIndex = "2";//标题+摘要
				}
				//关键词
				QueryBuilder builder = WordSpacingUtil.handleKeyWords(this.anyKeyword,keywordIndex,this.weight);
				searchBuilder.filterByTRSL(builder.asTRSL());
				searchBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
				//拼凑排除词
				String excludeIndex = this.getExcludeWordsIndex();
				String excludeWordTrsl = WordSpacingUtil.appendExcludeWords(excludeWords,excludeIndex);
				if(StringUtil.isNotEmpty(excludeWordTrsl)){
					searchBuilder.filterByTRSL(excludeWordTrsl);
				}
				/*for (String field : this.scope.getField()) {
					if (StringUtil.isNotEmpty(this.excludeWords)) {
						StringBuilder exBuilder = new StringBuilder();
						exBuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						searchBuilder.filterByTRSL(exBuilder.toString());
					}
				}*/

				if (StringUtil.isNotEmpty(this.excludeSiteName)) {
					if (this.excludeSiteName.endsWith(";") || this.excludeSiteName.endsWith("；")){
						this.excludeSiteName = this.excludeSiteName.substring(0,this.excludeSiteName.length()-1);
					}
					searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME_LIKE, this.excludeSiteName.replaceAll(";|；", " OR "), Operator.NotEqual);
				}
				//监测网站
				if (StringUtil.isNotEmpty(this.monitorSite)) {
					if (this.monitorSite.endsWith(";") || this.monitorSite.endsWith("；")){
						this.monitorSite = this.monitorSite.substring(0,this.monitorSite.length()-1);
					}
					searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME_LIKE, this.monitorSite.replaceAll("[;|；]", " OR "), Operator.Equal);
				}
				break;
			case SPECIAL:
				String trsl = "";
				List<String> database = new ArrayList<>();
				if(this.trsl != null && !"".equals(this.trsl)){
					String trsl_1 = FtsFieldConst.FIELD_GROUPNAME + ":("+Const.TYPE_NEWS_SPECIAL_ALERT.replaceAll(";"," OR ")+") AND (" + this.trsl +")";
					trsl = "("+trsl_1+ ")";
					database.add(Const.HYBASE_NI_INDEX);
					database.add(Const.HYBASE_OVERSEAS);
				}
				if(this.statusTrsl != null && !"".equals(this.statusTrsl)){
					String trsl_1  = FtsFieldConst.FIELD_GROUPNAME +  ":(微博) AND (" + this.statusTrsl +")";
					trsl = "".equals(trsl) ? "("+trsl_1 + ")" : trsl +" OR (" + trsl_1  + ")";
					database.add(Const.WEIBO);
				}
				if(this.weChatTrsl != null && !"".equals(this.weChatTrsl)){
					String trsl_1 = FtsFieldConst.FIELD_GROUPNAME +  ":(国内微信 OR 微信 ) AND (" + this.weChatTrsl +")";
					trsl = "".equals(trsl) ? "("+trsl_1+ ")" : trsl +" OR (" + trsl_1 + ")";
					database.add(Const.WECHAT_COMMON);
				}
				if (StringUtils.isBlank(trsl)) {
					return  null;
				}
				searchBuilder.filterByTRSL(trsl);
				String[] arrays = new String[database.size()];
				searchBuilder.setDatabase(database.toArray(arrays));
				break;
			default:
				break;
		}
		if(!"noTime".equals(time)){
			if(StringUtils.isNotBlank(time)){
					searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, DateUtil.formatTimeRange(time), Operator.Between);
			}else{
				searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, new String[] { before, now }, Operator.Between);
			}
		}
		return searchBuilder;
	}

	/**
	 * 拼凑检索表达式  全部数据源
	 *
	 * @param time
	 *            频率id
	 * @return 返回拼凑后的builder
	 */
	public QueryCommonBuilder toSearchBuilderCommonNew(String time,String sort) throws OperationException {
		QueryCommonBuilder searchBuilder = new QueryCommonBuilder();
		//searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		// 获取当前时间
		SimpleDateFormat df = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);// 设置日期格式
		String now = df.format(new Date());// new Date()为获取当前系统时间
		String before = null;
		//是按照热度值预警还是五分钟一次预警
		if("md5".equals(countBy)){
			before = DateUtil.getTimeByHour(-md5Range);
		}else{
			if(StringUtils.isBlank(lastStartTime)){
				if(timeInterval == 1){
					before = DateUtil.getTimeByMinute(-5);
				}else{
					before = DateUtil.getTimeByMinute(-this.timeInterval);
				}
			}else{
				before=this.lastStartTime;
			}
		}

		addFilterCondition(mediaLevel, mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea, preciseFilter);
		switch (specialType) {
			case COMMON:
				// 根据关键词位置，任意关键词，排除词，拼凑表达式
				String keywordIndex = "0";//仅标题
				if(SearchScope.TITLE_CONTENT.equals(this.scope)){
					keywordIndex = "1";//标题+正文
				}
				if(SearchScope.TITLE_ABSTRACT.equals(this.scope)){
					keywordIndex = "2";//标题+摘要
				}
				//关键词
				QueryBuilder builder = WordSpacingUtil.handleKeyWords(this.anyKeyword,keywordIndex,this.weight);
				searchBuilder.filterByTRSL(builder.asTRSL());
				searchBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
				//拼凑排除词
				String excludeIndex = this.getExcludeWordsIndex();
				String excludeWordTrsl = WordSpacingUtil.appendExcludeWords(excludeWords,excludeIndex);
				if(StringUtil.isNotEmpty(excludeWordTrsl)){
					searchBuilder.filterByTRSL(excludeWordTrsl);
				}
				/*for (String field : this.scope.getField()) {
					if (StringUtil.isNotEmpty(this.excludeWords)) {
						StringBuilder exBuilder = new StringBuilder();
						exBuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						searchBuilder.filterByTRSL(exBuilder.toString());
					}
				}*/

				if (StringUtil.isNotEmpty(this.excludeSiteName)) {
					if (this.excludeSiteName.endsWith(";") || this.excludeSiteName.endsWith("；")){
						this.excludeSiteName = this.excludeSiteName.substring(0,this.excludeSiteName.length()-1);
					}
					searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME_LIKE, this.excludeSiteName.replaceAll(";|；", " OR "), Operator.NotEqual);
				}
				//监测网站
				if (StringUtil.isNotEmpty(this.monitorSite)) {
					if (this.monitorSite.endsWith(";") || this.monitorSite.endsWith("；")){
						this.monitorSite = this.monitorSite.substring(0,this.monitorSite.length()-1);
					}
					searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME_LIKE, this.monitorSite.replaceAll("[;|；]", " OR "), Operator.Equal);
				}

				//筛选条件信息
				//媒体等级
				if(StringUtil.isNotEmpty(mediaLevel)){
					addFieldFilter(searchBuilder,FtsFieldConst.FIELD_MEDIA_LEVEL,mediaLevel,Const.MEDIA_LEVEL);
				}
				//媒体行业
				if(StringUtil.isNotEmpty(mediaIndustry )){
					addFieldFilter(searchBuilder,FtsFieldConst.FIELD_MEDIA_INDUSTRY,mediaIndustry,Const.MEDIA_INDUSTRY);
				}
				//内容行业
				if(StringUtil.isNotEmpty(contentIndustry )){
					addFieldFilter(searchBuilder,FtsFieldConst.FIELD_CONTENT_INDUSTRY,contentIndustry,Const.CONTENT_INDUSTRY);
				}
				//内容地域
				if(StringUtil.isNotEmpty(contentArea )){
					addAreaFilter(searchBuilder,FtsFieldConst.FIELD_CATALOG_AREA,contentArea);
				}
				//媒体地域
				if(StringUtil.isNotEmpty(mediaArea )){
					addAreaFilter(searchBuilder,FtsFieldConst.FIELD_MEDIA_AREA,mediaArea);
				}

				//信息过滤
				if(StringUtil.isNotEmpty(filterInfo) && !filterInfo.equals(Const.NOT_FILTER_INFO)){
					String trsl = searchBuilder.asTRSL();
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
						searchBuilder = new QueryCommonBuilder();
						searchBuilder.filterByTRSL(sb.toString());
					}
				}
				break;
			case SPECIAL:
				String trsl = "";
				List<String> database = new ArrayList<>();
				if(this.trsl != null && !"".equals(this.trsl)){
					String trsl_1 = FtsFieldConst.FIELD_GROUPNAME + ":("+Const.TYPE_NEWS_SPECIAL_ALERT.replaceAll(";"," OR ")+") AND (" + this.trsl +")";
					trsl = "("+trsl_1+ ")";
					database.add(Const.HYBASE_NI_INDEX);
					database.add(Const.HYBASE_OVERSEAS);
				}
				if(this.statusTrsl != null && !"".equals(this.statusTrsl)){
					String trsl_1  = FtsFieldConst.FIELD_GROUPNAME +  ":(微博) AND (" + this.statusTrsl +")";
					trsl = "".equals(trsl) ? "("+trsl_1 + ")" : trsl +" OR (" + trsl_1  + ")";
					database.add(Const.WEIBO);
				}
				if(this.weChatTrsl != null && !"".equals(this.weChatTrsl)){
					String trsl_1 = FtsFieldConst.FIELD_GROUPNAME +  ":(国内微信 OR 微信 ) AND (" + this.weChatTrsl +")";
					trsl = "".equals(trsl) ? "("+trsl_1+ ")" : trsl +" OR (" + trsl_1 + ")";
					database.add(Const.WECHAT_COMMON);
				}
				if (StringUtils.isBlank(trsl)) {
					return  null;
				}
				searchBuilder.filterByTRSL(trsl);
				String[] arrays = new String[database.size()];
				searchBuilder.setDatabase(database.toArray(arrays));
				break;
			default:
				break;
		}
		if(!"noTime".equals(time)){
			if(StringUtils.isNotBlank(time)){
				if("loadtime".equals(sort)){
					searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, DateUtil.formatTimeRange(time), Operator.Between);
				}else {
					searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				}
			}else{
				if("loadtime".equals(sort)){
					searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, new String[] { before, now }, Operator.Between);
				}else {
					searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { before, now }, Operator.Between);
				}
			}
		}
		return searchBuilder;
	}

	/**
	 * 添加筛选条件信息  ---  如果要添加筛选条件，需要写在initSection 方法前面,
	 * @param mediaLevel  媒体等级
	 * @param mediaIndustry  媒体行业
	 * @param contentIndustry  内容行业
	 * @param filterInfo  过滤信息
	 * @param contentArea  内容行业
	 * @param mediaArea  媒体行业
	 * @param preciseFilter  精准筛选
	 */
	public void addFilterCondition(String mediaLevel,String mediaIndustry,String contentIndustry,String filterInfo,
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
		this.mediaLevel = mediaLevel;
		this.mediaIndustry = mediaIndustry;
		this.contentIndustry = contentIndustry;
		this.filterInfo = filterInfo;
		this.contentArea = contentArea;
		this.mediaArea = mediaArea;
		this.preciseFilter = preciseFilter;
	}

	private void addFieldFilter(QueryCommonBuilder queryBuilder,String field,String value,List<String> allValue){
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
				queryBuilder.filterField(field,StringUtils.join(valueArrList," OR ") , Operator.Equal);
			}
		}
	}

	private void addAreaFilter(QueryCommonBuilder queryBuilder,String field,String areas){
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
				if(FtsFieldConst.FIELD_CATALOG_AREA.equals(field) && this.getGroupName().contains(Const.TYPE_WEIXIN)){
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(FtsFieldConst.FIELD_CATALOG_AREA).append(":(").append(StringUtils.join(areaList," OR ")).append(")").append(" OR ").append(FtsFieldConst.FIELD_CATALOG_AREA_MULTIPLE).append(":(").append(StringUtils.join(areaList," OR ")).append(")");
					queryBuilder.filterByTRSL(stringBuilder.toString());
				}else {
					queryBuilder.filterField(field, StringUtils.join(areaList, " OR "), Operator.Equal);
				}
			}
		}
	}

	public String getAlertRuleTrsl() throws OperationException {
		QueryCommonBuilder queryCommonBuilder = this.toSearchBuilderCommon("noTime");
		String queryTrsl = queryCommonBuilder.asTRSL();
		if(this.specialType.equals(SpecialType.COMMON)) {
			if (StringUtil.isNotEmpty(this.getGroupName())) {
				List<String> groupList = CommonListChartUtil.formatGroupName(this.getGroupName());
				String groupTrsl = StringUtils.join(groupList, " OR ");
				queryTrsl = "((" + queryTrsl + ") AND (IR_GROUPNAME:(" + groupTrsl + ")))";
			}
		}
		// simflag排重 1000为不重复
		if (repetition) {
			if (StringUtils.isNotBlank(queryTrsl)) {
				queryTrsl = queryTrsl + " AND (" + FtsFieldConst.FIELD_SIMFLAG + ":(1000 OR \"\"))";
			} else {
				queryTrsl = FtsFieldConst.FIELD_SIMFLAG + ":(1000 OR \"\")";
			}
		}
		// 该配置默认清除历史配置
//		if (irSimflagOpen) {
//			irSimflag = true;
//		}
		if (irSimflag) {
			if (StringUtils.isNotBlank(queryTrsl)) {
				queryTrsl = queryTrsl + " AND (" + Const.IR_SIMFLAG_TRSL + ")";
			} else {
				queryTrsl = Const.IR_SIMFLAG_TRSL;
			}
		}
		if (irSimflagAll) {
			if (StringUtils.isNotBlank(queryTrsl)) {
				queryTrsl = queryTrsl + " AND (" + Const.IR_SIMFLAGALL_TRSL + ")";
			} else {
				queryTrsl = Const.IR_SIMFLAGALL_TRSL;
			}
		}
		return queryTrsl;

	}
	public String getQueryHybaseDataBase(){
		String database = Const.MIX_DATABASE;
		switch (specialType) {
			case COMMON:
				String group = this.getGroupName();

				String[] databaseArr = TrslUtil.chooseDatabases(CommonListChartUtil.changeGroupName(group).split(";"));
				List<String> dataList = new ArrayList<>();
				for(String data :databaseArr){
					if(StringUtil.isNotEmpty(data)){
						dataList.add(data);
					}
				}
				database = StringUtils.join(dataList,";");
				break;
			case SPECIAL:
				List<String> dataListS = new ArrayList<>();
				if(this.trsl != null && !"".equals(this.trsl)){
					dataListS.add(Const.HYBASE_NI_INDEX);
					dataListS.add(Const.HYBASE_OVERSEAS);
				}
				if(this.statusTrsl != null && !"".equals(this.statusTrsl)){
					dataListS.add(Const.WEIBO);
				}
				if(this.weChatTrsl != null && !"".equals(this.weChatTrsl)){
					dataListS.add(Const.WECHAT_COMMON);
				}
				database = StringUtils.join(dataListS,";");
				break;
			default:
				break;
		}
		return database;
	}

}
