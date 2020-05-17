package com.trs.netInsight.widget.user.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 机构登录页配置表
 *
 *
 * @Type LoginPageConfig.java
 * @author
 * @date
 * @version
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "`login_page_config`")
public class LoginPageConfig extends BaseEntity {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // --------------基本信息------------------------

    /**
     * 网页链接参数
     */
    @Column(name = "`suffix`")
    private String suffix;

    /**
     * 对应的机构id
     */
    @Column(name = "`relevance_organization_id`")
    private String relevanceOrganizationId;

    /**
     * 机构配置登录页logo图片 名字
     */
    @Column(name = "`logo_pic_name`")
    private String logoPicName;

    /**
     * 网页标签
     */
    @Column(name = "`page_title`")
    private String pageTitle;

    /**
     * 登录页公司名
     */
    @Column(name = "`company_name`")
    private String companyName;

    /**
     * 登录页申请电话
     */
    @Column(name = "`apply_tel`")
    private String applyTel;

    /**
     * 是否显示屏幕轮播图
     */
    @Column(name = "`is_show_carousel`")
    private Boolean isShowCarousel = true;

    /**
     * 是否屏蔽线上申请试用
     */
    @Column(name = "`is_shield_register`")
    private Boolean isShieldRegister = false;

    /**
     * 机构配置登录页底栏二维码
     */
    @Column(name = "`qr_code_name`")
    private String QRCodeName;

    @Override
    public String toString() {
        return "LoginPageConfig{" +
                "suffix='" + suffix + '\'' +
                ", relevanceOrganizationId='" + relevanceOrganizationId + '\'' +
                ", logoPicName='" + logoPicName + '\'' +
                ", pageTitle='" + pageTitle + '\'' +
                ", companyName='" + companyName + '\'' +
                ", applyTel='" + applyTel + '\'' +
                ", isShieldCarousel=" + isShowCarousel +
                ", isShieldRegister=" + isShieldRegister +
                ", QRCodeName='" + QRCodeName + '\'' +
                '}';
    }
}
