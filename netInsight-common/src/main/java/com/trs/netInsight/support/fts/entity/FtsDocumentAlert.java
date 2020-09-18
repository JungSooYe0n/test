package com.trs.netInsight.support.fts.entity;

import com.sun.mail.imap.protocol.ID;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;

/**
 * 已发预警 详情
 * @Author:拓尔思信息股份有限公司
 * @Description:
 * @Date:Created in  2020/6/30 15:16
 * @Created By yangyanyan
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.ALERT)
public class FtsDocumentAlert extends IDocument implements Serializable {
    private static final long serialVersionUID = 9199979032898055980L;

    /**
     * 用于修改
     */
    @FtsField("uid")
    private String uid;

    //    唯一 id
    @FtsField("IR_ALERT_ID")
    private String alertId;
    /**
     * 文章 id
     */
    @FtsField("IR_SID")
    private String sid;

    /**
     * 文章标题
     */
    @FtsField(value = "IR_URLTITLE", highLight = true)
    private String title;
    /**
     * 文章标题 未截取过
     */
    @FtsField(value = "IR_URLTITLE_WHOLE", highLight = true)
    private String titleWhole;
    /**
     * 文章正文 150字内
     */
    @FtsField(value = "IR_CONTENT", highLight = true)
    private String content;

    /**
     * 文章正文 去掉图片后的完整正文
     */
    @FtsField(value = "IR_FULL_CONTENT", highLight = true)
    private String fullContent;

    /**
     * 文章链接
     */
    @FtsField("IR_URLNAME")
    private String urlName;

    /**
     * 文章时间
     */
    @FtsField("IR_URLTIME")
    private Date time;

    /**
     * 文章站点
     */
    @FtsField("IR_SITENAME")
    private String siteName;

    /**
     * 来源
     */
    @FtsField("IR_GROUPNAME")
    private String groupName;

    /**
     * 规则id(热值时用到-SengMd5.java)
     */
    @FtsField("IR_ALERT_RULE_BACKUPS_ID")
    private String alertRuleBackupsId;

    /**
     * 发送方式  自动 手动
     */
//    @Column(name = "alert_source")
//    private AlertSource alertSource;

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
     * 用户昵称  作者
     */
    @FtsField("IR_SCREEN_NAME")
    private String screenName;

    /**
     * 正负面
     */
    @FtsField("IR_APPRAISE")
    private String appraise;

    /**
     * 接收人
     */
    /*@FtsField("IR_RECEIVER")
    private String receiver;*/

    /**
     * 发送方式 邮件 站内 微信
     */
    /*@FtsField("IR_SEND_WAY")
    private String sendWay;*/

    /**
     * 接收人
     */
    @FtsField("IR_RECEIVER_LIST")
    private String receiver;

    /**
     * 发送方式 邮件 站内 微信
     */
    @FtsField("IR_SEND_WAY_LIST")
    private String sendWay;

    /**
     * 主贴 0 回帖1
     */
    @FtsField("IR_NRESERVED1")
    private String  nreserved1;

    /**
     * md5值  异步算相似文章数时用
     */
    @FtsField("MD5TAG")
    private String md5tag;
    /**
     * 已读未读
     */
//    @Column(name ="flag")
//    private boolean flag=false;
    /**
     * 微博 原发 转发
     */
    @FtsField("IR_RETWEETED_MID")
    private String retweetedMid;

    /**
     * 文件存储后 每个人只能看到自己Userid下的预警
     *  用这个字段区分是发送的预警还是接受的预警
     *  用于区分站内与其他
     */
    @FtsField("IR_SEND_RECEIVE")
    private String sendOrreceive;
    /**
     * APP页面展示使用
     */
    @FtsField("IR_IMAGE_URL")
    private String imageUrl;

    /**
     * 是否发送
     */
    @Transient
    private boolean send;

    /**
     * 收藏Boolean型( true 表示已收藏)
     */
    @Transient
    private Boolean favourite;

    /**
     * 收藏Boolean型( true 表示已收藏)
     */
    @Transient
    private String copyTitle;

    /**
     * 热度值预警时相似文章数
     */
    @Transient
    private int sim;

    /**
     * 存表达式的key
     */
    @Transient
    private String trslk;
    /**
     * 关键词
     */
    @FtsField("IR_KEYWORDS")
    private String keywords;
    /**
     * 入库时间(即创建时间)
     */
    @FtsField("IR_LOADTIME")
    private Date loadTime;
    //    用户id
    @FtsField("IR_USER_ID")
    private String userId;
    //    用户分组id
    @FtsField("IR_SubGroup_ID")
    private String subGroupId;
    //    机构id
    @FtsField("IR_ORGANIZATION_ID")
    private String organizationId;
    @Transient
    private Date createdTime;

    public String getId() {
        return this.uid;
    }
    public Date getCreatedTime() {
        return this.loadTime;
    }

    public FtsDocumentAlert(String sid, String title, String titleWhole, String content,String fullContent, String urlName, Date time, String siteName, String groupName, long commtCount, long rttCount, String screenName, String appraise, String receiver, String sendWay, String nreserved1, String md5tag, String retweetedMid, String imageUrl, String keywords, int sim, String alertRuleBackupsId) {
        this.sid = sid;
        this.title = title;
        this.titleWhole = titleWhole;
        this.content = content;
        this.fullContent = fullContent;
        this.urlName = urlName;
        this.time = time;
        this.siteName = siteName;
        this.groupName = groupName;
        this.commtCount = commtCount;
        this.rttCount = rttCount;
        this.screenName = screenName;
        this.appraise = appraise;
        this.receiver = receiver;
        this.sendWay = sendWay;
        this.nreserved1 = nreserved1;
        this.md5tag = md5tag;
        this.retweetedMid = retweetedMid;
        this.imageUrl = imageUrl;
//        this.trslk = trslk;
        this.keywords = keywords;
        this.sim = sim;
        this.alertRuleBackupsId = alertRuleBackupsId;
    }
}
