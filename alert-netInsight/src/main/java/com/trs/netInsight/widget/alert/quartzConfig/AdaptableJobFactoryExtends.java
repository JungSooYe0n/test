package com.trs.netInsight.widget.alert.quartzConfig;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;

public class AdaptableJobFactoryExtends extends AdaptableJobFactory {

    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;

    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception{
        //调用父类方法
        Object createJobInstance = super.createJobInstance(bundle);
        //进行注入
        capableBeanFactory.autowireBean(createJobInstance);
        return createJobInstance;
    }

}
