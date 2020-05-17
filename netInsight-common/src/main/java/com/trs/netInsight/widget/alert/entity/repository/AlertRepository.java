package com.trs.netInsight.widget.alert.entity.repository;

import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 预警数据实体检索Repository
 *
 * Created by ChangXiaoyang on 2017/6/15.
 */
@Repository
public interface AlertRepository extends PagingAndSortingRepository<AlertEntity, String>,
		JpaSpecificationExecutor<AlertEntity>, JpaRepository<AlertEntity, String> {

	/**
	 * 根据用户id检索
	 * 
	 * @param userId
	 * @param sort
	 * @return
	 */
	public List<AlertEntity> findByUserId(String userId, Sort sort);

	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:16:35
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param sort
	 * @return
	 */
	public List<AlertEntity> findByOrganizationId(String organizationId, Sort sort);

	/**
	 * 根据用户id分页检索
	 * 
	 * @param userId
	 * @param pageable
	 * @return
	 */
	public List<AlertEntity> findByUserId(String userId, Pageable pageable);

	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:25:07
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageable
	 * @return
	 */
	public List<AlertEntity> findByOrganizationId(String organizationId, Pageable pageable);

	/**
	 * 
	 * @param ruleId
	 * @return
	 */
	public List<AlertEntity> findByAlertRuleBackupsId(String ruleId);

	/**
	 * 分页查询
	 * 
	 * @param userName
	 * @param sendWay
	 * @param id
	 * @param pageable
	 * @return
	 */
	public Page<AlertEntity> findByReceiverAndSendWayAndAlertRuleBackupsId(String userName, SendWay sendWay, String id,
                                                                           Pageable pageable);

	/**
	 * 不带分页的
	 *
	 * @param userName
	 * @param sendWay
	 * @param id
	 * @return
	 */
	public List<AlertEntity> findByReceiverAndSendWayAndAlertRuleBackupsIdAndCreatedTimeBetween(String userName,
                                                                                                SendWay sendWay, String id, Date start, Date end);

	/**
	 * sid的where in
	 *
	 * @param sids
	 * @return
	 */
	public List<AlertEntity> findByUserIdAndSidIn(String userId, Collection<String> sids);

	public List<AlertEntity> findBySubGroupIdAndSidIn(String subGroupId, Collection<String> sids);
	/**
	 * 站内预警查询发给自己的
	 *
	 * @date Created at 2018年11月23日 上午10:48:41
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param userName
	 * @param sms
	 * @param parseStart
	 * @param parseEnd
	 * @return
	 */
	public List<AlertEntity> findByReceiverAndSendWayAndCreatedTimeBetween(String userName, SendWay sms,
                                                                           Date parseStart, Date parseEnd);

}
