package com.trs.netInsight.widget.column.repository;

import com.trs.netInsight.widget.column.entity.StatisticalChart;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticalChartRepository extends PagingAndSortingRepository<StatisticalChart, String>, JpaSpecificationExecutor<StatisticalChart> {
    List<StatisticalChart> findByParentId(String parentId);
    List<StatisticalChart> findByParentId(String parentId, Sort sort);
    List<StatisticalChart> findByParentIdAndIsTop(String parentId, Boolean isTop);
    List<StatisticalChart> findByParentIdAndIsTop(String parentId, Boolean isTop, Sort sort);
}
