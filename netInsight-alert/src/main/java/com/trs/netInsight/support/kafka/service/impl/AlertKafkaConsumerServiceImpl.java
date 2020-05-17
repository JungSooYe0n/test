/*
 * Project: alertnetinsight
 * 
 * File Created at 2018年11月15日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.kafka.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentTF;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.kafka.service.IAlertKafkaConsumerService;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertKafkaSend;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.AlertRuleBackups;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.service.IAlertRuleBackupsService;
import com.trs.netInsight.widget.alert.service.IAlertRuleService;
import com.trs.netInsight.widget.notice.service.INoticeSendService;

import lombok.extern.slf4j.Slf4j;

/**
 * 定时发送预警kafka实现类
 * 
 * @Type AlertKafkaConsumerServiceImpl.java
 * @author 谷泽昊
 * @date 2018年11月15日 下午3:19:13
 * @version
 */
@Slf4j
@Service
public class AlertKafkaConsumerServiceImpl implements IAlertKafkaConsumerService {
	@Autowired
	private IAlertRuleService alertRuleService;

	@Autowired
	private FullTextSearch hybase8SearchService;
	@Autowired
	private IAlertRuleBackupsService alertRuleBackupsService;
	@Autowired
	private INoticeSendService noticeSendService;
	/**
	 * 模板
	 */
	private static final String TEMPLATE = "mailmess2.ftl";

	@Override
	public void send(AlertKafkaSend alertKafkaSend) {
		long beginTime = System.currentTimeMillis();
		log.info("预警开始执行：" + DateUtil.millis2String(beginTime, DateUtil.yyyyMMdd));
		try {
			if (alertKafkaSend != null) {
				String alertRuleBackupsId = alertKafkaSend.getAlertRuleBackupsId();
				String alertRuleId = alertKafkaSend.getAlertRuleId();
				if (StringUtils.isNotBlank(alertRuleBackupsId) && StringUtils.isNotBlank(alertRuleId)) {
					AlertRuleBackups alertRuleBackups = alertRuleBackupsService.findOne(alertRuleBackupsId);
					AlertRule alertRule = alertRuleService.findOne(alertRuleId);
					if (alertRuleBackups != null && alertRule != null) {
						log.info("正在执行的预警为：" + alertRule.getTitle());
						QueryBuilder searchBuilder = alertRuleBackups.toSearchBuilder();
						QueryBuilder searchBuilderWeiBo = alertRuleBackups.toSearchBuilderWeiBo();
						QueryBuilder searchBuilderWeiXin = alertRuleBackups.toSearchBuilderWeiXin();
						QueryBuilder searchBuilderTF = alertRuleBackups.toSearchBuilderTF();
						String trs = searchBuilder.getChildSearchCondition().toString();
						if(StringUtil.isEmpty(trs)){
							trs = searchBuilder.getAppendTRSL().toString();
						}
						String trs_wb = searchBuilderWeiBo.getChildSearchCondition().toString();
						if(StringUtil.isEmpty(trs_wb)){
							trs_wb = searchBuilderWeiBo.getAppendTRSL().toString();
						}
						String trs_wx = searchBuilderWeiXin.getChildSearchCondition().toString();
						if(StringUtil.isEmpty(trs_wx)){
							trs_wx = searchBuilderWeiXin.getAppendTRSL().toString();
						}
						String trs_tf = searchBuilderTF.getChildSearchCondition().toString();
						if(StringUtil.isEmpty(trs_tf)){
							trs_tf = searchBuilderTF.getAppendTRSL().toString();
						}
						if (searchBuilder != null) {
							searchBuilder.page(0, 5);
							searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
						}
						if (searchBuilderWeiBo != null) {
							searchBuilderWeiBo.page(0, 5);
							searchBuilderWeiBo.setDatabase(Const.WEIBO);
						}
						if (searchBuilderWeiXin != null) {
							searchBuilderWeiXin.page(0, 5);
							searchBuilderWeiXin.setDatabase(Const.WECHAT_COMMON);
						}
						if (searchBuilderTF != null) {
							searchBuilderTF.page(0, 5);
							searchBuilderTF.setDatabase(Const.HYBASE_OVERSEAS);
						}
						try {
							List<AlertEntity> list = this.list(searchBuilder, searchBuilderWeiBo, searchBuilderWeiXin,
									searchBuilderTF, alertRuleBackups,trs,trs_wb,trs_wx,trs_tf);
							this.send(list, alertRuleBackups);
							// 更新时间, 发送成功后更新时间
							alertRuleBackups = alertRuleBackupsService.findOne(alertRuleBackupsId);
							alertRule = alertRuleService.findOne(alertRuleId);
							if (StringUtils.isNotBlank(alertRuleBackupsId) && StringUtils.isNotBlank(alertRuleId)) {
								alertRuleBackups.setLastStartTime(DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss));
								alertRule.setLastStartTime(DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss));
								alertRuleBackups.setLastExecutionTime(System.currentTimeMillis());
								alertRule.setLastExecutionTime(System.currentTimeMillis());
								alertRuleBackupsService.update(alertRuleBackups);
								alertRuleService.update(alertRule, false);
							}
						} catch (Exception e) {
							log.error("规则【" + alertRule.getTitle() + "】发送失败 ", e);
						}
					}
				}
			}
		} catch (Exception e) {
			log.info("预警报错：", e);
		} finally {
			long endTime = System.currentTimeMillis();
			log.info("预警开始完毕：" + DateUtil.millis2String(endTime, DateUtil.yyyyMMdd) + "，执行时间："
					+ (endTime - beginTime) + "毫秒");
		}
	}

	@Override
	public void send(List<AlertEntity> list, AlertRuleBackups alertRuleBackups) {
		if (list != null && list.size() > 0) {
			List<Map<String, String>> ListMap = new ArrayList<>();
			for (AlertEntity alertEntity : list) {
				Map<String, String> map = new HashMap<>();
				if(StringUtil.isNotEmpty(alertEntity.getTrslk())){
					map.put("trslk", alertEntity.getTrslk());
				} else {
					map.put("trslk", "");
				}
				// 微博没标题
				map.put("url", alertEntity.getUrlName());
				// alertEntity.setTitle("这个<font color=red>雾霾</font>太严重如何去掉<font
				// color=red>雾霾</font>11111111111111111111111");
				String title = StringUtil.replaceNRT(StringUtil.removeFourChar(alertEntity.getTitle()));
				title = StringUtil.replaceEmoji(title);
				if (StringUtil.isNotEmpty(title)) {
					title = StringUtil.calcuCutLength(title, Const.ALERT_NUM);
					// if (title.length() > Const.ALERT_NUM) {
					// title = title.substring(0, Const.ALERT_NUM) + "...";
					// }
					map.put("title", title);
				} else {
					map.put("title", "");
				}
				// map.put("title", title);
				map.put("groupName", alertEntity.getGroupName());
				map.put("author", alertEntity.getAuthor());
				map.put("sid", alertEntity.getSid());
				map.put("retweetedMid", alertEntity.getRetweetedMid());
				map.put("sim", 0 == alertEntity.getSim() ? null : String.valueOf(alertEntity.getSim()));// 热度值要显示相似文章数
				String source = alertEntity.getSiteName();
				if (StringUtil.isEmpty(source)) {
					source = alertEntity.getGroupName();
				}
				map.put("source", source);
				// log.info("source: "+map.get("source"));
				// System.out.println(source+" "+alertEntity.getSid());
				Date urlTime = alertEntity.getTime();
				SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
				if (urlTime != null) {
					String str = sdf.format(urlTime);
					map.put("urlTime", str);
				} else {
					map.put("urlTime", "");
				}
				// log.info("urltime: "+map.get("urlTime"));
				map.put("appraise", alertEntity.getAppraise());
				map.put("siteName", alertEntity.getSiteName());
				map.put("md5", alertEntity.getMd5tag());
				// 过滤img标签
				// alertEntity.setContent(StringUtil.replaceImg(alertEntity.getContent()));
				// String content = alertEntity.getContent();
				String content = StringUtil.removeFourChar(alertEntity.getContent());
				content = StringUtil.replaceEmoji(content);
				content = StringUtil.replaceImg(content);
				content = StringUtil.notFontEnd(content, 150);
				map.put("content", content);
				map.put("screenName", alertEntity.getScreenName());
				map.put("rttCount", String.valueOf(alertEntity.getRttCount()));
				map.put("commtCount", String.valueOf(alertEntity.getCommtCount()));
				// map.put("alertRuleBackupsId",
				// alertRuleBackups.getAlertRuleId());
				map.put("ruleId", alertRuleBackups.getAlertRuleId());
				map.put("alertRuleBackupsId", alertRuleBackups.getId());
				map.put("organizationId", alertRuleBackups.getOrganizationId());
				map.put("countBy", alertRuleBackups.getCountBy());
				// each.get("countBy")
				ListMap.add(map);
			}

			Map<String, Object> map = new HashMap<>();
			map.put("listMap", ListMap);
			map.put("size", list.size());
			// 自动预警标题
			map.put("title", StringUtil.calcuCutLength(alertRuleBackups.getTitle(), Const.ALERT_NUM));

			String sendWays = alertRuleBackups.getSendWay();
			if (StringUtils.isNotBlank(sendWays)) {
				String[] split = sendWays.split(";|；");
				for (int i = 0; i < split.length; i++) {
					String string = split[i];
					SendWay sendWay = SendWay.valueOf(string);
					String webReceiver = alertRuleBackups.getWebsiteId();
					String[] splitWeb = webReceiver.split(";|；");
					// 我让前段把接受者放到websiteid里边了 然后用户和发送方式一一对应 和手动发送方式一致
					noticeSendService.sendAll(sendWay, TEMPLATE, alertRuleBackups.getTitle(), map, splitWeb[i],
							alertRuleBackups.getUserId(),AlertSource.AUTO);
				}
			}
		}
	}

	/**
	 * 查询列表
	 * 
	 * @date Created at 2018年3月2日 下午3:46:53
	 * @Author 谷泽昊
	 * @param searchBuilder
	 * @param searchBuilderWeiBo
	 * @param searchBuilderWeiXin
	 * @param alertRuleBackups
	 * @return
	 */
	private List<AlertEntity> list(QueryBuilder searchBuilder, QueryBuilder searchBuilderWeiBo,
			QueryBuilder searchBuilderWeiXin, QueryBuilder searchBuilderTF, AlertRuleBackups alertRuleBackups,
			String trs,String trs_wb,String trs_wx,String trs_tf) {
		List<AlertEntity> list = new ArrayList<>();
		if (searchBuilder != null) {
			list.addAll(this.chauntong(searchBuilder, alertRuleBackups,trs));
		}
		if (searchBuilderWeiBo != null) {
			list.addAll(this.weibo(searchBuilderWeiBo, alertRuleBackups,trs_wb));
		}
		if (searchBuilderWeiXin != null) {
			list.addAll(this.weixin(searchBuilderWeiXin, alertRuleBackups,trs_wx));
		}
		if (searchBuilderTF != null) {
			list.addAll(this.searchTF(searchBuilderTF, alertRuleBackups,trs_tf));
		}
		return list;
	}

	/**
	 * 查传统库
	 * 
	 * @date Created at 2018年3月2日 下午3:46:03
	 * @Author 谷泽昊
	 * @param searchBuilder
	 * @param alertRuleBackups
	 * @return
	 */
	private List<AlertEntity> chauntong(QueryBuilder searchBuilder, AlertRuleBackups alertRuleBackups,String trs) {
		List<AlertEntity> list = new ArrayList<>();
		List<FtsDocument> documents = null;
		StringBuffer bufferSid = new StringBuffer();
		try {
			documents = hybase8SearchService.ftsQuery(searchBuilder, FtsDocument.class, alertRuleBackups.isRepetition(),
					alertRuleBackups.isIrSimflag(),alertRuleBackups.isIrSimflagAll(),null);
			for (FtsDocument ftsDocument : documents) {
				String content = ftsDocument.getContent();
				String[] imaUrls = null;
				String imaUrl = "";

				if (content != null){
					imaUrls = content.split("IMAGE&nbsp;SRC=&quot;");
					if (imaUrls.length>1){
						imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
					}
				}
				String keywords = "";
				if(ftsDocument.getKeywords() != null){
					keywords = ftsDocument.getKeywords().toString();
				}
				AlertEntity alertEntity = new AlertEntity(ftsDocument.getSid(), ftsDocument.getTitle(),ftsDocument.getTitle(),
						ftsDocument.getContent(), ftsDocument.getUrlName(), ftsDocument.getUrlTime(),
						ftsDocument.getSiteName(), ftsDocument.getGroupName(), alertRuleBackups.getId(),
						alertRuleBackups.getAlertType(), 0, 0, "", ftsDocument.getAppraise(), "", null,
						ftsDocument.getNreserved1(), ftsDocument.getMd5Tag(), false, null, "",imaUrl,false, false, 0,trs,keywords,ftsDocument.getAuthors());
				bufferSid.append(ftsDocument.getSid()).append(" OR ");
				list.add(alertEntity);
			}
		} catch (Exception e) {
			log.info("hybase查询出错", e);
		}
		return list;
	}

	/**
	 * 查微博库
	 * 
	 * @date Created at 2018年3月2日 下午3:45:57
	 * @Author 谷泽昊
	 * @param searchBuilderWeiBo
	 * @param alertRuleBackups
	 * @return
	 */
	private List<AlertEntity> weibo(QueryBuilder searchBuilderWeiBo, AlertRuleBackups alertRuleBackups,String trs) {
		List<AlertEntity> list = new ArrayList<>();
		List<FtsDocumentStatus> documents = null;
		StringBuffer bufferSid = new StringBuffer();
		try {
			documents = hybase8SearchService.ftsQuery(searchBuilderWeiBo, FtsDocumentStatus.class,
					alertRuleBackups.isRepetition(), alertRuleBackups.isIrSimflag(),alertRuleBackups.isIrSimflagAll(),null);
			for (FtsDocumentStatus ftsDocument : documents) {
				String content = ftsDocument.getStatusContent();
				String[] imaUrls = null;
				String imaUrl = "";

				if (content != null){
					imaUrls = content.split("IMAGE&nbsp;SRC=&quot;");
					if (imaUrls.length>1){
						imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
					}
				}
				AlertEntity alertEntity = new AlertEntity(ftsDocument.getMid(), ftsDocument.getStatusContent(),null,
						ftsDocument.getStatusContent(), ftsDocument.getUrlName(), ftsDocument.getCreatedAt(),
						ftsDocument.getSiteName(), ftsDocument.getGroupName(), alertRuleBackups.getId(),
						alertRuleBackups.getAlertType(), 0, 0, ftsDocument.getScreenName(), ftsDocument.getAppraise(),
						"", null, "", ftsDocument.getMd5Tag(), false, ftsDocument.getRetweetedMid(),"", imaUrl,false, false, 0,trs,null,ftsDocument.getAuthors());
				bufferSid.append(ftsDocument.getSid()).append(" OR ");
				list.add(alertEntity);
			}
		} catch (Exception e) {
			log.info("hybase查询出错", e);
		}
		return list;
	}

	/**
	 * 查传微信库
	 * 
	 * @date Created at 2018年3月2日 下午3:45:47
	 * @Author 谷泽昊
	 * @param searchBuilderWeiBo
	 * @param alertRuleBackups
	 * @return
	 */
	private List<AlertEntity> weixin(QueryBuilder searchBuilderWeiBo, AlertRuleBackups alertRuleBackups,String trs) {
		List<AlertEntity> list = new ArrayList<>();
		List<FtsDocumentWeChat> documents = null;
		StringBuffer bufferSid = new StringBuffer();
		try {
			documents = hybase8SearchService.ftsQuery(searchBuilderWeiBo, FtsDocumentWeChat.class,
					alertRuleBackups.isRepetition(), alertRuleBackups.isIrSimflag(),alertRuleBackups.isIrSimflagAll(),null);
			for (FtsDocumentWeChat ftsDocument : documents) {
				String content = ftsDocument.getContent();
				String[] imaUrls = null;
				String imaUrl = "";

				if (content != null){
					imaUrls = content.split("IMAGE&nbsp;SRC=&quot;");
					if (imaUrls.length>1){
						imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
					}
				}
				AlertEntity alertEntity = new AlertEntity(ftsDocument.getHkey(), ftsDocument.getUrlTitle(),ftsDocument.getUrlTitle(),
						ftsDocument.getContent(), ftsDocument.getUrlName(), ftsDocument.getUrlTime(),
						ftsDocument.getAuthors(), ftsDocument.getGroupName(), alertRuleBackups.getId(),
						alertRuleBackups.getAlertType(), 0, 0, "", ftsDocument.getAppraise(), "", null, "",
						ftsDocument.getMd5Tag(), false, null,"",imaUrl, false, false, 0,trs,null,ftsDocument.getAuthors());
				bufferSid.append(ftsDocument.getSid()).append(" OR ");
				list.add(alertEntity);
			}
		} catch (Exception e) {
			log.info("hybase查询出错", e);
		}
		return list;
	}

	private List<AlertEntity> searchTF(QueryBuilder searchBuilderTF, AlertRuleBackups alertRuleBackups,String trs) {
		List<AlertEntity> list = new ArrayList<>();
		List<FtsDocumentTF> documents = null;
		StringBuffer bufferSid = new StringBuffer();
		try {
			documents = hybase8SearchService.ftsQuery(searchBuilderTF, FtsDocumentTF.class,
					alertRuleBackups.isRepetition(), alertRuleBackups.isIrSimflag(),alertRuleBackups.isIrSimflagAll(),null);
			for (FtsDocumentTF ftsDocument : documents) {
				String content = ftsDocument.getContent();
				String[] imaUrls = null;
				String imaUrl = "";

				if (content != null){
					imaUrls = content.split("IMAGE&nbsp;SRC=&quot;");
					if (imaUrls.length>1){
						imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
					}
				}
				AlertEntity alertEntity = new AlertEntity(ftsDocument.getMid(), ftsDocument.getStatusContent(),null,
						ftsDocument.getStatusContent(), ftsDocument.getUrlName(), ftsDocument.getCreatedAt(),
						ftsDocument.getSiteName(), ftsDocument.getGroupName(), alertRuleBackups.getId(),
						alertRuleBackups.getAlertType(), 0, 0, "", ftsDocument.getAppraise(), "", null, "",
						ftsDocument.getMd5Tag(), false, ftsDocument.getRetweetedMid(),"",imaUrl, false, false, 0,trs,null,ftsDocument.getAuthors());
				bufferSid.append(ftsDocument.getSid()).append(" OR ");
				list.add(alertEntity);
			}
		} catch (Exception e) {
			log.info("hybase查询出错", e);
		}
		return list;
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年11月15日 谷泽昊 creat
 */