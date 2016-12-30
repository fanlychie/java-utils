package org.fanlychie.util;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件操作工具类, 基于 JAVA-8 开发
 *
 * @author fanlychie
 */
public final class FileUtils {

    private static final byte[] BUFFERB = new byte[1024 * 1024];

    private static final char[] BUFFERC = new char[1024 * 1024];

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
        String type = url.substring(url.lastIndexOf(".") + 1);
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
        String filename = src.getName();
        String type = filename.substring(filename.lastIndexOf(".") + 1);
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
         * @param response HttpServletResponse
         * @param filename 客户端下载文件的名称
         */
        public void to(HttpServletResponse response, String filename) {
            try (OutputStream out = response.getOutputStream()) {
                filename = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
                response.setContentType("application/octet-stream; charset=iso-8859-1");
                response.setHeader("Content-Disposition", "attachment; filename=" + filename);
                this.to(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            this.fileName = url.substring(url.lastIndexOf("/") + 1);
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
                if (type == null || type.equalsIgnoreCase("jpg")) {
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