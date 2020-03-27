package com.trs.netInsight.widget.report.entity.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.trs.netInsight.widget.report.entity.ReportData;

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
