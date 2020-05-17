package com.trs.netInsight.util;

/**
 * 字符串编码工具类
 *
 * Create by yan.changjiang on 2017年11月21日
 */
public class EncodeString {

	public static void main(String args[]){
		String dd = encodeString("新冠状病毒疫情报告");
		System.out.println(dd);
		System.out.println(decodeString(dd));
	}

	/**
	 * 解码 字符串
	 * 
	 * @param coding
	 *            码
	 * @return String原文字
	 */
	public static String decodeString(String coding) {
		String enUnicode = null;
		String deUnicode = null;
		for (int i = 0; i < coding.length(); i++) {
			if (enUnicode == null) {
				enUnicode = String.valueOf(coding.charAt(i));
			} else {
				enUnicode = enUnicode + coding.charAt(i);
			}
			if (i % 3 == 2) {
				if (deUnicode == null) {
					deUnicode = String.valueOf((char) Integer.valueOf(enUnicode, 35).intValue());
				} else {
					deUnicode = deUnicode + String.valueOf((char) Integer.valueOf(enUnicode, 35).intValue());
				}
				enUnicode = null;
			}

		}
		return deUnicode;
	}

	/**
	 * 编码 字符串
	 * 
	 * @param resource
	 *            原文字
	 * @return 编码后的String
	 */
	public static String encodeString(String resource) {
		String enUnicode = null;
		for (int i = 0; i < resource.length(); i++) {
			if (i == 0) {
				enUnicode = getHexString(baseString(resource.charAt(i), 35));
			} else {
				enUnicode = enUnicode + getHexString(baseString(resource.charAt(i), 35));
			}
		}
		return enUnicode;
	}

	/**
	 * 补足位数
	 * 
	 * @param hexString
	 *            原字符
	 * @return 3位String
	 */
	private static String getHexString(String hexString) {
		String hexStr = "";
		for (int i = hexString.length(); i < 3; i++) {
			if (i == hexString.length()) {
				hexStr = "0";
			} else {
				hexStr = hexStr + "0";
			}
		}
		return hexStr + hexString;
	}

	/**
	 * 转换进制
	 * 
	 * @param num
	 *            原数
	 * @param base
	 *            进制
	 * @return String
	 */
	private static String baseString(int num, int base) {
		String str = "0123456789abcdefghijklmnopqrstuvwxyz";
		String digit = "0123456789abcdefghijklmnopqrstuvwxyz";
		if (num == 0) {
			return "";
		} else {
			str = baseString(num / base, base);
			return str + digit.charAt(num % base);
		}
	}
}
