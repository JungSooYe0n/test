package com.trs.netInsight.widget.report.entity.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.report.entity.TemplateNew;

/**
 * Created by shao.guangze on 2018年5月24日 下午5:59:46
 */
@Repository
public interface TemplateNewRepository extends PagingAndSortingRepository<TemplateNew, String>,JpaSpecificationExecutor<TemplateNew>,JpaRepository<TemplateNew,String> {

	List<TemplateNew> findByUserId(String userId);

	List<TemplateNew> findByUserIdAndTemplateType(String userId, String templateType, Sort sort);
	
	List<TemplateNew> findBySubGroupIdAndTemplateType(String subGroupId, String templateType, Sort sort);

	List<TemplateNew> findByUserIdAndTemplateTypeAndIsDefault(String userId,String templateType,int isDefault);
	List<TemplateNew> findBySubGroupIdAndTemplateTypeAndIsDefault(String userId,String templateType,int isDefault);
	/**
	 * 
	 * findMaxTemplatePositionByUserIdAndTemplateType
	 * @author shao.guangze
	 * @return
	 */
/*	@Query("select max(t.template_position) from report_template_new t where t.user_id = :user_id AND t.template_type = :template_type")
	Object findMaxTemplatePosition(@Param("user_id") String user_id, @Param("template_type")String template_type);*/
}
