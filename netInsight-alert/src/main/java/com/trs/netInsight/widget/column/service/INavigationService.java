package com.trs.netInsight.widget.column.service;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;

import java.util.List;

/**
 * 导航栏
 * @author xiaoying
 *
 */
public interface INavigationService {
	/**
	 * 通过userId(超管、机构管理员、运维)或通过subGroupId（普通用户）找所有的导航栏 有排序
	 * @param userId
	 * @return
	 */
	List<NavigationConfig> findByUserIdOrSubGroupIdAndSort(String userId, String sortBy);

	/**
	 * @return
	 */
	NavigationConfig addNavigation(String name);

	/**
	 * 删除该导航栏下的所有一级二级栏目
	 * @param typeId
	 * @return
	 */
	public Object deleteNavigation(String typeId) throws OperationException;

	/**
	 * 导航栏拖拽
	 * @param typeId
	 * @return
	 */
	public Object moveNavigation(String typeId);

	/**
	 *
	 * 导航栏隐藏显示接口
	 * @param typeId 导航栏id
	 * @param hide 隐藏true 不隐藏false
	 */
	public Object hideOrShowNavi(String typeId, boolean hide);

	/**
	 * 修改导航栏
	 * @param typeId 导航栏id
	 * @param name 修改后的名字
	 * @return
	 */
	public Object updateNavigation(String typeId, String name);

	void copyNavigation2Common(User orgUser, User user);

	/**
	 * 权限重构 机构管理员数据 同步到某用户分组下
	 * @param navIds
	 * @param subGroup
	 */
	void copySomeNavigationToUserGroup(List<String> navIds, SubGroup subGroup);
	
	/**
	 * 根据id查询
	 * @date Created at 2018年11月7日  下午6:15:39
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param id
	 * @return
	 */
	public NavigationConfig findOne(String id);

	/**
	 * 只为迁移历史数据
	 * @param userId
	 * @return
	 */
	public List<NavigationConfig> findByUserId(String userId);

	/**
	 * 批量修改 只为迁移历史数据
	 * @param navigationConfigs
	 */
	public void updateAll(List<NavigationConfig> navigationConfigs);

}
