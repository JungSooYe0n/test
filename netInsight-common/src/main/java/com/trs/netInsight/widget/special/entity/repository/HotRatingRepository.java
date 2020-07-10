package com.trs.netInsight.widget.special.entity.repository;

import com.trs.netInsight.widget.special.entity.HotRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface HotRatingRepository extends PagingAndSortingRepository<HotRating,String>,JpaSpecificationExecutor<HotRating>,JpaRepository<HotRating,String> {
    List<HotRating> findByOrganizationId(String organizationId);
}
