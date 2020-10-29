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

import com.trs.hybase.client.TRSException;
import com.trs.hybase.client.TRSInputRecord;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.result.Message;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.*;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.entity.repository.AlertAccountRepository;
import com.trs.netInsight.widget.notice.service.IMailSendService;
import com.trs.netInsight.widget.notice.service.INoticeSendService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import com.trs.netInsight.widget.weixin.entity.AlertTemplateMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @Type NoticeSendServiceImpl.java
 * @author
 * @date
 * @version
 */
@Service
@Slf4j
public class NoticeSendServiceImpl implements INoticeSendService {

	@Autowired
	private IMailSendService mailSend;

	@Autowired
	private AlertAccountRepository alertAccountRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private FullTextSearch hybase8SearchServiceNew;

	private SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);

	@Value("${netinsight.url}")
	private String netinsightUrl;

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
                           String userId, AlertSource sendType) {
		try {
			User user_ = userRepository.findOne(userId);
			String userName = null;
			if (user_ != null) {
				userName = user_.getUserName();
			}
			@SuppressWarnings("unchecked")
			List<Map<String, String>> list = (List<Map<String, String>>) map.get("listMap");
			int size = (int) map.get("size");
			log.error("receivers信息："+receivers+"，sendAll传入userId："+userId);
			User findByUserName = null;
			try {
				findByUserName = this.findByUserName(receivers);
			} catch (Exception e) {
				log.error("findByUserName查询失败！receivers信息是："+receivers+"findById查询所得userName为："+userName);
			}
			switch (send) {
			case EMAIL:
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String emailNetinsightUrl = netinsightUrl;
				int index = emailNetinsightUrl.lastIndexOf("/netInsight");
				emailNetinsightUrl = emailNetinsightUrl.substring(0,index);
				map.put("url", emailNetinsightUrl);
				if (findByUserName != null) {
					//计算今天与到期时间差多少
					String expireAt = findByUserName.getExpireAt();
					int n = 1;
					if(!UserUtils.FOREVER_DATE.equals(expireAt)){
						n = DateUtil.rangBetweenNow(sdf.parse(expireAt));
					}
					if(n>0){
						List<AlertAccount> userList = alertAccountRepository.findByUserIdAndType(findByUserName.getId(),
								SendWay.EMAIL);
						List<String> emailList = new ArrayList<>();
						for (AlertAccount account : userList) {
							emailList.add(account.getAccount());
						}
						if (ObjectUtil.isNotEmpty(emailList)) {
							String rece = String.join(";", emailList);
							String ids = this.addAlertOrAlertBackups( list, rece, SendWay.EMAIL, userId,"send");
							map.put("userName", userName);
							return mailSend.sendEmail(template, subject, map, rece);
						}
					}
				}else{//直接传的邮箱
					String ids = this.addAlertOrAlertBackups( list, receivers, SendWay.EMAIL, userId,"send");
					map.put("userName", userName);
					return mailSend.sendEmail(template, subject, map, receivers);
				}

				return Message.getMessage(CodeUtils.FAIL, "没有账号！", null);
			case SMS:// 站内
				String[] split = receivers.split(";");
				for (String receiver : split) {
					String ids = this.addAlertOrAlertBackups( list, receiver, SendWay.SMS, userId,"send");
					//按文件存储  每个用户只能看到自己userid对应下的文件  所以在用接受者的userid存一遍 所发出的站内预警才能被接受者看到
					User user = findByUserName(receivers);
//					//接受者id和发送者id不一致时
					if(user!=null){
						if(!user.getId().equals(userId)){
							String saveOther = this.addAlertOrAlertBackups( list, receiver, SendWay.SMS, user.getId(),"receive");
						}else{//一致时  我是发送方也是接收方
							String saveOther = this.addAlertOrAlertBackups( list, receiver, SendWay.SMS, userId,"receive");
						}
					}
				}
				return Message.getMessage(CodeUtils.SUCCESS, "发送成功！", null);

			case WE_CHAT:
				List<List<Map<String, String>>> listWeiChats = new ArrayList<>();
				List<List<String>> messageLists = new ArrayList<>();
				for(int i = 0;i<list.size();i+=5){
					if(i+5<list.size()){
						listWeiChats.add(list.subList(i,i+5));
					}else {
						listWeiChats.add(list.subList(i,list.size()));
					}
				}
				int i = 1;
				for(List<Map<String, String>> listWeiChat :listWeiChats){
					String alertTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMdd);
					String id = UUID.randomUUID().toString();
					TRSInputRecord record = new TRSInputRecord();
					record.addColumn(FtsFieldConst.FIELD_ALERT_TYPE_ID,id);
					record.addColumn(FtsFieldConst.FIELD_ALERT_NAME,subject);
					record.addColumn(FtsFieldConst.FIELD_ALERT_TIME,alertTime);
					record.addColumn(FtsFieldConst.FIELD_SEND_WAY,SendWay.WE_CHAT.toString());
					record.addColumn(FtsFieldConst.FIELD_SIZE,listWeiChat.size());
					record.addColumn(FtsFieldConst.FIELD_USER_ID,userId);
					record.addColumn(FtsFieldConst.FIELD_RECEIVER,receivers);

					List<String> messageList = new ArrayList<>();

					Environment env = SpringUtil.getBean(Environment.class);
					String netinsightUrl = env.getProperty(Const.NETINSIGHT_URL);

					String alertDetailUrl = WeixinMessageUtil.ALERT_DETAILS_URL.replaceAll("ID","")
							.replace("NETINSIGHT_URL", netinsightUrl)+id;

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
					if (findByUserName != null) {
						accountList = alertAccountRepository.findByUserIdAndType(findByUserName.getId(), SendWay.WE_CHAT);
					}else{//直接传的微信号   这个人没停止预警  就发  停止预警就不发
						//通过微信号查alertaccount中的active  true发  false不发
						List<AlertAccount> byAccountAndUserIdAndType = alertAccountRepository.findByAccountAndUserIdAndType(receivers, userId, SendWay.WE_CHAT);
						AlertAccount alertaccount = null;
						if (list != null && byAccountAndUserIdAndType.size() > 0) {
							alertaccount =  byAccountAndUserIdAndType.get(0);
						}
						accountList.add(alertaccount);
					}
					for (AlertAccount alertAccount : accountList) {
						if (alertAccount != null) {
							String alertIds = this.addAlertOrAlertBackups( listWeiChat, receivers, SendWay.WE_CHAT,
									userId,"send");
							record.addColumn(FtsFieldConst.FIELD_ALERT_IDS,alertIds);
							//直接存文件不编辑了
//							存入hybase system2.alert_type库
							hybase8SearchServiceNew.insertRecords(record,Const.ALERTTYPE,true,null);
							if (alertAccount.isActive()) {
								log.error("netinsightUrl:" + netinsightUrl);
								AlertTemplateMsg alertTemplateMsg = new AlertTemplateMsg(alertAccount.getAccount(),
										alertDetailUrl,alertTitle, StringUtil.toString(listWeiChat,i), alertTime, "");

								String sendWeixin =  null;
								try {
									sendWeixin = WeixinUtil.sendWeixin(WeixinUtil.getToken(), alertTemplateMsg);
									log.error("微信预警自动推送成功！预警名称："+subject+"接收人："+receivers+"预警账号："+alertAccount.getAccount());
								} catch (Exception e) {
									log.error("微信预警自动推送失败！预警名称："+subject+"接收人："+receivers+"预警账号："+alertAccount.getAccount());
									e.printStackTrace();
								}
								log.error("发送微信返回值：" + sendWeixin);
								if (StringUtils.equals("ok", sendWeixin)) {
									messageList.add(alertAccount.getName() + "：发送成功！");
								} else {
									messageList.add(alertAccount.getName() + "：发送失败！");
								}
							} else {
								messageList.add(alertAccount.getName() + "：关闭了预警！");
							}

						} else {
							messageList.add(receivers + "：没有查询到微信号！");
						}
						log.error("微信推送！预警名称："+subject+"接收人："+receivers+"接收人id："+alertAccount.getAccount());
					}
					log.error("微信推送循环外！预警名称："+subject+"接收人："+receivers);
					messageLists.add(messageList);
					i = i+listWeiChat.size();
				}
				return Message.getMessage(CodeUtils.SUCCESS, "发送成功！", messageLists);
				case APP:// 安卓端
					String appAlertTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMdd);
					//手动
					User byUserNameAPP = this.findByUserName(receivers);
					String nameAPPId = byUserNameAPP.getId();
					String appAlertId = UUID.randomUUID().toString().replaceAll("-", "");
					TRSInputRecord record = new TRSInputRecord();
					record.addColumn(FtsFieldConst.FIELD_ALERT_TYPE_ID,appAlertId);
					record.addColumn(FtsFieldConst.FIELD_ALERT_NAME,subject);
					record.addColumn(FtsFieldConst.FIELD_ALERT_TIME,appAlertTime);
					record.addColumn(FtsFieldConst.FIELD_SEND_WAY,SendWay.APP.toString());
					record.addColumn(FtsFieldConst.FIELD_SIZE,size);
					record.addColumn(FtsFieldConst.FIELD_USER_ID,userId);
					record.addColumn(FtsFieldConst.FIELD_RECEIVER,receivers);
					record.addColumn(FtsFieldConst.FIELD_ALERT_SOURCE,sendType.toString());

					if (ObjectUtil.isEmpty(byUserNameAPP)){
						throw new OperationException("请选择APP端预警接收人！");
					}

					Map<String, String> mapData = new HashMap<>();
					String alertMessage = JPushMessageUtil.ALERT_MESSAGE.replace("SUBJECT", subject).replace("SIZE", String.valueOf(size)).replace("USERNAME", userName);
					mapData.put("type","预警");
					mapData.put("title",alertMessage);
					mapData.put("alertId",appAlertId);
					mapData.put("receviceUserid",nameAPPId);
					Message message = null;
					//try住后 APP端不登录时，收不到提醒，但会查到预警信息
					try {
						message = JPushClientUtil.SendPush(mapData);
						log.error("APP预警手动推送成功！预警名称："+subject+"接收人："+receivers+"接收人id："+nameAPPId);
					} catch (Exception e) {
						log.error("APP预警手动推送出错！预警名称："+subject+"接收人："+receivers+"接收人id："+nameAPPId,e);
					}
					String alertAppIds = this.addAlertOrAlertBackups( list, receivers, SendWay.APP,
							userId,"send");
					record.addColumn(FtsFieldConst.FIELD_ALERT_IDS,alertAppIds);
					//直接存文件不编辑了
					hybase8SearchServiceNew.insertRecords(record,Const.ALERTTYPE,true,null);
					return message;

				default:
				return Message.getMessage(CodeUtils.FAIL, "发送方式不明确！", null);
			}
		} catch (Exception e) {
			log.error("发送报错", e);
			e.printStackTrace();
		}
		return Message.getMessage(CodeUtils.FAIL, "发送方式不明确！", send);

	}

	public User findByUserName(String userName) {
		List<User> list = userRepository.findByUserName(userName);
		if (list != null && list.size() > 0) {
			User user = list.get(0);
			String organizationId = user.getOrganizationId();
			if (StringUtils.isNotBlank(organizationId)) {
				Organization organization = organizationRepository.findOne(user.getOrganizationId());
				if (organization != null) {
					user.setOrganizationName(organization.getOrganizationName());
				}
			}
			return user;
		}
		return null;
	}

	/**
	 * 判断保存到那个表
	 *
	 * @param list
	 * @param receiver
	 * @param sendWay
	 * @param userId
	 * @throws OperationException
	 * @date Created at 2018年3月14日 下午3:45:01
	 * @Author 谷泽昊
	 */
	private String addAlertOrAlertBackups( List<Map<String, String>> list, String receiver,
										  SendWay sendWay, String userId, String sendOrreceive) throws OperationException {
		StringBuffer buffer = new StringBuffer();
		User user = userRepository.findOne(userId);

			//			批量添加预警记录 至  hybase库
			List<TRSInputRecord> trsInputRecords = new ArrayList<>();
			for (Map<String, String> each : list) {
				String title = StringUtil.replaceNRT(StringUtil.removeFourChar(each.get("title")));
				title = StringUtil.replaceEmoji(title);
				String content = each.get("content");

				if (!SendWay.APP.equals(sendWay)) {
					content = StringUtil.removeFourChar(content);
					content = StringUtil.replaceEmoji(content);
					content = StringUtil.replaceImg(content);
				}
				TRSInputRecord record = new TRSInputRecord();
				try {
					record.addColumn(FtsFieldConst.FIELD_URLNAME, each.get("url"));
					record.addColumn(FtsFieldConst.FIELD_URLTITLE, title);
					record.addColumn(FtsFieldConst.FIELD_GROUPNAME, each.get("groupName"));
					record.addColumn(FtsFieldConst.FIELD_SID, each.get("sid"));
					record.addColumn(FtsFieldConst.FIELD_SEND_WAY, sendWay.toString());
					record.addColumn(FtsFieldConst.FIELD_RECEIVER, receiver);
					record.addColumn(FtsFieldConst.FIELD_APPRAISE, each.get("appraise"));
					record.addColumn(FtsFieldConst.FIELD_SITENAME, each.get("siteName"));
					record.addColumn(FtsFieldConst.FIELD_NRESERVED1, each.get("nreserved1"));
					record.addColumn(FtsFieldConst.FIELD_KEYWORDS, each.get("keywords"));
					record.addColumn(FtsFieldConst.FIELD_SEND_RECEIVE, sendOrreceive);
					String retweetedMid = each.get("retweetedMid");
					if (null == retweetedMid) {
						retweetedMid = "0";
					}
					record.addColumn(FtsFieldConst.IR_RETWEETED_MID, retweetedMid);
					record.addColumn(FtsFieldConst.FIELD_MD5TAG, each.get("md5"));
					record.addColumn(FtsFieldConst.FIELD_CONTENT, content);
					String scrName = "";
					if (StringUtil.isNotEmpty(each.get("screenName"))) {
						scrName = each.get("screenName");
					} else {
						scrName = each.get("author");
					}
					record.addColumn(FtsFieldConst.FIELD_SCREEN_NAME, scrName);
					String rtString = each.get("rttCount");
					if (StringUtil.isNotEmpty(rtString)) {
						record.addColumn(FtsFieldConst.FIELD_RTTCOUNT, Long.valueOf(each.get("rttCount")));
					} else {
						record.addColumn(FtsFieldConst.FIELD_RTTCOUNT, 0);
					}
					String commtCount = each.get("commtCount");
					if (StringUtil.isNotEmpty(commtCount)) {
						record.addColumn(FtsFieldConst.FIELD_COMMTCOUNT, Long.valueOf(each.get("commtCount")));
					} else {
						record.addColumn(FtsFieldConst.FIELD_COMMTCOUNT, 0);
					}
					try {
						record.addColumn(FtsFieldConst.FIELD_URLTIME, sdf.parse(each.get("urlTime")));
					} catch (ParseException e) {
						log.error("时间转换出错或者Hybase中时间为空");
						record.addColumn(FtsFieldConst.FIELD_URLTIME, new Date());
						e.printStackTrace();
					}
					if (ObjectUtil.isNotEmpty(user)) {
						record.addColumn(FtsFieldConst.FIELD_SubGroup_ID, user.getSubGroupId());
					}
					record.addColumn(FtsFieldConst.FIELD_USER_ID, userId);
					record.addColumn(FtsFieldConst.FIELD_ORGANIZATION_ID, each.get("organizationId"));
					String alertId = UUID.randomUUID().toString();
					record.addColumn(FtsFieldConst.FIELD_ALERT_ID, alertId);
					record.addColumn(FtsFieldConst.FIELD_IMAGE_URL, each.get("imageUrl"));
					record.addColumn(FtsFieldConst.FIELD_URLTITLE_WHOLE, each.get("titleWhole"));

					trsInputRecords.add(record);
					buffer.append(alertId).append(";");

				} catch (TRSException e) {
					log.error("预警记录record.addColumn出错：", e);
					e.printStackTrace();
				}
			}

			try {
				hybase8SearchServiceNew.insertRecords(trsInputRecords, Const.ALERT, true, null);
			} catch (com.trs.netInsight.handler.exception.TRSException e) {
				log.error("批量添加预警记录至 hybase 出错：用户id：" + userId + "，该次预警为手动预警，预警内容id：" + buffer.toString(), e);
				e.printStackTrace();
			}
			return buffer.toString();

	}

	/**
	 * 发送预警信息
	 *
	 * @return
	 */
	public String sendAlert(AlertRule alertRule, Map<String, Object> sendMap) {
		String subject = alertRule.getTitle();

		String sendWays = alertRule.getSendWay();
		if (StringUtils.isNotBlank(sendWays)) {
			String webReceiver = alertRule.getWebsiteId();
			String userId = alertRule.getUserId();
			this.sendAlert(AlertSource.AUTO, subject, userId, sendWays, webReceiver, sendMap, true);
		}
		return null;
	}


	/**
	 * @param sendType  是自动预警还是手动预警
	 * @param subject   当前这批预警的主题，自动预警则是预警规则名，手动预警则由用户自定义输入
	 * @param userId    当前发送人的id，自动预警的话为当前预警规则对应的用户，如果是手动预警，则为当前登陆用户
	 * @param sendWays  发送方式字符串组，跟下面的接收人一一对应
	 * @param receivers 接收人信息组，跟上面的发送方式一一对应
	 * @param map       发送数据
	 * @return
	 */
	public String sendAlert(AlertSource sendType, String subject, String userId,
							String sendWays, String receivers, Map<String, Object> map, Boolean cutSendData) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		List<String> emailList = new ArrayList<>();
		List<String> smsList = new ArrayList<>();
		List<String> appList = new ArrayList<>();

		List<String> wechatList = new ArrayList<>();

		String[] sendWayList = sendWays.split(";|；");
		String[] receiverList = receivers.split(";");


		if (sendWayList != null && sendWayList.length > 0) {
			for (int i = 0; i < sendWayList.length; i++) {

				SendWay sendWay = SendWay.valueOf(sendWayList[i]);
				String webReceiver = receiverList[i];

				User findByUserName = null;
				try {
					findByUserName = this.findByUserName(webReceiver);
				} catch (Exception e) {
				}
				switch (sendWay) {
					case EMAIL:
						if (findByUserName != null) {
							//计算今天与到期时间差多少
							String expireAt = findByUserName.getExpireAt();
							int n = 0;
							if (!UserUtils.FOREVER_DATE.equals(expireAt)) {
								try {
									n = DateUtil.rangBetweenNow(sdf.parse(expireAt));
								} catch (Exception e) {
									n = 0;
								}
							}
							if (n > 0) {
								List<AlertAccount> userList = alertAccountRepository.findByUserIdAndType(findByUserName.getId(),
										SendWay.EMAIL);
								if (userList != null && userList.size() > 0) {
									for (AlertAccount account : userList) {
										emailList.add(account.getAccount());
									}
								}
							}
						} else {
							emailList.add(webReceiver);
						}

						break;
					case SMS:// 站内
						smsList.add(webReceiver);

						break;
					case WE_CHAT:
						List<AlertAccount> accountList = new ArrayList<>();
						if (findByUserName != null) {
							accountList = alertAccountRepository.findByUserIdAndType(findByUserName.getId(), SendWay.WE_CHAT);
						} else {//直接传的微信号   这个人没停止预警  就发  停止预警就不发
							//通过微信号查alertaccount中的active  true发  false不发
							List<AlertAccount> byAccountAndUserIdAndType = alertAccountRepository.findByAccountAndUserIdAndType(webReceiver, userId, SendWay.WE_CHAT);
							AlertAccount alertaccount = null;
							if (byAccountAndUserIdAndType.size() > 0) {
								alertaccount = byAccountAndUserIdAndType.get(0);
							}
							accountList.add(alertaccount);
						}
						for (AlertAccount alertAccount : accountList) {
							if (alertAccount != null) {
								if (alertAccount.isActive()) {
									wechatList.add(alertAccount.getAccount());
								}
							}
						}
						break;
					case APP:// 安卓端
						appList.add(webReceiver);
						break;
					default:
						break;
				}

			}
			User user = userRepository.findOne(userId);

			try {

				map = this.saveAlertEntityInfo(map, receivers, sendWays, user, "send", cutSendData);

				if (emailList.size() > 0) {
					Message message = this.sendAlertInfo(sendType, subject, user, SendWay.EMAIL, emailList, map);
				}
				if (smsList.size() > 0) {
					Message message = this.sendAlertInfo(sendType, subject, user, SendWay.SMS, smsList, map);
				}
				if (wechatList.size() > 0) {
					Message message = this.sendAlertInfo(sendType, subject, user, SendWay.WE_CHAT, wechatList, map);
				}
				if (appList.size() > 0) {
					Message message = this.sendAlertInfo(sendType, subject, user, SendWay.APP, appList, map);
				}

				return "发送成功了";
			} catch (Exception e) {
				log.info("发送成功");
				e.printStackTrace();
				return "发送失败了";
			}
		}

		return "无账号可发送";
	}

	/**
	 * 模板
	 */
	private static final String TEMPLATE = "mailmess2.ftl";

	/**
	 * @param sendType  是自动预警还是手动预警
	 * @param subject   当前这批预警的主题，自动预警则是预警规则名，手动预警则由用户自定义输入
	 * @param user_     当前发送人的信息，自动预警的话为当前预警规则对应的用户，如果是手动预警，则为当前登陆用户
	 * @param sendWay   发送方式 1个
	 * @param receivers 接收人  当前发送方式对应的，要发送的所有人，如果是邮箱方式，这个则全是邮箱，如果是微信则全是openID
	 * @param map       发送数据
	 * @return
	 */
	private Message sendAlertInfo(AlertSource sendType, String subject, User user_,
								  SendWay sendWay, List<String> receivers, Map<String, Object> map) {
		try {
			int size = (int) map.get("size");
			List<Map<String, String>> list = (List<Map<String, String>>) map.get("listMap");
			switch (sendWay) {
				case EMAIL:
					String emailNetinsightUrl = netinsightUrl;
					int index = emailNetinsightUrl.lastIndexOf("/netInsight");
					emailNetinsightUrl = emailNetinsightUrl.substring(0, index);
					map.put("url", emailNetinsightUrl);

					map.put("userName", user_.getUserName());
					String rece = String.join(";", receivers);
					return mailSend.sendEmail(TEMPLATE, subject, map, rece);

				case SMS:// 站内
					for (String receiver : receivers) {
						//按文件存储  每个用户只能看到自己userid对应下的文件  所以在用接受者的userid存一遍 所发出的站内预警才能被接受者看到
						User user = findByUserName(receiver);
						if (user != null) {
							this.saveAlertEntityInfo(map, receiver, SendWay.SMS.toString(), user, "receive",false);
						}
					}
					return Message.getMessage(CodeUtils.SUCCESS, "发送成功！", null);

				case WE_CHAT:
					Environment env = SpringUtil.getBean(Environment.class);
					String wechatNetinsightUrl = netinsightUrl;
					if (subject == null) {
						subject = "";
					}
					// 将要发送的微信通知页面显示的预警标题信息展示出来
					List<Map<String ,List<String>>> listWeiChats = new ArrayList<>();
					List<List<String>> messageLists = new ArrayList<>();
					for (int i = 0; i < list.size(); i += 5) {
						Map<String ,List<String>> listWeiChat = new HashMap<>();

						List<String> sids = new ArrayList<>();
						List<String> titles = new ArrayList<>();
						List<Map<String, String>> oneList = null;
						if (i + 5 < list.size()) {
							oneList = list.subList(i, i + 5);
						} else {
							oneList = list.subList(i, list.size());
						}
						oneList.stream().forEach(oneMap -> titles.add(oneMap.get("title")));
						oneList.stream().forEach(oneMap -> sids.add(oneMap.get(FtsFieldConst.FIELD_ALERT_ID)));
						listWeiChat.put(FtsFieldConst.FIELD_ALERT_ID,sids);
						listWeiChat.put("title",titles);

						listWeiChats.add(listWeiChat);
					}
					int i = 1;
					for (Map<String ,List<String>> listWeiChat : listWeiChats) {
						List<String> sids = listWeiChat.get(FtsFieldConst.FIELD_ALERT_ID);
						List<String> titles = listWeiChat.get("title");

						String alertTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMdd);
						String id = UUID.randomUUID().toString();
						TRSInputRecord record = new TRSInputRecord();
						try {
							record.addColumn(FtsFieldConst.FIELD_ALERT_TYPE_ID, id);
							record.addColumn(FtsFieldConst.FIELD_ALERT_NAME, subject);
							record.addColumn(FtsFieldConst.FIELD_ALERT_TIME, alertTime);
							record.addColumn(FtsFieldConst.FIELD_SEND_WAY, SendWay.WE_CHAT.toString());
							record.addColumn(FtsFieldConst.FIELD_SIZE, listWeiChat.size());
							record.addColumn(FtsFieldConst.FIELD_USER_ID, user_.getId());
							record.addColumn(FtsFieldConst.FIELD_RECEIVER, StringUtils.join(receivers, ";"));

							record.addColumn(FtsFieldConst.FIELD_ALERT_IDS, StringUtils.join(sids,";"));
							//直接存文件不编辑了
							hybase8SearchServiceNew.insertRecords(record, Const.ALERTTYPE, true, null);
						} catch (TRSException ex) {
							ex.printStackTrace();
						} catch (com.trs.netInsight.handler.exception.TRSException ex) {
							ex.printStackTrace();
						}

						List<String> messageList = new ArrayList<>();
						String alertDetailUrl = WeixinMessageUtil.ALERT_DETAILS_URL.replaceAll("ID", "")
								.replace("NETINSIGHT_URL", wechatNetinsightUrl) + id;

						if (user_.getUserName() == null) {
							user_.setUserName("");
						}
						String alertTitle = WeixinMessageUtil.ALERT_TITLE.replace("SUBJECT", subject)
								.replace("SIZE", String.valueOf(size)).replace("USERNAME",
										user_.getUserName());
						for (String openId : receivers) {
							if (StringUtil.isNotEmpty(openId)) {
								log.error("netinsightUrl:" + wechatNetinsightUrl);
								AlertTemplateMsg alertTemplateMsg = new AlertTemplateMsg(openId,
										alertDetailUrl, alertTitle, StringUtil.getTitleList(titles, i), alertTime, "");

								String sendWeixin = null;
								try {
									sendWeixin = WeixinUtil.sendWeixin(WeixinUtil.getToken(), alertTemplateMsg);
									log.error("微信预警自动推送成功！预警名称：" + subject + "接收人：" + receivers + "预警账号：" + openId);
								} catch (Exception e) {
									log.error("微信预警自动推送失败！预警名称：" + subject + "接收人：" + receivers + "预警账号：" + openId);
									e.printStackTrace();
								}
								log.error("发送微信返回值：" + sendWeixin);
								if (StringUtils.equals("ok", sendWeixin)) {
									messageList.add(openId + "：发送成功！");
								} else {
									messageList.add(openId + "：发送失败！");
								}

							} else {
								messageList.add(receivers + "：没有查询到微信号！");
							}
							log.error("微信推送！预警名称：" + subject + "接收人：" + receivers + "接收人id：" + openId);
						}
						log.error("微信推送循环外！预警名称：" + subject + "接收人：" + receivers);
						messageLists.add(messageList);
						i = i + titles.size();
					}
					return Message.getMessage(CodeUtils.SUCCESS, "发送成功！", messageLists);
				case APP:// 安卓端
					List<String> sids = new ArrayList<>();
					list.stream().forEach(oneMap -> sids.add(oneMap.get(FtsFieldConst.FIELD_ALERT_ID)));
					String appAlertTime = DateUtil.formatCurrentTime(DateUtil.yyyyMMdd);

					String appAlertId = UUID.randomUUID().toString().replaceAll("-", "");
					TRSInputRecord record = new TRSInputRecord();
					record.addColumn(FtsFieldConst.FIELD_ALERT_TYPE_ID, appAlertId);
					record.addColumn(FtsFieldConst.FIELD_ALERT_NAME, subject);
					record.addColumn(FtsFieldConst.FIELD_ALERT_TIME, appAlertTime);
					record.addColumn(FtsFieldConst.FIELD_SEND_WAY, SendWay.APP.toString());
					record.addColumn(FtsFieldConst.FIELD_SIZE, size);
					record.addColumn(FtsFieldConst.FIELD_USER_ID, user_.getId());
					record.addColumn(FtsFieldConst.FIELD_RECEIVER, StringUtils.join(receivers,";"));
					record.addColumn(FtsFieldConst.FIELD_ALERT_SOURCE, sendType.toString());

					List<Message> messageList = new ArrayList<>();
					for (String name : receivers) {
						//手动
						User byUserNameAPP = this.findByUserName(name);
						String nameAPPId = byUserNameAPP.getId();

						if (ObjectUtil.isEmpty(byUserNameAPP)) {
							throw new OperationException("请选择APP端预警接收人！");
						}

						Map<String, String> mapData = new HashMap<>();
						String alertMessage = JPushMessageUtil.ALERT_MESSAGE.replace("SUBJECT", subject).replace("SIZE", String.valueOf(size)).replace("USERNAME", user_.getUserName());
						mapData.put("type", "预警");
						mapData.put("title", alertMessage);
						mapData.put("alertId", appAlertId);
						mapData.put("receviceUserid", nameAPPId);
						Message message = null;
						//try住后 APP端不登录时，收不到提醒，但会查到预警信息
						try {
							message = JPushClientUtil.SendPush(mapData);
							messageList.add(message);
							log.error("APP预警手动推送成功！预警名称：" + subject + "接收人：" + receivers + "接收人id：" + nameAPPId);
						} catch (Exception e) {
							log.error("APP预警手动推送出错！预警名称：" + subject + "接收人：" + receivers + "接收人id：" + nameAPPId, e);
						}

						record.addColumn(FtsFieldConst.FIELD_ALERT_IDS, StringUtils.join(sids,";"));
						//直接存文件不编辑了
						hybase8SearchServiceNew.insertRecords(record, Const.ALERTTYPE, true, null);
					}

					return new Message(CodeUtils.SUCCESS, "发送成功！", messageList);

				default:
					return Message.getMessage(CodeUtils.FAIL, "发送方式不明确！", null);
			}

		} catch (Exception e) {
			log.error("发送报错", e);
			e.printStackTrace();
		}
		return Message.getMessage(CodeUtils.FAIL, "发送方式不明确！", sendWay);
	}


	/**
	 * 这里主要是微信和app的发送信息概括  saveAlertEntityInfo
	 *
	 * @return
	 */
	private Map<String, Object> saveAlertEntityInfo(Map<String, Object> map, String receivers,
													String sendWays, User user, String sendOrreceive, Boolean cutSeadDta) {

		List<Map<String, String>> list = (List<Map<String, String>>) map.get("listMap");

		List<Map<String, String>> sendDataList = new ArrayList<>();
		int i = 0;
		//			批量添加预警记录 至  hybase库
		List<TRSInputRecord> trsInputRecords = new ArrayList<>();
		for (Map<String, String> each : list) {
			String title = StringUtil.replaceNRT(StringUtil.removeFourChar(each.get("title")));
			String content = each.get("content");

			TRSInputRecord record = new TRSInputRecord();
			try {
				record.addColumn(FtsFieldConst.FIELD_URLNAME, each.get("url"));
				record.addColumn(FtsFieldConst.FIELD_URLTITLE, title);
				record.addColumn(FtsFieldConst.FIELD_GROUPNAME, each.get("groupName"));
				record.addColumn(FtsFieldConst.FIELD_SID, each.get("sid"));
				record.addColumn(FtsFieldConst.FIELD_SEND_WAY_LIST, sendWays);
				record.addColumn(FtsFieldConst.FIELD_RECEIVER_LIST, receivers);
				record.addColumn(FtsFieldConst.FIELD_APPRAISE, each.get("appraise"));
				record.addColumn(FtsFieldConst.FIELD_SITENAME, each.get("siteName"));
				record.addColumn(FtsFieldConst.FIELD_NRESERVED1, each.get("nreserved1"));
				record.addColumn(FtsFieldConst.FIELD_KEYWORDS, each.get("keywords"));
				record.addColumn(FtsFieldConst.FIELD_SEND_RECEIVE, sendOrreceive);
				String retweetedMid = each.get("retweetedMid");
				if (null == retweetedMid) {
					retweetedMid = "0";
				}
				record.addColumn(FtsFieldConst.IR_RETWEETED_MID, retweetedMid);
				record.addColumn(FtsFieldConst.FIELD_MD5TAG, each.get("md5"));
				record.addColumn(FtsFieldConst.FIELD_CONTENT, content);
				record.addColumn(FtsFieldConst.FIELD_ALERT_CONTENT, each.get("fullContent"));
				String scrName = "";
				if (StringUtil.isNotEmpty(each.get("screenName"))) {
					scrName = each.get("screenName");
				} else {
					scrName = each.get("author");
				}
				record.addColumn(FtsFieldConst.FIELD_SCREEN_NAME, scrName);
				String rtString = each.get("rttCount");
				if (StringUtil.isNotEmpty(rtString)) {
					record.addColumn(FtsFieldConst.FIELD_RTTCOUNT, Long.valueOf(each.get("rttCount")));
				} else {
					record.addColumn(FtsFieldConst.FIELD_RTTCOUNT, 0);
				}
				String commtCount = each.get("commtCount");
				if (StringUtil.isNotEmpty(commtCount)) {
					record.addColumn(FtsFieldConst.FIELD_COMMTCOUNT, Long.valueOf(each.get("commtCount")));
				} else {
					record.addColumn(FtsFieldConst.FIELD_COMMTCOUNT, 0);
				}
				try {
					record.addColumn(FtsFieldConst.FIELD_URLTIME, sdf.parse(each.get("urlTime")));
				} catch (ParseException e) {
					log.error("时间转换出错或者Hybase中时间为空");
					record.addColumn(FtsFieldConst.FIELD_URLTIME, new Date());
					e.printStackTrace();
				}
				if (ObjectUtil.isNotEmpty(user)) {
					record.addColumn(FtsFieldConst.FIELD_SubGroup_ID, user.getSubGroupId());
					record.addColumn(FtsFieldConst.FIELD_USER_ID, user.getId());
				}
				//record.addColumn(FtsFieldConst.FIELD_USER_ID, user.getId());
				record.addColumn(FtsFieldConst.FIELD_ORGANIZATION_ID, each.get("organizationId"));
				String alertId = UUID.randomUUID().toString();
				record.addColumn(FtsFieldConst.FIELD_ALERT_ID, alertId);
				record.addColumn(FtsFieldConst.FIELD_IMAGE_URL, each.get("imageUrl"));
				record.addColumn(FtsFieldConst.FIELD_URLTITLE_WHOLE, each.get("titleWhole"));

				trsInputRecords.add(record);

				each.put(FtsFieldConst.FIELD_ALERT_ID,alertId);
				if (cutSeadDta != null && cutSeadDta) {
					if (i < 20) {
						// 如果 需要切割发送数据的话，则发送数据只发送20条
						sendDataList.add(each);
					}
				} else {
					sendDataList.add(each);
				}
				i++;

			} catch (TRSException e) {
				log.error("预警记录record.addColumn出错：", e);
				e.printStackTrace();
			}
		}

		try {
			hybase8SearchServiceNew.insertRecords(trsInputRecords, Const.ALERT, true, null);
		} catch (com.trs.netInsight.handler.exception.TRSException e) {
			log.error("批量添加预警记录至 hybase 出错：用户id：" + user.getId() + "，预警" , e);
			e.printStackTrace();
		}

		map.put("listMap", sendDataList);
		return map;

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