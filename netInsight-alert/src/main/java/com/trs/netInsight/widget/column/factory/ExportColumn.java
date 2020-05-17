package com.trs.netInsight.widget.column.factory;

import java.io.IOException;

import com.alibaba.fastjson.JSONArray;

import lombok.Getter;
import lombok.Setter;

/**
 * 日常监测图数据导出抽象类
 * @author liangxin
 *
 */
@Getter
@Setter
public abstract class ExportColumn {
	
	/**
	 * 饼图和柱状图数据导出
	 * @author liangxin
	 * @since  2018年8月28日
	 */
	public abstract void exportData(JSONArray array) throws IOException;
	
	/**
	 * 折线图数据导出
	 * @author liangxin
	 * @since  2018年8月28日
	 */
	public abstract void exportChartLine(JSONArray array) throws IOException;
	
	/**
	 * 词云图数据导出
	 * @author liangxin
	 * @since  2018年8月28日
	 */
	public abstract void exportWordCloud(JSONArray array) throws IOException;
	
	/**
	 * 地域图数据导出
	 * @author liangxin
	 * @since  2018年8月28日
	 */
	public abstract void exportMap(JSONArray array) throws IOException;
	
}
