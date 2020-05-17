/*
package com.trs.netInsight.widget.special.controller;

import java.text.SimpleDateFormat;
import java.com.trs.netInsight.util.Date;
import java.com.trs.netInsight.util.HashMap;
import java.com.trs.netInsight.util.List;
import java.com.trs.netInsight.util.Map;

import javax.mail.search.SearchException;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.handler.result.Message;
import com.trs.netInsight.com.trs.netInsight.support.cache.PerpetualPool;
import com.trs.netInsight.com.trs.netInsight.support.cache.TimingCachePool;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.log.annotation.SystemControllerLog;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.special.service.ISpecialService;
import com.trs.netInsight.widget.special.service.ISpecialSubjectService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

*/
/**
 * 无锡专题分析模块Controller
 * Created by yangyanyan on 2018/5/3.
 *//*

@Slf4j
@RestController
@RequestMapping("/specialAnalyze")
@Api(description = "无锡专项检测规则管理接口")
public class SpecialAnalyzeController {
@Autowired
	private ISpecialService specialService;

	@Autowired
	private ISpecialSubjectService specialSubjectService;

	@Autowired
	private ISpecialProjectService specialProjectService;

	@Autowired
	private SpecialSubjectRepository specialSubjectRepository;

	@Autowired
	private SpecialProjectRepository specialProjectRepository;

	*/
/**
	 * 新提出置顶接口
	 *
	 * @date Created at 2017年12月1日 上午10:34:28
	 * @Author 谷泽昊
	 * @param specialId
	 *            专题id
	 * @return
	 * @throws TRSException
	 *//*

	@SystemControllerLog(module = "专项监测", description = "新提出置顶接口")
	@FormatResult
	@RequestMapping(value = "/topFlag", method = RequestMethod.GET)
	public Object topFlag(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId) throws TRSException {
		try {
			SpecialProject findOne = specialProjectService.findOne(specialId);
			findOne.setTopFlag("top");
			specialProjectService.save(findOne);
			return findOne;
		} catch (Exception e) {
			throw new OperationException(String.format("获取[id=%s]专项详情失败,message: %s", specialId, e));
		}
	}

	*/
/**
	 * 取消置顶
	 *
	 * @date Created at 2017年12月1日 上午10:35:18
	 * @Author 谷泽昊
	 * @param specialId
	 *            专题id
	 * @return
	 * @throws TRSException
	 *//*

	@SystemControllerLog(module = "专项监测", description = "新提出取消置顶接口")
	@FormatResult
	@RequestMapping(value = "/noTopFlag", method = RequestMethod.GET)
	public Object noTopFlag(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId) throws TRSException {
		try {
			SpecialProject findOne = specialProjectService.findOne(specialId);
			findOne.setTopFlag("");
			specialProjectService.save(findOne);
			return findOne;
		} catch (Exception e) {
			throw new OperationException(String.format("获取[id=%s]专项详情失败,message: %s", specialId, e));
		}
	}

	*/
/**
	 * 添加新专项
	 *
	 * @param specialName
	 *            专项名
	 * @param allKeywords
	 *            所有关键词
	 * @param anyKeywords
	 *            任意关键词
	 * @param excludeWords
	 *            排除词
	 * @param timeRange
	 *            时间范围
	 * @param trsl
	 *            专家表达式
	 * @param searchScope
	 *            检索域
	 * @param specialType
	 *            专项类型
	 * @return Object
	 * @throws TRSException
	 *             TRSException
	 *//*

	@ApiOperation("新建专项， 两种模式一个接口，通过 special_type 区分")
	@SystemControllerLog(module = "专项监测", description = "新建专项")
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialName", value = "专项名称", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "timeRange", value = "时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "allKeywords", value = "所有关键词[北京;雾霾]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "anyKeywords", value = "任意关键词[中国,河北;美国,洛杉矶]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "trsl", value = "专家模式传统库表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "statusTrsl", value = "专家模式微博库表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weChatTrsl", value = "专家模式微信库表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "searchScope", value = "搜索范围[TITLE，TITLE_ABSTRACT, TITLE_CONTENT]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "specialType", value = "专项模式[COMMON, SPECIAL]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupName", value = "分组名", dataType = "String", paramType = "query", required = false),
			//@ApiImplicitParam(name = "isSimilar", value = "是否排重", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排,url排", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupId", value = "分组id", dataType = "String", paramType = "query", required = false) })
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Object addSpecial(HttpServletRequest request, @RequestParam("specialName") String specialName, // 验证空格
			@RequestParam("timeRange") String timeRange,
			@RequestParam(value = "allKeywords", required = false) String allKeywords,
			@RequestParam(value = "anyKeywords", required = false) String anyKeywords,
			@RequestParam(value = "excludeWords", required = false) String excludeWords,
			@RequestParam(value = "trsl", required = false) String trsl,
			@RequestParam(value = "statusTrsl", required = false) String statusTrsl,
			@RequestParam(value = "weChatTrsl", required = false) String weChatTrsl,
			@RequestParam(value = "searchScope", required = false, defaultValue = "TITLE") String searchScope,
			@RequestParam(value = "specialType", required = false, defaultValue = "COMMON") String specialType,
			@RequestParam(value = "source", required = false) String source,
			@RequestParam(value = "groupName", required = false) String groupName,
			//@RequestParam(value = "isSimilar", required = false) boolean isSimilar,
			@RequestParam(value = "simflag", required = false) String simflag,
			@RequestParam(value = "groupId", required = false) String groupId) throws TRSException {
		try {
			//默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			if("netRemove".equals(simflag)){
				isSimilar = true;
			}else if("urlRemove".equals(simflag)){
				irSimflag = true;
			}
			if (StringUtil.isNotEmpty(specialName) && StringUtil.isNotEmpty(timeRange)
					&& StringUtil.isNotEmpty(specialType)) {
				String userId = UserUtils.getUser().getId();
				SpecialType type = SpecialType.valueOf(specialType);
				// 专家模式
				if (SpecialType.SPECIAL.equals(type)) {
					if (StringUtil.isEmpty(trsl)) {
						throw new OperationException("创建监测方案失败");
					}
				} else if (SpecialType.COMMON.equals(type)) {
					// 普通模式
					if (StringUtil.isEmpty(allKeywords) && StringUtil.isEmpty(anyKeywords)) {
						throw new OperationException("创建监测方案失败");
					}
				}
				String timerange = "";
				if (timeRange.contains("d") || timeRange.contains("h")) {
					timerange = timeRange;
				}
				String[] formatTimeRange = DateUtil.formatTimeRange(timeRange);
				SearchScope scope = SearchScope.valueOf(searchScope);
				Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[0]);
				Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[1]);
				SpecialProject specialProject = new SpecialProject(userId, type, specialName, allKeywords, anyKeywords,
						excludeWords, trsl, statusTrsl, weChatTrsl, scope, startTime, endTime, source, groupName,
						groupId, timerange,isSimilar,irSimflag);
				String imgUrl = "";
				specialProject.setImgUrl(imgUrl);
				specialService.createSpecial(specialProject);
				PerpetualPool.put(userId, DateUtil.formatCurrentTime("yyyyMMddHHmmss"));
				Map<String, String> map = new HashMap<String, String>();
				map.put("specialName", specialProject.getSpecialName());
				map.put("specialId", specialProject.getId());
				return map;
			} else {
				throw new OperationException("创建监测方案失败");
			}
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("创建监测方案失败,message:" + e,e);
		}
	}

    */
/**
     * 预览数据
     * @param request
     * @param specialName
     * @param timeRange
     * @param allKeywords
     * @param anyKeywords
     * @param excludeWords
     * @param trsl
     * @param statusTrsl
     * @param weChatTrsl
     * @param searchScope
     * @param specialType
     * @param source
     * @param isSimilar
     * @param groupName
     * @return
     * @throws TRSException
     *//*

	@ApiOperation("预览数据")
	@SystemControllerLog(module = "专项监测", description = "预览数据")
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialName", value = "专项名称", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "timeRange", value = "时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "allKeywords", value = "所有关键词[北京;雾霾]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "anyKeywords", value = "任意关键词[中国,河北;美国,洛杉矶]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "trsl", value = "专家模式表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "statusTrsl", value = "专家模式微博库表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weChatTrsl", value = "专家模式微信库表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "searchScope", value = "搜索范围[TITLE，TITLE_ABSTRACT, TITLE_CONTENT]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "specialType", value = "专项模式[COMMON, SPECIAL]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false),
			//@ApiImplicitParam(name = "isSimilar", value = "是否排重", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排,url排", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupName", value = "分组名", dataType = "String", paramType = "query", required = false) })
	@RequestMapping(value = "/preview", method = RequestMethod.POST)
	public Object preview(HttpServletRequest request, @RequestParam("specialName") String specialName,
			@RequestParam("timeRange") String timeRange,
			@RequestParam(value = "allKeywords", required = false) String allKeywords,
			@RequestParam(value = "anyKeywords", required = false) String anyKeywords,
			@RequestParam(value = "excludeWords", required = false) String excludeWords,
			@RequestParam(value = "trsl", required = false) String trsl,
			@RequestParam(value = "statusTrsl", required = false) String statusTrsl,
			@RequestParam(value = "weChatTrsl", required = false) String weChatTrsl,
			@RequestParam(value = "searchScope", required = false, defaultValue = "TITLE") String searchScope,
			@RequestParam(value = "specialType", required = false, defaultValue = "COMMON") String specialType,
			@RequestParam(value = "source", required = false) String source,
			//@RequestParam(value = "isSimilar", required = false) boolean isSimilar,
						  @RequestParam(value = "simflag", required = false) String simflag,
			@RequestParam(value = "groupName", required = false) String groupName) throws TRSException {
		String userId = UserUtils.getUser().getId();
		try {
			//默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			if("netRemove".equals(simflag)){
				isSimilar = true;
			}else if("urlRemove".equals(simflag)){
				irSimflag = true;
			}
			String timerange = "";
			if (timeRange.contains("d") || timeRange.contains("h")) {
				timerange = timeRange;
			}
			String groupId = "";
			SpecialType type = SpecialType.valueOf(specialType);
			SearchScope scope = SearchScope.valueOf(searchScope);
			String[] formatTimeRange = DateUtil.formatTimeRange(timeRange);
			Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[0]);
			Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[1]);
			//如果是专家模式 哪个先不为空查哪个
			// 专家模式
			if (SpecialType.SPECIAL.equals(type)) {
				if(StringUtil.isNotEmpty(trsl)){
					source = "传统媒体";
				}else if(StringUtil.isNotEmpty(statusTrsl)){
					source = "微博";
				}else if(StringUtil.isNotEmpty(weChatTrsl)){
					source = "微信";
				}
			}
			SpecialProject specialProject = new SpecialProject(userId, type, specialName, allKeywords, anyKeywords,
					excludeWords, trsl, statusTrsl, weChatTrsl, scope, startTime, endTime, source, groupName, groupId,
					timerange,isSimilar,irSimflag);
			return specialService.preview(specialProject, source);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("预览失败,message: " + e,e);
		}
	}

	*/
/**
	 * 获取专项详细信息
	 *
	 * @param specialId
	 *            专项ID
	 *//*

	@SystemControllerLog(module = "专项监测", description = "查看 专项信息")
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialId", value = "专项Id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public Object specialList(@RequestParam(value = "specialId") String specialId) throws TRSException {
		try {
			return specialProjectService.findOne(specialId);
		} catch (Exception e) {
			throw new OperationException(String.format("获取[id=%s]专项详情失败,message: %s", specialId, e));
		}
	}

    */
/**
     * 修改监测方案
     * @param specialId
     *           专项Id
     * @param specialName
     *           专项名称
     * @param timeRange
     *           时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]
     * @param allKeywords
     *          所有关键词[北京;雾霾]
     * @param anyKeywords
     *          任意关键词[中国,河北;美国,洛杉矶]
     * @param excludeWords
     *          排除词[雾霾;沙尘暴]
     * @param trsl
     *          专家模式表达式
     * @param statusTrsl
     * @param weChatTrsl
     * @param searchScope
     *          搜索范围[TITLE，TITLE_ABSTRACT, TITLE_CONTENT]
     * @param specialType
     *          专项模式[COMMON, SPECIAL]
     * @param isSimilar
     *          是否排重
     * @param source
     *          来源
     * @return
     * @throws TRSException
     *//*

	@SystemControllerLog(module = "专项监测", description = "修改 监测方案")
	@FormatResult
	@ApiOperation("修改监测方案")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialId", value = "专项Id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "specialName", value = "专项名称", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "timeRange", value = "时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "allKeywords", value = "所有关键词[北京;雾霾]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "anyKeywords", value = "任意关键词[中国,河北;美国,洛杉矶]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "trsl", value = "专家模式表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "searchScope", value = "搜索范围[TITLE，TITLE_ABSTRACT, TITLE_CONTENT]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "specialType", value = "专项模式[COMMON, SPECIAL]", dataType = "String", paramType = "query", required = false),
			//@ApiImplicitParam(name = "isSimilar", value = "是否排重", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排,url排", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false) })
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public Object updateSpecial(@RequestParam("specialId") String specialId,
			@RequestParam("specialName") String specialName, @RequestParam("timeRange") String timeRange,
			@RequestParam(value = "allKeywords", required = false) String allKeywords,
			@RequestParam(value = "anyKeywords", required = false) String anyKeywords,
			@RequestParam(value = "excludeWords", required = false) String excludeWords,
			@RequestParam(value = "trsl", required = false) String trsl,
			@RequestParam(value = "statusTrsl", required = false) String statusTrsl,
			@RequestParam(value = "weChatTrsl", required = false) String weChatTrsl,
			@RequestParam(value = "searchScope", required = false, defaultValue = "TITLE") String searchScope,
			@RequestParam(value = "specialType", required = false, defaultValue = "COMMON") String specialType,
			//@RequestParam(value = "isSimilar", required = false) boolean isSimilar,
			@RequestParam(value = "simflag", required = false) String simflag,
			@RequestParam(value = "source", required = false) String source) throws TRSException {
		try {
			//默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			if("netRemove".equals(simflag)){
				isSimilar = true;
			}else if("urlRemove".equals(simflag)){
				irSimflag = true;
			}
			SpecialType type = SpecialType.valueOf(specialType);
			SearchScope scope = SearchScope.valueOf(searchScope);
			String timerange = "";
			if (timeRange.contains("d") || timeRange.contains("h")) {
				timerange = timeRange;
			}
			String[] formatTimeRange = DateUtil.formatTimeRange(timeRange);
			Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[0]);
			Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[1]);
			// 专家模式
			if (SpecialType.SPECIAL.equals(type)) {
				if (StringUtil.isEmpty(trsl) && StringUtil.isEmpty(statusTrsl) && StringUtil.isEmpty(weChatTrsl)) {
					throw new OperationException("创建监测方案失败");
				}
			}
			SpecialProject updateSpecial = specialService.updateSpecial(specialId, type, specialName, allKeywords,
					anyKeywords, excludeWords, trsl, statusTrsl, weChatTrsl, scope, startTime, endTime, source,
					timerange,isSimilar,irSimflag);
			Map<String, String> map = new HashMap<String, String>();
			map.put("specialName", updateSpecial.getSpecialName());
			map.put("specialId", updateSpecial.getId());
			return map;
		} catch (Exception e) {
			throw new OperationException("修改监测方案失败:" + e,e);
		}
	}

	*/
/**
	 * 置顶
	 *
	 * @param id
	 *            专项id
	 *//*

	@SystemControllerLog(module = "专项监测", description = "专项 置顶")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "专项Id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/top", method = RequestMethod.GET)
	public Object promotion(@RequestParam("specialId") String id) throws TRSException {
		try {
			SpecialProject specialProject = specialProjectService.findOne(id);
			specialProject.setLastModifiedTime(new Date());
			specialProjectService.save(specialProject);
			return "promotion success";
		} catch (Exception e) {
			throw new OperationException("置顶失败,message" + e,e);
		}
	}

	*/
/**
	 * 删除
	 *
	 * @param id
	 *            专项id
	 *//*

	@ApiOperation("删除专项")
	@SystemControllerLog(module = "专项监测", description = "删除专项")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "专项Id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public Object deleteSpecial(@RequestParam("specialId") String id) throws TRSException {
		try {
			specialProjectService.delete(id);
			// 删除缓存池
			String sumKey = "sum" + id;
			TimingCachePool.clear(sumKey);
			return "delete success";
		} catch (Exception e) {
			throw new OperationException("删除失败,message" + e,e);
		}
	}

*/
/*	@ApiOperation("RequestBody 测试")
	@FormatResult
	@RequestMapping(value = "/add111", method = RequestMethod.POST)
	public Object aaa(@ApiParam("专项json") @RequestBody SpecialProject sp) throws TRSException {
		try {
			return sp;
		} catch (Exception e) {
			throw new OperationException("创建监测方案失败,message:" + e,e);
		}
	}*//*


	*/
/**
	 * 根据专项名字查看专项
	 *
	 * @date Created at 2017年11月27日 下午5:04:12
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 * @throws SearchException
	 * @throws TRSException
	 *//*

	@ApiOperation("根据专项名查看专项")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "selectName", value = "专项名", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/selectName", method = RequestMethod.GET)
	@FormatResult
	public Object selectName(@RequestParam("name") String name) throws SearchException, TRSException {
		Criteria<SpecialProject> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("specialName", name));
		List<SpecialProject> findAll = specialProjectService.findBySpecialNameContains(name);
		return findAll;

	}

	*/
/**
	 * 检测方案查询
	 *
	 * @date Created at 2017年11月27日 下午2:39:57
	 * @Author 谷泽昊
	 * @param request
	 * @return
	 * @throws SearchException
	 * @throws TRSException
	 *//*

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ApiOperation("监测方案栏目查询")
	// @SystemControllerLog(module = "专项监测", description = "栏目查询")
	@FormatResult
	@RequestMapping(value = "/selectSpecial", method = RequestMethod.GET)
	public Object selectSpecial(HttpServletRequest request) throws SearchException, TRSException {
		String userId = UserUtils.getUser().getId();
		return specialService.selectSpecial(userId);
	}

	*/
/**
	 * 新建主题
	 *
	 * @date Created at 2017年11月27日 下午5:02:15
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 * @throws TRSException
	 *//*

	@ApiOperation("新建主题")
	@SystemControllerLog(module = "专项监测", description = "新建主题")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "name", value = "主题名", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/addSubject", method = RequestMethod.GET)
	public Object addSubject(@RequestParam("name") String name) throws TRSException {
		if (StringUtil.isNotEmpty(name)) {
			SpecialSubject subject = new SpecialSubject(name);
			specialSubjectService.save(subject);
			return subject.getId();
		}
		return null;
	}

	*/
/**
	 * 新建专题
	 *
	 * @date Created at 2017年11月27日 下午5:02:23
	 * @Author 谷泽昊
	 * @param specialName
	 * @param subjectId
	 * @return
	 * @throws TRSException
	 *//*

	@ApiOperation("新建专题")
	@SystemControllerLog(module = "专项监测", description = "新建专题")
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialName", value = "专题名", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "subjectId", value = "主题id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/addzhuan", method = RequestMethod.GET)
	public Object addzhuan(@RequestParam("specialName") String specialName,
			@RequestParam(value = "subjectId", required = false) String subjectId) throws TRSException {
		SpecialSubject specialSubject = new SpecialSubject(specialName, subjectId);
		specialSubjectService.save(specialSubject);
		return specialSubject.getId();
	}

	*/
/**
	 * 专题重命名
	 *
	 * @date Created at 2017年11月27日 下午5:02:33
	 * @Author 谷泽昊
	 * @param newName
	 * @param specialId
	 * @return
	 * @throws TRSException
	 *//*

	@ApiOperation("专题重命名")
	@SystemControllerLog(module = "专项监测", description = "专题重命名")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "newName", value = "新名字", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "subjectId", value = "主题id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/renameSpecial", method = RequestMethod.GET)
	public Object renameSpecial(String newName, String specialId) throws TRSException {
		// 专题id也是主键 只是用subjectid用来辨别他的一级是谁
		SpecialSubject findOne = specialSubjectService.findOne(specialId);
		findOne.setName(newName);
		specialSubjectService.save(findOne);
		return newName;
	}

	*/
/**
	 * 主题重命名
	 *
	 * @date Created at 2017年11月27日 下午5:02:48
	 * @Author 谷泽昊
	 * @param newName
	 * @param subjectId
	 * @return
	 * @throws TRSException
	 *//*

	@ApiOperation("主题重命名")
	@SystemControllerLog(module = "专项监测", description = "主题重命名")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "newName", value = "新名字", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "subjectId", value = "主题id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/renameSubject", method = RequestMethod.GET)
	public Object renameSubject(@ApiParam("新主题名") @RequestParam("newName") String newName,
			@ApiParam("主题id") @RequestParam("subjectId") String subjectId) throws TRSException {
		// 主题id就是主键
		SpecialSubject findOne = specialSubjectRepository.findOne(subjectId);
		findOne.setName(newName);
		specialSubjectService.save(findOne);
		return newName;
	}

	*/
/**
	 * 删除主题
	 *
	 * @date Created at 2017年11月27日 下午5:02:57
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 * @throws TRSException
	 *//*

	@ApiOperation("删除主题")
	@SystemControllerLog(module = "专项监测", description = "删除主题")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "主题id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/deleteSubject", method = RequestMethod.GET)
	public Object deleteSubject(@RequestParam("id") String id) throws TRSException {
		try {
			// 删除主题下专题，方案以及专题下方案
			SpecialSubject subject = specialSubjectRepository.findOne(id);
			specialSubjectRepository.delete(subject);
			// for(SpecialSubject subject : list){
			// 删除主题下专题
			Criteria<SpecialSubject> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("subjectId", subject.getId()));
			List<SpecialSubject> findAll2 = specialSubjectRepository.findAll(criteria);
			for (SpecialSubject sujectTwo : findAll2) {
				specialSubjectRepository.delete(sujectTwo);
				String twoId = sujectTwo.getId();
				List<SpecialProject> byTwoId = specialProjectService.findByGroupId(twoId);
				// 删除专题下方案
				for (SpecialProject special : byTwoId) {
					String specialId = special.getId();
					specialProjectService.delete(specialId);
				}
			}
//			String twoId = subject.getId();
//			List<SpecialProject> byTwoId = specialProjectService.findByGroupId(twoId);
//			// 删除专题下方案
//			for (SpecialProject special : byTwoId) {
//				String specialId = special.getId();
//				specialProjectService.delete(specialId);
//			}
			// 删除主题下方案
			List<SpecialProject> byOneId = specialProjectService.findByGroupId(id);
			for (SpecialProject project : byOneId) {
				String specialId = project.getId();
				specialProjectService.delete(specialId);
			}
			// }
			return "删除主题成功";
		} catch (Exception e) {
			throw new OperationException("删除主题失败" + e,e);
		}

	}

	*/
/**
	 * 删除专题
	 *
	 * @date Created at 2017年11月27日 下午5:03:33
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 * @throws TRSException
	 *//*

	@ApiOperation("删除专题")
	@SystemControllerLog(module = "专项监测", description = "删除专题")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "专题id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/deleteZhuanTi", method = RequestMethod.GET)
	public Object deleteZhuanTi(@ApiParam("专题id") @RequestParam("id") String id) throws TRSException {
		try {
			// 删除专题
			specialSubjectService.delete(id);
			// 删除专题下方案
			List<SpecialProject> byGroupId = specialProjectService.findByGroupId(id);
			for (SpecialProject specialProject : byGroupId) {
				String specialId = specialProject.getId();
				specialProjectService.delete(specialId);
			}
			return "删除专题成功";
		} catch (Exception e) {
			throw new OperationException("删除专题失败" + e,e);
		}
	}

	*/
/**
	 * 专题联想词
	 *
	 * @date Created at 2018年1月10日 下午4:56:36
	 * @Author 谷泽昊
	 * @param name
	 * @param pageSize
	 * @return
	 *//*

	@ApiOperation("专题联想词")
	@RequestMapping(value = "/associational", method = RequestMethod.GET)
	public Message associational(@ApiParam(value = "名称") @RequestParam(value = "name", required = false) String name,
                                 @ApiParam(value = "size") @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		List<String> list = null;
		try {
			list = specialProjectService.associational(name, pageSize);
			return Message.getMessage(CodeUtils.SUCCESS, "查询成功！", list);
		} catch (Exception e) {
			log.error("专题联想词查询失败：" + e,e);
		}
		return Message.getMessage(CodeUtils.FAIL, "查询失败！", list);
	}

	*/
/**
	 *  模糊查询专项
	 * @date Created at 2018年1月18日  上午9:54:53
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 *//*

	@ApiOperation("模糊查询专项")
	@RequestMapping(value = "/selectByWord", method = RequestMethod.GET)
	public Message selectByWord(@ApiParam(value = "名称") @RequestParam(value = "name", required = false) String name) {
		List<Object> map = null;
		try {
			map = specialProjectService.selectByWord(name);
			return Message.getMessage(CodeUtils.SUCCESS, "查询成功！", map);
		} catch (Exception e) {
			log.error("模糊查询专项查询失败：" + e,e);
		}
		return Message.getMessage(CodeUtils.FAIL, "查询失败！", map);
	}
}
*/
