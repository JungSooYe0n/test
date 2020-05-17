/*
 * Project: netInsight
 *
 * File Created at 2019/2/22
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.tool.controller;

import com.trs.hybase.client.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.support.fts.builder.HomecomingBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.tool.service.IHomecomingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 返乡日记分析控制器
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since yan.changjiang @ 2019/2/22
 */
@Slf4j
@Api(description = "返乡日记分析")
@RestController
@RequestMapping("homecoming")
public class HomecomingController {

    @Autowired
    private IHomecomingService homecomingService;


    /**
     * 返乡日记数据分析接口
     *
     * @return
     */
    @EnableRedis(poolId = "tool", cacheMinutes = 120)
    @FormatResult
    @GetMapping("/analysis")
    @ApiOperation("返乡日记数据分析接口")
    public Object analysis() throws TRSException {
        return homecomingService.getAnalysisData();
    }

    /**
     * 分页检索
     *
     * @param province
     * @param pageNo
     * @param pageSize
     * @param year
     * @param label
     * @return
     */
    @EnableRedis(poolId = "tool", cacheMinutes = 120)
    @FormatResult
    @PostMapping("/pageList")
    @ApiOperation("返乡日记数据分页检索")
    public Object pageList(@ApiParam("province") @RequestParam(value = "province") String province,
                           @ApiParam("pageNo") @RequestParam(value = "pageNo", defaultValue = "0") long pageNo,
                           @ApiParam("pageSize") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                           @ApiParam("year") @RequestParam(value = "year", required = false) String year,
                           @ApiParam("label") @RequestParam(value = "label", required = false) String[] label,
                           @ApiParam("order") @RequestParam(value = "order", required = false) String order,
                           @ApiParam("desc") @RequestParam(value = "desc", required = false) boolean desc) throws TRSException {
        //防止前端乱输入
        pageSize = pageSize>=1?pageSize:10;
        HomecomingBuilder builder = new HomecomingBuilder(pageNo, pageSize);
        if (StringUtils.isNotBlank(province)) { // 省份
            builder.filterField("PROVINCE", "\"" + province + "\"", Operator.Equal);
        }
        if (StringUtils.isNotBlank(year)) { // 年份
            builder.filterField("YEAR", year, Operator.Equal);
        }
        if (ObjectUtil.isNotEmpty(label)) { // 标签，支持多值
            builder.filterField("LABEL", label, Operator.Equal);
        }
        if (StringUtils.isNotBlank(order)) { // 排序
            builder.orderBy(order, desc);
        }
        return homecomingService.pageList(builder);
    }

    /**
     * 返乡日记数据获取年份信息
     * @return
     * @throws TRSException
     */
    @EnableRedis(poolId = "tool", cacheMinutes = 120)
    @FormatResult
    @GetMapping("/getConditions")
    @ApiOperation("返乡日记数据获取筛选条件")
    public Object getYears() throws TRSException{
        return homecomingService.getConditions();
    }


}

