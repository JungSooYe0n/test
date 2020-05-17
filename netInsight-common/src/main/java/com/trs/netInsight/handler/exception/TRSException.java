package com.trs.netInsight.handler.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * TRS异常基类类
 *
 * Create by yan.changjiang on 2017年11月24日
 */
public class TRSException extends Exception {

	private static final long serialVersionUID = 3918707200183569313L;

	@Setter
	@Getter
	private int code;

	public TRSException(String message) {
		super(message);
	}

	public TRSException(String message, int code) {
		super(message);
		this.code = code;
	}
	
	public TRSException(String message, int code,Throwable e) {
		super(message,e);
		this.code = code;
	}
	
	public TRSException(int code, String message, Throwable e){
		super(message, e);
		this.code = code;
	}
	
	public TRSException(int code, String message){
		super(message);
		this.code = code;
	}
	
	public TRSException(Throwable e){
		super(e);
	}
	
	public TRSException(String message, Throwable e){
		super(message, e);
	}

}
