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

import com.trs.jpa.utils.Criteria;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.user.entity.User;

/**
 * 专题
 * @Type ISpecialSubjectService.java 
 * @author 谷泽昊
 * @date 2017年11月27日 下午6:16:17
 * @version 
 */
public interface ISpecialSubjectService {

	/**
	 * 查询
	 * @date Created at 2017年11月27日  下午6:17:57
	 * @Author 谷泽昊
	 * @param criteria
	 * @return
	 */
	public List<SpecialSubject> findAll(Criteria<SpecialSubject> criteria);

	/**
	 * 保存
	 * @date Created at 2017年11月27日  下午6:18:24
	 * @Author 谷泽昊
	 * @param specialSubject
	 */
	public void save(SpecialSubject specialSubject);

	/**
	 * 根据id查找
	 * @date Created at 2017年11月27日  下午6:18:34
	 * @Author 谷泽昊
	 * @param specialId
	 * @return
	 */
	public SpecialSubject findOne(String specialId);

	/**
	 * 根据SubjectId查找
	 * @date Created at 2017年11月27日  下午6:18:50
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 */
	public List<SpecialSubject> findBySubjectId(String id);

	/**
	 * 根据id删除
	 * @date Created at 2017年11月27日  下午6:18:53
	 * @Author 谷泽昊
	 * @param subjectId 主题或者专题id
	 * @param oneOrTwo 一级传one  二级传two
	 */
	public void delete(String subjectId,String oneOrTwo);

	public Object deleteSubject(String subjectId) throws TRSException;
	public void deleteProject(String projectId) throws OperationException, TRSException;
	public void deleteByUserId(String userId);
	/**
	 * 新建主题
	 * @param name 主题名
	 * @return
	 * createdby xiaoying
	 */
	public Object addSubject(String name);

	/**
	 * 新建专题分组
	 * @param name
	 * @param parentId
	 * @return
	 */
	public Object addSubject(String name,String parentId,User user);

	/**
	 * 根据id 批量获取主题
	 * @param ids
	 * @return
	 */
	public List<SpecialSubject> findByIdIn(List<String> ids);

	/**
	 * 只为迁移历史数据
	 * @param userId
	 * @return
	 */
	public List<SpecialSubject> findByUserId(String userId);

	/**
	 * 只为迁移数据
	 * @param specialSubjects
	 */
	public void updateAll(List<SpecialSubject> specialSubjects);

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