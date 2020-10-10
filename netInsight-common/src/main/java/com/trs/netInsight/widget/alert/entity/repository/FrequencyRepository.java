package com.trs.netInsight.widget.alert.entity.repository;

import com.trs.netInsight.widget.alert.entity.Frequency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 预警频率检索Repository
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Repository
public interface FrequencyRepository
		extends JpaRepository<Frequency, String>,PagingAndSortingRepository<Frequency, String>, JpaSpecificationExecutor<Frequency> {

}
