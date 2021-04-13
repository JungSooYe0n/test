package com.trs.netInsight.widget.report.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.log.entity.RequestTimeLog;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.repository.RequestTimeLogRepository;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.report.constant.Chapter;
import com.trs.netInsight.widget.report.entity.*;
import com.trs.netInsight.widget.report.entity.repository.TemplateNewRepository;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.docx4j.wml.Tr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.report.service.IReportServiceNew;
import com.trs.netInsight.widget.report.service.ISpecialReportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;
import static com.trs.netInsight.widget.report.constant.ReportConst.ACTIVEACCOUNTkey;
import static com.trs.netInsight.widget.report.constant.ReportConst.WEIBOHOTTOPICSkey;

/**
 * Created by shao.guangze on 2018年5月24日 下午5:46:05
 * 所有报告请求的接口
 */
@Slf4j
@RestController
@RequestMapping("/reportNew")
@Api(description = "新版舆情报告模块接口")
public class ReportControllerNew {

	@Autowired
	private IReportServiceNew reportServiceNew;

	@Autowired
	private ISpecialReportService sepcialReportService;

	@Autowired
	private RequestTimeLogRepository requestTimeLogRepository;

	@Autowired
	private TemplateNewRepository templateNewRepository;

	// 新增和修改共用的接口，修改时提供主键
	@RequestMapping(value = "/saveTemplate", method = RequestMethod.POST)
	@ApiOperation("新增和修改报告模板")
	@Log(systemLogOperation = SystemLogOperation.REPORT_SAVE_TEMPLATE, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "templateId", value = "报告模板id", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "templateName", value = "模板名称", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "reportName", value = "报告名称", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "templateList", value = "具体模板数据", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "templateType", value = "报告类型", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "totalIssue", value = "总期号", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "thisIssue", value = "本期期号", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "preparationAuthors", value = "编制作者", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "preparationUnits", value = "编制单位", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "statisticsTime", value = "统计时间", dataType = "String", paramType = "query", required = false )})
	@FormatResult
	public Object saveTemplate(String templateId,
							   String templateName,
							   String reportName,
							   String templateList,
							   String templateType,
							   String totalIssue,
							   String thisIssue,
							   String preparationUnits,
							   String preparationAuthors,
							   String statisticsTime) throws OperationException{
		if(StringUtil.isEmpty(templateType)){
			throw new OperationException("没有选择模板类型");
		}
		return reportServiceNew.saveTemplate(templateId, templateName,reportName,
				templateList, templateType, totalIssue, thisIssue, preparationUnits, preparationAuthors, statisticsTime);
	}

	// 新增和修改共用的接口，修改时提供主键
	@RequestMapping(value = "/saveSpecialTemplate", method = RequestMethod.POST)
	@ApiOperation("专报：：新增和修改报告模板")
	@Log(systemLogOperation = SystemLogOperation.REPORT_SAVE_SPECIAL_TEMPLATE, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "templateId", value = "报告模板id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "templateName", value = "模板名称", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "templateList", value = "具体模板数据", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "groupName", value = "专报的分组名称", dataType = "String", paramType = "query")})
	@FormatResult
	public Object saveSpecialTemplate(String templateId, String templateName,
			String templateList, String groupName) {
		return reportServiceNew.saveSpecialTemplate(templateId, templateName,
			templateList, groupName);
		}	
	
	// 根据所选报告类型(日报、周报、月报、专报)查询所有报告模板(分组)
	// 专报模板不做分组限制，所以废弃groupName也可以。
	@RequestMapping(value = "/findAllTemplate", method = RequestMethod.GET)
	@ApiOperation("查询所有报告模板")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "templateType", value = "模板类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "groupName", value = "专报的分组名称", dataType = "String", paramType = "query")})
	@FormatResult
	public Object findAllTemplate(String templateType, String groupName) {
		return reportServiceNew.findAllTemplate(templateType, groupName);
	}
	
	@RequestMapping(value = "/findEmptyTemplate", method = RequestMethod.GET)
	@ApiOperation("查询固定报告模板")
	@FormatResult
	public Object findEmptyTemplate(String reportType) {
		return reportServiceNew.findEmptyTemplate(reportType);
	}

	@RequestMapping(value = "/deleteTemplate", method = RequestMethod.POST)
	@Log(systemLogOperation = SystemLogOperation.REPORT_DELETE_TEMPLATE, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiOperation("删除报告模板")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "templateId", value = "模板ID", dataType = "String", paramType = "query",required = true)})
	@FormatResult
	public Object deleteTemplate(String templateId) {
		return reportServiceNew.deleteTemplate(templateId);
	}

	@RequestMapping(value = "/tOrderSet", method = RequestMethod.POST)
	@ApiOperation("修改模板顺序，这里指模板显示顺序，而非模板中的模块.中间用逗号分隔。")
	@Log(systemLogOperation = SystemLogOperation.REPORT_T_ORDERSET, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "templateId", value = "模板ID", dataType = "String", paramType = "query",required = true),
			@ApiImplicitParam(name = "templatePosition", value = "模板位置", dataType = "String", paramType = "query",required = true)})
	@FormatResult
	public Object templateOrederSet(String templateId, String templatePosition){
		reportServiceNew.templateOrderSet(templateId, templatePosition);
		return "success";
	}
	/**
	 * 列出所有报告资源池中的资源(分组）
	 * 
	 * @author shao.guangze
	 * @returnf
	 * @throws TRSException
	 */
	@ApiOperation("显示所有的报告资源，在添加日报、周报、月报时显示 - 是在制作报告页面的数据，当前页面的数据不是生产报告中的资源")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportType", value = "报告类型", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "templateId", value = "报告模板id", dataType = "String", paramType = "query", required = true) })
	@RequestMapping(value = "/listAllReportResource", method = RequestMethod.GET)
	@ResponseBody
	@FormatResult
	public Object listAllReportResource(
			@RequestParam(value = "reportType")String reportType, 
			@RequestParam(value = "templateId")String templateId)
			throws TRSSearchException, TRSException {
		return reportServiceNew.listAllReportResource( reportType,
				templateId);
	}

	/**
	 * 添加到我的资源池/已生成的报告 judged by isEmpty(reportId)
	 *
	 * @param sids
	 *            需要增加到资源池的sid集合，用分号（;）隔开
	 * @return
	 * @throws OperationException
	 *             createdby shao.guangze
	 */
	@ApiOperation("添加到我的资源池/已生成的报告，单选、多选公用1个接口")
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "sids", value = "需要增加到资源池的sid集合，用分号（;）隔开", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupName", value = "来源 查库用 传多个的时候用（;）隔开", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "trslk", value = "列表查询表达式，查数据用", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "chapter", value = "章节", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "img_data", value = "图片资源数据", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "img_type", value = "图片资源数据类型", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "reportType", value = "报告类型", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "templateId", value = "报告模板id", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "chapterPosition", value = "章节位置", dataType = "Integer", paramType = "query", required = true),
			@ApiImplicitParam(name = "reportId", value = "报告id(此时为已生成的报告)", dataType = "reportId", paramType = "query", required = false),
			@ApiImplicitParam(name = "mapto", value = "下钻地图地域", dataType = "mapto", paramType = "query", required = false)})
	@Log(systemLogOperation = SystemLogOperation.REPORT_ADD_REPORT_RESOURCE, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@RequestMapping(value = "/addReportResource", method = RequestMethod.POST)
	public Object addReportResource(
			@RequestParam(value = "sids",required = false) String sids,
			@RequestParam(value = "groupName",required = false) String groupName,
			@RequestParam(value = "trslk",required = false) String trslk,
			@RequestParam(value = "chapter") String chapter,
			// 二级标题暂时不用
			@RequestParam(value = "img_data", required = false) String img_data,
			@RequestParam(value = "img_type", required = false) String img_type,
			@RequestParam(value = "reportType") String reportType,
			@RequestParam(value = "templateId") String templateId,
			@RequestParam(value = "chapterPosition") Integer chapterPosition,
			@RequestParam(value = "reportId",required = false) String reportId,
			@RequestParam(value = "mapto",required = false) String mapto) throws Exception {
		try {
			Date startDate = new Date();
			//页面栏目数据未加载出来就点“加入简报”操作，造成传空入库后，模板内前端页面无法显示
			if (StringUtil.isNotEmpty(img_type) && StringUtil.isEmpty(img_data)){
				throw new OperationException("已知是图表类模块，图表数据为空！");
			}
			if(StringUtil.isNotEmpty(groupName)){
				String[] split = groupName.split(";");
				for (int i = 0; i < split.length; i++) {
					split[i] = Const.SOURCE_GROUPNAME_CONTRAST.get(split[i]);
				}
				groupName = StringUtils.join(split, ";");
			}
			String userId = UserUtils.getUser().getId();
			Date hyStartDate = new Date();
			Object result = reportServiceNew.saveReportResource(sids,trslk, userId,
					groupName, chapter, img_data, reportType,
					templateId, img_type, chapterPosition, reportId,mapto);
			if ("fail".equals(result)) {
				throw new OperationException("请检查sid和md5tag及groupName的数量是否一致");
			} else if ("AllSimilar".equals(result)) {
				log.error("该文章已被添加到资源池中");
				return "AllSimilar";
			}
			RequestTimeLog requestTimeLog = new RequestTimeLog();
			requestTimeLog.setTabId(templateId);
			TemplateNew templateNew = templateNewRepository.findOne(templateId);
			requestTimeLog.setTabName(templateNew.getTemplateName());
			requestTimeLog.setStartHybaseTime(hyStartDate);
			requestTimeLog.setEndHybaseTime(new Date());
			requestTimeLog.setStartTime(startDate);
			requestTimeLog.setEndTime(new Date());
			//requestTimeLog.setRandomNum(randomNum);
			requestTimeLog.setOperation("舆情报告-手动报告-"+reportType);
			requestTimeLogRepository.save(requestTimeLog);
			return result;
		} catch (Exception e) {
			log.error("增加资源池失败", e);
			throw new OperationException("增加资源池失败,message" + e);
		}
	}

	/**
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("删除资源池中的资源")
	@ApiImplicitParams({
	@ApiImplicitParam(name = "resourceId", value = "需要删除的报告资源id，多选时用 ; 分隔", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "reportId", value = "报告id", dataType = "String", paramType = "query", required = false)})
	@Log(systemLogOperation = SystemLogOperation.REPORT_DELETE_REPORT_RESOURCE, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@FormatResult
	@RequestMapping(value = "/delReportResource", method = RequestMethod.POST)
	public Object deleteReportResource(
			@RequestParam(value = "resourceId") String resourceId,@RequestParam(value = "reportId",required = false) String reportId) throws OperationException {
		try {
			return reportServiceNew.delReportResource(resourceId);
		} catch (Exception e) {
			log.error("删除报告资源失败", e);
			throw new OperationException("删除报告资源失败,message" + e);
		}
	}

	/**
	 * 报告预览
	 * 日报、周报、月报，编辑页面点击预览 到这里。
	 * @author shao.guangze
	 * @return
	 * @throws Exception 
	 */
	@ApiOperation("报告预览")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "reportName", value = "报告名称", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "totalIssue", value = "总期号", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "thisIssue", value = "本期期号", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "preparationAuthors", value = "编制作者", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "preparationUnits", value = "编制单位", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "templateId", value = "报告模板id", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "reportIntro", value = "报告简介文本内容", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "statisticsTime", value = "统计时间", dataType = "String", paramType = "query" ) ,
		@ApiImplicitParam(name = "resourceDeleted", value = "是否删除资源，默认删除资源", dataType = "Integer", paramType = "query")})
	@RequestMapping(value = "/preview",method = RequestMethod.POST)
	@FormatResult
	public Object reportPreview(@RequestParam(value = "reportName")String reportName, 
			@RequestParam(value = "totalIssue")String totalIssue,
			@RequestParam(value = "thisIssue")String thisIssue, 
			@RequestParam(value = "preparationUnits")String preparationUnits,
			@RequestParam(value = "preparationAuthors")String preparationAuthors, 
			@RequestParam(value = "templateId")String templateId,
			@RequestParam(value = "reportIntro")String reportIntro,
			@RequestParam(value = "statisticsTime")String statisticsTime,
			@RequestParam(value = "resourceDeleted", required = false, defaultValue = "0")Integer resourceDeleted) throws Exception {
		return reportServiceNew.preview(reportName,totalIssue,thisIssue,preparationUnits,preparationAuthors,templateId,reportIntro,statisticsTime, resourceDeleted);
	}

	/**
	 * 生成报告, 日报、周报、月报
	 * 
	 * @author shao.guangze
	 * @return
	 * @throws Exception 
	 */
	@ApiOperation("生成报告, 日报、周报、月报")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportIntro", value = "报告简介文本内容", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "reportName", value = "报告名称", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "totalIssue", value = "总期号", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "thisIssue", value = "本期期号", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "preparationAuthors", value = "编制作者", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "preparationUnits", value = "编制单位", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "templateId", value = "报告模板id", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "statisticsTime", value = "统计时间", dataType = "String", paramType = "query" ) ,
			@ApiImplicitParam(name = "jsonImgElements", value = "json格式的图片数据", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "resourceDeleted", value = "是否删除资源，默认删除资源", dataType = "Integer", paramType = "query"),
			@ApiImplicitParam(name = "templateList", value = "报告模板", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "isUpdateTemplate", value = "是否更新对应模块", dataType = "Integer", paramType = "query", required = false),
			@ApiImplicitParam(name = "dataSummary", value = "数据统计概述文本内容", dataType = "String", paramType = "query", required = true)})
	@Log(systemLogOperation = SystemLogOperation.REPORT_CREATE, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "报告名称:${reportName}")
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	@FormatResult
	public Object createReport(
			@RequestParam(value = "reportIntro")String reportIntro,
			@RequestParam(value = "reportName",required = false)String reportName, 
			@RequestParam(value = "totalIssue",required = false)String totalIssue,
			@RequestParam(value = "thisIssue",required = false)String thisIssue, 
			@RequestParam(value = "preparationUnits",required = false)String preparationUnits,
			@RequestParam(value = "preparationAuthors",required = false)String preparationAuthors, 
			@RequestParam(value = "templateId",required = false)String templateId,
			@RequestParam(value = "statisticsTime",required = false)String statisticsTime,
			@RequestParam(value = "jsonImgElements",required = false)String jsonImgElements, 
			@RequestParam(value = "reportId", required = false)String reportId,
			@RequestParam(value = "resourceDeleted", required = false, defaultValue = "0")Integer resourceDeleted,
			@RequestParam(value = "templateList", required = false)String templateList,
			@RequestParam(value = "isUpdateTemplate", required = false)Integer isUpdateTemplate,
			@RequestParam(value = "dataSummary", required = false)String dataSummary) throws Exception {
		//如果reportId为空的话说明没走预览，这个时候需要把预览做的事情再做一遍————保存表头到report表中。
		//无论该请求是从预览过来还是直接从报告资源池过来，都需要reportIntro,因为reportIntro是存在report_data表中
		if(StringUtil.isEmpty(reportId)){
			ReportNew report = new ReportNew.Builder().withReportName(reportName)
					.withTotalIssue(totalIssue)
					.withThisIssue(thisIssue)
					.withPreparationUnits(preparationUnits)
					.withPreparationAuthors(preparationAuthors)
					.withTemplateId(templateId)
					.withStatisticsTime(statisticsTime)
					.withResourceDeleted(resourceDeleted)
					.withTemplateList(templateList).build();
			return reportServiceNew.create(reportIntro, dataSummary, jsonImgElements, report, isUpdateTemplate);
		}else{
			return reportServiceNew.create(reportIntro, dataSummary, jsonImgElements, reportId);
		}
	}

	/***
	 * 报告编辑，重新生成
	 * @return
	 */
	@RequestMapping(value = "reBuild", method = RequestMethod.POST)
	@FormatResult
	@ApiImplicitParams({
		@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "jsonImgElements", value = "json格式的图片数据", dataType = "String", paramType = "query"),
		@ApiImplicitParam(name = "reportIntro", value = "报告简介文本内容", dataType = "String", paramType = "body"),
		@ApiImplicitParam(name = "dataSummary", value = "数据统计概述文本内容", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "templateList", value = "报告模板", dataType = "String", paramType = "query", required = true)})
	public Object reBuildReport(String reportId, String jsonImgElements, String reportIntro, String dataSummary, String templateList) throws Exception {
		return reportServiceNew.reBuildReport(reportId, jsonImgElements, reportIntro, dataSummary, templateList);
	}
	/**
	 * 创建专报(分组）
	 * @author shao.guangze
	 * @return
	 * @throws Exception
	 */
	@ApiOperation("生成报告, 专报")
	@Log(systemLogOperation = SystemLogOperation.REPORT_CREATE_SPECIAL, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "templateId", value = "报告模板id", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "templateList", value = "报告模板", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "reportIntro", value = "报告简介文本内容", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "dataSummary", value = "数据统计概述", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "jsonImgElements", value = "json格式的图片数据", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "statisticsTime", value = "统计时间", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "reportName", value = "报告名称", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "thisIssue", value = "本期号", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "totalIssue", value = "总期号", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "preparationUnits", value = "编辑单位", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "preparationAuthors", value = "编辑作者", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/createSpecial", method = RequestMethod.POST)
	@FormatResult
	public Object createReport(
			@RequestParam(value = "reportId", required = false) String reportId,
			@RequestParam(value = "templateId", required = false) String templateId,
			@RequestParam(value = "templateList", required = false) String templateList,
			@RequestParam(value = "reportIntro", required = false) String reportIntro,
			@RequestParam(value = "dataSummary", required = false) String dataSummary,
			@RequestParam(value = "jsonImgElements", required = false) String jsonImgElements,
			@RequestParam(value = "statisticsTime", required = false) String statisticsTime,
			@RequestParam(value = "reportName", required = false) String reportName,
			@RequestParam(value = "thisIssue", required = false) String thisIssue,
			@RequestParam(value = "totalIssue", required = false) String totalIssue,
			@RequestParam(value = "preparationUnits", required = false) String preparationUnits,
			@RequestParam(value = "preparationAuthors", required = false) String preparationAuthors, javax.servlet.http.HttpServletRequest request) throws Exception {
		return sepcialReportService.createSepcial(reportId, templateId,templateList, jsonImgElements, reportIntro, statisticsTime, reportName, thisIssue, totalIssue, preparationUnits, preparationAuthors,dataSummary);
	}
	
	/**
	 * 下载报告
	 * 
	 * @author shao.guangze
	 * @return
	 * @throws Exception 
	 */
	@Log(systemLogOperation = SystemLogOperation.REPORT_DOWNLOAD, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiOperation("下载报告")
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> downloadReport(HttpServletRequest request, HttpServletResponse response, String reportId) throws Exception {
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
	 * 查询所有报告,这里未做模糊查询(分组)
	 * @author shao.guangze
	 * @param reportType
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ApiOperation("查询所有报告")
    @Log(systemLogOperation = SystemLogOperation.LIST_ALL_REPORT, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "报告类型：${reportType}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportType", value = "报告类型", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "searchText", value = "结果中搜索", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "groupName", value = "专报分组名称", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "pageNum", value = "分页第几页", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "pageSize", value = "分页每页显示多少条", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "time", value = "筛选时间", dataType = "String", paramType = "query", required = false)})
	@RequestMapping(value = "/listAllReport", method = RequestMethod.POST)
	@FormatResult
	public Object listAllReport(String reportType, String searchText, String groupName, Integer pageNum, Integer pageSize,String time){
		Page<ReportNew> reportNewPage = reportServiceNew.listAllReport(reportType, searchText, groupName, pageNum, pageSize,time);
		List<ReportNew> reportNewList = reportNewPage.getContent();
		for (ReportNew reportNew : reportNewList){
			String templateList = reportNew.getTemplateList();
				List<TElementNew> parseArray = JSONArray.parseArray(templateList, TElementNew.class);
				for (TElementNew tElementNew : parseArray) {
					switch (tElementNew.getChapterDetail()) {
						case OVERVIEWOFDATANew:
							tElementNew.setChapterTabName(OVERVIEWOFDATANew);
							break;
						case NEWSHOTTOP10key:
							tElementNew.setChapterTabName(NEWSHOTTOPICSkey);
							break;
						case WEIBOHOTTOP10key:
							tElementNew.setChapterTabName(NEWSHOTTOPICSkey);
							break;
						case WECHATHOTTOP10key:
							tElementNew.setChapterTabName(NEWSHOTTOPICSkey);
							break;
						case WEMEDIAkey:
							tElementNew.setChapterTabName(NEWSHOTTOPICSkey);
							break;
						case WECHATEVENTCONTEXTkey:
							tElementNew.setChapterTabName(NEWSEVENTCONTEXTkey);
							break;
						case WEIBOEVENTCONTEXTkey:
							tElementNew.setChapterTabName(NEWSEVENTCONTEXTkey);
							break;
						case NEWSEVENTCONTEXTkey:
							tElementNew.setChapterTabName(NEWSEVENTCONTEXTkey);
							break;
						case WEMEDIAEVENTCONTEXTkey:
							tElementNew.setChapterTabName(NEWSEVENTCONTEXTkey);
							break;
						case NEWSPROPAFATIONANALYSISTIMELISTkey:
							tElementNew.setChapterTabName(PROPAFATIONANALYSISkey);
							break;
						case WEMEDIAPROPAFATIONANALYSISTIMELISTkey:
							tElementNew.setChapterTabName(PROPAFATIONANALYSISkey);
							break;
						case SITUATIONACCESSMENTkey:
							tElementNew.setChapterTabName(SITUATIONACCESSMENTkey);
							break;
						case DATATRENDANALYSISkey:
							tElementNew.setChapterTabName(DATATRENDANALYSISkey);
							break;
						case DATASOURCEANALYSISkey:
							tElementNew.setChapterTabName(DATASOURCEANALYSISkey);
							break;
						case OPINIONANALYSISkey:
							tElementNew.setChapterTabName(OPINIONANALYSISkey);
							break;
						case EMOTIONANALYSISkey:
							tElementNew.setChapterTabName(EMOTIONANALYSISkey);
							break;
						case MOODSTATISTICSkey:
							tElementNew.setChapterTabName(MOODSTATISTICSkey);
							break;
						case WORDCLOUDSTATISTICSkey:
							tElementNew.setChapterTabName(WORDCLOUDSTATISTICSkey);
							break;
						case AREAkey:
							tElementNew.setChapterTabName(AREAkey);
							break;
						case ACTIVEACCOUNTkey:
							tElementNew.setChapterTabName(ACTIVEACCOUNTkey);
							break;
						case WEIBOHOTTOPICSkey:
							tElementNew.setChapterTabName(WEIBOHOTTOPICSkey);
							break;
					}
				}
		}
return reportNewPage;
//		return reportServiceNew.listAllReport(reportType, searchText, groupName, pageNum, pageSize,time);
	}
	
	/**
	 * 报告列表点击预览接口
	 * @author shao.guangze
	 * @param reportId
	 * @return
	 */
	@ApiOperation("报告列表点击预览接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/listPreview" , method = RequestMethod.GET)
	@FormatResult
	public Object listPreview(String reportId, String reportType){
		return reportServiceNew.listPreview(reportId, reportType);
	}
	
	/**
	 * 删除报告
	 * @author shao.guangze
	 * @param reportId
	 * @return
	 */
	@Log(systemLogOperation = SystemLogOperation.REPORT_DELETE_REPORT, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiOperation("删除报告")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/deleteReport" , method = RequestMethod.POST)
	@FormatResult
	public Object deleteReport(String reportId){
		return reportServiceNew.deleteReport(reportId);
	}
	/**
	 * 计算专报数据（分组）
	 * @throws Exception 
	 * 
	 */
	@ApiOperation("计算专报数据")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "reportName", value = "报告名称", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "totalIssue", value = "总期号", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "thisIssue", value = "本期期号", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "preparationAuthors", value = "编制作者", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "preparationUnits", value = "编制单位", dataType = "String", paramType = "query", required = true),
		@ApiImplicitParam(name = "searchType", value = "检索模式，简单0，专家1", dataType = "Integer", paramType = "query", required = true),
		@ApiImplicitParam(name = "keyWords", value = "搜索关键词", dataType = "String", paramType = "query", required = false),
		@ApiImplicitParam(name = "excludeWords", value = "排除管检测", dataType = "String", paramType = "query", required = false) ,
		@ApiImplicitParam(name = "keyWordsIndex", value = "关键词位置，仅标题0，标题加正文1", dataType = "Integer", paramType = "query", required = false),
		@ApiImplicitParam(name = "excludeWebs", value = "排除网站", dataType = "Integer", paramType = "query", required = false),
		@ApiImplicitParam(name = "simflag", value = "排重选择，no,netRemove,urlRemove", dataType = "query", paramType = "query", required = false),
		@ApiImplicitParam(name = "timeRange", value = "时间选择0d,24h,3d,7d,30d,", dataType = "query", paramType = "query", required = false),
		@ApiImplicitParam(name = "groupName", value = "专报分组,", dataType = "query", paramType = "query", required = true),
		@ApiImplicitParam(name = "trsl", value = "高级模式的trsl,", dataType = "query", paramType = "query", required = false)})	
	@RequestMapping(value = "/special", method = RequestMethod.POST)
	@FormatResult
	public Object  calculateSpecialReportData(@RequestParam(value = "reportName",required = true)String reportName, 
			@RequestParam(value = "totalIssue",required = true)String totalIssue,
			@RequestParam(value = "thisIssue",required = true)String thisIssue, 
			@RequestParam(value = "preparationUnits",required = true)String preparationUnits,
			@RequestParam(value = "preparationAuthors",required = true)String preparationAuthors,
			@RequestParam(value = "searchType",required = false , defaultValue = "0")Integer searchType,
			@RequestParam(value = "keyWords",required = false)String keyWords,
			@RequestParam(value = "excludeWords",required = false)String excludeWords,
			@RequestParam(value = "keyWordsIndex",required = false, defaultValue = "0")Integer keyWordsIndex,
			@RequestParam(value = "excludeWebs",required = false)String excludeWebs,
			@RequestParam(value = "simflag",required = false , defaultValue = "no")String simflag,
			@RequestParam(value = "timeRange",required = false, defaultValue = "3d")String timeRange,
			@RequestParam(value = "groupName",required = true)String groupName,
			@RequestParam(value = "trsl",required = false)String trsl,
			@RequestParam(value = "weight",required = false, defaultValue = "false")boolean weight) throws Exception{

			ReportNew report = new ReportNew.Builder().withReportName(reportName)
					.withTotalIssue(totalIssue)
					.withThisIssue(thisIssue)
					.withPreparationUnits(preparationUnits)
					.withPreparationAuthors(preparationAuthors)
					.withGroupName(groupName)
					.withReportType("专报").build();
			//TODO 不需要进行筛选参数添加
			return sepcialReportService.calculateSpecialReportData(false,report, keyWords, excludeWords, keyWordsIndex, excludeWebs, simflag, timeRange, trsl, searchType,weight,null);
	}
	
	/**
	 * 供页面轮询查看专报数据。此时用户并没有选择某个模板(分组)
	 * @author shao.guangze
	 * @param reportId
	 * @return
	 * @throws ParseException
	 */
	@ApiOperation("供页面轮询查看专报数据。此时用户并没有选择某个模板")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/specialPreview", method = RequestMethod.GET)
	@FormatResult
	public Object findSpecialReportData(@RequestParam(value = "reportId",required = false)String reportId) throws ParseException{
		List<Object> result = sepcialReportService.findSpecialData(reportId);
		return result;
	}

	/**
	 * 供页面轮询查看专报数据。此时用户并没有选择某个模板(分组)
	 * @author shao.guangze
	 * @param reportId
	 * @return
	 * @throws ParseException
	 */
	@ApiOperation("供页面轮询查看专报数据。此时用户并没有选择某个模板")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/indexTabPreview", method = RequestMethod.GET)
	@FormatResult
	public Object findIndexTabReportData(@RequestParam(value = "reportId",required = false)String reportId) throws ParseException{
		List<Object> result = sepcialReportService.findIndexTabData(reportId);
		return result;
	}


	/**
	 *
	 * @author shao.guangze
	 * @param reportId
	 * @return
	 * @throws ParseException
	 */
	@ApiOperation("供页面轮询查看专报数据。此时用户并没有选择某个模板")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/findReportById", method = RequestMethod.GET)
	@FormatResult
	public Object findReportById(@RequestParam(value = "reportId",required = true)String reportId) throws ParseException{
		Object result = sepcialReportService.findReportById(reportId);
		return result;
	}
	
	/**
	 * findSRDByTemplate
	 * findSpecialReportDataByTemplate
	 * 根据模板查出用户的数据，此时用户已经选择了某个模板（分组）
	 * @author shao.guangze
	 * @param reportId
	 * @return
	 * @throws ParseException
	 */
	@ApiOperation("根据模板查出用户的数据，此时用户已经选择了某个模板")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "templateId", value = "模板ID", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/stPreview", method = RequestMethod.GET)
	@FormatResult
	public Object findSRDByTemplate(@RequestParam(value = "reportId")String reportId,
									@RequestParam(value = "templateId")String templateId) throws ParseException{
		List<Object> result = sepcialReportService.findSRDByTemplate(reportId , templateId);
		return result;
	}
	
	/**
	 * 添加专报分组
	 * @author shao.guangze
	 * @param groupName
	 * @return
	 */
	@ApiOperation("添加专报分组")
	@Log(systemLogOperation = SystemLogOperation.REPORT_ADD_GROUP, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "groupName", value = "报告分组", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/addGroup", method = RequestMethod.POST)
	@FormatResult
	public Object addGroup(@RequestParam(value = "groupName")String groupName){
		String satus = sepcialReportService.saveSpecialReportGroup(groupName);
		return satus;
	}
	
	/**
	 * 查找该用户下所有分组(分组）
	 * @author shao.guangze
	 * @return
	 */
	@ApiOperation("查找该用户下所有分组")
	@RequestMapping(value = "/findGroup", method = RequestMethod.GET)
	@FormatResult
	public Object findAllGroup(){
		List<SpecialReportGroup> result = sepcialReportService.findAllGroup();
		return result;
	}
	
	/**
	 * 删除专报列表数据中的1条数据
	 * @author shao.guangze
	 * @param reportId
	 * @param chapterDetail
	 * @return
	 * @throws Exception
	 */
	@ApiOperation("删除专报列表数据中的1条数据")
	@Log(systemLogOperation = SystemLogOperation.REPORT_DEL_SPECIAL_RESOURCE, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "chapterDetail", value = "章节名称", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/delSpecialResource", method = RequestMethod.POST)
	@FormatResult
	public Object delSpecialResource(
			@ApiParam("专报id")@RequestParam(value = "reportId", required = false)String reportId,
			@ApiParam("模块类型")@RequestParam(value = "chapterDetail", required = true)String chapterDetail) throws Exception{
		sepcialReportService.delReportResource(reportId, chapterDetail);
		return Const.SUCCESS;
	}

	/**
	 * 日报、周报、月报编辑页面中的，清空资源按钮
	 * */
	@ApiOperation("清空所有报告资源")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "templateId", value = "报告ID", dataType = "String", paramType = "query", required = true)})
	@Log(systemLogOperation = SystemLogOperation.REPORT_DEL_ALL_RES, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@RequestMapping(value = "/delAllRes", method = RequestMethod.POST)
	@FormatResult
	public Object deleteAllReportResource(String templateId){
		reportServiceNew.delAllReportResource(templateId);
		return Const.SUCCESS;
	}

	/**
	 * 专题分析跳转专报
	 * */
	@ApiOperation("专题分析跳转专报")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialId", value = "专题Id", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/specialReportCal", method = RequestMethod.POST)
	@FormatResult
	public Object special(String specialId) throws Exception {
		Date hyStartDate = new Date();
		List<Object> list = sepcialReportService.jumptoSpecialReport(specialId);
		RequestTimeLog requestTimeLog = new RequestTimeLog();
		requestTimeLog.setTabName("专题分析报告");
		requestTimeLog.setStartHybaseTime(hyStartDate);
		requestTimeLog.setEndHybaseTime(new Date());
		requestTimeLog.setStartTime(hyStartDate);
		requestTimeLog.setEndTime(new Date());
		//requestTimeLog.setRandomNum(randomNum);
		requestTimeLog.setOperation("舆情报告-智能报告-专题分析报告");
		requestTimeLogRepository.save(requestTimeLog);
		return list.get(0);
	}

	/**
	 * 日常监测跳转日常监测报
	 * */
	@ApiOperation("日常监测跳转日常监测报")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "日常监测映射id", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/indexTabReportCal", method = RequestMethod.POST)
	@FormatResult
	public Object indexTabReportCal(String id) throws Exception {
		Date hyStartDate = new Date();
		List<Object> list =sepcialReportService.jumptoIndexTabReport(id);
		RequestTimeLog requestTimeLog = new RequestTimeLog();
		requestTimeLog.setTabName("日常监测报告");
		requestTimeLog.setStartHybaseTime(hyStartDate);
		requestTimeLog.setEndHybaseTime(new Date());
		requestTimeLog.setStartTime(hyStartDate);
		requestTimeLog.setEndTime(new Date());
		//requestTimeLog.setRandomNum(randomNum);
		requestTimeLog.setOperation("舆情报告-智能报告-日常监测报告");
		requestTimeLogRepository.save(requestTimeLog);
		return list.get(0);
	}

	/**
	 * 删除专报分组
	 * */
	@ApiOperation("删除专报分组")
	@Log(systemLogOperation = SystemLogOperation.REPORT_DEL_SPECIAL_GROUP, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "groupName", value = "分组名称", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/delSpecialGroup", method = RequestMethod.POST)
	@FormatResult
	public Object delSpecialReportGroup(String groupName) {
		if(StringUtil.isEmpty(groupName)){
			return "not-null";
		}
		sepcialReportService.delSpecialReportGroup(groupName);
		return Const.SUCCESS;
	}

	/**
	 * 编辑专报分组名称
	 * */
	@ApiOperation("编辑专报分组名称")
	@Log(systemLogOperation = SystemLogOperation.REPORT_EDIT_SPECIAL_GROUP, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "originGroup", value = "修改前专报名称", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "currentGroup", value = "修改后专报名称", dataType = "String", paramType = "query", required = true)})
	@RequestMapping(value = "/editSpecialGroup", method = RequestMethod.POST)
	@FormatResult
	public Object editSpecialReportGroup(String originGroup, String currentGroup) {
		return sepcialReportService.editSpecialReportGroup(originGroup, currentGroup);
	}

	/***
	 * 舆情报告列表页查找资源
	 * 默认url排重，混合列表
	 * @return
	 */
	@RequestMapping(value =
			"/searchResources", method = RequestMethod.GET)
	@FormatResult
	public Object searchResources(String keyWords, String statisticsTime, Integer pageNum, Integer pageSize) throws Exception {
		return reportServiceNew.searchResources(keyWords, statisticsTime, pageNum, pageSize);
	}
	
	@ApiOperation("报告信息列表中文章顺序拖拽")
	@FormatResult
	@GetMapping("/changeDocPosition")
	public Object changeDocPosition(
		@ApiParam("原位置")@RequestParam(value = "oldPosition", required = false)Integer docPosition,
		@ApiParam("新位置")@RequestParam(value = "newPosition", required = false)Integer newPosition,
		@ApiParam("模块类型")@RequestParam(value = "chapter", required = false)String chapter,
		@ApiParam("模板id")@RequestParam(value = "templateId", required = false)String templateId,
		@ApiParam("制作/完成（预览）")@RequestParam(value = "resourceStatus", required = false)int resourceStatus,
		@ApiParam("专报中拖拽才传这个参数")@RequestParam(value = "reportDataId", required = false)String reportDataId,
		@ApiParam("该记录在数据库中的id")@RequestParam(value = "id", required = false)String id,
		@ApiParam("报告id")@RequestParam(value = "reportId", required = false)String reportId
			){
		//位置信息未别改变
		if(docPosition == newPosition){
			return null;
		}
		//处理位置信息
		return reportServiceNew.changePosition(docPosition, newPosition, chapter, templateId, resourceStatus, id, reportDataId,reportId);
	}

	/**
	 * 生成专报时编辑图表模块类下面的文字
	 * @param reportId    报告名称
	 * @param chapterDetail 模块对应不变属性值
	 * @param resourceId    章节中资源ID
	 * @return
	 * @throws Exception
	 */
	@ApiOperation("生成专报时编辑图表模块类下面的文字")
	@Log(systemLogOperation = SystemLogOperation.REPORT_UPDATE_SPECIAL_RESOURCE, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "reportId", value = "报告ID", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "chapterDetail", value = "模块对应不变属性值", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "resourceId", value = "章节中资源ID", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "imgComment", value = "章节中资源可编辑內容", dataType = "String", paramType = "query", required = false)})
	@RequestMapping(value = "/updateChartData", method = RequestMethod.POST)
	@FormatResult
	public Object updateChartData(
			@ApiParam("专报id")@RequestParam(value = "reportId", required = true)String reportId,
			@ApiParam("模块类型")@RequestParam(value = "chapterDetail", required = true)String chapterDetail,
			@ApiParam("该条资源的id（不是sid）")@RequestParam(value = "resourceId", required = false)String resourceId,
			@ApiParam("修改内容")@RequestParam(value = "imgComment", required = false)String imgComment) throws Exception{
		sepcialReportService.updateChartData(reportId, chapterDetail, resourceId,imgComment);
		return Const.SUCCESS;
	}

	/**
	 * 生成日报、周报 、月报时编辑数据概述模块
	 * @param resourceId
	 * @param imgComment
	 * @return
	 * @throws Exception
	 */
	@ApiOperation("生成日报、周报 、月报时编辑数据概述模块")
	@Log(systemLogOperation = SystemLogOperation.REPORT_UPDATE_SPECIAL_OVER_VIEW, systemLogType = SystemLogType.REPORT, systemLogOperationPosition = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "templateId", value = "模板id", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "resourceId", value = "章节中资源ID", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "imgComment", value = "章节中资源可编辑內容", dataType = "String", paramType = "query", required = false)})
	@RequestMapping(value = "/updateOverView", method = RequestMethod.POST)
	@FormatResult
	public Object updateOverView(
			@ApiParam("模板id")@RequestParam(value = "templateId", required = false)String templateId,
			@ApiParam("报告类型")@RequestParam(value = "reportType", required = false)String reportType,
			@ApiParam("该条资源的id（不是sid）")@RequestParam(value = "resourceId", required = false)String resourceId,
			@ApiParam("修改内容")@RequestParam(value = "imgComment", required = false)String imgComment) throws Exception{
		if (StringUtil.isNotEmpty(resourceId)){
			reportServiceNew.updateReportResource(resourceId,imgComment);
		}else if (StringUtil.isNotEmpty(templateId)){
			reportServiceNew.saveOverView( UserUtils.getUser().getId(), Chapter.Statistics_Summarize.toString(), imgComment, reportType, templateId);
		}
		return Const.SUCCESS;
	}
	/**
	 * 专题报 默认模板改造  导致历史报告使用默认模板时 无法预览
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/changeHistoryForSpecialReport", method = RequestMethod.POST)
	@FormatResult
	public Object changeHistoryForSpecialReport() throws Exception{
		//查出所有专报
		List<ReportNew> allSpecialReports = sepcialReportService.findAllSpecialReports();
		if (ObjectUtil.isNotEmpty(allSpecialReports)){
			//遍历，根据所属用户id或用户分组id，查找当下默认模板
			for (ReportNew specialReport : allSpecialReports) {
				boolean already = false;
				String userId = specialReport.getUserId();
				String subGroupId = specialReport.getSubGroupId();
				if (StringUtil.isNotEmpty(subGroupId)){
					//查当前分组下的默认专题报
					List<TemplateNew> moRenSpecialBySubGroupId = sepcialReportService.findMoRenSpecialBySubGroupId(subGroupId);
					if (ObjectUtil.isNotEmpty(moRenSpecialBySubGroupId)){
						for (TemplateNew templateNew : moRenSpecialBySubGroupId) {
							//将 report 的tenplateList重新赋值
							if (StringUtils.equals(templateNew.getId(),specialReport.getTemplateId())){
								specialReport.setTemplateList("[{\"chapterDetail\":\"REPORTINTRO\",\"chapterName\":\"报告简介\",\"chapterPosition\":0,\"chapterType\":\"SingleResource\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"OVERVIEWOFDATA\",\"chapterName\":\"数据统计概述\",\"chapterPosition\":1,\"chapterType\":\"SingleResource\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"NEWSHOTTOP10\",\"chapterName\":\"新闻热点TOP10\",\"chapterPosition\":2,\"chapterType\":\"ListResources\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WEIBOHOTTOP10\",\"chapterName\":\"微博热点TOP10\",\"chapterPosition\":3,\"chapterType\":\"ListResources\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WECHATHOTTOP10\",\"chapterName\":\"微信热点TOP10\",\"chapterPosition\":4,\"chapterType\":\"ListResources\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"DATATRENDANALYSIS\",\"chapterName\":\"数据趋势分析\",\"chapterPosition\":5,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"DATASOURCEANALYSIS\",\"chapterName\":\"数据来源对比\",\"chapterPosition\":6,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WEBSITESOURCETOP10\",\"chapterName\":\"网站来源TOP10\",\"chapterPosition\":7,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WEIBOACTIVETOP10\",\"chapterName\":\"微博活跃用户TOP10\",\"chapterPosition\":8,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WECHATACTIVETOP10\",\"chapterName\":\"微信活跃用户TOP10\",\"chapterPosition\":9,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"AREA\",\"chapterName\":\"全国地域分布\",\"chapterPosition\":10,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"EMOTIONANALYSIS\",\"chapterName\":\"情感分析\",\"chapterPosition\":11,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1}]");
							}
							already = true;
							break;
						}
					}

				}
					if (!already){
						List<TemplateNew> moRenSpecialByUserId = sepcialReportService.findMoRenSpecialByUserId(userId);
						if (ObjectUtil.isNotEmpty(moRenSpecialByUserId)){
							for (TemplateNew templateNew : moRenSpecialByUserId) {
								//将 report 的tenplateList重新赋值
								if (StringUtils.equals(templateNew.getId(),specialReport.getTemplateId())){
									specialReport.setTemplateList("[{\"chapterDetail\":\"REPORTINTRO\",\"chapterName\":\"报告简介\",\"chapterPosition\":0,\"chapterType\":\"SingleResource\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"OVERVIEWOFDATA\",\"chapterName\":\"数据统计概述\",\"chapterPosition\":1,\"chapterType\":\"SingleResource\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"NEWSHOTTOP10\",\"chapterName\":\"新闻热点TOP10\",\"chapterPosition\":2,\"chapterType\":\"ListResources\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WEIBOHOTTOP10\",\"chapterName\":\"微博热点TOP10\",\"chapterPosition\":3,\"chapterType\":\"ListResources\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WECHATHOTTOP10\",\"chapterName\":\"微信热点TOP10\",\"chapterPosition\":4,\"chapterType\":\"ListResources\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"DATATRENDANALYSIS\",\"chapterName\":\"数据趋势分析\",\"chapterPosition\":5,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"DATASOURCEANALYSIS\",\"chapterName\":\"数据来源对比\",\"chapterPosition\":6,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WEBSITESOURCETOP10\",\"chapterName\":\"网站来源TOP10\",\"chapterPosition\":7,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WEIBOACTIVETOP10\",\"chapterName\":\"微博活跃用户TOP10\",\"chapterPosition\":8,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"WECHATACTIVETOP10\",\"chapterName\":\"微信活跃用户TOP10\",\"chapterPosition\":9,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"AREA\",\"chapterName\":\"全国地域分布\",\"chapterPosition\":10,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1},{\"chapterDetail\":\"EMOTIONANALYSIS\",\"chapterName\":\"情感分析\",\"chapterPosition\":11,\"chapterType\":\"chart\",\"extraChapterFlag\":0,\"selected\":1}]");
								}
								already = true;
								break;
							}
						}
					}
					if (already){
						sepcialReportService.updateSpecialReport(specialReport);
					}else {
						System.err.println("报告id："+specialReport.getId());
					}

			}
		}
		return Const.SUCCESS;
	}
}
