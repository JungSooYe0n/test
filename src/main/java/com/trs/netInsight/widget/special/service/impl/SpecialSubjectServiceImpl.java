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
package com.trs.netInsight.widget.special.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.special.service.ISpecialSubjectService;

/**
 * 
 * @Type SpecialSubjectServiceImpl.java 
 * @author 谷泽昊
 * @date 2017年11月27日 下午6:16:50
 * @version 
 */
@Service
@Transactional
public class SpecialSubjectServiceImpl implements ISpecialSubjectService{

	@Autowired
	private SpecialSubjectRepository specialSubjectRepository;
	
	@Autowired
	private SpecialProjectRepository specialProjectRepository;
	
	@Override
	public List<SpecialSubject> findAll(Criteria<SpecialSubject> criteria) {
		return specialSubjectRepository.findAll(criteria);
	}

	@Override
	public void save(SpecialSubject specialSubject) {
		specialSubjectRepository.save(specialSubject);		
	}

	@Override
	public SpecialSubject findOne(String specialId) {
		return specialSubjectRepository.findOne(specialId);
	}

	@Override
	public List<SpecialSubject> findBySubjectId(String id) {
		return specialSubjectRepository.findBySubjectId(id);
	}

	@Override
	public void delete(String subjectId,String oneOrTwo) {
		User loginUser = UserUtils.getUser();
		if("one".equals(oneOrTwo)){
			// 删除主题下专题，方案以及专题下方案
			SpecialSubject subject = specialSubjectRepository.findOne(subjectId);
			//把sequence比当前主题大的主题和专项都往前挪
			Criteria<SpecialSubject> criteriaSubject = new Criteria<>();
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				criteriaSubject.add(Restrictions.eq("userId", loginUser.getId()));
			}else {
				criteriaSubject.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			}
			criteriaSubject.add(Restrictions.gt("sequence", subject.getSequence()));
			List<SpecialSubject> findAll = findAll(criteriaSubject);
			//该用户有多少个主题
			for(SpecialSubject specialSubject:findAll){
				if(StringUtil.isEmpty(specialSubject.getSubjectId())){
					specialSubject.setSequence(specialSubject.getSequence()-1);
					save(specialSubject);
				}
			}
			//只查当前用户有多少个一级的专项
			Criteria<SpecialProject> criteriaSpecial = new Criteria<>();
			criteriaSpecial.add(Restrictions.eq("groupId", ""));
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				criteriaSpecial.add(Restrictions.eq("userId", UserUtils.getUser().getId()));
			}else {
				criteriaSpecial.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			}
			criteriaSpecial.add(Restrictions.gt("sequence", subject.getSequence()));
			List<SpecialProject> list = specialProjectRepository.findAll(criteriaSpecial);
			for(SpecialProject special:list){
				special.setSequence(special.getSequence()-1);
				specialProjectRepository.save(special);
			}
			/////
			specialSubjectRepository.delete(subject);
			// for(SpecialSubject subject : list){
			// 删除主题下专题
			Criteria<SpecialSubject> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("subjectId", subject.getId()));
			List<SpecialSubject> findAll2 = specialSubjectRepository.findAll(criteria);
			for (SpecialSubject sujectTwo : findAll2) {
				specialSubjectRepository.delete(sujectTwo);
				String twoId = sujectTwo.getId();
				List<SpecialProject> byTwoId = specialProjectRepository.findByGroupId(twoId);
				// 删除专题下方案
				for (SpecialProject special : byTwoId) {
					String specialId = special.getId();
					specialProjectRepository.delete(specialId);
				}
			}
			// 删除主题下方案
			List<SpecialProject> byOneId = specialProjectRepository.findByGroupId(subjectId);
			for (SpecialProject project : byOneId) {
				String specialId = project.getId();
				specialProjectRepository.delete(specialId);
			}
//			specialSubjectRepository.delete(subjectId);	
		}else if("two".equals(oneOrTwo)){
			//比当前专题大的专题和专项都往前挪
			SpecialSubject findOne = findOne(subjectId);
			//专题
			Criteria<SpecialSubject> criteria = new Criteria<>();
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				criteria.add(Restrictions.eq("userId", loginUser.getId()));
			}else {
				criteria.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			}
			criteria.add(Restrictions.eq("subjectId", findOne.getSubjectId()));
			criteria.add(Restrictions.gt("sequence", findOne.getSequence()));
			List<SpecialSubject> findAll = findAll(criteria);
			for(SpecialSubject subject:findAll){
				subject.setSequence(subject.getSequence()-1);
				save(subject);
			}
			//专项
			Criteria<SpecialProject> criteriaSpecial = new Criteria<>();
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				criteriaSpecial.add(Restrictions.eq("userId", loginUser.getId()));
			}else {
				criteriaSpecial.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			}
			criteriaSpecial.add(Restrictions.eq("groupId", findOne.getSubjectId()));
			criteriaSpecial.add(Restrictions.gt("sequence", findOne.getSequence()));
			List<SpecialProject> specialList = specialProjectRepository.findAll(criteriaSpecial);
			for(SpecialProject special:specialList){
				special.setSequence(special.getSequence()-1);
				specialProjectRepository.save(special);
			}
//			// 删除专题
			specialSubjectRepository.delete(subjectId);		
			// 删除专题下方案
			List<SpecialProject> byGroupId = specialProjectRepository.findByGroupId(subjectId);
			for (SpecialProject specialProject : byGroupId) {
				String specialId = specialProject.getId();
				specialProjectRepository.delete(specialId);
			}
		}
//		specialSubjectRepository.delete(subjectId);		
	}

	@Override
	public void deleteByUserId(String userId) {
		List<SpecialSubject> specialSubjects = specialSubjectRepository.findByUserId(userId);
		if (ObjectUtil.isNotEmpty(specialSubjects)){
			specialSubjectRepository.delete(specialSubjects);
			specialSubjectRepository.flush();
		}
	}

	/**
	 * 添加一级分类
	 */
	@Override
	public Object addSubject(String name) {
		//新添加的放前边  把当前一级sequence存为1  在他之前的专题和一级分类存为+1  查找时按sequence正序排列
		if (StringUtil.isNotEmpty(name)) {
			SpecialSubject subject = new SpecialSubject(name);
			Criteria<SpecialSubject> criteria = new Criteria<>();
			User loginUser = UserUtils.getUser();
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				criteria.add(Restrictions.eq("userId", loginUser.getId()));
			}else {
				criteria.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			}
//			criteria.add(Restrictions.eq("subjectId", ""));
			List<SpecialSubject> findAll = findAll(criteria);
			//该用户有多少个主题
			for(SpecialSubject specialSubject:findAll){
				if(StringUtil.isEmpty(specialSubject.getSubjectId())){
					specialSubject.setSequence(specialSubject.getSequence()+1);
					//存入数据库
					save(specialSubject);
				}
			}
			//只查当前用户有多少个一级的专项
			Criteria<SpecialProject> criteriaSpecial = new Criteria<>();
			criteriaSpecial.add(Restrictions.eq("groupId", ""));
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				criteriaSpecial.add(Restrictions.eq("userId", UserUtils.getUser().getId()));
			}else {
				criteriaSpecial.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			}
			List<SpecialProject> list = specialProjectRepository.findAll(criteriaSpecial);
			for(SpecialProject special:list){
				special.setSequence(special.getSequence()+1);
				//存入数据库
				specialProjectRepository.save(special);
			}
			//该用户有多少个一级的专项
			subject.setSequence(1);
			save(subject);
			return subject.getId();
		}
		return null;
	}

	@Override
	public List<SpecialSubject> findByIdIn(List<String> ids) {
		return specialSubjectRepository.findByIdIn(ids);
	}

	@Override
	public List<SpecialSubject> findByUserId(String userId) {
		return specialSubjectRepository.findByUserId(userId);
	}

	@Override
	public void updateAll(List<SpecialSubject> specialSubjects) {
		specialSubjectRepository.save(specialSubjects);
		specialSubjectRepository.flush();
	}

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