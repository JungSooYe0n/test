package com.trs.netInsight.widget.alert.util;

import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;

public class ScheduleUtil {




    /**
     * 排重方法
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List removeMd5(List ftsQuery) {
        // map排重
        Map<String, FtsDocument> map = new HashMap<>();
        Iterator<FtsDocument> docIterator = ftsQuery.iterator();
        while (docIterator.hasNext()) {
            FtsDocument ftsDocument = docIterator.next();
            map.put(ftsDocument.getMd5Tag(), ftsDocument);
        }
        // 再把排重之后的装起来
        List<FtsDocument> listrepetition = new ArrayList<>();
        Iterator<String> iterator = map.keySet().iterator();
        String key = "";
        while (iterator.hasNext()) {
            key = iterator.next();
            listrepetition.add(map.get(key));
        }
        // 把装起来之后的赋值给查询结果
        ftsQuery = listrepetition;
        return ftsQuery;
    }

    /**
     * 计算当前时间是否在发送预警的时间范围内
     */
    public static boolean time(AlertRule alertRule) {
        // 判断当前星期几
        // Date date = new Date();
        // SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
        // String currSun = dateFm.format(date);
        String currSun = DateUtil.getWeekOfDate(new Date());
        String week = alertRule.getWeek();
        if (StringUtils.isBlank(week)) {
            week = "星期一;星期二;星期三;星期四;星期五;星期六;星期日";
        }
        String[] split = week.split(";");
        List<String> list = Arrays.asList(split);
        // 在发送的星期
        if (list.contains(currSun)) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String nowTime = sdf.format(new Date());
            return isInTime(alertRule.getAlertStartHour(), alertRule.getAlertEndHour(), nowTime);
        }
        return false;
    }

    /**
     * 判断某一时间是否在一个区间内
     *
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isInTime(String beginTime, String endTime, String nowTime) {
        if (beginTime == null || !beginTime.contains(":")) {
            return false;
        }
        if (endTime == null || !endTime.contains(":")) {
            return false;
        }
        if (nowTime == null || !nowTime.contains(":")) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            long now = sdf.parse(nowTime).getTime();
            long start = sdf.parse(beginTime).getTime();
            if (endTime.equals("00:00")) {
                endTime = "24:00";
            }
            long end = sdf.parse(endTime).getTime();
            if (end < start) {
                if (now >= end && now < start) {
                    return false;
                } else {
                    return true;
                }
            } else {
                if (now >= start && now < end) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


}
