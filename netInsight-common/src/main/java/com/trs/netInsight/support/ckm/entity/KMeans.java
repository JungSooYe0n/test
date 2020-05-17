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

import java.util.Random;

import com.trs.netInsight.support.ckm.ICluster;
import com.trs.netInsight.util.MathUtil;

import lombok.Getter;

/**
 * @Desc k-means 聚类实体
 * @author yan.changjiang
 * @date 2018年3月6日 下午5:57:11
 * @version 
 */
@Getter
public class KMeans implements ICluster{
	
	/**
	 * 聚类数量
	 */
	private final int _k;

	/**
	 * 原始数据数量
	 */
	private final int _count;

	/**
	 * 原始数据
	 */
	private final double[][] _data;

	/**
	 * 用于记录和跟踪每个点属于哪个聚类簇 _clusterAssignments[j] = i; 表示第 j 个资料点对象属于第 i 个群聚类
	 */
	private final int[] _clusterAssignments;

	/**
	 * 定义一个变量用于记录和跟踪每个点离聚类最近
	 */
	private final int[] _nearestCluster;

	/**
	 * 定义一个变量，来表示资料点到中心点的距离, 其中—_distanceCache[i][j] 表示第i个点到第j个群聚对象中心点的距离；
	 */
	private final double[][] _distanceCache;

	/**
	 * 聚类结果
	 */
	private final ClusterInfo[] _clusters;

	public KMeans(double[][] data, int k) {
		this._k = k;
		this._count = data.length;
		this._data = data;
		
        _clusters = new ClusterInfo[k];
        _clusterAssignments = new int[data.length];
        _nearestCluster = new int[data.length];
        _distanceCache = new double[data.length][data.length];
	}

	@Override
	public ClusterInfo[] cluster() {
		this.initRandomK();
		while (true) {
			// 1、重新计算每个聚类的均值
			for (int i = 0; i < _k; i++) {
				_clusters[i].updateMean(_data);
			}
			// 2、计算每个数据和每个聚类中心的距离
			for (int i = 0; i < _count; i++) {
				for (int j = 0; j < _k; j++) {
					double dist = getDistance(_data[i], _clusters[j].mean);
					_distanceCache[i][j] = dist;
				}
			}
			// 3、计算每个数据离哪个聚类最近
			for (int i = 0; i < _count; i++) {
				_nearestCluster[i] = nearestCluster(i);
			}
			// 4、比较每个数据最近的聚类是否就是它所属的聚类
			// 如果全相等表示所有的点已经是最佳距离了，直接返回；
			int k = 0;
			for (int i = 0; i < _count; i++) {
				if (_nearestCluster[i] == _clusterAssignments[i]) {
					k++;
				}
			}
			if (k == _count) {
				break;
			}
			// 5、否则需要重新调整资料点和群聚类的关系，调整完毕后再重新开始循环；
			// 需要修改每个聚类的成员和表示某个数据属于哪个聚类的变量
			for (int j = 0; j < _k; j++) {
				_clusters[j].currentMembership.clear();
			}
			for (int i = 0; i < _count; i++) {
				_clusters[_nearestCluster[i]].currentMembership.add(i);
				_clusterAssignments[i] = _nearestCluster[i];
			}
		}
		return _clusters;
	}
	
	/**
	 * 
	 * 随机初始化k个聚类
	 * 
	 * @since yan.changjiang @ 2018年3月6日
	 */
	private void initRandomK() {
		Random _rnd = new Random(1);
		for (int i = 0; i < _k; i++) {
			int temp = _rnd.nextInt(_count);
			_clusterAssignments[temp] = i; // 记录第TEMP个点属于第i个聚类
			_clusters[i] = new ClusterInfo(temp, _data[temp]);
		}
	}

	/**
	 * 
	 * 计算某数据离某聚类中心的距离
	 * 
	 * @param coord
	 * @param center
	 * @return
	 * @since yan.changjiang @ 2018年3月6日
	 */
	private double getDistance(double[] coord, double[] center) {
		return MathUtil.euclideanDistance(coord, center);
	}

	/**
	 * 
	 * 计算某个数据离哪个聚类最近
	 * @param ndx
	 * @return
	 * @since yan.changjiang @ 2018年3月6日
	 */
	private int nearestCluster(int ndx) {
		int nearest = -1;
		double min = Double.MAX_VALUE;
		for (int c = 0; c < _k; c++) {
			double d = _distanceCache[ndx][c];
			if (d < min) {
				min = d;
				nearest = c;
			}
		}
		if (nearest == -1) {
			;
		}
		return nearest;
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