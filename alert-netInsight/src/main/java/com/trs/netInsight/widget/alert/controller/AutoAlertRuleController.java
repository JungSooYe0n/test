package com.trs.netInsight.widget.alert.controller;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.service.IAutoAlertRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autoAlert")
@Api(description = "自动预警接口")
public class AutoAlertRuleController {
    @Autowired
    private IAutoAlertRuleService autoAlertRuleService;
    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @ApiOperation("注册自动预警，可能存在情况为 当前预警已经存在，则为修改，不存在则新增")
    @FormatResult
    @RequestMapping(value = "/saveAutoAlert", method = RequestMethod.GET)
    public Object saveAutoAlert(@RequestParam("id")String id) throws OperationException {

        try {
            AlertRule alertRule = alertRuleRepository.findOne(id);
            autoAlertRuleService.registerAutoAlert(alertRule);
            //4028b4e572557b0d017255adfc700009
            //4028b4e5724ffafb017250059c070000
            return "success";
        } catch (Exception e) {
            throw new OperationException("自动预警注册失败,message:" + e, e);
        }

    }

    @ApiOperation("删除对应自动预警信息")
    @FormatResult
    @RequestMapping(value = "/deleteAutoAlertRule", method = RequestMethod.GET)
    public Object deleteAutoAlertRule(@RequestParam("id")String id) throws OperationException {

        try {

            AlertRule alertRule = alertRuleRepository.findOne(id);
            Object result = autoAlertRuleService.deleteAutoAlert(alertRule);
            return result;
        } catch (Exception e) {
            throw new OperationException("自动预警信息删除失败,message:" + e, e);
        }
    }

    @ApiOperation("修改当前自动预警的状态：开启或者关闭")
    @FormatResult
    @RequestMapping(value = "/updateAutoAlertRuleStatus", method = RequestMethod.GET)
    public Object updateAutoAlertRuleStatus(@RequestParam("id")String id) throws OperationException {

        try {
            AlertRule alertRule = alertRuleRepository.findOne(id);
            if(ScheduleStatus.CLOSE.equals(alertRule.getStatus())){
                autoAlertRuleService.stopAutoAlert(alertRule);
            }else if(ScheduleStatus.OPEN.equals(alertRule.getStatus())){
                autoAlertRuleService.startAutoAlert(alertRule);
            }

            return "success";
        } catch (Exception e) {
            throw new OperationException("自动预警修改状态失败,message:" + e, e);
        }
    }


}
