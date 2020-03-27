package com.trs.netInsight.widget.alert.entity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;

/**
 * 预警账号检索repository
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Repository
public interface AlertAccountRepository
		extends PagingAndSortingRepository<AlertAccount, String>, JpaSpecificationExecutor<AlertAccount>,JpaRepository<AlertAccount,String> {

	/**
	 * 根据userId检索列表
	 * 
	 * @param userId
	 *            用户id
	 * @param sort
	 *            排序
	 * @return
	 */
	public List<AlertAccount> findByUserId(String userId, Sort sort);

	/**
	 * 根据用户分组id检索
	 * @param subGroupId
	 * @param sort
	 * @return
	 */
	public List<AlertAccount> findBySubGroupId(String subGroupId, Sort sort);
	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:23:26
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param sort
	 * @return
	 */
	public List<AlertAccount> findByOrganizationId(String organizationId, Sort sort);

	/**
	 * 根据用户id分页检索列表
	 * 
	 * @param userId
	 * @param pageable
	 * @return
	 */
	public Page<AlertAccount> findByUserId(String userId, Pageable pageable);
	public Page<AlertAccount> findBySubGroupId(String userId, Pageable pageable);
	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:23:37
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageable
	 * @return
	 */
	public List<AlertAccount> findByOrganizationId(String organizationId, Pageable pageable);

	/**
	 * 根据账号查
	 * 
	 * @param openId
	 * @return
	 */
	public List<AlertAccount> findByAccount(String account);

	/**
	 * 通过机构名和发送类型查找
	 * 
	 * @param userId
	 * @param type
	 * @return
	 */
	public List<AlertAccount> findByOrganizationIdAndType(String organizationId, SendWay type);

	/**
	 * 通过用户名 发送方式 分页查询
	 * 
	 * @param userId
	 * @param type
	 * @param pageable
	 * @return
	 */
	public List<AlertAccount> findByUserIdAndType(String userId, SendWay type, Pageable pageable);

	/**
	 * 通过用户名发送方式 不分页 查总数
	 * 
	 * @param userId
	 * @param type
	 * @return
	 */
	public List<AlertAccount> findByUserIdAndType(String userId, SendWay type);

	/**
	 * 通过用户分组id 发送方式 不分页 查询
	 * @param subGroupId
	 * @param type
	 * @return
	 */
	public List<AlertAccount> findBySubGroupIdAndType(String subGroupId, SendWay type);
	/**
	 * 根据账号，用户id和类型查询
	 * 
	 * @date Created at 2018年3月1日 上午11:40:26
	 * @Author 谷泽昊
	 * @param fromUserName
	 * @param id
	 * @param weChat
	 * @return
	 */
	public List<AlertAccount> findByAccountAndUserIdAndType(String fromUserName, String id, SendWay weChat);
	public List<AlertAccount> findByAccountAndSubGroupIdAndType(String fromUserName, String id, SendWay weChat);
	/**
	 * 根据用户名查询
	 * @param userName
	 * @param id
	 * @param weChat
	 * @return
	 */
	public List<AlertAccount> findByUserAccountAndUserIdAndType(String fromUserName, String id, SendWay weChat);

	/**
	 * 根据 类型和账号查询
	 * 
	 * @date Created at 2018年3月1日 下午2:03:14
	 * @Author 谷泽昊
	 * @param type
	 * @param account
	 * @param pageable
	 * @return
	 */
	public Page<AlertAccount> findByTypeAndAccountContainingAndUserId(SendWay type, String account, String id,
			Pageable pageable);
	public Page<AlertAccount> findByTypeAndAccountContainingAndSubGroupId(SendWay type, String account, String subGroupId,
																	  Pageable pageable);

	/**
	 * 根据类型
	 * 
	 * @date Created at 2018年3月1日 下午2:03:17
	 * @Author 谷泽昊
	 * @param type
	 * @param pageable
	 * @return
	 */
	public Page<AlertAccount> findByTypeAndUserId(SendWay type, String id, Pageable pageable);
	public Page<AlertAccount> findByTypeAndSubGroupId(SendWay type, String subGroupId, Pageable pageable);
	/**
	 * 根据账号
	 * 
	 * @date Created at 2018年3月1日 下午2:03:21
	 * @Author 谷泽昊
	 * @param account
	 * @param pageable
	 * @return
	 */
	public Page<AlertAccount> findByAccountContainingAndUserId(String account, String id, Pageable pageable);
	public Page<AlertAccount> findByAccountContainingAndSubGroupId(String account, String subGroupId, Pageable pageable);

	/**
	 * 根据用户名和类型查所关联的账号
	 * @param userName 用户名
	 * @param weChat 类型
	 * @return
	 * xiaoying 
	 */
	public List<AlertAccount> findByUserAccountAndType(String userName, SendWay weChat);
	
	/**
	 * 根据账号和账号类型查
	 * @param account
	 * @param send
	 * @return
	 */
	public List<AlertAccount> findByAccountAndType(String account, SendWay send);


}
