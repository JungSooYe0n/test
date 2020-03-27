package com.trs.netInsight.widget.report.controller;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Criterion.MatchMode;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.util.favourites.FavouritesUtil;
import com.trs.netInsight.widget.report.entity.*;
import com.trs.netInsight.widget.report.entity.repository.MaterialLibraryRepository;
import com.trs.netInsight.widget.report.entity.repository.ReportMaterialRepository;
import com.trs.netInsight.widget.report.service.IReportService;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * Created by Xiaoying on 2017年12月13日
 */
@Slf4j
@RestController
@RequestMapping("/report")
@Api(description = "舆情报告模块接口")
public class ReportController {

	@Autowired
	private IReportService reportService;

	@Autowired
	private MaterialLibraryRepository materialLibraryRepository;

	@Autowired
	private IInfoListService infoListService;

	@Autowired
	private ReportMaterialRepository reportMaterialRepository;

	/**
	 * 获取该用户下我的收藏列表
	 * 
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页大小
	 * @param request
	 * @return list<Favourites>
	 * @throws TRSException
	 */
	@ApiOperation("我的收藏列表")
	// @SystemControllerLog(module = "舆情报告", description = "查看 我的收藏列表")
	@FormatResult
	@RequestMapping(value = "/dearList", method = RequestMethod.GET)
	public Object favouritesList(
			@ApiParam("页数 从0开始") @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@ApiParam("一页几条") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
			@ApiParam("来源") @RequestParam(value = "groupName", required = false) String groupName,
			@ApiParam("在结果中搜索") @RequestParam(value = "keywords", required = false) String keywords,
			@ApiParam("在结果中搜索de范围") @RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("是否导出 主要是全部收藏导出时使用") @RequestParam(value = "isExport", defaultValue = "false", required = false) Boolean isExport,
			HttpServletRequest request) throws TRSException {
		try {
			User loginUser = UserUtils.getUser();
			List<String> source = new ArrayList<>();
			if("ALL".equals(groupName)){
				groupName = Const.ALL_GROUP_COLLECT;
			}
			String[] groupNameArray = groupName.split(";");

			for(String str : groupNameArray){
				if(Const.FAVOURITES_SOURCE_GROUPNAME.containsKey(str)){
					source.add(Const.FAVOURITES_SOURCE_GROUPNAME.get(str));
				}else{
					throw new OperationException("所传参数：groupName值有误，为:"+str+"，获取我的收藏列表出错");
				}
			}

			return reportService.getFavouritesByCondition(loginUser, pageNo, pageSize,
					source, keywords,fuzzyValueScope, invitationCard, forwarPrimary);
			/*
			历史方法，但是没有筛选功能
			return reportService.getAllFavourites(loginUser, pageNo, pageSize,
					groupName, keywords, invitationCard, forwarPrimary,isExport);
			 */
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			log.error("获取我的收藏列表出错", e);
			throw new OperationException("获取我的收藏列表出错" + e);
		}

	}

	/**
	 * 添加到我的收藏
	 * 
	 * @param sids
	 *            需要增加到收藏的sid集合，用分号（;）隔开
	 * @param md5tag
	 *            需要增加到收藏的md5集合，用分号（;）隔开
	 * @param request
	 * @return
	 * @throws OperationException
	 *             createdbyxiaoying
	 */
	@ApiOperation("添加到我的收藏")
	// @SystemControllerLog(module = "舆情报告", description = "新增收藏")
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "sids", value = "需要增加到收藏的sid集合，用分号（;）隔开", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "urltime", value = "发布时间  ;隔开", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "md5tag", value = " 需要增加到收藏的md5集合，用分号（;）隔开", dataType = "String", paramType = "query", required = true),
			@ApiImplicitParam(name = "groupName", value = "来源 查库用 传多个的时候用（;）隔开", dataType = "String", paramType = "query", required = true) })
	@RequestMapping(value = "/addFavourites", method = RequestMethod.POST)
	public Object addFavourites(@RequestParam(value = "sids") String sids,
			@RequestParam(value = "urltime", required = false) String urltime,
			@ApiParam("文章MD5值，历史原因暂保留这个字段，可不传")@RequestParam(value = "md5tag", required = false) String md5tag,
			@RequestParam(value = "groupName") String groupName,
			HttpServletRequest request) throws OperationException {
		try {

			String[] groupNameArray = groupName.split(";");
			String[] sidArray = sids.split(";");
			if(groupNameArray.length != sidArray.length){
				return new OperationException("所传sid和groupName的个数不相同");
			}

			for(int i = 0 ;i < groupNameArray.length ;i++ ){
				if(Const.FAVOURITES_SOURCE_GROUPNAME.containsKey(groupNameArray[i])){
					groupNameArray[i] = Const.FAVOURITES_SOURCE_GROUPNAME.get(groupNameArray[i]);
				}else{
					return new OperationException("所传参数：groupName值有误，获取我的收藏列表出错");
				}
			}
			groupName = StringUtils.join(groupNameArray, ";");
			String userId = UserUtils.getUser().getId();
			String subGroupId = UserUtils.getUser().getSubGroupId();
			String result = reportService.saveFavourites(sids, userId,subGroupId,md5tag,
					groupName,urltime);
			return result;
		} catch (Exception e) {
			log.error("增加收藏失败", e);
			throw new OperationException("增加收藏失败,message" + e);
		}
	}
	@ApiOperation("更改历史收藏")
	@RequestMapping(value = "/changeHistoryFav",method = RequestMethod.GET)
	public Object changeFav(){
		String favourites = reportService.changeHistoryFavourites();
		System.err.println("结束了~~~~~~~~~~~~~~~~~~~~~~");
		return favourites;

	}

	@ApiOperation("更改历史收藏的groupName  - > 数据来源统一")
	@RequestMapping(value = "/changeHistoryFavGroupName",method = RequestMethod.GET)
	public Object changeFavGroupName(){
		String favourites = reportService.changeHistoryFavouritesGroupName();
		System.err.println("更改历史收藏的groupName  结束了~~~~~~~~~~~~~~~~~~~~~~");
		return favourites;
	}

	/**
	 * @param sids
	 *            需要删除的sid集合，用分号（;）隔开
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("取消收藏")
	// @SystemControllerLog(module = "舆情报告", description = "取消收藏")
	@FormatResult
	@RequestMapping(value = "delFavourites", method = RequestMethod.GET)
	public Object deleteFavourites(@RequestParam(value = "sids") String sids,
			HttpServletRequest request) throws OperationException {
		try {
			String userId = UserUtils.getUser().getId();

			return reportService.delFavourites(sids, userId);
		} catch (Exception e) {
			log.error("删除收藏失败", e);
			throw new OperationException("删除收藏失败,message" + e);
		}
	}
	/**
	 * 新增素材库
	 * 
	 * @param libraryName
	 *            素材库名
	 * @param source
	 *            素材库来源
	 * @param mode
	 *            检索模式
	 * @param allKeyword
	 *            全部关键字
	 * @param anyKeyword
	 *            任意关键字
	 * @param excludeKeyword
	 *            排除词
	 * @param keywordsLocation
	 *            关键字位置
	 * @param BeginTime
	 *            检索开始时间
	 * @param EndTime
	 *            检索结束时间
	 * @param expression
	 *            专家模式检索表达式
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("新增素材库")
	// @SystemControllerLog(module = "舆情报告", description = "新建 素材库")
	@FormatResult
	@RequestMapping(value = "addMatlib", method = RequestMethod.POST)
	public Object addMaterialLibrary(
			@ApiParam("素材库名称") @RequestParam("libraryName") String libraryName,
			@ApiParam("来源 0:专项监测，1:舆情报告") @RequestParam(value = "source", defaultValue = "1") int source,
			@ApiParam("来源 国内新闻 微博...") @RequestParam(value = "groupName", required = false) String groupName,
			@ApiParam("模式：[普通:0,专家:1]") @RequestParam("mode") int mode,
			@ApiParam("全部关键词") @RequestParam(value = "allKeyword", required = false) String allKeyword,
			@ApiParam("任意关键词") @RequestParam(value = "anyKeyword", required = false) String anyKeyword,
			@ApiParam("排除词") @RequestParam(value = "excludeKeyword", required = false) String excludeKeyword,
			@ApiParam("关键词位置") @RequestParam(value = "keywordsLocation", defaultValue = "TITLE") String keywordsLocation,
			@ApiParam("开始时间[yyyy-MM-dd HH:mm:ss]") @RequestParam(value = "searchBeginTime") String BeginTime,
			@ApiParam("结束时间[yyyy-MM-dd HH:mm:ss]") @RequestParam(value = "searchEndTime") String EndTime,
			@ApiParam("专家模式下的传统检索表达式") @RequestParam(value = "expression", required = false) String expression,
			@ApiParam("专家模式下的微博检索表达式") @RequestParam(value = "weiboExpression", required = false) String weiboExpression,
			@ApiParam("专家模式下的微信检索表达式") @RequestParam(value = "weixinExpression", required = false) String weixinExpression,
			HttpServletRequest request) throws OperationException {
		try {
			// 专家模式
			if (1 == mode) {
				if (StringUtil.isEmpty(expression)
						&& StringUtil.isEmpty(weiboExpression)
						&& StringUtil.isEmpty(weixinExpression)) {
					throw new OperationException("创建监测方案失败");
				}
			} else if (2 == mode) {
				// 普通模式
				if (StringUtil.isEmpty(allKeyword)
						&& StringUtil.isEmpty(anyKeyword)) {
					throw new OperationException("创建监测方案失败");
				}
			}
			Date searchBeginTime = DateUtil.stringToDate(BeginTime,
					DateUtil.DEFAULT_TIME_PATTERN);
			if ("至今".equals(EndTime)) {
				EndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm")
						.format(new Date());
			}
			Date searchEndTime = DateUtil.stringToDate(EndTime,
					DateUtil.DEFAULT_TIME_PATTERN);
			SearchScope scope = SearchScope.valueOf(keywordsLocation);
			MaterialLibrary materialLibrary = new MaterialLibrary(libraryName,
					source, groupName, mode, allKeyword, anyKeyword,
					excludeKeyword, scope, searchBeginTime, searchEndTime,
					expression, weiboExpression, weixinExpression);
			return reportService.saveMaterialLibrary(materialLibrary);
		} catch (Exception e) {
			log.error("新增素材库失败", e);
			throw new OperationException("新增素材库失败,message" + e);
		}
	}

	/**
	 * 获取该用户下的所有素材库 我的收藏列表中加入素材库 素材库名称的下拉列表和素材库列表
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("获取该用户下的所有素材库")
	// @SystemControllerLog(module = "舆情报告", description = "查看 素材库列表")
	@FormatResult
	@RequestMapping(value = "matlibList", method = RequestMethod.GET)
	public Object materialLibraryList(
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
			HttpServletRequest request) throws OperationException {
		try {
			String userId = UserUtils.getUser().getId();
			return reportService.getUserLibrary(userId, pageNo, pageSize);
		} catch (Exception e) {
			log.error("查询所有素材库失败", e);
			throw new OperationException("查询所有素材库失败,message" + e);
		}

	}

	/**
	 * 获取单个素材库信息
	 * 
	 * @param libraryId
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("获取单个素材库信息")
	// @SystemControllerLog(module = "舆情报告", description = "查看单个素材库")
	@RequestMapping(value = "theLib", method = RequestMethod.GET)
	public Object theLibrary(@RequestParam(value = "libraryId") String libraryId)
			throws TRSException {
		try {
			return reportService.getOneLibrary(libraryId);
		} catch (Exception e) {
			throw new OperationException(String.format(
					"获取[id=%s]素材库详情失败,message: %s", libraryId, e));
		}
	}

	/**
	 * @param libraryId
	 *            素材库id
	 * @param libraryName
	 *            素材库名
	 * @param source
	 *            素材库来源 0:专项监测，1:舆情报告
	 * @param mode
	 *            检索模式
	 * @param allKeyword
	 *            全部关键字
	 * @param anyKeyword
	 *            任意关键字
	 * @param excludeKeyword
	 *            排除词
	 * @param keywordsLocation
	 *            关键字位置
	 * @param BeginTime
	 *            检索开始时间
	 * @param EndTime
	 *            检索结束时间
	 * @param expression
	 *            专家模式检索表达式
	 * @param request
	 * @return
	 * @throws OperationException
	 *             Createdy xiaoying
	 */
	@ApiOperation("更新素材库")
	// @SystemControllerLog(module = "舆情报告", description = "更新 素材库")
	@FormatResult
	@RequestMapping(value = "updateLib", method = RequestMethod.POST)
	public Object updateTheLibrary(
			@ApiParam("素材Id") @RequestParam(value = "libraryId") String libraryId,
			@ApiParam("素材库名字") @RequestParam("libraryName") String libraryName,
			@ApiParam("素材库来源  0:专项监测，1:舆情报告") @RequestParam(value = "source", defaultValue = "1") int source,
			@ApiParam("来源 国内新闻 微博 微信...") @RequestParam(value = "groupName", required = false) String groupName,
			@ApiParam("模式：[普通:0,专家:1]") @RequestParam("mode") int mode,
			@ApiParam("全部关键字") @RequestParam(value = "allKeyword", required = false) String allKeyword,
			@ApiParam("任意关键字") @RequestParam(value = "anyKeyword", required = false) String anyKeyword,
			@ApiParam("排除词") @RequestParam(value = "excludeKeyword", required = false) String excludeKeyword,
			@ApiParam("关键词搜索位置 TITLE...") @RequestParam(value = "keywordsLocation", required = false) String keywordsLocation,
			@ApiParam("开始时间") @RequestParam(value = "searchBeginTime", required = false) String BeginTime,
			@ApiParam("结束时间") @RequestParam(value = "searchEndTime", required = false) String EndTime,
			@ApiParam("专家模式传统库检索表达式") @RequestParam(value = "expression", required = false) String expression,
			@ApiParam("专家模式下的微博检索表达式") @RequestParam(value = "weiboExpression", required = false) String weiboExpression,
			@ApiParam("专家模式下的微信检索表达式") @RequestParam(value = "weixinExpression", required = false) String weixinExpression,
			HttpServletRequest request) throws OperationException {
		try {
			Date searchBeginTime = DateUtil.stringToDate(BeginTime,
					DateUtil.DEFAULT_TIME_PATTERN);
			if ("至今".equals(EndTime)) {
				EndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm")
						.format(new Date());
			}
			Date searchEndTime = DateUtil.stringToDate(EndTime,
					DateUtil.DEFAULT_TIME_PATTERN);
			SearchScope scope = null;
			if (0 == mode) {
				scope = SearchScope.valueOf(keywordsLocation);
			}
			MaterialLibrary materialLibrary = reportService
					.getOneLibrary(libraryId);
			materialLibrary.setAllKeyword(allKeyword);
			materialLibrary.setAnyKeyword(anyKeyword);
			materialLibrary.setExcludeKeyword(excludeKeyword);
			materialLibrary.setExpression(expression);
			materialLibrary.setWeiboExpression(weiboExpression);
			materialLibrary.setWeixinExpression(weixinExpression);
			materialLibrary.setKeywordsLocation(scope);
			materialLibrary.setLibraryName(libraryName);
			materialLibrary.setMode(mode);
			materialLibrary.setSearchBeginTime(searchBeginTime);
			materialLibrary.setSearchEndTime(searchEndTime);
			materialLibrary.setSource(source);
			materialLibrary.setGroupName(groupName);
			return reportService.saveMaterialLibrary(materialLibrary);
		} catch (Exception e) {
			log.error("修改素材库失败", e);
			throw new OperationException("修改素材库失败,message" + e);
		}
	}

	/**
	 * 延长素材库保留天数
	 * 
	 * @param libraryId
	 * @param incrDay
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("延长素材库保留天数")
	// @SystemControllerLog(module = "舆情报告", description = "修改 素材库保留天数")
	@FormatResult
	@RequestMapping(value = "incrDay", method = RequestMethod.POST)
	public Object prolongDays(
			@RequestParam(value = "libraryId") String libraryId,
			@RequestParam(value = "increaseDay") int incrDay)
			throws OperationException {
		try {
			MaterialLibrary materialLibrary = reportService
					.getOneLibrary(libraryId);
			materialLibrary.setRemainDays(materialLibrary.getRemainDays()
					+ incrDay);
			return reportService.saveMaterialLibrary(materialLibrary);
		} catch (Exception e) {
			log.error("延长素材库保留天数失败", e);
			throw new OperationException("延长素材库保留天数失败,message" + e);
		}
	}

	/**
	 * 素材库预览
	 * 
	 * @param libraryName
	 *            素材库名
	 * @param source
	 *            0:专项监测，1:舆情报告 素材库来源 专项检测 舆情报告
	 * @param mode
	 *            检索模式
	 * @param allKeyword
	 *            全部关键字
	 * @param anyKeyword
	 *            任意关键字
	 * @param excludeKeyword
	 *            排除词
	 * @param keywordsLocation
	 *            关键字位置 检索开始时间 检索结束时间
	 * @param expression
	 *            专家模式检索表达式
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("素材库预览")
	// @SystemControllerLog(module = "舆情报告", description = "预览 素材库")
	@FormatResult
	@RequestMapping(value = "preview", method = RequestMethod.POST)
	public Object preview(
			@ApiParam("素材库名") @RequestParam("libraryName") String libraryName,
			@ApiParam("素材库来源 0:专项监测，1:舆情报告") @RequestParam(value = "source", defaultValue = "1") int source,
			@ApiParam("检索模式 0：普通模式，1：专家模式") @RequestParam(value = "mode") int mode,
			@ApiParam("来源 国内新闻 微博 微信...") @RequestParam(value = "groupName", required = false) String groupName,
			@ApiParam("全部关键字") @RequestParam(value = "allKeyword", required = false) String allKeyword,
			@ApiParam("任意关键字") @RequestParam(value = "anyKeyword", required = false) String anyKeyword,
			@ApiParam("排除词") @RequestParam(value = "excludeKeyword", required = false) String excludeKeyword,
			@ApiParam("关键字位置") @RequestParam(value = "keywordsLocation", defaultValue = "TITLE") String keywordsLocation,
			@ApiParam("检索开始时间") @RequestParam(value = "searchBeginTime") String beginTime,
			@ApiParam("检索结束时间") @RequestParam(value = "searchEndTime") String endTime,
			@ApiParam("专家模式检索表达式") @RequestParam(value = "expression", required = false) String expression,
			@ApiParam("专家模式下的微博检索表达式") @RequestParam(value = "weiboExpression", required = false) String weiboExpression,
			@ApiParam("专家模式下的微信检索表达式") @RequestParam(value = "weixinExpression", required = false) String weixinExpression)
			throws OperationException {
		try {
			Date searchBeginTime = DateUtil.stringToDate(beginTime,
					DateUtil.DEFAULT_TIME_PATTERN);
			Date searchEndTime = DateUtil.stringToDate(endTime,
					DateUtil.DEFAULT_TIME_PATTERN);
			SearchScope scope = SearchScope.valueOf(keywordsLocation);
			MaterialLibrary materialLibrary = new MaterialLibrary(libraryName,
					source, groupName, mode, allKeyword, anyKeyword,
					excludeKeyword, scope, searchBeginTime, searchEndTime,
					expression, weiboExpression, weixinExpression);
			// 只预览第一个来源
			return reportService.preview(materialLibrary, groupName);
		} catch (Exception e) {
			log.error("预览素材库数据失败", e);
			throw new OperationException("预览素材库数据失败,message" + e);
		}

	}

	/**
	 * 选择模板，获取模板列表(从编辑素材过来)
	 * 
	 * @param libraryId
	 * @param request
	 * @return
	 */
	@ApiOperation("选择模板，获取模板列表(从编辑素材过来)")
	// @SystemControllerLog(module = "舆情报告", description = "选择 模板")
	@FormatResult
	@RequestMapping(value = "choiceTemp", method = RequestMethod.GET)
	public Object choiceTemplate(
			@RequestParam(value = "libraryId") String libraryId,
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
			HttpServletRequest request) throws OperationException {
		try {
			String userId = UserUtils.getUser().getId();
			return reportService.getTemplateList(userId, libraryId, pageNo,
					pageSize);
		} catch (Exception e) {
			log.error("获取模板列表失败(从编辑素材至模板选择)", e);
			throw new OperationException("获取模板列表失败，(从编辑素材至模板选择)/n" + e);
		}
	}

	/**
	 * 直接查看当前用户建的所有模板(直接点击我的模板进入)
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("直接查看当前用户建的所有模板(直接点击我的模板进入)")
	// @SystemControllerLog(module = "舆情报告", description = "查看 模板列表")
	@FormatResult
	@RequestMapping(value = "allTemp", method = RequestMethod.GET)
	public Object choiceTemplate(
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
			HttpServletRequest request) throws OperationException {
		try {
			// 不知道要不要改 联调看看
			String userId = request.getAttribute(Const.USER_ID).toString();
			return reportService.getTemplateNoLib(userId, pageNo, pageSize);
		} catch (Exception e) {
			log.error("获取模板列表失败(直接查看已有模板)", e);
			throw new OperationException("获取模板列表失败(直接查看已有模板)/n" + e);
		}
	}

	/**
	 * 将我的收藏中的素材添加到或者移除素材库(可以批量 SID用;隔开)
	 * 
	 * @param sid
	 * @param libraryId
	 * @param operate
	 *            0:删除 1：新增
	 * @param request
	 * @return
	 */
	@ApiOperation("将我的收藏中的素材添加到或者移除素材库(可以批量 SID用;隔开)")
	// @SystemControllerLog(module = "舆情报告", description = "收藏->素材库")
	@FormatResult
	@RequestMapping(value = "operateTolib", method = RequestMethod.POST)
	public Object favoriteToLib(
			@ApiParam("sid或者mid或者hkey") @RequestParam(value = "sid") String sid,
			@ApiParam("素材id") @RequestParam(value = "libraryId") String libraryId,
			@ApiParam("0:删除 1：新增") @RequestParam(value = "operate") String operate,
			@ApiParam("来源") @RequestParam(value = "groupName") String groupName,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.saveFavMaterial(sid, libraryId, operate,
					groupName);
		} catch (Exception e) {
			log.error("处理我的收藏和素材库的关系出错", e);
			throw new OperationException("处理我的收藏和素材库的关系出错,message" + e);
		}
	}

	/**
	 * 舆情报告信息列表
	 * 
	 * @param libraryId
	 * @param pageSize
	 * @param source
	 * @param timeRange
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param keywords
	 * @return
	 * @throws TRSException
	 *             CreatedByXiaoying
	 */
	@SuppressWarnings("unused")
	// @EnableRedis
	@FormatResult
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public Object dataList(
			@ApiParam("素材库id") @RequestParam("libraryId") String libraryId,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
			@RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(value = "groupName", defaultValue = "ALL") String source,
			@RequestParam(value = "time", defaultValue = "0d") String timeRange,
			@RequestParam(value = "area", defaultValue = "ALL") String area,
			@RequestParam(value = "industry", defaultValue = "ALL") String industry,
			@RequestParam(value = "emotion", defaultValue = "ALL") String emotion,
			@RequestParam(value = "sort", defaultValue = "default") String sort,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@RequestParam(value = "keyword", required = false) String keywords,
			HttpServletRequest request) throws TRSException {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		try {
			MaterialLibrary materialLibrary = materialLibraryRepository
					.findOne(libraryId);
			QueryBuilder builder = null;
			QueryBuilder countBuilder = null;
			if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
				builder = materialLibrary.toNoTimeBuilderWeiXin(pageNo,
						pageSize);
				countBuilder = materialLibrary.toNoPagedAndTimeBuilderWeiXin();
			} else if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
				builder = materialLibrary
						.toNoTimeBuilderWeiBo(pageNo, pageSize);
				countBuilder = materialLibrary.toNoPagedAndTimeBuilderWeiBo();
			} else if (Const.MEDIA_TYPE_NEWS.contains(source)
					|| "ALL".equals(source)) {
				builder = materialLibrary.toNoTimeBuilder(pageNo, pageSize);
				countBuilder = materialLibrary.toNoPagedAndTimeBuilder();
			}

			// 把添加到素材库的放进来
			// 过后要按照时间排列 sidList同我的收藏
			Criteria<ReportMaterial> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("libraryId", libraryId));
			criteria.add(Restrictions.eq("status", 1));
			criteria.add(Restrictions.like("groupName", source,
					MatchMode.ANYWHERE));
			// 查询未删除的 因为这个表里边还有通过我的收藏列表添加到素材库的
			Page<ReportMaterial> findAll = reportMaterialRepository.findAll(
					criteria, new PageRequest(pageNo, pageSize));
			if (findAll != null) {
				List<String> idList = new ArrayList<>();
				for (ReportMaterial reportMaterial : findAll) {
					idList.add(reportMaterial.getSid());
				}
				// 构建传统检索表达式
				String buildSql = FavouritesUtil.buildSql(idList);
				if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
					// 构建微博检索表达式
					buildSql = FavouritesUtil.buildSqlWeiBo(idList);
					builder = materialLibrary.toNoTimeBuilderWeiBo(pageNo,
							pageSize);
					return statusSearch(builder, countBuilder, pageNo,
							pageSize, source, timeRange, area, industry,
							emotion, sort, keywords, forwarPrimary);
				} else if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
					// 构建微信检索表达式
					buildSql = FavouritesUtil.buildSqlWeiXin(idList);
					builder = materialLibrary.toNoTimeBuilderWeiXin(pageNo,
							pageSize);
					return weChatSearch(builder, countBuilder, pageNo,
							pageSize, source, timeRange, area, industry,
							emotion, sort, keywords);
				} else {
					return documentSearch(builder, countBuilder, pageNo,
							pageSize, source, timeRange, area, industry,
							emotion, sort, keywords, invitationCard);
				}
			} else {
				if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
					// 构建微博检索表达式
					builder = materialLibrary.toNoTimeBuilderWeiBo(pageNo,
							pageSize);
					return statusSearch(builder, countBuilder, pageNo,
							pageSize, source, timeRange, area, industry,
							emotion, sort, keywords, forwarPrimary);
				} else if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
					// 构建微信检索表达式
					builder = materialLibrary.toNoTimeBuilderWeiXin(pageNo,
							pageSize);
					return weChatSearch(builder, countBuilder, pageNo,
							pageSize, source, timeRange, area, industry,
							emotion, sort, keywords);
				} else {
					return documentSearch(builder, countBuilder, pageNo,
							pageSize, source, timeRange, area, industry,
							emotion, sort, keywords, invitationCard);
				}
			}
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		}
	}

	/**
	 * 传统库搜索
	 * 
	 * @param builder
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param keywords
	 * @return
	 * @throws TRSException
	 *             CreatedByXiaoying
	 */
	private Object documentSearch(QueryBuilder builder,
			QueryBuilder countBuilder, int pageNo, int pageSize, String source,
			String time, String area, String industry, String emotion,
			String sort, String keywords, String invitationCard)
			throws TRSException {
		log.warn("专项检测信息列表  开始调用接口");
		try {
			User loginUser = UserUtils.getUser();
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME,
					DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME,
					DateUtil.formatTimeRange(time), Operator.Between);
			// 来源
			if (!"ALL".equals(source)) {
				// 单选状态
				if ("国内新闻".equals(source)) {
					String trsl = new StringBuffer(
							FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
							.append(FtsFieldConst.FIELD_SITENAME)
							.append(":百度贴吧").toString();
					builder.filterByTRSL(trsl);
					countBuilder.filterByTRSL(trsl);
				} else if ("国内论坛".equals(source)) {
					StringBuffer sb = new StringBuffer(
							FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
							.append(FtsFieldConst.FIELD_SITENAME)
							.append(":百度贴吧");
					if ("0".equals(invitationCard)) {// 主贴
						sb.append(" AND ")
								.append(FtsFieldConst.FIELD_NRESERVED1)
								.append(":0");
					} else if ("1".equals(invitationCard)) {// 回帖
						sb.append(" AND ")
								.append(FtsFieldConst.FIELD_NRESERVED1)
								.append(":1");
					}
					builder.filterByTRSL(sb.toString());
					countBuilder.filterByTRSL(sb.toString());
				} else {
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source,
							Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,
							source, Operator.Equal);
				}
			}

			if (!"ALL".equals(area)) { // 地域
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":"
						+ contentArea);
				countBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA
						+ ":" + contentArea);
			}
			if (!"ALL".equals(industry)) { // 行业
				builder.filterField(FtsFieldConst.FIELD_INDUSTRY,
						industry.split(";"), Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY,
						industry.split(";"), Operator.Equal);
			}
			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion,
						Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion,
						Operator.Equal);
			}
			// 现在在结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer()
						.append(FtsFieldConst.FIELD_TITLE).append(":")
						.append(keywords).append(" OR ")
						.append(FtsFieldConst.FIELD_ABSTRACTS).append(":")
						.append(keywords).append(" OR ")
						.append(FtsFieldConst.FIELD_CONTENT).append(":")
						.append(keywords).append(" OR ")
						.append(FtsFieldConst.FIELD_KEYWORDS).append(":")
						.append(keywords).toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
				log.info(builder.asTRSL());
			}
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService
						.getHotList(builder, countBuilder, loginUser,null);// specialId传了也没用
			default:
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			}

			return infoListService.getDocList(builder, loginUser, false,false,false,false,null);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		}
	}

	/**
	 * 微博库搜索
	 * 
	 * @param builder
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param keywords
	 * @return
	 * @throws TRSException
	 *             CreatedByXiaoying
	 */
	private Object statusSearch(QueryBuilder builder,
			QueryBuilder countBuilder, int pageNo, int pageSize, String source,
			String time, String area, String industry, String emotion,
			String sort, String keywords, String forwarPrimary)
			throws TRSException {
		log.warn("专项检测信息列表，微博，  开始调用接口");
		try {
			User loginUser = UserUtils.getUser();
			// 时间
			builder.filterField(FtsFieldConst.FIELD_CREATED_AT,
					DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT,
					DateUtil.formatTimeRange(time), Operator.Between);
			if (!"ALL".equals(area)) { // 地域

				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":"
						+ contentArea);
				countBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA
						+ ":" + contentArea);
			}
			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion,
						Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion,
						Operator.Equal);
			}
			// 转发 / 原发
			String builderTRSL = builder.asTRSL();
			String builderDatabase = builder.getDatabase();
			String countTRSL = countBuilder.asTRSL();
			String countBuilerDatabase = countBuilder.getDatabase();
			StringBuilder builderTrsl = new StringBuilder(builderTRSL);
			StringBuilder countBuilderTrsl = new StringBuilder(countTRSL);
			if ("primary".equals(forwarPrimary)) {
				// 原发
				builder.filterByTRSL(Const.PRIMARY_WEIBO);
				countBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
			} else if ("forward".equals(forwarPrimary)) {
				// 转发
				builder = new QueryBuilder();
				countBuilder = new QueryBuilder();

				builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
				builder.filterByTRSL(builderTrsl.toString());

				builder.setDatabase(builderDatabase);
				builder.setPageSize(pageSize);
				builder.setPageNo(pageNo);

				countBuilderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
				countBuilder.filterByTRSL(countBuilderTrsl.toString());
				countBuilder.setDatabase(countBuilerDatabase);
				countBuilder.setPageSize(pageSize);
				countBuilder.setPageNo(pageNo);
			}
			// 转发 / 原发
			/*if ("primary".equals(forwarPrimary)) {
				// 原发
				builder.filterField(FtsFieldConst.IR_RETWEETED_MID, "0",
						Operator.Equal);
				countBuilder.filterField(FtsFieldConst.IR_RETWEETED_MID, "0",
						Operator.Equal);
			} else if ("forward".equals(forwarPrimary)) {
				// 转发
				builder.filterField(FtsFieldConst.IR_RETWEETED_MID, "0",
						Operator.NotEqual);
				countBuilder.filterField(FtsFieldConst.IR_RETWEETED_MID, "0",
						Operator.NotEqual);
			}*/
			// 现在在结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer()
						.append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":")
						.append(keywords).toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
				log.info(builder.asTRSL());
			}
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotListStatus(builder, countBuilder,
						loginUser,null);
			default:
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			}

			return infoListService.getStatusList(builder, loginUser,true,false,false,false,null);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		}
	}

	/**
	 * 微信库搜索
	 * 
	 * @param builder
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param keywords
	 * @return
	 * @throws TRSException
	 *             CreatedByXiaoying
	 */
	private Object weChatSearch(QueryBuilder builder,
			QueryBuilder countBuilder, int pageNo, int pageSize, String source,
			String time, String area, String industry, String emotion,
			String sort, String keywords) throws TRSException {
		log.warn("专项检测信息列表，微信，  开始调用接口");
		try {
			User loginUser = UserUtils.getUser();
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME,
					DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME,
					DateUtil.formatTimeRange(time), Operator.Between);
			if (!"ALL".equals(area)) { // 地域
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":"
						+ contentArea);
				countBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA
						+ ":" + contentArea);
			}
			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion,
						Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion,
						Operator.Equal);
			}
			// 现在在结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer()
						.append(FtsFieldConst.FIELD_TITLE).append(":")
						.append(keywords).append(" OR ")
						.append(FtsFieldConst.FIELD_CONTENT).append(":")
						.append(keywords).toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			}
			log.info(builder.asTRSL());
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
				break;
			case "hot":
				return infoListService.getHotListWeChat(builder, countBuilder,
						loginUser,null);
			default:
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			}

			return infoListService.getWeChatList(builder, loginUser,true,false,false,false,null);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		}
	}

	@RequestMapping(value = "/infoDelete", method = RequestMethod.GET)
	@FormatResult
	public Object infoDelete(@ApiParam("文章id") @RequestParam("sid") String sid,
			@ApiParam("素材id") @RequestParam("library_id") String libraryId) {
		ReportMaterial reportMaterial = new ReportMaterial();
		reportMaterial.setSid(sid);
		reportMaterial.setLibraryId(libraryId);
		// 0表示删除
		reportMaterial.setStatus("0");
		reportMaterialRepository.save(reportMaterial);
		return "success";

	}

	/**
	 * 获取所有报告
	 * 
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页大小
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@ApiOperation("获取所有报告")
	// @SystemControllerLog(module="舆情报告",description="查看 报告列表")
	@RequestMapping(value = "reportList", method = RequestMethod.GET)
	public Object getAllReport(
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
			HttpServletRequest request) throws OperationException {
		try {
			User loginUser = UserUtils.getUser();
			return reportService.getAllReport(loginUser, pageNo, pageSize);
		} catch (Exception e) {
			log.error("获取报告列表失败", e);
			throw new OperationException("获取报告列表失败,message" + e);
		}
	}

	/**
	 * 模糊查询报告
	 * 
	 * @param reportName
	 *            报告名称
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("模糊查询报告")
	// @SystemControllerLog(module = "舆情报告", description = "查询 我的报告")
	@FormatResult
	@RequestMapping(value = "seachReport", method = RequestMethod.GET)
	public Object seachReport(
			@RequestParam(value = "reportName", required = false) String reportName,
			HttpServletRequest request) throws OperationException {
		try {
			User loginUser = UserUtils.getUser();
			return reportService.seachReport(loginUser, reportName);
		} catch (Exception e) {
			log.error("查询报告失败", e);
			throw new OperationException("查询报告失败,message" + e);
		}
	}

	/**
	 * 模糊查询报告
	 * 
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("模糊查询素材库")
	// @SystemControllerLog(module = "舆情报告", description = "查询 素材库")
	@FormatResult
	@RequestMapping(value = "seachLib", method = RequestMethod.GET)
	public Object seachLibarary(
			@RequestParam(value = "libName", required = false) String libName,
			HttpServletRequest request) throws OperationException {
		try {
			User loginUser = UserUtils.getUser();
			return reportService.seachLib(loginUser, libName);
		} catch (Exception e) {
			log.error("查询报告失败", e);
			throw new OperationException("查询报告失败,message" + e);
		}
	}

	/**
	 * 删除报告
	 * 
	 * @param reportId
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("删除报告")
	// @SystemControllerLog(module = "舆情报告", description = "删除 我的报告")
	@FormatResult
	@RequestMapping(value = "delReport", method = RequestMethod.GET)
	public Object delReport(@RequestParam(value = "reportId") String reportId)
			throws OperationException {
		try {
			return reportService.deleteReport(reportId);
		} catch (Exception e) {
			log.error("删除报告失败", e);
			throw new OperationException("删除报告失败,message" + e);
		}
	}

	/**
	 * 生成报告
	 * 
	 * @param libraryId
	 * @param templateId
	 * @param templateName
	 * @param templateList
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ApiOperation("生成报告")
	// @SystemControllerLog(module = "舆情报告", description = "新建 报告")
	@FormatResult
	@RequestMapping(value = "create", method = RequestMethod.POST)
	public Object creatReport(
			@RequestParam(value = "libraryId") String libraryId,
			// 第一次创建模板 模板id和模板名为空？
			@RequestParam(value = "templateId", required = false) String templateId,
			@RequestParam(value = "templateName", required = false) String templateName,
			@ApiParam("以json格式传来的模板信息") @RequestParam(value = "templateList") String templateList,
			@RequestParam(value = "reportName") String reportName,
			HttpServletRequest request) throws OperationException {
		try {
			validation(templateList);
			Template currentTemplate;
			if (StringUtil.isEmpty(templateId)) {
				String userId = UserUtils.getUser().getId();
				Template template = new Template();
				template.setTemplateName(templateName);
				template.setTemplateList(templateList);
				template.setUserId(userId);
				// 存进模板表
				currentTemplate = reportService.saveOrupdateTep(template);
			} else {
				currentTemplate = reportService.getOneTemplate(templateId,
						templateList);
			}
			// json转list
			List<TElement> eleList = convertData(templateList);
			return reportService.analyTemplateList(eleList, libraryId,
					currentTemplate, reportName);
		} catch (Exception e) {
			log.error("生成报告失败", e);
			throw new OperationException("生成报告失败!/n" + e);
		}
	}

	/**
	 * 格式验证
	 * 
	 * @param list
	 * @throws OperationException
	 */
	private void validation(String list) throws OperationException {
		Object object = ObjectUtil.toObject(list);
		if (ObjectUtil.isEmpty(object)) {
			log.error("模板格式出错");
			throw new OperationException("模板格式出错！");
		}
	}

	/**
	 * 格式转换json->list
	 * 
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
	private List<TElement> convertData(String dataList) throws Exception {
		if (ObjectUtil.isNotEmpty(dataList)) {
			List<TElement> result = new ArrayList<TElement>();
			try {
				List<?> objeList = ObjectUtil.toObject(dataList, List.class);
				for (int i = 0; i < objeList.size(); i++) {
					String oneData = ObjectUtil.toJson(objeList.get(i));
					TElement oneElement = ObjectUtil.toObject(oneData,
							TElement.class);
					result.add(oneElement);
				}
			} catch (Exception e) {
				log.error("转换模板数据格式出错！", e);
				throw new OperationException("转换模板数据格式出错！" + e);
			}
			return result;
		} else {
			return null;
		}
	}

	/**
	 * 验证是否能下载报告,预下载
	 * 
	 * @param reportId
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "preDownload", method = RequestMethod.GET)
	public Object validateReport(
			@RequestParam(value = "reportId") String reportId,
			HttpServletRequest request) throws OperationException {
		try {
			Report report = reportService.getOneReport(reportId);
			String reportPath = report.getDocFilePath();
			Map<String, Object> retMap = new HashMap<String, Object>();
			int status;
			String path = "";
			Object obj;
			if (StringUtil.isNotEmpty(reportPath)) {
				// 执行下载操作，返回下载报告路径,
				status = Const.SUCCESS_INT;
				path = "download";
				obj = null;
			} else {
				report.setStatus(Const.DOWNLOAD_INT);
				reportService.saveReport(report);
				status = Const.FAIL_INT;
				obj = reportService.bulidReportDoc(report);
				path = "mergeReport";
			}
			retMap.put("status", status);
			retMap.put("path", path);
			retMap.put("data", obj);
			return retMap;
		} catch (Exception e) {
			log.error("预下载报告出错！", e);
			throw new OperationException("预下载报告出错" + e);
		}
	}

	@ApiOperation("合并报告接口")
	@RequestMapping(value = "mergeReport", method = RequestMethod.POST)
	public Object mergeReport(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			HttpServletRequest request) throws OperationException {
		try {
			Map<String, Object> retMap = new HashMap<String, Object>();
			int status;
			String path = "";
			if (reportService.mergeReport(reportId)) {
				status = Const.SUCCESS_INT;
				path = "download";
			} else {
				status = Const.FAIL_INT;
			}
			retMap.put("status", status);
			retMap.put("path", path);
			return retMap;
		} catch (Exception e) {
			log.error("合并报告出错！", e);
			throw new OperationException("合并报告出错！" + e);
		}
	}

	/**
	 * 下载报告
	 * 
	 * @param reportId
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	// @SystemControllerLog(module = "舆情报告", description = "下载 报告")
	@RequestMapping(value = "download", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> downloadReport(
			@RequestParam(value = "reportId") String reportId,
			HttpServletRequest request, HttpServletResponse response)
			throws OperationException {
		try {
			Report report = reportService.getOneReport(reportId);
			String reportPath = report.getDocFilePath();
			report.setStatus(Const.SUCCESS_INT);
			reportService.saveReport(report);
			FileSystemResource file = new FileSystemResource(reportPath);
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
			log.error("下载报告失败！", e);
			throw new OperationException("下载报告出错！" + e);
		}
	}

	@RequestMapping(value = "downloadTest", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> downloadTest(
			HttpServletRequest request, HttpServletResponse response)
			throws OperationException {
		String fileName = "test.zip";
		String path = "/home/netinsight/data/java/book.zip";
		try {
			// HttpServletResponse response =
			// ServletActionContext.getResponse();
			File file = new File(path);
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename="
					+ new String(fileName.getBytes("ISO8859-1"), "UTF-8"));
			response.setContentLength((int) file.length());
			response.setContentType("application/zip");// 定义输出类型
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream buff = new BufferedInputStream(fis);
			byte[] b = new byte[1024];// 相当于我们的缓存
			long k = 0;// 该值用于计算当前实际下载了多少字节
			OutputStream myout = response.getOutputStream();// 从response对象中得到输出流,准备下载
			// 开始循环下载
			while (k < file.length()) {
				int j = buff.read(b, 0, 1024);
				k += j;
				myout.write(b, 0, j);
			}
			myout.flush();
			buff.close();
			// file.delete();
		} catch (Exception e) {
			log.error(e.toString());
		}
		return null;
	}

	@ApiOperation("上传地域分布图接口")
	@RequestMapping(value = "drawImage2", method = RequestMethod.POST)
	public Object drawAreaImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.AREA_2, "AREA");
		} catch (Exception e) {
			log.error("处理地域分布图出错！", e);
			throw new OperationException("处理地域分布图出错！" + e);
		}
	}

	@ApiOperation("上传来源类型分析图接口")
	@RequestMapping(value = "drawImage3", method = RequestMethod.POST)
	public Object drawSourceImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.SOURCE_3, "SOURCE");
		} catch (Exception e) {
			log.error("处理来源类型分析图文档出错！", e);
			throw new OperationException("处理来源类型分析图文档出错！" + e);
		}
	}

	@ApiOperation("上传媒体活跃度图接口")
	@RequestMapping(value = "drawImage4", method = RequestMethod.POST)
	public Object drawActiveImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.ACTIVE_4, "ACTIVE");
		} catch (Exception e) {
			log.error("处理媒体活跃度图文档出错！", e);
			throw new OperationException("处理媒体活跃度图文档出错！" + e);
		}
	}

	@ApiOperation("上传媒体扩散图接口")
	@RequestMapping(value = "drawImage5", method = RequestMethod.POST)
	public Object drawDiffuseImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.DIFFUSE_5, "DIFFUSE");
		} catch (Exception e) {
			log.error("处理媒体扩散分析图文档出错！", e);
			throw new OperationException("处理媒体扩散分析图文档出错！" + e);
		}

	}

	@ApiOperation("上传情感分析图接口")
	@RequestMapping(value = "drawImage6", method = RequestMethod.POST)
	public Object draweMotionImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.EMOTION_6, "EMOTION");
		} catch (Exception e) {
			log.error("处理情感分析图文档出错！", e);
			throw new OperationException("处理情感分析图文档出错！" + e);
		}
	}

	@ApiOperation("上传热词分布图接口")
	@RequestMapping(value = "drawImage7", method = RequestMethod.POST)
	public Object drawHotwordImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.HOTWORD_7, "HOTWORD");
		} catch (Exception e) {
			log.error("处理热词分布图文档出错！", e);
			throw new OperationException("处理热词分布图文档出错！" + e);
		}
	}

	@ApiOperation("上传热点地名分布图接口")
	@RequestMapping(value = "drawImage8", method = RequestMethod.POST)
	public Object drawHotplaceImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.HOTPLACE_8, "HOTPLACE");
		} catch (Exception e) {
			log.error("处理热点地名分布图文档出错！", e);
			throw new OperationException("处理热点地名分布图文档出错！" + e);
		}
	}

	@ApiOperation("上传热点机构分布图接口")
	@RequestMapping(value = "drawImage9", method = RequestMethod.POST)
	public Object drawHotorganImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.HOTORGAN_9, "HOTORGAN");
		} catch (Exception e) {
			log.error("处理热点机构分布图文档出错！", e);
			throw new OperationException("处理热点机构分布图文档出错！" + e);
		}
	}

	@ApiOperation("上传热点人名分布图接口")
	@RequestMapping(value = "drawImage10", method = RequestMethod.POST)
	public Object drawHotnameImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.HOTNAME_10, "HOTNAME");
		} catch (Exception e) {
			log.error("处理热点人名分布图文档出错！", e);
			throw new OperationException("处理热点人名分布图文档出错！" + e);
		}
	}

	@ApiOperation("上传声量趋势图接口")
	@RequestMapping(value = "drawImage11", method = RequestMethod.POST)
	public Object drawVolumeImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.VOLUME_11, "VOLUME");
		} catch (Exception e) {
			log.error("处理声量趋势图文档出错！", e);
			throw new OperationException("处理声量趋势图文档出错！" + e);
		}
	}

	@ApiOperation("上传引爆点分析图接口")
	@RequestMapping(value = "drawImage12", method = RequestMethod.POST)
	public Object drawBoomImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.BOOM_12, "BOOM");
		} catch (Exception e) {
			log.error("处理引爆点分析图文档出错！", e);
			throw new OperationException("处理引爆点分析图文档出错！" + e);
		}
	}

	@ApiOperation("上传舆情指数刻画图接口")
	@RequestMapping(value = "drawImage13", method = RequestMethod.POST)
	public Object drawExponentImage(
			@ApiParam("报告ID") @RequestParam(value = "reportId") String reportId,
			@ApiParam("图片资源") @RequestParam(value = "imageDate") String imageDate,
			@ApiParam("图片的标题") @RequestParam(value = "title") String title,
			@ApiParam("报告中的位置") @RequestParam(value = "position") Integer position,
			HttpServletRequest request) throws OperationException {
		try {
			return reportService.drawImage(reportId, imageDate, title,
					position, Const.EXPONENT_13, "EXPONENT");
		} catch (Exception e) {
			log.error("处理舆情指数刻画图文档出错！", e);
			throw new OperationException("处理舆情指数刻画图文档出错！" + e);
		}
	}

}
