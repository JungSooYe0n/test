package com.trs.netInsight.widget.alert.controller;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.UserHelp;
import com.trs.netInsight.widget.alert.constant.AlertAutoConst;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.service.IAlertAccountService;
import com.trs.netInsight.widget.alert.service.IAlertRuleService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.SubGroupRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 预警接口
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Slf4j
@RestController
@RequestMapping("/rule")
@Api(description = "新建预警接口")
public class AlertRuleController {

	@Autowired
	private IAlertRuleService alertRuleService;

	@Autowired
	private IAlertAccountService alertAccountService;

	@Autowired
	private UserHelp userService;
	@Autowired
	private AlertRuleRepository alertRuleRepository;
	@Autowired
	private SubGroupRepository subGroupRepository;

	@Autowired
	private OrganizationRepository organizationRepository;
	@Value("${http.client}")
	private boolean httpClient;
	@Value("${http.alert.netinsight.url}")
	private String alertNetinsightUrl;

	private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

	/**
	 * 请求自动预警工程项目的修改自动预警预警 - 只有按数量预警可以修改成功
	 * 需要自动预警时，再去数据中心注册相关信息，所以将管理交给自动预警项目，自动预警项目启动时才可注册
	 * @param alertRule
	 * @param interfaceInfo
	 */
	private void managementAutoAlertRule(AlertRule alertRule, String interfaceInfo) {
		if (alertRule != null && StringUtil.isNotEmpty(alertRule.getId()) && StringUtil.isNotEmpty(interfaceInfo)) {
			if (AlertSource.ARTIFICIAL.equals(alertRule.getAlertType())) {
				//当前为手动预警，不可以进行自动预警的注册，但是如果之前是自动预警，则将当前预警信息删除
				interfaceInfo = AlertAutoConst.alertNetInsight_delete_auto;
			}
			if("md5".equals(alertRule.getCountBy())){
				//当前为按热度值预警，热度值预警不在数据中心的自动预警中注册，所以需要判断之前是否有
				interfaceInfo = AlertAutoConst.alertNetInsight_delete_auto;
			}
			Map<String, String> param = new HashMap<>();
			param.put("id", alertRule.getId());
			String result = HttpUtil.doPost(alertNetinsightUrl + interfaceInfo, param, "utf-8");
			log.info("接口请求结果为：" + result);
		} else {
			log.info("方法执行失败，当前存在个别数据为空");
		}
	}

	/**
	 * 分页查询(分组)
	 *
	 * @date Created at 2018年3月1日 下午3:02:19
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("分页查询")
	@FormatResult
	@RequestMapping(value = "/pageList", method = RequestMethod.GET)
	public Object pageList(
			@ApiParam("页码") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
			@ApiParam("size") @RequestParam(value = "pageSize", required = false, defaultValue = "15") int pageSize)
			throws OperationException {
		Page<AlertRule> pageList = null;
		try {
			pageList = alertRuleService.pageList(UserUtils.getUser(), pageNo, pageSize);
		} catch (Exception e) {
			log.error("查询失败：", e);
			throw new OperationException("查询失败,message:" + e, e);
		}
		return pageList;
	}

	/**
	 * 不分页查询（分组）
	 *
	 * @date Created at 2018年3月1日 下午3:02:11
	 * @Author 谷泽昊
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("不分页查询")
	@Log(systemLogOperation = SystemLogOperation.ALERT_RULE_LIST, systemLogType = SystemLogType.ALERT, systemLogOperationPosition = "预警规则列表：")
	@FormatResult
	@RequestMapping(value = "/allList", method = RequestMethod.GET)
	public Object allList() throws OperationException {
		List<AlertRule> list = null;
		try {
			list = alertRuleService.selectAll(UserUtils.getUser());
		} catch (Exception e) {
			throw new OperationException("查询失败,message:" + e, e);
		}
		return list;
	}

	/**
	 * 专项预警保存
	 *
	 * @date Created at 2018年3月1日 上午10:46:22
	 * @Author 谷泽昊
	 * @param title
	 * @param timeInterval
	 * @param growth
	 * @param groupName
	 * @param anyKeyword
	 * @param excludeWords
	 * @param excludeSiteName
	 * @param scope
	 * @param sendWay
	 * @param websiteSendWay
	 * @param websiteId
	 * @param alertStartHour
	 * @param alertEndHour
	 * @param status
	 * @param alertType
	 * @param specialType
	 * @param trsl
	 * @param statusTrsl
	 * @param weChatTrsl
	 * @param week
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("新建预警")
	@Log(systemLogOperation = SystemLogOperation.ALERT_RULE_ADD, systemLogType = SystemLogType.ALERT, systemLogOperationPosition = "预警标题：${title}")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@FormatResult
	public Object addSpecial(@ApiParam("预警标题") @RequestParam("title") String title,
			@ApiParam("定时推送时间间隔 即频率 5min;30min;1h") @RequestParam(value = "timeInterval", required = false, defaultValue = "60") int timeInterval,
			@ApiParam("增长量 默认0") @RequestParam(value = "growth", required = false, defaultValue = "0") int growth,
			// @ApiParam("预警机制 true：全量 false：排重 默认全量") @RequestParam(value =
			// "repetition", required = false, defaultValue = "true") boolean
			// repetition,
			@ApiParam("排重方式 不排 no，全网排 netRemove,url排 urlRemove,跨数据源排 sourceRemove") @RequestParam(value = "simflag", required = false) String simflag,
			@ApiParam("数据来源(可多值,中间以';'隔开,默认为ALL，查询当前用户所能查取数据)") @RequestParam(value = "groupName", required = false, defaultValue = "ALL") String groupName,
			@ApiParam("任意关键词") @RequestParam(value = "anyKeyword", required = false) String anyKeyword,
			@ApiParam("排除词") @RequestParam(value = "excludeWords", required = false) String excludeWords,
			@ApiParam("排除词命中位置") @RequestParam(value = "excludeWordsIndex", required = false) String excludeWordsIndex,
			@ApiParam("排除网站") @RequestParam(value = "excludeSiteName", required = false) String excludeSiteName,
							 @ApiParam("排除网站") @RequestParam(value = "monitorSite", required = false) String monitorSite,
			@ApiParam("关键词位置 TITLE；TITLE_ABSTRACT；TITLE_CONTENT") @RequestParam(value = "scope", required = false, defaultValue = "TITLE") String scope,
			@ApiParam("发送方式 ") @RequestParam(value = "sendway", defaultValue = "EMAIL", required = false) String sendWay,
			@ApiParam("站内用户发送方式 ") @RequestParam(value = "websiteSendWay", defaultValue = "EMAIL", required = false) String websiteSendWay,
			@ApiParam("站内用户id ") @RequestParam(value = "websiteId", required = false) String websiteId,
			@ApiParam("预警开始时间") @RequestParam(value = "alertStart", required = false, defaultValue = "00:00") String alertStartHour,
			@ApiParam("预警结束时间") @RequestParam(value = "alertEnd", required = false, defaultValue = "00:00") String alertEndHour,
			@ApiParam("预警开关 OPEN,CLOSE 默认开启") @RequestParam(value = "status", required = false, defaultValue = "OPEN") String status,
			@ApiParam("预警类型") @RequestParam(value = "alertType", required = false, defaultValue = "AUTO") String alertType,
			@ApiParam("预警模式") @RequestParam(value = "specialType", required = false, defaultValue = "COMMON") String specialType,
			@ApiParam("传统媒体表达式") @RequestParam(value = "trsl", required = false) String trsl,
			@ApiParam("微博表达式") @RequestParam(value = "statusTrsl", required = false) String statusTrsl,
			@ApiParam("微信表达式") @RequestParam(value = "weChatTrsl", required = false) String weChatTrsl,
			@ApiParam("是否按权重查找") @RequestParam(value = "weight", required = false) boolean weight,
		    @ApiParam("排序方式") @RequestParam(value = "sort", required = false) String sort,
		    @ApiParam("默认空按数量计算预警  md5按照热度值计算预警") @RequestParam(value = "countBy", required = false) String countBy,
			@ApiParam("按热度值预警时 分类统计大于这个值时发送预警") @RequestParam(value = "md5Num", defaultValue = "0") int md5Num,
			@ApiParam("按热度值预警时  拼builder的时间范围") @RequestParam(value = "md5Range", defaultValue = "0") int md5Range,
			@ApiParam("发送时间，。星期一;星期二;星期三;星期四;星期五;星期六;星期日") @RequestParam(value = "week", required = false, defaultValue = "星期一;星期二;星期三;星期四;星期五;星期六;星期日") String week)
			throws TRSException {
		//anyKeyword = "[{\"wordSpace\":0,\"wordOrder\":\"false\",\"keyWords\":\"湖人总冠军\"}]";
		//首先判断下用户权限（若为机构管理员，只受新建与编辑的权限，不受可创建资源数量的限制）
		User loginUser = UserUtils.getUser();
		Organization organization = null;
		if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser)) && StringUtil.isNotEmpty(loginUser.getOrganizationId())){
			organization = organizationRepository.findOne(loginUser.getOrganizationId());
		}

		if (UserUtils.isRoleAdmin()){
			//机构管理员(通过userID查询)
			if (organization.getColumnNum() <= alertRuleService.getSubGroupAlertCount(loginUser)){
				throw new TRSException(CodeUtils.FAIL,"您目前创建的预警主题已达上限，如需更多，请联系相关运维人员。");
			}
		}
		if (UserUtils.isRoleOrdinary(loginUser)){
			//如果是普通用户 受用户分组 可创建资源的限制
			//查询该用户所在的用户分组下 是否有可创建资源
			SubGroup subGroup = subGroupRepository.findOne(loginUser.getSubGroupId());
			//通过用户分组
			if (subGroup.getColumnNum() <= alertRuleService.getSubGroupAlertCount(loginUser)){
				throw new TRSException(CodeUtils.FAIL,"您目前创建的预警主题已达上限，如需更多，请联系相关运维人员。！");
			}
		}
		//若为机构管理员或者普通用户 若为普通模式，判断关键字字数
		if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
			int chineseCount = 0;
			if (StringUtil.isNotEmpty(anyKeyword)){
				chineseCount = StringUtil.getChineseCountForSimple(anyKeyword);
			}else if (StringUtil.isNotEmpty(trsl) || StringUtil.isNotEmpty(weChatTrsl) || StringUtil.isNotEmpty(statusTrsl)){
				int trslCount = StringUtil.getChineseCount(trsl);
				int weChatTrslCount = StringUtil.getChineseCount(weChatTrsl);
				int statusTrslCount = StringUtil.getChineseCount(statusTrsl);
				chineseCount = trslCount+weChatTrslCount+statusTrslCount;
			}
			if (ObjectUtil.isNotEmpty(organization) && (chineseCount > organization.getKeyWordsNum())){
				throw new TRSException(CodeUtils.FAIL,"该预警主题暂时仅支持检索"+organization.getKeyWordsNum()+"个关键字，如需更多，请联系相关运维人员。");
			}
		}
		// 默认不排重
		boolean repetition = false;
		boolean irSimflag = false;
		boolean irSimflagAll = false;
		if ("netRemove".equals(simflag)) {
			repetition = true;
		} else if ("urlRemove".equals(simflag)) {
			irSimflag = true;
		}else if ("sourceRemove".equals(simflag)){
			irSimflagAll = true;
		}
		if(groupName == null || "".equals(groupName)){
			groupName = "ALL";
		}

		ScheduleStatus statusValue = ScheduleStatus.valueOf(status);
		SearchScope scopeValue = SearchScope.valueOf(scope);
		AlertSource alertSource = AlertSource.valueOf(alertType);
		SpecialType type = SpecialType.valueOf(specialType);
		String frequencyId = null;
		// 确定定时预警时走哪个方法
		if (AlertSource.AUTO.equals(alertSource)) {
			if ("md5".equals(countBy)) {
				if (timeInterval == 30) {
					frequencyId = "4";
				} else if (timeInterval == 60) {
					frequencyId = "5";
				} else if (timeInterval == 120) {
					frequencyId = "6";
				} else if (timeInterval == 180) {
					frequencyId = "7";
				}
			} else {
				md5Num = 0;
				md5Range = 0;
				frequencyId = "3";// 默认按数量统计
			}
		}
		// 我让前段把接受者放到websiteid里边了 然后用户和发送方式一一对应 和手动发送方式一致
		AlertRule alertRule = new AlertRule(statusValue, title, timeInterval, growth, repetition, irSimflag,irSimflagAll,groupName, anyKeyword,
				excludeWords,excludeWordsIndex, excludeSiteName,monitorSite,scopeValue, sendWay, websiteSendWay, websiteId, alertStartHour,
				alertEndHour, null, 0L, alertSource, week, type, trsl, statusTrsl, weChatTrsl, weight,sort, null, null,
				countBy, frequencyId, md5Num, md5Range, false, false);
		// timeInterval看逻辑是按分钟存储 2h 120
		try {
			// 验证方法
			AlertRule addAlertRule = alertRuleService.addAlertRule(alertRule);
			if (addAlertRule != null) {
				fixedThreadPool.execute(() -> this.managementAutoAlertRule(addAlertRule, AlertAutoConst.alertNetInsight_save_auto));
				return addAlertRule.getId();
			}
		} catch (Exception e) {
			throw new OperationException("新建预警失败:" + e, e);
		}
		return null;
	}

	/**
	 * 专项预警修改
	 *
	 * @date Created at 2018年3月1日 上午10:46:04
	 * @Author 谷泽昊
	 * @param id
	 * @param title
	 * @param timeInterval
	 * @param growth
	 * @param anyKeyword
	 * @param excludeWords
	 * @param excludeSiteName
	 * @param scope
	 * @param sendWay
	 * @param websiteSendWay
	 * @param websiteId
	 * @param alertStartHour
	 * @param alertEndHour
	 * @param status
	 * @param alertType
	 * @param specialType
	 * @param trsl
	 * @param statusTrsl
	 * @param weChatTrsl
	 * @param week
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("专项预警保存修改")
	@Log(systemLogOperation = SystemLogOperation.ALERT_RULE_UPDATE, systemLogType = SystemLogType.ALERT, systemLogOperationPosition = "预警标题：${title}")
	@FormatResult
	@RequestMapping(value = "/saveUpdate", method = RequestMethod.POST)
	public Object saveUpdate(@ApiParam("预警规则id") @RequestParam("id") String id,
			@ApiParam("预警标题") @RequestParam("title") String title,
			@ApiParam("定时推送时间间隔 即频率 5min;30min;1h") @RequestParam(value = "timeInterval", required = false, defaultValue = "60") int timeInterval,
			@ApiParam("增长量 默认0") @RequestParam(value = "growth", required = false, defaultValue = "0") int growth,
			// @ApiParam("预警机制 true：全量 false：排重 默认全量") @RequestParam(value =
			// "repetition", required = false, defaultValue = "true") boolean
			// repetition,
			@ApiParam("排重方式 不排，全网排,url排") @RequestParam(value = "simflag", required = false) String simflag,
			@ApiParam("数据来源(可多值,中间以';'隔开,默认为ALL，查询当前用户所能查取数据)") @RequestParam(value = "groupName", required = false, defaultValue = "ALL") String groupName,
			@ApiParam("任意关键词") @RequestParam(value = "anyKeyword", required = false) String anyKeyword,
			@ApiParam("排除词") @RequestParam(value = "excludeWords", required = false) String excludeWords,
			@ApiParam("排除词命中位置") @RequestParam(value = "excludeWordsIndex", required = false) String excludeWordsIndex,
			@ApiParam("排除网站") @RequestParam(value = "excludeSiteName", required = false) String excludeSiteName,
							 @ApiParam("监测网站") @RequestParam(value = "monitorSite", required = false) String monitorSite,
			@ApiParam("关键词位置 TITLE；TITLE_ABSTRACT；TITLE_CONTENT") @RequestParam(value = "scope", required = false, defaultValue = "TITLE") String scope,
			@ApiParam("发送方式 ") @RequestParam(value = "sendway", defaultValue = "EMAIL", required = false) String sendWay,
			@ApiParam("站内用户发送方式 ") @RequestParam(value = "websiteSendWay", defaultValue = "EMAIL", required = false) String websiteSendWay,
			@ApiParam("站内用户id ") @RequestParam(value = "websiteId", required = false) String websiteId,
			@ApiParam("预警开始时间") @RequestParam(value = "alertStart", required = false, defaultValue = "00:00") String alertStartHour,
			@ApiParam("预警结束时间") @RequestParam(value = "alertEnd", required = false, defaultValue = "00:00") String alertEndHour,
			@ApiParam("预警开关 OPEN,CLOSE 默认开启") @RequestParam(value = "status", required = false, defaultValue = "OPEN") String status,
			@ApiParam("预警类型") @RequestParam(value = "alertType", required = false, defaultValue = "AUTO") String alertType,
			@ApiParam("预警模式") @RequestParam(value = "specialType", required = false, defaultValue = "COMMON") String specialType,
			@ApiParam("传统媒体表达式") @RequestParam(value = "trsl", required = false) String trsl,
			@ApiParam("微博表达式") @RequestParam(value = "statusTrsl", required = false) String statusTrsl,
			@ApiParam("微信表达式") @RequestParam(value = "weChatTrsl", required = false) String weChatTrsl,
			@ApiParam("是否按权重查找") @RequestParam(value = "weight", required = false) boolean weight,
		    @ApiParam("排序方式") @RequestParam(value = "sort", required = false) String sort,
		    @ApiParam("默认空按数量计算预警  md5按照热度值计算预警") @RequestParam(value = "countBy", required = false) String countBy,
			@ApiParam("按热度值预警时 分类统计大于这个值时发送预警") @RequestParam(value = "md5Num", defaultValue = "0") int md5Num,
			@ApiParam("按热度值预警时  拼builder的时间范围") @RequestParam(value = "md5Range", defaultValue = "0") int md5Range,
			@ApiParam("发送时间，。星期一;星期二;星期三;星期四;星期五;星期六;星期日") @RequestParam(value = "week", required = false, defaultValue = "星期一;星期二;星期三;星期四;星期五;星期六;星期日") String week)
			throws TRSException {

		User loginUser = UserUtils.getUser();
		//若为机构管理员或者普通用户 若为普通模式，判断关键字字数
		if ((UserUtils.isRoleAdmin()|| UserUtils.isRoleOrdinary(loginUser))){
			Organization organization = organizationRepository.findOne(loginUser.getOrganizationId());
			int chineseCount = 0;
			if (StringUtil.isNotEmpty(anyKeyword)){
				chineseCount = StringUtil.getChineseCountForSimple(anyKeyword);
			}else if (StringUtil.isNotEmpty(trsl) || StringUtil.isNotEmpty(weChatTrsl) || StringUtil.isNotEmpty(statusTrsl)){
				int trslCount = StringUtil.getChineseCount(trsl);
				int weChatTrslCount = StringUtil.getChineseCount(weChatTrsl);
				int statusTrslCount = StringUtil.getChineseCount(statusTrsl);
				chineseCount = trslCount+weChatTrslCount+statusTrslCount;
			}
			if (chineseCount > organization.getKeyWordsNum()){
				throw new TRSException(CodeUtils.FAIL,"该预警主题暂时仅支持检索"+organization.getKeyWordsNum()+"个关键字，如需更多，请联系相关运维人员。");
			}
		}
		// 判断预警发送时的 接收人与接收方式 是否一一对应
		checkParams(websiteId,sendWay);
		AlertRule alertRule = null;
		// 默认不排重
		boolean repetition = false;
		boolean irSimflag = false;
		boolean irSimflagAll = false;
		if ("netRemove".equals(simflag)) {
			repetition = true;
		} else if ("urlRemove".equals(simflag)) {
			irSimflag = true;
		}else if ("sourceRemove".equals(simflag)){
			irSimflagAll = true;
		}
		if(groupName == null || "".equals(groupName)){
			groupName = "ALL";
		}
		try {
			alertRule = alertRuleService.findOne(id);
		} catch (Exception e) {
			log.error("修改预警失败：", e);
			throw new TRSException(CodeUtils.FAIL, "修改预警失败：" + e);
		}
		if (alertRule == null) {
			throw new TRSException(CodeUtils.FAIL, "修改预警失败，查不到此预警！");
		}
		ScheduleStatus statusValue = ScheduleStatus.valueOf(status);
		SearchScope scopeValue = SearchScope.valueOf(scope);
		AlertSource alertSource = AlertSource.valueOf(alertType);
		SpecialType type = SpecialType.valueOf(specialType);

		alertRule.setStatus(statusValue);
		alertRule.setTitle(title);
		alertRule.setTimeInterval(timeInterval);
		alertRule.setGrowth(growth);
		alertRule.setRepetition(repetition);
		alertRule.setGroupName(groupName);
		alertRule.setAnyKeyword(anyKeyword);
		alertRule.setExcludeWords(excludeWords);
		alertRule.setExcludeWordsIndex(excludeWordsIndex);
		alertRule.setExcludeSiteName(excludeSiteName);
		alertRule.setMonitorSite(monitorSite);
		alertRule.setScope(scopeValue);
		alertRule.setSendWay(sendWay);
		alertRule.setWebsiteSendWay(websiteSendWay);
		alertRule.setWebsiteId(websiteId);
		alertRule.setAlertStartHour(alertStartHour);
		alertRule.setAlertEndHour(alertEndHour);
		// 防止修改后第一次预警时间交叉导致预警内容重复 所以注掉这两行
//		alertRule.setLastStartTime(null);
//		alertRule.setLastExecutionTime(0L);
		alertRule.setAlertType(alertSource);
		alertRule.setWeek(week);
		alertRule.setSpecialType(type);
		alertRule.setTrsl(trsl);
		alertRule.setStatusTrsl(statusTrsl);
		alertRule.setWeChatTrsl(weChatTrsl);
		alertRule.setLastSendTime(null);
		alertRule.setReceiver(null);
		alertRule.setIrSimflag(irSimflag);
		alertRule.setWeight(weight);
		alertRule.setSort(sort);
		alertRule.setIrSimflagAll(irSimflagAll);
		String frequencyId = null;
		// 确定定时预警时走哪个方法
		if (AlertSource.AUTO.equals(alertSource)) {
			if ("md5".equals(countBy)) {
				if (timeInterval == 30) {
					frequencyId = "4";
				} else if (timeInterval == 60) {
					frequencyId = "5";
				} else if (timeInterval == 120) {
					frequencyId = "6";
				} else if (timeInterval == 180) {
					frequencyId = "7";
				}
			} else {
				md5Num = 0;
				md5Range = 0;
				frequencyId = "3";// 默认按数量统计
			}
		}
		alertRule.setFrequencyId(frequencyId);
		alertRule.setCountBy(countBy);
		// if(md5Num == null || md5Range == null){
		// md5Num =0;
		// md5Range = 0;
		// }
		alertRule.setMd5Num(md5Num);
		alertRule.setMd5Range(md5Range);
		try {
			// 验证方法
			AlertRule alertRuleUpdate = alertRuleService.addAlertRule(alertRule);
			if (alertRuleUpdate != null) {
				fixedThreadPool.execute(() -> this.managementAutoAlertRule(alertRuleUpdate, AlertAutoConst.alertNetInsight_save_auto));
				return "修改预警成功！";
			}
		} catch (Exception e) {
			throw new OperationException("修改预警失败" + e, e);
		}
		throw new TRSException(CodeUtils.FAIL, "修改预警失败！");
	}

	/**
	 * 信息列表手动预警发送邮件（分组）
	 * @param receivers
	 * @param documentId
	 * @param urltime
	 * @param content
	 * @param groupName
	 * @param trslk
	 * @param sendWay
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@ApiOperation("信息列表手动预警发送邮件")
	@Log(systemLogOperation = SystemLogOperation.ARTIFICIAL_ALERT, systemLogType = SystemLogType.ALERT, systemLogOperationPosition = "页面上填写的预警标题：${title}")
	@FormatResult
	@RequestMapping(value = "/listEmail", method = RequestMethod.POST)
	public Object listEmail(
			@ApiParam("接收者 多个时用英文;隔开") @RequestParam(value = "receivers", required = false) String receivers,
			@ApiParam("文章的sid") @RequestParam(value = "documentId", required = false) String documentId,
			@ApiParam("文章的urltime") @RequestParam(value = "urltime", required = false) String urltime,
			@ApiParam("页面上填写的预警标题") @RequestParam(value = "content", required = false) String content,
			@ApiParam("如果选择微信或微博 传") @RequestParam(value = "groupName", required = false) String groupName,
			@ApiParam("redis中存储表达式的key") @RequestParam(value = "trslk", required = false) String trslk,
			@ApiParam("发送方式") @RequestParam(value = "sendWay") String sendWay)
			throws TRSException, TRSSearchException {
		// 判断预警发送时的 接收人与接收方式 是否一一对应
		checkParams(receivers,sendWay);
		String userId = UserUtils.getUser().getId();
		try {
			return alertRuleService.send(receivers, documentId, urltime, content, userId, groupName, sendWay,trslk);
		} catch (Exception e) {
			throw new OperationException("手动发送预警失败,message:" + e, e);
		}
		// return userId;
	}

	/**
	 * 信息列表手动预警发送邮件--混合列表（分组）
	 * @param receivers
	 * @param sids
	 * @param urltime
	 * @param content
	 * @param groupNames
	 * @param trslk
	 * @param sendWay
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 * @throws OperationException
	 */
	@ApiOperation("信息列表手动预警发送邮件--混合列表")
	@Log(systemLogOperation = SystemLogOperation.ARTIFICIAL_ALERT, systemLogType = SystemLogType.ALERT, systemLogOperationPosition = "页面上填写的预警标题：${title}")
	@FormatResult
	@RequestMapping(value = "/listEmailBlend", method = RequestMethod.POST)
	public Object listEmailBlend(
			@ApiParam("接收者 多个时用英文;隔开") @RequestParam(value = "receivers", required = false) String receivers,
			@ApiParam("文章的sid和文章类型, 多个时用英文,隔开") @RequestParam(value = "sids", required = false) String[] sids,
			@ApiParam("文章的urltime") @RequestParam(value = "urltime", required = false) String urltime,
			@ApiParam("页面上填写的预警标题") @RequestParam(value = "content", required = false) String content,
			@ApiParam("如果选择微信或微博 , 多个时用英文,隔开") @RequestParam(value = "groupName", required = false) String[] groupNames,
			@ApiParam("redis中存储表达式的key") @RequestParam(value = "trslk", required = false) String trslk,
			@ApiParam("发送方式") @RequestParam(value = "sendWay") String sendWay)
			throws TRSException, TRSSearchException, OperationException {
		// 判断预警发送时的 接收人与接收方式 是否一一对应
		checkParams(receivers,sendWay);
		String userId = UserUtils.getUser().getId();
		try {
			if(sids ==null || sids.length == 0){
				throw new TRSException("未选择数据");
			}
			return alertRuleService.sendBlend(receivers, sids, urltime, content, userId, groupNames, sendWay,trslk);

		} catch (Exception e) {
			throw new OperationException("手动发送预警失败,message:" + e, e);
		}
	}

	/**
	 * 展示发送方式(分组...）
	 * @param type
	 * @return
	 */
	@ApiOperation("展示发送方式")
	@RequestMapping(value = "/sendAdd", method = RequestMethod.GET)
	@FormatResult
	public Object sendAdd(
			@ApiParam("类型 : SMS, EMAIL, WE_CHAT, APP, COMPOSITE, SMSandEMAIL, SMSandWE_CHAT") @RequestParam(value = "type", defaultValue = "ALL") SendWay type) {
		User loginUser = UserUtils.getUser();
		List<AlertAccount> accountList = new ArrayList<>();
		if (SendWay.SMS.equals(type)) {
			// 获取机构内所有的用户名
			String organizationId = UserUtils.getUser().getOrganizationId();
			List<User> organizationList = userService.findByOrganizationIdAndIdNot(organizationId, loginUser.getId());
			// 为了前段方便 把用户名 账号塞进去
			for (User organization : organizationList) {
				AlertAccount account = new AlertAccount();
				account.setUserAccount(organization.getUserName());
				account.setName(organization.getUserName());
				accountList.add(account);
			}
		} else if (SendWay.WE_CHAT.equals(type)) {
			// 为了前段方便 把用户名 账号塞进去
			accountList = alertAccountService.findByUserAndType(loginUser, type);
			for (AlertAccount account : accountList) {
				if (StringUtils.isNotBlank(account.getDefinedName())) {
					account.setName(account.getDefinedName());
				}
			}
			return accountList;
		} else if (!"ALL".equals(type)) {
			accountList = alertAccountService.findByUserAndType(loginUser, type);
			List<AlertAccount> list = new ArrayList<>(accountList);
			for (AlertAccount account : list) {
				account.setName(account.getAccount());
			}
			return list;
		}
		return accountList;
	}

	// @ApiOperation("信息列表手动预警发送邮件")
	// @RequestMapping(value = "/email", method = RequestMethod.GET)
	// public void email(@ApiParam("用户名") @RequestParam(value = "username",
	// required = false) String username)
	// throws TRSException, SearchException {
	// messagingTemplate.convertAndSendToUser(username, "/topic/greetings",
	// "肖莹");
	//
	// }

	/**
	 * 关闭开启预警(分组）
	 *
	 * @date Created at 2018年3月1日 上午10:45:49
	 * @Author 谷泽昊
	 * @param id
	 * @param status
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("关闭开启预警")
	@Log(systemLogOperation = SystemLogOperation.ONOROFF_ALERT, systemLogType = SystemLogType.ALERT, systemLogOperationPosition = "预警id：${id}")
	@RequestMapping(value = "/onOrOff", method = RequestMethod.GET)
	@FormatResult
	public Object onOrOff(@ApiParam("预警规则id") @RequestParam("id") String id,
			@ApiParam("预警开关 OPEN,CLOSE 默认开启") @RequestParam(value = "status", defaultValue = "OPEN") String status)
			throws OperationException {
		AlertRule alertRule = null;
		try {
			alertRule = alertRuleService.findOne(id);
		} catch (Exception e) {
			throw new OperationException("查询预警失败" + e, e);
		}
		ScheduleStatus statusValue = ScheduleStatus.valueOf(status);
		alertRule.setStatus(statusValue);
		try {
			// 验证方法
			AlertRule alertRuleUpdate = alertRuleService.addAlertRule(alertRule);
			if (alertRuleUpdate != null) {
				fixedThreadPool.execute(() -> this.managementAutoAlertRule(alertRuleUpdate, AlertAutoConst.alertNetInsight_update_status_auto));
				return "修改预警成功！";
			}
		} catch (Exception e) {
			throw new OperationException("修改预警失败,message:" + e, e);
		}
		return null;
	}

	/**
	 * 删除预警规则（分组）
	 *
	 * @param id
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("删除预警规则")
	@Log(systemLogOperation = SystemLogOperation.ALERT_RULE_DELETE, systemLogType = SystemLogType.ALERT, systemLogOperationPosition = "预警id：${id}")
	@FormatResult
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public Object onOrOff(@ApiParam("预警规则id") @RequestParam("id") String id) throws OperationException {
		try {
			AlertRule alertRule = alertRuleService.findOne(id);
			Object result = alertRuleService.selectNextShowAlertRule(id);
			alertRuleService.delete(id);
			fixedThreadPool.execute(() -> this.managementAutoAlertRule(alertRule, AlertAutoConst.alertNetInsight_delete_auto));
			//return "删除预警成功！";
			return result;
		} catch (Exception e) {
			throw new OperationException("删除预警失败,message:" + e, e);
		}
	}

	/**
	 * 站内预警弹窗
	 *
	 * @param time
	 *            规则Id
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws OperationException
	 */
	/*
	@ApiOperation("站内预警弹窗")
	@FormatResult
	@RequestMapping(value = "/listSMS", method = RequestMethod.GET)
	public Object listSMS(@ApiParam("时间间隔") @RequestParam(value = "time", defaultValue = "5n") String time,
			@ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@ApiParam("步长") @RequestParam(value = "pageSize", defaultValue = "5", required = false) int pageSize)
			throws OperationException {

//		Subject currentUser = SecurityUtils.getSubject();
//		currentUser.logout();
		return null;
//		if (httpClient) {
//			return alertRuleService.listSmsHttp(time, pageNo, pageSize);
//		} else {
//			return alertRuleService.listSmsLocal(time, pageNo, pageSize);
//		}
	}
	*/

	/**
	 * 根据规则id查询规则（同样适用于分组）
	 * @param id
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param invitationCard
	 * @param forwarPrimary
	 * @param keywords
	 * @return
	 * @throws OperationException
	 */
	@ApiOperation("根据规则id查询规则")
	@Log(systemLogOperation = SystemLogOperation.ALERT_RULE_LIST_DOCUMENT, systemLogType = SystemLogType.ALERT, systemLogOperationPosition = "预警规则id：${id}")
	@RequestMapping(value = "/listDocument", method = RequestMethod.GET)
	@FormatResult
	public Object listDocument(@ApiParam("预警规则id") @RequestParam("id") String id,
			@ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
			@ApiParam("步长") @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
			@ApiParam("来源") @RequestParam(value = "source", defaultValue = "ALL", required = false) String source,
			@ApiParam("时间") @RequestParam(value = "time", defaultValue = "0d", required = false) String time,
			@ApiParam("内容地域") @RequestParam(value = "area", defaultValue = "ALL", required = false) String area,
			@ApiParam("行业类型") @RequestParam(value = "industry", defaultValue = "ALL", required = false) String industry,
			@ApiParam("情感") @RequestParam(value = "emotion", defaultValue = "ALL", required = false) String emotion,
			@ApiParam(" 排序方式") @RequestParam(value = "sort", defaultValue = "default", required = false) String sort,
			@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value = "invitationCard", required = false) String invitationCard,
			@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
			@ApiParam("是否导出") @RequestParam(value = "isExport", defaultValue = "false") boolean isExport,
			@ApiParam(" 在结果查询") @RequestParam(value = "keywords", required = false) String keywords,
			@ApiParam("结果中搜索的范围")@RequestParam(value = "fuzzyValueScope",defaultValue = "fullText",required = false) String fuzzyValueScope)
			throws OperationException {
		try {
			//防止前端乱输入
			pageSize = pageSize>=1?pageSize:10;
			// 关键词搜索位置
			log.error("进入listDocument方法:" + System.currentTimeMillis());
			String keywordIndex = "positioCon";
			AlertRule alertRule = alertRuleRepository.findOne(id);
			if(alertRule.getSort()!=null){
				if("hittitle".equals(alertRule.getSort())){
					alertRule.setWeight(true);
				}
			}
			SpecialType specialType = alertRule.getSpecialType();
			String groupName = alertRule.getGroupName();
			if(groupName == null || "".equals(groupName)){
				groupName = "ALL";
			}
			//普通搜索分数据源，专家不分
			if(!"ALL".equals(groupName) && !"ALL".equals(source) && SpecialType.COMMON.equals(specialType)){
				String[] groupArr = groupName.split(";");
				List<String> groupList = new ArrayList<>();
				for(String group : groupArr){
					group = Const.SOURCE_GROUPNAME_CONTRAST.get(group);
					groupList.add(group);
				}
				if(!groupList.contains(Const.SOURCE_GROUPNAME_CONTRAST.get(source))){
					return null;
				}
				if (!CommonListChartUtil.changeGroupName(groupName).contains(CommonListChartUtil.changeGroupName(source))){
					return null;
				}
			}
			// 普通模式 都搜索 专家模式 只搜索填写表达式的
			// 选择查询的库
			if (Const.MEDIA_TYPE_WEIXIN.contains(source)
					&& (SpecialType.COMMON.equals(specialType) || StringUtil.isNotEmpty(alertRule.getWeChatTrsl()))) {
				return alertRuleService.weChatSearch(alertRule, pageNo, pageSize, source, time, area, industry, emotion,
						sort, keywords, fuzzyValueScope, keywordIndex);
			} else if (Const.MEDIA_TYPE_WEIBO.contains(source)
					&& (SpecialType.COMMON.equals(specialType) || StringUtil.isNotEmpty(alertRule.getStatusTrsl()))) {
				return alertRuleService.statusSearch(alertRule, pageNo, pageSize, source, time, area, industry, emotion,
						sort, keywords,fuzzyValueScope, null, forwarPrimary);
			} else if ((Const.MEDIA_TYPE_NEWS.contains(source) || Const.GROUPNAME_CHANGSHIPIN.contains(source) || Const.GROUPNAME_DUANSHIPIN.contains(source))
					&& (SpecialType.COMMON.equals(specialType) || StringUtil.isNotEmpty(alertRule.getTrsl()))) {
				log.error("进入" + source + "方法:" + System.currentTimeMillis());
				return alertRuleService.documentSearch(alertRule, pageNo, pageSize, source, time, area, industry,
						emotion, sort, invitationCard, keywords, fuzzyValueScope, keywordIndex);
			} else if (Const.MEDIA_TYPE_TF.contains(source)
					&& (SpecialType.COMMON.equals(specialType) || StringUtil.isNotEmpty(alertRule.getTrsl()))) {
				//TF类型数据是用传统表达式查询的
				log.error("进入" + source + "方法:" + System.currentTimeMillis());
				return alertRuleService.documentTFSearch(alertRule, pageNo, pageSize, source, time, area, industry,
						emotion, sort, keywords, fuzzyValueScope, keywordIndex);
			} else if ("ALL".contains(source)) {
				log.error("进入" + source + "方法:" + System.currentTimeMillis());
				return alertRuleService.documentCommonSearch(alertRule, pageNo, pageSize, source, time, area, industry,
						emotion, sort, invitationCard,forwarPrimary, keywords, fuzzyValueScope,keywordIndex,isExport);
			}
		} catch (Exception e) {
			throw new OperationException("查询规则信息失败,message:" + e, e);
		}
		return null;
	}


	/**
	 * 判断预警发送时的 接收人与接收方式 是否一一对应
	 * @param receivers 接收人
	 * @param sendWay   接收方式
	 * @throws OperationException
	 */
	private void checkParams(String receivers,String sendWay) throws OperationException{
		if (StringUtil.isNotEmpty(sendWay) || StringUtil.isNotEmpty(receivers)){
			String[] split = sendWay.split(";|；");
			String[] split1 = receivers.split(";|；");
			if (split.length != split1.length){
				throw new OperationException("接收人与接收方式不对应！");
			}
		}
	}

	/**
	 * 修改历史数据
	 * @param request
	 * @param response
	 */
	@ApiOperation("修改历史数据 预警规则与预警备份关键词")
	@PostMapping(value = "/changAlertRuleAndBackupsForWordSpacing")
	public void changHistoryDataForWordSpacing(javax.servlet.http.HttpServletRequest request, HttpServletResponse response) {
		//修改普通模式预警规则和预警备份表
		alertRuleService.updateSimple();

	}
}
