/*
 * Project: netInsight
 * 
 * File Created at 2018年3月6日
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

/**
 * @Desc 数学计算工具类
 * @author yan.changjiang
 * @date 2018年3月6日 下午5:52:36
 * @version
 */
public class MathUtil {

	/**
	 * 格式化double数组
	 * 
	 * @param vector
	 * @return
	 */
	public static double[] normalize(double[] vector) {
		double magnitude = magnitude(vector);
		return magnitude != 0 ? mult(vector, 1 / magnitude) : vector;
	}

	public static double magnitude(double[] vector) {
		double magnitude = 0.0;
		for (int i = 0; i < vector.length; i++) {
			magnitude += Math.pow(vector[i], 2);
		}

		return Math.sqrt(magnitude);
	}

	public static double[] mult(double[] vector, double scalar) {
		int length = vector.length;
		double[] result = new double[length];
		for (int i = 0; i < length; i++) {
			result[i] = vector[i] * scalar;
		}
		return result;
	}
	
	/**
	 * 
	 * 计算两个点的欧氏距离
	 * 
	 * @param a
	 * @param b
	 * @return
	 * @since yan.changjiang @ 2018年3月6日
	 */
	public static double euclideanDistance(double[] a, double[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("The dimensions have to be equal!");
		}

		double sum = 0.0;
		for (int i = 0; i < a.length; i++) {
			sum += Math.pow(a[i] - b[i], 2);
		}

		return Math.sqrt(sum);
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月6日 yan.changjiang creat
 */