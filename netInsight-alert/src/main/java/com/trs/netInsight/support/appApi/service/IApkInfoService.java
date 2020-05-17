package com.trs.netInsight.support.appApi.service;

import com.trs.netInsight.support.appApi.entity.ApkInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * api业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by duhq on 2019/05/10.
 * @desc
 */
public interface IApkInfoService {

    /**
     * 验证用户名和密码获取token
     */
    public Object isLatest(int version,HttpServletRequest request);

    /**
     * 获取最新版本的apk信息
     */
    public ApkInfo getLatestVesion();

    /**
     * 保存apk信息
     */
    public ApkInfo saveApkInfo(String version,int versionCode,int isforce,String filepath,String filename,String description);
}
