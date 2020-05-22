package com.trs.netInsight.widget.special.entity.repository;

import com.trs.netInsight.widget.special.SpecialCustomChart;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialCustomChartRepository extends PagingAndSortingRepository<SpecialCustomChart, String>, JpaSpecificationExecutor<SpecialCustomChart> {
        List<SpecialCustomChart> findByParentId(String parentId);
        List<SpecialCustomChart> findByParentId(String parentId, Sort sort);
        List<SpecialCustomChart> findByParentIdAndIsTop(String parentId, Boolean isTop);
        List<SpecialCustomChart> findByParentIdAndIsTop(String parentId, Boolean isTop, Sort sort);
}
