package com.trs.netInsight.support.api.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.support.api.entity.ApiClient;

/**
 * client持久层服务
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月29日
 *
 */
@Repository
public interface IClientRepository extends JpaRepository<ApiClient, String>, JpaSpecificationExecutor<ApiClient> {

	/**
	 * 根据授权机构id获取client
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param grantOrgId
	 * @return
	 * @Return : ApiClient
	 */
	public ApiClient findByGrantOrgId(String grantOrgId);
}
