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
package com.trs.netInsight.widget.user.controller;

import com.trs.netInsight.config.constant.ChartConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.api.entity.ApiClient;
import com.trs.netInsight.support.api.service.IOAuthService;
import com.trs.netInsight.support.fts.model.factory.HybaseFactory;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.config.entity.SystemConfig;
import com.trs.netInsight.widget.config.service.ISystemConfigService;
import com.trs.netInsight.widget.notice.service.IMailSendService;
import com.trs.netInsight.widget.user.entity.LoginPageConfig;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.Organization.OrganizationType;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.entity.enums.Status;
import com.trs.netInsight.widget.user.service.ILoginPageConfigService;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.ISubGroupService;
import com.trs.netInsight.widget.user.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 机构Controller
 *
 * @Type OrganizationController.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:09:09
 * @version
 */
@Slf4j
@RestController
@Api(description = "机构接口")
@RequestMapping(value = { "/organization" })
public class OrganizationController {

	@Autowired
	private IOrganizationService organizationService;
	@Autowired
	private IUserService userService;

	@Autowired
	private IOAuthService authService;

	@Autowired
	private IMailSendService mailSendService;

	@Autowired
	private ISubGroupService subGroupService;

	@Autowired
	private ILoginPageConfigService loginPageConfigService;

	@Autowired
	private ISystemConfigService systemConfigService;
	@Autowired
	private IHybaseShardService hybaseShardService;
	/**
	 * 超管邮箱
	 */
	@Value(value = "${email.services.super}")
	private String emailServicesSuper;
	/**
	 * logo 图标地址
	 */
//	@Value("${logo.picture.path}")
//	private String logoPicPath;

	/**
	 * 添加机构
	 *
	 * @date Created at 2017年11月20日 下午2:04:41
	 * @Author 谷泽昊
	 * @param userName
	 *            账号
	 * @param displayName
	 *            用户名
	 * @param password
	 *            密码
	 * @param passwordAgain
	 *            再次输入密码
	 * @param organizationName
	 *            机构名称
	 * @param email
	 *            邮箱
	 * @param userLimit
	 *            机构用户限制
	 * @param expireAt
	 *            有效期
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("添加机构")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.ORGANIZATION_ADD, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@PostMapping(value = "/add")
	public Object add(
			@ApiParam("机构类型:正式formal，试用trial") @RequestParam(value = "organizationType") OrganizationType organizationType,
			@ApiParam("机构名称") @RequestParam(value = "organizationName") String organizationName,
			@ApiParam(name = "上传logo图片", required = false) @RequestParam(value = "filePicture", required = false) MultipartFile filePicture,
			@ApiParam("管理员账号") @RequestParam(value = "userName") String userName,
			@ApiParam("登录密码") @RequestParam(value = "password") String password,
			@ApiParam("确认密码") @RequestParam(value = "passwordAgain") String passwordAgain,
			@ApiParam("管理员昵称") @RequestParam(value = "displayName") String displayName,
			@ApiParam(name = "联系邮箱") @RequestParam(value = "email",required = false) String email,
			@ApiParam(name = "联系电话", required = false) @RequestParam(value = "phone", required = false) String phone,
			@ApiParam("有效期，默认永久2050-01-01 00:00:00") @RequestParam(value = "expireAt", defaultValue = "2050-01-01 00:00:00") String expireAt,
			@ApiParam("客户来源") @RequestParam(value = "customerSource") String customerSource,
			@ApiParam("销售负责人") @RequestParam(value = "headOfSales") String headOfSales,
			@ApiParam("运维负责人,多个用逗号隔开") @RequestParam(value = "rolePlatforms") String[] rolePlatforms,
			@ApiParam(name = "备注", required = false) @RequestParam(value = "descriptions", required = false) String descriptions,

			// 机构配置
			@ApiParam(name = "机构用户限制（登录账号个数）") @RequestParam(value = "userLimit", defaultValue = "5") int userLimit,
			//@ApiParam(name = "系统名称", required = false) @RequestParam(value = "systemName", required = false, defaultValue = "网察大数据分析平台") String systemName,
			@ApiParam(name = "数量限制——日常监测") @RequestParam(value = "columnNum", defaultValue = "50") int columnNum,
			@ApiParam(name = "数量限制——专题") @RequestParam(value = "specialNum", defaultValue = "10") int specialNum,
			@ApiParam(name = "数量限制——预警主题") @RequestParam(value = "alertNum", defaultValue = "10") int alertNum,
			@ApiParam(name = "数量限制——预警账号") @RequestParam(value = "alertAccountNum", defaultValue = "5") int alertAccountNum,
			@ApiParam(name = "数量限制——关键字") @RequestParam(value = "keyWordsNum", defaultValue = "500") int keyWordsNum,
			//@ApiParam(name = "账号同时登录", required = false) @RequestParam(value = "sameTimeLogin", required = false, defaultValue = "false") boolean sameTimeLogin,
			@ApiParam(name = "数据设置，全部传ALL，多个用逗号隔开") @RequestParam(value = "dataSources", defaultValue = "ALL") String[] dataSources,
			@ApiParam(name = "可检索时间限制——日常监测，为天数", required = false) @RequestParam(value = "columnDateLimit", required = false, defaultValue = "90") int columnDateLimit,
			@ApiParam(name = "可检索时间限制——专题分析，为天数", required = false) @RequestParam(value = "specialDateLimit", required = false, defaultValue = "365") int specialDateLimit,
			@ApiParam(name = "可检索时间限制——高级搜索，为天数", required = false) @RequestParam(value = "aSearchDateLimit", required = false, defaultValue = "90") int aSearchDateLimit,

			@ApiParam(name = "机构登录页配置网页标签", required = false) @RequestParam(value = "pageTitle",required = false) String pageTitle,
			@ApiParam(name = "机构登录页配置链接后缀", required = false) @RequestParam(value = "suffix",required = false) String suffix,
			@ApiParam(name = "机构登录页配置logo图片", required = false) @RequestParam(value = "loginPagePicture", required = false) MultipartFile loginPagePicture,
			@ApiParam(name = "机构登录页配置公司名称", required = false) @RequestParam(value = "companyName",required = false) String companyName,
			@ApiParam(name = "机构登录页配置申请电话", required = false) @RequestParam(value = "applyTel",required = false) String applyTel,
			@ApiParam(name = "机构登录页配置是否显示轮播图", required = false) @RequestParam(value = "isShowCarousel",required = false) Boolean isShowCarousel,
			@ApiParam(name = "机构登录页配置是否屏蔽申请试用", required = false) @RequestParam(value = "isShieldRegister",required = false) Boolean isShieldRegister,
			@ApiParam(name = "机构登录页配置底部二维码", required = false) @RequestParam(value = "loginPageQRCode", required = false) MultipartFile loginPageQRCode,
 			@ApiParam("传统库表名") @RequestParam(value = "tradition",required = false)String tradition,
			@ApiParam("微博库表名") @RequestParam(value = "weiBo",required = false)String weiBo,
			@ApiParam("微信库表名") @RequestParam(value = "weiXin",required = false)String weiXin,
			@ApiParam("海外库表名") @RequestParam(value = "overseas",required = false)String overseas
			//@ApiParam("普通账号权限，多个用逗号隔开") @RequestParam(value = "roleIds", required = false) String[] roleIds,
			// api暂时未实现
	) throws TRSException {
		SystemConfig systemConfig = systemConfigService.findSystemConfig();
		// 判断权限
		if (!UserUtils.isSuperAdmin()) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限添加机构！");

		}

		// 判断机构名是否为空
		if (StringUtils.isBlank(organizationName)) {
			throw new TRSException(CodeUtils.ORGANIZATIONNAME_NULL, "机构名字不能为空！");
		}

		// 判断账号是否为空
		if (StringUtils.isBlank(userName)) {
			throw new TRSException(CodeUtils.ACCOUNT_NULL, "账号不能为空！");
		}
		// 判断账号是否有空格
		if (RegexUtils.checkBlankSpace(userName)) {
			throw new TRSException(CodeUtils.ACCOUNT_SPACE, "账号中不能含有空格！");
		}

		// 判断密码是否为空
		if (StringUtils.isBlank(password)) {
			throw new TRSException(CodeUtils.PASSWORD_NULL, "密码不能为空！");
		}
		// 判断密码强度
		if (!RegexUtils.isLowSafely(password)) {
			throw new TRSException(CodeUtils.PASSWORD_LOW, "密码需要在8-16位，并且有大写、小写、数字和特殊字符至少三种！");
		}
		// 判断两次密码是否一致
		if (!password.equals(passwordAgain)) {
			throw new TRSException(CodeUtils.PASSWORD_NOT_SAME, "两次密码不一致！");
		}

		// 判断用户名 （昵称） 是否为空
		if (StringUtils.isBlank(displayName)) {
			throw new TRSException(CodeUtils.DISPLAYNAME_NULL, "用户名不能为空！");
		}

		// 判断邮件是否为空  产品确认后 邮箱为选填项
//		if (StringUtils.isBlank(email)){
//			throw new TRSException(CodeUtils.EMAIL_NULL,"联系邮箱不能为空！");
//		}
		//判断 邮箱格式是否正确
		if (StringUtils.isNotBlank(email) && !RegexUtils.checkEmail(email)) {
			throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
		}

		// 判断手机是否为空,是否正确
		if (StringUtils.isNotBlank(phone) && !RegexUtils.checkMobile(phone)) {
			throw new TRSException(CodeUtils.PHONE_FAIL, "手机号格式不正确！");
		}

		// 判断日期
		if (StringUtils.isBlank(expireAt)) {
			throw new TRSException(CodeUtils.FAIL, "日期状态不能为空！");
		} else {
			if (!expireAt.equals("0") && !DateUtil.isValidDate(expireAt, DateUtil.yyyyMMdd)) {
				throw new TRSException(CodeUtils.FAIL, "日期格式不正确，应为" + DateUtil.yyyyMMdd + "！");
			}
		}

		// 判断客户来源是否为空
		if (StringUtils.isBlank(customerSource)) {
			throw new TRSException(CodeUtils.FAIL, "客户来源不能为空！");
		}
		// 判断销售负责人是否为空
		if (StringUtils.isBlank(headOfSales)) {
			throw new TRSException(CodeUtils.FAIL, "销售负责人不能为空！");
		}
		if(systemConfig.getNeedOperation()){ // 系统设置，可以不需要运维人员
			//运维负责人是否为空
			if (null == rolePlatforms || rolePlatforms.length < 1){
				throw new TRSException(CodeUtils.FAIL,"运维负责人不能为空！");
			}
		}
		// 登录账号个数
		if (userLimit < 1) {
			throw new TRSException(CodeUtils.FAIL, "登录账号个数不能小于 1 ！");
		}
		// 日常监测栏目数
		if (columnNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "日常监测栏目个数不能小于 1 ！");
		}
		// 专题个数
		if (specialNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "专题个数不能小于 1 ！");
		}
		// 预警
		if (alertNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "预警主题个数不能小于 1 ！");
		}

		// 预警账号个数
		if (alertAccountNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数不能小于 1 ！");
		}
		// 关键字个数
		if (keyWordsNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "关键字个数不能小于 1 ！");
		}

		// 数据来源设置
		if (null == dataSources) {
			throw new TRSException(CodeUtils.FAIL, "请选择数据设置！");
		}

		if (dataSources == null) {
			dataSources = new String[]{"ALL"};
		}

//		try {updateOrganization
		// 判断机构名是否存在
		// 20191018 允许机构名称重复
		/*if (organizationService.findByOrganizationName(organizationName) != null) {
			throw new TRSException(CodeUtils.ORGANIZATIONNAME_EXISTED, "机构名字已存在！");
		}*/
		/* } catch (Exception e) {
			 log.error("判断机构名是否存在：", e);
			 throw new TRSException(CodeUtils.FAIL, "添加失败！");
		 }*/

		// 判断用户账号是否存在
		if (userService.findByUserName(userName) != null) {
			throw new TRSException(CodeUtils.USERNAME_EXISTED, "管理员账号已经被使用，请重新输入管理员账号！");
		}
		String loginLogoPic = null;
		String QRCodePic = null;
		if(suffix != null && !"".equals(suffix)){

			if(loginPageConfigService.findBySuffix(suffix) != null){
				throw new TRSException(CodeUtils.FAIL, "链接已被占用，请更换其他链接！");
			}
			/*if(!judgeConfigLinkFormat(suffix)){
				throw new TRSException(CodeUtils.FAIL, "网页链接：长度在2-16个字符之间，且只可以包含英文字符!");
			}*/
			// 判断网页标签是否为空
			if (StringUtils.isBlank(pageTitle)) {
				throw new TRSException(CodeUtils.FAIL, "网页标签不能为空！");
			}
			// 判断网页标签是否有空格
			if (RegexUtils.checkBlankSpace(pageTitle)) {
				throw new TRSException(CodeUtils.FAIL, "网页标签中不能含有空格！");
			}
			/*if(!judgeConfigTitleFormat(pageTitle)){
				throw new TRSException(CodeUtils.FAIL, "网页标签：长度在2-10个字符之间，且不包含特殊字符!");
			}*/

			// 公司名称是否为空
			if (StringUtils.isBlank(companyName)) {
				throw new TRSException(CodeUtils.FAIL, "公司名称不能为空！");
			}
			// 公司名称是否有空格
			if (RegexUtils.checkBlankSpace(companyName)) {
				throw new TRSException(CodeUtils.FAIL, "公司名称中不能含有空格！");
			}
			/*// 公司名称是否符合格式
			if (!judgeConfigTitleFormat(companyName)) {
				throw new TRSException(CodeUtils.FAIL, "公司名称：长度在2-10个字符，且不包含特殊字符！");
			}*/
			// 判断申请电话是否为空,格式是否正确
			if (StringUtils.isNotBlank(applyTel) && !judgeTel(applyTel) ) {
				throw new TRSException(CodeUtils.PHONE_FAIL, "手机号格式不正确！");
			}
			// 上传logo图片

			if (loginPagePicture != null) {
				try {
					loginLogoPic = PictureUtil.transferLogo(loginPagePicture, organizationName+"_loginLogo", "org");
				} catch (IOException e) {
					log.error("图片上传 失败", e);
					throw new TRSException(CodeUtils.FAIL, "添加公司logo图标失败");
				}
			}
			// 上传logo图片

			if (loginPageQRCode != null) {
				try {
					QRCodePic = PictureUtil.transferLogo(loginPageQRCode, organizationName+"_QRCode", "org");
				} catch (IOException e) {
					log.error("图片上传 失败", e);
					PictureUtil.deletePic( loginLogoPic,"org");
					throw new TRSException(CodeUtils.FAIL, "添加底栏二维码失败");
				}
			}
		}

		// 上传logo图片
		String pictureName = null;
		if (filePicture != null) {
			try {
				pictureName = PictureUtil.transferLogo(filePicture, organizationName, "org");
			} catch (IOException e) {
				log.error("图片上传 失败", e);
				PictureUtil.deletePic( loginLogoPic,"org");
				PictureUtil.deletePic( QRCodePic,"org");
				throw new TRSException(CodeUtils.FAIL, "添加logo图标失败");
			}
		}


		organizationService.add(organizationType, organizationName,pictureName, userName,password, displayName, email, phone,
				expireAt, customerSource, headOfSales, rolePlatforms,descriptions, userLimit, columnNum, specialNum,alertNum,alertAccountNum,keyWordsNum, dataSources, columnDateLimit
				,specialDateLimit,aSearchDateLimit,suffix,pageTitle,companyName,applyTel,loginLogoPic,QRCodePic,isShieldRegister,isShowCarousel,1,0,null,tradition,weiBo,weiXin,overseas);
		return "添加成功！";
	}

	/**
	 * 删除机构
	 *
	 * @date Created at 2017年11月20日 下午3:24:11
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("删除机构")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.ORGANIZATION_DELETE, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@GetMapping(value = "delete/{id}")
	public Object delete(@ApiParam("机构id") @PathVariable(value = "id") String id,
						 @ApiParam("验证码") @RequestParam(value = "code",required = false) String code, HttpServletRequest request,
						 HttpServletResponse response) throws TRSException {
		SystemConfig systemConfig = systemConfigService.findSystemConfig();
		//  系统管理   如果删除机构不需要
		if(systemConfig.getDeleteOrg()){
			// 验证码
			if (StringUtils.isBlank(code)) {
				throw new TRSException(CodeUtils.FAIL, "请输入验证码！");
			}
			if(!code.equals("trs300229")){
				HttpSession session = request.getSession();
				Object attribute = session.getAttribute(CodeUtils.SUPER_CODE_KEY + id);
				if (attribute == null) {
					throw new TRSException(CodeUtils.FAIL, "验证码不正确！");
				}
				String sessionCode = String.valueOf(attribute);
				if (!StringUtils.equals(sessionCode.toLowerCase(), code.toLowerCase())) {
					throw new TRSException(CodeUtils.FAIL, "验证码不正确！");
				}
				session.removeAttribute(CodeUtils.SUPER_CODE_KEY + id);
				// 判断权限
				if (!UserUtils.isSuperAdmin()) {
					throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限删除机构！");
				}
			}
		}else{
			// 判断权限
			if (!UserUtils.isSuperAdmin()) {
				throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限删除机构！必须为超管");
			}
		}
		// 删除机构
		try {
			organizationService.delete(id);
		} catch (Exception e) {
			throw new TRSException(CodeUtils.FAIL, "删除失败！");
		}
		return "删除成功！";
	}

	/**
	 * 修改状态--启用、禁用
	 *
	 * @date Created at 2017年11月20日 下午3:35:28
	 * @Author 谷泽昊
	 * @param id
	 * @param status
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("修改机构状态")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.ORGANIZATION_UPDATE_STATUS, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@GetMapping(value = "updateStatus/{id}")
	public Object updateStatus(@ApiParam("机构id") @PathVariable(value = "id") String id,
							   @ApiParam("状态  1为冻结 0为正常") @RequestParam(value = "status") String status) throws TRSException {
		// 判断权限
		if (!UserUtils.isSuperAdmin() && !UserUtils.isRolePlatform()) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改状态！");
		}
		Organization organization = organizationService.findById(id);
		if (ObjectUtil.isEmpty(organization)){
			throw new TRSException(CodeUtils.FAIL,"未找到对应机构信息！");
		}
		organization.setStatus(Status.getStatusByValue(status));
		organizationService.update(organization);
		//需要同时修改机构下所有用户状态信息
		//机构管理员
		List<User> orgAdminUser = userService.findRoleAdminByOrganizationId(organization.getId());
		if (ObjectUtil.isNotEmpty(orgAdminUser)){
			for (User user : orgAdminUser) {
				user.setStatus(Status.getStatusByValue(status));
				userService.update(user,false);
			}
		}
		//普通用户及用户分组
		List<SubGroup> subGroups = subGroupService.findByOrgId(organization.getId());
		if (ObjectUtil.isNotEmpty(subGroups)){
			for (SubGroup subGroup : subGroups) {
				//该方法 也会同时冻结 当前用户分组下的用户账号
				subGroupService.updateStatus(subGroup,status);
			}
		}

		return "修改状态成功！";
	}

	/**
	 * 修改机构
	 *
	 * @date Created at 2017年11月20日 下午5:04:22
	 * @Author 谷泽昊
	 * @param id
	 * @param displayName
	 * @param organizationName
	 * @param email
	 * @param userLimit
	 * @param expireAt
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("修改机构")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.ORGANIZATION_UPDATE, systemLogType = SystemLogType.USER, systemLogOperationPosition = "")
	@PostMapping(value = "/updateOrganization")
	public Object updateOrganization(@ApiParam("机构id") @RequestParam(value = "id") String id,
									 @ApiParam("机构类型:正式formal，试用trial") @RequestParam(value = "organizationType") OrganizationType organizationType,
									 @ApiParam("机构名称") @RequestParam(value = "organizationName") String organizationName,
									 @ApiParam(name = "上传logo图片", required = false) @RequestParam(value = "filePicture", required = false) MultipartFile filePicture,
									 @ApiParam("上传logo图片的名字") @RequestParam(value = "pictureName", required = false) String pictureName,
									 @ApiParam("管理员账号") @RequestParam(value = "userName") String userName,
									 @ApiParam("登录密码") @RequestParam(value = "password",required = false) String password,
									 @ApiParam("确认密码") @RequestParam(value = "passwordAgain",required = false) String passwordAgain,
									 @ApiParam("用户名") @RequestParam(value = "displayName") String displayName,
									 @ApiParam("联系邮箱") @RequestParam(value = "email",required = false) String email,
									 @ApiParam("联系电话") @RequestParam(value = "phone", required = false) String phone,
									 @ApiParam("有效期") @RequestParam(value = "expireAt", defaultValue = "2050-01-01 00:00:00") String expireAt,
									 @ApiParam("客户来源") @RequestParam(value = "customerSource") String customerSource,
									 @ApiParam("销售负责人") @RequestParam(value = "headOfSales") String headOfSales,
									 @ApiParam("运维负责人,多个用逗号隔开") @RequestParam(value = "rolePlatforms") String[] rolePlatforms,
									 @ApiParam("备注") @RequestParam(value = "descriptions", required = false) String descriptions,
									 // 机构配置
									 @ApiParam(name = "机构用户限制（登录账号个数）") @RequestParam(value = "userLimit", defaultValue = "5") int userLimit,
									 @ApiParam(name = "数量限制——日常监测") @RequestParam(value = "columnNum", defaultValue = "50") int columnNum,
									 @ApiParam(name = "数量限制——专题") @RequestParam(value = "specialNum", defaultValue = "10") int specialNum,
									 @ApiParam(name = "数量限制——预警主题") @RequestParam(value = "alertNum", defaultValue = "10") int alertNum,
									 @ApiParam(name = "数量限制——预警账号") @RequestParam(value = "alertAccountNum", defaultValue = "5") int alertAccountNum,
									 @ApiParam(name = "数量限制——关键字") @RequestParam(value = "keyWordsNum", defaultValue = "500") int keyWordsNum,
									 @ApiParam(name = "数据设置，全部传ALL，多个用逗号隔开") @RequestParam(value = "dataSources", defaultValue = "ALL") String[] dataSources,
									 @ApiParam(name = "可检索时间限制——日常监测，为天数", required = false) @RequestParam(value = "columnDateLimit", required = false, defaultValue = "90") int columnDateLimit,
									 @ApiParam(name = "可检索时间限制——专题分析，为天数", required = false) @RequestParam(value = "specialDateLimit", required = false, defaultValue = "365") int specialDateLimit,
									 @ApiParam(name = "可检索时间限制——高级搜索，为天数", required = false) @RequestParam(value = "asearchDateLimit", required = false, defaultValue = "90") int asearchDateLimit,


									 @ApiParam(name = "机构登录页配置网页标签", required = false) @RequestParam(value = "pageTitle",required = false) String pageTitle,
									 @ApiParam(name = "机构登录页配置链接后缀", required = false) @RequestParam(value = "suffix",required = false) String suffix,
									 @ApiParam(name = "机构登录页配置logo图片", required = false) @RequestParam(value = "loginPagePicture", required = false) MultipartFile loginPagePicture,
									 @ApiParam(name = "机构登录页配置logo图片名字",required = false) @RequestParam(value = "loginPagePictureName", required = false) String loginPagePictureName,
									 @ApiParam(name = "机构登录页配置公司名称", required = false) @RequestParam(value = "companyName",required = false) String companyName,
									 @ApiParam(name = "机构登录页配置申请电话", required = false) @RequestParam(value = "applyTel",required = false) String applyTel,
									 @ApiParam(name = "机构登录页配置是否显示轮播图", required = false) @RequestParam(value = "isShowCarousel",required = false) Boolean isShowCarousel,
									 @ApiParam(name = "机构登录页配置是否屏蔽申请试用", required = false) @RequestParam(value = "isShieldRegister",required = false) Boolean isShieldRegister,
									 @ApiParam(name = "机构登录页配置底部二维码", required = false) @RequestParam(value = "loginPageQRCode", required = false) MultipartFile loginPageQRCode,
									 @ApiParam(name="机构登录页配置底部二维码",required = false) @RequestParam(value = "QRCodePictureName", required = false) String QRCodePictureName,
									 @ApiParam("传统库表名") @RequestParam(value = "tradition",required = false)String tradition,
									 @ApiParam("微博库表名") @RequestParam(value = "weiBo",required = false)String weiBo,
									 @ApiParam("微信库表名") @RequestParam(value = "weiXin",required = false)String weiXin,
									 @ApiParam("海外库表名") @RequestParam(value = "overseas",required = false)String overseas
									 //@ApiParam("账号同时登录") @RequestParam(value = "sameTimeLogin", required = false, defaultValue = "false") boolean sameTimeLogin,
									 //@ApiParam("普通账号权限，多个用逗号隔开") @RequestParam(value = "roleIds", required = false) String[] roleIds,
									 //@ApiParam("数据设置，全部传ALL，多个用逗号隔开") @RequestParam(value = "dataSources", required = false, defaultValue = "ALL") String[] dataSources,
									 //@ApiParam("可检索数据，为天数") @RequestParam(value = "dataDate", required = false, defaultValue = "7") int dataDate
									 // api暂时未实现
	) throws TRSException {
		SystemConfig systemConfig = systemConfigService.findSystemConfig();
		// 判断机构名是否为空
		if (StringUtils.isBlank(organizationName)) {
			throw new TRSException(CodeUtils.ORGANIZATIONNAME_NULL, "机构名字不能为空！");
		}

		// 判断账号是否为空
		if (StringUtils.isBlank(userName)) {
			throw new TRSException(CodeUtils.ACCOUNT_NULL, "账号不能为空！");
		}
		// 判断账号是否有空格
		if (RegexUtils.checkBlankSpace(userName)) {
			throw new TRSException(CodeUtils.ACCOUNT_SPACE, "账号中不能含有空格！");
		}
		// 判断密码是否为空
		/*if (StringUtils.isBlank(password)) {
			throw new TRSException(CodeUtils.PASSWORD_NULL, "密码不能为空！");
		}*/
		// 判断密码强度
		if (StringUtils.isNotBlank(password) && !RegexUtils.isLowSafely(password)) {
			throw new TRSException(CodeUtils.PASSWORD_LOW, "密码需要在8-16位，并且有大写、小写、数字和特殊字符至少三种！");
		}
		// 判断两次密码是否一致
		if (StringUtils.isNotBlank(password) && StringUtils.isNotBlank(passwordAgain) && !password.equals(passwordAgain)) {
			throw new TRSException(CodeUtils.PASSWORD_NOT_SAME, "两次密码不一致！");
		}
		// 判断用户名是否为空
		if (StringUtils.isBlank(displayName)) {
			throw new TRSException(CodeUtils.DISPLAYNAME_NULL, "用户名不能为空！");
		}
		// 判断邮件是否为空  产品确认  邮箱为选填项
//		if (StringUtils.isBlank(email)){
//			throw new TRSException(CodeUtils.EMAIL_NULL,"联系邮箱不能为空！");
//		}
		// 判断邮件是否为空,是否正确
		if (StringUtils.isNotBlank(email) && !RegexUtils.checkEmail(email)) {
			throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
		}
		// 判断手机是否为空,是否正确
		if (StringUtils.isNotBlank(phone) && !RegexUtils.checkMobile(phone)) {
			throw new TRSException(CodeUtils.PHONE_FAIL, "手机号格式不正确！");
		}

		// 判断日期
		if (StringUtils.isBlank(expireAt)) {
			throw new TRSException(CodeUtils.FAIL, "日期状态不能为空！");
		} else {
			if (!expireAt.equals(UserUtils.FOREVER_DATE) && !DateUtil.isValidDate(expireAt, DateUtil.yyyyMMdd)) {
				throw new TRSException(CodeUtils.FAIL, "日期格式不正确，应为" + DateUtil.yyyyMMdd + "！");
			}
		}

		// 判断客户来源是否为空
		if (StringUtils.isBlank(customerSource)) {
			throw new TRSException(CodeUtils.FAIL, "客户来源不能为空！");
		}
		// 判断销售负责人是否为空
		if (StringUtils.isBlank(headOfSales)) {
			throw new TRSException(CodeUtils.FAIL, "销售负责人不能为空！");
		}
		if(systemConfig.getNeedOperation()){
			//运维负责人是否为空
			if (null == rolePlatforms || rolePlatforms.length < 1){
				throw new TRSException(CodeUtils.FAIL,"运维负责人不能为空！");
			}
		}

		List<SubGroup> subGroups = subGroupService.findByOrgId(id);
		int currentColumnNum = columnNum;
		int currentSpecialNum = specialNum;
		int currentAlertNum = alertNum;
		int currentAlertAccountNum = alertAccountNum;
		int currentUserLimit = userLimit;
		if (ObjectUtil.isNotEmpty(subGroups)){
			for (SubGroup subGroup : subGroups) {
				currentColumnNum -= subGroup.getColumnNum();
				currentSpecialNum -= subGroup.getSpecialNum();
				currentAlertNum -= subGroup.getAlertNum();
				currentAlertAccountNum -= subGroup.getAlertAccountNum();
				currentUserLimit -= subGroup.getUserLimit();
			}
		}

		// 登录账号个数
		if (userLimit < 1) {
			throw new TRSException(CodeUtils.FAIL, "登录账号个数不能小于 1 ！");
		}
		if (currentUserLimit < 0){
			throw new TRSException(CodeUtils.FAIL, "该机构已分配的登录账号大于 "+userLimit+" !");
		}

		// 日常监测栏目数
		if (columnNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "日常监测栏目个数不能小于 1 ！");
		}
		if (currentColumnNum < 0){
			throw new TRSException(CodeUtils.FAIL, "该机构已分配的日常监测栏目数大于 "+columnNum+" !");
		}
		// 专题个数
		if (specialNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "专题个数不能小于 1 ！");
		}
		if (currentSpecialNum < 0){
			throw new TRSException(CodeUtils.FAIL, "该机构已分配的专题分析数大于 "+specialNum+" !");
		}
		// 预警
		if (alertNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "预警主题个数不能小于 1 ！");
		}
		if (currentAlertNum < 0){
			throw new TRSException(CodeUtils.FAIL, "该机构已分配的预警主题数大于 "+alertNum+" !");
		}
		// 预警账号个数
		if (alertAccountNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数不能小于 1 ！");
		}
		if (currentAlertAccountNum < 0){
			throw new TRSException(CodeUtils.FAIL, "该机构已分配的可绑定预警账号数数大于 "+alertAccountNum+" !");
		}
		// 关键字个数
		if (keyWordsNum < 1) {
			throw new TRSException(CodeUtils.FAIL, "关键字个数不能小于 1 ！");
		}

		// 数据来源设置
		if (null == dataSources) {
			throw new TRSException(CodeUtils.FAIL, "请选择数据设置！");
		}
		if (dataSources == null) {
			dataSources = new String[]{"ALL"};
		}

		Organization organization = organizationService.findById(id);
		// 判断用户账号是否存在
		User userAmin = userService.findById(organization.getAdminUserId());
		if (ObjectUtil.isNotEmpty(userAmin)){
			if (userService.findByUserName(userName) != null && !userAmin.getUserName().equals(userName)) {
				throw new TRSException(CodeUtils.USERNAME_EXISTED, "管理员账号已经被使用，请重新输入管理员账号！");
			}
		}

		// 上传logo图片
		String fileName = null;

		try {
			if (organization != null) {
				String loginLogoPic = null;
				String QRCodePic = null;
				//后缀不为空要保存
				if(suffix != null && !"".equals(suffix)){

					if(loginPageConfigService.findBySuffix(suffix) != null
							&& !organization.getId().equals(loginPageConfigService.findBySuffix(suffix).getRelevanceOrganizationId())){
						throw new TRSException(CodeUtils.FAIL, "链接已被占用，请更换其他链接！");
					}
					/*if(!judgeConfigLinkFormat(suffix)){
						throw new TRSException(CodeUtils.FAIL, "网页链接：长度在2-16个字符之间，且只可以包含英文字符!");
					}*/
					// 判断网页标签是否为空
					if (StringUtils.isBlank(pageTitle)) {
						throw new TRSException(CodeUtils.FAIL, "网页标签不能为空！");
					}
					// 判断网页标签是否有空格
					if (RegexUtils.checkBlankSpace(pageTitle)) {
						throw new TRSException(CodeUtils.FAIL, "网页标签中不能含有空格！");
					}
					/*if(!judgeConfigTitleFormat(pageTitle)){
						throw new TRSException(CodeUtils.FAIL, "网页标签：长度在2-10个字符之间，且不包含特殊字符!");
					}*/

					// 公司名称是否为空
					if (StringUtils.isBlank(companyName)) {
						throw new TRSException(CodeUtils.FAIL, "公司名称不能为空！");
					}
					// 公司名称是否有空格
					if (RegexUtils.checkBlankSpace(companyName)) {
						throw new TRSException(CodeUtils.FAIL, "公司名称中不能含有空格！");
					}
					/*// 公司名称是否符合格式
					if (!judgeConfigTitleFormat(companyName)) {
						throw new TRSException(CodeUtils.FAIL, "公司名称：长度在2-10个字符，且不包含特殊字符！");
					}*/
					// 判断申请电话是否为空,格式是否正确
					if (StringUtils.isNotBlank(applyTel) && !judgeTel(applyTel)) {
						throw new TRSException(CodeUtils.PHONE_FAIL, "手机号格式不正确！");
					}
					// 上传logo图片

					if (loginPagePicture != null) {
						try {
							loginLogoPic = PictureUtil.transferLogo(loginPagePicture, organizationName+"_loginLogo", "org");
						} catch (IOException e) {
							log.error("图片上传 失败", e);
							throw new TRSException(CodeUtils.FAIL, "添加公司logo图标失败");
						}
					}
					// 上传logo图片

					if (loginPageQRCode != null) {
						try {
							QRCodePic = PictureUtil.transferLogo(loginPageQRCode, organizationName+"_QRCode", "org");
						} catch (IOException e) {
							log.error("图片上传 失败", e);
							// 只要有上传新的logo 原有logo必会删除
							// 删除原有图片
							PictureUtil.deletePic( loginLogoPic,"org");
							throw new TRSException(CodeUtils.FAIL, "添加底栏二维码失败");
						}
					}
				}

				if (filePicture != null && !StringUtils.equals(organization.getLogoPicName(), pictureName)) {
					//上传了新的logo图片
					// 文件上传时的原名
					try {
						// 上传图片
						fileName = PictureUtil.transferLogo(filePicture, organizationName, "org");

					} catch (IOException e) {
						log.error("图片上传 失败", e);
						PictureUtil.deletePic( loginLogoPic,"org");
						PictureUtil.deletePic( QRCodePic,"org");
						throw new TRSException(CodeUtils.FAIL, "添加logo图标失败");
					}

					// 只要有上传新的logo 原有logo必会删除
					// 删除原有图片
					PictureUtil.deletePic( organization.getLogoPicName(),"org");
				}
				// 认为删除 原来的logo图片（删除原有logo，同时没有上传新的logo图片）
				if ("".equals(pictureName) && fileName == null) {
					// 删除机构下的logo图片
					PictureUtil.deletePic(organization.getLogoPicName(),"org");
				}
				if (fileName == null && pictureName != "无logo" && !"".equals(pictureName)) {
					// 说明未上传新的logo 则采用原来的logo名
					// 此时 若pictureName也是null 说明该机构需要换回原有默认网察logo
					fileName = pictureName;
				}

				hybaseShardService.save(HybaseFactory.getServer(),HybaseFactory.getUserName(),HybaseFactory.getPassword(),tradition,weiBo,weiXin,overseas,null,id);
				organizationService.updateOrganization(id, organizationType, organizationName,fileName,userName,password, displayName, email,
						phone, expireAt, customerSource, headOfSales, rolePlatforms, descriptions, userLimit,columnNum,specialNum,alertNum,alertAccountNum,
						keyWordsNum,dataSources,columnDateLimit,specialDateLimit,asearchDateLimit,suffix,pageTitle,
						companyName,applyTel,loginLogoPic,QRCodePic,isShieldRegister,isShowCarousel,loginPagePictureName,QRCodePictureName);
				return "修改机构成功！";
			}
			return "未找到该机构";
		} catch (Exception e) {
			log.error("修改机构失败：", e);
			throw new TRSException(CodeUtils.FAIL, "修改机构失败！", e);
		}
	}

	/**
	 * 查询机构
	 *
	 * @date Created at 2017年11月20日 下午4:37:23
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @param organizationType
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("查询机构")
	@FormatResult
	@GetMapping(value = "/pageList")
	public Object pageList(
			@ApiParam("页码") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
			@ApiParam("页长") @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize,
			@ApiParam("客户来源(现支持多选，多个以英文分号（;）分割)") @RequestParam(value = "customerSource", required = false) String customerSource,
			@ApiParam("销售负责人(支持多选，多个以英文分号（;）分割)") @RequestParam(value = "headOfSales", required = false) String headOfSales,
			@ApiParam("剩余有效期排序,默认不传值，后台按创建日期降序排") @RequestParam(value = "surplusDateSort", required = false) String surplusDateSort,
			@ApiParam("机构类型:正式formal，试用trial") @RequestParam(value = "organizationType", required = false) String organizationType,
			@ApiParam("状态") @RequestParam(value = "status", required = false) String status,
			@ApiParam("检索条件:机构名称organizationName，登录账号userName") @RequestParam(value = "retrievalCondition", required = false) String retrievalCondition,
			@ApiParam("检索信息") @RequestParam(value = "retrievalInformation", required = false) String retrievalInformation)
			throws TRSException {
		if (pageNo < 0) {
			pageNo = 0;
		}

		// 判断权限
		if (!UserUtils.isSuperAdmin()) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限查看机构列表！");
		}

		List<String> customerSourceSplit = new ArrayList<>();
		List<String> headOfSalesSplit = new ArrayList<>();
		//客户来源
		if (StringUtil.isNotEmpty(customerSource)){
			String[] split = customerSource.split(";|；");
			customerSourceSplit = Arrays.asList(split);
		}
		//销售负责人
		if (StringUtil.isNotEmpty(headOfSales)){
			String[] split = headOfSales.split(";|；");
			headOfSalesSplit = Arrays.asList(split);
		}
		Page<Organization> pageList = null;
		try {
			pageList = organizationService.pageList(pageNo, pageSize, customerSourceSplit,headOfSalesSplit,surplusDateSort, organizationType,
					status,retrievalCondition, retrievalInformation);
		} catch (Exception e) {
			log.error("查询机构失败：", e);
			throw new TRSException(CodeUtils.FAIL, "查询失败！");
		}
		if (pageList != null && pageList.getContent() != null && pageList.getContent().size() > 0){
			for (Organization organization : pageList) {
				if (organization.getLogoPicName() == null || "null".equals(organization.getLogoPicName())) {
					organization.setLogoPicName("无logo");
				}
			}
		}

		return pageList;
	}

	/**
	 * 查询机构登录配置页信息
	 *
	 * @date Created at
	 * @Author
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("查询机构登录配置页信息")
	@FormatResult
	@PostMapping(value = "/selectOrganizationLoginConfig")
	public Object selectOrganizationLoginConfig(
			@ApiParam("机构id") @RequestParam(value = "id") String id
	)
			throws TRSException {
		// 判断权限
		if (!UserUtils.isSuperAdmin() && !UserUtils.isRolePlatform()) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限查看机构列表！");
		}

		try {
			Organization organization = organizationService.findById(id);
			if(organization != null ){
				LoginPageConfig loginPageConfig = loginPageConfigService.findByOrgId(id);
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
				return loginPageConfig;
			}
		} catch (Exception e) {
			log.error("查询机构配置失败：", e);
			throw new TRSException(CodeUtils.FAIL, "查询失败！");
		}
		return "无对应机构";
	}
	/**
	 * 添加logo图标
	 *
	 * @param filePicture
	 *            长传 的 logo 图片
	 * @param organizationName
	 *            机构名称
	 * @param organizationId
	 *            机构id
	 * @param request
	 * @param response
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("添加logo图标")
	@FormatResult
	@PostMapping(value = "/addLogo")
	public Object add(@ApiParam("上传图片") @RequestParam(value = "filePicture") MultipartFile filePicture,
					  @ApiParam("机构名称") @RequestParam(value = "organizationName") String organizationName,
					  @ApiParam("机构id") @RequestParam(value = "organizationId") String organizationId, HttpServletRequest request,
					  HttpServletResponse response) throws TRSException {
		// String realPath =
		// request.getSession().getServletContext().getRealPath("/logo/picture");

		Organization organization = organizationService.findById(organizationId);
		// 文件上传时的原名
		String originalFileName = filePicture.getOriginalFilename();
		// 文件扩展名
		String extendName = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
		// String newFileName = originalFileName + extendName;
		String newFileName = originalFileName + "_" + System.currentTimeMillis() + (int) (Math.random() * 10000)
				+ extendName;
		String logoPicPath = "D:/IdeaProjects/netInsight/logo/picture";
		// organization.setLogoPicUrl(logoPicPath);
		organization.setLogoPicName(newFileName);
		if (StringUtils.isNotBlank(organizationName)) {
			organization.setOrganizationName(organizationName);
		}
		organizationService.update(organization);
		try {
			// 上传图片
			filePicture.transferTo(new File(logoPicPath, newFileName));
			return "添加logo图标成功";
		} catch (IOException e) {
			e.printStackTrace();
			log.error("图片上传 失败", e);
			throw new TRSException(CodeUtils.FAIL, "添加logo图标失败");
		}
	}

	/**
	 * 获取 logo 图标（修改机构）
	 *
	 * @param organizationId
	 *            机构 id
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("获取logo图标")
	@GetMapping(value = "/getLogoPic")
	public void getLogoPicture(@ApiParam("机构id") @RequestParam(value = "organizationId") String organizationId,
							   HttpServletRequest request, HttpServletResponse response) {
		Organization organization = organizationService.findById(organizationId);
		String logoPicName = "wangcha.png";
		String type = "org";
		if (organization != null) {
			String logoPic = organization.getLogoPicName();
			if (StringUtils.isNotBlank(logoPic)) {
				logoPicName = logoPic;
			}
		}

		if("wangcha.png".equals(logoPicName) || "无logo".equals(logoPicName)){
			SystemConfig systemConfig = systemConfigService.findSystemConfig();
			if(!"".equals(systemConfig.getLogoPicName()) && !"无logo".equals(systemConfig.getLogoPicName())){
				logoPicName = systemConfig.getLogoPicName();
				type = "default";
			}
		}
		if("无logo".equals(logoPicName)){
			logoPicName = "wangcha.png";
		}

		File file = PictureUtil.getLogoPic(logoPicName,type);
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

	/**
	 * 获取 logo 图标（修改机构）
	 *
	 * @param organizationId
	 *            机构 id
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("获取登录页配置的logo图标或者二维码")
	@GetMapping(value = "/getLoginConfigPic")
	public void getLoginConfigPic(@ApiParam("机构id") @RequestParam(value = "organizationId") String organizationId,
								  @ApiParam("机构登录链接的后缀") @RequestParam(value = "suffix") String suffix,
								  @ApiParam("要获取图片的类型") @RequestParam(value = "type") String type,
								  HttpServletRequest request, HttpServletResponse response) {
		Organization organization = organizationService.findById(organizationId);
		String logoPicName = null;
		if("logo".equals(type)){
			logoPicName = "wangcha.png";
		}else if("QRCode".equals(type)){
			logoPicName = "WX_gzh.png";
		}

		if (organization != null) {
			LoginPageConfig loginPageConfig = loginPageConfigService.findByOrgId(organization.getId());
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
		SystemConfig systemConfig = systemConfigService.findSystemConfig();
		String fileType = "org";
		if("logo".equals(type) && ("".equals(logoPicName) || "wangcha.png".equals(logoPicName) || "无logo".equals(logoPicName))){
			if(!"".equals(systemConfig.getLogoPicName()) && !"无logo".equals(systemConfig.getLogoPicName())){
				logoPicName = systemConfig.getLogoPicName();
				fileType = "default";
			}
		}
		File file = PictureUtil.getLogoPic(logoPicName,fileType);
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

	/**
	 * 登录后 左上角展示
	 * @param organizationId
	 * @param userGroupId
	 * @param request
	 * @param response
	 */
	@ApiOperation("获取logo图标")
	@GetMapping(value = "/getSystemLogoPic")
	public void getSystemLogoPicture(@ApiParam("机构id") @RequestParam(value = "organizationId") String organizationId,
									 @ApiParam("用户分组id") @RequestParam(value = "userGroupId",required = false) String userGroupId,
									 HttpServletRequest request, HttpServletResponse response) {
		Organization organization = organizationService.findById(organizationId);
		SubGroup subGroup = null;
		if (StringUtil.isNotEmpty(userGroupId)){
			subGroup = subGroupService.findOne(userGroupId);
		}
		String logoPicName = "wangcha.png";
		String type = null;
		if (subGroup != null){
			String logoPic = subGroup.getLogoPicName();
			if (StringUtils.isNotBlank(logoPic)) {
				logoPicName = logoPic;
			}
			type = "group";
		}else if (organization != null) {
			String logoPic = organization.getLogoPicName();
			if (StringUtils.isNotBlank(logoPic)) {
				logoPicName = logoPic;
			}
			type = "org";
		}
		if("wangcha.png".equals(logoPicName) || "无logo".equals(logoPicName)){
			SystemConfig systemConfig = systemConfigService.findSystemConfig();
			if(!"".equals(systemConfig.getLogoPicName()) && !"无logo".equals(systemConfig.getLogoPicName())){
				logoPicName = systemConfig.getLogoPicName();
				type = "default";
			}
		}
		if("无logo".equals(logoPicName)){
			logoPicName = "wangcha.png";
		}

		File file = PictureUtil.getLogoPic(logoPicName,type);
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

	/**
	 * 获取 logo 图标（默认的机构图标）
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@ApiOperation("获取默认的logo图标")
	@GetMapping(value = "/getDefaultLogoPic")
	public void getDefaultLogoPic(
			HttpServletRequest request, HttpServletResponse response) {
		//获取默认的logo图片    如果修改了管理页面的logo图片则使用这个图片作为默认logo
		// 机构 、公司、分组的默认logo保持一致  只有本身图片信息为无logo时调用这个
		String logoPicName = "wangcha.png";
		String type = "default";
		SystemConfig systemConfig = systemConfigService.findSystemConfig();
		if (!"".equals(systemConfig.getLogoPicName()) && !"无logo".equals(systemConfig.getLogoPicName())) {
			logoPicName = systemConfig.getLogoPicName();
		}

		if("无logo".equals(logoPicName)){
			logoPicName = "wangcha.png";
		}
		File file = PictureUtil.getLogoPic(logoPicName, type);
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
	/**
	 * 申请开通客户端
	 *
	 * @since changjiang @ 2018年6月29日
	 * @return
	 * @Return : Object
	 */
	@ApiOperation("申请客户端")
	@GetMapping("/applyClient")
	public Object applyClient() {
		User user = UserUtils.getUser();
		String orgId = user.getOrganizationId();
		ApiClient client = authService.applyClient(orgId);
		return client;
	}

	/**
	 * 查询运维管理的机构或者没有管理的机构
	 *
	 * @date Created at 2018年9月20日 下午6:47:32
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("查询运维管理的机构或者没有管理的机构")
	@FormatResult
	@GetMapping(value = "/findByIsPlatformHold/{id}")
	public Object findByIsPlatformHold(@ApiParam("用户id") @PathVariable(value = "id") String id,
			@ApiParam("true 为管理的，false 为没有管理的") @RequestParam(value = "governing", required = false, defaultValue = "true") boolean governing,
			@ApiParam("pageNo") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
			@ApiParam("pageSize") @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@ApiParam("客户来源(现支持多选，多个以英文分号（;）分割)") @RequestParam(value = "customerSource", required = false) String customerSource,
			@ApiParam("销售负责人(支持多选，多个以英文分号（;）分割)") @RequestParam(value = "headOfSales", required = false) String headOfSales,
			@ApiParam("机构类型:正式formal，试用trial") @RequestParam(value = "organizationType", required = false) String organizationType,
			@ApiParam("剩余有效期排序,默认不传值，后台按创建日期降序排") @RequestParam(value = "surplusDateSort", required = false) String surplusDateSort,
			@ApiParam("状态") @RequestParam(value = "status", required = false) String status,
			@ApiParam("检索条件:机构名称organizationName，系统名称systemName，销售负责人headOfSales,登录账号userName") @RequestParam(value = "retrievalCondition", required = false) String retrievalCondition,
			@ApiParam("检索信息") @RequestParam(value = "retrievalInformation", required = false) String retrievalInformation)
			throws TRSException {
		if (!(UserUtils.isSuperAdmin() || UserUtils.isRolePlatform())) {
			throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "赋权失败！权限不够");
		}
		List<String> customerSourceSplit = new ArrayList<>();
		List<String> headOfSalesSplit = new ArrayList<>();
		//客户来源
		if (StringUtil.isNotEmpty(customerSource)){
			String[] split = customerSource.split(";|；");
			customerSourceSplit = Arrays.asList(split);
		}
		//销售负责人
		if (StringUtil.isNotEmpty(headOfSales)){
			String[] split = headOfSales.split(";|；");
			headOfSalesSplit = Arrays.asList(split);
		}
		return organizationService.findByIsPlatformHold(pageNo, pageSize, governing, id, customerSourceSplit,
				headOfSalesSplit,organizationType, retrievalCondition, retrievalInformation,surplusDateSort,status);
	}

	/**
	 * 根据机构id获取机构权限和是否同时登录
	 *
	 * @date Created at 2018年9月26日 下午10:14:56
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	@ApiOperation("根据机构id获取机构权限和是否同时登录")
	@FormatResult
	@GetMapping(value = "/getOrganizationRAndS")
	public Object getOrganizationRoleANdSameTimeLogin(
			@ApiParam("机构id") @RequestParam(value = "organizationId", required = false) String organizationId) {
		return organizationService.getOrganizationRoleANdSameTimeLogin(organizationId);
	}

	/**
	 * 删除机构时获取的验证码
	 *
	 * @date Created at 2018年9月29日 下午2:33:17
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ApiOperation("删除机构时获取的验证码")
	@FormatResult
	@GetMapping(value = "/sendCode")
	public Object sendCode(@ApiParam("机构id") @RequestParam(value = "organizationId") String organizationId,
						   HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (StringUtils.isBlank(emailServicesSuper)) {
			throw new TRSException(CodeUtils.FAIL, "请联系相关人员配置超管邮箱！");
		}
		String[] emailServicesSupers = emailServicesSuper.split(";");
		String code = VerifyUtil.getCode(4);
		HttpSession session = request.getSession();
		// 将验证码存入Session
		session.setMaxInactiveInterval(5 * 60);
		session.setAttribute(CodeUtils.SUPER_CODE_KEY + organizationId, code);
		return mailSendService.sendMail("验证码", "【网察】您的验证码为：" + code + "。（5分钟内有效！）", emailServicesSupers);
	}

	@ApiOperation("获取所有存入表内的销售负责人")
	@FormatResult
	@GetMapping(value = "/selectHeadOfSales")
	public Object selectHeadOfSales() throws Exception {
		if (!UserUtils.ROLE_LIST.contains(UserUtils.getUser().getCheckRole())){
			throw new TRSException(CodeUtils.FAIL,"当前用户没有权限查询销售负责人信息！");
		}
		return organizationService.findAllForHeadOfSales();
	}

	/**
	 * 查询机构详情
	 * @param organizationId
	 * @return
	 * @throws Exception
	 */
	@ApiOperation("查询机构详情")
	@FormatResult
	@GetMapping(value = "/selectOrgDetail")
	public Object selectOrgDetail(@ApiParam("机构id") @RequestParam(value = "organizationId",required = false) String organizationId) throws Exception {
		if (!UserUtils.ROLE_LIST.contains(UserUtils.getUser().getCheckRole())){
			throw new TRSException(CodeUtils.FAIL,"当前用户没有权限查询机构信息！");
		}
		if (UserUtils.isRoleAdmin()){
			//机构管理员
			organizationId = UserUtils.getUser().getOrganizationId();
		}
		return organizationService.findById(organizationId);
	}

	public Boolean judgeConfigLinkFormat(String str){
		if(str.length()>=2 && str.length()<=16 && str.matches("^[A-Za-z0-9]+$")){
			return true;
		}
		return false;
	}
	public Boolean judgeConfigTitleFormat(String str){
		String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		if(str.length() >=2 && str.length()<=10 && !m.find()){
			return true;
		}
		return false;
	}

	public boolean judgeTel(String str){
		String regex1 = "((0\\d{2,3})-)(\\d{7,8})(-(\\d{3,}))?$";
		String regex2 = "(\\+\\d+)?1[34578]\\d{9}$";
		if(Pattern.matches(regex1, str)){
			return true;
		}else if(Pattern.matches(regex2, str)){
			return true;
		}else{
			return false;
		}
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月20日 谷泽昊 creat
 */
