/*
 * Project: netInsight
 *
 * File Created at 2018年2月1日
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.thymeleaf.controller;

import com.alibaba.fastjson.JSON;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.appApi.service.IOAuthService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentAlert;
import com.trs.netInsight.support.fts.entity.FtsDocumentAlertType;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.service.IAlertSendService;
import com.trs.netInsight.widget.alert.service.IAlertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.mail.search.SearchException;
import java.util.*;

/**
 * @author 谷泽昊
 * @Type ThymeleafController.java
 * @date 2018年2月1日 上午8:52:20
 */
@Slf4j
@Controller
@RequestMapping("/thymeleaf")
@Api(description = "模板类")
public class ThymeleafController {

    @Autowired
    private FullTextSearch hybase8SearchServiceNew;

    @Value("${http.alert.netinsight.url}")
    private String alertNetinsightUrl;


    /**
     * 使用Thymeleaf模板
     *
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/demo", method = RequestMethod.GET)
    public String helloIndex(ModelMap modelMap) {
        // 加入一个属性，用来在模板中读取 - 此处的值将会覆盖模板页面本身已经存在的值
        modelMap.addAttribute("title", "标题-测试页面");
        modelMap.addAttribute("host", "demo.baomidou.com:8080");
        // return模板文件的名称，对应src/main/resources/templates/index.html
        return "index";
    }

    /**
     * 查看详情（微信）
     *
     * @param id
     * @return
     * @date Created at 2018年1月31日 下午4:18:50
     * @Author 谷泽昊
     */
    @ApiOperation("查看详情")
    @GetMapping(value = "/alertDetails/{id}")
    public String alertDetails(ModelMap modelMap, @PathVariable(value = "id", required = true) String id)throws  TRSException, TRSSearchException {
        Map<String,Object> returnMap = null;
        List<Map<String,Object>> listMapRedis = RedisUtil.getListMap(id);
        if(ObjectUtil.isNotEmpty(listMapRedis) && listMapRedis.size()>0){
            returnMap =  listMapRedis.get(0);
        }else{
            returnMap = new HashMap<String, Object>();
            QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.filterField(FtsFieldConst.FIELD_ALERT_TYPE_ID,id, Operator.Equal);
            queryBuilder.setDatabase(Const.ALERTTYPE);
            queryBuilder.page(0,1);
            PagedList<FtsDocumentAlertType> pagedList = hybase8SearchServiceNew.ftsAlertList(queryBuilder, FtsDocumentAlertType.class);
            if (ObjectUtil.isNotEmpty(pagedList) && ObjectUtil.isNotEmpty(pagedList.getPageItems().get(0))){
                FtsDocumentAlertType ftsDocumentAlertType = pagedList.getPageItems().get(0);
                returnMap.put("alertTime", ftsDocumentAlertType.getAlertTime());
                //returnMap.put("size", ftsDocumentAlertType.getSize());

                String alertIds = ftsDocumentAlertType.getIds();

                if (StringUtil.isNotEmpty(alertIds)) {
                    QueryBuilder builder = new QueryBuilder();
                    builder.setPageSize(-1);
                    builder.setPageNo(0);

                    builder.filterField(FtsFieldConst.FIELD_ALERT_ID,alertIds.split(";"),Operator.Equal);
                    builder.setDatabase(Const.ALERT);
                    PagedList<FtsDocumentAlert> ftsDocumentAlertPagedList = hybase8SearchServiceNew.ftsAlertList(builder, FtsDocumentAlert.class);
                    if (ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList) && ObjectUtil.isNotEmpty(ftsDocumentAlertPagedList.getPageItems())){
                        List<Map<String, Object>> listMap = new ArrayList<>();

                        List<FtsDocumentAlert> ftsDocumentAlerts = ftsDocumentAlertPagedList.getPageItems();
                        for (FtsDocumentAlert ftsDocumentAlert : ftsDocumentAlerts) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("title", StringUtil.replaceImg(ftsDocumentAlert.getTitle().replaceAll("&lt;","<").replaceAll("&nbsp;"," ").replaceAll("&gt;",">")));
                            Date time = ftsDocumentAlert.getTime();
                            map.put("urlTime", DateUtil.format2String(time, DateUtil.yyyyMMdd));
                            map.put("siteName", StringUtils.isBlank(ftsDocumentAlert.getSiteName()) ? ftsDocumentAlert.getGroupName()
                                    : ftsDocumentAlert.getSiteName());
                            map.put("urlName", ftsDocumentAlert.getUrlName());
                            map.put("sid", ftsDocumentAlert.getSid());
                            map.put("time", DateUtil.format2String(ftsDocumentAlert.getTime(), DateUtil.yyyyMMdd));
                            String content = ftsDocumentAlert.getFullContent();
                            if(StringUtil.isEmpty(content)){
                                content = ftsDocumentAlert.getContent();
                            }
                            String titleWhone = StringUtil.calcuCutLength(ftsDocumentAlert.getTitleWhole().replaceAll("&lt;","<").replaceAll("&nbsp;"," ").replaceAll("&gt;",">"),80);
                            if(titleWhone.contains("<font color=red>")){
                                titleWhone = StringUtil.cutContentMd5(titleWhone,80);
                            }
                            titleWhone = StringUtil.cutContentPro(titleWhone,80);
                            map.put("titleWhole", titleWhone);
                            if(content!=null) {
                                map.put("content", content.replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
                            }else {
                                map.put("content",null);
                            }
                            listMap.add(map);
                        }
                        returnMap.put("datas", listMap);
                        returnMap.put("size", listMap.size());
                    }
                }else {
                    returnMap.put("datas", null);
                }
                listMapRedis = new ArrayList<>();
                listMapRedis.add(returnMap);
                RedisUtil.setListMap(id, listMapRedis);
            }
        }
        modelMap.addAttribute("alertTime", returnMap.get("alertTime"));
        modelMap.addAttribute("size", returnMap.get("size"));
        modelMap.addAttribute("datas", returnMap.get("datas"));

        return "wechat";
    }
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年2月1日 谷泽昊 creat
 */