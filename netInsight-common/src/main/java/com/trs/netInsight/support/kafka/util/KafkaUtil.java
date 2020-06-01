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

	public static void send(Object object) {
		@SuppressWarnings("unchecked")
		KafkaTemplate<String, Object> kafkaTemplate = SpringUtil.getBean(KafkaTemplate.class);
		ListenableFuture<SendResult<String, Object>> listenableFuture = kafkaTemplate.send(KafkaConst.KAFKA_TOPIC,
				object);
		sendCallBack(listenableFuture);
	}

	/**
	 * 获取返回值
	 * @date Created at 2018年11月15日  下午3:27:22
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param listenableFuture
	 */
	private static void sendCallBack(ListenableFuture<SendResult<String, Object>> listenableFuture) {
		try {
			SendResult<String, Object> sendResult = listenableFuture.get();
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