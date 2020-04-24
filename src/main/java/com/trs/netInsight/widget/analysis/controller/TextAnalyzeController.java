/*
 * Project: netInsight
 * 
 * File Created at 2018年3月7日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.analysis.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.ckm.soap.CkmSoapException;
import com.trs.ckm.soap.CluClsInfo;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.ckm.ICkmService;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.special.service.ISpecialService;
import com.trs.netInsight.widget.user.service.IUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * @Type TextAnalyzeController.java
 * @Desc 
 * @author Administrator
 * @date 2018年3月7日 下午4:45:15
 * @version 
 */
@Slf4j
@RestController
@RequestMapping("/analysis/text")
@Api(description = "内容分析接口")
public class TextAnalyzeController {
	
	@Autowired
	private FullTextSearch hybase8SearchService;
	
	@Autowired
	private ICkmService ckmService;
	
	@Autowired
	private ISpecialService specialService;
	
	@Autowired
	private IUserService userService;
	
	@RequestMapping(value = "/demo", method = RequestMethod.GET)
	@ApiOperation("内容分析接口示例")
	public Object demo(){
		try {
			List<FtsDocument> docs = this.hybase8SearchService.ftsPageList("","IR_URLTITLE:上海", "IR_URLTIME", 10, 0, FtsDocument.class, false, false,false,false,null).getPageItems();
			List<CluClsInfo> cluster = this.ckmService.cluster(docs, 0);
			return cluster;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("内容分析失败!",e);
		}
		return null;
	}
	
	@RequestMapping(value = "/copy", method = RequestMethod.GET)
	public Object copy2org(String orgIid,String commonUserId){
		this.specialService.copyOrgSpecial2Common(userService.findById(orgIid),userService.findById(commonUserId));
		return "success";
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public Object search() throws TRSException{
		String s = "IR_URLTITLE:北京 AND IR_URLTIME:20180323";
		QueryCommonBuilder query = new QueryCommonBuilder();
		query.setDatabase(new String[]{Const.WECHAT,Const.HYBASE_NI_INDEX});
		query.orderBy("IR_URLTIME", false);
		query.filterByTRSL(s);
		PagedList<FtsDocumentCommonVO> list = hybase8SearchService.pageListCommon(query, true,true,true,null);
		return list;
	}
	/**
	 * 原生通用查询
	 * @since changjiang @ 2018年5月8日
	 * @param trsl
	 * @return
	 * @throws TRSException
	 * @Return : Object
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public Object search(String trsl) throws TRSException{
		long start = new Date().getTime();
		log.info("原生通用查询"+trsl);
		try{
			String[] databases = {Const.HYBASE_NI_INDEX, Const.WECHAT, Const.WEIBO, Const.HYBASE_OVERSEAS, Const.INSERT};
			
			QueryCommonBuilder builder = new QueryCommonBuilder();
			builder.filterByTRSL(trsl);
			builder.setDatabase(databases);
			builder.page(0, 10);
			PagedList<FtsDocumentCommonVO> common = hybase8SearchService.pageListCommon(builder, false,false,false,null);
			long end = new Date().getTime();
			long timeApi = end - start;
			Map<String, Object> result = new HashMap<>();
			result.put("list", common);
			result.put("time", timeApi);
			return result;
		}catch(Exception e){
			log.error(e.toString());
		}finally{
			long end = new Date().getTime();
			long timeApi = end - start;
			log.error("共用时:"+timeApi);
		}
		return null;
	}
	
	//测试接口，暂不包括检索出一批内容后各自情感划分的接口
	@ApiOperation("六类情感划分测试接口")
	@GetMapping("/judgeEmotion")
	public Object judgeEmotion(
		@ApiParam("检索出文章后再划分")@RequestParam(value = "trsl", required = false)String trsl,
		@ApiParam("直接对给出的内容划分")@RequestParam(value = "content", required = false)String content
			) throws CkmSoapException, TRSSearchException, TRSException{
		Map<String, Integer> emotiomMap = new HashMap<String, Integer>();
		if(StringUtil.isNotEmpty(trsl)){
			QueryBuilder builder = new QueryBuilder();
			builder.filterByTRSL(trsl);
			builder.setDatabase(Const.HYBASE_NI_INDEX);
			PagedList<FtsDocument> list = hybase8SearchService.ftsPageList(builder, FtsDocument.class, false, false,false,null);
			List<FtsDocument> docList = list.getPageItems();
			for (FtsDocument doc : docList) {
				emotiomMap = ckmService.emotionDivide(doc.getContent());
			}
		}else if(StringUtil.isEmpty(trsl) && StringUtil.isNotEmpty(content)){
			emotiomMap = ckmService.emotionDivide(content);
		}else{
			return "请输入检索条件！";
		}
		return emotiomMap;
	}

}


/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月7日 Administrator creat
 */