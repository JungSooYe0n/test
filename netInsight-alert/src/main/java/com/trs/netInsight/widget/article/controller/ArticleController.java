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
package com.trs.netInsight.widget.article.controller;

import java.text.ParseException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.ckm.soap.CkmSoapException;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.hybase.client.TRSException;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.entity.FtsDocumentInsert;
import com.trs.netInsight.support.fts.entity.FtsDocumentInsertShow;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.widget.article.service.IArticleHybaseService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 对海贝文章操作Controller
 * 
 * @Type ArticleController.java
 * @author 谷泽昊
 * @date 2018年4月2日 下午3:08:36
 * @version
 */
@RestController
@Api(description = "对海贝文章操作")
@RequestMapping(value = { "/article"})
public class ArticleController {
	@Autowired
	private IArticleHybaseService articleHybaseService;

	/**
	 * 添加文章到海贝
	 * @date Created at 2018年4月11日  下午4:32:42
	 * @Author 谷泽昊
	 * @param authors
	 * @param channel
	 * @param content
	 * @param industry
	 * @param appraise
	 * @param groupname
	 * @param sitename
	 * @param urlname
	 * @param urltitle
	 * @param urltime
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@ApiOperation("添加文章到海贝")
	@PostMapping("/addArticle")
	public Object addArticle(@ApiParam("作者") @RequestParam(value = "authors",required=false) String authors,
			@ApiParam("频道") @RequestParam(value = "channel",required=false) String channel,
			@ApiParam("正文") @RequestParam(value = "content") String content,
			@ApiParam("行业") @RequestParam(value = "industry",required=false) String industry,
			@ApiParam("正负面") @RequestParam(value = "appraise",required=false,defaultValue="中性") String appraise,
			@ApiParam("站点") @RequestParam(value = "sitename") String sitename,
			@ApiParam("文章来源") @RequestParam(value = "urlname") String urlname,
			@ApiParam("文章标题") @RequestParam(value = "urltitle") String urltitle,
			@ApiParam("文章时间") @RequestParam(value = "urltime") String urltime,
			@ApiParam("分组") @RequestParam(value = "groupname")String groupname) throws OperationException {
		FtsDocumentInsert documentInsert = new FtsDocumentInsert(authors, channel, content, sitename,
				urlname, DateUtil.stringToDate(urltime, DateUtil.yyyyMMdd), urltitle, appraise, industry,groupname);
		try {
			articleHybaseService.addArticle(documentInsert);
			return "success";
		} catch (TRSException e) {
			throw new OperationException("对海贝文章添加，message:" , e);
		} catch (com.trs.netInsight.handler.exception.TRSException e) {
			throw new OperationException("对海贝文章添加，message:" , e);
		} catch (ParseException e) {
			throw new OperationException("对海贝文章添加，message:" , e);
		} catch (CkmSoapException e) {
			throw new OperationException("对海贝文章添加，message:" , e);
		}
	}
	
	/**
	 * 查询机构文章
	 * @date Created at 2018年4月12日  上午10:19:47
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@ApiOperation("查询机构文章")
	@GetMapping("/pageList")
	public Object pageList(@ApiParam("pageNo") @RequestParam(value = "pageNo",required=false,defaultValue="0") int pageNo,
			@ApiParam("pageSize") @RequestParam(value = "pageSize",required=false,defaultValue="10") int pageSize) throws OperationException {
		if(pageNo<0){
			pageNo=0;
		}
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:10;
		try {
			PagedList<FtsDocumentInsertShow> pageList = articleHybaseService.pageList(pageNo,pageSize);
			return pageList;
		} catch (TRSSearchException e) {
			throw new OperationException("对海贝文章查询，message:" , e);
		} catch (com.trs.netInsight.handler.exception.TRSException e) {
			throw new OperationException("对海贝文章查询，message:" , e);
		}
	}
	
	/**
	 * 修改文章
	 * @date Created at 2018年4月12日  上午11:59:23
	 * @Author 谷泽昊
	 * @param uid
	 * @param authors
	 * @param channel
	 * @param content
	 * @param industry
	 * @param appraise
	 * @param sitename
	 * @param urlname
	 * @param urltitle
	 * @param urltime
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@ApiOperation("修改文章")
	@PostMapping("/updateArticle")
	public Object updateArticle(
			@ApiParam("唯一id") @RequestParam(value = "uid",required=false) String uid,
			@ApiParam("作者") @RequestParam(value = "authors",required=false) String authors,
			@ApiParam("频道") @RequestParam(value = "channel",required=false) String channel,
			@ApiParam("正文") @RequestParam(value = "content") String content,
			@ApiParam("行业") @RequestParam(value = "industry",required=false) String industry,
			@ApiParam("正负面") @RequestParam(value = "appraise",required=false,defaultValue="中性") String appraise,
			@ApiParam("站点") @RequestParam(value = "sitename") String sitename,
			@ApiParam("文章来源") @RequestParam(value = "urlname") String urlname,
			@ApiParam("文章标题") @RequestParam(value = "urltitle") String urltitle,
			@ApiParam("文章时间") @RequestParam(value = "urltime") String urltime,
			@ApiParam("分组") @RequestParam(value = "groupname")String groupname) throws OperationException {
		FtsDocumentInsert documentInsert = new FtsDocumentInsert(authors, channel, content, sitename,
				urlname, DateUtil.stringToDate(urltime, DateUtil.yyyyMMdd), urltitle, appraise, industry,groupname);
		try {
			articleHybaseService.updateArticle(uid,documentInsert);
			return "success";
		} catch (TRSException e) {
			throw new OperationException("对海贝文章修改，message:" , e);
		} catch (com.trs.netInsight.handler.exception.TRSException e) {
			throw new OperationException("对海贝文章修改，message:" , e);
		} catch (ParseException e) {
			throw new OperationException("对海贝文章修改，message:" , e);
		} catch (CkmSoapException e) {
			throw new OperationException("对海贝文章修改，message:" , e);
		}
	}
	/**
	 * 删除文章
	 * @date Created at 2018年4月12日  下午12:02:34
	 * @Author 谷泽昊
	 * @param uids
	 * @return
	 * @throws OperationException
	 */
	@FormatResult
	@ApiOperation("删除文章")
	@PostMapping("/deleteArticle")
	public Object deleteArticle(
			@ApiParam("唯一id,多个用英文逗号隔开") @RequestParam(value = "uids",required=false) String[] uids) throws OperationException {
			try {
				articleHybaseService.deleteArticle(uids);
				return "success";
			} catch (com.trs.netInsight.handler.exception.TRSException e) {
				throw new OperationException("对海贝文章删除，message:" , e);
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
 * 2018年4月2日 谷泽昊 creat
 */