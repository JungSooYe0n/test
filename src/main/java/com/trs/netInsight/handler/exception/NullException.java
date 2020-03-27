package com.trs.netInsight.handler.exception;

import com.trs.netInsight.handler.result.ResultCode;

/**
 * 空异常
 *
 * Created by yan.changjiang on 2017/2/21.
 */
public class NullException  extends TRSException {

    private static final long serialVersionUID = 2461953161222277429L;

    public NullException(String message) {
        super(message, ResultCode.NULL_EXCEPTION);
    }
}
