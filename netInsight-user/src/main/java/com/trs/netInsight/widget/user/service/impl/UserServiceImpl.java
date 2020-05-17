/*
 * Project: netInsight
 * 
 * File Created at 2017年11月17日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.user.service.impl;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.repository.UserRepository;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * 用户实现类
 * 
 * @Type UserServiceImpl.java
 * @author 谷泽昊
 * @date 2017年11月17日 下午7:20:55
 * @version
 */
@Service
public class UserServiceImpl implements IUserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private IOrganizationService organizationService;

	@Autowired
	private HelpService helpService;

	@Autowired
	private SessionDAO sessionDAO;


	@Override
	public User findByUserName(String userName) {
		List<User> list = userRepository.findByUserName(userName);
		if (list != null && list.size() > 0) {
			User user = list.get(0);
			String organizationId = user.getOrganizationId();
			if (StringUtils.isNotBlank(organizationId)) {
				Organization organization = organizationService.findById(user.getOrganizationId());
				if (organization != null) {
					user.setOrganizationName(organization.getOrganizationName());
				}
			}
			return user;
		}
		return null;
	}

	@Override
	public String add(User user, boolean copyFlag) {
		User save = userRepository.save(user);
		if (save == null) {
			return null;
		}
		return save.getId();
	}

//	@Override
//	public String add(User user, String columnSync, String[] specialSync,String specialSyncLevel) {
//		User save = userRepository.save(user);
//		if (null == save){
//			return null;
//		}
//		if (StringUtils.isNotBlank(columnSync)){
//			//同步选中的日常监测数据
//			List<DataSyncColumn> columnData = JSONArray.parseArray(columnSync, DataSyncColumn.class);
//			if (ObjectUtil.isNotEmpty(columnData)){
//                List<String> navIds = new ArrayList<>();
//                List<String> pageIds = new ArrayList<>();
//                for (DataSyncColumn dataSyncColumn : columnData) {
//                    //oneId对应导航
//                    navIds.add(dataSyncColumn.getId());
//					List<DataSyncColumn> list = dataSyncColumn.getList();
//					if (ObjectUtil.isNotEmpty(list)){
//						for (DataSyncColumn dataSyncColumn1 : list) {
//							pageIds.add(dataSyncColumn1.getId());
//						}
//					}
//                }
//                //同步导航
//                navigationService.copySomeNavigationToUser(navIds,user);
//                //同步栏目组及栏目
//                indexPageService.copySomePageAndTab(pageIds,user);
//            }
//		}
//		//同步专题分析
//        specialService.copySomeSpecial(specialSync,specialSyncLevel,user);
//		return save.getId();
//	}

	@Override
	public String update(User user, boolean isResetPassword) {
		User saveAndFlush = userRepository.saveAndFlush(user);
		if (saveAndFlush == null) {
			return null;
		}
		// 判断是否强行掉线
		if (isResetPassword) {
			compulsoryDownline(user.getUserName());
		}
		return saveAndFlush.getId();
	}

	@Override
	public void deleteByOrganizationId(String id) {
		userRepository.deleteByOrganizationId(id);
	}

	@Override
	public User findById(String id) {
		return userRepository.findOne(id);
	}

	@Override
	public User findCurrentUser() {

		return changeUser(userRepository.findOne(UserUtils.getUser().getId()));
	}

	@Override
	public List<User> findByOrganizationId(String organizationId) {
		return changeUser(userRepository.findByOrganizationId(organizationId));
	}

	@Override
	public List<User> findRoleAdminByOrganizationId(String organizationId) {
		return userRepository.findByOrganizationIdAndCheckRole(organizationId,CheckRole.ROLE_ADMIN.toString());
	}

	@Override
	@Transactional
	public void deleteByUser(User user) {
		// 删除用户时删除用户创建的所有东西
		try {
			//删除栏目分组
			helpService.deletePageByUserId(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			//根据关系映射表删除栏目 删除indexMapper
			helpService.deleteMapperByUserId(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			//删除用户下的专题分组
			helpService.deleteSubjectByUserId(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//删除用户下的专题
			helpService.deleteProjectByUserId(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//删除用户下的预警
			helpService.deleteAlertRuleByUserId(user.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//删除用户下绑定的预警账号
			List<AlertAccount> alertAccounts = helpService.findAlertAccountByUserId(user.getId());
			if (ObjectUtil.isNotEmpty(alertAccounts)){
				helpService.deleteAlertAccount(alertAccounts);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		userRepository.delete(user);
		// 强行掉线
		compulsoryDownline(user.getUserName());
	}

	@Override
	public Page<User> pageOrganListOrSubGroup(int pageNo, int pageSize) {
		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = new PageRequest(pageNo, pageSize, sort);
		User loginUser = UserUtils.getUser();
		String organizationId = loginUser.getOrganizationId();
		return changeUser(userRepository.findByOrganizationId(organizationId, pageable));
		/*if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			String organizationId = loginUser.getOrganizationId();
			return changeUser(userRepository.findByOrganizationId(organizationId, pageable));
		}else {
			String subGroupId = loginUser.getSubGroupId();
			return changeUser(userRepository.findBySubGroupId(subGroupId, pageable));
		}*/
	}

	@Override
	public Page<User> findByOrganizationId(String retrievalCondition, String retrievalInformation, int pageNo,
			int pageSize) {
		User user = UserUtils.getUser();
		return findByOrganizationId(retrievalCondition, retrievalInformation, pageNo, pageSize,
				user.getOrganizationId());
	}

	@Override
	public Page<User> pageListByPlatform(String retrievalCondition, String retrievalInformation, int pageNo,
			int pageSize) {

		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = new PageRequest(pageNo, pageSize, sort);
		Page<User> page = null;
		// 原生 查询sql
		Specification<User> criteria = new Specification<User>() {
			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(cb.equal(root.get("checkRole"), CheckRole.ROLE_PLATFORM.toString()));
				if (StringUtils.isBlank(retrievalCondition) && StringUtils.isNotBlank(retrievalInformation)) {
					predicate.add(cb.or(cb.like(root.get("userName"), "%" + retrievalInformation + "%"),
							cb.like(root.get("displayName"), "%" + retrievalInformation + "%")));
				} else if (StringUtils.isNotBlank(retrievalInformation) && StringUtils.isNotBlank(retrievalCondition)) {
					predicate.add(cb.like(root.get(retrievalCondition), "%" + retrievalInformation + "%"));

				}
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};
		page = userRepository.findAll(criteria, pageable);
		page = changeUser(page);
		return organizationSize(page);
	}

	/**
	 * 将机构名字添加到机构
	 * 
	 * @date Created at 2017年12月15日 下午4:58:16
	 * @Author 谷泽昊
	 * @param page
	 * @return
	 */
	private Page<User> changeUser(Page<User> page) {
		if (page == null) {
			return null;
		}
		List<User> content = page.getContent();
		for (User user : content) {
			String organizationId = user.getOrganizationId();
			if (StringUtils.isNotBlank(organizationId)) {
				Organization organization = organizationService.findById(organizationId);
				if (organization != null) {
					user.setOrganizationName(organization.getOrganizationName());
				}
			}
			//剩余有效期转换
			if (UserUtils.FOREVER_DATE.equals(user.getExpireAt())){
				user.setSurplusDate("永久");
			}else {
				String days = DateUtil.timeDifferenceDays(user.getExpireAt());
				user.setSurplusDate(days);
			}
			user.setLoginCount(UserUtils.getLoginCount(user.getUserName()+user.getId()));
		}
		return page;
	}

	/**
	 * 添加运维管理的机构数量
	 * 
	 * @date Created at 2018年9月26日 下午5:08:42
	 * @Author 谷泽昊
	 * @param page
	 * @return
	 */
	private Page<User> organizationSize(Page<User> page) {
		if (page == null) {
			return null;
		}
		List<User> content = page.getContent();
		for (User user : content) {
			Set<Organization> organizations = user.getOrganizations();
			if (organizations != null) {
				user.setOrganizationSize(organizations.size());
			}
		}
		return page;
	}

	/**
	 * 将机构名字添加到机构
	 * 
	 * @date Created at 2017年12月15日 下午4:58:16
	 * @Author 谷泽昊
	 * @param users
	 * @return
	 */
	private List<User> changeUser(List<User> users) {
		if (users == null) {
			return null;
		}
		Map<String,String> organInfo = new HashMap<>();
		for (User user : users) {
			String organizationId = user.getOrganizationId();
			if (StringUtils.isNotBlank(organizationId)) {
				//查询过的保存map中,防止多次查询
				if(organInfo.get(organizationId)!=null){
					user.setOrganizationName(organInfo.get(organizationId));
				}else{
					Organization organization = organizationService.findById(organizationId);
					if (organization != null) {
						user.setOrganizationName(organization.getOrganizationName());
						organInfo.put(organizationId,organization.getOrganizationName());
					}
				}
			}
			//剩余有效期转换
			if (UserUtils.FOREVER_DATE.equals(user.getExpireAt())){
				user.setSurplusDate("永久");
			}else {
				String days = DateUtil.timeDifferenceDays(user.getExpireAt());
				user.setSurplusDate(days);
			}
			user.setLoginCount(UserUtils.getLoginCount(user.getUserName()+user.getId()));

		}
		return users;
	}
	private User changeUser(User user) {
		if (user == null) {
			return null;
		}
			String organizationId = user.getOrganizationId();
			if (StringUtils.isNotBlank(organizationId)) {
				Organization organization = organizationService.findById(organizationId);
				if (organization != null) {
					user.setOrganizationName(organization.getOrganizationName());
				}
			}
			//剩余有效期转换
			if (UserUtils.FOREVER_DATE.equals(user.getExpireAt())){
				user.setSurplusDate("永久");
			}else {
				String days = DateUtil.timeDifferenceDays(user.getExpireAt());
				user.setSurplusDate(days);
			}
			user.setLoginCount(UserUtils.getLoginCount(user.getUserName()+user.getId()));

		return user;
	}
	@Override
	public void deleteByUser(List<User> list) {
		for (User user : list) {
			this.deleteByUser(user);
		}
	}

	@Override
	public List<User> findByUserId(String userId) {
		return changeUser(userRepository.findByUserId(userId));
	}

	@Override
	public List<User> findByOrganizationIdAndIdNot(String organizationId, String userId) {
		return changeUser(userRepository.findByOrganizationIdAndIdNot(organizationId, userId));
	}

	@Override
	public List<User> findOrgAmdin(String orgId) {
		return changeUser(userRepository.findByOrganizationIdAndCheckRole(orgId, UserUtils.ROLE_ADMIN));
	}

	@Override
	public List<User> findAll() {
		return changeUser(userRepository.findAll());
	}

	@Override
	public List<User> findAll(Specification<User> spec) {
		return userRepository.findAll(spec);
	}

	@Override
	public List<User> findByRolesIsNull() {
		return userRepository.findByRolesIsNull();
	}

	@Override
	public List<User> updateAll(Collection<User> users) {
		return userRepository.save(users);
	}

	@Override
	public List<User> findByIds(String[] ids) {
		if (ids == null || ids.length <= 0) {
			return null;
		}
		return findByIds(Arrays.asList(ids));
	}

	@Override
	public List<User> findByIds(Collection<String> ids) {
		if (ids == null || ids.size() <= 0) {
			return null;
		}
		return changeUser(userRepository.findAll(ids));
	}

	@Override
	public User addHoldOrganization(String id, String[] organizationIds) throws TRSException {
		User user = findById(id);
		if (UserUtils.isRolePlatform(user)) {
			List<Organization> organizations = organizationService.findByIds(organizationIds);
			if (organizations != null) {
				Set<Organization> organizationsSet = user.getOrganizations();
				organizationsSet.addAll(organizations);
				user.setOrganizations(organizationsSet);
				return userRepository.save(user);
			}
			return user;
		}
		throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户不是运维人员！");
	}

	@Override
	public User deleteHoldOrganization(String id, String[] organizationIds) throws TRSException {
		User user = findById(id);
		if (UserUtils.isRolePlatform(user)) {
			List<Organization> organizations = organizationService.findByIds(organizationIds);
			if (organizations != null) {
				Set<Organization> organizationsSet = user.getOrganizations();
				organizationsSet.removeAll(organizations);
				user.setOrganizations(organizationsSet);
				userRepository.save(user);
			}
			return user;
		}
		throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该用户不是运维人员！");
	}

	@Override
	public Page<User> findByOrganizationId(String retrievalCondition, String retrievalInformation, int pageNo,
			int pageSize, String organizationId) {
		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = new PageRequest(pageNo, pageSize, sort);
		Specification<User> criteria = new Specification<User>() {
			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				predicate.add(cb.equal(root.get("organizationId"), organizationId));
				if (StringUtils.isBlank(retrievalCondition) && StringUtils.isNotBlank(retrievalInformation)) {
					predicate.add(cb.or(cb.like(root.get("userName"), "%" + retrievalInformation + "%"),
							cb.like(root.get("displayName"), "%" + retrievalInformation + "%")));
				} else if (StringUtils.isNotBlank(retrievalInformation) && StringUtils.isNotBlank(retrievalCondition)) {
					predicate.add(cb.like(root.get(retrievalCondition), "%" + retrievalInformation + "%"));

				}
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};
		return changeUser(userRepository.findAll(criteria, pageable));
	}

	/**
	 * 强制下线
	 * 
	 * @date Created at 2018年9月19日 下午2:42:18
	 * @Author 谷泽昊
	 * @param userName
	 */
	private void compulsoryDownline(String userName) {
		String sessionId = RedisUtil.getString(UserUtils.USERNAME_LOGIN_USER + userName);
		if (StringUtils.isNotBlank(sessionId)) {
			Session session = null;
			try {
				session = sessionDAO.readSession(sessionId);
			} catch (Exception e) {
			}
			if (session != null) {
				// 强制退出
				sessionDAO.delete(session);
			}
		}
	}

	@Override
	public Set<String> findOrgIdByUserNameAndOrgIds(String userName, Collection<String> orgIds) {
		if (orgIds == null) {
			return userRepository.findOrgIdByUserName(userName);
		}
		return userRepository.findOrgIdByUserNameAndOrgIds(userName, orgIds);
	}

	@Override
	public boolean isPlatformHoldOrganization(String platformId, String organizationId) {
		User user = findById(platformId);
		if (!StringUtils.equals(user.getCheckRole(), CheckRole.ROLE_PLATFORM.toString())) {
			return false;
		}
		Set<Organization> organizations = user.getOrganizations();
		if (organizations == null || organizations.size() <= 0) {
			return false;
		}
		for (Organization organization : organizations) {
			String id = organization.getId();
			if (StringUtils.equals(id, organizationId)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public long countByOrganizationId(String organizationId) {
		return userRepository.countByOrganizationId(organizationId);
	}

	@Override
	public List<User> findBySubGroupId(String subGroupId) {
		List<User> users = userRepository.findBySubGroupId(subGroupId);
		//放入今日登录次数
		if (ObjectUtil.isNotEmpty(users)){
			for (User user : users) {
				user.setLoginCount(UserUtils.getLoginCount(user.getUserName()+user.getId()));
			}
		}
		return users;
	}

	@Override
	public Page<User> findBySubGroupId(int pageNo, int pageSize, String subGroupId) {
		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = new PageRequest(pageNo, pageSize, sort);
		Page<User> users = userRepository.findBySubGroupId(subGroupId, pageable);
		if (ObjectUtil.isNotEmpty(users)){
			return changeUser(users);
		}
		return null;
	}

	@Override
	public List<User> findByCheckRole(String checkRole) {
		return userRepository.findByCheckRole(checkRole);
	}

	@Override
	public Set<String> findByUserNameLike(String userName) {
		return userRepository.findOrgIdByUserName(userName);
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月17日 谷泽昊 creat
 */