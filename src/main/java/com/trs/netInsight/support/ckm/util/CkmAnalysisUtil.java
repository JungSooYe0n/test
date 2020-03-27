/*
 * Project: netInsight
 * 
 * File Created at 2018年3月7日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.ckm.util;

import java.util.ArrayList;
import java.util.List;

import com.auth0.jwt.internal.org.apache.commons.lang3.StringUtils;
import com.trs.netInsight.support.ckm.entity.AnalysisValue;

/**
 * @Desc ckm分析工具类
 * @author yan.changjiang
 * @date 2018年3月7日 上午11:24:08
 * @version
 */
public class CkmAnalysisUtil {

	/**
	 * 对相似分析结果分组
	 * 
	 * @param simData
	 * @return
	 */
	public static List<List<AnalysisValue>> simDataGroup(List<List<AnalysisValue>> simData) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < simData.size(); i++) {
			List<AnalysisValue> line = simData.get(i);
			for (int j = 0; j < line.size(); j++) {
				AnalysisValue kv = line.get(j);
				sb.append(kv.getMsgId());
				sb.append(";");
			}
			sb.append("\r\n");
		}
		String[] lines = sb.toString().split("\r\n");
		List<String> list = splitToGroup(lines);
		List<List<AnalysisValue>> cluserMessage = new ArrayList<List<AnalysisValue>>();
		for (int i = 0; i < list.size(); i++) {
			String group = list.get(i);
			String[] line = group.split("\r\n");
			List<AnalysisValue> groupList = new ArrayList<AnalysisValue>();
			for (int j = 0; j < line.length; j++) {
				String[] keyword = line[j].split(";");
				for (int k = 0; k < keyword.length; k++) {
					String id = keyword[k];
					boolean exist = false;
					for (int m = 0; m < groupList.size(); m++) {
						if ((groupList.get(m)).getMsgId().equals(id)) {
							exist = true;
							break;
						}
					}
					if (!exist) {
						AnalysisValue kv = new AnalysisValue();
						kv.setMsgId(id);
						groupList.add(kv);
					}
				}
			}
			cluserMessage.add(groupList);
		}
		return cluserMessage;
	}

	/**
	 * 处理id集
	 * 
	 * @param lines
	 * @return
	 */
	private static List<String> splitToGroup(String[] lines) {
		boolean finish;
		do {
			finish = false;
			for (int i = 0; i < lines.length; i++) {
				if (StringUtils.isBlank(lines[i])) {
					continue;
				}
				for (int j = i; j < lines.length; j++) {
					if (StringUtils.isBlank(lines[j])) {
						continue;
					}
					if ((i == j) || (!_topicIncludes(lines[i], lines[j]))) {
						continue;
					}
					lines[i] = (lines[i] + "\r\n" + lines[j]);
					lines[j] = "";
					finish = true;
				}
			}

		} while (finish);
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < lines.length; i++) {
			if (StringUtils.isNotEmpty(lines[i])) {
				list.add(lines[i]);
			}
		}
		return list;
	}

	private static boolean _topicIncludes(String group1, String group2) {
		String[] line1 = group1.split("\r\n");
		String[] line2 = group2.split("\r\n");
		for (int a = 0; a < line1.length; a++) {
			for (int b = 0; b < line2.length; b++) {
				boolean compare = _topicInclude(line1[a], line2[b]);
				if (compare) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean _topicInclude(String line1, String line2) {
		String[] keyword1 = line1.split(";");
		String[] keyword2 = line2.split(";");
		for (int i = 0; i < keyword1.length; i++) {
			String key1 = keyword1[i];
			if (key1.equals("")) {
				continue;
			}
			if (keyword1[i].indexOf("#") > 0) {
				String[] data1 = keyword1[i].split("#");
				key1 = data1[0];
			}
			for (int j = 0; j < keyword2.length; j++) {
				String key2 = keyword2[j];
				if (key2.equals("")) {
					continue;
				}
				if (keyword2[j].indexOf("#") > 0) {
					String[] data2 = keyword2[j].split("#");
					key2 = data2[0];
				}

				if (key1.equals(key2)) {
					return true;
				}
			}
		}
		return false;
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月7日 yan.changjiang creat
 */