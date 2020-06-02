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

import com.trs.netInsight.support.log.entity.SystemLog;
import com.trs.netInsight.support.log.repository.MysqlSystemLogRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.trs.netInsight.support.kafka.constant.KafkaConst;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaReceiverFactory {

	@Autowired
	private MysqlSystemLogRepository mysqlLogRepository;
	@KafkaListener(topics = { KafkaConst.KAFKA_TOPIC })
	public void listen(ConsumerRecord<?, ?> record) {
		log.info("kafka接收成功");
		Optional<?> kafkaMessage = Optional.ofNullable(record.value());
		if (kafkaMessage.isPresent()) {
			Object data = kafkaMessage.get();
			if (data != null) {
				try {
					SystemLog systemLog = (SystemLog)data;
					mysqlLogRepository.saveAndFlush(systemLog);
				} catch (Exception e) {
					log.error("kafka报错！", e);
				}
			}
		}
	}
}
