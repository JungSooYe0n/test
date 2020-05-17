package com.trs.netInsight.widget.column.repository;

import com.trs.netInsight.widget.column.entity.Columns;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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