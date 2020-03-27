package com.trs.netInsight.widget.alert.entity.repository;

import java.util.List;

import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;

/**
 * 
 * 预警规则实体检索Repository
 * 
 * Created by xiaoying on 2017/6/7.
 */
@Repository
public interface AlertRuleRepository
		extends PagingAndSortingRepository<AlertRule, String>, JpaSpecificationExecutor<AlertRule>,JpaRepository<AlertRule, String> {

	/**
	 * 根据用户id检索预警规则
	 * 
	 * @param userId
	 * @param sort
	 * @return
	 */
	public List<AlertRule> findByUserId(String userId, Sort sort);
	public List<AlertRule> findByUserId(String userId);
	/**
	 * 根据用户分组id 查询预警规则
	 * @param subGroupId
	 * @param sort
	 * @return
	 */
	public List<AlertRule> findBySubGroupId(String subGroupId, Sort sort);

	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:25:45
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param sort
	 * @return
	 */
	public List<AlertRule> findByOrganizationId(String organizationId, Sort sort);

	/**
	 * 根据用户id分页检索预警规则
	 * 
	 * @param userId
	 * @param pageable
	 * @return
	 */
	public Page<AlertRule> findByUserId(String userId, Pageable pageable);

	/**
	 * 根据用户分组id查询
	 * @param subGroupId
	 * @param pageable
	 * @return
	 */
	public Page<AlertRule> findBySubGroupId(String subGroupId, Pageable pageable);

	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:25:55
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageable
	 * @return
	 */
	public List<AlertRule> findByOrganizationId(String organizationId, Pageable pageable);

	/**
	 * 根据是否启动,手动自动查询
	 * @date Created at 2018年3月2日  上午10:19:08
	 * @Author 谷泽昊
	 * @param open
	 * @param auto
	 * @return
	 */
	public List<AlertRule> findByStatusAndAlertTypeAndFrequencyId(ScheduleStatus open, AlertSource auto,String frequencyId);
	public List<AlertRule> findBySpecialType(SpecialType specialType);
}
