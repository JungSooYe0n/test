package com.trs.netInsight.widget.analysis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * key-value类型对象, 适用于分类统计结果集封装等String-Integer类型数据集
 * <p>
 * Create by yan.changjiang on 2017年11月21日
 */
@Getter
@Setter
@NoArgsConstructor
public class ClassInfo {
    public String strValue = null;
    public long iRecordNum = 0;
    public ClassInfo(String strValue, long iRecordNum) {
        if("国内新闻".equals(strValue)){
            this.strValue = "新闻";
        }else if("国内博客".equals(strValue)){
            this.strValue = "博客";
        }else if("国内论坛".equals(strValue)){
            this.strValue = "论坛";
        }else if("国内视频".equals(strValue)){
            this.strValue = "视频";
        }else if("国内新闻_手机客户端".equals(strValue)){
            this.strValue = "手机客户端";
        }else if("国外新闻".equals(strValue)){
            this.strValue = "境外新闻";
        }else if("国内新闻_电子报".equals(strValue)){
            this.strValue = "电子报";
        }else{
            this.strValue = strValue;
        }
        this.iRecordNum = iRecordNum;
    }
}