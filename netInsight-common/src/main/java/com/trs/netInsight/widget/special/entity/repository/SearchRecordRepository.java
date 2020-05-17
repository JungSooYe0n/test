package com.trs.netInsight.widget.special.entity.repository;

import com.trs.netInsight.widget.special.entity.SearchRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 记录检索关键词持久层
 * Created by Xiaoying on 2018/11/26.
 *
 */
@Repository
public interface SearchRecordRepository extends JpaRepository<SearchRecord, String>, JpaSpecificationExecutor<SearchRecord>{

	/**
	 * 分页检索
	 * @param userId
	 * @param pageable
	 * @return
	 */
	public List<SearchRecord> findByUserId(String userId, Pageable pageable);
	
	/**
	 * 通过userid和搜索关键词查询  避免记录重复
	 * @param userId
	 * @param keywords
	 * @return
	 */
	public List<SearchRecord> findByUserIdAndKeywords(String userId, String keywords);

//	@Query(value = "SELECT s FROM search_record s WHERE user_id=(:userId) GROUP BY keywords ORDER BY created_time " )
	public List<SearchRecord> findByUserId(String userId, Sort sort);
}
