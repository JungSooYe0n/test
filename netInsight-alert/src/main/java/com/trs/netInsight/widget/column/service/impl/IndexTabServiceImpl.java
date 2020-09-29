/*
 * Project: netInsight
 *
 * File Created at 2017年12月4日
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.column.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.entity.IndexSequence;
import com.trs.netInsight.widget.column.entity.emuns.IndexFlag;
import com.trs.netInsight.widget.column.repository.IndexSequenceRepository;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.trs.jpa.utils.Criteria;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.service.IIndexTabService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Type IndexTabServiceImpl.java
 * @Desc 三级栏目检索服务实现
 * @author yan.changjiang
 * @date 2017年12月4日 下午6:36:41
 * @version
 */
@Service
public class IndexTabServiceImpl implements IIndexTabService {

	@Autowired
	private IndexTabRepository indexTabRepository;

	@Autowired
	private IndexPageRepository indexPageRepository;

	@Autowired
	private IndexTabMapperRepository indexTabMapperRepository;
	@Autowired
	private IndexSequenceRepository indexSequenceRepository;
	@Override
	public List<IndexTab> findByParentId(String parentId) {
		return indexTabRepository.findByParentId(parentId);
	}

	@Override
	public List<IndexTab> findByUserId(String userId) {
		return indexTabRepository.findByUserIdAndSubGroupIdIsNull(userId);
	}

	@Override
	public List<IndexTab> findSimple(String keyWord) {
		List<IndexTab> byKeyWordNotLikeAndKeyWordIsNotNull = indexTabRepository.findByKeyWordIsNotNullAndKeyWordIndexIsNotNullAndTrslIsNull();
		List<IndexTab> indexTabs = new ArrayList<>();
		for (IndexTab indexTab : byKeyWordNotLikeAndKeyWordIsNotNull) {
			if (!indexTab.getKeyWord().contains(keyWord)){
				indexTabs.add(indexTab);
			}
		}
		return indexTabs;
	}

	@Override
	public Object save(IndexTab indexTab) {
		IndexTab save = indexTabRepository.save(indexTab);
		// 保存映射表
		IndexTabMapper mapper = save.mapper();
		IndexPage indexPage = indexPageRepository.findOne(save.getParentId());
		mapper.setIndexPage(indexPage);
		mapper.setTypeId(indexTab.getTypeId());
		mapper = indexTabMapperRepository.save(mapper);
		return mapper;
	}
	@Override
	public Object save(IndexTab indexTab,Boolean changeMapper) {
		IndexTab save = indexTabRepository.save(indexTab);
		if(changeMapper){
			// 保存映射表
			IndexTabMapper mapper = save.mapper();
			IndexPage indexPage = indexPageRepository.findOne(save.getParentId());
			mapper.setIndexPage(indexPage);
			mapper.setTypeId(indexTab.getTypeId());
			mapper = indexTabMapperRepository.save(mapper);
		}

		return save;
	}

	@Override
	@Transactional
	public Object save(IndexTab indexTab, boolean share) {
		IndexTab save = indexTabRepository.save(indexTab);
		// 保存映射表
		IndexTabMapper mapper = save.mapper(share);
		if(StringUtil.isNotEmpty(save.getParentId())){
			IndexPage indexPage = indexPageRepository.findOne(save.getParentId());
			mapper.setIndexPage(indexPage);
		}
		mapper.setTypeId(indexTab.getTypeId());
		mapper = indexTabMapperRepository.save(mapper);
		IndexSequence indexSequence = new IndexSequence();
		indexSequence.setIndexId(mapper.getId());
		if (ObjectUtil.isEmpty(mapper.getIndexPage())){
			indexSequence.setParentId("");
		}else {
			indexSequence.setParentId(mapper.getIndexPage().getId());
		}

		indexSequence.setSequence(save.getSequence());
		indexSequence.setIndexFlag(IndexFlag.IndexTabFlag);
		indexSequence.setIndexTabId(save.getId());
		indexSequenceRepository.save(indexSequence);
		return mapper;
	}

	@Override
	public IndexTab findOne(String indexId) {
		return indexTabRepository.findOne(indexId);
	}

	@Override
	public void delete(String indexId) {
		indexTabRepository.delete(indexId);
	}

	@Override
	public void deleteByUserId(String userId) {
		List<IndexTab> indexTabs = indexTabRepository.findByUserId(userId);
		if (ObjectUtil.isNotEmpty(indexTabs)){
//			indexTabRepository.delete(indexTabs);
//			indexTabRepository.flush();
		}
	}

	@Override
	public int getSubGroupColumnCount(User user) {
		if (UserUtils.isRoleAdmin()){
			List<IndexTab> indexTabs = indexTabRepository.findByUserId(user.getId());
			if (ObjectUtil.isNotEmpty(indexTabs)){
				//机构管理员
				return indexTabs.size();
			}
		}
		if (UserUtils.isRoleOrdinary(user)){
			//如果是普通用户 受用户分组 可创建资源的限制
			//查询该用户所在的用户分组下 是否有可创建资源
			List<IndexTab> bySubGroupId = indexTabRepository.findBySubGroupId(user.getSubGroupId());
			if (ObjectUtil.isNotEmpty(bySubGroupId)){
				return bySubGroupId.size();
			}

		}
		return 0;
	}

	@Override
	public int getSubGroupColumnCountForSubGroup(String subGroupId) {
		List<IndexTab> bySubGroupId = indexTabRepository.findBySubGroupId(subGroupId);
		if (ObjectUtil.isNotEmpty(bySubGroupId)){
			return bySubGroupId.size();
		}
		return 0;
	}

	@Override
	public List<IndexTab> findWordCloudIndexTab(String groupName) {
		return indexTabRepository.findByTypeAndGroupName(ColumnConst.CHART_WORD_CLOUD,groupName);
	}

	@Override
	public List<IndexTab> findMapIndexTab(String groupName) {
		return indexTabRepository.findByTypeAndGroupName(ColumnConst.CHART_MAP,groupName);
	}


	@Override
	public List<IndexTab> findByOrganizationId(String organizationId) {
		return indexTabRepository.findByOrganizationId(organizationId);
	}

	@Override
	public Object update(IndexTab threeEntity) {
		return indexTabRepository.saveAndFlush(threeEntity);
	}
	@
			Override
	public Object update(IndexTab threeEntity,String mapperId, boolean share) {
		IndexTab indexTab = indexTabRepository.saveAndFlush(threeEntity);
		IndexTabMapper mapper = indexTabMapperRepository.findOne(mapperId);
		mapper.setTabWidth(Integer.valueOf(indexTab.getTabWidth()));
		mapper.setShare(share);
		mapper = indexTabMapperRepository.saveAndFlush(mapper);
		return mapper;
	}

	@Override
	public List<IndexTab> findByParentIdAndUser(String parentId, User user, Sort sort) {
		List<IndexTab> indexTabs = null;
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			indexTabs = indexTabRepository.findByParentIdAndUserId(parentId, user.getId(), sort);
		}else {
			indexTabs = indexTabRepository.findByParentIdAndSubGroupId(parentId, user.getSubGroupId(), sort);
		}
		return indexTabs;
	}

	@Override
	public IndexTab findByIdAndUser(String id, User user) {
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			return indexTabRepository.findByIdAndUserId(id, user.getId());
		}else {
			return indexTabRepository.findByIdAndSubGroupId(id, user.getSubGroupId());
		}

	}
	public Object updateHistortColumnField(String orgId) {
		try {
			/*
			需要去掉多余的字段，主要是需要合并
			1、修改列表类型，需要修改很多
			 豆腐块类型为  pieChart brokenLineChart barGraphChart wordCloudChart mapChart timeListInfo md5ListInfo

			barGraphChartCommon、barGraphChartMeta  barGraphChart
			brokenLineChart ： 折线图
			mapChart：地图
			md5ListInfo：热点

			pieGraphChartCommon：饼图  pieGraphChartMeta  pieGraphChart
			wordCloudChart：词云

			 2、groupName 需要跟tradition 合并

			 3、将trsl 和其他的表达式合并，保留一份
			 */
			List<IndexTab> list = null;
			if(StringUtil.isEmpty(orgId)){
				list = indexTabRepository.findAll();
			}else{
				list = indexTabRepository.findByOrganizationId(orgId);
			}


			if (list != null && list.size() > 0) {
				int n = 0;
				System.out.println("根据机构信息找到分组数"+list.size());
				for (IndexTab indexTab : list) {
					try {
						String source = indexTab.getGroupName();
						String[] columnType = indexTab.getType(true);
						String typeCode = "";
						if (columnType.length > 1 || columnType[0].equals(ColumnConst.LIST_STATUS_COMMON) || columnType[0].equals(ColumnConst.LIST_WECHAT_COMMON)
								|| columnType[0].equals(ColumnConst.LIST_TWITTER) || columnType[0].equals(ColumnConst.LIST_FaceBook)) {
							typeCode = ColumnConst.LIST_NO_SIM;
						} else {
							typeCode = columnType[0];
						}

						if (ColumnConst.LIST_NO_SIM.equals(typeCode)) {
							List<String> arr = new ArrayList<>();
							if(StringUtil.isNotEmpty(indexTab.getTradition())){
								arr.addAll(CommonListChartUtil.formatGroupName(indexTab.getTradition()));
							}else{
								arr.addAll(CommonListChartUtil.formatGroupName(source));
							}
							if (columnType.length > 0) {
								for (String oneType : columnType) {
									if (ColumnConst.LIST_STATUS_COMMON.equals(oneType)) {
										if (!arr.contains(Const.GROUPNAME_WEIBO)) {
											arr.add(Const.GROUPNAME_WEIBO);
										}
									}
									if (ColumnConst.LIST_WECHAT_COMMON.equals(oneType)) {
										if (!arr.contains(Const.GROUPNAME_WEIXIN)) {
											arr.add(Const.GROUPNAME_WEIXIN);
										}
									}
									if (ColumnConst.LIST_TWITTER.equals(oneType)) {
										if (!arr.contains(Const.GROUPNAME_TWITTER)) {
											arr.add(Const.GROUPNAME_TWITTER);
										}
									}
									if (ColumnConst.LIST_FaceBook.equals(oneType)) {
										if (!arr.contains(Const.GROUPNAME_FACEBOOK)) {
											arr.add(Const.GROUPNAME_FACEBOOK);
										}
									}
								}
							}
							source = StringUtils.join(arr, ";");
						} else if (ColumnConst.LIST_SIM.equals(typeCode)) {//热点列表
							source = StringUtils.join(indexTab.getTradition(), ";");
						} else {// 其他为图表时
							/**
							 * 柱状图+饼状图+折线图 ：
							 * 		来源对比：groupName
							 * 		站点对比：tradition
							 * 		微信公众号对比：tradition(微信)
							 * 		专家模式：groupName
							 * 词云图+地图 :
							 * 		groupName
							 */
							source = indexTab.getGroupName();

							if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast())) {
								//站点对比
								source = StringUtil.join(indexTab.getTradition().split(";"), ";");
							} else if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(indexTab.getContrast())) {
								//微信公众号对比
								source = Const.GROUPNAME_WEIXIN;
							}
							if (StringUtil.isNotEmpty(indexTab.getXyTrsl())) {
								if (StringUtil.isEmpty(source)) {
									source = "ALL";
								}
							}
						}
						List<String> sourceList = formatGroupName(source);
						indexTab.setGroupName(StringUtils.join(sourceList, ";"));

						if (StringUtil.isEmpty(indexTab.getTrsl())) {
							if (StringUtil.isNotEmpty(indexTab.getStatusTrsl())) {
								indexTab.setTrsl(indexTab.getStatusTrsl());
							} else if (StringUtil.isNotEmpty(indexTab.getWeChatTrsl())) {
								indexTab.setTrsl(indexTab.getWeChatTrsl());
							}
						}

						//pieChart brokenLineChart barGraphChart wordCloudChart mapChart timeListInfo md5ListInfo
						String type = StringUtil.join(indexTab.getType(true), ";");
						String typeNew = "";
						if (type.contains("pie")) {
							typeNew = "pieChart";
						} else if (type.contains("Line")) {
							typeNew = "brokenLineChart";
						} else if (type.contains("bar")) {
							typeNew = "barGraphChart";
						} else if (type.contains("wordCloud")) {
							typeNew = "wordCloudChart";
						} else if (type.contains("map")) {
							typeNew = "mapChart";
						} else if (type.contains("md5")) {
							typeNew = "md5ListInfo";
						} else {
							typeNew = "timeListInfo";
						}
						if (StringUtil.isNotEmpty(indexTab.getTrsl()) || StringUtil.isNotEmpty(indexTab.getXyTrsl())) {
							indexTab.setSpecialType(SpecialType.SPECIAL);
						} else {
							indexTab.setSpecialType(SpecialType.COMMON);
						}
						indexTab.setType(typeNew);
						indexTabRepository.save(indexTab);
						n++;
						System.out.println("修改具体日常监测栏目信息当前执行为第" + n + "个，名字为：" + indexTab.getName());
					}catch(Exception e){
						System.out.println("日常监测栏目信息第" + n + "个错误了，名字为：" + indexTab.getName()+",id:"+indexTab.getId());
					}
				}
				indexTabRepository.flush();
			}
			return "没毛病，你就放心吧";
		} catch (Exception e) {
			return "修改失败了哦" + e.getMessage();
		}
	}

	private List<String> formatGroupName(String source) {
		if ("ALL".equals(source)) {
			source = Const.STATTOTAL_GROUP;
		}

		String[] split = source.split("[;|；]");
		List<String> sourceList = new ArrayList<>();
		for (String str : split) {
			if (Const.SOURCE_GROUPNAME_CONTRAST.containsKey(str)) {
				String group = Const.SOURCE_GROUPNAME_CONTRAST.get(str);
				if (!sourceList.contains(group)) {
					sourceList.add(group);
				}
			} else if ("传统媒体".equals(str)) {
				str = Const.TYPE_NEWS;
				String[] newsArr = str.split(";");
				for (String news : newsArr) {
					if (Const.SOURCE_GROUPNAME_CONTRAST.containsKey(news)) {
						String group = Const.SOURCE_GROUPNAME_CONTRAST.get(news);
						if (!sourceList.contains(group)) {
							sourceList.add(group);
						}
					}
				}
			}
		}
		return sourceList;
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年12月4日 yan.changjiang creat
 */
