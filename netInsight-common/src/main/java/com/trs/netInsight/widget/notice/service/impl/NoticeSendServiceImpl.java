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
import com.trs.netInsight.widget.alert.entity.repository.AlertBackupsRepository;
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
	private AlertBackupsRepository backupsRepository;
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
							String ids = this.addAlertOrAlertBackups(true, list, rece, SendWay.EMAIL, userId,"send");
							map.put("userName", userName);
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
					User user = findByUserName(receivers);
//					//接受者id和发送者id不一致时
					if(user!=null){
						if(!user.getId().equals(userId)){
							String saveOther = this.addAlertOrAlertBackups(true, list, receiver, SendWay.SMS, user.getId(),"receive");
						}else{//一致时  我是发送方也是接收方
							String saveOther = this.addAlertOrAlertBackups(true, list, receiver, SendWay.SMS, userId,"receive");
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
							String alertIds = this.addAlertOrAlertBackups(true, listWeiChat, receivers, SendWay.WE_CHAT,
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
					String alertAppIds = this.addAlertOrAlertBackups(true, list, receivers, SendWay.APP,
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
	 * @param choose
	 * @param list
	 * @param receiver
	 * @param sendWay
	 * @param userId
	 * @throws OperationException
	 * @date Created at 2018年3月14日 下午3:45:01
	 * @Author 谷泽昊
	 */
	private String addAlertOrAlertBackups(boolean choose, List<Map<String, String>> list, String receiver,
										  SendWay sendWay, String userId, String sendOrreceive) throws OperationException {
		StringBuffer buffer = new StringBuffer();
		User user = userRepository.findOne(userId);
		if (choose) {
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
		} else {
			List<AlertBackups> alertBackupsList = new ArrayList<>();
			for (Map<String, String> each : list) {
				String content =each.get("content");
				if (!SendWay.APP.equals(sendWay)){
					content = StringUtil.removeFourChar(content);
					content = StringUtil.replaceEmoji(content);
					content = StringUtil.replaceImg(content) ;
					System.err.println("content"+content);
				}
				String title = StringUtil.replaceNRT(StringUtil.removeFourChar(each.get("title")));
				AlertBackups backups = new AlertBackups();
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

	/**
	 * 发送预警信息
	 * @return
	 */
	public String sendAlert(AlertRule alertRule,Map<String, Object> sendMap,List<Map<String, String>> bakMap){



		return null;
	}

	/**
	 * 通过各种方式发送预警信息
	 * @return
	 */

	private String sendWayAlertInfoForAlertRule(){

		return null;
	}

	/**
	 * 存储预警发送的信息 - 预警内容详情
	 *
	 * @return
	 */
	private String saveAlertEntityInfo(List<Map<String, String>> bakMap){

		return null;
	}

	/**
	 * 这里主要是微信和app的发送信息概括
	 * @return
	 */
	private String saveAlertTypeInfo(Map<String, Object> sendMap){

		return null;
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