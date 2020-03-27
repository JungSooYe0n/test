package com.trs.netInsight.widget.report.entity.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import com.trs.netInsight.widget.report.entity.SpecialReportGroup;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by shao.guangze on 2018年6月12日 下午8:13:52
 * 与SpecialReportGroup相关联
 */
public interface ReportGroupRepository  extends PagingAndSortingRepository<SpecialReportGroup, String>,JpaSpecificationExecutor<SpecialReportGroup>{

	List<SpecialReportGroup> findByUserId(String userId);
	List<SpecialReportGroup> findBySubGroupId(String subGroupId);

	SpecialReportGroup findByUserIdAndGroupName(String userId, String groupName);

	@Transactional
	void deleteByUserIdAndGroupName(String userId, String groupName);

	/***
	 * 测试不可用！
	 * @param current_group_name
	 * @param userId
	 * @param origin_group_name
	 */
	@Query(value = "update special_report_group set group_name=?1 where user_id=?2 and group_name=?3 ", nativeQuery = true)
	@Transactional
	@Modifying
	void saveGroupName(String current_group_name, String userId, String origin_group_name);
}
