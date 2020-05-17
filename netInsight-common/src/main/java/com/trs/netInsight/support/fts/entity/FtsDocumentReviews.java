package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 *  检索微博热门评论实体
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/19 9:53.
 * @desc
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(indices =Const.SINAREVIEWS)
public class FtsDocumentReviews extends IDocument implements Comparable<FtsDocumentReviews>,Serializable {


    /**
     * 库里唯一标识
     */
    @FtsField("IR_RID")
    private String rid;

    /**
     * 网站名称,站点名称
     */
    @FtsField("IR_SITENAME")
    private String siteName;

    /**
     * 栏目
     */
    @FtsField("IR_CHANNEL")
    private String channel;

    /**
     * 对应被评论微博地址
     */
    @FtsField("IR_URLNAME")
    private String urlName;

    /**
     * 作者
     */
    @FtsField("IR_AUTHORS")
    private String authors;
    /**
     * 发布时间
     */
    @FtsField("IR_URLTIME")
    private Date urlTime;

    /**
     * 子评论数
     */
    @FtsField("IR_COMMENTS")
    private long comments;

    /**
     * 评论正文
     */
    @FtsField(value="IR_CONTENT",highLight = true)
    private String content;

    /**
     * 点赞数
     */
    @FtsField("IR_AGREE")
    private long agree;

    /**
     * 数据中心内部
     */
    @FtsField("IR_BBSNUM")
    private long bbsNum;

    /**
     * serviceId
     */
    @FtsField("IR_SERVICEID")
    private long serviceId;
    /**
     * 采集时间
     */
    @FtsField("IR_LASTTIME")
    private Date lastTime;

    /**
     * 对应被评论微博
     */
    @FtsField("IR_MID")
    private String mid;
    /**
     * 数据入hybase库的时间
     */
    @FtsField("HYBASE_LOADTIME")
    private Date HyLoadTime;

    /**
     * 微博评论文章发布人
     */
    private StatusUser statusUser;


    @Override
    public int compareTo(FtsDocumentReviews o) {
        int sort = 0;
        long diffValue = this.agree - o.getAgree();
        if (diffValue > 0L){
            sort = -1;
        }else if (sort < 0L){
            sort = 1;
        }
        return sort;
    }
}
