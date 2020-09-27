
package com.trs.netInsight.widget.special.controller;

import com.alibaba.fastjson.JSONObject;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.PerpetualPool;
import com.trs.netInsight.support.cache.TimingCachePool;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.column.entity.emuns.SpecialFlag;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.special.service.*;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.SubGroupRepository;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 专项监测模块Controller
 *
 * Created by ChangXiaoyang on 2017/4/7.
 */
@Slf4j
@RestController
@RequestMapping("/special")
@Api(description = "专项检测规则管理接口")
public class SpecialController {

	@Autowired
	private ISpecialService specialService;

	@Autowired
	private ISpecialSubjectService specialSubjectService;

	@Autowired
	private ISpecialProjectService specialProjectService;

	@Autowired
	private SpecialSubjectRepository specialSubjectRepository;

	@Autowired
	private ISpecialComputeService computeService;
	@Autowired
	private SubGroupRepository subGroupRepository;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private IInfoListService infoListService;


	/**
	 * 线程池跑任务
	 */
	private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

	/**
	 * 新提出置顶接口(分组）
	 * 
	 * @date Created at 2017年12月1日 上午10:34:28
	 * @Author 谷泽昊
	 * @param specialId
	 *            专题id
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_TOP_FLAG, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "专项置顶：${specialId}")
	@RequestMapping(value = "/topFlag", method = RequestMethod.GET)
	public Object topFlag(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId) throws TRSException {
		try {
			// 查这个用户或用户分组下有多少个已经置顶的专题 新置顶的排前边 查找专题列表的时候按照sequence正序排
			User loginUser = UserUtils.getUser();

			List<Sort.Order> orders=new ArrayList<Sort.Order>();
			orders.add( new Sort.Order(Sort.Direction.DESC, "sequence"));
			orders.add( new Sort.Order(Sort.Direction.ASC, "createdTime"));
			Criteria<SpecialProject> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("topFlag", "top"));
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				criteria.add(Restrictions.eq("userId", loginUser.getId()));
			}else {
				criteria.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			}
			List<SpecialProject> findTop = specialProjectService.findAll(criteria,new Sort(orders));

			SpecialProject findOne = specialProjectService.findOne(specialId);
			//这里要做一步操作，把置顶的专题信息原来的东西重新排序
			//specialService.moveSequenceForSpecial(findOne.getId(), SpecialFlag.SpecialProjectFlag, loginUser);
			findOne.setBakParentId(findOne.getGroupId());
			findOne.setGroupId(null);
			findOne.setSpecialSubject(null);
			int seq =1;
			if(findTop != null &&findTop.size() >0){
				seq = findTop.get(0).getSequence()+1;
			}
			findOne.setSequence(seq);
			findOne.setTopFlag("top");
			specialProjectService.save(findOne);
			return findOne;
		} catch (Exception e) {
			throw new OperationException(String.format("获取[id=%s]专项详情失败,message: %s", specialId, e));
		}
	}

	/**
	 * 取消置顶（分组）
	 * 
	 * @date Created at 2017年12月1日 上午10:35:18
	 * @Author 谷泽昊
	 * @param specialId
	 *            专题id
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_NO_TOP_FLAG, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "取消专项置顶：${specialId}")
	@FormatResult
	@RequestMapping(value = "/noTopFlag", method = RequestMethod.GET)
	public Object noTopFlag(@ApiParam("专题id") @RequestParam(value = "specialId") String specialId) throws TRSException {
		try {
			User user = UserUtils.getUser();
			SpecialProject findOne = specialProjectService.findOne(specialId);
			findOne.setTopFlag(null);
			String parentId = null;
			if(StringUtil.isNotEmpty(findOne.getBakParentId())){
				SpecialSubject parent = specialSubjectService.findOne(findOne.getBakParentId());
				if(parent != null && StringUtil.isNotEmpty(parent.getId())){
					parentId = parent.getId();
					findOne.setGroupId(parentId);
				}
			}

			//Integer  seq = specialService.getMaxSequenceForSpecial(parentId,user) +1;
			//原本排序为正序，现在排序方式改为倒序
			specialService.insertPropectToLast(parentId,user);
			findOne.setSequence(1);
			findOne.setBakParentId(null);
			// 放在原来列表最后一个 查找时按照sequence排列
			specialProjectService.save(findOne);
			return findOne;
		} catch (Exception e) {
			throw new OperationException(String.format("获取[id=%s]专项详情失败,message: %s", specialId, e));
		}
	}
	/**
	 * 添加新专项（分组）
	 *
	 * @param specialName
	 *            专项名
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
	 */
	@ApiOperation("新建专项， 两种模式一个接口，通过 special_type 区分")
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialName", value = "专项名称", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "timeRange", value = "时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "anyKeywords", value = "任意关键词[中国,河北;美国,洛杉矶]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWordsIndex", value = "排除词命中位置：0标题、1标题+正文、2标题+摘要", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "trsl", value = "专家模式传统库表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "searchScope", value = "搜索范围[TITLE，TITLE_ABSTRACT, TITLE_CONTENT]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "specialType", value = "专项模式[COMMON, SPECIAL]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupName", value = "分组名", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weight", value = "是否按照权重查找", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排,url排", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "monitorSite", value = "监测网站", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "server", value = "是否转换为server表达式", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupId", value = "分组id", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "mediaLevel", value = "媒体等级", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaIndustry", value = "媒体行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentIndustry", value = "内容行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "filterInfo", value = "信息过滤", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentArea", value = "信息地域", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaArea", value = "媒体地域", dataType = "String", paramType = "query")})
	@RequestMapping(value = "/addProject", method = RequestMethod.POST)
	public Object addProject(HttpServletRequest request, @RequestParam("specialName") String specialName, // 验证空格
							 @RequestParam("timeRange") String timeRange,
							 @RequestParam(value = "anyKeywords", required = false) String anyKeywords,
							 @RequestParam(value = "excludeWords", required = false) String excludeWords,
							 @RequestParam(value = "excludeWordsIndex", required = false) String excludeWordsIndex,
							 @RequestParam(value = "trsl", required = false) String trsl,
							 @RequestParam(value = "searchScope", required = false, defaultValue = "TITLE") String searchScope,
							 @RequestParam(value = "specialType", required = false, defaultValue = "COMMON") String specialType,
							 @RequestParam(value = "source", required = false) String source,
							 @RequestParam(value = "groupName", required = false) String groupName,
							 @RequestParam(value = "weight", required = false) boolean weight,
							 @RequestParam(value = "simflag", required = false) String simflag,
							 @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
							 @RequestParam(value = "monitorSite", required = false) String monitorSite,
							 @RequestParam(value = "server", required = false) boolean server,
							 @RequestParam(value = "groupId", required = false) String groupId,
							 @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
							 @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
							 @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
							 @RequestParam(value = "filterInfo", required = false) String filterInfo,
							 @RequestParam(value = "contentArea", required = false) String contentArea,
							 @RequestParam(value = "mediaArea", required = false) String mediaArea) throws Exception {
		try {

			//首先判断下用户权限（若为机构管理员，只受新建与编辑的权限，不受用户分组可创建资源数量的限制，但是受机构可创建资源数量的限制）
			User loginUser = UserUtils.getUser();
			Organization organization = null;
			if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())){
				organization = organizationRepository.findOne(loginUser.getOrganizationId());
			}

			if (UserUtils.isRoleAdmin() && ObjectUtil.isNotEmpty(organization)){
				//机构管理员
				if (organization.getSpecialNum() <= specialProjectService.getSubGroupSpecialCount(loginUser)){
					throw new TRSException(CodeUtils.FAIL,"您目前创建的专题已达上限，如需更多，请联系相关运维人员。");
				}
			}
			if (UserUtils.isRoleOrdinary(loginUser)){
				//如果是普通用户 受用户分组 可创建资源的限制
				//查询该用户所在的用户分组下 是否有可创建资源
				SubGroup subGroup = subGroupRepository.findOne(loginUser.getSubGroupId());
				if (subGroup.getSpecialNum() <= specialProjectService.getSubGroupSpecialCount(loginUser)){
					throw new TRSException(CodeUtils.FAIL,"您目前创建的专题已达上限，如需更多，请联系相关运维人员。");
				}
			}
			// 默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			boolean irSimflagAll =false;
			if ("netRemove".equals(simflag)) {
				isSimilar = true;
			} else if ("urlRemove".equals(simflag)) {
				irSimflag = true;
			}else if ("sourceRemove".equals(simflag)){
				irSimflagAll = true;
			}
			if (StringUtil.isNotEmpty(specialName) && StringUtil.isNotEmpty(timeRange)
					&& StringUtil.isNotEmpty(specialType)) {
				String userId = UserUtils.getUser().getId();
				SpecialType type = SpecialType.valueOf(specialType);
				int chineseCount = 0;
				// 专家模式
				if (SpecialType.SPECIAL.equals(type)) {
					if (StringUtil.isEmpty(trsl)) {
						throw new OperationException("创建监测方案失败");
					}
					if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
						if (StringUtil.isNotEmpty(trsl)){
							int trslCount = StringUtil.getChineseCount(trsl);
							chineseCount = trslCount;
						}
					}
				} else if (SpecialType.COMMON.equals(type)) {
					// 普通模式
					if (StringUtil.isEmpty(anyKeywords)) {
						throw new OperationException("创建监测方案失败");
					}
					//若为机构管理员或者普通用户 若为普通模式，判断关键字字数
					if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
						if (StringUtil.isNotEmpty(anyKeywords)){
							chineseCount = StringUtil.getChineseCountForSimple(anyKeywords);
						}
					}
				}
				if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && ObjectUtil.isNotEmpty(organization)){
					if (chineseCount > organization.getKeyWordsNum()){
						throw new TRSException(CodeUtils.FAIL,"该专题暂时仅支持检索"+organization.getKeyWordsNum()+"个关键字，如需更多，请联系相关运维人员。");
					}
				}

				String timerange = "";
				timerange = timeRange;
				String[] formatTimeRange = DateUtil.formatTimeRange(timeRange);
				SearchScope scope = SearchScope.valueOf(searchScope);
				if (ObjectUtil.isEmpty(groupId)){
					groupId = null;
				}
				String sequence = String.valueOf(specialService.getMaxSequenceForSpecial(groupId,loginUser) +1);
				Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[0]);
				Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[1]);
				SpecialProject specialProject = new SpecialProject(userId, type, specialName, anyKeywords,
						excludeWords, trsl, scope, startTime, endTime, source, groupName,
						groupId, timerange,Integer.valueOf(sequence),isSimilar, irSimflag, weight, server,irSimflagAll,excludeWeb);
				specialProject.setExcludeWordIndex(excludeWordsIndex);
				String imgUrl = "";
				specialProject.setImgUrl(imgUrl);
				specialProject.setMonitorSite(monitorSite);
				specialProject.setStart(new SimpleDateFormat("yyyyMMddHHmmss").format(startTime));
				specialProject.setEnd(new SimpleDateFormat("yyyyMMddHHmmss").format(endTime));
				specialProject.setMediaLevel(mediaLevel);
				specialProject.setMediaIndustry(mediaIndustry);
				specialProject.setContentIndustry(contentIndustry);
				specialProject.setFilterInfo(filterInfo);
				specialProject.setMediaArea(mediaArea);
				specialProject.setContentArea(contentArea);
				specialService.createSpecial(specialProject);
				PerpetualPool.put(userId, DateUtil.formatCurrentTime("yyyyMMddHHmmss"));
				return specialProject;
			} else {
				throw new OperationException("创建监测方案失败");
			}
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("创建监测方案失败,message:" + e, e);
		}
	}
	/**
	 * 添加新专项（分组）
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
	 */
	@ApiOperation("新建专项， 两种模式一个接口，通过 special_type 区分")
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_ADD, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "添加专项：${groupId}/@{specialName}")
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialName", value = "专项名称", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "timeRange", value = "时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "anyKeywords", value = "任意关键词[中国,河北;美国,洛杉矶]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "trsl", value = "专家模式传统库表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "statusTrsl", value = "专家模式微博库表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weChatTrsl", value = "专家模式微信库表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "searchScope", value = "搜索范围[TITLE，TITLE_ABSTRACT, TITLE_CONTENT]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "specialType", value = "专项模式[COMMON, SPECIAL]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "groupName", value = "分组名", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weight", value = "是否按照权重查找", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排,url排", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "server", value = "是否转换为server表达式", dataType = "boolean", paramType = "query", required = false),
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
			@RequestParam(value = "weight", required = false) boolean weight,
			@RequestParam(value = "simflag", required = false) String simflag,
			@RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@RequestParam(value = "server", required = false) boolean server,
			@RequestParam(value = "groupId", required = false) String groupId) throws Exception {
		try {

			//首先判断下用户权限（若为机构管理员，只受新建与编辑的权限，不受用户分组可创建资源数量的限制，但是受机构可创建资源数量的限制）
			User loginUser = UserUtils.getUser();
			Organization organization = null;
			if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())){
				organization = organizationRepository.findOne(loginUser.getOrganizationId());
			}

			if (UserUtils.isRoleAdmin() && ObjectUtil.isNotEmpty(organization)){
				//机构管理员
				if (organization.getSpecialNum() <= specialProjectService.getSubGroupSpecialCount(loginUser)){
					throw new TRSException(CodeUtils.FAIL,"您目前创建的专题已达上限，如需更多，请联系相关运维人员。");
				}
			}
			if (UserUtils.isRoleOrdinary(loginUser)){
				//如果是普通用户 受用户分组 可创建资源的限制
				//查询该用户所在的用户分组下 是否有可创建资源
				SubGroup subGroup = subGroupRepository.findOne(loginUser.getSubGroupId());
				if (subGroup.getSpecialNum() <= specialProjectService.getSubGroupSpecialCount(loginUser)){
					throw new TRSException(CodeUtils.FAIL,"您目前创建的专题已达上限，如需更多，请联系相关运维人员。");
				}
			}
			// 默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			boolean irSimflagAll =false;
			if ("netRemove".equals(simflag)) {
				isSimilar = true;
			} else if ("urlRemove".equals(simflag)) {
				irSimflag = true;
			}else if ("sourceRemove".equals(simflag)){
				irSimflagAll = true;
			}
			if (StringUtil.isNotEmpty(specialName) && StringUtil.isNotEmpty(timeRange)
					&& StringUtil.isNotEmpty(specialType)) {
				String userId = UserUtils.getUser().getId();
				SpecialType type = SpecialType.valueOf(specialType);
				int chineseCount = 0;
				// 专家模式
				if (SpecialType.SPECIAL.equals(type)) {
					if (StringUtil.isEmpty(trsl)) {
						throw new OperationException("创建监测方案失败");
					}
					if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
						if (StringUtil.isNotEmpty(trsl) || StringUtil.isNotEmpty(weChatTrsl) || StringUtil.isNotEmpty(statusTrsl)){
							int trslCount = StringUtil.getChineseCount(trsl);
							int weChatTrslCount = StringUtil.getChineseCount(weChatTrsl);
							int statusTrslCount = StringUtil.getChineseCount(statusTrsl);
							chineseCount = trslCount+weChatTrslCount+statusTrslCount;
						}
					}
				} else if (SpecialType.COMMON.equals(type)) {
					// 普通模式
					if (StringUtil.isEmpty(allKeywords) && StringUtil.isEmpty(anyKeywords)) {
						throw new OperationException("创建监测方案失败");
					}
					//若为机构管理员或者普通用户 若为普通模式，判断关键字字数
					if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
						if (StringUtil.isNotEmpty(anyKeywords)){
							chineseCount = StringUtil.getChineseCountForSimple(anyKeywords);
						}
					}
				}
				if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && ObjectUtil.isNotEmpty(organization)){
					if (chineseCount > organization.getKeyWordsNum()){
						throw new TRSException(CodeUtils.FAIL,"该专题暂时仅支持检索"+organization.getKeyWordsNum()+"个关键字，如需更多，请联系相关运维人员。");
					}
				}

				String timerange = "";
				/*if (timeRange.contains("d") || timeRange.contains("h")) {
					timerange = timeRange;
				}*/
				timerange = timeRange;
				String[] formatTimeRange = DateUtil.formatTimeRange(timeRange);
				SearchScope scope = SearchScope.valueOf(searchScope);
				Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[0]);
				Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(formatTimeRange[1]);
				SpecialProject specialProject = new SpecialProject(userId, type, specialName, allKeywords, anyKeywords,
						excludeWords, trsl, statusTrsl, weChatTrsl, scope, startTime, endTime, source, groupName,
						groupId, timerange, isSimilar, irSimflag, weight, server,irSimflagAll,excludeWeb);
				String imgUrl = "";
				specialProject.setImgUrl(imgUrl);
				specialProject.setStart(new SimpleDateFormat("yyyyMMddHHmmss").format(startTime));
				specialProject.setEnd(new SimpleDateFormat("yyyyMMddHHmmss").format(endTime));
				// specialProject.setServer(server);
				// 新添加的放前边 查找时按照sequence正序排列
				specialSequence(groupId, specialProject, "before");
				specialService.createSpecial(specialProject);
				PerpetualPool.put(userId, DateUtil.formatCurrentTime("yyyyMMddHHmmss"));

				// 新建专题成功,默认触发专题指数计算服务
//				fixedThreadPool.execute(() -> compute());
				return specialProject;
			} else {
				throw new OperationException("创建监测方案失败");
			}
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("创建监测方案失败,message:" + e, e);
		}
	}

	/**
	 * 取消置顶和增加专项时调用 修改sequence(分组)
	 * 
	 * @param groupId
	 *            分组Id
	 * @param specialProject
	 *            专项
	 * @param beforeOrEnd
	 *            这个专项放前边还是放后边 正常是取消置顶的放后边 新添加的放前边
	 */
	public void specialSequence(String groupId, SpecialProject specialProject, String beforeOrEnd) {
		// 把已经置顶的排在当前专项后边的都往前提一个
		User loginUser = UserUtils.getUser();
		if ("end".equals(beforeOrEnd)) {
			Criteria<SpecialProject> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("topFlag", "top"));
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				criteria.add(Restrictions.eq("userId",loginUser.getId()));
			}else {
				criteria.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			}
			criteria.add(Restrictions.gt("sequence", specialProject.getSequence()));
			List<SpecialProject> findAll = specialProjectService.findAll(criteria);
			for (SpecialProject project : findAll) {
				project.setSequence(project.getSequence() - 1);
				specialProjectService.save(project);
			}
		}
		// 取消置顶 放后边
		int n = 0;
		if (StringUtil.isNotEmpty(groupId)) {// 是二级或者一级分类下的
			SpecialSubject subject = specialSubjectService.findOne(groupId);
			// 二级分类下的就查找下边有多少个专项
			if (StringUtil.isNotEmpty(subject.getSubjectId())) {
				List<SpecialProject> findByGroupId = specialProjectService.findByGroupId(subject.getId());
				// n = findByGroupId!=null?findByGroupId.size()+1:1;
				// 之前的都+1
				for (SpecialProject special : findByGroupId) {
					// 非置顶的
					if (StringUtil.isEmpty(special.getTopFlag())) {
						n++;
						if ("before".equals(beforeOrEnd)) {
							// for(SpecialProject special:findByGroupId){
							special.setSequence(special.getSequence() + 1);
							specialProjectService.save(special);
						}
					}
				}
			} else {
				// 一级分类下的就得查下边有多少个专项和专题
				List<SpecialSubject> subjectList = specialSubjectService.findBySubjectId(subject.getId());
				if (subjectList != null) {
					n += subjectList.size();
					if ("before".equals(beforeOrEnd)) {
						for (SpecialSubject specialSuject : subjectList) {
							specialSuject.setSequence(specialSuject.getSequence() + 1);
							specialSubjectService.save(specialSuject);
						}
					}

				}
				List<SpecialProject> specialList = specialProjectService.findByGroupId(subject.getId());
				if (specialList != null) {
					// n+=specialList.size();
					for (SpecialProject project : specialList) {
						// 非置顶的
						if (StringUtil.isEmpty(project.getTopFlag())) {
							n++;
							if ("before".equals(beforeOrEnd)) {
								project.setSequence(project.getSequence() + 1);
								specialProjectService.save(project);
							}
						}
					}
				}
			}
		} else {
			// 查当前用户有多少个一级的专项和多少个一级栏目
			Criteria<SpecialProject> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("groupId", ""));
			criteria.add(Restrictions.eq("userId", loginUser.getId()));
			List<SpecialProject> findAll = specialProjectService.findAll(criteria);
			for (SpecialProject specialproject : findAll) {
				// 非置顶的
				if (StringUtil.isEmpty(specialproject.getTopFlag())) {
					n++;// 取消置顶
					if ("before".equals(beforeOrEnd)) {// 专项新建
						specialproject.setSequence(specialproject.getSequence() + 1);
						specialProjectService.save(specialproject);
					}
				}
			}
			Criteria<SpecialSubject> criteriaSubject = new Criteria<>();
			criteriaSubject.add(Restrictions.eq("userId", loginUser.getId()));
			// criteria.add(Restrictions.eq("subjectId", ""));
			List<SpecialSubject> list = specialSubjectService.findAll(criteriaSubject);
			// n=findAll!=null?findAll.size():0;
			// 该用户有多少个主题
			for (SpecialSubject specialSubject : list) {
				if (StringUtil.isEmpty(specialSubject.getSubjectId())) {
					n++;
					if ("before".equals(beforeOrEnd)) {
						specialSubject.setSequence(specialSubject.getSequence() + 1);
						specialSubjectService.save(specialSubject);
					}
				}
			}
		}
		// 不为空就是修改 为空就是添加
		if (StringUtil.isNotEmpty(specialProject.getId())) {
			n = n - 1;
			specialProject.setSequence(n + 1);
		} else {
			specialProject.setSequence(1);
		}
	}

	/**
	 * 异步专题指数计算服务
	 * 
	 * @since changjiang @ 2018年5月8日
	 * @Return : void
	 */
	private void compute() {
		try {
			log.error("异步专题指数计算服务开始....");
			String[] org = { UserUtils.getUser().getOrganizationId() };
			this.computeService.compute(org);
		} catch (ParseException e) {
			log.error("专题指数计算计算失败!", e);
		} catch (TRSSearchException e) {
			log.error("专题指数计算计算失败!", e);
		} catch (OperationException e) {
			log.error("专题指数计算计算失败!", e);
		}
	}

	/**
	 * 预览数据
	 * 
	 * @date Created at 2017年11月27日 下午4:04:10
	 * @Author 谷泽昊
	 * @param request
	 * @param specialName
	 * @param timeRange
	 * @param allKeywords
	 * @param anyKeywords
	 * @param excludeWords
	 * @param trsl
	 * @param searchScope
	 * @param specialType
	 * @param source
	 * @param groupName
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("预览数据")
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
			@ApiImplicitParam(name = "weight", value = "是否按权重查找", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排,url排", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "server", value = "是否查server", dataType = "boolean", paramType = "query", required = false),
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
			@RequestParam(value = "weight", required = false) boolean weight,
			@RequestParam(value = "simflag", required = false) String simflag,
			@RequestParam(value = "excludeWeb", required = false) String excludeWeb,
			@RequestParam(value = "server", required = false) boolean server,
			@RequestParam(value = "groupName", required = false) String groupName) throws TRSException {
		String userId = UserUtils.getUser().getId();
		try {
			// 默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			boolean irSimflagAll = false;
			if ("netRemove".equals(simflag)) {
				isSimilar = true;
			} else if ("urlRemove".equals(simflag)) {
				irSimflag = true;
			}else if ("sourceRemove".equals(simflag)){
				irSimflagAll = true;
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
			// 如果是专家模式 哪个先不为空查哪个
			// 专家模式
			if (SpecialType.SPECIAL.equals(type)) {
				if (StringUtil.isNotEmpty(trsl)) {
					source = "传统媒体";
				} else if (StringUtil.isNotEmpty(statusTrsl)) {
					source = "微博";
				} else if (StringUtil.isNotEmpty(weChatTrsl)) {
					source = "微信";
				}
			}
			SpecialProject specialProject = new SpecialProject(userId, type, specialName, allKeywords, anyKeywords,
					excludeWords, trsl, statusTrsl, weChatTrsl, scope, startTime, endTime, source, groupName, groupId,
					timerange, isSimilar, irSimflag, weight, server,irSimflagAll,excludeWeb);
			return specialService.preview(specialProject, source);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("预览失败,message: " + e, e);
		}
	}

	/**
	 * 获取专项详细信息
	 *
	 * @param specialId
	 *            专项ID
	 */
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

	/**
	 * 修改监测方案
	 * 
	 * @date Created at 2017年11月27日 下午4:53:24
	 * @Author 谷泽昊
	 * @param specialId
	 *            专项Id
	 * @param specialName
	 *            专项名称
	 * @param timeRange
	 *            时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]
	 * @param anyKeywords
	 *            任意关键词[中国,河北;美国,洛杉矶]
	 * @param excludeWords
	 *            排除词[雾霾;沙尘暴]
	 * @param trsl
	 *            专家模式表达式
	 * @param searchScope
	 *            搜索范围[TITLE，TITLE_ABSTRACT, TITLE_CONTENT]
	 * @param specialType
	 *            专项模式[COMMON, SPECIAL]
	 * @param source
	 *            来源
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_UPDATE, systemLogType = SystemLogType.SPECIAL,
			systemLogOperationPosition = "修改专项：${specialId}",methodDescription="${specialName}")
	@ApiOperation("修改监测方案")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialId", value = "专项Id", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "specialName", value = "专项名称", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "timeRange", value = "时间范围[yyyy-MM-dd HH:mm:ss;yyyy-MM-dd HH:mm:ss]", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "anyKeywords", value = "任意关键词[中国,河北;美国,洛杉矶]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWords", value = "排除词[雾霾;沙尘暴]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "trsl", value = "专家模式表达式", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "searchScope", value = "搜索范围[TITLE，TITLE_ABSTRACT, TITLE_CONTENT]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "specialType", value = "专项模式[COMMON, SPECIAL]", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "weight", value = "是否按照权重查找", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "excludeWeb", value = "排除网站", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "monitorSite", value = "监测网站", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "simflag", value = "排重方式 不排，全网排,url排", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "server", value = "是否转换为server表达式", dataType = "boolean", paramType = "query", required = false),
			@ApiImplicitParam(name = "source", value = "来源", dataType = "String", paramType = "query", required = false),
			@ApiImplicitParam(name = "mediaLevel", value = "媒体等级", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaIndustry", value = "媒体行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentIndustry", value = "内容行业", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "filterInfo", value = "信息过滤", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "contentArea", value = "信息地域", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "mediaArea", value = "媒体地域", dataType = "String", paramType = "query")
	})
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public Object updateSpecial(@RequestParam("specialId") String specialId,
								@RequestParam("specialName") String specialName, @RequestParam("timeRange") String timeRange,
								@RequestParam(value = "anyKeywords", required = false) String anyKeywords,
								@RequestParam(value = "excludeWords", required = false) String excludeWords,
								@RequestParam(value = "excludeWordsIndex", required = false) String excludeWordsIndex,
								@RequestParam(value = "trsl", required = false) String trsl,
								@RequestParam(value = "searchScope", required = false, defaultValue = "TITLE") String searchScope,
								@RequestParam(value = "specialType", required = false, defaultValue = "COMMON") String specialType,
								@RequestParam(value = "weight", required = false) boolean weight,
								@RequestParam(value = "excludeWeb", required = false) String excludeWeb,
								@RequestParam(value = "monitorSite", required = false) String monitorSite,
								@RequestParam(value = "simflag", required = false) String simflag,
								@RequestParam(value = "server", required = false) boolean server,
								@RequestParam(value = "source", required = false) String source,
								@RequestParam(value = "mediaLevel", required = false) String mediaLevel,
								@RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
								@RequestParam(value = "contentIndustry", required = false) String contentIndustry,
								@RequestParam(value = "filterInfo", required = false) String filterInfo,
								@RequestParam(value = "contentArea", required = false) String contentArea,
								@RequestParam(value = "mediaArea", required = false) String mediaArea) throws TRSException {

		//若为机构管理员或者普通用户 若为普通模式，判断关键字字数
		User loginUser = UserUtils.getUser();
		if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())){
			Organization organization = organizationRepository.findOne(loginUser.getOrganizationId());

			int chineseCount = 0;
			if (StringUtil.isNotEmpty(anyKeywords)){
				chineseCount = StringUtil.getChineseCountForSimple(anyKeywords);
			}else if (StringUtil.isNotEmpty(trsl)){
				int trslCount = StringUtil.getChineseCount(trsl);
				chineseCount = trslCount;
			}
			if (chineseCount > organization.getKeyWordsNum()){
				throw new TRSException(CodeUtils.FAIL,"该专题暂时仅支持检索"+organization.getKeyWordsNum()+"个关键字，如需更多，请联系相关运维人员。");
			}
		}
		try {
			// 默认不排重
			boolean isSimilar = false;
			boolean irSimflag = false;
			boolean irSimflagAll = false;
			if ("netRemove".equals(simflag)) {
				isSimilar = true;
			} else if ("urlRemove".equals(simflag)) {
				irSimflag = true;
			}else if ("sourceRemove".equals(simflag)){
				irSimflagAll = true;
			}
			SpecialType type = SpecialType.valueOf(specialType);
			SearchScope scope = SearchScope.valueOf(searchScope);
			String timerange = "";
			/*if (timeRange.contains("d") || timeRange.contains("h")) {
				timerange = timeRange;
			}*/
			if (timeRange != null && !"".equals(timeRange)) {
				timerange = timeRange;
			}else {
				timerange = "7d";
			}
			String[] formatTimeRange = DateUtil.formatTimeRange(timeRange);
			Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(formatTimeRange[0]);
			Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(formatTimeRange[1]);
			// 专家模式
			if (SpecialType.SPECIAL.equals(type)) {
				if (StringUtil.isEmpty(trsl)) {
					throw new OperationException("创建监测方案失败");
				}
			}

			// 专家模式才会去server
			if (SpecialType.COMMON.equals(specialType)) {
				server = false;
			}
			SpecialProject updateSpecial = specialService.updateSpecial(specialId, type, specialName,
					anyKeywords, excludeWords,excludeWordsIndex, trsl, scope, startTime, endTime, source,
					timerange, isSimilar, weight, irSimflag, server,irSimflagAll,excludeWeb, monitorSite,mediaLevel,
					 mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea);

			// 修改专题成功,触发修改该专题当前日期指数
			fixedThreadPool.execute(() -> computeBySpecialId(specialId, new Date(), new Date()));
			//fixedThreadPool.execute(() -> compute());
			return updateSpecial;
		} catch (Exception e) {
			throw new OperationException("修改监测方案失败:" + e, e);
		}
	}

	/**
	 * 触发修改该专题当前日期指数
	 * 
	 * @since changjiang @ 2018年5月8日
	 * @param specialId
	 * @param begin
	 * @param end
	 * @Return : void
	 */
	private void computeBySpecialId(String specialId, Date begin, Date end) {
		try {
			this.computeService.computeBySpecialId(specialId, new Date(), new Date());
		} catch (TRSSearchException | TRSException | ParseException e) {
			log.error("异步修改该专题当前日期指数失败!specialId:[" + specialId + "]", e);
		}
	}
	@RequestMapping(value = "/specialList", method = RequestMethod.POST)
	public Object specialList(@RequestParam(value = "specialId") String specialId,
							  @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
							  @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
							  @RequestParam(value = "source", defaultValue = "ALL", required = false) String source,
							  @RequestParam(value = "sort", defaultValue = "", required = false) String sort,
							  @ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
							  @ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
							  @ApiParam("结果中搜索") @RequestParam(value = "keywords", required = false) String keywords,
							  @ApiParam("结果中搜索的范围") @RequestParam(value = "fuzzyValueScope", defaultValue = "fullText", required = false) String fuzzyValueScope,

							  @ApiParam("时间") @RequestParam(value = "timeRange", required = false) String timeRange,
							  @ApiParam("排重规则  -  替换栏目条件") @RequestParam(value = "simflag", required = false) String simflag,
							  @ApiParam("关键词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "wordIndex", required = false) String wordIndex,
							  @ApiParam("情感倾向") @RequestParam(value = "emotion", required = false) String emotion,
							  @ApiParam("阅读标记") @RequestParam(value = "read", required = false) String read,
							  @ApiParam("排除网站  替换栏目条件") @RequestParam(value = "excludeWeb", required = false) String excludeWeb,
							  @ApiParam("排除关键词  替换栏目条件") @RequestParam(value = "excludeWord", required = false) String excludeWord,
							  @ApiParam("排除词命中位置 0：标题、1：标题+正文、2：标题+摘要  替换栏目条件") @RequestParam(value = "excludeWordIndex", defaultValue = "1", required = false) String excludeWordIndex,
							  @ApiParam("修改词距标记 替换栏目条件") @RequestParam(value = "updateWordForm", defaultValue = "false", required = false) Boolean updateWordForm,
							  @ApiParam("词距间隔字符 替换栏目条件") @RequestParam(value = "wordFromNum", required = false) Integer wordFromNum,
							  @ApiParam("词距是否排序  替换栏目条件") @RequestParam(value = "wordFromSort", required = false) Boolean wordFromSort,
							  @ApiParam("媒体等级") @RequestParam(value = "mediaLevel", required = false) String mediaLevel,
							  @ApiParam("数据源  替换栏目条件") @RequestParam(value = "groupName", required = false) String groupName,
							  @ApiParam("媒体行业") @RequestParam(value = "mediaIndustry", required = false) String mediaIndustry,
							  @ApiParam("内容行业") @RequestParam(value = "contentIndustry", required = false) String contentIndustry,
							  @ApiParam("信息过滤") @RequestParam(value = "filterInfo", required = false) String filterInfo,
							  @ApiParam("信息地域") @RequestParam(value = "contentArea", required = false) String contentArea,
							  @ApiParam("媒体地域") @RequestParam(value = "mediaArea", required = false) String mediaArea,
							  @ApiParam("精准筛选") @RequestParam(value = "preciseFilter", required = false) String preciseFilter,
							  @ApiParam("OCR筛选，对图片的筛选：全部：ALL、仅看图片img、屏蔽图片noimg") @RequestParam(value = "imgOcr", defaultValue = "ALL", required = false) String imgOcr) throws TRSException {
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		log.info(loginpool.toString());
		log.warn("专项检测信息列表  开始调用接口"+ com.trs.netInsight.support.fts.util.DateUtil.formatCurrentTime(com.trs.netInsight.support.fts.util.DateUtil.yyyyMMdd));
		String userName = UserUtils.getUser().getUserName();
		long startTime = System.currentTimeMillis();

		try {
			SpecialProject specialProject = specialProjectService.findOne(specialId);
			ObjectUtil.assertNull(specialProject, "专题ID");
			if (StringUtils.isBlank(timeRange)) {
				timeRange = specialProject.getTimeRange();
				if (StringUtils.isBlank(timeRange)) {
					timeRange = com.trs.netInsight.support.fts.util.DateUtil.format2String(specialProject.getStartTime(), com.trs.netInsight.support.fts.util.DateUtil.yyyyMMdd) + ";";
					timeRange += com.trs.netInsight.support.fts.util.DateUtil.format2String(specialProject.getEndTime(), com.trs.netInsight.support.fts.util.DateUtil.yyyyMMdd);
				}
			}
			//排重
			if ("netRemove".equals(simflag)) { //单一媒体排重
				specialProject.setSimilar(true);
				specialProject.setIrSimflag(false);
				specialProject.setIrSimflagAll(false);
			} else if ("urlRemove".equals(simflag)) { //站内排重
				specialProject.setSimilar(false);
				specialProject.setIrSimflag(true);
				specialProject.setIrSimflagAll(false);
			} else if ("sourceRemove".equals(simflag)) { //全网排重
				specialProject.setSimilar(false);
				specialProject.setIrSimflag(false);
				specialProject.setIrSimflagAll(true);
			}
			//命中规则
			if (StringUtil.isNotEmpty(wordIndex) && StringUtil.isEmpty(specialProject.getTrsl())) {
				if (SearchScope.TITLE.equals(wordIndex)){
					specialProject.setSearchScope(SearchScope.TITLE);
				}
				if (SearchScope.TITLE_CONTENT.equals(wordIndex)){
					specialProject.setSearchScope(SearchScope.TITLE_CONTENT);
				}
				if (SearchScope.TITLE_ABSTRACT.equals(wordIndex)){
					specialProject.setSearchScope(SearchScope.TITLE_ABSTRACT);
				}

			}
			specialProject.setExcludeWeb(excludeWeb);
			//排除关键词
			specialProject.setExcludeWordIndex(excludeWordIndex);
			specialProject.setExcludeWords(excludeWord);

			//修改词距 选择修改词距时，才能修改词距
			if (updateWordForm != null && updateWordForm && StringUtil.isEmpty(specialProject.getTrsl()) && wordFromNum >= 0) {
				String keywordJson = specialProject.getAnyKeywords();
				com.alibaba.fastjson.JSONArray jsonArray = com.alibaba.fastjson.JSONArray.parseArray(keywordJson);
				//现在词距修改情况为：只有一个关键词组时，可以修改词距等，多个时不允许
				if (jsonArray != null && jsonArray.size() == 1) {
					Object o = jsonArray.get(0);
					JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
					jsonObject.put("wordSpace", wordFromNum);
					jsonObject.put("wordOrder", wordFromSort);
					jsonArray.set(0, jsonObject);
					specialProject.setAnyKeywords(jsonArray.toJSONString());
				}
			}
			// 跟统计表格一样 如果来源没选 就不查数据
			List<String> specialSource = CommonListChartUtil.formatGroupName(specialProject.getSource());
			if(!"ALL".equals(source)){
				source = CommonListChartUtil.changeGroupName(source);
				if(!specialSource.contains(source)){
					return null;
				}
			}else{
				source = StringUtils.join(specialSource,";");
			}
			specialProject.setConditionScreen(true);
			specialProject.addFilterCondition( mediaLevel, mediaIndustry, contentIndustry, filterInfo, contentArea, mediaArea);

			Object documentCommonSearch = infoListService.documentCommonSearch(specialProject, pageNo, pageSize, source,
					timeRange, emotion, sort, invitationCard,forwarPrimary, keywords, fuzzyValueScope,
					"special", read, preciseFilter,imgOcr);
			long endTime = System.currentTimeMillis();
			log.warn("间隔时间："+(endTime - startTime));
			return documentCommonSearch;


		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e,e);
		} finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			if (logReids.getFullHybase() > FtsFieldConst.OVER_TIME) {
				logReids.printTime(LogPrintUtil.INFO_LIST);
			}
			log.info("调用接口用了" + timeApi + "ms"+ com.trs.netInsight.support.fts.util.DateUtil.formatCurrentTime(com.trs.netInsight.support.fts.util.DateUtil.yyyyMMdd));
		}
	}

	/**
	 * 置顶 好像没用上
	 *
	 * @param id
	 *            专项id
	 */
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "专项Id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/top", method = RequestMethod.GET)
	public Object promotion(@RequestParam("id") String id) throws TRSException {
		try {
			SpecialProject specialProject = specialProjectService.findOne(id);
			specialProject.setLastModifiedTime(new Date());
			specialProjectService.save(specialProject);
			return "promotion success";
		} catch (Exception e) {
			throw new OperationException("置顶失败,message" + e, e);
		}
	}
	/**
	 * 删除
	 *
	 * @param projectId
	 *            专项id
	 */
	@ApiOperation("删除专题（新版）")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "projectId", value = "专题Id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/deleteProject", method = RequestMethod.POST)
	public Object deleteProject(@RequestParam("projectId") String projectId) throws TRSException {
		try {
			Object object = specialService.selectNextShowSpecial(projectId,SpecialFlag.SpecialProjectFlag);
			specialSubjectService.deleteProject(projectId);
			return object;
		} catch (Exception e) {
			throw new OperationException("删除失败,message" + e, e);
		}
	}
	/**
	 * 删除
	 *
	 * @param id
	 *            专项id
	 */
	@ApiOperation("删除专项")
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_DELETE, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "删除专项：${specialId}")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "specialId", value = "专项Id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public Object deleteSpecial(@RequestParam("specialId") String id) throws TRSException {
		try {
			Object object = specialService.selectNextShowSpecial(id,SpecialFlag.SpecialProjectFlag);
			specialProjectService.delete(id);
			// 删除缓存池
			String sumKey = "sum" + id;
			TimingCachePool.clear(sumKey);

			// 删除专题指数记录集
			String[] specialIds = { id };
			this.computeService.delete(specialIds);
			return object;
		} catch (Exception e) {
			throw new OperationException("删除失败,message" + e, e);
		}
	}

	@ApiOperation("RequestBody 测试")
	@FormatResult
	@RequestMapping(value = "/add111", method = RequestMethod.POST)
	public Object aaa(@ApiParam("专项json") @RequestBody SpecialProject sp) throws TRSException {
		try {
			return sp;
		} catch (Exception e) {
			throw new OperationException("创建监测方案失败,message:" + e, e);
		}
	}

	/**
	 * 根据专项名字查看专项
	 * 
	 * @date Created at 2017年11月27日 下午5:04:12
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 */
	@ApiOperation("根据专项名查看专项")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "selectName", value = "专项名", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/selectName", method = RequestMethod.GET)
	@FormatResult
	public Object selectName(@RequestParam("name") String name) throws TRSSearchException, TRSException {
		Criteria<SpecialProject> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("specialName", name));
		List<SpecialProject> findAll = specialProjectService.findBySpecialNameContains(name);
		return findAll;

	}

	/**
	 * 检测方案查询（分组）
	 * 
	 * @date Created at 2017年11月27日 下午2:39:57
	 * @Author 谷泽昊
	 * @param request
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 */
	@ApiOperation("监测方案栏目查询")
	@FormatResult
	@RequestMapping(value = "/selectSpecial", method = RequestMethod.GET)
	public Object selectSpecial(HttpServletRequest request) throws TRSSearchException, TRSException {
		User loginUser = UserUtils.getUser();
//		return specialService.selectSpecialNew(loginUser);
		return  specialService.selectSpecialReNew(loginUser);
	}

	/**
	 * 置顶之间拖拽接口（分组）
	 * @param ids
	 * @return
	 */
	@FormatResult
	@RequestMapping(value = "/moveTop", method = RequestMethod.POST)
	@ApiOperation("置顶之间拖拽接口")
	public Object move(@ApiParam("id按顺序排好;分割字符串") @RequestParam("ids") String ids) {
		return specialService.move(ids);
	}

	@FormatResult
	@RequestMapping(value = "/moveList", method = RequestMethod.POST)
	@ApiOperation("列表拖拽接口")
	public Object move(@ApiParam("被拖动文件夹或专题的id") @RequestParam("id") String id,
					   @ApiParam("被拖动文件夹或专题的新父级id") @RequestParam("parentId") String parentId,
					   @ApiParam("被拖动文件夹或专题的类型") @RequestParam("typeFlag") String typeFlag,
			@ApiParam("与被拖动文件夹或专题同级别的ids,按顺序排好;分割字符串") @RequestParam("ids") String[] ids,
			@ApiParam("与ids一一对应 ;分割 一级 0，二级 1， 专题传 2") @RequestParam("typeFlags") int[] typeFlags) throws TRSException{
		return specialService.moveListNew(id,parentId,typeFlag,ids, typeFlags);
	}
	@FormatResult
	@RequestMapping(value = "/moveProject", method = RequestMethod.POST)
	@ApiOperation("拖拽专题接口")
	public Object moveProject(@ApiParam("分组要拖拽后的父级分组") @RequestParam(value = "parentId", required = false) String parentId,
							  @ApiParam("被拖拽的对象的信息") @RequestParam("moveData") String moveData,
							  @ApiParam("拖拽完成后的顺序") @RequestParam("sequenceData") String sequenceData) throws TRSException{

		SpecialSubject parent = null;
		if(StringUtil.isNotEmpty(parentId)){
			parent = specialSubjectRepository.findOne(parentId);
			if(ObjectUtil.isEmpty(parent)){
				throw new TRSException(CodeUtils.FAIL,"对应的专题分组不存在");
			}
		}
		if(StringUtil.isEmpty(moveData)){
			throw new TRSException(CodeUtils.FAIL,"被拖拽的分组或专题信息为空");
		}
		if(StringUtil.isEmpty(sequenceData)){
			throw new TRSException(CodeUtils.FAIL,"拖拽后顺序为空");
		}
		User user = UserUtils.getUser();
		return specialService.moveProjectSequence(sequenceData,moveData,parentId,user);
	}

	/**
	 * 跨越等级拖动 （分组）
	 * @date Created at 2018年12月19日  下午3:15:20
	 * @author 北京拓尔思信息技术股份有限
	 * 公司
	 * @author 谷泽昊
	 * @param ids
	 * @param twoOrThree
	 * @return
	 */
	@FormatResult
	@RequestMapping(value = "/crossLevelDragging", method = RequestMethod.POST)
	@ApiOperation("跨越等级拖动")
	public Object crossLevelDragging(
			@ApiParam("父类id，如果是一级就传 one") @RequestParam(value="parentId",required=false) String parentId,
			@ApiParam("当前拖动栏目的id，一级二级传：oneOrtwo;id,专项传three;id") @RequestParam("specialId") String specialId,
			@ApiParam("拖动后位置的id按顺序排好;分割字符串") @RequestParam("ids") String ids,
			@ApiParam("与id一一对应 ;分割 一二级传oneOrtwo 专项传three") @RequestParam("twoOrThree") String twoOrThree) {
		return specialService.crossLevelDragging(parentId,specialId,ids, twoOrThree);
	}

	/**
	 * 新建主题 （分组）
	 * 
	 * @date Created at 2017年11月27日 下午5:02:15
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("新建主题")
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_ADD_SUBJECT, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "添加主题：@{name}")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "name", value = "主题名", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "parentId", value = "要创建的栏目分组的父级分组id", dataType = "String", paramType = "query")})
	@RequestMapping(value = "/addSubject", method = RequestMethod.GET)
	public Object addSubject(@RequestParam("name") String name,
							 @ApiParam("要创建的栏目分组的父级分组id") @RequestParam(value = "parentId" ,required = false) String parentId) throws TRSException {

		User loginUser = UserUtils.getUser();
		if (UserUtils.isRoleAdmin()){
			Organization organization = organizationRepository.findOne(loginUser.getOrganizationId());
			//机构管理员
			if (organization.getSpecialNum() <= specialProjectService.getSubGroupSpecialCount(loginUser)){
				throw new TRSException(CodeUtils.FAIL,"您目前创建的专题已达上限，该分组下已没有可新建专题分析的资源，如需更多，请联系相关运维人员。");
			}
		}
		if (UserUtils.isRoleOrdinary(loginUser)){
			//如果是普通用户 受用户分组 可创建资源的限制
			//查询该用户所在的用户分组下 是否有可创建资源
			SubGroup subGroup = subGroupRepository.findOne(loginUser.getSubGroupId());
			if (subGroup.getSpecialNum() <= specialProjectService.getSubGroupSpecialCount(loginUser)){
				throw new TRSException(CodeUtils.FAIL,"您目前创建的专题已达上限，该分组下已没有可新建专题分析的资源，如需更多，请联系相关运维人员。");
			}
		}
		return specialSubjectService.addSubject(name,parentId,loginUser);
//		return specialSubjectService.addSubject(name);
	}

	/**
	 * 新建专题
	 * 
	 * @date Created at 2017年11月27日 下午5:02:23
	 * @Author 谷泽昊
	 * @param specialName
	 * @param subjectId
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("新建专题")
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_ADD_ZHUAN, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "添加专题：${subjectId}/@{specialName}")
	@FormatResult
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specialName", value = "专题名", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "subjectId", value = "主题id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/addzhuan", method = RequestMethod.GET)
	public Object addzhuan(@RequestParam("specialName") String specialName,
			@RequestParam(value = "subjectId", required = false) String subjectId) throws TRSException {

		User loginUser = UserUtils.getUser();
		Organization organization =organizationRepository.findOne(loginUser.getOrganizationId());
		if (UserUtils.isRoleAdmin()){
			//机构管理员
			if (organization.getSpecialNum() <= specialProjectService.getSubGroupSpecialCount(loginUser)){
				throw new TRSException(CodeUtils.FAIL,"您目前创建的专题已达上限，该分组下已没有可新建专题分析的资源，如需更多，请联系相关运维人员。");
			}
		}
		if (UserUtils.isRoleOrdinary(loginUser)){
			//如果是普通用户 受用户分组 可创建资源的限制
			//查询该用户所在的用户分组下 是否有可创建资源
			SubGroup subGroup = subGroupRepository.findOne(loginUser.getSubGroupId());
			if (subGroup.getSpecialNum() <= specialProjectService.getSubGroupSpecialCount(loginUser)){
				throw new TRSException(CodeUtils.FAIL,"您目前创建的专题已达上限，该分组下已没有可新建专题分析的资源，如需更多，请联系相关运维人员。");
			}
		}

		SpecialSubject specialSubject = new SpecialSubject(specialName, subjectId);
		// 新添加的放前边 把当前一级sequence存为1 在他之前的专题和一级分类存为+1 查找时按sequence正序排列
		// 当前主题下有多少个二级和专项
		List<SpecialSubject> subjectList = specialSubjectService.findBySubjectId(subjectId);
		for (SpecialSubject subject : subjectList) {
			subject.setSequence(subject.getSequence() + 1);
			specialSubjectService.save(subject);
		}
		List<SpecialProject> specialList = specialProjectService.findByGroupId(subjectId);
		for (SpecialProject special : specialList) {
			special.setSequence(special.getSequence() + 1);
			specialProjectService.save(special);
		}
		specialSubject.setSequence(1);
		specialSubjectService.save(specialSubject);

		return specialSubject.getId();
	}

	/**
	 * 专题重命名
	 * 
	 * @date Created at 2017年11月27日 下午5:02:33
	 * @Author 谷泽昊
	 * @param newName
	 * @param specialId
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("专题重命名")
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_RENAME_SPECIAL, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "专题重命名：${subjectId}/@{newName}")
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

	/**
	 * 主题重命名
	 * 
	 * @date Created at 2017年11月27日 下午5:02:48
	 * @Author 谷泽昊
	 * @param newName
	 * @param subjectId
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("主题重命名")
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_RENAME_SUBJECT, systemLogType = SystemLogType.SPECIAL,
			systemLogOperationPosition = "主题重命名：${subjectId}/@{newName}",methodDescription="${newName}")
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

	/**
	 * 删除主题
	 * 
	 * @date Created at 2017年11月27日 下午5:02:57
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("删除分组")
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_DELETE_SUBJECT, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "删除主题：${id}")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "分组id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/deleteSubject", method = RequestMethod.POST)
	public Object deleteSubject(@RequestParam("id") String id) throws TRSException {
		try {
//			specialSubjectService.delete(id, "one");
			Object object = specialService.selectNextShowSpecial(id,SpecialFlag.SpecialSubjectFlag);
			specialSubjectService.deleteSubject(id);
			// }
			return object;
		} catch (Exception e) {
			throw new OperationException("删除主题失败" + e, e);
		}

	}
	/**
	 * 删除专题
	 * 
	 * @date Created at 2017年11月27日 下午5:03:33
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("删除专题")
	@Log(systemLogOperation = SystemLogOperation.SPECIAL_DELETE_ZHUANTI, systemLogType = SystemLogType.SPECIAL, systemLogOperationPosition = "删除专题：${id}")
	@FormatResult
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "专题id", dataType = "String", paramType = "query") })
	@RequestMapping(value = "/deleteZhuanTi", method = RequestMethod.GET)
	public Object deleteZhuanTi(@ApiParam("专题id") @RequestParam("id") String id) throws TRSException {
		try {
			// 删除专题
			specialSubjectService.delete(id, "two");
			return "删除专题成功";
		} catch (Exception e) {
			throw new OperationException("删除专题失败" + e, e);
		}
	}

	/**
	 * 专题联想词
	 * 
	 * @date Created at 2018年1月10日 下午4:56:36
	 * @Author 谷泽昊
	 * @param name
	 * @param pageSize
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("专题联想词")
	@FormatResult
	@RequestMapping(value = "/associational", method = RequestMethod.GET)
	public Object associational(@ApiParam(value = "名称") @RequestParam(value = "name", required = false) String name,
			@ApiParam(value = "size") @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize)
			throws TRSException {
		return specialProjectService.associational(name, pageSize);
	}

	/**
	 * 模糊查询专项
	 * 
	 * @date Created at 2018年1月18日 上午9:54:53
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("模糊查询专项")
	@FormatResult
	@RequestMapping(value = "/selectByWord", method = RequestMethod.GET)
	public Object selectByWord(@ApiParam(value = "名称") @RequestParam(value = "name", required = false) String name)
			throws TRSException {
		return specialProjectService.selectByWord(name);
	}
	/**
	 * 修改历史数据  词距
	 * @param request
	 * @param response
	 */
	@ApiOperation("修改历史数据 专题分析关键词")
	@PostMapping(value = "/changHistoryDataForSpecialProjectWordSpacing")
	public void changHistoryDataForSpecialProject(javax.servlet.http.HttpServletRequest request, HttpServletResponse response) {
		//查询所有用户分组
		List<SpecialProject> specialProjects = specialProjectService.findBySpecialType(SpecialType.COMMON);
		if (ObjectUtil.isNotEmpty(specialProjects)){
			System.err.println("普通模式专题数："+specialProjects.size());
			for (SpecialProject specialProject : specialProjects) {
				String anyKeywords = specialProject.getAnyKeywords();
				System.err.println("专题关键字："+anyKeywords);
				if (StringUtil.isNotEmpty(anyKeywords)){
					Map<String, Object> hashMap = new HashMap<>();
					hashMap.put("wordSpace",0);
					hashMap.put("wordOrder",false);
					hashMap.put("keyWords",anyKeywords);
					String toJSONString = JSONObject.toJSONString(hashMap);
					specialProject.setAnyKeywords("["+toJSONString+"]");
					specialProjectService.save(specialProject);
				}
			}
		}
		System.err.println("专题分析修改成功！");
	}

	public static void main(String[] args) {
		Map<String, Object> hashMap = new HashMap<>();
		hashMap.put("wordSpace",0);
		hashMap.put("wordOrder",false);
		hashMap.put("keyWords","北京");
		String toJSONString = JSONObject.toJSONString(hashMap);
		JSONArray jsonArray = JSONArray.fromObject("["+toJSONString+"]");
		for (Object keyWord : jsonArray) {

			net.sf.json.JSONObject parseObject = net.sf.json.JSONObject.fromObject(String.valueOf(keyWord));
			String keyWords = parseObject.getString("keyWords");
			int wordSpace = parseObject.getInt("wordSpace");
			boolean wordOrder = parseObject.getBoolean("wordOrder");
			System.err.println(keyWords);
			System.err.println(wordSpace);
			System.err.println(wordOrder);
		}
	}
}
