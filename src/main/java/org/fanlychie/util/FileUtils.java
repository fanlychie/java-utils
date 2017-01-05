package org.fanlychie.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件操作工具类, 基于 JAVA-8 开发
 *
 * @author fanlychie
 */
public final class FileUtils {

    // 缓存数组大小
    private static final byte[] BUFFERB = new byte[1024 * 1024];

    // 缓存数组大小
    private static final char[] BUFFERC = new char[1024 * 1024];

    // 文件大小单位
    private static final String[] FILE_SIZE_UNIT = {"B", "KB", "M", "G"};

    /**
     * 写出文件
     *
     * @param file 操作的文件对象
     * @return 返回一个可写的流对象
     */
    public static WritableStream write(File file) {
        try {
            return new WritableStream(new FileInputStream(file), true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 写出输入流
     *
     * @param in 操作的输入流对象
     * @return 返回一个可写的流对象
     */
    public static WritableStream write(InputStream in) {
        return new WritableStream(in);
    }

    /**
     * 写出文本内容
     *
     * @param text 文本内容
     * @return 返回一个可写的流对象
     */
    public static WritableStream write(String text) {
        return new WritableStream(new BufferedReader(new StringReader(text)));
    }

    /**
     * 打开文件
     *
     * @param file 操作的文件对象
     * @return 返回一个可读的流对象
     */
    public static ReadableStream open(File file) {
        return open(file, "UTF-8");
    }

    /**
     * 打开输入流, 操作完成之后, 输入流将被关闭
     *
     * @param in 操作的输入流对象
     * @return 返回一个可读的流对象
     */
    public static ReadableStream open(InputStream in) {
        return open(in, "UTF-8");
    }

    /**
     * 使用指定的字符集编码打开文件
     *
     * @param file    操作的文件对象
     * @param charset 字符集编码
     * @return 返回一个可读的流对象
     */
    public static ReadableStream open(File file, String charset) {
        try {
            return new ReadableStream(new FileInputStream(file), charset);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用指定的字符集编码打开输入流, 操作完成之后, 输入流将被关闭
     *
     * @param in      操作的输入流对象
     * @param charset 字符集编码
     * @return 返回一个可读的流对象
     */
    public static ReadableStream open(InputStream in, String charset) {
        return new ReadableStream(in, charset);
    }

    /**
     * 打开URL链接
     *
     * @param url 操作的URL链接
     * @return 返回一个URL流对象
     */
    public static UrlStream open(String url) {
        return new UrlStream(url);
    }

    /**
     * 解码 Base64 编码的图片文件字符串
     *
     * @param base64EncodeStr 图片文件被 Base64 编码后的字符串内容
     * @return 返回一个 Base64 图片文件解码器对象
     */
    public static Base64ImageFileDecoder base64ImageFileDecode(String base64EncodeStr) {
        return new Base64ImageFileDecoder(base64EncodeStr);
    }

    /**
     * Base64 编码 URL 链接的图片文件
     *
     * @param url 图片文件 URL 链接
     * @return 返回一个 Base64 图片文件编码器对象
     */
    public static Base64ImageFileEncoder base64ImageFileEncoder(String url) {
        String type = substringLastSeparator(url, ".");
        if (type.length() > 3) {
            type = null;
        }
        HttpURLConnection conn = UrlStream.createHttpURLConnection(url);
        try {
            return new Base64ImageFileEncoder(conn.getInputStream()).setType(type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Base64 编码图片文件
     *
     * @param src 源文件
     * @return 返回一个 Base64 图片文件编码器对象
     */
    public static Base64ImageFileEncoder base64ImageFileEncoder(File src) {
        String type = substringLastSeparator(src.getName(), ".");
        try {
            return new Base64ImageFileEncoder(new FileInputStream(src)).setType(type);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Base64 编码输入流中的图片资源
     *
     * @param in 输入流
     * @return 返回一个 Base64 图片文件编码器对象
     */
    public static Base64ImageFileEncoder base64ImageFileEncoder(InputStream in) {
        return new Base64ImageFileEncoder(in);
    }

    /**
     * 转换文件大小单位
     *
     * @param size 文件大小, 单位(B)
     * @return 返回换算后的大小单位, eg: "2M" or "2KB"
     */
    public static String transformFileUnit(long size) {
        int index = 0;
        while (size / 1024 > 0 && index < FILE_SIZE_UNIT.length) {
            size = Math.round(size / 1024);
            index++;
        }
        return size + FILE_SIZE_UNIT[index];
    }

    /**
     * SpringMVC 文件上传
     *
     * @param file 文件对象
     * @return 返回一个 SpringMVC 文件上传对象
     */
    public static SpringMVCFileUpload upload(MultipartFile file) {
        return new SpringMVCFileUpload(new MultipartFile[]{file});
    }

    /**
     * SpringMVC 文件上传
     *
     * @param files 文件对象数组
     * @return 返回一个 SpringMVC 文件上传对象
     */
    public static SpringMVCFileUpload upload(MultipartFile[] files) {
        return new SpringMVCFileUpload(files);
    }

    /**
     * HttpServletRequest 文件上传
     *
     * @param request HttpServletRequest 对象
     * @return 返回一个 HttpServletRequest 文件上传对象
     */
    public static HttpServletRequestFileUpload upload(HttpServletRequest request) {
        return new HttpServletRequestFileUpload(request);
    }

    /**
     * 输出到 HttpServletResponse
     *
     * @param response HttpServletResponse
     * @return 返回一个本地文件访问器
     */
    public static LocalFileAccessor outputResponse(HttpServletResponse response) {
        return new LocalFileAccessor(response);
    }

    /**
     * 获取本地上传的文件
     *
     * @param fileKey 文件上传时返回的文件 Key
     * @return 返回文件对象
     */
    public static File getLocalFile(String fileKey) {
        return LocalFileUpload.achieveLocalFile(fileKey);
    }

    /**
     * 解析 HTML 内容成 PDF 文档
     *
     * @param content HTML 内容
     * @return 返回 PDF - HTML 转换器对象
     */
    public static PDFHtmlConvertor parseHtml2PDF(String content) {
        return new PDFHtmlConvertor(content);
    }

    /**
     * 可写的流
     *
     * @author fanlychie
     */
    public static final class WritableStream {

        private Reader reader;

        private InputStream is;

        private boolean closable;

        // 私有构造
        private WritableStream(Reader reader) {
            this.reader = reader;
        }

        // 私有构造
        private WritableStream(InputStream is) {
            this.is = is;
        }

        // 私有构造
        private WritableStream(InputStream is, boolean closable) {
            this(is);
            this.closable = closable;
        }

        /**
         * 写出到目标文件
         *
         * @param dest 目标文件
         */
        public void to(File dest) {
            to(dest, false);
        }

        /**
         * 写出到目标文件
         *
         * @param dest   目标文件
         * @param append 是否追加到文件末尾, 默认为覆盖原文件
         */
        public void to(File dest, boolean append) {
            try (OutputStream os = new FileOutputStream(dest, append)) {
                to(os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 写出到输出流对象
         *
         * @param os 输出流对象, 该对象操作完成后不会被关闭, 若要关闭, 必须在外部手工关闭
         */
        public void to(OutputStream os) {
            try {
                int read;
                if (is != null) {
                    while ((read = is.read(BUFFERB)) != -1) {
                        os.write(BUFFERB, 0, read);
                    }
                    if (closable) {
                        is.close();
                    }
                } else if (reader != null) {
                    Writer writer = new BufferedWriter(new OutputStreamWriter(os));
                    while ((read = reader.read(BUFFERC)) != -1) {
                        writer.write(BUFFERC, 0, read);
                    }
                    writer.flush();
                    reader.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 写出到客户端以供客户端下载此文件, 兼容中文字符
         *
         * @param response         HttpServletResponse
         * @param downloadFileName 客户端下载文件的名称
         */
        public void to(HttpServletResponse response, String downloadFileName) {
            try (OutputStream out = response.getOutputStream()) {
                initBrowserDownload(response, downloadFileName);
                this.to(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 初始化浏览器下载相关配置
         *
         * @param response HttpServletResponse
         * @param filename 下载显示的文件名
         * @throws IOException
         */
        private static void initBrowserDownload(HttpServletResponse response, String filename) throws IOException {
            filename = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
            response.setContentType("application/octet-stream; charset=iso-8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        }

    }

    /**
     * 可读的流
     *
     * @author fanlychie
     */
    public static final class ReadableStream {

        private InputStream is;

        private String charset;

        // 私有构造
        private ReadableStream(InputStream is, String charset) {
            this.is = is;
            this.charset = charset;
        }

        /**
         * 逐行读取
         *
         * @param consumer (每行的文本内容)
         */
        public void readlines(Consumer<String> consumer) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
                String read;
                while ((read = reader.readLine()) != null) {
                    consumer.accept(read);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 读取全部
         *
         * @param consumer (读取到的全部的文本内容)
         */
        public void read(Consumer<String> consumer) {
            consumer.accept(read());
        }

        /**
         * 读取全部
         *
         * @return 返回读取到的全部的文本内容
         */
        public String read() {
            StringBuilder builder = new StringBuilder();
            readlines((line) -> builder.append(line).append("\n"));
            return builder.length() > 0 ? builder.toString().substring(0, builder.length() - 1) : "";
        }

    }

    /**
     * URL 流
     *
     * @author fanlychie
     */
    public static final class UrlStream {

        private String fileName;

        private HttpURLConnection conn;

        // 私有构造
        private UrlStream(String url) {
            this.conn = createHttpURLConnection(url);
            this.fileName = substringLastSeparator(url, "/");
        }

        /**
         * 下载到本地目录
         *
         * @param localFolder 本地目录
         */
        public void download(File localFolder) {
            if (!localFolder.exists()) {
                throw new IllegalArgumentException("'" + localFolder + "' not exist");
            } else if (!localFolder.isDirectory()) {
                throw new IllegalArgumentException("'" + localFolder + "' is not a directory");
            }
            try (InputStream is = conn.getInputStream(); OutputStream os = new FileOutputStream(new File(localFolder, fileName))) {
                int read;
                while ((read = is.read(BUFFERB)) != -1) {
                    os.write(BUFFERB, 0, read);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 设置下载的文件的名称
         *
         * @param fileName 文件名称
         * @return {@link UrlStream}
         */
        public UrlStream setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        /**
         * 设置读取超时时间
         *
         * @param second 秒
         * @return {@link UrlStream}
         */
        public UrlStream setReadTimeout(int second) {
            this.conn.setReadTimeout(second * 1000);
            return this;
        }

        /**
         * 设置连接超时时间
         *
         * @param second 秒
         * @return {@link UrlStream}
         */
        public UrlStream setConnectTimeout(int second) {
            this.conn.setConnectTimeout(second * 1000);
            return this;
        }

        /**
         * 创建一个 Http URL 链接对象
         *
         * @param url URL地址
         * @return HttpURLConnection
         */
        private static HttpURLConnection createHttpURLConnection(String url) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setReadTimeout(300000);
                conn.setConnectTimeout(60000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0");
                return conn;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Base64 图片文件解码器
     */
    public static final class Base64ImageFileDecoder {

        // 文件名
        private String fileName;

        // 图片文件被 Base64 编码后的字符串
        private String base64EncodeStr;

        // 私有化
        private Base64ImageFileDecoder(String base64EncodeStr) {
            this.base64EncodeStr = base64EncodeStr;
        }

        /**
         * 解码到文件夹
         *
         * @param folder 存放图片的文件夹对象
         * @return 返回解码后的图片文件对象
         */
        public File to(File folder) {
            Pattern pattern = Pattern.compile("(data:image/\\w+;base64,)(\\S+)");
            Matcher matcher = pattern.matcher(base64EncodeStr);
            if (fileName == null) {
                String scheme = matcher.replaceAll("$1");
                String extension = "";
                int index = scheme.indexOf("/") + 1;
                int limit = scheme.indexOf(";");
                if (index != -1 && limit != -1) {
                    extension = scheme.substring(index, limit);
                    if (extension.equalsIgnoreCase("jpeg")) {
                        extension = ".jpg";
                    } else if (extension.equalsIgnoreCase("x-icon")) {
                        extension = ".ico";
                    } else {
                        extension = "." + extension;
                    }
                }
                fileName = UUID.randomUUID().toString().replace("-", "") + extension;
            }
            File dest = new File(folder, fileName);
            base64EncodeStr = matcher.replaceAll("$2");
            byte[] contents = base64Decode(base64EncodeStr);
            for (int i = 0; i < contents.length; ++i) {
                if (contents[i] < 0) {
                    contents[i] += 256;
                }
            }
            try {
                try (OutputStream out = new FileOutputStream(dest)) {
                    out.write(contents);
                    out.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return dest;
        }

        /**
         * 设置图片文件存储的文件名
         *
         * @param fileName 文件名, eg: demo.jpg
         */
        public Base64ImageFileDecoder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

    }

    /**
     * Base64 图片文件编码器
     */
    public static final class Base64ImageFileEncoder {

        // 图片文件类型
        private String type;

        // 输入流
        private InputStream in;

        // 完成时自动关闭流
        private boolean autoCloseStream = true;

        // 私有化
        private Base64ImageFileEncoder(InputStream in) {
            this.in = in;
        }

        /**
         * 设置文件类型
         *
         * @param type 文件类型, eg: jpg
         */
        public Base64ImageFileEncoder setType(String type) {
            this.type = type;
            return this;
        }

        /**
         * 设置完成时是否自动关闭流, 默认自动关闭
         *
         * @param autoCloseStream true/false
         */
        public Base64ImageFileEncoder setAutoCloseStream(boolean autoCloseStream) {
            this.autoCloseStream = autoCloseStream;
            return this;
        }

        @Override
        public String toString() {
            try {
                int read, index = 0;
                byte[] buffer = new byte[2 * 1024 * 1024];
                while ((read = in.read(BUFFERB)) != -1) {
                    System.arraycopy(BUFFERB, 0, buffer, index, read);
                    index += read;
                }
                byte[] data = new byte[index];
                System.arraycopy(buffer, 0, data, 0, index);
                String content = new String(base64Encode(data));
                if (type == null || type.length() == 0 || type.equalsIgnoreCase("jpg")) {
                    type = "jpeg";
                } else if (type.equalsIgnoreCase("ico")) {
                    type = "x-icon";
                }
                content = "data:image/" + type + ";base64," + content;
                return content;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (autoCloseStream && in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }

    /**
     * SpringMVC 文件上传
     */
    public static final class SpringMVCFileUpload extends AbstractLocalFileUpload {

        // SpringMVC 文件对象数组
        private MultipartFile[] files;

        // 私有化
        private SpringMVCFileUpload(MultipartFile[] files) {
            this.files = files;
        }

        /**
         * 设置文件上传的大小限制
         *
         * @param minSize 最小大小, 单位(B), 默认为0, 表示不限制
         * @param maxSize 最大大小, 单位(B), 默认为0, 表示不限制
         * @return
         */
        public SpringMVCFileUpload setLimit(long minSize, long maxSize) {
            this.limit(minSize, maxSize);
            return this;
        }

        /**
         * 设置文件上传的允许的类型
         *
         * @param extension 文件扩展名, 默认为空, 表示不限制, eg: "jpg", "png", 表示只允许 jpg 和 png 类型的文件上传
         * @return
         */
        public SpringMVCFileUpload setFilters(String... extension) {
            this.filter(extension);
            return this;
        }

        /**
         * 设置文件类型适配功能
         *
         * @param typeAdapter 文件类型适配功能, 当上传的文件类型不符合时, 可通过此适配器转换并手工完成上传,
         *                    参数：(InputStream, File), File 为未存储的文件对象, 文件名不可修改, 否则
         *                    上传后工具类找不到文件位置, 可以通过 renameTo 来修改文件扩展名, 以完成文件
         *                    类型转换。转换成功时需返回true, 失败返回false.
         * @return
         */
        public SpringMVCFileUpload setTypeAdapter(BiFunction<InputStream, File, Boolean> typeAdapter) {
            this.typeAdapter = typeAdapter;
            return this;
        }

        /**
         * 设置文件大小适配功能
         *
         * @param sizeAdapter 文件大小适配功能, 当上传的文件大小不符合时, 可通过此适配器转换并手工完成上传,
         *                    参数：(InputStream, File), File 为未存储的文件对象, 文件名不可修改, 否则
         *                    上传后工具类找不到文件位置。转换成功时需返回true, 失败返回false.
         * @return
         */
        public SpringMVCFileUpload setSizeAdapter(BiFunction<InputStream, File, Boolean> sizeAdapter) {
            this.sizeAdapter = sizeAdapter;
            return this;
        }

        /**
         * 执行文件上传
         *
         * @return 返回一个文件上传的报告对象
         */
        public FileUploadReport execute() {
            FileUploadReport report = new FileUploadReport();
            Arrays.stream(files).filter(file -> file != null && !file.isEmpty()).forEach(file -> {
                excuteFileUpload(report, file, file.getOriginalFilename(), file.getSize(), destFile -> {
                    try {
                        file.transferTo(destFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            return report;
        }

    }

    /**
     * HttpServletRequest 文件上传
     */
    public static final class HttpServletRequestFileUpload extends AbstractLocalFileUpload {

        // HttpServletRequest
        private HttpServletRequest request;

        // 私有化
        private HttpServletRequestFileUpload(HttpServletRequest request) {
            this.request = request;
        }

        /**
         * 设置文件上传的大小限制
         *
         * @param minSize 最小大小, 单位(B), 默认为0, 表示不限制
         * @param maxSize 最大大小, 单位(B), 默认为0, 表示不限制
         * @return
         */
        public HttpServletRequestFileUpload setLimit(long minSize, long maxSize) {
            this.limit(minSize, maxSize);
            return this;
        }

        /**
         * 设置文件上传的允许的类型
         *
         * @param extension 文件扩展名, 默认为空, 表示不限制, eg: "jpg", "png", 表示只允许 jpg 和 png 类型的文件上传
         * @return
         */
        public HttpServletRequestFileUpload setFilters(String... extension) {
            this.filter(extension);
            return this;
        }

        /**
         * 设置文件类型适配功能
         *
         * @param typeAdapter 文件类型适配功能, 当上传的文件类型不符合时, 可通过此适配器转换并手工完成上传,
         *                    参数：(InputStream, File), File 为未存储的文件对象, 文件名不可修改, 否则
         *                    上传后工具类找不到文件位置, 可以通过 renameTo 来修改文件扩展名, 以完成文件
         *                    类型转换。转换成功时需返回true, 失败返回false.
         * @return
         */
        public HttpServletRequestFileUpload setTypeAdapter(BiFunction<InputStream, File, Boolean> typeAdapter) {
            this.typeAdapter = typeAdapter;
            return this;
        }

        /**
         * 设置文件大小适配功能
         *
         * @param sizeAdapter 文件大小适配功能, 当上传的文件大小不符合时, 可通过此适配器转换并手工完成上传,
         *                    参数：(InputStream, File), File 为未存储的文件对象, 文件名不可修改, 否则
         *                    上传后工具类找不到文件位置。转换成功时需返回true, 失败返回false.
         * @return
         */
        public HttpServletRequestFileUpload setSizeAdapter(BiFunction<InputStream, File, Boolean> sizeAdapter) {
            this.sizeAdapter = sizeAdapter;
            return this;
        }

        /**
         * 执行文件上传
         *
         * @return 返回一个文件上传的报告对象
         */
        public FileUploadReport execute() {
            FileUploadReport report = new FileUploadReport();
            if (!ServletFileUpload.isMultipartContent(request)) {
                report.addFailItem("不支持文件上传的表单域");
            } else {
                ServletFileUpload fileupload = new ServletFileUpload(new DiskFileItemFactory());
                fileupload.setHeaderEncoding("UTF-8");
                try {
                    fileupload.parseRequest(request).stream().filter(fileItem -> !fileItem.isFormField()).forEach(fileItem -> {
                        excuteFileUpload(report, fileItem, fileItem.getName(), fileItem.getSize(), file -> {
                            try {
                                fileItem.write(file);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    });
                } catch (FileUploadException e) {
                    e.printStackTrace();
                    report.addFailItem("文件上传失败, 请重新上传");
                }
            }
            return report;
        }

    }

    /**
     * 本地文件访问器
     */
    public static final class LocalFileAccessor {

        // HttpServletResponse
        private HttpServletResponse response;

        // 输出的 contentType
        private String contentType;

        // 私有化
        private LocalFileAccessor(HttpServletResponse response) {
            this.response = response;
        }

        /**
         * 设置 contentType
         *
         * @param contentType Content-Type
         * @return
         */
        public LocalFileAccessor setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * 访问本地文件
         *
         * @param fileKey 本地文件上传返回的文件 Key
         */
        public void access(String fileKey) {
            access(LocalFileUpload.achieveLocalFile(fileKey));
        }

        /**
         * 访问本地文件
         *
         * @param file 本地文件对象
         */
        public void access(File file) {
            if (file == null) {
                throw new RuntimeException("找不到文件: " + file);
            }
            if (contentType == null) {
                contentType = lookupMime(substringLastSeparator(file.getName(), "."));
            }
            response.setContentType(contentType);
            try (OutputStream out = response.getOutputStream()) {
                new WritableStream(new FileInputStream(file), true).to(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // 查找 MIME 类型
        private String lookupMime(String extension) {
            switch (extension) {
                case "doc":
                    return "application/msword";
                case "docx":
                    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case "rtf":
                    return "application/rtf";
                case "xls":
                    return "application/vnd.ms-excel";
                case "xlsx":
                    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case "ppt":
                    return "application/vnd.ms-powerpoint";
                case "pptx":
                    return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                case "pdf":
                    return "application/pdf";
                case "swf":
                    return "application/x-shockwave-flash";
                case "dll":
                    return "application/x-msdownload";
                case "exe":
                case "msi":
                case "chm":
                case "rar":
                    return "application/octet-stream";
                case "tar":
                    return "application/x-tar";
                case "zip":
                    return "application/x-zip-compressed";
                case "z":
                case "tgz":
                    return "application/x-compressed";
                case "wav":
                    return "audio/wav";
                case "wma":
                    return "audio/x-ms-wma";
                case "wmv":
                    return "video/x-ms-wmv";
                case "mp2":
                case "mp3":
                case "mpe":
                case "mpg":
                case "mpeg":
                    return "audio/mpeg";
                case "bmp":
                    return "image/bmp";
                case "gif":
                    return "image/gif";
                case "png":
                    return "image/png";
                case "tif":
                case "tiff":
                    return "image/tiff";
                case "jpe":
                case "jpg":
                case "jpeg":
                    return "image/jpeg";
                case "ico":
                    return "image/x-icon";
                case "txt":
                    return "text/plain";
                case "xml":
                    return "text/xml";
                case "html":
                    return "text/html";
                case "css":
                    return "text/css";
                case "js":
                    return "text/javascript";
                case "mht":
                case "mhtml":
                    return "message/rfc822";
                default:
                    return "";
            }
        }

    }

    /**
     * 本地文件上传基类
     */
    private static abstract class AbstractLocalFileUpload {

        // 允许文件上传的最小大小
        protected long minSize;

        // 允许文件上传的最大大小
        protected long maxSize;

        // 允许文件上传的类型
        protected List<String> filters;

        // 文件大小限制提示信息
        protected String limitTips;

        // 文件类型限制提示信息
        protected String filterTips;

        // 文件类型适配功能
        protected BiFunction<InputStream, File, Boolean> typeAdapter;

        // 文件大小适配功能
        protected BiFunction<InputStream, File, Boolean> sizeAdapter;

        // 限制上传的文件大小
        protected void limit(long minSize, long maxSize) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.limitTips = "请上传 ";
            if (maxSize != 0 && minSize == 0) {
                minSize = 1;
            }
            if (minSize != 0 && maxSize != 0) {
                this.limitTips += transformFileUnit(minSize);
                this.limitTips += " ~ ";
                this.limitTips += transformFileUnit(maxSize);
                this.limitTips += " 的文件";
            }
        }

        // 限制上传的文件类型
        protected void filter(String... extension) {
            this.filters = new ArrayList<>();
            Arrays.stream(extension).forEach(item -> filters.add(item.toLowerCase()));
            this.filterTips = Arrays.toString(filters.toArray());
            this.filterTips = filterTips.substring(1, filterTips.length() - 1);
        }

        // 执行文件上传
        protected void excuteFileUpload(FileUploadReport report, Object target, String fileName, long fileSize, Consumer<File> consumer) {
            String extension = substringLastSeparator(fileName, ".");
            if (filters == null || (filters != null && filters.contains(extension))) {
                String overSizeLimit = null;
                if (minSize != 0 && fileSize < minSize) {
                    overSizeLimit = "太小";
                } else if (maxSize != 0 && fileSize > maxSize) {
                    overSizeLimit = "太大";
                }
                if (overSizeLimit != null) {
                    if (sizeAdapter != null) {
                        adaptFile(report, target, fileName, extension, sizeAdapter);
                    } else {
                        report.addFailItem("文件 \"" + fileName + "\" " + overSizeLimit + ", 不符合上传标准, " + limitTips);
                    }
                } else {
                    LocalFile localFile = LocalFileUpload.createLocalFile(extension);
                    try {
                        consumer.accept(localFile.file);
                        report.addSuccessItem(localFile.key);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        report.addFailItem("文件 \"" + fileName + "\" " + overSizeLimit + ", 上传失败, 请重新选择上传");
                    }
                }
            } else {
                if (typeAdapter != null) {
                    adaptFile(report, target, fileName, extension, typeAdapter);
                } else {
                    report.addFailItem("文件 \"" + fileName + "\" 是不支持上传的类型, 请选择 " + filterTips + " 类型的文件");
                }
            }
        }

        // 适配文件
        private void adaptFile(FileUploadReport report, Object target, String fileName, String extension, BiFunction<InputStream, File, Boolean> adapter) {
            LocalFile localFile = LocalFileUpload.createLocalFile(extension);
            try {
                InputStream in = null;
                if (target instanceof FileItem) {
                    in = ((FileItem) target).getInputStream();
                } else if (target instanceof MultipartFile) {
                    in = ((MultipartFile) target).getInputStream();
                }
                Boolean adapterResult = adapter.apply(in, localFile.file);
                if (adapterResult != null && adapterResult) {
                    report.addSuccessItem(localFile.key);
                } else {
                    report.addFailItem("文件 \"" + fileName + "\" 上传失败, 请重新选择上传");
                }
            } catch (Throwable e) {
                e.printStackTrace();
                report.addFailItem("文件 \"" + fileName + "\" 上传失败, 请重新选择上传");
            }
        }

    }

    /**
     * 本地文件上传, 可在 Spring 中使用 bean 配置选项：
     *
     * <bean class="org.fanlychie.util.FileUtils.LocalFileUpload" p:storageRootFolder="/pathname/" p:childFolderLength="2" />
     */
    public static final class LocalFileUpload {

        // 上传的文件存储的根目录
        private static String storageRootFolder = System.getProperty("java.io.tmpdir");

        // 上传的文件存储的子目录长度
        private static int childFolderLength = 5;

        /**
         * 设置上传的文件存储的根目录
         *
         * @param storageRootFolder 上传的文件存储的根目录, 默认使用 Java IO 临时目录
         */
        public void setStorageRootFolder(String storageRootFolder) {
            LocalFileUpload.storageRootFolder = storageRootFolder;
        }

        /**
         * 设置上传的文件存储的子目录长度
         *
         * @param childFolderLength 上传的文件存储的子目录长度, 默认长度为 5
         */
        public void setChildFolderLength(int childFolderLength) {
            LocalFileUpload.childFolderLength = childFolderLength;
        }

        /**
         * 创建本地文件
         *
         * @param extension 文件扩展名
         * @return 返回一个本地文件对象
         */
        private static LocalFile createLocalFile(String extension) {
            if (extension == null) {
                extension = "";
            } else {
                extension = "." + extension;
            }
            String uuidStr = UUID.randomUUID().toString().replace("-", "");
            String fileName = uuidStr + extension;
            String childFolderName = fileName.substring(0, childFolderLength);
            File childFoloder = new File(storageRootFolder + "/" + childFolderName);
            if (!childFoloder.exists()) {
                childFoloder.mkdirs();
            }
            return new LocalFile(uuidStr, new File(childFoloder, fileName));
        }

        /**
         * 获取本地文件
         *
         * @param fileKey 文件上传返回的文件 Key
         * @return 返回找到的文件对象
         */
        private static File achieveLocalFile(String fileKey) {
            String childFolderName = fileKey.substring(0, childFolderLength);
            File childFoloder = new File(storageRootFolder + "/" + childFolderName);
            for (File file : childFoloder.listFiles()) {
                if (file.getName().startsWith(fileKey)) {
                    return file;
                }
            }
            return null;
        }

    }

    /**
     * 文件上传报告
     */
    public static final class FileUploadReport {

        // 失败个数
        private int failNum;

        // 成功个数
        private int successNum;

        // 成功的文件 Key 列表
        private List<String> fileKeys;

        // 失败的文件消息列表
        private List<String> failMsgs;

        // 私有化
        private FileUploadReport() {
            this.fileKeys = new ArrayList<>();
            this.failMsgs = new ArrayList<>();
        }

        /**
         * 获取失败的文件个数
         *
         * @return 返回失败的文件个数
         */
        public int getFailNum() {
            return failNum;
        }

        /**
         * 获取成功的文件个数
         *
         * @return 返回成功的文件个数
         */
        public int getSuccessNum() {
            return successNum;
        }

        /**
         * 获取成功的文件 Key 列表
         *
         * @return 返回成功的文件 Key 列表
         */
        public List<String> getFileKeys() {
            return fileKeys;
        }

        /**
         * 获取失败的文件消息列表
         *
         * @return 返回失败的文件消息列表
         */
        public List<String> getFailMsgs() {
            return failMsgs;
        }

        /**
         * 报告是否健康的, 若是, 表明上传全部成功, 否则表明存在上传失败的文件
         *
         * @return true/false
         */
        public boolean isHealthy() {
            return failNum == 0;
        }

        private void addSuccessItem(String fileKey) {
            this.successNum++;
            this.fileKeys.add(fileKey);
        }

        private void addFailItem(String failMsg) {
            this.failNum++;
            this.failMsgs.add(failMsg);
        }

    }

    /**
     * PDF-HTML 转换器
     */
    public static final class PDFHtmlConvertor {

        // HTML 内容
        private String content;

        // 私有化
        private PDFHtmlConvertor(String content) {
            this.content = content;
        }

        /**
         * 生成到文件
         *
         * @param file PDF 文件对象
         */
        public void to(File file) {
            try (OutputStream os = new FileOutputStream(file)) {
                execute(os);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 生成到本地文件存储
         *
         * @return 返回本地文件存储的文件 Key 值
         */
        public String toLocalFile() {
            LocalFile localFile = LocalFileUpload.createLocalFile("pdf");
            to(localFile.file);
            return localFile.key;
        }

        /**
         * 生成到客户端浏览器下载
         *
         * @param response         HttpServletResponse
         * @param downloadFileName 下载显示的文件名称
         */
        public void to(HttpServletResponse response, String downloadFileName) {
            try (OutputStream out = response.getOutputStream()) {
                WritableStream.initBrowserDownload(response, downloadFileName);
                execute(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 执行生成
         *
         * @param os 输出流对象
         */
        private void execute(OutputStream os) {
            Document document = new Document();
            try {
                PdfWriter writer = PdfWriter.getInstance(document, os);
                document.open();
                Reader reader = new BufferedReader(new StringReader(content));
                XMLWorkerHelper.getInstance().parseXHtml(writer, document, reader);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                if (document != null) {
                    document.close();
                }
            }
        }

    }

    /**
     * 本地文件
     */
    private static final class LocalFile {

        /**
         * 文件Key
         */
        private String key;

        // 文件对象
        private File file;

        private LocalFile(String key, File file) {
            this.key = key;
            this.file = file;
        }

    }

    /**
     * 在源字符串中切割给定的分隔符最后出现的位置起剩余的字符串
     *
     * @param source 源字符串
     * @param separator 分隔符
     * @return
     */
    private static String substringLastSeparator(String source, String separator) {
        if (source == null || separator == null) {
            return "";
        }
        int index = source.lastIndexOf(separator);
        if (index == -1) {
            return "";
        }
        return source.substring(index + 1).toLowerCase();
    }

    /**
     * Java 1.8 开始提供 java.util.Base64, 低于 Java 1.8 的可使用 Apache 的 Base64 算法替换：
     * <p>
     * org.apache.commons.codec.binary.Base64.encodeBase64(byte[] src)
     *
     * @param src 源字节数组
     * @return
     */
    private static byte[] base64Encode(byte[] src) {
        return Base64.getEncoder().encode(src);
    }

    /**
     * Java 1.8 开始提供 java.util.Base64, 低于 Java 1.8 的可使用 Apache 的 Base64 算法替换：
     * <p>
     * org.apache.commons.codec.binary.Base64.decodeBase64(byte[] src)
     *
     * @param src 源字节数组
     * @return
     */
    private static byte[] base64Decode(byte[] src) {
        return Base64.getDecoder().decode(src);
    }

    /**
     * Base64 解码
     *
     * @param src 源字符串
     * @return
     */
    private static byte[] base64Decode(String src) {
        return base64Decode(src.getBytes(Charset.forName("ISO-8859-1")));
    }

}