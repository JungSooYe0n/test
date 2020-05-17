package com.trs.netInsight.widget.alert.quartz;

import java.util.Iterator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.trs.netInsight.widget.alert.entity.Frequency;
import com.trs.netInsight.widget.alert.entity.repository.FrequencyRepository;

import lombok.extern.slf4j.Slf4j;

@WebListener //启动类 监听
@Slf4j
@Component
public class Start implements ServletContextListener{
	
	@Autowired
	private FrequencyRepository frequencyRepository;

	@Autowired
	private JobFactory jobFactory;
	
	@Value("${http.client}")
	private boolean httpClient;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		if(!httpClient){
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

}
