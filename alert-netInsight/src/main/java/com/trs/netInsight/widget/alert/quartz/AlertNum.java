package com.trs.netInsight.widget.alert.quartz;

import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.util.AutoAlertRedisUtil;
import com.trs.netInsight.widget.alert.util.ScheduleUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 定时任务类
 * - 发送预警，按数量发送预警
 */
@Service
@Slf4j
public class AlertNum implements Job {

    @Autowired
    private AlertRuleRepository alertRuleRepository;
    @Value("${alert.auto.prefix}")
    private String alertAutoPrefix;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 这个定时类找frequencyId为3的
        // 编写具体的业务逻辑
        List<AlertRule> rules = alertRuleRepository.findByStatusAndAlertTypeAndFrequencyId(ScheduleStatus.OPEN,
                AlertSource.AUTO, "3");

        if (rules != null && rules.size() > 0) {
            for (AlertRule alertRule : rules) {
                try {
                    String key = alertAutoPrefix + alertRule.getId();
                    Long dataSize = AutoAlertRedisUtil.getSizeForList(key);
                    if (ScheduleUtil.time(alertRule)) {
                        //在发送时间内，但是需要将昨晚的没用预警清空
                        if (dataSize > 0) {
                            int timeInterval = alertRule.getTimeInterval() == 1 ? 5 : alertRule.getTimeInterval();
                            // 一分钟只做展示 还是五分钟发送一次
                            boolean timeBoolean = alertRule.getLastExecutionTime() + (timeInterval * 60000) < System
                                    .currentTimeMillis();
                            if ((alertRule.getGrowth() > 0 && dataSize > alertRule.getGrowth()) || timeBoolean) {
                                //发送预警 列表中的预警全部拿出来 ，发送20条，其他的存入已发预警

                                List<Object> dataList = new ArrayList<>();
                                for (int i = 0; i < dataSize; i++) {
                                    Object vo = AutoAlertRedisUtil.getOneDataForList(key);
                                    if (vo != null) {
                                        dataList.add(vo);
                                    }
                                }
                                if (dataList.size() > 0) {
                                    //将当前数据挨个转化为对应的数据格式，并发送


                                }
                                alertRule.setLastStartTime(DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss));
                                alertRule.setLastExecutionTime(System.currentTimeMillis());
                            }
                        }
                    } else {
                        //指当前预警是开启的，但是不在用户指定的发送时间内，这时候需要记得将数据删除一部分
                        if(dataSize > 20){
                            //在不可发送时间内，如果有数据，只保留20条，等在可执行时间时，有数据可发
                            for (int i = 20; i < dataSize; i++) {
                                Object vo = AutoAlertRedisUtil.getOneDataForList(key);
                            }
                        }
                        alertRule.setLastStartTime(DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss));
                        alertRule.setLastExecutionTime(System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    log.error("预警【" + alertRule.getTitle() + "】任务报错：", e);
                }
            }

        }
    }

}
