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
package com.trs.netInsight.widget.article.service;

import java.text.ParseException;


import com.trs.ckm.soap.CkmSoapException;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.entity.FtsDocumentInsert;
import com.trs.netInsight.support.fts.entity.FtsDocumentInsertShow;

/**
 * 文章添加到海贝service
 * 
 * @Type IArticleDeleteService.java
 * @author 谷泽昊
 * @date 2018年4月2日 下午3:07:47
 * @version
 */
public interface IArticleHybaseService {

	/**
	 * 添加文章到海贝库
	 * @date Created at 2018年4月11日  下午4:18:46
	 * @Author 谷泽昊
	 * @param documentInsert
	 * @throws TRSException
	 * @throws com.trs.netInsight.handler.exception.TRSException
	 * @throws ParseException
	 * @throws CkmSoapException 
	 */
	public void addArticle(FtsDocumentInsert documentInsert) throws TRSException, com.trs.netInsight.handler.exception.TRSException, ParseException, CkmSoapException;

	/**
	 * 查询列表
	 * @date Created at 2018年4月12日  下午12:00:29
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws SearchException
	 * @throws com.trs.netInsight.handler.exception.TRSException
	 */
	public PagedList<FtsDocumentInsertShow> pageList(int pageNo, int pageSize) throws TRSSearchException, com.trs.netInsight.handler.exception.TRSException;

	/**
	 * 修改文章
	 * @date Created at 2018年4月12日  下午12:00:39
	 * @Author 谷泽昊
	 * @param uid
	 * @param documentInsert
	 * @throws TRSException
	 * @throws ParseException
	 * @throws com.trs.netInsight.handler.exception.TRSException
	 * @throws CkmSoapException
	 */
	public void updateArticle(String uid, FtsDocumentInsert documentInsert) throws TRSException, ParseException, com.trs.netInsight.handler.exception.TRSException, CkmSoapException;

	/**
	 * 删除文章
	 * @date Created at 2018年4月12日  下午12:00:45
	 * @Author 谷泽昊
	 * @param uids
	 * @throws com.trs.netInsight.handler.exception.TRSException 
	 */
	public void deleteArticle(String[] uids) throws com.trs.netInsight.handler.exception.TRSException;
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