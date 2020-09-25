package com.trs.netInsight;

import com.trs.netInsight.widget.alert.quartzConfig.AdaptableJobFactoryExtends;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Properties;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class AlertNetinsightApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertNetinsightApplication.class, args);
    }

    /**
     * 定时调度
     * @return
     * Created By xiao.ying
     */
    @Bean
    public AdaptableJobFactoryExtends adaptableJobFactory(){
        return new AdaptableJobFactoryExtends();
    }

    /**
     * 定时调度
     * @return
     * Created By xiao.ying
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(){
        Properties properties=new Properties();
        properties.setProperty("org.quartz.threadPool.threadCount", "50");
        SchedulerFactoryBean schedulerFactoryBean=new SchedulerFactoryBean();
        schedulerFactoryBean.setQuartzProperties(properties);
        schedulerFactoryBean.setJobFactory(adaptableJobFactory());
        return schedulerFactoryBean;

    }
}
