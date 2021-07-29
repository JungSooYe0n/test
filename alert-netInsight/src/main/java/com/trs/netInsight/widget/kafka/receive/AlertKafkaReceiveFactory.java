package com.trs.netInsight.widget.kafka.receive;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.AlertWebSocket;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.util.WebSocketAlertUtil;
import com.trs.netInsight.widget.kafka.constant.AlertKafkaConst;
import com.trs.netInsight.widget.kafka.entity.AlertKafkaSend;
import com.trs.netInsight.widget.notice.service.INoticeSendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class AlertKafkaReceiveFactory {
    @Autowired
    private INoticeSendService noticeSendService;

    WebSocketAlertUtil webSocketAlertUtil=new WebSocketAlertUtil();


    @KafkaListener(topics = {AlertKafkaConst.KAFKA_TOPIC_4,AlertKafkaConst.KAFKA_TOPIC_1,AlertKafkaConst.KAFKA_TOPIC_2,AlertKafkaConst.KAFKA_TOPIC_3,AlertKafkaConst.KAFKA_TOPIC_5,AlertKafkaConst.KAFKA_TOPIC_6,AlertKafkaConst.KAFKA_TOPIC_7,AlertKafkaConst.KAFKA_TOPIC_8})
    public void listen(ConsumerRecord<?, ?> record) {
        log.info("kafka接收成功");
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object data = kafkaMessage.get();
            if (data != null && StringUtils.isNotBlank(String.valueOf(data))) {
                String dataString = String.valueOf(data);
                AlertKafkaSend alertKafkaSend = JSONObject.parseObject(dataString, AlertKafkaSend.class);
                try {
                    AlertRule alertRule =alertKafkaSend.getAlertrule();
                    String autoType = alertRule.getCountBy();
                    if("md5".equals(autoType)){
                        autoType = "热度值预警";
                    }else{
                        autoType = "数据量预警";
                    }
                    log.info("Kafka发送开始--，"+autoType+"预警：" + alertRule.getId() + "，名字：" + alertRule.getTitle() );
                    Map<String, Object> sendData = alertKafkaSend.getSendData();
                    try {
                        noticeSendService.sendAlert(alertRule, sendData);
                                /*// 我让前段把接受者放到websiteid里边了 然后用户和发送方式一一对应 和手动发送方式一致
                                noticeSendService.sendAll(sendWay, TEMPLATE, alertRule.getTitle(), sendData, splitWeb[i],
                                        alertRule.getUserId(), AlertSource.AUTO);*/
                        //getMessage(alertRule,sendData);//站内自动预警调用WebSocket推送预警信息或者保存
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    log.info("Kafka发送完成**，"+autoType+"预警：" + alertRule.getId() + "，名字：" + alertRule.getTitle() );
                } catch (Exception e) {
                    log.error("kafka报错！", e);
                }
            }
        }
    }
    /*public void getMessage(AlertRule alertRule,Map sendData) throws IOException {
        String webReceiver = alertRule.getWebsiteId();//对预警信息拆解重新封装，取需要的数据
        List<Map> list= (List<Map>) sendData.get("listMap");
        List<AlertWebSocket> alertList=new ArrayList<>();
        for (int i=0;i<list.size();i++){
            AlertWebSocket alertWebSocket=new AlertWebSocket();
            alertWebSocket.setReceivemessage(list.get(i).get("title").toString());
            alertWebSocket.setReceivetime(list.get(i).get("urlTime").toString());
            alertWebSocket.setReceivefrom(list.get(i).get("source").toString());
            alertWebSocket.setReceiveurl(list.get(i).get("url").toString());
            alertList.add(alertWebSocket);
        }
        webSocketAlertUtil.sendMessageToUser(webReceiver,alertList);
    }*/
}
