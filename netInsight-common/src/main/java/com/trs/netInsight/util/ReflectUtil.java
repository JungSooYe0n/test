package com.trs.netInsight.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 反射工具类
 *
 * Create by yan.changjiang on 2017年11月22日
 */
public class ReflectUtil {
	@SuppressWarnings("rawtypes")
	public static Object getBean(String className) {
		Object obj = null;
		try {
			Class cls = Class.forName(className);
			Constructor[] cons = cls.getConstructors();
			Constructor defCon = cons[0];// 得到默认构造器,第0个是默认构造器，无参构造方法
			obj = defCon.newInstance();// 实例化，得到一个对象 //Spring - bean -id
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * java反射bean的set方法
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Method getSetMethod(Class objClass, String fieldName) {
		Class[] parameterTypes = new Class[1];
		Field field;
		try {
			field = objClass.getDeclaredField(fieldName);
			parameterTypes[0] = field.getType();
			StringBuilder builder = new StringBuilder();
			builder.append("set");
			builder.append(fieldName.substring(0, 1).toUpperCase());
			builder.append(fieldName.substring(1));
			Method method = objClass.getMethod(builder.toString(), parameterTypes);
			return method;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 执行set方法
	 */
	public static void invokeSet(Object obj, String fieldName, Object value) {
		Method method = getSetMethod(obj.getClass(), fieldName);
		try {
			method.invoke(obj, new Object[] { value });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * java反射bean的get方法
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Method getGetMethod(Class objClass, String fieldName) {
		try {
			StringBuilder builder = new StringBuilder();
			if ("boolean".equals(objClass.getDeclaredField(fieldName).getType().getName())) {
				builder.append("is");
			} else {
				builder.append("get");
			}
			builder.append(fieldName.substring(0, 1).toUpperCase());
			builder.append(fieldName.substring(1));
			Method method = objClass.getMethod(builder.toString());
			return method;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 执行get方法
	 */
	public static Object invokeGet(Object obj, String fieldName) {
		Method method = getGetMethod(obj.getClass(), fieldName);
		try {
			return method.invoke(obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 执行方法
	 */
	public static Object invoke(Object obj, String methodName, @SuppressWarnings("rawtypes") Class[] clazz,
			Object[] value) {
		Method method = null;
		try {
			if (clazz.length < 1) {
				method = obj.getClass().getMethod(methodName);
				return method.invoke(obj);
			} else {
				method = obj.getClass().getMethod(methodName, clazz);
				return method.invoke(obj, value);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取对象的字段值
	 *
	 * @param obj
	 *            对象
	 * @param fields
	 *            字段
	 * @param <T>
	 *            类型
	 * @return Map
	 */
	public static <T> Map<String, Object> getFields(T obj, String... fields) {
		Map<String, Object> result = new HashMap<>();
		for (String f : fields) {
			try {
				Field field = obj.getClass().getDeclaredField(f);
				field.setAccessible(true);
				result.put(f, field.get(obj));
			} catch (Exception ignore) {
			}
		}
		return result;
	}
}
