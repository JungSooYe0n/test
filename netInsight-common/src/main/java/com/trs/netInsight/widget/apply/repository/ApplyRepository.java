package com.trs.netInsight.widget.apply.repository;

import com.trs.netInsight.widget.apply.entity.Apply;
import com.trs.netInsight.widget.apply.entity.enums.ApplyUserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplyRepository extends PagingAndSortingRepository<Apply,String> , JpaSpecificationExecutor<Apply>, JpaRepository<Apply,String> {




}
