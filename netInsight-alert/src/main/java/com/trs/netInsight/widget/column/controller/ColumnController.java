/*
 * Project: netInsight
 *
 * File Created at 2017年11月20日
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.column.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.entity.*;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.column.entity.emuns.StatisticalChartInfo;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.service.*;
import com.trs.netInsight.widget.common.service.ICommonChartService;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.SubGroupRepository;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.search.SearchException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 栏目操作接口
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年10月11日
 *
 */
@RestController
@RequestMapping("/column")
@Api(description = "栏目接口")
@Slf4j
public class ColumnController {

	@Autowired
	private IIndexPageService indexPageService;

	@Autowired
	private IIndexTabService indexTabService;

	@Autowired
	private IColumnService columnService;

	@Autowired
	private IDistrictInfoService districtInfoService;

	@Autowired
	private INavigationService navigationService;

	@Autowired
	private IIndexTabMapperService indexTabMapperService;

	@Autowired
	private SubGroupRepository subGroupService;

	@Autowired
	private OrganizationRepository organizationService;

	@Autowired
	private ICommonListService commonListService;
	@Autowired
	private ICommonChartService commonChartService;
	@Autowired
	private IColumnChartService columnChartService;
	/**
	 * 刚加载页面时查询所有栏目（分组）
	 */
	@FormatResult
	@GetMapping(value = "/selectNavigation")
	@ApiOperation("刚加载页面时查询所有导航栏")
	public Object selectNavigation() {
		String userId = UserUtils.getUser().getId();
		// 通过sequence排序
		List<NavigationConfig> navigationList = navigationService.findByUserIdOrSubGroupIdAndSort(userId, "sequence");
		if (CollectionsUtil.isNotEmpty(navigationList)) {
			for (NavigationConfig navigationConfig : navigationList) {
				if (navigationConfig.getType() == NavigationEnum.share) { // 如果为共享池，则计算共享数量
					long total = indexTabMapperService.computeShareByOrg();
					navigationConfig.setShareNumber(total);
				}
			}
		}
		return navigationList;
	}

	/**
	 * 根据导航id检索下级栏目组(分组)
	 *
	 * @since changjiang @ 2018年10月11日
	 * @return
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping(value = "/selectIndexPage")
	@ApiOperation("根据导航id检索下级栏目组")
	public Object selectIndexPage(){
		return indexPageService.findByTypeId();
	}

	/**
	 * 添加自定义导航栏(分组)
	 *
	 * @param name
	 * @return
	 */
	@ApiOperation("添加自定义导航栏")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_ADD_NAVIGATION, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "添加自定义导航栏：@{name}")
	@GetMapping(value = "/addNavigation")
	public Object addNavigation(@ApiParam("导航的名") @RequestParam("name") String name) {
		return navigationService.addNavigation(name);
	}

	/**
	 * 删除自定义导航栏(分组)
	 *
	 * @param typeId
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_DELETE_NAVIGATION, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "删除自定义导航栏：${typeId}")
	@GetMapping(value = "/deleteNavigation")
	@ApiOperation("删除自定义导航栏")
	public Object deleteNavigation(@ApiParam("导航栏id") @RequestParam("typeId") String typeId) throws OperationException {
		// 删除该导航栏下的一级二级栏目
		return navigationService.deleteNavigation(typeId);
	}

	/**
	 * 导航栏拖拽接口 （分组）
	 *
	 * @param typeId
	 *            以分号隔开的导航栏id
	 * @return
	 */
	@FormatResult
	@GetMapping(value = "/moveNavigation")
	@Log(systemLogOperation = SystemLogOperation.COLUMN_MOVE_NAVIGATION, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "")
	@ApiOperation("导航栏拖拽接口")
	public Object moveNavigation(@ApiParam("导航栏id,分号隔开") @RequestParam("typeId") String typeId) {
		return navigationService.moveNavigation(typeId);
	}

	/**
	 * 导航栏修改接口（分组）
	 * @param typeId
	 * @param name
	 * @return
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_UPDATE_NAVIGATION, systemLogType = SystemLogType.COLUMN,
			systemLogOperationPosition = "修改自定义导航栏：@{name}",methodDescription="${name}")
	@GetMapping(value = "/updateNavigation")
	@ApiOperation("导航栏修改接口")
	public Object updateNavigation(@ApiParam("导航栏id,分号隔开") @RequestParam("typeId") String typeId,
								   @ApiParam("导航的名") @RequestParam("name") String name) {
		return navigationService.updateNavigation(typeId, name);
	}

	/**
	 * 导航栏隐藏显示接口（分组）
	 *
	 * @param typeId
	 *            导航栏id
	 * @param hide
	 *            隐藏true 不隐藏false
	 * @return
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_HIDE_OR_SHOW_NAVI, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "隐藏自定义导航栏：${typeId}")
	@GetMapping(value = "/hideOrShowNavi")
	@ApiOperation("导航栏隐藏显示接口")
	public Object hideOrShowNavi(@ApiParam("导航栏id") @RequestParam("typeId") String typeId,
								 @ApiParam("隐藏true 不隐藏false") @RequestParam("hide") boolean hide) {
		return navigationService.hideOrShowNavi(typeId, hide);
	}

	/**
	 * 一级栏目添加接口(分组)
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_ADD_INDEX_PAGE, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "添加栏目分组：@{typeId}/${name}")
	@RequestMapping(value = "/addIndexPage", method = RequestMethod.GET)
	@ApiOperation("栏目分组添加接口")
	//@PreAuthorize("hasPermission(#contact, 'admin')")
	public Object addOne(@ApiParam("栏目分组名") @RequestParam("name") String name,
						 @ApiParam("要创建的栏目分组的父级分组id") @RequestParam(value = "parentId" ,required = false) String parentId,
						 @ApiParam("导航栏id(非自定义情况下不传)") @RequestParam(value = "typeId",required = false, defaultValue = "") String typeId) throws TRSException {
		User loginUser = UserUtils.getUser();
		if (UserUtils.isRoleAdmin()){
			Organization organization = organizationService.findOne(loginUser.getOrganizationId());
			//机构管理员
			if (organization.getColumnNum() <= indexTabService.getSubGroupColumnCount(loginUser)){
				throw new TRSException(CodeUtils.FAIL,"该新创建的栏目分组下已没有可新建栏目的资源！");
			}
		}
		if (UserUtils.isRoleOrdinary(loginUser)){
			//如果是普通用户 受用户分组 可创建资源的限制
			//查询该用户所在的用户分组下 是否有可创建资源
			SubGroup subGroup = subGroupService.findOne(loginUser.getSubGroupId());
			if (subGroup.getColumnNum() <= indexTabService.getSubGroupColumnCount(loginUser)){
				throw new TRSException(CodeUtils.FAIL,"该新创建的栏目分组下已没有可新建栏目的资源！");
			}
		}

		if (StringUtil.isNotEmpty(name)) {
			if (StringUtil.isNotEmpty(parentId)) {
				IndexPage parent = indexPageService.findOne(parentId);
				if(ObjectUtil.isEmpty(parent)){
					throw new TRSException(CodeUtils.FAIL,"所选的上级分组不存在");
				}
			}
			if (StringUtil.isNotEmpty(typeId)) {
				NavigationConfig navigationConfig = navigationService.findOne(typeId);
				if(ObjectUtil.isEmpty(navigationConfig)){
					throw new TRSException(CodeUtils.FAIL,"选择的自定义日常监测栏目不存在");
				}
			}
			return indexPageService.addIndexPage(parentId,name,typeId,loginUser);
		}
		return null;
	}

	/**
	 * 修改一级栏目(分组)
	 *
	 * @param name
	 *            一级栏目名
	 * @param indexPageId
	 *            一级栏目id
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_UPDATE_INDEX_PAGE, systemLogType = SystemLogType.COLUMN,
			systemLogOperationPosition = "修改栏目分组名：${indexPageId}",methodDescription="${name}")
	@RequestMapping(value = "/updateIndexPage", method = RequestMethod.GET)
	@ApiOperation("栏目分组修改接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "name", value = "栏目分组名", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "indexPageId", value = "栏目分组id", dataType = "String", paramType = "query", required = false) })
	public Object updateOne(@RequestParam("name") String name, @RequestParam("indexPageId") String indexPageId,
							HttpServletRequest request) throws OperationException , TRSException{
		User user = UserUtils.getUser();
		IndexPage indexPage = indexPageService.findOne(indexPageId);
		if(ObjectUtil.isEmpty(indexPage)){
			throw new TRSException(CodeUtils.FAIL,"对应的日常监测栏目分组不存在");
		}
		if(StringUtil.isNotEmpty(name)){
			indexPage.setName(name);
			return indexPageService.save(indexPage);
		}
		return null;
	}

	/**
	 * 栏目分组拖拽接口（分组）
	 * @param parentId
	 * @param moveData
	 * @param sequenceData
	 * @return
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_MOVE_ONE, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "")
	@RequestMapping(value = "/moveColumn", method = RequestMethod.POST)
	@ApiOperation("栏目分组拖拽接口")
	public Object moveColumn(@ApiParam("分组要拖拽后的父级分组") @RequestParam(value = "parentId", required = false) String parentId,
							 @ApiParam("被拖拽的对象的信息") @RequestParam("moveData") String moveData,
							 @ApiParam("拖拽完成后的顺序") @RequestParam("sequenceData") String sequenceData)throws TRSException {
		IndexPage parent = null;
		if(StringUtil.isNotEmpty(parentId)){
			parent = indexPageService.findOne(parentId);
			if(ObjectUtil.isEmpty(parent)){
				throw new TRSException(CodeUtils.FAIL,"对应的日常监测栏目分组不存在");
			}
		}
		if(StringUtil.isEmpty(moveData)){
			throw new TRSException(CodeUtils.FAIL,"被拖拽的分组或栏目信息为空");
		}
		if(StringUtil.isEmpty(sequenceData)){
			throw new TRSException(CodeUtils.FAIL,"拖拽后顺序为空");
		}
		User user = UserUtils.getUser();
		columnService.moveIndexSequence(sequenceData,moveData,parentId,user);

		return "success";
	}


	/**
	 * 三级栏目（图表）添加接口（分组）
	 * @param name
	 * @param indexPageId
	 * @param type
	 * @param trsl
	 * @param xyTrsl
	 * @param keyWord
	 * @param keyWordIndex
	 * @param groupName
	 * @param timeRange
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_ADD_INDEX_TAB, systemLogType = SystemLogType.COLUMN,
			systemLogOperationPosition = "添加二级栏目（图表）：${indexPageId}/@{name}",methodDescription="添加栏目:${name}")
	@RequestMapping(value = "/addIndexTab", method = RequestMethod.POST)
	@ApiOperation("三级栏目（图表）添加接口")
	@ApiImplicitParams({ @ApiImplicitParam(name = "name", value = "三级栏目名", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "indexPageId", value = "父分组Id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "navigationId", value = "导航栏id(非自定义情况下不传)", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "type", value = "图表类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "columnType", value = "栏目模式类型：COMMON 普通模式、SPECIAL专家模式", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contrast", value = "分类对比类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "trsl", value = "检索表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keyWord", value = "关键词", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWordIndex", value = "排除词命中位置(0:标题,1:标题+正文,2:标题+摘要)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keyWordIndex", value = "关键词位置(0:标题,1:标题+正文,2:标题+摘要)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "xyTrsl", value = "XY轴检索表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupName", value = "数据来源(可多值,中间以';'隔开)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "timeRange", value = "发布时间范围(2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weight", value = "标题权重", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排 no，全网排 netRemove,url排 urlRemove,跨数据源排 sourceRemove", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "tabWidth", value = "栏目是不是通栏，50为半栏，100为通栏", dataType = "int", paramType = "query", required = false),
			@ApiImplicitParam(name = "share", value = "是否共享标记", dataType = "Boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "mediaLevel", value = "媒体等级", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaIndustry", value = "媒体行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentIndustry", value = "内容行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "filterInfo", value = "信息过滤", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentArea", value = "信息地域", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaArea", value = "媒体地域", dataType = "String", paramType = "query")})
	public Object addThree(@RequestParam("name") String name, @RequestParam(value = "indexPageId",required = false) String indexPageId,
						   @RequestParam(value = "navigationId", defaultValue = "") String navigationId,
						   @RequestParam("columnType") String columnType,
						   @RequestParam("type") String type, @RequestParam(value = "contrast", required = false) String contrast,
						   @RequestParam(value = "trsl", required = false) String trsl,
						   @RequestParam(value = "xyTrsl", required = false) String xyTrsl,
						   @RequestParam(value = "keyWord", required = false) String keyWord,
						   @RequestParam(value = "excludeWords", required = false) String excludeWords,
						   @RequestParam(value = "excludeWordIndex", required = false) String excludeWordIndex,
						   @RequestParam(value = "keyWordIndex", required = false) String keyWordIndex,
						   @RequestParam(value = "groupName", required = false, defaultValue = "ALL") String groupName,
						   @RequestParam(value = "timeRange", required = false) String timeRange,
						   @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
						   @RequestParam(value = "weight", required = false) boolean weight,
						   @RequestParam(value = "simflag", required = false) String simflag,
						   @RequestParam(value = "tabWidth", required = false, defaultValue = "50") int tabWidth,
						   @RequestParam(value = "share", defaultValue = "false") boolean share,
						   @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
						   @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
						   @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
						   @RequestParam(value = "filterInfo", required = false) String filterInfo,
						   @RequestParam(value = "contentArea", required = false) String contentArea,
						   @RequestParam(value = "mediaArea", required = false) String mediaArea,HttpServletRequest request)
			throws TRSException {

		//首先判断下用户权限（若为机构管理员，只受新建与编辑的权限，不受可创建资源数量的限制）
		User loginUser = UserUtils.getUser();
		if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())){
			Organization organization = organizationService.findOne(loginUser.getOrganizationId());
			if (UserUtils.isRoleAdmin()){
				//机构管理员
				if (organization.getColumnNum() <= indexTabService.getSubGroupColumnCount(loginUser)){
					throw new TRSException(CodeUtils.FAIL,"您目前创建的栏目已达上限，如需更多，请联系相关运维人员。");
				}
			}
			if (UserUtils.isRoleOrdinary(loginUser)){
				//如果是普通用户 受用户分组 可创建资源的限制
				//查询该用户所在的用户分组下 是否有可创建资源
				SubGroup subGroup = subGroupService.findOne(loginUser.getSubGroupId());
				if (subGroup.getColumnNum() <= indexTabService.getSubGroupColumnCount(loginUser)){
					throw new TRSException(CodeUtils.FAIL,"您目前创建的栏目已达上限，如需更多，请联系相关运维人员。");
				}
			}

			//若为机构管理员或者普通用户 若为普通模式，判断关键字字数
			if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
				int chineseCount = 0;
				if (StringUtil.isNotEmpty(keyWord)){
					chineseCount = StringUtil.getChineseCountForSimple(keyWord);
				}else if (StringUtil.isNotEmpty(trsl) || StringUtil.isNotEmpty(xyTrsl)){
					int trslCount = StringUtil.getChineseCount(trsl);
					int xyTrslCount = StringUtil.getChineseCount(xyTrsl);
					chineseCount = trslCount+xyTrslCount;
				}
				if (chineseCount > organization.getKeyWordsNum()){
					throw new TRSException(CodeUtils.FAIL,"该栏目暂时仅支持检索"+organization.getKeyWordsNum()+"个关键字，如需更多，请联系相关运维人员。");
				}
			}
		}

		IndexTabType indexTabType = ColumnFactory.chooseType(type);
		if(ObjectUtil.isEmpty(indexTabType)){
			throw new TRSException(CodeUtils.FAIL,"当前栏目类型不存在");
		}
		groupName = CommonListChartUtil.changeGroupName(groupName);
		String sequence = String.valueOf(columnService.getMaxSequenceForColumn(indexPageId,navigationId,loginUser) +1);
		if (StringUtil.isEmpty(timeRange) ) {
			timeRange = "7d";
		}
		SpecialType specialType = SpecialType.valueOf(columnType);
		// 有几个图专家模式下 必须传xy表达式
		if(SpecialType.SPECIAL.equals(specialType)){
			if (StringUtil.isNotEmpty(trsl)) {
				if(!IndexTabType.MAP.equals(indexTabType)){
					contrast = null;
				}
				if (IndexTabType.CHART_BAR.equals(indexTabType) || IndexTabType.CHART_LINE.equals(indexTabType) || IndexTabType.CHART_PIE.equals(indexTabType)) {
					if (StringUtil.isEmpty(xyTrsl)) {
						throw new TRSException(CodeUtils.FAIL,"专家模式下"+indexTabType.getTypeName() + "时必须传xy表达式");
					}
				}
			}else{
				if (!(IndexTabType.CHART_BAR.equals(indexTabType) || IndexTabType.CHART_LINE.equals(indexTabType)
						|| IndexTabType.CHART_PIE.equals(indexTabType))) {
					throw new TRSException(CodeUtils.FAIL,"专家模式下" + indexTabType.getTypeName() + "必须填写检索表达式");
				}
			}
		} else{
			trsl=null;
			xyTrsl = null;
			if(StringUtil.isEmpty(contrast) && !IndexTabType.HOT_LIST.equals(indexTabType) &&!IndexTabType.LIST_NO_SIM.equals(indexTabType)&&!IndexTabType.WORD_CLOUD.equals(indexTabType)&&!IndexTabType.MAP.equals(indexTabType) ){
				throw new TRSException(CodeUtils.FAIL,"普通模式下"+indexTabType.getTypeName() + "时，必须传对比类型");
			}else if(IndexTabType.HOT_LIST.equals(indexTabType) || IndexTabType.LIST_NO_SIM.equals(indexTabType)){
				contrast = null;
			}
		}
		if(ColumnConst.CONTRAST_TYPE_WECHAT.equals(contrast)){
			groupName = Const.PAGE_SHOW_WEIXIN;
		}
		// 默认不排重
		boolean isSimilar = false;
		boolean irSimflag = false;
		boolean irSimflagAll = false;
		if ("netRemove".equals(simflag)) {
			isSimilar = true;   //单一媒体排重
		} else if ("urlRemove".equals(simflag)) {
			irSimflag = true;   //站内排重
		}else if ("sourceRemove".equals(simflag)){
			irSimflagAll = true;//全网排重
		}
		// 因为时间在筛选的时候还要改 所以表达式中先不拼凑时间 查询的时候在拼凑时间
		IndexTab indexTab = new IndexTab(name, trsl, keyWord, excludeWords, keyWordIndex, xyTrsl, type,
				groupName, Integer.parseInt(sequence), timeRange, isSimilar,irSimflag, weight,irSimflagAll);
		indexTab.setExcludeWeb(excludeWeb);
		indexTab.setExcludeWordIndex(excludeWordIndex);
		indexTab.setContrast(contrast);
		indexTab.setTabWidth(tabWidth);
		indexTab.setSpecialType(specialType);
		indexTab.setTypeId(navigationId);
		indexTab.setMediaLevel(mediaLevel);
		indexTab.setMediaIndustry(mediaIndustry);
		indexTab.setContentIndustry(contentIndustry);
		indexTab.setFilterInfo(filterInfo);
		indexTab.setMediaArea(mediaArea);
		indexTab.setContentArea(contentArea);
		if(StringUtil.isNotEmpty(indexPageId)){
			IndexPage indexPage = indexPageService.findOne(indexPageId);
			indexTab.setParentId(indexPage.getId());
			indexTab.setOneName(indexPage.getName());
		}
		return indexTabService.save(indexTab, share);
	}


	/**
	 * 修改栏目名
	 *
	 * @param name
	 *            栏目名
	 * @param id
	 *            栏目id
	 * @param
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_UPDATE_INDEX_TAB, systemLogType = SystemLogType.COLUMN,
			systemLogOperationPosition = "修改栏目名：${id}",methodDescription="${name}")
	@RequestMapping(value = "/updateIndexTabName", method = RequestMethod.GET)
	@ApiOperation("栏目名修改接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "name", value = "栏目名", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "id", value = "栏目id", dataType = "String", paramType = "query", required = false) })
	public Object updateOne(@RequestParam("name") String name, @RequestParam("id") String id) throws OperationException , TRSException{
		User user = UserUtils.getUser();
		IndexTabMapper mapper = indexTabMapperService.findOne(id);
		if(ObjectUtil.isEmpty(mapper)){
			throw new TRSException(CodeUtils.FAIL,"对应的日常监测栏目不存在");
		}
		if(StringUtil.isNotEmpty(name)){
			IndexTab tab = mapper.getIndexTab();
			tab.setName(name);
			return indexTabService.save(tab,false);
		}
		return null;
	}

	/**
	 * 三级栏目（图表）修改接口（分组）
	 * @param id
	 * @param name
	 * @param type
	 * @param contrast
	 * @param trsl
	 * @param xyTrsl
	 * @param keyWord
	 * @param excludeWords
	 * @param keyWordIndex
	 * @param groupName
	 * @param timeRange
	 * @param excludeWeb
	 * @param weight
	 * @param simflag
	 * @param tabWidth
	 * @param share
	 * @param copy
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@RequestMapping(value = "/updateIndexTab", method = RequestMethod.POST)
	@Log(systemLogOperation = SystemLogOperation.COLUMN_UPDATE_INDEX_TAB, systemLogType = SystemLogType.COLUMN,
			systemLogOperationPosition = "修改二级栏目（图表）：${id}",methodDescription="${name}")
	@ApiOperation("三级栏目（图表）修改接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "三级栏目映射id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "name", value = "三级栏目名", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "indexPageId", value = "父分组Id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "navigationId", value = "导航栏id(非自定义情况下不传)", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "columnType", value = "栏目模式类型：COMMON 普通模式、SPECIAL专家模式", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "type", value = "图表类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contrast", value = "分类对比类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "trsl", value = "检索表达式", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "keyWord", value = "关键词", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWordIndex", value = "排除词命中位置(0:标题,1:标题+正文,2:标题+摘要)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keyWordIndex", value = "关键词位置(0:标题,1:标题+正文,2:标题+摘要)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "xyTrsl", value = "XY轴检索表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupName", value = "数据来源(可多值,中间以';'隔开,默认为新闻)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "timeRange", value = "发布时间范围(2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weight", value = "标题权重", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排 no，全网排 netRemove,url排 urlRemove,跨数据源排 sourceRemove", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "tabWidth", value = "栏目是不是通栏，50为半栏，100为通栏", dataType = "int", paramType = "query", required = false),
			@ApiImplicitParam(name = "mediaLevel", value = "媒体等级", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaIndustry", value = "媒体行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentIndustry", value = "内容行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "filterInfo", value = "信息过滤", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentArea", value = "信息地域", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaArea", value = "媒体地域", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "share", value = "栏目共享标记", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "copy", value = "另存为标记", dataType = "boolean", paramType = "query", required = false) })
	public Object updateThree(@RequestParam("id") String id, @RequestParam("name") String name,
							  @RequestParam(value = "indexPageId",required = false) String indexPageId,
							  @RequestParam(value = "navigationId",required = false, defaultValue = "") String navigationId,
							  @RequestParam("columnType") String columnType,
							  @RequestParam("type") String type, @RequestParam(value = "contrast", required = false) String contrast,
							  @RequestParam(value = "trsl", required = false) String trsl,
							  @RequestParam(value = "xyTrsl", required = false) String xyTrsl,
							  @RequestParam(value = "keyWord", required = false) String keyWord,
							  @RequestParam(value = "excludeWords", required = false) String excludeWords,
							  @RequestParam(value = "excludeWordIndex", required = false) String excludeWordIndex,
							  @RequestParam(value = "keyWordIndex", required = false) String keyWordIndex,
							  @RequestParam(value = "groupName", required = false, defaultValue = "ALL") String groupName,
							  @RequestParam(value = "timeRange", required = false) String timeRange,
							  @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
							  @RequestParam(value = "weight", required = false) boolean weight,
							  @RequestParam(value = "simflag", required = false) String simflag,
							  @RequestParam(value = "tabWidth", required = false, defaultValue = "50") int tabWidth,
							  @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
							  @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
							  @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
							  @RequestParam(value = "filterInfo", required = false) String filterInfo,
							  @RequestParam(value = "contentArea", required = false) String contentArea,
							  @RequestParam(value = "mediaArea", required = false) String mediaArea,
							  @RequestParam(value = "share", defaultValue = "false") boolean share,
							  @RequestParam(value = "copy", defaultValue = "false") boolean copy, HttpServletRequest request)
			throws TRSException {

		User loginUser = UserUtils.getUser();
		//若为机构管理员或者普通用户 若为普通模式，判断关键字字数
		if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())){
			Organization organization = organizationService.findOne(loginUser.getOrganizationId());
			int chineseCount = 0;
			if (StringUtil.isNotEmpty(keyWord)){
				chineseCount = StringUtil.getChineseCountForSimple(keyWord);
			}else if (StringUtil.isNotEmpty(trsl) || StringUtil.isNotEmpty(xyTrsl)){
				int trslCount = StringUtil.getChineseCount(trsl);
				int xyTrslCount = StringUtil.getChineseCount(xyTrsl);
				chineseCount = trslCount+xyTrslCount;
			}

			if (chineseCount > organization.getKeyWordsNum()){
				throw new TRSException(CodeUtils.FAIL,"所修改栏目的关键字字数 已超出 该机构关键字数限制的上限!");
			}
		}
		try {
			// 默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			boolean irSimflagAll = false;
			if ("netRemove".equals(simflag)) {
				isSimilar = true;
				//irSimflagAll = true;
			} else if ("urlRemove".equals(simflag)) {
				irSimflag = true;
			}else if ("sourceRemove".equals(simflag)){
				irSimflagAll = true;
			}
			if (StringUtil.isEmpty(timeRange)) {
				timeRange = "7d";
			}
			IndexTabType indexTabType = ColumnFactory.chooseType(type);
			if(ObjectUtil.isEmpty(indexTabType)){
				throw new TRSException(CodeUtils.FAIL,"当前栏目类型不存在");
			}
			SpecialType specialType = SpecialType.valueOf(columnType);
			// 有几个图专家模式下 必须传xy表达式
			if (SpecialType.SPECIAL.equals(specialType)) {
				if (StringUtil.isNotEmpty(trsl)) {
					if(!IndexTabType.MAP.equals(indexTabType)){
						contrast = null;
					}
					if (IndexTabType.CHART_BAR.equals(indexTabType) || IndexTabType.CHART_LINE.equals(indexTabType) || IndexTabType.CHART_PIE.equals(indexTabType)) {
						if (StringUtil.isEmpty(xyTrsl)) {
							throw new TRSException(CodeUtils.FAIL,"专家模式下" + indexTabType.getTypeName() + "时必须传xy表达式");
						}
					}
				}else{
					if (!(IndexTabType.CHART_BAR.equals(indexTabType) || IndexTabType.CHART_LINE.equals(indexTabType)
							|| IndexTabType.CHART_PIE.equals(indexTabType))) {
						throw new TRSException(CodeUtils.FAIL,"专家模式下" + indexTabType.getTypeName() + "必须填写检索表达式");
					}
				}
			} else {
				trsl = null;
				xyTrsl = null;
				if (StringUtil.isEmpty(contrast) && !IndexTabType.HOT_LIST.equals(indexTabType) && !IndexTabType.LIST_NO_SIM.equals(indexTabType)
						&& !IndexTabType.WORD_CLOUD.equals(indexTabType) && !IndexTabType.MAP.equals(indexTabType)) {
					throw new TRSException(CodeUtils.FAIL,"普通模式下" + indexTabType.getTypeName() + "时，必须传对比类型");
				}else if(IndexTabType.HOT_LIST.equals(indexTabType) || IndexTabType.LIST_NO_SIM.equals(indexTabType)){
					contrast = null;
				}
			}
			if(ColumnConst.CONTRAST_TYPE_WECHAT.equals(contrast)){
				groupName = Const.PAGE_SHOW_WEIXIN;
			}
			IndexTabMapper mapper = indexTabMapperService.findOne(id);
			if(ObjectUtil.isEmpty(mapper)){
				throw new TRSException(CodeUtils.FAIL,"当前栏目不存在");
			}
			IndexTab indexTab = mapper.getIndexTab();
			if (copy) { // 如果为另存为，则将持久态对象转换为瞬时对象
				indexTab = indexTab.tabCopy();
			}

			indexTab.setName(name);
			indexTab.setSpecialType(specialType);
			indexTab.setType(type);
			indexTab.setContrast(contrast);
			indexTab.setTrsl(trsl);
			indexTab.setKeyWord(keyWord);
			indexTab.setExcludeWords(excludeWords);
			indexTab.setExcludeWordIndex(excludeWordIndex);
			indexTab.setKeyWordIndex(keyWordIndex);
			indexTab.setXyTrsl(xyTrsl);
			indexTab.setGroupName(groupName);
			indexTab.setTimeRange(timeRange);
			indexTab.setExcludeWeb(excludeWeb);
			indexTab.setSimilar(isSimilar);
			indexTab.setIrSimflag(irSimflag);
			indexTab.setTabWidth(tabWidth);
			indexTab.setIrSimflagAll(irSimflagAll);
			indexTab.setMediaLevel(mediaLevel);
			indexTab.setMediaIndustry(mediaIndustry);
			indexTab.setContentIndustry(contentIndustry);
			indexTab.setFilterInfo(filterInfo);
			indexTab.setMediaArea(mediaArea);
			indexTab.setContentArea(contentArea);
			// 栏目从标题+正文修改为仅标题的时候不设置权重，但传的weight还是=true
			if ("0".equals(keyWordIndex)) {
				indexTab.setWeight(false);
			} else {
				indexTab.setWeight(weight);
			}
			IndexPage indexPage = null;
			// 根据另存为标识选择另存为与修改操作
			if (copy) {
				indexTab = indexTab.tabCopy();
				if(StringUtil.isEmpty(navigationId)){
					indexTab.setTypeId("");
				}else{
					NavigationConfig navigation = navigationService.findOne(navigationId);
					indexTab.setTypeId(navigation.getId());
				}
				indexTab.setParentId(null);
				if(StringUtil.isNotEmpty(indexPageId)){
					indexPage = indexPageService.findOne(indexPageId);
					if(ObjectUtil.isNotEmpty(indexPage)){
						indexTab.setParentId(indexPageId);
					}
				}
				indexTab.setSequence(columnService.getMaxSequenceForColumn(indexPageId,navigationId,loginUser)+1);
				indexTab.clear();
				mapper = (IndexTabMapper) indexTabService.save(indexTab, share);
				id = mapper.getId();
			}
			indexTabService.update(indexTab, id, share);
			List<IndexTabMapper> cacheMapper = indexTabMapperService.findByIndexTab(indexTab);
			for (IndexTabMapper indexTabMapper : cacheMapper) {
				RedisFactory.deleteAllKey(indexTabMapper.getId());
			}
			String pageName = "";
			if(ObjectUtil.isNotEmpty(mapper.getIndexPage())){
				pageName = mapper.getIndexPage().getName();
			}
			indexTab.setOneName(pageName);
			return mapper;
		} catch (Exception e) {
			log.error("三级栏目修改报错", e);
			throw new OperationException("三级栏目修改报错", e);
		}
	}

	/**
	 * 三级栏目（图表）修改半栏通栏属性
	 * @param indexMapperId
	 * @param tabWidth
	 * @param request
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_DELETE_INDEX_TAB, systemLogType = SystemLogType.COLUMN,
			systemLogOperationPosition = "修改通栏半栏属性（图表）：${indexMapperId}")
	@RequestMapping(value = "/changeTabWidth", method = RequestMethod.POST)
	@ApiOperation("三级栏目（图表）修改半栏通栏属性")
	@ApiImplicitParams({@ApiImplicitParam(name = "indexMapperId", value = "三级栏目映射id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "tabWidth", value = "栏目是不是通栏，50为半栏，100为通栏", dataType = "int", paramType = "query", required = false) })
	public Object changeTabWidth(@RequestParam("indexMapperId") String indexMapperId,
								 @RequestParam("tabWidth") String tabWidth, HttpServletRequest request)
			throws TRSException {
		//id和tabWidth都用;分割 顺序一一对应
		indexTabMapperService.changeTabWidth(indexMapperId,tabWidth);
		return "success";
	}
	/**
	 * 删除的时候要改变排序
	 *
	 * @param indexMapperId
	 * @param request
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_DELETE_INDEX_TAB, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "删除二级栏目（图表）：${indexMapperId}")
	@RequestMapping(value = "/deleteIndexTab", method = RequestMethod.POST)
	@ApiOperation("三级栏目（图表）删除接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indexMapperId", value = "三级栏目映射id", dataType = "String", paramType = "query") })
	public Object deleteThree(@RequestParam("indexMapperId") String indexMapperId, HttpServletRequest request)
			throws TRSException {
		indexTabMapperService.deleteMapper(indexMapperId);
		return "success";
	}


	/**
	 * 三级栏目（图表）显示接口（分组）
	 * @param indexMapperId
	 * @param showOrHide
	 * @param request
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_UPDATE_INDEX_TAB, systemLogType = SystemLogType.COLUMN,
			systemLogOperationPosition = "隐藏栏目（图表）：${indexMapperId}")
	@RequestMapping(value = "/showIndexTab", method = RequestMethod.POST)
	@ApiOperation("三级栏目（图表）显示接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indexMapperId", value = "三级栏目映射id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "showOrHide", value = "显示隐藏", dataType = "String", paramType = "query")})
	public Object showThree(@RequestParam("indexMapperId") String indexMapperId,
							@RequestParam("showOrHide") String showOrHide,HttpServletRequest request)
			throws TRSException {
		//id用;分割
		indexTabMapperService.hide(indexMapperId, showOrHide);
		if(indexMapperId.contains(";")){
			return "success";
		}
		return indexTabMapperService.findOne(indexMapperId);
	}

	/**
	 * 一级栏目删除接口（分组）
	 * @param indexPageId
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_DELETE_INDEX_PAGE, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "删除一级栏目：${indexPageId}")
	@RequestMapping(value = "/deleteIndexPage", method = RequestMethod.POST)
	@ApiOperation("一级栏目删除接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indexPageId", value = "一级栏目id", dataType = "String", paramType = "query") })
	public Object deleteOne(@ApiParam("一级栏目id") @RequestParam("indexPageId") String indexPageId,
							HttpServletRequest request) throws OperationException {
		//已修改，递归删除分组下的所有数据
		return columnService.deleteOne(indexPageId);
	}

	/**
	 * 一级栏目隐藏接口（分组）
	 * @param indexPageId
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_HIDE_INDEX_PAGE, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "隐藏一级栏目：${indexPageId}")
	@RequestMapping(value = "/hideIndexPage", method = RequestMethod.GET)
	@ApiOperation("一级栏目隐藏接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indexPageId", value = "一级栏目id", dataType = "String", paramType = "query") })
	public Object hideOne(@RequestParam("indexPageId") String indexPageId, HttpServletRequest request)
			throws OperationException {
		IndexPage findOne = indexPageService.findOne(indexPageId);
		findOne.setHide(true);
		indexPageService.save(findOne);
		return findOne;
	}

	/**
	 * 一级栏目显示接口（分组）
	 * @param indexPageId
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@RequestMapping(value = "/showIndexPage", method = RequestMethod.GET)
	@ApiOperation("一级栏目显示接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indexPageId", value = "一级栏目id", dataType = "String", paramType = "query") })
	public Object showOne(@RequestParam("indexPageId") String indexPageId, HttpServletRequest request)
			throws OperationException {
		IndexPage findOne = indexPageService.findOne(indexPageId);
		findOne.setHide(false);
		indexPageService.save(findOne);
		return findOne;
	}

	/**
	 * 查找所有栏目接口（分组）
	 * @param request
	 * @param typeId
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@RequestMapping(value = "/selectColumn", method = RequestMethod.GET)
	@ApiOperation("查找所有栏目接口")
	public Object selectColumn(HttpServletRequest request,
							   @ApiParam("自定义导航栏的id") @RequestParam(value = "typeId", defaultValue = "") String typeId)
			throws OperationException {
		User user = UserUtils.getUser();
		return columnService.selectColumn(user, typeId);
	}


	/**
	 * 查找三级栏目（图表 ）（分组）
	 * @param id
	 * @param chartPage
	 * @param timeRange
	 * @param groupName
	 * @param emotion
	 * @param showType
	 * @param entityType
	 * @return
	 * @throws SearchException
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.COLUMN_SELECT_INDEX_TAB_DATA, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "查看二级栏目（图表）：${id}")
	@FormatResult
	//@EnableRedis
	@RequestMapping(value = "/selectChart", method = RequestMethod.POST)
	@ApiOperation("查找图表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "图表id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "chartPage", value = "图的页面类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "entityType", value = "通用：keywords；人物：people；地域：location；机构：agency", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "showType", value = "指定折线图的展示方式：按小时，按天数", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "mapContrast", value = "对比类型，地域图需要，通过文章还是媒体地域", dataType = "String", paramType = "query", required = false),

			@ApiImplicitParam(name = "openFiltrate", value = "是否使用页面的条件筛选", dataType = "Boolean", paramType = "query"),
			@ApiImplicitParam(name = "timeRange", value = "按时间查询 (2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "groupName", value = "数据来源", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "emotion", value = "混合大列表情感筛选", dataType = "String", paramType = "query", defaultValue = "ALL"),
			@ApiImplicitParam(name = "simflag", value = "排重规则  -  替换栏目条件", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "wordIndex", value = "关键词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "read", value = "阅读标记", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "excludeWeb", value = "排除网站  替换栏目条件", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "excludeWord", value = "排除关键词  替换栏目条件", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "excludeWordIndex", value = "排除词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "updateWordForm", value = "修改词距标记 替换栏目条件", dataType = "Boolean", paramType = "query"),
			@ApiImplicitParam(name = "wordFromNum", value = "词距间隔字符 替换栏目条件", dataType = "Integer", paramType = "query"),
			@ApiImplicitParam(name = "wordFromSort", value = "词距是否排序  替换栏目条件", dataType = "Boolean", paramType = "query"),
			@ApiImplicitParam(name = "mediaLevel", value = "媒体等级", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaIndustry", value = "媒体行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentIndustry", value = "内容行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "filterInfo", value = "信息过滤", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentArea", value = "信息地域", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaArea", value = "媒体地域", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "preciseFilter", value = "精准筛选", dataType = "String", paramType = "query")})
	public Object selectChart(@RequestParam("id") String id,
							  @RequestParam(value = "chartPage", defaultValue = "TabChart") String chartPage,
							  @RequestParam(value = "showType", required = false, defaultValue = "") String showType,
							  @RequestParam(value = "entityType", defaultValue = "keywords") String entityType,//上部为查询图必须的条件
							  @RequestParam(value = "mapContrast", required = false) String mapContrast,
							  @RequestParam(value = "timeRange", required = false) String timeRange,
							  //查询时的暂时可变筛选条件  openFiltrate -- 只有有条件筛选时，才会使用下面的参数
							  @RequestParam(value = "openFiltrate", defaultValue = "false") Boolean openFiltrate,
							  @RequestParam(value = "emotion", required = false) String emotion,
							  @RequestParam(value = "simflag", required = false) String simflag,
							  @RequestParam(value = "wordIndex", required = false) String wordIndex,
							  @RequestParam(value = "read", required = false) String read,
							  @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
							  @RequestParam(value = "excludeWord", required = false) String excludeWord,
							  @RequestParam(value = "excludeWordIndex", defaultValue = "1", required = false) String excludeWordIndex,
							  @RequestParam(value = "updateWordForm", defaultValue = "false", required = false) Boolean updateWordForm,
							  @RequestParam(value = "wordFromNum", required = false) Integer wordFromNum,
							  @RequestParam(value = "wordFromSort", required = false) Boolean wordFromSort,
							  @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
							  @RequestParam(value = "groupName", required = false) String groupName,
							  @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
							  @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
							  @RequestParam(value = "filterInfo", required = false) String filterInfo,
							  @RequestParam(value = "contentArea", required = false) String contentArea,
							  @RequestParam(value = "mediaArea", required = false) String mediaArea,
							  @RequestParam(value = "preciseFilter", required = false) String preciseFilter)
			throws SearchException, TRSException {
		IndexTab indexTab = null;
		ChartPageInfo chartPageInfo = ChartPageInfo.valueOf(chartPage);
		if(ChartPageInfo.CustomChart.equals(chartPageInfo)){
			CustomChart customChart = columnChartService.findOneCustomChart(id);
			if(ObjectUtil.isEmpty(customChart)){
				throw new TRSException(CodeUtils.FAIL,"当前自定义图表不存在");
			}
			//待写， 需要通过自定义图表的类生成一个indextab
			indexTab = customChart.indexTab();
		}else if(ChartPageInfo.StatisticalChart.equals(chartPageInfo)){
			StatisticalChart statisticalChart = columnChartService.findOneStatisticalChart(id);
			if(ObjectUtil.isEmpty(statisticalChart)){
				throw new TRSException(CodeUtils.FAIL,"当前统计分析图表不存在");
			}
			IndexTabMapper mapper = indexTabMapperService.findOne(statisticalChart.getParentId());
			indexTab = mapper.getIndexTab();
			indexTab.setType(statisticalChart.getChartType());// TODO  舆情报告生成 饼状情感对比、活跃账号、微博热点话题时，需要处理这个地方，拿统计分析的参数填充
			StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.getStatisticalChartInfo(statisticalChart.getChartType());
			indexTab.setContrast(statisticalChartInfo.getContrast());// TODO  舆情报告生成 饼状情感对比、活跃账号、微博热点话题时，需要处理这个地方，拿统计分析的参数填充
			if(StatisticalChartInfo.WORD_CLOUD.equals(statisticalChartInfo)){
				indexTab.setTabWidth(100);
			}
		}else{
			IndexTabMapper mapper = indexTabMapperService.findOne(id);
			if(ObjectUtil.isEmpty(mapper)){
				throw new TRSException(CodeUtils.FAIL,"当前栏目不存在");
			}
			indexTab = mapper.getIndexTab();
		}
		String timerange = indexTab.getTimeRange();
		if (StringUtil.isNotEmpty(timeRange)) {
			timerange = timeRange;
		}
		IndexTabType indexTabType = ColumnFactory.chooseType(indexTab.getType());
		if(StringUtil.isNotEmpty(mapContrast) && IndexTabType.MAP.equals(indexTabType)){
			indexTab.setContrast(mapContrast);
		}
		if(openFiltrate != null && openFiltrate){
			//排重
			if ("netRemove".equals(simflag)) { //单一媒体排重
				indexTab.setSimilar(true);
				indexTab.setIrSimflag(false);
				indexTab.setIrSimflagAll(false);
			} else if ("urlRemove".equals(simflag)) { //站内排重
				indexTab.setSimilar(false);
				indexTab.setIrSimflag(true);
				indexTab.setIrSimflagAll(false);
			} else if ("sourceRemove".equals(simflag)) { //全网排重
				indexTab.setSimilar(false);
				indexTab.setIrSimflag(false);
				indexTab.setIrSimflagAll(true);
			}
			//命中规则
			if (StringUtil.isNotEmpty(wordIndex) && StringUtil.isEmpty(indexTab.getTrsl())) {
				indexTab.setKeyWordIndex(wordIndex);
			}
			indexTab.setExcludeWeb(excludeWeb);
			//排除关键词
			indexTab.setExcludeWordIndex(excludeWordIndex);
			indexTab.setExcludeWords(excludeWord);

			//修改词距 选择修改词距时，才能修改词距
			if (updateWordForm != null && updateWordForm && StringUtil.isEmpty(indexTab.getTrsl()) && wordFromNum >= 0) {
				String keywordJson = indexTab.getKeyWord();
				JSONArray jsonArray = JSONArray.parseArray(keywordJson);
				//现在词距修改情况为：只有一个关键词组时，可以修改词距等，多个时不允许
				if (jsonArray != null && jsonArray.size() == 1) {
					Object o = jsonArray.get(0);
					JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
					jsonObject.put("wordSpace", wordFromNum);
					jsonObject.put("wordOrder", wordFromSort);
					jsonArray.set(0, jsonObject);
					indexTab.setKeyWord(jsonArray.toJSONString());
				}
			}
			if (StringUtil.isNotEmpty(groupName)) {
				//现在没确定专家模式是否要数据源，暂时保留
				if ("ALL".equals(groupName)) {
					groupName = Const.ALL_GROUP;
				}
				indexTab.setGroupName(groupName);
			}
		}

		int pageSize = 10;
		//通栏下的站点统计和微信公众号改为15
		if ("100".equals(indexTab.getTabWidth()) && StringUtil.isNotEmpty(indexTab.getContrast()) && (indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_SITE) || indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_WECHAT))) {
			pageSize = 15;
		}
		AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
		ColumnConfig config = new ColumnConfig();
		if(openFiltrate != null && openFiltrate){
			config.initSection(indexTab, timerange, 0, pageSize, null, emotion, entityType, "", "", "default", "", "",
					"", "",read, mediaLevel, mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea, preciseFilter);
		}else{
			config.initSection(indexTab, timerange, 0, pageSize, null, emotion, entityType, "", "", "default",  "", "",
					"", "",read, indexTab.getMediaLevel(), indexTab.getMediaIndustry(), indexTab.getContentIndustry(), indexTab.getFilterInfo(),
					indexTab.getContentArea(), indexTab.getMediaArea(), preciseFilter);
		}
		// TODO  舆情报告生成 饼状情感对比、活跃账号、微博热点话题时，需要处理这个地方，用统计分析的StatisticalChart，其他图用tabChart
		config.setChartPage(chartPageInfo);
		config.setShowType(showType);
		column.setDistrictInfoService(districtInfoService);
		column.setCommonListService(commonListService);
		column.setCommonChartService(commonChartService);
		column.setConfig(config);
		//因折线图 关系 需要将时间参数往后传
		return column.getColumnData(timerange);
	}

	public void tradition(String[] tradition, QueryBuilder indexBuilder, QueryBuilder countBuiler,
						  String invitationCard, String source) {
		if ("国内新闻".equals(tradition[0])) {
			String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 ").toString();
			indexBuilder.filterByTRSL(trsl);
			countBuiler.filterByTRSL(trsl);
		} else if ("国内论坛".equals(tradition[0])) {
			StringBuffer sb = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 ");
			if ("0".equals(invitationCard)) {// 主贴
				sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
			} else if ("1".equals(invitationCard)) {// 回帖
				sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
			}
			indexBuilder.filterByTRSL(sb.toString());
			countBuiler.filterByTRSL(sb.toString());
		} else if ("国内新闻_手机客户端".equals(tradition[0])) {
			String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻_手机客户端").toString();
			indexBuilder.filterByTRSL(trsl);
			countBuiler.filterByTRSL(trsl);
		} else if ("境外媒体".equals(tradition[0])) {
			// String trsl = new
			// StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国外新闻
			// OR").append(":国外新闻_敏感 OR")
			// .append(":国外博客 OR").append(":国外论坛").toString();
			String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国外新闻").toString();
			indexBuilder.filterByTRSL(trsl);
			countBuiler.filterByTRSL(trsl);
		} else {
			indexBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
			countBuiler.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
		}
	}

	public void source(QueryBuilder indexBuilder, QueryBuilder countBuiler, String invitationCard, String source) {
		if ("国内新闻".equals(source)) {
			String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 ").toString();
			indexBuilder.filterByTRSL(trsl);
			countBuiler.filterByTRSL(trsl);
		} else if ("国内论坛".equals(source)) {
			StringBuffer sb = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 ");
			if ("0".equals(invitationCard)) {// 主贴
				sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
			} else if ("1".equals(invitationCard)) {// 回帖
				sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
			}
			indexBuilder.filterByTRSL(sb.toString());
			countBuiler.filterByTRSL(sb.toString());
		} else if ("国内新闻_手机客户端".equals(source)) {
			String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻_手机客户端").toString();
			indexBuilder.filterByTRSL(trsl);
			countBuiler.filterByTRSL(trsl);
		} else if ("境外媒体".equals(source)) {
			String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国外新闻").toString();
			indexBuilder.filterByTRSL(trsl);
			countBuiler.filterByTRSL(trsl);
		} else {
			indexBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
			countBuiler.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
		}
	}

	/**
	 * 原日常监测栏目进入列表页 预计做成同一列表页(分组)
	 *
	 * @param id
	 * @param source
	 * @param sort
	 * @param emotion
	 * @param
	 * @param timeRange
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws TRSException
	 *             xiaoying
	 * @throws SearchException
	 */
	//此接口不能加redis缓存  会导致文章删除功能无效
	@FormatResult
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	@Log(systemLogOperation = SystemLogOperation.COLUMN_SELECT_INDEX_TAB_DATA, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "查看二级栏目（图表）更多数据：${id}")
	@ApiOperation("进入到列表页")
	public Object list(
			@ApiParam("日常监测栏目或图表的id") @RequestParam(value = "id", required = false) String id,
			@ApiParam("图的页面类型") @RequestParam(value = "chartPage", required = false) String chartPage,
			@ApiParam("来源") @RequestParam(value = "source", defaultValue = "ALL") String source,
			@ApiParam("分类占比和单一媒体时 用于取得xy轴对应的表达式") @RequestParam(value = "key", required = false) String key,
			/*@ApiParam("地域名（点地图进去）") @RequestParam(value = "area", defaultValue = "ALL") String area,
			@ApiParam("词云点击进去") @RequestParam(value = "irKeyword", defaultValue = "ALL") String irKeyword,*/
			@ApiParam("折线图 数据时间") @RequestParam(value = "dateTime", required = false) String dateTime,
			@ApiParam("通用：keywords；人物：people；地域：location；机构：agency，当前点击的词的性质，不是当前图的性质") @RequestParam(value = "entityType", defaultValue = "keywords") String entityType,
			@ApiParam("对比类型，地域图需要，通过文章还是媒体地域") @RequestParam(value = "mapContrast", required = false) String mapContrast,
			@ApiParam("排序方式") @RequestParam(value = "sort", defaultValue = "default") String sort,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
			@ApiParam("结果中搜索de范围")@RequestParam(value = "fuzzyValueScope", defaultValue = "fullText",required = false) String fuzzyValueScope,
			@ApiParam("页数") @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
			@ApiParam("一页多少条") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
			@ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
			// 只有有条件筛选时，才会使用下面的参数 - 例如统计分析传下面的参数，其他时候不传
			@ApiParam("是否启用页面中条件筛选的条件") @RequestParam(value = "openFiltrate",defaultValue = "false") Boolean openFiltrate,
			@ApiParam("排重规则  -  替换栏目条件") @RequestParam(value = "simflag", required = false) String simflag,
			@ApiParam("关键词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "wordIndex", required = false) String wordIndex,
			@ApiParam("情感倾向") @RequestParam(value = "emotion", required = false) String emotion,
			@ApiParam("阅读标记") @RequestParam(value = "read", required = false) String read,
			@ApiParam("排除网站  替换栏目条件") @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@ApiParam("排除关键词  替换栏目条件") @RequestParam(value = "excludeWord", required = false) String excludeWord,
			@ApiParam("排除词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "excludeWordIndex",defaultValue ="1",required = false) String excludeWordIndex,
			@ApiParam("修改词距标记 替换栏目条件") @RequestParam(value = "updateWordForm",defaultValue = "false",required = false) Boolean updateWordForm,
			@ApiParam("词距间隔字符 替换栏目条件") @RequestParam(value = "wordFromNum", required = false) Integer wordFromNum,
			@ApiParam("词距是否排序  替换栏目条件") @RequestParam(value = "wordFromSort", required = false) Boolean wordFromSort,
			@ApiParam("媒体等级") @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
			@ApiParam("数据源  替换栏目条件") @RequestParam(value = "groupName", required = false) String groupName,
			@ApiParam("媒体行业") @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
			@ApiParam("内容行业") @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
			@ApiParam("信息过滤") @RequestParam(value = "filterInfo", required = false) String filterInfo,
			@ApiParam("信息地域") @RequestParam(value = "contentArea", required = false) String contentArea,
			@ApiParam("媒体地域") @RequestParam(value = "mediaArea", required = false) String mediaArea,
			@ApiParam("精准筛选") @RequestParam(value = "preciseFilter", required = false) String preciseFilter)
			throws TRSException, SearchException {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		IndexTab indexTab = null;
		ChartPageInfo chartPageInfo = ChartPageInfo.valueOf(chartPage);
		if(ChartPageInfo.CustomChart.equals(chartPageInfo)){
			CustomChart customChart = columnChartService.findOneCustomChart(id);
			if(ObjectUtil.isEmpty(customChart)){
				throw new TRSException(CodeUtils.FAIL,"当前自定义图表不存在");
			}
			//待写， 需要通过自定义图表的类生成一个indextab
			indexTab = customChart.indexTab();
		}else if(ChartPageInfo.StatisticalChart.equals(chartPageInfo)){
			StatisticalChart statisticalChart = columnChartService.findOneStatisticalChart(id);
			if(ObjectUtil.isEmpty(statisticalChart)){
				throw new TRSException(CodeUtils.FAIL,"当前统计分析图表不存在");
			}
			IndexTabMapper mapper = indexTabMapperService.findOne(statisticalChart.getParentId());
			indexTab = mapper.getIndexTab();
			indexTab.setType(statisticalChart.getChartType());
			StatisticalChartInfo statisticalChartInfo = StatisticalChartInfo.getStatisticalChartInfo(statisticalChart.getChartType());
			indexTab.setContrast(statisticalChartInfo.getContrast());
			if(StatisticalChartInfo.WORD_CLOUD.equals(statisticalChartInfo)){
				indexTab.setTabWidth(100);
			}
		}else{
			IndexTabMapper mapper = indexTabMapperService.findOne(id);
			if(ObjectUtil.isEmpty(mapper)){
				throw new TRSException(CodeUtils.FAIL,"当前栏目不存在");
			}
			indexTab = mapper.getIndexTab();
		}
		if(StringUtil.isNotEmpty(timeRange)){
			indexTab.setTimeRange(timeRange);
		}
		IndexTabType indexTabType = ColumnFactory.chooseType(indexTab.getType());

		if( "ALL".equals(source) && ObjectUtil.isNotEmpty(indexTab) && StringUtil.isNotEmpty(indexTab.getContrast())
				&& ColumnConst.CONTRAST_TYPE_GROUP.equals(indexTab.getContrast())
				&&( IndexTabType.CHART_BAR.equals(indexTabType) || IndexTabType.CHART_LINE.equals(indexTabType) || IndexTabType.CHART_PIE.equals(indexTabType))){
			source = key;
		}
		if(StringUtil.isNotEmpty(mapContrast) && IndexTabType.MAP.equals(indexTabType)){
			indexTab.setContrast(mapContrast);
		}
		if(openFiltrate != null && openFiltrate){
			//排重
			if ("netRemove".equals(simflag)) { //单一媒体排重
				indexTab.setSimilar(true);
				indexTab.setIrSimflag(false);
				indexTab.setIrSimflagAll(false);
			} else if ("urlRemove".equals(simflag)) { //站内排重
				indexTab.setSimilar(false);
				indexTab.setIrSimflag(true);
				indexTab.setIrSimflagAll(false);
			} else if ("sourceRemove".equals(simflag)) { //全网排重
				indexTab.setSimilar(false);
				indexTab.setIrSimflag(false);
				indexTab.setIrSimflagAll(true);
			}
			//命中规则
			if (StringUtil.isNotEmpty(wordIndex) && StringUtil.isEmpty(indexTab.getTrsl())) {
				indexTab.setKeyWordIndex(wordIndex);
			}
			indexTab.setExcludeWeb(excludeWeb);
			//排除关键词
			indexTab.setExcludeWordIndex(excludeWordIndex);
			indexTab.setExcludeWords(excludeWord);

			//修改词距 选择修改词距时，才能修改词距
			if (updateWordForm != null && updateWordForm && StringUtil.isEmpty(indexTab.getTrsl()) && wordFromNum >= 0) {
				String keywordJson = indexTab.getKeyWord();
				JSONArray jsonArray = JSONArray.parseArray(keywordJson);
				//现在词距修改情况为：只有一个关键词组时，可以修改词距等，多个时不允许
				if (jsonArray != null && jsonArray.size() == 1) {
					Object o = jsonArray.get(0);
					JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
					jsonObject.put("wordSpace", wordFromNum);
					jsonObject.put("wordOrder", wordFromSort);
					jsonArray.set(0, jsonObject);
					indexTab.setKeyWord(jsonArray.toJSONString());
				}
			}
			if (StringUtil.isNotEmpty(groupName)) {
				//现在没确定专家模式是否要数据源，暂时保留
				if ("ALL".equals(groupName)) {
					groupName = Const.ALL_GROUP;
				}
				indexTab.setGroupName(groupName);
			}
			return columnService.selectList(indexTab, pageNo, pageSize, source, emotion, entityType, dateTime, key,
					sort, invitationCard,forwarPrimary, fuzzyValue, fuzzyValueScope,read, mediaLevel, mediaIndustry,
					contentIndustry, filterInfo, contentArea, mediaArea, preciseFilter);
		}else{
			return columnService.selectList(indexTab, pageNo, pageSize, source, emotion, entityType, dateTime, key,
					sort, invitationCard,forwarPrimary, fuzzyValue, fuzzyValueScope,read, indexTab.getMediaLevel(), indexTab.getMediaIndustry(),
					indexTab.getContentIndustry(), indexTab.getFilterInfo(), indexTab.getContentArea(), indexTab.getMediaArea(), preciseFilter);
		}
	}

	@FormatResult
	@RequestMapping(value = "/columnList", method = RequestMethod.POST)
	@Log(systemLogOperation = SystemLogOperation.COLUMN_SELECT_INDEX_TAB_INFO, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "栏目对应信息列表页面数据查询：${id}")
	@ApiOperation("栏目对应信息列表页面数据查询")
	public Object columnList(
			@ApiParam("日常监测栏目id") @RequestParam(value = "id") String id,
			@ApiParam("来源") @RequestParam(value = "source", defaultValue = "ALL") String source,
			@ApiParam("排序方式") @RequestParam(value = "sort", defaultValue = "default") String sort,
			@ApiParam("页数") @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
			@ApiParam("一页多少条") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
			@ApiParam("结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
			@ApiParam("结果中搜索de范围")@RequestParam(value = "fuzzyValueScope", defaultValue = "fullText",required = false) String fuzzyValueScope,

			@ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
			@ApiParam("排重规则  -  替换栏目条件") @RequestParam(value = "simflag", required = false) String simflag,
			@ApiParam("关键词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "wordIndex", required = false) String wordIndex,
			@ApiParam("情感倾向") @RequestParam(value = "emotion", required = false) String emotion,
			@ApiParam("阅读标记") @RequestParam(value = "read", required = false) String read,
			@ApiParam("排除网站  替换栏目条件") @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@ApiParam("排除关键词  替换栏目条件") @RequestParam(value = "excludeWord", required = false) String excludeWord,
			@ApiParam("排除词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "excludeWordIndex",defaultValue ="1",required = false) String excludeWordIndex,
			@ApiParam("修改词距标记 替换栏目条件") @RequestParam(value = "updateWordForm",defaultValue = "false",required = false) Boolean updateWordForm,
			@ApiParam("词距间隔字符 替换栏目条件") @RequestParam(value = "wordFromNum", required = false) Integer wordFromNum,
			@ApiParam("词距是否排序  替换栏目条件") @RequestParam(value = "wordFromSort", required = false) Boolean wordFromSort,
			@ApiParam("媒体等级") @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
			@ApiParam("数据源  替换栏目条件") @RequestParam(value = "groupName", required = false) String groupName,
			@ApiParam("媒体行业") @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
			@ApiParam("内容行业") @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
			@ApiParam("信息过滤") @RequestParam(value = "filterInfo", required = false) String filterInfo,
			@ApiParam("信息地域") @RequestParam(value = "contentArea", required = false) String contentArea,
			@ApiParam("媒体地域") @RequestParam(value = "mediaArea", required = false) String mediaArea,
			@ApiParam("精准筛选") @RequestParam(value = "preciseFilter", required = false) String preciseFilter
	) throws TRSException, SearchException {

		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;

		//查询一个栏目的列表（不是通过点击图跳转的列表）时，其实就是把当前栏目当成普通列表，不受当前栏目类型的影响
		IndexTabMapper mapper = indexTabMapperService.findOne(id);
		if(ObjectUtil.isEmpty(mapper)){
			throw new TRSException(CodeUtils.FAIL,"当前栏目不存在");
		}
		IndexTab indexTab = mapper.getIndexTab();

		//时间筛选
		if(StringUtil.isNotEmpty(timeRange)){
			indexTab.setTimeRange(timeRange);
		}
		indexTab.setType(ColumnConst.LIST_NO_SIM);
		//排重
		if ("netRemove".equals(simflag)) { //单一媒体排重
			indexTab.setSimilar(true);
			indexTab.setIrSimflag(false);
			indexTab.setIrSimflagAll(false);
		} else if ("urlRemove".equals(simflag)) { //站内排重
			indexTab.setSimilar(false);
			indexTab.setIrSimflag(true);
			indexTab.setIrSimflagAll(false);
		} else if ("sourceRemove".equals(simflag)) { //全网排重
			indexTab.setSimilar(false);
			indexTab.setIrSimflag(false);
			indexTab.setIrSimflagAll(true);
		}
		//命中规则
		if (StringUtil.isNotEmpty(wordIndex) && StringUtil.isEmpty(indexTab.getTrsl())) {
			indexTab.setKeyWordIndex(wordIndex);
		}
		indexTab.setExcludeWeb(excludeWeb);
		//排除关键词
		indexTab.setExcludeWordIndex(excludeWordIndex);
		indexTab.setExcludeWords(excludeWord);

		//修改词距 选择修改词距时，才能修改词距
		if (updateWordForm != null && updateWordForm && StringUtil.isEmpty(indexTab.getTrsl()) && wordFromNum >= 0) {
			String keywordJson = indexTab.getKeyWord();
			JSONArray jsonArray = JSONArray.parseArray(keywordJson);
			//现在词距修改情况为：只有一个关键词组时，可以修改词距等，多个时不允许
			if (jsonArray != null && jsonArray.size() == 1) {
				Object o = jsonArray.get(0);
				JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
				jsonObject.put("wordSpace", wordFromNum);
				jsonObject.put("wordOrder", wordFromSort);
				jsonArray.set(0, jsonObject);
				indexTab.setKeyWord(jsonArray.toJSONString());
			}
		}
		if (StringUtil.isNotEmpty(groupName)) {
			//现在没确定专家模式是否要数据源，暂时保留
			if ("ALL".equals(groupName)) {
				groupName = Const.ALL_GROUP;
			}
			indexTab.setGroupName(groupName);
		}
		return columnService.selectList(indexTab, pageNo, pageSize, source, emotion, "", "", "",
				sort, "", "", fuzzyValue, fuzzyValueScope, read, mediaLevel, mediaIndustry,
				contentIndustry, filterInfo, contentArea, mediaArea, preciseFilter);
	}

	@FormatResult
	@RequestMapping(value = "/columnStattotal", method = RequestMethod.POST)
	@Log(systemLogOperation = SystemLogOperation.COLUMN_SELECT_INDEX_TAB_INFO, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "信息列表页的数据源统计：${id}")
	@ApiOperation("信息列表页的数据源统计")
	public Object columnStattotal(
			@ApiParam("日常监测栏目id") @RequestParam(value = "id") String id,
			@ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
			@ApiParam("排重规则  -  替换栏目条件") @RequestParam(value = "simflag", required = false) String simflag,
			@ApiParam("关键词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "wordIndex", required = false) String wordIndex,
			@ApiParam("情感倾向") @RequestParam(value = "emotion", required = false) String emotion,
			@ApiParam("阅读标记") @RequestParam(value = "read", required = false) String read,
			@ApiParam("排除网站  替换栏目条件") @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@ApiParam("排除关键词  替换栏目条件") @RequestParam(value = "excludeWord", required = false) String excludeWord,
			@ApiParam("排除词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "excludeWordIndex",defaultValue ="1",required = false) String excludeWordIndex,
			@ApiParam("修改词距标记 替换栏目条件") @RequestParam(value = "updateWordForm",defaultValue = "false",required = false) Boolean updateWordForm,
			@ApiParam("词距间隔字符 替换栏目条件") @RequestParam(value = "wordFromNum", required = false) Integer wordFromNum,
			@ApiParam("词距是否排序  替换栏目条件") @RequestParam(value = "wordFromSort", required = false) Boolean wordFromSort,
			@ApiParam("媒体等级") @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
			@ApiParam("数据源  替换栏目条件") @RequestParam(value = "groupName", required = false) String groupName,
			@ApiParam("媒体行业") @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
			@ApiParam("内容行业") @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
			@ApiParam("信息过滤") @RequestParam(value = "filterInfo", required = false) String filterInfo,
			@ApiParam("信息地域") @RequestParam(value = "contentArea", required = false) String contentArea,
			@ApiParam("媒体地域") @RequestParam(value = "mediaArea", required = false) String mediaArea,
			@ApiParam("精准筛选") @RequestParam(value = "preciseFilter", required = false) String preciseFilter)
			throws TRSException, SearchException {
		//查询一个栏目的列表（不是通过点击图跳转的列表）时，其实就是把当前栏目当成普通列表，不受当前栏目类型的影响
		IndexTabMapper mapper = indexTabMapperService.findOne(id);
		if(ObjectUtil.isEmpty(mapper)){
			throw new TRSException(CodeUtils.FAIL,"当前栏目不存在");
		}
		IndexTab indexTab = mapper.getIndexTab();

		if (StringUtil.isNotEmpty(timeRange)) {
			indexTab.setTimeRange(timeRange);
		}
		indexTab.setType(ColumnConst.LIST_NO_SIM);
		//排重
		if ("netRemove".equals(simflag)) { //单一媒体排重
			indexTab.setSimilar(true);
			indexTab.setIrSimflag(false);
			indexTab.setIrSimflagAll(false);
		} else if ("urlRemove".equals(simflag)) { //站内排重
			indexTab.setSimilar(false);
			indexTab.setIrSimflag(true);
			indexTab.setIrSimflagAll(false);
		} else if ("sourceRemove".equals(simflag)) { //全网排重
			indexTab.setSimilar(false);
			indexTab.setIrSimflag(false);
			indexTab.setIrSimflagAll(true);
		}
		//命中规则
		if (StringUtil.isNotEmpty(wordIndex) && StringUtil.isEmpty(indexTab.getTrsl())) {
			indexTab.setKeyWordIndex(wordIndex);
		}
		indexTab.setExcludeWeb(excludeWeb);
		//排除关键词
		indexTab.setExcludeWordIndex(excludeWordIndex);
		indexTab.setExcludeWords(excludeWord);

		//修改词距 选择修改词距时，才能修改词距
		if (updateWordForm != null && updateWordForm && StringUtil.isEmpty(indexTab.getTrsl()) && wordFromNum >= 0) {
			String keywordJson = indexTab.getKeyWord();
			JSONArray jsonArray = JSONArray.parseArray(keywordJson);
			//现在词距修改情况为：只有一个关键词组时，可以修改词距等，多个时不允许
			if (jsonArray != null && jsonArray.size() == 1) {
				Object o = jsonArray.get(0);
				JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
				jsonObject.put("wordSpace", wordFromNum);
				jsonObject.put("wordOrder", wordFromSort);
				jsonArray.set(0, jsonObject);
				indexTab.setKeyWord(jsonArray.toJSONString());
			}
		}
		if (StringUtil.isNotEmpty(groupName)) {
			//现在没确定专家模式是否要数据源，暂时保留
			if ("ALL".equals(groupName)) {
				groupName = Const.ALL_GROUP;
			}
			indexTab.setGroupName(groupName);
		}
		AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
		ColumnConfig config = new ColumnConfig();
		//这个页面中的筛选条件，前端会将回显数据传回来，所以直接用回显数据
		config.initSection(indexTab, indexTab.getTimeRange(), 0, 15, null, emotion, null, "", "", "default",  "", "",
				"", "",read, mediaLevel, mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea, preciseFilter);
		column.setDistrictInfoService(districtInfoService);
		column.setCommonListService(commonListService);
		column.setCommonChartService(commonChartService);
		column.setConfig(config);
		//因折线图 关系 需要将时间参数往后传
		return column.getListStattotal();
	}

	@ApiOperation("日常监测图表导出 - 所有图表都走这一个")
	@PostMapping("/exportChartData")
	public void exportChartData(HttpServletResponse response,
								@ApiParam("当前要导出的图的类型") @RequestParam(value = "chartType") String chartType,
								@ApiParam("前端给回需要导出的内容") @RequestParam(value = "data") String data) {
		try {
			IndexTabType indexTabType = ColumnFactory.chooseType(chartType);
			ServletOutputStream outputStream = response.getOutputStream();
			columnService.exportChartData(data,indexTabType).writeTo(outputStream);
		} catch (Exception e) {
			log.error("导出excel出错！", e);
		}
	}

	/**
	 * 修改历史数据  机构，用户下专题相关数据
	 * @param request
	 * @param response
	 */
	@ApiOperation("修改历史数据 机构，词云栏目来源")
	@PostMapping(value = "/changHistoryDataForWordCloudIndexTab")
	public void changHistoryDataForWordCloudIndexTab(HttpServletRequest request, HttpServletResponse response) {
		List<IndexTab> wordCloudIndexTab = indexTabService.findWordCloudIndexTab("混合");
		if (ObjectUtil.isNotEmpty(wordCloudIndexTab)){
			int i = 1;
			for (IndexTab indexTab : wordCloudIndexTab) {
				if (StringUtil.isNotEmpty(indexTab.getOrganizationId())){
					Organization organization = organizationService.findOne(indexTab.getOrganizationId());
					if (ObjectUtil.isNotEmpty(organization)){
						String dataSources = organization.getDataSources();

						if ("ALL".equals(dataSources) || StringUtil.isEmpty(dataSources)){
							indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
						}else {
							dataSources = dataSources.replace("新闻","国内新闻").replace("客户端","国内新闻_手机客户端").replace("论坛","国内论坛").replace("博客","国内博客").
									replace("电子报","国内新闻_电子报").replace("国外新闻","境外媒体").replace("国外国内新闻","境外媒体").replace(",",";");
							indexTab.setGroupName(dataSources);
						}
					}else {
						indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
					}
				}else {
					indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
				}
				System.err.println(indexTab.getGroupName());
				System.err.println("第"+i+"个栏目，栏目id："+indexTab.getId());
				i += 1;
				indexTabService.update(indexTab);
			}
		}

		List<IndexTab> wordCloudIndexTabChuan = indexTabService.findWordCloudIndexTab("传统媒体");
		if (ObjectUtil.isNotEmpty(wordCloudIndexTabChuan)){
			int i = 1;
			for (IndexTab indexTab : wordCloudIndexTabChuan) {
				if (StringUtil.isNotEmpty(indexTab.getOrganizationId())){
					Organization organization = organizationService.findOne(indexTab.getOrganizationId());
					if (ObjectUtil.isNotEmpty(organization)){
						String dataSources = organization.getDataSources();

						if ("ALL".equals(dataSources) || StringUtil.isEmpty(dataSources)){
							indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
						}else {
							dataSources = dataSources.replace("新闻","国内新闻").replace("客户端","国内新闻_手机客户端").replace("论坛","国内论坛").replace("博客","国内博客").
									replace("电子报","国内新闻_电子报").replace("国外新闻","境外媒体").replace("国外国内新闻","境外媒体").replace(",",";");
							indexTab.setGroupName(dataSources);
						}
					}else {
						indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
					}
				}else {
					indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
				}
				System.err.println(indexTab.getGroupName());
				System.err.println("第"+i+"个栏目，栏目id："+indexTab.getId());
				i += 1;
				indexTabService.update(indexTab);
			}
		}
		System.err.println("结束了===========================！");
	}

	/**
	 * 修改历史数据  机构，用户下专题相关数据
	 * @param request
	 * @param response
	 */
	@ApiOperation("修改历史数据 机构，地图栏目来源")
	@PostMapping(value = "/changHistoryDataForMapIndexTab")
	public void changHistoryDataForMapIndexTab(HttpServletRequest request, HttpServletResponse response) {
		List<IndexTab> mapIndexTab = indexTabService.findMapIndexTab("混合");
		if (ObjectUtil.isNotEmpty(mapIndexTab)){
			int i = 1;
			for (IndexTab indexTab : mapIndexTab) {
				if (StringUtil.isNotEmpty(indexTab.getOrganizationId())){
					Organization organization = organizationService.findOne(indexTab.getOrganizationId());
					if (ObjectUtil.isNotEmpty(organization)){
						String dataSources = organization.getDataSources();

						if ("ALL".equals(dataSources) || StringUtil.isEmpty(dataSources)){
							indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
						}else {
							dataSources = dataSources.replace("新闻","国内新闻").replace("客户端","国内新闻_手机客户端").replace("论坛","国内论坛").replace("博客","国内博客").
									replace("电子报","国内新闻_电子报").replace("国外新闻","境外媒体").replace("国外国内新闻","境外媒体").replace(",",";");
							indexTab.setGroupName(dataSources);
						}
					}else {
						indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
					}
				}else {
					indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
				}
				System.err.println(indexTab.getGroupName());
				System.err.println("第"+i+"个栏目，栏目id："+indexTab.getId());
				i += 1;
				indexTabService.update(indexTab);
			}
		}

		List<IndexTab> mapIndexTabChuan = indexTabService.findMapIndexTab("传统媒体");
		if (ObjectUtil.isNotEmpty(mapIndexTabChuan)){
			int i = 1;
			for (IndexTab indexTab : mapIndexTabChuan) {
				if (StringUtil.isNotEmpty(indexTab.getOrganizationId())){
					Organization organization = organizationService.findOne(indexTab.getOrganizationId());
					if (ObjectUtil.isNotEmpty(organization)){
						String dataSources = organization.getDataSources();

						if ("ALL".equals(dataSources) || StringUtil.isEmpty(dataSources)){
							indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
						}else {
							dataSources = dataSources.replace("新闻","国内新闻").replace("客户端","国内新闻_手机客户端").replace("论坛","国内论坛").replace("博客","国内博客").
									replace("电子报","国内新闻_电子报").replace("国外新闻","境外媒体").replace("国外国内新闻","境外媒体").replace(",",";");
							indexTab.setGroupName(dataSources);
						}
					}else {
						indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
					}
				}else {
					indexTab.setGroupName("国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;境外媒体");
				}
				System.err.println(indexTab.getGroupName());
				System.err.println("第"+i+"个栏目，栏目id："+indexTab.getId());
				i += 1;
				indexTabService.update(indexTab);
			}
		}
		System.err.println("结束了===========================！");
	}
	/**
	 * 修改历史数据
	 * @param request
	 * @param response
	 */
	@ApiOperation("修改历史数据 栏目关键词")
	@PostMapping(value = "/changHistoryDataForWordSpacing")
	public void changHistoryDataForWordSpacing(javax.servlet.http.HttpServletRequest request, HttpServletResponse response) {
		//查询所有用户分组
		List<IndexTab> indexTabs = indexTabService.findSimple("wordSpace");
		if (ObjectUtil.isNotEmpty(indexTabs)){
			System.err.println("普通模式栏目数："+indexTabs.size());
			for (IndexTab indexTab : indexTabs) {
				String anyKeywords = indexTab.getKeyWord();
				System.err.println("栏目关键字："+anyKeywords);
				if (StringUtil.isNotEmpty(anyKeywords)){
					Map<String, Object> hashMap = new HashMap<>();
					hashMap.put("wordSpace",0);
					hashMap.put("wordOrder",false);
					hashMap.put("keyWords",anyKeywords);
					String toJSONString = JSONObject.toJSONString(hashMap);
					indexTab.setKeyWord("["+toJSONString+"]");
					indexTabService.update(indexTab);
				}
			}
		}
		System.err.println("栏目修改成功！");
	}

}
