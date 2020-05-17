package com.trs.netInsight.widget.report.entity.repository;

import com.trs.netInsight.widget.report.entity.ReportData;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 报告repository
 * @Type ReportDataRepository.java
 * @Desc 
 * @author 谷泽昊
 * @date 2017年11月24日 下午4:42:19
 * @version
 */
public interface ReportDataRepository extends PagingAndSortingRepository<ReportData, String>,JpaSpecificationExecutor<ReportData>{

}
