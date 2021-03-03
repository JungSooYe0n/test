package com.trs.netInsight.util;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trs.dev4.jdk16.utils.StringHelper;

import com.trs.netInsight.config.constant.Const;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import ognl.OgnlException;

import org.apache.commons.lang3.StringUtils;

/**
 * String工具类
 *
 * Created by yan.changjiang on 2017/2/15.
 */
public final class StringUtil {

	public static final String nbsp = "&nbsp;";
	public static final String nbsp1 = "nbsp;";
	public static final String nbsp2 = " nbsp;";
	public static final String nbsps = "&nbsp;|";
	public static final String quot = "&quot;";
	public static final String period = ".";
	public static final String period1 = "。";
	public static final String semicolon = ";";
	public static final String semicolon1 = "；";
	public static final String exclamation = "!";
	public static final String exclamation1 = "！";
	public static final String img = "(?=&lt;IMAGE&nbsp;SRC=).+?(?=&nbsp;&gt;)";
	public static final String img2 = "(?=&lt;IMAGE&nbsp;SRC=).+?(?=&quot;&gt;)";
	public static final String img3 = "(?=&lt;IMAGE&nbsp;IDX=).+?(?=&nbsp;&gt;)";
	public static final String img4 = "(?=&lt;img&nbsp;src=).+?(?=&nbsp;&gt;)";
	public static final String img5 = "(?=&lt;img&nbsp;src=).+?(?=&quot;&gt;)";

	public static final String img6 = "(?=&lt;IMAGE&nbsp;ALT=).+?(?=&nbsp;&gt;)";
	public static final String img7 = "(?=&lt;IMAGE&nbsp;ALT=).+?(?=&quot;&gt;)";
	public static final String img8 = "(?=&lt;img&nbsp;alt=).+?(?=&nbsp;&gt;)";
	public static final String img9 = "(?=&lt;img&nbsp;alt=).+?(?=&quot;&gt;)";

	public static final String img11 = "(?=&lt;IMAGE&nbsp;ALT=).+?(?=&nbsp;/&gt;)";
	public static final String img12 = "(?=&lt;IMAGE&nbsp;ALT=).+?(?=&quot;/&gt;)";
	public static final String img13 = "(?=&lt;img&nbsp;alt=).+?(?=&nbsp;/&gt;)";
	public static final String img14 = "(?=&lt;img&nbsp;alt=).+?(?=&quot;/&gt;)";

	public static final String img20 = "(?=<IMAGE SRC=).+?(?=>)";
	public static final String img21 = "(?=<IMAGE SRC=).+?(?=\">)";
	public static final String img22 = "(?=<IMG SRC=).+?(?=>)";
	public static final String img23 = "(?=<IMG SRC=).+?(?=\">)";
	public static final String img24 = "(?=<img src=).+?(?=>)";
	public static final String img25 = "(?=<img src=).+?(?= \">)";
	public static final String img26 = "(?=&lt;img&nbsp;src=).+?(?=&nbsp;/&gt;)";


	public static final String video1 = "(?=&lt;video&nbsp;src=).+?(?=&quot;&gt;)";
	public static final String video2 = "(?=&lt;AUDIO&nbsp;SRC=).+?(?=&quot;&gt;)";
	public static final String video3 = "<video src=";
	public static final String video4 = "<VIDEO SRC=";
	public static final String videoSuffix1 = "&lt;/video&gt;";
	public static final String videoSuffix2 = "</video>";
	public static final String aHref1 = "(?=&lt;a).+?(?=&quot;&gt;)";
	public static final String aHref2 = "(?=<a).+?(?=&quot;>)";
	public static final String gt = "&gt;";
	public static final String gt1 = ">";
	public static final String rn = "\r\n";
	public static final String annotation1 = "(?=&lt;!--).+?(?=--&gt;)";
	public static final String annotationSuffix1 = "--&gt;";
	public static final String annotation2 = "(?=<!--).+?(?=-->)";
	public static final String annotationSuffix2 = "-->";
	public static final String font1 = "<font color='red'>";// ;<font
															// color=red>相思苦</font>
	public static final String font2 = "</font>";
	public static final String font3 = "<font color=red>";
	// Emoji表情字符
	public static final String emoji1 = "[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]";
	public static final String emoji2 = "[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]";

	public static String replaceEmoji(String content) {
		if (isNotEmpty(content)) {
			content = content.replaceAll(emoji1, "").replaceAll(emoji2, "");
		}
		return content;
	}

	/**
	 * 判断字符串是否为null或空.
	 *
	 * @param str
	 *            字符串
	 * @return true or false.
	 */
	public static boolean isEmpty(String str) {
		return (str == null || str.trim().length() == 0);
	}

	/**
	 * 避免不完整的<font>标签结尾导致大面积标红
	 * 
	 * @param content
	 *            原来的字符串
	 * @param length
	 *            要截取的长度
	 * @return
	 */
	public static String notFontEnd(String content, int length) {
		if (StringUtils.isNotBlank(content)) {
			if (content.length() > length) {
				content = content.substring(0, length);
				// 解决大片标红
				if (content.contains("<font color=red>")) {
					String endsContent = content.substring(content.lastIndexOf("<font color=red>"),
							content.length());
					if (!endsContent.contains("</font>")) {
						if (endsContent.contains("</")) {
							content = content.substring(0, content.lastIndexOf("</"))+"</font>" + "...";
						} else {
							content = content + "</font>...";
						}
					}
				}
				// String end = content.lastIndexOf("<f", content.length()-1);
				if (-1 != content.lastIndexOf("<f")) {
					String end = content.substring(content.lastIndexOf("<f"), content.length());
					if (end.length() < "<font color=red>".length() && !"<font color=red>".equals(end)) {
						content = content.substring(0, content.lastIndexOf("<f")) + "...";
					}
				}
				if (!content.endsWith("...")) {
					content = content + "...";
				}
			}
		}
		return content;
	}


	/**
	 * 解决因含有标签导致标题截取过短
	 *
	 * @param content
	 *            原来的字符串
	 * @param length
	 *            要截取的长度
	 * @return
	 */
	public static String calcuCutLength(String content, int length) {
		if (StringUtils.isNotBlank(content)) {
			if (content.length() > length) {
				String content_new = content.replaceAll("&nbsp;"," ").replaceAll("<font color=red>","").replaceAll("</font>","");
				int length_new = length;
				String nbsp = "&nbsp;";
				//font是一组，查的时候查前缀，加长度的时候要算上后缀
				String font1 = "<font color=red>";
				String font2 = "<font color=red></font>";
				//如果有标签在后边，但是先检索到了，也会导致截取出错
				List<Integer> list1 = check(content, nbsp);
				List<Integer> list2 = check(content, font1);
				if (check(content, nbsp).size() > 0 || check(content, font1).size() > 0) {
					length_new = addCheckLength(length_new, nbsp.length()-1,font2.length(), list1,list2);

				}
				content = notFontEnd(content, length_new);
			}
		}
		return content;
	}
	/**
	 * 增加原来要截取的长度
	 *
	 * @param length
	 *            原来要截取的长度
	 * @param num1
	 *            要增加的长度--标签的长度
	 * @param sites1
	 * 			  含有的标签的位置
	 * @return
	 */
	public static Integer addCheckLength(int length, int num1,int num2, List<Integer> sites1 ,List<Integer> sites2) {
		int length_new = length;
		if (sites1.size() > 0 || sites2.size() >0) {
			List<Integer> list = new ArrayList<>();
			list.addAll(sites1);
			list.addAll(sites2);
			Collections.sort(list);

			for (int i = 0; i <list.size(); i++) {
				int site = list.get(i);
				if(site < length_new){
					if(sites1.contains(site)){
						length_new =length_new +num1;
					}else if(sites2.contains(site)){
						length_new =length_new +num2;
					}
				}
			}
		}
		return length_new;
	}

	public static List<Integer> check(String str,String subStr){
		List<Integer> list = new ArrayList<>();
		int t = str.length();
		do{
			t = str.lastIndexOf(subStr,t-1);
			if(t==-1){
				break;
			}
			list.add(t);
		}while(t!=-1);
		return list;
	}

	/**
	 * 过滤导出excel时的font标签
	 * 
	 * @param title
	 * @return xiaoying
	 */
	public static String replaceFont(String title) {
		if (StringUtils.isBlank(title)) {
			return title;
		}
		Pattern pat = Pattern.compile(font1);
		Matcher mat = pat.matcher(title);
		title = mat.replaceAll("");
		pat = Pattern.compile(font2);
		mat = pat.matcher(title);
		title = mat.replaceAll("");
		pat = Pattern.compile(quot);
		mat = pat.matcher(title);
		title = mat.replaceAll("");
		return title;
	}

	/**
	 * 过滤导出excel时的font标签
	 *
	 * @param title
	 * @return xiaoying
	 */
	public static String replacePeriod(String title) {
		if (StringUtils.isBlank(title)) {
			return title;
		}
		if (title.contains(period) || title.contains(period1) || title.contains(semicolon) || title.contains(semicolon1)
				|| title.contains(exclamation) || title.contains(exclamation1)) {
			// title = title.replaceAll(period," ");
			title = title.replaceAll(period1, " ");
			// title = title.replaceAll(semicolon," ");
			title = title.replaceAll(semicolon1, " ");
			// title = title.replaceAll(exclamation," ");
			title = title.replaceAll(exclamation1, " ");
		}
		return title;
	}

	/*
	 *  这个方法主要是替换掉标题中的 <！--   --->  标签符，因为content中的需要替换的更多，分开写
	 */
	public static String replaceAnnotation(String content) {
		if (StringUtils.isBlank(content)) {
			return content;
		}

		content = content.replaceAll(annotation1,"");
		content = content.replaceAll(annotation2,"");
		content = content.replaceAll(annotationSuffix1,"");
		content = content.replaceAll(annotationSuffix2,"");
		return content;
	}

	public static String replaceImg(String content) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		Pattern pat = Pattern.compile(img);
		Matcher mat = pat.matcher(content);
		content = content.replaceAll(img, "");
		content = content.replaceAll(img2, "");
		content = content.replaceAll(img3,"");
		content = content.replaceAll(img4,"");
		content = content.replaceAll(img5,"");

		content = content.replaceAll(img6,"");
		content = content.replaceAll(img7,"");
		content = content.replaceAll(img8,"");
		content = content.replaceAll(img9,"");
		content = content.replaceAll(img11,"");
		content = content.replaceAll(img12,"");
		content = content.replaceAll(img13,"");
		content = content.replaceAll(img14,"");
		content = content.replaceAll(img26,"");
		content = content.replaceAll(video1,"");
		content = content.replaceAll(video2,"");
		content = content.replaceAll(videoSuffix1,"");
		content = content.replaceAll(videoSuffix2,"");
		content = content.replaceAll(aHref1,"");
		content = content.replaceAll(aHref2,"");

		//去掉多余的注释标签
		content = content.replaceAll(annotation1,"");
		content = content.replaceAll(annotation2,"");
		content = content.replaceAll(annotationSuffix1,"");
		content = content.replaceAll(annotationSuffix2,"");
		content = content.replaceAll("---","");

		pat = Pattern.compile(nbsp);
		mat = pat.matcher(content);
		content = mat.replaceAll(" ");

		pat = Pattern.compile(nbsp1);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(nbsp2);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(nbsps);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(quot);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(gt);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(rn);
		mat = pat.matcher(content);
		content = mat.replaceAll("");
		return content;
	}

	public static String replaceImgNew(String content) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		Pattern pat = Pattern.compile(img);
		Matcher mat = pat.matcher(content);
		content = content.replaceAll(img26,"");
		content = content.replaceAll(img, "");
		content = content.replaceAll(img2, "");
		content = content.replaceAll(img3,"");
		content = content.replaceAll(img4,"");
		content = content.replaceAll(img5,"");

		content = content.replaceAll(img6,"");
		content = content.replaceAll(img7,"");
		content = content.replaceAll(img8,"");
		content = content.replaceAll(img9,"");
		content = content.replaceAll(img11,"");
		content = content.replaceAll(img12,"");
		content = content.replaceAll(img13,"");
		content = content.replaceAll(img14,"");

		content = content.replaceAll(img20,"");
		content = content.replaceAll(img21,"");
		content = content.replaceAll(img22,"");
		content = content.replaceAll(img23,"");
		content = content.replaceAll(img24,"");
		content = content.replaceAll(img25,"");


		content = content.replaceAll(video1,"");
		content = content.replaceAll(video2,"");
		content = content.replaceAll(videoSuffix1,"");
		content = content.replaceAll(videoSuffix2,"");
		content = content.replaceAll(aHref1,"");
		content = content.replaceAll(aHref2,"");

		//去掉多余的注释标签
		content = content.replaceAll(annotation1,"");
		content = content.replaceAll(annotation2,"");
		content = content.replaceAll(annotationSuffix1,"");
		content = content.replaceAll(annotationSuffix2,"");

		pat = Pattern.compile(nbsp);
		mat = pat.matcher(content);
		content = mat.replaceAll(" ");

		pat = Pattern.compile(nbsp1);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(nbsp2);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(nbsps);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(quot);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(gt);
		mat = pat.matcher(content);
		content = mat.replaceAll("");
//		pat = Pattern.compile(gt1);
//		mat = pat.matcher(content);
//		content = mat.replaceAll("");

		pat = Pattern.compile(rn);
		mat = pat.matcher(content);
		content = mat.replaceAll("");
		return content;
	}

	public static String replaceImgNew2(String content) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		Pattern pat = Pattern.compile(img);
		Matcher mat = pat.matcher(content);
		content = content.replaceAll(img, "");
		content = content.replaceAll(img2, "");
		content = content.replaceAll(img3,"");
		content = content.replaceAll(img4,"");
		content = content.replaceAll(img5,"");

		content = content.replaceAll(img6,"");
		content = content.replaceAll(img7,"");
		content = content.replaceAll(img8,"");
		content = content.replaceAll(img9,"");
		content = content.replaceAll(img11,"");
		content = content.replaceAll(img12,"");
		content = content.replaceAll(img13,"");
		content = content.replaceAll(img14,"");

		content = content.replaceAll(img20,"");
		content = content.replaceAll(img21,"");
		content = content.replaceAll(img22,"");
		content = content.replaceAll(img23,"");
		content = content.replaceAll(img24,"");
		content = content.replaceAll(img25,"");
        content = content.replaceAll("<img src=","");
		content = content.replaceAll("<IMAGE SRC=","");
		content = content.replaceAll("<img class=","");
		content = content.replaceAll("<img ","");
		content = content.replaceAll("<IMAGE ","");
		//content = content.replaceAll("<font color='...","");
		content = content.replaceAll(video1,"");
		content = content.replaceAll(video2,"");
		content = content.replaceAll(video3,"");
		content = content.replaceAll(video4,"");
		content = content.replaceAll(videoSuffix1,"");
		content = content.replaceAll(videoSuffix2,"");
		content = content.replaceAll(aHref1,"");
		content = content.replaceAll(aHref2,"");
		content = content.replaceAll("<a","");
		content = content.replaceAll("<=","");
		content = content.replaceAll("p=","");
		content = content.replaceAll("a=","");

		content = content.replaceAll("---","");

		//去掉多余的注释标签
		content = content.replaceAll(annotation1,"");
		content = content.replaceAll(annotation2,"");
		content = content.replaceAll(annotationSuffix1,"");
		content = content.replaceAll(annotationSuffix2,"");

		pat = Pattern.compile(nbsp);
		mat = pat.matcher(content);
		content = mat.replaceAll(" ");

		pat = Pattern.compile(nbsp1);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(nbsp2);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(nbsps);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(quot);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

		pat = Pattern.compile(gt);
		mat = pat.matcher(content);
		content = mat.replaceAll("");
		pat = Pattern.compile(gt1);
		mat = pat.matcher(content);
		content = mat.replaceAll("");
		content = content.replaceAll("</font","</font>");
		content = content.replaceAll("<font color='red'","<font color='red'>");

		pat = Pattern.compile(rn);
		mat = pat.matcher(content);
		content = mat.replaceAll("");
		return content;
	}

	/**
	 * 去除\n \r \t
	 * 
	 * @date Created at 2018年3月21日 上午11:16:39
	 * @Author 谷泽昊
	 * @param content
	 * @return
	 */
	public static String replaceNRT(String content) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		content = content.replaceAll("\n|\r|\t", "");
		return content;
	}

	/**
	 * 校验邮箱格式
	 *
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email) {
		String check = "^([a-z0-9A-Z]+[-|_.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		// String check = "\\p{Alpha}\\w{2,15}[@][a-z0-9]{3,}[.]\\p{Lower}{2,}";
		// String check = "^[http:\\]?[www.]?\\w+@\\w+\\.[a-z]{2,}$";
		Pattern regex = Pattern.compile(check);
		Matcher matcher = regex.matcher(email);
		return matcher.matches();
	}

	/**
	 * 校验多个邮箱
	 * 
	 * @param email
	 * @return
	 */
	public static boolean checkEmailMore(String email) {
		String[] split = email.split(";");
		for (int i = 0; i < split.length; i++) {
			String check = "^([a-z0-9A-Z]+[-|_.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			// String check =
			// "\\p{Alpha}\\w{2,15}[@][a-z0-9]{3,}[.]\\p{Lower}{2,}";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(split[i]);
			boolean matches = matcher.matches();
			if (!matches) {
				return matches;
			}
		}

		return true;
	}

	public static boolean checkPhone(String mobileNumber) {
		Pattern regexPhone = Pattern
				.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
		Matcher matcherPhone = regexPhone.matcher(mobileNumber);
		return matcherPhone.matches();
	}

	/**
	 * 判断字符串是否为null或空.
	 *
	 * @param str
	 *            字符串
	 * @return true or false.
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 判断中文
	 *
	 * @param a
	 *            char
	 * @return boolean
	 */
	public static boolean isChinese(char a) {
		int v = (int) a;
		return (v >= 19968 && v <= 171941);
	}

	public static String join(Object[] array, String separator) {
		return array == null ? null : join(array, separator, array.length);
	}

	private static String join(Object[] array, String separator, int endIndex) {
		if (array == null) {
			return null;
		} else {
			int bufSize = endIndex;
			if (bufSize <= 0) {
				return "";
			} else {
				bufSize *= (array[0] == null ? 16 : array[0].toString().length()) + 1;
				StringBuilder buf = new StringBuilder(bufSize);

				for (int i = 0; i < endIndex; ++i) {
					if (i > 0) {
						buf.append(separator);
					}

					if (array[i] != null) {
						buf.append(array[i]);
					}
				}

				return buf.toString();
			}
		}
	}

	public static String avoidNull(String str) {
		return (str == null) ? "" : str;
	}

	/**
	 * 字符串编码
	 */
	public static String encode(String source) {
		if (StringUtil.isEmpty(source)) {
			return source;
		}
		StringBuilder result = new StringBuilder();
		char[] sourceArray = source.toCharArray();
		for (char ch : sourceArray) {
			result.append(fillString(base62String(ch)));
		}
		return result.toString();
	}

	/**
	 * 字符串解码
	 */
	public static String decode(String code) {
		if (StringUtil.isEmpty(code)) {
			return code;
		}
		char[] codeChar = code.toCharArray();
		int[] baseInt = new int[3];
		StringBuilder source = new StringBuilder();
		for (int i = 0; i < codeChar.length; i++) {
			int index = -1;
			// noinspection StatementWithEmptyBody
			while (++index < 62 && BASE_CHAR[index] != codeChar[i]) {

			}
			if (index == 62) {// error
				return null;
			}
			baseInt[i % 3] = index;
			if (i % 3 == 2) {
				source.append((char) (baseInt[0] * 62 * 62 + baseInt[1] * 62 + baseInt[2]));
			}
		}
		return source.toString();
	}

	/**
	 * 62个基本字符
	 */
	private static final char[] BASE_CHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
			'V', 'W', 'X', 'Y', 'Z' };

	/**
	 * 将char的int值转成62进制
	 *
	 * @param charAt
	 *            当前字符的int值
	 * @return String
	 */
	private static String base62String(int charAt) {
		String str;
		if (charAt == 0) {
			return "";
		} else {
			// 用baseChar[i]表示 <62 的余数
			str = base62String(charAt / 62);
			return str + BASE_CHAR[charAt % 62];
		}
	}

	/**
	 * 将不足3位的补0
	 *
	 * @param base62Str
	 *            62进制char值
	 * @return 3位String
	 */
	private static String fillString(String base62Str) {
		StringBuilder fillStr = new StringBuilder();
		for (int i = base62Str.length(); i < 3; i++) {
			fillStr.append(BASE_CHAR[0]);
		}
		return fillStr + base62Str;
	}

	/**
	 * 将list变成String
	 * 
	 * @date Created at 2018年1月31日 下午2:37:49
	 * @Author 谷泽昊
	 * @param list
	 * @return
	 */
	public static String toString(List<Map<String, String>> list,int start) {
		StringBuffer buffer = new StringBuffer();
		if (list != null && list.size() > 0) {
			int i = start;
			for (Map<String, String> map : list) {
				//这个地方把字体红色标签去掉了  因为微信推送不识别font标签
				buffer.append(i + "、").append(replaceNRT(replaceFont(replaceImg(map.get("title"))))).append("\\n\\n");
				i++;
			}
			// 去除最后的\\n\\n
			if (buffer.length() > 4) {
				buffer.delete(buffer.length() - 4, buffer.length());
			}
		}
		return buffer.toString();
	}

	/**
	 * 将list变成String
	 *
	 * @date Created at 2018年1月31日 下午2:37:49
	 * @Author 谷泽昊
	 * @param list
	 * @return
	 */
	public static String getTitleList(List<String> list,int start) {
		StringBuffer buffer = new StringBuffer();
		if (list != null && list.size() > 0) {
			int i = start;
			for (String title : list) {
				title = replaceNRT(replaceFont(replaceImg(title)));
				title = StringUtil.calcuCutLength(title, Const.ALERT_NUM);
				//这个地方把字体红色标签去掉了  因为微信推送不识别font标签
				title = title.replaceAll("<font color='red'>", "").replaceAll("<font color=red>", "").replaceAll("</font>", "");
				buffer.append(i + "、").append(title).append("\\n\\n");
				i++;
			}
			// 去除最后的\\n\\n
			if (buffer.length() > 4) {
				buffer.delete(buffer.length() - 4, buffer.length());
			}
		}
		return buffer.toString();
	}

	/**
	 * 替换四个字节的字符 '\xF0\x9F\x98\x84\xF0\x9F）的解决方案 😁
	 * 
	 * @date Created at 2018年3月15日 下午5:08:04
	 * @Author 谷泽昊
	 * @param content
	 * @return
	 */
	public static String removeFourChar(String content) {
		if (null != content) {
			byte[] conbyte = content.getBytes();
			for (int i = 0; i < conbyte.length; i++) {
				if ((conbyte[i] & 0xF8) == 0xF0) {
					for (int j = 0; j < 4; j++) {
						conbyte[i + j] = 0x30;
					}
					i += 3;
				}
			}
			content = new String(conbyte);
			return content;
//			return content.replaceAll("0000", "");
		}
		return null;
	}

	/**
	 * 内容标红定位显示
	 * 
	 * @param fieldValue
	 *            筛选内容
	 * @param iCutsize
	 *            显示长度
	 * @return
	 */
	public static String substringRed(String fieldValue, int iCutsize) {
		String returnValue = "";
		if (fieldValue != null) {
			try {
				if (StringHelper.isNotEmpty(fieldValue) && iCutsize > 0) {
					if (fieldValue.length() > iCutsize) {
						int flagValue = fieldValue.indexOf("red");
						if (flagValue != -1 && flagValue >= 50) {
							String file = "";
							String fileAfter = "";
							String fileBefore = "";
							file = fieldValue.substring(flagValue - 50, flagValue);// 长度50
							fileAfter = file.lastIndexOf("。") != -1
									? file.substring(file.lastIndexOf("。") + 1, file.length()) : "..." + file;
							fileBefore = fieldValue.length() - 1 > flagValue + 150
									? fieldValue.substring(flagValue, flagValue + 150) + "..."
									: fieldValue.substring(flagValue, fieldValue.length());
							returnValue = fileAfter + fileBefore;
						} else {
							returnValue = fieldValue.substring(0, iCutsize);
						}
					} else {
						returnValue = fieldValue;
					}
				}
			} catch (Exception e) {
				if (StringHelper.isNotEmpty(fieldValue) && iCutsize > 0 && fieldValue.length() > iCutsize) {
					returnValue = fieldValue.substring(0, iCutsize);
				} else {
					returnValue = fieldValue;
				}
			}
		}
		return returnValue;
	}

	/**
	 * 对文章的指定内容进行截取操作，第一个出现的关键字位置前后，截取总长度为size；或者没有标红的关键字，直接从开头截 size长度
	 * 
	 * @param content
	 *            需要截取的内容
	 * @param size
	 *            保留的长度
	 * @return
	 */
	public static String cutContent(String content, int size) {
		// 长度大于160才截取，否则不处理
		if (content.length() > size) {
			if (content.contains(font1)) {
				// 找到第一个关键字的位置
				int position = content.indexOf(font1);
				// 中间点
				int midpoint = content.length() / 2;
				// 关键词出现的地方后面剩余的长度
				int remain = content.length() - position;
				// 关键词出现在中点之前，长度又大于160的，直接取前面160个字符
				if (position <= midpoint) {
					content = content.substring(0, size);
					return content + "---";
					// 关键词出现在中点之后，中点之后的长度又大于80的，取中点前后共160字符长度
				} else if (position > midpoint && remain > size / 2) {
					content = content.substring(position - size / 2, position + size / 2);
					return "---" + content + "---";
					// 关键词出现在中点之后，中点之后的长度又小于80的，取最后的160字符
				} else if (position > midpoint && remain < size / 2) {
					content = content.substring(content.length() - size / 2, content.length());
					return "---" + content;
				}
				// 有些标题无标红关键字，但是长度需要控制
			} else {
				content = content.substring(0, size);
				return content + "---";
			}
		}

		return content;
	}
	/*
	* @Description: 截取内容用于列表展示(只允许带有Font标签)改进版方法
	* @param: content
	* @param: size
	* @return: java.lang.String
	* @Author: Maguocai
	* @create time: 2019/9/11 14:45
	*/
	public static String cutContentPro(String content, int size) {
		if(content==null){
			return content;
		}
		if(content!=null&&content.length()<=size){
			if (content.contains("<font color='red'>")) {
				String endsContent = content.substring(content.lastIndexOf("<font color='red'>"),
						content.length());
				if (!endsContent.contains("</font>")) {
					if (endsContent.contains("</")) {
						content = content.substring(0, content.lastIndexOf("</"))+"</font>";
					} else {
						content = content + "</font>";
					}
				}
			}
			// String end = content.lastIndexOf("<f", content.length()-1);
			if (-1 != content.lastIndexOf("<f")) {
				String end = content.substring(content.lastIndexOf("<f"), content.length());
				if (end.length() < "<font color='red'>".length() && !"<font color='red'>".equals(end)) {
					content = content.substring(0, content.lastIndexOf("<f"));
				}
			}
//			if (!content.endsWith("...")) {
//				content = content + "...";
//			}
			return content;
		}

		if (content.length() > size && content.contains(font1)) {
			String contentPro = content;
			// 找到第一个font标签开始位置
				int startPosition = content.indexOf(font1);
				//找到第一个font标签结束位置
				int endPosition = content.indexOf(font2)+7;
				if(endPosition<size){//第一个font标签都在size范围.
					for (int i =0;i<content.length()/23;i++){
						//将content重新赋值,第一个font后面的内容
						contentPro = contentPro.substring(endPosition,contentPro.length());
						int subInt = content.indexOf(contentPro);//截取点在完整content的位置

						if (contentPro.contains(font1)) {
							int start = contentPro.indexOf(font1)+subInt;
							int end = contentPro.indexOf(font2)+7+subInt;
							endPosition = contentPro.indexOf(font2)+7;

							if(end>size && start >=size){//循环内的标签只要不是卡在size之间.都可以直接截取
								content = content.substring(0,size);
								break;
							}else if(end>size && start<size) {//标签卡在size之间,那就多截取一点点.与size不完全符合,木办法
								content = content.substring(0, end);
								break;
							}

						}else {//后面没有font便签了,那就直接截取
							content = content.substring(0,size);
							break;
						}
					}
				}else if(endPosition>size && startPosition >=size){//第一个标签都不在size的范围之内,那无须顾虑,直接截取
					content = content.substring(0,size);
				}else if(endPosition>=size && startPosition<size){//标签卡在size之间,那就多截取一点点.与size不完全符合,木办法
					content = content.substring(0,endPosition);
				}

			if (content.contains("<font color='red'>")) {
				String endsContent = content.substring(content.lastIndexOf("<font color='red'>"),
						content.length());
				if (!endsContent.contains("</font>")) {
					if (endsContent.contains("</")) {
						content = content.substring(0, content.lastIndexOf("</"))+"</font>" + "...";
					} else {
						content = content + "</font>...";
					}
				}
			}
			// String end = content.lastIndexOf("<f", content.length()-1);
			if (-1 != content.lastIndexOf("<f")) {
				String end = content.substring(content.lastIndexOf("<f"), content.length());
				if (end.length() < "<font color='red'>".length() && !"<font color='red'>".equals(end)) {
					content = content.substring(0, content.lastIndexOf("<f")) + "...";
				}
			}
			if (!content.endsWith("...")) {
				content = content + "...";
			}
			return content ;
		}else {
			content = content.substring(0, size);
			if (content.contains("<font color='red'>")) {
				String endsContent = content.substring(content.lastIndexOf("<font color='red'>"),
						content.length());
				if (!endsContent.contains("</font>")) {
					if (endsContent.contains("</")) {
						content = content.substring(0, content.lastIndexOf("</"))+"</font>" + "...";
					} else {
						content = content + "</font>...";
					}
				}
			}
			// String end = content.lastIndexOf("<f", content.length()-1);
			if (-1 != content.lastIndexOf("<f")) {
				String end = content.substring(content.lastIndexOf("<f"), content.length());
				if (end.length() < "<font color='red'>".length() && !"<font color='red'>".equals(end)) {
					content = content.substring(0, content.lastIndexOf("<f")) + "...";
				}
			}
			if (!content.endsWith("...")) {
				content = content + "...";
			}
			return content;
		}
	}

	/*
	 * @Description: 截取内容用于列表展示(只允许带有Font标签)改进版方法
	 * @param: content
	 * @param: size
	 * @return: java.lang.String
	 * @Author: Likenan
	 * @create time: 2020/11/19 14:45
	 */
	public static String cutContentMd5(String content, int size) {
		if(content==null){
			return content;
		}
		if(content!=null&&content.length()<=size){
			if (content.contains(font3)) {
				String endsContent = content.substring(content.lastIndexOf(font3),
						content.length());
				if (!endsContent.contains("</font>")) {
					if (endsContent.contains("</")) {
						content = content.substring(0, content.lastIndexOf("</"))+"</font>";
					} else {
						content = content + "</font>";
					}
				}
			}
			// String end = content.lastIndexOf("<f", content.length()-1);
			if (-1 != content.lastIndexOf("<f")) {
				String end = content.substring(content.lastIndexOf("<f"), content.length());
				if (end.length() < font3.length() && !font3.equals(end)) {
					content = content.substring(0, content.lastIndexOf("<f"));
				}
			}
//			if (!content.endsWith("...")) {
//				content = content + "...";
//			}
			return content;
		}

		if (content.length() > size && content.contains(font3)) {
			String contentPro = content;
			// 找到第一个font标签开始位置
			int startPosition = content.indexOf(font3);
			//找到第一个font标签结束位置
			int endPosition = content.indexOf(font2)+7;
			if(endPosition<size){//第一个font标签都在size范围.
				for (int i =0;i<content.length()/23;i++){
					//将content重新赋值,第一个font后面的内容
					contentPro = contentPro.substring(endPosition,contentPro.length());
					int subInt = content.indexOf(contentPro);//截取点在完整content的位置

					if (contentPro.contains(font3)) {
						int start = contentPro.indexOf(font3)+subInt;
						int end = contentPro.indexOf(font2)+7+subInt;
						endPosition = contentPro.indexOf(font2)+7;

						if(end>size && start >=size){//循环内的标签只要不是卡在size之间.都可以直接截取
							content = content.substring(0,size);
							break;
						}else if(end>size && start<size) {//标签卡在size之间,那就多截取一点点.与size不完全符合,木办法
							content = content.substring(0, end);
							break;
						}

					}else {//后面没有font便签了,那就直接截取
						content = content.substring(0,size);
						break;
					}
				}
			}else if(endPosition>size && startPosition >=size){//第一个标签都不在size的范围之内,那无须顾虑,直接截取
				content = content.substring(0,size);
			}else if(endPosition>=size && startPosition<size){//标签卡在size之间,那就多截取一点点.与size不完全符合,木办法
				content = content.substring(0,endPosition);
			}

			if (content.contains(font3)) {
				String endsContent = content.substring(content.lastIndexOf(font3),
						content.length());
				if (!endsContent.contains("</font>")) {
					if (endsContent.contains("</")) {
						content = content.substring(0, content.lastIndexOf("</"))+"</font>" + "...";
					} else {
						content = content + "</font>...";
					}
				}
			}
			// String end = content.lastIndexOf("<f", content.length()-1);
			if (-1 != content.lastIndexOf("<f")) {
				String end = content.substring(content.lastIndexOf("<f"), content.length());
				if (end.length() < font3.length() && !font3.equals(end)) {
					content = content.substring(0, content.lastIndexOf("<f")) + "...";
				}
			}
			if (!content.endsWith("...")) {
				content = content + "...";
			}
			return content ;
		}else {
			content = content.substring(0, size);
			if (content.contains(font3)) {
				String endsContent = content.substring(content.lastIndexOf(font3),
						content.length());
				if (!endsContent.contains("</font>")) {
					if (endsContent.contains("</")) {
						content = content.substring(0, content.lastIndexOf("</"))+"</font>" + "...";
					} else {
						content = content + "</font>...";
					}
				}
			}
			// String end = content.lastIndexOf("<f", content.length()-1);
			if (-1 != content.lastIndexOf("<f")) {
				String end = content.substring(content.lastIndexOf("<f"), content.length());
				if (end.length() < font3.length() && !font3.equals(end)) {
					content = content.substring(0, content.lastIndexOf("<f")) + "...";
				}
			}
			if (!content.endsWith("...")) {
				content = content + "...";
			}
			return content;
		}
	}
	/*
	 * @Description: 截取正文 - 在命中第一个font标签的情况下，截取100个字符
	 */
	public static String cutContentByFont(String content, int size) {
		//先去掉首尾的空格和换行
		while(content.indexOf(" ") ==0 || content.indexOf("\n") ==0){
			content = content.trim();
			if(content.indexOf("\n") == 0){
				content = content.substring(1,content.length());
			}
		}


		if(content.length()<=size){
			return content;
		}
		// 截取规则改变，想让标红的字靠前显示，所以如果在前，直接从开头截取，如果太靠近结尾，从结尾开始截取
		// 如果在中间，则从开始标签前30个字开始截取，如果这30个字内有句号分号或换行，则从这三种符号开始截取
		if (content.length() > size && content.contains(font1)) {
			//需要截取文本  注意 截取的文本必须保证是一对font标签
			int beforeSize = size > 100 ? 30: size/5; //暂时这么写，保证第一个标签在前面
			// 找到第一个font标签开始位置
			int startPosition = content.indexOf(font1);
			//找到第一个font标签结束位置
			int endPosition = content.indexOf(font2)+7;
			//第一个标红字体的长度 - 算上标签
			int redWord = endPosition-startPosition;
			int countLength = content.length();
			String contentNew = content;
			if(redWord > size){ //标红比截取长,直接截取标红+。。。
				contentNew= contentNew.substring(startPosition,endPosition);
				if(startPosition ==0){
					contentNew = "..."+contentNew;
				}
				return contentNew+"...";
			}else{
				//如果标签在开头或者结尾，就直接按字数截取，如果在中间部分，则需要找到靠前的一个句号或分号，从那个开始按字数截取
				if(countLength == endPosition){
					// 标红标签比截取长度短，且文本总长大于截取长度，只有一个标签，且结尾在文本结尾
					int endCut = endPosition-size;
					contentNew= contentNew.substring(endCut,endPosition);
					return "..."+contentNew+"...";
				}else{
					int startCut = 0;
					int endCut = size;
					int  fontAvg = (startPosition+ endPosition) /2;
					//1、开头标签在中点前，结尾标签在要截取的总长前，从头按要截取的总长截取
					if(startPosition <= beforeSize){
						if( endPosition >size){ //结尾标签在要截取的总长后，需要重新计算截取长度
							//截取 以标红为中心截取
							startCut = 0;
							endCut = endPosition;
						}
					}else if(startPosition >=beforeSize && startPosition >= countLength-size){ //3、开头标签在中点后，且整体都在文本结尾处
						endCut = countLength;
						startCut = countLength-size;
					}else{//2、第一组font标签整个都在后半段中且截取只需要判断开头结尾标签，在中间即可
						startCut = startPosition-beforeSize;
						endCut = startCut+size;
						String frontContent = content.substring(startCut,startPosition);
						int symbolIndex = frontContent.lastIndexOf("\n");
						if(symbolIndex == -1){
							symbolIndex = frontContent.lastIndexOf("。");
						}
						if(symbolIndex == -1){
							symbolIndex = frontContent.lastIndexOf("；");
						}
						if(symbolIndex != -1){
							startCut = symbolIndex +startCut+1;
							if(symbolIndex +endCut+1 < countLength){

								endCut = symbolIndex +endCut+1;
							}else{
								//当前从句号开始截取会超出文本长度
								endCut = countLength;
							}
						}
					}
					contentNew= contentNew.substring(startCut,endCut);
					/*
					需要判断截取的内容是否截取到的font标签中间
					 1、截取到了第一个font中间
					<font color=red>  之间
					也是找到结尾标签 可能不存在标签了，则保持原来的
					-- 截取到的标签成组，需要判断剩下的字符串 里面的第一个标签是结尾还是开头，
					2、在一对中间，正好是分割了
					<font color=red> </font>  之间如果是这样直接 找到后边那个，截取
					截取到的部分 标签不成组
					3、结尾标签中间
					</font>
					找到结尾标签。 截取到的部分 标签不成组
					 */

					int startFont = getNumForString(contentNew,font1);
					int endFont = getNumForString(contentNew,font2);
					String contentOther = content.substring(endCut,countLength);
					if(startFont == endFont){
						// 找到剩余字符串第一个font标签开始位置
						int startOther = contentOther.indexOf(font1);
						//找到剩余字符串第一个font标签结束位置
						int endOther = contentOther.indexOf(font2)+7;
						if(startOther > endOther || (startOther == -1 && endOther != -1)){
							endCut = endCut + endOther;
						}
					}else{
						//这是相对 截取到的文本算的
						int lastFont1 = contentNew.lastIndexOf(font1);
						//找到被截断的标红串
						String cutStr = contentNew.substring(lastFont1,contentNew.length());
						contentOther = cutStr+contentOther;
						int startOther = contentOther.indexOf(font1);
						//找到剩余字符串第一个font标签结束位置
						int endOther = contentOther.indexOf(font2)+7;
						endCut = startCut + lastFont1 +(endOther-startOther);//这个需要计算，存在标红是不同字符的情况
					}
					if(endCut > countLength){
						endCut = countLength;
					}
					if(startCut <=5){
						startCut = 0;
					}
					contentNew= content.substring(startCut,endCut);
					if(startCut != 0){
						contentNew = "..." + contentNew;
					}
					if(endCut != countLength){
						contentNew =  contentNew + "...";
					}
					return contentNew ;
				}
			}
		}else{
			content = content.substring(0, size);
			return content + "...";
		}
	}

	private static int getNumForString(String str,String pattern){
		int i = 0;
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		while (m.find()) {
			i++;
		}
		return i;
	}

	/***
	 * 对于微博数据，日常监测中会截取前160个字符， 但如果刚好第160个字符附近是<font .. 就会在页面出现不和谐的html字符
	 * 
	 * @param part
	 *            元字符串
	 * @return 去除html残缺片段的字符串
	 */
	public static String replacePartOfHtml(String part) {
		if (StringUtil.isEmpty(part)){
			return null;
		}
		if (part.length() < 160 || part.length() == 160) {
			return part;
		} else {
			String part1 = part.substring(0, 125);
			String part2 = part.substring(125, part.length());
			part2 = replaceFont(part2);
			part2 = replaceImg(part2);
			part2 = replaceEmoji(part2);
			part2 = part2.length() > 35 ? part2.substring(0, 35) : part2;
			return part1 + part2;
		}
	}

	/**
	 * 获取文章中图片
	 * 
	 * @date Created at 2018年8月16日 下午6:41:36
	 * @Author 谷泽昊
	 * @param htmlStr
	 * @return
	 */
	public static List<String> getImgStr(String htmlStr) {
		if (StringUtils.isBlank(htmlStr)) {
			return null;
		}
		List<String> pics = new ArrayList<>();
		String img = "";
		Pattern p_image;
		Matcher m_image;
		String regEx_img = "(&lt;|<)(img|IMAGE|IMG|image).*(src|SRC)\\s*=\\s*(.*?)[^>]*?(>|&gt;)";
		p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
		m_image = p_image.matcher(htmlStr);
		while (m_image.find()) {
			// 得到<img />数据
			img = m_image.group();
			// 匹配<img>中的src数据
//			Matcher m = Pattern.compile("(http:|https:)//[^\":<>]*\\.(jpg|bmp|gif|ico|pcx|jpeg|tif|png)").matcher(img);
			Matcher m = Pattern.compile("(http:|https:)//[^\":<>]*\\.(jpg|bmp|gif|ico|pcx|jpeg|tif|png).\\s*(.*?)[^>]*?(>|&gt;|&quot;|nbsp;)").matcher(img);
			while (m.find()) {
				pics.add(m.group().replace("&quot;/&gt;","").replace("&amp;","&").replace("&quot;","").replace("&gt;","").replace("&nbsp;",""));
			}
		}
		return pics;
	}

	/**
	 * 获得组合后的KEY值
	 * 
	 * @date Created at 2018年7月27日 下午4:28:17
	 * @Author 谷泽昊
	 * @param key
	 * @param map
	 * @return
	 */
	public static String getKeyNameFromParam(String key, Map<String, String[]> map,String rege) {
		if (!key.contains(rege)) {
			return key;
		}
		String regexp = "\\"+rege+"\\{[^\\}]+\\}";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(key);
		List<String> names = new ArrayList<String>();
		try {
			while (matcher.find()) {
				names.add(matcher.group());
			}
			key = executeNames(key, names, map);
		} catch (Exception e) {
			// log.error("Regex Parse Error!", e);
		}
		return key;
	}

	/**
	 * 对KEY中的参数进行替换
	 * 
	 * @date Created at 2018年7月27日 下午4:28:26
	 * @Author 谷泽昊
	 * @param key
	 * @param names
	 * @param map
	 * @return
	 * @throws OgnlException
	 */
	private static String executeNames(String key, List<String> names, Map<String, String[]> map) throws OgnlException {
		for (String name : names) {
			String temp = name.substring(2);
			temp = temp.substring(0, temp.length() - 1);
			List<String> asList = Arrays.asList(map.get(temp));
			if (asList != null && asList.size() > 0) {
				String keys = "";
				for (String string : asList) {
					keys += string;
				}
				key = StringUtils.replace(key, name, keys);
			}
		}
		return key;
	}

	/**
	 * 判断字符串内 关键字字数，只统计中文字数（排除掉中文的分号（;|；）和英文 逗号（,））
	 * @param keyWords
	 * @return
	 */
	public static int getChineseCount(String keyWords){
		if (StringUtil.isEmpty(keyWords)){
			return 0;
		}
		double valueLength = 0;
		String chinese = "[\u4e00-\u9fa5]";
		for (int i = 0; i < keyWords.length(); i++) {
			// 获取一个字符
			String temp = keyWords.substring(i, i + 1);
			// 判断是否为中文字符
			if (temp.matches(chinese)) {
				// 中文字符长度为1
				valueLength += 1;
				//} else if (!temp.contains(";|；|,")){
				//} else if (!temp.contains(";") && !temp.contains("；") && !temp.contains(",") && !temp.contains("，") && !temp.contains("IR_CONTENT:")){
			}
			//仅计算汉字
//			else if(!Const.EXSTR.contains(temp)){
//				// 其他字符长度为0.5
//				valueLength += 0.5;
//			}
		}
		//进位取整
		return (int)Math.ceil(valueLength);
	}
    /**
     * 判断字符串内 关键字字数，只统计中文字数（排除掉中文的分号（;|；）和英文 逗号（,））
     * @param keyWordArray
     * @return
     */
    public static int getChineseCountForSimple(String keyWordArray){
        if (StringUtil.isEmpty(keyWordArray)){
            return 0;
        }
        double valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
		JSONArray jsonArray = JSONArray.fromObject(keyWordArray);
		for (Object keyWord : jsonArray) {
			JSONObject parseObject = JSONObject.fromObject(String.valueOf(keyWord));
			String keyWords = parseObject.getString("keyWords");
			for (int i = 0; i < keyWords.length(); i++) {
				// 获取一个字符
				String temp = keyWords.substring(i, i + 1);
				// 判断是否为中文字符
				if (temp.matches(chinese)) {
					// 中文字符长度为1
					valueLength += 1;
					//} else if (!temp.contains(";|；|,")){
					//} else if (!temp.contains(";") && !temp.contains("；") && !temp.contains(",") && !temp.contains("，") && !temp.contains("IR_CONTENT:")){
				}
				//仅计算汉字
//			else if(!Const.EXSTR.contains(temp)){
//				// 其他字符长度为0.5
//				valueLength += 0.5;
//			}
			}
		}

        //进位取整
        return (int)Math.ceil(valueLength);
    }

	/**
	 * 判断字符串字节数,海贝查询大于16k就报错,保障16k以内,提前判断
	 * @param str
	 * @return
	 */
	public static boolean getStringKBIsTooLong(String str){
		if(isEmpty(str)){
			return false;
		}
		try {
			int strleng =  str.getBytes("utf-8").length;
			if(strleng>14000) return true;
			else return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return true;
		}
	}
	/**
	 * 判断字符串字节数,海贝查询大于16k就报错,保障16k以内,提前判断
	 * @param str
	 * @return
	 */
	public static boolean getStringKBIsTooLongLast(String str){
		if(isEmpty(str)){
			return false;
		}
		try {
			int strleng =  str.getBytes("utf-8").length;
			if(strleng>16000) return true;
			else return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return true;
		}
	}

	/**
	 * 过滤表情<br>
	 * @param source
	 * @return
	 */
	public static String filterEmoji(String source) {
		if(source != null)
		{
			source = source.replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "");
			Pattern emoji = Pattern.compile ("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]|[\ue000-\uf8ff]",Pattern.UNICODE_CASE | Pattern . CASE_INSENSITIVE ) ;
			Matcher emojiMatcher = emoji.matcher(source);
			if ( emojiMatcher.find())
			{
				source = emojiMatcher.replaceAll("");
				return source;
			}
			return source;
		}
		return source;
	}
}
