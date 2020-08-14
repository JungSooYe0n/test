/*
 * Project: netInsight
 * 
 * File Created at 2017年11月27日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.special.service;

import java.util.List;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.trs.jpa.utils.Criteria;
import com.trs.netInsight.widget.special.entity.SpecialProject;

/**
 * 专项数据访问
 * @Type ISpecialProjectService.java 
 * @author 谷泽昊
 * @date 2017年11月27日 下午6:05:34
 * @version 
 */
public interface ISpecialProjectService {

	/**
	 * 根据id查询SpecialProject
	 * @date Created at 2017年11月27日  下午6:07:44
	 * @Author 谷泽昊
	 * @param specialId
	 * @return
	 */
	public SpecialProject findOne(String specialId);

	/**
	 * 保存
	 * @date Created at 2017年11月27日  下午6:32:07
	 * @Author 谷泽昊
	 * @param findOne
	 */
	public void save(SpecialProject findOne);

	/**
	 * 根据id删除
	 * @date Created at 2017年11月27日  下午6:32:14
	 * @Author 谷泽昊
	 * @param id
	 */
	public void delete(String id);
	public void deleteByUserId(String userId);
	/**
	 * 根据专题名字查找
	 * @date Created at 2017年11月27日  下午6:32:21
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 */
	public List<SpecialProject> findBySpecialNameContains(String name);

	/**
	 * 根据用户名查找
	 * @date Created at 2017年11月27日  下午6:32:49
	 * @Author 谷泽昊
	 * @param userId
	 * @param sort
	 * @return
	 */
	public List<SpecialProject> findByUserId(String userId, Sort sort);
	public List<SpecialProject> findBySubGroupId(String subGroupId, Sort sort);
	/**
	 * 查找全部
	 * @date Created at 2017年11月27日  下午6:32:57
	 * @Author 谷泽昊
	 * @param criteria2
	 * @return
	 */
	public List<SpecialProject> findAll(Criteria<SpecialProject> criteria2);
	List<SpecialProject> findAll(Criteria<SpecialProject> criteria2,Sort sort);

	public List<SpecialProject> findBySpecialType(SpecialType specialType);

	/**
	 * 根据分组查找
	 * @date Created at 2017年11月27日  下午6:33:13
	 * @Author 谷泽昊
	 * @param subjectId
	 * @return
	 */
	public List<SpecialProject> findByGroupId(String subjectId);

	/**
	 * 根据id查找
	 * @date Created at 2017年12月4日  下午7:49:30
	 * @Author 谷泽昊
	 * @param userId
	 * @param pageable
	 * @return
	 */
	public List<SpecialProject> findByUserId(String userId, Pageable pageable);

	/**
	 * 根据机构id查询
	 * @param organizationId 
	 * @date Created at 2017年12月28日  下午2:13:58
	 * @Author 谷泽昊
	 * @param pageable
	 * @return
	 */
	public List<SpecialProject> findByOrganizationId(String organizationId, Pageable pageable);

	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午2:58:13
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param sort
	 * @return
	 */
	public List<SpecialProject> findByOrganizationId(String organizationId, Sort sort);

	/**
	 * 联想词
	 * @date Created at 2018年1月10日  上午11:20:09
	 * @Author 谷泽昊
	 * @param name
	 * @param pageSize
	 * @return
	 */
	public List<String> associational(String name, int pageSize);

	/**
	 * 根据名称模糊查询
	 * @date Created at 2018年1月11日  下午3:58:54
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 */
	public List<Object> selectByWord(String name);

	/**
	 * 删除专项前的处理 比如要把跟他同级在他后边的专项和专题和二级都往前挪
	 * @param findOne 专项
	 */
	public void beforeDelete(SpecialProject findOne);

	public List<SpecialProject> findByIds(List<String> ids);

	/**
	 * 主要是添加专题分析时 判断资源数量
	 * @param user
	 * @return
	 */
	public int getSubGroupSpecialCount(User user);

	/**
	 * 主要在添加用户分组，设置用户分组用
	 * @param subGroupId
	 * @return
	 */
	public int getSubGroupSpecialCountForSubGroup(String subGroupId);

	/**
	 * 只为迁移历史数据
	 * @param userId
	 * @return
	 */
	public List<SpecialProject> findByUserId(String userId);

	public void updateAll(List<SpecialProject> specialProjects);
}


/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月27日 谷泽昊 creat
 */