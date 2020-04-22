package com.trs.netInsight.support.fts.util;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.util.RegexUtils;
import com.trs.netInsight.util.TrsArrayUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrslUtil {
	private final static String FIELD_DATE = "((?:IR_URLDATE|IR_CREATED_DATE))"; // 日期字段
	private final static String FIELD_DATETIME = "((?:IR_URLTIME|IR_CREATED_AT))";// 发布时间字段
	private final static String COLON = "(:)"; // 冒号
	private final static String INTERVAL_L = "(\\[|\\{)"; // [ or {
	private final static String DATE = "((?:(?:[1]\\d{3})|(?:[2]\\d{3}))[-:/.](?:[0]?[1-9]|[1][012])[-:/.](?:(?:[0-2]?\\d)|(?:[3][01])))(?![\\d])";
	private final static String SPACES = "( )"; // 空格
	private final static String TO = "((?:TO))"; // TO
	private final static String INTERVAL_R = "(]|})"; // ] or }
	private final static String TIME = "((?:(?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):(?:[0-5][0-9])(?::[0-5][0-9])?(?:\\s?(?:am|AM|pm|PM))?)";
	private final static String QUOTATION = "(\")";// 引号

	private final static String ALL_NUMBER = "([0-9]{14})";

	//这个正则表达式不能识别出来2月份
	//private final static String URLTIME = "([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})([-:\\\\/.])?(((0[13578]|1[02])([-:\\\\/.])?(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)([-:\\\\/.])?(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))(\\s)?((?:([0-1][0-9])|([2][0-3])|([0-9]))[:\\s]?(?:[0-5][0-9])[:\\s]?(?:[0-5][0-9]))";
	private final static String URLTIME = "\\d{4}([0][1-9]|(1[0-2]))([1-9]|([012]\\d)|(3[01]))(([0-1]{1}[0-9]{1})|([2]{1}[0-4]{1}))(([0-5]{1}[0-9]{1}|[6]{1}[0]{1}))((([0-5]{1}[0-9]{1}|[6]{1}[0]{1})))";

	private final static String PATTERN_DATE = FIELD_DATE + COLON + INTERVAL_L + DATE + SPACES + TO + SPACES + DATE
			+ INTERVAL_R;
	private final static String PATTERN_DATETIME = FIELD_DATETIME + COLON + INTERVAL_L + QUOTATION + DATE + SPACES
			+ TIME + QUOTATION + SPACES + TO + SPACES + QUOTATION + DATE + SPACES + TIME + QUOTATION + INTERVAL_R;
	private final static String PATTERN_TIME = FIELD_DATETIME + COLON + INTERVAL_L + ALL_NUMBER + SPACES + TO + SPACES
			+ ALL_NUMBER + INTERVAL_R;

	public static Date[] getDateRange(String trsl) {
		Pattern p = Pattern.compile(PATTERN_DATE, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(trsl);
		if (m.find()) {
			return new Date[] { DateUtil.stringToDate(m.group(4).replaceAll("[-:./]", "-"), "yyyy-MM"),
					DateUtil.stringToDate(m.group(8).replaceAll("[-:./]", "-"), "yyyy-MM") };
		} else {
			p = Pattern.compile(PATTERN_DATETIME, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			m = p.matcher(trsl);
			if (m.find()) {
				return new Date[] {
						DateUtil.stringToDate(m.group(5) + m.group(6) + m.group(7),
								Math.min(m.group(7).split(";").length, m.group(15).split(";").length) > 2
										? "yyyy.MM.dd HH:mm:ss" : "yyyy.MM.dd HH:mm"),
						DateUtil.stringToDate(m.group(13) + m.group(14) + m.group(15),
								Math.min(m.group(7).split(";").length, m.group(15).split(";").length) > 2
										? "yyyy.MM.dd HH:mm:ss" : "yyyy.MM.dd HH:mm") };
			} else {
				p = Pattern.compile(PATTERN_TIME, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				m = p.matcher(trsl);
				if (m.find()) {
					return new Date[] { DateUtil.stringToDate(m.group(4), "yyyyMMddHHmmss"),
							DateUtil.stringToDate(m.group(8), "yyyyMMddHHmmss") };
				}
			}
		}
		return null;
	}

	/**
	 * 判断字段是否包含日期格式数据
	 *
	 * @param str
	 *            要判定的字符串
	 * @return boolean
	 */
	public static boolean isDate(String str) {
		Pattern p = Pattern.compile(DATE, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(str);
		return m.find();
	}

	/**
	 * 根据来源数组选择需要查询的hybase库
	 * 
	 * @param groupNames
	 * @return
	 */
	public static String[] chooseDatabases(String[] groupNames) {
		int count = 1;
		String[] databases = null;
		String[] data = null;
		if (TrsArrayUtil.compileArray(Const.MEDIA_TYPE_NEWS.toArray(), groupNames)) {
			data = new String[1];
			data[0] = Const.HYBASE_NI_INDEX;
			count++;
		}
		if (TrsArrayUtil.compileArray(Const.MEDIA_TYPE_WEIBO.toArray(), groupNames)) {
			if (count == 2) {
				databases = new String[count];
				databases[0] = data[0];
				databases[1] = Const.WEIBO;
				data = databases;
			} else {
				data = new String[count];
				data[0] = Const.WEIBO;
			}
			count++;
		}
		if (TrsArrayUtil.compileArray(Const.MEDIA_TYPE_WEIXIN.toArray(), groupNames)) {
			if (count == 2) {
				databases = new String[count];
				databases[0] = data[0];
				databases[1] = Const.WECHAT_COMMON;
				data = databases;
			} else if (count == 3) {
				databases = new String[count];
				databases[0] = data[0];
				databases[1] = data[1];
				databases[2] = Const.WECHAT_COMMON;
				data = databases;
			} else {
				data = new String[count];
				data[0] = Const.WECHAT_COMMON;
			}
			count++;
		}
		if (TrsArrayUtil.compileArray(Const.MEDIA_TYPE_TF.toArray(), groupNames)) {
			if (count == 2) {
				databases = new String[count];
				databases[0] = data[0];
				databases[1] = Const.HYBASE_OVERSEAS;
				data = databases;
			} else if (count == 3) {
				databases = new String[count];
				databases[0] = data[0];
				databases[1] = data[1];
				databases[2] = Const.HYBASE_OVERSEAS;
				data = databases;
			} else if (count == 4) {
				databases = new String[count];
				databases[0] = data[0];
				databases[1] = data[1];
				databases[2] = data[2];
				databases[3] = Const.HYBASE_OVERSEAS;
				data = databases;
			} else {
				data = new String[count];
				data[0] = Const.HYBASE_OVERSEAS;
			}
		}

		return data;
	}

	/**
	 * 根据单groupName选择需要查询的hybase库
	 * 
	 * @param groupName
	 * @return
	 */
	public static String chooseDatabases(String groupName) {
		String database = Const.HYBASE_NI_INDEX;
		if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
			database = Const.WEIBO;
		} else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
			database = Const.WECHAT;
		} else if (Const.MEDIA_TYPE_TF.contains(groupName)) {
			database = Const.HYBASE_OVERSEAS;
		}

		return database;
	}

	/**
	 * @Desc : 根据栏目类型选择数据源
	 * @since changjiang @ 2018年4月8日
	 * @param types
	 * @return
	 * @Return : String[]
	 */
	public static String[] chooseDatabaseByIndexType(String[] types) {
		List<String> list = new ArrayList<>();
		List<String> asList = Arrays.asList(types);
		if (types.length == 5) {
			return new String[] { Const.HYBASE_NI_INDEX, Const.WEIBO, Const.WECHAT, Const.HYBASE_OVERSEAS };
		} else {
			// 防止推特 facebook同时存在时数据库重复查询错误
			if (asList.contains(ColumnConst.LIST_STATUS_COMMON)) {
				list.add(Const.WEIBO);
			}
			if (asList.contains(ColumnConst.LIST_WECHAT_COMMON)) {
				list.add(Const.WECHAT);
			}
			if (asList.contains(ColumnConst.LIST_TWITTER) || asList.contains(ColumnConst.LIST_FaceBook)) {
				list.add(Const.HYBASE_OVERSEAS);
			}
			if (asList.contains(ColumnConst.LIST_NO_SIM)) {
				list.add(Const.HYBASE_NI_INDEX);
			}
			return list.toArray(new String[list.size()]);
		}
	}
	// public static String[] chooseDatabaseByIndexType(String[] types) {
	// String[] databases = new String[types.length];
	// if (types.length == 5) {
	// return new String[] { Const.HYBASE_NI_INDEX, Const.WEIBO, Const.WECHAT,
	// Const.HYBASE_OVERSEAS };
	// }else if(types.length == 4){
	// databases[0] = chooseDatabaseByIndexType(types[0]);
	// databases[1] = chooseDatabaseByIndexType(types[1]);
	// databases[2] = chooseDatabaseByIndexType(types[2]);
	// databases[3] = chooseDatabaseByIndexType(types[3]);
	// }else if(types.length == 3){
	// databases[0] = chooseDatabaseByIndexType(types[0]);
	// databases[1] = chooseDatabaseByIndexType(types[1]);
	// databases[2] = chooseDatabaseByIndexType(types[2]);
	// }else if(types.length == 2){
	// databases[0] = chooseDatabaseByIndexType(types[0]);
	// databases[1] = chooseDatabaseByIndexType(types[1]);
	// }else if(types.length == 1){
	// databases[0] = chooseDatabaseByIndexType(types[0]);
	// }
	// return databases;
	//
	// }

	/**
	 * @Desc : 根据栏目类型选择对应数据源
	 * @since changjiang @ 2018年4月8日
	 * @param type
	 * @return
	 * @Return : String
	 */
	public static String chooseDatabaseByIndexType(String type) {
		String database = Const.HYBASE_NI_INDEX;
		if (type.equals(ColumnConst.LIST_STATUS_COMMON)) {
			database = Const.WEIBO;
		} else if (type.equals(ColumnConst.LIST_WECHAT_COMMON)) {
			database = Const.WECHAT;
		} else if (type.equals(ColumnConst.LIST_TWITTER) || type.equals(ColumnConst.LIST_FaceBook)) {
			database = Const.HYBASE_OVERSEAS;
		}
		return database;
	}

	/**
	 * 正则一部分表达式,从中抽取时间
	 * 
	 * @param trsl
	 * @return
	 */
	public static String getTimeRangeByTrsl(String trsl) {
		String timeRange = "";

		// 表达式中出现且仅出现一次IR_URLTIME,表示正常逻辑
		if (trsl.contains(FtsFieldConst.FIELD_URLTIME) && trsl.indexOf(FtsFieldConst.FIELD_URLTIME) == trsl.lastIndexOf(FtsFieldConst.FIELD_URLTIME)) {
			Pattern p = Pattern.compile(URLTIME);
			Matcher m = p.matcher(trsl);
			while (m.find()) {
				timeRange += m.group() + ";";
			}
		}

		return timeRange;
	}
	/**
	 * 正则一部分表达式,从中抽取时间  转为IR_LOADTIME字段
	 *
	 * @param trsl
	 * @return
	 */
	public static String getLoadTimeRangeByTrsl(String trsl) {
		String timeRange = "";

		// 表达式中出现且仅出现一次IR_URLTIME,表示正常逻辑
		if (trsl.contains(FtsFieldConst.FIELD_LOADTIME) && trsl.indexOf(FtsFieldConst.FIELD_LOADTIME) == trsl.lastIndexOf(FtsFieldConst.FIELD_LOADTIME)) {
			Pattern p = Pattern.compile(URLTIME);
			Matcher m = p.matcher(trsl);
			while (m.find()) {
				timeRange += m.group() + ";";
			}
		}

		return timeRange;
	}

	/**
	 * 正则一部分表达式,从中抽取时间  转为IR_LOADTIME字段
	 *
	 * @param trsl
	 * @return
	 */
	public static String getHybaseTimeRangeByTrsl(String trsl) {
		String timeRange = "";

		// 表达式中出现且仅出现一次IR_URLTIME,表示正常逻辑
		if (trsl.contains(FtsFieldConst.FIELD_HYLOAD_TIME) && trsl.indexOf(FtsFieldConst.FIELD_HYLOAD_TIME) == trsl.lastIndexOf(FtsFieldConst.FIELD_HYLOAD_TIME)) {
			Pattern p = Pattern.compile(URLTIME);
			Matcher m = p.matcher(trsl);
			while (m.find()) {
				timeRange += m.group() + ";";
			}
		}

		return timeRange;
	}
	/**
	 * 根据表达式获取选择的数据库
	 * 
	 * @date Created at 2018年11月9日 上午10:01:23
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param trsl
	 * @return
	 */
	public static String[] chooseDatabasesBySql(String trsl) {
		return chooseDatabases(getGroupNameByTrsl(trsl));
	}

	/**
	 * 获取整体的表达式
	 * <p>
	 * 该正则基于trs表达式正确的情况使用，只针对(IR_GROUPNAME:微博)和(IR_GROUPNAME:(微博)),微博可以为多个来源
	 * </p>
	 * 
	 * @date Created at 2018年11月8日 下午5:07:05
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param trsl
	 * @return
	 */
	public static List<String> getGroupNamesByGroupNameTrsl(String trsl) {
		String regex = "((?:"+FtsFieldConst.FIELD_GROUPNAME+"))(:)?:(\\(|.)(.*?)(?:\\)|.)(?:\\))";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(trsl);
		List<String> list = new ArrayList<>();

		while (m.find()) {
			String group = m.group();
			if (StringUtils.isNotBlank(group)) {
				group = group.trim();
				if (!RegexUtils.isMatch(group)) {
					int i = group.lastIndexOf(")");
					group = group.substring(0, i) + group.substring(i + 1);
				}
			}
			list.add(group);
		}
		return list;

	}

	/**
	 * 根据表达式获取里面的来源
	 * 
	 * @date Created at 2018年11月8日 下午4:29:19
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param trsl
	 * @return
	 */
	public static String[] getGroupNameByTrsl(String trsl) {
		List<String> list = getGroupNamesByGroupNameTrsl(trsl);
		Set<String> groupNameSets = new HashSet<>();
		if (list != null && list.size() > 0) {
			for (String groupNameStr : list) {
				groupNameStr=groupNameStr.replace(FtsFieldConst.FIELD_GROUPNAME, "").replace("(", "").replace(")", "").replace(":", "");
				if (StringUtils.isNotBlank(groupNameStr)) {
					String[] groupNames = groupNameStr.split("OR");
					for (int i = 0; i < groupNames.length; i++) {
						groupNameSets.add(groupNames[i].trim());
					}
				}
			}
		}
		return groupNameSets.toArray(new String[] {});
	}


	public static String removeSimflag(String trslk) {
		if(trslk.indexOf("AND SIMFLAG:(1000 OR \"\")") != -1){
			trslk = trslk.replace(" AND SIMFLAG:(1000 OR \"\")","");
		}else if (trslk.indexOf("AND (IR_SIMFLAGALL:(\"0\" OR \"\"))") != -1){
			trslk = trslk.replace(" AND (IR_SIMFLAGALL:(\"0\" OR \"\"))","");
		}
		trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)","");
		return trslk;
	}


}
