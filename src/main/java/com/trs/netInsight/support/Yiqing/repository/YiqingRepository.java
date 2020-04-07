package com.trs.netInsight.support.Yiqing.repository;

import com.trs.netInsight.support.Yiqing.entity.Yiqing;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lilyy
 * @date 2020/4/2 18:37
 */
@Repository
public interface YiqingRepository extends PagingAndSortingRepository<Yiqing,String> {

    Yiqing findByName(String name);

}
