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

import java.util.ArrayList;
import java.util.List;

import com.trs.netInsight.support.hybaseRedis.HybaseReadUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.report.service.IMaterialLibraryNewService;
import com.trs.netInsight.widget.report.service.IReportService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.article.entity.ArticleDelete;
import com.trs.netInsight.widget.article.entity.enums.ArticleDeleteGroupName;
import com.trs.netInsight.widget.article.service.IArticleDeleteService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import static com.trs.netInsight.widget.report.constant.ReportConst.SEMICOLON;

/**
 * 文章删除Controller
 * 
 * @Type ArticleDeleteController.java
 * @author 谷泽昊
 * @date 2018年4月2日 下午3:08:36
 * @version
 */
@Slf4j
@RestController
@Api(description = "文章删除")
@RequestMapping(value = { "/articledelete" })
public class ArticleDeleteController {

	@Autowired
	private IArticleDeleteService articleDeleteService;

	@Autowired
	private IReportService reportService;
	@Autowired
	private IMaterialLibraryNewService materialLibraryNewService;

	/**
	 * 添加文章删除
	 * 
	 * @date Created at 2018年7月30日 上午10:54:11
	 * @Author 谷泽昊
	 * @param id
	 *            栏目id，专题或者日常监测
	 * @param sids
	 * @param groupName
	 * @return
	 * @throws TRSException
	 */
	@FormatResult
	@ApiOperation("添加文章删除")
	@PostMapping("/addArticleDelete")
	public Object addArticleDelete(@ApiParam("栏目id，专题或者日常监测") @RequestParam(value = "id", required = false) String id,
			@ApiParam("文章sid，批量的话用英文逗号隔开") @RequestParam(value = "sids", required = false) String[] sids,
			@ApiParam("分类，分为：chuantong,weixin,weibo,TF") @RequestParam(value = "groupName", required = false) ArticleDeleteGroupName[] groupName)
			throws TRSException {
		String userId = "";
		if (sids != null && sids.length > 0 && groupName != null && groupName.length > 0
				&& sids.length == groupName.length) {
			List<String> list = new ArrayList<>();
			for (int i = 0; i < sids.length; i++) {

				ArticleDelete articleDelete = null;
				try {
					userId = UserUtils.getUser().getId();
					articleDelete = articleDeleteService.findBySidAndUserIdAndGroupName(sids[i], userId,groupName[i]);
					if (articleDelete == null) {
						articleDelete = articleDeleteService.addArticleDelete(sids[i], groupName[i]);
						if (articleDelete != null) {
							list.add("sid：" + sids[i] + "，groupName：" + groupName[i] + "，删除成功！");
						}
					} else {
						list.add("sid：" + sids[i] + "，groupName：" + groupName[i] + "，已经删除过！");
					}
				} catch (Exception e) {
					log.error("sid：" + sids[i] + "，groupName：" + groupName[i] + "，删除失败！");
				}

			}
			// 同时需要删除收藏
			String sidsJoined = StringUtil.join(sids, SEMICOLON);
			reportService.delFavourites(sidsJoined, userId);
			materialLibraryNewService.delLibraryResourceForIds(sidsJoined);
			if(StringUtils.isNotBlank(id)){
				RedisFactory.deleteAllKey(id);
			}
			if(sids != null && sids.length >0){
				HybaseReadUtil.remaveHybaseReadRedisKey(UserUtils.getUser());
			}
			return list;
		}
		throw new TRSException(CodeUtils.FAIL, "添加删除失败");
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