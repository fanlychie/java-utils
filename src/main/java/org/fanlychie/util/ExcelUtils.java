package org.fanlychie.util;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Excel 工具类
 *
 * @author fanlychie
 */
public final class ExcelUtils {

    /**
     * 返回一个可写的 Excel 对象
     *
     * @param list 将要写出的数据集合
     * @return WritableExcel
     */
    public static WritableExcel write(List<?> list) {
        return new WritableExcel(list);
    }

    /**
     * 可写的 Excel
     */
    public static class WritableExcel {

        // 数据
        private List<?> data;

        // 字段
        private String[] fields;

        // 别名
        private String[] aliases;

        // 单元格宽度
        private int width;

        // 标题行高
        private int titleHeight = 28;

        // 内容行高
        private int contentHeight = 24;

        // 工作表名称
        private String sheetName = "Sheet1";

        // 日期格式
        private String dateFormat;

        // 可缓存的类
        private CacheableClass cacheableClass;

        // 布尔类型对照表
        private Map<Boolean, String> boolStrMap;

        // 格式对照表
        private static final Map<Class<?>, String> FORMAT;

        // 工作薄
        private XSSFWorkbook workbook = new XSSFWorkbook();

        WritableExcel(List<?> data) {
            this.data = data;
            this.setWidth(18);
            this.setBoolStrMapping("是", "否");
            this.cacheableClass = new CacheableClass(data.get(0).getClass());
        }

        /**
         * 写出到文件
         *
         * @param file 文件对象
         */
        public boolean to(File file) {
            try {
                return to(new FileOutputStream(file));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 写出到输出流
         *
         * @param out 输出流
         */
        public boolean to(OutputStream out) {
            try {
                // 创建工作表
                XSSFSheet sheet = workbook.createSheet(sheetName);
                // 创建标题行
                createTitleRow(sheet);
                // 内容行索引
                int index = 1;
                for (Object item : data) {
                    // 创建内容行
                    createContentRow(sheet, index++, item);
                }
                workbook.write(out);
                return true;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                }
            }
        }

        /**
         * 设置哪些字段的数据被写出到Excel, 字段的中文名称(即Excel标题行的文字)用 setAliases 来设置, 先后顺序即Excel标题行从左到右的顺序
         *
         * @param fields 字段集合
         * @return
         */
        public WritableExcel setFields(String... fields) {
            this.fields = fields;
            return this;
        }

        /**
         * 设置写出的字段的别名(即Excel标题行的文字), 顺序需与 setFields 相对应
         *
         * @param aliases 别名集合
         * @return
         */
        public WritableExcel setAliases(String... aliases) {
            this.aliases = aliases;
            return this;
        }

        /**
         * 设置单元格的宽度
         *
         * @param width 宽度
         * @return
         */
        public WritableExcel setWidth(int width) {
            this.width = width * 256 + 184;
            return this;
        }

        /**
         * 设置标题行行高
         *
         * @param titleHeight 标题行行高
         * @return
         */
        public WritableExcel setTitleHeight(int titleHeight) {
            this.titleHeight = titleHeight;
            return this;
        }

        /**
         * 设置内容行行高
         *
         * @param contentHeight 内容行行高
         * @return
         */
        public WritableExcel setContentHeight(int contentHeight) {
            this.contentHeight = contentHeight;
            return this;
        }

        /**
         * 设置工作表的名称, 默认为 Sheet1
         *
         * @param sheetName 工作表的名称
         * @return
         */
        public WritableExcel setSheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        /**
         * 设置日期格式, 默认为 yyyy-MM-dd
         *
         * @param dateFormat 日期格式
         * @return
         */
        public WritableExcel setDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        /**
         * 设置布尔类型值的字符串映射
         *
         * @param trueStr  true值的映射
         * @param falseStr false值的映射
         */
        public void setBoolStrMapping(String trueStr, String falseStr) {
            Map<Boolean, String> map = new HashMap<>();
            map.put(true, trueStr);
            map.put(false, falseStr);
            this.boolStrMap = map;
        }

        /**
         * 创建标题行
         *
         * @throws Throwable
         */
        private void createTitleRow(XSSFSheet sheet) throws Throwable {
            // 创建一行
            XSSFRow row = sheet.createRow(0);
            // 设置行高
            row.setHeightInPoints(titleHeight);
            // 单元格样式
            CellStyle style = workbook.createCellStyle();
            // 水平居中
            style.setAlignment(CellStyle.ALIGN_CENTER);
            // 垂直居中
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            // 背景颜色
            setBackgroundColor(style, IndexedColors.YELLOW.index);
            // 字体
            style.setFont(createFont(12, IndexedColors.BLUE_GREY.index));
            // 数据格式
            style.setDataFormat(workbook.createDataFormat().getFormat("GENERAL"));
            // 自动换行
            style.setWrapText(true);
            // 别名作为标题
            for (int i = 0; i < aliases.length; i++) {
                // 设置宽度
                sheet.setColumnWidth(i, width);
                // 创建单元格
                XSSFCell cell = row.createCell(i);
                // 设置单元格样式
                cell.setCellStyle(style);
                // 设置单元格的值
                cell.setCellValue(aliases[i]);
            }
        }

        /**
         * 创建内容行
         *
         * @param index 行的索引
         * @param bean  填充行的对象
         * @throws Throwable
         */
        private void createContentRow(XSSFSheet sheet, int index, Object bean) throws Throwable {
            // 创建一行
            XSSFRow row = sheet.createRow(index);
            // 设置行高
            row.setHeightInPoints(contentHeight);
            // 迭代 Bean 属性
            for (int i = 0; i < fields.length; i++) {
                // 属性的值
                Object value = cacheableClass.getFieldValue(bean, fields[i]);
                // 属性类型
                Class<?> type = cacheableClass.getFieldType(fields[i]);
                // 创建单元格
                XSSFCell cell = row.createCell(i);
                // 单元格样式
                CellStyle style = workbook.createCellStyle();
                // 单元格数据格式
                String format = FORMAT.get(type);
                if (type == Date.class && dateFormat != null) {
                    format = dateFormat;
                } else if (format == null || value == null) {
                    format = FORMAT.get(String.class);
                }
                // 水平居中
                style.setAlignment(CellStyle.ALIGN_CENTER);
                // 垂直居中
                style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
                // 背景颜色
                setBackgroundColor(style, IndexedColors.LIGHT_TURQUOISE.index);
                // 字体
                style.setFont(createFont(11, IndexedColors.GREY_50_PERCENT.index));
                // 数据格式
                style.setDataFormat(workbook.createDataFormat().getFormat(format));
                // 自动换行
                style.setWrapText(true);
                // 按类型设值
                if (value == null) {
                    cell.setCellValue("");
                } else if (type == Boolean.TYPE || type == Boolean.class) {
                    cell.setCellValue(boolStrMap.get(Boolean.parseBoolean(value.toString())));
                } else if ((Number.class.isAssignableFrom(type) || type.isPrimitive()) && type != Byte.TYPE && type != Character.TYPE) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else if (type == Date.class) {
                    cell.setCellValue((Date) value);
                } else {
                    cell.setCellValue(value.toString());
                }
                // 单元格样式
                cell.setCellStyle(style);
            }
        }

        /**
         * 设置背景颜色
         *
         * @param style 单元格样式
         * @param color 颜色值
         */
        private void setBackgroundColor(CellStyle style, short color) {
            // 边框设置
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderTop(CellStyle.BORDER_THIN);
            // 边框颜色
            style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.index);
            style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.index);
            style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.index);
            style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.index);
            // 背景颜色
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            style.setFillForegroundColor(color);
        }

        /**
         * 创建字体
         *
         * @param fontSize  字体大小
         * @param fontColor 字体颜色
         * @return Font
         */
        private Font createFont(int fontSize, short fontColor) {
            // 创建字体
            Font font = workbook.createFont();
            // 字体颜色
            font.setColor(fontColor);
            // 字体大小
            font.setFontHeightInPoints((short) fontSize);
            // 字体名称
            if (System.getProperty("os.name").contains("Windows")) {
                font.setFontName("Microsoft YaHei");
            }
            return font;
        }

        static {
            FORMAT = new HashMap<>();
            FORMAT.put(Short.TYPE, "0");
            FORMAT.put(Short.class, "0");
            FORMAT.put(Integer.TYPE, "0");
            FORMAT.put(Integer.class, "0");
            FORMAT.put(Long.TYPE, "0");
            FORMAT.put(Long.class, "0");
            FORMAT.put(Float.TYPE, "0.00");
            FORMAT.put(Float.class, "0.00");
            FORMAT.put(Double.TYPE, "0.00");
            FORMAT.put(Double.class, "0.00");
            FORMAT.put(String.class, "GENERAL");
            FORMAT.put(Date.class, "yyyy-MM-dd");
        }

    }

    /**
     * 可缓存的类
     */
    private static class CacheableClass {

        /**
         * 类
         */
        private Class<?> clazz;

        /**
         * 锁
         */
        private static final Lock lock = new ReentrantLock();

        /**
         * 缓存类的元数据信息
         */
        private static final Map<Class<?>, Map<String, Field>> CLASS_METADATA_MAP = new HashMap<>();

        private CacheableClass(Class<?> clazz) {
            this.clazz = clazz;
            initMetadataToMemory();
        }

        /**
         * 设置字段的值
         *
         * @param obj   实例对象
         * @param field 字段名称
         * @param value 字段的值
         */
        private void setFieldValue(Object obj, String field, Object value) {
            try {
                CLASS_METADATA_MAP.get(clazz).get(field).set(obj, value);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 获取字段的值
         *
         * @param obj   实例对象
         * @param field 字段名称
         * @return
         */
        private <T> T getFieldValue(Object obj, String field) {
            try {
                return (T) CLASS_METADATA_MAP.get(clazz).get(field).get(obj);
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 获取字段的类型
         *
         * @param field 字段名称
         * @return
         */
        private Class<?> getFieldType(String field) {
            return CLASS_METADATA_MAP.get(clazz).get(field).getType();
        }

        /**
         * 缓存类的元数据到内存
         */
        private void initMetadataToMemory() {
            lock.lock();
            try {
                Class<?> currClass = clazz;
                // 只处理还没有缓存过的类
                if (!CLASS_METADATA_MAP.containsKey(currClass)) {
                    // 字段对照表
                    Map<String, Field> fieldMap = new HashMap<>();
                    do {
                        for (Field field : currClass.getDeclaredFields()) {
                            if (!Modifier.isStatic(field.getModifiers())) {
                                if (!fieldMap.containsKey(field.getName())) {
                                    fieldMap.put(field.getName(), field);
                                    field.setAccessible(true);
                                }
                            }
                        }
                    } while ((currClass = currClass.getSuperclass()) != null);
                    // 内存缓存
                    CLASS_METADATA_MAP.put(clazz, fieldMap);
                }
            } finally {
                lock.unlock();
            }
        }

    }

}