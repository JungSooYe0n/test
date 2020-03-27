package com.trs.netInsight.support.api.utils;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.auth0.jwt.internal.org.apache.commons.codec.digest.DigestUtils;

/**
 * 授权相关工具类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 *
 */
public class GrantUtil {

	/**
	 * 根据clientId以及secretKey生成accessToken,不可逆
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param clientId
	 * @param secretKey
	 * @return
	 * @Return : String
	 */
	public static String computeToken(String clientId, String secretKey) {
		if (StringUtils.isBlank(clientId) || StringUtils.isBlank(secretKey)) {
			return null;
		}
		return DigestUtils.md5Hex(clientId + secretKey + UUID.randomUUID().toString());
	}

	/**
	 * 根据accessCode生成accessToken
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param accessCode
	 * @return
	 * @Return : String
	 */
	public static String computeToken(String accessCode) {
		if (StringUtils.isBlank(accessCode)) {
			return null;
		}
		return DigestUtils.md5Hex(accessCode);
	}
}
