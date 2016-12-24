package org.fanlychie.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * 字符串工具类
 * 
 * @author fanlychie
 */
public final class StrUtils {

	/**
	 * 判断是否为空字符串
	 * 
	 * @param str
	 *            源字串
	 * 
	 * @return 字符串若为 null 或长度为 0, 则返回 true, 否则返回 false
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	/**
	 * 判断是否为非空字符串
	 * 
	 * @param str
	 *            源字串
	 * 
	 * @return 字符串若不为 null 或长度不为 0, 则返回 true, 否则返回 false
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 判断是否为空白字符串
	 * 
	 * @param str
	 *            源字串
	 * 
	 * @return 字符串若为非空串, 且至少有一个非空白字符(空格,段落,回车,制表符等), 则返回 false, 否则返回 true
	 */
	public static boolean isBlank(String str) {
		if (isNotEmpty(str)) {
			int len = str.length();
			for (int i = 0; i < len; i++) {
				if (!Character.isWhitespace(str.charAt(i))) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 判断是否为非空白字符串
	 * 
	 * @param str
	 *            源字串
	 * 
	 * @return 字符串若为非空串, 且至少有一个非空白字符(空格,段落,回车,制表符等), 则返回 true, 否则返回 false
	 */
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	/**
	 * 编码字符串
	 * 
	 * @param str
	 *            源字串
	 * 
	 * @return 返回 UTF8 编码后的字符串
	 */
	public static String encode(String str) {
		return encode(str, "UTF-8");
	}

	/**
	 * 编码字符串
	 * 
	 * @param str
	 *            源字串
	 * @param charset
	 *            编码字符集, 默认使用 UTF-8
	 * @return
	 */
	public static String encode(String str, String charset) {
		try {
			return URLEncoder.encode(str, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 解码字符串
	 * 
	 * @param str
	 *            源字串
	 * 
	 * @return 返回 UTF-8 解码后的字符串
	 */
	public static String decode(String str) {
		return decode(str, "UTF-8");
	}

	/**
	 * 解码字符串
	 * 
	 * @param str
	 *            源字串
	 * @param charset
	 *            编码字符集, 默认使用 UTF-8
	 * @return
	 */
	public static String decode(String str, String charset) {
		try {
			return URLDecoder.decode(str, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 若字符串不为 null 且所有字符都是字母则返回 true, 否则返回 false
	 * 
	 * @param str
	 *            源字串
	 * @return
	 */
	public static boolean isAlpha(String str) {
		return str != null ? str.matches("^[a-zA-Z]+$") : false;
	}

	/**
	 * 若字符串不为 null 且所有字符都是数字则返回 true, 否则返回 false
	 * 
	 * @param str
	 *            源字串
	 * @return
	 */
	public static boolean isNumeric(String str) {
		return str != null ? str.matches("^[0-9]+$") : false;
	}

	/**
	 * 返回一个以','(逗号)或';'(分号)切割成的数组
	 * 
	 * @param str
	 *            源字串
	 * 
	 * @return 若字符串为空串则返回 null
	 */
	public static String[] asArray(String str) {
		return isNotBlank(str) ? str.trim().split("[,;]") : null;
	}

	/**
	 * 返回一个以','(逗号)或';'(分号)切割成的列表
	 * 
	 * @param str
	 *            源字串
	 * 
	 * @return 若字符串为空串则返回 null
	 */
	public static List<String> asList(String str) {
		String[] items = asArray(str);
		return items != null ? Arrays.asList(items) : null;
	}

	/**
	 * 查找字符串
	 * 
	 * @param source
	 *            源字串
	 * @param sub
	 *            查找的子串
	 * 
	 * @return 返回查找的子串在源串中第一次出现的位置索引值, 若查找不到则返回-1
	 */
	public static int find(String source, String sub) {
		return find(source, sub, 1);
	}

	/**
	 * 查找字符串
	 * 
	 * @param source
	 *            源字串
	 * @param sub
	 *            查找的子串
	 * @param crash
	 *            默认值是1, 表示在源字符串中从左至右开始查找子串并在第一次发现时返回; 该值可以是负数, 如-1,
	 *            表示在源串中从右至左开始查找
	 * 
	 * @return 返回查找的子串在源串中出现的位置索引值, 若查找不到则返回-1
	 */
	public static int find(String source, String sub, int crash) {
		if (source != null && sub != null) {
			int times = 0, index = 0, len = sub.length();
			if (crash > 0) {
				while (times++ < crash) {
					if (times == 1) {
						index = source.indexOf(sub);
					} else {
						index = source.indexOf(sub, index + len);
					}
					if (index == -1) {
						break;
					}
				}
				if (times > crash) {
					return index;
				}
			}
			if (crash < 0) {
				while (times-- > crash) {
					if (times == -1) {
						index = source.lastIndexOf(sub);
					} else {
						index = source.lastIndexOf(sub, index - len);
					}
					if (index == -1) {
						break;
					}
				}
				if (times < crash) {
					return index;
				}
			}
		}
		return -1;
	}

	/**
	 * 向左截取字串
	 * 
	 * @param source
	 *            源字串
	 * @param sub
	 *            子串
	 * 
	 * @return 返回子串在源字串中第一次出现的位置起向左的全部字符串(不包含子串), 若没有, 则返回 null
	 */
	public static String left(String source, String sub) {
		return left(source, sub, 1);
	}

	/**
	 * 向左截取字串
	 * 
	 * @param source
	 *            源字串
	 * @param sub
	 *            子串
	 * @param crash
	 *            默认值是1, 表示在源字符串中从左至右开始查找子串并在第一次发现时返回; 该值可以是负数, 如-1,
	 *            表示在源串中从右至左开始查找
	 * 
	 * @return 返回子串在源字串中出现的位置起向左的全部字符串(不包含子串), 若没有, 则返回 null
	 */
	public static String left(String source, String sub, int crash) {
		int index = find(source, sub, crash);
		return index != -1 ? source.substring(0, index) : null;
	}

	/**
	 * 向右截取字串
	 * 
	 * @param source
	 *            源字串
	 * @param sub
	 *            子串
	 * 
	 * @return 返回子串在源字串中第一次出现的位置起向右的全部字符串(不包含子串), 若没有, 则返回 null
	 */
	public static String right(String source, String sub) {
		return right(source, sub, 1);
	}

	/**
	 * 向右截取字串
	 * 
	 * @param source
	 *            源字串
	 * @param sub
	 *            子串
	 * @param crash
	 *            默认值是1, 表示在源字符串中从左至右开始查找子串并在第一次发现时返回; 该值可以是负数, 如-1,
	 *            表示在源串中从右至左开始查找
	 * 
	 * @return 返回子串在源字串中出现的位置起向右的全部字符串(不包含子串), 若没有, 则返回 null
	 */
	public static String right(String source, String sub, int crash) {
		int index = find(source, sub, crash);
		return index != -1 ? source.substring(index + sub.length()) : null;
	}

	/**
	 * 分割字符串
	 * 
	 * @param source
	 *            源字串
	 * @param begin
	 *            开始索引, 可以是负数, 表示从右往左运算
	 * @return
	 */
	public static String substring(String source, int begin) {
		return substring(source, begin, source.length());
	}

	/**
	 * 分割字符串
	 * 
	 * @param source
	 *            源字串
	 * @param begin
	 *            开始索引, 可以是负数, 表示从右往左运算
	 * @param end
	 *            结束索引, 可以是负数, 表示从右往左运算
	 * @return
	 */
	public static String substring(String source, int begin, int end) {
		if (source != null) {
			int len = source.length();
			if (begin < 0) {
				begin += len;
			}
			if (end < 0) {
				end += len;
			}
			return source.substring(begin, end);
		}
		return null;
	}

	/**
	 * 统计子串在源串中出现的次数
	 * 
	 * @param source
	 *            源字串
	 * @param sub
	 *            子串
	 * 
	 * @return 只要源字串或子串其中的一个为 null 返回 -1
	 */
	public static int count(String source, String sub) {
		return source != null && sub != null ? (source.length() - source.replace(sub, "").length()) / sub.length() : -1;
	}

	/**
	 * 首字母大写
	 * 
	 * @param str
	 *            源字串
	 * @return
	 */
	public static String capitalize(String str) {
		if (isNotBlank(str)) {
			char first = str.charAt(0);
			if (!Character.isTitleCase(first)) {
				return Character.toUpperCase(first) + str.substring(1);
			}
		}
		return str;
	}
	
	/**
	 * 首字母小写
	 * 
	 * @param str
	 *            源字串
	 * @return
	 */
	public static String uncapitalize(String str) {
		if (isNotBlank(str)) {
			char first = str.charAt(0);
			if (!Character.isLowerCase(first)) {
				return Character.toLowerCase(first) + str.substring(1);
			}
		}
		return str;
	}
	
}