package com.trs.netInsight.support.appApi.service;

import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.support.appApi.entity.AppApiClient;
import com.trs.netInsight.widget.user.entity.Organization;

import java.util.Date;
import java.util.List;

/**
 * OAuth 服务接口
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月29日
 *
 */
public interface IOAuthService {

	/**
	 * 申请client,用于超管代理申请
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @param orgId
	 * @return
	 * @Return : Client
	 */
	public AppApiClient applyClient(String orgId);

	/**
	 * 申请client,用于超管代理申请
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param org
	 * @return
	 * @Return : Client
	 */
	public AppApiClient applyClient(Organization org);

	/**
	 * 申请client,只允许机构管理员申请
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 * @Return : Client
	 */
	public AppApiClient applyClient(String userName, String password);

	/**
	 * 申请授权code
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param userName
	 * @param password
	 * @return
	 * @Return : String
	 */
	public String applyAccessCode(String userName, String password);

	/**
	 * 申请授权code
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 * @return
	 * @Return : String
	 */
	public String applyAccessCode(String orgId);

	/**
	 * 根据id获取client
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @param clientId
	 * @return
	 * @Return : Client
	 */
	public AppApiClient findById(String clientId);

	/**
	 * 获取client
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @param orgId
	 * @return
	 * @Return : Client
	 */
	public AppApiClient findOne(String orgId);

	/**
	 * 根据secretKey检索Client
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @param secretKey
	 * @return
	 * @Return : Client
	 */
	public AppApiClient findByClientSecretKey(String secretKey);

	/**
	 * 检索所有client
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @return
	 * @Return : List<Client>
	 */
	public List<AppApiClient> listAll();

	/**
	 * 校验secretKey
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @param orgId
	 * @param secretKey
	 * @return
	 * @Return : boolean
	 */
	public boolean checkSecretKey(String secretKey);

	/**
	 * 校验accessToken
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @param accessToken
	 * @return
	 * @Return : boolean
	 */
	public AppApiAccessToken checkAccessToken(String accessToken);

	/**
	 * 获取失效时间
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @param accessToken
	 * @return
	 * @Return : long
	 */
	public long getExpireIn(String accessToken);

	/**
	 * 超管授权机构AccessToken
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 *            机构id
	 * @param expireTime
	 *            过期时间
	 * @param sourceOwner
	 *            资源拥有者
	 * @param level
	 *            api调用频率
	 * @param grantRange
	 *            授权范围
	 * @return
	 * @Return : AccessToken
	 */
	public AppApiAccessToken applyAccessToken(String orgId, Date expireTime, String sourceOwner,String subGoupId, String level,
                                              String grantRange);

	/**
	 * 超管刷新授权机构或客户端token
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 *            机构id
	 * @param clientId
	 *            授权客户端id
	 * @return
	 * @Return : AppApiAccessToken
	 */
	public AppApiAccessToken refresh(String orgId, String clientId);

	/**
	 * 延长授权机构或客户端token
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 *            授权机构id
	 * @param clientId
	 *            授权客户端id
	 * @param expireTime
	 *            有效期
	 * @return
	 * @Return : AppApiAccessToken
	 */
	public AppApiAccessToken extendToken(String orgId, String clientId, Date expireTime);

	/**
	 * 延长已知token有效期
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param token
	 *            accessToken实体对象
	 * @param expireTime
	 *            有效期
	 * @return
	 * @Return : AppApiAccessToken
	 */
	public AppApiAccessToken extendToken(AppApiAccessToken token, Date expireTime);

	/**
	 * 根据授权机构id或授权客户端id获取token对象
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 *            授权机构id
	 * @param clientId
	 *            授权客户端id
	 * @return
	 * @Return : AppApiAccessToken
	 */
	public AppApiAccessToken findByOrgOrClient(String orgId, String clientId);

	/**
	 * 根据token获取token详情
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param accessToken
	 * @return
	 * @Return : AppApiAccessToken
	 */
	public AppApiAccessToken findByAccessToken(String accessToken);

}
