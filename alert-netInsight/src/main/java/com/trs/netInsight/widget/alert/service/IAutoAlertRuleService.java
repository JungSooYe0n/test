package com.trs.netInsight.widget.alert.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.AutoAlertRuleResult;

public interface IAutoAlertRuleService {

    //String registerAutoAlert(AlertRule alertRule) throws TRSException;
    AutoAlertRuleResult deleteAutoAlert(AlertRule alertRule) throws TRSException;
    AutoAlertRuleResult fuzzyDeleteAutoAlert() throws TRSException;
    AutoAlertRuleResult startAutoAlert(AlertRule alertRule) throws TRSException;
    AutoAlertRuleResult stopAutoAlert(AlertRule alertRule) throws TRSException;
    AutoAlertRuleResult saveAutoAlert(AlertRule alertRule) throws TRSException;
    //AutoAlertRuleResult updateAutoAlert(AlertRule alertRule) throws TRSException;
    AutoAlertRuleResult findOneAutoAlert(AlertRule alertRule) throws TRSException;
    AutoAlertRuleResult fuzzyFindAutoAlert() throws TRSException;
}
