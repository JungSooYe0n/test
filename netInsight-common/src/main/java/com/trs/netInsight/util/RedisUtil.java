/*
 * Project: netInsight
 *
 * File Created at 2017年11月21日
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.util;

import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocumentChaos;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis 工具类
 *
 * @author 谷泽昊
 * @Type RedisUtil.java
 * @date 2017年11月21日 下午3:44:20
 */
@SuppressWarnings("unchecked")
public class RedisUtil {

	public static final String PER_KEY  = "redisKey";
	public static final String SUFFIX_KEY = "server";

	private static RedisTemplate<String, User> redisTemplate;
	private static RedisTemplate<String, Object> objTemplate;
	private static StringRedisTemplate stringRedisTemplate;
	private static RedisTemplate<String, List<FtsDocumentChaos>> redisForStream;
	private static RedisTemplate<String,List<Map<String, Object>>> listRedis;
	private static RedisTemplate<String, List<Object>> listRedisForObject;
	private static RedisTemplate<String,Subject> weixinLogin;
	private static RedisTemplate<String, List<FtsDocumentCommonVO>> redisPageMix;
	private static RedisTemplate<String,LogPrintUtil> logRedis;
	private static RedisTemplate<String,AppApiAccessToken> appTokenRedis;

	static {
		try {
			redisTemplate = (RedisTemplate<String, User>) SpringUtil.getBean("redisTemplate");
			objTemplate = (RedisTemplate<String, Object>) SpringUtil.getBean("redisTemplate");
			redisForStream = (RedisTemplate<String, List<FtsDocumentChaos>>) SpringUtil.getBean("redisTemplate");
			listRedis = (RedisTemplate<String, List<Map<String, Object>>>) SpringUtil.getBean("redisTemplate");
			listRedisForObject = (RedisTemplate<String, List<Object>>) SpringUtil.getBean("redisTemplate");
			stringRedisTemplate = (StringRedisTemplate) SpringUtil.getBean("stringRedisTemplate");
			weixinLogin = (RedisTemplate<String, Subject>) SpringUtil.getBean("redisTemplate");
			redisPageMix = (RedisTemplate<String, List<FtsDocumentCommonVO>>) SpringUtil.getBean("redisTemplate");
			logRedis = (RedisTemplate<String, LogPrintUtil>) SpringUtil.getBean("redisTemplate");
			appTokenRedis = (RedisTemplate<String , AppApiAccessToken>) SpringUtil.getBean("redisTemplate");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	/**
	 * 为保证同一线程  logutil相同
	 * @param threadId 线程id
	 * @param log
	 * @return
	 */
	public static boolean setLog(long threadId,LogPrintUtil log){
		ValueOperations<String,LogPrintUtil> opsForValue = logRedis.opsForValue();
		opsForValue.set(String.valueOf(threadId),log);
		logRedis.expire(String.valueOf(threadId), 2, TimeUnit.HOURS);//两小时后  key过期
		return true;
	}
	/**
	 *
	 * @param threadId
	 * @return
	 */
	public static LogPrintUtil getLog(long threadId){
		ValueOperations<String,LogPrintUtil> opsForValue = logRedis.opsForValue();
		return opsForValue.get(String.valueOf(threadId));
	}
	/**
	 * 把查询出的混合列表信息存入redis  导出时取出
	 * @param ticket
	 * @param mixList
	 * @return
	 */
	public static boolean setMix(String ticket,List<FtsDocumentCommonVO> mixList){
		if (StringUtils.isBlank(ticket)) {
			return false;
		}
		ValueOperations<String, List<FtsDocumentCommonVO>> opsForValue = redisPageMix.opsForValue();
		opsForValue.set(ticket, mixList);
		redisPageMix.expire(ticket, 20, TimeUnit.MINUTES);//两小时后  key过期
		return true;
	}

	/**
	 * 取出混合列表信息
	 * @param key
	 * @return
	 */
	public static List<FtsDocumentCommonVO> getMix(String key){
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ValueOperations<String, List<FtsDocumentCommonVO>> opsForValue = redisPageMix.opsForValue();
		return opsForValue.get(key);
	}

	/**
	 * 把登录信息存入redis  微信登录时取出
	 * @param ticket ticket+WEIXIN_LOGIN_REIDS
	 * @param currentUser
	 * @return
	 */
	public static boolean setLogin(String ticket,Subject currentUser){
		if (StringUtils.isBlank(ticket)) {
			return false;
		}
		ValueOperations<String, Subject> opsForValue = weixinLogin.opsForValue();
		opsForValue.set(ticket, currentUser);
		weixinLogin.expire(ticket, 2, TimeUnit.HOURS);//两小时后  key过期
		return true;
	}
	/**
	 * 登录信息从redis取出
	 * @param key ticket+WEIXIN_LOGIN_REIDS
	 * @return
	 */
	public static Subject getLogin(String key){
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ValueOperations<String, Subject> opsForValue = weixinLogin.opsForValue();
		return opsForValue.get(key);
	}
	/**
	 * 海贝查询信息
	 * @param key
	 * @param obj
	 * @return
	 */
	public static boolean setObject(String key,Object obj){
		if (StringUtils.isBlank(key)) {
			return false;
		}
		ValueOperations<String, Object> opsForValue = objTemplate.opsForValue();
		opsForValue.set(key, obj);
		weixinLogin.expire(key, 24, TimeUnit.HOURS);//key过期
		return true;
	}
	/**
	 * 登录信息从redis取出
	 * @param key ticket+WEIXIN_LOGIN_REIDS
	 * @return
	 */
	public static Object getObject(String key){
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			ValueOperations<String, Object> opsForValue = objTemplate.opsForValue();
			return opsForValue.get(key);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 把List<Map<String, Object>>存到redis
	 * @param key
	 * @param listMap
	 * @return
	 */
	public static boolean setListMap(String key,List<Map<String, Object>> listMap){
		if (StringUtils.isBlank(key)) {
			return false;
		}
		ValueOperations<String, List<Map<String, Object>>> opsForValue = listRedis.opsForValue();
		opsForValue.set(key, listMap);
		listRedis.expire(key, 10, TimeUnit.MINUTES);// 默认10分钟缓存
		//expire(key, 2, TimeUnit.HOURS);//两小时后  key过期
		return true;
	}

	/**
	 * 把List<Map<String, Object>>从redis中取出
	 * @param key
	 * @return
	 */
	public static List<Map<String, Object>> getListMap(String key){
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ValueOperations<String, List<Map<String, Object>>> opsForValue = listRedis.opsForValue();
		return opsForValue.get(key);
	}
	/**
	 * 将导出的文件信息存到redis
	 * @param key
	 * @param listMix
	 * @return
	 */
	public static boolean setOutputStream(String key,List<FtsDocumentChaos> listMix){
		if (StringUtils.isBlank(key)) {
			return false;
		}
		ValueOperations<String, List<FtsDocumentChaos>> opsForValue = redisForStream.opsForValue();
		opsForValue.set(key, listMix);
		redisForStream.expire(key, 20, TimeUnit.MINUTES);//两小时后  key过期
		return true;
	}

	/**
	 * 将导出的文件信息取出
	 * @param key
	 * @return
	 */
	public static List<FtsDocumentChaos> getOutputStream(String key){
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ValueOperations<String, List<FtsDocumentChaos>> opsForValue = redisForStream.opsForValue();
		return opsForValue.get(key);
	}

	/**
	 * Object列表操作
	 *
	 * @since changjiang @ 2018年7月9日
	 * @param key
	 * @param list
	 * @return
	 * @Return : boolean
	 */
	public static <T> boolean setListForObject(String key, List<T> list) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		ValueOperations<String, List<Object>> opsForValue = listRedisForObject.opsForValue();
		opsForValue.set(key, (List<Object>)list);
		listRedisForObject.expire(key, 10, TimeUnit.MINUTES);// 默认10分钟缓存
		return true;
	}

	/**
	 * Object列表操作
	 *
	 * @since changjiang @ 2018年7月9日
	 * @param key
	 * @param list
	 * @param expire
	 *            过期时间
	 * @param unit
	 *            时间单位
	 * @return
	 * @Return : boolean
	 */
	public static <T> boolean setListForObject(String key, List<T> list, long expire, TimeUnit unit) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		ValueOperations<String, List<Object>> opsForValue = listRedisForObject.opsForValue();
		opsForValue.set(key, (List<Object>) list);
		listRedisForObject.expire(key, expire, unit);
		return true;
	}

	/**
	 * Object列表操作
	 *
	 * @since changjiang @ 2018年7月9日
	 * @param key
	 * @return
	 * @Return : List<Object>
	 */
	public static <T> List<T> getList(String key,Class<T> t) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ValueOperations<String, List<Object>> opsForValue = listRedisForObject.opsForValue();
		return (List<T>)opsForValue.get(key);
	}

	/**
	 * 添加map类型参数
	 *
	 * @return
	 * @date Created at 2017年11月21日 下午3:49:36
	 * @Author 马文
	 */
	public static boolean setMapper(String key, Map<String, Object> map) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
		hash.putAll(key, map);
		redisTemplate.expire(key, 10, TimeUnit.MINUTES);// 默认10分钟缓存
		return true;
	}

	/**
	 * 将map信息取出
	 *
	 * @param key
	 * @return
	 * @date Created at 2017年11月21日 下午3:49:36
	 * @Author 马文
	 */
	public static Map<Object, Object> getMapper(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}

		HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
		return hash.entries(key);
	}


	public static boolean deleteKeyForFuzzy(String key){
		Set<String> keys = stringRedisTemplate.keys(key + "*");
		if(keys != null && keys.size()>0){
			stringRedisTemplate.delete(keys);
		}
		return true;
	}
	/**
	 * 将key信息删除
	 *
	 * @param key
	 * @return
	 * @date Created at 2017年11月21日 下午3:49:36
	 * @Author 谷泽昊
	 */
	public static boolean deleteKey(String key) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		redisTemplate.delete(key);
		return true;
	}

	/**
	 * 删除stringRedisTemplate中的key
	 * @param key
	 * @return
	 */
	public static boolean deleteString(String key) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		stringRedisTemplate.delete(key);
		return true;
	}

	/**
	 * 获取所有相似Key
	 * @param key
	 * @return
	 */
	public static Set<String> getKeys(String key){
		Set<String> keys = stringRedisTemplate.keys(key);
		return keys;
	}

	/**
	 * 存储key值
	 * @param builder
	 * @return
	 */
	public static String saveKey(QueryBuilder builder){
		String asTrsl = builder.asTRSL();
		// 把表达式放缓存里边 把key值返回给前端
		long threadId = Thread.currentThread().getId();
		String trslk = RedisUtil.PER_KEY + threadId;
		//如果需要转换成server  key就以server结尾  在导出exel时用
		if(builder.isServer()){
			trslk = trslk+RedisUtil.SUFFIX_KEY;
		}
		if(asTrsl.contains(FtsFieldConst.WEIGHT)){
			trslk = trslk+FtsFieldConst.WEIGHT;
		}
		RedisUtil.setString(trslk, asTrsl);
		return trslk;
	}

	/**
	 * 将string 类型存入redis
	 *
	 * @param key
	 * @param value
	 * @date Created at 2017年11月21日 下午4:11:45
	 * @Author 谷泽昊
	 */
	public static boolean setString(String key, String value) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		opsForValue.set(key, value);
		expire(key, 2, TimeUnit.HOURS);
		return true;
	}
	/**
	 * 将string 类型存入redis，并增加时间
	 *
	 * @param key
	 * @param value
	 * @date Created at 2017年11月21日 下午4:11:45
	 * @Author 谷泽昊
	 */
	public static boolean setString(String key, String value, long timeout, TimeUnit unit) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		opsForValue.set(key, value, timeout, unit);
		return true;
	}


	/**
	 * 取出string类型数据
	 *
	 * @param key
	 * @return
	 * @date Created at 2017年11月21日 下午4:11:48
	 * @Author 谷泽昊
	 */
	public static String getString(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		return opsForValue.get(key);
	}

	/**
	 * 存int类型
	 * @date Created at 2018年1月25日  下午3:51:36
	 * @Author 谷泽昊
	 * @param key
	 * @param integer
	 * @return
	 */
	public static boolean setInteger(String key, Integer integer) {
		if (StringUtils.isBlank(key)) {
			return false;
		}

		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		opsForValue.set(key, String.valueOf(integer));
		return true;
	}

	/**
	 * 取出int类型
	 * @date Created at 2018年1月25日  下午3:51:40
	 * @Author 谷泽昊
	 * @param key
	 * @return
	 */
	public static Integer getInteger(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		if (opsForValue.get(key) == null) {
			return null;
		}
		return Integer.valueOf(opsForValue.get(key));
	}
	/**
	 * 存appToken类型
	 * @param key
	 * @param appToken
	 * @return
	 */
	public static boolean setAppToken(String key, AppApiAccessToken appToken) {
		if (StringUtils.isBlank(key)) {
			return false;
		}

		ValueOperations<String, AppApiAccessToken> opsForValue = appTokenRedis.opsForValue();
		opsForValue.set(key, appToken);
		appTokenRedis.expire(key, 60, TimeUnit.MINUTES);// 默认60分钟缓存
		return true;
	}

	/**
	 * 取出int类型
	 * @param key
	 * @return
	 */
	public static AppApiAccessToken getAppToken(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ValueOperations<String, AppApiAccessToken> opsForValue = appTokenRedis.opsForValue();
		if (opsForValue.get(key) == null) {
			return null;
		}
		return opsForValue.get(key);
	}

	/**
	 * 自增存储次数
	 *
	 * @param key
	 * @return
	 * @date Created at 2017年11月21日 下午4:13:35
	 * @Author 谷泽昊
	 */
	public static Long increment(String key, long delta) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
		return opsForValue.increment(key, delta);
	}

	/**
	 * key 保存的时间
	 *
	 * @param key
	 * @param timeout
	 * @param unit
	 * @date Created at 2017年11月21日 下午4:17:49
	 * @Author 谷泽昊
	 */
	public static boolean expire(String key, long timeout, TimeUnit unit) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		stringRedisTemplate.expire(key, timeout, unit);
		return true;
	}

	/**
	 *  key 保存的时间
	 * @date Created at 2018年11月6日  下午5:44:59
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param key
	 * @param date
	 * @return
	 */
	public static boolean expire(String key, Date date) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		stringRedisTemplate.expireAt(key, date);
		return true;
	}

	/**
	 * 校验是否存在该key
	 *
	 * @since changjiang @ 2018年7月3日
	 * @param key
	 * @return
	 * @Return : boolean
	 */
	public static boolean exist(String key) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		return stringRedisTemplate.persist(key);
	}

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月21日 谷泽昊 creat
 */
