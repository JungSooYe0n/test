package com.trs.netInsight.widget.alert.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.AlertRuleBackups;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.service.IAlertRuleBackupsService;
import com.trs.netInsight.widget.alert.service.IAlertRuleService;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.notice.service.INoticeSendService;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.ISubGroupService;
import com.trs.netInsight.widget.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xiaoying on 2017/6/7.
 */
@Service
@Slf4j
@Transactional
public class AlertRuleServiceImpl implements IAlertRuleService {

	@Autowired
	private AlertRuleRepository alertRuleRepository;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private IInfoListService infoListService;

	@Autowired
	private INoticeSendService noticeSendService;

	@Autowired
	private IAlertRuleBackupsService alertRuleBackupsService;

	@Autowired
	private IAlertService alertService;

	@Autowired
	private IUserService userService;

	@Autowired
	private ISubGroupService subGroupService;
	
	@Value("${http.alert.netinsight.url}")
	private String alertNetinsightUrl;
	
	@Value("${http.client}")
	private boolean httpClient;

	/**
	 * 手动发送预警 receiver 接收人 documentId 文章Id content 发送时填写的标题 userId 用户id
	 * groupName 来源 send 发送方式 xiaoying
	 */
	@Override
	public Object send(String receiver, String documentId,String urlTime, String content, String userId, String groupName,
			String send,String trslk)throws TRSException, TRSSearchException {
		QueryBuilder builder = DateUtil.timeBuilder(urlTime);
		if(StringUtil.isNotEmpty(trslk)){
			String trs = RedisUtil.getString(trslk);
			if(StringUtil.isNotEmpty(trs)){
				builder.filterByTRSL(trs);
			}
		}
		if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
			// 调用查微信库的方法
			String weixinTrsl = "IR_HKEY:(";
			weixinTrsl += documentId.replace(";", " OR ") + ")";
			builder.filterByTRSL(weixinTrsl);
			builder.setDatabase(Const.WECHAT);
			builder.setPageSize(-1);
			return weixinSend(builder, content, userId, receiver, send,false,false);
		} else if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
			// 调用查微博库的方法
			String weiboTrsl = "IR_MID:(";
			weiboTrsl += documentId.replace(";", " OR ") + ")";
			builder.filterByTRSL(weiboTrsl);
			builder.setDatabase(Const.WEIBO);
			builder.setPageSize(-1);
			return weiboSend(builder, content, userId, receiver, send,false,false);
		} else if (Const.MEDIA_TYPE_TF.contains(groupName)) {
			// 调用查微博库的方法
			String tfTrsl = "IR_SID:(";
			tfTrsl += documentId.replace(";", " OR ") + ")";
			builder.filterByTRSL(tfTrsl);
			builder.setDatabase(Const.HYBASE_OVERSEAS);
			builder.setPageSize(-1);
			return tfSend(builder, content, userId, receiver, send,false,false);
		} else {
			// 根据documentid查hybase
			String trsl = "IR_SID:(";
			trsl += documentId.replace(";", " OR ") + ")";
			// 看看有多少篇文章
			builder.filterByTRSL(trsl);
			builder.setPageSize(-1);
			builder.setDatabase(Const.HYBASE_NI_INDEX);
			// 调用下边查传统库的方法
			return chuangtongSend(builder, content, userId, receiver, send,false,false);
		}

	}

	/**
	 * 查传统库发预警 由于映射实体不一样 所以传统 微信 微博分别写一个方法 CreatedBy xiao.ying
	 * 
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	private Object chuangtongSend(QueryBuilder queryBuilder, String content, String userId, String receiver,
			String send,boolean irSimflag,boolean irSimflagAll ) throws TRSSearchException, TRSException {
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, false,irSimflag,irSimflagAll ,null);
		if(ObjectUtil.isEmpty(ftsQuery)){
			ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, false,irSimflag,irSimflagAll,null );
		}
		String trs = queryBuilder.getAppendTRSL().toString();
		List<Map<String, String>> list = new ArrayList<>();
		StringBuffer bufferSid = new StringBuffer();
		for (FtsDocument ftsDocument : ftsQuery) {
			String groupName = ftsDocument.getGroupName();
			Map<String, String> map = new HashMap<>();
			String url = ftsDocument.getUrlName();
			if(StringUtil.isNotEmpty(trs)){
				map.put("trslk", trs);
			} else {
				map.put("trslk", "");
			}
			if (StringUtil.isNotEmpty(url)) {
				map.put("url", url);
			} else {
				map.put("url", "");
			}

			String docContent = ftsDocument.getContent();
			String[] imaUrls = null;
			String imaUrl = "";

			if (docContent != null){
				imaUrls = docContent.split("IMAGE&nbsp;SRC=&quot;");
				if (imaUrls.length>1){
					imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
				}
			}
			map.put("imageUrl",imaUrl);
			map.put("author",ftsDocument.getAuthors());
//			String title = ftsDocument.getTitle();
			String title = StringUtil.replaceNRT(StringUtil.removeFourChar(ftsDocument.getTitle()));
			title = StringUtil.replaceEmoji(title);
			if (StringUtil.isNotEmpty(title)) {
				/*if (title.length() > Const.ALERT_NUM) {
					title = title.substring(0, Const.ALERT_NUM) + "...";
				}*/
				map.put("titleWhole",title);
				//标题在这里截取，如果含有高亮标签或导致字数变多，截取出现问题
				//解决，先判断是否出现高亮标签和特殊字符再截取
				title = StringUtil.calcuCutLength(title, Const.ALERT_NUM);
				map.put("title", title);
			} else {
				map.put("title", "");
			}
			String source = ftsDocument.getSiteName();
			if (StringUtil.isEmpty(source)) {
				source = ftsDocument.getGroupName();
			}
			map.put("size", String.valueOf(ftsQuery.size()));
			map.put("md5", ftsDocument.getMd5Tag());
			map.put("source", source);
			/*if(ftsDocument.getKeywords() !=null){
				for(String s:ftsDocument.getKeywords()){
					map.put("keywords",new String() +s+",");
				}

			}*/
			if(ftsDocument.getKeywords() != null){
				map.put("keywords",ftsDocument.getKeywords().toString());
			}
			map.put("appraise", ftsDocument.getAppraise());
			map.put("siteName", ftsDocument.getSiteName());
			map.put("sid", ftsDocument.getSid());
			// 超过150 那就只存150个字
//			String documentContent = ftsDocument.getContent();
//			documentContent = StringUtil.replaceImg(documentContent);
			String documentContent = StringUtil.removeFourChar(ftsDocument.getContent());
			documentContent =StringUtil.replaceEmoji(documentContent);
			documentContent =StringUtil.replaceImg(documentContent) ;
			documentContent = StringUtil.notFontEnd(documentContent, 150);
			/*if (StringUtils.isNotBlank(documentContent)) {
				documentContent = documentContent.length() > 150 ? documentContent.substring(0, 140) + "..."
						: documentContent;
			}*/
			map.put("content", documentContent);
			map.put("nreserved1", ftsDocument.getNreserved1());
			Date urlTime = ftsDocument.getUrlTime();
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
			if (ObjectUtil.isNotEmpty(urlTime)) {
				String str = sdf.format(urlTime);
				map.put("urlTime", str);
			} else {
				map.put("urlTime", "");
			}
			map.put("groupName", groupName);
			list.add(map);
			// 根据groupname不同 实体不同 存的也不同 备注
			bufferSid.append(ftsDocument.getSid()).append(" OR ");
			// 存到表里

		}
		Map<String, Object> mapList = new HashMap<>();
		// notice工程接收的参数
		mapList.put("listMap", list);
		mapList.put("size", ftsQuery.size());
		// 预警标题
		mapList.put("title", content);
		if (bufferSid.length() > 4) {
			bufferSid.delete(bufferSid.length() - 4, bufferSid.length());
		}
		mapList.put("sids", bufferSid.toString());
		String[] receive = receiver.split(";");
		String[] sendWay = send.split(";");
		for (int i = 0; i < receive.length; i++) {
			SendWay sendValue = SendWay.valueOf(sendWay[i]);
			noticeSendService.sendAll(sendValue, "mailmess2.ftl", content, mapList, receive[i], userId,AlertSource.ARTIFICIAL);
		}
		// 当前用户名
//		String user_ = userService.findById(userId).getUserName();
		// 发给自己站内 现在被干掉了
		// noticeSendService.sendAll(SendWay.SMS, "mailmess2.ftl", content,
		// mapList, user_, userId);
		return "success";
		// return noticeSendService.sendAll(send, "mailmess2.ftl", content,
		// mapList, receiver, userId);
	}

	/**
	 * 查微信库发预警 由于映射实体不一样 所以传统 微信 微博分别写一个方法 CreatedBy xiao.ying
	 * 
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	private Object weixinSend(QueryBuilder queryBuilder, String content, String userId, String receiver, String send,boolean irSimflag,boolean irSimflagAll )
			throws TRSException, TRSSearchException {
		List<FtsDocumentWeChat> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentWeChat.class, false,irSimflag,irSimflagAll,null );
		log.info(queryBuilder.asTRSL());
		List<Map<String, String>> list = new ArrayList<>();
		String trs = queryBuilder.getAppendTRSL().toString();
		StringBuffer bufferSid = new StringBuffer();
		for (FtsDocumentWeChat ftsDocument : ftsQuery) {
			Map<String, String> map = new HashMap<>();
			if(StringUtil.isNotEmpty(trs)){
				map.put("trslk", trs);
			} else {
				map.put("trslk", "");
			}
			String url = ftsDocument.getUrlName();
			if (StringUtil.isNotEmpty(url)) {
				map.put("url", url);
			} else {
				map.put("url", "");
			}


			String docContent = ftsDocument.getContent();
			String[] imaUrls = null;
			String imaUrl = "";

			if (docContent != null){
				imaUrls = docContent.split("IMAGE&nbsp;SRC=&quot;");
				if (imaUrls.length>1){
					imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
				}
			}
			map.put("imageUrl",imaUrl);

			map.put("siteName", ftsDocument.getSiteName());
//			String title = ftsDocument.getUrlTitle();
			String title = StringUtil.replaceNRT(StringUtil.removeFourChar(ftsDocument.getUrlTitle()));
			title = StringUtil.replaceEmoji(title);
			if (StringUtil.isNotEmpty(title)) {
//				if (title.length() > Const.ALERT_NUM) {
//					title = title.substring(0, Const.ALERT_NUM) + "...";
//				}
				map.put("titleWhole",title);
				title = StringUtil.calcuCutLength(title, Const.ALERT_NUM);
				map.put("title", title);
			} else {
				map.put("title", "");
			}
			String source = "微信";
			map.put("author",ftsDocument.getAuthors());
			map.put("md5", ftsDocument.getMd5Tag());
			map.put("appraise", ftsDocument.getAppraise());
			map.put("size", String.valueOf(ftsQuery.size()));
			map.put("sid", ftsDocument.getHkey());
			map.put("source", source);
			// 超过150 那就只存150个字
//			String documentContent = ftsDocument.getContent();
			String documentContent = StringUtil.removeFourChar(ftsDocument.getContent());
			documentContent =StringUtil.replaceEmoji(documentContent);
			documentContent =StringUtil.replaceImg(documentContent) ;
//			if (StringUtils.isNotBlank(documentContent)) {
//				documentContent = documentContent.length() > 150 ? documentContent.substring(0, 140) + "..."
//						: documentContent;
//			}
			documentContent = StringUtil.notFontEnd(documentContent, 150);
			map.put("content", documentContent);
			Date urlTime = ftsDocument.getUrlTime();
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
			if (ObjectUtil.isNotEmpty(urlTime)) {
				String str = sdf.format(urlTime);
				map.put("urlTime", str);
			} else {
				map.put("urlTime", "");
			}
			map.put("groupName", "微信");
			list.add(map);
			bufferSid.append(ftsDocument.getHkey()).append(" OR ");
		}
		Map<String, Object> mapList = new HashMap<>();
		// notice工程接收的参数
		mapList.put("listMap", list);
		mapList.put("size", ftsQuery.size());
		// 预警标题
		mapList.put("title", content);
		if (bufferSid.length() > 4) {
			bufferSid.delete(bufferSid.length() - 4, bufferSid.length());
		}
		mapList.put("sids", bufferSid.toString());
		String[] receive = receiver.split(";");
		String[] sendWay = send.split(";");
		for (int i = 0; i < receive.length; i++) {
			SendWay sendValue = SendWay.valueOf(sendWay[i]);
			noticeSendService.sendAll(sendValue, "mailmess2.ftl", content, mapList, receive[i], userId,AlertSource.ARTIFICIAL);
		}
		// 当前用户名
		String user_ = userService.findById(userId).getUserName();
		// 发给自己站内
		// noticeSendService.sendAll(SendWay.SMS, "mailmess2.ftl", content,
		// mapList, user_, userId);
		return "success";
	}

	/**
	 * 查微博库发预警 由于映射实体不一样 所以传统 微信 微博分别写一个方法 CreatedBy xiao.ying
	 * 
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	private Object weiboSend(QueryBuilder queryBuilder, String content, String userId, String receiver, String send,boolean irSimflag,boolean irSimflagAll )
			throws TRSSearchException, TRSException {
		List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentStatus.class, false,irSimflag,irSimflagAll,null );
		String trs = queryBuilder.getAppendTRSL().toString();
		List<Map<String, String>> list = new ArrayList<>();
		StringBuffer bufferSid = new StringBuffer();
		for (FtsDocumentStatus ftsDocument : ftsQuery) {
			Map<String, String> map = new HashMap<>();
			if(StringUtil.isNotEmpty(trs)){
				map.put("trslk", trs);
			} else {
				map.put("trslk", "");
			}
			String url = ftsDocument.getUrlName();
			if (StringUtil.isNotEmpty(url)) {
				map.put("url", url);
			} else {
				map.put("url", "");
			}
			String docContent = ftsDocument.getStatusContent();
			String[] imaUrls = null;
			String imaUrl = "";

			if (docContent != null){
				imaUrls = docContent.split("IMAGE&nbsp;SRC=&quot;");
				if (imaUrls.length>1){
					imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
				}
			}
			map.put("imageUrl",imaUrl);
			map.put("siteName", ftsDocument.getSiteName());
			map.put("author",ftsDocument.getAuthors());
//			String title = ftsDocument.getStatusContent();
//			String title = StringUtil.replaceNRT(StringUtil.removeFourChar(ftsDocument.getUrlTitle()));
//			title = StringUtil.replaceEmoji(title);
			String title = StringUtil.removeFourChar(ftsDocument.getStatusContent());
			title =StringUtil.replaceEmoji(title);
			title =StringUtil.replaceImg(title) ;
			if (StringUtil.isNotEmpty(title)) {
//				if (title.length() > Const.ALERT_NUM) {
//					title = title.substring(0, Const.ALERT_NUM) + "...";
//				}
				map.put("titleWhole",title);
				title = StringUtil.calcuCutLength(title, Const.ALERT_NUM);
				map.put("title", title);
			} else {
				map.put("title", "");
			}
			String source = "微博";
			map.put("md5", ftsDocument.getMd5Tag());
			map.put("appraise", ftsDocument.getAppraise());
			map.put("size", String.valueOf(ftsQuery.size()));
			map.put("sid", ftsDocument.getMid());
			map.put("source", source);
			// 超过150 那就只存150个字
//			String documentContent = ftsDocument.getStatusContent();
//			documentContent = StringUtil.replaceImg(documentContent);
			String documentContent = StringUtil.removeFourChar(ftsDocument.getStatusContent());
			documentContent =StringUtil.replaceEmoji(documentContent);
			documentContent =StringUtil.replaceImg(documentContent) ;
//			if (StringUtils.isNotBlank(documentContent)) {
//				documentContent = documentContent.length() > 150 ? documentContent.substring(0, 140) + "..."
//						: documentContent;
//			}
			documentContent = StringUtil.notFontEnd(documentContent, 150);
			map.put("content", documentContent);
			map.put("screenName", ftsDocument.getScreenName());
			map.put("rttCount", String.valueOf(ftsDocument.getRttCount()));
			map.put("commtCount", String.valueOf(ftsDocument.getCommtCount()));
			map.put("retweetedMid",ftsDocument.getRetweetedMid());
			Date urlTime = ftsDocument.getCreatedAt();
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
			if (ObjectUtil.isNotEmpty(urlTime)) {
				String str = sdf.format(urlTime);
				map.put("urlTime", str);
			} else {
				map.put("urlTime", "");
			}
			map.put("groupName", "微博");
			list.add(map);
			bufferSid.append(ftsDocument.getMid()).append(" OR ");
		}
		Map<String, Object> mapList = new HashMap<>();
		// notice工程接收的参数
		mapList.put("listMap", list);
		mapList.put("size", ftsQuery.size());
		// 预警标题
		mapList.put("title", content);
		if (bufferSid.length() > 4) {
			bufferSid.delete(bufferSid.length() - 4, bufferSid.length());
		}
		mapList.put("sids", bufferSid.toString());
		String[] receive = receiver.split(";");
		String[] sendWay = send.split(";");
		for (int i = 0; i < receive.length; i++) {
			SendWay sendValue = SendWay.valueOf(sendWay[i]);
			noticeSendService.sendAll(sendValue, "mailmess2.ftl", content, mapList, receive[i], userId,AlertSource.ARTIFICIAL);
		}
		// 当前用户名
		String user_ = userService.findById(userId).getUserName();
		// 发给自己站内
		// noticeSendService.sendAll(SendWay.SMS, "mailmess2.ftl", content,
		// mapList, user_, userId);
		return "success";
		// return noticeSendService.sendAll(send, "mailmess2.ftl", content,
		// mapList, receiver, userId);
	}
	
	/**
	 * 查微博库发预警 由于映射实体不一样 所以传统 微信 微博分别写一个方法 CreatedBy xiao.ying
	 * 
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	private Object tfSend(QueryBuilder queryBuilder, String content, String userId, String receiver, String send,boolean irSimflag,boolean irSimflagAll )
			throws TRSSearchException, TRSException {
		List<FtsDocumentTF> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentTF.class, false,irSimflag,irSimflagAll,null );
		String trs = queryBuilder.getAppendTRSL().toString();
		List<Map<String, String>> list = new ArrayList<>();
		StringBuffer bufferSid = new StringBuffer();
		for (FtsDocumentTF ftsDocument : ftsQuery) {
			Map<String, String> map = new HashMap<>();
			if(StringUtil.isNotEmpty(trs)){
				map.put("trslk", trs);
			} else {
				map.put("trslk", "");
			}
			String url = ftsDocument.getUrlName();
			if (StringUtil.isNotEmpty(url)) {
				map.put("url", url);
			} else {
				map.put("url", "");
			}

			String docContent = ftsDocument.getContent();
			String[] imaUrls = null;
			String imaUrl = "";

			if (docContent != null){
				imaUrls = docContent.split("IMAGE&nbsp;SRC=&quot;");
				if (imaUrls.length>1){
					imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
				}
			}
			map.put("imageUrl",imaUrl);

			map.put("siteName", ftsDocument.getSiteName());
			map.put("author",ftsDocument.getAuthors());
//			String title = ftsDocument.getStatusContent();
			String title = StringUtil.removeFourChar(ftsDocument.getStatusContent());
			title =StringUtil.replaceEmoji(title);
			title =StringUtil.replaceImg(title) ;
			if (StringUtil.isNotEmpty(title)) {
//				if (title.length() > Const.ALERT_NUM) {
//					title = title.substring(0, Const.ALERT_NUM) + "...";
//				}
				map.put("titleWhole",title);
				title = StringUtil.calcuCutLength(title, Const.ALERT_NUM);
				map.put("title", title);
			} else {
				map.put("title", "");
			}
			String source = ftsDocument.getGroupName();
			map.put("md5", ftsDocument.getMd5Tag());
			map.put("appraise", ftsDocument.getAppraise());
			map.put("size", String.valueOf(ftsQuery.size()));
			map.put("sid", ftsDocument.getMid());
			map.put("source", source);
			// 超过150 那就只存150个字
//			String documentContent = ftsDocument.getStatusContent();
//			documentContent = StringUtil.replaceImg(documentContent);
			String documentContent = StringUtil.removeFourChar(ftsDocument.getStatusContent());
			documentContent =StringUtil.replaceEmoji(documentContent);
			documentContent =StringUtil.replaceImg(documentContent) ;
//			if (StringUtils.isNotBlank(documentContent)) {
//				documentContent = documentContent.length() > 150 ? documentContent.substring(0, 140) + "..."
//						: documentContent;
//			}
			documentContent = StringUtil.notFontEnd(documentContent, 150);
			map.put("content", documentContent);
			map.put("screenName", ftsDocument.getScreenName());
			map.put("rttCount", String.valueOf(ftsDocument.getRttCount()));
			map.put("commtCount", String.valueOf(ftsDocument.getCommtCount()));
			map.put("retweetedMid",ftsDocument.getRetweetedMid());
			Date urlTime = ftsDocument.getCreatedAt();
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
			if (ObjectUtil.isNotEmpty(urlTime)) {
				String str = sdf.format(urlTime);
				map.put("urlTime", str);
			} else {
				map.put("urlTime", "");
			}
			map.put("groupName", ftsDocument.getGroupName());
			list.add(map);
			bufferSid.append(ftsDocument.getSid()).append(" OR ");
		}
		Map<String, Object> mapList = new HashMap<>();
		// notice工程接收的参数
		mapList.put("listMap", list);
		mapList.put("size", ftsQuery.size());
		// 预警标题
		mapList.put("title", content);
		if (bufferSid.length() > 4) {
			bufferSid.delete(bufferSid.length() - 4, bufferSid.length());
		}
		mapList.put("sids", bufferSid.toString());
		String[] receive = receiver.split(";");
		String[] sendWay = send.split(";");
		for (int i = 0; i < receive.length; i++) {
			SendWay sendValue = SendWay.valueOf(sendWay[i]);
			noticeSendService.sendAll(sendValue, "mailmess2.ftl", content, mapList, receive[i], userId,AlertSource.ARTIFICIAL);
		}
		// 当前用户名
		String user_ = userService.findById(userId).getUserName();
		// 发给自己站内
		// noticeSendService.sendAll(SendWay.SMS, "mailmess2.ftl", content,
		// mapList, user_, userId);
		return "success";
		// return noticeSendService.sendAll(send, "mailmess2.ftl", content,
		// mapList, receiver, userId);
	}

	/**
	 * 查看相似文章数
	 * 
	 * @param md5
	 * @return CreatedBy xiao.ying
	 */
	public int md5(String md5) {
		String trslMd5 = "MD5TAG:" + md5;
		QueryBuilder queryMd5 = new QueryBuilder();
		queryMd5.filterByTRSL(trslMd5);
		queryMd5.setDatabase(Const.HYBASE_NI_INDEX);
		// long转int
		int ftsCount = (int) hybase8SearchService.ftsCount(queryMd5, true,false,false,null);
		return ftsCount;
	}

	@Override
	public AlertRule addAlertRule(AlertRule alertRule) {
		// int alertEnd = alertRule.getAlertEndHour();
		// int alertStart = alertRule.getAlertStartHour();
		// if (alertStart > alertEnd) {
		// return null;
		// }
		// 存入数据库
		alertRule = alertRuleRepository.saveAndFlush(alertRule);

		alertRuleBackupsService.add(AlertRuleBackups.getAlertRuleBackups(alertRule));
		return alertRule;
	}

	@Override
	public List<AlertRule> selectAll(User user) {
		List<AlertRule> countList;
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			// 按新建时间排序
			countList = alertRuleRepository.findByUserId(user.getId(), new Sort(Direction.DESC, "createdTime"));

		}else {
			// 按新建时间排序
			countList = alertRuleRepository.findBySubGroupId(user.getSubGroupId(), new Sort(Direction.DESC, "createdTime"));

		}
		return countList;
	}

	@Override
	public List<AlertRule> selectType(AlertSource alertSource) {
		Criteria<AlertRule> criteria = new Criteria<>();
		List<AlertRule> list = null;
		try {
			criteria.add(Restrictions.eq("alertSource", alertSource));
			list = alertRuleRepository.findAll(criteria);
			return list;
		} catch (Exception e) {
			log.error("未能按条件查询", e);
			return list;
		}
	}

	/**
	 * 根据md5查询相似文章数
	 * 
	 * @param md5Str
	 * @return
	 */
	@SuppressWarnings("unused")
	private int md5Count(String md5Str) {
		String trslMd5 = "MD5TAG:" + md5Str;
		QueryBuilder queryMd5 = new QueryBuilder();
		queryMd5.filterByTRSL(trslMd5);
		queryMd5.setDatabase(Const.HYBASE_NI_INDEX);
		// long转int
		int ftsCount = (int) hybase8SearchService.ftsCount(queryMd5, true,false,false,null);
		return ftsCount;
	}

	@Override
	public AlertRule save(AlertRule alertRule) {
		return alertRuleRepository.save(alertRule);
	}

	@Override
	public void delete(String ruleId) throws OperationException {
		//现在的逻辑是  只删除规则 不删除该规则下的历史预警
		/*if(httpClient){
			//跨工程删除该规则产生的预警结果
			alertRuleRepository.delete(ruleId);
			try {
			 	String url = alertNetinsightUrl+"/alert/deleteByRule?ruleId="+ruleId;
		        String doGet = HttpUtil.doGet(url, null);
		        if(StringUtil.isEmpty(doGet)){
		        	throw new OperationException("预警删除失败" ,new Exception());
				}else if(doGet.contains("\"code\":500")){
					Map<String,String> map = (Map<String,String>)JSON.parse(doGet);
					String message = map.get("message");
					throw new OperationException("预警删除失败:"+message ,new Exception());
				}
			} catch (Exception e) {
				throw new OperationException("预警删除失败,message:" + e,e);
			}
		}else{*/
			//删除该规则产生的预警结果
			alertRuleRepository.delete(ruleId);
			//删除该规则 对应的预警备份表记录
			alertRuleBackupsService.deleteByAlertRuleId(ruleId);
//		}
	}

	@Override
	public void deleteByUserId(String userId){
		List<AlertRule> alertRules = alertRuleRepository.findByUserId(userId);
		if (ObjectUtil.isNotEmpty(alertRules)){
			alertRuleRepository.delete(alertRules);
			alertRuleRepository.flush();
		}
	}

	@Override
	public AlertRule findOne(String id) {
		return alertRuleRepository.findOne(id);
	}



	@Override
	public Page<AlertRule> pageList(User user, int pageNo, int pageSize) {
		Sort sort = new Sort(Direction.DESC, "lastModifiedTime");
		Pageable pageable = new PageRequest(pageNo, pageSize, sort);
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			return alertRuleRepository.findByUserId(user.getId(), pageable);
		}else {
			return alertRuleRepository.findBySubGroupId(user.getSubGroupId(), pageable);
		}
	}

//	@Override
//	public List<AlertRule> findByStatusAndAlertType(ScheduleStatus open, AlertSource auto) {
//		return alertRuleRepository.findByStatusAndAlertType(open, auto);
//	}
	@Override
	public List<AlertRule> findByStatusAndAlertTypeAndFrequencyId(ScheduleStatus open, AlertSource auto,String frequencyId) {
		return alertRuleRepository.findByStatusAndAlertTypeAndFrequencyId(open, auto,frequencyId);
	}

	@Override
	public AlertRule update(AlertRule alertRule, boolean choose) {
		AlertRule rule = alertRuleRepository.save(alertRule);
		if (choose) {
			alertRuleBackupsService.add(AlertRuleBackups.getAlertRuleBackups(alertRule));
		}
		return rule;
	}

	@Override
	public Object weChatSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String keywords,String fuzzyValueScope, String notKeyWords, String keyWordIndex)
			throws Exception {
		log.warn("专项检测信息列表，微信，  开始调用接口");
		QueryBuilder builder = new QueryBuilder();
		QueryBuilder countBuilder = new QueryBuilder();// 展示总数的
		builder.setPageNo(pageNo);
		builder.setPageSize(pageSize);
		User loginUser = UserUtils.getUser();
		boolean irSimflag = false;
		boolean irSimflagAll = false;
		if (alertRule!=null) {
//			AlertRule alertRule = alertRuleRepository.findOne(id);
//			if (alertRule != null) {
				irSimflag = alertRule.isIrSimflag();
				irSimflagAll = alertRule.isIrSimflagAll();
				builder = alertRule.toSearchBuilderWeiXin(time);
				countBuilder = alertRule.toSearchBuilderWeiXin(time);
				builder.setPageNo(pageNo);
				builder.setPageSize(pageSize);
				countBuilder.setPageNo(pageNo);
				countBuilder.setPageSize(pageSize);
//			} else {
//				// 时间
//				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
//				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
//			}

		} else {
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
		}
		if (!"ALL".equals(area)) { // 地域

			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			countBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
		}
		if (!"ALL".equals(emotion)) { // 情感
			builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
		}
		// 结果中搜索
		if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
			String[] split = keywords.split(",");
			String splitNode = "";
			for (int i = 0; i < split.length; i++) {
				if (StringUtil.isNotEmpty(split[i])) {
					splitNode += split[i] + ",";
				}
			}
			keywords = splitNode.substring(0, splitNode.length() - 1);
			if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
					|| keywords.endsWith("，")) {
				keywords = keywords.substring(0, keywords.length() - 1);

			}
			StringBuilder fuzzyBuilder  = new StringBuilder();
			String hybaseField = "fullText";
			switch (fuzzyValueScope){
				case "title":
					hybaseField = FtsFieldConst.FIELD_URLTITLE;
					break;
				case "source":
					hybaseField = FtsFieldConst.FIELD_SITENAME;
					break;
				case "author":
					hybaseField = FtsFieldConst.FIELD_AUTHORS;
					break;
			}
			if("fullText".equals(hybaseField)){
				fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			builder.filterByTRSL(fuzzyBuilder.toString());
			countBuilder.filterByTRSL(fuzzyBuilder.toString());
			log.info(builder.asTRSL());
		}
		//拼接排除词
		if (StringUtil.isNotEmpty(notKeyWords) ) {
			if("positioCon".equals(keyWordIndex)){
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				builder.filterByTRSL(exbuilder.toString());
				countBuilder.filterByTRSL(exbuilder.toString());
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}else {
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}

		}
		log.info(builder.asTRSL());
		switch (sort) { // 排序
		case "desc":
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			break;
		case "asc":
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			break;
		case "hot":
			return infoListService.getHotListWeChat(builder, countBuilder, loginUser,"alert");
		case "relevance"://相关性排序
			builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
		default:
			if(alertRule!=null){
				if(alertRule.isWeight()){
					builder.setOrderBy("-"+FtsFieldConst.FIELD_RELEVANCE+";-"+FtsFieldConst.FIELD_URLTIME);
				}else{
					builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
				}
			}else{
				builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
			}
			break;
		}
		countBuilder.setDatabase(Const.WECHAT);
		// 把查询结果和数据库里边的sid对比 标记此条是否发过 用户id时间作为条件去数据库里查
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String[] formatTimeRange = DateUtil.formatTimeRange(time);
		Date parseStart = sdf.parse(formatTimeRange[0]);
		Date parseEnd = sdf.parse(formatTimeRange[1]);
		// Criteria<AlertEntity> criteria = new Criteria<>();
		// criteria.add(Restrictions.between("time", parseStart, parseEnd));
		// criteria.add(Restrictions.eq("userId", userId));
		// List<AlertEntity> findAll = alertService.findAll(criteria);
		InfoListResult<FtsDocumentWeChat> docList = infoListService.getWeChatList(builder, loginUser,alertRule.isRepetition(),irSimflag,irSimflagAll,false,"alert");
		List<FtsDocumentWeChat> pageItems = null;
		// if (docList != null) {
		// PagedList<FtsDocumentWeChat> content = (PagedList<FtsDocumentWeChat>)
		// docList.getContent();
		// if (content != null) {
		// pageItems = content.getPageItems();
		// }
		// }
		// if (ObjectUtil.isNotEmpty(findAll)) {
		// if (pageItems != null) {
		// for (FtsDocumentWeChat wechat : pageItems) {
		// String sid = wechat.getHkey();
		// wechat.setSend(findAll.stream().anyMatch(alert ->
		// alert.getSid().equals(sid)));
		// }
		// }
		// }
		return docList;
	}

	@Override
	public Object statusSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String keywords,String fuzzyValueScope, String notKeyWords, String keyWordIndex,String forwarPrimary)
			throws Exception {
		log.warn("专项检测信息列表，微博，  开始调用接口");
		QueryBuilder builder = new QueryBuilder();
		builder.setPageNo(pageNo);
		builder.setPageSize(pageSize);
		QueryBuilder countBuilder = new QueryBuilder();
		countBuilder.setPageNo(pageNo);
		countBuilder.setPageSize(pageSize);
		User loginUser = UserUtils.getUser();
		boolean irSimflag = false;
		boolean irSimflagAll = false;
		if (alertRule != null) {
				irSimflag = alertRule.isIrSimflag();
				irSimflagAll = alertRule.isIrSimflagAll();
				builder = alertRule.toSearchBuilderWeiBo(time);
				countBuilder = alertRule.toSearchBuilderWeiBo(time);
				builder.setPageNo(pageNo);
				builder.setPageSize(pageSize);
				countBuilder.setPageNo(pageNo);
				countBuilder.setPageSize(pageSize);

		} else {
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
		}
		if (!"ALL".equals(area)) { // 地域

			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			countBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
		}
		if (!"ALL".equals(emotion)) { // 情感
			builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
		}
		// 转发 / 原发
		String builderTRSL = builder.asTRSL();
		String builderDatabase = builder.getDatabase();
		String countTRSL = countBuilder.asTRSL();
		String countBuilerDatabase = countBuilder.getDatabase();
		StringBuilder builderTrsl = new StringBuilder(builderTRSL);
		StringBuilder countBuilderTrsl = new StringBuilder(countTRSL);
		if ("primary".equals(forwarPrimary)) {
			// 原发
			builder.filterByTRSL(Const.PRIMARY_WEIBO);
			countBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
		} else if ("forward".equals(forwarPrimary)) {
			// 转发
			builder = new QueryBuilder();
			countBuilder = new QueryBuilder();

			builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
			builder.filterByTRSL(builderTrsl.toString());

			builder.setDatabase(builderDatabase);
			builder.setPageSize(pageSize);
			builder.setPageNo(pageNo);

			countBuilderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
			countBuilder.filterByTRSL(countBuilderTrsl.toString());
			countBuilder.setDatabase(countBuilerDatabase);
			countBuilder.setPageSize(pageSize);
			countBuilder.setPageNo(pageNo);
		}
		//转发 /  原发
		/*if ("primary".equals(forwarPrimary)){
			//原发
			builder.filterField(FtsFieldConst.IR_RETWEETED_MID,"0",Operator.Equal);
			countBuilder.filterField(FtsFieldConst.IR_RETWEETED_MID,"0",Operator.Equal);
		}else if ("forward".equals(forwarPrimary)){
			//转发
			builder.filterField(FtsFieldConst.IR_RETWEETED_MID,"0",Operator.NotEqual);
			countBuilder.filterField(FtsFieldConst.IR_RETWEETED_MID,"0",Operator.NotEqual);
		}*/
		// 结果中搜索
		if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
			String[] split = keywords.split(",");
			String splitNode = "";
			for (int i = 0; i < split.length; i++) {
				if (StringUtil.isNotEmpty(split[i])) {
					splitNode += split[i] + ",";
				}
			}
			keywords = splitNode.substring(0, splitNode.length() - 1);
			if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
					|| keywords.endsWith("，")) {
				keywords = keywords.substring(0, keywords.length() - 1);

			}
			StringBuilder fuzzyBuilder  = new StringBuilder();
			String hybaseField = "fullText";
			switch (fuzzyValueScope){
				case "title":
					hybaseField = FtsFieldConst.FIELD_URLTITLE;
					break;
				case "source":
					hybaseField = FtsFieldConst.FIELD_SITENAME;
					break;
				case "author":
					hybaseField = FtsFieldConst.FIELD_AUTHORS;
					break;
			}
			if("fullText".equals(hybaseField)){
				fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			builder.filterByTRSL(fuzzyBuilder.toString());
			countBuilder.filterByTRSL(fuzzyBuilder.toString());
			log.info(builder.asTRSL());
		}
		//拼接排除词
		if (StringUtil.isNotEmpty(notKeyWords) ) {
			if("positioCon".equals(keyWordIndex)){
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				builder.filterByTRSL(exbuilder.toString());
				countBuilder.filterByTRSL(exbuilder.toString());
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}else {
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}

		}

		log.info(builder.asTRSL());
		switch (sort) { // 排序
		case "desc":
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			break;
		case "asc":
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			break;
		case "hot":
			return infoListService.getHotListStatus(builder, countBuilder, loginUser,"alert");
		case "relevance"://相关性排序
			builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
		default:
			if(alertRule!=null){
				if(alertRule.isWeight()){
					builder.setOrderBy("-"+FtsFieldConst.FIELD_RELEVANCE+";-"+FtsFieldConst.FIELD_URLTIME);
				}else{
					builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
				}
			}else{
				builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
			}
			break;
		}
		countBuilder.setDatabase(Const.WEIBO);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String[] formatTimeRange = DateUtil.formatTimeRange(time);
		Date parseStart = sdf.parse(formatTimeRange[0]);
		Date parseEnd = sdf.parse(formatTimeRange[1]);
		// Criteria<AlertEntity> criteria = new Criteria<>();
		// criteria.add(Restrictions.between("time", parseStart, parseEnd));
		// criteria.add(Restrictions.eq("userId", userId));
		// List<AlertEntity> findAll = alertService.findAll(criteria);
		InfoListResult<FtsDocumentStatus> docList = infoListService.getStatusList(builder, loginUser,alertRule.isRepetition(),irSimflag,irSimflagAll,false,"alert");
		// List<FtsDocumentStatus> content = (List<FtsDocumentStatus>)
		// statusList.getContent();
		// List<FtsDocumentStatus> pageItems = null;
		// if (docList != null) {
		// PagedList<FtsDocumentStatus> content = (PagedList<FtsDocumentStatus>)
		// docList.getContent();
		// if (content != null) {
		// pageItems = content.getPageItems();
		// }
		// }
		// if (ObjectUtil.isNotEmpty(findAll)) {
		// if (pageItems != null) {
		// for (FtsDocumentStatus wechat : pageItems) {
		// String sid = wechat.getMid();
		// wechat.setSend(findAll.stream().anyMatch(alert ->
		// alert.getSid().equals(sid)));
		// }
		// }
		// }
		return docList;
	}

	@Override
	public Object documentSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String invitationCard, String keywords, String fuzzyValueScope,String notKeyWords,
			String keyWordIndex) throws Exception {
		log.error(source + "信息列表  开始调用接口:" + System.currentTimeMillis());
		QueryBuilder builder = new QueryBuilder();
		builder.setPageNo(pageNo);
		builder.setPageSize(pageSize);
		QueryBuilder countBuilder = new QueryBuilder();
		User loginUser = UserUtils.getUser();
		boolean simflag = true;
		boolean irSimflag = true;
		boolean irSimflagAll = true;
		if (alertRule != null) {
			simflag = alertRule.isRepetition();
			irSimflag = alertRule.isIrSimflag();
			irSimflagAll = alertRule.isIrSimflagAll();
			builder = alertRule.toSearchBuilder(time);
			countBuilder = alertRule.toSearchBuilder(time);
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			countBuilder.setPageNo(pageNo);
			countBuilder.setPageSize(pageSize);
		} else {
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
		}
		// 来源
		if (!"ALL".equals(source)) {
			// 单选状态
			if ("国内新闻".equals(source)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 ").toString();
				builder.filterByTRSL(trsl);
				countBuilder.filterByTRSL(trsl);
			} else if ("国内论坛".equals(source)) {
				StringBuffer sb = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 ");
				if ("0".equals(invitationCard)) {// 主贴
					sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
				} else if ("1".equals(invitationCard)) {// 回帖
					sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
				}
				builder.filterByTRSL(sb.toString());
				countBuilder.filterByTRSL(sb.toString());
			} else {
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
			}
		}

		if (!"ALL".equals(area)) { // 地域
			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			countBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
		}
		if (!"ALL".equals(industry)) { // 行业
			builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(emotion)) { // 情感
			builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
		}
		// 结果中搜索
		if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
			String[] split = keywords.split(",");
			String splitNode = "";
			for (int i = 0; i < split.length; i++) {
				if (StringUtil.isNotEmpty(split[i])) {
					splitNode += split[i] + ",";
				}
			}
			keywords = splitNode.substring(0, splitNode.length() - 1);
			if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
					|| keywords.endsWith("，")) {
				keywords = keywords.substring(0, keywords.length() - 1);

			}
			StringBuilder fuzzyBuilder  = new StringBuilder();
			String hybaseField = "fullText";
			switch (fuzzyValueScope){
				case "title":
					hybaseField = FtsFieldConst.FIELD_URLTITLE;
					break;
				case "source":
					hybaseField = FtsFieldConst.FIELD_SITENAME;
					break;
				case "author":
					hybaseField = FtsFieldConst.FIELD_AUTHORS;
					break;
			}
			if("fullText".equals(hybaseField)){
				fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			builder.filterByTRSL(fuzzyBuilder.toString());
			countBuilder.filterByTRSL(fuzzyBuilder.toString());
			log.info(builder.asTRSL());
		}
		//拼接排除词
		if (StringUtil.isNotEmpty(notKeyWords) ) {
			if("positioCon".equals(keyWordIndex)){
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				builder.filterByTRSL(exbuilder.toString());
				countBuilder.filterByTRSL(exbuilder.toString());
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}else {
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}

		}

		log.info(builder.asTRSL());
		switch (sort) { // 排序
		case "desc":
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			break;
		case "asc":
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			break;
		case "hot":
			return infoListService.getHotList(builder, countBuilder, loginUser,"alert");
		case "relevance"://相关性排序
			builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
		default:
			if(alertRule!=null){
				if(alertRule.isWeight()){
					builder.setOrderBy("-"+FtsFieldConst.FIELD_RELEVANCE+";-"+FtsFieldConst.FIELD_URLTIME);
				}else{
					builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
				}
			}else{
				builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
			}
			break;
		}
		countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		log.info(builder.asTRSL());
		log.error("开始查询海贝:" + System.currentTimeMillis());
		InfoListResult<FtsDocument> docList = infoListService.getDocList(builder, loginUser, simflag,irSimflag,irSimflagAll,false,"alert");
		log.error("查询完成:" + System.currentTimeMillis());
		log.error("方法返回:" + System.currentTimeMillis());
		return docList;
	}
	
	@Override
	public Object documentTFSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String keywords,String fuzzyValueScope, String notKeyWords,
			String keyWordIndex) throws Exception {
		log.error(source + "信息列表  开始调用接口:" + System.currentTimeMillis());
		QueryBuilder builder = new QueryBuilder();
		builder.setPageNo(pageNo);
		builder.setPageSize(pageSize);
		QueryBuilder countBuilder = new QueryBuilder();
		User loginUser = UserUtils.getUser();
		boolean simflag = true;
		boolean irsimflag = false;
		boolean irSimflagAll = false;
		if (null!=alertRule) {
			simflag = alertRule.isRepetition();
			irsimflag = alertRule.isIrSimflag();
			irSimflagAll = alertRule.isIrSimflagAll();
			builder = alertRule.toSearchBuilder(time);
			countBuilder = alertRule.toSearchBuilder(time);
			builder.setDatabase(Const.HYBASE_OVERSEAS);
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			countBuilder.setPageNo(pageNo);
			countBuilder.setPageSize(pageSize);
		} else {
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
		}
		// 来源
		builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
		countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);

		// 结果中搜索
		if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
			String[] split = keywords.split(",");
			String splitNode = "";
			for (int i = 0; i < split.length; i++) {
				if (StringUtil.isNotEmpty(split[i])) {
					splitNode += split[i] + ",";
				}
			}
			keywords = splitNode.substring(0, splitNode.length() - 1);
			if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
					|| keywords.endsWith("，")) {
				keywords = keywords.substring(0, keywords.length() - 1);

			}
			StringBuilder fuzzyBuilder  = new StringBuilder();
			String hybaseField = "fullText";
			switch (fuzzyValueScope){
				case "title":
					hybaseField = FtsFieldConst.FIELD_URLTITLE;
					break;
				case "source":
					hybaseField = FtsFieldConst.FIELD_SITENAME;
					break;
				case "author":
					hybaseField = FtsFieldConst.FIELD_AUTHORS;
					break;
			}
			if("fullText".equals(hybaseField)){
				fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			builder.filterByTRSL(fuzzyBuilder.toString());
			countBuilder.filterByTRSL(fuzzyBuilder.toString());
			log.info(builder.asTRSL());
		}
		//拼接排除词
		if (StringUtil.isNotEmpty(notKeyWords) ) {
			if("positioCon".equals(keyWordIndex)){
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				builder.filterByTRSL(exbuilder.toString());
				countBuilder.filterByTRSL(exbuilder.toString());
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}else {
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}

		}
		
		log.info(builder.asTRSL());
		switch (sort) { // 排序
		case "desc":
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			break;
		case "asc":
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			break;
		case "relevance"://相关性排序
			builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
		default:
			if(alertRule!=null){
				if(alertRule.isWeight()){
					builder.setOrderBy("-"+FtsFieldConst.FIELD_RELEVANCE+";-"+FtsFieldConst.FIELD_URLTIME);
				}else{
					builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
				}
			}else{
				builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
			}
			break;
		}
		countBuilder.setDatabase(Const.HYBASE_OVERSEAS);
		InfoListResult<FtsDocumentTF> docList = infoListService.getDocTFList(builder, loginUser, simflag,irsimflag,irSimflagAll,"alert");
		return docList;
	}
	@Override
	public Object documentCommonSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
								 String industry, String emotion, String sort, String invitationCard,String forwarPrimary, String keywords,String fuzzyValueScope, String notKeyWords,
								 String keyWordIndex,Boolean isExport) throws Exception {
		log.error(source + "信息列表  开始调用接口:" + System.currentTimeMillis());
		QueryCommonBuilder builder = new QueryCommonBuilder();
		builder.setPageNo(pageNo);
		builder.setPageSize(pageSize);
		QueryCommonBuilder countBuilder = new QueryCommonBuilder();
		User loginUser = UserUtils.getUser();
		boolean simflag = true;
		boolean irSimflag = true;
		boolean irSimflagAll = true;
		if (alertRule != null) {
			simflag = alertRule.isRepetition();
			irSimflag = alertRule.isIrSimflag();
			irSimflagAll = alertRule.isIrSimflagAll();
			builder = alertRule.toSearchBuilderCommon(time);
			countBuilder = alertRule.toSearchBuilderCommon(time);
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			countBuilder.setPageNo(pageNo);
			countBuilder.setPageSize(pageSize);
		} else {
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
		}
		// 来源
		//注释原因  - > 添加主回帖、原转发筛选，需要对来源修改，且判断语句存在部分问题，专家模式有数据源，也可添加数据源 互相为AND关系，不影响
		/*if ("ALL".equals(source) && SpecialType.COMMON.equals(alertRule.getSpecialType())) {
			//需要判断是否是专家模式，专家模式在拼接表达式时已经添加了groupname
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.STATTOTAL_GROUP.replaceAll(";"," OR "), Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.STATTOTAL_GROUP.replaceAll(";"," OR "), Operator.Equal);
		}*/
		if("ALL".equals(source)){
			String groupName = alertRule.getGroupName();
			if(StringUtil.isEmpty(groupName) || "ALL".equals(groupName) || SpecialType.SPECIAL.equals(alertRule.getSpecialType())){
				source = Const.STATTOTAL_GROUP;
			}else{
				String[] groupArr = groupName.split(";");
				List<String> groupList = new ArrayList<>();
				for(String group : groupArr){
					group = Const.DATA_SOURCES.get(group);
					groupList.add(group);
				}
				source = StringUtils.join(groupList,";");
			}
		}
		if (source.contains("微信") && !source.contains("国内微信")){
			source = source.replaceAll("微信","国内微信");
		}
		if((source.contains("国内论坛") && StringUtil.isNotEmpty(invitationCard)) || ( source.contains("微博") && StringUtil.isNotEmpty(forwarPrimary))){
			//只有包含微博和论坛的情况下才会出现主贴和回帖，其他时候无意义
			//这段代码要写在添加groupName之前，因为主回帖和原转发都是特性，主要把grouname的论坛和微博拿出来，单独用OR拼接，否则回帖时其他类型数据查不到
			StringBuffer sb = new StringBuffer();
			if(source.contains("微博") && StringUtil.isNotEmpty(forwarPrimary) ){
				sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(\"微博\")");
				if ("primary".equals(forwarPrimary)) {
					// 原发
					sb.append(" AND ").append(Const.PRIMARY_WEIBO);
				}else  if ("forward".equals(forwarPrimary)){
					//转发
					sb.append(" NOT ").append(Const.PRIMARY_WEIBO);
				}

				sb.append(")");
				source = source.replaceAll(";微博","").replaceAll("微博;","");
			}
			if(source.contains("国内论坛") && StringUtil.isNotEmpty(invitationCard)){
				if(sb.length() >0){
					sb.append(" OR ");
				}
				sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(\"国内论坛\")");
				if ("0".equals(invitationCard)) {// 主贴
					sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
				} else if ("1".equals(invitationCard)) {// 回帖
					sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
				}
				sb.append(")");
				source = source.replaceAll(";国内论坛","").replaceAll("国内论坛;","");
			}
			source = source.replaceAll(";", " OR ").replace("境外媒体", "国外新闻");
			if (source.endsWith("OR ")) {
				source = source.substring(0, source.lastIndexOf("OR"));
			}
			if(sb.length() > 0){
				sb.append(" OR ");
			}
			sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":("+source+")").append(")");

			builder.filterByTRSL(sb.toString());
			countBuilder.filterByTRSL(sb.toString());
		}else{
			// 增加具体来源
			if (StringUtils.isNotBlank(source) && !"ALL".equals(source)) {
				source = source.replaceAll(";", " OR ");
				if (source.endsWith("OR ")) {
					source = source.substring(0, source.lastIndexOf("OR"));
				}
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
			}else if("ALL".equals(source)){
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME,
						Const.STATTOTAL_GROUP.replace(";", " OR ").replace("境外媒体", "国外新闻"), Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.STATTOTAL_GROUP.replace(";", " OR ").replace("境外媒体", "国外新闻"), Operator.Equal);
			}
		}
		if (!"ALL".equals(area)) { // 地域
			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			countBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
		}
		if (!"ALL".equals(industry)) { // 行业
			builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
		}
		if (!"ALL".equals(emotion)) { // 情感
			builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
		}
		// 结果中搜索
		if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
			String[] split = keywords.split(",");
			String splitNode = "";
			for (int i = 0; i < split.length; i++) {
				if (StringUtil.isNotEmpty(split[i])) {
					splitNode += split[i] + ",";
				}
			}
			keywords = splitNode.substring(0, splitNode.length() - 1);
			if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
					|| keywords.endsWith("，")) {
				keywords = keywords.substring(0, keywords.length() - 1);

			}
			StringBuilder fuzzyBuilder  = new StringBuilder();
			String hybaseField = "fullText";
			switch (fuzzyValueScope){
				case "title":
					hybaseField = FtsFieldConst.FIELD_URLTITLE;
					break;
				case "source":
					hybaseField = FtsFieldConst.FIELD_SITENAME;
					break;
				case "author":
					hybaseField = FtsFieldConst.FIELD_AUTHORS;
					break;
			}
			if("fullText".equals(hybaseField)){
				fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}else {
				fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
						.replaceAll("[;|；]+","\" OR \"")).append("\"))");
			}
			builder.filterByTRSL(fuzzyBuilder.toString());
			countBuilder.filterByTRSL(fuzzyBuilder.toString());
			log.info(builder.asTRSL());
		}
		//拼接排除词
		if (StringUtil.isNotEmpty(notKeyWords) ) {
			if("positioCon".equals(keyWordIndex)){
				StringBuilder exbuilder = new StringBuilder();
				exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				builder.filterByTRSL(exbuilder.toString());
				countBuilder.filterByTRSL(exbuilder.toString());
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}else {
				StringBuilder exbuilder2 = new StringBuilder();
				exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
						.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
				builder.filterByTRSL(exbuilder2.toString());
				countBuilder.filterByTRSL(exbuilder2.toString());
			}

		}

		log.info(builder.asTRSL());
		switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				//20191031  修复JIRA1605专题分析信息列表 全部 加上热点排序（因前端没时间，页面上暂未加上相似文章数显示）
				QueryBuilder hotBuilder = new QueryBuilder();
				hotBuilder.filterByTRSL(builder.asTRSL());
				hotBuilder.page(builder.getPageNo(),builder.getPageSize());
				String[] database = builder.getDatabase();
				if (ObjectUtil.isNotEmpty(database)){
					hotBuilder.setDatabase(StringUtil.join(database,";"));
				}
				QueryBuilder hotCountBuilder = new QueryBuilder();
				hotCountBuilder.filterByTRSL(countBuilder.asTRSL());
				hotCountBuilder.page(countBuilder.getPageNo(),countBuilder.getPageSize());
				if (ObjectUtil.isNotEmpty(database)){
					hotCountBuilder.setDatabase(StringUtil.join(database,";"));
				}
				InfoListResult list = infoListService.getHotList(hotBuilder, hotCountBuilder, loginUser,null);
				if (isExport) {
					PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) list.getContent();
					List<FtsDocumentCommonVO> listVo = content.getPageItems();
					RedisUtil.setMix(alertRule.getId(), listVo);
				}
				return list;
			case "relevance"://相关性排序
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
			default:
				if(alertRule!=null){
					if(alertRule.isWeight()){
						builder.setOrderBy("-"+FtsFieldConst.FIELD_RELEVANCE+";-"+FtsFieldConst.FIELD_URLTIME);
					}else{
						builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
					}
				}else{
					builder.setOrderBy("-"+FtsFieldConst.FIELD_URLTIME+";-"+FtsFieldConst.FIELD_RELEVANCE);
				}
				break;
		}
		log.info(builder.asTRSL());
		log.error("开始查询海贝:" + System.currentTimeMillis());

		InfoListResult list = infoListService.getDocListContrast(builder, loginUser, simflag, irSimflag,irSimflagAll,null);
		if (isExport) {
			PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) list.getContent();
			List<FtsDocumentCommonVO> listVo = content.getPageItems();
			RedisUtil.setMix(alertRule.getId(), listVo);
		}
		log.error("查询完成:" + System.currentTimeMillis());
		log.error("方法返回:" + System.currentTimeMillis());
		return list;
	}

	@Override
	public Object sendBlend(String receivers, String[] sids,String urltime, String content, String userId, String[] groupNames,
			String sendWay,String trslk) throws TRSException {
		QueryBuilder builderTime = DateUtil.timeBuilder(urltime);
		Date start = builderTime.getStartTime();
		Date end = builderTime.getEndTime();
		SimpleDateFormat format = new SimpleDateFormat(DateUtil.yyyyMMdd2);
		String startString = format.format(start);
		String endString = format.format(end);
		String trs = "";
		//为了手动推送预警描红
		if(StringUtil.isNotEmpty(trslk)){
			trs = RedisUtil.getString(trslk);
		}
//		//限制时间范围查库 时间不存在时底层限制在一个月内导致有些信息查询不到
		List<AlertEntity> list = new ArrayList<>();
		if (sids != null && sids.length > 0 && groupNames != null && groupNames.length > 0) {
			for (int i = 0; i < sids.length; i++) {
				if (Const.MEDIA_TYPE_WEIXIN.contains(groupNames[i])) {
					// 调用查微信库的方法
					QueryBuilder weixinBuilder = new QueryBuilder();
					String weixinTrsl = FtsFieldConst.FIELD_HKEY + ":(" + sids[i] + ")";
					weixinBuilder.filterByTRSL(weixinTrsl);
					weixinBuilder.setDatabase(Const.WECHAT);
					weixinBuilder.setPageSize(-1);
					weixinBuilder.setStartTime(start);
					weixinBuilder.setEndTime(end);
					if(StringUtil.isNotEmpty(startString) && StringUtil.isNotEmpty(endString)){
						weixinBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString+"000000",endString+"235959"}, Operator.Between);
					}
					if(StringUtil.isNotEmpty(trs)){
						weixinBuilder.filterByTRSL(trs);
					}
					list.addAll(this.weixin(weixinBuilder,false,false));
				} else if (Const.MEDIA_TYPE_WEIBO.contains(groupNames[i])) {
					// 调用查微博库的方法
					QueryBuilder weiboBuilder = new QueryBuilder();
					String weiboTrsl = FtsFieldConst.FIELD_MID + ":(" + sids[i] + ")";
					weiboBuilder.filterByTRSL(weiboTrsl);
					weiboBuilder.setDatabase(Const.WEIBO);
					weiboBuilder.setPageSize(-1);
					weiboBuilder.setStartTime(start);
					weiboBuilder.setEndTime(end);
					if(StringUtil.isNotEmpty(startString) && StringUtil.isNotEmpty(endString)){
						weiboBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString+"000000",endString+"235959"}, Operator.Between);
					}
					if(StringUtil.isNotEmpty(trs)){
						weiboBuilder.filterByTRSL(trs);
					}
					list.addAll(this.weibo(weiboBuilder,false,false));
				} else if (Const.MEDIA_TYPE_TF.contains(groupNames[i])) {
					// 调用查微博库的方法
					QueryBuilder tfBuilder = new QueryBuilder();
					String tfTrsl = FtsFieldConst.FIELD_SID + ":(" + sids[i] + ")";
					tfBuilder.filterByTRSL(tfTrsl);
					tfBuilder.setDatabase(Const.HYBASE_OVERSEAS);
					tfBuilder.setPageSize(-1);
					tfBuilder.setStartTime(start);
					tfBuilder.setEndTime(end);
					if(StringUtil.isNotEmpty(startString) && StringUtil.isNotEmpty(endString)){
						tfBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString+"000000",endString+"235959"}, Operator.Between);
					}
					if(StringUtil.isNotEmpty(trs)){
						tfBuilder.filterByTRSL(trs);
					}

					list.addAll(this.tf(tfBuilder,false,false));
				} else {
					// 根据documentid查hybase
					String trsl = FtsFieldConst.FIELD_SID + ":(" + sids[i] + ")";
					// 看看有多少篇文章
					QueryBuilder queryBuilder = new QueryBuilder();
					queryBuilder.filterByTRSL(trsl);
					queryBuilder.setPageSize(-1);
					queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
					queryBuilder.setStartTime(start);
					queryBuilder.setEndTime(end);
					if(StringUtil.isNotEmpty(startString) && StringUtil.isNotEmpty(endString)){
						queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString+"000000",endString+"235959"}, Operator.Between);
					}
					if(StringUtil.isNotEmpty(trs)){
						queryBuilder.filterByTRSL(trs);
					}
					// 调用下边查传统库的方法
					list.addAll(this.chauntong(queryBuilder,false,false));//通过id查询 没必要排重
				}
			}

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
					//处理title
					String title = StringUtil.replaceNRT(StringUtil.removeFourChar(alertEntity.getTitle()));
					title = StringUtil.replaceEmoji(title);
					if (StringUtil.isNotEmpty(title)) {
						/*if (title.length() > Const.ALERT_NUM) {
							title = title.substring(0, Const.ALERT_NUM) + "...";
						}*/
						map.put("titleWhole",title);
						title = StringUtil.calcuCutLength(title, Const.ALERT_NUM);
						map.put("title", title);
					} else {
						map.put("title", "");
					}
//					map.put("title", alertEntity.getTitle());
					map.put("groupName", alertEntity.getGroupName());
					map.put("sid", alertEntity.getSid());
					String source = alertEntity.getSiteName();
					if (StringUtil.isEmpty(source)) {
						source = alertEntity.getGroupName();
					}
					map.put("source", source);
					Date urlTime = alertEntity.getTime();
					SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
					if (ObjectUtil.isNotEmpty(urlTime)) {
						String str = sdf.format(urlTime);
						map.put("urlTime", str);
					} else {
						map.put("urlTime", "");
					}
					map.put("appraise", alertEntity.getAppraise());
					map.put("author",alertEntity.getAuthor());
					map.put("screenName", alertEntity.getScreenName());
					map.put("siteName", alertEntity.getSiteName());
					map.put("md5", alertEntity.getMd5tag());
					// 过滤img标签
					//处理content
//					alertEntity.setContent(StringUtil.replaceImg(alertEntity.getContent()));
//					String content1 = alertEntity.getContent();
//					if (StringUtils.isNotBlank(alertEntity.getContent())) {
//						if (alertEntity.getContent().length() > 150) {
//							content1 = alertEntity.getContent().substring(0, 140) + "...";
//						} else {
//							content1 = alertEntity.getContent();
//						}
//					}
//					map.put("content", content1);
					String documentContent = StringUtil.removeFourChar(alertEntity.getContent());
					documentContent =StringUtil.replaceEmoji(documentContent);
					documentContent =StringUtil.replaceImg(documentContent) ;
					documentContent = StringUtil.notFontEnd(documentContent, 150);
					/*if (StringUtils.isNotBlank(documentContent)) {
						documentContent = documentContent.length() > 150 ? documentContent.substring(0, 140) + "..."
								: documentContent;
					}*/
					map.put("content", documentContent);
					map.put("screenName", alertEntity.getScreenName());
					map.put("rttCount", String.valueOf(alertEntity.getRttCount()));
					map.put("commtCount", String.valueOf(alertEntity.getCommtCount()));
					map.put("nreserved1", alertEntity.getNreserved1());
					ListMap.add(map);

				}

				Map<String, Object> map = new HashMap<>();
				map.put("listMap", ListMap);
				map.put("size", list.size());
				// 自动预警标题
				map.put("title", content);

				if (StringUtils.isNotBlank(sendWay)) {
					String[] split = sendWay.split(";|；");
					for (int i = 0; i < split.length; i++) {
						// for (String string : split) {
						String string = split[i];
						SendWay sendValue = SendWay.valueOf(string);
						String[] splitWeb = receivers.split(";");
						// 我让前段把接受者放到websiteid里边了 然后用户和发送方式一一对应 和手动发送方式一致
						noticeSendService.sendAll(sendValue, "mailmess2.ftl", content, map, splitWeb[i], userId,AlertSource.ARTIFICIAL);
					}
				}
			}
		}
		return "success";
	}

	/**
	 * 查传统库
	 * 
	 * @date Created at 2018年3月2日 下午3:46:03
	 * @Author 谷泽昊
	 * @param searchBuilder
	 * @return
	 */
	private List<AlertEntity> chauntong(QueryBuilder searchBuilder,boolean irSimflag,boolean irSimflagAll ) {
		List<AlertEntity> list = new ArrayList<>();
		List<FtsDocument> documents = null;
		StringBuffer bufferSid = new StringBuffer();
		try {
			documents = hybase8SearchService.ftsQuery(searchBuilder, FtsDocument.class, false,irSimflag,irSimflagAll,null);
			String trs = searchBuilder.getAppendTRSL().toString();
			log.info(searchBuilder.asTRSL());
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
						ftsDocument.getSiteName(), ftsDocument.getGroupName(), null, null, 0, 0, "",
						ftsDocument.getAppraise(), "", null, ftsDocument.getNreserved1(), "", false,null,"",imaUrl,false,false,0,trs,keywords,ftsDocument.getAuthors());
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
	 * @return
	 */
	private List<AlertEntity> weibo(QueryBuilder searchBuilderWeiBo,boolean irSimflag,boolean irSimflagAll) {
		List<AlertEntity> list = new ArrayList<>();
		List<FtsDocumentStatus> documents = null;
		StringBuffer bufferSid = new StringBuffer();
		try {
			documents = hybase8SearchService.ftsQuery(searchBuilderWeiBo, FtsDocumentStatus.class, false,irSimflag,irSimflagAll,null );
			String trs = searchBuilderWeiBo.getAppendTRSL().toString();
			log.info(searchBuilderWeiBo.asTRSL());
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
				AlertEntity alertEntity = new AlertEntity(ftsDocument.getMid(), ftsDocument.getStatusContent(),content,
						ftsDocument.getStatusContent(), ftsDocument.getUrlName(), ftsDocument.getCreatedAt(),
						ftsDocument.getSiteName(), ftsDocument.getGroupName(), null, null, 0, 0, ftsDocument.getScreenName(),
						ftsDocument.getAppraise(), "", null, "", "", false,ftsDocument.getRetweetedMid(),"",imaUrl,false,false,0,trs,null,ftsDocument.getAuthors());
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
	 * @return
	 */
	private List<AlertEntity> weixin(QueryBuilder searchBuilderWeiBo,boolean irSimflag,boolean irSimflagAll ) {
		List<AlertEntity> list = new ArrayList<>();
		List<FtsDocumentWeChat> documents = null;
		StringBuffer bufferSid = new StringBuffer();
		try {
			documents = hybase8SearchService.ftsQuery(searchBuilderWeiBo, FtsDocumentWeChat.class, false,irSimflag,irSimflagAll,null );
			log.info(searchBuilderWeiBo.asTRSL());
			String trs = searchBuilderWeiBo.getAppendTRSL().toString();
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
                        ftsDocument.getSiteName(), ftsDocument.getGroupName(), null, null, 0, 0, "",
                        ftsDocument.getAppraise(), "", null, "", "", false,null,"",imaUrl,false,false,0,trs,null,ftsDocument.getAuthors());
				bufferSid.append(ftsDocument.getSid()).append(" OR ");
				list.add(alertEntity);
			}
		} catch (Exception e) {
			log.info("hybase查询出错", e);
		}
		return list;
	}

	/**
	 * 查Twitter Facebook库
	 *
	 * @date Created at 2019年11月6日 下午3:45:57
	 * @Author 张娅
	 * @param searchBuilderTF
	 * @return
	 */
	private List<AlertEntity> tf(QueryBuilder searchBuilderTF,boolean irSimflag,boolean irSimflagAll) {
		List<AlertEntity> list = new ArrayList<>();
		List<FtsDocumentTF> documents = null;
		StringBuffer bufferSid = new StringBuffer();
		try {
			documents = hybase8SearchService.ftsQuery(searchBuilderTF, FtsDocumentTF.class, false,irSimflag,irSimflagAll,null );
			String trs = searchBuilderTF.getAppendTRSL().toString();
			log.info(searchBuilderTF.asTRSL());
			for (FtsDocumentTF ftsDocument : documents) {
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
						ftsDocument.getSiteName(), ftsDocument.getGroupName(), null, null, 0, 0, ftsDocument.getScreenName(),
						ftsDocument.getAppraise(), "", null, "", "", false,ftsDocument.getRetweetedMid(),"",imaUrl,false,false,0,trs,null,ftsDocument.getAuthors());
				bufferSid.append(ftsDocument.getSid()).append(" OR ");
				list.add(alertEntity);
			}
		} catch (Exception e) {
			log.info("hybase查询出错", e);
		}
		return list;
	}
	
	/**
	 * 不关联alert_netinsight预警工程
	 * @param time 时间
	 * @param pageNo 0开始页数
	 * @param pageSize 一页几条
	 * @return
	 * @throws OperationException
	 */
	@Override
	public Object listSmsLocal(String time,int pageNo,int pageSize) throws OperationException{
		String userName = UserUtils.getUser().getUserName();
		String userId = UserUtils.getUser().getId();
		try {
//			List<AlertEntity> allList = new ArrayList<AlertEntity>();//总的  一起返回给前端
			String[] formatTimeRange = DateUtil.formatTimeRange(time);//时间间隔
			String start = formatTimeRange[0];
			String end = formatTimeRange[1];
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date parseStart = null;
			Date parseEnd = null;
			try {
				parseStart = sdf.parse(start);
				parseEnd = sdf.parse(end);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			//获取当前用户下所有的规则
//			List<AlertRule> ruleList = findByUserId(userId);
//			Criteria<AlertEntity> criteria = new Criteria<>();
//			criteria.add(Restrictions.between("createdTime", parseStart, parseEnd));
//			criteria.add(Restrictions.eq("receiver", userName));
//			criteria.add(Restrictions.eq("sendWay", SendWay.SMS));
//			for(AlertRule rule : ruleList){
//				criteria.add(Restrictions.eq("alertRuleBackupsId", rule.getId()));
//				Page<AlertEntity> findAll = alertService.findAll(criteria,pageNo,pageSize);
//				allList.addAll(findAll.getContent());
//			}
//			String id = null;//不要走规则的
//			List<AlertEntity> findAll = alertService.findByReceiverAndSendWayAndAlertRuleBackupsIdAndCreatedTimeBetween(userName,SendWay.SMS,id, parseStart,parseEnd);
//			allList.addAll(findAll);
			List<AlertEntity> findAll = alertService.findByReceiverAndSendWayAndCreatedTimeBetween(userName,
					SendWay.SMS, parseStart, parseEnd);
			return findAll;
		} catch (Exception e) {
			throw new OperationException("预警弹窗获取失败,message:" + e,e);
		}
	}
	
	/**
	 * 关联alert_netinsight预警工程
	 * @param time 时间
	 * @param pageNo 0开始页数
	 * @param pageSize 一页几条
	 * @return
	 * @throws OperationException
	 */
	@Override
	public Object listSmsHttp(String time,int pageNo,int pageSize) throws OperationException{
		String userName = UserUtils.getUser().getUserName();
		String userId = UserUtils.getUser().getId();
		String url = alertNetinsightUrl+"/rule/listSMS?time="+time+"&pageNo="+pageNo+"&pageSize="
		+pageSize+"&userName="+userName+"&userId="+userId;
		String doGet = HttpUtil.doGet(url, null);
		if(StringUtil.isEmpty(doGet)){
			return null;
		}else if(doGet.contains("\"code\":500")){
			Map<String,String> map = (Map<String,String>)JSON.parse(doGet);
			String message = map.get("message");
			throw new OperationException("预警弹窗获取失败,message:"+message ,new Exception());
		}
		List<AlertEntity> ts = (List<AlertEntity>) JSONArray.parseArray(doGet, AlertEntity.class);
		return ts;
	}

	@Override
	public int getSubGroupAlertCount(User user) {
		if (UserUtils.isRoleAdmin()){
			List<AlertRule> alertRules = alertRuleRepository.findByUserId(user.getId(), new Sort(Direction.DESC, "createdTime"));
			if (ObjectUtil.isNotEmpty(alertRules)){
				//机构管理员
				return alertRules.size();
			}
		}
		if (UserUtils.isRoleOrdinary(user)){
			//如果是普通用户 受用户分组 可创建资源的限制
			//查询该用户所在的用户分组下 是否有可创建资源
			SubGroup subGroup = subGroupService.findOne(user.getSubGroupId());
			List<AlertRule> bySubGroupId = alertRuleRepository.findBySubGroupId(subGroup.getId(), new Sort(Direction.DESC, "createdTime"));
			if (ObjectUtil.isNotEmpty(bySubGroupId)){
				return bySubGroupId.size();
			}

		}

		return 0;
	}

	@Override
	public int getSubGroupAlertCountForSubGroup(String subGroupId) {
		List<AlertRule> bySubGroupId = alertRuleRepository.findBySubGroupId(subGroupId, new Sort(Direction.DESC, "createdTime"));
		if (ObjectUtil.isNotEmpty(bySubGroupId)){
			return bySubGroupId.size();
		}
		return 0;
	}

	@Override
	public List<AlertRule> findByUserId(String userId) {
		return alertRuleRepository.findByUserId(userId,new Sort(Direction.DESC, "createdTime"));
	}

	@Override
	public void updateAll(List<AlertRule> alertRules) {
		alertRuleRepository.save(alertRules);
		alertRuleRepository.flush();
	}

	@Override
	public void updateSimple() {
		List<AlertRule> alertRules = alertRuleRepository.findBySpecialType(SpecialType.COMMON);
		if (ObjectUtil.isNotEmpty(alertRules)){
			System.err.println("预警规则开始，共"+alertRules.size()+"条。");
			for (AlertRule alertRule : alertRules) {
				String anyKeywords = alertRule.getAnyKeyword();
				if (StringUtil.isNotEmpty(anyKeywords)){
					Map<String, Object> hashMap = new HashMap<>();
					hashMap.put("wordSpace",0);
					hashMap.put("wordOrder",false);
					hashMap.put("keyWords",anyKeywords);
					String toJSONString = JSONObject.toJSONString(hashMap);
					alertRule.setAnyKeyword("["+toJSONString+"]");
					alertRuleRepository.save(alertRule);
				}
			}
		}
		System.err.println("预警规则结束~~~~~~~~~~~~~~~");


		List<AlertRuleBackups> alertRuleBackups = alertRuleBackupsService.findSimple();
		if (ObjectUtil.isNotEmpty(alertRuleBackups)){
			System.err.println("预警备份表开始，共"+alertRuleBackups.size()+"条。");
			for (AlertRuleBackups alertRuleBackup : alertRuleBackups) {
				String anyKeyword = alertRuleBackup.getAnyKeyword();
				if (StringUtil.isNotEmpty(anyKeyword)){
					Map<String, Object> hashMap = new HashMap<>();
					hashMap.put("wordSpace",0);
					hashMap.put("wordOrder",false);
					hashMap.put("keyWords",anyKeyword);
					String toJSONString = JSONObject.toJSONString(hashMap);
					alertRuleBackup.setAnyKeyword("["+toJSONString+"]");
					alertRuleBackupsService.add(alertRuleBackup);
				}
			}
		}
		System.err.println("预警规则备份表结束~~~~~~~~~~~~~");
	}
}