
package com.trs.netInsight.widget.column.entity;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;

/**
 * 三级栏目（图）实体类
 * 
 * @author xiaoying
 *
 */
@Table(name = "index_tab")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndexTab extends BaseEntity implements Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5624243370102578700L;

	@Column(name = "name")
	private String name;// 三级栏目（图）名
	/**
	 * 专项类型
	 */
	@Column(name = "special_type")
	@ApiModelProperty(notes = "专项类型")
	@Enumerated(EnumType.ORDINAL)
	private SpecialType specialType;
	public SpecialType getSpecialType(){
		if(this.specialType == null) {
			if (StringUtil.isNotEmpty(this.getXyTrsl()) || StringUtil.isNotEmpty(this.getTrsl())) {
				this.specialType = SpecialType.SPECIAL;
			} else {
				this.specialType = SpecialType.COMMON;
			}
		}
		return this.specialType;
	}

	@Column(name = "trsl", columnDefinition = "TEXT")
	private String trsl;// trs表达式

	@Column(name = "xy_trsl", columnDefinition = "TEXT")
	private String xyTrsl;// x轴y轴 表达式

	// private ColumnType type;//图表类型
	@Column(name = "type")
	private String type;// 图表类型

	/**
	 * 分类对比类型
	 */
	@Column(name = "contrast")
	private String contrast;

	@Column(name = "tradition")
	private String tradition;// 传统来源细分

	@Column(name = "excludeWeb")
	private String excludeWeb;// 排除网站

	@Column(name = "monitor_site")
	private String monitorSite;// 监测网站

	/**
	 * 栏目分组id
	 */
	@Column(name = "parent_id")
	private String parentId;

	/**
	 * 自定义类型导航栏的id  如果为默认，则为“”
	 */
	@Column(name = "type_id")
	private String typeId;

	@Column(name = "sequence")
	private Integer sequence;

	@Column(name = "max_size")
	private int maxSize;// 最大条数

	@Column(name = "time_range")
	private String timeRange;// 发布时间范围

	@Column(name = "hide")
	private boolean hide;

	@Column(name = "status_trsl", columnDefinition = "TEXT")
	private String statusTrsl;// 微博检索表达式

	@Column(name = "we_chat_trsl", columnDefinition = "TEXT")
	private String weChatTrsl;// 微信检索表达式

	/**
	 * 检索关键词
	 */
	@Column(name = "key_word", columnDefinition = "TEXT")
	private String keyWord;
	
	/**
	 * 排除词
	 */
	@Column(name = "exclude_words", columnDefinition = "TEXT")
	private String excludeWords;
	/**
	 * 排除词检索位置 0：标题 1：标题+正文  2：标题+摘要
	 */
	@Column(name = "exclude_word_index", columnDefinition = "TEXT")
	private String excludeWordIndex;
	public String getExcludeWordIndex(){
		if(StringUtil.isEmpty(this.excludeWordIndex)){
			return this.keyWordIndex;
		}
		return this.excludeWordIndex;
	}

	/**
	 * 检索关键词位置 0：标题 1：标题+正文  2：标题+摘要
	 */
	@Column(name = "key_word_index", columnDefinition = "TEXT")
	private String keyWordIndex;

	/**
	 * xy轴检索关键词 exp:北京=xx;南京=xx;
	 */
	@Column(name = "xy_key_word", columnDefinition = "TEXT")
	private String xyKeyWord;

	/**
	 * xy轴检索关键词位置
	 */
	@Column(name = "xy_key_kord_index", columnDefinition = "TEXT")
	private String xyKeyWordIndex;

	/**
	 * 保存栏目类型,多值,中间以';'分隔
	 */
	@Column(name = "group_name", columnDefinition = "TEXT")
	private String groupName;
	/**
	 * 是否排重
	 */
	@Column(name = "similar")
	private boolean similar;
	
	/**
     * 按urlname排重  0排 1不排  true排 false不排
     */
    @Column(name = "ir_simflag")
    private boolean irSimflag = false;
//    public boolean isIrSimflag(){
//    	if(similar == false && irSimflagAll == false){
//			return true;
//		}else{
//			return irSimflag;
//		}
//	}

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
	 * 50为半栏 100为通栏
	 */
	@Column(name = "tab_width")
	private int tabWidth = 50;
	
	public String getTabWidth(){
		return String.valueOf(this.tabWidth);
	}
	
	/**
	 * 一级栏目名
	 */
	@Transient
	private String oneName;

	/**
	 * 此字段专 为 专报、极简报告而设
	 */
	@Transient
	private String notSids;


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

	public String[]  getType(Boolean isArr){
		if (StringUtil.isNotEmpty(this.type)){
			return this.type.trim().split(";");
		}
		return null;
	}


	public String[]  getExcludeWeb(Boolean Arr){
		if (StringUtil.isNotEmpty(this.excludeWeb)){
			return excludeWeb.trim().split(";|；");
		}
		return null;
	}

	public String  getExcludeWeb(){
		return excludeWeb;
	}


	public IndexTab(String name, String trsl, String keyWord, String excludeWords, String keyWordIndex, String xyTrsl, String type, String parentId, String groupName,
                    int sequence, int maxSize, String timeRange, boolean similar, boolean irSimflag, boolean weight, boolean irSimflagAll) {
		this.name = name;
		this.trsl = trsl;
		this.keyWord = keyWord;
		this.excludeWords = excludeWords;
		this.keyWordIndex = keyWordIndex;
		this.xyTrsl = xyTrsl;
		this.type = type;
		this.parentId = parentId;
		this.groupName = groupName;
		this.sequence = sequence;
		this.maxSize = maxSize;
		this.timeRange = timeRange;
		this.similar = similar;
		this.irSimflag = irSimflag;
		// 默认不隐藏
		this.hide = false;
		this.weight = weight;
		this.irSimflagAll = irSimflagAll;
	}
	
	public IndexTab(String name, String trsl, String keyWord, String excludeWords, String keyWordIndex, String xyTrsl, String type, String groupName,
                    int sequence, String timeRange, boolean similar, boolean irSimflag, boolean weight, boolean irSimflagAll) {
		this.name = name;
		this.trsl = trsl;
		this.keyWord = keyWord;
		this.excludeWords = excludeWords;
		this.keyWordIndex = keyWordIndex;
		this.xyTrsl = xyTrsl;
		this.type = type;
		this.groupName = groupName;
		this.sequence = sequence;
		this.timeRange = timeRange;
		this.similar = similar;
		this.irSimflag = irSimflag;
		// 默认不隐藏
		this.hide = false;
		this.weight = weight;
		this.irSimflagAll = irSimflagAll;
	}

	public IndexTab(String name, String trsl, String xyTrsl, String type, String contrast, String excludeWeb,String monitorSite, String timeRange, boolean hide, String keyWord,
					String excludeWords, String excludeWordIndex,String keyWordIndex, String groupName, boolean similar, boolean irSimflag, boolean irSimflagAll, boolean weight,
					int tabWidth, Integer sequence,SpecialType specialType,String mediaLevel,String mediaIndustry,String contentIndustry,String filterInfo,
					String contentArea,String mediaArea) {
		this.name = name;
		this.trsl = trsl;
		this.xyTrsl = xyTrsl;
		this.type = type;
		this.contrast = contrast;
		this.excludeWeb = excludeWeb;
		this.monitorSite= monitorSite;
		this.timeRange = timeRange;
		this.hide = hide;
		this.keyWord = keyWord;
		this.excludeWords = excludeWords;
		this.excludeWordIndex = excludeWordIndex;
		this.keyWordIndex = keyWordIndex;
		this.groupName = groupName;
		this.similar = similar;
		this.irSimflag = irSimflag;
		this.irSimflagAll = irSimflagAll;
		this.weight = weight;
		this.tabWidth = tabWidth;
		this.sequence = sequence;
		this.specialType = specialType;
		this.mediaLevel = mediaLevel;
		this.mediaIndustry = mediaIndustry;
		this.contentIndustry = contentIndustry;
		this.filterInfo = filterInfo;
		this.contentArea = contentArea;
		this.mediaArea = mediaArea;
	}


	/**
	 * 复制tab，谨慎使用，会更新持久态对象
	 * @date Created at 2018年3月22日  上午11:04:55
	 * @Author 谷泽昊
	 * @return
	 */
	@Override  
    public IndexTab clone() {
		IndexTab indexTab = null;
        try{  
        	indexTab = (IndexTab)super.clone();
        }catch(CloneNotSupportedException e) {  
            e.printStackTrace();  
        }  
        return indexTab;  
    }  
	
	/**parentId  typeId  hide
	 * 复制栏目
	 * @since changjiang @ 2018年9月13日
	 * @return
	 * @Return : IndexTab
	 */
	public IndexTab tabCopy(){
		IndexTab indexTab=new IndexTab(name,specialType, trsl, xyTrsl, type,contrast, tradition, excludeWeb,monitorSite, parentId,typeId, sequence,
				maxSize, timeRange, hide, statusTrsl, weChatTrsl, keyWord,
				excludeWords,excludeWordIndex, keyWordIndex, xyKeyWord, xyKeyWordIndex, groupName,similar,irSimflag,irSimflagAll,weight,sort,tabWidth,oneName,notSids,
				mediaLevel, mediaIndustry, contentIndustry, filterInfo, preciseFilter, contentArea, mediaArea);
		return indexTab;
	}
	
	/**
	 * 映射关联
	 * @since changjiang @ 2018年9月18日
	 * @return
	 * @Return : IndexTabMapper
	 */
	public IndexTabMapper mapper(){
		IndexTabMapper mapper = new IndexTabMapper();
		mapper.setHide(this.hide);
		mapper.setIndexTab(this);
		mapper.setMe(true);
		mapper.setSequence(this.sequence);
		mapper.setTabWidth(this.tabWidth);
		return mapper;
	}
	
	/**
	 * 映射关联
	 * @since changjiang @ 2018年10月9日
	 * @param share
	 * @return
	 * @Return : IndexTabMapper
	 */
	public IndexTabMapper mapper(boolean share){
		IndexTabMapper mapper = new IndexTabMapper();
		mapper.setHide(this.hide);
		mapper.setUserId(super.getUserId());
		mapper.setIndexTab(this);
		mapper.setMe(true);
		mapper.setSequence(this.sequence);
		mapper.setTabWidth(this.tabWidth);
		mapper.setShare(share);
		return mapper;
	}
	
	/**
	 * 持久态对象转为瞬时状态
	 * @since changjiang @ 2018年10月15日
	 * @return
	 * @Return : IndexTab
	 */
	public void clear(){
		super.setId(null);
		super.setCreatedTime(null);
		super.setCreatedUserId(null);
		super.setUserAccount(null);
		super.setUserId(null);
	}

}
