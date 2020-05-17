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

import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * @Type IIndexPageService.java
 * @Desc 以及栏目页检索接口
 * @author yan.changjiang
 * @date 2017年12月4日 下午6:36:12
 * @version
 */
public interface IIndexPageService {

	/**
	 * 根据父id检索列表
	 * 
	 * @param parentId
	 * @return
	 */
	public List<IndexPage> findByParentId(String parentId);

	/**
	 * 根据用户id检索列表
	 * 
	 * @param userId
	 * @param sort
	 * @return
	 */
	public List<IndexPage> findByUserId(String userId, Sort sort);

	/**
	 * 根据用户分组id检索列表
	 * @param subGroupId
	 * @param sort
	 * @return
	 */
	public List<IndexPage> findBySubGroupId(String subGroupId, Sort sort);
	/**
	 * 保存
	 * 
	 * @param oneAndTwo
	 * @return
	 */
	public IndexPage save(IndexPage oneAndTwo);
	
	/**
	 * 批量修改或保存
	 * @since changjiang @ 2018年9月19日
	 * @param indexPages
	 * @Return : void
	 */
	public void saveAndFulsh(List<IndexPage> indexPages);

	/**
	 * 根据id检索
	 * 
	 * @param oneId
	 * @return
	 */
	public IndexPage findOne(String oneId);

	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午2:19:37
	 * @Author 谷泽昊
	 * @param sort
	 * @return
	 */
	public List<IndexPage> findByOrganizationId(String organizationId, Sort sort);


	/**
	 * 同步 选中的 栏目组 及 栏目组包含的 栏目
	 * @param pageIds
	 * @param subGroup
	 */
	public void copySomePageAndTabToUserGroup(List<String> pageIds, SubGroup subGroup);

	/**
	 * 根据资源拥有者及pageId检索
	 * @since changjiang @ 2018年7月17日
	 * @param ownerId
	 * 			资源拥有者
	 * @param indexPageId
	 * 			pageId
	 * @return
	 * @Return : IndexPage
	 */
	public IndexPage findOne(String ownerId, String indexPageId);

	/**
	 * 根据用户id及共享状态检索栏目组
	 * @since changjiang @ 2018年9月20日
	 * @param userId
	 * @param share
	 * @return
	 * @Return : List<IndexPage>
	 */
	public List<IndexPage> findByUserIdAndShare(String userId, boolean share);

	/**
	 * 根据导航栏id检索栏目组
	 * @since changjiang @ 2018年10月11日
	 * @return
	 * @Return : List<IndexPage>
	 */
	public List<Map<String, Object>> findByTypeId();

	/**
	 * 获取栏目组，带着导航信息,专为api；目前只是默认日常监测下，自定义不算
	 * @param user token拥有者
	 * @return
	 */
	public List<Map<String, Object>> findIndexPageForApi(User user);

	/**
	 * 查找 某机构管理员账号下的栏目分组
	 * @param orgAdminId
	 * @return
	 */
	public List<Map<String, Object>> findByOrgAdminId(String orgAdminId);
	/**
	 * 根据用户id查询一级菜单
	 * @param userId
	 * @return list
	 */
	public List<Map<String, Object>> findByUserId(String userId);

	/**
	 * 根据用户id查询二级菜单
	 * @param token
	 * @return list
	 */
	public List<Map<String, Object>> findByUserIdAndTypeId(AppApiAccessToken token, String typeId);

	/**
	 * 只为迁移历史数据
	 * @param userId
	 * @return
	 */
	public List<IndexPage> findByUserIdForHistory(String userId);

	public void deleteByUserId(String userId);

	/**
	 * 修改历史数据 - 栏目分组 当前分组只有一层，将分组的parentid重置，并添加层级信息、修改parentname 为name
	 * @return
	 */
	Object updateHistoryIndexPage();
	IndexPage addIndexPage(String parentId, String name, String typeId, User user);
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年12月4日 yan.changjiang creat
 */