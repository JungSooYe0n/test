package com.trs.netInsight.util;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Map 工具
 *
 * Created by yan.changjiang on 2017/2/16.
 */
public class MapUtil {

	/**
	 * 装载Map
	 *
	 * @param keys
	 *            键
	 * @param value
	 *            值
	 * @return Map
	 * @throws OperationException
	 *             异常
	 */
	public static Map<String, Object> putValue(String[] keys, Object... value) throws OperationException {
		if (keys.length == value.length) {
			Map<String, Object> result = new HashMap<>();
			for (int i = 0; i < keys.length; i++) {
				result.put(keys[i], value[i]);
			}
			return result;
		}
		throw new OperationException("MapUtil put value error, value must as the number of keys");
	}

	/**
	 * 装载Map
	 *
	 * @param keys
	 *            键
	 * @param value
	 *            值
	 * @return Map
	 * @throws OperationException
	 *             异常
	 */
	public static Map<String, Object> putLinkValue(String[] keys, Object... value) throws OperationException {
		if (keys.length == value.length) {
			Map<String, Object> result = new LinkedHashMap<>();
			for (int i = 0; i < keys.length; i++) {
				result.put(keys[i], value[i]);
			}
			return result;
		}
		throw new OperationException("MapUtil put value error, value must as the number of keys");
	}

	/**
	 * Map value 排序
	 *
	 * @param listMap
	 *            要排序的map集合
	 * @param key
	 *            要排序的key
	 * @return 按照map.get(key)的值排序的List
	 * @since songbinbin 2017年5月5日
	 */
	public static List<Map<String, Object>> sortByValue(List<Map<String, Object>> listMap, String key) {
		listMap.sort((o1, o2) -> (int) (Double.parseDouble(o2.get(key).toString()))
				- (int) (Double.parseDouble(o1.get(key).toString())));
		return listMap;
	}

	/**
	 * 将时间分类统计的结果排序
	 *
	 * @date Created at 2017年12月7日 下午1:45:57
	 * @Author 谷泽昊
	 * @param groupResult
	 * @param dates
	 * @return
	 */
	public static List<Long> sortAndChangeList(GroupResult groupResult, List<String> dates, String dateFormat,
			boolean flag) {
		List<Long> list = new ArrayList<>();
		if (groupResult != null) {
			List<GroupInfo> groupList = groupResult.getGroupList();
			if (groupList != null && groupList.size() > 0) {
				Collections.sort(groupList, new ComparatorDate(flag));
				Map<String, Long> map = new LinkedHashMap<>();

				// List<Long> list = new ArrayList<>();
				for (String date : dates) {
					if (flag) {
						Date stringToDate = DateUtil.stringToDate(date, dateFormat);
						date = DateUtil.date2String(stringToDate, "yyyy/MM/dd");
						map.put(date, 0L);
					} else {
						map.put(date, 0L);
					}
				}

				for (Map.Entry<String, Long> entry : map.entrySet()) {
					for (GroupInfo groupInfo : groupList) {
						String value = groupInfo.getFieldValue();
						if (value.length() == 1) {
							value = "0" + value;
						}
						if (value.equals(entry.getKey())) {
							map.put(value, groupInfo.getCount());
						}

					}
					list.add(entry.getValue());
				}
				return list;

			}
		}
		for (int i = 0; i < dates.size(); i++) {
			list.add(0L);
		}
		return list;
	}

    /**
     * map排序
     * @param map
     * @param sort
     *          true为DESC，false为ASC
     * @return
     */
	public static Map<String,Long> sortByValue(Map<String,Long> map, boolean sort) {
        if (ObjectUtil.isNotEmpty(map)) {
            List<Map.Entry<String, Long>> list = new ArrayList<>(map.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
                @Override
                public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                    if (sort) {
                        return o2.getValue().compareTo(o1.getValue());
                    } else {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }
            });
            map = list.stream().collect(Collectors.toMap(entity -> entity.getKey(), entity -> entity.getValue()));
        }
        return map;
    }
}
