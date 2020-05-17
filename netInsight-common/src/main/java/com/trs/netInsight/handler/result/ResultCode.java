package com.trs.netInsight.handler.result;

/**
 * 响应码枚举类
 *
 * Created by yan.changjiang on 2016/12/30.
 */
public interface ResultCode {

    int SUCCESS = 200;

    /**
     * 认证信息异常
     */
    int AUTHORITY_EXCEPTION = 401;

    int REQUEST_PATH_EXCEPTION = 404;

    int NULL_EXCEPTION = 204;

    /**
     * 服务器运算出错
     */
    int OPERATION_EXCEPTION = 500;

}
