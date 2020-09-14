package com.trs.netInsight.widget.kafka.constant;

import java.util.ArrayList;
import java.util.List;

public class AlertKafkaConst {
    /**
     * kafka topic
     */
    public static final String KAFKA_TOPIC ="ALERT_NETINSIGHT_DEFAULT";

    public static final String KAFKA_TOPIC_1 ="ALERT_NETINSIGHT_ONE";
    public static final String KAFKA_TOPIC_2 ="ALERT_NETINSIGHT_TWO";
    public static final String KAFKA_TOPIC_3 ="ALERT_NETINSIGHT_THREE";
    public static final String KAFKA_TOPIC_4 ="ALERT_NETINSIGHT_FOUR";

    public static final List<String> Topics = new ArrayList<String>(){
        {
            add(KAFKA_TOPIC_1);
            add(KAFKA_TOPIC_2);
            add(KAFKA_TOPIC_3);
            add(KAFKA_TOPIC_4);
        }
    };
}
