/*
 * Project: netInsight
 * 
 * File Created at 2017年12月7日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码类
 * 
 * @Type VerifyUtil.java
 * @author 谷泽昊
 * @date 2017年12月7日 下午4:16:47
 * @version
 */
public class VerifyUtil {
	// 图片的宽度。
	private int width = 160;
	// 图片的高度。
	private int height = 40;
	// 验证码字符个数
	private int codeCount = 5;
	// 验证码干扰线数
	private int lineCount = 150;
	// 验证码
	private static String code = null;
	// 验证码图片Buffer
	private BufferedImage buffImg = null;

	// 验证码字符来源
	private static final char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	/**
	 * 构造函数--按照初始化的配置
	 */
	public VerifyUtil() {
		this.createCode();
	}

	/**
	 * 构造函数--指定宽、高
	 * 
	 * @param width
	 *            图片宽
	 * @param height
	 *            图片高
	 */
	public VerifyUtil(int width, int height) {
		this.width = width;
		this.height = height;
		this.createCode();
	}

	/**
	 * 构造函数--指定宽、高、字符数、干扰线条数
	 * 
	 * @param width
	 *            图片宽
	 * @param height
	 *            图片高
	 * @param codeCount
	 *            字符个数
	 * @param lineCount
	 *            干扰线条数
	 */
	public VerifyUtil(int width, int height, int codeCount, int lineCount) {
		this.width = width;
		this.height = height;
		this.codeCount = codeCount;
		this.lineCount = lineCount;
		this.createCode();
	}

	/**
	 * 设置图片属性
	 */
	public void createCode() {
		int x = 0;
		int fontHeight = 0;
		int codeY = 0;
		int red = 0;
		int green = 0;
		int blue = 0;

		x = width / (codeCount + 2);// 每个字符的宽度
		fontHeight = height - 2;// 字体的高度
		codeY = height - 4;

		// 图像buffer
		buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = buffImg.createGraphics();
		// 生成随机数
		Random random = new Random();
		// 将图像填充为白色
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		// 创建字体
		Font font = new Font("Fixedsys", Font.PLAIN, fontHeight);
		g.setFont(font);

		for (int i = 0; i < lineCount; i++) {
			int xs = random.nextInt(width);
			int ys = random.nextInt(height);
			int xe = xs + random.nextInt(width / 8);
			int ye = ys + random.nextInt(height / 8);
			red = random.nextInt(255);
			green = random.nextInt(255);
			blue = random.nextInt(255);
			g.setColor(new Color(red, green, blue));
			g.drawLine(xs, ys, xe, ye);
		}

		// randomCode记录随机产生的验证码
		StringBuffer randomCode = new StringBuffer();
		// 随机产生codeCount个字符的验证码。
		try {
			for (int i = 0; i < codeCount; i++) {
				String strRand = String.valueOf(codeSequence[random.nextInt(codeSequence.length)]);
				// 产生随机的颜色值，让输出的每个字符的颜色值都将不同。
				red = random.nextInt(255);
				green = random.nextInt(255);
				blue = random.nextInt(255);
				g.setFont(new Font("STIX", Font.PLAIN, 32));
				g.setColor(new Color(red, green, blue));
				g.drawString(strRand, (i + 1) * x, codeY);
				// 将产生的四个随机数组合在一起。
				randomCode.append(strRand);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		code = randomCode.toString();
	}

	public BufferedImage getBuffImg() {
		return buffImg;
	}

	public String getCode() {
		return code;
	}

	
	/**
	 * 只生成验证码
	 * @date Created at 2018年9月29日  下午2:28:00
	 * @Author 谷泽昊
	 * @param codeCount 验证码个数
	 * @return
	 */
	public static String getCode(int codeCount) {
		Random random = new Random();
		// randomCode记录随机产生的验证码
		StringBuffer randomCode = new StringBuffer();
		// 随机产生codeCount个字符的验证码。
		try {
			for (int i = 0; i < codeCount; i++) {
				String strRand = String.valueOf(codeSequence[random.nextInt(codeSequence.length)]);
				// 将产生的四个随机数组合在一起。
				randomCode.append(strRand);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return randomCode.toString();
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年12月7日 谷泽昊 creat
 */