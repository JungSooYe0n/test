/*
 * Project: netInsight
 * 
 * File Created at 2017年12月7日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.widget.analysis.entity.ClassInfo;

/**
 * ClassInfoUtil 工具类
 * 
 * @Type ClassInfoUtil.java
 * @author 谷泽昊
 * @date 2017年12月7日 下午3:13:34
 * @version
 */
public class ClassInfoUtil {

	/**
	 * 排序并格式化时间
	 * 
	 * @date Created at 2017年12月7日 下午3:31:10
	 * @Author 谷泽昊
	 * @param list
	 * @param dateList
	 * @return
	 */
	public static List<ClassInfo> timeChange(List<ClassInfo> list, List<String> dateList, boolean flag) {
		List<ClassInfo> listClassInfo = new ArrayList<>();
		Map<String, Long> map = new HashMap<>();
		if (!flag) {
			for (String date : dateList) {
				map.put(date, 0L);
			}
		} else {
			String formatCurrentTime = DateUtil.formatCurrentTime("HH");
			for (int i = 0; i <= Integer.valueOf(formatCurrentTime); i++) {
				if (i < 10) {
					map.put("0" + i + ":00", 0L);
				} else {
					map.put(i + ":00", 0L);
				}
			}
		}
		if (list != null && list.size() > 0 && dateList != null && dateList.size() > 0) {
			for (ClassInfo classInfo : list) {
				String date = null;
				if (!flag) {
					Date stringToDate = DateUtil.stringToDate(classInfo.getStrValue(), "yyyy/MM/dd");
					date = DateUtil.date2String(stringToDate, "yyyy-MM-dd");
				} else {
					date = classInfo.getStrValue();
				}
				if (!flag) {
					map.put(date, classInfo.getIRecordNum());
				}else{
					map.put(String.valueOf(date), classInfo.getIRecordNum());
				}
			}

		}
		for (Map.Entry<String, Long> entry : map.entrySet()) {
			listClassInfo.add(new ClassInfo(entry.getKey(), entry.getValue()));
		}
		Collections.sort(listClassInfo, new ClassInfoComparatorDate(flag));
		return listClassInfo;
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年12月7日 谷泽昊 creat
 */