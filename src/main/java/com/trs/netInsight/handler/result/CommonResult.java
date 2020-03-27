package com.trs.netInsight.handler.result;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.util.ObjectUtil;

import lombok.Getter;
import lombok.Setter;

/**
 * CommonResult 返回结果处理
 *
 * Create by yan.changjiang on 2017年11月24日
 */
@Setter
@Getter
public class CommonResult {

	/**
	 * 检索状态
	 */
	private Boolean status = true;

	/**
	 * 检索正确状态码
	 */
	private int code = ResultCode.SUCCESS;

	/**
	 * 状态描述
	 */
	private String message;

	/**
	 * 返回数据
	 */
	private Object data;
	
	private CommonResult(Object data) {
//		if(data instanceof  Page){
//			Page<?> p= (Page<?>) data;
//			if(ObjectUtil.isEmpty(p.getContent())){
////				commonResult = null;
////				return new CommonResult(204,"没有符合条件的数据！",null);
//				this.data = null;
//				this.code = 204;
//				this.message = "没有符合条件的数据！";
//			}
//		}else{
//			this.data = data;
//		}
		this.data = data;
	}
	

	/**
	 * 空方法构造
	 */
	public CommonResult() {
	}

	/**
	 * 构建返回值方法
	 *
	 * @param data
	 *            数据
	 * @return CommonResult
	 * @throws TRSException
	 *             自定义异常
	 */
	public static CommonResult build(Object data) throws TRSException {
		if (ObjectUtil.isEmpty(data)) {
			return new CommonResult(204,"没有符合条件的数据！",data);
		}
		// 正常返回数据
		return new CommonResult(data);
	}

	public CommonResult(int code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

}
