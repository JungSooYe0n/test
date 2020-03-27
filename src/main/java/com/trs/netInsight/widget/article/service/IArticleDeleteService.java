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

import java.util.List;

import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Page;

import com.trs.netInsight.widget.article.entity.ArticleDelete;
import com.trs.netInsight.widget.article.entity.enums.ArticleDeleteGroupName;

/**
 * 文章删除service
 * 
 * @Type IArticleDeleteService.java
 * @author 谷泽昊
 * @date 2018年4月2日 下午3:07:47
 * @version
 */
public interface IArticleDeleteService {

	/**
	 * 添加文章删除
	 * 
	 * @date Created at 2018年4月2日 下午3:13:22
	 * @Author 谷泽昊
	 * @param sid
	 * @param groupName
	 * @return
	 */
	public ArticleDelete addArticleDelete(String sid, ArticleDeleteGroupName groupName);

	/**
	 * 根据sid和userId查找文章
	 * 
	 * @date Created at 2018年4月2日 下午3:23:23
	 * @Author 谷泽昊
	 * @param sid
	 * @param userId
	 * @return
	 */
	public ArticleDelete findBySidAndUserIdAndGroupName(String sid, String userId,ArticleDeleteGroupName groupName);

	/**
	 * 根据分组和用户id查询
	 * @date Created at 2018年4月2日  下午3:31:50
	 * @Author 谷泽昊
	 * @param groupName
	 * @param userId
	 * @return
	 */
	public List<ArticleDelete> findByGroupNameAndUserId(ArticleDeleteGroupName groupName,String userId);

	public List<ArticleDelete> findByUserId(String userId);
	
	/**
	 * 分页根据分组和用户id查询
	 * @date Created at 2018年4月2日  下午3:58:49
	 * @Author 谷泽昊
	 * @param groupName
	 * @param user
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Page<ArticleDelete> findByGroupNameAndUserId(ArticleDeleteGroupName groupName, User user, int pageNo, int pageSize);

	public void updateForHistoryData(List<ArticleDelete> articleDeletes);
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