package org.fanlychie.util;

import javax.servlet.http.HttpServletRequest;

/**
 * IP 工具类
 *
 * @author fanlychie
 */
public final class IPUtils {

    /**
     * 获取IP地址
     *
     * @param request HttpServletRequest
     * @return 返回获取到的IP地址字符串
     */
    public static String getIPAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (isFound(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (isFound(ip)) {
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (isFound(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isFound(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * 判断IP是否已经被找到
     *
     * @param ip IP地址
     * @return
     */
    private static boolean isFound(String ip) {
        return ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip);
    }

}