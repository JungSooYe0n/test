package com.trs.netInsight.widget.special.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.special.entity.SpecialExponent;

/**
 * 专题指数持久层
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年5月3日
 *
 */
@Repository
public interface SpecialExponentRepository
		extends JpaRepository<SpecialExponent, String>, JpaSpecificationExecutor<SpecialExponent> {

	/**
	 * 根据专题及计算时间范围统计各指数,并按照指定条件排序
	 * 
	 * @since changjiang @ 2018年5月4日
	 * @param specialIds
	 * @param begin
	 * @param end
	 * @param sort
	 * @return
	 * @Return : List<SpecialExponent>
	 */
	@Query(value = "select `special_id`, `special_name`,"
			+ " SUM(`hot_degree`) AS `hot_degree`, SUM(`netizen_degree`) AS `netizen_degree`,"
			+ " SUM(`meta_degree`) AS `meta_degree`" 
			+ " FROM `special_exponent`"
			+ " WHERE `special_id` IN (:specialIds)" 
			+ " AND (`compute_time` BETWEEN (:begin) AND (:end))"
			+ " GROUP BY `special_id`", nativeQuery = true)
	public List<Object[]> computeTotleExponentBySort(@Param(value = "specialIds") List<String> specialIds,
			@Param(value = "begin") Date begin, @Param(value = "end") Date end);

	/**
	 * 在指定时间范围内检索指定专题指数集,并按照计算时间进行
	 * @since changjiang @ 2018年5月7日
	 * @param specialId
	 * @param begin
	 * @param end
	 * @return
	 * @Return : Object[]
	 */
	@Query(value = "select `special_name`, `compute_time`,"
			+ " `hot_degree` AS `hot_degree`, `netizen_degree` AS `netizen_degree`,"
			+ " `meta_degree` AS `meta_degree`" 
			+ " FROM `special_exponent`"
			+ " WHERE `special_id` = (:specialId)" 
			+ " AND (`compute_time` BETWEEN (:begin) AND (:end))"
			+ " ORDER BY `compute_time` ASC;", nativeQuery = true)
	public List<Object[]> computeExponentGroupbyTime(@Param(value = "specialId") String specialId,
			@Param(value = "begin") Date begin, @Param(value = "end") Date end);
	
}
