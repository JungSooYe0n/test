/*
 * Project: netInsight
 * 
 * File Created at 2018年1月26日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.alert.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.alert.entity.AlertSendWeChat;

/**
 * 
 * @Type WeixinBindRepository.java
 * @author 谷泽昊
 * @date 2018年1月26日 下午2:38:34
 * @version
 */
@Repository
public interface SendAlertRepository extends JpaRepository<AlertSendWeChat, String> {


}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月26日 谷泽昊 creat
 */