package com.trs.netInsight.handler.exception;

import com.trs.netInsight.handler.result.ResultCode;

/**
 * 服务器运算异常
 *
 * Create by yan.changjiang on 2017年11月24日
 */
public class OperationException extends TRSException {

	private static final long serialVersionUID = 8889648842441664681L;

	public OperationException(String message) {
		super(message, ResultCode.OPERATION_EXCEPTION);
	}
	
	public OperationException(String message, Throwable e) {
		super(message, ResultCode.OPERATION_EXCEPTION,e);
	}
}
