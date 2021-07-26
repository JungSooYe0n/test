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
package com.trs.netInsight.widget.alert.service;

import java.util.Date;
import java.util.List;

import com.trs.netInsight.support.fts.entity.FtsDocumentAlert;
import com.trs.netInsight.widget.alert.entity.AlertWebSocket;
import com.trs.netInsight.widget.alert.entity.PageAlert;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.trs.jpa.utils.Criteria;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;

/**
 * @Type IAlertService.java
 * @Desc 预警接口服务
 * @author yan.changjiang
 * @date 2017年11月20日 下午5:37:13
 * @version
 */
public interface IAlertService {

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
	public List<AlertEntity> selectAll(String userId, int pageSize) throws TRSException;

	/**
	 * 保存
	 * 
	 * @date Created at 2017年11月27日 下午6:26:38
	 * @Author 谷泽昊
	 * @param alertEntity
	 */
	public void save(AlertEntity alertEntity);
	/**
	 * 批量保存
	 * @date Created at 2018年3月6日  上午9:49:03
	 * @Author 谷泽昊
	 * @param entities
	 */
	public void save(Iterable<AlertEntity> entities);


	/**
	 * 删除
	 * 
	 * @date Created at 2017年11月27日 下午6:26:45
	 * @Author 谷泽昊
	 * @param alertId
	 */
	public void delete(String alertId);
	/**
	 * 删除
	 * 
	 * @date Created at 2017年11月27日 下午6:26:45
	 * @Author 谷泽昊
	 * @param ids
	 */
	public void delete(String[] ids);

	/**
	 * 根据用户id查询
	 * 
	 * @date Created at 2017年12月4日 下午7:51:24
	 * @Author 谷泽昊
	 * @param uid
	 * @param sort
	 * @return
	 */
	public List<AlertEntity> findByUserId(String uid, Sort sort);

	/**
	 * 根据用户角色的不同  决定查询用户下的还是用户分组下的预警
	 * @param user
	 * @return
	 */
	public List<AlertEntity> findByUser(User user,List<String> sids);
	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:12:53
	 * @Author 谷泽昊
	 * @param sort
	 * @return
	 */
	public List<AlertEntity> findByOrganizationId(String organizationId, Sort sort);

	/**
	 * 根据机构id查询
	 * 
	 * @date Created at 2017年12月28日 下午2:41:26
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageSize
	 * @return
	 */
	public List<AlertEntity> selectByOrganizationId(String organizationId, Integer pageSize);
	
	/**
	 * 按条件查找
	 * @param criteria
	 * @return
	 */
	public Page<AlertEntity> findAll(Criteria<AlertEntity> criteria,int pageNo,int pageSize);
	/**
	 * 站内预警列表
	 * @param criteria
	 * @return
	 */
	public Object findSMS(String userId,Criteria<AlertEntity> criteria,int pageNo,int pageSize) throws OperationException;
	/**
	 * 按条件不分页查找 为满足mysql和hybase匹配
	 * @param criteria
	 * @return
	 */
	public List<AlertEntity> findAll(Criteria<AlertEntity> criteria);

	/**
	 * 批量查询
	 * @date Created at 2018年3月14日  下午4:16:52
	 * @Author 谷泽昊
	 * @param listString
	 * @return
	 */
	public List<FtsDocumentAlert> findbyIds(String userId, String listString) throws TRSException;

	/**
	 * 根据规则Id查询
	 * @param id 规则id
	 * @param pageSize 查几条
	 * @return
	 * xiaoying
	 */
	public Page<AlertEntity> findByReceiverAndSendWayAndAlertRuleBackupsIdAndCreatedTimeBetween(String userName,SendWay sendWay,String id,int pageNo,int pageSize,Date start,Date end);
	
	/**
	 * 不带分页的
	 * @param userName
	 * @param sendWay
	 * @param id
	 * @param start
	 * @param end
	 * @return
	 */
	public List<AlertEntity> findByReceiverAndSendWayAndAlertRuleBackupsIdAndCreatedTimeBetween(String userName,SendWay sendWay,String id,Date start,Date end);
	
	/**
	 * 原预警代码  不关联alert_netinsight工程
	 * @param pageNo 0开始 第几页
	 * @param pageSize 一页几条
	 * @param way 发送方式
	 * @param source 来源
	 * @param time 时间
	 * @param receivers 接收者
	 * @param invitationCard  论坛主贴 0 /回帖 1
	 * @param forwarPrimary 微博 原发 primary / 转发 forward
	 * @param keywords 结果中搜索
	 * @return
	 * @throws OperationException 
	 */
	public Object alertListLocal(int pageNo,int pageSize,String way,String source,String time,String receivers,String invitationCard,
			String forwarPrimary,String keywords,String fuzzyValueScope) throws OperationException;
	/**
	 * 关联alert_netinsight工程
	 * @param pageNo 0开始 第几页
	 * @param pageSize 一页几条
	 * @param way 发送方式
	 * @param source 来源
	 * @param time 时间
	 * @param receivers 接收者
	 * @param invitationCard  论坛主贴 0 /回帖 1
	 * @param forwarPrimary 微博 原发 primary / 转发 forward
	 * @param keywords 结果中搜索
	 * @return
	 * @throws OperationException 
	 */
	public Object alertListHttp(int pageNo,int pageSize,String way,String source,String time,String receivers,String invitationCard,
			String forwarPrimary,String keywords,String fuzzyValueScope) throws OperationException;

	/**
	 * 已发和站内预警列表
	 * @param pageNo 第几页  从0开始
	 * @param pageSize 一页几条
	 * @param way 站内SMS  否则已发
	 * @param source 来源
	 * @param time 时间
	 * @param receivers 接收人
	 * @param invitationCard 对应国内论坛的nreserved1
	 * @param forwarPrimary 微博的原发 primary 转发 forward
	 * @param keywords 关键字
	 * @return
	 */
	public PageAlert alertListHybase(int pageNo, int pageSize, String way, String source, String time, String receivers, String invitationCard,
									 String forwarPrimary, String keywords, String fuzzyValueScope) throws TRSException;

	/**
	 * 已发和站内预警列表
	 * @param pageNo 第几页  从0开始
	 * @param pageSize 一页几条
	 * @param way 站内SMS  否则已发
	 * @param source 来源
	 * @param time 时间
	 * @param receivers 接收人
	 * @param invitationCard 对应国内论坛的nreserved1
	 * @param forwarPrimary 微博的原发 primary 转发 forward
	 * @param keywords 关键字
	 * @return
	 */
	public PageAlert alertListHybaseNew(int pageNo, int pageSize, String way, String source, String time, String receivers, String invitationCard,
									 String forwarPrimary, String keywords,String sort, String fuzzyValueScope) throws TRSException;

	/**
	 * 原预警删除  本地不用启动alert_netinsight
	 * @param id 要删除的id 多个时以分号分割
	 * @return
	 * @throws OperationException 
	 */
	public Object deleteLocal(String id) throws OperationException;

	/**
	 * 关联alert_netinsight的删除预警
	 * @param id 要删除的id 多个时以分号分割
	 * @return
	 * @throws OperationException
	 */
	public Object deleteHttp(String id) throws OperationException;

	/**
	 * 站内预警
	 * @date Created at 2018年11月23日  上午10:47:28
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
	/**
	 * 发送站内预警信息相关方法
	 * @param alertWebSocket
	 * 增删查
	 */
	public void saveReceiveAlert(AlertWebSocket alertWebSocket);

	public List<AlertWebSocket> findReceiveAlert(String receiveid);

	public void deleteReceiveAlert(String receiveid);
	
}
