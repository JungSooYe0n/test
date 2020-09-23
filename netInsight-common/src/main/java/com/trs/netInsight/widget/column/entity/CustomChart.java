package com.trs.netInsight.widget.column.entity;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;

/**
 * 日常监测自定义图表实体类
 * 一个栏目可以对应多个自定义图表
 *
 * @author 张娅
 */
@Entity(name = "custom_chart")
@Table
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomChart extends BaseEntity {


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

    @Column(name = "type")
    private String type;// 图表类型

    /**
     * 分类对比类型
     */
    @Column(name = "contrast")
    private String contrast;

    @Column(name = "excludeWeb")
    private String excludeWeb;// 排除网站

    @Column(name = "monitor_site")
    private String monitorSite;// 监测网站

    @Column(name = "time_range")
    private String timeRange;// 发布时间范围

    @Column(name = "hide")
    private boolean hide = false;

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
     * 检索关键词位置 0：标题 1：标题+正文
     */
    @Column(name = "key_word_index", columnDefinition = "TEXT")
    private String keyWordIndex;

    /**
     * 保存栏目类型,多值,中间以';'分隔
     */
    @Column(name = "group_name", columnDefinition = "TEXT")
    private String groupName;

    /**
     * 是否排重
     */
    @Column(name = "similar")
    private boolean similar = false;

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
     * 50为半栏 100为通栏
     */
    @Column(name = "tab_width")
    private int tabWidth = 50;
    public String getTabWidth(){
        return String.valueOf(this.tabWidth);
    }


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

    /**
     * 对应的日常监测的栏目id
     */
    @Column(name = "parent_id")
    private String parentId;
    /**
     * 顺序 - 统计分析暂不可改变顺序
     */
    @Column(name = "sequence")
    private Integer sequence;
    /**
     * 是否置顶 也就是缩略豆腐块时，是否展示
     */
    @Column(name = "is_top")
    private Boolean isTop = false;
    /**
     * 置顶时顺序
     */
    @Column(name = "top_sequence")
    private Integer topSequence;


    public IndexTab indexTab() {
        IndexTab indexTab = new IndexTab(this.name, this.trsl, this.xyTrsl, this.type, this.contrast, this.excludeWeb, this.monitorSite,this.timeRange,
                this.hide, this.keyWord, this.excludeWords, this.excludeWordIndex, this.keyWordIndex, this.groupName, this.similar, this.irSimflag, this.irSimflagAll,
                this.weight, this.tabWidth, this.sequence, this.specialType, this.mediaLevel, this.mediaIndustry, this.contentIndustry, this.filterInfo, this.contentArea, this.mediaArea);
        return indexTab;
    }


    public CustomChart(String name, String trsl, String xyTrsl, String type, String contrast, String excludeWeb,String monitorSite, String timeRange, Boolean hide, String keyWord, String excludeWords,String excludeWordIndex,
                       String keyWordIndex, String groupName, Boolean similar, Boolean irSimflag, Boolean irSimflagAll, Boolean weight, Integer tabWidth, String parentId, Integer sequence,SpecialType specialType) {
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
        this.parentId = parentId;
        this.specialType = specialType;
    }
    public CustomChart(String name, String trsl, String xyTrsl, String type, String contrast, String excludeWeb,String monitorSite, String timeRange, Boolean hide, String keyWord,
                       String excludeWords,String excludeWordIndex, String keyWordIndex, String groupName, Boolean similar, Boolean irSimflag, Boolean irSimflagAll,
                       Boolean weight, Integer tabWidth, String parentId, Integer sequence,SpecialType specialType,String mediaLevel,String mediaIndustry,String contentIndustry,
                       String filterInfo, String contentArea,String mediaArea) {
        this(name, trsl, xyTrsl, type, contrast, excludeWeb,monitorSite, timeRange, hide, keyWord, excludeWords, excludeWordIndex, keyWordIndex, groupName, similar, irSimflag, irSimflagAll, weight, tabWidth, parentId, sequence, specialType);
        this.mediaLevel = mediaLevel;
        this.mediaIndustry = mediaIndustry;
        this.contentIndustry = contentIndustry;
        this.filterInfo = filterInfo;
        this.contentArea = contentArea;
        this.mediaArea = mediaArea;
    }

}
