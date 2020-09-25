package com.trs.netInsight.widget.alert.quartzConfig;


import com.trs.netInsight.widget.alert.entity.Frequency;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobFactory {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void register(Frequency frequency) throws ClassNotFoundException, SchedulerException {
        //Scheduler初始化
        Scheduler scheduler=schedulerFactoryBean.getScheduler();
        //JobDataMap实现数据传输
        JobDataMap jobDataMap=new JobDataMap();
        //为防止key重复
        //jobDataMap.put(alertRule.getRuleId(), alertRule);
        //每次调用都会new一个  不会重复
        jobDataMap.put("schedule", frequency);
        //反射  得到任务 把类名存到这个字段里边  就可以获取到这个类  这个类是调用消息工程发送预警的

        log.info(frequency.getClassName());
        // task 是要执行定时任务的类
        Class task=Class.forName(frequency.getClassName());//通过类名获取这个类
        //通过过JobDetail封装SimpleJob，同时指定Job在Scheduler中所属组及名称
        //JobDetail通过build模式创建
        JobDetail newJob = JobBuilder.newJob(task)
                .withIdentity(frequency.getId(), frequency.getUserId())//定义唯一标识 name group  必须存在 .withIdentity("name", "group")
                .usingJobData(jobDataMap)//这个不写也行
                .build();
        //获得cron表达式
        CronScheduleBuilder cronScheduleBuilder=CronScheduleBuilder.cronSchedule(frequency.getCron());
        //cron特定触发器  触发器用来告诉调度程序作业什么时候出发
        //也是build模式创建
        CronTrigger trigger=TriggerBuilder.newTrigger()
                .withIdentity(frequency.getId(), frequency.getUserId())
                .withSchedule(cronScheduleBuilder).build();
        scheduler.scheduleJob(newJob, trigger);
    }

}
