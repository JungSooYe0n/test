/*
 * Project: netInsight
 * 
 * File Created at 2018年1月22日
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.SHA1;
import com.trs.netInsight.util.WeixinMessageUtil;
import com.trs.netInsight.util.WeixinUtil;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertSendWeChat;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.service.IAlertAccountService;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.alert.service.ISendAlertService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IUserService;
import com.trs.netInsight.widget.weixin.entity.AlertTemplateMsg;
import com.trs.netInsight.widget.weixin.entity.menu.Button;
import com.trs.netInsight.widget.weixin.entity.menu.CommonButton;
import com.trs.netInsight.widget.weixin.entity.menu.Menu;
import com.trs.netInsight.widget.weixin.service.IReplyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 微信调用Controller
 * 
 * @Type WeixinAlertController.java
 * @author 谷泽昊
 * @date 2018年1月22日 下午3:36:58
 * @version
 */
@Slf4j
@RestController
@RequestMapping("/system/weixin")
@Api(description = "微信预警")
public class WeixinController {

	@Autowired
	private IReplyService replyService;

	@Autowired
	private IAlertAccountService alertAccountService;

	@Autowired
	private ISendAlertService sendAlertService;

	@Autowired
	private IUserService userService;

	@Autowired
	private IAlertService alertService;

	// 自定义 token
	private String TOKEN = "trsnetinsight";

	@FormatResult
	@GetMapping("/demo")
	public Object send() {
		String access_token = WeixinUtil.getToken();
		AlertTemplateMsg alertTemplateMsg = new AlertTemplateMsg("on_HD1SAng0MfDXKXO1xmz0rK7WM", "http://www.baidu.com",
				"diy", "\\n1\\n2\\n3\\n4\\n", "时间", "备注");
		return  WeixinUtil.sendWeixin(access_token, alertTemplateMsg);
	}

	/**
	 * 与微信服务器验证绑定
	 * 
	 * @date Created at 2018年1月23日 上午10:20:07
	 * @Author 谷泽昊
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @param echostr
	 * @throws IOException
	 */
	@ApiOperation("与微信服务器验证绑定")
	@GetMapping(value = { "", "/" })
	public void sign(HttpServletRequest request, HttpServletResponse response) throws IOException { // 微信加密签名
		response.setCharacterEncoding("utf-8");
		String signature = request.getParameter("signature");
		// 随机字符串
		String echostr = request.getParameter("echostr");
		// 时间戳
		String timestamp = request.getParameter("timestamp");
		// 随机数
		String nonce = request.getParameter("nonce");

		String[] str = { TOKEN, timestamp, nonce };
		Arrays.sort(str); // 字典序排序
		String bigStr = str[0] + str[1] + str[2];
		// SHA1加密
		String digest = new SHA1().getDigestOfString(bigStr.getBytes()).toLowerCase();
		// 确认请求来至微信
		if (digest.equals(signature)) {
			response.getWriter().print(echostr);
		}
	}

	/**
	 * 微信公众号执行操作：点击，聊天。。。
	 * 
	 * @date Created at 2018年1月26日 上午11:12:02
	 * @Author 谷泽昊
	 * @param body
	 * @return
	 */
	@ApiOperation("微信公众号执行操作：点击，聊天。。。")
	@PostMapping(value = { "", "/" })
	public String message(@RequestBody String body) {
		return replyService.processRequest(body);
	}

	/**
	 * 激活预警或者停用预警
	 * 
	 * @date Created at 2018年1月26日 上午11:41:40
	 * @Author 谷泽昊
	 * @param openId
	 * @param username
	 * @param active
	 * @return
	 */
	@ApiOperation("激活预警或者停用预警")
	@GetMapping(value = "/isActive")
	public String isActive(@RequestParam(value = "openId", required = true) String openId,
			@RequestParam(value = "userName", required = true) String userName,
			@RequestParam(value = "active", required = true, defaultValue = "true") boolean active) {

		try {
			log.error("openId:" + openId);
			log.error("userName:" + userName);
			User user = userService.findByUserName(userName);
			AlertAccount account = alertAccountService.findByAccountAndUserIdAndType(openId, user.getId(),
					SendWay.WE_CHAT);
			if (account == null) {
				return "您还没有绑定！";
			}
			account.setActive(active);
			alertAccountService.add(account);
		} catch (Exception e) {
			if (active) {
				log.error("激活预警失败：", e);
				return "激活预警失败：" + e;
			} else {
				log.error("停用预警失败：", e);
				return "停用预警失败：" + e;
			}
		}
		if (active) {
			return "激活预警成功！";
		} else {
			return "停用预警成功！";
		}
	}

	/**
	 * 取消绑定
	 * 
	 * @date Created at 2018年1月26日 上午11:41:40
	 * @Author 谷泽昊
	 * @param openId
	 * @param username
	 * @param active
	 * @return
	 */
	@ApiOperation("取消绑定")
	@GetMapping(value = "/unBind")
	public String unBind(@RequestParam(value = "openId", required = true) String openId,
			@RequestParam(value = "userName", required = true) String userName) {

		try {
			log.error("openId:" + openId);
			log.error("userName:" + userName);
			User user = userService.findByUserName(userName);
			AlertAccount account = alertAccountService.findByAccountAndUserIdAndType(openId, user.getId(),
					SendWay.WE_CHAT);
			if (account == null) {
				return "您还没有绑定！";
			}
			alertAccountService.delete(account);
		} catch (Exception e) {
			log.error("解除绑定失败：", e);
			return "解除绑定失败：" + e;
		}
		return "解除绑定成功！";
	}

	/**
	 * 查看详情
	 * 
	 * @date Created at 2018年1月31日 下午4:18:50
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 * @throws TRSException 
	 */
	@ApiOperation("查看详情")
	@FormatResult
	@GetMapping(value = "/alertDetails")
	public Object alertDetails(@RequestParam(value = "id", required = true) String id) throws TRSException {
		try {
			AlertSendWeChat findOne = sendAlertService.findOne(id);
			if (findOne != null) {
				Map<String, Object> mapData = new HashMap<>();
				List<Map<String, Object>> listMap = new ArrayList<>();
				String sids = findOne.getIds();
				/*List<String> listString = null;
				if (sids != null) {
					String[] split = sids.split(";");
					listString = Arrays.asList(split);
				} else {
					return "查询不到预警内容！";
				}*/
				List<AlertEntity> list = alertService.findbyIds(sids);
				for (AlertEntity alertEntity : list) {
					Map<String, Object> map = new HashMap<>();
					map.put("title", alertEntity.getTitle());
					map.put("urlTime", alertEntity.getTime());
					map.put("siteName", alertEntity.getSiteName());
					map.put("urlName", alertEntity.getUrlName());
					listMap.add(map);
				}
				mapData.put("alertTime", findOne.getAlertTime());
				mapData.put("size", findOne.getSize());
				mapData.put("data", listMap);
				return mapData;
			} else {
				return "查询不到预警内容！";
			}
		} catch (Exception e) {
			throw new TRSException(CodeUtils.FAIL, "查询失败！");
		}
	}

	/**
	 * 创建菜单
	 * 
	 * @date Created at 2018年1月25日 上午10:53:15
	 * @Author 谷泽昊
	 * @return
	 */
	@GetMapping(value = "/createMenu")
	public int createMenu() {
		// 调用接口获取access_token
		String token = WeixinUtil.getToken();
		int result = 0;
		if (null != token) {
			// 调用接口创建菜单
			result = WeixinUtil.createMenu(getMenu(), token);

			// 判断菜单创建结果
			if (0 == result) {
				log.info("菜单创建成功！");
			} else {
				log.info("菜单创建失败，错误码：" + result);
			}
		}
		return result;
	}

	/**
	 * 组装菜单数据
	 * 
	 * @return
	 */
	private static Menu getMenu() {

		CommonButton btn12 = new CommonButton();
		btn12.setName("预警管理");
		btn12.setType("click");
		btn12.setKey(WeixinMessageUtil.MENU_KEY_ALERT_MANAGE);
		CommonButton btn21 = new CommonButton();
		btn21.setName("解绑");
		btn21.setType("click");
		btn21.setKey(WeixinMessageUtil.MENU_KEY_ACCOUNT);

		/**
		 * 封装整个菜单
		 */
		Menu menu = new Menu();
		menu.setButton(new Button[] { btn12, btn21 });

		return menu;
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月22日 谷泽昊 creat
 */