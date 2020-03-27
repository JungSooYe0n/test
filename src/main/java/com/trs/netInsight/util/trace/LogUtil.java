package com.trs.netInsight.util.trace;

import javax.servlet.http.HttpServletRequest;

/**
 * 日志工具类
 */
public class LogUtil {

    private static ThreadLocal<String> hostIp = new InheritableThreadLocal<>();

    private static ThreadLocal<String> client = new InheritableThreadLocal<>();
    
    private static ThreadLocal<String> userId = new InheritableThreadLocal<>();

    public static void cleanAttribute() {
        hostIp.remove();
        client.remove();
    }

    public static String getClient() {
        return client.get();
    }

    public static String getHostIp() {
        return hostIp.get();
    }
    
    public static String getUserId() {
        return userId.get();
    }

    /**
     * 设置当前会话属性
     */
    public static void setAttribute(HttpServletRequest request) {
        hostIp.set(request.getHeader("X-Real-IP"));
        client.set(request.getHeader("User-agent"));
//        HttpSession session = request.getSession();
//        SecurityContextImpl security = (SecurityContextImpl) session
//                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
//        Map principal = (Map) security.getAuthentication().getPrincipal();
//        userId.set(principal.get("id").toString());
    }
   
}
