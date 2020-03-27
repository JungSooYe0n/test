package com.trs.netInsight.widget.analysis.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.analysis.entity.TaskResult;

/**
 * 观点聚类结果表Repository
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Repository
public interface TaskResultRepository
		extends PagingAndSortingRepository<TaskResult, String>, JpaSpecificationExecutor<TaskResult> {
}
