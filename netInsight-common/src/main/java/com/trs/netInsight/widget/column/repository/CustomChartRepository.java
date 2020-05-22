package com.trs.netInsight.widget.column.repository;

import com.trs.netInsight.widget.column.entity.CustomChart;
import com.trs.netInsight.widget.column.entity.IndexTab;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomChartRepository extends PagingAndSortingRepository<CustomChart, String>, JpaSpecificationExecutor<CustomChart>, JpaRepository<CustomChart, String> {
    List<CustomChart> findByParentId(String parentId);
    List<CustomChart> findByParentId(String parentId, Sort sort);

    List<CustomChart> findByParentIdAndIsTop(String parentId, Boolean isTop);

    List<CustomChart> findByParentIdAndIsTop(String parentId, Boolean isTop, Sort sort);

    Long countAllByParentId(String id);
}
