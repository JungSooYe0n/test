package com.trs.netInsight.widget.alert.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AutoAlertFrequency {

    //从3开始是因为原来的预警是从3开始的，这里写这个是为了防止这个alert_frequency没有数据，则自动预警不起作用
    AutoAlertFrequency_3("3","default","com.trs.netInsight.widget.alert.quartz.AlertNum","0 0/5 * * * ? "),
    AutoAlertFrequency_4("4","default","com.trs.netInsight.widget.alert.quartz.AlertMd5","0 0/30 * * * ? "),
    AutoAlertFrequency_5("5","default","com.trs.netInsight.widget.alert.quartz.AlertMd5","0 0 0/1 * * ? "),
    AutoAlertFrequency_6("6","default","com.trs.netInsight.widget.alert.quartz.AlertMd5","0 0 0/2 * * ? "),
    AutoAlertFrequency_7("7","default","com.trs.netInsight.widget.alert.quartz.AlertMd5","0 0 0/3 * * ? ");

    private String  id;

    private String userId;

    private String className;

    private String cron;

}
