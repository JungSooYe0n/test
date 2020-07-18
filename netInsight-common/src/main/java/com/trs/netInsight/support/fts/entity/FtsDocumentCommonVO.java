/*
 * Project: netInsight
 *
 * File Created at 2018年3月23日
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.alert.entity.enums.Store;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author changjiang
 * @Desc 混合列表通用查询对象
 * @date 2018年3月23日 上午10:11:30
 */
@Getter
@Setter
@FtsClient(hybaseType = FtsHybaseType.MIX)
public class FtsDocumentCommonVO extends IDocument implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 唯一标识
     */
    @FtsField("IR_SID")
    private String sid;

    /**
     * 同sid
     */
    @FtsField("IR_MID")
    private String mid;
    /**
     * 微信hkey
     */
    @FtsField("IR_HKEY")
    private String hkey;

    /**
     * 入库时间
     */
    @FtsField("IR_LOADTIME")
    private Date loadTime;

    /**
     * 作者
     */
    @FtsField("IR_AUTHORS")
    private String authors;

    /**
     * 链接
     */
    @FtsField("IR_URLNAME")
    private String urlName;

    /**
     * 标题
     */
    @FtsField(value = "IR_URLTITLE", highLight = true)
    private String title;

    public String getUrlTitle() {
        if(title == null){
            return urlTitle;
        }
        return title;
    }

    /**
     * 正文
     */
    @FtsField(value = "IR_CONTENT", highLight = true)
    private String content;

    /**
     * ocrContent
     */
    @FtsField(value = "IR_OCR_CONTENT", highLight = true)
    private String ocrContent;

    private Boolean imgOcr = false;
    public Boolean getImgOcr(){
        if(StringUtil.isNotEmpty(this.ocrContent)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 不做截取处理（解决混合情况导出内容不完整）
     * 为混合列表导出正文使用
     */
    @FtsField(value = "IR_CONTENT", highLight = true)
    private String exportContent;

    /**
     * 微博用户昵称
     */
    @FtsField("IR_SCREEN_NAME")
    private String screenName;

    @FtsField("IR_URLTIME_YEAR")
    private String urlYear;

    @FtsField("IR_URLTIME_MONTH")
    private String urlMonth;

    @FtsField("IR_URLTIME_HOUR")
    private String urlHour;

    /**
     * 原文时间-日期
     */
    @FtsField("IR_URLDATE")
    private Date urlDate;

    /**
     * 原文时间-时间
     */
    @FtsField("IR_URLTIME")
    private Date urlTime;

    @FtsField("IR_GROUPNAME")
    private String groupName;

    /**
     * 相似度
     */
    @FtsField("MD5TAG")
    private String md5Tag;

    /**
     * 区域
     */
    @FtsField("CATALOG_AREA")
    private String catalogArea;

    /**
     * 正负面
     */
    @FtsField("IR_APPRAISE")
    private String appraise;

    /**
     * 排重标
     */
    @FtsField("SIMFLAG")
    private String simflag;

    /**
     * 站点
     */
    @FtsField("IR_SITENAME")
    private String siteName;

    /**
     * 原发媒体
     */
    @FtsField("IR_SRCNAME")
    private String srcName;
    /**
     * 摘要
     */
    @FtsField(value = "IR_ABSTRACT", highLight = true)
    private String abstracts;

    /**
     * 区分论坛 主贴/回帖
     */
    @FtsField("IR_NRESERVED1")
    private String nreserved1;
    /**
     * 判断微博转发 / 原发
     */
    @FtsField("IR_RETWEETED_MID")
    private String retweetedMid;
    /**
     * 评论数
     */
    @FtsField("IR_COMMTCOUNT")
    private long commtCount;
    /**
     * 转发数
     */
    @FtsField("IR_RTTCOUNT")
    private long rttCount;
    /**
     * 新闻信息服务资质
     */
    @FtsField("IR_WXB_GRADE")
    private String wxbGrade;
    /**
     * 新闻可供转载网站/门户类型
     */
    @FtsField("IR_SITE_APTUTIDE")
    private String siteAptutide;
    /**
     * 网站类型
     */
    @FtsField("IR_SITE_PROPERTY")
    private String siteProperty;

    /**
     * 媒体行业/频道
     */
    @FtsField("IR_CHANNEL_INDUSTRY")
    private String channelIndustry;


    /**
     * 是否发送过预警
     */
    private boolean send;
    /**
     * 是否已收藏
     */
    private boolean favourite;
    /**
     * hybase表达式缓存key
     */
    private String trslk;


    /**
     * 地址层级
     */
    @FtsField("IR_URLLEVEL")
    private int urlLevel;

    /**
     * 关键词
     */
    @FtsField("IR_KEYWORDS")
    private List<String> keywords;


    /**
     * 图片标记
     */
    @FtsField("IR_IMAGEFLAG")
    private int imageFlag;


    /**
     * 频道
     */
    @FtsField(value = "IR_CHANNEL", highLight = true)
    private String channel;


    /**
     * 论坛 楼层
     */
    @FtsField("IR_BBSNUM")
    private int bbsNum;


    /**
     * 舆情分类==敏感分类
     */
    @FtsField("IR_CATALOG2")
    private String sensitiveType;

    /**
     * wxb白名单
     */
    @FtsField("IR_WXB_LIST")
    private String wxbList;


    /**
     * 数据入hybase库的时间
     */
    @FtsField("HYBASE_LOADTIME")
    private Date HyLoadTime;
    /**
     * 用户id
     */
    @FtsField("IR_UID")
    private String uid;
    /**
     * 相似文章数
     */
    private long simCount;
    /**
     * 相似文章数
     */
    private int sim;

    /**
     * 相似文章数
     */
    private int count;

    /**
     * 收藏
     */
    private Store store;


    /**
     * 回帖数量
     */
    private int replyCount;


    /**
     * 文章中图片路径
     */
    private String imgSrc;

    /**
     * 转发消息的用户昵称
     */
    @FtsField("IR_RETWEETED_SCREEN_NAME")
    private String retweetedScreenName;


    /**
     * 发布时间
     */
    @FtsField("IR_URLTIME")
    private Date createdAt;


    /**
     * 点赞数
     */
    @FtsField("IR_APPROVE_COUNT")
    private long approveCount;

    /**
     * 微博正文
     */
    @FtsField(value = "IR_CONTENT", highLight = true)
    private String statusContent;
    /**
     * 文章标题
     */
    @FtsField(value = "IR_URLTITLE", highLight = true)
    private String urlTitle;

    /**
     * 阅读数 （设置列存储）
     */
    @FtsField("IR_RDCOUNT")
    private long rdcount;

    /**
     * 赞数 （设置列存储）
     */
    @FtsField("IR_PRCOUNT")
    private long prcount;

    /**
     * 地域
     */
    @FtsField("IR_LOCATION")
    private String location;
    @FtsField("IR_VIA")
    private String via;
    /**
     * 视频分享数
     */
    @FtsField("IR_SHARE_COUNT")
    private long shareCount;
//    @FtsField("IR_VRESERVED1")
//    private String vreserved1;
//    @FtsField("IR_VRESERVED2")
//    private String vreserved2;
    /**
     * 命中句，包含关键词的语句
     */
    private String hit;

    /**
     * 命中词，被标红的字
     */
    private String hitWord;
    /**
     * 粉丝数
     */
    private long followersCount;

    /**
     * 版面位置1  电子报专属
     */
    @FtsField("IR_VRESERVED1")
    private String vreserved1;

    /**
     * 版面位置2  电子报专属
     */
    @FtsField("IR_VRESERVED2")
    private String vreserved2;

    /**
     * 转发消息的url
     */
    @FtsField("IR_RETWEETED_URL")
    private String retweetedUrl;
    /**
     * 电子报专属  vreserved1 + vreserved2 确定一个位置
     */
    private String vreserved;

    public String getVreserved(){
        if(StringUtil.isNotEmpty(this.vreserved1) && StringUtil.isNotEmpty(this.vreserved2)){
            return this.vreserved1+" "+this.vreserved2;
        } else if(StringUtil.isNotEmpty(this.vreserved1) && StringUtil.isEmpty(this.vreserved2)){
            return this.vreserved1;
        }else if(StringUtil.isNotEmpty(this.vreserved2) && StringUtil.isEmpty(this.vreserved2)){
            return this.vreserved2;
        }else{
            return  null;
        }
    }

    public String getSrcName() {
        if (StringUtil.isNotEmpty(srcName) && srcName.length() > 30) {
            return  srcName.substring(0,30);
        }else {
            return srcName;
        }
    }
    /**
     * 用于引爆点
     * @return
     */
    public String beforeUrl() {
        return StringUtil.isEmpty(this.urlName) ? this.retweetedUrl : this.urlName;
    }
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月23日 changjiang creat
 */