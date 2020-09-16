/*
 * Project: netInsight
 * 
 * File Created at 2018年4月2日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.article.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.widget.common.service.ICommonListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.ckm.soap.CkmSoapException;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.TRSException;
import com.trs.hybase.client.TRSInputRecord;
import com.trs.hybase.client.TRSReport;
import com.trs.hybase.client.params.OperationParams;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.ckm.ICkmService;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentInsert;
import com.trs.netInsight.support.fts.entity.FtsDocumentInsertShow;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.article.service.IArticleHybaseService;

/**
 * 文章添加到海贝service
 * 
 * @Type ArticleDeleteServiceImpl.java
 * @author 谷泽昊
 * @date 2018年4月2日 下午3:08:06
 * @version
 */
@Service
public class ArticleHybaseServiceImpl implements IArticleHybaseService {

	@Autowired
	private ICkmService ckmService;

	@Autowired
	private FullTextSearch hybase8SearchService;
	@Autowired
	private ICommonListService commonListService;

	@Override
	public void addArticle(FtsDocumentInsert documentInsert)
			throws TRSException, com.trs.netInsight.handler.exception.TRSException, ParseException, CkmSoapException {
		TRSInputRecord record = new TRSInputRecord();
		String content = documentInsert.getContent();
		Date urltime = documentInsert.getUrltime();
		// MD5
		// String md5 = ckmService.simMD5GenerateTheme(content);
		// 主题词
		// List<AbsTheme> theme = ckmService.theme(content, 10);
		// 人物关键词
		// Map<String, Integer> people = ckmService.statisticsEntity(content,
		// 10, WordType.PEOPLE_NAME.getTYPE_CODE());
		// // 地域关键词
		// Map<String, Integer> regional = ckmService.statisticsEntity(content,
		// 10, WordType.REGIONAL_NAME.getTYPE_CODE());
		// // 机构关键词
		// Map<String, Integer> organizationName =
		// ckmService.statisticsEntity(content, 10,
		// WordType.ORGANIZATION_NAME.getTYPE_CODE());
		record.addColumn(FtsFieldConst.FIELD_AUTHORS, documentInsert.getAuthors());
		record.addColumn(FtsFieldConst.FIELD_CHANNEL, documentInsert.getChannel());
		record.addColumn(FtsFieldConst.FIELD_CONTENT, documentInsert.getContent());
		record.addColumn(FtsFieldConst.FIELD_DOCLENGTH, content.length());
		// record.addColumn(FtsFieldConst.FIELD_KEYWORDS, theme);
		record.addColumn(FtsFieldConst.FIELD_LASTTIME, new Date());
		record.addColumn(FtsFieldConst.FIELD_LOADTIME, new Date());
		String uuid = UUID.randomUUID().toString();
		record.addColumn(FtsFieldConst.FIELD_SID,uuid);
		if (Const.GROUPNAME_WEIXIN.equals(documentInsert.getGroupname())){
			record.addColumn(FtsFieldConst.FIELD_HKEY, UUID.randomUUID().toString());
		}
		if (Const.GROUPNAME_WEIBO.equals(documentInsert.getGroupname())){
			record.addColumn(FtsFieldConst.FIELD_SCREEN_NAME, documentInsert.getAuthors());
			record.addColumn(FtsFieldConst.FIELD_MID, uuid);
		}
		record.addColumn(FtsFieldConst.FIELD_SITENAME, documentInsert.getSitename());
		record.addColumn(FtsFieldConst.FIELD_URLDATE,
				DateFormat.getDateInstance().parse(DateFormat.getDateInstance().format(urltime)));
		record.addColumn(FtsFieldConst.FIELD_URLNAME, documentInsert.getUrlname());
		record.addColumn(FtsFieldConst.FIELD_URLTIME, urltime);
		record.addColumn(FtsFieldConst.FIELD_URLTITLE, documentInsert.getUrltitle());
		// record.addColumn(FtsFieldConst.FIELD_CATALOG_AREA, regional);
		// record.addColumn(FtsFieldConst.FIELD_MD5TAG, md5);
		record.addColumn(FtsFieldConst.FIELD_SIMFLAG, "1000");
		record.addColumn(FtsFieldConst.FIELD_URLTIME_YEAR, DateUtil.format2String(urltime, "yyyy"));
		record.addColumn(FtsFieldConst.FIELD_URLTIME_MONTH, DateUtil.format2String(urltime, "MM"));
		record.addColumn(FtsFieldConst.FIELD_URLTIME_HOUR, DateUtil.format2String(urltime, "HH"));
		// record.addColumn(FtsFieldConst.FIELD_PEOPLE, people);
		// record.addColumn(FtsFieldConst.FIELD_AGENCY, organizationName);
		record.addColumn(FtsFieldConst.FIELD_APPRAISE, documentInsert.getAppraise());
		record.addColumn(FtsFieldConst.FIELD_INDUSTRY, documentInsert.getIndustry());
		// record.addColumn(FtsFieldConst.CQ_LOCATIONSNEW, regional);
		record.addColumn(FtsFieldConst.FIELD_ORGANIZATIONID, UserUtils.getUser().getOrganizationId());
		//添加分组
		record.addColumn(FtsFieldConst.FIELD_GROUPNAME,documentInsert.getGroupname());
		hybase8SearchService.insertRecords(record, Const.INSERT, true, null);
	}

	@Override
	public PagedList<FtsDocumentInsertShow> pageList(int pageNo, int pageSize)
			throws TRSSearchException, com.trs.netInsight.handler.exception.TRSException {
		QueryBuilder query = new QueryBuilder();
		query.setPageNo(pageNo);
		query.setPageSize(pageSize);
		query.orderBy(FtsFieldConst.FIELD_URLTIME,true);
		String organizationId = UserUtils.getUser().getOrganizationId();
		query.filterField(FtsFieldConst.FIELD_ORGANIZATIONID, organizationId, Operator.Equal);
		PagedList<FtsDocumentInsertShow> ftsPageList = commonListService.queryPageListForClass(query,FtsDocumentInsertShow.class,false,false,false,null);
		List<FtsDocumentInsertShow> list = ftsPageList.getPageItems();
		for(FtsDocumentInsertShow vo :list){
			vo.setGroupName(Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(vo.getGroupName()));
		}
		return ftsPageList;
	}

	@Override
	public void updateArticle(String uid, FtsDocumentInsert documentInsert)
			throws TRSException, ParseException, com.trs.netInsight.handler.exception.TRSException, CkmSoapException {
		List<TRSInputRecord> records = new ArrayList<>(1);
		TRSInputRecord record = new TRSInputRecord();
		String content = documentInsert.getContent();
		Date urltime = documentInsert.getUrltime();
		// MD5
		// String md5 = ckmService.simMD5GenerateTheme(content);
		// 主题词
		// List<AbsTheme> theme = ckmService.theme(content, 10);
		// 人物关键词
		// Map<String, Integer> people = ckmService.statisticsEntity(content,
		// 10, WordType.PEOPLE_NAME.getTYPE_CODE());
		// // 地域关键词
		// Map<String, Integer> regional = ckmService.statisticsEntity(content,
		// 10, WordType.REGIONAL_NAME.getTYPE_CODE());
		// // 机构关键词
		// Map<String, Integer> organizationName =
		// ckmService.statisticsEntity(content, 10,
		// WordType.ORGANIZATION_NAME.getTYPE_CODE());
		record.setUid(uid);
		record.addColumn(FtsFieldConst.FIELD_AUTHORS, documentInsert.getAuthors());
		record.addColumn(FtsFieldConst.FIELD_CHANNEL, documentInsert.getChannel());
		record.addColumn(FtsFieldConst.FIELD_CONTENT, documentInsert.getContent());
		record.addColumn(FtsFieldConst.FIELD_DOCLENGTH, content.length());
		// record.addColumn(FtsFieldConst.FIELD_KEYWORDS, theme);
		record.addColumn(FtsFieldConst.FIELD_SITENAME, documentInsert.getSitename());
		record.addColumn(FtsFieldConst.FIELD_URLDATE,
				DateFormat.getDateInstance().parse(DateFormat.getDateInstance().format(urltime)));
		record.addColumn(FtsFieldConst.FIELD_URLNAME, documentInsert.getUrlname());
//      FIELD_URLTIME 目前不能修改，4月13查看
//		record.addColumn(FtsFieldConst.FIELD_URLTIME, urltime);
		record.addColumn(FtsFieldConst.FIELD_URLTITLE, documentInsert.getUrltitle());
		// record.addColumn(FtsFieldConst.FIELD_CATALOG_AREA, regional);
		// record.addColumn(FtsFieldConst.FIELD_MD5TAG, md5);
		record.addColumn(FtsFieldConst.FIELD_URLTIME_YEAR, DateUtil.format2String(urltime, "yyyy"));
		record.addColumn(FtsFieldConst.FIELD_URLTIME_MONTH, DateUtil.format2String(urltime, "MM"));
		record.addColumn(FtsFieldConst.FIELD_URLTIME_HOUR, DateUtil.format2String(urltime, "HH"));
		// record.addColumn(FtsFieldConst.FIELD_PEOPLE, people);
		// record.addColumn(FtsFieldConst.FIELD_AGENCY, organizationName);
		record.addColumn(FtsFieldConst.FIELD_APPRAISE, documentInsert.getAppraise());
		record.addColumn(FtsFieldConst.FIELD_INDUSTRY, documentInsert.getIndustry());
		// record.addColumn(FtsFieldConst.CQ_LOCATIONSNEW, regional);
		//添加分组
		record.addColumn(FtsFieldConst.FIELD_GROUPNAME,documentInsert.getGroupname());
		if ("微博".equals(documentInsert.getGroupname())){
			record.addColumn(FtsFieldConst.FIELD_SCREEN_NAME, documentInsert.getAuthors());
		}
		OperationParams uParams = new OperationParams();
		uParams.setProperty("update.mode.replace", "false");
		records.add(record);
		hybase8SearchService.updateRecords(Const.INSERT, records, uParams, new TRSReport());
	}

	@Override
	public void deleteArticle(String[] uids) throws com.trs.netInsight.handler.exception.TRSException {
		hybase8SearchService.delete(Const.INSERT, uids);
	}

	public static void main(String[] args) {
		String string = UUID.randomUUID().toString();
		System.out.println(string);
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年4月2日 谷泽昊 creat
 */