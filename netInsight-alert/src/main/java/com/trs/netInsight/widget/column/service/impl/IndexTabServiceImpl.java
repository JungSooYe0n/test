/*
 * Project: netInsight
 * 
 * File Created at 2017年12月4日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.column.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.service.IIndexTabService;

/**
 * @Type IndexTabServiceImpl.java
 * @Desc 三级栏目检索服务实现
 * @author yan.changjiang
 * @date 2017年12月4日 下午6:36:41
 * @version
 */
@Service
public class IndexTabServiceImpl implements IIndexTabService {

	@Autowired
	private IndexTabRepository indexTabRepository;
	
	@Autowired
	private IndexPageRepository indexPageRepository;
	
	@Autowired
	private IndexTabMapperRepository indexTabMapperRepository;

	@Override
	public List<IndexTab> findByParentId(String parentId) {
		return indexTabRepository.findByParentId(parentId);
	}

	@Override
	public List<IndexTab> findByUserId(String userId) {
		return indexTabRepository.findByUserIdAndSubGroupIdIsNull(userId);
	}

	@Override
	public List<IndexTab> findSimple(String keyWord) {
		List<IndexTab> byKeyWordNotLikeAndKeyWordIsNotNull = indexTabRepository.findByKeyWordIsNotNullAndKeyWordIndexIsNotNullAndTrslIsNull();
		List<IndexTab> indexTabs = new ArrayList<>();
		for (IndexTab indexTab : byKeyWordNotLikeAndKeyWordIsNotNull) {
			if (!indexTab.getKeyWord().contains(keyWord)){
				indexTabs.add(indexTab);
			}
		}
		return indexTabs;
	}

	@Override
	public Object save(IndexTab indexTab) {
		IndexTab save = indexTabRepository.save(indexTab);
		// 保存映射表
		IndexTabMapper mapper = save.mapper();
		IndexPage indexPage = indexPageRepository.findOne(save.getParentId());
		mapper.setIndexPage(indexPage);
		mapper = indexTabMapperRepository.save(mapper);
		return mapper;
	}
	
	@Override
	public Object save(IndexTab indexTab, boolean share) {
		IndexTab save = indexTabRepository.save(indexTab);
		// 保存映射表
		IndexTabMapper mapper = save.mapper(share);
		IndexPage indexPage = indexPageRepository.findOne(save.getParentId());
		mapper.setIndexPage(indexPage);
		mapper = indexTabMapperRepository.save(mapper);
		return mapper;
	}

	@Override
	public IndexTab findOne(String indexId) {
		return indexTabRepository.findOne(indexId);
	}

	@Override
	public void delete(String indexId) {
		indexTabRepository.delete(indexId);
	}

	@Override
	public void deleteByUserId(String userId) {
		List<IndexTab> indexTabs = indexTabRepository.findByUserId(userId);
		if (ObjectUtil.isNotEmpty(indexTabs)){
//			indexTabRepository.delete(indexTabs);
//			indexTabRepository.flush();
		}
	}

	@Override
	public int getSubGroupColumnCount(User user) {
		if (UserUtils.isRoleAdmin()){
			List<IndexTab> indexTabs = indexTabRepository.findByUserId(user.getId());
			if (ObjectUtil.isNotEmpty(indexTabs)){
				//机构管理员
				return indexTabs.size();
			}
		}
		if (UserUtils.isRoleOrdinary(user)){
			//如果是普通用户 受用户分组 可创建资源的限制
			//查询该用户所在的用户分组下 是否有可创建资源
			List<IndexTab> bySubGroupId = indexTabRepository.findBySubGroupId(user.getSubGroupId());
			if (ObjectUtil.isNotEmpty(bySubGroupId)){
				return bySubGroupId.size();
			}

		}
		return 0;
	}

	@Override
	public int getSubGroupColumnCountForSubGroup(String subGroupId) {
		List<IndexTab> bySubGroupId = indexTabRepository.findBySubGroupId(subGroupId);
		if (ObjectUtil.isNotEmpty(bySubGroupId)){
			return bySubGroupId.size();
		}
		return 0;
	}

	@Override
	public List<IndexTab> findWordCloudIndexTab(String groupName) {
		return indexTabRepository.findByTypeAndGroupName(ColumnConst.CHART_WORD_CLOUD,groupName);
	}

	@Override
	public List<IndexTab> findMapIndexTab(String groupName) {
		return indexTabRepository.findByTypeAndGroupName(ColumnConst.CHART_MAP,groupName);
	}


	@Override
	public List<IndexTab> findByOrganizationId(String organizationId) {
		return indexTabRepository.findByOrganizationId(organizationId);
	}

	@Override
	public Object update(IndexTab threeEntity) {
		return indexTabRepository.saveAndFlush(threeEntity);
	}
	@
	Override
	public Object update(IndexTab threeEntity,String mapperId, boolean share) {
		IndexTab indexTab = indexTabRepository.saveAndFlush(threeEntity);
		IndexTabMapper mapper = indexTabMapperRepository.findOne(mapperId);
		mapper.setTabWidth(Integer.valueOf(indexTab.getTabWidth()));
		mapper.setShare(share);
		mapper = indexTabMapperRepository.saveAndFlush(mapper);
		return mapper;
	}

	@Override
	public List<IndexTab> findByParentIdAndUser(String parentId, User user, Sort sort) {
		List<IndexTab> indexTabs = null;
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			indexTabs = indexTabRepository.findByParentIdAndUserId(parentId, user.getId(), sort);
		}else {
			indexTabs = indexTabRepository.findByParentIdAndSubGroupId(parentId, user.getSubGroupId(), sort);
		}
		return indexTabs;
	}

	@Override
	public IndexTab findByIdAndUser(String id, User user) {
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			return indexTabRepository.findByIdAndUserId(id, user.getId());
		}else {
			return indexTabRepository.findByIdAndSubGroupId(id, user.getSubGroupId());
		}

	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年12月4日 yan.changjiang creat
 */