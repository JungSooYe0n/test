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
package com.trs.netInsight.widget.article.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.article.entity.ArticleDelete;
import com.trs.netInsight.widget.article.entity.enums.ArticleDeleteGroupName;

/**
 * 文章删除 repository
 * 
 * @Type ArticleDeleteRepository.java
 * @author 谷泽昊
 * @date 2018年4月2日 下午3:07:03
 * @version
 */
@Repository
public interface ArticleDeleteRepository extends JpaRepository<ArticleDelete, String> {

	/**
	 * 根据sid和userId查询
	 * 
	 * @date Created at 2018年4月2日 下午3:24:33
	 * @Author 谷泽昊
	 * @param sid
	 * @param userId
	 * @return
	 */
	public ArticleDelete findBySidAndUserIdAndGroupName(String sid, String userId,ArticleDeleteGroupName groupName);

	/**
	 * 根据分组和用户id查询
	 * @date Created at 2018年4月2日  下午3:32:52
	 * @Author 谷泽昊
	 * @param groupName
	 * @param userId
	 * @return
	 */
	public List<ArticleDelete> findByGroupNameAndUserId(ArticleDeleteGroupName groupName, String userId);

	/**
	 * 分页查询 根据分组和用户id查询
	 * @date Created at 2018年4月2日  下午4:00:38
	 * @Author 谷泽昊
	 * @param groupName
	 * @param userId
	 * @param pageable
	 * @return
	 */
	public Page<ArticleDelete> findByGroupNameAndUserId(ArticleDeleteGroupName groupName, String userId, Pageable pageable);

	public Page<ArticleDelete> findByGroupNameAndSubGroupId(ArticleDeleteGroupName groupName, String subGroupId, Pageable pageable);

	public List<ArticleDelete> findByUserIdAndSubGroupIdIsNotNull(String userId);
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