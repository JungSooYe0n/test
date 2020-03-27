package com.trs.netInsight.widget.alert.entity.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.alert.entity.Frequency;

/**
 * 预警频率检索Repository
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Repository
public interface FrequencyRepository
		extends PagingAndSortingRepository<Frequency, String>, JpaSpecificationExecutor<Frequency> {

}
