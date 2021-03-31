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
package com.trs.netInsight.widget.user.repository;

import com.trs.netInsight.support.log.entity.RequestTimeLog;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 权限Repository
 * 
 * @Type UserR.java
 * @author 谷泽昊
 * @date 2017年11月17日 下午7:12:38
 * @version
 */
@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

	/**
	 * 根据用户名查询用户
	 * 
	 * @date Created at 2017年11月17日 下午7:23:54
	 * @Author 谷泽昊
	 * @param userName
	 * @return
	 */
	public List<User> findByUserName(String userName);

	/**
	 * 根据机构id删除该机构下的用户
	 * 
	 * @date Created at 2017年11月20日 下午3:27:55
	 * @Author 谷泽昊
	 * @param id
	 */
	public void deleteByOrganizationId(String id);

	/**
	 * 根据机构id查询机构用户
	 * 
	 * @date Created at 2017年12月5日 上午9:26:08
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public List<User> findByOrganizationId(String organizationId);

	// /**
	// * 根据角色名字和机构id查询
	// * @date Created at 2017年12月12日 上午9:44:47
	// * @Author 谷泽昊
	// * @param roleName
	// * @param organizationId
	// * @return
	// */
	// @Query("SELECT u FROM User u WHERE u.role=(SELECT r.id FROM Role r WHERE
	// r.roleName LIKE %:roleName%) AND u.organizationId=:organizationId")
	// public List<User> findByRoleNameAndOrganizationId(String roleName,String
	// organizationId);

	/**
	 * 根据机构id和用户名查询
	 * 
	 * @date Created at 2017年12月15日 下午3:21:47
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param userName
	 * @return
	 */
	public Page<User> findByOrganizationIdAndUserNameContainingAndIdNot(String organizationId, String userName,
                                                                        String userId, Pageable pageable);

	/**
	 * 不排除自身
	 *
	 * @date Created at 2018年3月26日 下午3:01:05
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param userName
	 * @param pageable
	 * @return
	 */
	public Page<User> findByOrganizationIdAndUserNameContaining(String organizationId, String userName,
                                                                Pageable pageable);

	/**
	 * 根据机构id查询
	 *
	 * @date Created at 2017年12月15日 下午3:22:06
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageable
	 * @return
	 */
	public Page<User> findByOrganizationId(String organizationId, Pageable pageable);

	/**
	 * 排除自己
	 *
	 * @date Created at 2018年3月26日 下午2:56:11
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageable
	 * @return
	 */
	public Page<User> findByOrganizationIdAndIdNot(String organizationId, String userId, Pageable pageable);

	/**
	 * 根据用户id找
	 *
	 * @param userId
	 * @return
	 */
	public List<User> findByUserId(String userId);

	/**
	 * 根据机构id 非本用户查询
	 *
	 * @date Created at 2018年3月14日 下午9:42:19
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param userId
	 * @return
	 */
	public List<User> findByOrganizationIdAndIdNot(String organizationId, String userId);

	/**
	 * 查截止时间不为空的
	 *
	 * @return
	 */
	@Query("SELECT u FROM User u WHERE u.expireAt!='2050-01-01 00:00:00' AND u.status='0'")
	public List<User> findByExpireatNot();

	/**
	 * 检索指定机构下的管理员
	 *
	 * @since changjiang @ 2018年7月17日
	 * @param organizationId
	 * @param role
	 * @return
	 * @Return : List<User>
	 */
	public List<User> findByOrganizationIdAndCheckRole(String organizationId, String role);

	/**
	 * 查询角色为空的用户
	 *
	 * @date Created at 2018年9月12日 上午11:06:05
	 * @Author 谷泽昊
	 * @return
	 */
	public List<User> findByRolesIsNull();

	/**
	 * 根据用户角色查询
	 *
	 * @date Created at 2018年9月17日 上午11:16:57
	 * @Author 谷泽昊
	 * @param checkRole
	 * @param userName
	 * @param pageable
	 * @return
	 */
	public Page<User> findByCheckRoleAndUserNameContaining(String checkRole, String userName, Pageable pageable);

	/**
	 * 根据用户角色查询
	 *
	 * @date Created at 2018年9月17日 上午11:16:57
	 * @Author 谷泽昊
	 * @param checkRole
	 * @param pageable
	 * @return
	 */
	public Page<User> findByCheckRole(String checkRole, Pageable pageable);

	/**
	 * 根据用户角色查询  无需分页
	 * @param checkRole
	 * @return
	 */
	public List<User> findByCheckRole(String checkRole);
	/**
	 * 根据用户名和机构id查询机构id
	 *
	 * @date Created at 2018年11月5日 下午2:07:22
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param userName
	 * @param orgIds
	 * @return
	 */
	@Query(value = "SELECT u.organizationId FROM User u WHERE u.userName LIKE %?1% AND u.organizationId IN (?2)")
	public Set<String> findOrgIdByUserNameAndOrgIds(String userName, Collection<String> orgIds);

	/**
	 * 根据名字模糊检索
	 * @date Created at 2018年11月6日  下午4:40:20
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param userName
	 * @return
	 */
	@Query(value = "SELECT u.organizationId FROM User u WHERE u.userName LIKE %?1% AND u.organizationId != null")
	public Set<String> findOrgIdByUserName(String userName);

	/**
	 * 根据机构id查询数量
	 * @date Created at 2018年12月17日  下午3:20:06
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public long countByOrganizationId(String organizationId);

	/**
	 * 查询分组下的 所有 用户
	 * @param subGroupId
	 * @return
	 */
	public List<User> findBySubGroupId(String subGroupId);

	/**
	 * 查询分组下的 所有 用户(分页查询)
	 * @param subGroupId
	 * @return
	 */
	public Page<User> findBySubGroupId(String subGroupId, Pageable pageable);

	//查询数量 根据机构查
	@Query(value = "SELECT * " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
			"FROM user a " +
			"WHERE `id` IN (:userIds) order by :orderBy :orderBy :sort", nativeQuery = true)
	List<User> topTenMoudleUserId(@Param("userIds")List<String> userIds,@Param("orderBy")String orderBy,@Param("sort")String sort);

	//查询数量 根据机构查
	@Query(value = "SELECT * " +//distinct 这里我进行了一个去重的操作 这个可以忽略,业务需求
			"FROM user a " +
			"WHERE `organization_id` IN (:orgIds) order by :orderBy :sort", nativeQuery = true)
	List<User> topTenMoudle(@Param("orgIds")List<String> orgIds,@Param("orderBy")String orderBy,@Param("sort")String sort);

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