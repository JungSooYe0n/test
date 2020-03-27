package com.trs.netInsight.widget.special.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trs.netInsight.widget.special.entity.SearchRecord;

/**
 * 检索关键词记录
 * Created by Xiaoying on 2018/11/26.
 *
 */
public interface ISearchRecordService {
	
	/**
	 * 增加记录
	 * @param record
	 */
	public void createRecord(String keywords);
	
	/**
	 * 根据userId分页查询
	 * @param userId
	 * @param pageable
	 * @return
	 */
	public List<SearchRecord> findByUserId(String userId);
	
	/**
	 * 通过userid和搜索关键词查询  避免记录重复
	 * @param userId
	 * @param keywords
	 * @return
	 */
	public List<SearchRecord> findByUserIdAndKeywords(String userId,String keywords);

}
