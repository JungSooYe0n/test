package com.trs.netInsight.support.log.repository;

import com.trs.netInsight.support.log.entity.RequestTimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RequestTimeLogRepository extends PagingAndSortingRepository<RequestTimeLog, String>, JpaSpecificationExecutor<RequestTimeLog>,JpaRepository<RequestTimeLog,String> {
}
