package com.trs.netInsight.widget.alert.quartz;

import java.util.List;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.FullTextSearch;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.kafka.entity.KafkaMessage;
import com.trs.netInsight.support.kafka.entity.enums.KafkaMessageHeaderEnum;
import com.trs.netInsight.support.kafka.util.KafkaUtil;
import com.trs.netInsight.widget.alert.entity.AlertKafkaSend;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.AlertRuleBackups;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.service.IAlertRuleBackupsService;
import com.trs.netInsight.widget.alert.service.IAlertRuleService;

import lombok.extern.slf4j.Slf4j;

/**
 * 定频发送预警
 * 
 * @author xiaoying
 *
 */
@Service
@Slf4j
public class SendEmail implements Job {// 在实现job的类里写具体的业务逻辑

	@Autowired
	private IAlertRuleService alertRuleService;

	@Autowired
	private FullTextSearch hybase8SearchService;
	@Autowired
	private IAlertRuleBackupsService alertRuleBackupsService;

	/**
	 * 分三个库查询 传统 微博 微信
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// 这个定时类找frequencyId为3的
		// 编写具体的业务逻辑
		List<AlertRule> rules = alertRuleService.findByStatusAndAlertTypeAndFrequencyId(ScheduleStatus.OPEN,
				AlertSource.AUTO, "3");
		int i=0;
		for (AlertRule alertRule : rules) {
			try {
				log.error("开始执行预警，共有【"+rules.size()+"】个，当前第"+(++i)+"个。");
				// 在发送时间
				if (ScheduleUtil.time(alertRule)) {
					List<AlertRuleBackups> listAlertRuleBackups = alertRuleBackupsService.list(alertRule.getId(),
							alertRule.getUserId());
					if (listAlertRuleBackups != null && listAlertRuleBackups.size() > 0) {
						AlertRuleBackups alertRuleBackups = listAlertRuleBackups.get(0);
						QueryBuilder searchBuilder = alertRuleBackups.toSearchBuilder();
						QueryBuilder searchBuilderWeiBo = alertRuleBackups.toSearchBuilderWeiBo();
						QueryBuilder searchBuilderWeiXin = alertRuleBackups.toSearchBuilderWeiXin();
						QueryBuilder searchBuilderTF = alertRuleBackups.toSearchBuilderTF();
						long countChuantong = 0;
						long countWeibo = 0;
						long countWeiXin = 0;
						long countTF = 0;
						if (searchBuilder != null) {
							searchBuilder.page(0, 5);
							searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
							countChuantong = hybase8SearchService.ftsCount(searchBuilder,
									alertRuleBackups.isRepetition(), alertRuleBackups.isIrSimflag(),alertRuleBackups.isIrSimflagAll(),null);
						}
						if (searchBuilderWeiBo != null) {
							searchBuilderWeiBo.page(0, 5);
							searchBuilderWeiBo.setDatabase(Const.WEIBO);
							countWeibo = hybase8SearchService.ftsCount(searchBuilderWeiBo,
									alertRuleBackups.isRepetition(), alertRuleBackups.isIrSimflag(),alertRuleBackups.isIrSimflagAll(),null);
						}
						if (searchBuilderWeiXin != null) {
							searchBuilderWeiXin.page(0, 5);
							searchBuilderWeiXin.setDatabase(Const.WECHAT_COMMON);
							countWeiXin = hybase8SearchService.ftsCount(searchBuilderWeiXin,
									alertRuleBackups.isRepetition(), alertRuleBackups.isIrSimflag(),alertRuleBackups.isIrSimflagAll(),null);
						}
						if (searchBuilderTF != null) {
							searchBuilderTF.page(0, 5);
							searchBuilderTF.setDatabase(Const.HYBASE_OVERSEAS);
							countTF = hybase8SearchService.ftsCount(searchBuilderTF, alertRuleBackups.isRepetition(),
									alertRuleBackups.isIrSimflag(),alertRuleBackups.isIrSimflagAll(),null);
						}

						long count = countChuantong + countWeibo + countWeiXin + countTF;
						int timeInterval = alertRuleBackups.getTimeInterval();
						if (timeInterval == 1) {
							timeInterval = 5;
						}
						// 一分钟只做展示 还是五分钟发送一次
						boolean timeBoolean = alertRuleBackups.getLastExecutionTime() + (timeInterval * 60000) < System
								.currentTimeMillis();
						// 增量够 或者到时间了 查库
						if ((alertRuleBackups.getGrowth() > 0 && count > alertRuleBackups.getGrowth()) || timeBoolean) {

							AlertKafkaSend alertKafkaSend = new AlertKafkaSend(alertRuleBackups.getId(), alertRule.getId());
							KafkaMessage kafkaMessage = new KafkaMessage(KafkaMessageHeaderEnum.ALERT, alertKafkaSend);
							KafkaUtil.send(kafkaMessage);
						}
					}
				}
			} catch (Exception e) {
				log.error("预警【"+alertRule.getTitle()+"】任务报错：", e);
			}
		}
	}

}
