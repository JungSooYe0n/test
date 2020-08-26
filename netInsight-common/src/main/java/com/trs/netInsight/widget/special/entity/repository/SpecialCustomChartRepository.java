package com.trs.netInsight.widget.special.entity.repository;

import com.trs.netInsight.widget.special.entity.SpecialCustomChart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialCustomChartRepository extends PagingAndSortingRepository<SpecialCustomChart, String>, JpaSpecificationExecutor<SpecialCustomChart> {
        List<SpecialCustomChart> findByParentId(String parentId);
        List<SpecialCustomChart> findByParentId(String parentId, Sort sort);
        Page<SpecialCustomChart> findByParentId(String parentId, Pageable pageable);

        List<SpecialCustomChart> findByParentIdAndIsTop(String parentId, Boolean isTop);
        List<SpecialCustomChart> findByParentIdAndIsTop(String parentId, Boolean isTop, Sort sort);
}
