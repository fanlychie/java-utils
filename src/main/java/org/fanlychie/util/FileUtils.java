package org.fanlychie.util;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
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

    public static Base64Image base64ImageDecode(String base64EncodeStr) {
        return new Base64Image(base64EncodeStr);
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
     * Base64 图片
     */
    public static final class Base64Image {

        private String fileName;

        private String base64EncodeStr;

        private Base64Image(String base64EncodeStr) {
            this.base64EncodeStr = base64EncodeStr;
        }

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

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        private static String encode(String url) {
            String type = url.substring(url.lastIndexOf(".") + 1);
            if (type.length() > 3) {
                type = null;
            }
            HttpURLConnection conn = UrlStream.createHttpURLConnection(url);
            try {
                try (InputStream in = conn.getInputStream()) {
                    return encode(in, type);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static String encode(File src) {
            try {
                try (InputStream in = new FileInputStream(src)) {
                    String filename = src.getName();
                    String type = filename.substring(filename.lastIndexOf(".") + 1);
                    return encode(in, type);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static String encode(InputStream in, String type) {
            try {
                System.out.println("in.available(): " + in.available());
                byte[] data = new byte[in.available()];
                int read = in.read(data);
                System.out.println("read: " + read);
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

    public static void main(String[] args) {
//        System.out.println(Base64Image.encode("http://www.wufafuwu.com/uploads/150129/160216/1-160216213120L3-lp.jpg"));
        FileUtils.base64ImageDecode("data:image/jpeg;base64,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").to(new File("C:\\data"));
    }

}