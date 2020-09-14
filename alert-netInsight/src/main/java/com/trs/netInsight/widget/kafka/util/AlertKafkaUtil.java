package com.trs.netInsight.widget.kafka.util;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.support.kafka.constant.KafkaConst;
import com.trs.netInsight.support.kafka.entity.KafkaMessage;
import com.trs.netInsight.util.SpringUtil;
import com.trs.netInsight.widget.kafka.constant.AlertKafkaConst;
import com.trs.netInsight.widget.kafka.entity.AlertKafkaSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Random;
@Slf4j
public class AlertKafkaUtil {

    public static void send(AlertKafkaSend message, boolean loop) {
        String jsonData = JSONObject.toJSONString(message);
        KafkaTemplate<String, String> kafkaTemplate = SpringUtil.getBean(KafkaTemplate.class);
        String topic = KafkaConst.KAFKA_TOPIC;
        if (loop){
            // 随机获取topic
            Random random = new Random();
            int index = random.nextInt(AlertKafkaConst.Topics.size());
            topic = AlertKafkaConst.Topics.get(index);
        }
        ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send(topic,
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
