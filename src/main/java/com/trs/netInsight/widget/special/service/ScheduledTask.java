package com.trs.netInsight.widget.special.service;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.autowork.task.AbstractTask;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.text.ParseException;

/**
 * 专题指数定时计算任务
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月3日
 */
@Component
@Configurable
@EnableScheduling
@Slf4j
public class ScheduledTask extends AbstractTask {

    @Autowired
    private ISpecialComputeService computeService;

    @Override
    protected void before() {

    }

    @Override
    protected void after() {

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.debug("专题对比开始!");
            System.out.println("专题对比开始!");
            computeService.compute();

            log.debug("专题对比结束!");
            System.out.println("专题对比结束!");
        } catch (ParseException e) {
            log.error("专题对比失败!" + e.getMessage());
        } catch (OperationException e) {
            log.error("专题对比失败!" + e.getMessage());
        }

    }
}
