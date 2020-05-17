package com.trs.netInsight.support.api.service.impl;

import java.util.Date;
import java.util.List;

import com.trs.netInsight.util.ClientUtil;
import com.trs.netInsight.widget.UserHelp;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.support.api.entity.ApiAccessToken;
import com.trs.netInsight.support.api.entity.ApiClient;
import com.trs.netInsight.support.api.entity.repository.IAccessTokenRepository;
import com.trs.netInsight.support.api.entity.repository.IClientRepository;
import com.trs.netInsight.support.api.service.IOAuthService;
import com.trs.netInsight.support.api.utils.GrantUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;

/**
 * OAuth 服务接口实现
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月29日
 *
 */
@Service
public class OAuthServiceImpl implements IOAuthService {

	@Autowired
	private IClientRepository clientRepository;

	@Autowired
	private IAccessTokenRepository accessTokenRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserHelp userService;

	@Override
	public ApiClient applyClient(String orgId) {
		Organization organization = organizationRepository.findOne(orgId);
		return ClientUtil.applyClientByOrg(organization, "Common",clientRepository);
	}

	@Override
	public ApiClient applyClient(Organization organization) {
		return ClientUtil.applyClientByOrg(organization, "Common",clientRepository);
	}


	@Override
	public ApiClient applyClient(String userName, String password) {
		ApiClient client = null;
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
	public ApiClient findOne(String orgId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApiClient findByClientSecretKey(String secretKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ApiClient> listAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkSecretKey(String secretKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ApiAccessToken checkAccessToken(String accessToken) {
		// TODO Auto-generated method stub
		ApiAccessToken token = null;
		if (StringUtils.isNotBlank(accessToken)) {
			token = this.accessTokenRepository.findByAccessToken(accessToken);
			if (token != null) {
				ApiClient client = this.clientRepository.findOne(token.getClientId());
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
	public ApiClient findById(String clientId) {
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
	public ApiAccessToken applyAccessToken(String orgId,int maxPageSize, Date expireTime, String sourceOwner, String level,
			String grantRange) {
		ApiAccessToken token = null;
		if (StringUtils.isNotBlank(orgId)) {
			Organization organization = this.organizationRepository.findOne(orgId);

			// 代理申请CLient
			ApiClient client = ClientUtil.applyClientByOrg(organization, "Common",clientRepository);
			if (client != null) {

				// 直接对该client进行授权
				token = new ApiAccessToken(client.getId(), sourceOwner, expireTime, grantRange,maxPageSize);
				// 计算accessToken
				String accessToken = GrantUtil.computeToken(client.getId(), client.getClientSecretKey());
				token.setAccessToken(accessToken);
				token = accessTokenRepository.save(token);
			}
		}
		return token;
	}

	@Override
	public ApiAccessToken refresh(String orgId, String clientId) {
		ApiAccessToken accessToken = null;
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
	 * @Return : ApiAccessToken
	 */
	private ApiAccessToken refreshByOrg(String orgId) {
		ApiAccessToken accessToken = null;
		if (StringUtils.isNotBlank(orgId)) {
			ApiClient client = this.clientRepository.findByGrantOrgId(orgId);
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
	 * @Return : ApiAccessToken
	 */
	private ApiAccessToken refreshByClientId(String clientId) {
		ApiAccessToken accessToken = null;
		ApiClient client = this.clientRepository.findOne(clientId);
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
	public ApiAccessToken extendToken(String orgId, String clientId, Date expireTime) {
		ApiAccessToken token = null;
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
	 * @Return : ApiAccessToken
	 */
	private ApiAccessToken extendTokenByOrgId(String orgId, Date expireTime) {
		ApiAccessToken accessToken = null;
		if (StringUtils.isNotBlank(orgId)) {
			ApiClient client = this.clientRepository.findByGrantOrgId(orgId);
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
	 * @Return : ApiAccessToken
	 */
	private ApiAccessToken extendTokenByClient(String clientId, Date expireTime) {
		ApiAccessToken accessToken = null;
		ApiClient client = this.clientRepository.findOne(clientId);
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
	public ApiAccessToken extendToken(ApiAccessToken token, Date expireTime) {
		if (token != null) {
			token.setExpireTime(expireTime);
			token = this.accessTokenRepository.saveAndFlush(token);
		}
		return token;
	}

	@Override
	public ApiAccessToken findByOrgOrClient(String orgId, String clientId) {
		if (StringUtils.isNotBlank(orgId)) {
			ApiClient client = this.findOne(orgId);
			clientId = client.getId();
		}
		return this.accessTokenRepository.findByClientId(clientId);
	}

	@Override
	public ApiAccessToken findByAccessToken(String accessToken) {
		if (StringUtils.isNotBlank(accessToken)) {
			return this.accessTokenRepository.findByAccessToken(accessToken);
		}
		return null;
	}

}
