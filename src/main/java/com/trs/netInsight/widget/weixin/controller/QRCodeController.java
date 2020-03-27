/*
 * Project: netInsight
 * 
 * File Created at 2018年1月25日
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.shiro.EasyTypeToken;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.NetworkUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.util.WeixinMessageUtil;
import com.trs.netInsight.util.WeixinUtil;
import com.trs.netInsight.widget.login.service.ILoginService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.weixin.entity.login.Weixinlogin;
import com.trs.netInsight.widget.weixin.entity.login.repository.WeixinLoginRepository;
import com.trs.netInsight.widget.weixin.entity.qrcode.QRCode;
import com.trs.netInsight.widget.weixin.entity.qrcode.QRCodeBind;
import com.trs.netInsight.widget.weixin.entity.qrcode.Ticket;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * 二维码类
 * 
 * @Type QRCodeController.java
 * @author 谷泽昊
 * @date 2018年1月25日 下午2:32:51
 * @version
 */
@Slf4j
@RestController
@RequestMapping("/system/qrcode")
@Api(description = "二维码")
public class QRCodeController {

	@Autowired
	private ILoginService loginService;
	@Autowired
	private WeixinLoginRepository weixinLoginRepository;

	@Value("${spring.session.timeout}")
	private int sessionTimeout;

	/**
	 * 生成关注的二维码
	 * 
	 * @date Created at 2018年1月26日 下午4:38:51
	 * @Author 谷泽昊
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("生成关注的二维码")
	@FormatResult
	@GetMapping(value = "/createQrcodeBind")
	public Object createQrcodeBind() throws TRSException {
		Map<String, String> map = new HashMap<>();
		// 调用接口获取access_token
		String token = WeixinUtil.getToken();
		Ticket createQrcode = null;
		String imgUrl = null;
		try {
			QRCode code = new QRCode(WeixinMessageUtil.QRCODE_TYPE_BIND);
			createQrcode = WeixinUtil.createQrcode(token, code);
			String ticket = createQrcode.getTicket();
			imgUrl = WeixinUtil.showQrcode(ticket);
			map.put("ticket", ticket);
			map.put("imgUrl", imgUrl);
			QRCodeBind qrCodeBind = new QRCodeBind(ticket, imgUrl, UserUtils.getUser().getId(),
					UserUtils.getUser().getUserName(), UserUtils.getUser().getDisplayName());
			RedisUtil.setString(ticket, JSONObject.toJSONString(qrCodeBind), 1800, TimeUnit.SECONDS);
			RedisUtil.setString(ticket + String.valueOf(WeixinMessageUtil.WEIXIN_BIND_ALERT), "新生成", 1800,
					TimeUnit.SECONDS);
			return map;
		} catch (Exception e) {
			log.error("生成二维码失败:", e);
			throw new TRSException(CodeUtils.FAIL, "生成二维码失败！");
		}
	}

	@ApiOperation("生成绑定登录的二维码")
	@FormatResult
	@GetMapping(value = "/qrcodeLoginBind")
	public Object qrcodeLoginBind() throws TRSException {
		User user = UserUtils.getUser();
		Map<String, String> map = new HashMap<>();
		// 调用接口获取access_token
		String token = WeixinUtil.getToken();
		Ticket createQrcode = null;
		String imgUrl = null;
		try {
			QRCode code = new QRCode(WeixinMessageUtil.QRCODE_TYPE_LOGIN);
			createQrcode = WeixinUtil.createQrcode(token, code);
			String ticket = createQrcode.getTicket();
			imgUrl = WeixinUtil.showQrcode(ticket);
			map.put("ticket", ticket);
			map.put("imgUrl", imgUrl);
			// QRCodeBind qrCodeBind = new QRCodeBind(ticket, imgUrl,
			// UserUtils.getUser().getId(), UserUtils.getUser().getUserName(),
			// UserUtils.getUser().getDisplayName());
			// RedisUtil.setString(ticket, JSONObject.toJSONString(qrCodeBind),
			// 1800, TimeUnit.SECONDS);
			if (ObjectUtil.isEmpty(user)) {// 如果为空 就是登录 如果不为空 就是绑定
				RedisUtil.setString(ticket, String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN), 1800,
						TimeUnit.SECONDS);
				RedisUtil.setString(ticket + String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN), "新生成", 1800,
						TimeUnit.SECONDS);
			} else {
				// 扫描二维码时获取不到这些信息 所以存到redis再取
				RedisUtil.setString(ticket + "userId", user.getId(), 1800, TimeUnit.SECONDS);
				RedisUtil.setString(ticket + "userName", user.getUserName(), 1800, TimeUnit.SECONDS);
				if (StringUtil.isNotEmpty(user.getSubGroupId())){
					RedisUtil.setString(ticket + "subGroupId", user.getSubGroupId(), 1800, TimeUnit.SECONDS);
				}

				// RedisUtil.setString(ticket+"userAccount",
				// user.getUserAccount(), 1800, TimeUnit.SECONDS);
				RedisUtil.setString(ticket, String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN_BIND), 1800,
						TimeUnit.SECONDS);
				RedisUtil.setString(ticket + String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN_BIND), "新生成", 1800,
						TimeUnit.SECONDS);
			}
			// token一样是 ticket一样 导致二维码登录时从redis取值一样
			// RedisUtil.deleteString(WeixinUtil.WEIXINREDISKEY);
			return map;
		} catch (Exception e) {
			log.error("生成二维码失败:", e);
			throw new TRSException(CodeUtils.FAIL, "生成二维码失败！");
		}
	}

	@ApiOperation("查看当前用户是否绑定微信")
	@FormatResult
	@GetMapping(value = "/isWeixinBind")
	public Object isWeixinBind() throws TRSException {
		Map<String, Boolean> map = new HashMap<>();
		try {
			String userName = UserUtils.getUser().getUserName();
			List<Weixinlogin> weixinBind = weixinLoginRepository.findByUserAccount(userName);
			if (weixinBind != null && weixinBind.size() > 0) {
				map.put("status", true);
			} else {
				map.put("status", false);
			}
			return map;
		} catch (Exception e) {
			throw new TRSException(CodeUtils.FAIL, "查询绑定状态失败");
		}
	}

	@ApiOperation("登录微信解除绑定")
	@FormatResult
	@GetMapping(value = "/noWeixinLoginBind")
	public Object noWeixinLoginBind() throws TRSException {
		try {
			String userName = UserUtils.getUser().getUserName();
			List<Weixinlogin> weixinBind = weixinLoginRepository.findByUserAccount(userName);
			if (weixinBind != null && weixinBind.size() > 0) {
				weixinLoginRepository.delete(weixinBind.get(0));
			} else {
				throw new TRSException(CodeUtils.FAIL, "未绑定");
			}
			return "解除微信登录绑定";
		} catch (Exception e) {
			throw new TRSException(CodeUtils.FAIL, "解除微信绑定失败");
		}
	}

	@ApiOperation("生成登录的二维码 不被shiro拦截")
	@FormatResult
	@GetMapping(value = "/createQrcodeLogin")
	public Object createQrcodeLogin(HttpServletRequest request) throws TRSException {
		// 获取登录ip
		String ip = NetworkUtil.getIpAddress(request);
		Map<String, String> map = new HashMap<>();
		// 调用接口获取access_token
		String token = WeixinUtil.getToken();
		Ticket createQrcode = null;
		String imgUrl = null;
		try {
			QRCode code = new QRCode(WeixinMessageUtil.QRCODE_TYPE_LOGIN);
			createQrcode = WeixinUtil.createQrcode(token, code);
			String ticket = createQrcode.getTicket();
			imgUrl = WeixinUtil.showQrcode(ticket);
			map.put("ticket", ticket);
			map.put("imgUrl", imgUrl);
			RedisUtil.setString(ticket, String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN), 1800, TimeUnit.SECONDS);
			RedisUtil.setString(ticket + String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN), "新生成", 1800,
					TimeUnit.SECONDS);
			RedisUtil.setString(ticket + "ip", ip, 3000, TimeUnit.SECONDS);
			// token一样时 ticket一样 导致二维码登录时从redis取值一样
			RedisUtil.deleteString(WeixinUtil.WEIXINREDISKEY);
			return map;
		} catch (Exception e) {
			log.error("生成二维码失败:", e);
			throw new TRSException(CodeUtils.FAIL, "生成二维码失败！");
		}
	}

	@ApiOperation("返回二维码状态  登录")
	@GetMapping(value = "/checkStatus")
	@FormatResult
	public Object checkStatus(@RequestParam(value = "ticket", required = true) String ticket) {
		Map<String, String> map = new HashMap<>();
		// 去redis检查登录状态
		String userName = RedisUtil.getString(ticket + WeixinMessageUtil.WEIXIN_USERNAME);
		map.put(WeixinMessageUtil.WEIXIN_USERNAME, userName);
		String ip = RedisUtil.getString(ticket + WeixinMessageUtil.WEIXIN_IP);
		map.put(WeixinMessageUtil.WEIXIN_IP, ip);
		if (StringUtil.isEmpty(RedisUtil.getString(ticket))) {
			map.put("status", "二维码已过期");
		} else {
			String status = RedisUtil.getString(ticket + String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN));
			map.put("status", status);
		}
		return map;
	}

	@ApiOperation("返回二维码状态  预警绑定")
	@GetMapping(value = "/checkAlertStatus")
	@FormatResult
	public Object checkAlertStatus(@RequestParam(value = "ticket", required = true) String ticket) {
		Map<String, String> map = new HashMap<>();
		String key = ticket + String.valueOf(WeixinMessageUtil.WEIXIN_BIND_ALERT);
		String status = RedisUtil.getString(key);
		if (StringUtil.isEmpty(status)) {
			map.put("status", "二维码已过期");
		} else {
			map.put("status", status);
		}
		return map;
	}

	@ApiOperation("通过二维码登录")
	@FormatResult
	@Log(systemLogOperation = SystemLogOperation.WECHAT_LOGIN, systemLogType = SystemLogType.LOGIN, methodDescription = "登录账号为：@{userName}", systemLogOperationPosition = "登录账号为：${userName}")
	@PostMapping(value = "/loginByQrcode")
	public Object loginByQrcode(HttpServletRequest request,
			@ApiParam("ticket") @RequestParam(value = "ticket") String ticket) throws TRSException {
		if (StringUtil.isEmpty(RedisUtil.getString(ticket))) {
			throw new TRSException(CodeUtils.FAIL, "登录失败,二维码已过期");
		}
		String status = RedisUtil.getString(ticket + String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN));
		if(!StringUtils.equals("已扫描",status)){
			throw new TRSException(CodeUtils.FAIL, "登录失败,状态为：["+status+"]");
		}
		String userName = RedisUtil.getString(ticket + WeixinMessageUtil.WEIXIN_USERNAME);
		String ip = RedisUtil.getString(ticket + WeixinMessageUtil.WEIXIN_IP);
		
		EasyTypeToken token = new EasyTypeToken(userName);
		String login = loginService.login(token, userName, ip);
		RedisUtil.setString(ticket + String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN), "登录成功", 1800,
				TimeUnit.SECONDS);
		return login;
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月25日 谷泽昊 creat
 */