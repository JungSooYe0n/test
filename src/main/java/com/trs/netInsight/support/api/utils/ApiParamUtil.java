package com.trs.netInsight.support.api.utils;

import org.apache.commons.lang3.StringUtils;

import com.trs.netInsight.support.api.exception.ApiException;
import com.trs.netInsight.support.api.result.ApiResultType;
import com.trs.netInsight.support.api.utils.constance.GrantType;

/**
 * api参数校验工具类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月4日
 *
 */
public class ApiParamUtil {

	/**
	 * 校验参数
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @param orgId
	 *            机构id
	 * @throws ApiException
	 * @Return : void
	 */
	public static void checkParam(String userName, String password, String orgId) throws ApiException {
		if ((StringUtils.isNotBlank(userName) && StringUtils.isBlank(password))
				|| (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(orgId))
				|| (StringUtils.isBlank(userName) && StringUtils.isBlank(orgId))) {
			throw new ApiException(ApiResultType.ParamError);
		}
	}

	/**
	 * 校验参数
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param grantType
	 *            授权模式
	 * @param userName
	 *            用户名
	 * @param password
	 *            密码
	 * @param clientId
	 *            客户端id
	 * @param secretKey
	 *            客户端秘钥
	 * @Return : void
	 */
	public static void checkParam(GrantType grantType, String userName, String password, String clientId,
			String secretKey) {
		switch (grantType) {
		case Password:
		case Implicit:
			if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
				throw new ApiException(ApiResultType.ParamError);
			}
			break;
		case Code:
		case Client:
			if (StringUtils.isBlank(clientId) || StringUtils.isBlank(secretKey)) {
				throw new ApiException(ApiResultType.ParamError);
			}
			break;
		default:
			break;
		}
	}

}
