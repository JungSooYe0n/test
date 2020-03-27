package com.trs.netInsight.handler;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.util.trace.LogUtil;


/**
 * 统一请求类
 *
 * Created by yan.changjiang on 2017年11月16日
 */
@ControllerAdvice
@Slf4j
public class GlobalRequestHandler {

	/**
	 * 应用到所有@RequestMapping注解方法，在其执行之前把返回值放入Model
	 * @param request
	 * @param response
	 * @throws Exception
	 */
    @ModelAttribute
    public void requestHandler(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /*
         * 将 HttpServletRequest 中用户信息设置的 ThreadLocal, 方便调用服务层获取信息
         */
        LogUtil.setAttribute(request);

        request.setAttribute(Const.DATA_SOURCE, Const.DATA_SOURCE_HB8);
        request.setAttribute(Const.USER_ID, Const.DEFAULT_UID);
        request.setAttribute(Const.TENANT_ID, Const.DEFAULT_TID);

    }

}
