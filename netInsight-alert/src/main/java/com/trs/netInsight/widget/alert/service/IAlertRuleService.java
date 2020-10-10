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

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @Type IAlertRuleService.java
 * @Desc 预警规则接口服务
 * @author yan.changjiang
 * @date 2017年11月20日 下午5:53:32
 * @version
 */
public interface IAlertRuleService {

	/**
	 * 手动发送预警 email 接收人 documentId 文章Id content 发送时填写的标题 userId 用户id groupName来源   trslk redis中存储表达式的key
	 * 来源 send 发送方式 xiaoying
	 */
	public Object send(String email, String documentId, String urlTime, String content, String userId, String groupName,
                       String send, String trslk)throws TRSException, TRSSearchException;

	/**
	 * 新建预警
	 * 
	 * @param alertRule
	 *            预警规则实体
	 * @return
	 * @throws TRSException
	 */
	public AlertRule addAlertRule(AlertRule alertRule);

	/**
	 * 修改预警
	 * 
	 * @param alertRule
	 *            预警规则实体
	 * @return
	 * @throws TRSException
	 */
	public AlertRule update(AlertRule alertRule, boolean choose);

	/**
	 * 查询预警信息
	 * 
	 * @param user
	 *            用户
	 * @return
	 * @throws TRSException
	 */
	public List<AlertRule> selectAll(User user);

	/**
	 * 按照预警类型进行条件检索
	 * 
	 * @param alertSource
	 *            预警类型
	 * @return
	 * @throws TRSException
	 */
	public List<AlertRule> selectType(AlertSource alertSource);

	/**
	 * 保存预警规则
	 * 
	 * @date Created at 2017年11月27日 下午6:22:42
	 * @Author 谷泽昊
	 * @param alertRule
	 * @return
	 */
	public AlertRule save(AlertRule alertRule);

	/**
	 * 删除预警规则
	 * 
	 * @date Created at 2017年11月27日 下午6:23:05
	 * @Author 谷泽昊
	 * @param ruleId
	 */
	public void delete(String ruleId) throws OperationException ;
	public void deleteByUserId(String userId);
	/**
	 * 根据id查询
	 * 
	 * @date Created at 2018年3月1日 上午10:31:07
	 * @Author 谷泽昊
	 * @param id
	 * @return
	 */
	public AlertRule findOne(String id);

	/**
	 * 分页查询
	 * 
	 * @date Created at 2018年3月1日 下午2:51:21
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Page<AlertRule> pageList(User user, int pageNo, int pageSize);

	/**
	 * 根据开关和预警类型 频率id查询
	 * 
	 * @date Created at 2018年3月6日 上午9:37:16
	 * @Author 谷泽昊
	 * @param open
	 * @param auto
	 * @param frequencyId 频率id
	 * @return
	 */
	public List<AlertRule> findByStatusAndAlertTypeAndFrequencyId(ScheduleStatus open, AlertSource auto, String frequencyId);

	/**
	 * 微信查询
	 * 
	 * @date Created at 2018年3月12日 下午6:31:58
	 * @Author 谷泽昊
	 * @param alertRule
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param keywords
	 * @return
	 */
	public Object weChatSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String keywords,String fuzzyValueScope, String keyWordIndex)
			throws Exception;

	/**
	 * 微博查询
	 * 
	 * @date Created at 2018年3月12日 下午6:32:02
	 * @Author 谷泽昊
	 * @param alertRule
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param keywords
	 * @return
	 */
	public Object statusSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String keywords, String fuzzyValueScope,String keyWordIndex,String forwarPrimary)
			throws Exception;

	/**
	 * 传统媒体查询
	 * 
	 * @date Created at 2018年3月12日 下午6:32:05
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param invitationCard
	 * @param keywords
	 * @return
	 */
	public Object documentSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort, String invitationCard, String keywords, String fuzzyValueScope,
			String keyWordIndex) throws Exception;
	
	/**
	 * Twitter And FaceBook 媒体查询
	 * @since changjiang @ 2018年4月26日
	 * @param alertRule
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param keywords
	 * @param notKeyWords
	 * @param keyWordIndex
	 * @return
	 * @throws Exception
	 * @Return : Object
	 */
	public Object documentTFSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
			String industry, String emotion, String sort,  String keywords, String fuzzyValueScope,
			String keyWordIndex) throws Exception;

	/**
	 * 全部数据源
	 * @since zhangya @ 2019年11月13日
	 * @param alertRule
	 * @param pageNo
	 * @param pageSize
	 * @param source
	 * @param time
	 * @param area
	 * @param industry
	 * @param emotion
	 * @param sort
	 * @param invitationCard
	 * @param keywords
	 * @param keyWordIndex
	 * @return
	 * @throws Exception
	 * @Return : Object
	 */
	public Object documentCommonSearch(AlertRule alertRule, int pageNo, int pageSize, String source, String time, String area,
								   String industry, String emotion, String sort, String invitationCard,String forwarPrimary, String keywords,String fuzzyValueScope,
								   String keyWordIndex,Boolean isExport) throws Exception;

	/**
	 * 发送预警。。混合列表
	 * @date Created at 2018年3月28日  上午9:45:59
	 * @Author 谷泽昊
	 * @param receivers
	 * @param documentId
	 * @param content
	 * @param userId
	 * @param groupName
	 * @param sendWay
	 * @return
	 */
	public Object sendBlend(String receivers, String[] documentId, String urltime, String content, String userId, String[] groupName,
                            String sendWay, String trslk) throws TRSException;
	/**
	 * 不关联alert_netinsight预警工程
	 * @param time 时间
	 * @param pageNo 0开始页数
	 * @param pageSize 一页几条
	 * @return
	 * @throws OperationException
	 */
	public Object listSmsLocal(String time, int pageNo, int pageSize) throws OperationException;
	/**
	 * 关联alert_netinsight预警工程
	 * @param time 时间
	 * @param pageNo 0开始页数
	 * @param pageSize 一页几条
	 * @return
	 * @throws OperationException
	 */
	public Object listSmsHttp(String time, int pageNo, int pageSize) throws OperationException;

	/**
	 * 查询某用户分组下预警个数（ 主要是添加预警规则时 判断资源数量）
	 * @param user
	 * @return
	 */
	public int getSubGroupAlertCount(User user);

	/**
	 * 主要在添加用户分组，设置用户分组用
	 * @param subGroupId
	 * @return
	 */
	public int getSubGroupAlertCountForSubGroup(String subGroupId);

	/**
	 * 迁移历史数据使用
	 * @param userId
	 * @return
	 */
	public List<AlertRule> findByUserId(String userId);


	/**
	 * 迁移历史数据用
	 * @param alertRules
	 */
	public void updateAll(List<AlertRule> alertRules);

	public void updateSimple();

	Object selectNextShowAlertRule(String id);
}
