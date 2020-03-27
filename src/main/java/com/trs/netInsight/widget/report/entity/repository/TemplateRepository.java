package com.trs.netInsight.widget.report.entity.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.trs.netInsight.widget.report.entity.Template;

/**
 * 模板表Repository
 * 
 * @Type TemplateRepository.java
 * @Desc
 * @author 谷泽昊
 * @date 2017年11月24日 下午4:45:22
 * @version
 */
public interface TemplateRepository
		extends PagingAndSortingRepository<Template, String>, JpaSpecificationExecutor<Template> {

	/**
	 * 根据用户id分页查询
	 * 
	 * @date Created at 2017年11月24日 下午4:45:34
	 * @Author 谷泽昊
	 * @param userId
	 * @param pageable
	 * @return
	 */
	List<Template> findByUserId(String userId, Pageable pageable);

	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午3:42:32
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param pageRequest
	 * @return
	 */
	List<Template> findByOrganizationId(String organizationId, PageRequest pageRequest);

}
