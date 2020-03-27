package com.trs.netInsight.widget.alert.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.alert.entity.AlertBackups;

@Repository
public interface AlertBackupsRepository extends JpaSpecificationExecutor<AlertBackups>, PagingAndSortingRepository<AlertBackups, String> {

	/**
	 * 根据用户id检索
	 * 
	 * @param userId
	 * @param sort
	 * @return
	 */
	public List<AlertBackups> findByUserId(String userId, Sort sort);
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午2:16:35
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param sort
	 * @return
	 */
	public List<AlertBackups> findByOrganizationId(String organizationId, Sort sort);
	/**
	 * 根据用户id分页检索
	 * 
	 * @param userId
	 * @param pageable
	 * @return
	 */
	public List<AlertBackups> findByUserId(String userId, Pageable pageable);
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午2:25:07
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageable
	 * @return
	 */
	public List<AlertBackups> findByOrganizationId(String organizationId, Pageable pageable);
	
	/**
	 * 根据规则id查询
	 * @param ruleId
	 * @return
	 */
	public List<AlertBackups> findByAlertRuleBackupsId(String ruleId);

}
