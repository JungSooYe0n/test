/*
 * Project: netInsight
 * 
 * File Created at 2018年9月18日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.config.service.impl;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.config.entity.HybaseDatabaseConfig;
import com.trs.netInsight.widget.config.entity.SystemConfig;
import com.trs.netInsight.widget.config.repository.HybaseDatabaseConfigRepository;
import com.trs.netInsight.widget.config.repository.SystemConfigRepository;
import com.trs.netInsight.widget.config.service.ISystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配置类 service
 * 
 * @Type SystemConfigServiceImpl.java
 * @author 张娅
 * @date 2020年1月6日
 * @version
 */
@Service
@Slf4j
public class SystemConfigServiceImpl implements ISystemConfigService {

	@Autowired
	private HybaseDatabaseConfigRepository hybaseDatabaseConfigRepository;

	@Autowired
	private SystemConfigRepository systemConfigRepository;

	/*@Override
	public String queryOneHybaseDatabase(String type) {
		HybaseDatabaseConfig hybaseDatabaseConfig = new HybaseDatabaseConfig();
		List<HybaseDatabaseConfig> hybaseDatabase = hybaseDatabaseConfigRepository.findAll();
		if (hybaseDatabase == null || hybaseDatabase.size() == 0) {
			hybaseDatabaseConfig.setWeibo(Const.DEFAULT_WEIBO);
			hybaseDatabaseConfig.setSinaweiboUsers(Const.DEFAULT_SINAUSERS);
			hybaseDatabaseConfig.setTraditional(Const.DEFAULT_HYBASE_NI_INDEX);
			hybaseDatabaseConfig.setWeixin(Const.DEFAULT_WECHAT);
			hybaseDatabaseConfig.setOverseas(Const.DEFAULT_HYBASE_OVERSEAS);
			hybaseDatabaseConfig.setVideo(Const.DEFAULT_HYBASE_VIDEO);
			hybaseDatabaseConfig.setInsert(Const.DEFAULT_INSERT);
			hybaseDatabaseConfigRepository.saveAndFlush(hybaseDatabaseConfig);
			Const.HYBASE_NI_INDEX = hybaseDatabaseConfig.getTraditional();
			Const.WEIBO = hybaseDatabaseConfig.getWeibo();
			Const.WECHAT_COMMON = hybaseDatabaseConfig.getWeixin();
			Const.WECHAT = hybaseDatabaseConfig.getWeixin();
			Const.SINAUSERS = hybaseDatabaseConfig.getSinaweiboUsers();
			Const.INSERT = hybaseDatabaseConfig.getInsert();
			Const.HYBASE_OVERSEAS = hybaseDatabaseConfig.getOverseas();
			Const.HYBASE_VIDEO = hybaseDatabaseConfig.getVideo();
		}else{
			hybaseDatabaseConfig = hybaseDatabase.get(0);
		}
		String indices = "";
		if(FtsHybaseType.valueOf(type).equals(FtsHybaseType.TRADITIONAL)){
			indices = Const.HYBASE_NI_INDEX;
		}else if(FtsHybaseType.valueOf(type).equals(FtsHybaseType.WEIBO)){
			indices = Const.WEIBO;
		}else if(FtsHybaseType.valueOf(type).equals(FtsHybaseType.WEIXIN)){
			indices = Const.WECHAT_COMMON;
		}else if(FtsHybaseType.valueOf(type).equals(FtsHybaseType.OVERSEAS)){
			indices = Const.HYBASE_OVERSEAS;
		}else if(FtsHybaseType.valueOf(type).equals(FtsHybaseType.VIDEO)){
			indices = Const.HYBASE_VIDEO;
		}else if(FtsHybaseType.valueOf(type).equals(FtsHybaseType.INSERT)){
			indices = Const.INSERT;
		}else if(FtsHybaseType.valueOf(type).equals(FtsHybaseType.SINAUSER)){
			indices = Const.SINAUSERS;
		} else if(FtsHybaseType.valueOf(type).equals(FtsHybaseType.MIX)){
			indices = Const.HYBASE_NI_INDEX+";"+ Const.WECHAT_COMMON+";"+ Const.WEIBO+";"+ Const.HYBASE_OVERSEAS+";"+Const.HYBASE_VIDEO;
		}
		return indices;
	}*/

	@Override
	public HybaseDatabaseConfig queryHybaseDatabases() {
		List<HybaseDatabaseConfig> hybaseDatabase = hybaseDatabaseConfigRepository.findAll();
		if (hybaseDatabase == null || hybaseDatabase.size() == 0) {
			HybaseDatabaseConfig hybaseDatabaseConfig = new HybaseDatabaseConfig();
			hybaseDatabaseConfig.setWeibo(Const.DEFAULT_WEIBO);
			hybaseDatabaseConfig.setSinaweiboUsers(Const.DEFAULT_SINAUSERS);
			hybaseDatabaseConfig.setTraditional(Const.DEFAULT_HYBASE_NI_INDEX);
			hybaseDatabaseConfig.setWeixin(Const.DEFAULT_WECHAT);
			hybaseDatabaseConfig.setOverseas(Const.DEFAULT_HYBASE_OVERSEAS);
			hybaseDatabaseConfig.setVideo(Const.DEFAULT_HYBASE_VIDEO);
			hybaseDatabaseConfig.setInsert(Const.DEFAULT_INSERT);
			hybaseDatabaseConfigRepository.saveAndFlush(hybaseDatabaseConfig);
			Const.HYBASE_NI_INDEX = hybaseDatabaseConfig.getTraditional();
			Const.WEIBO = hybaseDatabaseConfig.getWeibo();
			Const.WECHAT_COMMON = hybaseDatabaseConfig.getWeixin();
			Const.WECHAT = hybaseDatabaseConfig.getWeixin();
			Const.SINAUSERS = hybaseDatabaseConfig.getSinaweiboUsers();
			Const.INSERT = hybaseDatabaseConfig.getInsert();
			Const.HYBASE_OVERSEAS = hybaseDatabaseConfig.getOverseas();
			Const.HYBASE_VIDEO = hybaseDatabaseConfig.getVideo();
			return hybaseDatabaseConfig;
		}else{
			Boolean update = false;
			HybaseDatabaseConfig hybaseDatabaseConfig = hybaseDatabase.get(0);
			if (StringUtil.isEmpty(hybaseDatabaseConfig.getTraditional())) {
				update = true;
				Const.HYBASE_NI_INDEX = Const.DEFAULT_HYBASE_NI_INDEX;
				hybaseDatabaseConfig.setTraditional(Const.DEFAULT_HYBASE_NI_INDEX);

			}
			if (StringUtil.isEmpty(hybaseDatabaseConfig.getWeibo())) {
				update = true;
				Const.WEIBO = Const.DEFAULT_WEIBO;
				hybaseDatabaseConfig.setWeibo(Const.DEFAULT_WEIBO);

			}
			if (StringUtil.isEmpty(hybaseDatabaseConfig.getWeixin())) {
				update = true;
				Const.WECHAT_COMMON = Const.DEFAULT_WECHAT;
				Const.WECHAT = Const.DEFAULT_WECHAT;
				hybaseDatabaseConfig.setWeixin(Const.DEFAULT_WECHAT);

			}
			if (StringUtil.isEmpty(hybaseDatabaseConfig.getOverseas())) {
				update = true;
				Const.HYBASE_OVERSEAS = Const.DEFAULT_HYBASE_OVERSEAS;
				hybaseDatabaseConfig.setOverseas(Const.DEFAULT_HYBASE_OVERSEAS);

			}
			if (StringUtil.isEmpty(hybaseDatabaseConfig.getInsert())) {
				update = true;
				Const.INSERT = Const.DEFAULT_INSERT;
				hybaseDatabaseConfig.setInsert(Const.DEFAULT_INSERT);
			}
			if (StringUtil.isEmpty(hybaseDatabaseConfig.getSinaweiboUsers())) {
				update = true;
				Const.SINAUSERS  = Const.DEFAULT_SINAUSERS;
				hybaseDatabaseConfig.setSinaweiboUsers(Const.DEFAULT_SINAUSERS);

			}
			if (StringUtil.isEmpty(hybaseDatabaseConfig.getVideo())) {
				update = true;
				Const.HYBASE_VIDEO = Const.DEFAULT_HYBASE_VIDEO;
				hybaseDatabaseConfig.setVideo(Const.DEFAULT_HYBASE_VIDEO);

			}
			if(update){
				hybaseDatabaseConfigRepository.saveAndFlush(hybaseDatabaseConfig);
			}
			return hybaseDatabaseConfig;
		}
	}

	@Override
	public SystemConfig findSystemConfig() {
		List<SystemConfig> systemConfig = systemConfigRepository.findAll();
		if(systemConfig!= null && systemConfig.size() >0){
			return systemConfig.get(0);
		}
		return new SystemConfig();
	}

	@Override
	public void updateHybaseDatabaseConfig(String traditional,String weibo,String weixin,String overseas,String video,String insert,String sinaweiboUsers){
		if (StringUtil.isEmpty(traditional) || "".equals(traditional.trim())) {
			traditional = Const.DEFAULT_HYBASE_NI_INDEX;
		}
		if (StringUtil.isEmpty(weibo) || "".equals(weibo.trim())) {
			weibo = Const.DEFAULT_WEIBO;
		}
		if (StringUtil.isEmpty(weixin) || "".equals(weixin.trim())) {
			weixin = Const.DEFAULT_WECHAT;
		}
		if (StringUtil.isEmpty(overseas) || "".equals(overseas.trim())) {
			overseas = Const.DEFAULT_HYBASE_OVERSEAS;
		}
		if (StringUtil.isEmpty(insert) || "".equals(insert.trim())) {
			insert = Const.DEFAULT_INSERT;
		}
		if (StringUtil.isEmpty(sinaweiboUsers) || "".equals(sinaweiboUsers.trim())) {
			sinaweiboUsers = Const.DEFAULT_SINAUSERS;
		}
		if (StringUtil.isEmpty(video) || "".equals(video.trim())) {
			video = Const.DEFAULT_HYBASE_VIDEO;
		}
		HybaseDatabaseConfig hybaseDatabaseConfig = new HybaseDatabaseConfig();
		List<HybaseDatabaseConfig> list = hybaseDatabaseConfigRepository.findAll();
		if(list != null && list.size() >0){
			hybaseDatabaseConfig = list .get(0);
		}
		hybaseDatabaseConfig.setTraditional(traditional);
		hybaseDatabaseConfig.setWeibo(weibo);
		hybaseDatabaseConfig.setWeixin(weixin);
		hybaseDatabaseConfig.setOverseas(overseas);
		hybaseDatabaseConfig.setVideo(video);
		hybaseDatabaseConfig.setInsert(insert);
		hybaseDatabaseConfig.setSinaweiboUsers(sinaweiboUsers);
		hybaseDatabaseConfigRepository.saveAndFlush(hybaseDatabaseConfig);
		Const.HYBASE_NI_INDEX = hybaseDatabaseConfig.getTraditional();
		Const.WEIBO = hybaseDatabaseConfig.getWeibo();
		Const.WECHAT_COMMON = hybaseDatabaseConfig.getWeixin();
		Const.WECHAT = hybaseDatabaseConfig.getWeixin();
		Const.SINAUSERS = hybaseDatabaseConfig.getSinaweiboUsers();
		Const.INSERT = hybaseDatabaseConfig.getInsert();
		Const.HYBASE_OVERSEAS = hybaseDatabaseConfig.getOverseas();
		Const.HYBASE_VIDEO = hybaseDatabaseConfig.getVideo();
	}

	@Override
	public void updateSystemConfig(String orgName,String logoName,Boolean deleteOrg,Boolean needOperation){
		List<SystemConfig> list = systemConfigRepository.findAll();
		SystemConfig systemConfig = new SystemConfig();
		if(list!= null && list.size() >0){
			systemConfig = list.get(0);
		}
		systemConfig.setOrganizationName(orgName);
		systemConfig.setLogoPicName(logoName);
		systemConfig.setDeleteOrg(deleteOrg);
		systemConfig.setNeedOperation(needOperation);
		systemConfigRepository.saveAndFlush(systemConfig);

	}




}