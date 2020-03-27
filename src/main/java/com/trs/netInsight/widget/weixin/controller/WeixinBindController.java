/*
 * Project: netInsight
 * 
 * File Created at 2018年1月30日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.weixin.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.util.WeixinMessageUtil;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.service.IAlertAccountService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.weixin.entity.login.Weixinlogin;
import com.trs.netInsight.widget.weixin.entity.login.repository.WeixinLoginRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 绑定号类
 * 
 * @Type WeixinBindController.java
 * @author 谷泽昊
 * @date 2018年1月30日 上午9:22:23
 * @version
 */
@Slf4j
@RestController
@RequestMapping("/weixinbind")
@Api(description = "绑定类")
public class WeixinBindController {

	@Autowired
	private IAlertAccountService alertAccountService; 
	
	@Autowired
	private WeixinLoginRepository weixinLoginRepository;

	/**
	 * 查询绑定的微信号
	 * 
	 * @date Created at 2018年1月30日 上午9:27:36
	 * @Author 谷泽昊
	 * @return
	 * @throws TRSException 
	 */
	@ApiOperation("查询绑定的微信号")
	@FormatResult
	@GetMapping("/weixinbind")
	public Object getBindList() throws TRSException {
		User user = UserUtils.getUser();
		List<AlertAccount> list = null;
		try {
			list =alertAccountService.findByUserIdAndType(user.getId(), SendWay.WE_CHAT);
			return  list;
		} catch (Exception e) {
			log.error("查询失败：" , e);
		}
		throw new TRSException(CodeUtils.FAIL, "查询失败！");
	}
	/**
	 * 登录绑定微信号
	 * @param openId
	 * @return
	 */
	@RequestMapping(value = "/bindLogin", method = RequestMethod.GET)
	public String bindLogin(
			@RequestParam(value = "openId", required = true) String openId,
			@RequestParam(value = "ticket", required = true) String ticket) {
		//openId没绑定过就绑定  绑定过就不绑定
		List<Weixinlogin> findByOpenId = weixinLoginRepository.findByOpenId(openId);
		if(findByOpenId == null || findByOpenId.size() == 0){
			String organizationId = UserUtils.getUser().getOrganizationId();
			String userId = UserUtils.getUser().getId();
			Weixinlogin weixinLogin = new Weixinlogin();
			weixinLogin.setOpenId(openId);
			weixinLogin.setOrganizationId(organizationId);
			weixinLogin.setUserId(userId);
			weixinLoginRepository.save(weixinLogin);
			RedisUtil.setString(ticket+String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN), "刚添加到数据库", 1800, TimeUnit.SECONDS);
			return "success";
		}else{
			return "账号已绑定过";
		}
		
	}
	/**
	 * 解绑微信号
	 * 
	 * @date Created at 2018年1月30日 上午9:32:31
	 * @Author 谷泽昊
	 * @param openId
	 * @param userName
	 * @return
	 * @throws TRSException 
	 */
	@ApiOperation("解绑微信号")
	@FormatResult
	@GetMapping("/unBind")
	public Object unBind(@RequestParam(value = "openId") String openId,
			@RequestParam(value = "userName") String userName) throws TRSException {
		User user = UserUtils.getUser();
		String userNameLogin =user.getUserName();
		if (StringUtils.equals(userNameLogin, userName)) {
			try {
				AlertAccount alertAccount = alertAccountService.findByAccountAndUserIdAndType(openId, user.getId(), SendWay.WE_CHAT);
				if (alertAccount != null) {
					alertAccountService.delete(alertAccount);
					return "解绑成功！";
				}
				throw new TRSException(CodeUtils.FAIL, "解绑失败，没有查询到绑定账号！");
			} catch (Exception e) {
				log.error("解绑失败：" , e);
				throw new TRSException(CodeUtils.FAIL, "解绑失败：" + e,e);
			}
		}

		throw new TRSException(CodeUtils.FAIL, "您没有权限解绑！");
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月30日 谷泽昊 creat
 */