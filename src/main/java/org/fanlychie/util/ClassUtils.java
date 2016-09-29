package org.fanlychie.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;

/**
 * 字节码操作工具类
 * 
 * @author fanlychie
 */
@SuppressWarnings("unchecked")
public final class ClassUtils {
	
	/**
	 * 字段域索引值
	 */
	private static final int FIELD = 0;
	
	/**
	 * 方法域索引值
	 */
	private static final int METHOD = 1;
	
	/**
	 * 锁
	 */
	private static final Lock lock = new ReentrantLock();
	
	/**
	 * 类文件后缀
	 */
	private static final String CLASS_FILE_SUFFIX = ".class";
	
	/**
	 * 缓存类的元数据信息
	 */
	private static final Map<Class<?>, Map<?, ?>[]> CLASS_METADATA_MAP = new HashMap<>();
	
	/**
	 * 基本数据类型和对应的包装类型表
	 */
	private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap<>();
	
	static {
		PRIMITIVE_WRAPPER_MAP.put(Byte.TYPE, Byte.class);
		PRIMITIVE_WRAPPER_MAP.put(Short.TYPE, Short.class);
		PRIMITIVE_WRAPPER_MAP.put(Integer.TYPE, Integer.class);
		PRIMITIVE_WRAPPER_MAP.put(Long.TYPE, Long.class);
		PRIMITIVE_WRAPPER_MAP.put(Float.TYPE, Float.class);
		PRIMITIVE_WRAPPER_MAP.put(Double.TYPE, Double.class);
		PRIMITIVE_WRAPPER_MAP.put(Boolean.TYPE, Boolean.class);
		PRIMITIVE_WRAPPER_MAP.put(Character.TYPE, Character.class);
	}
	
	/**
	 * 修饰符
	 */
	public static enum Modifier {

		/**
		 * 全部修饰符
		 */
		WHOLE,

		/**
		 * 静态修饰符
		 */
		STATIC,

		/**
		 * 非静态修饰符
		 */
		NON_STATIC
		
	}
	
	/**
	 * 扫描加载类文件, 支持 JAR 包
	 * 
	 * @param pack
	 *            扫描的包名称, 该包下的子包也会被扫描到
	 * 
	 * @return 返回扫描加载完成的类列表
	 */
	public static List<Class<?>> loadClasses(String pack) {
		try {
			URL url = Thread.currentThread().getContextClassLoader().getResource(swapSeparator(pack));
			if (url == null) {
				throw new RuntimeException("can not found '" + pack + "' in the classpath.");
			}
			if (url.getPath().contains(".jar!")) {
				return loadClassesFromJarFile(url, pack);
			}
			else {
				return loadClassesFromUsrFile(new File(url.getPath()), pack);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 获取类声明的字段列表
	 * 
	 * @param clazz
	 *            类
	 * @param modifier
	 *            字段修饰符 {@link Modifier}
	 * @param backtrack
	 *            是否回溯到父类查找
	 * @return
	 */
	public static List<Field> getDeclaredFields(Class<?> clazz, Modifier modifier, boolean backtrack) {
		if (backtrack) {
			// 链表, 当前类字段先进, 父类字段后进
			List<Field> list = new LinkedList<>();
			do {
				list.addAll(loadDeclaredFields(clazz, modifier));
			} while ((clazz = clazz.getSuperclass()) != null);
			return list;
		}
		else {
			return loadDeclaredFields(clazz, modifier);
		}
	}
	
	/**
	 * 获取类声明的方法列表
	 * 
	 * @param clazz
	 *            类
	 * @param modifier
	 *            方法修饰符 {@link Modifier}
	 * @param backtrack
	 *            是否回溯到父类查找
	 * @return
	 */
	public static List<Method> getDeclaredMethods(Class<?> clazz, Modifier modifier, boolean backtrack) {
		if (backtrack) {
			// 链表, 当前类方法先进, 父类方法后进
			List<Method> list = new LinkedList<>();
			do {
				list.addAll(loadDeclaredMethods(clazz, modifier));
			} while ((clazz = clazz.getSuperclass()) != null);
			return list;
		}
		else {
			return loadDeclaredMethods(clazz, modifier);
		}
	}
	
	/**
	 * 缓存类的元数据到内存
	 * 
	 * @param clazz
	 *            类
	 */
	public static void cacheMetadataToMemory(Class<?> clazz) {
		lock.lock();
		try {
			// 只处理还没有缓存过的类
			if (!CLASS_METADATA_MAP.containsKey(clazz)) {
				// 处理类的字段列表
				List<Field> fields = getDeclaredFields(clazz, Modifier.WHOLE, true);
				Map<String, Field> fieldMap = new HashMap<>();
				for (Field field : fields) {
					// 丢弃名字重复的字段, 名字重复的字段必来自父类
					if (!fieldMap.containsKey(field.getName())) {
						fieldMap.put(field.getName(), field);
					}
				}
				// 处理类的方法列表
				List<Method> methods = getDeclaredMethods(clazz, Modifier.WHOLE, true);
				Map<String, Map<String, Method>> methodMap = new HashMap<>();
				for (Method method : methods) {
					String name = method.getName();
					String type = Arrays.toString(method.getParameterTypes());
					// 处理重载的方法
					if (methodMap.containsKey(method.getName())) {
						Map<String, Method> value = methodMap.get(name);
						// 丢弃签名相同的方法, 签名相同的方法必来自父类
						if (!value.containsKey(type)) {
							value.put(type, method);
						}
					}
					else {
						Map<String, Method> value = new HashMap<>();
						value.put(type, method);
						methodMap.put(name, value);
					}
				}
				// 内存缓存
				CLASS_METADATA_MAP.put(clazz, new Map[]{fieldMap, methodMap});
			}
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 缓存列表类的元数据到内存
	 * 
	 * @param classes
	 *            类列表
	 */
	public static void cacheMetadataToMemory(List<Class<?>> classes) {
		for (Class<?> clazz : classes) {
			cacheMetadataToMemory(clazz);
		}
	}
	
	/**
	 * 缓存指定包(含子包)下的所有类的元数据到内存
	 * 
	 * @param pack
	 *            扫描的包名字
	 */
	public static void cacheMetadataToMemory(String pack) {
		cacheMetadataToMemory(loadClasses(pack));
	}
	
	/**
	 * 设置字段的值
	 * 
	 * @param obj
	 *            对于实例变量(非静态字段), 此为实例对象; 对于类变量(静态字段), 此可为类对象(Class对象)
	 * @param field
	 *            字段
	 * @param value
	 *            字段的值
	 */
	public static void setFieldValue(Object obj, Field field, Object value) {
		try {
			field.set(obj, value);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取字段的值
	 * 
	 * @param obj
	 *            对于实例变量(非静态字段), 此为实例对象; 对于类变量(静态字段), 此可为类对象(Class对象)
	 * @param field
	 *            字段
	 * @return
	 */
	public static <T> T getFieldValue(Object obj, Field field) {
		try {
			return (T) field.get(obj);
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 设置字段的值.
	 * 
	 * 若此次操作的类(Class)不在内置的CLASS_METADATA_MAP内存缓存中, 则将其载入CLASS_METADATA_MAP缓存;
	 * 
	 * 若此次操作的类(Class)已存在于CLASS_METADATA_MAP缓存中, 则直接从CLASS_METADATA_MAP缓存中获取使用;
	 * 
	 * @param obj
	 *            对于实例变量(非静态字段), 此为实例对象; 对于类变量(静态字段), 此可为类对象(Class对象)
	 * @param field
	 *            字段
	 * @param value
	 *            字段的值
	 * @param value
	 */
	public static void setFieldValueQuickly(Object obj, String field, Object value) {
		Class<?> clazz = null;
		if (obj instanceof Class) {
			clazz = (Class<?>) obj;
		}
		else {
			clazz = obj.getClass();
		}
		setFieldValue(obj, getFieldFromCache(clazz, field), value);
	}

	/**
	 * 获取字段的值.
	 * 
	 * 若此次操作的类(Class)不在内置的CLASS_METADATA_MAP内存缓存中, 则将其载入CLASS_METADATA_MAP缓存;
	 * 
	 * 若此次操作的类(Class)已存在于CLASS_METADATA_MAP缓存中, 则直接从CLASS_METADATA_MAP缓存中获取使用;
	 * 
	 * @param obj
	 *            对于实例变量(非静态字段), 此为实例对象; 对于类变量(静态字段), 此可为类对象(Class对象)
	 * @param field
	 *            字段
	 * @return
	 */
	public static <T> T getFieldValueQuickly(Object obj, String field) {
		Class<?> clazz = null;
		if (obj instanceof Class) {
			clazz = (Class<?>) obj;
		}
		else {
			clazz = obj.getClass();
		}
		return getFieldValue(obj, getFieldFromCache(clazz, field));
	}
	
	/**
	 * 获取字段的类型.
	 * 
	 * 若此次操作的类(Class)不在内置的CLASS_METADATA_MAP内存缓存中, 则将其载入CLASS_METADATA_MAP缓存;
	 * 
	 * 若此次操作的类(Class)已存在于CLASS_METADATA_MAP缓存中, 则直接从CLASS_METADATA_MAP缓存中获取使用;
	 * 
	 * @param clazz
	 *            类
	 * @param field
	 *            字段
	 * @return
	 */
	public static Class<?> getFieldTypeQuickly(Class<?> clazz, String field) {
		return getFieldFromCache(clazz, field).getType();
	}
	
	/**
	 * 调用方法
	 * 
	 * @param obj
	 *            对于实例方法(非静态方法), 此为实例对象; 对于类方法(静态方法), 此可为类对象(Class对象)
	 * @param method
	 *            方法
	 * @param values
	 *            方法的参数值列表
	 * @return
	 */
	public static <T> T invokeMethod(Object obj, Method method, Object[] values) {
		try {
			return (T) method.invoke(obj, values);
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 调用方法, 由于缺省参数的类型列表, 使用范围比较受限, 以下几种场景可调用：
	 * 
	 * 1. 此方法没有参数;
	 * 
	 * 2. 只存在一个以此命名的方法, 即没有重载的方法;
	 * 
	 * 若此次操作的类(Class)不在内置的CLASS_METADATA_MAP内存缓存中, 则将其载入CLASS_METADATA_MAP缓存;
	 * 
	 * 若此次操作的类(Class)已存在于CLASS_METADATA_MAP缓存中, 则直接从CLASS_METADATA_MAP缓存中获取使用;
	 * 
	 * @param obj
	 *            对于实例方法(非静态方法), 此为实例对象; 对于类方法(静态方法), 此可为类对象(Class对象)
	 * @param method
	 *            方法名称
	 * @param argValues
	 *            方法的参数值列表
	 * @return
	 */
	public static <T> T invokeMethodQuickly(Object obj, String method, Object[] argValues) {
		return invokeMethodQuickly(obj, method, argValues, null);
	}
	
	/**
	 * 调用方法.
	 * 
	 * 若此次操作的类(Class)不在内置的CLASS_METADATA_MAP内存缓存中, 则将其载入CLASS_METADATA_MAP缓存;
	 * 
	 * 若此次操作的类(Class)已存在于CLASS_METADATA_MAP缓存中, 则直接从CLASS_METADATA_MAP缓存中获取使用;
	 * 
	 * @param obj
	 *            对于实例方法(非静态方法), 此为实例对象; 对于类方法(静态方法), 此可为类对象(Class对象)
	 * @param method
	 *            方法名称
	 * @param argValues
	 *            方法的参数值列表
	 * @param argTypes
	 *            方法的参数的类型列表
	 * @return
	 */
	public static <T> T invokeMethodQuickly(Object obj, String method, Object[] argValues, Class<?>[] argTypes) {
		Class<?> clazz = null;
		if (obj instanceof Class) {
			clazz = (Class<?>) obj;
		}
		else {
			clazz = obj.getClass();
		}
		return invokeMethod(obj, getMethodFromCache(clazz, method, argTypes), argValues);
	}
	
	/**
	 * 创建一个类的实例
	 * 
	 * @param clazz
	 *            类
	 * @return
	 */
	public static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 拷贝对象属性, 当源对象中具有与目标对象相同的非静态属性(属性名相同, 且属性类型相同或互为装箱拆箱类型)
	 * 
	 * 时, 将此属性的值从源对象中拷贝到目标对象中.
	 * 
	 * 若此次操作的类(Class)不在内置的CLASS_METADATA_MAP内存缓存中, 则将其载入CLASS_METADATA_MAP缓存;
	 * 
	 * 若此次操作的类(Class)已存在于CLASS_METADATA_MAP缓存中, 则直接从CLASS_METADATA_MAP缓存中获取使用;
	 * 
	 * @param src
	 *            源对象
	 * @param dest
	 *            目标对象
	 * @param acceptNull
	 *            是否拷贝 null 值属性
	 */
	public static void copyProperties(Object src, Object dest, boolean acceptNull) {
		Map<String, Field> sFieldMap = getFieldMapFromCache(src.getClass());
		Map<String, Field> dFieldMap = getFieldMapFromCache(dest.getClass());
		for (String name : sFieldMap.keySet()) {
			Field sField = sFieldMap.get(name);
			// 静态字段永不参与拷贝
			if (java.lang.reflect.Modifier.isStatic(sField.getModifiers())) {
				continue ;
			}
			if (dFieldMap.containsKey(name)) {
				Field dField = dFieldMap.get(name);
				Class<?> sType = sField.getType();
				Class<?> dType = dField.getType();
				// 字段类型, 兼容基本数据类型和包装类型
				if (sType == dType 
						|| (sType.isPrimitive() && PRIMITIVE_WRAPPER_MAP.get(sType) == dType)
						|| (dType.isPrimitive() && PRIMITIVE_WRAPPER_MAP.get(dType) == sType)) {
					Object value = getFieldValue(src, sField);
					if (value == null && !acceptNull) {
						continue ;
					}
					else {
						setFieldValue(dest, dField, value);
					}
				}
			}
		}
	}
	
	/**
	 * 拷贝对象属性, 当源对象中具有与目标对象相同的非静态属性(属性名相同, 且属性类型相同或互为装箱拆箱类型)
	 * 
	 * 时, 将此属性的值从源对象中拷贝到目标对象中.
	 * 
	 * 若此次操作的类(Class)不在内置的CLASS_METADATA_MAP内存缓存中, 则将其载入CLASS_METADATA_MAP缓存;
	 * 
	 * 若此次操作的类(Class)已存在于CLASS_METADATA_MAP缓存中, 则直接从CLASS_METADATA_MAP缓存中获取使用;
	 * 
	 * @param src
	 *            源对象
	 * @param dest
	 *            目标对象
	 */
	public static void copyProperties(Object src, Object dest) {
		copyProperties(src, dest, true);
	}
	
	/**
	 * 对象转换, 将源对象转换成目标类的一个实例对象, 并将源对象的值拷贝到新生成的实例对象中.
	 * 
	 * 若此次操作的类(Class)不在内置的CLASS_METADATA_MAP内存缓存中, 则将其载入CLASS_METADATA_MAP缓存;
	 * 
	 * 若此次操作的类(Class)已存在于CLASS_METADATA_MAP缓存中, 则直接从CLASS_METADATA_MAP缓存中获取使用;
	 * 
	 * @param src
	 *            源对象
	 * @param destClass
	 *            目标类
	 * @return
	 */
	public static <T> T convert(Object src, Class<T> destClass) {
		T target = newInstance(destClass);
		copyProperties(src, target, false);
		return target;
	}
	
	/**
	 * 对象列表转换, 将源列表中的每个对象转换成目标类的一个实例对象, 并将源对象的值拷贝到新生成的实例对象中.
	 * 
	 * 若此次操作的类(Class)不在内置的CLASS_METADATA_MAP内存缓存中, 则将其载入CLASS_METADATA_MAP缓存;
	 * 
	 * 若此次操作的类(Class)已存在于CLASS_METADATA_MAP缓存中, 则直接从CLASS_METADATA_MAP缓存中获取使用;
	 * 
	 * @param collection
	 *            源列表
	 * @param destClass
	 *            目标类
	 * @return
	 */
	public static <T> List<T> convert(Collection<?> collection, Class<T> destClass) {
		List<T> list = new ArrayList<>();
		for (Object src : collection) {
			list.add(convert(src, destClass));
		}
		return list;
	}
	
	/**
	 * 加载类声明的字段列表
	 * 
	 * @param clazz
	 *            类
	 * @param modifier
	 *            修饰符
	 * @return
	 */
	private static List<Field> loadDeclaredFields(Class<?> clazz, Modifier modifier) {
		List<Field> list = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			switch (modifier) {
			case WHOLE:
				list.add(field);
				break;
			case STATIC:
				if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
					list.add(field);
				}
				break;
			case NON_STATIC:
				if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
					list.add(field);
				}
				break;
			}
			field.setAccessible(true);
		}
		return list;
	}
	
	/**
	 * 加载类声明的方法列表
	 * 
	 * @param clazz
	 *            类
	 * @param modifier
	 *            修饰符
	 * @return
	 */
	private static List<Method> loadDeclaredMethods(Class<?> clazz, Modifier modifier) {
		List<Method> list = new ArrayList<>();
		for (Method method : clazz.getDeclaredMethods()) {
			switch (modifier) {
			case WHOLE:
				list.add(method);
				break;
			case STATIC:
				if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
					list.add(method);
				}
				break;
			case NON_STATIC:
				if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
					list.add(method);
				}
				break;
			}
			method.setAccessible(true);
		}
		return list;
	}

	/**
	 * 从用户目录扫描加载类文件
	 * 
	 * @param dir
	 *            目录
	 * @param pack
	 *            包名
	 * 
	 * @return 返回扫描加载完成的类列表
	 * @throws Exception
	 */
	private static List<Class<?>> loadClassesFromUsrFile(File dir, String pack) throws Exception {
		List<Class<?>> list = new ArrayList<>();
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				list.addAll(loadClassesFromUsrFile(f, pack + "." + f.getName()));
			}
			else if (f.isFile() && f.getName().endsWith(CLASS_FILE_SUFFIX)) {
				list.add(Class.forName(pack + "." + f.getName().replace(CLASS_FILE_SUFFIX, "")));
			}
		}
		return list;
	}
	
	/**
	 * 从JAR文件扫描加载类文件
	 * 
	 * @param url
	 *            路径
	 * @param pack
	 *            包名
	 * 
	 * @return 返回扫描加载完成的类列表
	 * @throws Exception
	 */
	private static List<Class<?>> loadClassesFromJarFile(URL url, String pack) throws Exception {
		List<Class<?>> list = new ArrayList<>();
		JarURLConnection conn = (JarURLConnection) url.openConnection();
		Enumeration<JarEntry> e = conn.getJarFile().entries();
		String path = swapSeparator(pack);
		while (e.hasMoreElements()) {
			String pathname = e.nextElement().getName();
			if (pathname.startsWith(path) && pathname.endsWith(CLASS_FILE_SUFFIX)) {
				pathname = pathname.substring(0, pathname.indexOf(CLASS_FILE_SUFFIX));
				list.add(Class.forName(swapSeparator(pathname)));
			}
		}
		return list;
	}
	
	/**
	 * 分隔符转换
	 * 
	 * @param source
	 *            源字符串
	 * 
	 * @return 返回转换后的字符串
	 */
	private static String swapSeparator(String source) {
		if (source.contains(".")) {
			source = source.replace(".", "/");
		}
		else if (source.contains("/") || source.contains("\\")) {
			source = source.replaceAll("[/\\\\]", ".");
		}
		return source;
	}
	
	/**
	 * 从内存缓存中获取类的字段表信息, 若此类从未被载入缓存, 则载入缓存
	 * 
	 * @param clazz
	 *            类
	 * @return
	 */
	private static Map<String, Field> getFieldMapFromCache(Class<?> clazz) {
		if (!CLASS_METADATA_MAP.containsKey(clazz)) {
			cacheMetadataToMemory(clazz);
//			throw new UnsupportedOperationException("'" + clazz + "' can not be found in the cache.");
		}
		return (Map<String, Field>) CLASS_METADATA_MAP.get(clazz)[FIELD];
	}

	/**
	 * 从内存缓存中获取类的方法表信息, 若此类从未被载入缓存, 则载入缓存
	 * 
	 * @param clazz
	 *            类
	 * @return
	 */
	private static Map<String, Map<String, Method>> getMethodMapFromCache(Class<?> clazz) {
		if (!CLASS_METADATA_MAP.containsKey(clazz)) {
			cacheMetadataToMemory(clazz);
//			throw new UnsupportedOperationException("'" + clazz + "' can not be found in the cache.");
		}
		return (Map<String, Map<String, Method>>) CLASS_METADATA_MAP.get(clazz)[METHOD];
	}
	
	/**
	 * 从缓存中获取字段对象
	 * 
	 * @param clazz
	 *            类
	 * @param fieldName
	 *            字段名称
	 * @return Field
	 */
	private static Field getFieldFromCache(Class<?> clazz, String fieldName) {
		Field field = getFieldMapFromCache(clazz).get(fieldName);
		if (field == null) {
			throw new UnsupportedOperationException("No such field " + fieldName + " in the " + clazz);
		}
		return field;
	}
	
	/**
	 * 从缓存中获取方法对象
	 * 
	 * @param clazz
	 *            类
	 * @param methodName
	 *            方法名称
	 * @param argTypes
	 *            方法参数类型列表
	 * @return Method
	 */
	private static Method getMethodFromCache(Class<?> clazz, String methodName, Class<?>[] argTypes) {
		Map<String, Method> methodMap = getMethodMapFromCache(clazz).get(methodName);
		if (methodMap == null) {
			throw new UnsupportedOperationException("can not found any method is named " + methodName + " in the " + clazz);
		}
		Method method;
		if (methodMap.size() == 1) {
			method = methodMap.values().toArray(new Method[1])[0];
		}
		else {
			if (argTypes == null) {
				throw new UnsupportedOperationException("No such method " + methodName + "() in the " + clazz);
			}
			String type = Arrays.toString(argTypes);
			method = methodMap.get(type);
			if (method == null) {
				type = type.substring(1, type.length() - 1);
				type = type.replace("class ", "").replace("java.lang.", "");
				throw new UnsupportedOperationException("No such method " + methodName + "(" + type + ") in the " + clazz);
			}
		}
		return method;
	}
	
}