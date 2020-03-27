package com.trs.netInsight.widget.column.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.user.entity.SubGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.column.entity.NavigationEnum;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.NavigationRepository;
import com.trs.netInsight.widget.column.service.IColumnService;
import com.trs.netInsight.widget.column.service.INavigationService;
import com.trs.netInsight.widget.user.entity.User;


@Service
public class NavigationServiceImpl implements INavigationService {

	@Autowired
	private NavigationRepository navigationRepository;
	@Autowired
	private IndexPageRepository indexPageRepository;
	@Autowired
	private IColumnService columnService;

	@Override
	public List<NavigationConfig> findByUserIdOrSubGroupIdAndSort(String userId, String sortBy) {
		Sort sort = new Sort(Sort.Direction.ASC, sortBy);
		List<NavigationConfig> navigationList = navigationRepository.findByUserIdAndSubGroupIdNull(userId, sort);
		// 如果用户为超管
		if (UserUtils.isSuperAdmin()) {
			NavigationConfig config1 = new NavigationConfig(NavigationEnum.monitoring, "数据监测", 1, false, false, false);
			NavigationConfig config2 = new NavigationConfig(NavigationEnum.management, "机构管理", 2, false, false, false);
			NavigationConfig config3 = new NavigationConfig(NavigationEnum.log, "系统日志", 3, false, false, false);
			NavigationConfig config4 = new NavigationConfig(NavigationEnum.setup, "系统设置", 4, false, false, false);
			// 要保存的
			List<NavigationConfig> navigationListData = new ArrayList<>();
			navigationListData.add(config1);
			navigationListData.add(config2);
			navigationListData.add(config3);
			navigationListData.add(config4);
			if (navigationList == null || navigationList.size() == 0) {
				// 为空 就添加原来的那些
				return navigationRepository.save(navigationListData);
			} else {
				navigationListData = removeSame(navigationList, navigationListData);
				if (navigationListData != null && navigationListData.size() > 0) {
					navigationRepository.save(navigationListData);
				}
			}
			return navigationRepository.findByUserIdAndSubGroupIdNull(userId, sort);
			// 如果用户为机构管理员;因为管理员和运维有课能栏目不一致，所以分来判断
		} else if (UserUtils.isRoleAdmin()) {
			// 为空 就添加原来的那些
			NavigationConfig config1 = new NavigationConfig(NavigationEnum.column, "日常监测", 1, false, false, false);
			NavigationConfig config2 = new NavigationConfig(NavigationEnum.special, "专题分析", 2, false, false, false);
			NavigationConfig config3 = new NavigationConfig(NavigationEnum.alert, "预警中心", 3, false, false, false);
			NavigationConfig config4 = new NavigationConfig(NavigationEnum.report, "舆情报告", 4, false, false, false);
			NavigationConfig config5 = new NavigationConfig(NavigationEnum.management, "机构管理", 5, false, false, false);
			NavigationConfig config6 = new NavigationConfig(NavigationEnum.log, "系统日志", 6, false, false, false);
			NavigationConfig config7 = new NavigationConfig(NavigationEnum.share, "共享监测", 7, false, false, false);
			NavigationConfig config8 = new NavigationConfig(NavigationEnum.square, "应用中心", 8, false, false, false);
			List<NavigationConfig> navigationListData = new ArrayList<>();
			navigationListData.add(config1);
			navigationListData.add(config2);
			navigationListData.add(config3);
			navigationListData.add(config4);
			navigationListData.add(config5);
			navigationListData.add(config6);
			navigationListData.add(config7);
			navigationListData.add(config8);
			if (navigationList == null || navigationList.size() == 0) {
				return navigationRepository.save(navigationListData);
			} else {
				navigationListData = removeSame(navigationList, navigationListData);
				if (navigationListData != null && navigationListData.size() > 0) {
					navigationRepository.save(navigationListData);
				}
			}
			return navigationRepository.findByUserIdAndSubGroupIdNull(userId, sort);
			// 如果用户为运维
		} else if (UserUtils.isRolePlatform()) {
			// 为空 就添加原来的那些
			NavigationConfig config1 = new NavigationConfig(NavigationEnum.column, "日常监测", 1, false, false, false);
			NavigationConfig config2 = new NavigationConfig(NavigationEnum.special, "专题分析", 2, false, false, false);
			NavigationConfig config3 = new NavigationConfig(NavigationEnum.alert, "预警中心", 3, false, false, false);
			NavigationConfig config4 = new NavigationConfig(NavigationEnum.report, "舆情报告", 4, false, false, false);
			NavigationConfig config5 = new NavigationConfig(NavigationEnum.management, "机构管理", 5, false, false, false);
			NavigationConfig config6 = new NavigationConfig(NavigationEnum.log, "系统日志", 6, false, false, false);
			//运维账号不需要共享监测
			//NavigationConfig config7 = new NavigationConfig(NavigationEnum.share, "共享监测", 7, false, false, false);
			NavigationConfig config8 = new NavigationConfig(NavigationEnum.square, "应用中心", 8, false, false, false);
			List<NavigationConfig> navigationListData = new ArrayList<>();
			navigationListData.add(config1);
			navigationListData.add(config2);
			navigationListData.add(config3);
			navigationListData.add(config4);
			navigationListData.add(config5);
			navigationListData.add(config6);
			//navigationListData.add(config7);
			navigationListData.add(config8);
			if (navigationList == null || navigationList.size() == 0) {

				return navigationRepository.save(navigationListData);
			} else {
				//删除掉共享监测
				navigationList = removeShare(navigationList);
				navigationListData = removeSame(navigationList, navigationListData);

				if (navigationListData != null && navigationListData.size() > 0) {
					navigationRepository.save(navigationListData);
				}
			}
			return navigationRepository.findByUserIdAndSubGroupIdNull(userId, sort);
		}else if (UserUtils.isRoleVisitor()){
			//访客权限
			//只有专题分析
			User user = UserUtils.getUser();
			if (StringUtil.isNotEmpty(user.getSubGroupId())){
				navigationList = navigationRepository.findBySubGroupId(user.getSubGroupId(),sort);
			}

			NavigationConfig config1 = new NavigationConfig(NavigationEnum.special, "专题分析", 1, false, false, false);
			List<NavigationConfig> navigationListData = new ArrayList<>();
			navigationListData.add(config1);
			if (navigationList == null || navigationList.size() == 0) {

				return navigationRepository.save(navigationListData);
			} else {
				navigationListData = removeSame(navigationList, navigationListData);
				if (navigationListData != null && navigationListData.size() > 0) {
					navigationRepository.save(navigationListData);
				}
			}
			navigationList = hideExcludeSpecial(navigationList);
			if (ObjectUtil.isEmpty(navigationList)){
				return navigationListData;
			}
			return navigationList;
		}else{
			String subGroupId = UserUtils.getUser().getSubGroupId();
			//普通用户 按用户分组id查询
			navigationList = navigationRepository.findBySubGroupId(subGroupId,sort);
			if (navigationList == null || navigationList.size() == 0) {
				// 为空 就添加原来的那些
				NavigationConfig config1 = new NavigationConfig(NavigationEnum.column, "日常监测", 1, false, false, false);
				NavigationConfig config2 = new NavigationConfig(NavigationEnum.special, "专题分析", 2, false, false, false);
				NavigationConfig config3 = new NavigationConfig(NavigationEnum.alert, "预警中心", 3, false, false, false);
				NavigationConfig config4 = new NavigationConfig(NavigationEnum.report, "舆情报告", 4, false, false, false);
				NavigationConfig config5 = new NavigationConfig(NavigationEnum.share, "共享监测", 5, false, false, false);
				NavigationConfig config6 = new NavigationConfig(NavigationEnum.square, "应用中心", 6, false, false, false);
				navigationList = new ArrayList<>();
				navigationList.add(config1);
				navigationList.add(config2);
				navigationList.add(config3);
				navigationList.add(config4);
				navigationList.add(config5);
				navigationList.add(config6);
				return navigationRepository.save(navigationList);
			}
			//因为权限重构 后 同步数据 只能同步所选择的 日常监测 栏目组或者专题分析。所以在同步数据的时候不能保证有哪个导航已入库，需作如下判断，且前四个顺序要固定是日常监测、专题分析、预警中心、预警报告
			boolean haveColumn = false;
			boolean haveSpecial = false;
			boolean haveAlert = false;
			boolean haveReport = false;
			boolean haveShare = false;
			boolean haveSquare = false;
			for (NavigationConfig navigationConfig : navigationList) {
				if (navigationConfig.getType().equals(NavigationEnum.column)) {
					haveColumn = true;
				}
				if (navigationConfig.getType().equals(NavigationEnum.special)){
					haveSpecial = true;
				}
				if (navigationConfig.getType().equals(NavigationEnum.alert)) {
					haveAlert = true;
				}
				if (navigationConfig.getType().equals(NavigationEnum.report)){
					haveReport = true;
				}
				if (navigationConfig.getType().equals(NavigationEnum.share)) {
					haveShare = true;
				}
				if (navigationConfig.getType().equals(NavigationEnum.square)){
					haveSquare = true;
				}
			}
			int size = navigationList.size();
			if (!haveColumn) {
				NavigationConfig navigationConfig = navigationRepository.save(new NavigationConfig(NavigationEnum.column, "日常监测", 1, false, false, false));
				navigationList.add(navigationConfig);
				size = size + 1;
			}

			if (!haveSpecial){
				NavigationConfig navigationConfig = navigationRepository.save(new NavigationConfig(NavigationEnum.special, "专题分析", 2, false, false, false));
				navigationList.add(navigationConfig);
			}
			if (!haveAlert) {
				NavigationConfig navigationConfig = navigationRepository.save(new NavigationConfig(NavigationEnum.alert, "预警中心", 3, false, false, false));
				navigationList.add(navigationConfig);
				size = size + 1;
			}

			if (!haveReport){
				NavigationConfig navigationConfig = navigationRepository.save(new NavigationConfig(NavigationEnum.report, "舆情报告", 4, false, false, false));
				navigationList.add(navigationConfig);
			}
			if (!haveShare) {
				NavigationConfig navigationConfig = navigationRepository.save(new NavigationConfig(NavigationEnum.share, "共享监测", size + 1, false, false, false));
				navigationList.add(navigationConfig);
				size = size + 1;
			}

			if (!haveSquare){
				NavigationConfig navigationConfig = navigationRepository.save(new NavigationConfig(NavigationEnum.square, "应用中心", size + 1, false, false, false));
				navigationList.add(navigationConfig);
			}
			return navigationList;
		}

	}

	/**
	 * 去除list2 中和list1 type一样的内容
	 * 
	 * @date Created at 2018年9月25日 下午2:31:51
	 * @Author 谷泽昊
	 * @param list1
	 * @param list2
	 * @return
	 */
	private List<NavigationConfig> removeSame(List<NavigationConfig> list1, List<NavigationConfig> list2) {
		List<NavigationConfig> listRemove = new ArrayList<>();
		for (NavigationConfig navigationConfig1 : list1) {
			for (NavigationConfig navigationConfig2 : list2) {
				if (navigationConfig2.getType().equals(navigationConfig1.getType())) {
					listRemove.add(navigationConfig2);
				}
			}
		}
		list2.removeAll(listRemove);
		for (int i = 0; i < list2.size(); i++) {
			NavigationConfig navigationConfig = list2.get(i);
			navigationConfig.setSequence(list1.size() + i + 1);
		}
		return list2;
	}

	/**
	 * 删除共享监测
	 * @param list
	 * @return
	 */
	private List<NavigationConfig> removeShare(List<NavigationConfig> list) {
		List<NavigationConfig> listRemove = new ArrayList<>();
		for (NavigationConfig navigationConfig : list) {
			if (navigationConfig.getType().equals(NavigationEnum.share)){
				listRemove.add(navigationConfig);
				navigationRepository.delete(navigationConfig.getId());
			}
		}
		list.removeAll(listRemove);
		Collections.sort(list);
		for (int i = 0; i < list.size(); i++) {
			NavigationConfig navigationConfig = list.get(i);
			navigationConfig.setSequence(i+1);
		}
        return list;
	}
	private List<NavigationConfig> hideExcludeSpecial(List<NavigationConfig> list){
		List<NavigationConfig> listRetain = new ArrayList<>();
		for (NavigationConfig navigationConfig : list) {
			if (navigationConfig.getType().equals(NavigationEnum.special)){
				listRetain.add(navigationConfig);
			}
		}
		return listRetain;
	}

	@Override
	public NavigationConfig addNavigation(String name) {
		User loginUser = UserUtils.getUser();
		String userId = loginUser.getId();
		List<NavigationConfig> configList = new ArrayList<>();
		if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			configList = navigationRepository.findByUserIdAndSubGroupIdNull(userId);
		}else {
			configList = navigationRepository.findBySubGroupId(loginUser.getSubGroupId());
		}

		NavigationConfig config = new NavigationConfig(NavigationEnum.definedself, name, configList.size() + 1, false,
				false, false);
		return navigationRepository.save(config);
	}

	@Override
	public Object deleteNavigation(String typeId) throws OperationException {
		User loginUser = UserUtils.getUser();
		String userId = loginUser.getId();
		List<NavigationConfig> configList = new ArrayList<>();
		if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			configList = navigationRepository.findByUserIdAndSubGroupIdNull(userId);
		}else {
			configList = navigationRepository.findBySubGroupId(loginUser.getSubGroupId());
		}
		NavigationConfig findOne = navigationRepository.findOne(typeId);
		NavigationEnum type = findOne.getType();
		// 原来的专项 报告 预警 不能删
		if (NavigationEnum.special.equals(type) || NavigationEnum.report.equals(type)
				|| NavigationEnum.alert.equals(type) || NavigationEnum.monitoring.equals(type)
				|| NavigationEnum.management.equals(type) || NavigationEnum.log.equals(type)
				|| NavigationEnum.setup.equals(type)|| NavigationEnum.share.equals(type)) {
			return "删除失败，默认模块不允许删除！";
		}
		int sequence = findOne.getSequence();
		for (NavigationConfig config : configList) {
			// 排序大于当前要删除的 序号减一
			int currentSequence = config.getSequence();
			if (currentSequence > sequence) {
				config.setSequence(currentSequence - 1);
				navigationRepository.save(config);
			}
		}
		// 删除导航栏下所有一级二级栏目
		List<IndexPage> indexPageList = indexPageRepository.findByTypeId(typeId);
		for (IndexPage indexPage : indexPageList) {
			String indexPageId = indexPage.getId();
			columnService.deleteOne(indexPageId);
		}
		navigationRepository.delete(typeId);
		return "删除成功";
	}

	/**
	 * 拖拽导航栏
	 * 
	 */
	@Override
	public Object moveNavigation(String typeId) {
		String[] split = typeId.split(";");
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			NavigationConfig findOne = navigationRepository.findOne(s);
			findOne.setSequence(i + 1);
			navigationRepository.save(findOne);
		}
		return "success";
	}

	@Override
	public Object hideOrShowNavi(String typeId, boolean hide) {
		NavigationConfig navigation = navigationRepository.findOne(typeId);
		navigation.setHide(hide);
		navigationRepository.save(navigation);
		return "success";
	}

	@Override
	public Object updateNavigation(String typeId, String name) {
		NavigationConfig navigation = navigationRepository.findOne(typeId);
		navigation.setName(name);
		navigationRepository.save(navigation);
		return "success";
	}

	@Override
	public void copyNavigation2Common(User orgUser,User user) {
		// 当前操作用户的id，即管理员id
		List<NavigationConfig> adminNavigations = navigationRepository.findByUserId(orgUser.getId());
		// List<NavigationConfig> commonNavigations = new
		// ArrayList<NavigationConfig>() ;
		NavigationConfig navigationConfig = null;
		NavigationConfig copyNavigation = null;
		if (adminNavigations != null && adminNavigations.size() > 0) {
			for (int i = 0; i < adminNavigations.size(); i++) {
				navigationConfig = adminNavigations.get(i);
				//管理员的不复制过去
				if (NavigationEnum.monitoring.equals(navigationConfig.getType())
						|| NavigationEnum.management.equals(navigationConfig.getType())
						|| NavigationEnum.log.equals(navigationConfig.getType())
						|| NavigationEnum.setup.equals(navigationConfig.getType())) {
					continue;
				}
				copyNavigation = navigationConfig.copyNavigation();
				copyNavigation.setUserId(user.getId());
				copyNavigation.setOrganizationId(user.getOrganizationId());
				navigationRepository.save(copyNavigation);
				// commonNavigations.add(copyNavigation);
			}
			// navigationRepository.save(commonNavigations);
		}
	}

	@Override
	public void copySomeNavigationToUserGroup(List<String> navIds, SubGroup subGroup) {
		List<NavigationConfig> adminNavigations = navigationRepository.findByIdIn(navIds);
		if (ObjectUtil.isNotEmpty(adminNavigations)){
			for (NavigationConfig adminNavigation : adminNavigations) {
				NavigationConfig navigationConfig = adminNavigation.copyNavigation();
				navigationConfig.setSubGroupId(subGroup.getId());
				//navigationConfig.setUserId(null);
				navigationConfig.setOrganizationId(subGroup.getOrganizationId());
				navigationConfig.setUserId("dataSync");
				navigationRepository.save(navigationConfig);
			}
		}
	}

	@Override
	public NavigationConfig findOne(String id) {
		return navigationRepository.findOne(id);
	}

	@Override
	public List<NavigationConfig> findByUserId(String userId) {
		return navigationRepository.findByUserIdAndSubGroupIdIsNull(userId);
	}

	@Override
	public void updateAll(List<NavigationConfig> navigationConfigs) {
		for (NavigationConfig navigationConfig : navigationConfigs) {
			navigationRepository.saveAndFlush(navigationConfig);

		}
	}


}
