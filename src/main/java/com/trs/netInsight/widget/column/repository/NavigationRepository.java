package com.trs.netInsight.widget.column.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.column.entity.NavigationConfig;
/**
 * 导航栏
 * xiaoying
 */
@Repository
public interface NavigationRepository extends JpaRepository<NavigationConfig, String> {
	/**
	 * 通过userId找所有的导航栏 有排序
	 * @param userId
	 * @return
	 */
	List<NavigationConfig> findByUserIdAndSubGroupIdNull(String userId,Sort sort);
	/**
	 * 通过userId找所有的导航栏 无排序
	 * @param userId
	 * @return
	 */
	List<NavigationConfig> findByUserIdAndSubGroupIdNull(String userId);
	/**
	 * 通过用户分组id查找所有的导航栏 有排序
	 * @param subGroupId
	 * @param sort
	 * @return
	 */
	List<NavigationConfig> findBySubGroupId(String subGroupId,Sort sort);

	/**
	 * 通过用户分组id查找所有的导航栏 无排序
	 * @param subGroupId
	 * @return
	 */
	List<NavigationConfig> findBySubGroupId(String subGroupId);
	
	/**
	 * 通过userId找所有的导航栏 无排序
	 * @param userId
	 * @return
	 */
	List<NavigationConfig> findByUserIdAndSubGroupIdIsNull(String userId);
	List<NavigationConfig> findByUserId(String userId);
	/**
	 * 通过userId找所有的导航栏 无排序
	 * @param userId
	 * @return
	 */
	List<NavigationConfig> findByUserIdAndId(String userId, String Id);

	/**
	 * 根据id查询
	 * @param ids
	 * @return
	 */
	List<NavigationConfig> findByIdIn(Collection<String> ids);
}
