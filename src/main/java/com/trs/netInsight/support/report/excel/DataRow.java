package com.trs.netInsight.support.report.excel;

import java.io.Serializable;

import lombok.Data;

/**
 * 表格数据实体
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Data
public class DataRow implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object value;
    private String link;

    public DataRow(Object value) {
        this.value = value;
        this.link = "";
    }

    public DataRow(Object value, String link) {
        this.value = value;
        this.link = link;
    }
}
