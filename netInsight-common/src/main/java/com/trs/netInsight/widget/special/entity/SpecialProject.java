package com.trs.netInsight.widget.special.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.config.serualizer.DateDeserializer;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.WordSpacingUtil;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 专项实体
 * <p>
 * Create by yan.changjiang on 2017年11月22日
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "special_project")
public class SpecialProject extends BaseEntity {
	/**
	* 
	*/
	private static final long serialVersionUID = -6694257325975609824L;

	/**
	 * 专项类型
	 */
	@Column(name = "special_type")
	@JsonView(SaveView.class)
	@ApiModelProperty(notes = "专项类型")
	private SpecialType specialType;

	/**
	 * 专项名
	 */
	@JsonView(DisplayView.class)
	@Column(name = "special_name")
	@ApiModelProperty(required = true, notes = "专项名")
	private String specialName;

	/**
	 * 所有关键词
	 */
	@JsonView(SaveView.class)
	@Column(name = "all_keywords", columnDefinition = "TEXT")
	private String allKeywords;

	/**
	 * 任意关键词
	 */
	@JsonView(SaveView.class)
	@Column(name = "any_keywords", columnDefinition = "TEXT")
	private String anyKeywords;

	/**
	 * 排除词
	 */
	@JsonView(SaveView.class)
	@Column(name = "exclude_words", columnDefinition = "TEXT")
	private String excludeWords;
	/**
	 * 排除词检索位置 0：标题 1：标题+正文  2：标题+摘要
	 */
	@Column(name = "exclude_word_index", columnDefinition = "TEXT")
	private String excludeWordIndex = "1";
//	public String getExcludeWordIndex(){
//		if(StringUtil.isEmpty(this.excludeWordIndex)){
//			if(SearchScope.TITLE_CONTENT.equals(this.searchScope)){//标题 + 正文
//				return "1";
//			}
//			if(SearchScope.TITLE.equals(this.searchScope)){//标题
//				return "0";
//			}
//			if(SearchScope.TITLE_ABSTRACT.equals(this.searchScope)){//标题 + 摘要
//				return "2";
//			}
//		}
//		return this.excludeWordIndex;
//	}

	/**
	 * 排除网站
	 */

	@JsonView(SaveView.class)
	@Column(name = "excludeWeb", columnDefinition = "TEXT")
	private String excludeWeb;

	@Column(name = "monitor_site", columnDefinition = "TEXT")
	private String monitorSite;// 监测网站

	/**
	 * 完整检索表达式
	 */
	@JsonView(SaveView.class)
	@Column(name = "trsl", columnDefinition = "TEXT")
	private String trsl;

	/**
	 * 微博检索表达式
	 */
	@JsonView(SaveView.class)
	@Column(name = "status_trsl", columnDefinition = "TEXT")
	private String statusTrsl;

	/**
	 * 微信检索表达式
	 */
	@JsonView(SaveView.class)
	@Column(name = "we_chat_trsl", columnDefinition = "TEXT")
	private String weChatTrsl;

	/**
	 * 搜索位置
	 */
	@JsonView(SaveView.class)
	@Column(name = "search_scope")
	private SearchScope searchScope;

	/**
	 * 检索开始时间
	 */
	@JsonView(SaveView.class)
	@Column(name = "start_time")
	@JsonDeserialize(using = DateDeserializer.class)
	@ApiModelProperty(value = "开始时间", example = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;

	/**
	 * 检索结束时间
	 */
	@JsonView(SaveView.class)
	@Column(name = "end_time")
	@JsonDeserialize(using = DateDeserializer.class)
	@ApiModelProperty(value = "结束时间", example = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;
	/**
	 * 分类对比类型
	 */
	@Column(name = "contrast")
	private String contrast;

	/**
	 * 存储3d 7d 24h
	 */
	@JsonView(SaveView.class)
	@JsonDeserialize(using = DateDeserializer.class)
	private String timeRange;
	
	/**
	 * 来源
	 */
	@JsonView(SaveView.class)
	@Column(name = "source")
	private String source;

	/**
	 * 专题名
	 */
	@JsonView(SaveView.class)
	@Column(name = "subject_name")
	private String subjectName;

	/**
	 * 所属分组
	 */
	@JsonView(SaveView.class)
	private String groupName;
	/**
	 * 所属分组ID
	 */
	@JsonView(SaveView.class)
	private String groupId;
	/**
	 * currentTheme 方案false boolean类型 MYSQL保存BOOLEAN值时用1代表TRUE,0代表FALSE
	 */
	@JsonView(SaveView.class)
	private boolean currentTheme;
	/**
	 * 主题0 专题1 方案2
	 */
	@JsonView(SaveView.class)
	private int flag;
	/**
	 * 用于展开更多中展示图片
	 */
	private String imgUrl;
	/**
	 * 用于首页轮播存储今日增量
	 */
	private long todayCount;
	/**
	 * 全部数量
	 */
	private long allCount;
	/**
	 * 置顶（新提出置顶字段）
	 */
	private String topFlag;
//	private String read;//  阅读标记
	private String preciseFilter;//精准筛选

	/**
	 * 是否排重
	 */
	@Column(name = "similar")
	private boolean similar;
	/**
	 * 跨数据源排重
	 */
	@Column(name = "ir_simflag_all")
	private boolean irSimflagAll = false;
	/**
	 * 是否按照权重查找
	 */
	@Column(name = "weight")
	private boolean weight = false;
	
	/**
     * 用作DateUtil返回时间
     */
    private String start="0";
    /**
     * 用作DateUtil返回时间
     */
    private String end="0";
    /**
     * 按urlname排重  0排 1不排  true排 false不排
     */
    @Column(name = "ir_simflag")
    private boolean irSimflag = false;
    
    /**
     * 专家模式情况下  是否转换为server表达式
     */
    @Column(name = "server")
    private boolean server = false;
    
    /**
     * 排序字段
     */
    @Column(name = "sequence", columnDefinition = "INT default 0")
	private int sequence;

	/**
	 * 多对一关联关系
	 */
	@ManyToOne()
	@JsonIgnore
	@JoinColumn(name = "special_subject_id")
	private SpecialSubject specialSubject;
	/**
	 * 媒体等级
	 */
	@Column(name = "media_level")
	private String mediaLevel;
	/**
	 * 媒体行业
	 */
	@Column(name = "media_industry")
	private String mediaIndustry;
	/**
	 * 内容行业
	 */
	@Column(name = "content_industry")
	private String contentIndustry;
	/**
	 * 信息过滤  -  信息性质打标，如抽奖
	 */
	@Column(name = "filter_info")
	private String filterInfo;
	/**
	 * 内容所属地域
	 */
	@Column(name = "content_area")
	private String contentArea;
	/**
	 * 媒体所属地域
	 */
	@Column(name = "media_area")
	private String mediaArea;

	public void setStart(String start) throws ParseException {
		this.start = start;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date parse = simpleDateFormat.parse(start);
		this.startTime = parse;
	}

	public void setEnd(String end) throws ParseException {
		this.end = end;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date parse = simpleDateFormat.parse(end);
		this.endTime = parse;
	}
	/**
	 * 构造函数
	 */
	public SpecialProject(String userId, SpecialType specialType, String specialName,
						  String anyKeywords, String excludeWords, String trsl,
						  SearchScope searchScope, Date startTime, Date endTime, String source, String groupName,
						  String groupId,String timeRange,int sequence,boolean isSimilar,boolean irSimflag,boolean weight,boolean server,boolean irSimflagAll,String excludeWeb) {
		this.setUserId(userId);
		this.specialType = specialType;
		this.specialName = specialName;
		this.anyKeywords = anyKeywords;
		this.excludeWords = excludeWords;
		this.trsl = trsl;
		this.searchScope = searchScope;
		this.startTime = startTime;
		this.endTime = endTime;
		this.source = source;
		this.groupName = groupName;
		this.groupId = groupId;
		this.currentTheme = false;
		this.timeRange=timeRange;
		this.sequence = sequence;
		this.similar = isSimilar;
		this.irSimflag = irSimflag;
		this.weight = weight;
		this.server = server;
		this.irSimflagAll = irSimflagAll;
		this.excludeWeb =excludeWeb;
		super.setCreatedTime(new Date());
		super.setLastModifiedTime(new Date());
	}
	/**
	 * 构造函数
	 */
	public SpecialProject(String userId, SpecialType specialType, String specialName, String allKeywords,
			String anyKeywords, String excludeWords, String trsl, String statusTrsl, String weChatTrsl,
			SearchScope searchScope, Date startTime, Date endTime, String source, String groupName, 
			String groupId,String timeRange,boolean isSimilar,boolean irSimflag,boolean weight,boolean server,boolean irSimflagAll,String excludeWeb) {
		this.setUserId(userId);
		this.specialType = specialType;
		this.specialName = specialName;
		this.allKeywords = allKeywords;
		this.anyKeywords = anyKeywords;
		this.excludeWords = excludeWords;
		this.trsl = trsl;
		this.statusTrsl = statusTrsl;
		this.weChatTrsl = weChatTrsl;
		this.searchScope = searchScope;
		this.startTime = startTime;
		this.endTime = endTime;
		this.source = source;
		this.groupName = groupName;
		this.groupId = groupId;
		this.currentTheme = false;
		this.flag = 2;
		this.timeRange=timeRange;
		this.similar = isSimilar;
		this.irSimflag = irSimflag;
		this.weight = weight;
		this.server = server;
		this.irSimflagAll = irSimflagAll;
		this.excludeWeb =excludeWeb;
		super.setCreatedTime(new Date());
		super.setLastModifiedTime(new Date());
	}
	/**
	 * 构造函数
	 */
	public SpecialProject(String userId, SpecialType specialType, String specialName, String allKeywords,
						  String anyKeywords, String excludeWords, String trsl, String statusTrsl, String weChatTrsl,
						  SearchScope searchScope, Date startTime, Date endTime, String source, String groupName,
						  String groupId,String timeRange,int sequence,boolean isSimilar,boolean irSimflag,boolean weight,boolean server,boolean irSimflagAll,String excludeWeb) {
		this.setUserId(userId);
		this.specialType = specialType;
		this.specialName = specialName;
		this.allKeywords = allKeywords;
		this.anyKeywords = anyKeywords;
		this.excludeWords = excludeWords;
		this.trsl = trsl;
		this.statusTrsl = statusTrsl;
		this.weChatTrsl = weChatTrsl;
		this.searchScope = searchScope;
		this.startTime = startTime;
		this.endTime = endTime;
		this.source = source;
		this.groupName = groupName;
		this.groupId = groupId;
		this.currentTheme = false;
		this.flag = 2;
		this.timeRange=timeRange;
		this.sequence = sequence;
		this.similar = isSimilar;
		this.irSimflag = irSimflag;
		this.weight = weight;
		this.server = server;
		this.irSimflagAll = irSimflagAll;
		this.excludeWeb =excludeWeb;
		super.setCreatedTime(new Date());
		super.setLastModifiedTime(new Date());
	}
	public SpecialProject(String userId,String subGroupId, SpecialType specialType, String specialName, String allKeywords,
						  String anyKeywords, String excludeWords, String trsl, String statusTrsl, String weChatTrsl,
						  SearchScope searchScope, Date startTime, Date endTime, String source, String groupName,
						  String groupId,String timeRange,boolean isSimilar,boolean irSimflag,boolean weight,boolean server,boolean irSimflagAll,String excludeWeb) {
		this.setUserId(userId);
		this.setSubGroupId(subGroupId);
		this.specialType = specialType;
		this.specialName = specialName;
		this.allKeywords = allKeywords;
		this.anyKeywords = anyKeywords;
		this.excludeWords = excludeWords;
		this.trsl = trsl;
		this.statusTrsl = statusTrsl;
		this.weChatTrsl = weChatTrsl;
		this.searchScope = searchScope;
		this.startTime = startTime;
		this.endTime = endTime;
		this.source = source;
		this.groupName = groupName;
		this.groupId = groupId;
		this.currentTheme = false;
		this.flag = 2;
		this.timeRange=timeRange;
		this.similar = isSimilar;
		this.irSimflag = irSimflag;
		this.weight = weight;
		this.server = server;
		this.irSimflagAll = irSimflagAll;
		this.excludeWeb =excludeWeb;
		super.setCreatedTime(new Date());
		super.setLastModifiedTime(new Date());
	}
	/**
	 * 构建查询builder
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toBuilder(int pageNo, int pageSize) {
		return toSearchBuilder(pageNo, pageSize, true);
	}

	/**
	 * 构建查询builder
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toBuilder(int pageNo, int pageSize,boolean withTime) {
		return toSearchBuilder(pageNo, pageSize, withTime);
	}
	/**
	 * 构造不带分页的builder
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toNoPagedBuilder() {
		return toSearchBuilder(-1, -1, true);
	}

	/**
	 * 构造不带时间的builder
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toNoTimeBuilder(int pageNo, int pageSize) {
		return toSearchBuilder(pageNo, pageSize, false);
	}

	/**
	 * 构造不带分页和时间的builder
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toNoPagedAndTimeBuilder() {
		return toSearchBuilder(-1, -1, false);
	}

	/**
	 * 查传统媒体拼写表达式
	 * 
	 * @param pageNo
	 *            页数（从0开始）
	 * @param pageSize
	 *            一页几条
	 * @param withTime
	 *            是否带时间查询
	 * @return CreateBy xiaoying
	 */
	public QueryBuilder toSearchBuilder(int pageNo, int pageSize, boolean withTime) {
		String startTime=null;
		String endTime=null;
		if(StringUtils.isNotBlank(start)&&StringUtils.isNotBlank(end)&&(!"0".equals(start))&&(!"0".equals(end))){
			startTime = start;
			endTime=end;
		}else{
			startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.startTime);
			endTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.endTime);
		}
		 QueryBuilder queryBuilder = new QueryBuilder();
		switch (this.specialType) {
		case COMMON:
			setNewFilter();
			String keyWordindex = "1";
			if(SearchScope.TITLE_CONTENT.equals(this.searchScope)){//标题 + 正文
				keyWordindex =  "1";
			}
			if(SearchScope.TITLE.equals(this.searchScope)){//标题
				keyWordindex = "0";
			}
			if(SearchScope.TITLE_ABSTRACT.equals(this.searchScope)){//标题 + 摘要
				keyWordindex = "2";
			}
			createFilter(queryBuilder,anyKeywords,keyWordindex,excludeWords,excludeWordIndex,weight);


			String keywordIndex = "0";//仅标题
			if(SearchScope.TITLE_CONTENT.equals(this.searchScope)) {//标题 + 正文
				keywordIndex = "1";//标题+正文
			}
			if(SearchScope.TITLE_ABSTRACT.equals(this.searchScope)) {//标题 + 正文
				keywordIndex = "2";//标题+摘要
			}
			QueryBuilder builder = WordSpacingUtil.handleKeyWords(this.anyKeywords, keywordIndex, this.weight);
			queryBuilder.filterByTRSL(builder.asTRSL());
			//监测网站
			if (StringUtil.isNotEmpty(this.monitorSite)) {
				queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.monitorSite.replaceAll("[;|；]", " OR "), Operator.Equal);
			}
			//拼凑排除词
			for (String field : this.searchScope.getField()) {
				StringBuilder childBuilder = new StringBuilder();
				if (StringUtil.isNotEmpty(this.excludeWords)) {
					childBuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					queryBuilder.filterByTRSL(childBuilder.toString());
				}
			}
			//排除网站
			if (StringUtil.isNotEmpty(this.excludeWeb)) {
				queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeWeb.replaceAll(";|；", " OR "), Operator.NotEqual);
			}
			//媒体等级
			if(StringUtil.isNotEmpty(mediaLevel)){
				addFieldFilter(queryBuilder,FtsFieldConst.FIELD_MEDIA_LEVEL,mediaLevel,Const.MEDIA_LEVEL);
			}
			//媒体行业
			if(StringUtil.isNotEmpty(mediaIndustry )){
				addFieldFilter(queryBuilder,FtsFieldConst.FIELD_MEDIA_INDUSTRY,mediaIndustry,Const.MEDIA_INDUSTRY);
			}
			//内容行业
			if(StringUtil.isNotEmpty(contentIndustry )){
				addFieldFilter(queryBuilder,FtsFieldConst.FIELD_CONTENT_INDUSTRY,contentIndustry,Const.CONTENT_INDUSTRY);
			}
			//内容地域
			if(StringUtil.isNotEmpty(contentArea )){
				addAreaFilter(queryBuilder,FtsFieldConst.FIELD_CATALOG_AREA,contentArea);
			}
			//媒体地域
			if(StringUtil.isNotEmpty(mediaArea )){
				addAreaFilter(queryBuilder,FtsFieldConst.FIELD_MEDIA_AREA,mediaArea);
			}
			//信息过滤
			if(StringUtil.isNotEmpty(filterInfo )){
				String trsl = queryBuilder.asTRSL();
				StringBuilder sb = new StringBuilder(trsl);
				String[] valueArr = filterInfo.split(";");
				List<String> valueArrList = new ArrayList<>();
				for(String v : valueArr){
					if(Const.FILTER_INFO.contains(v)){
						valueArrList.add(v);
					}
				}
				if(valueArrList.size() >0){
					sb.append(" NOT (").append(FtsFieldConst.FIELD_FILTER_INFO).append(":(").append(StringUtils.join(valueArrList," OR ")).append("))");
					queryBuilder = new QueryBuilder();
					queryBuilder.filterByTRSL(sb.toString());
				}
			}
			break;
		case SPECIAL:
			queryBuilder.filterByTRSL(this.trsl);
			if(server){
				queryBuilder.setServer(true);
			}
			break;
		default:
			break;
		}
//		if(irSimflag){
//			searchBuilder.filterByTRSL(Const.IR_SIMFLAG_TRSL);
//		}
		if (pageNo != -1) {
			queryBuilder.setPageNo(pageNo);
			queryBuilder.setPageSize(pageSize);
		}

		if (withTime) {
			queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { startTime, endTime },
					Operator.Between);
		}
		return queryBuilder;
	}

	/**
	 * 构建查询builder 针对微博
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toBuilderWeiBo(int pageNo, int pageSize) {
		return toSearchBuilderWeiBo(pageNo, pageSize, true);
	}
	/**
	 * 构建查询builder 针对微博
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toBuilderWeiBo(int pageNo, int pageSize,boolean withTime) {
		return toSearchBuilderWeiBo(pageNo, pageSize, withTime);
	}
	/**
	 * 构造不带分页的builder 针对微博 CreateBy xiaoying
	 */
	public QueryBuilder toNoPagedBuilderWeiBo() {
		return toSearchBuilderWeiBo(-1, -1, true);
	}

	/**
	 * 构造不带时间的builder 针对微博
	 *
	 * CreateBy xiaoying
	 */
	public QueryBuilder toNoTimeBuilderWeiBo(int pageNo, int pageSize) {
		return toSearchBuilderWeiBo(pageNo, pageSize, false);
	}

	/**
	 * 构造不带分页和时间的builder 针对微博
	 *
	 * CreateBy xiaoying
	 */
	public QueryBuilder toNoPagedAndTimeBuilderWeiBo() {
		return toSearchBuilderWeiBo(-1, -1, false);
	}

	/**
	 * 查微博拼写表达式
	 * 
	 * @param pageNo
	 *            页数（从0开始）
	 * @param pageSize
	 *            一页几条
	 * @param withTime
	 *            是否带时间查询
	 * @return CreateBy xiaoying
	 */
	private QueryBuilder toSearchBuilderWeiBo(int pageNo, int pageSize, boolean withTime) {
		String startTime=null;
		String endTime=null;
		if(StringUtils.isNotBlank(start)&&StringUtils.isNotBlank(end)&&(!"0".equals(start))&&(!"0".equals(end))){
			startTime = start;
			endTime=end;
		}else{
			startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.startTime);
			endTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.endTime);
		}
		QueryBuilder searchBuilder = new QueryBuilder();
		
		switch (this.specialType) {
		case COMMON:
			// 微博标题检索什么 待定 摘要也是
			// 内容
			String field = FtsFieldConst.FIELD_CONTENT;
			//关键词
			searchBuilder = WordSpacingUtil.handleKeyWordsToWeiboTF(this.anyKeywords,this.weight);
			//拼接排除词
			if (StringUtil.isNotEmpty(this.excludeWords)) {
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				searchBuilder.filterByTRSL(exbuilder.toString());
			}
			//排除网站
			if (StringUtil.isNotEmpty(this.excludeWeb)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeWeb.replaceAll(";|；", " OR "), Operator.NotEqual);
			}
			break;
		case SPECIAL:
			searchBuilder.filterByTRSL(this.trsl);
			if(server){
				searchBuilder.setServer(true);
			}
			break;
		default:
			break;
		}
//		if(irSimflag){
//			searchBuilder.filterByTRSL(Const.IR_SIMFLAG_TRSL);
//		}
		if (pageNo != -1) {
			searchBuilder.setPageNo(pageNo);
			searchBuilder.setPageSize(pageSize);
		} 
		if (withTime) {
			searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { startTime, endTime }, Operator.Between);
		}
		return searchBuilder;
	}

	/**
	 * 构建查询builder 针对微信
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toBuilderWeiXin(int pageNo, int pageSize) {
		return toSearchBuilderWeiXin(pageNo, pageSize, true);
	}
	/**
	 * 构建查询builder 针对微信
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toBuilderWeiXin(int pageNo, int pageSize,boolean withTime) {
		return toSearchBuilderWeiXin(pageNo, pageSize, withTime);
	}
	/**
	 * 构造不带分页的builder 针对微信 CreateBy xiaoying
	 */
	public QueryBuilder toNoPagedBuilderWeiXin() {
		return toSearchBuilderWeiXin(-1, -1, true);
	}

	/**
	 * 构造不带时间的builder 针对微信
	 *
	 * CreateBy xiaoying
	 */
	public QueryBuilder toNoTimeBuilderWeiXin(int pageNo, int pageSize) {
		return toSearchBuilderWeiXin(pageNo, pageSize, false);
	}

	/**
	 * 构造不带分页和时间的builder 针对微信
	 *
	 * CreateBy xiaoying
	 */
	public QueryBuilder toNoPagedAndTimeBuilderWeiXin() {
		return toSearchBuilderWeiXin(-1, -1, false);
	}

	/**
	 * 查微信拼写表达式
	 * 
	 * @param pageNo
	 *            页数（从0开始）
	 * @param pageSize
	 *            一页几条
	 * @param withTime
	 *            是否带时间查询
	 * @return CreateBy xiaoying
	 */
	private QueryBuilder toSearchBuilderWeiXin(int pageNo, int pageSize, boolean withTime) {
		String startTime=null;
		String endTime=null;
		if(StringUtils.isNotBlank(start)&&StringUtils.isNotBlank(end)&&(!"0".equals(start))&&(!"0".equals(end))){
			startTime = start;
			endTime=end;
		}else{
			startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.startTime);
			endTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.endTime);
		}
		QueryBuilder searchBuilder = new QueryBuilder();
		
		switch (this.specialType) {
		case COMMON:
			// 根据关键词位置，任意关键词，排除词，拼凑表达式
			//关键词
			String keywordIndex = "0";//仅标题
			if(SearchScope.TITLE_CONTENT.equals(this.searchScope)) {//标题+正文
				keywordIndex = "1";//标题+正文
			}
			searchBuilder = WordSpacingUtil.handleKeyWords(this.anyKeywords,keywordIndex,this.weight);
			//拼凑排除词
			for (String field : this.searchScope.getField()) {
				StringBuilder childBuilder = new StringBuilder();
				if (StringUtil.isNotEmpty(this.excludeWords)) {
					childBuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					searchBuilder.filterByTRSL(childBuilder.toString());
				}
			}
			//排除网站
			if (StringUtil.isNotEmpty(this.excludeWeb)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeWeb.replaceAll(";|；", " OR "), Operator.NotEqual);
			}
			break;
		case SPECIAL:
			searchBuilder.filterByTRSL(this.trsl);
			if(server){
				searchBuilder.setServer(true);
			}
			break;
		default:
			break;
		}
		if (pageNo != -1) {
			searchBuilder.setPageNo(pageNo);
			searchBuilder.setPageSize(pageSize);
		} 
		if (withTime) {
			searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[] { startTime, endTime }, Operator.Between);
		}
		return searchBuilder;
	}

	// 地域原因 es单分出来
	public QueryBuilder esNoPagedAndTimeBuilder() {
		return toSearchBuilder(-1, -1, false);
	}
	
	
	/**
	 * 构造新实例
	 * @param subjectId
	 * @param commonUserId
	 * @return
	 */
	public SpecialProject newInstance(String subjectId, String commonUserId) {
		SpecialProject project = new SpecialProject(commonUserId, specialType, specialName, allKeywords, anyKeywords,
				excludeWords, trsl, statusTrsl, weChatTrsl, searchScope, startTime, endTime, source, groupName,
				subjectId, timeRange,similar,irSimflag,weight,server,irSimflagAll,excludeWeb);
		return project;
	}
	
	/**
	 * 构造新实例
	 * @param commonUserId
	 * @return
	 */
	public SpecialProject newInstance(String commonUserId) {
		SpecialProject project = new SpecialProject(commonUserId, specialType, specialName, allKeywords, anyKeywords,
				excludeWords, trsl, statusTrsl, weChatTrsl, searchScope, startTime, endTime, source, groupName,
				groupId, timeRange,similar,irSimflag,weight,server,irSimflagAll,excludeWeb);
		return project;
	}
	/**
	 * 构造新实例
	 * @param subjectId
	 * @param commonSubGroupId
	 * @return
	 */
	public SpecialProject newInstanceForSubGroup(String subjectId, String commonSubGroupId) {
		SpecialProject project = new SpecialProject(null,commonSubGroupId, specialType, specialName, allKeywords, anyKeywords,
				excludeWords, trsl, statusTrsl, weChatTrsl, searchScope, startTime, endTime, source, groupName,
				subjectId, timeRange,similar,irSimflag,weight,server,irSimflagAll,excludeWeb);
		return project;
	}

	/**
	 * 构造新实例
	 * @param commonSubGroupId
	 * @return
	 */
	public SpecialProject newInstanceForSubGroup(String commonSubGroupId) {
		SpecialProject project = new SpecialProject(null,commonSubGroupId, specialType, specialName, allKeywords, anyKeywords,
				excludeWords, trsl, statusTrsl, weChatTrsl, searchScope, startTime, endTime, source, groupName,
				groupId, timeRange,similar,irSimflag,weight,server,irSimflagAll,excludeWeb);
		return project;
	}
	public QueryCommonBuilder toCommonBuilder(int pageNo, int pageSize, boolean withTime) {
		String[] timeArray = new String[2];
		try {
			timeArray = DateUtil.formatTimeRangeMinus1(timeRange);
		} catch (OperationException e) {
			e.printStackTrace();
		}
		QueryCommonBuilder searchBuilder = new QueryCommonBuilder();
		
		switch (this.specialType) {
		case COMMON:
			// 根据关键词位置，任意关键词，排除词，拼凑表达式
			//关键词

//			setNewFilter();
//
//			//媒体等级
//			if(StringUtil.isNotEmpty(mediaLevel)){
//				addFieldFilter(FtsFieldConst.FIELD_MEDIA_LEVEL,mediaLevel,Const.MEDIA_LEVEL);
//			}
//			//媒体行业
//			if(StringUtil.isNotEmpty(mediaIndustry )){
//				addFieldFilter(FtsFieldConst.FIELD_MEDIA_INDUSTRY,mediaIndustry,Const.MEDIA_INDUSTRY);
//			}
//			//内容行业
//			if(StringUtil.isNotEmpty(contentIndustry )){
//				addFieldFilter(FtsFieldConst.FIELD_CONTENT_INDUSTRY,contentIndustry,Const.CONTENT_INDUSTRY);
//			}
//			//内容地域
//			if(StringUtil.isNotEmpty(contentArea )){
//				addAreaFilter(FtsFieldConst.FIELD_CATALOG_AREA,contentArea);
//			}
//			//媒体地域
//			if(StringUtil.isNotEmpty(mediaArea )){
//				addAreaFilter(FtsFieldConst.FIELD_MEDIA_AREA,mediaArea);
//			}
//			//信息过滤
//			if(StringUtil.isNotEmpty(filterInfo )){
//				String trsl = searchBuilder.asTRSL();
//				StringBuilder sb = new StringBuilder(trsl);
//				String[] valueArr = filterInfo.split(";");
//				List<String> valueArrList = new ArrayList<>();
//				for(String v : valueArr){
//					if(Const.FILTER_INFO.contains(v)){
//						valueArrList.add(v);
//					}
//				}
//				sb.append(" NOT (").append(FtsFieldConst.FIELD_FILTER_INFO).append(":(").append(StringUtils.join(valueArrList," OR ")).append("))");
//				searchBuilder = new QueryCommonBuilder();
//				searchBuilder.filterByTRSL(sb.toString());
//			}


			String keywordIndex = "0";//仅标题
			if(SearchScope.TITLE_CONTENT.equals(this.searchScope)) {//标题 + 正文
				keywordIndex = "1";//标题+正文
			}
			if(SearchScope.TITLE_ABSTRACT.equals(this.searchScope)) {//标题 + 正文
				keywordIndex = "2";//标题+摘要
			}
			QueryBuilder builder = WordSpacingUtil.handleKeyWords(this.anyKeywords, keywordIndex, this.weight);
			searchBuilder.filterByTRSL(builder.asTRSL());
			//拼凑排除词
			for (String field : this.searchScope.getField()) {
				StringBuilder childBuilder = new StringBuilder();
				if (StringUtil.isNotEmpty(this.excludeWords)) {
					childBuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					searchBuilder.filterByTRSL(childBuilder.toString());
				}
			}
			//排除网站
			if (StringUtil.isNotEmpty(this.excludeWeb)) {
				searchBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeWeb.replaceAll(";|；", " OR "), Operator.NotEqual);
			}

			break;
		case SPECIAL:
			searchBuilder.filterByTRSL(this.trsl);
			break;
		default:
			break;
		}

		if (pageNo != -1) {
			searchBuilder.setPageNo(pageNo);
			searchBuilder.setPageSize(pageSize);
		}
		if (withTime) {
			searchBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray,
					Operator.Between);
		}
		return searchBuilder;
	}
	public QueryBuilder toSpecialBuilder(int pageNo, int pageSize, boolean withTime) {
		String[] timeArray = new String[2];
		try {
			timeArray = DateUtil.formatTimeRangeMinus1(timeRange);
		} catch (OperationException e) {
			e.printStackTrace();
		}
		QueryBuilder queryBuilder = new QueryBuilder();

		switch (this.specialType) {
			case COMMON:
				// 根据关键词位置，任意关键词，排除词，拼凑表达式
				//关键词
				setNewFilter();
				String keyWordindex = "1";
			if(SearchScope.TITLE_CONTENT.equals(this.searchScope)){//标题 + 正文
				keyWordindex =  "1";
			}
			if(SearchScope.TITLE.equals(this.searchScope)){//标题
				keyWordindex = "0";
			}
			if(SearchScope.TITLE_ABSTRACT.equals(this.searchScope)){//标题 + 摘要
				keyWordindex = "2";
			}
				createFilter(queryBuilder,anyKeywords,keyWordindex,excludeWords,excludeWordIndex,weight);


				String keywordIndex = "0";//仅标题
				if(SearchScope.TITLE_CONTENT.equals(this.searchScope)) {//标题 + 正文
					keywordIndex = "1";//标题+正文
				}
				if(SearchScope.TITLE_ABSTRACT.equals(this.searchScope)) {//标题 + 正文
					keywordIndex = "2";//标题+摘要
				}
				QueryBuilder builder = WordSpacingUtil.handleKeyWords(this.anyKeywords, keywordIndex, this.weight);
				queryBuilder.filterByTRSL(builder.asTRSL());
				//拼凑排除词
				for (String field : this.searchScope.getField()) {
					StringBuilder childBuilder = new StringBuilder();
					if (StringUtil.isNotEmpty(this.excludeWords)) {
						childBuilder.append("*:* -").append(field).append(":(\"").append(this.excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						queryBuilder.filterByTRSL(childBuilder.toString());
					}
				}
				//排除网站
				if (StringUtil.isNotEmpty(this.excludeWeb)) {
					queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, this.excludeWeb.replaceAll(";|；", " OR "), Operator.NotEqual);
				}
				//媒体等级
				if(StringUtil.isNotEmpty(mediaLevel)){
					addFieldFilter(queryBuilder,FtsFieldConst.FIELD_MEDIA_LEVEL,mediaLevel,Const.MEDIA_LEVEL);
				}
				//媒体行业
				if(StringUtil.isNotEmpty(mediaIndustry )){
					addFieldFilter(queryBuilder,FtsFieldConst.FIELD_MEDIA_INDUSTRY,mediaIndustry,Const.MEDIA_INDUSTRY);
				}
				//内容行业
				if(StringUtil.isNotEmpty(contentIndustry )){
					addFieldFilter(queryBuilder,FtsFieldConst.FIELD_CONTENT_INDUSTRY,contentIndustry,Const.CONTENT_INDUSTRY);
				}
				//内容地域
				if(StringUtil.isNotEmpty(contentArea )){
					addAreaFilter(queryBuilder,FtsFieldConst.FIELD_CATALOG_AREA,contentArea);
				}
				//媒体地域
				if(StringUtil.isNotEmpty(mediaArea )){
					addAreaFilter(queryBuilder,FtsFieldConst.FIELD_MEDIA_AREA,mediaArea);
				}
				//信息过滤
				if(StringUtil.isNotEmpty(filterInfo )){
					String trsl = queryBuilder.asTRSL();
					StringBuilder sb = new StringBuilder(trsl);
					String[] valueArr = filterInfo.split(";");
					List<String> valueArrList = new ArrayList<>();
					for(String v : valueArr){
						if(Const.FILTER_INFO.contains(v)){
							valueArrList.add(v);
						}
					}
					sb.append(" NOT (").append(FtsFieldConst.FIELD_FILTER_INFO).append(":(").append(StringUtils.join(valueArrList," OR ")).append("))");
					queryBuilder = new QueryBuilder();
					queryBuilder.filterByTRSL(sb.toString());
				}
				break;
			case SPECIAL:
				queryBuilder.filterByTRSL(this.trsl);
				break;
			default:
				break;
		}

		if (pageNo != -1) {
			queryBuilder.setPageNo(pageNo);
			queryBuilder.setPageSize(pageSize);
		}
		if (withTime) {
			queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray,
					Operator.Between);
		}
		return queryBuilder;
	}
	private void addFieldFilter(QueryBuilder queryBuilder,String field,String value,List<String> allValue){
		if(StringUtil.isNotEmpty(value)){
			if(value.contains("其它")){
				value = value.replaceAll("其它","其他");
			}
			String[] valueArr = value.split(";");
			List<String> valueArrList = new ArrayList<>();
			for(String v : valueArr){
				if("其他".equals(v)|| "中性".equals(v)){
					valueArrList.add("\"\"");
				}
				if(allValue.contains(v)){
					valueArrList.add(v);
				}
			}
			queryBuilder.filterField(field,StringUtils.join(valueArrList," OR ") , Operator.Equal);
		}
	}
	private void addAreaFilter(QueryBuilder queryBuilder,String field,String areas){
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
			List<String> areaList = new ArrayList<>();
			for(String area : areaArr){
				if("其他".equals(area)){
					areaList.add("\"\"");
				}
				if(areaMap.containsKey(area)){
					areaList.add(areaMap.get(area));
				}
			}
			queryBuilder.filterField(field,StringUtils.join(areaList," OR ") , Operator.Equal);
		}
	}
	/**
	 * 添加筛选条件信息
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
//		this.read = read;
		this.mediaLevel = mediaLevel;
		this.mediaIndustry = mediaIndustry;
		this.contentIndustry = contentIndustry;
		this.filterInfo = filterInfo;
		this.contentArea = contentArea;
		this.mediaArea = mediaArea;
		this.preciseFilter = preciseFilter;
	}
	public void setNewFilter(){
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
	}

	private void createFilter(QueryBuilder queryBuilder,String keyWords, String keyWordindex, String excludeWords,String excludeWordsIndex, boolean weight) {
		if (StringUtil.isNotEmpty(keyWordindex) && (StringUtil.isNotEmpty(keyWords) || StringUtil.isNotEmpty(excludeWords))) {// 普通模式

			queryBuilder = WordSpacingUtil.handleKeyWords(keyWords, keyWordindex, this.weight);
			//拼接排除词
			if("0".equals(excludeWordsIndex)){ //标题
				if (StringUtil.isNotEmpty(excludeWords)) {
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
							.append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					queryBuilder.filterByTRSL(exbuilder.toString());
				}
			} else if ("1".equals(excludeWordsIndex)) {// 标题加正文
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
			}else if("2".equals(excludeWordsIndex)){ //标题 +摘要
				if (StringUtil.isNotEmpty(excludeWords)) {
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
							.append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					queryBuilder.filterByTRSL(exbuilder.toString());
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_ABSTRACTS).append(":(\"")
							.append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					queryBuilder.filterByTRSL(exbuilder2.toString());
				}
			}
		} else {// 专家模式
			queryBuilder.filterByTRSL(this.trsl);// 专家模式
		}

	}
}
