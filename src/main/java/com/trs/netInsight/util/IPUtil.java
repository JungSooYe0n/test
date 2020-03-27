package com.trs.netInsight.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class IPUtil {
	/**
	 * 根据IP查询所在城市
	 * @param ip String IP地址
	 * @return value String 所在城市
	 * */
	public static String getArea(String ip){
		String value = "未知";
		if (ip != null && !ip.equals("") && !ip.equals("0:0:0:0:0:0:0:1") &&
				!ip.contains("127.0.0.1") && !ip.contains("192.168.")) {
			try {
				URL uf1 = new URL("https://www.ip138.com/iplookup.asp?ip=117.100.206.174&action=2");
				HttpURLConnection cf = (HttpURLConnection) uf1.openConnection();
				cf.addRequestProperty("Host", "www.ip138.com");
				cf.addRequestProperty("Sec-Fetch-Dest", "document");
				cf.addRequestProperty("Sec-Fetch-Mode", "navigate");
				cf.addRequestProperty("Sec-Fetch-Site", "none");
				cf.addRequestProperty("Sec-Fetch-User", "1");
				cf.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
				cf.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
				cf.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
				cf.addRequestProperty("Connection", "keep-alive");
				cf.setConnectTimeout(1000*30);
				cf.setReadTimeout(1000*30);
				cf.connect();
				InputStream iff1 = cf.getInputStream();
				Scanner sf1 = new Scanner(iff1,"gb2312");
				StringBuffer sb = new StringBuffer();
				while(sf1.hasNext()){
					String scsc = sf1.nextLine();
					sb.append(scsc);
//					System.out.println("scsc--->"+scsc);
				}
				String htmls = sb.toString();
				String s1 = htmls.substring(htmls.indexOf("归属地：")+4);
				int i2 = s1.indexOf("  ");
				String s2 = s1.substring(0,i2);
				value = s2;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return value;
	}
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String ip = "117.100.206.174";
		String city = "117.100.206.174";
		city = getArea(ip);
		System.out.println(ip + "所在的城市: " + city);

	}
}
