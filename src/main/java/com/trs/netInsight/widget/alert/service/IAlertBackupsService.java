package com.trs.netInsight.widget.alert.service;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.alert.entity.AlertBackups;

public interface IAlertBackupsService {

	/**
	 * 根据用户id检索指定数量的预警列表信息
	 * 
	 * @param userId
	 *            用户id
	 * @param pageSize
	 *            检索长度
	 * @return
	 * @throws TRSException
	 */
	public List<AlertBackups> selectAll(String userId, int pageSize) throws TRSException;

	/**
	 * 保存
	 * 
	 * @date Created at 2017年11月27日 下午6:26:38
	 * @Author 谷泽昊
	 * @param alertEntity
	 */
	public void save(AlertBackups alertEntity);
	/**
	 * 批量保存
	 * @date Created at 2018年3月6日  上午9:49:03
	 * @Author 谷泽昊
	 * @param entities
	 */
	public void save(Iterable<AlertBackups> entities);


	/**
	 * 删除
	 * 
	 * @date Created at 2017年11月27日 下午6:26:45
	 * @Author 谷泽昊
	 * @param alertId
	 */
	public void delete(String alertId);
	
	/**
	 * 根据用户id查询
	 * 
	 * @date Created at 2017年12月4日 下午7:51:24
	 * @Author 谷泽昊
	 * @param uid
	 * @param sort
	 * @return
	 */
	public List<AlertBackups> findByUserId(String uid, Sort sort);

	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:12:53
	 * @Author 谷泽昊
	 * @param sort
	 * @return
	 */
	public List<AlertBackups> findByOrganizationId(String organizationId, Sort sort);

	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:41:26
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageSize
	 * @return
	 */
	public List<AlertBackups> selectByOrganizationId(String organizationId, Integer pageSize);
	
	/**
	 * 查询所有
	 * @return
	 */
	public Iterable<AlertBackups> findAll();
	
	/**
	 * 通过规则id查询
	 * @param ruleId
	 * @return
	 */
	public List<AlertBackups> findByRuleId(String ruleId);

	/**
	 * 批量删除
	 * @date Created at 2018年3月15日  下午9:59:22
	 * @Author 谷泽昊
	 * @param findByRuleId
	 */
	public void delete(List<AlertBackups> findByRuleId);
	
}
