package com.trs.netInsight.widget.report.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.report.entity.ReportNew;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by shao.guangze on 2018年5月29日 下午8:22:21
 */
@Repository
public interface ReportNewRepository extends PagingAndSortingRepository<ReportNew, String>,JpaSpecificationExecutor<ReportNew>,JpaRepository<ReportNew,String> {
	Page<ReportNew> findByReportTypeAndUserIdAndDocPathNotNull(String reportType, String userId, Pageable pageable);
	
	Page<ReportNew> findByReportTypeAndUserIdAndTemplateId(String reportType, String userId, String templateId, Pageable pageable);
	Page<ReportNew> findByReportTypeAndSubGroupIdAndTemplateId(String reportType, String subGroupId, String templateId, Pageable pageable);

	Page<ReportNew> findByReportTypeAndUserIdAndReportNameLikeAndDocPathNotNull(String reportType, String userId, String reportName, Pageable pageable);

	Page<ReportNew> findByReportTypeAndUserIdAndGroupNameAndDocPathNotNull(String reportType, String userId, String groupName, Pageable pageable);

	Page<ReportNew> findByReportTypeAndUserIdAndGroupNameAndReportNameLikeAndCreatedTimeGreaterThanAndDocPathNotNull(String reportType, String userId, String groupName, String reportName, Date createdTime, Pageable pageable);

	List<ReportNew> findByReportTypeAndTemplateListIsNotNull(String reportType);
	List<ReportNew> findAllByReportTypeAndUserIdAndGroupName(String reportType, String userId, String groupName);
	List<ReportNew> findByUserId(String userId);
	@Transactional
	void deleteByUserIdAndGroupName(String userId, String groupName);

	/***
	 * 测试不可用，可能是因为字段重复？？？
	 * @param current_group_name
	 * @param userId
	 * @param origin_group_name
	 */
	@Query(value = "update report_new set group_name=?1 where user_id=?2 and group_name=?3 ", nativeQuery = true)
	@Transactional
	@Modifying
	void saveGroupName(String current_group_name, String userId, String origin_group_name);

}
