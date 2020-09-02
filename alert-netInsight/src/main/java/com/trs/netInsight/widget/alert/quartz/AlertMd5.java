package com.trs.netInsight.widget.alert.quartz;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.Frequency;
import com.trs.netInsight.widget.alert.entity.PastMd5;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.entity.repository.PastMd5Repository;
import com.trs.netInsight.widget.alert.util.ScheduleUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.search.SearchException;
import java.util.*;

@Service
@Slf4j
public class AlertMd5 implements Job {

    @Autowired
    private PastMd5Repository md5Repository;
    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private FullTextSearch hybase8SearchServiceNew;
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 获得频率id
        Frequency alertFrequency = (Frequency) context.getJobDetail().getJobDataMap().get("schedule");
        List<AlertRule> rules = alertRuleRepository.findByStatusAndAlertTypeAndFrequencyId(ScheduleStatus.OPEN,
                AlertSource.AUTO, alertFrequency.getId());

        if (rules != null && rules.size() > 0) {
            for (AlertRule alertRule : rules) {
                try {
                    if (ScheduleUtil.time(alertRule)) {
                        //在发送时间内，但是需要将昨晚的没用预警清空
                        List<PastMd5> backMd5List = md5Repository.findByRuleBackId(alertRule.getId());
                        StringBuilder stringBuilder = new StringBuilder();
                        for (PastMd5 pastMd5 : backMd5List) {
                            stringBuilder.append(" OR ").append(pastMd5.getMd5());
                        }
                        String notMd5 = stringBuilder.toString().replaceFirst(" OR ", "");
                        String appendGroupName = appendTrsl(alertRule);


                        List<FtsDocumentCommonVO> alertList = new ArrayList<>();
                        if (SpecialType.SPECIAL.equals(alertRule.getSpecialType())) {
                            QueryBuilder searchBuilder = alertRule.toSearchBuilder(null);
                            if (searchBuilder != null) {

                                searchBuilder.setDatabase(Const.HYBASE_NI_INDEX);
                                searchBuilder.filterByTRSL(appendGroupName);
                            }
                            QueryBuilder searchBuilderWeiBo = alertRule.toSearchBuilderWeiBo(null);
                            if (searchBuilderWeiBo != null) {

                                searchBuilderWeiBo.setDatabase(Const.WEIBO);
                                searchBuilderWeiBo.filterByTRSL(appendGroupName);
                            }
                            QueryBuilder searchBuilderWeiXin = alertRule.toSearchBuilderWeiXin(null);
                            if (searchBuilderWeiXin != null) {

                                searchBuilderWeiXin.setDatabase(Const.WECHAT);
                                searchBuilderWeiXin.filterByTRSL(appendGroupName);
                            }
                            QueryBuilder searchBuilderTF = alertRule.toSearchBuilder(null);
                            if (searchBuilderTF != null) {
                                searchBuilderTF.setDatabase(Const.HYBASE_OVERSEAS);
                                searchBuilderTF.filterByTRSL(appendGroupName);
                            }
                            GroupResult categoryQuery = category(searchBuilder, notMd5, alertRule);
                            GroupResult categoryQueryWeibo = category(searchBuilderWeiBo, notMd5, alertRule);
                            GroupResult categoryQueryWeixin = category(searchBuilderWeiXin, notMd5, alertRule);
                            GroupResult categoryQueryTF = category(searchBuilderTF, notMd5, alertRule);

                            queryHybase(searchBuilder, categoryQuery, new String[]{Const.HYBASE_NI_INDEX}, alertList);
                            queryHybase(searchBuilderWeiBo, categoryQueryWeibo, new String[]{Const.WEIBO}, alertList);
                            queryHybase(searchBuilderWeiXin, categoryQueryWeixin, new String[]{Const.WECHAT_COMMON}, alertList);
                            queryHybase(searchBuilderTF, categoryQueryTF, new String[]{Const.HYBASE_OVERSEAS}, alertList);


                        } else {
                            QueryCommonBuilder queryCommonBuilder = alertRule.toSearchBuilderCommon(null);
                            if (queryCommonBuilder != null) {
                                queryCommonBuilder.filterByTRSL(appendGroupName);
                            }
                            QueryBuilder queryBuilder = new QueryBuilder().filterByTRSL(queryCommonBuilder.asTRSL())
                                    .page(queryCommonBuilder.getPageNo(),queryCommonBuilder.getPageSize());
                            queryBuilder.setDatabase(Const.MIX_DATABASE);
                            GroupResult categoryQuery = category(queryBuilder, notMd5, alertRule);
                             queryHybase(queryBuilder, categoryQuery,Const.MIX_DATABASE.split(";"),alertList);

                        }
                        Collections.sort(alertList, new Comparator<FtsDocumentCommonVO>() {
                            public int compare(FtsDocumentCommonVO o1, FtsDocumentCommonVO o2) {
                                // 相似文章降序排列
                                if (o1.getSim() >= o2.getSim()) {
                                    return -1;
                                }
                                return 1;
                            }
                        });
                        if (alertList.size() > 0) {
                            //String result = SendUtil.send(alertList, alertRule);
                            //alertKafkaConsumerService.send(alertList, alertRule);
                        }
                    }
                } catch (Exception e) {
                    log.error("预警【" + alertRule.getTitle() + "】任务报错：", e);
                }
            }

        }
    }

    /**
     * 对builder进行新的拼接 返回结果分类统计结果
     *
     * @param searchBuilder
     * @param notMd5
     *            该备份规则已经发过的md5 OR 隔开的字符串
     * @param alertRule
     *            备份规则
     * @return
     */
    public GroupResult category(QueryBuilder searchBuilder, String notMd5, AlertRule alertRule) {
        // 如果是专家模式 且没填写对应的表达式 则builder是null
        if (searchBuilder != null) {
            searchBuilder.page(0, 5);
            if (StringUtil.isNotEmpty(notMd5)) {
                searchBuilder.filterChildField(FtsFieldConst.FIELD_MD5TAG, notMd5, Operator.NotEqual);
            }
            GroupResult categoryQuery = new GroupResult();
            try {
                categoryQuery = hybase8SearchServiceNew.categoryQuery(searchBuilder, false,
                        true, false,FtsFieldConst.FIELD_MD5TAG, searchBuilder.getDatabase());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("md5预警报错" + alertRule.getTitle() + e.toString());
            }
            List<GroupInfo> groupList = categoryQuery.getGroupList();
            GroupResult categoryQuery2 = new GroupResult();
            for (GroupInfo info : groupList) {
                if (info.getCount() >= alertRule.getMd5Num()) {
                    Map<String, Long> map = new HashMap<>();
                    map.put(info.getFieldValue(), info.getCount());
                    categoryQuery2.addAll(map);
                }
            }
            return categoryQuery2;
        }
        return null;

    }

    public List<FtsDocumentCommonVO> queryHybase(QueryBuilder searchBuilder,
                                                     GroupResult categoryQuery,String[] db,List<FtsDocumentCommonVO> result) {
        if (searchBuilder != null) {
            // 根据统计结果去查数据
            if (categoryQuery != null && categoryQuery.size() > 0) {
                Iterator<GroupInfo> iterator = categoryQuery.getGroupList().iterator();
                while (iterator.hasNext()) {
                    GroupInfo info = iterator.next();
                    QueryCommonBuilder queryBuilder = new QueryCommonBuilder().filterByTRSL(searchBuilder.asTRSL())
                            .page(searchBuilder.getPageNo(), searchBuilder.getPageSize())
                            .filterField(FtsFieldConst.FIELD_MD5TAG, info.getFieldValue(), Operator.Equal);
                    queryBuilder.setDatabase(db);
                    PagedList<FtsDocumentCommonVO> pagedList = null ;
                    try {
                         pagedList = hybase8SearchServiceNew.pageListCommon(queryBuilder, false, false,
                                false, "alert");
                    } catch (TRSException e) {
                        e.printStackTrace();
                    }
                    if(pagedList !=null && pagedList.getPageItems() != null && pagedList.getPageItems().size()>0){
                        result.add(pagedList.getPageItems().get(0));
                    }
                }
            }
            return result;
        }
        return null;
    }




    private String appendTrsl(AlertRule alertRule){
        String trsl = "";
        String userid =  alertRule.getUserId();
        User user = userRepository.findOne(userid);
        if (StringUtils.isNotBlank(user.getOrganizationId())) {
            Organization organization = organizationRepository.findOne(user.getOrganizationId());
            if (organization != null) {
                // 数据来源来源
                String dataSources = organization.getDataSources();
                // 新闻,论坛,博客,微博,微信,客户端,电子报,Twitter
                if (StringUtils.isNotBlank(dataSources) && !StringUtils.equals(dataSources, "ALL")) {
                    String[] dataSourcesArr = dataSources.split(",");
                    StringBuffer buffer = new StringBuffer();
                    if (dataSourcesArr != null && dataSourcesArr.length > 0) {
                        buffer.append(FtsFieldConst.FIELD_GROUPNAME).append(":(");
                        int beginLength = buffer.length();
                        for (String dataSource : dataSourcesArr) {
                            buffer.append(Const.SOURCE_GROUPNAME_CONTRAST.get(dataSource)).append(" OR ");
                        }
                        int endLength = buffer.length();
                        // 去掉最后的OR
                        if (endLength >= beginLength + 4) {
                            buffer.delete(endLength - 4, endLength);
                        }
                        buffer.append(")");
                    }
                    trsl = buffer.toString();
                }
            }
        }

        return trsl;
    }

}
