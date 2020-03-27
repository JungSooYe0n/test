package com.trs.netInsight.support.appApi.entity.repository;

import com.trs.netInsight.support.appApi.entity.AppApiFrequency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * api 调用频率 持久化服务
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 */
@Repository
public interface IAppApiFrequencyRepository extends JpaRepository<AppApiFrequency, String>,
        JpaSpecificationExecutor<AppApiFrequency>, PagingAndSortingRepository<AppApiFrequency, String> {

    /**
     * 根据code获取
     *
     * @param code
     * @return
     * @Return : List<AppApiFrequency>
     * @since changjiang @ 2018年7月3日
     */
    public List<AppApiFrequency> findByCode(int code);

    /**
     * 根据clientId及code值获取
     *
     * @param code
     * @param clientId
     * @return
     */
    public AppApiFrequency findByCodeAndClientId(int code, String clientId);

    /**
     * 根据clientId获取列表
     *
     * @param clientId
     * @return
     */
    List<AppApiFrequency> findByClientId(String clientId);
}
