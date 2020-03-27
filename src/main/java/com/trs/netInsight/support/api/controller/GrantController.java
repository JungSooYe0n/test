package com.trs.netInsight.support.api.controller;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.support.api.entity.ApiAccessToken;
import com.trs.netInsight.support.api.result.ApiCommonResult;
import com.trs.netInsight.support.api.result.ApiResultType;
import com.trs.netInsight.support.api.service.IOAuthService;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.UserUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * api授权相关控制器
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月29日
 *
 */
@RestController
@RequestMapping({ "/api/platform", "/admin/api/platform" })
@Api(description = "api授权相关")
public class GrantController {

	@Autowired
	private IOAuthService authService;

	@GetMapping("/index")
	@ResponseBody
	public Object index() {
		return "Hello API!";
	}

	@PostMapping("/applyClient")
	public ApiCommonResult applyClient(
			@ApiParam("用户名") @RequestParam(value = "userName", required = false) String userName,
			@ApiParam("密码") @RequestParam(value = "password", required = false) String password,
			@ApiParam("机构id") @RequestParam(value = "orgId", required = false) String orgId) {
		ApiCommonResult result = new ApiCommonResult();

		return result;
	}

	/**
	 * 超管直接对机构进行授权,并将授权信息发送至机构管理员邮箱
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 *            机构id
	 * @param expireTime
	 *            过期时间
	 * @param sourceOwnerId
	 *            资源拥有者id,默认使用机构管理员作为资源拥有者
	 * @param grantRange
	 *            授权范围,目前默认Column
	 * @return
	 * @Return : ApiCommonResult
	 */
	@PostMapping("/grant")
	public ApiCommonResult grant(@ApiParam("机构id") @RequestParam(value = "orgId") String orgId,
			@ApiParam(value = "列表页每页最多条数，默认1000") @RequestParam(value = "maxPageSize", defaultValue = "1000",required = false) int maxPageSize,
			@ApiParam(value = "过期时间yyyy-MM-dd HH:mm:ss", example = "yyyy-MM-dd HH:mm:ss") @RequestParam(value = "expireDate", required = false) String expireDate,
			@ApiParam(value = "持续时间,单位天 7", example = "7") @RequestParam(value = "continueTime", required = false, defaultValue = "30") int continueTime,
			@ApiParam("资源拥有者id,可选,默认使用机构管理员id") @RequestParam(value = "sourceOwnerId", defaultValue = "orgAdmin") String sourceOwnerId,
			@ApiParam("api调用频率级别，分为High，Common，Low，") @RequestParam(value = "level", defaultValue = "Common") String level,
			@ApiParam("授权范围,多值使用;隔开 具体值需结合GrantRange枚举类的code值填写,0000表示全部") @RequestParam(value = "grantRange", defaultValue = "Column") String grantRange) {
		ApiCommonResult result = null;
		boolean superAdmin = UserUtils.isSuperAdmin();
		if (!superAdmin) {
			result = new ApiCommonResult(ApiResultType.Forbidden, null);
		} else {
			// 格式化时间
			try {
				Date expireTime = null;
				if (StringUtils.isNotBlank(expireDate)) {
					expireTime = DateUtil.stringToDate(expireDate, DateUtil.yyyyMMdd);
				} else {
					String dateAfter = DateUtil.formatDateAfter(new Date(), DateUtil.yyyyMMdd, continueTime);
					expireTime = DateUtil.stringToDate(dateAfter, DateUtil.yyyyMMdd);
				}
				ApiAccessToken accessToken = this.authService.applyAccessToken(orgId,maxPageSize, expireTime, sourceOwnerId,level,
						grantRange);
				result = new ApiCommonResult(ApiResultType.Success, accessToken);
			} catch (Exception e) {
				result = new ApiCommonResult(ApiResultType.ServerError, null);
			}
		}
		return result;
	}

	/**
	 * 超管刷新授权机构token
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 * @return
	 * @Return : ApiCommonResult
	 */
	@GetMapping("/refreshToken")
	public ApiCommonResult refreshToken(
			@ApiParam("授权机构id") @RequestParam(value = "orgId", required = false) String orgId,
			@ApiParam("授权api客户端id") @RequestParam(value = "clientId", required = false) String clientId) {
		ApiCommonResult result = null;
		if (StringUtils.isBlank(orgId) && StringUtils.isBlank(clientId)) {
			result = new ApiCommonResult(ApiResultType.ParamError, null);
		}
		if (!UserUtils.isSuperAdmin()) {
			result = new ApiCommonResult(ApiResultType.Forbidden, null);
		}
		try {
			ApiAccessToken accessToken = this.authService.refresh(orgId, clientId);
			if (accessToken != null) {
				result = new ApiCommonResult(ApiResultType.Success, accessToken);
			} else {
				result = new ApiCommonResult(ApiResultType.GrantError, accessToken);
			}
		} catch (Exception e) {
			result = new ApiCommonResult(ApiResultType.ServerError, null);
		}
		return result;
	}

	/**
	 * 延长accessToken有效期
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param orgId
	 *            授权机构id
	 * @param clientId
	 *            授权客户端id
	 * @param expireDate
	 *            有效期
	 * @param continueTime
	 *            延长天数
	 * @return
	 * @Return : ApiCommonResult
	 */
	@GetMapping("/extendToken")
	public ApiCommonResult extendToken(
			@ApiParam("授权机构id") @RequestParam(value = "orgId", required = false) String orgId,
			@ApiParam("授权api客户端id") @RequestParam(value = "clientId", required = false) String clientId,
			@ApiParam(value = "列表页每页最多条数，默认1000") @RequestParam(value = "maxPageSize", defaultValue = "1000",required = false) int maxPageSize,
			@ApiParam(value = "有效期,yyyy-MM-dd HH:mm:ss", example = "yyyy-MM-dd HH:mm:ss") @RequestParam(value = "expireDate", required = false) String expireDate,
			@ApiParam(value = "持续时间,单位天", example = "7") @RequestParam(value = "continueTime", required = false, defaultValue = "30") int continueTime) {
		ApiCommonResult result = null;
		if (StringUtils.isBlank(orgId) && StringUtils.isBlank(clientId)) {
			result = new ApiCommonResult(ApiResultType.ParamError, null);
		}
		if (!UserUtils.isSuperAdmin()) {
			result = new ApiCommonResult(ApiResultType.Forbidden, null);
		}
		// 格式化时间
		ApiAccessToken token = null;
		Date expireTime = null;
		try {
			if (StringUtils.isNotBlank(expireDate)) {
				expireTime = DateUtil.stringToDate(expireDate, DateUtil.yyyyMMdd);
				token = this.authService.extendToken(orgId, clientId, expireTime);
			} else {
				token = this.authService.findByOrgOrClient(orgId, clientId);
				Date oldDate = token.getExpireTime();
				String dateAfter = DateUtil.formatDateAfter(oldDate, DateUtil.yyyyMMdd, continueTime);
				expireTime = DateUtil.stringToDate(dateAfter, DateUtil.yyyyMMdd);
				token = this.authService.extendToken(token, expireTime);
			}
			if (token != null) {
				result = new ApiCommonResult(ApiResultType.Success, token);
			} else {
				result = new ApiCommonResult(ApiResultType.GrantError);
			}
		} catch (Exception e) {
			result = new ApiCommonResult(ApiResultType.ServerError);
		}
		return result;
	}

}
