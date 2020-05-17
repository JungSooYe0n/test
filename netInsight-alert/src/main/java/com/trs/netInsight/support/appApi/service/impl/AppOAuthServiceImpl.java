package com.trs.netInsight.support.appApi.service.impl;

import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.support.appApi.entity.AppApiClient;
import com.trs.netInsight.support.appApi.entity.repository.IAppAccessTokenRepository;
import com.trs.netInsight.support.appApi.entity.repository.IAppClientRepository;
import com.trs.netInsight.support.appApi.service.IOAuthService;
import com.trs.netInsight.support.appApi.utils.GrantUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.UserHelp;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * OAuth 服务接口实现
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月29日
 *
 */
@Service
public class AppOAuthServiceImpl implements IOAuthService {

	@Autowired
	private IAppClientRepository clientRepository;

	@Autowired
	private IAppAccessTokenRepository accessTokenRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserHelp userService;

	@Override
	public AppApiClient applyClient(String orgId) {
		Organization organization = organizationRepository.findOne(orgId);
		return this.applyClientByOrg(organization, "Common");
	}

	@Override
	public AppApiClient applyClient(Organization organization) {
		return this.applyClientByOrg(organization, "Common");
	}

	/**
	 * 机构申请client
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param organization
	 * @return
	 * @Return : Client
	 */
	private AppApiClient applyClientByOrg(Organization organization, String level) {
		AppApiClient client = null;
		if (organization != null) {
			client = this.findOne(organization.getId());
			if (client != null) {
				return client;
			} else {
				client = new AppApiClient();
				client.setClientName(organization.getOrganizationName());
				client.setGrantOrgId(organization.getId());
				String secretKey = DigestUtils.md5Hex(organization.getId());
				secretKey = "NICLIENT" + secretKey;
				client.setClientSecretKey(secretKey);
				client.setFrequencyLevel(level);
				client = this.clientRepository.save(client);
			}
		}
		return client;
	}

	@Override
	public AppApiClient applyClient(String userName, String password) {
		AppApiClient client = null;
		if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
			User user = this.userService.findByUserName(userName);
			// 该用户必须存在且为机构管理员
			if (user != null && StringUtils.equals(UserUtils.ROLE_ADMIN, user.getCheckRole())) {

				// 校验密码
				String encryptPsw = UserUtils.getEncryptPsw(password, user.getSalt());
				if (encryptPsw.equals(user.getPassword())) {
					// 申请client
					client = this.applyClient(user.getOrganizationId());
				}
			}
		} else {

		}
		return client;
	}

	@Override
	public AppApiClient findOne(String orgId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppApiClient findByClientSecretKey(String secretKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AppApiClient> listAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkSecretKey(String secretKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AppApiAccessToken checkAccessToken(String accessToken) {
		// TODO Auto-generated method stub
		AppApiAccessToken token = null;
		if (StringUtils.isNotBlank(accessToken)) {
			token = this.accessTokenRepository.findByAccessToken(accessToken);
			if (token != null) {
				AppApiClient client = this.clientRepository.findOne(token.getClientId());
				if (client != null) {
					token.setClient(client);
				}
			}
		}
		return token;
	}

	@Override
	public long getExpireIn(String accessToken) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AppApiClient findById(String clientId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String applyAccessCode(String userName, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String applyAccessCode(String orgId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppApiAccessToken applyAccessToken(String orgId, Date expireTime, String sourceOwner, String subGoupId,String level,
											  String grantRange) {
		AppApiAccessToken token = null;
		if (StringUtils.isNotBlank(orgId)) {
			//非运维生成token
			if(!"platformId".equals(orgId)){
				Organization organization = this.organizationRepository.findOne(orgId);
				// 代理申请CLient
				AppApiClient client = this.applyClientByOrg(organization, level);
				if (client != null) {
					// 直接对该client进行授权
					token = new AppApiAccessToken(client.getId(), sourceOwner,subGoupId, expireTime, grantRange);
					// 计算accessToken
					String accessToken = GrantUtil.computeToken(client.getId(), client.getClientSecretKey());
					token.setAccessToken(accessToken);
					token = accessTokenRepository.save(token);
				}
			}else{
				//运维生成token
				AppApiClient client = null;
				client = new AppApiClient();
				client.setClientName("运维平台");
				client.setGrantOrgId("platformId");
				String secretKey = DigestUtils.md5Hex("platformId");
				secretKey = "NICLIENT" + secretKey;
				client.setClientSecretKey(secretKey);
				client.setFrequencyLevel(level);
				client = this.clientRepository.save(client);
				// 直接对该client进行授权
				token = new AppApiAccessToken(client.getId(), sourceOwner,subGoupId, expireTime, grantRange);
				// 计算accessToken
				String accessToken = GrantUtil.computeToken(client.getId(), client.getClientSecretKey());
				token.setAccessToken(accessToken);
				token = accessTokenRepository.save(token);
			}
		}
		return token;
	}

	@Override
	public AppApiAccessToken refresh(String orgId, String clientId) {
		AppApiAccessToken accessToken = null;
		if (StringUtils.isNotBlank(orgId)) {
			accessToken = this.refreshByOrg(orgId);
		} else if (StringUtils.isNotBlank(clientId)) {
			accessToken = this.refreshByClientId(clientId);
		}
		return accessToken;
	}

	/**
	 * 刷新accessToken,只改变accessToken值,其他属性不变
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 * @return
	 * @Return : AppApiAccessToken
	 */
	private AppApiAccessToken refreshByOrg(String orgId) {
		AppApiAccessToken accessToken = null;
		if (StringUtils.isNotBlank(orgId)) {
			AppApiClient client = this.clientRepository.findByGrantOrgId(orgId);
			if (client != null) {
				accessToken = this.accessTokenRepository.findByClientId(client.getId());
				if (accessToken != null) {
					String token = GrantUtil.computeToken(client.getId(), client.getClientSecretKey());
					accessToken.setAccessToken(token);
					accessToken = this.accessTokenRepository.saveAndFlush(accessToken);
				}
			}
		}
		return accessToken;
	}

	/**
	 * 刷新accessToken,只改变accessToken值,其他属性不变
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param clientId
	 * @return
	 * @Return : AppApiAccessToken
	 */
	private AppApiAccessToken refreshByClientId(String clientId) {
		AppApiAccessToken accessToken = null;
		AppApiClient client = this.clientRepository.findOne(clientId);
		if (client != null) {
			accessToken = this.accessTokenRepository.findByClientId(client.getId());
			if (accessToken != null) {
				String token = GrantUtil.computeToken(client.getId(), client.getClientSecretKey());
				accessToken.setAccessToken(token);
				accessToken = this.accessTokenRepository.saveAndFlush(accessToken);
			}
		}
		return accessToken;
	}

	@Override
	public AppApiAccessToken extendToken(String orgId, String clientId, Date expireTime) {
		AppApiAccessToken token = null;
		if (StringUtils.isNotBlank(orgId)) {
			token = this.extendTokenByOrgId(orgId, expireTime);
		} else if (StringUtils.isNotBlank(clientId)) {
			token = this.extendTokenByClient(clientId, expireTime);
		}

		return token;
	}

	/**
	 * 延长授权机构token有效期
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 *            机构id
	 * @param expireTime
	 *            有效日期
	 * @return
	 * @Return : AppApiAccessToken
	 */
	private AppApiAccessToken extendTokenByOrgId(String orgId, Date expireTime) {
		AppApiAccessToken accessToken = null;
		if (StringUtils.isNotBlank(orgId)) {
			AppApiClient client = this.clientRepository.findByGrantOrgId(orgId);
			if (client != null) {
				accessToken = this.accessTokenRepository.findByClientId(client.getId());
				if (accessToken != null) {
					accessToken.setExpireTime(expireTime);
					accessToken = this.accessTokenRepository.saveAndFlush(accessToken);
				}
			}
		}
		return accessToken;
	}

	/**
	 * 延长授权client token有效期
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param clientId
	 *            授权客户端
	 * @param expireTime
	 *            有效日期
	 * @return
	 * @Return : AppApiAccessToken
	 */
	private AppApiAccessToken extendTokenByClient(String clientId, Date expireTime) {
		AppApiAccessToken accessToken = null;
		AppApiClient client = this.clientRepository.findOne(clientId);
		if (client != null) {
			accessToken = this.accessTokenRepository.findByClientId(client.getId());
			if (accessToken != null) {
				accessToken.setExpireTime(expireTime);
				accessToken = this.accessTokenRepository.saveAndFlush(accessToken);
			}
		}
		return accessToken;
	}

	@Override
	public AppApiAccessToken extendToken(AppApiAccessToken token, Date expireTime) {
		if (token != null) {
			token.setExpireTime(expireTime);
			token = this.accessTokenRepository.saveAndFlush(token);
		}
		return token;
	}

	@Override
	public AppApiAccessToken findByOrgOrClient(String orgId, String clientId) {
		if (StringUtils.isNotBlank(orgId)) {
			AppApiClient client = this.findOne(orgId);
			clientId = client.getId();
		}
		return this.accessTokenRepository.findByClientId(clientId);
	}

	@Override
	public AppApiAccessToken findByAccessToken(String accessToken) {
		if (StringUtils.isNotBlank(accessToken)) {
			return this.accessTokenRepository.findByAccessToken(accessToken);
		}
		return null;
	}

}
