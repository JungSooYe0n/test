package com.trs.netInsight.widget.alert.entity;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.WordSpacingUtil;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	 * 排除网站
	 */
	@Column(name = "exclude_site_name", columnDefinition = "TEXT")
	private String excludeSiteName;

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
			//关键词
			searchBuilder = WordSpacingUtil.handleKeyWords(this.anyKeyword,keywordIndex,this.weight);
			searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			for (String field : this.scope.getField()) {
				if (StringUtil.isNotEmpty(this.excludeWords)) {
					StringBuilder exBuilder = new StringBuilder();
					exBuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					searchBuilder.filterByTRSL(exBuilder.toString());
				}
			}

			if (StringUtil.isNotEmpty(this.excludeSiteName)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeSiteName.replaceAll(";|；", " OR "), Operator.NotEqual);
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
			//拼接排除词
			if (StringUtil.isNotEmpty(this.excludeWords)) {
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				searchBuilder.filterByTRSL(exbuilder.toString());
			}
			//excludeSiteName 这个字段是排除网站   不应该和 excludeWords一样都拼在 URLTITLE里 或者URLTITLE + CONTENT 里  应该拼在 IR_SITENAME字段里
			if (StringUtil.isNotEmpty(this.excludeSiteName)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeSiteName.replaceAll(";|；", " OR "), Operator.NotEqual);
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
			searchBuilder = WordSpacingUtil.handleKeyWords(this.anyKeyword,keywordIndex,this.weight);
			searchBuilder.setDatabase(Const.WECHAT);
			//拼接排除词
			for (String field : this.scope.getField()) {
				if (StringUtil.isNotEmpty(this.excludeWords)) {
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					searchBuilder.filterByTRSL(exbuilder.toString());
				}
			}
			if (StringUtil.isNotEmpty(this.excludeSiteName)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeSiteName.replaceAll(";|；", " OR "), Operator.NotEqual);
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
				//关键词
				QueryBuilder builder = WordSpacingUtil.handleKeyWords(this.anyKeyword,keywordIndex,this.weight);
				searchBuilder.filterByTRSL(builder.asTRSL());
				searchBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
				for (String field : this.scope.getField()) {
					if (StringUtil.isNotEmpty(this.excludeWords)) {
						StringBuilder exBuilder = new StringBuilder();
						exBuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						searchBuilder.filterByTRSL(exBuilder.toString());
					}
				}

				if (StringUtil.isNotEmpty(this.excludeSiteName)) {
					searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeSiteName.replaceAll(";|；", " OR "), Operator.NotEqual);
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
					String trsl_1  = FtsFieldConst.FIELD_GROUPNAME +  ":(\"微博\") AND (" + this.statusTrsl +")";
					trsl = "".equals(trsl) ? "("+trsl_1 + ")" : trsl +" OR (" + trsl_1  + ")";
					database.add(Const.WEIBO);
				}
				if(this.weChatTrsl != null && !"".equals(this.weChatTrsl)){
					String trsl_1 = FtsFieldConst.FIELD_GROUPNAME +  ":(\"国内微信\" OR \"微信\" ) AND (" + this.weChatTrsl +")";
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
		if(StringUtils.isNotBlank(time)){
			searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, DateUtil.formatTimeRange(time), Operator.Between);
		}else{
			searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, new String[] { before, now }, Operator.Between);
		}
		return searchBuilder;
	}
}
