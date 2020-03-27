package com.trs.netInsight.widget.report.entity.repository;

import com.trs.netInsight.widget.report.entity.SpecialReportPreivew;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/***
 *  Created by shao.guangze on 2018/7/16
 */
@Repository
public interface SpecialReportPreivewRepository  extends PagingAndSortingRepository<SpecialReportPreivew, String>,JpaSpecificationExecutor<SpecialReportPreivew> {

    List<SpecialReportPreivew> findByUserId(String userId);

    List<SpecialReportPreivew> findByUserIdAndReportId(String userId, String reportId);
    List<SpecialReportPreivew> findBySubGroupIdAndReportId(String subGroupId, String reportId);

    @Transactional
    void deleteByUserIdAndReportId(String userId,String reportId);
}
