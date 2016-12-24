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
     * <p>
     * StrUtils.isEmpty("") // true
     * <p>
     * StrUtils.isEmpty(null) // true
     * <p>
     * StrUtils.isEmpty(" ") // false
     * <p>
     * StrUtils.isEmpty("abc") // false
     *
     * @param str 源字串
     * @return 字符串若为 null 或长度为 0, 则返回 true, 否则返回 false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断是否为非空字符串
     * <p>
     * StrUtils.isEmpty("") // false
     * <p>
     * StrUtils.isEmpty(null) // false
     * <p>
     * StrUtils.isEmpty(" ") // true
     * <p>
     * StrUtils.isEmpty("abc") // true
     *
     * @param str 源字串
     * @return 字符串若不为 null 或长度不为 0, 则返回 true, 否则返回 false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断是否为空白字符串
     * <p>
     * StrUtils.isBlank("") // true
     * <p>
     * StrUtils.isBlank(null) // true
     * <p>
     * StrUtils.isBlank(" ") // true
     * <p>
     * StrUtils.isBlank("abc") // false
     *
     * @param str 源字串
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
     * <p>
     * StrUtils.isBlank("") // false
     * <p>
     * StrUtils.isBlank(null) // false
     * <p>
     * StrUtils.isBlank(" ") // false
     * <p>
     * StrUtils.isBlank("abc") // true
     *
     * @param str 源字串
     * @return 字符串若为非空串, 且至少有一个非空白字符(空格,段落,回车,制表符等), 则返回 true, 否则返回 false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 编码字符串
     *
     * @param str 源字串
     * @return 返回 UTF8 编码后的字符串
     */
    public static String encode(String str) {
        return encode(str, "UTF-8");
    }

    /**
     * 编码字符串
     *
     * @param str     源字串
     * @param charset 编码字符集, 默认使用 UTF-8
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
     * @param str 源字串
     * @return 返回 UTF-8 解码后的字符串
     */
    public static String decode(String str) {
        return decode(str, "UTF-8");
    }

    /**
     * 解码字符串
     *
     * @param str     源字串
     * @param charset 编码字符集, 默认使用 UTF-8
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
     * <p>
     * StrUtils.isAlpha("") // false
     * <p>
     * StrUtils.isAlpha(null) // false
     * <p>
     * StrUtils.isAlpha(" ") // false
     * <p>
     * StrUtils.isAlpha("abc123") // false
     * <p>
     * StrUtils.isAlpha("abc") // true
     *
     * @param str 源字串
     * @return
     */
    public static boolean isAlpha(String str) {
        return str != null ? str.matches("^[a-zA-Z]+$") : false;
    }

    /**
     * 若字符串不为 null 且所有字符都是数字则返回 true, 否则返回 false
     * <p>
     * StrUtils.isNumeric("") // false
     * <p>
     * StrUtils.isNumeric(null) // false
     * <p>
     * StrUtils.isNumeric(" ") // false
     * <p>
     * StrUtils.isNumeric("abc123") // false
     * <p>
     * StrUtils.isNumeric("123") // true
     *
     * @param str 源字串
     * @return
     */
    public static boolean isNumeric(String str) {
        return str != null ? str.matches("^[0-9]+$") : false;
    }

    /**
     * 返回一个以','(逗号)或';'(分号)切割成的数组
     * <p>
     * StrUtils.asArray("aa,bb,cc") // [aa, bb, cc]
     * <p>
     * StrUtils.asArray("aa;bb;cc") // [aa, bb, cc]
     * <p>
     * StrUtils.asArray("aabbcc") // [aabbcc]
     *
     * @param str 源字串
     * @return 若字符串为空串则返回 null
     */
    public static String[] asArray(String str) {
        return isNotBlank(str) ? str.trim().split("[,;]") : null;
    }

    /**
     * 返回一个以','(逗号)或';'(分号)切割成的列表
     * <p>
     * StrUtils.asList("aa,bb,cc") // [aa, bb, cc]
     * <p>
     * StrUtils.asList("aa;bb;cc") // [aa, bb, cc]
     * <p>
     * StrUtils.asList("aabbcc") // [aabbcc]
     *
     * @param str 源字串
     * @return 若字符串为空串则返回 null
     */
    public static List<String> asList(String str) {
        String[] items = asArray(str);
        return items != null ? Arrays.asList(items) : null;
    }

    /**
     * 查找字符串
     * <p>
     * StrUtils.find("HongKong", "ng") // 2
     * <p>
     * StrUtils.find("HongKong", "ing") // -1
     *
     * @param source 源字串
     * @param sub    查找的子串
     * @return 返回查找的子串在源串中第一次出现的位置索引值, 若查找不到则返回-1
     */
    public static int find(String source, String sub) {
        return find(source, sub, 1);
    }

    /**
     * 查找字符串
     * <p>
     * StrUtils.find("HongKong", "ng", 1) // 2
     * <p>
     * StrUtils.find("HongKong", "ng", -1) // 6
     * <p>
     * StrUtils.find("HongKong", "ing") // -1
     *
     * @param source 源字串
     * @param sub    查找的子串
     * @param index  默认值是1, 表示在源字符串中从左至右开始查找子串并在第一次发现时返回; 该值可以是负数, 如-1,
     *               表示在源串中从右至左开始查找
     * @return 返回查找的子串在源串中出现的位置索引值, 若查找不到则返回-1
     */
    public static int find(String source, String sub, int index) {
        if (source != null && sub != null) {
            int times = 0, pos = 0, len = sub.length();
            if (index > 0) {
                while (times++ < index) {
                    if (times == 1) {
                        pos = source.indexOf(sub);
                    } else {
                        pos = source.indexOf(sub, pos + len);
                    }
                    if (pos == -1) {
                        break;
                    }
                }
                if (times > index) {
                    return pos;
                }
            }
            if (index < 0) {
                while (times-- > index) {
                    if (times == -1) {
                        pos = source.lastIndexOf(sub);
                    } else {
                        pos = source.lastIndexOf(sub, pos - len);
                    }
                    if (pos == -1) {
                        break;
                    }
                }
                if (times < index) {
                    return pos;
                }
            }
        }
        return -1;
    }

    /**
     * 向左截取字串
     * <p>
     * StrUtils.substringLeft("HongKong", "ng") // Ho
     * <p>
     * StrUtils.substringLeft("HongKong", "ing") // null
     *
     * @param source 源字串
     * @param sub    子串
     * @return 返回子串在源字串中第一次出现的位置起向左的全部字符串(不包含子串), 若没有, 则返回 null
     */
    public static String substringLeft(String source, String sub) {
        return substringLeft(source, sub, 1);
    }

    /**
     * 向左截取字串
     * <p>
     * StrUtils.substringLeft("HongKong", "ng", 1) // Ho
     * <p>
     * StrUtils.substringLeft("HongKong", "ng", 2) // HongKo
     * <p>
     * StrUtils.substringLeft("HongKong", "ng", -1) // HongKo
     * <p>
     * StrUtils.substringLeft("HongKong", "ing", 1) // null
     *
     * @param source 源字串
     * @param sub    子串
     * @param index  默认值是1, 表示在源字符串中从左至右开始查找子串并在第一次发现时返回; 该值可以是负数, 如-1,
     *               表示在源串中从右至左开始查找
     * @return 返回子串在源字串中出现的位置起向左的全部字符串(不包含子串), 若没有, 则返回 null
     */
    public static String substringLeft(String source, String sub, int index) {
        int pos = find(source, sub, index);
        return pos != -1 ? source.substring(0, pos) : null;
    }

    /**
     * 向右截取字串
     * <p>
     * StrUtils.substringRight("HongKong", "ng") // Kong
     * <p>
     * StrUtils.substringRight("HongKong", "ing") // null
     *
     * @param source 源字串
     * @param sub    子串
     * @return 返回子串在源字串中第一次出现的位置起向右的全部字符串(不包含子串), 若没有, 则返回 null
     */
    public static String substringRight(String source, String sub) {
        return substringRight(source, sub, 1);
    }

    /**
     * 向右截取字串
     * <p>
     * StrUtils.substringRight("HongKong", "ng", 1) // Kong
     * <p>
     * StrUtils.substringRight("HongKong", "ng", 2) // ""
     * <p>
     * StrUtils.substringRight("HongKong", "ng", -1) // ""
     * <p>
     * StrUtils.substringRight("HongKong", "ing", 1) // null
     *
     * @param source 源字串
     * @param sub    子串
     * @param index  默认值是1, 表示在源字符串中从左至右开始查找子串并在第一次发现时返回; 该值可以是负数, 如-1,
     *               表示在源串中从右至左开始查找
     * @return 返回子串在源字串中出现的位置起向右的全部字符串(不包含子串), 若没有, 则返回 null
     */
    public static String substringRight(String source, String sub, int index) {
        int pos = find(source, sub, index);
        return pos != -1 ? source.substring(pos + sub.length()) : null;
    }

    /**
     * 分割字符串
     * <p>
     * StrUtils.substring("HongKong", 3) // gKong
     * <p>
     * StrUtils.substring("HongKong", -3) // Kong
     *
     * @param source 源字串
     * @param begin  开始索引, 可以是负数, 表示从右往左运算
     * @return
     */
    public static String substring(String source, int begin) {
        return substring(source, begin, source.length());
    }

    /**
     * 分割字符串
     * <p>
     * StrUtils.substring("HongKong", 3, 8) // gKong
     * <p>
     * StrUtils.substring("HongKong", 3, -1) // gKon
     *
     * @param source 源字串
     * @param begin  开始索引, 可以是负数, 表示从右往左运算
     * @param end    结束索引, 可以是负数, 表示从右往左运算
     * @return
     */
    public static String substring(String source, int begin, int end) {
        if (source != null) {
            int len = source.length();
            if (begin < 0) {
                begin += len - 1;
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
     * <p>
     * StrUtils.count("HongKong", "ong") // 2
     * <p>
     * StrUtils.count("HongKong", "ing") // 0
     *
     * @param source 源字串
     * @param sub    子串
     * @return 只要源字串或子串其中的一个为 null 返回 -1
     */
    public static int count(String source, String sub) {
        return source != null && sub != null ? (source.length() - source.replace(sub, "").length()) / sub.length() : -1;
    }

    /**
     * 首字母大写
     * <p>
     * StrUtils.capitalize("hongKong") // HongKong
     * <p>
     * StrUtils.capitalize("HongKong") // HongKong
     * <p>
     * StrUtils.capitalize("hongkong") // Hongkong
     *
     * @param str 源字串
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
     * <p>
     * StrUtils.uncapitalize("hongKong") // hongKong
     * <p>
     * StrUtils.uncapitalize("HongKong") // hongKong
     * <p>
     * StrUtils.uncapitalize("hongkong") // hongkong
     *
     * @param str 源字串
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