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
     * 压缩文件夹
     *
     * @param sourceFolder 源文件目录
     * @param delSource    压缩完成后是否删除源文件目录
     * @return 返回压缩文件对象
     */
    public static File zip(File sourceFolder, boolean delSource) {
        ZipParameters params = new ZipParameters();
        params.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        params.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        params.setIncludeRootFolder(false);
        File zip = new File(sourceFolder.getParentFile(), sourceFolder.getName() + ".zip");
        try {
            ZipFile zipFile = new ZipFile(zip);
            zipFile.addFolder(sourceFolder, params);
            if (delSource) {
                delete(sourceFolder);
            }
            return zip;
        } catch (ZipException e) {
            throw new RuntimeException("创建压缩文件失败！", e);
        }
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
