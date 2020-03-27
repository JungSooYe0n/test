/*
 * Project: netInsight
 * 
 * File Created at 2018年9月18日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.config.controller;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.PictureUtil;
import com.trs.netInsight.util.RegexUtils;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.config.entity.HybaseDatabaseConfig;
import com.trs.netInsight.widget.config.entity.SystemConfig;
import com.trs.netInsight.widget.config.helper.ConfigConst;
import com.trs.netInsight.widget.config.service.IConfigurationService;
import com.trs.netInsight.widget.config.service.ISystemConfigService;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置类 controller
 * 
 * @Type ConfigurationController.java
 * @author 谷泽昊
 * @date 2018年9月18日 下午4:27:11
 * @version
 */
@RestController
@Api(description = "系统配置接口")
@RequestMapping(value = { "/configuration" })
public class ConfigurationController {
	@Autowired
	private IConfigurationService configurationService;

	@Autowired
	private ISystemConfigService systemConfigService;
	/**
	 * 添加指定配置
	 * 
	 * @date Created at 2018年9月25日 下午4:48:02
	 * @Author 谷泽昊
	 * @param recevice
	 * @param hotline
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("添加指定配置")
	@FormatResult
	@PostMapping("/addConfiguration")
	public Object addConfiguration(
			@ApiParam("申请试用邮箱接收人") @RequestParam(value = "recevice", required = false) String recevice,
			@ApiParam("过期提醒邮箱接收人") @RequestParam(value = "platform", required = false) String platform,
			@ApiParam("系统热线电话") @RequestParam(value = "hotline", required = false) String hotline) throws TRSException {
		if (StringUtils.isNotBlank(recevice)) {
			String[] recevices = recevice.split(";");
			for (String string : recevices) {
				if (!RegexUtils.checkEmail(string)) {
					throw new TRSException(CodeUtils.FAIL, "申请试用邮箱接收人【" + string + "】不正确！");
				}
			}
		}
		if (StringUtils.isNotBlank(platform)) {
			String[] platforms = platform.split(";");
			for (String string : platforms) {
				if (!RegexUtils.checkEmail(string)) {
					throw new TRSException(CodeUtils.FAIL, "过期提醒邮箱接收人【" + string + "】不正确！");
				}
			}
		}
		if (StringUtils.isNotBlank(hotline)) {
			if (!RegexUtils.checkDecimals(hotline)) {
				throw new TRSException(CodeUtils.FAIL, "系统热线电话不正确！");
			}
		}
		//添加
		configurationService.updateConfig(ConfigConst.EMAIL_SERVICES_APPLY_RECEVICE, recevice);
		configurationService.updateConfig(ConfigConst.EMAIL_SERVICES_PLATFORM,platform);
		configurationService.updateConfig(ConfigConst.SYS_HOTLINE, hotline);
		return "添加成功";
	}

	/**
	 * 查询指定配置--临时用
	 * 
	 * @date Created at 2018年9月25日 下午5:00:05
	 * @Author 谷泽昊
	 * @return
	 */
	@ApiOperation("查询指定配置--临时用")
	@FormatResult
	@GetMapping("/findBySysConfigure")
	public Object findBySysConfigure() {
		Map<String, String> map = new HashMap<>();
		String recevice = configurationService.getConfigValue(ConfigConst.EMAIL_SERVICES_APPLY_RECEVICE, "", null);
		String platform = configurationService.getConfigValue(ConfigConst.EMAIL_SERVICES_PLATFORM, "", null);
		String hotline = configurationService.getConfigValue(ConfigConst.SYS_HOTLINE, "", null);
		map.put("recevice", recevice);
		map.put("platform", platform);
		map.put("hotline", hotline);
		return map;
	}

	/**
	 * 查询系统配置信息  包括hybase设置  超管默认机构名和机构图片等
	 *
	 * @date Created at 2020年01月07日
	 * @Author 张娅
	 * @return
	 */
	@ApiOperation("查询系统配置信息  包括hybase设置  超管默认机构名和机构图片等")
	@FormatResult
	@GetMapping("/findSystemConfig")
	public Object findSystemConfig() throws  TRSException{
		User user = UserUtils.getUser();
		if(!UserUtils.SUPER_ADMIN.equals(user.getCheckRole())){
			throw new TRSException("仅超管可以访问");
		}
		Map<String, Object> map = new HashMap<>();
		HybaseDatabaseConfig hybaseDatabaseConfig = systemConfigService.queryHybaseDatabases();
		map.put("hybaseDatabase", hybaseDatabaseConfig);
		SystemConfig systemConfig = systemConfigService.findSystemConfig();
		map.put("systemConfig", systemConfig);
		return map;
	}

	/**
	 * 查询系统配置信息  包括hybase设置  超管默认机构名和机构图片等
	 *
	 * @date Created at 2020年01月07日
	 * @Author 张娅
	 * @return
	 */
	@ApiOperation("查询系统配置信息  主要为超管默认机构名、机构图片、删除机构和需要运维人员等")
	@FormatResult
	@GetMapping("/orgManage")
	public Object orgManage() throws  TRSException{
		Map<String, Object> map = new HashMap<>();
		SystemConfig systemConfig = systemConfigService.findSystemConfig();
		map.put("systemConfig", systemConfig);
		return map;
	}


	/**
	 * 修改系统配置信息  包括hybase设置  超管默认机构名和机构图片等
	 *
	 * @date Created at 2020年01月07日
	 * @Author 张娅
	 * @return
	 */
	@ApiOperation("修改系统配置信息  包括hybase设置  超管默认机构名和机构图片等")
	@FormatResult
	@PostMapping("/updateSystemConfig")
	public Object updateSystemConfig(
			@ApiParam("机构名称") @RequestParam(value = "organizationName", defaultValue = "网察大数据分析平台") String organizationName,
			@ApiParam(name = "上传logo图片", required = false) @RequestParam(value = "filePicture", required = false) MultipartFile filePicture,
			@ApiParam("上一次logo图片的名字") @RequestParam(value = "pictureName", required = false) String pictureName,

			@ApiParam("删除机构是否需要邮件 需要：true 默认需要") @RequestParam(value = "deleteOrg", defaultValue = "true") Boolean deleteOrg,
			@ApiParam("创建机构是否需要运维人员 需要true 默认需要") @RequestParam(value = "needOperation", defaultValue = "true") Boolean needOperation,

			@ApiParam("hybase库设置 -- 微博库") @RequestParam(value = "weibo", required = false) String weibo,
			@ApiParam("hybase库设置 -- 传统库") @RequestParam(value = "traditional", required = false) String traditional,
			@ApiParam("hybase库设置 -- 微信库") @RequestParam(value = "weixin", required = false) String weixin,
			@ApiParam("hybase库设置 -- 海外 TF库") @RequestParam(value = "overseas", required = false) String overseas,
			@ApiParam("hybase库设置 -- 手工录入库") @RequestParam(value = "insert", required = false) String insert,
			@ApiParam("hybase库设置 -- 微博用户库") @RequestParam(value = "sinaweiboUsers", required = false) String sinaweiboUsers
	) throws  TRSException{
		User user = UserUtils.getUser();
		if(!UserUtils.SUPER_ADMIN.equals(user.getCheckRole())){
			throw new TRSException("仅超管可以访问");
		}
		//机构问题  图片如果有修改，则传文件，和历史图片名，如果没有修改则传历史图片名
		SystemConfig systemConfig = systemConfigService.findSystemConfig();
		String fileName = systemConfig.getLogoPicName();
		if(pictureName == null || "".equals(pictureName) || "无logo".equals(pictureName)){
			PictureUtil.deletePic( systemConfig.getLogoPicName(),"default");
			fileName = "无logo";
		}
		if (filePicture != null ) {
			try {
				// 上传图片
				fileName = PictureUtil.transferLogo(filePicture, organizationName, "default");
			} catch (IOException e) {
				throw new TRSException(CodeUtils.FAIL, "添加logo图标失败");
			}
			// 只要有上传新的logo 原有logo必会删除
			// 删除原有图片
			PictureUtil.deletePic( systemConfig.getLogoPicName(),"default");
		}
		if(fileName == null && (pictureName == null || "".equals(pictureName))){
			fileName = "无logo";
		}else if(fileName == null && (pictureName != null && !"".equals(pictureName) && !"无logo".equals(pictureName) && !"无logo".equals(systemConfig.getLogoPicName()) )){
			fileName = systemConfig.getLogoPicName();
		}
		systemConfigService.updateSystemConfig(organizationName,fileName,deleteOrg,needOperation);

		//hybase库设置，如果为空设置成默认 或不改变
		systemConfigService.updateHybaseDatabaseConfig(traditional,weibo,weixin,overseas,insert,sinaweiboUsers);
		return "success";
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年9月18日 谷泽昊 creat
 */