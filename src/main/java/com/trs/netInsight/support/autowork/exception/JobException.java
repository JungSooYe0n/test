package com.trs.netInsight.support.autowork.exception;

import org.quartz.JobExecutionException;

import lombok.Getter;
import lombok.Setter;

/**
 * 定时任务异常实体
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年6月25日
 *
 */
public class JobException extends JobExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7679377251895367290L;

	@Getter
	@Setter
	private int code;

	public JobException() {
		super();
	}

	public JobException(String message, Throwable e) {
		super(message, e);
		this.code = 500;
	}

	public JobException(Throwable e) {
		super(e);
	}

	public JobException(String message) {
		super(message);
	}

	public JobException(int code, String message, Throwable e) {
		super(message, e);
		this.code = code;
	}

	@Override
	public String toString() {
		return "JobException [code=" + code + ", message=" + super.getMessage() + " ]";
	}

}
