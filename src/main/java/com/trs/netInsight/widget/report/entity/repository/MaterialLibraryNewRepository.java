package com.trs.netInsight.widget.report.entity.repository;

import com.trs.netInsight.widget.report.entity.MaterialLibraryNew;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/22.
 * @desc舆情报告 极简模式  素材库分组持久层
 */
@Repository
public interface MaterialLibraryNewRepository extends PagingAndSortingRepository<MaterialLibraryNew,String>,JpaSpecificationExecutor<MaterialLibraryNew> {
    List<MaterialLibraryNew> findByUserId(String userId, Sort sort);

    List<MaterialLibraryNew> findByUserId(String userId, Pageable pagebale);

    List<MaterialLibraryNew> findBySubGroupId(String subGroupId, Sort sort);

    List<MaterialLibraryNew> findBySubGroupId(String subGroupId, Pageable pagebale);
}
