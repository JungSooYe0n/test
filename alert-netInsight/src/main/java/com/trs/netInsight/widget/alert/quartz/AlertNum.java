package com.trs.netInsight.widget.alert.quartz;

import com.alibaba.fastjson.JSON;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.support.fts.entity.FtsDocumentAlert;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import com.trs.netInsight.widget.alert.entity.enums.ScheduleStatus;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.alert.util.AutoAlertRedisUtil;
import com.trs.netInsight.widget.alert.util.ScheduleUtil;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.kafka.entity.AlertKafkaSend;
import com.trs.netInsight.widget.kafka.util.AlertKafkaUtil;
import com.trs.netInsight.widget.notice.service.INoticeSendService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

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

    @Autowired
    private UserRepository userRepository;


    public static final String hashName = "TOPIC";
    public static final String hashKey = "SID";
    public static final String DATA = "DATA";


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long time = System.currentTimeMillis();
        System.out.println(time);
        log.info("按数据量预警定时任务开始执行 ------------------");
        // 这个定时类找frequencyId为3的
        // 编写具体的业务逻辑
        List<AlertRule> rules1 = alertRuleRepository.findByStatusAndAlertTypeAndFrequencyId(ScheduleStatus.OPEN,
                AlertSource.AUTO, "3");
        List<AlertRule> rules = new ArrayList<>();
        if(rules1 != null && rules1.size() > 0){
            for(int i=0;i<rules1.size();i++){
                String userid =  rules1.get(i).getUserId();
                User user = userRepository.findOne(userid);
                //剩余有效期转换
                if(user != null){
                    if (UserUtils.FOREVER_DATE.equals(user.getExpireAt())){
                        user.setSurplusDate("永久");
                    }else {
                        String days = com.trs.netInsight.util.DateUtil.timeDifferenceDays(user.getExpireAt());
                        user.setSurplusDate(days);
                    }
                }
                if(user != null && "0".equals(user.getStatus()) && !"过期".equals(user.getSurplusDate())){
                    rules.add(rules1.get(i));
                }
            }
        }
        if (rules != null && rules.size() > 0) {
            log.info("按数据量预警定时任务开启的数量为："+rules.size());
            for (AlertRule alertRule : rules) {
                try {
                    String key = alertAutoPrefix + alertRule.getId();
                    Long dataSize = AutoAlertRedisUtil.getSizeForList(key);

                    if (ScheduleUtil.time(alertRule)) {
                        //在发送时间内，但是需要将昨晚的没用预警清空
                        if (dataSize > 0) {
                            log.info("按数据量预警，有数据可发送，预警：" + alertRule.getId() + "，名字：" + alertRule.getTitle() + "，数据条数为："+dataSize);
                            int timeInterval = alertRule.getTimeInterval() == 1 ? 5 : alertRule.getTimeInterval();
                            // 一分钟只做展示 还是五分钟发送一次
                            if(alertRule.getLastExecutionTime()==0){
                                alertRule.setLastExecutionTime(time);
                            }
                            boolean timeBoolean = (alertRule.getLastExecutionTime() + (timeInterval * 60000))/1000 <= System
                                    .currentTimeMillis()/1000;
                            if ((alertRule.getGrowth() > 0 && dataSize > alertRule.getGrowth()) || timeBoolean) {
                                //发送预警 列表中的预警全部拿出来 ，发送20条，其他的存入已发预警
                                // 拿取规则决定了，先拿出来的是先放进去的，所以发送时，拿最后边的20条即可，保证最新数据
                                List<Object> dataList = new ArrayList<>();
                                for (int i = 0; i < dataSize; i++) {
                                    Object data = AutoAlertRedisUtil.getOneDataForList(key);
                                    if (data != null) {
                                        dataList.add(data);
                                    }
                                }
                                List<String> groupList = CommonListChartUtil.formatGroupName(alertRule.getGroupName());
                                if (dataList.size() > 0) {
                                    List<Map<String, String>> listMap = new ArrayList<>();
                                    for (Object data : dataList) {
                                        Map<String, String> dataMap = (LinkedHashMap<String, String>) data;
                                        Map<String, String> vo = new LinkedHashMap<>();
                                        Object str = dataMap.get(DATA);
                                        vo = JSON.parseObject(str.toString(),LinkedHashMap.class);
                                        //Object vo = dataMap.get(DATA);
                                        //Object vo = AutoAlertRedisUtil.getOneDataForHash(dataMap.get(hashName), dataMap.get(hashKey));
                                        Map<String, String> oneMap = this.formatData(vo,alertRule);
                                        if(oneMap != null && groupList.contains(oneMap.get("groupName"))){
                                            listMap.add(oneMap);
                                        }
                                    }
                                    //去除标题重复的预警信息，保留一条
                                    for (int i = 0; i < listMap.size(); i++) {
                                        Map m1 = listMap.get(i);

                                        for (int j = i+1; j < listMap.size(); j++) {
                                            Map m2 = listMap.get(j);
                                            if(m1.get("title").equals(m2.get("title"))){
                                                listMap.remove(j);
                                                j--;
                                            }

                                        }
                                       //System.out.println(listMap.get(i).get("content"));
                                    }
                                    //将当前数据挨个转化为对应的数据格式，并发送
                                    if (listMap.size() > 20) {
                                        listMap = listMap.subList(listMap.size() - 20, listMap.size());
//                                        String keyWords = alertRule.getAnyKeyword();
//                                        Map<String,String> map = JSON.parseObject(keyWords,HashMap.class);
//                                        keyWords = map.get("keyWords");
//                                        for(Map<String, String> list:listMap){
//                                            if (ObjectUtil.isNotEmpty(list.get("content"))) {
//                                                list.put("content",list.get("content").replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
//                                            }
//                                            list.put("title",list.get("title").replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
//                                            list.put("titleWhole",list.get("titleWhole").replaceAll("&lt;", "<").replaceAll("&nbsp;", " ").replaceAll("&gt;", ">"));
//                                        }
                                    }
                                    if(listMap.size() >0){
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("listMap", listMap);
                                        map.put("size", listMap.size());
                                        // 自动预警标题
                                        map.put("title", alertRule.getTitle());

                                        AlertKafkaSend alertKafkaSend = new AlertKafkaSend(alertRule,map);
                                        AlertKafkaUtil.send(alertKafkaSend,true);

                                    }
                                }
                                alertRule.setLastStartTime(DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss));
                                alertRule.setLastExecutionTime(time);
                            }
                            alertRuleRepository.saveAndFlush(alertRule);
                        }
                    } else {
                        //指当前预警是开启的，但是不在用户指定的发送时间内，这时候需要记得将数据删除
                        for (long i = 0L; i < dataSize; i++) {
                            Object vo = AutoAlertRedisUtil.getOneDataForList(key);
                        }

//                        alertRule.setLastStartTime(DateUtil.formatCurrentTime(DateUtil.yyyyMMddHHmmss));
//                        alertRule.setLastExecutionTime(System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    log.error("预警【" + alertRule.getTitle() + "】任务报错：", e);
                }
            }

        }
    }


    public Map<String, String> formatData(Object vo, AlertRule alertRule) {
        if (vo == null) {
            return null;
        }

        FtsDocumentAlert ftsDocumentAlert = null;
        Map<String, Object> dataMap = (LinkedHashMap<String, Object>) vo;
        String sid = (String) dataMap.get(FtsFieldConst.FIELD_SID);
        String hkey = (String) dataMap.get(FtsFieldConst.FIELD_HKEY);
        String authors = (String) dataMap.get(FtsFieldConst.FIELD_AUTHORS);
        String urlName = (String) dataMap.get(FtsFieldConst.FIELD_URLNAME);
        String title = (String) dataMap.get(FtsFieldConst.FIELD_TITLE);
        String content = (String) dataMap.get(FtsFieldConst.FIELD_CONTENT);
        String screenName = (String) dataMap.get(FtsFieldConst.FIELD_SCREEN_NAME);
        String groupName = (String) dataMap.get(FtsFieldConst.FIELD_GROUPNAME);
        String md5Tag = (String) dataMap.get(FtsFieldConst.FIELD_MD5TAG);
        String appraise = dataMap.get(FtsFieldConst.FIELD_APPRAISE) == null ? "中性" : dataMap.get(FtsFieldConst.FIELD_APPRAISE).toString();
        String siteName = (String) dataMap.get(FtsFieldConst.FIELD_SITENAME);
        String srcName = (String) dataMap.get(FtsFieldConst.FIELD_SRCNAME);
        String abstracts = (String) dataMap.get(FtsFieldConst.FIELD_ABSTRACTS);
        Object nreserved1Object = dataMap.get(FtsFieldConst.FIELD_NRESERVED1);
        String nreserved1 ="";
        if(nreserved1Object != null){
            if(nreserved1Object instanceof List){
                nreserved1 = ((List) nreserved1Object).get(0).toString();
            }else{
                nreserved1 = nreserved1Object.toString();
            }
        }
        String retweetedMid = (String) dataMap.get(FtsFieldConst.FIELD_RETWEETED_MID);
        Long commtCount = dataMap.get(FtsFieldConst.FIELD_COMMTCOUNT) == null ? 0 : Long.parseLong(dataMap.get(FtsFieldConst.FIELD_COMMTCOUNT).toString().equals("")?"0":dataMap.get(FtsFieldConst.FIELD_COMMTCOUNT).toString());
        Long rttCount = dataMap.get(FtsFieldConst.FIELD_RTTCOUNT) == null ? 0 : Long.parseLong(dataMap.get(FtsFieldConst.FIELD_RTTCOUNT).toString().equals("")?"0":dataMap.get(FtsFieldConst.FIELD_RTTCOUNT).toString());
        String keywords = (String) dataMap.get(FtsFieldConst.FIELD_KEYWORDS);
        String channel = (String) dataMap.get(FtsFieldConst.FIELD_CHANNEL);

        String loadTimeString = (String) dataMap.get(FtsFieldConst.FIELD_LOADTIME);
        String urlTimeString = (String) dataMap.get(FtsFieldConst.FIELD_URLTIME);

        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        //Date loadTime = null;
        Date urlTime = null;
        try {
           // loadTime = format.parse(loadTimeString);
            urlTime = format.parse(urlTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String imgUrl = "";
        if (content != null) {
            List<String> imgSrcList = StringUtil.getImgStr(content);
            if (imgSrcList != null && imgSrcList.size() > 0) {
                imgUrl = imgSrcList.get(0);
            }
        }
        content = StringUtil.replaceImgNew2(content);
        String cutContent = StringUtil.cutContentPro(content, 150);

        title =StringUtil.replaceImgNew2(title);
        title = StringUtil.cutContentPro(title, 150);
        if (Const.GROUPNAME_WEIBO.equals(groupName)) {

            ftsDocumentAlert = new FtsDocumentAlert(sid, cutContent, content, cutContent,content, urlName, urlTime, siteName, groupName,
                    commtCount, rttCount, screenName, appraise, "", null,
                    "other", md5Tag, retweetedMid, imgUrl, keywords, 0, alertRule.getId());
        } else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
            ftsDocumentAlert = new FtsDocumentAlert(hkey, title, title, cutContent,content, urlName, urlTime, siteName, groupName,
                    0, 0, authors, appraise, "", null,
                    "other", md5Tag, "other", imgUrl, keywords, 0, alertRule.getId());
        } else if (Const.MEDIA_TYPE_TF.contains(groupName)) {
            ftsDocumentAlert = new FtsDocumentAlert(sid, cutContent, content, cutContent,content, urlName, urlTime, siteName, groupName,
                    commtCount, rttCount, authors, appraise, "", null,
                    "other", md5Tag, "other", imgUrl, keywords, 0, alertRule.getId());
        } else {
            ftsDocumentAlert = new FtsDocumentAlert(sid, title, title, cutContent,content, urlName, urlTime, siteName, groupName,
                    0, 0, authors, appraise, "", null,
                    nreserved1, md5Tag, "", imgUrl, keywords, 0, alertRule.getId());
        }


        Map<String, String> map = new HashMap<>();

        map.put("url", ftsDocumentAlert.getUrlName());
        map.put("titleWhole", ftsDocumentAlert.getTitleWhole());
        map.put("title", ftsDocumentAlert.getTitle());
        map.put("content", ftsDocumentAlert.getContent());
        map.put("fullContent", ftsDocumentAlert.getFullContent());
        map.put("groupName", ftsDocumentAlert.getGroupName());
        map.put("sid", ftsDocumentAlert.getSid());
        map.put("retweetedMid", ftsDocumentAlert.getRetweetedMid());
        map.put("sim", "0");// 热度值要显示相似文章数
        String source = siteName;
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

        return map;


    }

}
