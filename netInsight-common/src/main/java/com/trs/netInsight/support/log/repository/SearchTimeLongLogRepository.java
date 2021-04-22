package com.trs.netInsight.support.log.repository;

import com.trs.netInsight.support.log.entity.SearchTimeLongLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author 马加鹏
 * @date 2021/4/21 17:32
 */
@Repository(value = "searchTimeLongLogRepository")
public interface SearchTimeLongLogRepository extends JpaRepository<SearchTimeLongLog, String>, JpaSpecificationExecutor<SearchTimeLongLog> {
}
