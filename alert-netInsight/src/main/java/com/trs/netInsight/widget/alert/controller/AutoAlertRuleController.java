package com.trs.netInsight.widget.alert.controller;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.AutoAlertRuleResult;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.service.IAutoAlertRuleService;
import com.trs.netInsight.widget.alert.util.AutoAlertRedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autoAlert")
@Api(description = "自动预警接口 - 按数量预警的自动预警")
public class AutoAlertRuleController {
    @Autowired
    private IAutoAlertRuleService autoAlertRuleService;
    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @ApiOperation("查找一个注册的自动预警")
    @FormatResult
    @RequestMapping(value = "/findOneAutoAlert", method = RequestMethod.POST)
    public Object findOneAutoAlert(@RequestParam("id") String id) throws OperationException {

        try {
            AlertRule alertRule = alertRuleRepository.findOne(id);
            if (alertRule == null) {
                throw new TRSException(CodeUtils.FAIL, "当前id对应的预警信息不存在。");
            }
            AutoAlertRuleResult alertRuleResult = autoAlertRuleService.findOneAutoAlert(alertRule);
            return alertRuleResult;
        } catch (Exception e) {
            throw new OperationException("自动预警查找失败,message:" + e, e);
        }

    }

    @ApiOperation("注册自动预警，可能存在情况为 当前预警已经存在，则为修改，不存在则新增")
    @FormatResult
    @RequestMapping(value = "/saveAutoAlert", method = RequestMethod.POST)
    public Object saveAutoAlert(@RequestParam("id") String id) throws OperationException {

        try { //4028b4e5724ffafb017250059c070000
            AlertRule alertRule = alertRuleRepository.findOne(id);
            if (alertRule == null) {
                throw new TRSException(CodeUtils.FAIL, "当前id对应的预警信息不存在。");
            }
            return autoAlertRuleService.saveAutoAlert(alertRule);
        } catch (Exception e) {
            throw new OperationException("自动预警注册失败,message:" + e, e);
        }
    }

    @ApiOperation("删除对应自动预警信息")
    @FormatResult
    @RequestMapping(value = "/deleteAutoAlertRule", method = RequestMethod.POST)
    public Object deleteAutoAlertRule(@RequestParam("id") String id) throws OperationException {

        try {
            AlertRule alertRule = alertRuleRepository.findOne(id);
            if (alertRule == null) {
                alertRule = new AlertRule();
                alertRule.setId(id);
            }
            AutoAlertRuleResult alertRuleResult = autoAlertRuleService.findOneAutoAlert(alertRule);
            if (alertRuleResult != null
                    && alertRuleResult.getCode() == 200 && alertRuleResult.getTotal() > 0) {
                AutoAlertRuleResult deleteResult = autoAlertRuleService.deleteAutoAlert(alertRule);
                return deleteResult;
            } else {
                return "删除预警失败，当前预警没有注册。";
            }
        } catch (Exception e) {
            throw new OperationException("自动预警信息删除失败,message:" + e, e);
        }
    }

    @ApiOperation("修改当前自动预警的状态：开启或者关闭")
    @FormatResult
    @RequestMapping(value = "/updateAutoAlertRuleStatus", method = RequestMethod.POST)
    public Object updateAutoAlertRuleStatus(@RequestParam("id") String id) throws OperationException {

        try {
            AlertRule alertRule = alertRuleRepository.findOne(id);
            if (alertRule == null) {
                throw new TRSException(CodeUtils.FAIL, "当前id对应的预警信息不存在。");
            }
            AutoAlertRuleResult alertRuleResult = autoAlertRuleService.findOneAutoAlert(alertRule);
            if (alertRuleResult != null
                    && alertRuleResult.getCode() == 200 && alertRuleResult.getTotal() > 0) {
                AutoAlertRuleResult result = null;
                if (ScheduleStatus.CLOSE.equals(alertRule.getStatus())) {
                    result = autoAlertRuleService.stopAutoAlert(alertRule);
                } else if (ScheduleStatus.OPEN.equals(alertRule.getStatus())) {
                    result = autoAlertRuleService.startAutoAlert(alertRule);
                }
                return result;
            } else {
                return autoAlertRuleService.saveAutoAlert(alertRule);
            }
        } catch (Exception e) {
            throw new OperationException("自动预警修改状态失败,message:" + e, e);
        }
    }

    @ApiOperation("")
    @FormatResult
    @RequestMapping(value = "/testRedisData", method = RequestMethod.POST)
    public Object testRedisData(@RequestParam("hashName") String hashName,
                                @RequestParam("hashKey") String hashKey) throws OperationException {
        try {
            Object object = AutoAlertRedisUtil.getOneDataForHash(hashName,hashKey);
            return object;
        } catch (Exception e) {
            throw new OperationException("自动预警查找失败,message:" + e, e);
        }

    }

}
