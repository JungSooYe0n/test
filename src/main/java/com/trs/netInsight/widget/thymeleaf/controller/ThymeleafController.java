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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.appApi.handler.AppApi;
import com.trs.netInsight.support.appApi.service.IOAuthService;
import com.trs.netInsight.support.appApi.utils.constance.ApiMethod;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentTF;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertSend;
import com.trs.netInsight.widget.alert.entity.PageAlertSend;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.service.IAlertSendService;
import com.trs.netInsight.widget.alert.service.IAlertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

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
    private IAlertSendService alertSendService;

    @Autowired
    private IAlertService alertService;
    @Autowired
    private FullTextSearch hybase8SearchService;

    @Value("${http.alert.netinsight.url}")
    private String alertNetinsightUrl;

    @Autowired
    private IOAuthService authService;

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
    public String alertDetails(ModelMap modelMap, @PathVariable(value = "id", required = true) String id) {
//		 	String url = "http://127.0.0.1:8020/netInsight/thymeleaf/alertDetails/"+id;  
        String url = WeixinMessageUtil.ALERT_DETAILS_URL.replace("ID", id)
                .replace("NETINSIGHT_URL", alertNetinsightUrl);
        String doGet = HttpUtil.doGet(url, null);
        Map<String, Object> map = (Map<String, Object>) JSON.parse(doGet);
        modelMap.addAttribute("alertTime", map.get("alertTime"));
        modelMap.addAttribute("size", map.get("size"));
        modelMap.addAttribute("datas", map.get("datas"));
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