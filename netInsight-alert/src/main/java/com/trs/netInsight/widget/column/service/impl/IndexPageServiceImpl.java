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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.column.service.*;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.column.entity.NavigationEnum;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.repository.NavigationRepository;
import com.trs.netInsight.widget.user.entity.User;

/**
 * @Type IndexPageServiceImpl.java
 * @Desc
 * @author Administrator
 * @date 2017年12月4日 下午6:37:49
 * @version
 */
@Service
@Transactional
@Slf4j
public class IndexPageServiceImpl implements IIndexPageService {
	@Autowired
	private IndexTabMapperRepository tabMapperRepository;
	@Autowired
	private IndexPageRepository indexPageRepository;

	@Autowired
	private NavigationRepository navigationRepository;
	@Autowired
	private IIndexTabService indexTabService;
	@Autowired
	private UserRepository userService;
	@Autowired
	private IIndexTabMapperService indexTabMapperService;
	@Autowired
	private IndexTabRepository indexTabRepository;

	@Autowired
	private IndexTabMapperRepository indexTabMapperRepository;
	@Autowired
	private IColumnService columnService;
	@Autowired
	private IColumnChartService columnChartService;

	@Override
	public List<IndexPage> findByParentId(String parentId) {
		return indexPageRepository.findByParentId(parentId);
	}

	@Override
	public List<IndexPage> findByUserId(String userId, Sort sort) {
		return indexPageRepository.findByUserId(userId, sort);
	}

	@Override
	public List<IndexPage> findBySubGroupId(String subGroupId, Sort sort) {
		return indexPageRepository.findBySubGroupId(subGroupId,sort);
	}

	@Override
	public List<IndexPage> findByOrganizationId(String organizationId, Sort sort) {
		return indexPageRepository.findByOrganizationId(organizationId, sort);
	}

	@Override
	public IndexPage save(IndexPage indexPage) {
		return indexPageRepository.saveAndFlush(indexPage);
	}

	@Override
	public IndexPage findOne(String oneId) {
		return indexPageRepository.findOne(oneId);
	}


	@Override
	public void copySomePageAndTabToUserGroup(List<String> pageIds, SubGroup group) {
		//被选中机构管理员的栏目分组
		List<IndexPage> indexPageList = indexPageRepository.findByIdIn(pageIds, new Sort(Sort.Direction.DESC, "sequence"));
		//当前被同步用户分组所拥有的导航
		List<NavigationConfig> navigations = navigationRepository.findBySubGroupId(group.getId(),
				new Sort(Sort.Direction.DESC, "sequence"));

		if (indexPageList != null && indexPageList.size() > 0) {
			for (IndexPage indexPage : indexPageList) {
				if (indexPage != null) {
					IndexPage pageCopy = indexPage.pageCopy();
					pageCopy.setSubGroupId(group.getId());
					pageCopy.setOrganizationId(group.getOrganizationId());
					pageCopy.setUserId("dataSync");
					// 此时说明该pageCopy 为 自定义导航栏下的内容
					if (pageCopy.getTypeId() != null && pageCopy.getTypeId().length() > 0) {
						//机构管理员的导航
						NavigationConfig navigation = navigationRepository.findOne(pageCopy.getTypeId());
						if(navigation!=null){
							int sequence = navigation.getSequence();
							for (NavigationConfig navigationTemp : navigations) {
								// 争取匹配
								if (navigationTemp.getSequence() == sequence) {
									//放入 被同步用户所属导航的id
									pageCopy.setTypeId(navigationTemp.getId());
									break;
								}
							}
						}
					}
					IndexPage page = indexPageRepository.save(pageCopy);
					//栏目分组所包含的栏目
					List<IndexTabMapper> indexTabList = tabMapperRepository.findByIndexPage(indexPage);
					if (page != null && indexTabList != null && indexTabList.size() > 0) {
						for (IndexTabMapper indexTabMapper : indexTabList) {
							IndexTab indexTab = indexTabMapper.getIndexTab();
							IndexTab tabCopy = indexTab.tabCopy();
							tabCopy.setSequence(indexTabMapper.getSequence());
							tabCopy.setSubGroupId(group.getId());
							tabCopy.setParentId(page.getId());
							tabCopy.setOrganizationId(group.getOrganizationId());
							tabCopy.setUserId("dataSync");
							indexTabService.save(tabCopy, false);
						}
					}
				}
			}
		}
	}

	@Override
	public IndexPage findOne(String ownerId, String indexPageId) {
		User user = userService.findOne(ownerId);
		IndexPage indexPage = null;
		if (ObjectUtil.isNotEmpty(user)){
			if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
				indexPage = indexPageRepository.findByIdAndUserId(indexPageId, ownerId);
			}else {
				indexPage =  indexPageRepository.findByIdAndSubGroupId(indexPageId, user.getSubGroupId());
			}
		}
		return indexPage;
	}

	@Override
	public void saveAndFulsh(List<IndexPage> indexPages) {
		this.indexPageRepository.save(indexPages);
		indexPageRepository.flush();
	}

	@Override
	public List<IndexPage> findByUserIdAndShare(String userId, boolean share) {
		return indexPageRepository.findByUserIdAndShare(userId, share);
	}

	@Override
	public List<Map<String, Object>> findByTypeId() {
		User user = UserUtils.getUser();
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> data = null;
		// 优化 一次性把一二级都返回给前端
		List<NavigationConfig> navigations = new ArrayList<>();
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			navigations = navigationRepository.findByUserIdAndSubGroupIdNull(user.getId());
		}else {
			navigations = navigationRepository.findBySubGroupId(user.getSubGroupId());
		}


		if (CollectionsUtil.isNotEmpty(navigations)) {
			for (NavigationConfig navigationConfig : navigations) {
				if (navigationConfig.getType() == NavigationEnum.column
						|| navigationConfig.getType() == NavigationEnum.definedself) {
					String tpId = "";
					data = new HashMap<>();
					data.put("indexPageName", navigationConfig.getName());
					data.put("id", navigationConfig.getId());
					data.put("definedself", 1);
					if (navigationConfig.getType() == NavigationEnum.definedself) {
						tpId = navigationConfig.getId();
						data.put("definedself", 0);
					}
					List<IndexPage> indexPages = new ArrayList<>();
					Map<String,Object> columnMap = columnService.getOneLevelColumnForMap(tpId,user);
					if(columnMap.containsKey("page")){
						indexPages = (List<IndexPage>)columnMap.get("page");
					}
					List<Object> pageList = columnService.sortColumn(new ArrayList<>(),null,indexPages,true,true);

					List<Object> list = new ArrayList<>();
					list.add(pageList);
					data.put("list", list);
					result.add(data);
				}
			}
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> findIndexPageForApi(User user) {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> data = null;
		// 优化 一次性把一二级都返回给前端
		List<NavigationConfig> navigations = new ArrayList<>();
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			navigations = navigationRepository.findByUserIdAndSubGroupIdNull(user.getId());
		}else {
			navigations = navigationRepository.findBySubGroupId(user.getSubGroupId());
		}


		if (CollectionsUtil.isNotEmpty(navigations)) {
			for (NavigationConfig navigationConfig : navigations) {
				if (navigationConfig.getType() == NavigationEnum.column
						|| navigationConfig.getType() == NavigationEnum.definedself) {
					String tpId = "";
					data = new HashMap<>();
					data.put("indexPageName", navigationConfig.getName());
					data.put("id", navigationConfig.getId());
					data.put("definedself", 1);
					if (navigationConfig.getType() == NavigationEnum.definedself) {
						tpId = navigationConfig.getId();
						data.put("definedself", 0);
					}
					List<IndexPage> indexPages = new ArrayList<>();
					if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
						indexPages = indexPageRepository.findByUserIdAndTypeId(user.getId(), tpId);
					}else {
						indexPages = indexPageRepository.findBySubGroupIdAndTypeId(user.getSubGroupId(),tpId);
					}

					List<Map<String, Object>> list = new ArrayList<>();
					if (CollectionsUtil.isNotEmpty(indexPages)) {
						for (IndexPage indexPage : indexPages) {
							Map<String, Object> datalist = new HashMap<>();
							datalist.put("indexPageName", indexPage.getName());
							datalist.put("id", indexPage.getId());
							//	datalist.put("definedself", 999);// 前端忽略改值
							list.add(datalist);
						}
					}
					data.put("list", list);
					result.add(data);
				}
			}
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> findByOrgAdminId(String orgAdminId) {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> data = null;
		// 优化 一次性把一二级都返回给前端
		// if (one) { // 一级栏目组（导航栏标记）
		List<NavigationConfig> navigations = navigationRepository.findByUserIdAndSubGroupIdNull(orgAdminId);
		if (CollectionsUtil.isNotEmpty(navigations)) {
			for (NavigationConfig navigationConfig : navigations) {
				if (navigationConfig.getType() == NavigationEnum.column
						|| navigationConfig.getType() == NavigationEnum.definedself) {
					String tpId = "";
					data = new HashMap<>();
					data.put("indexPageName", navigationConfig.getName());
					data.put("id", navigationConfig.getId());
					data.put("definedself", 1);
					if (navigationConfig.getType() == NavigationEnum.definedself) {
						tpId = navigationConfig.getId();
						data.put("definedself", 0);
					}
					List<IndexPage> indexPages = indexPageRepository.findByUserIdAndTypeId(orgAdminId, tpId);
					List<Map<String, Object>> list = new ArrayList<>();
					if (CollectionsUtil.isNotEmpty(indexPages)) {
						for (IndexPage indexPage : indexPages) {
							Map<String, Object> datalist = new HashMap<>();
							datalist.put("indexPageName", indexPage.getName());
							datalist.put("id", indexPage.getId());
							datalist.put("definedself", 999);// 前端忽略改值
							list.add(datalist);
						}
					}
					data.put("list", list);
					result.add(data);
				}
			}
		}
		return result;
	}

	public List<Map<String, Object>> findByUserId(String userId) {
		Map<String, Object> data = null;
		List<Map<String, Object>> result = new ArrayList<>();
		User user = userService.findOne(userId);
		List<NavigationConfig> navigations = null;
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			navigations = navigationRepository.findByUserIdAndSubGroupIdNull(userId,new Sort(Sort.Direction.ASC,"sequence"));
		}else {
			navigations = navigationRepository.findBySubGroupId(user.getSubGroupId(),new Sort(Sort.Direction.ASC,"sequence"));
		}
		if (CollectionsUtil.isNotEmpty(navigations)) {
			for (NavigationConfig navigationConfig : navigations) {
				//日常监测或者自定义,返回数据
				if (navigationConfig.getType() == NavigationEnum.column
						|| navigationConfig.getType() == NavigationEnum.definedself) {
					if(!navigationConfig.isHide()){
						data = new HashMap<>();
						data.put("indexPageName", navigationConfig.getName());
						data.put("id", navigationConfig.getId());
						data.put("definedself", 1);
						if (navigationConfig.getType() == NavigationEnum.definedself) {
							data.put("definedself", 0);
						}
						result.add(data);
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> findByUserIdAndTypeId(AppApiAccessToken token, String typeId) {
		String userId = token.getGrantSourceOwnerId();
		String subGoupId = token.getUser().getSubGroupId();
		List<NavigationConfig> navigationConfigs = navigationRepository.findByUserIdAndId(userId,typeId);
		if(navigationConfigs==null || navigationConfigs.size()<=0){
			Sort sort = new Sort(Sort.Direction.ASC, "sequence");
			navigationConfigs = navigationRepository.findBySubGroupId(subGoupId,sort);
		}
		if(navigationConfigs.get(0).getType() != NavigationEnum.definedself){
			typeId = "";
		}
		Map<String, Object> data = null;
		List<Map<String, Object>> result = new ArrayList<>();
		List<IndexPage> indexPages = null;
		if (UserUtils.ROLE_LIST.contains(token.getUser().getCheckRole())){
			indexPages = indexPageRepository.findByUserIdAndTypeIdOrderBySequence(userId, typeId);

		}else {
			indexPages = indexPageRepository.findBySubGroupIdAndTypeIdOrderBySequence(subGoupId, typeId);
		}
		if(indexPages==null || indexPages.size()<=0){
			// 从一级开始找
			// 把sonId为空的找出来 这是一级的
			Criteria<IndexPage> criteria = new Criteria<>();
			if (UserUtils.ROLE_LIST.contains(token.getUser().getCheckRole())){
				criteria.add(Restrictions.eq("userId", userId));
			}else {
				criteria.add(Restrictions.eq("subGroupId", subGoupId));
			}
			criteria.orderByASC("sequence");
			criteria.add(Restrictions.eq("typeId", typeId));
			indexPages = indexPageRepository.findAll(criteria);
		}
		if (CollectionsUtil.isNotEmpty(indexPages)) {
			for (IndexPage indexPage : indexPages) {
				data = new HashMap<>();
				data.put("indexPageName", indexPage.getName());
				data.put("id", indexPage.getId());
				data.put("definedself", 999);// 前端忽略改值
				result.add(data);
			}
		}
		return result;
	}

	@Override
	public List<IndexPage> findByUserIdForHistory(String userId) {
		return indexPageRepository.findByUserIdAndSubGroupIdIsNull(userId);
	}

	@Override
	@Transactional
	public void deleteByUserId(String userId) {
		List<IndexPage> indexPages = indexPageRepository.findByUserId(userId);
		if (ObjectUtil.isNotEmpty(indexPages)){
			for (IndexPage indexPage : indexPages) {
				String indexPageId = indexPage.getId();
				// 删除栏目组及下级子栏目
				List<IndexTabMapper> mappers = indexTabMapperService.findByIndexPageId(indexPageId);
				if (CollectionsUtil.isNotEmpty(mappers)) {
					for (IndexTabMapper mapper : mappers) {
						Integer deleteColumnChart = columnChartService.deleteCustomChartForTabMapper(mapper.getId());
						log.info("删除当前栏目下统计和自定义图表共："+deleteColumnChart +"条");
						if (mapper.isMe()) {
							// 删除栏目映射关系，isMe为true的栏目关系须级联删除栏目实体
							List<IndexTabMapper> findByIndexTab = indexTabMapperService.findByIndexTab(mapper.getIndexTab());
							//删除相关栏目映射的相关图表
							Integer deleteAbMapperColumnChart = 0;
							for(IndexTabMapper abMapper : findByIndexTab){
								deleteAbMapperColumnChart+= columnChartService.deleteCustomChartForTabMapper(abMapper.getId());
							}
							log.info("删除当前栏目相关统计和自定义图表共："+deleteAbMapperColumnChart +"条");

							//删除所有与indexTab关联的  否则剩余关联则删除indexTab时失败
							tabMapperRepository.delete(findByIndexTab);
							indexTabRepository.delete(mapper.getIndexTab());
						}else{
							//如果是引用，则只删除当前引用即可
							tabMapperRepository.delete(mapper);
						}
					}
				}
				// 删除栏目组
				indexPageRepository.delete(indexPageId);
			}
		}
	}


	public IndexPage addIndexPage(String parentId,String name,String typeId,User loginUser){
		//判断父id存在不存在，
		IndexPage indexPage = new IndexPage();
		indexPage.setName(name);
		indexPage.setHide(false);
		//排序先按照先入在前

		int seq = 0;
		seq = columnService.getMaxSequenceForColumn(parentId,typeId,loginUser);
		if(StringUtil.isNotEmpty(parentId)){

			List<IndexPage> parentList = indexPageRepository.findById(parentId);
			if(parentList!= null && parentList.size() >0){
				IndexPage parent = parentList.get(0);
				indexPage.setParentId(parent.getId());
			}
		}
		indexPage.setSequence(seq +1);
		indexPage.setTypeId(typeId);
		indexPageRepository.saveAndFlush(indexPage);

		return indexPage;
	}




	@Override
    @Transactional
	public Object updateHistoryIndexPage(){
		try {
			List<IndexPage> list = indexPageRepository.findAll();
			int n =0;
			if(list != null && list.size() > 0){
				for(IndexPage indexPage : list){
					if(StringUtil.isEmpty(indexPage.getName())){
						indexPage.setName(indexPage.getParentName());
					}
					indexPage.setParentName(null);

					//修改原来的indepage  ，原来有parent_id，都是虚假的，没用，直接全部删掉
					indexPage.setParentId(null);
					if(StringUtil.isNotEmpty(indexPage.getTypeId()) && StringUtil.isEmpty(indexPage.getParentId())){
						IndexPage parent = indexPageRepository.findOne(indexPage.getTypeId());
						if(parent != null ){
							indexPage.setParentId(parent.getId());
							indexPage.setTypeId("");
						}
					}
					indexPage.setChildrenPage(null);
					indexPageRepository.save(indexPage);
					n++;
					System.out.println("当前执行为第"+n + "个，名字为："+indexPage.getName());
				}
				indexPageRepository.flush();
			}

			return "没毛病，你就放心吧";
		}catch (Exception e){

			return "修改失败了哦" +e.getMessage();
		}
	}
	@Override
	@Transactional
	public Object updateHistortIndexPageForOrganization(String orgId){
		try {
			List<IndexPage> list =indexPageRepository.findByOrganizationId(orgId);

			int n =0;
			if(list != null && list.size() > 0){
				System.out.println("根据机构信息找到分组数"+list.size());
				for(IndexPage indexPage : list){
					if(StringUtil.isEmpty(indexPage.getName())){
						indexPage.setName(indexPage.getParentName());
						indexPage.setParentName(null);
					}
					//添加层级，现在的
					List<IndexTabMapper> indexTabList = tabMapperRepository.findByIndexPage(indexPage);
					// 判断当前的大栏目有没有被排序过
					if (indexPage.getOrderBefore() == null || !"after".equals(indexPage.getOrderBefore())) {
						List<IndexTabMapper> nohideList = new ArrayList<>();// 展示的
						List<IndexTabMapper> hideList = new ArrayList<>();// 隐藏的
						for (IndexTabMapper indextab : indexTabList) {
							if (indextab.isHide()) {
								hideList.add(indextab);
							} else {
								nohideList.add(indextab);
							}
						}
						indexTabList = new ArrayList<>();
						indexTabList.addAll(nohideList);
						indexTabList.addAll(hideList);
						// 根据各自隐藏展示的顺序排序
						for (int i = 0; i < hideList.size(); i++) {
							IndexTabMapper index = hideList.get(i);
							index.setSequence(i + 1);
							// tabMapperRepository.save(index);
						}
						tabMapperRepository.save(hideList);
						for (int j = 0; j < nohideList.size(); j++) {
							IndexTabMapper index = nohideList.get(j);
							index.setSequence(j + 1);
						}
						tabMapperRepository.save(nohideList);
						// 排序之后 存标示为已排序
						indexPage.setOrderBefore("after");
					}
					//修改原来的indepage  ，原来有parent_id，都是虚假的，没用，直接全部删掉
					indexPage.setParentId(null);
					if(StringUtil.isNotEmpty(indexPage.getTypeId()) && StringUtil.isEmpty(indexPage.getParentId())){
						IndexPage parent = indexPageRepository.findOne(indexPage.getTypeId());
						if(parent != null ){
							indexPage.setParentId(parent.getId());
							indexPage.setTypeId("");
						}
					}
					indexPage.setChildrenPage(null);
					indexPageRepository.save(indexPage);
					n++;
					System.out.println("修改分组 - --当前执行为第"+n + "个，名字为："+indexPage.getName());
				}
				indexPageRepository.flush();
			}

			return "没毛病，你就放心吧";
		}catch (Exception e){

			return "修改失败了哦" +e.getMessage();
		}
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年12月4日 Administrator creat
 */