package com.trs.netInsight.support.log.repository;


import com.trs.netInsight.support.log.entity.FuzzySearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 普通搜索模糊查询 存入日志
 * @Type FuzzySearchLogRepository.java
 * @author 张娅
 * @date 2020年3月11日14:45:47
 * @version
 */
@Repository
public interface FuzzySearchLogRepository extends JpaRepository<FuzzySearchLog, String>, JpaSpecificationExecutor<FuzzySearchLog> {

}
