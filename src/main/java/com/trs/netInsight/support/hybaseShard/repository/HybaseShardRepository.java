package com.trs.netInsight.support.hybaseShard.repository;

import com.trs.netInsight.support.hybaseShard.entity.HybaseShard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * @Author:拓尔思信息股份有限公司
 *
 * @Description: hybase小库持久层
 *
 * @Date:Created in  2020/3/3 15:40
 *
 * @Created By yangyanyan
 */
@Repository
public interface HybaseShardRepository extends PagingAndSortingRepository<HybaseShard, String>, JpaSpecificationExecutor<HybaseShard>, JpaRepository<HybaseShard,String> {
    HybaseShard findByOwnerId(String ownerId);
    HybaseShard findByOrganizationId(String organizationId);
}
