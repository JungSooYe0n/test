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
package com.trs.netInsight.widget.user.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 用户service
 * 
 * @Type IUserService.java
 * @author 谷泽昊
 * @date 2017年11月17日 下午7:20:06
 * @version
 */
public interface IUserService {

	/**
	 * 根据用户名查询用户
	 * 
	 * @date Created at 2017年11月17日 下午7:21:39
	 * @Author 谷泽昊
	 * @param userName
	 * @return
	 */
	public User findByUserName(String userName);

	/**
	 * 根据id查询
	 * 
	 * @date Created at 2017年11月20日 下午4:29:35
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 */
	public User findById(String id);

	public User findCurrentUser();

	/**
	 * 添加用户
	 * 
	 * @date Created at 2017年11月20日 上午11:52:30
	 * @Author 谷泽昊
	 * @param user
	 * @param copyFlag
	 */
	public String add(User user, boolean copyFlag);

	/**
	 * 添加用户
	 * @param user
	 * @param columnSync 同步日常监测 栏目
	 * @param specialSync 同步专题分析 不带分组专题
	 * @param specialSyncLevel 同步专题分析 带分组专题
	 * @return
	 */
	//public String add(User user,String columnSync, String[] specialSync,String specialSyncLevel);

	/**
	 * 修改用户
	 * 
	 * @date Created at 2017年11月20日 上午11:52:30
	 * @Author 谷泽昊
	 * @param user
	 * @param isResetPassword
	 *            是否强制掉线
	 */
	public String update(User user, boolean isResetPassword);

	/**
	 * 根据机构id删除机构
	 * 
	 * @date Created at 2017年11月20日 下午3:26:47
	 * @Author 谷泽昊
	 * @param id
	 */
	public void deleteByOrganizationId(String id);

	/**
	 * 根据用户删除用户
	 * 
	 * @date Created at 2017年12月5日 下午2:22:32
	 * @Author 谷泽昊
	 * @param user
	 */
	public void deleteByUser(User user);

	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月5日 上午9:22:03
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public List<User> findByOrganizationId(String organizationId);
	public List<User> findRoleAdminByOrganizationId(String organizationId);

	/**
	 * 根据用户名，分页查询
	 *
	 * @date Created at 2018年9月25日 上午9:53:53
	 * @Author 谷泽昊
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Page<User> findByOrganizationId(String retrievalCondition, String retrievalInformation, int pageNo,
                                           int pageSize);

	/**
	 * 批量删除
	 *
	 * @date Created at 2017年12月26日 下午3:12:12
	 * @Author 谷泽昊
	 * @param list
	 */
	public void deleteByUser(List<User> list);

	/**
	 * 通过用户Id查询
	 *
	 * @param userId
	 * @return
	 */
	public List<User> findByUserId(String userId);

	/**
	 * 通过机构id查询
	 */
	public Page<User> pageOrganListOrSubGroup(int pageNo, int pageSize);

	/**
	 * 根据机构id并且非当前用户
	 *
	 * @date Created at 2018年3月14日 下午9:41:22
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param userId
	 * @return
	 */
	public List<User> findByOrganizationIdAndIdNot(String organizationId, String userId);

	/**
	 * 检索指定机构下的管理员
	 *
	 * @since changjiang @ 2018年7月17日
	 * @param orgId
	 * @return
	 * @Return : User
	 */
	public List<User> findOrgAmdin(String orgId);

	/**
	 * 查询所有
	 *
	 * @date Created at 2018年9月12日 上午10:30:05
	 * @Author 谷泽昊
	 * @return
	 */
	public List<User> findAll();

	public List<User> findAll(Specification<User> spec);
	/**
	 * 查询角色为空的用户
	 *
	 * @date Created at 2018年9月12日 上午11:05:02
	 * @Author 谷泽昊
	 * @return
	 */
	public List<User> findByRolesIsNull();

	/**
	 * 批量更新
	 *
	 * @date Created at 2018年9月14日 下午6:06:52
	 * @Author 谷泽昊
	 * @param users
	 * @return
	 */
	public List<User> updateAll(Collection<User> users);

	/**
	 * 根据id批量查询
	 *
	 * @date Created at 2018年9月17日 上午9:49:37
	 * @Author 谷泽昊
	 * @param rolePlatformIds
	 * @return
	 */
	public List<User> findByIds(String[] ids);

	/**
	 * 根据id批量查询
	 *
	 * @date Created at 2018年9月17日 上午9:49:37
	 * @Author 谷泽昊
	 * @param rolePlatformIds
	 * @return
	 */
	public List<User> findByIds(Collection<String> ids);

	/**
	 * 分页查询所有运维
	 *
	 * @date Created at 2018年9月17日 上午11:03:42
	 * @Author 谷泽昊
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Page<User> pageListByPlatform(String retrievalCondition, String retrievalInformation, int pageNo,
                                         int pageSize);

	/**
	 * 给运维设置负责机构
	 *
	 * @date Created at 2018年9月17日 上午11:46:19
	 * @Author 谷泽昊
	 * @param id
	 * @param organizationIds
	 * @return
	 * @throws TRSException
	 */
	public User addHoldOrganization(String id, String[] organizationIds) throws TRSException;

	/**
	 * 去掉运维负责机构
	 *
	 * @date Created at 2018年9月17日 上午11:46:50
	 * @Author 谷泽昊
	 * @param id
	 * @param organizationIds
	 * @return
	 * @throws TRSException
	 */
	public User deleteHoldOrganization(String id, String[] organizationIds) throws TRSException;

	/**
	 * 查询用户
	 *
	 * @date Created at 2018年9月25日 上午9:49:44
	 * @Author 谷泽昊
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @param pageNo
	 * @param pageSize
	 * @param organizationId
	 * @return
	 */
	public Page<User> findByOrganizationId(String retrievalCondition, String retrievalInformation, int pageNo,
                                           int pageSize, String organizationId);


	/**
	 * 根据用户名和机构id获取 机构id
	 *
	 * @date Created at 2018年11月5日 下午2:04:54
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param retrievalInformation
	 * @param object
	 * @return
	 */
	public Set<String> findOrgIdByUserNameAndOrgIds(String userName, Collection<String> orgIds);

	/**
	 * 判断该运维管理员是否管理此机构
	 *
	 * @date Created at 2018年12月10日 下午2:55:03
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param platformId
	 * @param organizationId
	 * @return
	 */
	public boolean isPlatformHoldOrganization(String platformId, String organizationId);

	/**
	 * 根据机构查询数量
	 * @date Created at 2018年12月17日  下午3:19:14
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public long countByOrganizationId(String organizationId);

	/**
	 * 查询分组下的所有用户
	 * @param subGroupId
	 * @return
	 */
	public List<User> findBySubGroupId(String subGroupId);

	/**
	 * 查询分组下的用户（不分页）
	 * @param subGroupId
	 * @return
	 */
	public Page<User> findBySubGroupId(int pageNo, int pageSize, String subGroupId);

	public List<User> findByCheckRole(String checkRole);

	public Set<String> findByUserNameLike(String userName);
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