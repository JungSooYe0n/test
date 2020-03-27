package com.trs.netInsight.support.knowledgeBase.controller;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeClassify;
import com.trs.netInsight.support.knowledgeBase.service.IKnowledgeBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Scanner;

/**
 * @Author:拓尔思信息股份有限公司
 * @Description: 知识库控制层
 * @Date:Created in  2020/3/9 14:56
 * @Created By yangyanyan
 */
@RestController
@RequestMapping("/knowledgeBase")
@Api(description = "知识库控制层")
@Slf4j
public class KnowledgeBaseController {
    @Autowired
    private IKnowledgeBaseService knowledgeBaseService;

    @FormatResult
    @PostMapping(value = "/saveKnowledge")
    @ApiOperation("添加知识库元素")
    public Object saveKnowledge(@ApiParam("关键词，多个词以分号（;）分割") @RequestParam(value = "keywords",required = true)String keywords,
                                @ApiParam("知识库元素分类") @RequestParam(value = "classify",defaultValue = "Exclude")KnowledgeClassify classify){
        knowledgeBaseService.save(keywords,classify);
        return "success!";
    }

    @FormatResult
    @GetMapping(value = "/selectKnowledge")
    @ApiOperation("查询所有/按照分类查询知识库元素")
    public Object selectKnowledge(@ApiParam("知识库元素分类") @RequestParam(value = "classify",defaultValue = "Exclude")KnowledgeClassify classify){
        //后期可依据分类，查询
        return knowledgeBaseService.findByClassify(classify);
    }
    @FormatResult
    @GetMapping(value = "/selectOneKnowledge")
    @ApiOperation("查询某一条知识库元素")
    public Object selectOneKnowledge(@ApiParam("知识库元素id")@RequestParam(value = "id",required = true)String id){
        //后期可依据分类，查询
        return knowledgeBaseService.findOneById(id);
    }

    @FormatResult
    @GetMapping(value = "/deleteOneKnowledge")
    @ApiOperation("删除某一条知识库元素")
    public Object deleteOneKnowledge(@ApiParam("知识库元素id")@RequestParam(value = "id",required = true)String id){
        //后期可依据分类，查询
        knowledgeBaseService.deleteOne(id);
        return "success";
    }

    @FormatResult
    @GetMapping(value = "/updateOneKnowledge")
    @ApiOperation("更新某一条知识库元素")
    public Object updateOneKnowledge(@ApiParam("知识库元素id")@RequestParam(value = "id",required = true)String id,
                                     @ApiParam("主体内容")@RequestParam(value = "keywords",required = true)String keywords,
                                     @ApiParam("知识库元素分类")@RequestParam(value = "Exclude",required = true)KnowledgeClassify classify){
        //后期可依据分类，查询
        return knowledgeBaseService.updateOne(id,keywords,classify);
    }
}
