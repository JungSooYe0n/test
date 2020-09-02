package com.trs.netInsight.widget.alert.quartzConfig;

import com.trs.netInsight.widget.alert.entity.Frequency;
import com.trs.netInsight.widget.alert.entity.repository.FrequencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Iterator;


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
        Iterable<Frequency> findAll = frequencyRepository.findAll();
        if(findAll==null){
            log.info("频率表没数据");
            return;
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