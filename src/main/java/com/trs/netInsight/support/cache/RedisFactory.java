package com.trs.netInsight.support.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis缓存工具
 *
 * Created by yan.changjiang on 16/12/15.
 */
@Slf4j
@Component
public class RedisFactory {

	private static StringRedisTemplate stringRedisTemplate;

	@Autowired
	public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
		RedisFactory.stringRedisTemplate = stringRedisTemplate;
	}

	/**
	 * 从redis中获取value,如果出现异常(例如:redis连接不上等),会返回给前端一个""的字符串,异常信息记录在debug日志中
	 * 
	 * @param key
	 *            传入的key
	 * @return String
	 */
	public static String getValueFromRedis(String key) {
		return StringUtil.isEmpty(key) ? null : getValueFromRedis(key, "");
	}
	/**
	 * 模糊删除所有key
	 * @param key
	 * @return
	 */
	public static boolean deleteAllKey(String key){
        stringRedisTemplate.delete(stringRedisTemplate.keys("*"+key+"*"));
		return true;
	}
	
	/**
	 * 从redis中获取value,如果出现异常(例如:redis连接不上等),会返回给前端一个defaultValue的字符串,
	 * 异常信息记录在debug日志中
	 * 
	 * @param key
	 *            传入的key
	 * @param defaultValue
	 *            如果没有这个key,或者出现异常,返回defaultValue
	 * @return String
	 */
	public static String getValueFromRedis(String key, String defaultValue) {
		try {
			String redisValue = stringRedisTemplate.opsForValue().get(key);
			return redisValue == null ? defaultValue : redisValue;
		} catch (Exception e) {
			log.debug("get value from redis error, key : " + key + " , defautValue : " + defaultValue, e);
			return defaultValue;
		}
	}

	/**
	 * 将数据存入redis中,从redis中获取value,如果出现异常(例如:redis连接不上等),会将异常信息记录在debug日志中
	 *
	 * @param key
	 *            存入的key
	 * @param value
	 *            key对应的value
	 */
	public static void setValueToRedis(String key, String value) throws Exception {
		try {
			if (!StringUtil.isEmpty(value) && !StringUtil.isEmpty(key)) {
				stringRedisTemplate.opsForValue().set(key, value, Const.REDIS_KEEP_MINUTE, TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			log.debug("set value to redis error, key : " + key + ", value : " + value, e);
		}
	}

	/**
	 * 将数据存入redis中,从redis中获取value,如果出现异常(例如:redis连接不上等),会将异常信息记录在debug日志中
	 *
	 * @param key
	 *            存入的key
	 * @param value
	 *            key对应的value
	 */
	public static void setValueToRedis(String key, Object value) {
		try {
			if (!ObjectUtil.isEmpty(value) && !StringUtil.isEmpty(key)) {
				stringRedisTemplate.opsForValue().set(key, ObjectUtil.toJson(value), Const.REDIS_KEEP_MINUTE,
						TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			log.debug("set value to redis error, key : " + key + ", value : " + value, e);
		}
	}

	/**
	 * 将数据存入redis中 可设置缓存时间
	 */
	public static void setValueToRedis(String key, Object value, int time, TimeUnit unit) {
		try {
			if (!ObjectUtil.isEmpty(value) && !StringUtil.isEmpty(key)) {
				stringRedisTemplate.opsForValue().set(key, ObjectUtil.toJson(value), time, unit);
			}
		} catch (Exception e) {
			log.debug("set value to redis error, key : " + key + ", value : " + value, e);
		}
	}

	/**
	 * 清除redis数据
	 */
	public static void clearRedis(String key) {
		try {
			if (!StringUtil.isEmpty(key)) {
				stringRedisTemplate.delete(key);
			}
		} catch (Exception e) {
			log.debug("delete data in redis error, key : " + key, e);
		}
	}

	/**
	 * 模糊清除redis数据
	 */
	public static void batchClearRedis(String key) {
		try {
			if (!StringUtil.isEmpty(key)) {
				stringRedisTemplate.delete(stringRedisTemplate.keys(key + "*"));
			}
		} catch (Exception e) {
			log.debug("delete data in redis error, key : " + key, e);
		}
	}
}
