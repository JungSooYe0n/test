package com.trs.netInsight.handler;


import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.trs.netInsight.handler.exception.NullException;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.handler.result.CommonResult;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一异常捕获处理类
 *
 * Create by yan.changjiang on 2017年11月24日
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 异常处理机制
	 *
	 * @param e
	 *            TRSException
	 * @return CommonResult
	 */
	@ExceptionHandler(value = TRSException.class)
	@ResponseBody
	public CommonResult jsonErrorHandler(TRSException e) {
		CommonResult response = new CommonResult();
		response.setMessage(e.getMessage());
		response.setCode(e.getCode());
		response.setData(null);
		response.setStatus(false);
		log.error(String.format("GlobalException Code：%s ,message:", e.getCode()), e);
		return response;
	}
	
	@ExceptionHandler(value = OperationException.class)
	@ResponseBody
	public CommonResult jsonErrorHandler(OperationException e) {
		CommonResult response = new CommonResult();
		response.setMessage(e.getMessage());
		response.setCode(e.getCode());
		response.setData(null);
		response.setStatus(false);
		log.error(String.format("GlobalException Code：%s ,message:", e.getCode()), e);
		return response;
	}
	
	@ExceptionHandler(value = TRSSearchException.class)
	@ResponseBody
	public CommonResult jsonErrorHandler(TRSSearchException e) {
		CommonResult response = new CommonResult();
		response.setMessage(e.getMessage());
//		response.setCode(e.getCode());
		response.setData(null);
		response.setStatus(false);
		log.error(String.format("GlobalException Code：%s ,message:", 500), e);
		return response;
	}
	
	@ExceptionHandler(value = NullException.class)
	@ResponseBody
	public CommonResult jsonErrorHandler(NullException e) {
		CommonResult response = new CommonResult();
		response.setMessage(e.getMessage());
		response.setCode(e.getCode());
		response.setData(null);
		response.setStatus(false);
		log.error(String.format("GlobalException Code：%s ,message:", e.getCode()), e);
		return response;
	}
	
	

}
