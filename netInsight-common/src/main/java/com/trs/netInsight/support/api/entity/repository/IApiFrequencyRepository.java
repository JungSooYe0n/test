package com.trs.netInsight.support.api.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.support.api.entity.ApiFrequency;

/**
 * api 调用频率 持久化服务
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 */
@Repository
public interface IApiFrequencyRepository extends JpaRepository<ApiFrequency, String>,
        JpaSpecificationExecutor<ApiFrequency>, PagingAndSortingRepository<ApiFrequency, String> {

    /**
     * 根据code获取
     *
     * @param code
     * @return
     * @Return : List<ApiFrequency>
     * @since changjiang @ 2018年7月3日
     */
    public List<ApiFrequency> findByCode(int code);

    /**
     * 根据clientId及code值获取
     *
     * @param code
     * @param clientId
     * @return
     */
    public ApiFrequency findByCodeAndClientId(int code, String clientId);

    /**
     * 根据clientId获取列表
     *
     * @param clientId
     * @return
     */
    List<ApiFrequency> findByClientId(String clientId);

    ApiFrequency findByCodeAndClientIdIsNullAndFrequencyCustomIsNull(int code);
}
