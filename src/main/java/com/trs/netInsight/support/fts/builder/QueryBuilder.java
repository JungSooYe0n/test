package com.trs.netInsight.support.fts.builder;

import static com.trs.netInsight.util.StringUtil.isChinese;
import static com.trs.netInsight.util.StringUtil.isEmpty;
import static com.trs.netInsight.util.StringUtil.isNotEmpty;
import static com.trs.netInsight.util.StringUtil.join;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.builder.condition.SearchCondition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 全文检索条件构造器
 *
 * @author 拓尔思信息技术股份有限公司
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class QueryBuilder implements Cloneable{

	private String groupName;

	private String database;

	private Date startTime;
	private Date endTime;

	private List<SearchCondition> conditions = new ArrayList<>();

	private SearchCondition childSearchCondition = new SearchCondition();

	protected StringBuilder appendTRSL = new StringBuilder();
	
	private boolean server ;//是否转换为server表达式

	private String DataDateAndDataSources ;//是否有时间限制
	
	private String keyRedis;//把表达式存入redis

	/**
	 * 排序表达式
	 */
	protected String orderBy;

	/**
	 * 分页号
	 */
	protected long pageNo;

	/**
	 * 分页长
	 */
	protected int pageSize;

	public QueryBuilder(long pageNo, int pageSize) {
		super();
		this.pageNo = pageNo;
		this.pageSize = pageSize;
	}
	


	/**
	 * 添加检索条件,忽略重复的条件
	 *
	 * @param field
	 *            字段
	 * @param operator
	 *            Operator
	 * @param value
	 *            值
	 */
	private void addCondition(String field, Operator operator, String value) {
		SearchCondition c = new SearchCondition(field, value, operator);
		addCondition(c);
	}

	/**
	 * 添加检索条件,忽略重复的条件
	 *
	 * @param field
	 *            字段
	 * @param operator
	 *            Operator
	 * @param values
	 *            值
	 */
	private void addCondition(String field, Operator operator, String[] values) {
		SearchCondition c = new SearchCondition(field, values, operator);
		addCondition(c);
	}

	/**
	 * 添加检索条件,忽略重复的条件
	 *
	 * @param c
	 *            SearchCondition
	 */
	private void addCondition(SearchCondition c) {
		if (!conditions.contains(c)) {
			conditions.add(c);
		}
	}

	public String asTRSL() {
		Collections.sort(conditions);
		String trsl = join(conditions.toArray(), " AND ");
		String result = isEmpty(trsl) ? appendTRSL.toString()
				: appendTRSL.length() > 0 ? trsl + " AND " + appendTRSL.toString() : trsl;
//		if (log.isDebugEnabled()) {
//			log.debug("trs search build result : [" + result + "]");
//		}
//		return fmtTrsl(result);
		return result;
	}

	public QueryBuilder filterField(String fieldName, String fieldValue, Operator op) {
		if (!isEmpty(fieldName) && !isEmpty(fieldValue)) {
			addCondition(fieldName, op, fieldValue);
		}
		return this;
	}

	public QueryBuilder filterField(String fieldName, String[] fieldValues, Operator op) {
		if (!isEmpty(fieldName) && fieldValues != null && fieldValues.length > 0) {
			addCondition(fieldName, op, fieldValues);
		}
		return this;
	}

	public QueryBuilder filterFieldsByWeight(LinkedHashMap<String, Integer> fieldsWeightMap, String keyword,
			boolean escape) {
		StringBuilder buffer = new StringBuilder();
		if (fieldsWeightMap != null && fieldsWeightMap.size() > 0 && isNotEmpty(keyword)) {
			List<Map.Entry<String, Integer>> entrys = new ArrayList<>();
			entrys.addAll(fieldsWeightMap.entrySet());
			for (int i = 0; i < entrys.size(); i++) {
				buffer.append("(").append(entrys.get(i).getKey()).append(":(").append(keyword).append(")^")
						.append(entrys.get(i).getValue()).append(")");
				if (i != entrys.size() - 1) {
					buffer.append(" OR ");
				}
			}
			this.filterByTRSL(buffer.toString());
		}
		return this;
	}

	public QueryBuilder filterByTRSL(String trsl) {
		if (isEmpty(trsl)) {
			return this;
		}
		if (appendTRSL.length() > 0) {
			appendTRSL.append(" AND ");
		}
		this.appendTRSL.append("(").append(trsl).append(")");
		return this;
	}

	public QueryBuilder filterByTRSL_NOT(String trsl) {
		if (isEmpty(trsl)) {
			return this;
		}
		if (appendTRSL.length() > 0) {
			appendTRSL.append(" NOT ");
		}
		this.appendTRSL.append("(").append(trsl).append(")");
		return this;
	}

	public QueryBuilder filterChildField(String fieldName, String fieldValue, Operator op) {
		if (!isEmpty(fieldName)) {
			addCondition(childSearchCondition.addChildCondition(new SearchCondition(fieldName, fieldValue, op)));
		}
		return this;
	}

	public QueryBuilder orderBy(String field, boolean desc) {
		if (!isEmpty(field)) {
			this.orderBy = (desc ? "-" : "+") + field;
		}
		return this;
	}

	public QueryBuilder page(long pageNo, int pageSize) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		return this;
	}

	/**
	 * 表达式加引号
	 *
	 * @param trsl
	 *            表达式
	 */
	public static String fmtTrsl(String trsl) {
		StringBuilder builder = new StringBuilder();
		char[] c = ("#" + trsl + "#").toCharArray();
		boolean f = true;
		out: for (int i = 0; i < c.length; i++) {
			builder.append(c[i]);
			// 遇到引号切换遍历状态
			if (c[i] == '"') {
				f = !f;
				continue;
			}
			// 发现其他字符到汉字转换
			if (f && isChinese(c[i]) && !isChinese(c[i - 1])) {
				for (int j = 1; j <= i; j++) {
					char a = c[i - j];
					if (a == ':' || a == '(' || a == ' ' || a == '#') {
						builder.insert(builder.length() - j, "\"");
						continue out;
					}
				}
			}
			// 发现汉字到其他字符转换
			if (i > 1 && f && !isChinese(c[i]) && isChinese(c[i - 1])) {
				for (int j = 0; j < c.length - i; j++) {
					char a = c[i];
					if (a == ':' || a == ')' || a == ' ' || a == '#') {
						builder.insert(builder.length() - 1, "\"");
						continue out;
					}
					builder.append(c[i + 1]);
					i++;
				}
			}
		}
		// 由于地域CATALOG_AREA:中国\\河北*需要转义
		if (builder.toString().contains("*")) {
			String builderString = "";
			String[] builderSplit = builder.toString().split("OR");
			for (int b = 0; b < builderSplit.length; b++) {
				int z = builderSplit[b].indexOf("*");
				if (builderSplit[b].contains("*")) {
					if ("\"".equals("" + builderSplit[b].charAt(z + 1))) {
						builder.deleteCharAt(z + 1);
						StringBuffer split = new StringBuffer(builderSplit[b]);
						split.deleteCharAt(z + 1);
						builderSplit[b] = split.toString();
					}

				}
				if (builderSplit[b].contains("中国")) {
					int i = builderSplit[b].indexOf("中国");
					StringBuffer split = new StringBuffer(builderSplit[b]);
					if ("\"".equals("" + builderSplit[b].charAt(i - 1))) {
						split.deleteCharAt(i - 1);
					}
					builderSplit[b] = split.toString();
				}
				if (b != builderSplit.length - 1) {
					builderString += builderSplit[b] + " OR ";
				} else {
					builderString += builderSplit[b];
				}
			}
			StringBuilder result = new StringBuilder(builderString);
			// 对于首页地域 为了防止应为"问题导致sql表达式拼写错误 start
			int china = result.indexOf("中国");
			boolean equals1 = "\"".equals(String.valueOf(result.charAt(china - 1)));
			boolean equals2 = "\"".equals(String.valueOf(result.charAt(result.lastIndexOf("*") + 1)));
			if (!(equals1 && equals2) || !(!equals1 && !equals2)) {
				if (equals1) {
					return result.deleteCharAt(china - 1).deleteCharAt(result.length() - 1).deleteCharAt(0).toString();
				} else if (equals2) {
					return result.deleteCharAt(result.lastIndexOf("*") + 1).deleteCharAt(builderString.length() - 1)
							.deleteCharAt(0).toString();
				}
			}
			// 对于首页地域 为了防止应为"问题导致sql表达式拼写错误 end
			return result.deleteCharAt(builderString.length() - 1).deleteCharAt(0).toString();
		}
		return builder.deleteCharAt(builder.length() - 1).deleteCharAt(0).toString();

	}

}
