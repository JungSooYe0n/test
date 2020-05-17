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
package com.trs.netInsight.support.ckm.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @Desc 聚类结果信息
 * @author yan.changjiang
 * @date 2018年3月6日 下午6:04:17
 * @version
 */
public class ClusterInfo {

	public ClusterInfo(int dataindex, double[] data) {
		currentMembership.add(dataindex);
		mean = data;
	}

	/**
	 * 该聚类的数据成员索引
	 */
	List<Integer> currentMembership = new ArrayList<Integer>();

	/**
	 * 该聚类的中心
	 */
	double[] mean;

	/**
	 * 该方法计算聚类对象的均值
	 * 
	 * @param coordinates
	 */
	public void updateMean(double[][] coordinates) {
		// 根据 mCurrentMembership 取得原始资料点对象 coord ，该对象是 coordinates 的一个子集；
		// 然后取出该子集的均值；取均值的算法很简单，可以把 coordinates 想象成一个 m*n 的距阵 ,
		// 每个均值就是每个纵向列的取和平均值 , //该值保存在 mCenter 中

		for (int i = 0; i < currentMembership.size(); i++) {
			double[] coord = coordinates[currentMembership.get(i)];
			for (int j = 0; j < coord.length; j++) {
				mean[j] += coord[j]; // 得到每个纵向列的和；
			}
			for (int k = 0; k < mean.length; k++) {
				mean[k] /= coord.length; // 对每个纵向列取平均值
			}
		}
	}

	/**
	 * @return the currentMembership
	 */
	public List<Integer> getCurrentMembership() {
		return currentMembership;
	}
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月6日 yan.changjiang create
 */