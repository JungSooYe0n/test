package com.trs.netInsight.widget.column.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.column.entity.Columns;

/**
 * Columns Repository
 *
 * Created by trs on 2017/6/19.
 */
@Repository
public interface ColumnRepository extends PagingAndSortingRepository<Columns, String>,JpaSpecificationExecutor<Columns> {

	List<Columns> findByOrganizationId(String organizationId, Sort sort);
	
	
	
    List<Columns> findByUserId(String pageId, Sort sort);
}