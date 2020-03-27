package com.trs.netInsight.handler.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * TRS检索异常类
 *
 * Create by yan.changjiang on 2017年11月24日
 */
public class TRSSearchException extends RuntimeException {

	private static final long serialVersionUID = 3918707200183569313L;

	@Setter
	@Getter
	private int code;

	public TRSSearchException(String message) {
		super(message);
	}

	public TRSSearchException(String message, int code) {
		super(message);
		this.code = code;
	}
	
	public TRSSearchException(String message, int code,Throwable e) {
		super(message,e);
		this.code = code;
	}
	
	public TRSSearchException(int code, String message, Throwable e){
		super(message, e);
		this.code = code;
	}
	
	public TRSSearchException(Throwable e){
		super(e);
	}
	
	public TRSSearchException(String message, Throwable e){
		super(message, e);
	}

}
