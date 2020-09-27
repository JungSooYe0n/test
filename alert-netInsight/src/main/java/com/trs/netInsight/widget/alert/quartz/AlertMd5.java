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
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.Frequency;
import com.trs.netInsight.widget.alert.entity.PastMd5;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.entity.repository.PastMd5Repository;
import com.trs.netInsight.widget.alert.util.ScheduleUtil;
import com.trs.netInsight.widget.kafka.entity.AlertKafkaSend;
import com.trs.netInsight.widget.kafka.util.AlertKafkaUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.text.SimpleDateFormat;
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
        log.info("按热度值预警定时任务开始执行 ------------------");
        // 获得频率id
        Frequency alertFrequency = (Frequency) context.getJobDetail().getJobDataMap().get("schedule");
        List<AlertRule> rules1 = alertRuleRepository.findByStatusAndAlertTypeAndFrequencyId(ScheduleStatus.OPEN,
                AlertSource.AUTO, alertFrequency.getId());
        List<AlertRule> rules = new ArrayList<>();
       if(rules1 != null && rules1.size() > 0){
           for(int i=0;i<rules1.size();i++){
               String userid =  rules1.get(i).getUserId();
               User user = userRepository.findOne(userid);
               if(user.getStatus().equals("0")){
                   rules.add(rules1.get(i));
               }
           }
       }
        if (rules != null && rules.size() > 0) {
            log.info("按热度值预警定时任务开启的数量为："+rules.size());

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
                                searchBuilder.setPageNo(0);
                                searchBuilder.setPageSize(5);
                                if(StringUtil.isNotEmpty(appendGroupName)){
                                    searchBuilder.filterByTRSL(appendGroupName);
                                }
                            }
                            QueryBuilder searchBuilderWeiBo = alertRule.toSearchBuilderWeiBo(null);
                            if (searchBuilderWeiBo != null) {
                                searchBuilderWeiBo.setDatabase(Const.WEIBO);
                                searchBuilderWeiBo.setPageNo(0);
                                searchBuilderWeiBo.setPageSize(5);
                                if(StringUtil.isNotEmpty(appendGroupName)){
                                    searchBuilderWeiBo.filterByTRSL(appendGroupName);
                                }
                            }
                            QueryBuilder searchBuilderWeiXin = alertRule.toSearchBuilderWeiXin(null);
                            if (searchBuilderWeiXin != null) {
                                searchBuilderWeiXin.setDatabase(Const.WECHAT);
                                searchBuilderWeiXin.setPageNo(0);
                                searchBuilderWeiXin.setPageSize(5);
                                if(StringUtil.isNotEmpty(appendGroupName)){
                                    searchBuilderWeiXin.filterByTRSL(appendGroupName);
                                }
                            }
                            QueryBuilder searchBuilderTF = alertRule.toSearchBuilder(null);
                            if (searchBuilderTF != null) {
                                searchBuilderTF.setDatabase(Const.HYBASE_OVERSEAS);
                                searchBuilderTF.setPageNo(0);
                                searchBuilderTF.setPageSize(5);
                                if(StringUtil.isNotEmpty(appendGroupName)){
                                    searchBuilderTF.filterByTRSL(appendGroupName);
                                }
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
                                    .page(0,20);
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
                            log.info("按热度值预警，有数据可发送，预警：" + alertRule.getId() + "，名字：" + alertRule.getTitle());
                            List<Map<String, String>> sendMap = formatData(alertList,alertRule);
                            if(sendMap.size() >0){
                                Map<String, Object> map = new HashMap<>();
                                map.put("listMap", sendMap);
                                map.put("size", sendMap.size());
                                // 自动预警标题
                                map.put("title", alertRule.getTitle());
                                AlertKafkaSend alertKafkaSend = new AlertKafkaSend(alertRule,map);
                                AlertKafkaUtil.send(alertKafkaSend,true);

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

    public List<Map<String, String>> formatData(List<FtsDocumentCommonVO> voList, AlertRule alertRule) {
        List<Map<String, String>> listMap = new ArrayList<>();
        for(FtsDocumentCommonVO vo :voList){
            FtsDocumentAlert ftsDocumentAlert = null;
            String title = vo.getContent();
            title = StringUtil.replaceImgNew(title);
            String content = vo.getContent();
            String imgUrl = "";
            if (content != null) {
                List<String> imgSrcList = StringUtil.getImgStr(content);
                if (imgSrcList != null && imgSrcList.size() > 0) {
                    imgUrl = imgSrcList.get(0);
                }
            }
            content = StringUtil.replaceImg(content);
            String cutContent = StringUtil.cutContentPro(content, 150);

            String keywords = vo.getKeywords() == null || vo.getKeywords().size() ==0 ? "" : StringUtils.join(vo.getKeywords(),";");
            String groupName = vo.getGroupName();
            if (Const.GROUPNAME_WEIBO.equals(groupName)) {
                ftsDocumentAlert = new FtsDocumentAlert(vo.getSid(), cutContent, content, cutContent, content,vo.getUrlName(), vo.getUrlTime(), vo.getSiteName(), groupName,
                        vo.getCommtCount(), vo.getRttCount(), vo.getScreenName(), vo.getAppraise(), "", null,
                        "other", vo.getMd5Tag(), vo.getRetweetedMid(), imgUrl,keywords , 0, alertRule.getId());
            } else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
                ftsDocumentAlert = new FtsDocumentAlert(vo.getHkey(), title, title, cutContent,content, vo.getUrlName(), vo.getUrlTime(), vo.getSiteName(), groupName,
                        0, 0, vo.getAuthors(), vo.getAppraise(), "", null,
                        "other", vo.getMd5Tag(), "other", imgUrl, keywords, 0, alertRule.getId());
            } else if (Const.MEDIA_TYPE_TF.contains(groupName)) {
                ftsDocumentAlert = new FtsDocumentAlert(vo.getSid(), cutContent, content, cutContent,content, vo.getUrlName(), vo.getUrlTime(), vo.getSiteName(), groupName,
                        vo.getCommtCount(), vo.getRttCount(), vo.getAuthors(), vo.getAppraise(), "", null,
                        "other",  vo.getMd5Tag(), "other", imgUrl, keywords, 0, alertRule.getId());
            } else {
                ftsDocumentAlert = new FtsDocumentAlert(vo.getSid(), title, title, cutContent,content, vo.getUrlName(), vo.getUrlTime(), vo.getSiteName(), groupName,
                        0, 0, vo.getScreenName(), vo.getAppraise(), "", null,
                        vo.getNreserved1(),  vo.getMd5Tag(), "", imgUrl, keywords, 0, alertRule.getId());
            }

            Map<String, String> map = new HashMap<>();

            map.put("url", ftsDocumentAlert.getUrlName());
            map.put("titleWhole", ftsDocumentAlert.getTitleWhole());
            map.put("title", ftsDocumentAlert.getTitle());
            map.put("fullContent", ftsDocumentAlert.getFullContent());
            map.put("groupName", ftsDocumentAlert.getGroupName());
            map.put("sid", ftsDocumentAlert.getSid());
            map.put("retweetedMid", ftsDocumentAlert.getRetweetedMid());
            map.put("sim", "0");// 热度值要显示相似文章数
            String source = vo.getSiteName();
            if (StringUtil.isEmpty(source)) {
                source = groupName;
            }
            map.put("source", source);
            map.put("imageUrl", ftsDocumentAlert.getImageUrl());
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
            if (ftsDocumentAlert.getTime() != null) {
                String str = sdf.format(ftsDocumentAlert.getTime());
                map.put("urlTime", str);
            } else {
                map.put("urlTime", "");
            }

            map.put("appraise", ftsDocumentAlert.getAppraise());
            map.put("siteName", ftsDocumentAlert.getSiteName());
            map.put("md5", ftsDocumentAlert.getMd5tag());

            map.put("screenName", ftsDocumentAlert.getScreenName());
            map.put("rttCount", String.valueOf(ftsDocumentAlert.getRttCount()));
            map.put("commtCount", String.valueOf(ftsDocumentAlert.getCommtCount()));
            map.put("nreserved1", ftsDocumentAlert.getNreserved1());
            map.put("ruleId", alertRule.getId());
            map.put("organizationId", alertRule.getOrganizationId());
            map.put("countBy", alertRule.getCountBy());

            listMap.add(map);
        }

        return listMap;


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
            //searchBuilder.page(0, 5);
            if (StringUtil.isNotEmpty(notMd5)) {
                searchBuilder.filterChildField(FtsFieldConst.FIELD_MD5TAG, notMd5, Operator.NotEqual);
            }
            GroupResult categoryQuery = new GroupResult();
            try {
                categoryQuery = hybase8SearchServiceNew.categoryQuery(searchBuilder, false,
                        true, false,FtsFieldConst.FIELD_MD5TAG, null,Const.MIX_DATABASE.split(";"));
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
                                false, null);
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
                            if (Const.SOURCE_GROUPNAME_CONTRAST.containsKey(dataSource)) {
                                String group = Const.SOURCE_GROUPNAME_CONTRAST.get(dataSource);
                                buffer.append(group).append(" OR ");
                            }
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
