package com.trs.netInsight.support.fts.util;

import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日期时间工具类
 * <p>
 * Create by yan.changjiang on 2017年11月20日
 */
@Slf4j
public class DateUtil {

	public static final String FMT_TRS_yMdhms = "yyyy.MM.dd HH:mm:ss";
	/**
	 * 默认TRS的时间格式
	 */
	public static final String FMT_TRS_yMdHm = "yyyy.MM.dd HH:mm";

	/**
	 * 默认TRS的长时间格式
	 */
	public static final String FMT_TRS_yMd = "yyyy.MM.dd";

	/**
	 * 默认时间格式
	 */
	public static final String DEFAULT_TIME_PATTERN = "yyyy-MM-dd HH:mm";

	/**
	 */
	public static final String yyyyMMdd = "yyyy-MM-dd HH:mm:ss";
	public static final String yyyyMMddHHmm = "yyyy-MM-dd HH:mm";

	/**
	 * 长时间格式
	 */
	public static final String yyyyMMdd_5 = "yyyyMMdd";
	public static final String yyyyMMddHH = "yyyyMMddHH";
	public static final String yyyyMMddHH2 = "yyyy-MM-dd HH";
	public static final String yyyyMMdd2 = "yyyyMMdd";
	public static final String yyyyMMdd3 = "yyyy-MM-dd";
	public static final String yyyyMMdd4 = "yyyy/MM/dd";
	public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	public static final String yyyyMMddHHmmss_Line = "yyyy/MM/dd HH:mm:ss";
	public static final String yyyyMMddHHmmss_Line2 = "yyyy/MM/dd HH:mm";
	/**
	 * 全格式,至毫秒级
	 */
	public static final String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";


	public static final String YMD_PAGE = "yyyy年MM月dd日";


	public static final String YMDH_PAGE = "yyyy年MM月dd日HH时";

	/**
	 * 一天的总秒数
	 */
	public static final long ONE_DAY_SECONDS = 24 * 3600;

	/**
	 * 一天的总豪秒数
	 */
	private static final long ONE_DAY_MILLISSECONDS = 24 * 3600 * 1000;
	/**
	 * 时间为天数时的后缀
	 */
	public static String DAY_START = "000000";
	public static String DAY_END = "235959";
	/**
	 * 时间为小时是的后缀
	 */
	public static String HOUR_START = "0000";
	public static String HOUR_END = "5959";

	/**
	 * 将毫秒数转化为可读的时间字符串,如1天，1小时，1分<br>
	 * 的区别是不会显示多个单位,更符合阅读习惯,如1天1小时显示为25小时,1小时3分显示为63分
	 *
	 * @param millis
	 *            毫秒数
	 * @return 时间字符串
	 * @since huangshengbo @ Mar 31, 2011
	 */
	public static String timeToString(long millis) {
		if (millis < 0) {
			throw new IllegalArgumentException("[millis] must be greater than zero.");
		}
		// 毫秒
		if ((millis % 1000) > 0) {
			return String.valueOf(millis) + "毫秒";
		}

		// 秒
		long seconds = millis / 1000;
		if ((seconds % 60) > 0) {
			return String.valueOf(seconds) + "秒";
		}

		// 分
		long minutes = seconds / 60;
		if ((minutes % 60) > 0) {
			return String.valueOf(minutes) + "分";
		}

		// 小时
		long hours = minutes / 60;
		if ((hours % 24) > 0) {
			return String.valueOf(hours) + "小时";
		}

		// 天
		long days = hours / 24;
		return String.valueOf(days) + "天";
	}

	/**
	 * 今天与指定时间差几天 返回大于0说明指定时间还没到 小于0说明指定时间已过
	 * 
	 * @param dateExpire
	 * @return
	 */
	public static int rangBetweenNow(Date dateExpire) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date()); // 需要将date数据转移到Calender对象中操作
		Calendar calendarExpire = Calendar.getInstance();
		calendarExpire.setTime(dateExpire);
		int last = calendarExpire.get(Calendar.DAY_OF_YEAR);
		int before = calendar.get(Calendar.DAY_OF_YEAR);
		int year1 = calendarExpire.get(Calendar.YEAR);
		int year2 = calendar.get(Calendar.YEAR);
		if (year1 != year2) { // 不同一年
			int timeDistance = 0;
			for (int i = year2; i < year1; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) { // 闰年
					timeDistance += 366;
				} else {// 不是闰年
					timeDistance += 365;
				}
			}
			return timeDistance + (last - before);
		} else { // 同年
			return last - before;
		}
	}

	public static   int daysBetween(Date returnDate) {
		Date now = new Date();
		Calendar cNow = Calendar.getInstance();
		Calendar cReturnDate = Calendar.getInstance();
		cNow.setTime(now);
		cReturnDate.setTime(returnDate);
		setTimeToMidnight(cNow);
		setTimeToMidnight(cReturnDate);
		long todayMs = cNow.getTimeInMillis();
		long returnMs = cReturnDate.getTimeInMillis();
		long intervalMs = returnMs - todayMs;
		return millisecondsToDays(intervalMs);
	}

	public static void setTimeToMidnight(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
	}

	public static int millisecondsToDays(long intervalMs) {
		return (int) (intervalMs / (1000 * 86400));
	}


	public static void main(String[] args) {
		try {
			String time = "10n";
			String[] timeArray = formatTimeRangeMinus1(time);
			System.out.println(timeArray[0]);
			System.out.println(timeArray[1]);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * 获取与当前时间间隔一小时的时间 hour正数为之后 负数为之前
	 *
	 * @param hour
	 * @return
	 */
	public static String getTimeByHour(int hour) {

		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hour);

		return new SimpleDateFormat(yyyyMMddHHmmss).format(calendar.getTime());

	}

	/**
	 * 获取当前时间之前或之后几分钟 minute 负数为之前
	 *
	 * @param minute
	 * @return
	 */
	public static String getTimeByMinute(int minute) {

		Calendar calendar = Calendar.getInstance();

		calendar.add(Calendar.MINUTE, minute);

		return new SimpleDateFormat(yyyyMMddHHmmss).format(calendar.getTime());

	}
	/** TODO
	 * 获取指定时间之前或之后几分钟 minute 负数为之前
	 * @date Created at 2018年11月6日  下午5:19:39
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param date  指定
	 * @param minute
	 * @return
	 */
	public static Date getTimeByMinute(Date date,int minute) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, minute);
		
		return new Date(calendar.getTimeInMillis());
		
	}

	/**
	 * 计算文章urltime与当前时间差
	 */
	public static Map<String, String> timeDifference(FtsDocument ftsDocument) {
		Map<String, String> map = new HashMap<>();
		long nowTime = new Date().getTime();
		long lastTime = ftsDocument.getUrlTime().getTime();
		long result = nowTime - lastTime;
		if (ftsDocument.getSid().equals("10290424616106519227_0_637054")) {
			log.info("10290424616106519227_0_637054");
		}
		int days = (int) (result / (1000 * 60 * 60 * 24));
		if (days < 1) {
			// 计算小时查
			int hours = (int) (result / (1000 * 60 * 60));
			// 计算分钟差
			int minutes = (int) (result / (1000 * 60));
			if (0 == minutes) {
				map.put("timeAgo", "刚刚");
			} else if (0 != minutes) {
				map.put("timeAgo", minutes + "分钟前");
			}
			if (0 != hours) {
				map.put("timeAgo", hours + "小时前");
			}
			// if (0 != days) {
			// map.put("timeAgo", days + "天前");
			// }
		} else {
			map.put("urlTime", date2String(ftsDocument.getUrlTime(), FMT_TRS_yMdhms));
		}
		return map;
	}

	/**
	 * 计算文章urltime与当前时间差
	 */
	public static Map<String, String> timeDifference(FtsDocumentCommonVO ftsDocument) {
		Map<String, String> map = new HashMap<>();
		long nowTime = new Date().getTime();
		long lastTime = ftsDocument.getUrlTime().getTime();
		long result = nowTime - lastTime;
		if (ftsDocument.getSid().equals("10290424616106519227_0_637054")) {
			log.info("10290424616106519227_0_637054");
		}
		int days = (int) (result / (1000 * 60 * 60 * 24));
		if (days < 1) {
			// 计算小时查
			int hours = (int) (result / (1000 * 60 * 60));
			// 计算分钟差
			int minutes = (int) (result / (1000 * 60));
			if (0 == minutes) {
				map.put("timeAgo", "刚刚");
			} else if (0 != minutes) {
				map.put("timeAgo", minutes + "分钟前");
			}
			if (0 != hours) {
				map.put("timeAgo", hours + "小时前");
			}
			// if (0 != days) {
			// map.put("timeAgo", days + "天前");
			// }
		} else {
			map.put("urlTime", date2String(ftsDocument.getUrlTime(), FMT_TRS_yMdhms));
		}
		return map;
	}

	/**
	 * 计算文章urltime与当前时间差
	 */
	public static Map<String, String> timeDifference(FtsDocumentStatus ftsDocument) {
		Map<String, String> map = new HashMap<>();
		long nowTime = new Date().getTime();
		long lastTime = ftsDocument.getCreatedAt().getTime();
		long result = nowTime - lastTime;
		int days = (int) (result / (1000 * 60 * 60 * 24));
		if (days < 1) {
			// 计算小时查
			int hours = (int) (result / (1000 * 60 * 60));
			// 计算分钟差
			int minutes = (int) (result / (1000 * 60));
			if (0 == minutes) {
				map.put("timeAgo", "刚刚");
			} else if (0 != minutes) {
				map.put("timeAgo", minutes + "分钟前");
			}
			if (0 != hours) {
				map.put("timeAgo", hours + "小时前");
			}
			// if (0 != days) {
			// map.put("timeAgo", days + "天前");
			// }
		} else {
			map.put("createdAt", date2String(ftsDocument.getCreatedAt(), FMT_TRS_yMdhms));
		}
		return map;
	}

	/**
	 * 计算时间差
	 * 
	 * @param doc
	 * @return
	 */
	public static Map<String, String> timeDifference(FtsDocumentWeChat doc) {
		Map<String, String> map = new HashMap<>();
		long nowTime = new Date().getTime();
		long lastTime = doc.getUrlTime().getTime();
		long result = nowTime - lastTime;
		int days = (int) (result / (1000 * 60 * 60 * 24));
		if (days < 1) {
			// 计算小时查
			int hours = (int) (result / (1000 * 60 * 60));
			// 计算分钟差
			int minutes = (int) (result / (1000 * 60));
			if (0 == minutes) {
				map.put("timeAgo", "刚刚");
			} else if (0 != minutes) {
				map.put("timeAgo", minutes + "分钟前");
			}
			if (0 != hours) {
				map.put("timeAgo", hours + "小时前");
			}
			// if (0 != days) {
			// map.put("timeAgo", days + "天前");
			// }
		} else {
			map.put("urlTime", date2String(doc.getUrlTime(), FMT_TRS_yMdhms));
		}

		return map;
	}

	/**
	 * 根据天数获得时间范围，最近几天，形如：1970.01.01 - 2014.10.01
	 * <p>
	 * <strong>注意：最近7天是6天前到今天</strong>
	 *
	 * @param day
	 *            最近几天
	 * @return String
	 * @since zhangchengbing @ 2012-3-8
	 */
	public static String getDateRange(int day) {
		String dateRange = "";
		if (day > 0) {
			Calendar now = Calendar.getInstance();
			String endDate = date2String(now.getTime(), FMT_TRS_yMd);
			now.add(Calendar.DATE, 1 - day);
			String beginDate = date2String(now.getTime(), FMT_TRS_yMd);
			if (endDate.equals(beginDate)) {
				dateRange = endDate;
			} else {
				dateRange = beginDate.concat(" - ").concat(endDate);
			}
		}
		return dateRange;
	}

	public static String date2String(Date date, String pattern) {
		if (date == null) {
			return null;
		}
		try {
			return new SimpleDateFormat(pattern).format(date);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取当前日期
	 *
	 * @param format
	 *            格式
	 * @return String
	 * @since zhanghu @ 2012-4-12
	 */
	public static String formatCurrentTime(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	/**
	 * 获取与指定日期间隔指定天数的日期
	 *
	 * @param date
	 *            时间
	 * @param format
	 *            日期格式
	 * @param relativeDay
	 *            间隔天数
	 * @return 间隔日期
	 * @since zhanghu @ 2012-4-5
	 */
	public static String formatDateAfter(String date, String format, int relativeDay) {
		if (StringUtil.isEmpty(date) || StringUtil.isEmpty(format)) {
			return "";
		}
		Calendar calendarTemp = Calendar.getInstance();
		try {
			Date dateTemp = new SimpleDateFormat(format).parse(date);
			calendarTemp.setTime(dateTemp);
			calendarTemp.add(Calendar.DAY_OF_YEAR, relativeDay);
		} catch (Exception e) {
			return "";
		}
		return new SimpleDateFormat(format).format(calendarTemp.getTime());
	}

	/**
	 * 获取与指定日期间隔指定天数的日期
	 *
	 * @param date
	 *            时间
	 * @param format
	 *            日期格式
	 * @param relativeDay
	 *            间隔天数
	 * @return 间隔日期
	 * @since zhanghu @ 2012-4-5
	 */
	public static String formatDateAfter(Date date, String format, int relativeDay) {
		if (date == null || StringUtil.isEmpty(format)) {
			return "";
		}
		Calendar calendarTemp = Calendar.getInstance();
		calendarTemp.setTime(date);
		calendarTemp.add(Calendar.DAY_OF_YEAR, relativeDay);
		return new SimpleDateFormat(format).format(calendarTemp.getTime());
	}

	public static String formatDateAfterNow(String format, int relativeDay) {
		if (StringUtil.isEmpty(format)) {
			return "";
		}
		Calendar calendarTemp = Calendar.getInstance();
		calendarTemp.setTimeInMillis(System.currentTimeMillis());
		calendarTemp.add(Calendar.DAY_OF_YEAR, relativeDay);
		return new SimpleDateFormat(format).format(calendarTemp.getTime());
	}

	/**
	 * 获取与指定日期间隔指定天数的日期
	 *
	 * @param date
	 *            时间
	 * @param relativeDay
	 *            间隔天数
	 * @return 间隔日期
	 * @since zhanghu @ 2013-2-22
	 */
	public static long getDate(long date, int relativeDay) {
		Calendar calendarTemp = Calendar.getInstance();
		try {
			calendarTemp.setTime(new Date(date));
			calendarTemp.add(Calendar.DAY_OF_YEAR, relativeDay);
		} catch (Exception e) {
			return 0;
		}
		return calendarTemp.getTime().getTime();
	}

	/**
	 * 获取和当天相隔指定天数的Date对象. 大于0表示之后, 小于0表之前.
	 *
	 * @param relativeDay
	 *            相隔指定天数
	 * @return Date对象
	 * @see #getCalendar(Calendar, int)
	 */
	public static Date getDate(int relativeDay) {
		return getCalendar(Calendar.getInstance(), relativeDay).getTime();
	}

	/**
	 * 获取和指定cal对象相隔指定天数的cal对象. 大于0表示之后, 小于0表之前.
	 *
	 * @param cal
	 *            指定cal对象
	 * @param relativeDay
	 *            相隔指定天数
	 * @return cal对象
	 */
	public static Calendar getCalendar(Calendar cal, int relativeDay) {
		cal.add(Calendar.DATE, relativeDay);
		return cal;
	}

	/**
	 * 获取与指定日期间隔指定月份的日期
	 *
	 * @param date
	 *            指定时间
	 * @param format
	 *            转换格式
	 * @param relativeMonth
	 *            间隔月份
	 * @return 间隔时间
	 * @since zhanghu @ 2012-5-10
	 */
	public static String getRelativeMonthDate(String date, String format, int relativeMonth) {
		Calendar calendarTemp = Calendar.getInstance();
		try {
			Date dateTemp = new SimpleDateFormat(format).parse(date);
			calendarTemp.setTime(dateTemp);
			calendarTemp.add(Calendar.MONTH, relativeMonth);
		} catch (Exception e) {
			return "";
		}
		return new SimpleDateFormat(format).format(calendarTemp.getTime());
	}

	/**
	 * 取得最近一月时间范围
	 *
	 * @return String
	 * @since zhangchengbing @ 2012-3-8
	 */
	public static String getMonthOfLate() {
		Calendar now = Calendar.getInstance();
		String endDate = date2String(now.getTime(), FMT_TRS_yMd);
		now.add(Calendar.MONTH, -1);
		String beginDate = date2String(now.getTime(), FMT_TRS_yMd);
		return beginDate.concat(" - ").concat(endDate);
	}

	/**
	 * trs日期格式转换(yyyy.MM.dd HH:mm:ss转为yyyy.MM.dd)
	 *
	 * @param dateTime
	 *            转换日期
	 * @return 转换后日期
	 * @since zhanghu @ 2012-3-27
	 */
	public static String trsTimeFormatToDateFormat(String dateTime) {
		if (StringUtil.isEmpty(dateTime)) {
			return null;
		}
		SimpleDateFormat sf = new SimpleDateFormat(FMT_TRS_yMdhms);
		try {
			Date date = sf.parse(dateTime);
			if (date == null) {
				return null;
			}
			return new SimpleDateFormat(FMT_TRS_yMd).format(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 获取某年某月的最后一天
	 *
	 * @param year
	 *            年
	 * @param month
	 *            月
	 * @return int
	 * @since zhanghu @ 2011-12-7
	 */
	public static int getLastDayOfMonth(int year, int month) {
		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			return 31;
		}
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			return 30;
		}
		if (month == 2) {
			if (isLeapYear(year)) {
				return 29;
			} else {
				return 28;
			}
		}
		return 0;

	}

	/**
	 * 是否是闰年
	 *
	 * @param year
	 *            年份
	 * @return true or false
	 * @since zhanghu @ 2011-12-7
	 */
	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	/**
	 * 将格式的日期字符串转换为当天0时的毫秒时间
	 *
	 * @param date
	 *            日期字符串
	 * @return 当天0时的毫秒时间
	 * @since zhangchengbing @ 2012-6-25
	 */
	public static long formatDate2Millis(String date, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.parse(date).getTime();
		} catch (ParseException e) {
			return 0;
		}
	}


	public static String formatDateToString(String date,String oldFormat, String newFormat) {
		try {
			SimpleDateFormat old_sdf = new SimpleDateFormat(oldFormat);
			SimpleDateFormat new_sdf = new SimpleDateFormat(newFormat);
			Date time = old_sdf.parse(date);
			return new_sdf.format(time);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 格式化时间
	 *
	 * @param date
	 *            日期字符串
	 * @param pattern
	 *            格式
	 * @return String
	 * @since zengqingmeng @ Jan 14, 2013
	 */
	public static String format2String(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

	/**
	 * 获取当前的时间（毫秒）
	 *
	 * @return 当前的时间，以毫秒为单位
	 * @since fangxiang @ May 15, 2010
	 */
	public static long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * 转换时间成毫秒
	 *
	 * @param time
	 *            待转换的时间
	 * @param pattern
	 *            与time一致的时间格式
	 * @return 转换后的毫秒数
	 * @since fangxiang @ Apr 17, 2010
	 */

	public static final long getDateAsMillis(String time, String pattern) {
		if (time == null) {
			return -1;
		}
		Date theTime = stringToDate(time, (pattern == null) ? DEFAULT_TIME_PATTERN : pattern);
		if (theTime == null) {
			return -1;
		}
		return theTime.getTime();
	}

	/**
	 * 获取当前年份.
	 *
	 * @since liushen @ Jun 13, 2011
	 */
	public static int getThisYear() {
		return getYear(System.currentTimeMillis());
	}

	/**
	 * 获取当前年份.
	 *
	 * @since liushen @ Jun 13, 2011
	 */
	public static int getYear(long timestamp) {
		Date now = new Date(timestamp);
		Calendar cal = toCalendar(now);
		return cal.get(Calendar.YEAR);
	}

	/**
	 * 解析新浪微博的时间
	 *
	 * @param text
	 *            新浪微博时间
	 * @return long
	 * @since zengqingmeng @ Jul 27, 2012
	 */
	public static long parseDateFromWeiboPageAsMillis(String text) {
		if ("".equals(text) || text == null) {
			return 0;
		}

		long currentTime = getCurrentTimeMillis();
		Pattern minutesPattern = Pattern.compile("\\d+分钟前");
		Matcher match = minutesPattern.matcher(text);
		if (match.find()) {
			return currentTime - Integer.parseInt(match.group().trim().replace("分钟前", "")) * 60 * 1000;
		}

		Pattern todayPattern = Pattern.compile("今天\\s+\\d+:\\d+");
		match = todayPattern.matcher(text);
		if (match.find()) {
			return getDateAsMillis(match.group().replace("今天", formatCurrentTime(FMT_TRS_yMd)), "yyyy.MM.dd HH:mm");
		}

		Pattern monthPattern = Pattern.compile("\\d+月\\d+日\\s+\\d+:\\d+");
		match = monthPattern.matcher(text);
		if (match.find()) {
			return getDateAsMillis(getThisYear() + "年" + match.group(), "yyyy年MM月dd日 HH:mm");
		}

		return 0;
	}

	/**
	 * 解析新浪微博的时间
	 *
	 * @param text
	 *            包含时间的字符串
	 * @param pattern
	 *            返回的时间格式
	 * @return String
	 * @since zengqingmeng @ Jul 27, 2012
	 */
	public static String getDateFromWeiboPage(String text, String pattern) {
		if ("".equals(text) || text == null) {
			return "";
		}
		if ("".equals(pattern) || pattern == null) {
			pattern = FMT_TRS_yMdhms;
		}
		long time = parseDateFromWeiboPageAsMillis(text);
		if (time == 0) {
			return "";
		}
		return formatMillis(time, pattern);
	}

	/**
	 * @param timeMillis
	 *            毫秒数
	 * @return String
	 */
	public static String formatMillis(long timeMillis, String format) {
		return formatMillis(timeMillis, format, Locale.SIMPLIFIED_CHINESE);
	}

	/**
	 * 按指定地域的习惯来显示时间.
	 *
	 * @since liushen @ May 25, 2011
	 */
	public static String formatMillis(long timeMillis, String format, Locale locale) {
		SimpleDateFormat sdf = (locale == null) ? new SimpleDateFormat(format) : new SimpleDateFormat(format, locale);
		return sdf.format(new Date(timeMillis));
	}

	public static long getDayStartAsMillis(String date, String pattern) {
		Calendar current = toCalendar(stringToDate(date, pattern));
		return current.getTimeInMillis();
	}

	public static long getDayEndAsMillis(String date, String pattern) {
		Calendar current = toCalendar(stringToDate(date, pattern));
		return current.getTimeInMillis() + ONE_DAY_MILLISSECONDS;
	}

	public static long getDayStartAsMillis(int relativeDay) {
		Calendar day = getCalendar(Calendar.getInstance(), relativeDay);
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		return day.getTimeInMillis();
	}

	public static long getDayEndAsMillis(int relativeDay) {
		return getDayStartAsMillis(relativeDay) + ONE_DAY_MILLISSECONDS;
	}

	/**
	 * 完成日期串到日期对象的转换. <BR>
	 *
	 * @param dateString
	 *            日期字符串
	 * @param dateFormat
	 *            日期格式
	 * @return date 日期对象
	 * @since zengqingmeng @ Jan 14, 2013
	 */
	public static Date stringToDate(String dateString, String dateFormat) {
		if ("".equals(dateString) || dateString == null) {
			return null;
		}
		try {
			return new SimpleDateFormat(dateFormat).parse(dateString);
		} catch (Exception e) {
			return null;
		}
	}
	public static String stringToStringDate(String dateString,String beforeFormat,String nowFormat) {
		if ("".equals(dateString) || dateString == null) {
			return null;
		}
		try {
			Date date = new SimpleDateFormat(beforeFormat).parse(dateString);
			return new SimpleDateFormat(nowFormat).format(date);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 从Date对象得到Calendar对象. <BR>
	 * JDK提供了Calendar.getTime()方法, 可从Calendar对象得到Date对象,
	 * 但没有提供从Date对象得到Calendar对象的方法.
	 *
	 * @param date
	 *            给定的Date对象
	 * @return 得到的Calendar对象. 如果date参数为null, 则得到表示当前时间的Calendar对象.
	 */
	public static Calendar toCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		return cal;
	}

	/**
	 * 是否为yyyy.MM.dd HH格式
	 *
	 * @param date
	 *            日期
	 * @return 是否为小时格式
	 * @since zhanghu @ 2013-1-31
	 */
	public static boolean isTimeHourFormat(String date) {
		String regex = "[0-9]+\\.[0-9]+\\.[0-9]+\\s[0-9][0-9]$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(date);
		return matcher.find();
	}

	/**
	 * 获得本周一的日期
	 *
	 * @return 本周一日期
	 * @since zhanghu @ 2013-3-7
	 */
	public static String getMondayOfThisWeek(String format) {
		int mondayPlus = getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus);
		Date monday = currentDate.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(monday);
	}

	/**
	 * 获得当前日期与本周日相差的天数
	 *
	 * @return 相差天数
	 * @since zhanghu @ 2013-3-7
	 */
	private static int getMondayPlus() {
		Calendar cd = Calendar.getInstance();
		int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 1) {
			return 0;
		} else {
			return 1 - dayOfWeek;
		}
	}

	public static int dateForWeek(String date, String format) {
		int dayForWeek = 0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(date));
			if (c.get(Calendar.DAY_OF_WEEK) == 1) {
				dayForWeek = 7;
			} else {
				dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
			}
		} catch (Exception e) {
			return 0;
		}
		return dayForWeek;
	}

	/**
	 * 获取24小时字符串
	 *
	 * @return 24小时字符串列表
	 * @since zhanghu @ 2013-4-7
	 */
	public static List<String> get24HourString() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < 24; i++) {
			if (i >= 10) {
				list.add(String.valueOf(i));
			} else {
				list.add("0" + i);
			}
		}
		return list;
	}

	/**
	 * 获取指定日期所有小时字符串,例如：2011.02.22 00-2011.02.23 00
	 *
	 * @param date
	 *            日期
	 * @return 小时列表
	 * @since zhanghu @ 2013-4-7
	 */
	public static List<String> getDateHourString(String date) {
		List<String> list = new ArrayList<>();
		if (StringUtil.isEmpty(date)) {
			date = DateUtil.formatCurrentTime(FMT_TRS_yMd);
		}
		for (int i = 0; i < 24; i++) {
			if (i >= 10) {
				list.add(date + " " + i);
			} else {
				list.add(date + " " + "0" + i);
			}
		}
		return list;
	}

	/**
	 * 获取指定日期所有小时字符串,例如：2011.02.22 00-2011.02.23 00
	 *
	 * @param date
	 *            日期
	 * @param endHour
	 *            截止小时
	 * @return 小时列表
	 * @since zhanghu @ 2013-4-7
	 */
	public static List<String> getDateHourString(String date, int endHour) {
		List<String> list = new ArrayList<String>();
		if (StringUtil.isEmpty(date)) {
			date = DateUtil.formatCurrentTime(FMT_TRS_yMd);
		}
		for (int i = 0; i <= endHour; i++) {
			if (i >= 10) {
				list.add(date + "" + i + "0000");
			} else {
				list.add(date + "" + "0" + i + "0000");
			}
		}
		return list;
	}

	/**
	 * 只返回小时
	 * 
	 * @param date
	 * @param endHour
	 * @return
	 */
	public static List<String> getDateHour(String date, int endHour) {
		List<String> list = new ArrayList<String>();
		if (StringUtil.isEmpty(date)) {
			date = DateUtil.formatCurrentTime(FMT_TRS_yMd);
		}
		for (int i = 0; i <= endHour; i++) {
			if (i >= 10) {
				list.add("" + i);
			} else {
				list.add("0" + i);
			}
		}
		return list;
	}

	/**
	 * 校验字符串日期格式
	 *
	 * @param str
	 * @return
	 */
	public static boolean isValidDate(String str) {
		boolean convertSuccess = true;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			format.setLenient(false);
			format.parse(str);
		} catch (ParseException e) {
			// 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
			convertSuccess = false;
		}
		return convertSuccess;
	}
	/**
	 * 校验字符串日期格式
	 *
	 * @param str 时间
	 * @param dateFormat 格式
	 * @return
	 */
	public static boolean isValidDate(String str,String dateFormat) {
		boolean convertSuccess = true;
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		try {
			format.setLenient(false);
			format.parse(str);
		} catch (ParseException e) {
			// 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
			convertSuccess = false;
		}
		return convertSuccess;
	}

	/**
	 * 获取日期小时
	 *
	 * @param date
	 *            日期
	 * @param format
	 *            日期格式
	 * @return 小时
	 * @since zhanghu @ 2013-4-7
	 */
	public static int getDateHour(String date, String format) {
		Calendar ca = Calendar.getInstance();
		try {
			Date dateTemp = new SimpleDateFormat(format).parse(date);
			ca.setTime(dateTemp);
		} catch (ParseException e) {
			return 0;
		}
		return ca.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 获得两个日期字符串之间的所有日期字符串对象
	 *
	 * @param beginDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @param dateFormat
	 *            日期格式
	 * @return 日期列表
	 * @since zhanghu @ 2013-4-7
	 */
	public static List<String> getBetweenDateString(String beginDate, String endDate, String dateFormat) {
		Vector<GregorianCalendar> v = new Vector<GregorianCalendar>();
		List<String> list = new ArrayList<String>();
		if (StringUtil.isEmpty(beginDate) || StringUtil.isEmpty(endDate)) {
			return list;
		}
		if (beginDate.compareTo(endDate) > 0) {
			return list;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		GregorianCalendar gc1 = new GregorianCalendar();
		GregorianCalendar gc2 = new GregorianCalendar();
		try {
			gc1.setTime(sdf.parse(beginDate));
			gc2.setTime(sdf.parse(endDate));
		} catch (ParseException e) {
			log.error(e.getMessage());
			return list;
		}
		do {
			GregorianCalendar gc3 = (GregorianCalendar) gc1.clone();
			v.add(gc3);
			gc1.add(Calendar.DAY_OF_MONTH, 1);
		} while (!gc1.after(gc2));
		for (GregorianCalendar g : v) {
			list.add(DateUtil.format2String(g.getTime(), dateFormat));
		}
		return list;
	}

	/**
	 * 获得与当前时间的相隔时间
	 *
	 * @param datetime
	 *            时间
	 * @return 相隔时间
	 * @since songjia @ 2013-4-7
	 */
	public static String getNowBetweenDateString(String datetime) {
		if (StringUtil.isEmpty(datetime)) {
			return "";
		}
		Calendar calendar = new GregorianCalendar();
		Date trialTime = new Date();
		calendar.setTime(trialTime);
		Calendar ca = Calendar.getInstance();
		Date dateTemp;
		try {
			dateTemp = new SimpleDateFormat(FMT_TRS_yMdhms).parse(datetime);
		} catch (ParseException e) {
			return datetime;
		}
		ca.setTime(dateTemp);
		String M = ((ca.get(Calendar.MONTH) + 1) + "").length() == 1 ? "0" + (ca.get(Calendar.MONTH) + 1)
				: (ca.get(Calendar.MONTH) + 1) + "";
		String d = (ca.get(Calendar.DATE) + "").length() == 1 ? "0" + ca.get(Calendar.DATE)
				: ca.get(Calendar.DATE) + "";
		String h = (ca.get(Calendar.HOUR_OF_DAY) + "").length() == 1 ? "0" + ca.get(Calendar.HOUR_OF_DAY)
				: ca.get(Calendar.HOUR_OF_DAY) + "";
		String m = (ca.get(Calendar.MINUTE) + "").length() == 1 ? "0" + ca.get(Calendar.MINUTE)
				: ca.get(Calendar.MINUTE) + "";
		if (calendar.get(Calendar.YEAR) != ca.get(Calendar.YEAR)
				|| calendar.get(Calendar.MONTH) != ca.get(Calendar.MONTH)) {
			return ca.get(Calendar.YEAR) + "." + M + "." + d;
		} else if (calendar.get(Calendar.DATE) > ca.get(Calendar.DATE)) {
			int date = calendar.get(Calendar.DATE) - ca.get(Calendar.DATE);
			switch (date) {
			case 1:
				return "昨天 " + h + ":" + m;
			case 2:
				return "前天 " + h + ":" + m;
			default:
				return ca.get(Calendar.YEAR) + "." + M + "." + d;
			}

		} else if (calendar.get(Calendar.HOUR_OF_DAY) - ca.get(Calendar.HOUR_OF_DAY) > 1
				|| (calendar.get(Calendar.HOUR_OF_DAY) - ca.get(Calendar.HOUR_OF_DAY) == 1
						&& calendar.get(Calendar.MINUTE) > ca.get(Calendar.MINUTE))) {
			int hour = calendar.get(Calendar.HOUR_OF_DAY) - ca.get(Calendar.HOUR_OF_DAY);
			return hour + "小时前";
		} else if (calendar.get(Calendar.MINUTE) > ca.get(Calendar.MINUTE)
				|| (calendar.get(Calendar.HOUR_OF_DAY) - ca.get(Calendar.HOUR_OF_DAY) == 1)) {
			int hm = (calendar.get(Calendar.HOUR_OF_DAY) - ca.get(Calendar.HOUR_OF_DAY) == 1) ? 60 : 0;
			int minute = calendar.get(Calendar.MINUTE) - ca.get(Calendar.MINUTE) + hm;
			return minute + "分钟前";
		} else {
			return "刚刚";
		}
	}
	public static boolean getNowBetween30Day(Date datetime) {
		if (ObjectUtil.isEmpty(datetime)) {
			return false;
		}
		Calendar calendar = new GregorianCalendar();
		Date trialTime = new Date();
		calendar.setTime(trialTime);
		Calendar ca = Calendar.getInstance();

		ca.setTime(datetime);
		String M = ((ca.get(Calendar.MONTH) + 1) + "").length() == 1 ? "0" + (ca.get(Calendar.MONTH) + 1)
				: (ca.get(Calendar.MONTH) + 1) + "";
		String d = (ca.get(Calendar.DATE) + "").length() == 1 ? "0" + ca.get(Calendar.DATE)
				: ca.get(Calendar.DATE) + "";
		String h = (ca.get(Calendar.HOUR_OF_DAY) + "").length() == 1 ? "0" + ca.get(Calendar.HOUR_OF_DAY)
				: ca.get(Calendar.HOUR_OF_DAY) + "";
		String m = (ca.get(Calendar.MINUTE) + "").length() == 1 ? "0" + ca.get(Calendar.MINUTE)
				: ca.get(Calendar.MINUTE) + "";
		if (calendar.get(Calendar.YEAR) != ca.get(Calendar.YEAR)
				|| calendar.get(Calendar.MONTH) != ca.get(Calendar.MONTH)) {
			return true;
		} else if (calendar.get(Calendar.DATE) > ca.get(Calendar.DATE)) {
			int date = calendar.get(Calendar.DATE) - ca.get(Calendar.DATE);
			if (date >= 30) return true;

		}else {
			return false;
		}
		return false;
	}

	public static Long[] getCurrentDayIntervalTime() {
		String date = formatDateAfter(format2String(new Date(), FMT_TRS_yMd), FMT_TRS_yMd, -1);
		long beginTime = getDateAsMillis(date + " 00:00", FMT_TRS_yMdHm);
		long endTime = getDate(new Date().getTime(), -1);
		return new Long[] { beginTime, endTime };
	}

	public static String getZHDatetime(String datetime) {
		Calendar ca = Calendar.getInstance();
		Date dateTemp;
		try {
			dateTemp = new SimpleDateFormat(FMT_TRS_yMdhms).parse(datetime);
		} catch (ParseException e) {
			return datetime;
		}
		ca.setTime(dateTemp);
		return ca.get(Calendar.YEAR) + "年" + String.format("%02d", ca.get(Calendar.MONTH) + 1) + "月"
				+ String.format("%02d", ca.get(Calendar.DATE)) + "日 "
				+ String.format("%02d", ca.get(Calendar.HOUR_OF_DAY)) + ":"
				+ String.format("%02d", ca.get(Calendar.MINUTE)) + ":" + String.format("%02d", ca.get(Calendar.SECOND));
	}

	/**
	 * 当天的开始时间
	 *
	 * @return
	 */
	public static long startOfTodDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date date = calendar.getTime();
		return date.getTime();
	}

	/**
	 * 当天的结束时间
	 *
	 * @return
	 */
	public static long endOfTodDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		Date date = calendar.getTime();
		return date.getTime();
	}

	/**
	 * 本月第一天
	 *
	 * @return
	 */
	public static String startOfMonth() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		// 获取当前月第一天：
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
		return format.format(c.getTime());
	}

	/**
	 * 本月最后一天
	 */
	public static String endOfMonth() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Calendar ca = Calendar.getInstance();
		ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
		return format.format(ca.getTime());
	}

	/**
	 * 本周第一天
	 *
	 * @return
	 */
	public static String startOfWeek() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return format.format(calendar.getTime());
	}

	/**
	 * 本周最后一天
	 *
	 * @return
	 */
	public static String endOfWeek() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance(Locale.CHINA);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		return format.format(calendar.getTime());
	}

	/**
	 * 秒数转为日期字符串格式
	 *
	 * @param dateMillis
	 *            秒数
	 * @param pattern
	 *            格式
	 * @return 日期字符串
	 * @since zhanghu @ Jul 9, 2014 5:04:47 PM
	 */
	public static String millis2String(long dateMillis, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(new Date(dateMillis));
	}

	/**
	 * 秒数转为小时(保留小数点2位)
	 *
	 * @param millis
	 *            秒数
	 * @return 小时
	 * @since zhanghu @ Jul 9, 2014 5:04:01 PM
	 */
	public static String millisToHour(long millis) {
		return new DecimalFormat("###0.00").format((float) millis / (60 * 60000));
	}

	/**
	 * 将起止时间按天分割
	 *
	 * @param beginDate
	 *            开始日期
	 * @param endDate
	 *            结束日期
	 * @return List
	 */
	public static List<String[]> subDateAsList(String beginDate, String endDate, String format) throws TRSException {
		try {
			Date b1 = new SimpleDateFormat(format).parse(beginDate);
			Date e1 = new SimpleDateFormat(format).parse(endDate);
			long bl = b1.getTime();
			long el = e1.getTime();
			int days = (int) (el / ONE_DAY_MILLISSECONDS - bl / ONE_DAY_MILLISSECONDS);
			int interval = days / 7;
			List<String[]> result = new ArrayList<>();
			el = el + ONE_DAY_MILLISSECONDS;
			while (bl < el) {
				String[] oneDay = new String[2];
				oneDay[1] = new SimpleDateFormat(format).format(new Date(el - ONE_DAY_MILLISSECONDS));
				el = el - ONE_DAY_MILLISSECONDS * interval;
				oneDay[0] = new SimpleDateFormat(format).format(new Date(el));
				result.add(oneDay);
			}
			return result;
		} catch (Exception e) {
			throw new OperationException("时间转换出错,message:" + e);
		}
	}

	/**
	 * 根据年月获取当月的开始日期
	 *
	 * @param year
	 * @param month
	 * @return
	 */
	public static Date getBeginTime(int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate localDate = yearMonth.atDay(1);
		LocalDateTime startOfDay = localDate.atStartOfDay();
		ZonedDateTime zonedDateTime = startOfDay.atZone(ZoneId.of("Asia/Shanghai"));

		return Date.from(zonedDateTime.toInstant());
	}

	/**
	 * 根据年月获取当月的结束日期
	 *
	 * @param year
	 * @param month
	 * @return
	 */
	public static Date getEndTime(int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate endOfMonth = yearMonth.atEndOfMonth();
		LocalDateTime localDateTime = endOfMonth.atTime(23, 59, 59, 999);
		ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Shanghai"));
		return Date.from(zonedDateTime.toInstant());
	}

	/**
	 * 获得两个日期字符串之间的所有日期 + 小时字符串字符串对象
	 *
	 * @param beginDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @param dateFormat
	 *            日期格式
	 * @return 日期 + 小时列表
	 * @author songbinbin 2017-5-3 09:52:40
	 */
	public static List<String> getBetweenDateHourString(String beginDate, String endDate, String dateFormat) {
		Vector<GregorianCalendar> v = new Vector<GregorianCalendar>();
		List<String> list = new ArrayList<String>();
		if (StringUtil.isEmpty(beginDate) || StringUtil.isEmpty(endDate)) {
			return list;
		}
		if (beginDate.compareTo(endDate) > 0) {
			return list;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		GregorianCalendar gc1 = new GregorianCalendar();
		GregorianCalendar gc2 = new GregorianCalendar();
		try {
			gc1.setTime(sdf.parse(beginDate));
			gc2.setTime(sdf.parse(endDate));
		} catch (ParseException e) {
			return list;
		}
		do {
			GregorianCalendar gc3 = (GregorianCalendar) gc1.clone();
			v.add(gc3);
			gc1.add(Calendar.DAY_OF_MONTH, 1);
		} while (!gc1.after(gc2));
		for (GregorianCalendar g : v) {
			for (int i = 0; i < 24; i++) {
				if (i >= 10) {
					list.add(DateUtil.format2String(g.getTime(), yyyyMMdd2) + i + "0000");
				} else {
					list.add(DateUtil.format2String(g.getTime(), yyyyMMdd2) + "0" + i + "0000");
				}
			}
		}
		return list;
	}

	/**
	 * 获取两个时间段之前的小时
	 *
	 * 要求开始时间和结束时间为同一天(单条微博分析——被转载趋势图)
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public static List<String> getBetweenDateHourString2(String beginDate, String endDate) {
		List<String> list = new ArrayList<String>();
		String start = "";
		String end = "";
		start = beginDate.substring(8,10);
		end = endDate.substring(8,10);

		int i1 = Integer.parseInt(end) - Integer.parseInt(start);
		for (int i = 0; i <= (Integer.parseInt(end)-Integer.parseInt(start)); i++) {
			String value = String.valueOf(Integer.parseInt(start) + i);
			if (value.length()==1){
				value = "0"+value;
			}
			if ("00".equals(value)){//每天的00点要加上具体年月日
				value = stringToStringDate(beginDate.substring(0, 8), yyyyMMdd2, yyyyMMdd3);
			}
			list.add(value);
		}
		return list;
	}
	/**
	 * 获取当前时间内的所有时间 小时 例如 2017.01.01 01 2017.01.01 02
	 *
	 * @return list
	 * @author songbinbin 2017年5月3日
	 */
	public static List<String> getCurrentDateHours() {
		List<String> list = new ArrayList<String>();
		Date time = new Date();
		String current = date2String(time, yyyyMMddHH);
		// 获取当前时间的小时,并计算今天所有时间加上小时。
		list = getDateHourString(date2String(time, yyyyMMdd2), getDateHour(current, yyyyMMddHH));
		return list;
	}

	// 格式变化
	public static List<String> getCurrentDateHours3() {
		List<String> list = new ArrayList<String>();
		Date time = new Date();
		String current = date2String(time, yyyyMMddHH);
		// 获取当前时间的小时,并计算今天所有时间加上小时。
		list = getDateHourString2(date2String(time, "yyyy-MM-dd"), getDateHour(current, yyyyMMddHH));
		return list;
	}

	// 格式变化
	public static List<String> getDateHourString2(String date, int endHour) {
		List<String> list = new ArrayList<String>();
		if (StringUtil.isEmpty(date)) {
			date = DateUtil.formatCurrentTime("yyyy-dd-mm");
		}
		for (int i = 0; i <= endHour; i++) {
			if (i >= 10) {
				list.add(i + ":00:00");
			} else {
				list.add("0" + i + ":00:00");
			}
		}
		return list;
	}

	public static List<String> getCurrentDateHours2() {
		List<String> list = new ArrayList<String>();
		Date time = new Date();
		String current = date2String(time, yyyyMMddHH);
		// 获取当前时间的小时,并计算今天所有时间加上小时。
		list = getDateHourString(date2String(time, yyyyMMdd2), getDateHour(current, yyyyMMddHH));
		return list;
	}

	/**
	 * 获取24小时内时间格式
	 *
	 * @author songbinbin 2017年5月3日15:48:11
	 */
	public static List<String> get24Hours() {
		List<String> list = new ArrayList<String>();
		Date time = new Date();
		Calendar date = Calendar.getInstance();
		date.setTime(time);
		date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);
		Date startTime = date.getTime();
		list.addAll(getDateBeforHourString(date2String(startTime, yyyyMMdd2),
				getDateHour(date2String(startTime, yyyyMMddHH), yyyyMMddHH)));
		list.addAll(getDateHourString(date2String(time, yyyyMMdd2),
				getDateHour(date2String(startTime, yyyyMMddHH), yyyyMMddHH)));
		return list;
	}

	public static List<String> get24Hour() {
		List<String> list = new ArrayList<String>();
		Date time = new Date();
		Calendar date = Calendar.getInstance();
		date.setTime(time);
		date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);
		Date startTime = date.getTime();
		list.addAll(getDateBeforHourString2(date2String(startTime, yyyyMMddHH),
				getDateHour(date2String(startTime, yyyyMMddHH), yyyyMMddHH)));
		list.addAll(getDateHour(date2String(time, yyyyMMddHH),
				getDateHour(date2String(startTime, yyyyMMddHH), yyyyMMddHH)));
		return list;
	}

	// yyyyMMddHH2
	// 前一天24小时
	public static List<String> get24Hours2() {
		List<String> list = new ArrayList<String>();
		Date time = new Date();
		Calendar date = Calendar.getInstance();
		date.setTime(time);
		date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);
		Date startTime = date.getTime();
		list.addAll(getDateBeforHourString(date2String(startTime, yyyyMMdd3),
				getDateHour(date2String(startTime, yyyyMMdd3), yyyyMMdd3)));
		list.addAll(getDateHourString(date2String(time, yyyyMMdd3),
				getDateHour(date2String(startTime, yyyyMMdd3), yyyyMMdd3)));
		return list;
	}

	/**
	 * 获取指定日期后所有小时字符串,
	 *
	 * @param date
	 *            日期
	 * @param endHour
	 *            截止小时
	 * @return 小时列表
	 * @since songbinbin @ 2017年5月3日16:48:45
	 */
	public static List<String> getDateBeforHourString(String date, int endHour) {
		List<String> list = new ArrayList<String>();
		if (StringUtil.isEmpty(date)) {
			date = DateUtil.formatCurrentTime(FMT_TRS_yMd);
		}
		for (int i = endHour; i < 24; i++) {
			if (i >= 10) {
				list.add(date + " " + i + ":00:00");
			} else {
				list.add(date + " " + "0" + i + ":00:00");
			}
		}
		return list;
	}

	/**
	 * 不带符号 只返回小时
	 * 
	 * @param date
	 * @param endHour
	 * @return
	 */
	public static List<String> getDateBeforHourString2(String date, int endHour) {
		List<String> list = new ArrayList<String>();
		if (StringUtil.isEmpty(date)) {
			date = DateUtil.formatCurrentTime(FMT_TRS_yMd);
		}
		for (int i = endHour; i < 24; i++) {
			if (i >= 10) {
				list.add(i + "");
			} else {
				list.add("0" + i);
			}
		}
		return list;
	}

	/**
	 * 得到前n天的说所有日期列表
	 */
	public static List<String> getDataStinglist(int n) {
		// 得到前n天的数据
		Calendar now = Calendar.getInstance();
		String endDate = date2String(now.getTime(), yyyyMMddHHmmss);
		now.add(Calendar.DATE, -n);
		String beginDate = date2String(now.getTime(), yyyyMMddHHmmss);
		// 获取日期list
		return getBetweenDateString(beginDate, endDate, yyyyMMddHHmmss);
	}

	/**
	 * 得到前n天的说所有日期列表
	 */
	public static List<String> getDataStinglist2(int n) {
		// 得到前n天的数据
		Calendar now = Calendar.getInstance();
		String endDate = date2String(now.getTime(), yyyyMMdd);
		now.add(Calendar.DATE, -n);
		String beginDate = date2String(now.getTime(), yyyyMMdd);
		// 获取日期list
		return getBetweenDateString(beginDate, endDate, yyyyMMdd);
	}
	
	/**
	 * 得到前n天的说所有日期列表
	 * 根据传入的时间格式决定返回的时间格式
	 */
	public static List<String> getDataStinglist2(int n,String format) {
		// 得到前n天的数据
		Calendar now = Calendar.getInstance();
		String endDate = date2String(now.getTime(), format);
		now.add(Calendar.DATE, -n);
		String beginDate = date2String(now.getTime(), format);
		// 获取日期list
		return getBetweenDateString(beginDate, endDate, format);
	}

	/**
	 * 将传入的时间格式化
	 * 
	 * @param time
	 *            传入的时间标志
	 * @return String[]
	 * @throws OperationException
	 *             Exception
	 */
	public static String[] formatTimeRange(String time) throws OperationException {
		// 可以不选择结束时间 以"至今"判断
		String[] timeArray = new String[2];
		// 检查time字符串是否符合要求,不符合要求抛出异常
		if (StringUtils.isEmpty(time)) {
			throw new OperationException("传入的时间为空");
		}

		time = time.replace("至今", formatCurrentTime("yyyy-MM-dd HH:mm:ss"));

		Pattern pattern1 = Pattern.compile("[0-9]*[hdwmyn]");
		Pattern pattern2 = Pattern
				.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2};\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
		if (pattern1.matcher(time).matches()) {
			// 如果满足xh/d/w/m/y(代表:近x时/天/周/月/年)这种格式
			int timeNum = Integer.parseInt(time.substring(0, time.length() - 1));
			//timeNum = timeNum>0?timeNum-1:0;
			char timeFlag = time.charAt(time.length() - 1);
			timeArray[1] = new SimpleDateFormat(yyyyMMddHHmmss).format(new Date());
			switch (timeFlag) {
				case 'h':
					timeArray[0] = getDateBefore(timeNum+1, Calendar.HOUR_OF_DAY);
					break;
				case 'd':
					timeArray[0] = getDateBefore(timeNum, Calendar.DAY_OF_MONTH);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				case 'w':
					timeArray[0] = getDateBefore(timeNum, Calendar.WEEK_OF_MONTH);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				case 'm':
					timeArray[0] = getDateBefore(timeNum, Calendar.MONTH);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				case 'y':
					timeArray[0] = getDateBefore(timeNum+1, Calendar.YEAR);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				case 'n':
					timeArray[0] = getDateBefore(timeNum+1, Calendar.MINUTE);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				default:
					break;
			}
			return timeArray;
		} else if (pattern2.matcher(time).matches()) {
			String dString = time.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
			return dString.split(";");
		} else {
			throw new OperationException("传入了不支持的时间格式");
		}
	}

	/**
	 * n-1 * 24 + 当天时间
	 * @param time
	 * @return
	 * @throws OperationException
	 */
	public static String[] formatTimeRange2(String time) throws OperationException {
		// 可以不选择结束时间 以"至今"判断
		String[] timeArray = new String[2];
		// 检查time字符串是否符合要求,不符合要求抛出异常
		if (StringUtils.isEmpty(time)) {
			throw new OperationException("传入的时间为空");
		}

		time = time.replace("至今", formatCurrentTime("yyyy-MM-dd HH:mm:ss"));

		Pattern pattern1 = Pattern.compile("[0-9]*[hdwmyn]");
		Pattern pattern2 = Pattern
				.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2};\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
		if (pattern1.matcher(time).matches()) {
			// 如果满足xh/d/w/m/y(代表:近x时/天/周/月/年)这种格式
			int timeNum = Integer.parseInt(time.substring(0, time.length() - 1));
			char timeFlag = time.charAt(time.length() - 1);
			timeArray[1] = new SimpleDateFormat(yyyyMMddHHmmss).format(new Date());
			switch (timeFlag) {
				case 'h':
					timeArray[0] = getDateBefore(timeNum, Calendar.HOUR_OF_DAY);
					break;
				case 'd':
//				if (timeNum > 0) {
//					timeNum = timeNum - 1;
//				}
					timeArray[0] = getDateBefore(timeNum, Calendar.DAY_OF_MONTH);
					break;
				case 'w':
					timeArray[0] = getDateBefore(timeNum, Calendar.WEEK_OF_MONTH);
					break;
				case 'm':
					timeArray[0] = getDateBefore(timeNum, Calendar.MONTH);
					break;
				case 'y':
					timeArray[0] = getDateBefore(timeNum, Calendar.YEAR);
					break;
				case 'n':
					timeArray[0] = getDateBefore(timeNum, Calendar.MINUTE);
					break;
				default:
					break;
			}
			return timeArray;
		} else if (pattern2.matcher(time).matches()) {
			String dString = time.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
			return dString.split(";");
		} else {
			throw new OperationException("传入了不支持的时间格式");
		}
	}
	/*
	* @Description: 将传入的时间格式化，并减一天，比如3d。3-21~3-23，较上面方法少了20号这一天.而且开始时间是从零点零分零秒算起
	* @param: time
	* @return: java.lang.String[]
	* @Author: Maguocai
	* @create time: 2020/3/23 11:52
	*/
	public static String[] formatTimeRangeMinus1(String time) throws OperationException {
		// 可以不选择结束时间 以"至今"判断
		String[] timeArray = new String[2];
		// 检查time字符串是否符合要求,不符合要求抛出异常
		if (StringUtils.isEmpty(time)) {
			throw new OperationException("传入的时间为空");
		}

		time = time.replace("至今", formatCurrentTime("yyyy-MM-dd HH:mm:ss"));

		Pattern pattern1 = Pattern.compile("[0-9]*[hdwmyn]");
		Pattern pattern2 = Pattern
				.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2};\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
		if (pattern1.matcher(time).matches()) {
			// 如果满足xh/d/w/m/y(代表:近x时/天/周/月/年)这种格式
			int timeNum = Integer.parseInt(time.substring(0, time.length() - 1));
			//timeNum = timeNum>0?timeNum-1:0;
			char timeFlag = time.charAt(time.length() - 1);
			timeArray[1] = new SimpleDateFormat(yyyyMMddHHmmss).format(new Date());
			switch (timeFlag) {
				case 'h':
					timeArray[0] = getDateBefore(timeNum+1, Calendar.HOUR_OF_DAY);
					break;
				case 'd':
					timeArray[0] = getDateBefore(timeNum, Calendar.DAY_OF_MONTH);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				case 'w':
					timeArray[0] = getDateBefore(timeNum, Calendar.WEEK_OF_MONTH);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				case 'm':
					timeArray[0] = getDateBefore(timeNum, Calendar.MONTH);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				case 'y':
					timeArray[0] = getDateBefore(timeNum+1, Calendar.YEAR);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				case 'n':
					timeArray[0] = getDateBefore(timeNum+1, Calendar.MINUTE);
					timeArray[0] = timeArray[0].substring(0,8)+"000000";
					break;
				default:
					break;
			}
			return timeArray;
		} else if (pattern2.matcher(time).matches()) {
			String dString = time.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
			return dString.split(";");
		} else {
			throw new OperationException("传入了不支持的时间格式");
		}
	}

	/**
	 * 将传入的时间格式化 为 yyyy/MM/dd HH:mm:ss 主要是折线图部分需要
	 *
	 * @param time
	 *            传入的时间标志
	 * @return String[]
	 * @throws OperationException
	 *             Exception
	 */
	public static String[] formatTimeRange_Line(String time) throws OperationException {
		// 可以不选择结束时间 以"至今"判断
		String[] timeArray = new String[2];
		// 检查time字符串是否符合要求,不符合要求抛出异常
		if (StringUtils.isEmpty(time)) {
			throw new OperationException("传入的时间为空");
		}
		time = time.replace("至今", formatCurrentTime("yyyy-MM-dd HH:mm:ss"));
		Pattern pattern1 = Pattern.compile("[0-9]*[hdwmyn]");
		Pattern pattern2 = Pattern
				.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2};\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
		if (pattern1.matcher(time).matches()) {
			// 如果满足xh/d/w/m/y(代表:近x时/天/周/月/年)这种格式
			int timeNum = Integer.parseInt(time.substring(0, time.length() - 1));
			char timeFlag = time.charAt(time.length() - 1);
			timeArray[1] = new SimpleDateFormat(yyyyMMddHHmmss_Line).format(new Date());
			switch (timeFlag) {
				case 'h':
					timeArray[0] = getDateBefore_line(timeNum, Calendar.HOUR_OF_DAY);
					break;
				case 'd':
					timeArray[0] = getDateBefore_line(timeNum, Calendar.DAY_OF_MONTH);
					break;
				case 'w':
					timeArray[0] = getDateBefore_line(timeNum, Calendar.WEEK_OF_MONTH);
					break;
				case 'm':
					timeArray[0] = getDateBefore_line(timeNum, Calendar.MONTH);
					break;
				case 'y':
					timeArray[0] = getDateBefore_line(timeNum, Calendar.YEAR);
					break;
				case 'n':
					timeArray[0] = getDateBefore_line(timeNum, Calendar.MINUTE);
					break;
				default:
					break;
			}
			return timeArray;
		} else if (pattern2.matcher(time).matches()) {
			String dString = time.replaceAll("-", "/");
			return dString.split(";");
		} else {
			throw new OperationException("传入了不支持的时间格式");
		}
	}

	// 保留格式
	public static String[] formatRange(String time) throws OperationException {
		String[] timeArray = new String[2];
		// 检查time字符串是否符合要求,不符合要求抛出异常
		if (StringUtils.isEmpty(time)) {
			throw new OperationException("传入的时间为空");
		}
		Pattern pattern1 = Pattern.compile("[0-9]*[hdwmy]");
		Pattern pattern2 = Pattern
				.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2};\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
		if (pattern1.matcher(time).matches()) {
			// 如果满足xh/d/w/m/y(代表:近x时/天/周/月/年)这种格式
			int timeNum = Integer.parseInt(time.substring(0, time.length() - 1));
			char timeFlag = time.charAt(time.length() - 1);
			timeArray[1] = new SimpleDateFormat(yyyyMMddHHmmss).format(new Date());
			switch (timeFlag) {
			case 'h':
				timeArray[0] = getDateBefore(timeNum, Calendar.HOUR_OF_DAY);
				break;
			case 'd':
				timeArray[0] = getDateBefore(timeNum, Calendar.DAY_OF_MONTH);
				break;
			case 'w':
				timeArray[0] = getDateBefore(timeNum, Calendar.WEEK_OF_MONTH);
				break;
			case 'm':
				timeArray[0] = getDateBefore(timeNum, Calendar.MONTH);
				break;
			case 'y':
				timeArray[0] = getDateBefore(timeNum, Calendar.YEAR);
				break;
			default:
				break;
			}
			return timeArray;
		} else if (pattern2.matcher(time).matches()) {
			return time.split(";");
		} else {
			throw new OperationException("传入了不支持的时间格式");
		}
	}

	/**
	 * 获取几天前的时间
	 *
	 * @param num
	 *            时间
	 * @param field
	 *            字段
	 * @return String
	 */
	private static String getDateBefore(int num, int field) {
		Calendar cal = Calendar.getInstance();
		if (num == 0 && field == Calendar.DAY_OF_MONTH) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		} else {
			cal.add(field, -num);
		}
		return new SimpleDateFormat(yyyyMMddHHmmss).format(cal.getTime());
	}
	/**
	 * 获取几天前的时间 格式为yyyy/MM/dd HH:mm:ss 主要是折线图部分需要
	 *
	 * @param num
	 *            时间
	 * @param field
	 *            字段
	 * @return String
	 */
	private static String getDateBefore_line(int num, int field) {
		Calendar cal = Calendar.getInstance();
		if (num == 0 && field == Calendar.DAY_OF_MONTH) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		} else {
			cal.add(field, -num);
		}
		return new SimpleDateFormat(yyyyMMddHHmmss_Line).format(cal.getTime());
	}

	/**
	 * n天前的凌晨时间
	 *
	 * @return Date
	 */
	public static Date getMorningBefore(int n) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.DAY_OF_MONTH, -n);
		return cal.getTime();
	}

	/**
	 * 获取两个时间直接的所有月份
	 *
	 * @param minDate
	 *            小
	 * @param maxDate
	 *            大
	 * @return List
	 */
	public static List<String> getMonthBetween(Date minDate, Date maxDate) {
		ArrayList<String> result = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");// 格式化为年月
		Calendar min = Calendar.getInstance();
		Calendar max = Calendar.getInstance();
		min.setTime(minDate);
		min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
		max.setTime(maxDate);
		max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);
		while (min.before(max)) {
			result.add(sdf.format(min.getTime()));
			min.add(Calendar.MONTH, 1);
		}
		return result;
	}

	/**
	 * 获取当前时间往后一定毫秒之后的时间
	 */
	public static Date getTimeNow() {
		long now = new Date().getTime() + 1800000;
		Date d = new Date(now);
		return d;
	}

	/**
	 * 获取当前日期是星期几<br>
	 * 
	 * @date Created at 2018年3月15日 下午9:14:17
	 * @Author 谷泽昊
	 * @param date
	 * @return
	 */
	public static String getWeekOfDate(Date date) {
		String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;
		return weekDays[w];
	}

	/**
	 * 获得两个日期字符串之间的所有日期字符串对象
	 * 
	 * @date Created at 2018年3月29日 下午6:16:18
	 * @Author 谷泽昊
	 * @param beginDate
	 * @param endDate
	 * @param dateFormat
	 *            beginDate 和 endDate时间格式
	 * @param dateFormatTwo
	 *            转成list的时间格式
	 * @return
	 */
	public static List<String> getBetweenDateString(String beginDate, String endDate, String dateFormat,
			String dateFormatTwo) {
		Vector<GregorianCalendar> v = new Vector<GregorianCalendar>();
		List<String> list = new ArrayList<String>();
		if (StringUtil.isEmpty(beginDate) || StringUtil.isEmpty(endDate)) {
			return list;
		}
		if (beginDate.compareTo(endDate) > 0) {
			return list;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		GregorianCalendar gc1 = new GregorianCalendar();
		GregorianCalendar gc2 = new GregorianCalendar();
		try {
			gc1.setTime(sdf.parse(beginDate));
			gc2.setTime(sdf.parse(endDate));
		} catch (ParseException e) {
			return list;
		}
		do {
			GregorianCalendar gc3 = (GregorianCalendar) gc1.clone();
			v.add(gc3);
			gc1.add(Calendar.DAY_OF_MONTH, 1);
		} while (!gc1.after(gc2));
		for (GregorianCalendar g : v) {
			list.add(DateUtil.format2String(g.getTime(), dateFormatTwo));
		}
		return list;
	}

	/**
	 * 获取传入时间往后推24小时
	 * 
	 * @date Created at 2018年3月29日 下午6:30:38
	 * @Author 谷泽昊
	 * @param nowDate
	 * @param dateFormat
	 * @return
	 */
	public static List<String> getNowDateHourString(String nowDate, String dateFormat) {
		List<String> list = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date parse = null;
		try {
			parse = sdf.parse(nowDate);

			String format2String = DateUtil.format2String(parse, "HH");
			for (int i = 0; i < 24; i++) {
				Integer valueOf = Integer.valueOf(format2String);
				valueOf += i;
				String hour = null;
				if (valueOf >= 24) {
					valueOf -= 24;
				}
				if (valueOf < 10) {
					hour = "0" + valueOf;
				} else {
					hour = String.valueOf(valueOf);
				}
				list.add(hour);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * timeRange 为0d 处理
	 * 
	 * @param nowDate
	 * @param endDate
	 * @param dateFormat
	 * @return
	 */
	public static List<String> getCurrentDateHourString(String nowDate, String endDate, String dateFormat) {
		List<String> list = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date startParse = null;
		Date endParse = null;
		try {
			startParse = sdf.parse(nowDate);
			endParse = sdf.parse(endDate);

			String startString = DateUtil.format2String(startParse, "HH");
			String endString = DateUtil.format2String(endParse, "HH");
			int endHour = Integer.parseInt(endString);
			for (int i = 0; i <= endHour; i++) {
				Integer valueOf = Integer.valueOf(startString);
				valueOf += i;
				String hour = null;
				if (valueOf > endHour) {
					valueOf -= endHour;
				}
				if (valueOf < 10) {
					hour = "0" + valueOf;
				} else {
					hour = String.valueOf(valueOf);
				}
				list.add(hour);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 获取指点日期范围内所有日期,包含起止日期
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @param begin
	 *            起始日期
	 * @param end
	 *            截止日期
	 * @return
	 * @Return : List<String>
	 */
	public static List<String> subDateRangeToList(String begin, String end) {
		List<String> days = new ArrayList<>();
		SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd");
		process(begin, end, smf, days);
		return days;
	}

	private static void process(String date1, String date2, SimpleDateFormat format, List<String> days) {
		if (date1.equals(date2)) {
			days.add(date1);
			return;
		}

		String tmp;
		if (date1.compareTo(date2) > 0) { // 确保 date1的日期不晚于date2
			tmp = date1;
			date1 = date2;
			date2 = tmp;
		}

		tmp = format.format(str2Date(date1, format).getTime() + 3600 * 24 * 1000);

		days.add(date1);
		while (tmp.compareTo(date2) < 0) {
			days.add(tmp);
			tmp = format.format(str2Date(tmp, format).getTime() + 3600 * 24 * 1000);
		}
		days.add(date2);
	}

	private static Date str2Date(String str, SimpleDateFormat format) {
		if (str == null)
			return null;

		try {
			return format.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 时间字符串的list排序 从小到大
	 * 
	 * @param data
	 * @param format
	 * @return
	 */
	public static void compareTime(List<String> data, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Collections.sort(data, new Comparator<String>() {

			public int compare(String o1, String o2) {

				Date d1;
				Date d2;
				try {
					d1 = (Date) f.parseObject(o1);
					d2 = (Date) f.parseObject(o2);
					return d1.compareTo(d2);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return -1;
			}
		});
	}

	/**
	 * 判断两个时间是否为同一天
	 * @date Created at 2018年8月15日  下午5:17:10
	 * @Author 谷泽昊
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 != null && date2 != null) {
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(date1);
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(date2);
			return isSameDay(cal1, cal2);
		} else {
			throw new IllegalArgumentException("The date must not be null");
		}
	}

	/**
	 * 判断两个时间是否为同一天
	 * @date Created at 2018年8月15日  下午5:17:37
	 * @Author 谷泽昊
	 * @param cal1
	 * @param cal2
	 * @return
	 */
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 != null && cal2 != null) {
			return cal1.get(0) == cal2.get(0) && cal1.get(1) == cal2.get(1) && cal1.get(6) == cal2.get(6);
		} else {
			throw new IllegalArgumentException("The date must not be null");
		}
	}
	
	/**
	 * 前端传来一串时间进行构造builder
	 * @return
	 * @throws TRSException 
	 */
	public static QueryBuilder timeBuilder(String urlTime) throws TRSException{
		QueryBuilder builder = new QueryBuilder();
		//限制时间范围查库 时间不存在时底层限制在一个月内导致有些信息查询不到
		if(StringUtil.isNotEmpty(urlTime)){
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
			Date start = null;
			Date end = null;
			String startString ;
			String endString ;
			String[] split = urlTime.split(";");
			if(split.length>1){
				List<String> list = new ArrayList<>();
				for(String s:split){
					list.add(s.replace("-", "").replace(" ", "").replace(":", "").substring(0, 8));
				}
				DateUtil.compareTime(list,DateUtil.yyyyMMdd2);
				try {
					start = sdf.parse(list.get(0));
					end = sdf.parse(list.get(list.size()-1));
					startString = list.get(0);
					endString = list.get(list.size()-1);
				} catch (ParseException e) {
					throw new TRSException(e);
				}
			}else{
				try {
					start = sdf.parse(split[0]);
					end = new Date();
					startString = split[0].replace("-", "").replace(" ", "").replace(":", "").substring(0, 8);
					endString = sdf.format(end);
				} catch (ParseException e) {
					throw new TRSException(e);
				}
			}
			builder.setStartTime(start);
			builder.setEndTime(end);
			builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString+"000000",endString+"235959"}, Operator.Between);
		}
		return builder;
	}
	/**
	 * 前端传来一串时间进行构造builder
	 * @return
	 * @throws TRSException
	 */
	public static QueryCommonBuilder timeCommonBuilder(String urlTime) throws TRSException{
		QueryCommonBuilder builder = new QueryCommonBuilder();
		//限制时间范围查库 时间不存在时底层限制在一个月内导致有些信息查询不到
		if(StringUtil.isNotEmpty(urlTime)){
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd2);
			Date start = null;
			Date end = null;
			String startString ;
			String endString ;
			String[] split = urlTime.split(";");
			if(split.length>1){
				List<String> list = new ArrayList<>();
				for(String s:split){
					list.add(s.replace("-", "").replace(" ", "").replace(":", "").substring(0, 8));
				}
				DateUtil.compareTime(list,DateUtil.yyyyMMdd2);
				try {
					start = sdf.parse(list.get(0));
					end = sdf.parse(list.get(list.size()-1));
					startString = list.get(0);
					endString = list.get(list.size()-1);
				} catch (ParseException e) {
					throw new TRSException(e);
				}
			}else{
				try {
					start = sdf.parse(split[0]);
					end = new Date();
					startString = split[0].replace("-", "").replace(" ", "").replace(":", "").substring(0, 8);
					endString = sdf.format(end);
				} catch (ParseException e) {
					throw new TRSException(e);
				}
			}
			builder.setStartTime(start);
			builder.setEndTime(end);
			builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString+"000000",endString+"235959"}, Operator.Between);
		}
		return builder;
	}

	/**
	 * 根据文章时间获取搜索时间,
	 *
	 * @date Created at 2018年8月16日 下午3:37:39
	 * @Author 谷泽昊
	 * @param date 			文章时间
	 * @param format		格式化
	 * @param day			文章时间前后时间范围
	 * @return
	 */
	public static  String[] getTimeInterval(Date date, String format, int day) {
		String beginTime = null;
		String endTime = null;
		int now = DateUtil.rangBetweenNow(date);
		if (now == 0) {
			beginTime = DateUtil.formatDateAfter(date, format, -day * 2);
			endTime = DateUtil.formatCurrentTime(format);
		} else {
			beginTime = DateUtil.formatDateAfter(date, format, -day * 2 - now);
			endTime = DateUtil.formatDateAfter(date, format,  -now);
		}
		return new String[] { beginTime, endTime };
	}
	
	/**
	 * 获取距指定时间一段距离的时间
	 * @date Created at 2018年11月9日  上午10:57:25
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param date
	 * @param relativeDay
	 * @return
	 */
	public static Date dateAfter(Date date, int relativeDay) {
		if (date == null) {
			return null;
		}
		Calendar calendarTemp = Calendar.getInstance();
		calendarTemp.setTime(date);
		calendarTemp.add(Calendar.DAY_OF_YEAR, relativeDay);
		return calendarTemp.getTime();
	}

	/**
	 * 判断两个时间之间的时间差  根据通栏、半栏判断24 或48小时
	 *
	 * @param startTime 开始时间
	 * @param endTime   结束时间
	 * @param tabWidth  通栏  半栏  三级栏目宽度 50为半栏 100为通栏
	 * @return boolean
	 */
	public static Integer judgeTime24Or48(String startTime, String endTime, String tabWidth) {
		Boolean flag = false;
		if (StringUtils.equals(tabWidth, "100")) {
			flag = true;
		}
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		//字符串转换成LocalDateTime类型
		LocalDateTime start_Time = LocalDateTime.parse(startTime, dtf);
		LocalDateTime end_Time = LocalDateTime.parse(endTime, dtf);
		LocalDateTime mid_Time1 = start_Time.plusHours(24);
		LocalDateTime mid_Time2 = start_Time.plusHours(48);
		//折线图改为 不管是通栏还是半栏，只要是48小时内，就默认按小时展示
		if (mid_Time1.isAfter(end_Time)) {//在时间范围内返回true//小于24小时  按小时展示且不分组
			return 0;
		} else if (mid_Time2.isAfter(end_Time) && flag) {//小于48且是通栏  需要分组
			return 1;
		} else if (mid_Time2.isAfter(end_Time) && !flag) {//小于48且是半栏
			return 1;
		} else {//大于48  按天
			return 3;
		}
	}

	/**
	 * 获取传入时间之间所有的天的起止时间
	 *
	 * @param start 开始时间
	 * @param end   结束时间
	 * @return
	 */
	public static List<String[]> getBetweenTimeOfStartToEnd(String start, String end) {
		List<String[]> list = new ArrayList<>();
		String[] array_start = new String[2];
		array_start[0] = start;
		array_start[1] = start.substring(0, 8) + "235959";
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(yyyyMMddHHmmss);
		DateTimeFormatter dtf_mid = DateTimeFormatter.ofPattern("yyyyMMdd");
		//字符串转换成LocalDateTime类型
		LocalDateTime start_Time = LocalDateTime.parse(start, dtf);
		LocalDateTime end_Time = LocalDateTime.parse(end, dtf);
		LocalDateTime mid_time = start_Time.plusDays(1);
		LocalDate start_date = start_Time.toLocalDate();
		LocalDate end_date = end_Time.toLocalDate();
		LocalDate mid_date;
		Boolean flag = true;
		while (flag) {
			String[] array = new String[2];
			mid_date = mid_time.toLocalDate();
			if (start_date.isEqual(end_date)) {//在同一天 直接存储并返回
				array_start[1] = end;
				flag = false;
			}
			if (list.size() == 0) {
				list.add(array_start);
			}
			if (mid_date.isEqual(end_date)) {//最后一天相等
				array[0] = end.substring(0, 8) + "000000";
				array[1] = end;
				list.add(array);
				flag = false;

			} else if (mid_date.isBefore(end_date)) {
				array[0] = mid_date.format(dtf_mid) + "000000";
				array[1] = mid_date.format(dtf_mid) + "235959";
				list.add(array);
				mid_time = mid_time.plusDays(1);
			}
		}
		return list;
	}

	/**
	 * 获取传入时间之间小时数 开始时间与结束在一天内 返回值带有当天日期
	 *
	 * @param start 开始时间
	 * @param end   结束时间
	 * @return
	 */
	public static List<String> getStartToEndOfHour(String start, String end) {
		List<String> list = new ArrayList<>();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(yyyyMMddHHmmss);
		DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		//字符串转换成LocalDateTime类型
		LocalDateTime start_Time = LocalDateTime.parse(start, dtf);
		LocalDateTime end_Time = LocalDateTime.parse(end, dtf);
		String day = start_Time.toLocalDate().format(dtf1);
		int start_num = start_Time.getHour();
		int end_num;
		if (StringUtils.equals(end.substring(8, end.length()), "235959")) {//代表这一天的最后一秒
			end_num = 23;
		} else {
			end_num = end_Time.getHour();
		}
		String h;
		for (int i = start_num; i <= end_num; i++) {
			if (i < 10) {
				h = day + " 0" + i + ":00";
			} else {
				h = day + " " + i + ":00";
			}
			list.add(h);
		}
		return list;
	}
	public static List<String> getStartToEndOfHourNew(String start, String end) {
		List<String> list = new ArrayList<>();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(yyyyMMddHHmmss);
		DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyyMMdd");
		//字符串转换成LocalDateTime类型
		LocalDateTime start_Time = LocalDateTime.parse(start, dtf);
		LocalDateTime end_Time = LocalDateTime.parse(end, dtf);
		String day = start_Time.toLocalDate().format(dtf1);
		int start_num = start_Time.getHour();
		int end_num;
		if (StringUtils.equals(end.substring(8, end.length()), "235959")) {//代表这一天的最后一秒
			end_num = 23;
		} else {
			end_num = end_Time.getHour();
		}
		String h;
		for (int i = start_num; i <= end_num; i++) {
			if (i < 10) {
				h = day + "0" + i + "0000";
			} else {
				h = day + "" + i + "0000";
			}
			list.add(h);
		}
		return list;
	}
	/**
	 * 获取传入时间之间小时数 开始时间与结束在一天内 无当天日期
	 *
	 * @param start 开始时间
	 * @param end   结束时间
	 * @return
	 */
	public static List<String> getHourOfHH(String start, String end) {
		List<String> list = new ArrayList<>();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(yyyyMMddHHmmss);
		//字符串转换成LocalDateTime类型
		LocalDateTime start_Time = LocalDateTime.parse(start, dtf);
		LocalDateTime end_Time = LocalDateTime.parse(end, dtf);
		int start_num = start_Time.getHour();
		int end_num;
		if (StringUtils.equals(end.substring(8, end.length()), "235959")) {//代表这一天的最后一秒
			end_num = 23;
		} else {
			end_num = end_Time.getHour();
		}
		String h;
		for (int i = start_num; i <= end_num; i++) {
			if (i < 10) {
				h = "0" + i;
			} else {
				h = i + "";
			}
			list.add(h);
		}
		return list;
	}

	/**
	 * 获取传入时间之间小时数 开始与结束间隔不超过24小时  无当天日期
	 *
	 * @param start 开始时间
	 * @param end   结束时间
	 * @return
	 */
	public static List<String> getHHLess24(String start, String end) {
		List<String> list = new ArrayList<>();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(yyyyMMddHHmmss);
		DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		//字符串转换成LocalDateTime类型
		LocalDateTime start_Time = LocalDateTime.parse(start, dtf);
		LocalDateTime end_Time = LocalDateTime.parse(end, dtf);
		LocalDate start_date = start_Time.toLocalDate();
		LocalDate end_date = end_Time.toLocalDate();
		if (start_date.isEqual(end_date)) {//一天
			int start_num1 = start_Time.getHour();
			int end_num1;
			if (StringUtils.equals(end.substring(8, end.length()), "235959")) {//代表这一天的最后一秒
				end_num1 = 23;
			} else {
				end_num1 = end_Time.getHour();
			}
			String h;
			for (int i = start_num1; i <= end_num1; i++) {
				if (i < 10) {
					h = "0" + i;
				} else {
					h = i + "";
				}
				list.add(h);
			}
		} else {
			int start_num1 = start_Time.getHour();
			int end_num1 = 23;
			int start_num2 = 0;
			int end_num2;
			if (StringUtils.equals(end.substring(8, end.length()), "235959")) {//代表这一天的最后一秒
				end_num2 = 23;
			} else {
				end_num2 = end_Time.getHour();
			}
			String h;
			for (int i = start_num1; i <= end_num1; i++) {
				if (i < 10) {
					h = "0" + i;
				} else {
					h = i + "";
				}
				list.add(h);
			}
			for (int i = start_num2; i <= end_num2; i++) {
				if (i < 10) {
					h = "0" + i;
				} else {
					h = i + "";
				}
				list.add(h);
			}
		}
		return list;
	}
	/**
	 * 获取一个月之前的时间
	 *
	 * @return
	 */
	public static String getTimeBeforeOneMonth() {

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(yyyyMMdd);
		LocalDateTime currentTime = LocalDateTime.parse(formatCurrentTime(yyyyMMdd), dtf);
		LocalDateTime beforeTime = currentTime.minusDays(30);
		return beforeTime.format(dtf);
	}
	/**
	 * 计算指定时间  开始到现在不超过七天
	 *
	 * @param
	 * @return
	 */
	public static String[] getTimeToSevenDay(String start,String end) {
		String[] timeArray = new String[2];
		timeArray[0] = start;
		timeArray[1] = end;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(yyyyMMddHHmmss);
		//字符串转换成LocalDateTime类型
		LocalDateTime startTime = LocalDateTime.parse(timeArray[0], dtf);
		LocalDateTime endTime = LocalDateTime.parse(timeArray[timeArray.length - 1], dtf);
		LocalDateTime midTime = endTime.minusDays(7);
		if (startTime.isBefore(midTime)) {
			timeArray[0] = midTime.format(dtf);
			timeArray[timeArray.length - 1] = endTime.format(dtf);
		}else{
			timeArray[0] = startTime.format(dtf);
			timeArray[timeArray.length - 1] = endTime.format(dtf);
		}
		return timeArray;
	}
	/**
	 * 获取几个月之前的时间 年月日格式的
	 *
	 * @return
	 */
	public static String getTimeBeforeSomeMonthForYMD(int num,String getFormatter) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(getFormatter);
		LocalDate currentTime = LocalDate.parse(formatCurrentTime(getFormatter), dtf);
		LocalDate beforeTime = currentTime.minusMonths(num);
		return beforeTime.format(dtf);
	}

	/**
	 * 获取指定时间几个小时前的时间
	 *
	 * @return
	 */
	public static String[] setTimeForSomeMinute(String time,int num) {
		String[] array = new String[2];
		DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern(yyyyMMdd);
		DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern(yyyyMMddHHmmss);
		LocalDateTime date = LocalDateTime.parse(time, dtf1);
		LocalDateTime beforeTime = date.minusMinutes(num);
		array[0] = beforeTime.format(dtf2);
		array[1] = date.format(dtf2);
		return array;
	}

	/**
	 * 是否为yyyy-MM-dd HH:mm:ss格式
	 *
	 * @param date
	 *            日期
	 * @return 是否为小时格式
	 * @since zhanghu @ 2013-1-31
	 */
	public static boolean isTimeFormatter(String date) {
		Pattern pattern = Pattern
				.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
		Matcher matcher = pattern.matcher(date);
		return matcher.find();
	}

	public static String getDataToTime(Date date){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}

	/**
	 * 获取昨天的日期
	 * @return
	 */
	public static String getYesToday(){
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DATE,-1);
		Date d=cal.getTime();


		SimpleDateFormat sp=new SimpleDateFormat("yyyyMMdd");
		String ZUOTIAN=sp.format(d);
		return ZUOTIAN;

	}

	/**
	 * 是否为yyyy-MM-dd HH:mm:ss格式
	 *
	 * @param date
	 *            日期
	 * @return 是否为小时格式
	 * @since zhanghu @ 2013-1-31
	 */
	public static boolean isTimeFormatterYMD(String date) {
		Pattern pattern1 = Pattern
				.compile("\\d{4}-\\d{2}-\\d{2}");
		Matcher matcher1 = pattern1.matcher(date);
		Pattern pattern2 = Pattern
				.compile("\\d{4}/\\d{2}/\\d{2}");
		Matcher matcher2 = pattern2.matcher(date);
		if(matcher1.find() || matcher2.find()){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 是否为yyyy-MM-dd HH:mm:ss格式
	 *
	 * @param date
	 *            日期
	 * @return 是否为小时格式
	 * @since zhanghu @ 2013-1-31
	 */
	public static boolean isTimeFormatterYMDH(String date) {
		Pattern pattern1 = Pattern
				.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
		Matcher matcher1 = pattern1.matcher(date);
		Pattern pattern2 = Pattern
				.compile("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}");
		Matcher matcher2 = pattern2.matcher(date);
		if(matcher1.find() || matcher2.find()){
			return true;
		}else{
			return false;
		}
	}



}