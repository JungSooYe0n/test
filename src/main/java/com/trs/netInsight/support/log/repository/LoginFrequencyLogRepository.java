package com.trs.netInsight.support.log.repository;


import com.trs.netInsight.support.log.entity.FuzzySearchLog;
import com.trs.netInsight.support.log.entity.LoginFrequencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 普通搜索模糊查询 存入日志
 * @Type FuzzySearchLogRepository.java
 * @author 张娅
 * @date 2020年3月11日14:45:47
 * @version
 */
@Repository
public interface LoginFrequencyLogRepository extends JpaRepository<LoginFrequencyLog, String>, JpaSpecificationExecutor<LoginFrequencyLog> {


    @Query(value = "SELECT SUM(login_num) FROM login_frequency_log l WHERE l.organization_id = ?1 AND TO_DAYS(created_time) = TO_DAYS(now())", nativeQuery = true)
    Integer countOrganizationLoginNum(String orgId);

}
