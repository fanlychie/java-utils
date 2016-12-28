package org.fanlychie.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;

/**
 * ZIP压缩文件工具类
 *
 * @author fanlychie
 */
public final class ZipUtils {

    /**
     * 压缩文件或目录
     *
     * @param srcPathname  源文件或目录的路径
     * @param destPathname 压缩文件存储的目录的路径
     * @return 返回压缩文件对象
     */
    public static File zip(String srcPathname, String destPathname) {
        return zip(srcPathname, destPathname, null, false);
    }

    /**
     * 压缩文件或目录
     *
     * @param srcPathname  源文件或目录的路径
     * @param destPathname 压缩文件存储的目录的路径
     * @param delSrc       压缩完成后是否删除源文件目录
     * @return 返回压缩文件对象
     */
    public static File zip(String srcPathname, String destPathname, boolean delSrc) {
        return zip(srcPathname, destPathname, null, delSrc);
    }

    /**
     * 压缩文件或目录
     *
     * @param srcPathname  源文件或目录的路径
     * @param destPathname 压缩文件存储的目录的路径
     * @param password     使用密码压缩
     * @return 返回压缩文件对象
     */
    public static File zip(String srcPathname, String destPathname, String password) {
        return zip(srcPathname, destPathname, password, false);
    }

    /**
     * 压缩文件或目录, 压缩后的文件存储于源文件同级目录或源目录的上级目录
     *
     * @param srcPathname  源文件或目录的路径
     * @return 返回压缩文件对象
     */
    public static File zip(String srcPathname) {
        return zip(srcPathname, new File(srcPathname).getParent(), null, false);
    }

    /**
     * 压缩文件或目录, 压缩后的文件存储于源文件同级目录或源目录的上级目录
     *
     * @param srcPathname  源文件或目录的路径
     * @param delSrc       压缩完成后是否删除源文件目录
     * @return 返回压缩文件对象
     */
    public static File zip(String srcPathname, boolean delSrc) {
        return zip(srcPathname, new File(srcPathname).getParent(), null, delSrc);
    }

    /**
     * 解压缩文件
     *
     * @param srcPathname  源文件(压缩文件)
     * @param destPathname 解压缩后存储的目录
     * @return 返回解压缩的文件目录对象
     */
    public static File unzip(String srcPathname, String destPathname) {
        return unzip(srcPathname, destPathname, null, false);
    }

    /**
     * 解压缩文件
     *
     * @param srcPathname  源文件(压缩文件)
     * @param destPathname 解压缩后存储的目录
     * @param password     解压缩使用的密码
     * @return 返回解压缩的文件目录对象
     */
    public static File unzip(String srcPathname, String destPathname, String password) {
        return unzip(srcPathname, destPathname, password, false);
    }

    /**
     * 解压缩文件
     *
     * @param srcPathname  源文件(压缩文件)
     * @param destPathname 解压缩后存储的目录
     * @param delSrc       解压缩完成后是否删除源文件(压缩文件)
     * @return 返回解压缩的文件目录对象
     */
    public static File unzip(String srcPathname, String destPathname, boolean delSrc) {
        return unzip(srcPathname, destPathname, null, delSrc);
    }

    /**
     * 解压缩文件, 解压缩的目录存储于源文件的同级目录
     *
     * @param srcPathname  源文件(压缩文件)
     * @return 返回解压缩的文件目录对象
     */
    public static File unzip(String srcPathname) {
        return unzip(srcPathname, new File(srcPathname).getParent(), null, false);
    }

    /**
     * 解压缩文件, 解压缩的目录存储于源文件的同级目录
     *
     * @param srcPathname  源文件(压缩文件)
     * @param delSrc       解压缩完成后是否删除源文件(压缩文件)
     * @return 返回解压缩的文件目录对象
     */
    public static File unzip(String srcPathname, boolean delSrc) {
        return unzip(srcPathname, new File(srcPathname).getParent(), null, false);
    }



    /**
     * 压缩文件或目录
     *
     * @param srcPathname  源文件或目录的路径
     * @param destPathname 压缩文件存储的目录的路径
     * @param password     使用密码压缩
     * @param delSrc       压缩完成后是否删除源文件目录
     * @return 返回压缩文件对象
     */
    public static File zip(String srcPathname, String destPathname, String password, boolean delSrc) {
        File src = new File(srcPathname);
        if (!src.exists()) {
            throw new RuntimeException(src + " 不是一个有效的文件或目录");
        }
        File dest = new File(destPathname);
        if (!dest.isDirectory()) {
            throw new RuntimeException(dest + " 不是一个有效的目录");
        }
        ZipParameters params = new ZipParameters();
        params.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        params.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        params.setIncludeRootFolder(false);
        if (password != null && password.length() > 0) {
            params.setEncryptFiles(true);
            params.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            params.setPassword(password);
        }
        File zip = new File(dest, src.getName() + ".zip");
        try {
            ZipFile zipFile = new ZipFile(zip);
            if (src.isDirectory()) {
                zipFile.addFolder(src, params);
            } else {
                zipFile.addFile(src, params);
            }
            if (delSrc) {
                delete(src);
            }
            return zip;
        } catch (ZipException e) {
            throw new RuntimeException("创建压缩文件失败", e);
        }
    }

    /**
     * 解压缩文件
     *
     * @param srcPathname  源文件(压缩文件)
     * @param destPathname 解压缩后存储的目录
     * @param password     解压缩使用的密码
     * @param delSrc       解压缩完成后是否删除源文件(压缩文件)
     * @return 返回解压缩的文件目录对象
     */
    public static File unzip(String srcPathname, String destPathname, String password, boolean delSrc) {
        File src = new File(srcPathname);
        if (!src.exists()) {
            throw new RuntimeException(src + " 不是一个有效的文件");
        }
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(src);
        } catch (ZipException e) {
            throw new RuntimeException("加载压缩文件失败", e);
        }
        if (!zipFile.isValidZipFile()) {
            throw new RuntimeException("压缩文件已被损坏，无法打开");
        }
        try {
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
        } catch (ZipException e) {
            throw new RuntimeException("解压缩密码不正确", e);
        }
        String srcName = src.getName();
        File dest = new File(destPathname + "/" + srcName.substring(0, srcName.lastIndexOf(".")));
        dest.mkdirs();
        try {
            zipFile.extractAll(dest.getAbsolutePath());
        } catch (ZipException e) {
            throw new RuntimeException("解压缩文件失败", e);
        }
        if (delSrc) {
            delete(src);
        }
        return dest;
    }

    /**
     * 删除文件或目录
     *
     * @param src 文件或目录
     */
    private static void delete(File src) {
        if (src.isFile()) {
            src.delete();
        } else if (src.isDirectory()) {
            for (File item : src.listFiles()) {
                delete(item);
            }
        }
        src.delete();
    }

}
