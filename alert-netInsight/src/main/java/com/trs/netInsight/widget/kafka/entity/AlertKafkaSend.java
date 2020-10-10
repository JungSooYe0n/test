package com.trs.netInsight.widget.kafka.entity;

import com.trs.netInsight.widget.alert.entity.AlertRule;
import javafx.scene.control.Alert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AlertKafkaSend {

    private AlertRule alertrule;
    private Map<String, Object> sendData;

}
