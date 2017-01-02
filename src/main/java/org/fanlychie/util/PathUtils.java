package org.fanlychie.util;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

/**
 * 路径工具类
 *
 * @author fanlychie
 */
public final class PathUtils {

    /**
     * 获取类路径(classpath)的路径
     *
     * @return 返回项目类路径的绝对路径
     */
    public static String getClassPath() {
        return Thread.currentThread().getContextClassLoader().getResource("").getPath();
    }

    /**
     * 获取类路径(classpath)下的文件
     *
     * @param pathname 文件相对于类路径下的路径名, 如: "com/path/file.pdf"
     * @return 返回 classpath 目录下由 pathname 指定的路径的文件对象
     */
    public static File getClassPathFile(String pathname) {
        return new File(getClassPath() + pathname);
    }

    /**
     * 获取类路径(classpath)下的文件输入流
     *
     * @param pathname 文件相对于类路径下的路径名, 如: "com/path/file.pdf"
     * @return 返回 classpath 目录下由 pathname 指定的路径的文件输入流对象
     */
    public static InputStream getClassPathStream(String pathname) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathname);
    }

    /**
     * 获取类路径(classpath)下的文件读取流, 通过 readLine 可以每次读取一行
     *
     * @param pathname 文件相对于类路径下的路径名, 如: "com/path/file.pdf"
     * @return 返回 classpath 目录下由 pathname 指定的路径的文件读取流对象
     */
    public static BufferedReader getClassPathReader(String pathname) {
        return new BufferedReader(new InputStreamReader(getClassPathStream(pathname)));
    }

    /**
     * 获取项目的路径
     *
     * @param request HttpServletRequest
     * @return 返回项目的绝对路径
     */
    public static String getProjectPath(HttpServletRequest request) {
        return request.getSession().getServletContext().getRealPath("/");
    }

    /**
     * 获取项目 WEB-INF 的路径
     *
     * @param request HttpServletRequest
     * @return 返回项目 WEB-INF 的绝对路径
     */
    public static String getWebInfPath(HttpServletRequest request) {
        return getProjectPath(request) + "WEB-INF/";
    }

}