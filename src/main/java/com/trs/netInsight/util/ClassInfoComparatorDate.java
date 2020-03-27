package com.trs.netInsight.util;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.Logger;

import com.trs.netInsight.widget.analysis.entity.ClassInfo;

/**
 * ClassInfo 分类统计出来的时间排序
 * 
 * @Type ComparatorDate.java
 * @Desc
 * @author 谷泽昊
 * @date 2017年12月7日 下午1:38:18
 * @version
 */
public class ClassInfoComparatorDate implements Comparator<Object> {
	private static final Logger logger = Logger.getLogger(ClassInfoComparatorDate.class);
	private String dateformat = null;
	private SimpleDateFormat format = null;

	public ClassInfoComparatorDate(boolean flag) {
		this.dateformat = flag ? "HH" : "yyyy-MM-dd";
		this.format = new SimpleDateFormat(dateformat);
	}

	public int compare(Object obj1, Object obj2) {
		ClassInfo t1 = (ClassInfo) obj1;
		ClassInfo t2 = (ClassInfo) obj2;
		Date d1;
		Date d2;
		try {
			d1 = format.parse(t1.getStrValue());
			d2 = format.parse(t2.getStrValue());
		} catch (Exception e) {
			// 解析出错，则不进行排序
			logger.error("排序出错：" + e);
			return 0;
		}
		if (d1.before(d2)) {
			return -1;
		} else {
			return 1;
		}
	}
}