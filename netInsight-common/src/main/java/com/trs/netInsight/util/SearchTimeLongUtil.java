package com.trs.netInsight.util;

import com.trs.netInsight.handler.exception.OperationException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 马加鹏
 * @date 2021/4/22 11:24
 */
public class SearchTimeLongUtil {


    //获取栏目名称或者专题名称、检索时间范围
    public static void execute(String modelName, String timeRange) {
        //获取HttpServletRequest请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        request.setAttribute("model_name_123", modelName);
        request.setAttribute("timeRange_123", timeRange);
    }

}
