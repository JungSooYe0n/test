package com.trs.netInsight.support.log.controller;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.log.repository.SearchTimeLongLogRepository;
import com.trs.netInsight.support.log.service.SearchTimeLongLogService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 检索hybase时间范围超过90天日志功能模块
 * @author 马加鹏
 * @date 2021/4/22 18:15
 */
@RestController
@RequestMapping("/searchLog")
public class SearchTimeLongLogController {

    @Autowired
    private SearchTimeLongLogService searchTimeLongLogService;

    @ApiOperation("只有超级管理员才能使用的检索时间超长日志功能")
    @RequestMapping(value = "/selectSearchLog", method = RequestMethod.POST)
    @FormatResult
    public Object selectSearchLog(
            @ApiParam(name = "modelType", value = "模块类型") @RequestParam(value = "modelType", required = false) String modelType,
            @ApiParam(name = "createdTime", value = "最近使用时间") @RequestParam(value = "createdTime", required = false) String createdTime,
            @ApiParam(name = "searchTime", value = "检索时间范围差") @RequestParam(value = "searchTime", required = false) String searchTime,
            @ApiParam(name = "pageNum", value = "第几页") @RequestParam(value = "pageNum", required = false) int pageNum,
            @ApiParam(name = "pageSize", value = "每页条数") @RequestParam(value = "pageSize", required = false) int pageSize,
            @ApiParam(name = "searchType", value = "按照机构名称或者系统账号查询") @RequestParam(value = "searchType", required = false) String searchType,
            @ApiParam(name = "searchText", value = "全文检索条件") @RequestParam(value = "searchText", required = false) String searchText) {
        return searchTimeLongLogService.selectSearchLog(modelType, createdTime, searchTime, pageNum, pageSize, searchType, searchText);
    }

}















