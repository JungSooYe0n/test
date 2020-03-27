package com.trs.netInsight.support.fts.model.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 检索返回结果对象
 *
 * Create by yan.changjiang on 2017年11月20日
 */
public class GroupResult implements Iterable<GroupInfo> {

	private List<GroupInfo> groupList = new ArrayList<>();

	public void addAll(GroupResult result) {
		groupList.addAll(result.getGroupList());
	}

	public void addAll(Map<String, Long> map) {
		for (Map.Entry<String, Long> entry : map.entrySet()) {
			groupList.add(new GroupInfo(entry.getKey(), entry.getValue()));
		}
	}

	public int size() {
		return groupList.size();
	}

	public void addGroup(String field, long number) {
		groupList.add(new GroupInfo(field, number));
	}

	public String getFieldValue(int index) {
		return groupList.get(index).getFieldValue();
	}

	public long getCount(int index) {
		return groupList.get(index).getCount();
	}

	public List<GroupInfo> getGroupList() {
		return groupList;
	}

	@Override
	public Iterator<GroupInfo> iterator() {
		return groupList.iterator();
	}

	public void sort() {
		this.groupList.sort(GroupInfo::compareTo);
	}

}
