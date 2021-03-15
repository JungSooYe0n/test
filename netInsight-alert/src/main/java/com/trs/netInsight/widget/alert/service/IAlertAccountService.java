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

import java.util.List;

import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Page;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;

/**
 * @Type IAlertAccountService.java
 * @Desc 预警账号相关管理接口
 * @author yan.changjiang
 * @date 2017年11月20日 下午4:36:41
 */
public interface IAlertAccountService {


	/**
	 * 添加预警账号
	 * 
	 * @param AlertAccount
	 * @return
	 * @throws TRSException
	 */
	public AlertAccount add(AlertAccount alertAccount);

	/**
	 * 根据id删除
	 * 
	 * @param countID
	 *            预警账号id
	 * @return
	 * @throws TRSException
	 */
	public void delete(String countID);

	/**
	 * 修改预警账号
	 * 
	 * @param id
	 *            预警账号id
	 * @param name
	 *            预警名称
	 * @param type
	 *            预警发送类型
	 * @param count
	 *            预警账号
	 * @return
	 * @throws TRSException
	 */
	public AlertAccount update(String id, String name, SendWay type, String account);
	
	
	
	/**
	 * 删除一个实体
	 * @param AlertAccount
	 */
	public void delete(AlertAccount alertAccount);
	
	/**
	 * 删除一个list
	 * @param list
	 */
	public void delete(List<AlertAccount> list);

	/**
	 * 根据账号，用户id和类型查询
	 * @date Created at 2018年3月1日  上午11:38:47
	 * @Author 谷泽昊
	 * @param fromUserName
	 * @param id
	 * @param weChat
	 * @return
	 */
	public AlertAccount findByAccountAndUserIdAndType(String fromUserName, String id, SendWay weChat);
	public AlertAccount findByAccountAndSubGroupIdAndType(String fromUserName, String id, SendWay weChat);
	
	/**
	 * 根据用户名，用户id和类型查询
	 * @date Created at 2018年3月1日  上午11:38:47
	 * @Author 谷泽昊
	 * @param fromUserName
	 * @param id
	 * @param weChat
	 * @return
	 */
	public AlertAccount findByUserAccountAndUserIdAndType(String fromUserName, String id, SendWay weChat);

	/**
	 * 根据账号查询
	 * @date Created at 2018年3月1日  上午11:43:04
	 * @Author 谷泽昊
	 * @param fromUserName
	 * @return
	 */
	public List<AlertAccount> findByAccount(String fromUserName);

	/**
	 * 根据预警类型和用户id查询
	 * @date Created at 2018年3月1日  上午11:43:55
	 * @Author 谷泽昊
	 * @param id
	 * @param weChat
	 * @return
	 */
	public List<AlertAccount> findByUserIdAndType(String id, SendWay weChat);

	/**
	 * 根据预警账号和用户id查询
	 * @date Created at 2018年3月1日  上午11:43:55
	 * @Author 谷泽昊
	 * @param id
	 * @param account
	 * @return
	 */
	public List<AlertAccount> findByUserIdAndAccount(String id, String account);

	/**
	 *  通过用户角色 决定 通过用户id 或者 用户分组id 和 发送类型 查询
	 * @param user
	 * @param type
	 * @return
	 */
	public List<AlertAccount> findByUserAndType(User user, SendWay type);
	/**
	 * 分页，分类型查询
	 * @date Created at 2018年3月1日  下午1:53:58
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @param type
	 * @param account
	 * @return
	 */
	public Page<AlertAccount> pageList(int pageNo, int pageSize, SendWay type, String account);

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
	 * @param userName
	 * @param weChat
	 * @return
	 */
	public List<AlertAccount> findByAccountAndType(String account, SendWay send);

	/**
	 * 查询该用户下所拥有的预警账号
	 * @param userId
	 * @return
	 */
	public List<AlertAccount> findByUserId(String userId);

	/**
	 * 查询某用户分组下预警账号个数（ 主要是添加预警规则时 判断资源数量）
	 * @param user
	 * @return
	 */
	public int getSubGroupAlertAccountCount(User user, SendWay type);

	/**
	 * 主要在添加用户分组，设置用户分组用
	 * @param subGroupId
	 * @return
	 */
	public int getSubGroupAlertAccountCountForSubGroup(String subGroupId, SendWay type);

	public void updateAll(List<AlertAccount> alertAccounts);
}
