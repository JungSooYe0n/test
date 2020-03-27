/*
 * Project: alertnetinsight
 * 
 * File Created at 2018年11月15日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.kafka.util;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.support.kafka.constant.KafkaConst;
import com.trs.netInsight.support.kafka.entity.KafkaMessage;
import com.trs.netInsight.util.SpringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * kafka工具类
 * 
 * @Type KafkaUtil.java
 * @author 谷泽昊
 * @date 2018年11月15日 下午3:13:26
 * @version
 */
@Slf4j
public class KafkaUtil {

	/**
	 * kafka发送
	 * <p>
	 * 采用 KafkaMessage 结构体定义消息有个好处：可以复用某个topic。如果kafka集群是公用的，那么随着集群里添加的微服务增多，
	 * 如果每个业务消息都生成一个topic，
	 * 会对kafka集群的消息处理产生很重的负担，因为每个topic的每个分区都是一个文件，文件数增多，消息处理的I/O效率会大幅下降。
	 * 所以如果每个微服务可以将涉及到的业务消息共用一个topic，会提高资源利用率，同时在众多消息中追溯消息的来源也更加简单。 所以设计了
	 * KafkaMessage 数据结构，可以复用topic
	 * </p>
	 * 
	 * @date Created at 2018年11月15日 下午3:21:08
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param topic
	 * @param message
	 */
	public static void send(KafkaMessage message) {
		String jsonData = JSONObject.toJSONString(message);
		@SuppressWarnings("unchecked")
		KafkaTemplate<String, String> kafkaTemplate = SpringUtil.getBean(KafkaTemplate.class);
		ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send(KafkaConst.KAFKA_TOPIC,
				jsonData);
		sendCallBack(listenableFuture);
	}

	/**
	 * 获取返回值
	 * @date Created at 2018年11月15日  下午3:27:22
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param listenableFuture
	 */
	private static void sendCallBack(ListenableFuture<SendResult<String, String>> listenableFuture) {
		try {
			SendResult<String, String> sendResult = listenableFuture.get();
			listenableFuture.addCallback(SuccessCallback -> {
				log.info("kafka Producer发送消息成功！topic=" + sendResult.getRecordMetadata().topic() + ",partition"
						+ sendResult.getRecordMetadata().partition() + ",offset="
						+ sendResult.getRecordMetadata().offset());
			}, FailureCallback -> log.error(
					"kafka Producer发送消息失败！sendResult=" + JSONObject.toJSONString(sendResult.getProducerRecord())));
		} catch (Exception e) {
			log.error("获取producer返回值失败", e);
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
 * 2018年11月15日 谷泽昊 creat
 */