/*
 * Project: netInsight
 * 
 * File Created at 2017年11月20日
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

import com.trs.netInsight.widget.user.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

/**
 * 机构Repository
 * @Type OrganizationRepository.java 
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:13:03
 * @version 
 */
@Repository("organizationRepository")
public interface OrganizationRepository extends JpaRepository<Organization, String>,JpaSpecificationExecutor<Organization>{

	/**
	 * 根据用户名查找机构
	 * @date Created at 2017年11月20日  下午2:24:55
	 * @Author 谷泽昊
	 * @param organizationName
	 * @return
	 */
	public List<Organization> findByOrganizationName(String organizationName);

	/**
	 * 根据用户名查找机构
	 * @date Created at 2017年11月20日  下午2:24:55
	 * @Author 谷泽昊
	 * @param organizationName
	 * @return
	 */
	public Page<Organization> findByOrganizationName(String organizationName, Pageable pageable);

	/**
	 * 根据机构名称分页查询机构
	 * @date Created at 2017年11月20日  下午4:27:01
	 * @Author 谷泽昊
	 * @param organizationName
	 * @param pageable
	 * @return
	 */
	public Page<Organization> findByOrganizationNameLike(String organizationName, Pageable pageable);

	/**
	 * 根据id批量查询
	 * @date Created at 2018年9月25日  下午8:09:45
	 * @Author 谷泽昊
	 * @param ids
	 * @param pageable
	 * @return
	 */
	public Page<Organization> findByIdIn(Collection<String> ids, Pageable pageable);

	/**
	 * 根据id批量查询
	 * @date Created at 2018年9月25日  下午8:09:45
	 * @Author 谷泽昊
	 * @param ids
	 * @param pageable
	 * @return
	 */
	public Page<Organization> findByOrganizationNameLikeAndIdIn(String organizationName, Collection<String> ids, Pageable pageable);
	
	/**
	 * 根据id批量不查询
	 * @date Created at 2018年9月25日  下午8:09:54
	 * @Author 谷泽昊
	 * @param ids
	 * @param pageable
	 * @return
	 */
	public Page<Organization> findByIdNotIn(Collection<String> ids, Pageable pageable);

	//public Page<Organization> findAll(Pageable pageable);

	public Page<Organization> findById(String id, Pageable pageable);

	public Collection<Organization> findByOrganizationNameLikeAndIdIn(String organizationName, Collection<String> ids);

	/*public Collection<Organization> findBySystemNameLikeAndIdIn(String systemName, Collection<String> ids);*/

	@Query(value = "select distinct `head_of_sales` FROM `organization` where head_of_sales is not null", nativeQuery = true)
	public List<String> findAllForHeadOfSale();

	/**
	 * 批量修改隐藏状态
	 *
	 *
	 * @Return : void
	 */
	@Transactional
	@Modifying
	@Query(value = "UPDATE `organization` o SET o.login_count = ?1 WHERE o.id = ?2", nativeQuery = true)
	void updateOrganizationLoginCount(Integer loginCount, String organizationId);

}


/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月20日 谷泽昊 creat
 */