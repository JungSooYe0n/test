package com.trs.netInsight.widget.special.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.trs.netInsight.config.serualizer.DateDeserializer;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 舆情报告 极简模式 自定义专题 实体
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/10.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "custom_special_project")
public class CustomSpecial extends BaseEntity {
    private static final long serialVersionUID = 4878430839630389942L;

    /**
     * 专题类型  普通  |  专家 模式
     */
    @Column(name = "special_type")
    @JsonView(SaveView.class)
    @ApiModelProperty(notes = "专题项类型")
    private SpecialType specialType;

    /**
     * 专题名
     */
    @JsonView(SaveView.class)
    @Column(name = "special_name")
    @ApiModelProperty(notes = "专题名")
    private String specialName;

    /**
     * 任意关键词
     */
    @JsonView(SaveView.class)
    @Column(name = "any_keywords", columnDefinition = "TEXT")
    private String anyKeyWords;

    /**
     * 排除词
     */
    @JsonView(SaveView.class)
    @Column(name = "exclude_words", columnDefinition = "TEXT")
    private String excludeWords;

    /**
     * 排除网站
     */

    @JsonView(SaveView.class)
    @Column(name = "excludeWeb", columnDefinition = "TEXT")
    private String excludeWeb;

    /**
     * 专家检索表达式
     */
    @JsonView(SaveView.class)
    @Column(name = "trsl", columnDefinition = "TEXT")
    private String trsl;

    /**
     * 搜索位置
     */
    @JsonView(SaveView.class)
    @Column(name = "search_scope")
    private SearchScope searchScope;

    /**
     * 检索开始时间
     */
/*    @JsonView(SaveView.class)
    @Column(name = "start_time")
    @JsonDeserialize(using = DateDeserializer.class)
    @ApiModelProperty(value = "开始时间", example = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;*/

    /**
     * 检索结束时间
     */
    /*@JsonView(SaveView.class)
    @Column(name = "end_time")
    @JsonDeserialize(using = DateDeserializer.class)
    @ApiModelProperty(value = "结束时间", example = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;*/

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
     * 按urlname排重  0排 1不排  true排 false不排  url排重
     */
    @Column(name = "ir_simflag")
    private boolean irSimflag = false;

    /**
     * 按urlname排重  0排 1不排  true排 false不排  url排重
     */
    @Column(name = "ir_simflag_all")
    private boolean irSimflagAll = false;


    /**
     * 是否排重  netRemove 全网排重
     */
    @Column(name = "similar")
    private boolean similar;

    /**
     * 是否按照权重查找 排序规则 优先命中标题true
     */
    @Column(name = "weight")
    private boolean weight = false;


    public CustomSpecial(String userId,SpecialType specialType, String specialName, String anyKeyWords, String excludeWords, String excludeWeb,String trsl, SearchScope searchScope, String timeRange, String source, boolean irSimflag, boolean similar,boolean irSimflagAll, boolean weight) {
        this.setUserId(userId);
        this.specialType = specialType;
        this.specialName = specialName;
        this.anyKeyWords = anyKeyWords;
        this.excludeWords = excludeWords;
        this.excludeWeb = excludeWeb;
        this.trsl = trsl;
        this.searchScope = searchScope;
        this.timeRange = timeRange;
        this.source = source;
        this.irSimflag = irSimflag;
        this.similar = similar;
        this.irSimflagAll = irSimflagAll;
        this.weight = weight;
    }
}
