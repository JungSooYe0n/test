/*
 * Project: netInsight
 * 
 * File Created at 2018年2月1日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.notice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.result.Message;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.*;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.entity.repository.AlertBackupsRepository;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.alert.service.IAlertAccountService;
import com.trs.netInsight.widget.alert.service.IAlertSendService;
import com.trs.netInsight.widget.alert.service.ISendAlertService;
import com.trs.netInsight.widget.notice.service.IMailSendService;
import com.trs.netInsight.widget.notice.service.INoticeSendService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IUserService;
import com.trs.netInsight.widget.weixin.entity.AlertTemplateMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @Type NoticeSendServiceImpl.java
 * @author 谷泽昊
 * @date 2018年2月1日 下午1:57:28
 * @version
 */
@Service
@Slf4j
public class NoticeSendServiceImpl implements INoticeSendService {

	@Autowired
	private IMailSendService mailSend;

	@Autowired
	private ISendAlertService sendAlertService;
	/**
	 * app
	 */
	@Autowired
	private IAlertSendService alertSendService;

	@Autowired
	private IAlertAccountService alertAccountService;

	@Autowired
	private IUserService userService;

	@Autowired
	private AlertRepository alertRepository;

	@Autowired
	private AlertBackupsRepository backupsRepository;

//	@Autowired
//	private

	private SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
	
	@Value("${http.client}")
	private boolean httpClient;
	
	@Value("${http.alert.netinsight.url}")
	private String alertNetinsightUrl;

	/**
	 * 发送预警
	 * 
	 * @param send
	 *            发送方式
	 * @param map
	 *            往notice传递的参数
	 * @return
	 */
	@Override
	public Message sendAll(SendWay send, String template, String subject, Map<String, Object> map, String receivers,
			String userId,AlertSource sendType) {
		try {
			User user_ = userService.findById(userId);
			String userName = null;
			if (user_ != null) {
				userName = user_.getUserName();
			}
			@SuppressWarnings("unchecked")
			List<Map<String, String>> list = (List<Map<String, String>>) map.get("listMap");
			int size = (int) map.get("size");
			switch (send) {
			case EMAIL:
				User findByUserName = userService.findByUserName(receivers);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				if (findByUserName != null) {
					//计算今天与到期时间差多少
					String expireAt = findByUserName.getExpireAt();
					int n = 1;
					if(!UserUtils.FOREVER_DATE.equals(expireAt)){
						n = DateUtil.rangBetweenNow(sdf.parse(expireAt));
					}
					if(n>0){
						List<AlertAccount> userList = alertAccountService.findByUserIdAndType(findByUserName.getId(),
								SendWay.EMAIL);
						List<String> emailList = new ArrayList<>();
						for (AlertAccount account : userList) {
							emailList.add(account.getAccount());
						}
						if (ObjectUtil.isNotEmpty(emailList)) {
							String rece = String.join(";", emailList);
							String ids = this.addAlertOrAlertBackups(true, list, rece, SendWay.EMAIL, userId,"send");
//							map.put("userName", findByUserName.getUserName());
							map.put("userName", userName);
//							return mailSend.sendEmail(template, subject, JSONObject.toJSONString(map), rece);
							return mailSend.sendEmail(template, subject, map, rece);
						}
					}
				}else{//直接传的邮箱
					String ids = this.addAlertOrAlertBackups(true, list, receivers, SendWay.EMAIL, userId,"send");
					map.put("userName", userName);
					return mailSend.sendEmail(template, subject, map, receivers);
				}

				return Message.getMessage(CodeUtils.FAIL, "没有账号！", null);
			case SMS:// 站内
				String[] split = receivers.split(";");
				for (String receiver : split) {
					String ids = this.addAlertOrAlertBackups(true, list, receiver, SendWay.SMS, userId,"send");
					//按文件存储  每个用户只能看到自己userid对应下的文件  所以在用接受者的userid存一遍 所发出的站内预警才能被接受者看到
					User user = userService.findByUserName(receivers);
//					//接受者id和发送者id不一致时
					if(user!=null){
						if(!user.getId().equals(userId)){
							String saveOther = this.addAlertOrAlertBackups(true, list, receiver, SendWay.SMS, user.getId(),"receive");
						}else{//一致时  我是发送方也是接收方
							String saveOther = this.addAlertOrAlertBackups(true, list, receiver, SendWay.SMS, userId,"receive");
						}
					}
					
//					AlertSendWeChat sendAlert = new AlertSendWeChat(ids, subject, DateUtil.formatCurrentTime(DateUtil.yyyyMMdd),
//							SendWay.SMS, size);
//					sendAlert.setCreatedUserId(userId);
//					AlertSendWeChat add = sendAlertService.add(sendAlert);
//					messagingTemplate.convertAndSendToUser(receiver, "/topic/greetings", add);
				}
				return Message.getMessage(CodeUtils.SUCCESS, "发送成功！", null);

			case WE_CHAT:
				List<Map<String, String>> listWeiChat = new ArrayList<>();
				if (list != null && list.size() > 5) {
					listWeiChat = list.subList(0, 5);
				} else {
					listWeiChat = list;
				}
				/*List<List<Map<String, String>>> listWeiChats = new ArrayList<>();
				List<List<String>> messageLists = new ArrayList<>();
				if (list != null && list.size() > 5) {
					listWeiChats.add(list.subList(0, 5));
					listWeiChats.add(list.subList(5,list.size()));
				} else {
					listWeiChats.add(list);
				}
				int i = 0;*/
				//for(List<Map<String, String>> listWeiChat :listWeiChats){

					String alertTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMdd);
					AlertSendWeChat sendAlert = new AlertSendWeChat(null, subject, alertTime, SendWay.WE_CHAT, list.size());
					sendAlert.setCreatedUserId(userId);
					String id = UUID.randomUUID().toString();

//				AlertSendWeChat add = sendAlertService.add(sendAlert);
//				AlertSendWeChat add = sendAlertService.add(sendAlert);
//				String id = add.getId();
					List<String> messageList = new ArrayList<>();
					// 通过用户名
					User findByUserNameWE_CHAT = userService.findByUserName(receivers);
					Environment env = SpringUtil.getBean(Environment.class);
					String netinsightUrl = env.getProperty(Const.NETINSIGHT_URL);
					//json命名，为了方便快速的查找。
					SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
					SimpleDateFormat df2 = new SimpleDateFormat("HH");//设置日期格式
					Date nowDate = new Date();
					String format = df1.format(nowDate);
					int hour = Integer.parseInt(df2.format(nowDate));
					String amOrPm = "am";
					if(hour>=12){
						amOrPm = "pm";
					}
					sendAlert.setId(id+"+"+format+"+"+amOrPm);
					String alertDetailUrl = WeixinMessageUtil.ALERT_DETAILS_URL.replaceAll("ID","")
							.replace("NETINSIGHT_URL", netinsightUrl)+id+"+"+format+"+"+amOrPm;

					if(subject==null){
						subject="";
					}
					if(userName==null){
						userName="";
					}
					String alertTitle = WeixinMessageUtil.ALERT_TITLE.replace("SUBJECT", subject)
							.replace("SIZE", String.valueOf(size)).replace("USERNAME",
									userName);
					List<AlertAccount> accountList = new ArrayList<>();
					if (findByUserNameWE_CHAT != null) {
//					List<AlertAccount> receiveList = alertAccountService
//							.findByUserIdAndType(findByUserNameWE_CHAT.getId(), SendWay.WE_CHAT);
						accountList = alertAccountService.findByUserIdAndType(findByUserNameWE_CHAT.getId(), SendWay.WE_CHAT);
					}else{//直接传的微信号   这个人没停止预警  就发  停止预警就不发
						//通过微信号查alertaccount中的active  true发  false不发
//					List<AlertAccount> accountList = alertAccountService.findByAccount(receivers);
						AlertAccount alertaccount = alertAccountService.findByAccountAndUserIdAndType(receivers,userId, SendWay.WE_CHAT);
						accountList.add(alertaccount);
					}
//				if (accountList != null && accountList.size() > 0) {
					for (AlertAccount alertAccount : accountList) {
						if (alertAccount != null) {
							if (alertAccount.isActive()) {
								log.error("netinsightUrl:" + netinsightUrl);
								AlertTemplateMsg alertTemplateMsg = new AlertTemplateMsg(alertAccount.getAccount(),
										alertDetailUrl,alertTitle,StringUtil.toString(listWeiChat), alertTime, "");

								String sendWeixin = WeixinUtil.sendWeixin(WeixinUtil.getToken(), alertTemplateMsg);

								log.error("发送微信返回值：" + sendWeixin);
								if (StringUtils.equals("ok", sendWeixin)) {
									messageList.add(alertAccount.getName() + "：发送成功！");
								} else {
									messageList.add(alertAccount.getName() + "：发送失败！");
								}
							} else {
								messageList.add(alertAccount.getName() + "：关闭了预警！");
							}
							String alertIds = this.addAlertOrAlertBackups(true, list, receivers, SendWay.WE_CHAT,
									userId+"+"+format+"+"+amOrPm,"send");
//							add.setIds(alertIds);
							sendAlert.setIds(alertIds);
							//直接存文件不编辑了
							sendAlertService.add(sendAlert);
//							sendAlertService.edit(add);
						} else {
							messageList.add(receivers + "：没有查询到微信号！");
						}
						log.error("微信推送！预警名称："+subject+"接收人："+receivers+"接收人id："+alertAccount.getAccount());
					}
//				}
					log.error("微信推送循环外！预警名称："+subject+"接收人："+receivers);
				//}
				return Message.getMessage(CodeUtils.SUCCESS, "发送成功！", messageList);
				case APP:// 安卓端
					String appAlertTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMdd);
					//手动
					User byUserNameAPP = userService.findByUserName(receivers);
					String nameAPPId = byUserNameAPP.getId();
					AlertSend alertSend = new AlertSend(null, subject, appAlertTime,SendWay.APP,sendType, nameAPPId,size);
					alertSend.setCreatedUserId(userId);
					String appAlertId = UUID.randomUUID().toString().replaceAll("-", "");
					alertSend.setId(appAlertId);

					if (ObjectUtil.isEmpty(byUserNameAPP)){
						throw new OperationException("请选择APP端预警接收人！");
					}

					Map<String, String> mapData = new HashMap<>();
					String alertMessage = JPushMessageUtil.ALERT_MESSAGE.replace("SUBJECT", subject).replace("SIZE", String.valueOf(size)).replace("USERNAME", userName);
					mapData.put("type","预警");
					mapData.put("title",alertMessage);
					mapData.put("alertId",alertSend.getId());
					mapData.put("receviceUserid",nameAPPId);
					Message message = null;
					//try住后 APP端不登录时，收不到提醒，但会查到预警信息
					try {
						message = JPushClientUtil.SendPush(mapData);
						log.error("APP预警手动推送成功！预警名称："+subject+"接收人："+receivers+"接收人id："+nameAPPId);
					} catch (Exception e) {
						log.error("APP预警手动推送出错！预警名称："+subject+"接收人："+receivers+"接收人id："+nameAPPId,e);
					}
					String alertAppIds = this.addAlertOrAlertBackups(true, list, receivers, SendWay.APP,
							userId,"send");
					alertSend.setIds(alertAppIds);
					//直接存文件不编辑了
					alertSendService.add(alertSend);
					return message;

			default:
				return Message.getMessage(CodeUtils.FAIL, "发送方式不明确！", null);
			}
		} catch (Exception e) {
			log.error("发送报错", e);
		}
		return Message.getMessage(CodeUtils.FAIL, "发送方式不明确！", send);

	}

	/**
	 * 判断保存到那个表
	 * 
	 * @date Created at 2018年3月14日 下午3:45:01
	 * @Author 谷泽昊
	 * @param choose
	 * @param list
	 * @param receiver
	 * @param sendWay
	 * @param userId
	 * @throws OperationException 
	 */
	private String addAlertOrAlertBackups(boolean choose, List<Map<String, String>> list, String receiver,
			SendWay sendWay, String userId,String sendOrreceive) throws OperationException {
		StringBuffer buffer = new StringBuffer();
		if (choose) {
			for (Map<String, String> each : list) {
				String title = StringUtil.replaceNRT(StringUtil.removeFourChar(each.get("title")));
				title = StringUtil.replaceEmoji(title);
				String content = each.get("content");

				if (!SendWay.APP.equals(sendWay)){
					content = StringUtil.removeFourChar(content);
					content =StringUtil.replaceEmoji(content);
					content =StringUtil.replaceImg(content) ;
					System.err.println("content"+content);
				}
				each.put("content", content);
				each.put("title", title);
				AlertEntity alert = new AlertEntity();
//				Map<String,Object> map = new HashMap<String,Object>();
				alert.setTrslk(each.get("trslk") == null ? "" : each.get("trslk"));
				alert.setUrlName(each.get("url"));
				alert.setTitle(title);
				alert.setGroupName(each.get("groupName"));
				alert.setSid(each.get("sid"));
				alert.setSendWay(sendWay);
				each.put("sendWay", String.valueOf(sendWay));
				alert.setReceiver(receiver);
				alert.setAuthor(each.get("author"));
				each.put("receiver", receiver);
				each.put("sendOrreceive", sendOrreceive);
				alert.setAppraise(each.get("appraise"));
				alert.setSiteName(each.get("siteName"));
				alert.setNreserved1(each.get("nreserved1"));
				alert.setKeywords(each.get("keywords"));
				String retweetedMid = each.get("retweetedMid");
				if (null == retweetedMid){
					retweetedMid = "0";
				}
				alert.setRetweetedMid(retweetedMid);
				alert.setMd5tag(each.get("md5"));
				alert.setContent(content);
				if(StringUtil.isNotEmpty(each.get("screenName"))){
					alert.setScreenName(each.get("screenName"));
				}else{
					alert.setScreenName(each.get("author"));
				}
				String rtString = each.get("rttCount");
				if (StringUtil.isNotEmpty(rtString)) {
					alert.setRttCount(Long.valueOf(each.get("rttCount")));
				} else {
					alert.setRttCount(0);
				}
				String commtCount = each.get("commtCount");
				if (StringUtil.isNotEmpty(commtCount)) {
					alert.setCommtCount(Long.valueOf(each.get("commtCount")));
				} else {
					alert.setCommtCount(0);
				}
				each.put("commtCount", String.valueOf(alert.getCommtCount()));
				try {
					alert.setTime(sdf.parse(each.get("urlTime")));
				} catch (ParseException e) {
					log.error("时间转换出错或者Hybase中时间为空");
					alert.setTime(new Date());
					e.printStackTrace();
				}
//				each.put("time", each.get("urlTime"));
				alert.setUserId(userId);
				each.put("userId", userId);
				alert.setAlertRuleBackupsId(each.get("alertRuleBackupsId"));
				alert.setOrganizationId(each.get("organizationId"));
				alert.setFlag(false);
				alert.setImageUrl(each.get("imageUrl"));
				each.put("md5tag", each.get("md5"));
				each.put("urlName", each.get("url"));
				alert.setTitleWhole(each.get("titleWhole"));
//				each.put("createdUserId", userId);
				if(httpClient){
					String url = alertNetinsightUrl+"/alert/add";
					String doPost = HttpUtil.doPost(url, each, "utf-8");
					//json转实体
					ObjectMapper om = new ObjectMapper();
					AlertEntity readValue = null;;
					try {
						 //json转实体
						 readValue = om.readValue(doPost, AlertEntity.class);
					} catch (IOException e) {
						throw new OperationException("预警查找报错", e);
					}
					String id = readValue.getId();
					buffer.append(id).append(";");
				}else{
					User user = userService.findByUserName(receiver);
					String userIds = UserUtils.getUser().getId();
					if(user != null){
						if(user.getId().equals(userIds)){
							if("receive".equals(sendOrreceive)){
								return  null;
							}
						}
					}
					AlertEntity save = alertRepository.save(alert);
					String id = save.getId();
					buffer.append(id).append(";");
				}
			}
			return buffer.toString();
		} else {
			List<AlertBackups> alertBackupsList = new ArrayList<>();
			for (Map<String, String> each : list) {
				String content =each.get("content");
				if (!SendWay.APP.equals(sendWay)){
					content = StringUtil.removeFourChar(content);
					content =StringUtil.replaceEmoji(content);
					content =StringUtil.replaceImg(content) ;
					System.err.println("content"+content);
				}
				String title = StringUtil.replaceNRT(StringUtil.removeFourChar(each.get("title")));
				AlertBackups backups = new AlertBackups();
				//backups.setTrslk(each.get("trslk"));
				backups.setUrlName(each.get("url"));
				backups.setAuthor(each.get("author"));
				backups.setTitle(title);
				backups.setGroupName(each.get("groupName"));
				backups.setSid(each.get("sid"));
				backups.setSendWay(SendWay.SMS);
				backups.setReceiver(receiver);
				backups.setNreserved1(each.get("nreserved1"));
				backups.setAppraise(each.get("appraise"));
				backups.setSiteName(each.get("siteName"));
				backups.setMd5tag(each.get("md5"));
				backups.setContent(content);
				backups.setScreenName(each.get("screenName"));
				String rtString = each.get("rttCount");
				backups.setImageUrl(each.get("imageUrl"));
				if (StringUtil.isNotEmpty(rtString)) {
					backups.setRttCount(Long.valueOf(each.get("rttCount")));
				} else {
					backups.setRttCount(0);
				}
				String commtCount = each.get("commtCount");
				if (StringUtil.isNotEmpty(commtCount)) {
					backups.setCommtCount(Long.valueOf(each.get("commtCount")));
				} else {
					backups.setCommtCount(0);
				}
				try {
					backups.setTime(sdf.parse(each.get("urlTime")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				backups.setOrganizationId(each.get("organizationId"));
				backups.setAlertRuleBackupsId(each.get("alertRuleBackupsId"));
				backups.setUserId(userId);
				alertBackupsList.add(backups);
			}
			backupsRepository.save(alertBackupsList);
			return buffer.toString();
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
 * 2018年2月1日 谷泽昊 creat
 */