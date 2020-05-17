package com.trs.netInsight.widget.login.controller;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.LoginException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.login.service.ILoginService;
import com.trs.netInsight.widget.user.entity.*;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

import static com.trs.netInsight.util.LoginUtil.rangeExpiret;

/**
 * 登录controller
 *
 * @Type LoginController.java
 * @Desc
 * @author 谷泽昊
 * @date 2017年12月7日 上午10:17:53
 * @version
 */
@Slf4j
@Controller
@Api(description = "登录接口")
public class LoginController {

	@Autowired
	private ILoginService loginService;

	@Value("${form.login.success.url}")
	private String loginSuccessUrl;

	@Autowired
	private IUserService userService;

	@Autowired
	private IRoleService roleService;
	@Autowired
	private IOrganizationService organizationService;

	@Autowired
	private ISubGroupService subGroupService;

	@Autowired
	private ILoginPageConfigService loginPageConfigService;

	@Value("${system.authority.login.orgId}")
	private String authorityOrgId;

	/**
	 * 登录
	 *
	 * @date Created at 2017年11月17日 下午6:58:50
	 * @Author 谷泽昊
	 * @param userName
	 *            账号
	 * @param password
	 *            密码
	 * @param rememberMe
	 *            记住我 验证码
	 * @return
	 * @throws TRSException
	 * @throws TRSException
	 */
	@ApiOperation("登录")
	@FormatResult
	@ResponseBody
	@Log(systemLogOperation = SystemLogOperation.USERNAME_LOGIN, systemLogType = SystemLogType.LOGIN, methodDescription = "登录账号为：${userName}", systemLogOperationPosition = "登录账号为：@{userName}")
	@RequestMapping(value = "/login", method = {RequestMethod.POST})
	public Object login(@ApiParam("账号") @RequestParam(value = "userName") String userName,
						@ApiParam("密码") @RequestParam(value = "password") String password,
						@ApiParam("记住我") @RequestParam(value = "rememberMe", required = false) String rememberMe,
						@ApiParam("机构链接后缀") @RequestParam(value = "suffix",required = false) String suffix,
						// @ApiParam("验证码") @RequestParam(value = "code") String code,
						HttpServletRequest request, HttpServletResponse response) throws TRSException {
		// String v = (String) session.getAttribute(CodeUtils.CODE_SESSION_KEY);
		// log.error("sessionID:" + session.getId());
		// log.error("login--session 验证码" + v);
		// log.error("login--输入 验证码" + code);
		// 读取一次后把验证码清空，这样每次登录都必须获取验证码
		// session.removeAttribute(CodeUtils.CODE_SESSION_KEY);
		// 判断验证码
		// if (!StringUtils.equalsIgnoreCase(code, v)) {
		// throw new TRSException(CodeUtils.VCODE_FAIL, "验证码错误!");
		// }
		User byUserName = userService.findByUserName(userName);
		if(userService.findByUserName(userName) != null){
			User user = userService.findByUserName(userName);
			//登录页无后缀 用户对应机构有后缀
			if(user.getOrganizationId() == null){
				if(suffix != null && !"".equals(suffix)){
					throw new TRSException(CodeUtils.FAIL, "该网页链接与登录账号不匹配，请联系运维人员进行核查。");
				}
			}else{
				if((suffix == null || "".equals(suffix)) && loginPageConfigService.findByOrgId(user.getOrganizationId()) != null){
					log.error("对用户[" + userName + "]进行登录验证..登录页与机构不匹配");
					throw new TRSException(CodeUtils.FAIL, "该网页链接与登录账号不匹配，请联系运维人员进行核查。");
				}
				if((suffix != null && !"".equals(suffix)) && loginPageConfigService.findByOrgId(user.getOrganizationId()) == null){
					log.error("对用户[" + userName + "]进行登录验证..登录页与机构不匹配");
					throw new TRSException(CodeUtils.FAIL, "该网页链接与登录账号不匹配，请联系运维人员进行核查。");
				}
				if((suffix != null && !"".equals(suffix)) && loginPageConfigService.findByOrgId(user.getOrganizationId()) != null
						&& !suffix.equals(loginPageConfigService.findByOrgId(user.getOrganizationId()).getSuffix())){
					log.error("对用户[" + userName + "]进行登录验证..登录页与机构不匹配");
					throw new TRSException(CodeUtils.FAIL, "该网页链接与登录账号不匹配，请联系运维人员进行核查。");
				}
			}
		}
		UsernamePasswordToken token = new UsernamePasswordToken(userName, password, rememberMe);
		return loginService.login(token, userName, NetworkUtil.getIpAddress(request));
	}



	/**
	 * 获取验证码图片
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ApiOperation("获取验证码")
	@RequestMapping(value = "/getKaptchaImage", method = RequestMethod.GET)
	public void getKaptchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession session = request.getSession();
		response.setDateHeader("Expires", 0);

		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");

		VerifyUtil vCode = new VerifyUtil(150, 35, 4, 10);
		log.error(vCode.getCode());
		// 将验证码存入Session
		session.setAttribute(CodeUtils.CODE_SESSION_KEY, vCode.getCode());
		// 将图片输出给浏览器
		BufferedImage image = vCode.getBuffImg();
		OutputStream os = response.getOutputStream();

		// write the data out
		ImageIO.write(image, "jpg", os);
		try {
			image.flush();
		} finally {
			os.close();
		}
	}

	/**
	 * 通过后缀获取登录页面配置信息
	 *
	 * @param suffix
	 * @return
	 * @throws Exception
	 */
	@FormatResult
	@ResponseBody
	@ApiOperation("通过后缀获取登录页面配置信息")
	@RequestMapping(value = "/loginSuffix", method = RequestMethod.POST)
	public Object loginSuffix(@ApiParam("后缀") @RequestParam(value = "suffix") String suffix) throws Exception {
		LoginPageConfig loginPageConfig = null;
		if(suffix!= null && !"".equals(suffix)){
			loginPageConfig = loginPageConfigService.findBySuffix(suffix);
			if(loginPageConfig != null ){
				if (loginPageConfig.getLogoPicName() == null || "null".equals(loginPageConfig.getLogoPicName())
						||"".equals(loginPageConfig.getLogoPicName())) {
					loginPageConfig.setLogoPicName("无logo");
				}
				if (loginPageConfig.getQRCodeName() == null || "null".equals(loginPageConfig.getQRCodeName())
						|| "".equals(loginPageConfig.getQRCodeName())) {
					loginPageConfig.setQRCodeName("无logo");
				}
			}
		}
		return loginPageConfig;
	}

	/**
	 * 没有权限
	 *
	 * @date Created at 2017年11月17日 下午7:36:36
	 * @Author 谷泽昊
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@ResponseBody
	@RequestMapping(value = "/forbidden", method = RequestMethod.GET)
	public Object forbidden() throws TRSException {
		throw new LoginException(CodeUtils.FORBIDDEN_FAIL, "没有权限！");
	}

	/**
	 *
	 * @return
	 */
	@FormatResult
	@ResponseBody
	@RequestMapping(value = "/error", method = RequestMethod.GET)
	public Object error() {
		return "error";
	}

	/**
	 * 退出
	 *
	 * @date Created at 2017年11月17日 下午7:31:17
	 * @Author 谷泽昊
	 * @return
	 */
	@FormatResult
	@ApiOperation("退出")
	@ResponseBody
	@Log(systemLogOperation = SystemLogOperation.LOGOUT, systemLogType = SystemLogType.LOGIN, systemLogOperationPosition = "登出")
	@RequestMapping(value = "/loginout", method = RequestMethod.GET)
	public Object loginout() {
		Subject currentUser = SecurityUtils.getSubject();
		// 删除微信登录信息
		String username = null;
		LoginPageConfig loginPageConfig = null;
		try {
			User user = (User) currentUser.getPrincipal();
			username = user.getUserName();
			String orgId = user.getOrganizationId();
			loginPageConfig = loginPageConfigService.findByOrgId(orgId);
			RedisUtil.deleteString(username + String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN));
		} catch (Exception e) {
		}
		currentUser.logout();
		Map<String,Object> map = new HashMap<>();
		map.put("massage","退出成功");
		map.put("data",loginPageConfig);
		return map;
	}

	/**
	 * 强制下线
	 *
	 * @date Created at 2017年11月17日 下午7:32:56
	 * @Author 谷泽昊
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@ResponseBody
	@Log(systemLogOperation = SystemLogOperation.KICKOUT, systemLogType = SystemLogType.USER, systemLogOperationPosition = "强制下线账号为：@{userName}")
	@RequestMapping(value = "/kickout", method = RequestMethod.GET)
	public Object kickout(@RequestParam(value = "userName") String userName) throws TRSException {
		throw new TRSException(CodeUtils.COMPULSORY_OFFLINE, "[" + userName + "]已被强制下线！");
	}

	/**
	 * 判断是否登录
	 *
	 * @date Created at 2017年11月17日 下午7:32:45
	 * @Author 谷泽昊
	 * @return
	 * @throws TRSException
	 * @throws TRSException
	 */
	@FormatResult
	@ApiOperation("判断是否登录")
	@ResponseBody
	@RequestMapping(value = "/isLogin", method = RequestMethod.GET)
	public Object isLogin(HttpServletRequest request) throws TRSException, TRSException {
		//普通登录
		User user = null;
		Subject currentUser = SecurityUtils.getSubject();
		if (currentUser.isAuthenticated()) {
			Object principal = currentUser.getPrincipal();
			if (principal instanceof User) {
				user = (User) principal;
			}
		}
		if (ObjectUtil.isNotEmpty(user)){
			//预警弹框是否提醒   同登录信息一起存在shior中
			Boolean isAlert = user.getIsAlert();
			user = userService.findByUserName(user.getUserName());
			if (StringUtils.equals(UserUtils.SUPER_ADMIN, user.getCheckRole())
					|| StringUtils.equals(UserUtils.ROLE_ADMIN, user.getCheckRole())
					|| StringUtils.equals(UserUtils.ROLE_PLATFORM, user.getCheckRole())) {
				List<Role> list = roleService.findByRoleTypeAndDes(CheckRole.ROLE_ADMIN,"日常监测、专题分析、预警中心");

				user.setRoles(new HashSet<>(list));
			}
			user.setIsAlert(isAlert);
			// 到期提醒
			rangeExpiret(user);
			//一些权限信息
			user = UserUtils.checkOrganization(user);
			String dataSource = user.getDataSources();
			if(StringUtil.isNotEmpty(dataSource) && !"ALL".equals(dataSource)){
				StringBuffer dataSourceNew = new StringBuffer();
				String source = CommonListChartUtil.formatPageShowGroupName(dataSource);
				if(source != null){


					List<String> dataSourceList = Arrays.asList(source.split(","));
					for(String pageShow: Const.PAGE_SHOW_DATASOURCE_SORT){
						if(dataSourceList.contains(pageShow)){
							if(dataSourceNew.length() == 0){
								dataSourceNew.append(source);
							}else{
								dataSourceNew.append(",").append(source);
							}
						}
					}
				}
				user.setDataSources(dataSourceNew.toString());
			}
			return user;
		}
		throw new LoginException(CodeUtils.NO_LOGIN, NetworkUtil.getIpAddress(request) + "没有登录！");
	}

	/**
	 * form 表单登录时接口
	 *
	 * @date Created at 2018年7月2日 下午3:49:18
	 * @Author 谷泽昊
	 * @param modelMap
	 * @param userName
	 * @param password
	 * @param rememberMe
	 * @param request
	 * @param response
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.FORM_LOGIN, systemLogType = SystemLogType.LOGIN, methodDescription = "登录账号为：${userName}", systemLogOperationPosition = "登录账号为：@{userName}")
	@RequestMapping(value = "/doLogin", method = RequestMethod.POST)
	public String doLogin(ModelMap modelMap, @RequestParam(value = "userName") String userName,
						  @RequestParam(value = "password") String password,
						  @RequestParam(value = "rememberMe", required = false, defaultValue = "false") boolean rememberMe,
						  HttpServletRequest request, HttpServletResponse response) {
		UsernamePasswordToken token = new UsernamePasswordToken(userName, password, rememberMe);
		try {
			loginService.login(token, userName, NetworkUtil.getIpAddress(request));
			User user = userService.findByUserName(userName);
			user = UserUtils.checkOrganization(user);
			if (user != null
					&& (!authorityOrgId.contains(user.getOrganizationId()) || user.getOrganizationId().equals("0"))) {
				Subject currentUser = SecurityUtils.getSubject();
				currentUser.logout();
				modelMap.addAttribute("message", "Lack Of Authority, Forbidden!");
				return "login";
			}
			return "redirect:" + loginSuccessUrl;
		} catch (Exception e) {
			log.error("登录失败：", e);
			modelMap.addAttribute("message", e.getMessage());
			return "login";
		}
	}

	/**
	 * 获取模拟登录的token
	 *
	 * @date Created at 2018年12月10日 下午2:32:58
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param loginId
	 *            当前登录的用户id
	 * @param organizationId
	 *            需要模拟登录用户的机构id
	 * @param userId
	 *            需要模拟登录的用户id
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("获取模拟登录的token")
	@FormatResult
	@ResponseBody
	@Log(systemLogOperation = SystemLogOperation.SIMULATEDLOGINTOKEN, systemLogType = SystemLogType.LOGIN, systemLogOperationPosition = "登录账号Id为：@{userId}")
	@RequestMapping(value = "/getSimulatedLoginToken", method = RequestMethod.POST)
	public Object getSimulatedLoginToken(HttpServletRequest request,
										 @ApiParam("当前登录用户id") @RequestParam(value = "loginId") String loginId,
										 @ApiParam("需要模拟登录的机构id") @RequestParam(value = "organizationId") String organizationId,
										 @ApiParam("需要模拟登录的用户分组id") @RequestParam(value = "userGroupId",required = false) String userGroupId,
										 @ApiParam("需要模拟登录的用户id") @RequestParam(value = "userId") String userId) throws TRSException {
		User user = UserUtils.getUser();
		if (StringUtils.isBlank(loginId) || StringUtils.isBlank(user.getId())
				|| !StringUtils.equals(loginId, user.getId())) {
			throw new TRSException(CodeUtils.FAIL, "用户不匹配！");
		}

		// 判断账号权限
		if (!UserUtils.ROLE_LIST.contains(user.getCheckRole())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户没有权限！");
		}
		// 如果是运维，判断这个机构是否为运维所管理的
		if (UserUtils.isRolePlatform() && !userService.isPlatformHoldOrganization(loginId, organizationId)) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户没有权限！");
		}

		//如果是管理员，判断机构id是否一致
		if(UserUtils.isRoleAdmin() && !user.getOrganizationId().equals(organizationId)){
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户没有权限！");
		}

		// 判断这个用户是不是这个机构的
		if (!organizationService.isOrganizationExistUser(organizationId, userId)) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户权限不正确！");
		}

		if (UserUtils.isRoleOrdinary(user) && (StringUtil.isEmpty(userGroupId) || subGroupService.isSubGroupExistUser(userGroupId,userId))){
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户权限不正确！");
		}
		Organization simulatedLoginOrganization = organizationService.findById(organizationId);
		SubGroup subGroup = subGroupService.findOne(userGroupId);
		User simulatedLoginUser = userService.findById(userId);
		//来源，个数等一些限制
		simulatedLoginUser = UserUtils.checkOrganization(simulatedLoginUser);
		Map<String,Object> map =new HashMap<>();
		if (ObjectUtil.isNotEmpty(simulatedLoginOrganization)){
			map.put("organizationName", simulatedLoginOrganization.getOrganizationName());
			map.put("organizationId", simulatedLoginOrganization.getId());
		}
		if (UserUtils.isRoleOrdinary(user)){
			map.put("systemName", subGroup.getName());
		}else {
			map.put("systemName", simulatedLoginOrganization.getOrganizationName());
		}
		//	map.put("systemName", simulatedLoginOrganization.getSystemName());
		map.put("user", simulatedLoginUser);
//		map.put("userName", simulatedLoginUser.getUserName());
//		map.put("userId", simulatedLoginUser.getId());
//		map.put("userCheckRole", simulatedLoginUser.getCheckRole());
		map.put("userLimit", String.valueOf(simulatedLoginOrganization.getUserLimit()));
		map.put("token", loginService.getSimulatedLoginToken(loginId, organizationId,userGroupId, userId));
		HttpSession session = request.getSession();
		session.removeAttribute(UserUtils.SIMULATED_LOGIN_USER+loginId+userId);
		return map;
	}

	public static void main(String[] args) {

		int x=1023;//从最后一天开始倒过来算
		for(int i=1;i<10;i++){//第10天不算，共9天
			//x=(x+1)*2;
			x=x/2+1;
		}
		System.out.println(x);


	}
	/**
	 * 获取 logo 图标（登录页）
	 *
	 * @param
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("获取登录页配置的logo图标或者二维码")
	@GetMapping(value = "/getLoginPagePic")
	public void getLoginPagePic(@ApiParam("机构登录链接的后缀") @RequestParam(value = "suffix") String suffix,
								  @ApiParam("要获取图片的类型") @RequestParam(value = "type") String type,
								  HttpServletRequest request, HttpServletResponse response) {
		String logoPicName = null;
		if("logo".equals(type)){
			logoPicName = "wangcha.png";
		}else if("QRCode".equals(type)){
			logoPicName = "WX_gzh.png";
		}
		if(suffix != null && !"".equals(suffix)){
			LoginPageConfig loginPageConfig = loginPageConfigService.findBySuffix(suffix);
			if(loginPageConfig != null){
				String logoPic = null;
				if("logo".equals(type)){
					logoPic = loginPageConfig.getLogoPicName();
				}else if("QRCode".equals(type)){
					logoPic = loginPageConfig.getQRCodeName();
				}
				if (StringUtils.isNotBlank(logoPic)) {
					logoPicName = logoPic;
				}
			}
		}
		File file = PictureUtil.getLogoPic(logoPicName,"org");
		if (!file.exists()) {
			return;
		}
		FileInputStream fis = null;
		try {
			// 去指定上传目录 获取当前下载图片的输入流
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			log.error("图片转流失败", e);
		}
		ServletOutputStream os = null;
		try {
			// 获取response响应流
			os = response.getOutputStream();
			// 设置下载的响应类型
			response.setContentType("image/jpeg");
			response.setHeader("content-disposition", "inline; name= " + URLEncoder.encode(logoPicName, "UTF-8"));
			int len = 0;
			byte[] b = new byte[1024];
			while (true) {
				len = fis.read(b);
				if (len == -1)
					break;
				os.write(b, 0, len);
			}
		} catch (IOException e) {
			log.error("图片 获取 失败", e);
		} finally {
			// 无论如何 关流
			IOUtils.closeQuietly(fis);
			IOUtils.closeQuietly(os);
		}

	}
}
