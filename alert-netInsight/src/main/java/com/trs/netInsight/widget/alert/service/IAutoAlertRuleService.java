package com.trs.netInsight.widget.alert.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.alert.entity.AlertRule;

public interface IAutoAlertRuleService {

    void registerAutoAlert(AlertRule alertRule) throws TRSException;

    Object deleteAutoAlert(AlertRule alertRule) throws TRSException;
    Object startAutoAlert(AlertRule alertRule) throws TRSException;
    Object stopAutoAlert(AlertRule alertRule) throws TRSException;
    Object insertAutoAlert(AlertRule alertRule) throws TRSException;
    Object updateAutoAlert(AlertRule alertRule) throws TRSException;
    Object findOneAutoAlert(AlertRule alertRule) throws TRSException;
}
