package com.trs.netInsight.util;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trs.dev4.jdk16.utils.StringHelper;

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
	public static final String video1 = "(?=&lt;video&nbsp;src=).+?(?=&quot;&gt;)";
	public static final String video2 = "(?=&lt;AUDIO&nbsp;SRC=).+?(?=&quot;&gt;)";
	public static final String videoSuffix1 = "&lt;/video&gt;";
	public static final String videoSuffix2 = "</video>";
	public static final String gt = "&gt;";
	public static final String rn = "\r\n";
	public static final String annotation1 = "(?=&lt;!--).+?(?=--&gt;)";
	public static final String annotationSuffix1 = "--&gt;";
	public static final String annotation2 = "(?=<!--).+?(?=-->)";
	public static final String annotationSuffix2 = "-->";
	public static final String font1 = "<font color=red>";// ;<font
															// color=red>相思苦</font>
	public static final String font2 = "</font>";

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
		content = content.replaceAll(video1,"");
		content = content.replaceAll(video2,"");
		content = content.replaceAll(videoSuffix1,"");
		content = content.replaceAll(videoSuffix2,"");

		//去掉多余的注释标签
		content = content.replaceAll(annotation1,"");
		content = content.replaceAll(annotation2,"");
		content = content.replaceAll(annotationSuffix1,"");
		content = content.replaceAll(annotationSuffix2,"");

		pat = Pattern.compile(nbsp);
		mat = pat.matcher(content);
		content = mat.replaceAll("");

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
			return content.replaceAll("0000", "");
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
					return content + "...";
					// 关键词出现在中点之后，中点之后的长度又大于80的，取中点前后共160字符长度
				} else if (position > midpoint && remain > size / 2) {
					content = content.substring(position - size / 2, position + size / 2);
					return "..." + content + "...";
					// 关键词出现在中点之后，中点之后的长度又小于80的，取最后的160字符
				} else if (position > midpoint && remain < size / 2) {
					content = content.substring(content.length() - size / 2, content.length());
					return "..." + content;
				}
				// 有些标题无标红关键字，但是长度需要控制
			} else {
				content = content.substring(0, size);
				return content + "...";
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
		if(content.length()<=size){
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

			return content + "...";
		}else {
			content = content.substring(0, size);
			return content + "...";
		}
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
			Matcher m = Pattern.compile("(http:|https:)//[^\":<>]*\\.(jpg|bmp|gif|ico|pcx|jpeg|tif|png)").matcher(img);
			while (m.find()) {
				pics.add(m.group());
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
