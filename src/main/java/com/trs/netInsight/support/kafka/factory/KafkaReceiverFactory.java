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

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.support.kafka.constant.KafkaConst;
import com.trs.netInsight.support.kafka.entity.KafkaMessage;
import com.trs.netInsight.support.kafka.entity.enums.KafkaMessageHeaderEnum;
import com.trs.netInsight.support.kafka.service.IAlertKafkaConsumerService;
import com.trs.netInsight.widget.alert.entity.AlertKafkaSend;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Type KafkaReceiverFactory.java
 * @author 谷泽昊
 * @date 2018年11月16日 上午11:04:04
 * @version
 */
@Component
@Slf4j
public class KafkaReceiverFactory {

	@Autowired
	private IAlertKafkaConsumerService alertKafkaConsumerService;
	@Value("${http.client}")
	private boolean httpClient;

	@KafkaListener(topics = { KafkaConst.KAFKA_TOPIC })
	public void listen(ConsumerRecord<?, ?> record) {
		System.err.println("kafka接收成功");
		Optional<?> kafkaMessage = Optional.ofNullable(record.value());
		if (kafkaMessage.isPresent()) {
			Object data = kafkaMessage.get();
			if (data != null && StringUtils.isNotBlank(String.valueOf(data))) {
				try {
					String dataString = String.valueOf(data);
					KafkaMessage message = JSONObject.parseObject(dataString, KafkaMessage.class);
					KafkaMessageHeaderEnum kafkaMessageHeaderEnum = message.getMessageHeaderEnum();
					if (kafkaMessageHeaderEnum != null) {
						// 每个case里面最好不要写逻辑，直接写实现类
						switch (kafkaMessageHeaderEnum) {
						case ALERT:
							if(!httpClient){
								Object messageData = message.getData();
								if (messageData != null) {
									AlertKafkaSend alertKafkaSend = JSONObject.parseObject(String.valueOf(messageData),
											AlertKafkaSend.class);
									System.err.println(alertKafkaSend);
									alertKafkaConsumerService.send(alertKafkaSend);
								}
							}
							break;

						default:
							break;
						}
					}
				} catch (Exception e) {
					log.error("kafka报错！", e);
				}
			}
		}
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年11月16日 谷泽昊 creat
 */