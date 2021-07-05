package com.trs.netInsight.widget.column.service.impl;

import com.trs.netInsight.widget.column.entity.HotTop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface HotTopRepository extends PagingAndSortingRepository<HotTop, String>, JpaSpecificationExecutor<HotTop>,JpaRepository<HotTop,String> {
    List<HotTop> findByUserIdOrderBySequence(String userId);

    @Override
    List<HotTop> findAll();

    List<HotTop> findByUserId(String userId);
    List<HotTop> findByUserIdAndName(String userId,String name);

}
