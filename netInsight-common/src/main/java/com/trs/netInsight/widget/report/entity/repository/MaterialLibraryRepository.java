package com.trs.netInsight.widget.report.entity.repository;

import com.trs.netInsight.widget.report.entity.MaterialLibrary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 素材库
 * 
 * @Type MaterialLibraryRepository.java
 * @Desc
 * @author 谷泽昊
 * @date 2017年11月24日 下午4:40:21
 * @version
 */
@Repository
public interface MaterialLibraryRepository
		extends PagingAndSortingRepository<MaterialLibrary, String>, JpaSpecificationExecutor<MaterialLibrary> {

	/**
	 * 根据专项检测id查询
	 * 
	 * @date Created at 2017年11月24日 下午4:40:52
	 * @Author 谷泽昊
	 * @param specialId
	 * @return
	 */
	List<MaterialLibrary> findBySpecialId(String specialId);

	/**
	 * 根据用户id 分页查询
	 * 
	 * @date Created at 2017年11月24日 下午4:40:56
	 * @Author 谷泽昊
	 * @param userId
	 * @param pageable
	 * @return
	 */
	Page<MaterialLibrary> findByUserId(String userId, Pageable pageable);
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午3:40:50
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageable
	 * @return
	 */
	Page<MaterialLibrary> findByOrganizationId(String organizationId, Pageable pageable);

	/**
	 * 查询全部
	 */
	List<MaterialLibrary> findAll();
}
