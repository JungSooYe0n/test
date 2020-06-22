package com.trs.netInsight.widget.special;


import com.fasterxml.jackson.annotation.JsonView;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 专题分析自定义图表实体类
 * 一个栏目可以对应多个自定义图表
 *
 * @author 张娅
 */
@Entity(name = "special_custom_chart")
@Table
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialCustomChart extends BaseEntity{
    @Column(name = "name")
    private String name;// 三级栏目（图）名

    /**
     * 专项类型
     */
    @Column(name = "special_type")
    @ApiModelProperty(notes = "专项类型")
    private SpecialType specialType;

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

    public SpecialCustomChart(String name, String trsl, String xyTrsl, String type, String contrast, String excludeWeb,String monitorSite, String timeRange, String keyWord, String excludeWords,
                       String keyWordIndex, String groupName, Boolean similar, Boolean irSimflag, Boolean irSimflagAll, Boolean weight, Integer tabWidth, String parentId, Integer sequence,SpecialType specialType) {
        this.name = name;
        this.trsl = trsl;
        this.xyTrsl = xyTrsl;
        this.type = type;
        this.contrast = contrast;
        this.excludeWeb = excludeWeb;
        this.monitorSite = monitorSite;
        this.timeRange = timeRange;
        this.keyWord = keyWord;
        this.excludeWords = excludeWords;
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
    public SpecialCustomChart(String name, String trsl, String xyTrsl, String type, String contrast, String excludeWeb, String monitorSite,String timeRange, String keyWord, String excludeWords,
                              String keyWordIndex, String groupName, Boolean similar, Boolean irSimflag, Boolean irSimflagAll, Boolean weight, Integer tabWidth, String parentId, Integer sequence,SpecialType specialType,String mediaLevel,String mediaIndustry,String contentIndustry,
                              String filterInfo, String contentArea,String mediaArea) {
        this(name, trsl, xyTrsl, type, contrast, excludeWeb,monitorSite,timeRange, keyWord, excludeWords, keyWordIndex, groupName, similar, irSimflag, irSimflagAll, weight, tabWidth, parentId, sequence, specialType);
        this.mediaLevel = mediaLevel;
        this.mediaIndustry = mediaIndustry;
        this.contentIndustry = contentIndustry;
        this.filterInfo = filterInfo;
        this.contentArea = contentArea;
        this.mediaArea = mediaArea;
    }
}
