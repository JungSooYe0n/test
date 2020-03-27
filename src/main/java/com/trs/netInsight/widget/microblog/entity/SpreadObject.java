package com.trs.netInsight.widget.microblog.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.entity.StatusUser;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.ObjectUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 单条微博 传播路径对象
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/1/18.
 * @desc
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.WEIBO)
public class SpreadObject extends IDocument implements Comparable<SpreadObject>,Serializable{

    /**
     * id
     */
    private String onlyId;

    /**
     * 微博地址
     */
    @FtsField("IR_URLNAME")
    private String urlName;

    /**
     * 微博内容
     */
    @FtsField("IR_CONTENT")
    private String content;

    /**
     * 微博发布时间
     */
    @FtsField("IR_URLTIME")
    private Date urlTime;
    /**
     * 用户昵称
     */
    @FtsField("IR_SCREEN_NAME")
    private String screenName;

    /**
     * 用来关联微博用户库
     */
    @FtsField("IR_HKEY")
    private String hKey;
    /**
     * 用来关联微博用户库
     */
    @FtsField("IR_SID")
    private String sid;
    /**
     * 原发微博地址
     */
    @FtsField("IR_RETWEETED_URL")
    private String retweetedUrl;

    /**
     * 转自哪个
     */
    @FtsField("IR_RETWEETED_FROM")
    private String retweetedFrom;

    /**
     * 判断是否为原发微博
     */
    @FtsField("IR_RETWEETED_MID")
    private String retweetedMid;
    /**
     * 用户id
     */
    @FtsField("IR_UID")
    private String uid;

    /**
     * 用户使用设备
     */
    @FtsField("IR_VIA")
    private String via;
    /**
     * 被转发数
     */
    private long forwardedNum;

    /**
     * 微博文章发布人
     */
    private StatusUser statusUser;

    /**
     *  转发对象 对应下一级（即转发此条微博的）
     */
    private List<SpreadObject> subSpreadObjects;

    @Override
    public int compareTo(SpreadObject o) {
        //根据微博用户粉丝数排序
        long thisFollowersCount = 0;
        long followersCount = 0;
        if (ObjectUtil.isNotEmpty(this.statusUser)){
            thisFollowersCount = this.statusUser.getFollowersCount();
        }
        if (ObjectUtil.isNotEmpty(o.getStatusUser())){
            followersCount = o.getStatusUser().getFollowersCount();
        }
        int sort = 0;
        int diffValue = (int)(thisFollowersCount - followersCount);
        if (diffValue > 0L){
            sort = -1;
         }else if (diffValue < 0L){
            sort = 1;
        }
        return sort;

//        if (ObjectUtil.isNotEmpty(o.getStatusUser()) && ObjectUtil.isNotEmpty(this.statusUser)){
//            long diffValue = this.statusUser.getFollowersCount() - o.getStatusUser().getFollowersCount();
//            if (diffValue > 0L){
//                sort = -1;
//            }else if (diffValue < 0L){
//                sort = 1;
//            }
//        }if (ObjectUtil.isEmpty(o.getStatusUser()) && ObjectUtil.isNotEmpty(this.statusUser)){
//            sort = -1;
//        }if (ObjectUtil.isNotEmpty(o.getStatusUser()) && ObjectUtil.isEmpty(this.statusUser)){
//            sort = 1;
//        }
       // return sort;
    }

    public String getOnlyId() {
        return UUID.randomUUID().toString().replace("-","");
    }
}
