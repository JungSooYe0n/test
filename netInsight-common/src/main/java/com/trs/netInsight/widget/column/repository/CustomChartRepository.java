package com.trs.netInsight.widget.column.repository;

import com.trs.netInsight.widget.column.entity.CustomChart;
import com.trs.netInsight.widget.column.entity.IndexTab;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CustomChartRepository extends PagingAndSortingRepository<CustomChart, String>, JpaSpecificationExecutor<CustomChart>, JpaRepository<CustomChart, String> {
    List<CustomChart> findByParentId(String parentId);

    List<CustomChart> findByParentId(String parentId, Sort sort);

    Page<CustomChart> findByParentId(String parentId, Pageable pageable);

    List<CustomChart> findByParentIdAndIsTop(String parentId, Boolean isTop);

    List<CustomChart> findByParentIdAndIsTop(String parentId, Boolean isTop, Sort sort);

    Long countAllByParentId(String id);

    @Query(value = "update custom_chart set filter_info=?1 where id=?2 ", nativeQuery = true)
    @Transactional
    @Modifying
    public void saveFilterInfo(String filterInfo, String id);
}
