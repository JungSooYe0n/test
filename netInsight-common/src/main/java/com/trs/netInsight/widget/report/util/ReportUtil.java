package com.trs.netInsight.widget.report.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.SpringUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.report.constant.*;
import com.trs.netInsight.widget.report.entity.ReportDataNew;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.TElementNew;
import com.trs.netInsight.widget.report.entity.TemplateNew;
import com.trs.netInsight.widget.report.entity.repository.ReportResourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;

/***
 * Created by shao.guangze on 2018/6/27
 */
@Slf4j
public class ReportUtil {

	private static final String startSpan = "<span>";
	private static final String startFontSpan = "<span class=\"color\">";
	private static final String endSpan = "</span>";


	// 生成报告后需在页面上二次编辑，样式代码会原样显示在页面上，现决定将其去掉
	private static final String SPANCOLORLEFT = "";
	private static final String SPANCOLORRIGHT = "";

	public static String getImgComment(String img_data, String imgType, String chapter) {
		if ("brokenLineChart".equals(imgType)) {
			return getLineCommentNew(img_data);
		} else if ("pieGraphChartMeta".equals(imgType) || ColumnConst.CHART_PIE.equals(imgType) || "opinionStatistics".equals(imgType)) {
			return String.format(getbarComment(img_data), chapter);
		} else if ("barGraphChartMeta".equals(imgType) || ColumnConst.CHART_BAR.equals(imgType)) {
			return String.format(getbarComment(img_data), chapter);
		} else if ("mapChart".equals(imgType) || ColumnConst.CHART_MAP.equals(imgType)) {
			return getMapComment(img_data);
		} else if (ColumnConst.CHART_PIE_EMOTION.equals(imgType) || "moodStatistics".equals(imgType)) {
			return String.format(getEmotion(img_data), chapter);
		} else if ("gaugeChart".equals(imgType)) {
			return getMapComment(img_data);
		} else if ("wordCloudChart".equals(imgType)) {
			return String.format(getbarComment(img_data), chapter);
		} else if (ColumnConst.CHART_BAR_CROSS.equals(imgType)) {
			List<String> strResult1 = new ArrayList<>();
			List<Map<String, Object>> sumList = JSONObject.parseObject(img_data,
					new TypeReference<List<Map<String, Object>>>() {
					});
			for(int i=0;i<sumList.size();i++){
				Map<String, Object> fieldMap = sumList.get(i);
				List<Map<String, Object>> sumListNew = (List<Map<String, Object>>) fieldMap.get("info");
				strResult1.add(String.format(getbarComment(sumListNew.toString()),chapter));
			}
			return JSON.toJSONString(strResult1);
		} else {
			log.info("没有匹配到对应的图片类型 - " + imgType);
		}
		return null;
	}

	/**
	 * 数据统计概述图片处理 将图片的jsonData中的data转变为文字描述 柱状图、饼图、折线图
	 *
	 * @param data
	 * @return
	 * @author shao.guangze
	 */
	@SuppressWarnings("rawtypes")
	public static String getOverviewOfData(String data) {
		// 向 数据统计概述 中添加数据
		if (StringUtils.isEmpty(data)) {
			return null;
		}
		String str1 = "";
		String str2 = "";
		StringBuffer strResult = new StringBuffer();
		Object parse = JSONArray.parse(data);
		List list = (List) parse;
		if (list.size() > 0) {
			if (((JSONObject) list.get(0)).getInteger("value") == null) {
				// 进入折线图模式
				return lineOverviewOfData(data);
			}
		}

		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			JSONObject jObj = (JSONObject) list.get(i);
			arrayList.add(jObj.getInteger("value"));
		}
		Collections.sort(arrayList);
		IntSummaryStatistics collect = arrayList.stream().collect(Collectors.summarizingInt(value -> value));
		strResult.append("监测主题相关信息" + collect.getSum() + "条,其中");
		NumberFormat numberFormat = NumberFormat.getPercentInstance();
		numberFormat.setMinimumFractionDigits(2);
		for (int i = 0; i < list.size(); i++) {
			JSONObject jObj = (JSONObject) list.get(i);
			strResult.append(jObj.getString("name") + "信息" + jObj.getInteger("value") + "条，信息占比"
					+ numberFormat.format(jObj.getInteger("value").longValue() * 1.0 / collect.getSum()) + ",");
			if (arrayList.get(arrayList.size() - 1).equals(jObj.getInteger("value"))) {
				str1 = jObj.getString("name");
			} else if (jObj.getInteger("value").equals(arrayList.get(arrayList.size() - 2))) {
				str2 = jObj.getString("name");
			}
		}
		strResult.append("由此可见，" + str1 + "报道数据量最多，" + "其次为" + str2 + "数据。");
		return strResult.toString();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static String lineOverviewOfData(String data) {
		List<Map> parseArray = JSONArray.parseArray(data, Map.class);
		List<Long> arrayList = new ArrayList<Long>();
		// 第一次循环把每一条折线放到arrayList中，必须要取到所有折线的count才能开始拼字符串
		for (Map map : parseArray) {
			List<Map> list = (List<Map>) map.get("data");
			long sum = list.stream().map(e -> JSONObject.parseObject(e.toString(), GroupInfo.class).getCount())
					.collect(Collectors.summarizingLong(value -> value)).getSum();
			arrayList.add(sum);
		}
		long allCount = arrayList.stream().collect(Collectors.summarizingLong(value -> value)).getSum();
		arrayList = arrayList.stream().sorted().collect(Collectors.toList());
		StringBuffer strResult = new StringBuffer();
		NumberFormat numberFormat = NumberFormat.getPercentInstance();
		numberFormat.setMinimumFractionDigits(2);
		String str1 = null;
		String str2 = null;
		strResult.append("监测主题相关信息" + allCount + "条,其中");
		// 第二次循环开始拼字符串
		for (Map map : parseArray) {
			List<Map> list = (List<Map>) map.get("data");
			long sum = list.stream().map(e -> JSONObject.parseObject(e.toString(), GroupInfo.class).getCount())
					.collect(Collectors.summingLong(value -> value));
			strResult.append(map.get("groupName")).append(sum).append("条，信息占比")
					.append(numberFormat.format(sum * 1.0 / allCount)).append(",");
			if (arrayList.get(arrayList.size() - 1).equals(sum)) {
				str1 = map.get("groupName").toString();
			} else if (arrayList.get(arrayList.size() - 2).equals(sum)) {
				str2 = map.get("groupName").toString();
			}
		}
		strResult.append("由此可见，").append(str1).append("数据报道量最多，其次为").append(str2).append("数据。");
		return strResult.toString();
	}

	/**
	 * 日常监测：折线图普通模式、专家模式
	 *
	 * @author shao.guangze
	 */
	@SuppressWarnings("unchecked")
	private static String getLineCommentNew(String imgData) {

		if (StringUtil.isEmpty(imgData)) {
			return "";
		}
		JSONObject object = JSONObject.parseObject(imgData);

		JSONObject single = object.getJSONObject("single");

		JSONArray groupNameArr = single.getJSONArray("legendData");
		JSONArray countArr = single.getJSONArray("lineYdata");
		if (groupNameArr == null || groupNameArr.size() == 0) {
			return "";
		}
		List<Map<String, Object>> sumList = new ArrayList<>();

		for (int i = 1; i < groupNameArr.size(); i++) {
			String oneGroupName = groupNameArr.getString(i);
			List<Integer> countList = (List<Integer>) countArr.get(i);
			Integer sum = countList.stream().reduce(Integer::sum).orElse(0);
			HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("name", oneGroupName);
			hashMap.put("count", sum);
			sumList.add(hashMap);
		}

		Collections.sort(sumList, new Comparator<Map<String, Object>>() {

			@Override
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				Object count1 = m1.get("count");
				Object count2 = m2.get("count");
				if ((Integer) count1 == (Integer) count2) {
					return 0;
				} else {
					return (Integer) count1 > (Integer) count2 ? 1 : -1;
				}
			}

		});

		removeTotalInLine(sumList);
		List<String> headStr = new ArrayList<>();
		List<String> secStr = new ArrayList<>();
		List<Object> headCount = new ArrayList<>();
		List<Object> secCount = new ArrayList<>();
		// 最大的数
		Object countSum = sumList.get(sumList.size() - 1).get("count");
		Object countSec = null;
		// 获取 "首位"
		headStr.add(SPANCOLORLEFT + sumList.get(sumList.size() - 1).get("name") + SPANCOLORRIGHT);
		headCount.add(SPANCOLORLEFT + sumList.get(sumList.size() - 1).get("count") + SPANCOLORRIGHT);
		for (int i = sumList.size() - 2; i > -1; i--) {
			if (sumList.get(i).get("count").equals(countSum)) {
				// 此时说明 "首位数据有重复"
				headStr.add("和" + SPANCOLORLEFT + sumList.get(i).get("name") + SPANCOLORRIGHT);
				headCount.add(SPANCOLORLEFT + sumList.get(i).get("count") + SPANCOLORRIGHT);
			} else if ((Integer) (sumList.get(i).get("count")) < (Integer) (countSum)) {
				// 此时说明拥有 "次位" 数
				if (secStr.size() == 0) {
					secStr.add(SPANCOLORLEFT + sumList.get(i).get("name").toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get("count"));
					// 记录 "次位" count
					countSec = sumList.get(i).get("count");
				} else if (secStr.size() < 2) {
					secStr.add("和" + SPANCOLORLEFT + sumList.get(i).get("name").toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get("count"));
				} else if (sumList.get(i).get("count").equals(countSec)) {
					// >= 3 的情况
					secStr.add("和" + SPANCOLORLEFT + sumList.get(i).get("name").toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get("count"));
				}
			}
		}
		StringBuffer strResult = new StringBuffer();
		strResult.append("由图可知，传播数量居首位的是");
		for (String str : headStr) {
			strResult.append(str);
		}
		strResult.append("，为");
		strResult.append(headCount.get(0));
		if (secStr.size() > 0) {
			strResult.append("篇，其次为");
			if (secStr.size() > 2) {
				secStr = secStr.subList(0, 2);
			}
			for (String str : secStr) {
				strResult.append(str);
			}
		}
		strResult.append("。");
		return strResult.toString();
	}


	/**
	 * 日常监测：折线图普通模式、专家模式
	 *
	 * @author shao.guangze
	 */
	@SuppressWarnings("unchecked")
	private static String getLineComment(String imgData) {
		List<Map<String, Object>> parseArray = JSONObject.parseObject(imgData,
				new TypeReference<List<Map<String, Object>>>() {
				});
		if (parseArray.size() == 0) {
			return "";
		}
		List<Map<String, Object>> sumList = new ArrayList<>();
		for (Map<String, Object> map : parseArray) {
			List<Object> list = (List<Object>) map.get("data");
			// 这里的getCount是get GroupInfo 对象而不是stream
			Long sum = list.stream().map(e -> JSONObject.parseObject(e.toString(), GroupInfo.class).getCount())
					.collect(Collectors.summingLong(value -> value));
			HashMap<String, Object> hashMap = new HashMap<>();
			hashMap.put("groupName", map.get("groupName"));
			hashMap.put("count", sum);
			sumList.add(hashMap);
		}
		Collections.sort(sumList, new Comparator<Map<String, Object>>() {

			@Override
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				Object count1 = m1.get("count");
				Object count2 = m2.get("count");
				if ((Long) count1 == (Long) count2) {
					return 0;
				} else {
					return (Long) count1 > (Long) count2 ? 1 : -1;
				}
			}

		});

		removeTotalInLine(sumList);
		List<String> headStr = new ArrayList<>();
		List<String> secStr = new ArrayList<>();
		List<Object> headCount = new ArrayList<>();
		List<Object> secCount = new ArrayList<>();
		// 最大的数
		Object countSum = sumList.get(sumList.size() - 1).get("count");
		Object countSec = null;
		// 获取 "首位"
		headStr.add(SPANCOLORLEFT + sumList.get(sumList.size() - 1).get("groupName") + SPANCOLORRIGHT);
		headCount.add(SPANCOLORLEFT + sumList.get(sumList.size() - 1).get("count") + SPANCOLORRIGHT);
		for (int i = sumList.size() - 2; i > -1; i--) {
			if (sumList.get(i).get("count").equals(countSum)) {
				// 此时说明 "首位数据有重复"
				headStr.add("和" + SPANCOLORLEFT + sumList.get(i).get("groupName") + SPANCOLORRIGHT);
				headCount.add(SPANCOLORLEFT + sumList.get(i).get("count") + SPANCOLORRIGHT);
			} else if ((Long) (sumList.get(i).get("count")) < (Long) (countSum)) {
				// 此时说明拥有 "次位" 数
				if (secStr.size() == 0) {
					secStr.add(SPANCOLORLEFT + sumList.get(i).get("groupName").toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get("count"));
					// 记录 "次位" count
					countSec = sumList.get(i).get("count");
				} else if (secStr.size() < 2) {
					secStr.add("和" + SPANCOLORLEFT + sumList.get(i).get("groupName").toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get("count"));
				} else if (sumList.get(i).get("count").equals(countSec)) {
					// >= 3 的情况
					secStr.add("和" + SPANCOLORLEFT + sumList.get(i).get("groupName").toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get("count"));
				}
			}
		}
		StringBuffer strResult = new StringBuffer();
		strResult.append("由图可知，传播数量居首位的是");
		for (String str : headStr) {
			strResult.append(str);
		}
		strResult.append("，为");
		strResult.append(headCount.get(0));
		if (secStr.size() > 0) {
			strResult.append("篇，其次为");
			for (String str : secStr) {
				strResult.append(str);
			}
		}
		strResult.append("。");
		return strResult.toString();
	}

	private static void removeTotalInLine(List<Map<String, Object>> sumList) {
		if ("总量".equals(sumList.get(sumList.size() - 1).get("groupName"))) {
			sumList.remove(sumList.size() - 1);
		}
	}

	/**
	 * 日常监测：柱状图； 日常监测：饼图 eg
	 * ：由图可知，微博活跃用户TOP10居首位的是竹里绣生和半夏半半琳，为11篇，其次为永远是你的珍珠糖和_廷哥超a_。
	 * 实现逻辑：记录最大的数和次大的数。前半部分为最大的数的name，不计个数；后半部分为次大的数，
	 * 只有次大的数超过2时，后半部分才有可能显示2个以上，其他情况后半部分只显示2个。
	 *
	 * @return
	 * @author shao.guangze
	 */
	private static String getbarComment(String imgData) {
		// 向 数据统计概述 中添加数据
		if ("\"暂无数据\"".equals(imgData) || "暂无数据".equals(imgData)) {
			return "";
		}
		List<Map<String, Object>> sumList = JSONObject.parseObject(imgData,
				new TypeReference<List<Map<String, Object>>>() {
				});
		if (sumList == null || sumList.size() == 0) {
			return "";
		}
		ChartResultField chartResultField = null;
		Map<String, Object> fieldMap = sumList.get(0);

		if (fieldMap.containsKey("name") && fieldMap.containsKey("value")) {
			chartResultField = new ChartResultField("name", "value");
			Collections.sort(sumList, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					Object count1 = m1.get("value");
					Object count2 = m2.get("value");
					Integer countLeft = count1 instanceof Integer ? (Integer) count1 : Integer.parseInt(count1.toString());
					Integer countRight = count2 instanceof Integer ? (Integer) count2 : Integer.parseInt(count2.toString());
					if (countLeft == countRight) {
						return 0;
					} else {
						return countLeft > countRight ? 1 : -1;
					}
				}
			});
		} else {
			chartResultField = new ChartResultField("groupName", "num");
			Collections.sort(sumList, new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					Object count1 = m1.get("num");
					Object count2 = m2.get("num");
					Integer countLeft = count1 instanceof Integer ? (Integer) count1 : Integer.parseInt(count1.toString());
					Integer countRight = count2 instanceof Integer ? (Integer) count2 : Integer.parseInt(count2.toString());
					if (countLeft == countRight) {
						return 0;
					} else {
						return countLeft > countRight ? 1 : -1;
					}
				}
			});
		}

		List<String> headStr = new ArrayList<String>();
		List<String> secStr = new ArrayList<String>();
		List<Object> headCount = new ArrayList<Object>();
		List<Object> secCount = new ArrayList<Object>();
		// 最大的数
		Integer countSum = sumList.get(sumList.size() - 1).get(chartResultField.getCountField()) instanceof Integer
				? (Integer) (sumList.get(sumList.size() - 1).get(chartResultField.getCountField()))
				: Integer.parseInt(sumList.get(sumList.size() - 1).get(chartResultField.getCountField()).toString());
		Object countSec = null;
		// 获取 "首位"
		headStr.add(SPANCOLORLEFT + sumList.get(sumList.size() - 1).get(chartResultField.getContrastField()) + SPANCOLORRIGHT);
		headCount.add(SPANCOLORLEFT + sumList.get(sumList.size() - 1).get(chartResultField.getCountField()) + SPANCOLORRIGHT);
		for (int i = sumList.size() - 2; i > -1; i--) {
			if (sumList.size() < 2) {
				break;
			}
			Integer num1 = sumList.get(i).get(chartResultField.getCountField()) instanceof Integer ? (Integer) (sumList.get(i).get(chartResultField.getCountField()))
					: Integer.parseInt(sumList.get(i).get(chartResultField.getCountField()).toString());


			if (countSum.equals(num1)) {
				// 此时说明 "首位数据有重复"
				headStr.add("和" + SPANCOLORLEFT + sumList.get(i).get(chartResultField.getContrastField()).toString() + SPANCOLORRIGHT);
				headCount.add(SPANCOLORLEFT + sumList.get(i).get(chartResultField.getCountField()) + SPANCOLORRIGHT);
			} else if (num1 < countSum) {
				// 此时说明拥有 "次位" 数
				if (secStr.size() == 0) {
					secStr.add(SPANCOLORLEFT + sumList.get(i).get(chartResultField.getContrastField()).toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get(chartResultField.getCountField()));
					// 记录 "次位" count
					countSec = sumList.get(i).get(chartResultField.getCountField());
				} else if (secStr.size() < 2) {
					secStr.add("和" + SPANCOLORLEFT + sumList.get(i).get(chartResultField.getContrastField()).toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get(chartResultField.getCountField()));
				} else if (sumList.get(i).get(chartResultField.getCountField()).equals(countSec)) {
					// >= 3 的情况
					secStr.add("和" + SPANCOLORLEFT + sumList.get(i).get(chartResultField.getContrastField()).toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get(chartResultField.getCountField()));
				}
			}
		}
		StringBuffer strResult = new StringBuffer();
		strResult.append("由图可知，%s居首位的是");
		for (String str : headStr) {
			strResult.append(str);
		}
		strResult.append("，为");
		strResult.append(headCount.get(0));
		if (secStr.size() > 0) {
			strResult.append("篇，其次为");
			if (secStr.size() > 2) {
				secStr = secStr.subList(0, 2);
			}
			for (String str : secStr) {
				strResult.append(str);
			}
		}
		strResult.append("。");
		return strResult.toString();
	}

	private static String getEmotion(String imgData) {
		// 向 数据统计概述 中添加数据
		if ("\"暂无数据\"".equals(imgData) || "暂无数据".equals(imgData)) {
			return "";
		}
		List<Map<String, Object>> sumList = JSONObject.parseObject(imgData,
				new TypeReference<List<Map<String, Object>>>() {
				});
		if (sumList == null || sumList.size() == 0) {
			return "";
		}
		ChartResultField chartResultField = new ChartResultField("name", "value");
		Collections.sort(sumList, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				Object count1 = m1.get("value");
				Object count2 = m2.get("value");
				Integer countLeft = count1 instanceof Integer ? (Integer) count1 : Integer.parseInt(count1.toString());
				Integer countRight = count2 instanceof Integer ? (Integer) count2 : Integer.parseInt(count2.toString());
				if (countLeft == countRight) {
					return 0;
				} else {
					return countLeft > countRight ? 1 : -1;
				}
			}
		});

		List<String> headStr = new ArrayList<String>();
		List<String> secStr = new ArrayList<String>();
		List<Object> headCount = new ArrayList<Object>();
		List<Object> secCount = new ArrayList<Object>();
		// 最大的数
		Integer countSum = sumList.get(sumList.size() - 1).get(chartResultField.getCountField()) instanceof Integer
				? (Integer) (sumList.get(sumList.size() - 1).get(chartResultField.getCountField()))
				: Integer.parseInt(sumList.get(sumList.size() - 1).get(chartResultField.getCountField()).toString());
		Object countSec = null;
		// 获取 "首位"
		headStr.add(SPANCOLORLEFT + sumList.get(sumList.size() - 1).get(chartResultField.getContrastField()) + SPANCOLORRIGHT);
		headCount.add(SPANCOLORLEFT + sumList.get(sumList.size() - 1).get(chartResultField.getCountField()) + SPANCOLORRIGHT);
		for (int i = sumList.size() - 2; i > -1; i--) {
			if (sumList.size() < 2) {
				break;
			}
			Integer num1 = sumList.get(i).get(chartResultField.getCountField()) instanceof Integer ? (Integer) (sumList.get(i).get(chartResultField.getCountField()))
					: Integer.parseInt(sumList.get(i).get(chartResultField.getCountField()).toString());


			if (countSum.equals(num1)) {
				// 此时说明 "首位数据有重复"
				headStr.add("和" + SPANCOLORLEFT + sumList.get(i).get(chartResultField.getContrastField()).toString() + SPANCOLORRIGHT);
				headCount.add(SPANCOLORLEFT + sumList.get(i).get(chartResultField.getCountField()) + SPANCOLORRIGHT);
			} else if (num1 < countSum) {
				// 此时说明拥有 "次位" 数
				if (secStr.size() == 0) {
					secStr.add(SPANCOLORLEFT + sumList.get(i).get(chartResultField.getContrastField()).toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get(chartResultField.getCountField()));
					// 记录 "次位" count
					countSec = sumList.get(i).get(chartResultField.getCountField());
				} else if (secStr.size() < 2) {
					secStr.add("和" + SPANCOLORLEFT + sumList.get(i).get(chartResultField.getContrastField()).toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get(chartResultField.getCountField()));
				} else if (sumList.get(i).get(chartResultField.getCountField()).equals(countSec)) {
					// >= 3 的情况
					secStr.add("和" + SPANCOLORLEFT + sumList.get(i).get(chartResultField.getContrastField()).toString() + SPANCOLORRIGHT);
					secCount.add(sumList.get(i).get(chartResultField.getCountField()));
				}
			}
		}
		StringBuffer strResult = new StringBuffer();
		strResult.append("由图可知，情感分析居首位的是");
		for (String str : headStr) {
			strResult.append(str);
		}
		strResult.append("，为");
		strResult.append(headCount.get(0));
		if (secStr.size() > 0) {
			strResult.append("篇，其次为");
			for (String str : secStr) {
				strResult.append(str);
			}
		}
		strResult.append("。");
		return strResult.toString();
	}

	private static String getMapComment(String imgData) {
		// 地图
		List<Map<String, Object>> parseArray = JSONObject.parseObject(imgData,
				new TypeReference<List<Map<String, Object>>>() {
				});

		ChartResultField chartResultField = null;
		Map<String, Object> fieldMap = parseArray.get(0);

		List<Integer> arrayList = new ArrayList<Integer>();

		if(fieldMap.containsKey("name") && fieldMap.containsKey("value")){
			chartResultField = new ChartResultField("name","value");
			arrayList = parseArray.stream().map(e -> Integer.parseInt(e.get("value").toString())).sorted()
					.collect(Collectors.toList());
		}else{
			chartResultField = new ChartResultField("areaName","areaCount");
			arrayList = parseArray.stream().map(e -> Integer.parseInt(e.get("areaCount").toString())).sorted()
					.collect(Collectors.toList());
		}
		// 排序完成
		System.out.println(arrayList);
		String str1 = null;
		String str2 = null;
		String str3 = null;
		String str4 = null;
		String str5 = null;
		if (parseArray.size() > 0) {

			for (Map<String, Object> map : parseArray) {
				if (arrayList.get(arrayList.size() - 1).equals(map.get(chartResultField.getCountField())) && str1 == null) {
					str1 = map.get(chartResultField.getContrastField()).toString();
				} else if (arrayList.get(arrayList.size() - 2).equals(map.get(chartResultField.getCountField())) && str2 == null) {
					str2 = map.get(chartResultField.getContrastField()).toString();
				} else if (arrayList.get(arrayList.size() - 3).equals(map.get(chartResultField.getCountField())) && str3 == null) {
					str3 = map.get(chartResultField.getContrastField()).toString();
				} else if (arrayList.get(arrayList.size() - 4).equals(map.get(chartResultField.getCountField())) && str4 == null) {
					str4 = map.get(chartResultField.getContrastField()).toString();
				} else if (arrayList.get(arrayList.size() - 5).equals(map.get(chartResultField.getCountField())) && str5 == null) {
					str5 = map.get(chartResultField.getContrastField()).toString();
				}
			}
			// 所有数据都一样 1 1 1 1 1 ...
			if (str2 == null) {
				str2 = parseArray.get(0).get(chartResultField.getContrastField()).toString();
				str3 = parseArray.get(0).get(chartResultField.getContrastField()).toString();
				str4 = parseArray.get(0).get(chartResultField.getContrastField()).toString();
				str5 = parseArray.get(0).get(chartResultField.getContrastField()).toString();
			}
			// 2 1 1 1 1 1 ...
			if (str3 == null) {
				str3 = parseArray.get(1).get(chartResultField.getContrastField()).toString();
				str4 = parseArray.get(1).get(chartResultField.getContrastField()).toString();
				str5 = parseArray.get(1).get(chartResultField.getContrastField()).toString();
			}
			// 3 2 1 1 1 1 ...
			if (str4 == null) {
				str4 = parseArray.get(2).get(chartResultField.getContrastField()).toString();
				str5 = parseArray.get(2).get(chartResultField.getContrastField()).toString();
			}
			// 4 3 2 1 1 1 ...
			if (str5 == null) {
				str5 = parseArray.get(3).get(chartResultField.getContrastField()).toString();
			}
			StringBuffer strResult = new StringBuffer();
			strResult.append("由图可知，热点地域分布主要集中于：")
                    .append(str1).append("、").append(str2).append("、").append(str3)
					.append("、").append(str4).append("、").append(str5)
                    .append("，关注度较高的前五个省信息量分别是：")
					.append(arrayList.get(arrayList.size() - 1)).append("、").append(arrayList.get(arrayList.size() - 2))
					.append("、").append(arrayList.get(arrayList.size() - 3)).append("、")
					.append(arrayList.get(arrayList.size() - 4)).append("、").append(arrayList.get(arrayList.size() - 5))
					.append("。");

			return strResult.toString();
		}
		return null;
	}

	/**
	 * 替换部分html标签。没有用StringUtil的原因是&nbsp replace 空格，不能去除。
	 * 不然报告中全英文的一行会导致格式错乱，里面得有空格
	 * 
	 * @author shao.guangze
	 * @param topDatas
	 * @return
	 */
	public static String replaceHtml(String topDatas) {
		if (topDatas == null) {
			return null;
		}
		topDatas = topDatas.replace("&nbsp;", " ");
		topDatas = topDatas.replace("<font color=red>", "");
		topDatas = topDatas.replace("</font>", "");
		topDatas = topDatas.replace("//", " ");
		topDatas = topDatas.replace("&amp;quot;", " ");
		topDatas = topDatas.replace("&quot;", " ");
		topDatas = topDatas.replace("&amp;", " ");
		topDatas = topDatas.replace("&lt;", "<");
		topDatas = topDatas.replace("&gt;", ">");
		return topDatas;
	}

	public static String buildSql(List<String> sidList) {
		if (ObjectUtil.isNotEmpty(sidList)) {
			StringBuilder strb = new StringBuilder();
			strb.append("IR_SID:(");
			sidList.forEach(sid -> {
				strb.append(sid).append(" OR ");
			});
			String trsl = strb.toString();
			trsl = trsl.substring(0, trsl.lastIndexOf("OR") - 1) + ")";
			return trsl;
		} else {
			return null;
		}
	}

	public static String buildSqlWeiXin(List<String> sidList) {
		if (ObjectUtil.isNotEmpty(sidList)) {
			StringBuilder strb = new StringBuilder();
			strb.append("IR_HKEY:(");
			sidList.forEach(sid -> {
				strb.append(sid).append(" OR ");
			});
			String trsl = strb.toString();
			trsl = trsl.substring(0, trsl.lastIndexOf("OR") - 1) + ")";
			return trsl;
		} else {
			return null;
		}
	}

	/**
	 * 对已经创建模板的用户，在这里加1道门槛， 确保页面显示的模块与Enum Chapter中的内容是一样的
	 */
	public static List<TElementNew> tElementListHandle(List<TElementNew> allTElement) {
		List<Chapter> allChapters = Arrays.asList(Chapter.values());
		AtomicInteger equalsFlag = new AtomicInteger(0);
		List<TElementNew> removingElements = new ArrayList<>();
		for (TElementNew tElementNew : allTElement) {
			allChapters.stream().forEach(e -> {
				if (e.getValue().equals(tElementNew.getChapterName())) {
					equalsFlag.set(1);
					return;
				}
			});
			if (equalsFlag.get() == 0) {
				// 此时说明Enum Chapter 中没有有该章节
				removingElements.add(tElementNew);
			}
			equalsFlag.set(0);
		}
		return allTElement.stream().filter(e -> !removingElements.contains(e)).collect(Collectors.toList());
	}

	public static List<TElementNew> setResoucesIntoElements(TemplateNew template,
			Map<?, List<ReportResource>> collect) {
		ArrayList<TElementNew> tElements = new ArrayList<>();
		List<TElementNew> elementList = JSONArray.parseArray(template.getTemplateList(), TElementNew.class);
		// 同上，保证旧版可以正常使用
		boolean flag = false;
		if (collect.keySet().iterator().hasNext()) {
			flag = collect.keySet().iterator().next() instanceof Integer ? true : false;
		}
		boolean finalFlag = flag;
		elementList.stream().forEach(e -> {
			if (finalFlag) {
				if (OVERVIEWOFDATA.equals(e.getChapterName())||Chapter.Statistics_Summarize.toString().equals(e.getChapterDetail())) {
					// 在数据统计概述有两条及以上时，选第一条(最新的一条).
					e.setChapaterContent(collect.get(e.getChapterPosition()) != null
							? Arrays.asList(collect.get(e.getChapterPosition()).get(0)) : null);
				} else {
					// 对每项列表中的信息排序
					List<ReportResource> list = collect.get(e.getChapterPosition());
					if (ObjectUtil.isNotEmpty(list)) {
						if(e.getChapterType().equals(ReportConst.LISTRESOURCES)){//如果是列表，则需要把列表的数据源进行转化
							e.setChapaterContent(sortDoc(list));
						}else{
							e.setChapaterContent(sortDoc(list));
						}
					} else {
						e.setChapaterContent(collect.get(e.getChapterPosition()));
					}

				}
				tElements.add(e);
			} else {
				if (OVERVIEWOFDATA.equals(e.getChapterName())||Chapter.Statistics_Summarize.toString().equals(e.getChapterDetail())) {
					// 在数据统计概述有两条及以上时，选第一条(最新的一条).
					e.setChapaterContent(collect.get(e.getChapterName()) != null
							? Arrays.asList(collect.get(e.getChapterName()).get(0)) : null);
				} else {
					e.setChapaterContent(collect.get(e.getChapterName()));
				}
				tElements.add(e);
			}
		});
		List<TElementNew> result = tElements.stream().sorted(Comparator.comparing(TElementNew::getChapterPosition))
				.collect(Collectors.toList());
		return result;
	}

	/**
	 * 
	 * @param list
	 *            每个模块的doc集合进行排序
	 * @return 排序后的List<ReportResource>
	 */
	private static List<ReportResource> sortDoc(List<ReportResource> list) {
		List<ReportResource> collect = new ArrayList<ReportResource>();
		// 静态注入
		ReportResourceRepository reportResourceRepository = SpringUtil.getBean(ReportResourceRepository.class);
		// 位置字段新加的，为了不删除原报告数据，先对原数据位置字段进行赋值
		for (int i = 0; i < list.size(); i++) {
//			if (list.get(i).getDocPosition() == 0) {
//				list.get(i).setDocPosition(i + 1);
//				reportResourceRepository.save(list.get(i));
//			}
			if(StringUtil.isNotEmpty(list.get(i).getGroupName())){
				list.get(i).setGroupName(Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(list.get(i).getGroupName()));
			}
		}
		collect = list.stream().sorted(Comparator.comparing(ReportResource::getDocPosition))
				.collect(Collectors.toList());
		return collect;
	}

	public static String statisticsTimeHandle(String timeRange) throws ParseException {
		Calendar cal = Calendar.getInstance();
		if ("24h".equals(timeRange)) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
			String yesterday = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 0);
			String today = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			return yesterday + " - " + today;
		} else if ("0d".equals(timeRange)) {
			cal.add(Calendar.DAY_OF_MONTH, 0);
			String yesterday = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 0);
			String today = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			return yesterday.substring(0, 11) + "00时" + " - " + today;
		} else if ("3d".equals(timeRange)) {
			cal.add(Calendar.DAY_OF_MONTH, -3);
			String yesterday = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 0);
			String today = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			return yesterday + " - " + today;
		} else if ("7d".equals(timeRange)) {
			cal.add(Calendar.DAY_OF_MONTH, -7);
			String yesterday = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 0);
			String today = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			return yesterday + " - " + today;
		} else if ("30d".equals(timeRange)) {
			cal.add(Calendar.DAY_OF_MONTH, -30);
			String yesterday = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 0);
			String today = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
			return yesterday + " - " + today;
		} else {
			// 自定义时间
			Date start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeRange.substring(0, 19));
			String startFormated = new SimpleDateFormat("yyyy年MM月dd日 HH时").format(start);
			Date end;
			String endFormated = "";
			// 中间有1个分号
			try {
				if (timeRange.substring(20).contains("至今")) {
					cal.add(Calendar.DAY_OF_MONTH, 0);
					endFormated = new SimpleDateFormat(STATISTICSTIMEFORMAT).format(cal.getTime());
				} else {
					end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeRange.substring(20));
					endFormated = new SimpleDateFormat("yyyy年MM月dd日 HH时").format(end);
				}
			} catch (Exception e) {
				// 现有的专报允许用户自定义统计时间，日报类也有可能也允许用户自定是统计时间
				return timeRange;
			}
			return startFormated + " - " + endFormated;
		}
	}

	public static List<TElementNew> createEmptyTemplate(Integer selected) {
		Chapter[] values = Chapter.values();
		List<TElementNew> allChapters = new ArrayList<>();
		for (Chapter value : values) {
			TElementNew tElementNew = new TElementNew();
			tElementNew.setChapterName(value.getValue());
			tElementNew.setChapterType(value.getValueType());
			tElementNew.setChapterDetail(value.toString());
			tElementNew.setChapterPosition(value.getSequence());
			tElementNew.setSelected(selected);
			tElementNew.setElementNewType("表格");
			allChapters.add(tElementNew);
		}
		return allChapters;
	}
	public static List<TElementNew> createEmptyTemplateForSpecial(Integer selected) {
		SpeicealChapter[] values = SpeicealChapter.values();
		List<TElementNew> allChapters = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			TElementNew tElementNew = new TElementNew();
			if (StringUtil.isNotEmpty(values[i].getValue()) && (values[i].getValue().indexOf("事件脉络")!=-1 || values[i].getValue().indexOf("热点")!=-1)){
				if (values[i].getValue().indexOf("微博")!=-1 || values[i].getValue().indexOf("词云")!=-1){
					tElementNew.setSelected(selected);
				}else {
					tElementNew.setSelected(0);
				}
			}else {
				tElementNew.setSelected(selected);
			}
			tElementNew.setChapterName(values[i].getValue());
			tElementNew.setChapterType(values[i].getValueType());
			tElementNew.setChapterDetail(values[i].toString());
			tElementNew.setChapterPosition(i);

			allChapters.add(tElementNew);
		}
		return allChapters;
	}

	public static List<TElementNew> createEmptyTemplateForIndexTab(Integer selected) {
		IndexTabChapter[] values = IndexTabChapter.values();
		List<TElementNew> allChapters = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			TElementNew tElementNew = new TElementNew();
			tElementNew.setSelected(selected);
			tElementNew.setChapterName(values[i].getValue());
			tElementNew.setChapterType(values[i].getValueType());
			tElementNew.setChapterDetail(values[i].toString());
			tElementNew.setChapterPosition(i);
			allChapters.add(tElementNew);
		}
		return allChapters;
	}
	/**
	 * 极简报告默认模板
	 * 
	 * @return
	 */
	public static List<TElementNew> createSimplerTemplate() {
		SimplerChapter[] values = SimplerChapter.values();
		List<TElementNew> allChapters = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			TElementNew tElementNew = new TElementNew();
			tElementNew.setChapterName(values[i].getValue());
			tElementNew.setChapterType(values[i].getValueType());
			tElementNew.setChapterDetail(values[i].toString());
			tElementNew.setChapterPosition(i + 1);
			tElementNew.setSelected(1);
			allChapters.add(tElementNew);
		}
		return allChapters;
	}

	public static List<TElementNew> createMaterialTemplate() {
		List<TElementNew> allChapters = new ArrayList<>();
		TElementNew quickView = new TElementNew();
		quickView.setChapterName("舆情速览");
		quickView.setChapterType(ReportConst.LISTRESOURCES);
		quickView.setChapterDetail("QUICKVIEW");
		quickView.setChapterPosition(1);
		// 0未选中，1选中
		quickView.setSelected(1);
		allChapters.add(quickView);

		TElementNew listData = new TElementNew();
		listData.setChapterName("数据详情");
		listData.setChapterType(ReportConst.LISTRESOURCES);
		listData.setChapterDetail("LISTDATA");
		listData.setChapterPosition(1);
		listData.setSelected(1);
		allChapters.add(listData);

		return allChapters;
	}

	public static List<TElementNew> setDataInElements(List<TElementNew> elementList, ReportDataNew reportData) {
		List<TElementNew> tElements = new ArrayList<>();
		elementList.stream().forEach(e -> {
			// 遍历模板中各模块的时候需要判断该模块是否被选中
			if (ObjectUtil.isNotEmpty(e.getSelected()) && 1 == e.getSelected()) {
				String jsonData = null;
				try {

					Object jsonObject = reportData.getClass()
							.getDeclaredMethod(CHAPTERS2METHODNEW.get(e.getChapterDetail())).invoke(reportData);
					jsonData = jsonObject == null ? null : jsonObject.toString();
				} catch (Exception e1) {
					log.info(e.getChapterDetail());
					e1.printStackTrace();
				}
				TElementNew tElement = new TElementNew(e.getChapterName(), 1, e.getChapterPosition());
				tElement.setChapterDetail(e.getChapterDetail());
				tElement.setChapterType(e.getChapterType());
				if ("报告简介".equals(e.getChapterName())) {
					List<ReportResource> common = new ArrayList<>();
					ReportResource resource = new ReportResource();
					resource.setImgComment(jsonData);
					common.add(resource);
					tElement.setChapaterContent(common);
				} else if ("数据统计概述".equals(e.getChapterName()) && jsonData != null) {
					List<ReportResource> common = new ArrayList<>();
					ReportResource resource = new ReportResource();
					resource.setImgComment(jsonData);
					common.add(resource);
					tElement.setChapaterContent(common);
				} else {
					try {
						tElement.setChapaterContent(JSONObject.parseObject(jsonData, new TypeReference<List<ReportResource>>() {}));
					} catch (Exception exception) {
						tElement.setChapaterContent(null);
					}
				}
				tElements.add(tElement);
			}
		});
		// 专报按位置信息排序，列表信息才能排序即： ListResources "SingleResource", "chart"不能
		for (TElementNew tElementNew : tElements) {
			List<ReportResource> list = new ArrayList<ReportResource>();
			if (!tElementNew.getChapterType().equals("SingleResource")
					&& !tElementNew.getChapterType().equals("chart")) {
				list = tElementNew.getChapaterContent();
				if (ObjectUtil.isNotEmpty(list)) {
					List<ReportResource> list2 = list.stream()
							.sorted(Comparator.comparing(ReportResource::getDocPosition)).collect(Collectors.toList());
					tElementNew.setChapaterContent(list2);
				}
			}
		}
		List<TElementNew> tElementsResult = tElements.stream()
				.sorted(Comparator.comparing(TElementNew::getChapterPosition)).collect(Collectors.toList());
		return tElementsResult;
	}

	public static List<TElementNew> setDataInElements(List<TElementNew> elementList,
			List<ReportResource> previewResources) {
		// 在这里要包报告简介剔除，保证listpreview的数据格式跟之前一样
		Map<?, List<ReportResource>> collect = previewResources.stream().filter(e -> e.getChapterPosition() != 0)
				.collect(Collectors.groupingBy(ReportResource::getChapterPosition));
		TemplateNew templateNew = new TemplateNew();
		templateNew.setTemplateList(JSONArray.toJSONString(elementList));
		return ReportUtil.setResoucesIntoElements(templateNew, collect);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, List<Map<String, String>>> getBase64data(String jsonImgElements) {
		List<Map> parseArray2 = JSONArray.parseArray(jsonImgElements, Map.class);
		Map<String, List<Map<String, String>>> result = new HashMap<>();
		parseArray2.stream().forEach(e -> {
			if(StringUtil.isNotEmpty(e.get("img_data").toString())){
				String key = e.get("chaptername").toString();
				if(e.containsKey("chapterDetail")){
					key = e.get("chapterDetail").toString()+"_"+e.get("chapterPosition").toString();
				}
				// result 中 有该数据
				List<Map<String, String>> list = result.get(key);
				if (list == null) {
					list = new ArrayList<>();
				}
				if(e.get("imgComment") != null){
					String imgComment = e.get("imgComment").toString();
					imgComment = imgComment.replaceAll(startSpan,"").replaceAll(startFontSpan,"").replaceAll(endSpan,"");
					e.put("imgComment",imgComment);
				}
				list.add(e);
				result.put((String) key, list);
			}
		});
		return result;
	}

	public static void main(String[] args) {
		ArrayList<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		list.add("e");
		list.add("f");
		list.add("g");
		list.add("h");
		list.add(0, "1");
		list.add(1, "2");
		System.out.println(list);
	}
	public static String statisticsTimeRestore(String primaryStatisticsTime) {
		// TODO 需要把经过预览之后的时间格式还原
		return "7d";
	}

	public static String contentToAbstract(String str){
		Matcher m = Pattern.compile("<font color=red>([\\s\\S]*?)</font>").matcher(str);
		String[] split = str.split("。");
		if (ObjectUtil.isNotEmpty(split) && split.length < 2){
			split = str.split("[,|，]+");
		}
		List<String> hitWords = new ArrayList<>();
		//所有命中关键词
		while (m.find()){
			String trim = m.group(1).trim();
			if (!hitWords.contains(trim)){
				hitWords.add(trim);
			}
		}

		List<String> result = new ArrayList<>();
		for (String re : hitWords) {
			for (String s : split) {
				if (s.contains(re) && !result.contains(s)){
					s = s.replaceAll("<font color=red>","").replaceAll("</font>","");
					result.add(s);
				}
			}
		}
		if (ObjectUtil.isEmpty(result)){
			return "";
		}
		return StringUtils.join(result.toArray(), "。")+"。";

	}

	public static String calcuRedWord(Boolean isHitWord, String title, String content) {
		String result = "";
		if (isHitWord) {
			String string = title + content;
			if (StringUtil.isEmpty(string)) {
				return "";
			}
			Matcher m = Pattern.compile("<font color=red>([\\s\\S]*?)</font>").matcher(string);

			List<String> hitWords = new ArrayList<>();
			//所有命中关键词
			while (m.find() && hitWords.size() < 6) {
				String trim = m.group(1).trim().replaceAll("&nbsp;"," ");
				if (!hitWords.contains(trim)) {
					hitWords.add(trim);
				}
			}
			result = StringUtils.join(hitWords, "、");
		}
		return result;
	}

	public static String calcuHit(String title, String content, Boolean isHit) {
		String result = "";
		if (isHit) {
			String toAbstract = "";
			toAbstract = ReportUtil.contentToAbstract(content);
			String anAbstract = "";
			String anAbstractNew = "";
			if (StringUtil.isEmpty(toAbstract)) {
				anAbstract = title.replaceAll("<font color=red>", "").replaceAll("</font>", "");
				anAbstractNew = anAbstract + "。" + toAbstract;
			} else {
				anAbstractNew = toAbstract;
			}
			if (StringUtil.isNotEmpty(anAbstractNew)) {
				result = StringUtil.replaceImg(StringUtil.replaceFont(anAbstractNew));
			}
		}
		return result;
	}

	/**
	 * 将TOP10类型的列表json数据转换为List<ReportResource>并且给位置信息赋值
	 *
	 * @author shao.guangze
	 */
	public static List<ReportResource> top10list2RR(String jsonData, String chapter) {
		if (StringUtils.isEmpty(jsonData)) {
			return null;
		}
		jsonData = StringUtil.removeFourChar(jsonData);
		List<Map<String, String>> lists = JSONObject.parseObject(jsonData, new TypeReference<List<Map<String, String>>>() {
		});
		List<ReportResource> listResult = new ArrayList<>();
		int position = 0;
		for (Map<String, String> map : lists) {
			ReportResource reportResource = new ReportResource();
			reportResource.setMd5Tag(map.get("md5Tag"));
			if (StringUtil.isNotEmpty(map.get("urlTime")) && (new Long(map.get("urlTime")) > 345398400000L)) {
				reportResource.setUrlDate(new Date(new Long(map.get("urlTime"))));
			} else {
				reportResource.setUrlDate(timeAgo2urlDate(map.get("timeAgo")));
			}
			//位置信息赋值
			if ("新闻网站TOP10".equals(chapter) || "微博TOP10".equals(chapter) || "微信TOP10".equals(chapter)
					|| "新闻热点话题".equals(chapter) || "微博热点话题".equals(chapter) || "新闻热点".equals(chapter)
					|| "微博热点".equals(chapter) || "微信热点".equals(chapter) || "自媒体号热点".equals(chapter)
					|| "新闻网站事件脉络".equals(chapter) || "微博事件脉络".equals(chapter) || "微信事件脉络".equals(chapter)
					|| "自媒体号事件脉络".equals(chapter)) {
				reportResource.setDocPosition(++position);
			}
			//专题报 改造  20191121
			if ("新闻热点".equals(chapter) || "微博热点".equals(chapter) || "微信热点".equals(chapter)
					|| "自媒体号热点".equals(chapter) || "新闻网站事件脉络".equals(chapter) || "微博事件脉络".equals(chapter)
					|| "微信事件脉络".equals(chapter) || "自媒体号事件脉络".equals(chapter)) {
				reportResource.setSimCount(map.get("simCount")!=null?map.get("simCount"):map.get("simNum"));
				reportResource.setSimNum(map.get("simNum"));
			}
			reportResource.setTimeAgo(map.get("timeAgo"));
			reportResource.setTime(map.get("time")!=null?map.get("time"):map.get("urlTime"));
			reportResource.setUrlTime(map.get("time")!=null?map.get("time"):map.get("urlTime"));
			reportResource.setUrlTitle(map.get("urlTitle"));

			if ("微博事件脉络".equals(chapter)) {
				reportResource.setSiteName(map.get("authors"));
			} else {
				reportResource.setSiteName(map.get("siteName"));
			}

			if("微博TOP10".equals(chapter) || "微博热点".equals(chapter)) {
				reportResource.setSrcName(map.get("author"));
			} else {
				reportResource.setSrcName(map.get("siteName"));
			}
			reportResource.setUrlName(map.get("urlName"));
			reportResource.setTitle(map.get("title"));
			reportResource.setAbstracts(map.get("abstracts"));
			reportResource.setContent(StringUtil.isEmpty(map.get("content")) ? map.get("title") : map.get("content"));
			reportResource.setSid(map.get("sid"));
			reportResource.setGroupName(map.get("groupName"));
			reportResource.setReportType("专报");
			reportResource.setChapter(chapter);
			reportResource.setId(UUID.randomUUID().toString().replace("-", ""));
			//重新截取微博title
			if (chapter.contains("微博")) {
				String content = ReportUtil.replaceHtml(map.get("content"));
				String subStr = "";
				if (content == null)
					content = ReportUtil.replaceHtml(map.get("title"));
				if (content.length() > 160) {
					subStr = content.substring(0, 160);
				} else {
					subStr = content.substring(0, content.length());
				}
				reportResource.setTitle(subStr);
			}
			listResult.add(reportResource);
		}
		return listResult;
	}

	public static Date timeAgo2urlDate(String timeAgo) {
		Calendar cal = Calendar.getInstance();
		if (StringUtils.isNotEmpty(timeAgo) && timeAgo.contains("分钟")) {
			cal.add(Calendar.MINUTE, new Integer("-" + timeAgo.replace("分钟前", "")));
		} else if (StringUtils.isNotEmpty(timeAgo) && timeAgo.contains("小时")) {
			cal.add(Calendar.HOUR_OF_DAY, new Integer("-" + timeAgo.replace("小时前", "")));
		} else if (StringUtils.isNotEmpty(timeAgo) && timeAgo.contains("天")) {
			cal.add(Calendar.DAY_OF_MONTH, new Integer("-" + timeAgo.replace("天前", "")));
		} else if (StringUtils.isNotEmpty(timeAgo) && timeAgo.contains(".")) {
			try {
				return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(timeAgo);
			} catch (Exception e) {
				return cal.getTime();
			}
		}
		// Date -> 时间戳
		return cal.getTime();
	}

	public static void  setEmptyData(ReportDataNew reportData, String chapterType, String chapterDetail) {
		if (ReportConst.SINGLERESOURCE.equals(chapterType) && OVERVIEWOFDATAkey.equals(chapterDetail)) {
			ReportResource overviewRR = new ReportResource();
			overviewRR.setImgComment("无");
			overviewRR.setImg_data("无");
			reportData.setOverviewOfdata(ReportUtil.replaceHtml(JSON.toJSONString(Collections.singletonList(overviewRR))));
		} else {
			ReportResource emptyResource = new ReportResource();
			ArrayList<ReportResource> resources = new ArrayList<>();
			resources.add(emptyResource);
			String data = JSONArray.toJSONString(resources);
			try {
				reportData.getClass().getDeclaredMethod(CHAPTERS2METHODSETNEW.get(chapterDetail), String.class).invoke(reportData, data);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("存储 --" + chapterDetail);
			}
		}
	}
}
