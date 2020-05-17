package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.HttpUtil;
import com.trs.netInsight.util.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * hybase微博用户表 映射实体类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/1/18.
 * @desc
 */
@Getter
@Setter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.SINAUSER)
public class StatusUser extends IDocument {

    /**
     * 生日
     */
    @FtsField("IR_BIRTH")
    private Date birth;

    /**
     * 时间
     */
    @FtsField("IR_CREATED_AT")
    private Date urlTime;

    /**
     * 粉丝数
     */
    @FtsField("IR_FOLLOWERS_COUNT")
    private long followersCount;

    /**
     * 关注数
     */
    @FtsField("IR_FRIENDS_COUNT")
    private long friendsCount;

    /**
     * 性别
     */
    @FtsField("IR_GENDER")
    private String gender;

    /**
     * 分组名称
     */
    @FtsField("IR_GROUPNAME")
    private String groupName;


    /**
     * 与正文关联字段
     */
    @FtsField("IR_HKEY")
    private String hkey;

    @FtsField("IR_LASTTIME")
    private Date lastTime;

    /**
     * 地域
     */
    @FtsField("IR_LOCATION")
    private String location;

    /**
     * 头像地址
     */
    @FtsField("IR_PROFILE_IMAGE_URL")
    private String profileImageUrl;

    /**
     * 省份
     */
    @FtsField("IR_PROVINCE")
    private String province;

    /**
     * 用户昵称
     */
    @FtsField("IR_SCREEN_NAME")
    private String screenName;

    /**
     * 与正文关联字段
     */
    @FtsField("IR_SID")
    private String sid;

    /**
     * 发微博数
     */
    @FtsField("IR_STATUSES_COUNT")
    private long statusesCount;

    /**
     * 用户UID
     */
    @FtsField("IR_UID")
    private String uid;

    /**
     * 认证类型 （黄V是新浪个人认证  蓝V是新浪机构认证）
     */
    @FtsField("IR_VERIFIED")
    private String verified;

    public String getProfileImageUrl() {
     //   String imageBase64 = HttpUtil.getImageBase64(profileImageUrl);
        String base64 = HttpUtil.encodeImageToBase64(profileImageUrl);
        if (StringUtil.isEmpty(profileImageUrl) || StringUtil.isEmpty(base64)){
            return null;
        }

        return "data:image/png;base64,"+base64;
    }
}
