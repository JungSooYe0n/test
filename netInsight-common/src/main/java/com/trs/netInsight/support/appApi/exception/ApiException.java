package com.trs.netInsight.support.appApi.exception;

import com.trs.netInsight.support.appApi.result.ApiResultType;
import lombok.Getter;
import lombok.Setter;

/**
 * openApi异常类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月28日
 *
 */
@Getter
@Setter
public class ApiException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2247408748067869830L;

	/**
	 * 状态码{@link ApiResultType #getCode()}
	 */
	
	private int code;
	
	private ApiResultType type;

	public ApiException(int code) {
		super();
		this.code = code;
	}

	public ApiException(int code, String message, Throwable e) {
		super(message, e);
		this.code = code;
	}

	public ApiException(String message, Throwable e) {
		super(message, e);
	}

	public ApiException(int code, Throwable e) {
		super(e);
		this.code = code;
	}

	public ApiException(Throwable e) {
		super(e);
	}

	public ApiException(ApiResultType resultType) {
		super(new Throwable(resultType.getMessage()));
		this.code = resultType.getCode();
		this.type = resultType;
	}
	
	public ApiException(ApiResultType resultType, Throwable e) {
		super(e);
		this.code = resultType.getCode();
		this.type = resultType;
	}

}
