package com.trs.netInsight.widget.report.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SpecialReportUtil {

    //根据数据获取对应图片的描述信息
    public static String getImgComment(String img_data, String imgType, String chapter) {
        if ("gaugeChart".equals(imgType)) {
            //态势评估
            return getGaugeChart(img_data);
        } else if("brokenLineChart".equals(imgType)){
            //折线图
            return getSprcialBrokenLineChart(img_data);
        } else if("pieChart".equals(imgType)|| "moodStatistics".equals(imgType)){
            //饼图  数据源统计
            return getPieChart(img_data);
        } else if("emotionPieChart".equals(imgType) ){
            //情感分析 饼图    情绪分析 饼图
            return getEmotionChart(img_data);
        } else if("activeAccount".equals(imgType)){
            return getActiveAccount(img_data);
        } else if("mapChart".equals(imgType)){
            return getArea(img_data);
        }  else{
            log.info("没有匹配到对应的图片类型 - "+imgType);
        }
        return null;
    }


    //态势评估
    private static String getGaugeChart(String imgData) {
        if(StringUtil.isNotEmpty(imgData)){
            StringBuilder resultSb = new StringBuilder();
            resultSb.append("由图可知，目前热度指数为").append(imgData);
            resultSb.append("，事件态势处于");
            Integer data = Integer.valueOf(imgData);
            if(data <=40){
                resultSb.append("低热。");
            }else if(data <80 && data >40){
                resultSb.append("中热。");
            }else{
                resultSb.append("高热。");
            }
            return resultSb.toString();
        }
        return null;
    }

    // 日常监测 专题分析报告的 各舆论场趋势分析
    private static String getSprcialBrokenLineChart(String imgData) {
        if(StringUtil.isNotEmpty(imgData)){
            JSONObject object = JSONObject.parseObject(imgData);

            JSONObject single = object.getJSONObject("double");

            JSONArray timeArr = single.getJSONArray("lineXdata");
            JSONArray totalArr = single.getJSONArray("total");
            if (totalArr == null || totalArr.size() ==0) {
                return "";
            }
            int index = 0;
            int num = 0;
            for(int i =0;i<totalArr.size();i++){
                Integer one = Integer.valueOf(totalArr.get(i).toString());
                if(one > num){
                    num = one;
                    index = i;
                }
            }
            String time = timeArr.get(index).toString();

            StringBuilder resultSb = new StringBuilder();

            resultSb.append("由图可知，信息量于");
            //时间
            if(time.length() ==10 &&  DateUtil.isTimeFormatterYMD(time)){
                //time = time.replaceAll("/","").replaceAll(" ","");
                String date = DateUtil.formatDateToString(time,DateUtil.yyyyMMdd4,DateUtil.YMD_PAGE);
                resultSb.append(date);
            }else if(time.length() ==16 && DateUtil.isTimeFormatterYMDH(time)){
                //time = time.replaceAll("/","").replaceAll(" ","").replaceAll(":","");
                String date = DateUtil.formatDateToString(time,DateUtil.yyyyMMddHHmmss_Line2,DateUtil.YMDH_PAGE);
                resultSb.append(date);
            }
            resultSb.append("达到最高峰，信息量为").append(num).append("篇。");

            return resultSb.toString();

        }
        return null;

    }
    // 日常监测 专题分析报告的 舆论场发布统计 - 饼图
    private static String getPieChart(String imgData) {
        if(StringUtil.isNotEmpty(imgData)){
            List<Map<String, Object>> parseArray = JSONObject.parseObject(imgData,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            Collections.sort(parseArray, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                    Integer count1 = Integer.valueOf(m1.get("value").toString());
                    Integer count2 = Integer.valueOf(m2.get("value").toString());
                    if (count1 ==  count2) {
                        return 0;
                    } else {
                        return count2 >  count1 ? 1 : -1;
                    }
                }
            });
            Integer total = 0;
            for (Map<String, Object> oneMap : parseArray) {
                Integer num = Integer.valueOf( oneMap.get("value").toString());
                total += num;
            }
            DecimalFormat df = new DecimalFormat("0.00");//格式化小数
            StringBuilder resultSb = new StringBuilder();
            resultSb.append("由图可知，");
            for(int i = 0;i <parseArray.size(); i++){
                Map<String, Object> oneData = parseArray.get(i);
                Integer value = Integer.valueOf(oneData.get("value").toString());

                String num = df.format((float)value*100/total);
                if( i == 0){
                    resultSb.append(oneData.get("name")).append("信息量占比最高，占总信息量的");
                    resultSb.append(num).append("%");
                }else if(i ==1){
                    resultSb.append("其次是").append(oneData.get("name")).append("，信息量占比");
                    resultSb.append(num).append("%");
                }else if(i ==2){
                    resultSb.append("再次是").append(oneData.get("name")).append("，信息量占比");
                    resultSb.append(num).append("%");
                }else if( i==3){
                    resultSb.append("此外，").append(oneData.get("name")).append("，信息量占比");
                    resultSb.append(num).append("%");
                }else{
                    resultSb.append(oneData.get("name")).append("，信息量占比");
                    resultSb.append(num).append("%");
                }
                if(i == parseArray.size() -1){
                    resultSb.append("。");
                }else{
                    resultSb.append("；");
                }
            }
            return resultSb.toString();
        }
        return null;
    }
    // 日常监测 专题分析报告的 正负面占比和情绪统计 - 饼图
    private static String getEmotionChart(String imgData) {
        if(StringUtil.isNotEmpty(imgData)){
            List<Map<String, Object>> parseArray = JSONObject.parseObject(imgData,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            Collections.sort(parseArray, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                    Integer count1 = Integer.valueOf(m1.get("value").toString());
                    Integer count2 = Integer.valueOf(m2.get("value").toString());
                    if (count1 ==  count2) {
                        return 0;
                    } else {
                        return  count2 >  count1 ? 1 : -1;
                    }
                }
            });
            Integer total = 0;
            for (Map<String, Object> oneMap : parseArray) {
                Integer num = Integer.valueOf( oneMap.get("value").toString());
                total += num;
            }
            DecimalFormat df = new DecimalFormat("0.00");//格式化小数
            StringBuilder resultSb = new StringBuilder();
            resultSb.append("由图可知，");
            for(int i = 0;i <parseArray.size(); i++){
                Map<String, Object> oneData = parseArray.get(i);
                Integer value = Integer.valueOf( oneData.get("value").toString());

                String num = df.format((float)value*100/total);

                if( i == 0){
                    resultSb.append(oneData.get("name")).append("占比最高，占比");
                    resultSb.append(num).append("%");
                }else if(i ==1){
                    resultSb.append("其次是").append(oneData.get("name")).append("，占比");
                    resultSb.append(num).append("%");
                }else {
                    resultSb.append(oneData.get("name")).append("，占比最低");
                    resultSb.append(num).append("%");
                }
                if(i == parseArray.size() -1){
                    resultSb.append("。");
                }else{
                    resultSb.append("；");
                }
            }
            return resultSb.toString();
        }
        return null;
    }
    // 日常监测 专题分析报告的 活跃账号
    private static String getActiveAccount(String imgData) {
        if(StringUtil.isNotEmpty(imgData)){
            StringBuilder resultSb = new StringBuilder();
            List<Map<String, Object>> parseArray = JSONObject.parseObject(imgData,
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            Map<String, Object> oneMap = parseArray.get(0);
            String name = (String)oneMap.get("name");
            List<Map<String, Object>>info = (List<Map<String, Object>>)oneMap.get("info");
            resultSb.append("由图可知，活跃度最高的10个为");
            String maxName = "";
            Integer maxNum = -1;
            for(Map<String, Object> infoMap :info){
                String sitename = (String)infoMap.get("name");
                if(StringUtil.isNotEmpty(sitename)){
                    Integer value = Integer.valueOf(infoMap.get("value").toString());
                    if(value >maxNum){
                        maxName = sitename;
                        maxNum = value;
                    }
                    resultSb.append(sitename).append("、");
                }
            }
            String str = resultSb.toString().substring(0,resultSb.length()-1);
            resultSb = new StringBuilder(str);
            resultSb.append("。其中，").append(maxName).append("发布的信息量最大，共")
                    .append(maxNum).append("篇。");

            return resultSb.toString();
        }
        return null;
    }

    // 日常监测 专题分析报告的 地图
    private static String getArea(String imgData) {
        if(StringUtil.isNotEmpty(imgData)){
            StringBuilder resultSb = new StringBuilder();
            List<Map<String, Object>> parseArray = JSONObject.parseObject(imgData,
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            resultSb.append("由图可知，提及度较高的是");
            for(int i =0;i<parseArray.size() ;i++){
                Map<String, Object> oneMap = (Map<String, Object>)parseArray.get(i);
                if(i <3){
                    resultSb.append(oneMap.get("name")).append("、");
                }else{
                    break;
                }
            }
            String str = resultSb.toString().substring(0,resultSb.length()-1);
            resultSb = new StringBuilder(str);
            Map<String, Object> oneMap = (Map<String, Object>)parseArray.get(0);
            resultSb.append("等地。其中，提及").append(oneMap.get("name")).append("的信息量最大，共")
                    .append(oneMap.get("value")).append("篇。");
            return resultSb.toString();
        }
        return null;
    }

}
