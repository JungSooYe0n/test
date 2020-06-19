package com.trs.netInsight.widget.column.controller;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.widget.column.service.IColumnChartService;
import com.trs.netInsight.widget.column.service.IIndexPageService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.column.service.IIndexTabService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/column/history")
@Api(description = "栏目历史数据修改接口")
@Slf4j
public class ColumnHistoryController {
    @Autowired
    private IIndexPageService indexPageService;

    @Autowired
    private IIndexTabService indexTabService;

    @Autowired
    private IIndexTabMapperService indexTabMapperService;
    @Autowired
    private IColumnChartService columnChartService;

    /**
     * 修改日常监测分组栏目的历史数据
     */
    @FormatResult
    @GetMapping(value = "/updateHistortIndexPage")
    @ApiOperation("修改日常监测分组栏目的历史数据")
    public Object updateHistortIndexPage() {
        System.out.println("修改分组------------");
        Object result = indexPageService.updateHistoryIndexPage();
        return result;
    }

    /**
     * 修改日常监测数据栏目的数据
     */
    @FormatResult
    @GetMapping(value = "/updateHistortIndexTab")
    @ApiOperation("修改日常监测数据栏目的数据")
    public Object updateHistortIndexTab() {
        System.out.println("修改栏目------------");
        Object result = indexTabMapperService.updateHistortIndexTab();
        return result;
    }


    /**
     * 修改日常监测数据栏目的数据
     */
    @FormatResult
    @GetMapping(value = "/updateHistortColumnField")
    @ApiOperation("修改日常监测数据栏目的数据 - 字段修改")
    public Object updateHistortColumnField() {
        System.out.println("修改字段------------");
        Object result = indexTabService.updateHistortColumnField();
        return result;
    }

    /**
     * 修改日常监测数据栏目的数据
     */
    @FormatResult
    @GetMapping(value = "/updateHistortColumnType")
    @ApiOperation("修改日常监测数据栏目的数据 - 添加字段 - 判断是专家模式还是普通模式，需要修改历史数据")
    public Object updateHistortColumnType() {
        System.out.println("修改类型------------");
        Object result =columnChartService.addColumnType();
        return result;
    }

}
