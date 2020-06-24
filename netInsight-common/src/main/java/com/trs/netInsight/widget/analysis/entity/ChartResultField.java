package com.trs.netInsight.widget.analysis.entity;

import lombok.Getter;

@Getter
public class ChartResultField {
    //返回结果中的对比数据存的字段名
    private String contrastField;
    //返回结果中的统计数据存的字段名
    private String countField;
    //这个在折线图中用，代表x轴对应的数据存的字段名
    private String lineXField;

    public ChartResultField(String contrastField, String countField, String lineXField){
        this.contrastField = contrastField;
        this.countField = countField;
        this.lineXField = lineXField;
    }

    public ChartResultField(String contrastField, String countField){
        this.contrastField = contrastField;
        this.countField = countField;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(contrastField);
        sb.append(";");
        sb.append(countField);
        sb.append(";");
        sb.append(lineXField);
        sb.append(";");
        return sb.toString();
    }

}
