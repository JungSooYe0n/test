package com.trs.netInsight.widget.user.repository;

import com.trs.netInsight.widget.user.entity.SubGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


/**
 * 用户分组持久层
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/7/24 10:54.
 * @desc
 */
@Repository("subGroupRepository")
public interface SubGroupRepository extends JpaRepository<SubGroup,String>,JpaSpecificationExecutor<SubGroup> {

    /**机构下所有分组
     * 查询
     * @param organizationId
     * @return
     */
    public List<SubGroup> findByOrganizationId(String organizationId);

    /**
     * 通过分组名称查询分组
     * @param name
     * @return
     */
    public List<SubGroup> findByName(String name);


    /**
     * 根据 id 批量查询
     * @param subGroupIds
     * @param pageable
     * @return
     */
    public Page<SubGroup> findByIdIn(Collection<String> subGroupIds, Pageable pageable);

}
