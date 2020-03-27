package com.trs.netInsight.support.api.result;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * openApi结果返回
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月28日
 *
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ApiCommonResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8004746314753246149L;

	/**
	 * 回文状态码
	 */
	private int code;

	/**
	 * 回文消息
	 */
	private String message;

	/**
	 * 返回结果数据
	 */
	private Object data;

	public ApiCommonResult(int code, String message, Object data) {
		super();
		this.code = code;
		this.message = message;
		this.data = data;
	}

	/**
	 * 根据状态初始化结果集
	 * 
	 * @param stateCode
	 * @param data
	 */
	public ApiCommonResult(ApiResultType stateCode, Object data) {
		super();
		this.code = stateCode.getCode();
		this.message = stateCode.getMessage();
		this.data = data;
	}

	/**
	 * 根据状态初始化结果集
	 * 
	 * @param resultType
	 */
	public ApiCommonResult(ApiResultType resultType) {
		super();
		this.code = resultType.getCode();
		this.message = resultType.getMessage();
	}

	/**
	 * 根据状态初始化结果集
	 * 
	 * @param resultType
	 * @param message
	 */
	public ApiCommonResult(ApiResultType resultType, String message) {
		super();
		this.code = resultType.getCode();
		this.message = message;
	}

	/**
	 * 空结果集
	 * 
	 * @since changjiang @ 2018年6月28日
	 * @return
	 * @Return : ApiCommonResult
	 */
	public static ApiCommonResult empty() {
		return new ApiCommonResult(ApiResultType.Empty, new Object());
	}

	/**
	 * 错误
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @return
	 * @Return : ApiCommonResult
	 */
	public static ApiCommonResult chooseError() {
		return new ApiCommonResult(ApiResultType.ServerError, null);
	}

	/**
	 * 错误
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @param type
	 * @param message
	 * @return
	 * @Return : ApiCommonResult
	 */
	public static ApiCommonResult chooseError(ApiResultType type, String message) {
		ApiCommonResult result = new ApiCommonResult();
		result.setCode(type.getCode());
		result.setMessage(message);
		return result;
	}

	/**
	 * 成功
	 * 
	 * @since changjiang @ 2018年6月29日
	 * @param data
	 * @return
	 * @Return : ApiCommonResult
	 */
	public static ApiCommonResult chooseSuccess(Object data) {
		return new ApiCommonResult(data == null ? ApiResultType.Empty : ApiResultType.Success, data);
	}

	/**
	 * json格式化结果集
	 * 
	 * @since changjiang @ 2018年6月28日
	 * @return
	 * @Return : String
	 */
	public String toJson() {
		return JSONObject.toJSON(this).toString();
	}

	/**
	 * xml格式化结果集,待使用
	 * 
	 * @since changjiang @ 2018年6月28日
	 * @return
	 * @Return : String
	 */
	public String toXml() {
		return null;
	}

}
