package com.trs.netInsight.handler.exception;

import com.trs.netInsight.handler.result.ResultCode;

/**
 * 权限异常 code
 *
 * Create by yan.changjiang on 2017年11月24日
 */
public class AuthorityException extends TRSException  {

    private static final long serialVersionUID = 1859359674094008702L;

    public AuthorityException(String message) {
        super(message, ResultCode.AUTHORITY_EXCEPTION);
    }
}
