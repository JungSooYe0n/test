package com.trs.netInsight.widget.special.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.util.WordSpacingUtil;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
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
import java.util.*;

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
	public String getExcludeWordIndex(){
		if(StringUtil.isEmpty(this.excludeWordIndex)){
			if(SearchScope.TITLE_CONTENT.equals(this.searchScope)){//标题 + 正文
				return "1";
			}
			if(SearchScope.TITLE.equals(this.searchScope)){//标题
				return "0";
			}
			if(SearchScope.TITLE_ABSTRACT.equals(this.searchScope)){//标题 + 摘要
				return "2";
			}
			return "1";
		}
		return this.excludeWordIndex;
	}

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

	/**
	 * 上级id备份，主要是为了置顶存在，置顶元素取消置顶时回到原来分组下
	 */
	@Column(name = "bak_parent_id")
	private String bakParentId;

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
	 * 排序方式
	 */
	@Column(name = "sort")
	private String sort;
	
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
	public boolean isIrSimflag(){
		if(similar == false && irSimflagAll == false){
			return true;
		}else{
			return irSimflag;
		}
	}
    
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
	@Column(name = "precise_filter")
	private String preciseFilter;
	/**
	 * 是否有条件筛选  -  例如现在列表页中的条件筛选页面
	 * 因为在没有条件筛选的情况下，专家模式只拼接trsl，不拼接跟普通模式相同的参数
	 * 添加这个参数是防止普通模式和专家模式的参数污染
	 */
	@Transient
	private Boolean conditionScreen = false;

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
						  String groupId,String timeRange,int sequence,boolean similar,boolean irSimflag,boolean weight,boolean server,boolean irSimflagAll,String excludeWeb) {
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
		this.similar = similar;
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
    public SpecialProject(String userId,String subGroupId, SpecialType specialType, String specialName,
                          String anyKeywords, String excludeWords,String excludeWordIndex, String trsl,
                          SearchScope searchScope, Date startTime, Date endTime, String source, String groupName,
                          String groupId,String timeRange,int sequence,boolean similar,boolean irSimflag,boolean weight,boolean server,boolean irSimflagAll,String excludeWeb,
                          String imgUrl,String monitorSite,String mediaLevel,String mediaIndustry,String contentIndustry,String filterInfo,String mediaArea,String contentArea,String topFlag) {

        this.setUserId(userId);
        this.setSubGroupId(subGroupId);
        this.specialType = specialType;
        this.specialName = specialName;
        this.anyKeywords = anyKeywords;
        this.excludeWords = excludeWords;
        this.excludeWordIndex = excludeWordIndex;
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
        this.similar = similar;
        this.irSimflag = irSimflag;
        this.weight = weight;
        this.server = server;
        this.irSimflagAll = irSimflagAll;
        this.excludeWeb =excludeWeb;
        super.setCreatedTime(new Date());
        super.setLastModifiedTime(new Date());
        this.imgUrl = imgUrl;
        this.monitorSite = monitorSite;
        this.mediaLevel = mediaLevel;
        this.mediaIndustry = mediaIndustry;
        this.contentIndustry = contentIndustry;
        this.filterInfo = filterInfo;
        this.mediaArea = mediaArea;
        this.contentArea = contentArea;
		this.topFlag = topFlag;
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
			String keywordIndex = "0";//仅标题
			if(SearchScope.TITLE_CONTENT.equals(this.searchScope)) {//标题 + 正文
				keywordIndex = "1";//标题+正文
			}
			if(SearchScope.TITLE_ABSTRACT.equals(this.searchScope)) {//标题 + 正文
				keywordIndex = "2";//标题+摘要
			}
			QueryBuilder builder = WordSpacingUtil.handleKeyWords(this.anyKeywords, keywordIndex, this.weight);
			queryBuilder.filterByTRSL(builder.asTRSL());
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
		if(conditionScreen || SpecialType.COMMON.equals(this.specialType)){
			//监测网站
			if (StringUtil.isNotEmpty(this.monitorSite)) {
				String addMonitorSite = addMonitorSite(this.monitorSite);
				if(StringUtil.isNotEmpty(addMonitorSite)){
					queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME_LIKE,addMonitorSite, Operator.Equal);
				}
			}
			String excludeIndex = this.getExcludeWordIndex();
			//拼凑排除词
			String excludeWordTrsl = WordSpacingUtil.appendExcludeWords(excludeWords,excludeIndex);
			if(StringUtil.isNotEmpty(excludeWordTrsl)){
				queryBuilder.filterByTRSL(excludeWordTrsl);
			}
			//排除网站
			if (StringUtil.isNotEmpty(this.excludeWeb)) {
				String Site = addMonitorSite(this.excludeWeb);
				if(StringUtil.isNotEmpty(Site)){
					queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME_LIKE,Site, Operator.NotEqual);
				}
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
			if(StringUtil.isNotEmpty(filterInfo )&& !filterInfo.equals(Const.NOT_FILTER_INFO)){
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

			// 情感值
			if (StringUtils.isNoneBlank(emotion) && !"ALL".equals(emotion)) {
				addFieldFilter(queryBuilder,FtsFieldConst.FIELD_APPRAISE,emotion,Const.ARRAY_APPRAISE);
			}
			// 是否查看OCR_CONTENT 数据
			if (StringUtil.isNotEmpty(imgOcr) && !"ALL".equals(imgOcr)) {
				if ("img".equals(imgOcr)) { // 看有ocr的
					queryBuilder.filterByTRSL(Const.OCR_INCLUDE);
				} else if ("noimg".equals(imgOcr)) {  // 不看有ocr的
					queryBuilder.filterByTRSL(Const.OCR_NOT_INCLUDE);
				}
			}
			//	阅读标记	已读/未读
			if ("已读".equals(read)){//已读
				queryBuilder.filterField(FtsFieldConst.FIELD_READ, UserUtils.getUser().getId(),Operator.Equal);
			}else if ("未读".equals(read)){//未读
				queryBuilder.filterField(FtsFieldConst.FIELD_READ, UserUtils.getUser().getId(),Operator.NotEqual);
			}
			// 精准筛选
			if (StringUtils.isNoneBlank(preciseFilter) && !"ALL".equals(preciseFilter)) {

				List<String> sourceList = CommonListChartUtil.formatGroupName(source);
				//精准筛选 与上面论坛的主回帖和微博的原转发类似 ，都需要在数据源的基础上进行修改
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

							sourceList.remove(Const.GROUPNAME_LUNTAN);
						}

						//微博筛选  ----  微博筛选时 ，屏蔽微博原发 - 为转发、 屏蔽微博转发 - 为原发
						if (sourceList.contains(Const.GROUPNAME_WEIBO) && (preciseFilterList.contains("notWeiboForward") || preciseFilterList.contains("notWeiboPrimary")
						|| preciseFilterList.contains("notWeiboOrgAuthen") || preciseFilterList.contains("notWeiboPeopleAuthen")
						|| preciseFilterList.contains("notWeiboAuthen") || preciseFilterList.contains("notWeiboLocation")
						|| preciseFilterList.contains("notWeiboScreenName") || preciseFilterList.contains("notWeiboTopic")
						)) {

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
								buffer.append(" NOT (").append(Const.ORGANIZATION_WEIBO).append(")");
							}
							if (preciseFilterList.contains("notWeiboPeopleAuthen")) {//屏蔽微博个人认证
								buffer.append(" NOT (").append(Const.PERSON_WEIBO).append(")");
							}
							if (preciseFilterList.contains("notWeiboAuthen")) {//屏蔽微博无认证
								buffer.append(" NOT (").append(Const.NONE_WEIBO).append(")");
							}
							if (StringUtil.isNotEmpty(anyKeywords)) {
								net.sf.json.JSONArray jsonArray = net.sf.json.JSONArray.fromObject(anyKeywords.trim());
								StringBuilder childTrsl = new StringBuilder();
								StringBuilder childTrsl2 = new StringBuilder();
								for (Object keyWord : jsonArray) {

									net.sf.json.JSONObject parseObject = net.sf.json.JSONObject.fromObject(String.valueOf(keyWord));
									String keyWordsSingle = parseObject.getString("keyWords");
									if (StringUtil.isNotEmpty(keyWordsSingle)) {
										//防止关键字以多个 , （逗号）结尾，导致表达式故障问题
										String[] split = keyWordsSingle.split(",");
										String splitNode = "";
										for (int i = 0; i < split.length; i++) {
											if (StringUtil.isNotEmpty(split[i])) {
												if (split[i].endsWith(";")) {
													split[i] = split[i].substring(0, split[i].length() - 1);
												}
												splitNode += split[i] + ",";
											}
										}
										keyWordsSingle = splitNode.substring(0, splitNode.length() - 1);
										childTrsl.append("((\"")
												.append(keyWordsSingle.replaceAll("[,|，]", "*\") AND (\"").replaceAll("[;|；]+", "*\" OR \""))
												.append("*\"))");
//										childTrsl2.append("((")
//												.append(keyWordsSingle.replaceAll("[,|，]", "*) AND (").replaceAll("[;|；]+", "* OR "))
//												.append("*))");
										childTrsl2.append("((")
												.append(keyWordsSingle.replaceAll("[,|，]", ") AND (").replaceAll("[;|；]+", " OR "))
												.append("))");
									}
								}
								if (preciseFilterList.contains("notWeiboLocation")) {//屏蔽命中微博位置信息
									buffer.append(" NOT (").append(FtsFieldConst.FIELD_LOCATION).append(":(").append(childTrsl2.toString()).append(") OR ").append(FtsFieldConst.FIELD_LOCATION_LIKE).append(":(").append(childTrsl2.toString()).append("))");
								}
								if (preciseFilterList.contains("notWeiboScreenName")) {//忽略命中微博博主名
									buffer.append(" NOT (").append(FtsFieldConst.FIELD_SCREEN_NAME).append(":(").append(childTrsl2.toString()).append("))");
									buffer.append(" NOT (").append(FtsFieldConst.FIELD_RETWEETED_FROM_ALL).append(":(").append(childTrsl2.toString()).append(") OR ").append(FtsFieldConst.FIELD_RETWEETED_FROM_ALL_LIKE).append(":(").append(childTrsl2.toString()).append("))");
								}
								if (preciseFilterList.contains("notWeiboTopic")) {//屏蔽命中微博话题信息
									buffer.append(" NOT (").append(FtsFieldConst.FIELD_TAG).append(":(").append(childTrsl2.toString()).append(") OR ").append(FtsFieldConst.FIELD_TAG_LIKE).append(":(").append(childTrsl2.toString()).append("))");
								}
							}
							buffer.append(")");

							sourceList.remove(Const.GROUPNAME_WEIBO);
						}
						if (buffer.length() > 0) {
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
			}
		}
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
	public SpecialProject copyForSubGroup(String commonSubGroupId) {
		SpecialProject project = new SpecialProject( null, commonSubGroupId, specialType,  specialName,
				 anyKeywords,  excludeWords,excludeWordIndex,  trsl,
				 searchScope,  startTime,  endTime,  source,  groupName,
				 null, timeRange, sequence, similar, irSimflag, weight, server, irSimflagAll, excludeWeb,  imgUrl,
				monitorSite, mediaLevel, mediaIndustry, contentIndustry, filterInfo, mediaArea, contentArea,topFlag);

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
	private void addFieldFilter(QueryBuilder queryBuilder,String field,String value,List<String> allValue){
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
			Set<String> areaList = new HashSet<>();
			for(String area : areaArr){
				if("其他".equals(area)){
					areaList.add("\"\"");
				}
				if(areaMap.containsKey(area)){
					areaList.add(areaMap.get(area));
				}
			}
			//如果list中有其他，则其他为 其他+“”。依然是算两个
			if(areaList.size() >0  &&  areaList.size() < areaMap.size() +1){
				if(FtsFieldConst.FIELD_CATALOG_AREA.equals(field) && this.source.contains(Const.TYPE_WEIXIN)){
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(FtsFieldConst.FIELD_CATALOG_AREA).append(":(").append(StringUtils.join(areaList," OR ")).append(")").append(" OR ").append(FtsFieldConst.FIELD_CATALOG_AREA_MULTIPLE).append(":(").append(StringUtils.join(areaList," OR ")).append(")");
					queryBuilder.filterByTRSL(stringBuilder.toString());
				}else {
					queryBuilder.filterField(field, StringUtils.join(areaList, " OR "), Operator.Equal);
				}
			}
		}
	}
	/**
	 * 添加筛选条件信息
	 * @param mediaLevel  媒体等级
	 * @param mediaIndustry  媒体行业
	 * @param contentIndustry  内容行业
	 * @param filterInfo  过滤信息
	 * @param contentArea  内容行业
	 * @param mediaArea  媒体行业
	 */
	public void addFilterCondition(String mediaLevel,String mediaIndustry,String contentIndustry,String filterInfo,
								   String contentArea,String mediaArea){
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

	@Transient
	private String read;//  阅读标记
//	@Transient
//	private String preciseFilter;//精准筛选
	@Transient
	private String emotion;//  阅读标记
	@Transient
	private String imgOcr;//  阅读标记

	/**
	 * 添加筛选条件信息
	 * @param read  媒体等级
	 * @param preciseFilter  媒体行业
	 * @param emotion  内容行业
	 */
	public void addFilterCondition(String read,String preciseFilter,String emotion,String imgOcr){
		this.read = read;
		this.preciseFilter = preciseFilter;
		this.emotion = emotion;
		this.imgOcr = imgOcr;

	}

	public void formatSpecialProject(String simflag,String wordIndex,String excludeWeb,String monitorSite,
									  String excludeWord, String excludeWordIndex,Boolean updateWordForm,Integer wordFromNum,Boolean wordFromSort,String mediaLevel,
									  String groupName,String mediaIndustry,String contentIndustry,String filterInfo,String contentArea,String mediaArea){
		//暂时修改专题分析栏目的属性值，这些条件来自页面上的条件筛选，只是这次作为筛选规则
		this.conditionScreen =true;

		this.similar = false;
		this.irSimflag = true;
		this.irSimflagAll = false;
		//排重
		if ("netRemove".equals(simflag)) { //单一媒体排重
			this.similar = true;
			this.irSimflag = false;
			this.irSimflagAll = false;
		} else if ("urlRemove".equals(simflag)) { //站内排重
			this.similar = false;
			this.irSimflag = true;
			this.irSimflagAll = false;
		} else if ("sourceRemove".equals(simflag)) { //全网排重
			this.similar = false;
			this.irSimflag = false;
			this.irSimflagAll = true;
		}
		//命中规则
		if (StringUtil.isNotEmpty(wordIndex) && StringUtil.isEmpty(this.trsl)) {
			if (SearchScope.TITLE.equals(wordIndex) || "0".equals(wordIndex)){
				this.searchScope = SearchScope.TITLE;
			}
			if (SearchScope.TITLE_CONTENT.equals(wordIndex)|| "1".equals(wordIndex)){
				this.searchScope = SearchScope.TITLE_CONTENT;
			}
			if (SearchScope.TITLE_ABSTRACT.equals(wordIndex)|| "2".equals(wordIndex)){
				this.searchScope = SearchScope.TITLE_ABSTRACT;
			}

		}
		this.monitorSite = monitorSite;
		this.excludeWeb = excludeWeb;
		//排除关键词
		this.excludeWordIndex = excludeWordIndex;
		this.excludeWords = excludeWord;

		//修改词距 选择修改词距时，才能修改词距
		if (updateWordForm != null && updateWordForm && StringUtil.isEmpty(this.trsl) && wordFromNum >= 0) {
			JSONArray jsonArray = JSONArray.parseArray(this.anyKeywords);
			//现在词距修改情况为：只有一个关键词组时，可以修改词距等，多个时不允许
			if (jsonArray != null && jsonArray.size() == 1) {
				Object o = jsonArray.get(0);
				JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
				jsonObject.put("wordSpace", wordFromNum);
				jsonObject.put("wordOrder", wordFromSort);
				jsonArray.set(0, jsonObject);
				this.anyKeywords = jsonArray.toJSONString();
			}
		}
		if(StringUtil.isNotEmpty(groupName)){
			this.source = groupName;
		}
		this.addFilterCondition(mediaLevel, mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea);
	}



}
