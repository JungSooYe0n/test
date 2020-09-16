package com.trs.netInsight.widget.alert.entity.repository;

import com.trs.netInsight.widget.alert.entity.AlertTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AlertTimeRepository extends PagingAndSortingRepository<AlertTime, String>,
        JpaSpecificationExecutor<AlertTime>, JpaRepository<AlertTime, String> {
    public List<AlertTime> findByUserId(String userId);
    public List<AlertTime> findBySubGroupId(String subGroupId);
}
