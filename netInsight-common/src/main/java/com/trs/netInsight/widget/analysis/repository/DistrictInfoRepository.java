package com.trs.netInsight.widget.analysis.repository;

import com.trs.netInsight.widget.analysis.entity.DistrictInfo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 地域信息实体Repository
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Repository
public interface DistrictInfoRepository extends PagingAndSortingRepository<DistrictInfo, String>, JpaSpecificationExecutor<DistrictInfo> {

	/**
	 * 根据地域id检索列表
	 * 
	 * @param areaId
	 * @return
	 */
	public List<DistrictInfo> findById(String areaId);
}
