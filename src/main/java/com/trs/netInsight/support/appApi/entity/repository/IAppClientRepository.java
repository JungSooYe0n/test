package com.trs.netInsight.support.appApi.entity.repository;

import com.trs.netInsight.support.appApi.entity.AppApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * client持久层服务
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月29日
 *
 */
@Repository
public interface IAppClientRepository extends JpaRepository<AppApiClient, String>, JpaSpecificationExecutor<AppApiClient> {

	/**
	 * 根据授权机构id获取client
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param grantOrgId
	 * @return
	 * @Return : AppApiClient
	 */
	public AppApiClient findByGrantOrgId(String grantOrgId);
}
