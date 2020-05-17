package com.trs.netInsight.support.fts.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@FtsClient
public class FtsDocumentChaos extends IDocument implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * 1.记录标识，海贝自动生成
     */
    @ApiModelProperty(notes = "文章ID")
    @JsonView(BaseEntity.DisplayView.class)
    private String sid;

    /**
     * 2.文章标题
     */
    @ApiModelProperty(notes = "文章标题")
    @JsonView(BaseEntity.DisplayView.class)
    private String urlTitle;

    /**
     * 3.urlTime
     */
    @ApiModelProperty(notes = "urlTime")
    @JsonView(BaseEntity.DisplayView.class)
    private Date urlTime;
    /**
     * 4.文章来源
     */
    @ApiModelProperty(notes = "来源")
    @JsonView(BaseEntity.DisplayView.class)
    private String groupName;

    /**
     * 5.文章来源网址
     */
    @ApiModelProperty(notes = "来源网址")
    @JsonView(BaseEntity.DisplayView.class)
    private String urlName;

    /**
     * 6.入库时间，海贝设置缺省值
     */
    @ApiModelProperty(notes = "入库时间")
    @JsonView(BaseEntity.DisplayView.class)
    private Date loadTime;

    /**
     *7.md5Tag值
     */
    @ApiModelProperty(notes = "入库时间")
    @JsonView(BaseEntity.DisplayView.class)
    private String md5Tag;
    /**
     * 8. isNew
     */
    private boolean isNew;

    /**
     * 9. timeAgo
     */
    @ApiModelProperty(notes = "距离URLtime的时间差")
    @JsonView(BaseEntity.DisplayView.class)
    private String timeAgo;

    /**
     * 10.情感值
     */
    @ApiModelProperty(notes = "情感值")
    @JsonView(BaseEntity.DisplayView.class)
    private String appraise;

    /**
     *11. 微博用户昵称
     */
    @ApiModelProperty(notes = "用户昵称")
    @JsonView(BaseEntity.DisplayView.class)
    private String screenName;

    /**
     *12. 微博转发数
     */
    @ApiModelProperty(notes = "转发数")
    @JsonView(BaseEntity.DisplayView.class)
    private long rttCount;

    /**
     * 13.微博评论数
     */
    @ApiModelProperty(notes = "评论数")
    @JsonView(BaseEntity.DisplayView.class)
    private long commtCount;

    /**
     * 14.微博hkey
     */
    @ApiModelProperty(notes = "hkey")
    @JsonView(BaseEntity.DisplayView.class)
    private String hkey;

    /**
     * 15.微信mid
     */
    @ApiModelProperty(notes = "mid")
    @JsonView(BaseEntity.DisplayView.class)
    private String mid;

    /**
     * 16.传统站点名
     */
    @ApiModelProperty(notes = "传统站点名")
    @JsonView(BaseEntity.DisplayView.class)
    private String siteName;
    /**
     * 17.微信authors
     */
    @ApiModelProperty(notes = "微信authors")
    @JsonView(BaseEntity.DisplayView.class)
    private String authors;
    /**
     * 18.混合列表小标题shortWords
     */
    @ApiModelProperty(notes = "混合列表小标题")
    @JsonView(BaseEntity.DisplayView.class)
    private String shortWords;

    /**
     * 19.论坛  0：主贴  1：回帖
     */
    @ApiModelProperty(notes = "论坛  0：主贴  1：回帖")
    @JsonView(BaseEntity.DisplayView.class)
    private String nreserved1;
    
    /**
	 * 收藏Boolean型(true 表示已收藏)
	 */
	private Boolean favourites;
    
}