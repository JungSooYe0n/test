package com.trs.netInsight.widget.home.controller;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.trs.netInsight.widget.apply.entity.Apply;
import com.trs.netInsight.widget.apply.entity.enums.AccountType;
import com.trs.netInsight.widget.apply.entity.enums.ApplyUserType;
import com.trs.netInsight.widget.apply.service.IApplyService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.EnableRedis;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RegexUtils;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.analysis.entity.ClassInfo;
import com.trs.netInsight.widget.column.entity.Columns;
import com.trs.netInsight.widget.column.service.IColumnService;
import com.trs.netInsight.widget.home.entity.EHtml;
import com.trs.netInsight.widget.home.entity.EHtmlList;
import com.trs.netInsight.widget.home.entity.enums.ColumnType;
import com.trs.netInsight.widget.home.entity.enums.TabType;
import com.trs.netInsight.widget.home.service.IHomeService;
import com.trs.netInsight.widget.notice.service.IMailSendService;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 首页Controller
 *
 * Created by Xiaoying on 2017/12/05.
 */
@RestController
@RequestMapping("/home")
@Api(description = "首页接口")
public class HomeController {

	@Autowired
	private IColumnService columnService;

	@Autowired
	private ISpecialProjectService specialProjectService;

	@Autowired
	private IAlertService alertService;

	@Autowired
	private IHomeService homeService;

	@Autowired
	private RestTemplate initRestTemplate;

	@Autowired
	private IMailSendService mailSendService;
	@Autowired
	private IApplyService applyService;

	/**
	 * 获取用户首页配置的栏目信息
	 *
	 * @param request
	 *            HttpServletRequest
	 * @return getColumns
	 */

	// @EnableRedis(cacheMinutes=60)
	@FormatResult
	@ApiOperation("获取用户首页配置的所有栏目")
	@RequestMapping(value = "/columns", method = RequestMethod.GET)
	public Object getColumns(HttpServletRequest request) throws OperationException {
		try {
			String userId = UserUtils.getUser().getId();
			List<Columns> columns = new ArrayList<>();
			List<SpecialProject> specials = specialProjectService.findByUserId(userId, new PageRequest(0, 1));
			List<AlertEntity> alerts = alertService.findByUserId(userId,
					new Sort(Sort.Direction.DESC, "lastModifiedTime"));
			int i = 0;
			for (AlertEntity alert : alerts) {
				String title = alert.getTitle();
				if (StringUtil.isEmpty(title)) {
					i++;
				}
			}
			if (ObjectUtil.isNotEmpty(specials) && i > 0) {
				columns.addAll(Arrays.asList(ColumnConst.DEFAULT_COLUMNS));
			}
			if (ObjectUtil.isEmpty(specials) && i > 0) {
				columns.addAll(Arrays.asList(ColumnConst.NO_SPECIAL_COLUMNS));
			}
			if (ObjectUtil.isNotEmpty(specials) && i == 0) {
				columns.addAll(Arrays.asList(ColumnConst.NO_ALERT_COLUMNS));
			}
			if (ObjectUtil.isEmpty(specials) && i == 0) {
				columns.addAll(Arrays.asList(ColumnConst.BLANK_COLUMNS));
			}
			columns.addAll(columnService.findByOrganizationId(new Sort(Sort.Direction.ASC, "lastModifiedTime")));

			return ObjectUtil.writeWithView(columns, Columns.DisplayView.class);
		} catch (Exception e) {
			throw new OperationException("获取用户栏目出错：" + e);
		}
	}

	/**
	 * 添加可配置栏目
	 * 
	 * @date Created at 2017年12月4日 下午7:55:32
	 * @Author 谷泽昊
	 * @param column
	 * @return
	 * @throws OperationException
	 */
	// @EnableRedis(cacheMinutes=60)
	@FormatResult
	@ApiOperation("添加可配置栏目")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Object addColumn(@RequestBody Columns column) throws OperationException {
		try {
			column.setId(GUIDGenerator.generate(HomeController.class));
			columnService.save(column);
			return "success";
		} catch (Exception e) {
			throw new OperationException("添加可配置栏目出错：" + e);
		}

	}

	/**
	 * 跟上边那个接口一样
	 * 
	 * @param columnName
	 * @param type
	 * @param tabKeywords
	 * @param tabType
	 * @param position
	 * @param keywords
	 * @return
	 * @throws TRSException
	 */
	// @EnableRedis(cacheMinutes=60)
	@FormatResult
	@ApiOperation("添加可配置栏目2")
	@RequestMapping(value = "/add2", method = RequestMethod.POST)
	public Object add(@ApiParam("栏目名称") @RequestParam("column_name") String columnName,
			@ApiParam("栏目类型") @RequestParam("type") ColumnType type,
			@ApiParam("页签关键词") @RequestParam("tab_keywords") String tabKeywords,
			@ApiParam("页签类型") @RequestParam("tab_type") TabType tabType,
			@ApiParam("在本页中位置") @RequestParam("position") int position,
			@ApiParam("检索关键字") @RequestParam("keywords") String keywords) throws TRSException {
		return homeService.save(columnName, type, tabKeywords, tabType, position, keywords);
	}

	/**
	 * 首页专项信息趋势接口
	 * 
	 * @date Created at 2017年12月4日 下午7:55:56
	 * @Author 谷泽昊
	 * @param request
	 * @return
	 * @throws TRSException
	 * @throws
	 */
	// @EnableRedis(cacheMinutes=60, poolId = "userId")
	@FormatResult
	@ApiOperation("首页专项信息趋势接口")
	@RequestMapping(value = "/specialTrend", method = RequestMethod.GET)
	public Object specialTrend(HttpServletRequest request) throws TRSException, TRSSearchException {
		String userId = UserUtils.getUser().getId();
		// 只要前五条
		List<SpecialProject> findByUserId = specialProjectService.findByUserId(userId,
				new PageRequest(0, 5, new Sort(Sort.Direction.DESC, "createdTime")));
		ObjectUtil.assertNull(findByUserId, "首页专项检测为空");
		return homeService.trend(findByUserId);
	}

	/**
	 * 按照地域/关键词 时间 查询 写一个统一查询接口
	 * 
	 * @return
	 * @throws TRSException
	 *             Created by Xiaoying on 2017/12/05.
	 */

	// @EnableRedis
	@FormatResult
	@ApiOperation("首页地域热力图")
	@RequestMapping(value = "/region", method = RequestMethod.GET)
	public Object areaCount(@ApiParam("来源") @RequestParam(value = "group_name", defaultValue = "国内新闻") String grouName,
			@ApiParam("时间") @RequestParam(value = "timeRange", defaultValue = "0d") String timeRange)
			throws TRSException {
		QueryBuilder queryBuilder = new QueryBuilder();
		String[] timeArray = DateUtil.formatTimeRange(timeRange);
		// 境外媒体 先只查国外新闻
		if ("境外媒体".equals(grouName)) {
			grouName = "国外新闻";
		}
		return homeService.areaCount(grouName, queryBuilder, timeArray);
	}

	/**
	 * 百度排行榜 http://top.baidu.com/clip?b=5 b=?
	 * 参考：http://top.baidu.com/add?fr=topbuzz_b258 时事热点
	 * http://top.baidu.com/buzz?b=1 http://top.baidu.com/clip?b=342 看ip是否解封
	 */
	@EnableRedis(cacheMinutes = 60)
	@FormatResult
	@ApiOperation("首页热搜")
	@RequestMapping(value = "/hs", method = RequestMethod.GET)
	public Object hotSearch(@ApiParam("关键词[keywords],人物[person]") @RequestParam("type") String type,
			@ApiParam("返回个数") @RequestParam(value = "limit", defaultValue = "10") int limit) throws TRSException {

		String url = String.format("http://top.baidu.com/clip?b=%s", "keywords".equals(type) ? 342 : 257);
		// 后边跟的数字返回的是搜索指数 可能要根据搜索指数排序
		// String url = String.format("http://top.baidu.com/clip?b=%s",
		// "keywords".equals(type) ? 1 : 258);

		String html = initRestTemplate.getForObject(url, String.class);
		if (StringUtil.isNotEmpty(html)) {
			Pattern regex = Pattern.compile("((\\[\\{\"title).*?(}]))");
			Matcher matcher = regex.matcher(html);
			String result = "";
			while (matcher.find()) {
				result = matcher.group();
			}

			EHtmlList list = ObjectUtil.toObject(result, EHtmlList.class);
			if (list != null) {
				limit = list.size() > limit ? limit : list.size();
				return ObjectUtil.writeWithView(list.subList(0, limit), EHtml.Show.class);
			}
		}
		return null;
	}

	/**
	 * 新闻榜单
	 * 
	 * @date Created at 2017年12月4日 下午7:56:25
	 * @Author 谷泽昊
	 * @param site
	 * @return
	 * @throws TRSException
	 */
	@EnableRedis(cacheMinutes = 30)
	@FormatResult
	@ApiOperation("新闻榜单")
	@RequestMapping(value = "/focus", method = RequestMethod.GET)
	public Object newsFocus(@ApiParam("网站名 如果是全部就不用传") @RequestParam(value = "site", required = false) String site)
			throws TRSException {

		String format = "yyyyMMddHHmmss";
		String trsl = "(IR_SITENAME:(\"新华网\" OR \"中国网\" OR \"央视网\"  OR \"中国新闻网\" OR  \"新浪网\" OR  \"网易\" OR \"搜狐网\" OR  \"凤凰网\") "
				+ "NOT IR_CHANNEL:(游戏)" + "AND IR_GROUPNAME:国内新闻 )NOT IR_URLTITLE:(吴君如 OR 汽车 OR 新车 OR 优惠)";
		// String trsl = "(IR_SITENAME:(\"新华网\" OR \"中国网\" OR \"央视网\" OR
		// \"中国新闻网\" OR \"新浪网\" OR \"网易\" OR \"搜狐网\" OR \"凤凰网\") "
		// + "AND IR_CHANNEL:(要闻 OR 即时* OR 国内* OR 国际* OR 互联网* OR 资讯首页_要闻区 OR
		// 新闻中心_新闻排行榜 OR 头条新闻 OR 首页_要闻区 OR 门户_首页 OR 新闻中心_首页 OR 首页 OR 国内新闻_首页要闻
		// OR 门户首页_首页 OR 资讯排行 OR 新闻首页 OR 要闻) "
		// + "AND IR_GROUPNAME:国内新闻 )NOT IR_URLTITLE:(吴君如 OR 汽车 OR 新车 OR 优惠)";
		SimpleDateFormat sdf1 = new SimpleDateFormat(format);
		Date date = DateUtil.getDate(-10);
		String str1 = sdf1.format(date);
		trsl += String.format(" AND IR_URLTIME:[%s TO %s]", str1, DateUtil.formatCurrentTime(format));
		return homeService.newsFocus(10, trsl);
	}

	@EnableRedis(cacheMinutes = 3)
	@FormatResult
	@ApiOperation("分类信息列表")
	@RequestMapping(value = "/classify", method = RequestMethod.GET)
	public Object classify(@ApiParam("栏目id") @RequestParam("column_id") String columnId) throws TRSException {

		return null;
	}

	/**
	 * 热点信息列表
	 * 
	 * @date Created at 2017年12月4日 下午7:56:37
	 * @Author 谷泽昊
	 * @param columnId
	 * @param limit
	 * @return
	 * @throws TRSException
	 */
	@EnableRedis(cacheMinutes = 3)
	@FormatResult
	@ApiOperation("热点信息列表")
	@RequestMapping(value = "/hot_info", method = RequestMethod.GET)
	public Object hotInfo(@ApiParam("栏目id") @RequestParam("column_id") String columnId,
			@ApiParam("返回个数") @RequestParam(value = "limit", defaultValue = "20") int limit) throws TRSException {
		return homeService.hotInfo(columnId, limit);
	}

	/**
	 * 微博热点列表 和 微博热点信息
	 * 
	 * @date Created at 2017年12月4日 下午7:56:44
	 * @Author 谷泽昊
	 * @param columnId
	 * @return
	 * @throws TRSException
	 * @throws
	 */
	@EnableRedis(cacheMinutes = 3)
	@FormatResult
	@ApiOperation("微博热点列表 和 微博热点信息")
	@RequestMapping(value = "/weibo_hot", method = RequestMethod.GET)
	public Object weibo(@ApiParam("栏目id") @RequestParam("column_id") String columnId)
			throws TRSException, TRSSearchException {
		try {
			// 头像没查
			return homeService.weibo(columnId);
		} catch (com.trs.dc.entity.TRSException e) {
			e.printStackTrace();
			throw new OperationException("微博热点列表查找失败");
		}
	}

	/**
	 * 首页全搜索 解析 标题 OR 正文 24h内
	 * 
	 * @param keyWords
	 * @return
	 */
	// @EnableRedis
	@FormatResult
	@ApiOperation("首页 全网搜索解析")
	@RequestMapping(value = "/netSearch", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "keyWords", value = "检索的关键词", dataType = "String", paramType = "query") })
	@ResponseBody
	public Object netSearch(@RequestParam(value = "keyWords") String keyWords) throws TRSException, TRSSearchException {
		List<ClassInfo> classInfos = homeService.netAnalysis(keyWords);
		return classInfos;
	}

	/**
	 * 首页申请使用
	 * 
	 * @date Created at 2018年8月3日 下午3:52:11
	 * @Author 谷泽昊
	 * @param
	 * @param applyTryName
	 * @param applyTryPhone
	 * @param applyTryTel
	 * @param applyTryEmail
	 * @return
	 * @throws Exception
	 */
	@FormatResult
	@ApiOperation("首页 申请试用")
	@RequestMapping(value = "/applyForTry", method = RequestMethod.POST)
	public Object applyForTry(
			@ApiParam("政企申请人公司姓名") @RequestParam(value = "applyTryCompanyName") String applyTryCompanyName,
			@ApiParam("政企申请人姓名") @RequestParam(value = "applyTryName") String applyTryName,
			@ApiParam("政企申请人联系电话") @RequestParam(value = "applyTryPhone", required = false) String applyTryPhone,
			@ApiParam("政企申请人联系方式") @RequestParam(value = "applyTryTel") String applyTryTel,
			@ApiParam("政企申请人邮件地址") @RequestParam(value = "applyTryEmail") String applyTryEmail) throws Exception {
		// 判断邮件是否正确
		if (!RegexUtils.checkEmail(applyTryEmail)) {
			throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
		}
		// 验证手机号
		if (StringUtils.isNotBlank(applyTryTel) && !RegexUtils.checkMobile(applyTryTel)) {
			throw new TRSException(CodeUtils.PHONE_FAIL, "手机号填写错误！");
		}

		String text = applyTryName + "客户申请网察免费试用。<br> 公司名称：" + applyTryCompanyName + "。<br> 联系电话：" + applyTryPhone
				+ "。<br> 手机号码：" + applyTryTel + "。<br> 邮件：" + applyTryEmail + "。<br>申请时间："
				+ DateUtil.formatDateAfter(new Date(), DateUtil.FMT_TRS_yMdhms, 0) + "。";
		String subject = "政企用户("+applyTryName+")申请试用";
		boolean b = mailSendService.sendOneMail(subject, text);
		return b;
	}


	/**
	 * 广告页申请使用
	 *
	 *
	 */
	@FormatResult
	@ApiOperation("4.0广告宣传 申请试用")
	@RequestMapping(value = "/applyForAdvertising", method = RequestMethod.POST)
	public Object applyForAdvertising(
			@ApiParam("申请用户类型，老用户、新用户") @RequestParam(value = "applyUserType") String applyUserType,

			@ApiParam("单位名称") @RequestParam(value = "unitName",required = false) String unitName,
			@ApiParam("姓名") @RequestParam(value = "name",required = false) String name,
			@ApiParam("手机号") @RequestParam(value = "phone",required = false) String phone,
			@ApiParam("工作电话") @RequestParam(value = "workPhone",required = false) String workPhone,
			@ApiParam("工作邮箱") @RequestParam(value = "email") String email,
			@ApiParam("来源渠道") @RequestParam(value = "sourceWay",required = false) String sourceWay,
			@ApiParam("原有账号") @RequestParam(value = "originalAccount",required = false) String originalAccount,
			@ApiParam("用户类型：正式formal，试用trial") @RequestParam(value = "accountType",required = false) String accountType

	) throws Exception {
		// 判断邮件是否正确
		if (!RegexUtils.checkEmail(email)) {
			throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
		}
		// 验证手机号
		if (StringUtils.isNotBlank(phone) && !RegexUtils.checkMobile(phone)) {
			throw new TRSException(CodeUtils.PHONE_FAIL, "手机号填写错误！");
		}
		String text = "";
		String subject = "";
		ApplyUserType userType = ApplyUserType.valueOf(applyUserType);
		if(ApplyUserType.newUser.equals(userType)){

			if(StringUtil.isEmpty(unitName)){
				throw new TRSException(CodeUtils.FAIL, "新用户申请单位名称不能为空");
			}
			if(StringUtil.isEmpty(name)){
				throw new TRSException(CodeUtils.FAIL, "新用户申请姓名不能为空");
			}
			if(StringUtil.isEmpty(phone)){
				throw new TRSException(CodeUtils.FAIL, "新用户申请手机号码不能为空");
			}
			if(StringUtil.isEmpty(sourceWay)){
				throw new TRSException(CodeUtils.FAIL, "新用户申请来源渠道不能为空");
			}

			text = MessageFormat.format(newEmailTemplate, unitName,name,phone,workPhone,email,sourceWay);
			subject = MessageFormat.format(newEmailTitle, name);
		}else if(ApplyUserType.oldUser.equals(userType)){
			if(StringUtil.isEmpty(originalAccount)){
				throw new TRSException(CodeUtils.FAIL, "老用户申请原有账号不能为为空");
			}
			if(StringUtil.isEmpty(accountType)){
				throw new TRSException(CodeUtils.FAIL, "老用户申请用户类型不能为空");
			}
			AccountType accountType1=AccountType.valueOf(accountType);

			text = MessageFormat.format(oldEmailTemplate, originalAccount,name,phone,workPhone,email,accountType1.getName());
			subject = MessageFormat.format(oldEmailTitle, name);
		}else{
			throw new TRSException(CodeUtils.FAIL, "未能识别用户类型");
		}
		Apply apply = new Apply(applyUserType,unitName,name,phone,workPhone,email,sourceWay,originalAccount,accountType);
		applyService.updateApply(apply);
		boolean b = mailSendService.sendOneMail(subject, text);
		return b;
	}
	public static final String newEmailTitle = "新用户{0}申请网察免费试用";
	public static final String newEmailTemplate = "单位名称：{0}<br>" +
			"姓名：{1}<br>" +
			"手机号码：{2}<br>" +
			"工作电话：{3}<br>" +
			"工作邮箱：{4}<br>" +
			"来源渠道：{5}<br>";

	public static final String oldEmailTitle = "老用户{0}申请网察免费试用";
	public static final String oldEmailTemplate = "原有账号：{0}<br>" +
			"姓名：{1}<br>" +
			"手机号码：{2}<br>" +
			"工作电话：{3}<br>" +
			"工作邮箱：{4}<br>" +
			"用户类型：{5}<br>";

}
