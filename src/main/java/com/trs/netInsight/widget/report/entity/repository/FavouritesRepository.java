package com.trs.netInsight.widget.report.entity.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.report.entity.Favourites;

/**
 * 我的收藏数据访问 Created by xiaoying on 2017年12月13日
 */
@Repository
public interface FavouritesRepository
		extends PagingAndSortingRepository<Favourites, String>, JpaSpecificationExecutor<Favourites>,JpaRepository<Favourites,String> {

	/**
	 * 根据用户id查询收藏表，排序
	 * 
	 * @date Created at 2017年11月24日 下午4:38:11
	 * @Author 谷泽昊
	 * @param userId
	 * @param sort
	 * @return
	 */
	List<Favourites> findByUserId(String userId, Sort sort);

	/**
	 * 根据用户id查询收藏表，分页
	 * 
	 * @date Created at 2017年11月24日 下午4:38:15
	 * @Author 谷泽昊
	 * @param userId
	 * @param pageable
	 * @return
	 */
	List<Favourites> findByUserId(String userId, Pageable pageable);

	/**
	 * 只为迁移历史数据
	 * @param userId
	 * @return
	 */
	List<Favourites> findByUserId(String userId);
	/**
	 * 根据素材库id查询收藏
	 * @date Created at 2017年11月24日 下午4:38:18
	 * @Author 谷泽昊
	 * @param libraryId
	 * @return
	 */
	List<Favourites> findByLibraryId(String libraryId);
	
	/**
	 * 根据sid查询是否已收藏
	 * @Author liangxin
	 * @param sid
	 * @return
	 */
	List<Favourites> findBySid(String sid);
	
	/**
	 * 根据sid和userId查看是否已收藏
	 * @param userId
	 * @param sid
	 * @return
	 */
	Favourites findByUserIdAndSid(String userId,String sid);

	/**
	 * 根据sid和subGroupId查看是否已收藏
	 * @param subGroupId
	 * @param sid
	 * @return
	 */
	Favourites findBySubGroupIdAndSid(String subGroupId,String sid);

	
	/**
	 * 根据userId和sid列表查询是否收藏
	 * @param userId
	 * @param sid
	 * @return
	 */
	List<Favourites> findByUserIdAndSidIn(String userId,Collection<String> sid);

}
