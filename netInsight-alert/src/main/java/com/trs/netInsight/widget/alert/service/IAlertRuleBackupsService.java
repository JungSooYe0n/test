///*
// * Project: netInsight
// *
// * File Created at 2018年3月2日
// *
// * Copyright 2017 trs Corporation Limited.
// * All rights reserved.
// *
// * This software is the confidential and proprietary information of
// * TRS Company. ("Confidential Information").  You shall not
// * disclose such Confidential Information and shall use it only in
// * accordance with the terms of the license.
// */
//package com.trs.netInsight.widget.alert.service;
//
//import java.util.List;
//
//import com.trs.netInsight.widget.alert.entity.AlertRuleBackups;
//
///**
// * 规则备份表
// * @Type IAlertRuleBackupsService.java
// * @author 谷泽昊
// * @date 2018年3月2日 上午9:53:16
// * @version
// */
//public interface IAlertRuleBackupsService {
//
//	/**
//	 * 查询
//	 * @date Created at 2018年3月2日  上午9:59:44
//	 * @Author 谷泽昊
//	 * @param alertRuleId
//	 * @param userId
//	 * @return
//	 */
//	public List<AlertRuleBackups> list(String alertRuleId, String userId);
//
//	/**
//	 * 添加
//	 * @date Created at 2018年3月2日  上午9:57:28
//	 * @Author 谷泽昊
//	 * @param alertRuleBackups
//	 * @return
//	 */
//	public AlertRuleBackups add(AlertRuleBackups alertRuleBackups);
//
//	/**
//	 * 删除
//	 * @date Created at 2018年3月2日  上午9:58:33
//	 * @Author 谷泽昊
//	 * @param alertRuleBackups
//	 */
//	public void delete(AlertRuleBackups alertRuleBackups);
//
//	/**
//	 * 删除
//	 * @param alertRuleId
//	 */
//	public void deleteByAlertRuleId(String alertRuleId);
//
//	/**
//	 * 删除
//	 * @date Created at 2018年3月2日  上午9:58:33
//	 * @Author 谷泽昊
//	 * @param alertRuleBackups
//	 */
//	public void delete(List<AlertRuleBackups> alertRuleBackupsList);
//
//	/**
//	 * 修改
//	 * @date Created at 2018年3月6日  上午9:39:02
//	 * @Author 谷泽昊
//	 * @param alertRuleBackups
//	 */
//	public void update(AlertRuleBackups alertRuleBackups);
//
//	/**
//	 * 根据id查询单个
//	 * @date Created at 2018年11月21日  上午11:30:33
//	 * @author 北京拓尔思信息技术股份有限公司
//	 * @author 谷泽昊
//	 * @param alertRuleBackupsId
//	 * @return
//	 */
//	public AlertRuleBackups findOne(String alertRuleBackupsId);
//
//	public List<AlertRuleBackups> findSimple();
//
//}
//
//
///**
// *
// * Revision history
// * -------------------------------------------------------------------------
// *
// * Date Author Note
// * -------------------------------------------------------------------------
// * 2018年3月2日 谷泽昊 creat
// */