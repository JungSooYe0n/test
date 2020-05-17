package com.trs.netInsight.support.api.entity.repository;

import com.trs.netInsight.support.api.entity.ApiFrequencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * api 调用频率日志 持久化服务
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since zhangya @ 2019年10月16日
 */
@Repository
public interface IApiFrequencyLogRepository extends JpaRepository<ApiFrequencyLog, String>,
        JpaSpecificationExecutor<ApiFrequencyLog>, PagingAndSortingRepository<ApiFrequencyLog, String> {

}
