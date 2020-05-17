package com.trs.netInsight.support.appApi.entity.repository;

import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * access token 持久化服务
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 *
 */
@Repository
public interface IAppAccessTokenRepository
		extends JpaRepository<AppApiAccessToken, String>, JpaSpecificationExecutor<AppApiAccessToken> {

	/**
	 * 根据accessToken 获取token实体
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param accessToken
	 * @return
	 * @Return : AppApiAccessToken
	 */
	public AppApiAccessToken findByAccessToken(String accessToken);

	/**
	 * 查询全部
	 * @return
	 */
	public List<AppApiAccessToken> findAll();

	/**
	 * 根据授权机构id,获取token实体
	 *
	 * @since changjiang @ 2018年7月2日
	 * @param clientId
	 *            授权机构id
	 * @return
	 * @Return : AppApiAccessToken
	 */
	public AppApiAccessToken findByClientId(String clientId);

	/**
	 * 根据token所属人id获取token
	 * @param OwnerId
	 */
	public AppApiAccessToken findAppApiAccessTokensByGrantSourceOwnerId(String OwnerId);

}
