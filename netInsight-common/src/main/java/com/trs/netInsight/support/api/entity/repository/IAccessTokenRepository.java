package com.trs.netInsight.support.api.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.support.api.entity.ApiAccessToken;

/**
 * access token 持久化服务
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 *
 */
@Repository
public interface IAccessTokenRepository
		extends JpaRepository<ApiAccessToken, String>, JpaSpecificationExecutor<ApiAccessToken> {

	/**
	 * 根据accessToken 获取token实体
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param accessToken
	 * @return
	 * @Return : ApiAccessToken
	 */
	public ApiAccessToken findByAccessToken(String accessToken);

	/**
	 * 根据授权机构id,获取token实体
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param grantOrgId
	 *            授权机构id
	 * @return
	 * @Return : ApiAccessToken
	 */
	public ApiAccessToken findByClientId(String clientId);
	
}
