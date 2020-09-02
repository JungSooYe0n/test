package com.trs.netInsight.widget.alert.service.impl;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.HttpUtil;
import com.trs.netInsight.widget.alert.constant.AlertAutoConst;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.AutoAlertRuleResult;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.service.IAutoAlertRuleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AutoAlertRuleServiceImpl implements IAutoAlertRuleService {
    @Value("${alert.auto.server}")
    private String alertAutoServer;
    @Value("${alert.auto.prefix}")
    private String alertAutoPrefix;

    public Object startAutoAlert(AlertRule alertRule) throws TRSException {
        if (alertRule == null) {
            throw new TRSException("当前预警规则为空");
        }
        if (AlertSource.ARTIFICIAL.equals(alertRule.getAlertType())) {
            throw new TRSException("当前预警规则为手动预警");
        }
        if (ScheduleStatus.CLOSE.equals(alertRule.getStatus())) {
            throw new TRSException("当前预警为关闭状态，不应该开启");
        }

        String nameKey = alertAutoPrefix + alertRule.getId();
        Map<String, String> start_alertRule_param = new HashMap<>();
        start_alertRule_param.put(AlertAutoConst.nameKey, nameKey);
        String result = HttpUtil.doPost(alertAutoServer + AlertAutoConst.start_alertRule, start_alertRule_param, "utf-8");
        return AutoAlertRuleResult.StringToObject(result);
    }

    public Object stopAutoAlert(AlertRule alertRule) throws TRSException {
        if (alertRule == null) {
            throw new TRSException("当前预警规则为空");
        }
        if (AlertSource.ARTIFICIAL.equals(alertRule.getAlertType())) {
            throw new TRSException("当前预警规则为手动预警");
        }
        if (ScheduleStatus.OPEN.equals(alertRule.getStatus())) {
            //这个是说的当前预警的状态，是在修改完之后才会做这步操作
            throw new TRSException("当前预警为开启状态，不应该关闭");
        }

        String nameKey = alertAutoPrefix + alertRule.getId();
        Map<String, String> stop_alertRule_param = new HashMap<>();
        stop_alertRule_param.put(AlertAutoConst.nameKey, nameKey);
        String result = HttpUtil.doPost(alertAutoServer + AlertAutoConst.stop_alertRule, stop_alertRule_param, "utf-8");
        return AutoAlertRuleResult.StringToObject(result);
    }

    public Object insertAutoAlert(AlertRule alertRule) throws TRSException {
        if (alertRule == null) {
            throw new TRSException("当前预警规则为空");
        }
        if (AlertSource.ARTIFICIAL.equals(alertRule.getAlertType())) {
            throw new TRSException("当前预警规则为手动预警");
        }

        String nameKey = alertAutoPrefix + alertRule.getId();
        String queryTrsl = alertRule.getAlertRuleTrsl();
        String source =alertRule.getQueryHybaseDataBase();
        Boolean stopStatus = false; //当前预警是否是被关闭的  没被关闭
        if (ScheduleStatus.CLOSE.equals(alertRule.getStatus())) {
            stopStatus = true;
        }
        Map<String, String> insertParam = new HashMap<>();
        insertParam.put(AlertAutoConst.nameKey, nameKey);
        insertParam.put(AlertAutoConst.source, source);
        insertParam.put(AlertAutoConst.queryTrsl, queryTrsl);
        insertParam.put(AlertAutoConst.stop_alertRule, stopStatus.toString());
        String result = HttpUtil.doPost(alertAutoServer + AlertAutoConst.add_alertRule, insertParam, "utf-8");
        return AutoAlertRuleResult.StringToObject(result);
    }

    public Object deleteAutoAlert(AlertRule alertRule) throws TRSException {
        if (alertRule == null) {
            throw new TRSException("当前预警规则为空");
        }

        String nameKey = alertAutoPrefix + alertRule.getId();
        Map<String, String> param = new HashMap<>();
        param.put(AlertAutoConst.nameKey, nameKey);
        String result = HttpUtil.doPost(alertAutoServer + AlertAutoConst.delete_alertRule, param, "utf-8");
        return AutoAlertRuleResult.StringToObject(result);
    }

    public Object updateAutoAlert(AlertRule alertRule) throws TRSException {
        if (alertRule == null) {
            throw new TRSException("当前预警规则为空");
        }
        if (AlertSource.ARTIFICIAL.equals(alertRule.getAlertType())) {
            throw new TRSException("当前预警规则为手动预警");
        }

        String nameKey = alertAutoPrefix + alertRule.getId();
        String queryTrsl = alertRule.getAlertRuleTrsl();
        String source =alertRule.getQueryHybaseDataBase();
        Boolean stopStatus = false; //当前预警是否是被关闭的  没被关闭
        if (ScheduleStatus.CLOSE.equals(alertRule.getStatus())) {
            stopStatus = true;
        }
        Map<String, String> param = new HashMap<>();
        param.put(AlertAutoConst.nameKey, nameKey);
        param.put(AlertAutoConst.source, source);
        param.put(AlertAutoConst.queryTrsl, queryTrsl);
        param.put(AlertAutoConst.stop_alertRule, stopStatus.toString());
        String result = HttpUtil.doPost(alertAutoServer + AlertAutoConst.update_alertRule, param, "utf-8");

        return AutoAlertRuleResult.StringToObject(result);
    }

    public Object findOneAutoAlert(AlertRule alertRule) throws TRSException {
        if (alertRule == null) {
            throw new TRSException("当前预警规则为空");
        }

        String nameKey = alertAutoPrefix + alertRule.getId();
        Map<String, String> param = new HashMap<>();
        param.put(AlertAutoConst.nameKey, nameKey);
        String result = HttpUtil.doPost(alertAutoServer + AlertAutoConst.findOne_alertRule, param, "utf-8");
        return AutoAlertRuleResult.StringToObject(result);
        //doPost
    }

    public void registerAutoAlert(AlertRule alertRule) throws TRSException {
        if (alertRule == null) {
            throw new TRSException("当前预警规则为空");
        }
        if (AlertSource.ARTIFICIAL.equals(alertRule.getAlertType())) {
            throw new TRSException("当前预警规则为手动预警");
        }
        String nameKey = alertAutoPrefix + alertRule.getId();
        Map<String, String> param = new HashMap<>();
        param.put(AlertAutoConst.nameKey, nameKey);
        String result = HttpUtil.doPost(alertAutoServer + AlertAutoConst.findOne_alertRule, param, "utf-8");

        if (result != null) {
            this.updateAutoAlert(alertRule);
        } else {
            this.insertAutoAlert(alertRule);
        }
    }
}
