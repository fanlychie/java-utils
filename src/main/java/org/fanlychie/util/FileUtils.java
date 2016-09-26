package org.fanlychie.util;

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
import java.util.function.Consumer;

/**
 * 文件操作工具类, 基于 JAVA-8 开发
 * 
 * @author fanlychie
 */
public final class FileUtils {
	
	private static final byte[] BUFFERB = new byte[1024 * 1024];
	
	private static final char[] BUFFERC = new char[1024 * 1024];

	/**
	 * 返回一个包装可写的流对象
	 * 
	 * @param file
	 *            操作的文件对象
	 *            
	 * @return {@link WritableStream}
	 */
	public static WritableStream write(File file) {
		try {
			return new WritableStream(new FileInputStream(file), true);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 返回一个包装可写的流对象
	 * 
	 * @param in
	 *            操作的输入流对象
	 * 
	 * @return {@link WritableStream}
	 */
	public static WritableStream write(InputStream in) {
		return new WritableStream(in);
	}

	/**
	 * 返回一个包装可写的流对象
	 * 
	 * @param text
	 *            文本内容
	 *            
	 * @return {@link WritableStream}
	 */
	public static WritableStream write(String text) {
		return new WritableStream(new BufferedReader(new StringReader(text)));
	}

	/**
	 * 打开一个包装可读的流对象
	 * 
	 * @param file
	 *            操作的文件对象
	 * 
	 * @return {@link ReadableStream}
	 */
	public static ReadableStream open(File file) {
		return open(file, "UTF-8");
	}

	/**
	 * 打开一个包装可读的流对象
	 * 
	 * @param in
	 *            操作的输入流对象, 操作完成之后, 输入流将被关闭
	 * 
	 * @return {@link ReadableStream}
	 */
	public static ReadableStream open(InputStream in) {
		return open(in, "UTF-8");
	}

	/**
	 * 打开一个包装可读的流对象
	 * 
	 * @param file
	 *            操作的文件对象
	 * @param charset
	 *            字符集编码, 默认为 UTF-8
	 * 
	 * @return {@link ReadableStream}
	 */
	public static ReadableStream open(File file, String charset) {
		try {
			return new ReadableStream(new FileInputStream(file), charset);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 打开一个包装可读的流对象
	 * 
	 * @param in
	 *            操作的输入流对象, 操作完成之后, 输入流将被关闭
	 * @param charset
	 *            字符集编码, 默认为 UTF-8
	 * 
	 * @return {@link ReadableStream}
	 */
	public static ReadableStream open(InputStream in, String charset) {
		return new ReadableStream(in, charset);
	}
	
	/**
	 * 打开一个包装Url的流对象
	 * 
	 * @param url
	 *            操作的URL链接
	 * 
	 * @return {@link UrlStream}
	 */
	public static UrlStream open(String url) {
		return new UrlStream(url);
	}
	
	/**
	 * 包装可写的流操作
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
		 * @param dest
		 *            目标文件
		 */
		public void to(File dest) {
			to(dest, false);
		}

		/**
		 * 写出到目标文件
		 * 
		 * @param dest
		 *            目标文件
		 * @param append
		 *            是否追加到文件末尾, 默认为覆盖原文件
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
		 * @param os
		 *            输出流对象, 该对象操作完成后不会被关闭, 若要关闭, 必须在外部手工关闭
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
				}
				else if (reader != null) {
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
		
	}
	
	/**
	 * 包装可读的流操作
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
		 * @param consumer
		 *            (每行的文本内容)
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
		 * @param consumer
		 *            (读取到的全部的文本内容)
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
			readlines((line) -> builder.append(line).append("\r\n"));
			return builder.length() > 0 ? builder.toString().substring(0, builder.length() - 2) : "";
		}
		
	}
	
	/**
	 * 包装Url的流操作
	 * 
	 * @author fanlychie
	 */
	public static final class UrlStream {
		
		private String fileName;

		private HttpURLConnection conn;
		
		// 私有构造
		private UrlStream(String url) {
			init(url);
		}
		
		/**
		 * 下载到本地目录
		 * 
		 * @param localFolder
		 *            本地目录
		 */
		public void download(File localFolder) {
			if (!localFolder.exists()) {
				throw new IllegalArgumentException("'" + localFolder + "' not exist");
			}
			else if (!localFolder.isDirectory()) {
				throw new IllegalArgumentException("'" + localFolder + "' is not a directory");
			}
			try (InputStream is = conn.getInputStream();
					OutputStream os = new FileOutputStream(new File(localFolder, fileName))) {
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
		 * @param fileName
		 *            文件名称
		 *            
		 * @return {@link UrlStream}
		 */
		public UrlStream setFileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
		
		/**
		 * 设置读取超时时间
		 * 
		 * @param second
		 *            秒
		 *            
		 * @return {@link UrlStream}
		 */
		public UrlStream setReadTimeout(int second) {
			this.conn.setReadTimeout(second * 1000);
			return this;
		}
		
		/**
		 * 设置连接超时时间
		 * 
		 * @param second
		 *            秒
		 *            
		 * @return {@link UrlStream}
		 */
		public UrlStream setConnectTimeout(int second) {
			this.conn.setConnectTimeout(second * 1000);
			return this;
		}
		
		// 初始化
		private void init(String url) {
			try {
				this.conn = (HttpURLConnection) new URL(url).openConnection();
				this.conn.setReadTimeout(300000);
				this.conn.setConnectTimeout(60000);
				this.conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0");
				this.fileName = url.substring(url.lastIndexOf("/") + 1);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

}