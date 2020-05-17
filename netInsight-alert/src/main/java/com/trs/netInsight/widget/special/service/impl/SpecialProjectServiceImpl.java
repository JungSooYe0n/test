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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.SubGroupRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;

/**
 * 专项数据访问
 * 
 * @Type SpecialProjectServiceImpl.java
 * @author 谷泽昊
 * @date 2017年11月27日 下午6:06:25
 * @version
 */
@Service
@Transactional
public class SpecialProjectServiceImpl implements ISpecialProjectService {

	@Autowired
	private SpecialProjectRepository specialProjectRepository;
	@Autowired
	private SpecialSubjectRepository specialSubjectRepository;

	@Autowired
	private SubGroupRepository subGroupRepository;

	@Override
	public SpecialProject findOne(String specialId) {
		return specialProjectRepository.findOne(specialId);
	}

	@Override
	public void save(SpecialProject findOne) {
		specialProjectRepository.save(findOne);
	}

	@Override
	public void delete(String id) {
		SpecialProject findOne = findOne(id);
		//删除这个专项之前要把他同级后边的都往前挪
		beforeDelete(findOne);
		specialProjectRepository.delete(id);
	}

	@Override
	public void deleteByUserId(String userId) {
		List<SpecialProject> specialProjects = specialProjectRepository.findByUserId(userId);
		if (ObjectUtil.isNotEmpty(specialProjects)){
			specialProjectRepository.delete(specialProjects);
			specialProjectRepository.flush();
		}
	}

	@Override
	public void beforeDelete(SpecialProject findOne){
		//sequence大于他的都要往前挪
		User loginUser = UserUtils.getUser();
		if(StringUtil.isNotEmpty(findOne.getGroupId())){//是二级或者一级分类下的
			SpecialSubject subject = specialSubjectRepository.findOne(findOne.getGroupId());
			//二级分类下的就查找下边有多少个专项
			if(StringUtil.isNotEmpty(subject.getSubjectId())){
				Criteria<SpecialProject> criteria = new Criteria<>();
				criteria.add(Restrictions.eq("groupId", subject.getId()));
				criteria.add(Restrictions.gt("sequence", findOne.getSequence()));
//				criteria.add(Restrictions.eq("topFlag", ""));
				List<SpecialProject> findAll = findAll(criteria);
				for(SpecialProject special:findAll){
					if(StringUtil.isEmpty(special.getTopFlag())){
						special.setSequence(special.getSequence()-1);
						save(special);
					}
				}
			}else{
				Criteria<SpecialSubject> criteria = new Criteria<>();
				criteria.add(Restrictions.eq("subjectId", subject.getId()));
				criteria.add(Restrictions.gt("sequence", findOne.getSequence()));
				List<SpecialSubject> findAll = specialSubjectRepository.findAll(criteria);
				for(SpecialSubject specialSubject:findAll){
					specialSubject.setSequence(specialSubject.getSequence()-1);
					specialSubjectRepository.save(specialSubject);
				}
				Criteria<SpecialProject> criteriaSpecial = new Criteria<>();
				criteriaSpecial.add(Restrictions.eq("groupId", subject.getId()));
				criteriaSpecial.add(Restrictions.gt("sequence", findOne.getSequence()));
//				criteriaSpecial.add(Restrictions.eq("topFlag", ""));
				List<SpecialProject> specialList = findAll(criteriaSpecial);
				for(SpecialProject special:specialList){
					if(StringUtil.isEmpty(special.getTopFlag())){
						special.setSequence(special.getSequence()-1);
						save(special);
					}
				}
			}
		}else if(UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			//查当前用户有多少个一级的专项和多少个一级栏目
			Criteria<SpecialProject> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("groupId", ""));
			criteria.add(Restrictions.eq("userId", loginUser.getId()));
			criteria.add(Restrictions.gt("sequence", findOne.getSequence()));
//			criteria.add(Restrictions.eq("topFlag", ""));
			List<SpecialProject> findAll = findAll(criteria);
			for(SpecialProject special:findAll){
				if(StringUtil.isEmpty(special.getTopFlag())){
					special.setSequence(special.getSequence()-1);
					save(special);
				}
			}
			Criteria<SpecialSubject> criteriaSubject = new Criteria<>();
			criteriaSubject.add(Restrictions.eq("userId", loginUser.getId()));
			criteriaSubject.add(Restrictions.gt("sequence", findOne.getSequence()));
			List<SpecialSubject> list = specialSubjectRepository.findAll(criteriaSubject);
			//该用户有多少个主题
			for(SpecialSubject specialSubject:list){
				if(StringUtil.isEmpty(specialSubject.getSubjectId())){
					specialSubject.setSequence(specialSubject.getSequence()-1);
					specialSubjectRepository.save(specialSubject);
				}
			}
		}else {
			//查当前用户分组下有多少个一级的专项和多少个一级栏目
			Criteria<SpecialProject> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("groupId", ""));
			criteria.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			criteria.add(Restrictions.gt("sequence", findOne.getSequence()));
//			criteria.add(Restrictions.eq("topFlag", ""));
			List<SpecialProject> findAll = findAll(criteria);
			for(SpecialProject special:findAll){
				if(StringUtil.isEmpty(special.getTopFlag())){
					special.setSequence(special.getSequence()-1);
					save(special);
				}
			}
			Criteria<SpecialSubject> criteriaSubject = new Criteria<>();
			criteriaSubject.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
			criteriaSubject.add(Restrictions.gt("sequence", findOne.getSequence()));
			List<SpecialSubject> list = specialSubjectRepository.findAll(criteriaSubject);
			//该用户分组有多少个主题
			for(SpecialSubject specialSubject:list){
				if(StringUtil.isEmpty(specialSubject.getSubjectId())){
					specialSubject.setSequence(specialSubject.getSequence()-1);
					specialSubjectRepository.save(specialSubject);
				}
			}
		}
	}

	@Override
	public List<SpecialProject> findByIds(List<String> ids) {
		return specialProjectRepository.findByIdIn(ids);
	}

	@Override
	public List<SpecialProject> findBySpecialNameContains(String name) {
		return specialProjectRepository.findBySpecialNameContains(name);
	}

	@Override
	public List<SpecialProject> findByUserId(String userId, Sort sort) {
		return specialProjectRepository.findAllByUserId(userId, sort);
	}

	@Override
	public List<SpecialProject> findBySubGroupId(String subGroupId, Sort sort) {
		return specialProjectRepository.findAllBySubGroupId(subGroupId,sort);
	}

	@Override
	public List<SpecialProject> findAll(Criteria<SpecialProject> criteria2) {
		return specialProjectRepository.findAll(criteria2);
	}
	@Override
	public List<SpecialProject> findBySpecialType(SpecialType specialType) {
		return specialProjectRepository.findBySpecialType(specialType);
	}

	@Override
	public List<SpecialProject> findByGroupId(String subjectId) {
		return specialProjectRepository.findByGroupId(subjectId);
	}

	@Override
	public List<SpecialProject> findByUserId(String userId, Pageable pageable) {
		return specialProjectRepository.findByUserId(userId, pageable);
	}

	@Override
	public List<SpecialProject> findByOrganizationId(String organizationId, Pageable pageable) {
		return specialProjectRepository.findByOrganizationId(organizationId, pageable);
	}

	@Override
	public List<SpecialProject> findByOrganizationId(String organizationId, Sort sort) {
		return specialProjectRepository.findByOrganizationId(organizationId, sort);
	}

	@Override
	public List<String> associational(String name, int pageSize) {
		User loginUser = UserUtils.getUser();
		List<String> list = new ArrayList<>(10);
		Pageable pageable = new PageRequest(0, pageSize);
		// Spring data jpa 原生sql
		Specification<SpecialSubject> specialSubjectSpec = new Specification<SpecialSubject>() {
			@Override
			public Predicate toPredicate(Root<SpecialSubject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				// 标题
				Expression<String> as = root.get("name").as(String.class);
				if (StringUtils.isNotBlank(name)) {
					predicate.add(cb.like(as, "%" + name + "%"));
				}
				if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
					predicate.add(cb.equal(root.get("userId").as(String.class), loginUser.getId()));
				}else {
					predicate.add(cb.equal(root.get("subGroupId").as(String.class), loginUser.getSubGroupId()));
				}
				Predicate[] pre = new Predicate[predicate.size()];
				query.groupBy(as);
				return query.where(predicate.toArray(pre)).getRestriction();
			}

		};

		// 添加搜到的分组
		Page<SpecialSubject> specialSubjectList = specialSubjectRepository.findAll(specialSubjectSpec, pageable);
		if (specialSubjectList != null) {
			List<SpecialSubject> content = specialSubjectList.getContent();
			if (content != null && content.size() > 0) {
				for (SpecialSubject specialSubject : content) {
					list.add(specialSubject.getName());
				}
			}
		}
		// 判断是否大于10
		if (list != null && list.size() >= pageSize) {
			return list;
		} else {
			// 还差几个
			int num = pageSize - list.size();
			// Spring data jpa 原生sql
			Specification<SpecialProject> spec = new Specification<SpecialProject>() {
				@Override
				public Predicate toPredicate(Root<SpecialProject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					List<Predicate> predicate = new ArrayList<>();
					// 标题
					Expression<String> as = root.get("specialName").as(String.class);
					if (StringUtils.isNotBlank(name)) {
						predicate.add(cb.like(as, "%" + name + "%"));
					}
					if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
						predicate.add(cb.equal(root.get("userId").as(String.class), loginUser.getId()));
					}else {
						predicate.add(cb.equal(root.get("subGroupId").as(String.class), loginUser.getSubGroupId()));
					}
					Predicate[] pre = new Predicate[predicate.size()];
					query.groupBy(as);
					return query.where(predicate.toArray(pre)).getRestriction();
				}

			};
			Pageable pageableSpecialProject = new PageRequest(0, num);
			Page<SpecialProject> pageSpecialProject = specialProjectRepository.findAll(spec, pageableSpecialProject);
			if (pageSpecialProject != null) {
				List<SpecialProject> content = pageSpecialProject.getContent();
				if (content != null && content.size() > 0) {
					if (content.size() > num) {
						for (int i = 0; i < num; i++) {
							list.add(content.get(i).getSpecialName());
						}
					} else {
						for (SpecialProject specialProject : content) {
							list.add(specialProject.getSpecialName());
						}
					}
				}
			}

		}

		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> selectByWord(String name) {
		Set<String> set = new HashSet<>();
		List<Object> listMap = new ArrayList<>();
		User loginUser = UserUtils.getUser();

		// 搜索一级
		Sort sort = new Sort(Sort.Direction.ASC, "sequence");
		List<SpecialSubject> listSpecialSubject = null;
		if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			listSpecialSubject =specialSubjectRepository.findByUserIdAndNameContainingAndFlag(loginUser.getId(),
					name, 0, sort);
		}else {
			listSpecialSubject = specialSubjectRepository.findBySubGroupIdAndNameContainingAndFlag(loginUser.getSubGroupId(),
					name, 0, sort);
		}

		// 判断是有文件夹
		if (listSpecialSubject != null && listSpecialSubject.size() > 0) {
			for (SpecialSubject specialSubject : listSpecialSubject) {
				Map<String, Object> map = new HashMap<>();

				map.put("flag", 0);
				map.put("specialName", specialSubject.getName());
				map.put("id", specialSubject.getId());
				map.put("flagColor",false);
				set.add(specialSubject.getId());

				// 搜索二级文件夹
				List<SpecialSubject> listSpecialSubject2 = null;
				if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
					listSpecialSubject2 = specialSubjectRepository
							.findByUserIdAndSubjectIdAndFlag(loginUser.getId(), specialSubject.getId(), 1, sort);
				}else {
					listSpecialSubject2 = specialSubjectRepository
							.findBySubGroupIdAndSubjectIdAndFlag(loginUser.getSubGroupId(), specialSubject.getId(), 1, sort);
				}

				List<Object> listMapSubject = new ArrayList<>();
				if (listSpecialSubject2 != null && listSpecialSubject2.size() > 0) {
					for (SpecialSubject specialSubject2 : listSpecialSubject2) {
						Map<String, Object> mapSubject = new HashMap<>();
						mapSubject.put("flag", 1);
						mapSubject.put("specialName", specialSubject2.getName());
						mapSubject.put("id", specialSubject2.getId());
						mapSubject.put("flagColor",false);
						set.add(specialSubject2.getId());
						// 三级
						List<SpecialProject> listSpecialProject = null;
						if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
							listSpecialProject = specialProjectRepository
									.findByUserIdAndGroupId(loginUser.getId(), specialSubject2.getId(), sort);
						}else {
							listSpecialProject = specialProjectRepository
									.findBySubGroupIdAndGroupId(loginUser.getSubGroupId(), specialSubject2.getId(), sort);
						}
						List<Map> arrayList = new ArrayList<>();
						if (listSpecialProject != null && listSpecialProject.size() > 0) {
							for (SpecialProject specialProject : listSpecialProject) {
								set.add(specialProject.getId());
								Map<String, Object> stringObjectMap = forSpeicalProjectToMap(specialProject);
								arrayList.add(stringObjectMap);
							}
						}
						mapSubject.put("children", arrayList);
						listMapSubject.add(mapSubject);
					}
				}
				map.put("children", listMapSubject);
				// 一级下直接专题
				Criteria<SpecialProject> criteriaProject = new Criteria<>();
				if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
					criteriaProject.add(Restrictions.eq("userId", loginUser.getId()));
				}else {
					criteriaProject.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
				}

				criteriaProject.add(Restrictions.eq("groupId", specialSubject.getId()));
				criteriaProject.orderByASC("sequence");
				List<SpecialProject> listSpecialProject = specialProjectRepository.findAll(criteriaProject);
				if (listSpecialProject != null && listSpecialProject.size() > 0) {
					for (SpecialProject specialProject : listSpecialProject) {
						set.add(specialProject.getId());
						if (!StringUtils.equals("top", specialProject.getTopFlag())) {
							// 把不置顶的放进去 左侧竖着显示
							Map<String, Object> stringObjectMap = forSpeicalProjectToMap(specialProject);
							listMapSubject.add(stringObjectMap);
						}
					}
					

				}
				map.put("children", listMapSubject);
				listMap.add(map);
			}

		}
		// 搜索二级
		List<SpecialSubject> listSpecialSubject2 = null;
		if (set != null && set.size() > 0) {
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				listSpecialSubject2 = specialSubjectRepository.findByUserIdAndNameContainingAndFlagAndIdNotIn(loginUser.getId(), name,
						1, set, sort);
			}else {
				listSpecialSubject2 = specialSubjectRepository.findBySubGroupIdAndNameContainingAndFlagAndIdNotIn(loginUser.getSubGroupId(), name,
						1, set, sort);
			}

		} else {
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				listSpecialSubject2 =specialSubjectRepository.findByUserIdAndNameContainingAndFlag(loginUser.getId(),
						name, 1, sort);
			}else {
				listSpecialSubject2 = specialSubjectRepository.findBySubGroupIdAndNameContainingAndFlag(loginUser.getSubGroupId(),
						name, 1, sort);
			}
		//	listSpecialSubject2 = specialSubjectRepository.findByUserIdAndNameContainingAndFlag(userId, name, 1, sort);
		}
		Map<String, Object> one = new HashMap<>();
		if (listSpecialSubject2 != null && listSpecialSubject2.size() > 0) {
			for (SpecialSubject specialSubject2 : listSpecialSubject2) {
				Map<String, Object> mapZhuan = new HashMap<>();
				SpecialSubject subject = specialSubjectRepository.findOne(specialSubject2.getSubjectId());
				set.add(subject.getId());
				set.add(specialSubject2.getId());
				Map<String, Object> mapZhu = (Map<String, Object>) one.get(subject.getId());
				if (mapZhu == null) {
					mapZhu = new HashMap<>();
				}
				List<Map<String, Object>> listZhuan = (List<Map<String, Object>>) mapZhu.get("children");
				if (listZhuan == null) {
					listZhuan = new ArrayList<>();
				}

				List<SpecialProject> findByGroupId = specialProjectRepository.findByGroupId(specialSubject2.getId());
				List<Map> arrayList = new ArrayList<>();
				if (findByGroupId != null && findByGroupId.size() > 0) {
					for (SpecialProject specialProject : findByGroupId) {
						set.add(specialProject.getId());
						Map<String, Object> stringObjectMap = forSpeicalProjectToMap(specialProject);
						arrayList.add(stringObjectMap);
					}
				}
				mapZhuan.put("flag", 1);
				mapZhuan.put("specialName", specialSubject2.getName());
				mapZhuan.put("id", specialSubject2.getId());
				mapZhuan.put("flagColor",false);
				mapZhuan.put("children", arrayList);
				listZhuan.add(mapZhuan);

				mapZhu.put("flag", 0);
				mapZhu.put("specialName", subject.getName());
				mapZhu.put("id", subject.getId());
				mapZhu.put("flagColor",false);
				mapZhu.put("children", listZhuan);

				one.put(subject.getId(), mapZhu);
			}
		}
		// 搜索专题
		List<SpecialProject> listSpecialProject = null;
		if (set != null && set.size() > 0) {
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				listSpecialProject = specialProjectRepository.findByUserIdAndSpecialNameContainingAndIdNotIn(loginUser.getId(), name,
						set, sort);
			}else {
				listSpecialProject = specialProjectRepository.findBySubGroupIdAndSpecialNameContainingAndIdNotIn(loginUser.getSubGroupId(), name,
						set, sort);
			}

		} else {
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				listSpecialProject = specialProjectRepository.findByUserIdAndSpecialNameContaining(loginUser.getId(), "%" + name + "%",
						sort);
			}else {
				listSpecialProject = specialProjectRepository.findBySubGroupIdAndSpecialNameContaining(loginUser.getSubGroupId(), "%" + name + "%",
						sort);
			}

		}
		if (listSpecialProject != null && listSpecialProject.size() > 0) {
			Map<String, Object> two = new HashMap<>();
			for (SpecialProject specialProject : listSpecialProject) {
				if (StringUtils.isNotBlank(specialProject.getGroupId())) {
					SpecialSubject subject = specialSubjectRepository.findOne(specialProject.getGroupId());
					if (subject != null) {
						// 是一级文件夹还是二级
						if (subject.getFlag() == 1) {
							// 先判断二级
							Map<String, Object> mapZhuan = (Map<String, Object>) two.get(subject.getId());
							if (mapZhuan == null) {
								mapZhuan = new HashMap<>();
							}
							List<Object> listZhuan = (List<Object>) mapZhuan.get("children");
							if (listZhuan == null) {
								listZhuan = new ArrayList<>();
							}
							if (!StringUtils.equals("top", specialProject.getTopFlag())) {
								Map<String, Object> stringObjectMap = forSpeicalProjectToMap(specialProject);
								// 把不置顶的放进去 左侧竖着显示
								listZhuan.add(stringObjectMap);
							}
							mapZhuan.put("flag", 1);
							mapZhuan.put("specialName", subject.getName());
							mapZhuan.put("id", subject.getId());
							mapZhuan.put("flagColor",false);
							mapZhuan.put("children", listZhuan);
							two.put(subject.getId(), mapZhuan);
							// 判断一级
							SpecialSubject subjectOne = specialSubjectRepository.findOne(subject.getSubjectId());

							Map<String, Object> mapZhu = (Map<String, Object>) one.get(subjectOne.getId());
							if (mapZhu == null) {
								mapZhu = new HashMap<>();
							}
							List<Object> listZhu = (List<Object>) mapZhu.get("children");
							if (listZhu == null) {
								listZhu = new ArrayList<>();
							}

							listZhu.add(mapZhuan);

							mapZhu.put("flag", 0);
							mapZhu.put("specialName", subjectOne.getName());
							mapZhu.put("id", subjectOne.getId());
							mapZhu.put("flagColor",false);
							mapZhu.put("children", listZhu);

							one.put(subjectOne.getId(), mapZhu);

						} else if (subject.getFlag() == 0) {
							Map<String, Object> mapZhu = (Map<String, Object>) one.get(subject.getId());
							if (mapZhu == null) {
								mapZhu = new HashMap<>();
							}
							List<Object> listZhuan = (List<Object>) mapZhu.get("children");
							if (listZhuan == null) {
								listZhuan = new ArrayList<>();
							}
							if (!StringUtils.equals("top", specialProject.getTopFlag())) {
								// 把不置顶的放进去 左侧竖着显示
								Map<String, Object> stringObjectMap = forSpeicalProjectToMap(specialProject);
								listZhuan.add(stringObjectMap);
							}

							mapZhu.put("flag", 0);
							mapZhu.put("specialName", subject.getName());
							mapZhu.put("id", subject.getId());
							mapZhu.put("flagColor",false);
							mapZhu.put("children", listZhuan);

							one.put(subject.getId(), mapZhu);
						}
					}
				} else {
					if (!StringUtils.equals("top", specialProject.getTopFlag())) {
						// 把不置顶的放进去 左侧竖着显示
						Map<String, Object> stringObjectMap = forSpeicalProjectToMap(specialProject);
						one.put(specialProject.getId(), stringObjectMap);
					}
				}
			}
		}
		for (Map.Entry<String, Object> entry : one.entrySet()) {
			listMap.add(entry.getValue());
		}
		return listMap;
	}

	@Override
	public int getSubGroupSpecialCount(User user) {
		if (UserUtils.isRoleAdmin()){
			List<SpecialProject> specialProjects = specialProjectRepository.findByUserId(user.getId(), new Sort(Sort.Direction.DESC, "createdTime"));
			if (ObjectUtil.isNotEmpty(specialProjects)){
				//机构管理员
				return specialProjects.size();
			}
		}
		if (UserUtils.isRoleOrdinary(user)){
			//如果是普通用户 受用户分组 可创建资源的限制
			//查询该用户所在的用户分组下 是否有可创建资源
			SubGroup subGroup = subGroupRepository.findOne(user.getSubGroupId());
			List<SpecialProject> bySubGroupId = specialProjectRepository.findBySubGroupId(subGroup.getId());
			if (ObjectUtil.isNotEmpty(bySubGroupId)){
				return bySubGroupId.size();
			}

		}
		return 0;
	}

	@Override
	public int getSubGroupSpecialCountForSubGroup(String subGroupId) {
		List<SpecialProject> bySubGroupId = specialProjectRepository.findBySubGroupId(subGroupId);
		if (ObjectUtil.isNotEmpty(bySubGroupId)){
			return bySubGroupId.size();
		}
		return 0;
	}

	@Override
	public List<SpecialProject> findByUserId(String userId) {
		return specialProjectRepository.findByUserId(userId);
	}

	@Override
	public void updateAll(List<SpecialProject> specialProjects) {
		specialProjectRepository.save(specialProjects);
		specialProjectRepository.flush();
	}

	private Map<String,Object> forSpeicalProjectToMap(SpecialProject specialProject){
		if (ObjectUtil.isNotEmpty(specialProject)){
			Map<String, Object> map = new HashMap<>();
			map.put("id",specialProject.getId());
			map.put("specialName",specialProject.getSpecialName());
			map.put("source",specialProject.getSource());
			map.put("startTime",specialProject.getStartTime());
			map.put("endTime",specialProject.getEndTime());
			map.put("timeRange",specialProject.getTimeRange());
			map.put("flag",specialProject.getFlag());
			map.put("flagColor",false);

			return map;
		}
		return null;
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