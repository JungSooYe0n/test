package com.trs.netInsight.support.appApi.service.impl;

import com.trs.netInsight.support.appApi.entity.ApkInfo;
import com.trs.netInsight.support.appApi.entity.repository.IApkInfoRepository;
import com.trs.netInsight.support.appApi.service.IApkInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;

@Service
public class ApkInfoServiceImpl implements IApkInfoService {
    @Autowired
    private IApkInfoRepository repository;

    @Value("${apk.save.url}")
    private String apkSaveUrl;

    @Override
    public Object isLatest(int version, HttpServletRequest request) {
        ApkInfo apkInfo = repository.findFirstByOrderByVersioncodeDesc();
        if (apkInfo == null){
            return new HashMap<>();
        }
        HashMap rtnMap = new HashMap();
        //是否最新版本
        if (version < apkInfo.getVersioncode()){
            rtnMap.put("isNew",false);
            if (apkInfo.getIsforce()==0){
                rtnMap.put("isForce",false);
            }else if (apkInfo.getIsforce() == 1){
                rtnMap.put("isForce",true);
            }else {
                rtnMap.put("isForce",null);
            }
            rtnMap.put("apkUrl",apkDownLoadUrl(request));
            rtnMap.put("versionName",apkInfo.getVersion());
            rtnMap.put("versionCode",apkInfo.getVersioncode());
            rtnMap.put("description",apkInfo.getDescription());
        }else {
            rtnMap.put("isNew",true);
            rtnMap.put("isForce",false);
            rtnMap.put("apkUrl","");
            rtnMap.put("versionName",apkInfo.getVersion());
            rtnMap.put("versionCode",apkInfo.getVersioncode());
            rtnMap.put("description",apkInfo.getDescription());
        }
        return rtnMap;
    }

    @Override
    public ApkInfo getLatestVesion() {
        return repository.findFirstByOrderByVersioncodeDesc();
    }

    @Override
    public ApkInfo saveApkInfo(String version,int versionCode,int isforce,String filepath,String filename,String description) {

        ApkInfo apkInfo = new ApkInfo();
        apkInfo.setFilename(filename);
        apkInfo.setFilepath(filepath);
        apkInfo.setIsforce(isforce);
        apkInfo.setVersion(version);
        apkInfo.setVersioncode(versionCode);
        apkInfo.setDescription(description);
        apkInfo.setCreatedTime(new Date());
        ApkInfo apk = repository.save(apkInfo);
        return apk;
    }

    //获取apk下载链接
    String apkDownLoadUrl(HttpServletRequest request){
        StringBuffer requestUrl = request.getRequestURL();
        String url = requestUrl.toString();
        int index = url.indexOf("/netInsight/");
        String mainUrl = url.substring(0,index);
        String Url = mainUrl+"/netInsight/apk/appDownload";
        return Url;
    }
}
