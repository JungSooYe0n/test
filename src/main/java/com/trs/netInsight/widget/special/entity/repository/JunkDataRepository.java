package com.trs.netInsight.widget.special.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.special.entity.JunkData;

/**
 * 垃圾数据
 *
 * Created by ChangXiaoyang on 2017/5/5.
 */
@Repository
public interface JunkDataRepository extends PagingAndSortingRepository<JunkData, String>,JpaSpecificationExecutor<JunkData> {

	/**
	 * 根据专题id查询
	 * @date Created at 2017年11月24日  下午3:51:40
	 * @Author 谷泽昊
	 * @param specialId
	 * @return
	 */
    List<JunkData> findBySpecialId(String specialId);
}
