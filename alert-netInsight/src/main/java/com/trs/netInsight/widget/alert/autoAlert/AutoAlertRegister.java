package com.trs.netInsight.widget.alert.autoAlert;

import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.Frequency;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.service.IAutoAlertRuleService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Iterator;
import java.util.List;

@WebListener //启动类 监听
@Slf4j
@Component
public class AutoAlertRegister implements ServletContextListener {

    @Autowired
    private AlertRuleRepository alertRuleRepository;
    @Autowired
    private IAutoAlertRuleService autoAlertRuleService;


    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        //项目关闭时执行
        List<AlertRule> list = alertRuleRepository.findByAlertType(AlertSource.AUTO);

        for (AlertRule alertRule : list) {
            try {
                //autoAlertRuleService.deleteAutoAlert(alertRule);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        //项目启动时执行
        List<AlertRule> list = alertRuleRepository.findByAlertType(AlertSource.AUTO);

        for (AlertRule alertRule : list) {
            try {
                //autoAlertRuleService.registerAutoAlert(alertRule);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
