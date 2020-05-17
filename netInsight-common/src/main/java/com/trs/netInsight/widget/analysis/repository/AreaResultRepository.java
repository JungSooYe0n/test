package com.trs.netInsight.widget.analysis.repository;

import com.trs.netInsight.widget.analysis.entity.AreaResult;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * 地域热点聚类结果Repository
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Repository
public interface AreaResultRepository
		extends PagingAndSortingRepository<AreaResult, String>, JpaSpecificationExecutor<AreaResult> {
}
