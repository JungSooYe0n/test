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
package com.trs.netInsight.widget.column.service;

import java.util.List;

import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Sort;

import com.trs.jpa.utils.Criteria;
import com.trs.netInsight.widget.column.entity.IndexTab;

/**
 * @Type IThreeEntityService.java
 * @Desc 三级栏目检索服务接口
 * @author Administrator
 * @date 2017年12月4日 下午6:28:18
 * @version
 */
public interface IIndexTabService {

	/**
	 * 通过一级栏目id找下一级栏目
	 * 
	 * @param parentId
	 *            一级栏目id
	 * @return
	 */
	public List<IndexTab> findByParentId(String parentId);
	
	/**
	 * 根据pageId以及用户id检索tab列表
	 * @since changjiang @ 2018年7月17日
	 * @param parentId
	 * @param user
	 * @return
	 * @Return : List<IndexTab>
	 */
	public List<IndexTab> findByParentIdAndUser(String parentId, User user, Sort sort);

	/**
	 * 根据userId检索列表
	 * 
	 * @param userId
	 * @return
	 */
	public List<IndexTab> findByUserId(String userId);
	
	/**
	 * 根据id及用户id检索
	 * @since changjiang @ 2018年7月17日
	 * @param id
	 * @param user
	 * @return
	 * @Return : IndexTab
	 */
	public IndexTab findByIdAndUser(String id, User user);
	
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午2:35:07
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public List<IndexTab> findByOrganizationId(String organizationId);


	public List<IndexTab> findSimple(String keyWord);

	/**
	 * 保存
	 * 
	 * @param threeEntity
	 * @return
	 */
	public Object save(IndexTab threeEntity);
	
	/**
	 * 保存
	 * @since changjiang @ 2018年10月9日
	 * @param threeEntity
	 * @param share
	 * @return
	 * @Return : Object
	 */
	public Object save(IndexTab threeEntity, boolean share);
	
	/**
	 * 修改
	 * 
	 * @param threeEntity
	 * @return
	 */
	public Object update(IndexTab threeEntity);
	/**
	 * 修改
	 * @since changjiang @ 2018年10月9日
	 * @param threeEntity
	 * @return
	 * @Return : Object
	 */
	public Object update(IndexTab threeEntity, String mapperId, boolean share);

	/**
	 * 根据id检索
	 * 
	 * @param indexId
	 * @return
	 */
	public IndexTab findOne(String indexId);

	/**
	 * 根据id删除
	 * 
	 * @param indexId
	 */
	public void delete(String indexId);

	public void deleteByUserId(String userId);
	/**
	 * 主要是添加栏目时 判断资源数量
	 * @param user
	 * @return
	 */
	public int getSubGroupColumnCount(User user);

	/**
	 * 主要在添加用户分组，设置用户分组用
	 * @param subGroupId
	 * @return
	 */
	public int getSubGroupColumnCountForSubGroup(String subGroupId);

	public List<IndexTab> findWordCloudIndexTab(String groupName);

	public List<IndexTab> findMapIndexTab(String groupName);

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年12月4日 Administrator creat
 */