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
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.column.entity.NavigationEnum;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.service.*;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.ISubGroupService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.mail.search.SearchException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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
	private IndexPageRepository oneAndTwoRepository;

	@Autowired
	private IInfoListService infoListService;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private ChartAnalyzeService chartAnalyzeService;

	@Autowired
	private IDistrictInfoService districtInfoService;

	@Autowired
	private FavouritesRepository favouritesRepository;

	@Autowired
	private AlertRepository alertRepository;

	@Autowired
	private INavigationService navigationService;

	@Autowired
	private IIndexTabMapperService indexTabMapperService;

	@Autowired
	private ISubGroupService subGroupService;

	@Autowired
	private IOrganizationService organizationService;
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
	 * @param typeId
	 * @return
	 * @Return : Object
	 */
	@FormatResult
	@GetMapping(value = "/selectIndexPage")
	@ApiOperation("根据导航id检索下级栏目组")
	public Object selectIndexPage(
			@ApiParam("导航id，为空则检索默认") @RequestParam(value = "typeId", required = false) String typeId,
			@ApiParam("一级栏目分组为true") @RequestParam(value = "one", required = false) boolean one,
			@ApiParam("自定义为true") @RequestParam(value = "definedself", required = false) boolean definedself){
		return indexPageService.findByTypeId(typeId, one, definedself);
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
	@Log(systemLogOperation = SystemLogOperation.COLUMN_ADD_INDEX_PAGE, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "添加一级栏目：${typeId}/@{name}")
	@RequestMapping(value = "/addIndexPage", method = RequestMethod.GET)
	@ApiOperation("一级栏目添加接口")
	//@PreAuthorize("hasPermission(#contact, 'admin')")
	public Object addOne(@ApiParam("一级栏目名") @RequestParam("name") String name,
			@ApiParam("导航栏id(非自定义情况下不传)") @RequestParam(value = "typeId", defaultValue = "") String typeId) throws TRSException {
		User loginUser = UserUtils.getUser();
		if (UserUtils.isRoleAdmin()){
			Organization organization = organizationService.findById(loginUser.getOrganizationId());
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

		String userId = loginUser.getId();
		String subGroupId = loginUser.getSubGroupId();
		// 添加时要排序
		Sort sort = new Sort(Sort.Direction.DESC, "createdTime");
		List<IndexPage> list = new ArrayList<>();
		if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			list = indexPageService.findByUserId(userId, sort);
		}else {
			list = indexPageService.findBySubGroupId(subGroupId,sort);
		}
		if (StringUtil.isNotEmpty(name)) {
			IndexPage oneAndTwo = new IndexPage(null, name, false, typeId, list.size() + 1);
			return indexPageService.save(oneAndTwo);
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
			systemLogOperationPosition = "修改一级栏目：${indexPageId}",methodDescription="${name}")
	@RequestMapping(value = "/updateIndexPage", method = RequestMethod.GET)
	@ApiOperation("一级栏目修改接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "name", value = "一级栏目名", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "indexPageId", value = "一级栏目id", dataType = "String", paramType = "query", required = false) })
	public Object updateOne(@RequestParam("name") String name, @RequestParam("indexPageId") String indexPageId,
			HttpServletRequest request) throws OperationException {
		User user = UserUtils.getUser();
		return columnService.updateOne(user, name, indexPageId);
	}

	/**
	 * 一级栏目拖拽接口（分组）
	 * @param ids
	 * @return
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_MOVE_ONE, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "")
	@RequestMapping(value = "/moveOne", method = RequestMethod.GET)
	@ApiOperation("一级栏目拖拽接口")
	public Object moveOne(@ApiParam("id按顺序排好;分割字符串") @RequestParam("ids") String ids) {
		String[] split = ids.split(";");
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			IndexPage findOne = oneAndTwoRepository.findOne(s);
			if (ObjectUtil.isNotEmpty(findOne)) {
				findOne.setSequence(i + 1);
				oneAndTwoRepository.save(findOne);
			}
		}
		return "success";
	}

	/**
	 * 二级栏目拖拽接口(分组)
	 *
	 * @param ids
	 * @return
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.COLUMN_MOVE_TWO, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "")
	@RequestMapping(value = "/moveTwo", method = RequestMethod.GET)
	@ApiOperation("二级栏目拖拽接口")
	public Object moveTwo(@ApiParam("id按顺序排好;分割字符串") @RequestParam("ids") String ids) {
		String[] split = ids.split(";");
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			IndexTabMapper findOne = indexTabMapperService.findOne(s);
			if (ObjectUtil.isNotEmpty(findOne)) {
				findOne.setSequence(i + 1);
				indexTabMapperService.save(findOne);
			}
		}
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
	 * @param sequence
	 * @param maxSize
	 * @param timeRange
	 * @param timeRecent
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
			@ApiImplicitParam(name = "indexPageId", value = "父栏目Id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "type", value = "图表类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contrast", value = "分类对比类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "trsl", value = "检索表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "statusTrsl", value = "微博检索表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weChatTrsl", value = "检索表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keyWord", value = "关键词", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keyWordIndex", value = "关键词位置(0:标题,1:标题+正文,2:标题+摘要)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "xyTrsl", value = "XY轴检索表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupName", value = "数据来源(可多值,中间以';'隔开,默认为传统媒体)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "sequence", value = "图标位置 int数字", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "maxSize", value = "最大条数", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "timeRange", value = "发布时间范围(2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weight", value = "标题权重", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排 no，全网排 netRemove,url排 urlRemove,跨数据源排 sourceRemove", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "timeRecent", value = " 最近发布时间7d", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "server", value = "是否为server表达式  主要针对专家模式", dataType = "Boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "tabWidth", value = "栏目是不是通栏，50为半栏，100为通栏", dataType = "int", paramType = "query", required = false),
			@ApiImplicitParam(name = "share", value = "是否共享标记", dataType = "Boolean", paramType = "query", required = false) })
	public Object addThree(@RequestParam("name") String name, @RequestParam("indexPageId") String indexPageId,
			@RequestParam("type") String type, @RequestParam(value = "contrast", required = false) String contrast,
			@RequestParam(value = "trsl", required = false) String trsl,
			@RequestParam(value = "statusTrsl", required = false) String statusTrsl,
			@RequestParam(value = "weChatTrsl", required = false) String weChatTrsl,
			@RequestParam(value = "xyTrsl", required = false) String xyTrsl,
			@RequestParam(value = "keyWord", required = false) String keyWord,
			@RequestParam(value = "excludeWords", required = false) String excludeWords,
			@RequestParam(value = "keyWordIndex", required = false) String keyWordIndex,
			@RequestParam(value = "groupName", required = false, defaultValue = "传统媒体") String groupName,
			@RequestParam(value = "sequence", required = false) String sequence,
			@RequestParam(value = "maxSize", defaultValue = "10") int maxSize,
			@RequestParam(value = "timeRange", required = false) String timeRange,
			@RequestParam(value = "tradition", required = false) String tradition,
			@RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@RequestParam(value = "weight", required = false) boolean weight,
			@RequestParam(value = "simflag", required = false) String simflag,
			@RequestParam(value = "timeRecent", required = false) String timeRecent,
			@RequestParam(value = "server", required = false) boolean server,
			@RequestParam(value = "tabWidth", required = false, defaultValue = "50") int tabWidth,
			@RequestParam(value = "share", defaultValue = "false") boolean share, HttpServletRequest request)
			throws TRSException {

		//首先判断下用户权限（若为机构管理员，只受新建与编辑的权限，不受可创建资源数量的限制）
		User loginUser = UserUtils.getUser();
		if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())){
			Organization organization = organizationService.findById(loginUser.getOrganizationId());
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
				}else if (StringUtil.isNotEmpty(trsl) || StringUtil.isNotEmpty(xyTrsl) || StringUtil.isNotEmpty(weChatTrsl) || StringUtil.isNotEmpty(statusTrsl)){
					int trslCount = StringUtil.getChineseCount(trsl);
					int xyTrslCount = StringUtil.getChineseCount(xyTrsl);
					int weChatTrslCount = StringUtil.getChineseCount(weChatTrsl);
					int statusTrslCount = StringUtil.getChineseCount(statusTrsl);
					chineseCount = trslCount+xyTrslCount+weChatTrslCount+statusTrslCount;
				}
				if (chineseCount > organization.getKeyWordsNum()){
					throw new TRSException(CodeUtils.FAIL,"该栏目暂时仅支持检索"+organization.getKeyWordsNum()+"个关键字，如需更多，请联系相关运维人员。");
				}
			}
		}

		String[] splitType = type.split(";");
		String[] splitTradition = null;
		boolean mix = false;
		if (StringUtil.isNotEmpty(tradition)) {

			//现在栏目添加或修改 来源数据 均传给了 tradition  20181116
			groupName = tradition;

			splitTradition = tradition.split(";");
			// 此处是判断是当多个数据源的 groupName=国内新闻 时，前端展示要算为混合列表
//			if (splitType.length == 1 && splitTradition.length > 1) {
//				mix = true;
//			}
			//通过来源的长度判断是否为混合栏目 20181116
			if (splitTradition.length>1){
				mix = true;
			}
		}
		if (splitType.length > 1 || ColumnConst.LIST_CHAOS_DOCUMENT.equals(splitType[0]) || mix) {
			groupName = "混合";
		}
		// 关键词搜索位置 如果是微博 只搜索内容
		// 前端有的groupname传的不对
		if (StringUtil.isNotEmpty(weChatTrsl) && StringUtil.isEmpty(trsl) && StringUtil.isEmpty(statusTrsl)) {
			type = ColumnConst.LIST_WECHAT_COMMON;
		} else if (StringUtil.isNotEmpty(statusTrsl) && StringUtil.isEmpty(trsl) && StringUtil.isEmpty(weChatTrsl)) {
			type = ColumnConst.LIST_STATUS_COMMON;
		}
		if (ColumnConst.LIST_STATUS_COMMON.equals(type)) {// 微博
			groupName = "微博";
		} else if (ColumnConst.LIST_WECHAT_COMMON.equals(type)) {// 微信
			groupName = "微信";
		}
		// String userId = UserUtils.getUser().getId();
		if (ObjectUtil.isEmpty(sequence)) {
			// 通过二级和userId找这个是第几个图表
			// Criteria<IndexTab> criteria = new Criteria<>();
			// criteria.add(Restrictions.eq("userId", userId));
			// criteria.add(Restrictions.eq("parentId", indexPageId));
			// List<IndexTab> findAll = indexTabService.findAll(criteria);
			List<IndexTabMapper> mappers = indexTabMapperService.findByIndexPageId(indexPageId);
			if (ObjectUtil.isEmpty(mappers)) {
				sequence = String.valueOf(1);
			} else {
				sequence = String.valueOf(mappers.size() + 1);
			}
		}
		if (StringUtil.isEmpty(timeRange) && StringUtil.isEmpty(timeRecent)) {
			timeRecent = "7d";
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
		IndexTab indexTab = new IndexTab(name, trsl, statusTrsl, weChatTrsl, keyWord, excludeWords, keyWordIndex,
				xyTrsl, type, indexPageId, groupName, Integer.parseInt(sequence), maxSize, timeRange, timeRecent,
				 isSimilar,irSimflag, weight, server,irSimflagAll);
		indexTab.setTradition(tradition);
		indexTab.setExcludeWeb(excludeWeb);
		indexTab.setContrast(contrast);
		indexTab.setTabWidth(tabWidth);
		IndexPage indexPage = indexPageService.findOne(indexPageId);
		indexTab.setOneName(indexPage.getParentName());
		return indexTabService.save(indexTab, share);
	}

	/**
	 * 三级栏目（图表）修改接口（分组）
	 * @param indexMapperId
	 * @param name
	 * @param type
	 * @param contrast
	 * @param trsl
	 * @param statusTrsl
	 * @param weChatTrsl
	 * @param xyTrsl
	 * @param keyWord
	 * @param excludeWords
	 * @param keyWordIndex
	 * @param groupName
	 * @param sequence
	 * @param maxSize
	 * @param timeRange
	 * @param timeRecent
	 * @param tradition
	 * @param excludeWeb
	 * @param weight
	 * @param simflag
	 * @param server
	 * @param tabWidth
	 * @param share
	 * @param indexPageId
	 * @param copy
	 * @param request
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@RequestMapping(value = "/updateIndexTab", method = RequestMethod.POST)
	@Log(systemLogOperation = SystemLogOperation.COLUMN_UPDATE_INDEX_TAB, systemLogType = SystemLogType.COLUMN,
			systemLogOperationPosition = "修改二级栏目（图表）：${indexMapperId}",methodDescription="${name}")
	@ApiOperation("三级栏目（图表）修改接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indexMapperId", value = "三级栏目映射id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "name", value = "三级栏目名", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "type", value = "图表类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contrast", value = "分类对比类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "trsl", value = "检索表达式", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "statusTrsl", value = "微博检索表达式", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "weChatTrsl", value = "微信检索表达式", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "keyWord", value = "关键词", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "keyWordIndex", value = "关键词位置(0:标题,1:标题+正文,2:标题+摘要)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "xyTrsl", value = "XY轴检索表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupName", value = "数据来源(可多值,中间以';'隔开,默认为新闻)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "sequence", value = "图标位置 int数字", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "maxSize", value = "最大条数", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "timeRange", value = "发布时间范围(2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "timeRecent", value = " 最近发布时间7d", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weight", value = "标题权重", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排 no，全网排 netRemove,url排 urlRemove,跨数据源排 sourceRemove", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "server", value = "是否为server表达式  主要针对专家模式", dataType = "Boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "tabWidth", value = "栏目是不是通栏，50为半栏，100为通栏", dataType = "int", paramType = "query", required = false),
			@ApiImplicitParam(name = "share", value = "栏目共享标记", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "indexPageId", value = "栏目另存为目标栏目组id", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "copy", value = "另存为标记", dataType = "boolean", paramType = "query", required = false) })
	public Object updateThree(@RequestParam("indexMapperId") String indexMapperId, @RequestParam("name") String name,
			@RequestParam("type") String type, @RequestParam(value = "contrast", required = false) String contrast,
			@RequestParam(value = "trsl", required = false) String trsl,
			@RequestParam(value = "statusTrsl", required = false) String statusTrsl,
			@RequestParam(value = "weChatTrsl", required = false) String weChatTrsl,
			@RequestParam(value = "xyTrsl", required = false) String xyTrsl,
			@RequestParam(value = "keyWord", required = false) String keyWord,
			@RequestParam(value = "excludeWords", required = false) String excludeWords,
			@RequestParam(value = "keyWordIndex", required = false) String keyWordIndex,
			@RequestParam(value = "groupName", required = false, defaultValue = "传统媒体") String groupName,
			@RequestParam(value = "sequence", required = false) int sequence,
			@RequestParam(value = "maxSize", defaultValue = "10") int maxSize,
			@RequestParam(value = "timeRange", required = false) String timeRange,
			@RequestParam(value = "timeRecent", required = false) String timeRecent,
			@RequestParam(value = "tradition", required = false) String tradition,
			@RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@RequestParam(value = "weight", required = false) boolean weight,
			@RequestParam(value = "simflag", required = false) String simflag,
			@RequestParam(value = "server", required = false) boolean server,
			@RequestParam(value = "tabWidth", required = false, defaultValue = "50") int tabWidth,
			@RequestParam(value = "share", defaultValue = "false") boolean share,
			@RequestParam(value = "indexPageId", defaultValue = "false") String indexPageId,
			@RequestParam(value = "copy", defaultValue = "false") boolean copy, HttpServletRequest request)
			throws TRSException {

		User loginUser = UserUtils.getUser();
		//若为机构管理员或者普通用户 若为普通模式，判断关键字字数
		if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())){
			Organization organization = organizationService.findById(loginUser.getOrganizationId());
			int chineseCount = 0;
			if (StringUtil.isNotEmpty(keyWord)){
				chineseCount = StringUtil.getChineseCountForSimple(keyWord);
			}else if (StringUtil.isNotEmpty(trsl) || StringUtil.isNotEmpty(xyTrsl) || StringUtil.isNotEmpty(weChatTrsl) || StringUtil.isNotEmpty(statusTrsl)){
				int trslCount = StringUtil.getChineseCount(trsl);
				int xyTrslCount = StringUtil.getChineseCount(xyTrsl);
				int weChatTrslCount = StringUtil.getChineseCount(weChatTrsl);
				int statusTrslCount = StringUtil.getChineseCount(statusTrsl);
				chineseCount = trslCount+xyTrslCount+weChatTrslCount+statusTrslCount;
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
			if (StringUtil.isEmpty(timeRange) && StringUtil.isEmpty(timeRecent)) {
				timeRecent = "7d";
			}
			String[] splitType = type.split(";");
			String[] splitTradition = null;
			boolean mix = false;
			if (StringUtil.isNotEmpty(tradition)) {

				//现在栏目添加或修改 来源数据 均传给了 tradition  20181116
				groupName = tradition;

				splitTradition = tradition.split(";");
				// 此处是判断是当多个数据源的 groupName=国内新闻 时，前端展示要算为混合列表
//				if (splitType.length == 1 && splitTradition.length > 1) {
//					mix = true;
//				}
				//通过来源的长度判断是否为混合栏目 20181116
				if (splitTradition.length>1){
					mix = true;
				}
			}
			if (splitType.length > 1 || ColumnConst.LIST_CHAOS_DOCUMENT.equals(splitType[0]) || mix) {
				groupName = "混合";
			}
			if (ColumnConst.LIST_STATUS_COMMON.equals(type)) {// 微博
				groupName = "微博";
			} else if (ColumnConst.LIST_WECHAT_COMMON.equals(type)) {// 微信
				groupName = "微信";
			}
			// 柱状图时必须传xy表达式
			if ("bar-graph-chart".equals(type) && StringUtil.isEmpty(xyTrsl)) {
				throw new OperationException("柱状图时必须传xy表达式");
			}
			IndexTabMapper mapper = indexTabMapperService.findOne(indexMapperId);
			IndexTab indexTab = mapper.getIndexTab();
			if (copy) { // 如果为另存为，则将持久态对象转换为瞬时对象
				indexTab = indexTab.tabCopy();
			}
            //判断原来数据是否含有至今，并没有修改数据
			//不做3个月限制，具体查询范围，只以机构查询范围为准
//            if (timeRange.indexOf("至今") == -1) {
//                String old_timeRange = indexTab.getTimeRange();
//                if (old_timeRange.indexOf("至今") != -1) {
//                    String old_time = DateUtil.getStartToThreeMonth(old_timeRange);
//                    if (timeRange.equals(old_time)) {
//                        timeRange = old_timeRange;
//                    }
//                }
//            }
			indexTab.setName(name);
			indexTab.setType(type);
			indexTab.setContrast(contrast);
			indexTab.setTrsl(trsl);
			indexTab.setWeChatTrsl(weChatTrsl);
			indexTab.setStatusTrsl(statusTrsl);
			indexTab.setKeyWord(keyWord);
			indexTab.setExcludeWords(excludeWords);
			indexTab.setKeyWordIndex(keyWordIndex);
			indexTab.setXyTrsl(xyTrsl);
			indexTab.setGroupName(groupName);
			indexTab.setSequence(sequence);
			indexTab.setMaxSize(maxSize);
			indexTab.setTimeRange(timeRange);
			indexTab.setTimeRecent(timeRecent);
			indexTab.setTradition(tradition);
			indexTab.setExcludeWeb(excludeWeb);
			indexTab.setSimilar(isSimilar);
			indexTab.setIrSimflag(irSimflag);
			indexTab.setTabWidth(tabWidth);
			indexTab.setIrSimflagAll(irSimflagAll);
			// 栏目从标题+正文修改为仅标题的时候不设置权重，但传的weight还是=true
			if ("0".equals(keyWordIndex)) {
				indexTab.setWeight(false);
			} else {
				indexTab.setWeight(weight);
			}
			indexTab.setServer(server);
			IndexPage indexPage = null;
			// 根据另存为标识选择另存为与修改操作
			if (copy) {
				indexPage = this.indexPageService.findOne(indexPageId);
				indexTab = indexTab.tabCopy();
				indexTab.setParentId(indexPageId);
				long total = indexTabMapperService.countByIndexPage(indexPage);
				indexTab.setSequence((int) total + 1);
				indexTab.clear();
				mapper = (IndexTabMapper) indexTabService.save(indexTab, share);
				indexMapperId = mapper.getId();
			}
			indexTabService.update(indexTab, indexMapperId, share);
			List<IndexTabMapper> cacheMapper = indexTabMapperService.findByIndexTab(indexTab);
			for (IndexTabMapper indexTabMapper : cacheMapper) {
				RedisFactory.deleteAllKey(indexTabMapper.getId());
			}
			indexTab.setOneName(mapper.getIndexPage().getParentName());
			return mapper;
		} catch (Exception e) {
			log.error("三级栏目修改报错", e);
			throw new OperationException("三级栏目修改报错", e);
		}
	}

	/**
	 * 三级栏目（图表）修改半栏通栏属性（分组）
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
	 * 删除的时候要改变排序（分组）
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

	//显示隐藏合成一个接口
	/*@FormatResult
	@RequestMapping(value = "/hideIndexTab", method = RequestMethod.POST)
	@Log(systemLogOperation = SystemLogOperation.COLUMN_HIDE_INDEX_TAB, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "隐藏二级栏目（图表）：${indexMapperId}")
	@ApiOperation("三级栏目（图表）隐藏接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indexMapperId", value = "三级栏目映射id", dataType = "String", paramType = "query") })
	public Object hideThree(@RequestParam("indexMapperId") String indexMapperId, HttpServletRequest request)
			throws TRSException {
		//id用;分割
		indexTabMapperService.hide(indexMapperId, true);
		return indexTabMapperService.findOne(indexMapperId);
	}*/

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
	@RequestMapping(value = "/deleteIndexPage", method = RequestMethod.GET)
	@ApiOperation("一级栏目删除接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indexPageId", value = "一级栏目id", dataType = "String", paramType = "query") })
	public Object deleteOne(@ApiParam("一级栏目id") @RequestParam("indexPageId") String indexPageId,
			HttpServletRequest request) throws OperationException {
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
	 * @param indexMapperId
	 * @param timeRange
	 * @param grouName
	 * @param mix
	 * @param emotion
	 * @param tradition
	 * @param excludeWeb
	 * @param showType
	 * @param entityType
	 * @return
	 * @throws SearchException
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.COLUMN_SELECT_INDEX_TAB_DATA, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "查看二级栏目（图表）：${indexMapperId}")
	@FormatResult
	//@EnableRedis
	@RequestMapping(value = "/selectChart", method = RequestMethod.GET)
	@ApiOperation("查找三级栏目（图表 ）")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indexMapperId", value = "三级栏目关系映射id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "timeRange", value = "按时间查询 (2017-10-01 00:00:00;2017-10-20 00:00:00)", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "groupName", value = "数据来源(传统媒体 or 微博 or 微信)", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "entityType", value = "通用：keywords；人物：people；地域：location；机构：agency", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mix", value = "判断是否是混合大列表 true 是跳大列表  false 栏目块", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "emotion", value = "混合大列表情感筛选", dataType = "String", paramType = "query", defaultValue = "ALL"),
			@ApiImplicitParam(name = "tradition", value = "传统媒体的子参数", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "showType", value = "指定折线图的展示方式：按小时，按天数", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWeb", value = "针对传统媒体的siteName字段做排除", dataType = "String", paramType = "query", required = false) })
	public Object selectChart(@RequestParam("indexMapperId") String indexMapperId,
			@RequestParam(value = "timeRange", required = false) String timeRange,
			@RequestParam(value = "groupName", required = false) String grouName,
			@RequestParam(value = "mix", defaultValue = "false", required = false) boolean mix,
			@RequestParam(value = "emotion", required = false) String emotion,
			@RequestParam(value = "tradition", required = false) String tradition,
			@RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@RequestParam(value = "showType", required = false,defaultValue = "") String showType,
			@RequestParam(value = "entityType", defaultValue = "keywords") String entityType)
			throws SearchException, TRSException {
		IndexTabMapper mapper = indexTabMapperService.findOne(indexMapperId);
		IndexTab indexTab = mapper.getIndexTab();
		String timerange = indexTab.getTimeRange();
		if (StringUtil.isNotEmpty(timeRange)) {
			timerange = timeRange;
		}
		//至今 不再做3个月的限制
       // timerange = DateUtil.getStartToThreeMonth(timerange);
		/*
		 * String[] splitTradition = tradition.split(";"); String[]
		 * splitExcludeWeb = excludeWeb.split(";");
		 */
		int pageSize=10;
		//通栏下的站点统计和微信公众号改为15
		if ("100".equals(indexTab.getTabWidth()) && StringUtil.isNotEmpty(indexTab.getContrast()) && (indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_SITE) || indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_WECHAT))){
			pageSize=15;
		}
		AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
		ColumnConfig config = new ColumnConfig();
		// config.init(indexTab, timerange, 0, 10 , entityType,"","");
		config.initSection(indexTab, timerange, 0, pageSize, grouName, emotion, entityType,"", "", "default", "", "", "", "",
				"","");
		config.setShowType(showType);
		column.setHybase8SearchService(hybase8SearchService);
		column.setChartAnalyzeService(chartAnalyzeService);
		column.setInfoListService(infoListService);
		column.setDistrictInfoService(districtInfoService);
		column.setAlertRepository(alertRepository);
		column.setFavouritesRepository(favouritesRepository);
		column.setConfig(config);
		if (mix) {
			return column.getSectionList(); //混合大列表报错
		} else {
			//因折线图 关系 需要将时间参数往后传
			return column.getColumnData(timerange);
		}

		// return columnService.selectChart(indexTab, timeArray, grouName,
		// entityType, mix, emotion, 0, timeRange);
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
	 * @param indexMapperId
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
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@Log(systemLogOperation = SystemLogOperation.COLUMN_SELECT_INDEX_TAB_DATA, systemLogType = SystemLogType.COLUMN, systemLogOperationPosition = "查看二级栏目（图表）更多数据：${indexMapperId}")
	@ApiOperation("进入到列表页")
	public Object list(
			@ApiParam("日常监测栏目id") @RequestParam(value = "indexMapperId", required = false) String indexMapperId,
			@ApiParam("热搜关键词/热搜人物") @RequestParam(value = "hotKeywords", required = false) String hotKeywords,
			@ApiParam("来源") @RequestParam(value = "source", defaultValue = "ALL") String source,
			@ApiParam("分类占比和单一媒体时 用于取得xy轴对应的表达式") @RequestParam(value = "key", required = false) String key,
			@ApiParam("地域名（点地图进去）") @RequestParam(value = "area", defaultValue = "ALL") String area,
			@ApiParam("词云点击进去") @RequestParam(value = "irKeyword", defaultValue = "ALL") String irKeyword,
			@ApiParam("折线图 数据时间") @RequestParam(value = "dateTime", required = false) String dateTime,
			@ApiParam("通用：keywords；人物：people；地域：location；机构：agency") @RequestParam(value = "entityType", defaultValue = "keywords") String entityType,
			@ApiParam("排序方式") @RequestParam(value = "sort", defaultValue = "default") String sort,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("情感") @RequestParam(value = "emotion", defaultValue = "ALL") String emotion,
			@ApiParam("行业") @RequestParam(value = "industry", defaultValue = "ALL") String industry,
			@ApiParam("结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
			@ApiParam("结果中搜索de范围")@RequestParam(value = "fuzzyValueScope", defaultValue = "fullText",required = false) String fuzzyValueScope,
			@ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
			@ApiParam("页数") @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
			@ApiParam("一页多少条") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
			@ApiParam("是否导出") @RequestParam(value = "isExport", defaultValue = "false") boolean isExport)
			throws TRSException, SearchException {// 为了防止前台的来源名和数据库里边不对应
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		return columnService.selectList(indexMapperId, pageNo, pageSize, source, emotion, entityType, dateTime, key,
				sort, area, irKeyword, invitationCard, fuzzyValue, fuzzyValueScope,forwarPrimary, isExport);
	}

	/**
	 * @Desc 针对传统 查相似文章列表（分组）
	 */
	@FormatResult
	@ApiOperation("相似文章列表 做统一列表  返回格式和日常监测跳的列表页一样")
	@RequestMapping(value = "/colListsim", method = RequestMethod.GET)
	public Object simList(@ApiParam("来源") @RequestParam("source") String source,
			@ApiParam("md5标示") @RequestParam(value = "md5Tag", required = false) String md5Tag,
			@ApiParam("表达式") @RequestParam("trslk") String trslk,
			@ApiParam("排序方式") @RequestParam(value = "sort", defaultValue = "default") String sort,
			@ApiParam("情感") @RequestParam(value = "emotion", defaultValue = "ALL") String emotion,
			@ApiParam("结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
			@ApiParam("在结果中搜索de范围") @RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope,
			@ApiParam("针对论坛  主贴 0/回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("页数") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@ApiParam("一页多少条") @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize)
			throws TRSException {
		Boolean irsimflag = true; // 相似文章计算  要先站内排重之后再筛选  为true  要站内排重
		boolean sim = false;// 默认走相似文章列表
		trslk = RedisUtil.getString(trslk);
		// 去掉排重处理
		if (StringUtil.isNotEmpty(trslk)) {
			trslk = removeSimflag(trslk);
			trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
		}
		log.info("redis" + trslk);
		// 时间倒叙
		QueryBuilder builder = new QueryBuilder();
		builder.page(pageNo, pageSize);
		builder.filterByTRSL(trslk);
		if (StringUtil.isNotEmpty(md5Tag)) {
			builder.filterChildField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
		} else {// 若MD5为空 则走推荐文章列表
			sim = false;
		}
		
		QueryBuilder countBuilder = new QueryBuilder();// 算数的
		countBuilder.filterByTRSL(trslk);
		if (StringUtil.isNotEmpty(md5Tag)) {
			countBuilder.filterChildField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
		}
		
		// 为热点豆腐块进列表支持主贴回帖筛选而加
		if("国内论坛".equals(source)){
			//可以加主回帖筛选，但是不要加groupName字段，会限制查询条数
			StringBuffer sb = new StringBuffer();
			if ("0".equals(invitationCard)) {// 主贴
				sb.append(Const.NRESERVED1_LUNTAN);
			} else if ("1".equals(invitationCard)) {// 回帖
				sb.append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
			}
			if(StringUtil.isNotEmpty(sb.toString())){
                builder.filterByTRSL(sb.toString());
                countBuilder.filterByTRSL(sb.toString());
            }
		}
		
		User loginUser = UserUtils.getUser();
		if (!"ALL".equals(emotion)) { // 情感
			if("中性".equals(emotion)){
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, "(\"正面\" OR \"负面\")", Operator.NotEqual);
			}else {
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}
		}
		if (StringUtil.isNotEmpty(trslk) && trslk.contains(
				"(IR_SITENAME:(\"新华网\" OR \"中国网\" OR \"央视网\"  OR \"中国新闻网\" OR  \"新浪网\" OR  \"网易\" OR \"搜狐网\" OR  \"凤凰网\") "
						+ "NOT IR_CHANNEL:(游戏)" + "AND IR_GROUPNAME:国内新闻 )NOT IR_URLTITLE:(吴君如 OR 汽车 OR 新车 OR 优惠)")) {
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {
				StringBuffer trsl = new StringBuffer();
				switch (fuzzyValueScope){
					case "title":
						trsl.append(FtsFieldConst.FIELD_TITLE).append(":").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
								.replaceAll("[;|；]+","\" OR \""));
						break;
					case "source":
						trsl.append(FtsFieldConst.FIELD_SITENAME).append(":").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
								.replaceAll("[;|；]+","\" OR \""));
						break;
					case "author":
						trsl.append(FtsFieldConst.FIELD_AUTHORS).append(":").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
								.replaceAll("[;|；]+","\" OR \""));
						break;
					case "fullText":
						trsl.append(FtsFieldConst.FIELD_TITLE).append(":").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(") OR ("+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND \"")
								.replaceAll("[;|；]+","\" OR \"")).append("\"))");
						break;
				}
				builder.filterByTRSL(trsl.toString());
				countBuilder.filterByTRSL(trsl.toString());
			}
			log.info(builder.asTRSL());
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotList(builder, countBuilder, loginUser,"column");
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			return infoListService.getDocList(builder, loginUser, sim, irsimflag, false,false,"column");
		} else {
			if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {
				StringBuffer trsl = new StringBuffer();
				switch (fuzzyValueScope){
					case "title":
						trsl.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
								.replaceAll("[;|；]+","\" OR \"")).append("\"))");
						break;
					case "source":
						trsl.append(FtsFieldConst.FIELD_SITENAME).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
								.replaceAll("[;|；]+","\" OR \"")).append("\"))");
						break;
					case "author":
						trsl.append(FtsFieldConst.FIELD_AUTHORS).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
								.replaceAll("[;|；]+","\" OR \"")).append("\"))");
						break;
					case "fullText":
						trsl.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
								.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(") OR ("+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
								.replaceAll("[;|；]+","\" OR \"")).append("\"))");
						break;
				}
				builder.filterByTRSL(trsl.toString());
				countBuilder.filterByTRSL(trsl.toString());
			}
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotList(builder, countBuilder, loginUser,"column");
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			log.info(builder.asTRSL());
			if (StringUtil.isNotEmpty(source) && source.equals("微博")) {
				return infoListService.getStatusList(builder, loginUser, sim, irsimflag, false,false,"column");
			}
			return infoListService.getDocList(builder, loginUser, sim, irsimflag, false,false,"column");
		}
	}

	private String removeSimflag(String trslk) {
		trslk = trslk.replace(" AND (SIMFLAG:(1000 OR \"\"))", "");
		trslk = trslk.replace(" AND SIMFLAG:(1000 OR \"\")", "");
		trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
		if(trslk.indexOf("AND SIMFLAG:(1000 OR \"\")") != -1){
			trslk = trslk.replace(" AND SIMFLAG:(1000 OR \"\")","");
		}else if (trslk.indexOf("AND (IR_SIMFLAGALL:(\"0\" OR \"\"))") != -1){
			trslk = trslk.replace(" AND (IR_SIMFLAGALL:(\"0\" OR \"\"))","");
		}
		return trslk;
	}

	/**
	 * @Desc 针对热点微信 查看相似文章列表（分组）
	 */
	@FormatResult
	@ApiOperation("热点微信相似文章列表，该页不再计算相似文章数，不再做排序")
	@RequestMapping(value = "/colListsimForWechat", method = RequestMethod.GET)
	public Object simListForWechat(@ApiParam("md5标示") @RequestParam(value = "md5Tag", required = false) String md5Tag,
			@ApiParam("表达式") @RequestParam("trslk") String trslk,
			@ApiParam("排序方式") @RequestParam(value = "sort", defaultValue = "default") String sort,
			@ApiParam("情感") @RequestParam(value = "emotion", defaultValue = "ALL") String emotion,
			@ApiParam("结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
			@ApiParam("页数") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@ApiParam("一页多少条") @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize)
			throws TRSException {
		Boolean irsimflag = true; // 相似文章计算  要先站内排重之后再筛选  为true  要站内排重
		boolean sim = false;// 默认走相似文章列表
		trslk = RedisUtil.getString(trslk);
		if (StringUtil.isNotEmpty(trslk)) {
			trslk = removeSimflag(trslk);
			trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
		}

		log.info("redis" + trslk);// 记录此处取出的表达式
		QueryBuilder builder = new QueryBuilder();
		builder.page(pageNo, pageSize);
		builder.filterByTRSL(trslk);
		if (StringUtil.isNotEmpty(md5Tag)) {
			builder.filterChildField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
		} else {
			sim = false;
		}
		if (!"ALL".equals(emotion)) { // 情感
			if("中性".equals(emotion)){
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, "(\"正面\" OR \"负面\")", Operator.NotEqual);
			}else {
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}
		}
		switch (sort) { // 排序 不做热点排序，本身就是热点，不做
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
		}
		User loginUser = UserUtils.getUser();
		log.info(builder.asTRSL());
		return infoListService.getHotWechatSimListDetail(builder, loginUser, sim, irsimflag,false,"column");
	}

	/**
	 * @Desc 针对热点微博 查看相似文章列表（分组）
	 */
	@FormatResult
	@ApiOperation("热点微博相似文章列表，该页不再计算相似文章数")
	@RequestMapping(value = "/colListsimForStatus", method = RequestMethod.GET)
	public Object simListForStatus(@ApiParam("md5标示") @RequestParam(value = "md5Tag", required = false) String md5Tag,
			@ApiParam("表达式") @RequestParam("trslk") String trslk,
			@ApiParam("正负面") @RequestParam(value = "emotion", defaultValue = "ALL", required = false) String emotion,
			@ApiParam("排序方式") @RequestParam(value = "sort", defaultValue = "default") String sort,
			@ApiParam("结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@ApiParam("页数") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@ApiParam("一页多少条") @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize)
			throws TRSException {
		Boolean irsimflag = true; // 相似文章计算  要先站内排重之后再筛选  为true  要站内排重
		boolean sim = false;// 默认走相似文章列表
		trslk = RedisUtil.getString(trslk);
		if (StringUtil.isNotEmpty(trslk)) {
			trslk = removeSimflag(trslk);
			trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
		}

		log.info("redis" + trslk);// 记录此处用到的表达式
		QueryBuilder builder = new QueryBuilder();
		builder.page(pageNo, pageSize);
		builder.filterByTRSL(trslk);
		if (StringUtil.isNotEmpty(md5Tag)) {
			builder.filterChildField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
		} else {
			sim = false;
		}
		StringBuilder builderTrsl = new StringBuilder(builder.asTRSL());
		if ("primary".equals(forwarPrimary)) {
			// 原发
			builder.filterByTRSL(Const.PRIMARY_WEIBO);
		} else if ("forward".equals(forwarPrimary)) {
			// 转发
			builder = new QueryBuilder();
			builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
			builder.filterByTRSL(builderTrsl.toString());
			builder.page(pageNo, pageSize);
		}
		// ALL = ((NOT 正面) && (NOT 负面)) + 正面 + 负面
		if (!"ALL".equals(emotion)) { // 情感
			if("中性".equals(emotion)){
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, "(\"正面\" OR \"负面\")", Operator.NotEqual);
			}else {
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}
		}
		User loginUser = UserUtils.getUser();
		log.info(builder.asTRSL());
		switch (sort) { // 排序 不做热点排序，本身就是热点，不做
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
		}
		return infoListService.getHotStatusSimListDetail(builder, loginUser, sim, irsimflag,false,"column");

	}

	@ApiOperation("饼图和柱状图数据导出接口")
	@PostMapping("/exportData")
	public void exportData(HttpServletResponse response,
			@ApiParam("该三级栏目的id,用于生成的文件名字") @RequestParam(value = "indexTabId", required = false) String indexTabId,
			@ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data) {
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			JSONArray array = JSONObject.parseArray(data);
			columnService.exportData(array).writeTo(outputStream);

		} catch (Exception e) {
			log.error("导出excel出错！", e);
		}

	}

	@ApiOperation("折线图数据导出接口")
	@PostMapping("/exportChartLine")
	public void exportChartLine(HttpServletResponse response,
			@ApiParam("该三级栏目的id,用于生成的文件名字") @RequestParam(value = "indexTabId", required = false) String indexTabId,
			@ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data) {
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			JSONArray array = JSONObject.parseArray(data);
			columnService.exportChartLine(array).writeTo(outputStream);

		} catch (Exception e) {
			log.error("导出excel出错！", e);
		}

	}

	@ApiOperation("词云图数据导出接口")
	@PostMapping("/exportWordCloud")
	public void exportWordCloud(HttpServletResponse response,
			@ApiParam("该三级栏目的id,用于生成的文件名字") @RequestParam(value = "indexTabId", required = false) String indexTabId,
			@ApiParam("数据类型") @RequestParam(value = "dataType", required = true) String dataType,
			@ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data) {
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			JSONArray array = JSONObject.parseArray(data);
			columnService.exportWordCloud(dataType,array).writeTo(outputStream);

		} catch (Exception e) {
			log.error("导出excel出错！", e);
		}

	}

	@ApiOperation("地域图数据导出接口")
	@PostMapping("/exportMap")
	public void exportMap(HttpServletResponse response,
			@ApiParam("该三级栏目的id,用于生成的文件名字") @RequestParam(value = "indexTabId", required = false) String indexTabId,
			@ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data) {
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			JSONArray array = JSONObject.parseArray(data);
			columnService.exportMap(array).writeTo(outputStream);
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
					Organization organization = organizationService.findById(indexTab.getOrganizationId());
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
					Organization organization = organizationService.findById(indexTab.getOrganizationId());
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
					Organization organization = organizationService.findById(indexTab.getOrganizationId());
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
					Organization organization = organizationService.findById(indexTab.getOrganizationId());
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
