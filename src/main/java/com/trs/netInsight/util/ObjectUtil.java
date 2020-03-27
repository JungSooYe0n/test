package com.trs.netInsight.util;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trs.netInsight.handler.exception.NullException;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.GroupWordResult;
import com.trs.netInsight.widget.special.entity.InfoListResult;

/**
 * 对象判断工具
 *
 * Created by TRS on 17/2/12.
 */
public final class ObjectUtil {

	/**
	 * 断言空抛异常
	 *
	 * @param obj
	 *            Object
	 * @throws TRSException
	 *             TRSException
	 */
	public static void assertNull(Object obj, String message) throws TRSException {
		if (isEmpty(obj)) {
			throw new NullException(String.format("NULL Exception,message:%s 为空", message));
		}
	}

	/**
	 * 判断对象或对象数组中每一个对象是否为空 对象为null，字符序列长度为0，集合类、Map为empty
	 *
	 * @param obj
	 *            对象
	 * @return true or false
	 */
	public static boolean isEmpty(Object obj) {
		boolean isEmpty = false;
		if (obj != null) {
			if (obj instanceof String) { // 字符串
				isEmpty = StringUtil.isEmpty(String.valueOf(obj));
			} else if (obj instanceof Map) { // map
				isEmpty = ((Map<?, ?>) obj).isEmpty();
			} else if (obj instanceof Collection) { // 集合
				isEmpty = ((Collection<?>) obj).isEmpty();
			} else if (obj instanceof Number) {// 数值
				isEmpty = obj.hashCode() == 0;
			} else if (obj.getClass().isArray()) {// 数组
				isEmpty = Array.getLength(obj) == 0;
			} else if(obj instanceof Page){
				List<?> content = ((Page<?>)obj).getContent();
				isEmpty=(content==null||content.size()==0);
			} else if(obj instanceof InfoListResult){
				Object content = ((InfoListResult<?>)obj).getContent();
				isEmpty=(content==null ||isEmpty(content));
			}else if(obj instanceof GroupResult){
				Object content = ((GroupResult)obj).getGroupList();
				isEmpty=(content==null ||isEmpty(content));
			}else if(obj instanceof GroupWordResult){
				Object content = ((GroupWordResult)obj).getGroupList();
				isEmpty=(content==null ||isEmpty(content));
			}
		} else {
			isEmpty = true;
		}
		return isEmpty;
	}

	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}

	/**
	 * 将对象转化为Json
	 *
	 * @param obj
	 *            对象
	 * @return String
	 */
	public static String toJson(Object obj) throws OperationException {
		if (obj instanceof String) {
			return obj.toString();
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new OperationException("Json 转换出错");
		}
	}
	
	/**
	 * 排序方法
	 * @param targetList 要排序的对象
	 * @param sortField 用哪个字段排序
	 * @param sortMode 倒序desc还是正序asc
	 */
	public static void sort(List<Object> targetList, String sortField, String sortMode) {  
	    //使用集合的sort方法  ，并且自定义一个排序的比较器
		 Collections.sort(targetList, new Comparator<Object>() { 	
			 	//匿名内部类，重写compare方法 
	            public int compare(Object obj1, Object obj2) {   
	                int result = 0;  
	                try {  
	                    //首字母转大写  
	                    String newStr = sortField.substring(0, 1).toUpperCase()+sortField.replaceFirst("\\w","");   
	                    //获取需要排序字段的“get方法名”
	                    String methodStr = "get"+newStr;  
	                    /**	API文档：：
	                     *  getMethod(String name, Class<?>... parameterTypes)
	                     *  返回一个 Method 对象，它反映此 Class 对象所表示的类或接口的指定公共成员方法。
	                     */
	                    Method method1 = obj1.getClass().getMethod(methodStr, null);  
	                    Method method2 = obj2.getClass().getMethod(methodStr, null);  
	                    Object returnObj1 = method1.invoke((obj1), null);
	                    Object returnObj2 = method2.invoke((obj2), null);
	                    result = (Integer)returnObj1 - (Integer)returnObj2;
	                } catch (Exception e) {  
	                    throw new RuntimeException();  
	                }  
	                if ("desc".equals(sortMode)) {
                        // 倒序
                        result = -result;
                    }
	                return result;  
	            }  
	        });  
	    }

	/**
	 * 将Json转化为对象
	 *
	 * @param str
	 *            Json
	 * @return Object
	 */
	public static Object toObject(String str) {
		return toObject(str, Object.class);
	}

	public static <T> T toObject(String str, Class<T> clazz) {
		try {
			return new ObjectMapper().readValue(str, clazz);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 简化Json对象视图展示
	 *
	 * @return Object
	 * @throws TRSException
	 *             TRSException
	 */
	public static <T> List<?> writeWithView(List<T> t, Class<?> clazz) throws TRSException {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			objectMapper.writerWithView(clazz).writeValue(bos, t);
			return toObject(bos.toString(), List.class);
		} catch (Exception e) {
			throw new OperationException("Json视图转换出错,message:" + e);
		}
	}

}
