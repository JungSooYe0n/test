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

import java.util.List;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.trs.netInsight.widget.article.entity.ArticleDelete;
import com.trs.netInsight.widget.article.entity.enums.ArticleDeleteGroupName;
import com.trs.netInsight.widget.article.repository.ArticleDeleteRepository;
import com.trs.netInsight.widget.article.service.IArticleDeleteService;

/**
 * 文章删除service
 * 
 * @Type ArticleDeleteServiceImpl.java
 * @author 谷泽昊
 * @date 2018年4月2日 下午3:08:06
 * @version
 */
@Service
public class ArticleDeleteServiceImpl implements IArticleDeleteService {

	@Autowired
	private ArticleDeleteRepository articleDeleteRepository;

	@Override
	public ArticleDelete addArticleDelete(String sid, ArticleDeleteGroupName groupName) {
		if (StringUtils.isNotBlank(sid) && groupName!=null) {
			ArticleDelete articleDelete = new ArticleDelete(sid, groupName);
			return articleDeleteRepository.saveAndFlush(articleDelete);
		}
		return null;
	}

	@Override
	public ArticleDelete findBySidAndUserIdAndGroupName(String sid, String userId,ArticleDeleteGroupName groupName) {
		return articleDeleteRepository.findBySidAndUserIdAndGroupName(sid, userId,groupName);
	}

	@Override
	public List<ArticleDelete> findByGroupNameAndUserId(ArticleDeleteGroupName groupName, String userId) {
		return articleDeleteRepository.findByGroupNameAndUserId(groupName, userId);
	}

	@Override
	public List<ArticleDelete> findByUserId(String userId) {
		return articleDeleteRepository.findByUserIdAndSubGroupIdIsNotNull(userId);
	}

	@Override
	public Page<ArticleDelete> findByGroupNameAndUserId(ArticleDeleteGroupName groupName, User user, int pageNo, int pageSize) {
		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = new PageRequest(pageNo, pageSize, sort);
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			return articleDeleteRepository.findByGroupNameAndUserId(groupName, user.getId(), pageable);
		}else {
			return articleDeleteRepository.findByGroupNameAndSubGroupId(groupName, user.getSubGroupId(), pageable);
		}
	}

	@Override
	public void updateForHistoryData(List<ArticleDelete> articleDeletes) {
		articleDeleteRepository.save(articleDeletes);
		articleDeleteRepository.flush();
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