package com.trs.netInsight.widget.special.entity.repository;

import com.trs.netInsight.widget.special.entity.CustomSpecial;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  舆情报告  极简模式 持久层
 * Created by yangyanyan on 2018/10/10.
 */
@Repository
public interface CustomSpecialRepository extends PagingAndSortingRepository<CustomSpecial,String>,JpaSpecificationExecutor<CustomSpecial> {

    List<CustomSpecial> findByUserId(String userId, Sort sort);

    List<CustomSpecial> findByUserId(String userId, Pageable pagebale);

    List<CustomSpecial> findBySubGroupId(String subGroupId, Sort sort);

    List<CustomSpecial> findBySubGroupId(String subGroupId, Pageable pagebale);

    List<CustomSpecial> findBySpecialType(SpecialType specialType);
}
