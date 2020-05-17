package com.trs.netInsight.support.appApi.utils.constance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统默认api
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月3日
 */
@Getter
@AllArgsConstructor
public enum ApiMethod {
    selectAppColumnOne(10001, "selectAppColumnOne", "10000"),
    selectAppColumnTwo(10002, "selectAppColumnTwo", "10000"),
    selectAppColumnThr(10003, "selectAppColumnThr", "10000"),
    showOrHideOp(10004, "showOrHideOp", "10000"),
    sortColumnOp(10005, "sortColumnOp", "10000"),
    appLogin(10006, "applogin", "10000"),
    thirdColumnOp(10007, "thirdColumnOp", "10000"),
    getChartData(10008,"getChartData","10000"),
    addfavourites(10009,"addfavourites","10000"),
    deladdfavourites(10010,"deladdfavourites","10000"),
    favouritesList(10011,"favouritesList","10000"),
    oneInfo(10012,"oneInfo","10000"),
    oneInfoStatus(10013,"oneInfoStatus","10000"),
    oneInfoWeChat(10014,"oneInfoWeChat","10000"),
    oneInfoTF(10015,"oneInfoTF","10000"),
    selectAppAlert(10016,"selectAppAlert","10000"),
    selectOneAppAlert(10017,"selectOneAppAlert","10000");

    /**
     * api代码
     */
    private int code;

    /**
     * api
     */
    private String name;

    /**
     * 低频
     */
    private String frequencyLow;

    /**
     * 中频
     */
    private String frequencyCommon;

    /**
     * 高频
     */
    private String frequencyHigh;

    /**
     * 粗粒度授权范围
     */
    private String grantTypeCode;

    /**
     * 细粒度权限校验参数集
     */
    private String[] grantParams;

    /**
     * 使用默认频率构造
     *
     * @param code
     * @param name
     */
    private ApiMethod(int code, String name, String grantTypeCode, String[] grantParams) {
        this.code = code;
        this.name = name;
        this.grantTypeCode = grantTypeCode;
        this.grantParams = grantParams;
    }

    /**
     * 使用默认频率构造
     *
     * @param code
     * @param name
     */
    private ApiMethod(int code, String name, String grantTypeCode) {
        this.code = code;
        this.name = name;
        this.grantTypeCode = grantTypeCode;
    }

    /**
     * 根据code获取method
     *
     * @param code
     * @return
     */
    public static ApiMethod findByCode(int code) {
        for (ApiMethod method : ApiMethod.values()) {
            if (code == method.code) {
                return method;
            }
        }
        return null;
    }

}
