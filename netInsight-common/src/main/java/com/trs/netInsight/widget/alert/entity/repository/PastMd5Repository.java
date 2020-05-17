package com.trs.netInsight.widget.alert.entity.repository;

import com.trs.netInsight.widget.alert.entity.PastMd5;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PastMd5Repository extends JpaSpecificationExecutor<PastMd5> ,JpaRepository<PastMd5, String>{

	/**
	 * 通过备份规则id查已发过的md5
	 * @param ruleBackId
	 * @return
	 */
	public List<PastMd5> findByRuleBackId(String ruleBackId);
} 
