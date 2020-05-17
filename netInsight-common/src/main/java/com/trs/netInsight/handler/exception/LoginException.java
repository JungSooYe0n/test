package com.trs.netInsight.handler.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * TRS检索异常类
 *
 * Create by yan.changjiang on 2017年11月24日
 */
public class LoginException extends TRSException {

	private static final long serialVersionUID = 3918707200183569313L;

	@Setter
	@Getter
	private int code;

	public LoginException(String message) {
		super(message);
	}

	public LoginException(String message, int code) {
		super(message);
		this.code = code;
	}

	public LoginException(String message, int code, Throwable e) {
		super(message, e);
		this.code = code;
	}

	public LoginException(int code, String message, Throwable e) {
		super(message, e);
		this.code = code;
	}

	public LoginException(int code, String message) {
		super(message);
		this.code = code;
	}

	public LoginException(Throwable e) {
		super(e);
	}

	public LoginException(String message, Throwable e) {
		super(message, e);
	}

}
