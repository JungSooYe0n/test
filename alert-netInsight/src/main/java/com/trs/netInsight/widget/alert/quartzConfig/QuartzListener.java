package com.trs.netInsight.widget.alert.quartzConfig;

import com.fasterxml.jackson.databind.util.ArrayIterator;
import com.trs.netInsight.widget.alert.entity.Frequency;
import com.trs.netInsight.widget.alert.entity.enums.AutoAlertFrequency;
import com.trs.netInsight.widget.alert.entity.repository.FrequencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@WebListener //启动类 监听
@Slf4j
@Component
public class QuartzListener implements ServletContextListener {

    @Autowired
    private FrequencyRepository frequencyRepository;

    @Autowired
    private JobFactory jobFactory;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        List<Frequency> findAll = frequencyRepository.findAll();
        if(findAll==null || findAll.size() ==0){
            log.info("频率表没数据");
            findAll = new ArrayList<>();
            try {
                AutoAlertFrequency[] autoAlertFrequencies = AutoAlertFrequency.values();
                for (AutoAlertFrequency autoAlertFrequency : autoAlertFrequencies) {
                    Frequency frequency = new Frequency(autoAlertFrequency.getId(), autoAlertFrequency.getUserId(),
                            autoAlertFrequency.getClassName(), autoAlertFrequency.getCron());
                    findAll.add(frequency);
                }
                frequencyRepository.save(findAll);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        Iterator<Frequency> iterator = findAll.iterator();
        while(iterator.hasNext()){
            Frequency frequency=iterator.next();
            try {
                jobFactory.register(frequency);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
    }

}