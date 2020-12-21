/*
 * Project: netInsight
 *
 * File Created at 2017年11月21日
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.analysis.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trs.netInsight.config.constant.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Criterion.MatchMode;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.widget.analysis.entity.DistrictInfo;
import com.trs.netInsight.widget.analysis.enums.DistrictType;
import com.trs.netInsight.widget.analysis.repository.DistrictInfoRepository;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;

/**
 * 地域检索服务接口实现
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Service("districtInfoServiceImpl")
public class DistrictInfoServiceImpl implements IDistrictInfoService {

	@Autowired
	private DistrictInfoRepository districtInfoRepository;

	@Override
	public Map<String, List<String>> allAreas() {
		Criteria<DistrictInfo> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("areaType", DistrictType.PRIVIENCE.getCode()));

		List<DistrictInfo> provienceInfos = districtInfoRepository.findAll(criteria);
		Map<String, List<String>> map = new HashMap<>();
		for (DistrictInfo provienceInfo : provienceInfos) {
			Criteria<DistrictInfo> cityCriteria = new Criteria<>();
			cityCriteria.add(Restrictions.like("id", provienceInfo.getId(), MatchMode.ANYWHERE));
			cityCriteria.add(Restrictions.eq("areaType", DistrictType.CITY.getCode()));
			List<DistrictInfo> cityInfos = districtInfoRepository.findAll(cityCriteria);
			List<String> cityNames = new ArrayList<>();
			for (DistrictInfo city : cityInfos) {
				cityNames.add(city.getAreaName());
			}
			map.put(provienceInfo.getAreaName(), cityNames);
		}
		return map;
	}

	@Override
	public String province(String area) {
		// 先确定是省还是市
		Criteria<DistrictInfo> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("areaName", area));
		List<DistrictInfo> provienceInfos = districtInfoRepository.findAll(criteria);
		DistrictInfo districtInfo = provienceInfos.get(0);
		String areaType = districtInfo.getAreaType();
		// type是1 就是省 直接返回
		if ("1".equals(areaType)) {
			return area;
		} else if ("2".equals(areaType)) {
			// 如果是2 就是市 返回省+市
			// 去mysql里查找对应的省
			String areaId = districtInfo.getId().substring(0, 5);
			DistrictInfo province = districtInfoRepository.findOne(areaId);
			String areaName = province.getAreaName();
			// 判断这个省是不是自治区
			if (!"内蒙古".equals(areaName) && !"广西".equals(areaName) && !"西藏".equals(areaName) && !"宁夏".equals(areaName) && !"新疆".equals(areaName)) {
				if ("北京".equals(areaName) || "天津".equals(areaName) || "上海".equals(areaName) || "重庆".equals(areaName)) {
					// 直辖市
					area = areaName + "市\\\\" + area;
				} else if (area.contains("地区") || area.contains("自治州")) {
					area = areaName + "省\\\\" + area;
				} else {
					area = areaName + "省\\\\" + area + "市";
				}

			} else if ("内蒙古".equals(areaName) || "西藏".equals(areaName) || "新疆".equals(areaName)) {
				// if( area.equals("锡林郭勒盟") || area.equals("兴安盟") ||
				// area.equals("阿拉善盟")){
				if ("阿勒泰地区".equals(area)) {
					area = "新疆自治区\\\\伊犁哈萨克自治州\\\\阿勒泰地区";
				} else if (area.contains("盟") || area.contains("地区") || area.contains("自治州") || area.contains("市")) {
					area = areaName + "自治区\\\\" + area;
				} else {
					area = areaName + "自治区\\\\" + area + "市";
				}

			} else if ("广西".equals(areaName)) {
				area = areaName + "壮族自治区\\\\" + area + "市";
			} else if ("宁夏".equals(areaName)) {
				area = areaName + "回族自治区\\\\" + area + "市";
			}
		}
		return area;
	}

	@Override
	public DistrictInfo getCodeBy(String city) {
		// 先确定是省还是市
		Criteria<DistrictInfo> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("areaName", city));
		criteria.add(Restrictions.eq("areaType", "1"));
		List<DistrictInfo> provienceInfos = districtInfoRepository.findAll(criteria);
		DistrictInfo districtInfo = provienceInfos.get(0);
		return districtInfo;
	}

	@Override
	public List<DistrictInfo> getAreasByCode(String city) {

		DistrictInfo districtInfo = getCodeBy(city);

		List<DistrictInfo> provienceInfos = districtInfoRepository.findByIdLike(districtInfo.getId()+Const.maptopri+"%");
		return provienceInfos;
	}

	@Override
	public DistrictInfo getCityByCode(String city) {
		// 先确定是省还是市
		Criteria<DistrictInfo> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("areaName", city));
		criteria.add(Restrictions.eq("areaType", "2"));
		List<DistrictInfo> provienceInfos = districtInfoRepository.findAll(criteria);
		DistrictInfo districtInfo = provienceInfos.get(0);
		return districtInfo;
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 *
 * When I wrote this, only God and I understood what I was doing But now, God
 * only knows!
 * -------------------------------------------------------------------------
 * 2017年11月21日 Administrator creat
 */
