package com.trs.netInsight.support.appApi.result;

import lombok.Getter;

/**
 * openApi返回状态码,状态码规则参考Http协议
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月28日
 *
 */
@Getter
public enum ApiResultType {

	/**
	 * 请求成功
	 */
	Success(200, "Request Success!"),

	/**
	 * 参数错误
	 */
	ParamError(400, "Method Not Allowed!"),
	/**
	 * 未经授权
	 */
	Unauthorized(401, "Have Not Been Authorized!"),
	/**
	 * 认证失败
	 */
	GrantError(402, "Grant Error!"),
	/**
	 * 权限不足
	 */
	Forbidden(403, "Lack Of Authority, Forbidden!"),
	/**
	 * 未找到该方法
	 */
	NotFind(404, "Method Is Not Find!"),

	/**
	 * 方法请求方式不被允许,一般为请求类型错误
	 */
	NotAllowed(405, "Method Is Not Allowed!"),
	
	/**
	 * 未找到指定资源
	 */
	NotFindSource(406,"Source Not Find!"),
	/**
	 * 请求超时
	 */
	TimeOut(408, "Request TimeOut!"),
	/**
	 * 请求指定接口超限
	 */
	TooMany(421, "Too Many Request!"),
	/**
	 * 请求接口被锁定
	 */
	Locked(423, "This Source Is Locked!"),

	/**
	 * ip受限
	 */
	IpLimited(424, "IP Limited!"),

	/**
	 * 服务器异常
	 */
	ServerError(500, "Server Error,Please Contact To Administrators!"),

	/**
	 * token已过期
	 */
	Invalidate(205, "请重新登录！"),
	/**
	 * 请求成功,但计算数据为空
	 */
	Empty(204, "Request Success, But Task Compute Data Is Empty!");

	/**
	 * 回文状态码
	 */
	private int code;

	/**
	 * 回文消息
	 */
	private String message;

	private ApiResultType(int code, String message) {
		this.code = code;
		this.message = message;
	}

}
