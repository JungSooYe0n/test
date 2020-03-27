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

import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trs.netInsight.util.CollectionsUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.column.entity.NavigationEnum;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.repository.NavigationRepository;
import com.trs.netInsight.widget.column.service.IIndexPageService;
import com.trs.netInsight.widget.column.service.IIndexTabService;
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
	private IUserService userService;
	@Autowired
	private IIndexTabMapperService indexTabMapperService;
	@Autowired
	private IndexTabRepository indexTabRepository;

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
	public void copyPageAndTab(User orgUser, User user) {
//		List<IndexPage> indexPageList = findByUserId(orgUser.getId(), new Sort(Sort.Direction.DESC, "sequence"));
//		List<NavigationConfig> navigations = navigationRepository.findByUserId(user.getId(),
//				new Sort(Sort.Direction.DESC, "sequence"));
//		NavigationConfig navigation = null;
//		if (indexPageList != null && indexPageList.size() > 0) {
//			for (IndexPage indexPage : indexPageList) {
//				if (indexPage != null) {
//					IndexPage pageCopy = indexPage.pageCopy();
//					pageCopy.setUserId(user.getId());
//					pageCopy.setOrganizationId(user.getOrganizationId());
//					// 此时说明该pageCopy 为 自定义导航栏下的内容
//					if (pageCopy.getTypeId() != null && pageCopy.getTypeId().length() > 0) {
//						navigation = navigationRepository.findOne(pageCopy.getTypeId());
//						if(navigation!=null){
//							int sequence = navigation.getSequence();
//							for (NavigationConfig navigationTemp : navigations) {
//								// 争取匹配
//								if (navigationTemp.getSequence() == sequence) {
//									pageCopy.setTypeId(navigationTemp.getId());
//									break;
//								}
//							}
//						}
//					}
//					IndexPage page = indexPageRepository.save(pageCopy);
//					List<IndexTabMapper> indexTabList = tabMapperRepository.findByIndexPage(indexPage);
////					List<IndexTab> indexTabList = indexTabRepository.findByParentId(indexPage.getId(),
////							new Sort(Sort.Direction.DESC, "sequence"));
//					if (page != null && indexTabList != null && indexTabList.size() > 0) {
//						for (IndexTabMapper indexTabMapper : indexTabList) {
//							IndexTab indexTab = indexTabMapper.getIndexTab();
//							IndexTab tabCopy = indexTab.tabCopy();
//							tabCopy.setSequence(indexTabMapper.getSequence());
//							tabCopy.setUserId(user.getId());
//							tabCopy.setParentId(page.getId());
//							tabCopy.setOrganizationId(user.getOrganizationId());
//							indexTabService.save(tabCopy, false);
//						}
//					}
//				}
//			}
//		}
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
		User user = userService.findById(ownerId);
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
	public List<Map<String, Object>> findByTypeId(String typeId, boolean one, boolean definedself) {
		User user = UserUtils.getUser();
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> data = null;
		// 优化 一次性把一二级都返回给前端
		// if (one) { // 一级栏目组（导航栏标记）
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
							datalist.put("indexPageName", indexPage.getParentName());
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
		/*
		 * }else { if (typeId == null || !definedself) { typeId = ""; }
		 * List<IndexPage> indexPages =
		 * indexPageRepository.findByUserIdAndTypeId(user.getId(),typeId); if
		 * (CollectionsUtil.isNotEmpty(indexPages)) { for (IndexPage indexPage :
		 * indexPages) { data = new HashMap<>(); data.put("indexPageName",
		 * indexPage.getParentName()); data.put("id", indexPage.getId());
		 * data.put("definedself", 999);//前端忽略改值 result.add(data); } } }
		 */
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
							datalist.put("indexPageName", indexPage.getParentName());
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
							datalist.put("indexPageName", indexPage.getParentName());
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
		User user = userService.findById(userId);
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
				data.put("indexPageName", indexPage.getParentName());
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

						// 删除栏目映射关系，isMe为true的栏目关系须级联删除栏目实体
						List<IndexTabMapper> findByIndexTab = indexTabMapperService.findByIndexTab(mapper.getIndexTab());
						//删除所有与indexTab关联的  否则剩余关联则删除indexTab时失败
						tabMapperRepository.delete(findByIndexTab);
						if (mapper.isMe()) {
							indexTabRepository.delete(mapper.getIndexTab());
						}
					}
				}
				// 删除栏目组
				indexPageRepository.delete(indexPageId);
			}
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