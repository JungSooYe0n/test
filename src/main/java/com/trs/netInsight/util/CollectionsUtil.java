package com.trs.netInsight.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trs.netInsight.handler.exception.TRSException;

/**
 * 集合操作工具类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年9月13日
 *
 */
public class CollectionsUtil {
	/**
	 * 从实体对象集合中获取指定属性的值集合
	 * 
	 * @since changjiang @ 2018年9月13日
	 * @param objs
	 *            实体对象集合
	 * @param attrName
	 *            字段名称
	 * @param returnClazz
	 *            返回字段类型
	 * @return
	 * @throws TRSException
	 * @Return : List<T>
	 */
	@SuppressWarnings({ "unchecked" })
	public static <T> List<T> getAttrList(List<? extends Object> objs, String attrName, Class<T> returnClazz)
			throws TRSException {
		List<T> attrValueList = null;
		Class<?> clazz = null;
		Field field = null;
		T instance = null;
		try {
			if (isNotEmpty(objs)) {
				attrValueList = new ArrayList<>();
				for (Object obj : objs) {
					clazz = obj.getClass();
					field = clazz.getDeclaredField(attrName);
					if (field != null) {
						field.setAccessible(true);
						instance = (T) field.get(obj);
						attrValueList.add(instance);
					}
				}
			}

		} catch (Exception e) {
			throw new TRSException(e);
		}
		return attrValueList;

	}

	/**
	 * 集合非空验证
	 * 
	 * @since changjiang @ 2018年9月13日
	 * @param colleantions
	 * @return
	 * @Return : boolean
	 */
	public static <T extends Object> boolean isNotEmpty(Collection<T> colleantions) {
		return colleantions != null && colleantions.size() > 0;
	}

	/**
	 * 批量修改集合中指定属性值
	 * 
	 * @param colleantions
	 * @param attrName
	 *            属性名
	 * @param attrValue
	 *            属性值
	 * @return
	 * @throws TRSException
	 */
	public static <T> Collection<T> batchUpdateAttr(Collection<T> colleantions, String attrName, Object attrValue)
			throws TRSException {
		try {
			if (isNotEmpty(colleantions)) {
				Field declaredField = null;
				for (T clazz : colleantions) {
					declaredField = clazz.getClass().getDeclaredField(attrName);
					declaredField.setAccessible(true);
					declaredField.set(clazz, attrValue);
				}
			}
		} catch (Exception e) {
			throw new TRSException(e);
		}
		return colleantions;
	}

}
