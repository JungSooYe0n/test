package com.trs.netInsight.support.report.excel;

import com.trs.netInsight.util.ObjectUtil;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * Excel数据
 *
 * Create by yan.changjiang on 2017年11月24日
 */
@NoArgsConstructor
public class ExcelData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<String> head = new ArrayList<>();

	private Map<String,List<String>> headMap = new LinkedHashMap<>();

	private Map<Integer, List<DataRow>> row = new LinkedHashMap<>();

	private Map<String, LinkedHashMap<Integer, List<DataRow>>> mixSheet = new LinkedHashMap<>();

	private List<List<String>> rowAndCell = new ArrayList<>();

	public List<List<String>> getRowAndCell() {
		return rowAndCell;
	}

	public void setRowAndCell(List<List<String>> rowAndCell) {
		this.rowAndCell = rowAndCell;
	}

	/**
	 * 设置表头
	 *
	 * @param names
	 *            列名
	 */
	public void setHead(String... names) {
		if (names.length > 0) {
			head.addAll(Arrays.asList(names));
		}
	}

	public String getHead(int index) {
		return head.get(index);
	}

	public int column() {
		return head.size();
	}

	public int row() {
		return row.size();
	}

	public void addRow(Object... value) {
		List<DataRow> entities = new ArrayList<>();
		if (ObjectUtil.isNotEmpty(value)) {
			for (Object obj : value) {
				if (!(obj instanceof DataRow)) {
					entities.add(new DataRow(obj));
				} else {
					entities.add((DataRow) obj);
				}
			}
			row.put(row.size(), entities);
		}
	}
	/**
	 * 直接把list传进来
	 * @param entities
	 */
	public void addRow(List<DataRow> entities) {
		row.put(row.size(), entities);
	}
	
	public List<DataRow> getRow(int index) {
		if (index < row.size()) {
			return row.get(index);
		}
		return null;
	}

	public Map<String, LinkedHashMap<Integer, List<DataRow>>> getSheet(){
		return mixSheet;
	}

	public LinkedHashMap<Integer, List<DataRow>> getOneSheet(String key){
		if(mixSheet.containsKey(key)){
			return mixSheet.get(key);
		}
		return null;
	}
	public void putSheet(String key ,List<DataRow> row){
		if(mixSheet.containsKey(key)){
			LinkedHashMap<Integer, List<DataRow>> rows = mixSheet.get(key);
			rows.put(rows.size(),row);
			mixSheet.put(key,rows);
		}else{
			LinkedHashMap<Integer, List<DataRow>> rows = new LinkedHashMap<>();
			rows.put(rows.size(),row);
			mixSheet.put(key,rows);
		}
	}

	public void putSheets(String key ,Object obj){
		if(obj == null){
			LinkedHashMap<Integer, List<DataRow>> rows = new LinkedHashMap<>();
			if(!mixSheet.containsKey(key)){
				mixSheet.put(key,rows);
			}
		}else{
			mixSheet.put(key,(LinkedHashMap<Integer, List<DataRow>>)obj);
		}
	}

	public void putHeadMap(String key ,List<String> headList){
		if(key != null){
			headMap.put(key,headList);
		}
	}

	public List<String> getHeadList(String key){
		if(headMap.containsKey(key)){
			return headMap.get(key);
		}
		return null;
	}
}
