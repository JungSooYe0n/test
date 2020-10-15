package com.trs.netInsight.widget.alert.autoAlert;

import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.service.IAutoAlertRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
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
        try {
            //把这个项目注册的预警全部删除
            autoAlertRuleService.fuzzyDeleteAutoAlert();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        log.info("按数量预警的自动预警，删除完成");
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        try {
            //把这个项目注册的预警全部删除  - -- 项目开始前执行，防止之前有没删除的，数据乱了
            autoAlertRuleService.fuzzyDeleteAutoAlert();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        //项目启动时执行
        //需要数据中心捕获数据的自动预警为 按数据量预警
        List<AlertRule> list = alertRuleRepository.findByAlertTypeAndFrequencyId(AlertSource.AUTO,"3");
        if(list != null && list.size()>0){
            log.info("按数量预警的自动预警，开始注册，共有："+list.size());
            for (AlertRule alertRule : list) {
                try {
                    autoAlertRuleService.saveAutoAlert(alertRule);
                } catch (Exception e) {
                    log.info( e.getMessage());
                }
            }
        }
        log.info("按数量预警的自动预警，注册完成");
    }
}
