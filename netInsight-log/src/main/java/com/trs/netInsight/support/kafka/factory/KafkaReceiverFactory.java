/*
 * Project: alertnetinsight
 * 
 * File Created at 2018年11月16日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.kafka.factory;

import java.util.Optional;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.support.kafka.entity.KafkaMessage;
import com.trs.netInsight.support.log.entity.SearchTimeLongLog;
import com.trs.netInsight.support.log.entity.SystemLog;
import com.trs.netInsight.support.log.repository.MysqlSystemLogRepository;
import com.trs.netInsight.support.log.repository.SearchTimeLongLogRepository;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.trs.netInsight.support.kafka.constant.KafkaConst;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaReceiverFactory {

	@Autowired
	@Qualifier("mysqlSystemLogRepository")
	private MysqlSystemLogRepository mysqlLogRepository;

	@Qualifier("searchTimeLongLogRepository")
	@Autowired
	private SearchTimeLongLogRepository searchTimeLongLogRepository;

	@KafkaListener(topics = { KafkaConst.KAFKA_TOPIC })
	public void listen(ConsumerRecord<?, ?> record) {
		log.info("kafka接收成功");
		Optional<?> kafkaMessage = Optional.ofNullable(record.value());
		if (kafkaMessage.isPresent()) {
			Object data = kafkaMessage.get();
			if (data != null) {
				String dataString = String.valueOf(data);
				JSONObject jsonObject = JSONObject.parseObject(dataString);
				String searchTime = jsonObject.getString("searchTime");
				SystemLog systemLog = null;
				SearchTimeLongLog searchTimeLongLog = null;
				if (StringUtil.isEmpty(searchTime)) {
					systemLog = JSONObject.parseObject(dataString, SystemLog.class);
				} else {
					searchTimeLongLog = JSONObject.parseObject(dataString, SearchTimeLongLog.class);
				}
				try {
					if (ObjectUtil.isNotEmpty(systemLog)) {
						mysqlLogRepository.saveAndFlush(systemLog);
					} else if (ObjectUtil.isNotEmpty(searchTimeLongLog)) {
						searchTimeLongLogRepository.saveAndFlush(searchTimeLongLog);
					}
				} catch (Exception e) {
					log.error("kafka报错！", e);
				}
			}
		}
	}
}
