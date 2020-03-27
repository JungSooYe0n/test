package com.trs.netInsight.widget.analysis.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.analysis.entity.ClusterData;

/**
 * 数据聚类Repository
 *
 * Created by even on 2017/5/10.
 */
@Repository
public interface ClusterDataRepository
		extends PagingAndSortingRepository<ClusterData, String>, JpaSpecificationExecutor<ClusterData> {
}
