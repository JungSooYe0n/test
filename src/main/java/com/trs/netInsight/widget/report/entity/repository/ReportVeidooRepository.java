package com.trs.netInsight.widget.report.entity.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.report.entity.ReportVeidoo;

/**
 * 生成报告模板的模块Repository
 * @Type ReportVeidooRepository.java
 * @Desc 
 * @author 谷泽昊
 * @date 2017年11月24日 下午4:44:54
 * @version
 */
@Repository
public interface ReportVeidooRepository extends PagingAndSortingRepository<ReportVeidoo, String>,JpaSpecificationExecutor<ReportVeidoo>{

	List<ReportVeidoo> findByReportId(String reportId, Sort sort);
}
