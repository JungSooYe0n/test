package com.trs.netInsight.widget.alert.quartz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.FullTextSearch;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentTF;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.kafka.service.IAlertKafkaConsumerService;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.AlertRuleBackups;
import com.trs.netInsight.widget.alert.entity.Frequency;
import com.trs.netInsight.widget.alert.entity.PastMd5;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.entity.repository.PastMd5Repository;
import com.trs.netInsight.widget.alert.service.IAlertRuleBackupsService;
import com.trs.netInsight.widget.alert.service.IAlertRuleService;

import lombok.extern.slf4j.Slf4j;

/**
 * 按照热度值发送预警
 * 
 * @author xiaoying 2018.8.27
 */
@Service
@Slf4j
public class SendMd5 implements Job {

	@Autowired
	private IAlertRuleService alertRuleService;

	@Autowired
	private PastMd5Repository md5Repository;

	@Autowired
	private IAlertRuleBackupsService alertRuleBackupsService;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private IAlertKafkaConsumerService alertKafkaConsumerService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// 获得频率id
		Frequency alertFrequency = (Frequency) context.getJobDetail().getJobDataMap().get("schedule");
		List<AlertRule> rules = alertRuleService.findByStatusAndAlertTypeAndFrequencyId(ScheduleStatus.OPEN,
				AlertSource.AUTO, alertFrequency.getId());
		for (AlertRule alertRule : rules) {
			if (ScheduleUtil.time(alertRule)) {// 在时间范围内才去分类统计发送
				List<AlertRuleBackups> listAlertRuleBackups;
				try {
					listAlertRuleBackups = alertRuleBackupsService.list(alertRule.getId(), alertRule.getUserId());
					if (listAlertRuleBackups != null && listAlertRuleBackups.size() > 0) {
						AlertRuleBackups alertRuleBackups = listAlertRuleBackups.get(0);
						List<PastMd5> backMd5List = md5Repository.findByRuleBackId(alertRuleBackups.getId());
						StringBuilder stringBuilder = new StringBuilder();
						for (PastMd5 pastMd5 : backMd5List) {
							stringBuilder.append(" OR ").append(pastMd5.getMd5());
						}
						String notMd5 = stringBuilder.toString().replaceFirst(" OR ", "");
						QueryBuilder searchBuilder = alertRuleBackups.toSearchBuilder();
						if (searchBuilder != null) {
							searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
						}
						QueryBuilder searchBuilderWeiBo = alertRuleBackups.toSearchBuilderWeiBo();
						if (searchBuilderWeiBo != null) {
							searchBuilderWeiBo.setDatabase(Const.WEIBO);
						}
						QueryBuilder searchBuilderWeiXin = alertRuleBackups.toSearchBuilderWeiXin();
						if (searchBuilderWeiXin != null) {
							searchBuilderWeiXin.setDatabase(Const.WECHAT);
						}
						QueryBuilder searchBuilderTF = alertRuleBackups.toSearchBuilderTF();
						if (searchBuilderTF != null) {
							searchBuilderTF.setDatabase(Const.HYBASE_OVERSEAS);
						}
						GroupResult categoryQuery = category(searchBuilder, notMd5, alertRuleBackups);
						GroupResult categoryQueryWeibo = category(searchBuilderWeiBo, notMd5, alertRuleBackups);
						GroupResult categoryQueryWeixin = category(searchBuilderWeiXin, notMd5, alertRuleBackups);
						GroupResult categoryQueryTF = category(searchBuilderTF, notMd5, alertRuleBackups);
						// 分类统计计算数量
						// long count = 0;
						// if(categoryQuery!=null &&
						// categoryQuery.getGroupList()!=null){
						// count += categoryQuery.getGroupList().size();
						// }
						// if(categoryQueryWeibo!=null &&
						// categoryQueryWeibo.getGroupList()!=null){
						// count += categoryQueryWeibo.getGroupList().size();
						// }
						// if(categoryQueryWeixin!=null &&
						// categoryQueryWeixin.getGroupList()!=null){
						// count += categoryQueryWeixin.getGroupList().size();
						// }
						// if(categoryQueryTF!=null &&
						// categoryQueryTF.getGroupList()!=null){
						// count += categoryQueryTF.getGroupList().size();
						// }
						// long count =
						// categoryQuery.getGroupList().size()+categoryQueryWeibo.getGroupList().size()
						// +categoryQueryWeixin.getGroupList().size()+categoryQueryTF.getGroupList().size();
						// 当前统计出来的数量大于等于设定的md5值
						// if(count>=alertRuleBackups.getMd5Num()){
						// 根据统计结果去查数据
						List<FtsDocument> list = queryHybase(FtsDocument.class, searchBuilder, categoryQuery,
								alertRuleBackups);
						List<FtsDocumentStatus> weiBolist = queryHybase(FtsDocumentStatus.class, searchBuilderWeiBo,
								categoryQueryWeibo, alertRuleBackups);
						List<FtsDocumentWeChat> weixinlist = queryHybase(FtsDocumentWeChat.class, searchBuilderWeiXin,
								categoryQueryWeixin, alertRuleBackups);
						List<FtsDocumentTF> TFlist = queryHybase(FtsDocumentTF.class, searchBuilderTF, categoryQueryTF,
								alertRuleBackups);
						List<AlertEntity> alertList = new ArrayList<>();
						// 结果集放到alertList中形成统一结果集
						if (list != null && list.size() > 0) {
							alertList.addAll(addAlert(new FtsDocument(), list, alertRuleBackups, categoryQuery));
						}
						if (weiBolist != null && weiBolist.size() > 0) {
							alertList.addAll(
									addAlert(new FtsDocumentStatus(), weiBolist, alertRuleBackups, categoryQueryWeibo));
						}
						if (weixinlist != null && weixinlist.size() > 0) {
							alertList.addAll(
									addAlert(new FtsDocumentWeChat(), weixinlist, alertRuleBackups, categoryQueryWeixin));
						}
						if (TFlist != null && TFlist.size() > 0) {
							alertList.addAll(addAlert(new FtsDocumentTF(), TFlist, alertRuleBackups, categoryQueryTF));
						}
						Collections.sort(alertList, new Comparator<AlertEntity>() {
							public int compare(AlertEntity o1, AlertEntity o2) {
								// 相似文章降序排列
								if (o1.getSim() >= o2.getSim()) {
									return -1;
								}
								// if(o1.getSim() == o2.getSim()){
								// return 0;
								// }
								return 1;
							}
						});
						if (alertList.size() > 0) {
							alertKafkaConsumerService.send(alertList, alertRuleBackups);
						}
						// 发送后更新时间
						// if(){
						//
						// }
						// }else{
						// log.info("当前md5数量不够 "+alertRuleBackups.getTitle());
						// }
					}
				} catch (Exception e) {
					log.error("规则【"+alertRule.getTitle()+"】发送失败 " ,e);
				}
				
			} else {
				log.info("不在发送时间范围 " + alertRule.getTitle());
			}

		}

	}

	/**
	 * 对builder进行新的拼接 返回结果分类统计结果
	 * 
	 * @param searchBuilder
	 * @param notMd5
	 *            该备份规则已经发过的md5 OR 隔开的字符串
	 * @param alertRuleBackups
	 *            备份规则
	 * @return
	 */
	public GroupResult category(QueryBuilder searchBuilder, String notMd5, AlertRuleBackups alertRuleBackups) {
		// 如果是专家模式 且没填写对应的表达式 则builder是null
		if (searchBuilder != null) {
			searchBuilder.page(0, 5);
			if (StringUtil.isNotEmpty(notMd5)) {
				searchBuilder.filterChildField(FtsFieldConst.FIELD_MD5TAG, notMd5, Operator.NotEqual);
			}
			GroupResult categoryQuery = new GroupResult();
			try {
				categoryQuery = hybase8SearchService.categoryQuery(searchBuilder, alertRuleBackups.isRepetition(),
						alertRuleBackups.isIrSimflag(),alertRuleBackups.isIrSimflagAll(), FtsFieldConst.FIELD_MD5TAG, null,searchBuilder.getDatabase());
			} catch (TRSSearchException e) {
				e.printStackTrace();
				log.error("md5预警报错" + alertRuleBackups.getTitle() + e.toString());
			}
			List<GroupInfo> groupList = categoryQuery.getGroupList();
			GroupResult categoryQuery2 = new GroupResult();
			for (GroupInfo info : groupList) {
				if (info.getCount() >= alertRuleBackups.getMd5Num()) {
					Map<String, Long> map = new HashMap<>();
					map.put(info.getFieldValue(), info.getCount());
					categoryQuery2.addAll(map);
				}
			}
			return categoryQuery2;
		}
		return null;

	}

	/**
	 * 根据分类统计结果去查列表
	 * 
	 * @param resultClass
	 *            返回实体类
	 * @param searchBuilder
	 * @param categoryQuery
	 *            分类统计结果
	 * @param alertRuleBackups
	 *            备份规则
	 * @return
	 */
	public <T extends IDocument> List<T> queryHybase(Class<T> resultClass, QueryBuilder searchBuilder,
			GroupResult categoryQuery, AlertRuleBackups alertRuleBackups) {
		if (searchBuilder != null) {
			List<T> list = new ArrayList<>();
			// 根据统计结果去查数据
			if (categoryQuery != null && categoryQuery.size() > 0) {
				Iterator<GroupInfo> iterator = categoryQuery.getGroupList().iterator();
				while (iterator.hasNext()) {
					GroupInfo info = iterator.next();
					QueryBuilder queryBuilder = new QueryBuilder().filterByTRSL(searchBuilder.asTRSL())
							.page(searchBuilder.getPageNo(), searchBuilder.getPageSize())
							.filterField(FtsFieldConst.FIELD_MD5TAG, info.getFieldValue(), Operator.Equal);
					List<T> ftsQuery = new ArrayList<>();
					try {
						//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-3
						ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, resultClass,
								alertRuleBackups.isRepetition(), true,alertRuleBackups.isIrSimflagAll(),null);
					} catch (TRSSearchException | TRSException e) {
						e.printStackTrace();
					}
					if (ftsQuery.size() > 0) {
						list.add(ftsQuery.get(0));
					}
				}
			}
			return list;
		}
		return null;
	}

	/**
	 * 
	 * @param resultClass
	 * @param list
	 * @param categoryQuery
	 * @param alertRuleBackups
	 */
	public <T extends IDocument> List<AlertEntity> addAlert(Object resultClass, List<T> list,
			AlertRuleBackups alertRuleBackups, GroupResult categoryQuery) {
		List<AlertEntity> alertList = new ArrayList<>();
		List<GroupInfo> groupList = new ArrayList<>();
		if (categoryQuery != null && categoryQuery.getGroupList() != null) {
			groupList = categoryQuery.getGroupList();
		}
		if (list != null && list.size() > 0) {
			if (resultClass instanceof FtsDocument) {
				List<FtsDocument> ftsList = (List<FtsDocument>) list;
				for (int i = 0; i < ftsList.size(); i++) {
					FtsDocument ftsDocument = ftsList.get(i);
					String content = ftsDocument.getContent();
					String[] imaUrls = null;
					String imaUrl = "";

					if (content != null){
						imaUrls = content.split("IMAGE&nbsp;SRC=&quot;");
						if (imaUrls.length>1){
							imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
						}
					}
					String keys = "";
					if(ftsDocument.getKeywords() != null){
						keys = ftsDocument.getKeywords().toString();
					}
					AlertEntity alertEntity = new AlertEntity(ftsDocument.getSid(), ftsDocument.getTitle(),ftsDocument.getTitle(),
							ftsDocument.getContent(), ftsDocument.getUrlName(), ftsDocument.getUrlTime(),
							ftsDocument.getSiteName(), ftsDocument.getGroupName(), alertRuleBackups.getId(),
							alertRuleBackups.getAlertType(), 0, 0, "", ftsDocument.getAppraise(), "", null,
							ftsDocument.getNreserved1(), ftsDocument.getMd5Tag(), false, null,"", imaUrl,false, false,
							(int) groupList.get(i).getCount(),"",keys,ftsDocument.getAuthors());
					alertList.add(alertEntity);
				}
			} else if (resultClass instanceof FtsDocumentStatus) {
				List<FtsDocumentStatus> ftsList = (List<FtsDocumentStatus>) list;
				for (int i = 0; i < ftsList.size(); i++) {
					FtsDocumentStatus ftsDocument = ftsList.get(i);
					String content = ftsDocument.getStatusContent();
					String[] imaUrls = null;
					String imaUrl = "";

					if (content != null){
						imaUrls = content.split("IMAGE&nbsp;SRC=&quot;");
						if (imaUrls.length>1){
							imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
						}
					}
					AlertEntity alertEntity = new AlertEntity(ftsDocument.getMid(), ftsDocument.getStatusContent(),null,
							ftsDocument.getStatusContent(), ftsDocument.getUrlName(), ftsDocument.getCreatedAt(),
							ftsDocument.getSiteName(), ftsDocument.getGroupName(), alertRuleBackups.getId(),
							alertRuleBackups.getAlertType(), 0, 0, ftsDocument.getScreenName(),
							ftsDocument.getAppraise(), "", null, "", ftsDocument.getMd5Tag(), false,
							ftsDocument.getRetweetedMid(),"", imaUrl,false, false, (int) groupList.get(i).getCount(),"",null,ftsDocument.getAuthors());
					alertList.add(alertEntity);
				}
			} else if (resultClass instanceof FtsDocumentWeChat) {
				List<FtsDocumentWeChat> ftsList = (List<FtsDocumentWeChat>) list;
				for (int i = 0; i < ftsList.size(); i++) {
					FtsDocumentWeChat ftsDocument = ftsList.get(i);
					String content = ftsDocument.getContent();
					String[] imaUrls = null;
					String imaUrl = "";

					if (content != null){
						imaUrls = content.split("IMAGE&nbsp;SRC=&quot;");
						if (imaUrls.length>1){
							imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
						}
					}
					AlertEntity alertEntity = new AlertEntity(ftsDocument.getHkey(), ftsDocument.getUrlTitle(),ftsDocument.getUrlTitle(),
							ftsDocument.getContent(), ftsDocument.getUrlName(), ftsDocument.getUrlTime(),
							ftsDocument.getAuthors(), ftsDocument.getGroupName(), alertRuleBackups.getId(),
							alertRuleBackups.getAlertType(), 0, 0, "", ftsDocument.getAppraise(), "", null, "",
							ftsDocument.getMd5Tag(), false, null,"",imaUrl, false, false, (int) groupList.get(i).getCount(),"",null,ftsDocument.getAuthors());
					alertList.add(alertEntity);
				}
			} else if (resultClass instanceof FtsDocumentTF) {
				List<FtsDocumentTF> ftsList = (List<FtsDocumentTF>) list;
				for (int i = 0; i < ftsList.size(); i++) {
					FtsDocumentTF ftsDocument = ftsList.get(i);
					String content = ftsDocument.getContent();
					String[] imaUrls = null;
					String imaUrl = "";

					if (content != null){
						imaUrls = content.split("IMAGE&nbsp;SRC=&quot;");
						if (imaUrls.length>1){
							imaUrl = imaUrls[1].substring(0,imaUrls[1].indexOf("&quot;"));
						}
					}
					AlertEntity alertEntity = new AlertEntity(ftsDocument.getMid(), ftsDocument.getStatusContent(),null,
							ftsDocument.getStatusContent(), ftsDocument.getUrlName(), ftsDocument.getCreatedAt(),
							ftsDocument.getSiteName(), ftsDocument.getGroupName(), alertRuleBackups.getId(),
							alertRuleBackups.getAlertType(), 0, 0, "", ftsDocument.getAppraise(), "", null, "",
							ftsDocument.getMd5Tag(), false, ftsDocument.getRetweetedMid(), "",imaUrl,false, false,
							(int) groupList.get(i).getCount(),"",null,ftsDocument.getAuthors());
					alertList.add(alertEntity);
				}
			}
			return alertList;
		}
		return null;
	}

}
