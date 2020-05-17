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
package com.trs.netInsight.widget.weixin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.UserHelp;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.service.IAlertAccountService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.SubGroupRepository;
import com.trs.netInsight.widget.weixin.entity.ReplyHandler;
import com.trs.netInsight.widget.weixin.entity.login.Weixinlogin;
import com.trs.netInsight.widget.weixin.entity.login.repository.WeixinLoginRepository;
import com.trs.netInsight.widget.weixin.entity.qrcode.QRCodeBind;
import com.trs.netInsight.widget.weixin.message.resp.TextMessage;
import com.trs.netInsight.widget.weixin.service.IReplyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信回复服务接口
 * 
 * @Type ReplyServiceImpl.java
 * @author 谷泽昊
 * @date 2018年1月25日 下午4:04:57
 * @version
 */
@Slf4j
@Service
public class ReplyServiceImpl implements IReplyService {
	
	@Autowired
	private WeixinLoginRepository weixinLoginRepository;
	@Autowired
	private IAlertAccountService alertAccountService;
	@Autowired
	private UserHelp userService;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private SubGroupRepository subGroupRepository;

	private Map<String, ReplyHandler> replyMap = null;

	private void init() {
		replyMap = new HashMap<String, ReplyHandler>();
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_TEXT, new ReplyHandler() {
			// 发送text
			@Override
			public String reply(String messageBody) throws DocumentException {
				Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
				// 发送方账号
				String fromUserName = requestMap.get("FromUserName");
				// 开发者微信号
				String toUserName = requestMap.get("ToUserName");
				String respContent = "文本查询功能正在开发中，敬请期待！感谢您一直以来对网察的支持！";
				return replyTextMessage(fromUserName, toUserName, respContent);
			}

		});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_IMAGE, new ReplyHandler() {
			// 发送图片
			@Override
			public String reply(String messageBody) throws DocumentException {
				Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
				// 发送方账号
				String fromUserName = requestMap.get("FromUserName");
				// 开发者微信号
				String toUserName = requestMap.get("ToUserName");
				String respContent = "图片功能正在开发中，敬请期待！感谢您一直以来对网察的支持！";
				return replyTextMessage(fromUserName, toUserName, respContent);
			}

		});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_VOICE, new ReplyHandler() {
			// 语音
			@Override
			public String reply(String messageBody) throws DocumentException {
				Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
				// 发送方账号
				String fromUserName = requestMap.get("FromUserName");
				// 开发者微信号
				String toUserName = requestMap.get("ToUserName");
				String respContent = "语音功能正在开发中，敬请期待！感谢您一直以来对网察的支持！";
				return replyTextMessage(fromUserName, toUserName, respContent);
			}

		});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_VIDEO, new ReplyHandler() {
			// 视频
			@Override
			public String reply(String messageBody) throws DocumentException {
				Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
				// 发送方账号
				String fromUserName = requestMap.get("FromUserName");
				// 开发者微信号
				String toUserName = requestMap.get("ToUserName");
				String respContent = "视频功能正在开发中，敬请期待！感谢您一直以来对网察的支持！";
				return replyTextMessage(fromUserName, toUserName, respContent);

			}

		});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_LOCATION, new ReplyHandler() {
			// 地理位置
			@Override
			public String reply(String messageBody) throws DocumentException {
				Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
				// 发送方账号
				String fromUserName = requestMap.get("FromUserName");
				// 开发者微信号
				String toUserName = requestMap.get("ToUserName");
				String respContent = "地理位置功能正在开发中，敬请期待！感谢您一直以来对网察的支持！";
				return replyTextMessage(fromUserName, toUserName, respContent);
			}

		});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_LINK, new ReplyHandler() {
			// 链接功能
			@Override
			public String reply(String messageBody) throws DocumentException {
				Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
				// 发送方账号
				String fromUserName = requestMap.get("FromUserName");
				// 开发者微信号
				String toUserName = requestMap.get("ToUserName");
				String respContent = "链接功能正在开发中，敬请期待！感谢您一直以来对网察的支持！";
				return replyTextMessage(fromUserName, toUserName, respContent);
			}

		});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_EVENT + WeixinMessageUtil.EVENT_TYPE_SUBSCRIBE,
				new ReplyHandler() {
					// 关注
					@Override
					public String reply(String messageBody) throws DocumentException {
						Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
						// 发送方账号
						String fromUserName = requestMap.get("FromUserName");
						// 开发者微信号
						String toUserName = requestMap.get("ToUserName");
						String respContent = WeixinMessageUtil.SUBSCRIBE_INFO;
						return replyTextMessage(fromUserName, toUserName, respContent);
					}

				});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_EVENT + WeixinMessageUtil.EVENT_TYPE_UNSUBSCRIBE,
				new ReplyHandler() {
					// 取消关注
					@Override
					public String reply(String messageBody) throws DocumentException {
						Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
						// 发送方账号
						String fromUserName = requestMap.get("FromUserName");
						// 开发者微信号
						String toUserName = requestMap.get("ToUserName");
						String respContent = "你已经取消【网察的公众号】\n";
						//IAlertAccountService accountService = SpringUtil.getBean(AlertAccountServiceImpl.class);
						List<AlertAccount> list =alertAccountService.findByAccount(fromUserName);
						alertAccountService.delete(list);
						return replyTextMessage(fromUserName, toUserName, respContent);
					}

				});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_EVENT + WeixinMessageUtil.EVENT_TYPE_SCAN, new ReplyHandler() {
			// 扫描二维码---目前只有绑定
			@Override
			public String reply(String messageBody) throws DocumentException {
			//	IAlertAccountService alertAccountService = SpringUtil.getBean(AlertAccountServiceImpl.class);
//				IUserService userService = SpringUtil.getBean(UserServiceImpl.class);
//				IOrganizationService organizationService = SpringUtil.getBean(OrganizationServiceImpl.class);
//				ISubGroupService subGroupService = SpringUtil.getBean(SubGroupServiceImpl.class);
				Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
				// 发送方账号
				String fromUserName = requestMap.get("FromUserName");
				// 开发者微信号
				String toUserName = requestMap.get("ToUserName");
				String ticket = requestMap.get("Ticket");
				String respContent = "你已经扫描【网察的公众号】\n";

				String string = RedisUtil.getString(ticket);
				QRCodeBind qrCodeBind = JSONObject.parseObject(string, QRCodeBind.class);
				String userName = qrCodeBind.getUserName();
				String userId = qrCodeBind.getUserId();
				//根据userId找originationId
				log.info(userId+"扫描获取到userID");
				User user = userService.findById(userId);
				String origanizationId = user.getOrganizationId();
					log.info("扫描获取到分组id"+origanizationId);
				try {

					String displayName = qrCodeBind.getDisplayName();
					String userWeixin = WeixinUtil.getUser(WeixinUtil.getToken(), fromUserName);
					JSONObject jsonObject = JSONObject.parseObject(userWeixin);
					log.error("jsonObject：" + jsonObject);
					log.error("displayName：" + displayName);
					log.error("userName：" + userName);

					AlertAccount alertAccount=null;
					//运维账号可重复绑定微信预警账号问题
					if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
						alertAccount=alertAccountService.findByAccountAndUserIdAndType(fromUserName, user.getId(), SendWay.WE_CHAT);
					}else if (UserUtils.isRoleOrdinary(user)){
						alertAccount=alertAccountService.findByAccountAndSubGroupIdAndType(fromUserName, user.getSubGroupId(), SendWay.WE_CHAT);
					}
					if (alertAccount == null) {
						int accountNumCan = 5;
						int accountNumHad = 0;
						if (UserUtils.ROLE_ADMIN.equals(user.getCheckRole())){
							//机构管理员
							Organization organization = organizationRepository.findOne(origanizationId);
							accountNumCan = organization.getAlertAccountNum();
						}else if (UserUtils.isRoleOrdinary(user)){
							//普通用户
							SubGroup subGroup = subGroupRepository.findOne(user.getSubGroupId());
							accountNumCan = subGroup.getAlertAccountNum();
						}
						accountNumHad = alertAccountService.getSubGroupAlertAccountCount(user, SendWay.WE_CHAT);
						//List<AlertAccount> list = alertAccountService.findByUserIdAndType(user.getId(),SendWay.WE_CHAT);
						//if (list != null && list.size() > 10) {
						if (accountNumHad >= accountNumCan) {
							//respContent = "该网察账号已经绑定够10个微信，请先解绑其他。";
							respContent = "该网察账号已经绑定够"+accountNumCan+"个微信，请先解绑其他。";
							return replyTextMessage(fromUserName, toUserName, respContent);
						}
						//绑定预警后把账号存到预警账号表
						alertAccount = new AlertAccount(jsonObject.getString("nickname"), "", SendWay.WE_CHAT, fromUserName, true, true, jsonObject.getString("headimgurl"),true,true,true,true,true);
						alertAccount.setUserId(userId);
						alertAccount.setOrganizationId(origanizationId);
						alertAccount.setSubGroupId(user.getSubGroupId());
						alertAccountService.add(alertAccount);
						respContent = WeixinMessageUtil.BIND_SUCCESS_INFO.replace("USERNAME", chooseString(userName));
					} else {
						respContent = WeixinMessageUtil.BIND_SUCCESS_INFO_OLD.replace("USERNAME",
								chooseString(userName));
					}
				} catch (Exception e) {
					AlertAccount alertAccount=alertAccountService.findByAccountAndUserIdAndType(fromUserName, user.getId(), SendWay.WE_CHAT);
					if (alertAccount != null) {
						alertAccountService.delete(alertAccount);
					}
					log.error("绑定微信出错：" , e);
					respContent = "系统出错，请联系管理员！";
				}
				RedisUtil.setString(ticket+String.valueOf(WeixinMessageUtil.WEIXIN_BIND_ALERT),"已扫描");
				return replyTextMessage(fromUserName, toUserName, respContent);
			}

		});
		//为登录而绑定
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_EVENT
				+ WeixinMessageUtil.EVENT_TYPE_SCAN + WeixinMessageUtil.QRCODE_TYPE_LOGIN_BIND,
				new ReplyHandler() {
					@Override
					public String reply(String messageBody) throws DocumentException {
						Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
						String fromUserName = requestMap.get("FromUserName");// 发送方帐号（open_id）
						String toUserName = requestMap.get("ToUserName");// 公众帐号
						String ticket = requestMap.get("Ticket");

						String respContent = null;
						List<Weixinlogin> findByOpenId = weixinLoginRepository.findByOpenId(fromUserName);
						if (findByOpenId == null || findByOpenId.size() == 0) {
							// 未绑定
//							respContent = WeixinMessageUtil.NOT_BIND_INFO
//									.replace("OPENID", fromUserName)
//									.replace("Ticket", ticket);
//							RedisUtil.setString(ticket+"userName", user.getUserName(), 1800, TimeUnit.SECONDS);
//							RedisUtil.setString(ticket+"userAccount", user.getUserAccount(), 1800, TimeUnit.SECONDS);
							//登录成功后存入redis的是username
							String userName = RedisUtil.getString(ticket+"userName");
							String subGroupId = RedisUtil.getString(ticket + "subGroupId");
							Weixinlogin weixinLogin = new Weixinlogin(fromUserName, RedisUtil.getString(ticket+"userId"),userName);
							if (StringUtil.isNotEmpty(subGroupId)){
								weixinLogin.setSubGroupId(subGroupId);
							}
//							weixinLogin.setOpenId(fromUserName);
////							weixinLogin.setOrganizationId(RedisUtil.getString(ticket+"userId"));
//							weixinLogin.setUserId(RedisUtil.getString(ticket+"userId"));
//							//登录成功后存入redis的是username
//							String userName = RedisUtil.getString(ticket+"userName");
//							weixinLogin.setUserAccount(userName);
							weixinLoginRepository.save(weixinLogin);
							respContent = "绑定成功";
						} else {//已绑定过  不能再次绑定
							respContent = "已绑定过";
						}
						return replyTextMessage(fromUserName, toUserName,
								respContent);
					}

				});
		//登录
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_EVENT
				+ WeixinMessageUtil.EVENT_TYPE_SCAN + WeixinMessageUtil.QRCODE_TYPE_LOGIN,
				new ReplyHandler() {
					@Override
					public String reply(String messageBody) throws DocumentException {
						Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
						String fromUserName = requestMap.get("FromUserName");// 发送方帐号（open_id）
						String toUserName = requestMap.get("ToUserName");// 公众帐号
						String ticket = requestMap.get("Ticket");

						String respContent = null;
						List<Weixinlogin> weixinLogin = weixinLoginRepository.findByOpenId(fromUserName);
						if (weixinLogin == null || weixinLogin.size() == 0) {
							// 未绑定
							//respContent = "未绑定";
							//2019-11-27 新的提示文案
							respContent = WeixinMessageUtil.NO_BIND_INFO;
						} else {//已绑定 去登陆
							//ip状态在redis里  其他在实体里
							String ip = RedisUtil.getString(ticket+"ip");
							Weixinlogin loginUser = weixinLogin.get(0);
							String userName = loginUser.getUserAccount();
							Subject currentUser = SecurityUtils.getSubject();
							Session session = currentUser.getSession();
							String sessionId = session.getId().toString();
							RedisUtil.setString(ticket+ WeixinMessageUtil.WEIXIN_USERNAME, userName);
							RedisUtil.setString(ticket+ WeixinMessageUtil.WEIXIN_IP, ip);
							RedisUtil.setString(ticket+ WeixinMessageUtil.WEIXIN_SESSIONID, sessionId);
							RedisUtil.setString(ticket+ WeixinMessageUtil.WEIXIN_TICKET, ticket);
							RedisUtil.setString(ticket+String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN),"已扫描");
							//对应为登录平台展示的分组名称，机构管理员展示为机构名称。  运维和超管为“网察大数据分析平台”
//							IUserService userService = SpringUtil.getBean(UserServiceImpl.class);
//							IOrganizationService organizationService = SpringUtil.getBean(OrganizationServiceImpl.class);
//							ISubGroupService subGroupService = SpringUtil.getBean(SubGroupServiceImpl.class);
							String platformInfo = "网察大数据分析平台";
							User user = userService.findByUserName(userName);
							//微信登录成功后， 提示文案 2019-12-30
							if (ObjectUtil.isNotEmpty(user)){
								if (UserUtils.isRoleOrdinary(user)){
									//普通用户 显示分组名称
									String subGroupId = user.getSubGroupId();
									if (StringUtil.isNotEmpty(subGroupId)){
										SubGroup subGroup = subGroupRepository.findOne(subGroupId);
										if (ObjectUtil.isNotEmpty(subGroup) && StringUtil.isNotEmpty(subGroup.getName())){
											platformInfo = subGroup.getName();
										}
									}
								}else if (UserUtils.isRoleAdmin(user) && StringUtil.isNotEmpty(user.getOrganizationId())){
									//机构管理员，显示 机构名称
									Organization organization = organizationRepository.findOne(user.getOrganizationId());
									if (ObjectUtil.isNotEmpty(organization) && StringUtil.isNotEmpty(organization.getOrganizationName())){
										platformInfo = organization.getOrganizationName();
									}
								}
							}
							respContent = WeixinMessageUtil.LOGIN_INFO.replace("PLATFORM_INFO",platformInfo);
//							respContent = "正在登录，请稍后";
//							respContent = WeixinMessageUtil.LOGIN_CONFIRM_INFO
//									.replace("USERNAME", userName)
//									.replace("IP", ip)
//									.replace("SESSIONID", sessionId)
//									.replace("TICKET", ticket);
						}
						return replyTextMessage(fromUserName, toUserName,
								respContent);
					}

				});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_EVENT + WeixinMessageUtil.EVENT_TYPE_TEMPLATESENDJOBFINISH,
				new ReplyHandler() {
					// 模板事件回调
					@Override
					public String reply(String messageBody) throws DocumentException {
						Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
						// 发送方账号
						String fromUserName = requestMap.get("FromUserName");
						// 开发者微信号
						String toUserName = requestMap.get("ToUserName");
						String status = requestMap.get("Status");
						log.error(fromUserName);
						log.error(toUserName);
						log.error(status);
						return replyTextMessage(fromUserName, toUserName, status);
					}

				});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_EVENT + WeixinMessageUtil.EVENT_TYPE_LOCATION,
				new ReplyHandler() {
					// 处理上报地理位置事件
					@Override
					public String reply(String messageBody) throws DocumentException {
						Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
						// 发送方账号
						String fromUserName = requestMap.get("FromUserName");
						// 开发者微信号
						String toUserName = requestMap.get("ToUserName");
						String respContent = "处理上报地理位置事件";
						return replyTextMessage(fromUserName, toUserName, respContent);
					}

				});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_EVENT + WeixinMessageUtil.EVENT_TYPE_CLICK
				+ WeixinMessageUtil.MENU_KEY_ALERT_MANAGE, new ReplyHandler() {
					// 预警管理
					@Override
					public String reply(String messageBody) throws DocumentException {
					//	IAlertAccountService alertAccountService = SpringUtil.getBean(AlertAccountServiceImpl.class);
//						IUserService userService = SpringUtil.getBean(UserServiceImpl.class);
						Environment env = SpringUtil.getBean(Environment.class);
						String netinsightUrl = env.getProperty(Const.NETINSIGHT_URL);
						log.error("netinsightUrl:" + netinsightUrl);
						Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
						// 发送方账号
						String fromUserName = requestMap.get("FromUserName");
						// 开发者微信号
						String toUserName = requestMap.get("ToUserName");
						String respContent = null;
						StringBuffer buffer = new StringBuffer();
						List<AlertAccount> list = alertAccountService.findByAccount(fromUserName);
						if (list != null && list.size() > 0) {
							for (AlertAccount alertAccount : list) {
								User user = userService.findById(alertAccount.getUserId());
								if(null!=user){//有可能账号被删了
									buffer.append(WeixinMessageUtil.ALERT_LIST_INFO
											.replace("DISPLAYNAME", chooseString(user.getDisplayName()))
											.replace("NETINSIGHT_URL", netinsightUrl)
											.replace("USERNAME", chooseString(user.getUserName()))
											.replace("OPENID", chooseString(alertAccount.getAccount()))
											.replace("ACTIVE", String.valueOf(!alertAccount.isActive()))
											.replace("HANYU", alertAccount.isActive() ? "停用预警" : "激活预警")).append("\n");
								}
							}
							respContent = buffer.toString();
							return replyTextMessage(fromUserName, toUserName, respContent);
						}
						respContent = "您没有绑定！";
						return replyTextMessage(fromUserName, toUserName, respContent);
					}

				});
		replyMap.put(WeixinMessageUtil.REQ_MESSAGE_TYPE_EVENT + WeixinMessageUtil.EVENT_TYPE_CLICK
				+ WeixinMessageUtil.MENU_KEY_ACCOUNT, new ReplyHandler() {
					// 绑定/解绑
					@Override
					public String reply(String messageBody) throws DocumentException {
//						IAlertAccountService alertAccountService = SpringUtil.getBean(AlertAccountServiceImpl.class);
//						IUserService userService = SpringUtil.getBean(UserServiceImpl.class);
						Environment env = SpringUtil.getBean(Environment.class);
						String netinsightUrl = env.getProperty(Const.NETINSIGHT_URL);
						log.error("netinsightUrl:" + netinsightUrl);
						Map<String, String> requestMap = WeixinMessageUtil.parseXml(messageBody);
						// 发送方账号
						String fromUserName = requestMap.get("FromUserName");
						// 开发者微信号
						String toUserName = requestMap.get("ToUserName");
						String respContent = "绑定/解绑";
						StringBuffer buffer = new StringBuffer();
						List<AlertAccount> list = alertAccountService.findByAccount(fromUserName);
						if (list != null && list.size() > 0) {

							for (AlertAccount alertAccount : list) {
								User user = userService.findById(alertAccount.getUserId());
								if(null!=user){//有可能账号被删了
									buffer.append(WeixinMessageUtil.BIND_LIST_INFO
											.replace("DISPLAYNAME", chooseString(user.getDisplayName()))
											.replace("NETINSIGHT_URL", netinsightUrl)
											.replace("USERNAME", chooseString(user.getUserName()))
											.replace("OPENID", chooseString(alertAccount.getAccount()))).append("\n");
								}
							}
							respContent = buffer.toString();
							return replyTextMessage(fromUserName, toUserName, respContent);
						}
						respContent = "您没有绑定！";
						return replyTextMessage(fromUserName, toUserName, respContent);
					}

				});

	}

	@Override
	public String processRequest(String messageBody) {
		this.init();
		log.error("init:");
		String generateKey = generateKey(messageBody);
		log.error("generateKey:" + generateKey);
		ReplyHandler replyHandler = replyMap.get(generateKey);
		log.error("replyHandler:" + replyHandler);
		if (replyHandler == null) {
			return null;
		}
		try {
			return replyHandler.reply(messageBody);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("提示微信信息失败:" , e);
			return null;
		}
	}

	/**
	 * 解析请求
	 * 
	 * @date Created at 2018年1月25日 下午5:28:31
	 * @Author 谷泽昊
	 * @param messageBody
	 * @return
	 */
	private String generateKey(String messageBody) {
		// xml请求解析
		Map<String, String> requestMap = null;
		try {
			requestMap = WeixinMessageUtil.parseXml(messageBody);
		} catch (DocumentException e) {
			log.error("解析出错：", e);
		}
		if (requestMap == null) {
			return null;
		}
		String msgType = requestMap.get("MsgType");// 消息类型
		String eventType = requestMap.get("Event");// 事件类型
		String eventKey = requestMap.get("EventKey");
		String ticket = requestMap.get("Ticket");
		String code = RedisUtil.getString(ticket);
		StringBuffer sb = new StringBuffer();
		if (!StringUtils.isEmpty(msgType)) {
			sb.append(msgType);
		}
		if (!StringUtils.isEmpty(eventType)) {
			sb.append(eventType);
		}
		if (StringUtils.equals(eventType, WeixinMessageUtil.EVENT_TYPE_CLICK) && !StringUtils.isEmpty(eventKey)) {
			sb.append(eventKey);
		}
		//登录和绑定
		if("0".equals(code)){
			sb.append("0");
		}else if("2".equals(code)){
			sb.append("2");
		}
		return sb.toString();
	}

	/**
	 * 
	 * 回复文本消息
	 * 
	 * @param fromUserName
	 * @param toUserName
	 * @param respContent
	 * @return
	 * @since FengWei @ 2014-1-3 下午2:09:13
	 */
	private String replyTextMessage(String fromUserName, String toUserName, String respContent) {
		TextMessage textMessage = new TextMessage();
		textMessage.setToUserName(fromUserName);
		textMessage.setFromUserName(toUserName);
		textMessage.setCreateTime(System.currentTimeMillis());
		textMessage.setMsgType(WeixinMessageUtil.RESP_MESSAGE_TYPE_TEXT);
		textMessage.setFuncFlag(0);
		textMessage.setContent(respContent);
		return WeixinMessageUtil.messageToXml(textMessage);
	}

	/**
	 * 把null 改成""
	 * 
	 * @date Created at 2018年1月29日 下午3:25:37
	 * @Author 谷泽昊
	 * @param str
	 * @return
	 */
	private String chooseString(String str) {
		if (StringUtils.isNotBlank(str)) {
			return str;
		}
		return "";
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