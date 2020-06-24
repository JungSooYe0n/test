package com.trs.netInsight.widget.column.controller;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.service.IColumnChartService;
import com.trs.netInsight.widget.column.service.IIndexPageService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.column.service.IIndexTabService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private IndexPageRepository indexPageRepository;

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
    @GetMapping(value = "/updateHistortIndexPageTypeId")
    @ApiOperation("修改日常监测导航栏数据")
    public Object updateHistortIndexPageTypeId() throws Exception{
        System.out.println("修改栏目------------");
        List<IndexPage> list = indexPageRepository.findAll();
        if (list != null && list.size() > 0) {
            for (IndexPage indexPage : list) {
                if (StringUtil.isNotEmpty(indexPage.getTypeId()) && StringUtil.isEmpty(indexPage.getParentId())) {
                    IndexPage parent = indexPageRepository.findOne(indexPage.getTypeId());
                    if(parent != null ){
                        indexPage.setParentId(parent.getId());
                        indexPage.setTypeId("");
                        indexPageRepository.saveAndFlush(indexPage);
                    }
                }
            }
        }
        return "success";
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

}
