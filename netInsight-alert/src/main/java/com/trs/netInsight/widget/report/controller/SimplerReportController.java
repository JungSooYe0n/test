package com.trs.netInsight.widget.report.controller;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.report.service.IReportServiceNew;
import com.trs.netInsight.widget.report.service.ISimplerReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.search.SearchException;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 舆情报告 极简模式 控制层
 *
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/9.
*/
@Slf4j
@RestController
@RequestMapping("/simplerReport")
@Api(description = "舆情报告极简模式模块接口")
public class SimplerReportController {

    @Autowired
    private ISimplerReportService simplerReportService;

    @Autowired
    private IReportServiceNew reportServiceNew;


    /**
     *    1、查询所有 专题、素材库（收藏夹）、自定义列表 接口
     * @param type
     * @return
     * @throws SearchException
     * @throws TRSException
     */
    @RequestMapping(value = "/showGroupList", method = RequestMethod.GET)
    @ApiOperation("查询所有 专题分析 或者 素材库  或者  自定义专题")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_LIST, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "区分 专题分析special 或者  素材库material  或者  自定义专题custom", dataType = "String", paramType = "query", required = false)
//            @ApiImplicitParam(name = "pageNum", value = "分页第几页", dataType = "String", paramType = "query", required = false),
//            @ApiImplicitParam(name = "pageSize", value = "分页每页显示多少条", dataType = "String", paramType = "query", required = false)
})
    @FormatResult
    public Object showGroupList(@RequestParam(value = "type",defaultValue = "special",required = false) String type
//                                @RequestParam(value = "pageNum",required = false,defaultValue = "0") Integer pageNum,
//                                @RequestParam(value = "pageSize",required = false,defaultValue = "6") Integer pageSize
                                                                                                                ) throws TRSException {
        //专题分析 、 素材库、自定义专题分析 列表
        Object groupList = simplerReportService.getGroupList(type);

        //模板列表
        String templateType = "";
        if ("special".equals(type) || "custom".equals(type)){
            templateType = "special";
        }else {
            templateType =type;

        }
        Object templateNewList = simplerReportService.getTemplateNew(templateType);
        //报告列表
        List<ReportNew> reportList = simplerReportService.listSimplerReport(type);

        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("first",groupList);
        returnMap.put("second",templateNewList);
        returnMap.put("third",reportList);
        return returnMap;
    }

    /**
     *    1、查询所有 专题分析 、 素材库、自定义专题分析 列表
     * @param type
     * @return
     * @throws SearchException
     * @throws TRSException
     */
    @RequestMapping(value = "/showList", method = RequestMethod.GET)
    @ApiOperation("查询所有 专题分析 或者 素材库  或者  自定义专题")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_LIST, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "区分 专题分析special 或者  素材库material  或者  自定义专题custom", dataType = "String", paramType = "query", required = false)})
    @FormatResult
    public Object showList(@RequestParam(value = "type",defaultValue = "special",required = false) String type) throws TRSException {
        //专题分析 、 素材库、自定义专题分析 列表
        Object groupList = simplerReportService.getGroupList(type);
        return groupList;
    }
    /**
     *    2、查询所有 模板
     * @param type
     * @return
     * @throws SearchException
     * @throws TRSException
     */
    @RequestMapping(value = "/showTemplates", method = RequestMethod.GET)
    @ApiOperation("查询对应模板")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_LIST, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "区分 专题分析special 或者  素材库material  或者  自定义专题custom", dataType = "String", paramType = "query", required = false)})
    @FormatResult
    public Object showTemplates(@RequestParam(value = "type",defaultValue = "special",required = false) String type) throws TRSException {

        //模板列表
        String templateType = "";
        if ("special".equals(type) || "custom".equals(type)){
            templateType = "special";
        }else {
            templateType =type;
        }
        Object templateNewList = simplerReportService.getTemplateNew(templateType);

        return templateNewList;
    }
    /**
     *    3、查询所有 报告
     * @param type
     * @return
     * @throws SearchException
     * @throws TRSException
     */
    @RequestMapping(value = "/showSimplerReports", method = RequestMethod.GET)
    @ApiOperation("查询对应历史报告")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_LIST, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "区分 专题分析special 或者  素材库material  或者  自定义专题custom", dataType = "String", paramType = "query", required = false)})
    @FormatResult
    public Object showSimplerReports(@RequestParam(value = "type",defaultValue = "special",required = false) String type) throws TRSException {
        //报告列表
        List<ReportNew> reportList = simplerReportService.listSimplerReport(type);
        return reportList;
    }
    /**
     *  生成报告  第一步  接收前端传的 专题id（专题分析|自定义专题） 、素材组id 和 模板Id 生成报告
     * @param reportType
     * @param typeId
     * @param templateId
     * @return
     * @throws Exception
     */
    @ApiOperation("极简模式 生成报告")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_CALCULATE, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({@ApiImplicitParam(name = "reportType",value = "报告类型 专题（专题分析 special|自定义专题 custom）、素材库material",dataType = "String",paramType = "query",required = true),
                        @ApiImplicitParam(name = "typeId",value = "专题id 、 素材库id",dataType = "String",paramType = "query",required = true),
                        @ApiImplicitParam(name = "templateId",value = "模板id",dataType = "String",paramType = "query",required = true)})
    @RequestMapping(value = "/calculateSimplerReport",method = RequestMethod.POST)
    @FormatResult
    public Object calculateReportData(@RequestParam(value = "reportType",required = true) String reportType,
                                   @RequestParam(value = "typeId",required = true) String typeId,
                                   @RequestParam(value = "templateId",required = true) String templateId) throws Exception{
        return simplerReportService.calculateReport(reportType,typeId,templateId);
    }

    /**
     * 生成报告  第二步 针对专题分析、自定义专题图片类数据返回前端生成base64码返回后台
     * @param reportId
     * @return
     * @throws Exception
     */
    @ApiOperation("极简模式 生成报告")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_IMGDATA, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "reportId",value = "id",dataType = "String",paramType = "query",required = true)})
    @RequestMapping(value = "/reportImgData",method = RequestMethod.GET)
    @FormatResult
    public Object reportImgData(
            @RequestParam(value = "reportId",required = true) String reportId) throws Exception{
        return simplerReportService.imgData(reportId);
    }
    /**
     * 生成报告  第三步  拿到前端处理后的数据  完成对word文档读写
     * @author shao.guangze
     * @param reportId
     * @param templateId
     * @param jsonImgElements
     * @return
     * @throws Exception
     */
    @ApiOperation("生成报告")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_GENERATION, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "templateId", value = "报告模板id", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "jsonImgElements", value = "json格式的图片数据", dataType = "String", paramType = "query", required = false)})
    @RequestMapping(value = "/generationReport", method = RequestMethod.POST)
    @FormatResult
    public Object generationReport(@RequestParam(value = "reportId",required = true) String reportId, @RequestParam(value = "templateId",required = true) String templateId,
                                   @RequestParam(value = "jsonImgElements") String jsonImgElements) throws Exception{
        return simplerReportService.createImplerReport(reportId,templateId,jsonImgElements);
    }

    /**
     * 编辑报告时 报告数据详情接口
     * @param reportId
     * @return
     */
    @ApiOperation("编辑报告时 报告数据详情接口")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_DETAIL, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
           @ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true)})
    @RequestMapping(value = "/detailReport", method = RequestMethod.GET)
    @FormatResult
    public Object detailReport(@RequestParam(value = "reportId",required = true) String reportId) throws OperationException{
        return simplerReportService.reportDetail(reportId);
    }


    /**
     * 删除历史报告   编辑数据  单条|批量
     * @return
     * @throws OperationException
     */
    @ApiOperation("删除历史报告中的资源")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_DELRESOURCES, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "resourceId", value = "需要删除的报告资源id，多选时用 ; 分隔", dataType = "String", paramType = "query", required = false) })
    @FormatResult
    @RequestMapping(value = "/delReportResource", method = RequestMethod.POST)
    public Object deleteReportResource(@RequestParam(value = "resourceId") String resourceId) throws OperationException {
        try {
            return reportServiceNew.delReportResource(resourceId);
        } catch (Exception e) {
            log.error("删除报告资源失败", e);
            throw new OperationException("删除报告资源失败,message" + e);
        }
    }
    /**
     * 编辑历史报告   编辑数据  单条  针对图表类模块编辑
     * @return
     * @throws OperationException
     */
    @ApiOperation("编辑历史报告中的资源")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_DELRESOURCES, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "resourceId", value = "需要删除的报告资源id", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "imgComment", value = "图表数据表述", dataType = "String", paramType = "query", required = false)})
    @FormatResult
    @RequestMapping(value = "/updateReportResource", method = RequestMethod.POST)
    public Object updateReportResource(@RequestParam(value = "resourceId") String resourceId,
                                       @RequestParam(value = "imgComment") String imgComment) throws OperationException {
        try {
            return reportServiceNew.updateReportResource(resourceId, imgComment);
        } catch (Exception e) {
            log.error("编辑历史报告中的资源失败", e);
            throw new OperationException("编辑历史报告中的资源失败,message" + e);
        }
    }
    /***
     * 报告编辑
     * @return
     */
    @RequestMapping(value = "/updateReport", method = RequestMethod.POST)
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_UPDATE, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @FormatResult
    @ApiImplicitParams({
            @ApiImplicitParam(name = "reportId", value = "报告id", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "reportName", value = "报告名称", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "templateList", value = "报告模板内容", dataType = "String", paramType = "query", required = false)})
    public Object updateReport(String reportId,String reportName,String templateList) throws Exception {
        return simplerReportService.updateReport(reportId,reportName,templateList);
    }
    /***
     * 报告编辑，重新生成
     * @return
     */
    @RequestMapping(value = "/reBuildReport", method = RequestMethod.POST)
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_REBUILD, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @FormatResult
    @ApiImplicitParams({
            @ApiImplicitParam(name = "reportId", value = "报告id", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "jsonImgElements", value = "报告图表数据", dataType = "String", paramType = "query", required = false),
            @ApiImplicitParam(name = "reportIntro", value = "报告简介", dataType = "String", paramType = "query", required = false)})
    public Object reBuildReport(@RequestParam(value = "reportId",required = true) String reportId,
                                @RequestParam(value = "jsonImgElements") String jsonImgElements,
                                @RequestParam(value = "reportIntro") String reportIntro) throws Exception {
        return simplerReportService.reBuildReport(reportId, jsonImgElements,reportIntro);
    }


    /**
     * 下载报告
     *
     * @author shao.guangze
     * @return
     * @throws Exception
     */
    @ApiOperation("下载报告")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_DOWNLOAD, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @RequestMapping(value = "/downloadSimplerReport", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadSimplerReport(HttpServletRequest request, HttpServletResponse response, String reportId) throws Exception {
        try {
            ReportNew report = reportServiceNew.download(reportId);
            FileSystemResource file = new FileSystemResource(report.getDocPath());
            response.resetBuffer();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add(
                    "Content-Disposition",
                    String.format("attachment; filename="
                            + new String((report.getReportName() + ".docx")
                            .getBytes(), "iso-8859-1")));
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(file.contentLength())
                    .contentType(
                            MediaType
                                    .parseMediaType("application/octet-stream"))
                    .body(new InputStreamResource(file.getInputStream()));
        } catch (Exception e) {
            log.error("报告下载错误！！！",e);
        }
        return null;
    }

    /**
     * 删除报告
     * @param reportId
     * @return
     */
    @ApiOperation("删除报告")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_DELETE, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true)})
    @RequestMapping(value = "/deleteReport" , method = RequestMethod.POST)
    @FormatResult
    public Object deleteReport(String reportId) throws Exception{
        if (StringUtil.isEmpty(reportId)) {
            throw new OperationException("删除报告，id不能为空");
        }return reportServiceNew.deleteReport(reportId);
    }


// 模板 BEGIN====================================
    /**
     *  新增和修改共用的接口，修改时提供主键
     * @param templateId
     * @param templateName
     * @param templateList
     * @param templateType
     * @return
     */
    @RequestMapping(value = "/saveSimplerTemplate", method = RequestMethod.POST)
    @ApiOperation("极简报告：：新增和修改报告模板")
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_TEMPLATE_ADD_UPDATE, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "templateId", value = "报告模板id", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "templateName", value = "模板名称", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "templateList", value = "模板具体模块", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "templateType", value = "模板类型", dataType = "String", paramType = "query")})
    @FormatResult
    public Object saveSimplerTemplate(@RequestParam(value = "templateId") String templateId,@RequestParam(value = "templateName") String templateName,
                                      @RequestParam(value = "templateList") String templateList,@RequestParam(value = "templateType") String templateType) {
        return reportServiceNew.saveCustomTemplate(templateId, templateName,
                templateList, templateType);
    }
    /**
     * 查询一个 报告模板
     * @param templateId
     * @return
     */
    @RequestMapping(value = "/detailTemplate", method = RequestMethod.GET)
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_TEMPLATE_DETAIL, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiOperation("查询一个 报告模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "templateId", value = "自定义专题ID", dataType = "String", paramType = "query",required = true)})
    @FormatResult
    public Object detailTemplate(@RequestParam(value = "templateId",required = true) String templateId) throws Exception {
        if (StringUtil.isEmpty(templateId)) {
            throw new OperationException("查模板详情，id不能为空");
        }
        return simplerReportService.findOneTemplateNew(templateId);
    }
    /**
     * 删除 模板
     * @param templateId
     * @return
     */
    @RequestMapping(value = "/deleteTemplate", method = RequestMethod.POST)
    @Log(systemLogOperation = SystemLogOperation.SIMPLER_REPORT_TEMPLATE_DELETE, systemLogType = SystemLogType.SIMPLER_REPORT, systemLogOperationPosition = "")
    @ApiOperation("删除报告模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "templateId", value = "模板ID", dataType = "String", paramType = "query",required = true)})
    @FormatResult
    public Object deleteTemplate(@RequestParam(value = "templateId",required = true) String templateId) throws Exception {
        if (StringUtil.isEmpty(templateId)) {
            throw new OperationException("删除模板，id不能为空");
        }
        return reportServiceNew.deleteTemplate(templateId);
    }
//模板 END=======================================
}
