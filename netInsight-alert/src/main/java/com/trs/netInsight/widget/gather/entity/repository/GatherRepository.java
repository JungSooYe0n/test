package com.trs.netInsight.widget.gather.entity.repository;

import com.trs.netInsight.widget.gather.entity.GatherPoint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface GatherRepository extends PagingAndSortingRepository<GatherPoint, String>, JpaSpecificationExecutor<GatherPoint>,JpaRepository<GatherPoint,String> {
    List<GatherPoint> findByUserId(String userId, Sort sort);

    List<GatherPoint> findByUserId(String userId, Pageable pageable);



}
