package com.trs.netInsight.util.chart;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trs.dc.entity.TRSEsRecord;
import com.trs.dc.entity.TRSEsRecordSet;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.util.MapUtil;
import com.trs.netInsight.util.ObjectUtil;


/**
 * 折线图工具类
 * Created by xiaoying on 2017/7/7.
 */
@SuppressWarnings("rawtypes")
public class BrokenLineUtil {

	public static Map brokenLineNew(GroupResult recordSet, String spacialName, List<String> dateList) throws OperationException {
		//迭代取值
		Iterator<GroupInfo> resultIterator=null;
		if(ObjectUtil.isNotEmpty(recordSet)){
			resultIterator = recordSet.iterator();
		}
		GroupInfo result = null;
		Map mapAll = new HashMap<String,Object>();
		Long[] array = new Long[dateList.size()];
		if(ObjectUtil.isNotEmpty(resultIterator)){
			while (resultIterator.hasNext()) {
				result = resultIterator.next();
				String key =  result.getFieldValue();
				key=key.replaceAll("[-/.: ]", "").trim();
				Long  value = result.getCount();
				int ind = dateList.indexOf(key);
				if(ind!=-1){
					array[ind] = value;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				array[i] = (long) 0;
			}
		}
		mapAll= MapUtil.putValue(new String[]{"groupname","result"},spacialName,array);
		return mapAll;
	}
	//dateList是小时的
	public static Map broken(GroupResult recordSet, String spacialName, List<String> dateList) throws OperationException {
		//迭代取值
		Iterator<GroupInfo> resultIterator=null;
		if(ObjectUtil.isNotEmpty(recordSet)){
			resultIterator = recordSet.iterator();
		}
		GroupInfo result = null;
		Long[] array = new Long[dateList.size()];
		if(ObjectUtil.isNotEmpty(resultIterator)){
			while (resultIterator.hasNext()) {
				result = resultIterator.next();
				String key =  result.getFieldValue();
				key=key.replaceAll("[-/.: ]", "").trim();
				Long  value = result.getCount();
				int ind = dateList.indexOf(key);
				if(ind!=-1){
					array[ind] = value;
				}
			}
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				array[i] = (long) 0;
			}
		}
		Map<String, Object> mapAll= MapUtil.putValue(new String[]{"groupname","result"},spacialName,array);
		return mapAll;
	}
	public static Map brokenLine(TRSEsRecordSet recordSet, String spacialName, List<String> date) throws OperationException {
		//迭代取值
		Iterator<TRSEsRecord> resultIterator=null;
		if(ObjectUtil.isNotEmpty(recordSet)){
			resultIterator = recordSet.iterator();
		}
		TRSEsRecord result = null;
		Map mapAll = new HashMap<String,Object>();
		Object[] array = new String[date.size()];
		if(ObjectUtil.isNotEmpty(resultIterator)){
			while (resultIterator.hasNext()) {
				result = resultIterator.next();
				Map map = result.getResultMap();
				Iterator it = map.entrySet().iterator();
				String key = "";
				Object value = "";
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					key = (String) entry.getKey();
					value = entry.getValue();
				}
				String everyDay = String.valueOf(key);
				everyDay = everyDay.replaceAll("[-/.: ]", "").trim();
				everyDay = everyDay.substring(0, 8);
				int ind = date.indexOf(everyDay);
				array[ind] = value;

			}
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				array[i] = "0";
			}
		}
		mapAll= MapUtil.putValue(new String[]{"spacialName","array"},spacialName,array);
		return mapAll;
	}
	public static Map brokenLineNew1(GroupResult recordSet, String spacialName, List<String> dateList) throws OperationException {
		//迭代取值
		Iterator<GroupInfo> resultIterator=null;
		if(ObjectUtil.isNotEmpty(recordSet)){
			resultIterator = recordSet.iterator();
		}

		GroupInfo result = null;

		Map mapAll = new HashMap<String,Object>();
		Integer[] array = new Integer[dateList.size()];
		if(ObjectUtil.isNotEmpty(resultIterator)){
			while (resultIterator.hasNext()) {
				result = resultIterator.next();
				String key = "";
				Object value = "";
				key =  result.getFieldValue();
				value = result.getCount();
				int ind = dateList.indexOf(key);
				array[ind] = (Integer) value;

			}
		}

		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				array[i] = 0;
			}
		}
		mapAll= MapUtil.putValue(new String[]{"groupname","result"},spacialName,array);
		return mapAll;
	}

	public static Map brokenLineTime(TRSEsRecordSet recordSet, String spacialName, List<String> date) throws OperationException {
		//迭代取值
		Iterator<TRSEsRecord> resultIterator = recordSet.iterator();
		TRSEsRecord result = null;

		Map mapAll = new HashMap<String,Object>();
		Object[] array = new String[date.size()];
		while (resultIterator.hasNext()) {
			result = resultIterator.next();
			Map map = result.getResultMap();
			Iterator it = map.entrySet().iterator();
			String key = "";
			Object value = "";
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				key = (String) entry.getKey();
				value = entry.getValue();
			}
			String everyDay = String.valueOf(key);

			int ind = date.indexOf(everyDay);
			if(ind!=-1){
				array[ind] = value;
			}


		}
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				array[i] = "0";
			}
		}
		mapAll= MapUtil.putValue(new String[]{"spacialName","array"},spacialName,array);
		return mapAll;
	}

}
