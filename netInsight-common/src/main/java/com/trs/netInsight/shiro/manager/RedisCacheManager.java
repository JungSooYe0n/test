/*
 * Project: netInsight
 * 
 * File Created at 2017年11月15日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.shiro.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

/**
 * @Type RedisCacheManager.java
 * @Desc 
 * @author 谷泽昊
 * @date 2017年11月15日 下午5:16:34
 * @version 
 */
public class RedisCacheManager implements CacheManager {

	// fast lookup by name map
	@SuppressWarnings("rawtypes")
	private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

	private RedisManager redisManager;

	/**
	 * The Redis key prefix for caches 
	 */
	private String keyPrefix = "shiro_redis_cache:";
	
	/**
	 * Returns the Redis session keys
	 * prefix.
	 * @return The prefix
	 */
	public String getKeyPrefix() {
		return keyPrefix;
	}

	/**
	 * Sets the Redis sessions key 
	 * prefix.
	 * @param keyPrefix The prefix
	 */
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <K, V> Cache<K, V> getCache(String name) throws CacheException {
		
		@SuppressWarnings("rawtypes")
		Cache c = caches.get(name);
		
		if (c == null) {

			// initialize the Redis manager instance
			redisManager.init();
			
			// create a new cache instance
			c = new RedisCache<K, V>(redisManager, keyPrefix);
			
			// add it to the cache collection
			caches.put(name, c);
		}
		return c;
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;
	}

}


