package com.trs.netInsight.widget.report.entity.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.report.entity.Report;

/**
 * 舆情报告实体Repository
 * @Type ReportRepository.java
 * @Desc 
 * @author 谷泽昊
 * @date 2017年11月24日 下午4:44:23
 * @version
 */
@Repository
public interface ReportRepository extends PagingAndSortingRepository<Report, String>,JpaSpecificationExecutor<Report>{

	List<Report> findByUserId(String userId,Pageable pageable);
	List<Report> findBySubGroupId(String subGroupId,Pageable pageable);
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午3:33:43
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageRequest
	 * @return
	 */
	List<Report> findByOrganizationId(String organizationId, Pageable pageable);
}
