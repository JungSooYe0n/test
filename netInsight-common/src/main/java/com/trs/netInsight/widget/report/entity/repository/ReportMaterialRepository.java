package com.trs.netInsight.widget.report.entity.repository;

import com.trs.netInsight.widget.report.entity.ReportMaterial;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * 素材库Repository
 * @Type ReportMaterialRepository.java
 * @Desc 
 * @author 谷泽昊
 * @date 2017年11月24日 下午4:43:51
 * @version
 */
@Repository
public interface ReportMaterialRepository extends PagingAndSortingRepository<ReportMaterial, String>,JpaSpecificationExecutor<ReportMaterial>{

}
