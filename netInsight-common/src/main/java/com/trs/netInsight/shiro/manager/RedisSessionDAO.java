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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;

import com.trs.netInsight.shiro.util.SerializeUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @Type RedisSessionDAO.java
 * @Desc
 * @author 谷泽昊
 * @date 2017年11月15日 下午5:17:07
 * @version
 */
@Slf4j
public class RedisSessionDAO extends AbstractSessionDAO {
	/**
	 * shiro-redis
	 */
	private RedisManager redisManager;

	/**
	 * The Redis key prefix for the sessions
	 */
	private String keyPrefix = "shiro_redis_session:";

	@Override
	public void update(Session session) throws UnknownSessionException {
		this.saveSession(session);
	}

	/**
	 * save session
	 * 
	 * @param session
	 * @throws UnknownSessionException
	 */
	private void saveSession(Session session) throws UnknownSessionException {
		if (session == null || session.getId() == null) {
			log.error("session or session id is null");
			return;
		}
		session.setTimeout(redisManager.getExpire()*1000);
		byte[] key = getByteKey(session.getId());
		byte[] value = SerializeUtils.serialize(session);
		this.redisManager.set(key, value, redisManager.getExpire());
	}

	@Override
	public void delete(Session session) {
		if (session == null || session.getId() == null) {
			log.error("session or session id is null");
			return;
		}
		redisManager.del(this.getByteKey(session.getId()));

	}

	@Override
	public Collection<Session> getActiveSessions() {
		Set<Session> sessions = new HashSet<Session>();

		Set<byte[]> keys = redisManager.keys(this.keyPrefix + "*");
		if (keys != null && keys.size() > 0) {
			for (byte[] key : keys) {
				Session s = (Session) SerializeUtils.deserialize(redisManager.get(key));
				sessions.add(s);
			}
		}

		return sessions;
	}

	@Override
	protected Serializable doCreate(Session session) {
		Serializable sessionId = this.generateSessionId(session);
		this.assignSessionId(session, sessionId);
		this.saveSession(session);
		return sessionId;
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		if (sessionId == null) {
			log.error("session id is null");
			return null;
		}
		Session s = (Session) SerializeUtils.deserialize(redisManager.get(this.getByteKey(sessionId)));
//		if(s!=null){
//			s.setTimeout(redisManager.getExpire() * 1000);
//		}
		return s;
	}

	/**
	 * 根据key获得byte[]数组
	 * 
	 * @param key
	 * @return
	 */
	private byte[] getByteKey(Serializable sessionId) {
		String preKey = this.keyPrefix + sessionId;
		return preKey.getBytes();
	}

	public RedisManager getRedisManager() {
		return redisManager;
	}

	public void setRedisManager(RedisManager redisManager) {
		this.redisManager = redisManager;

		/**
		 * 初始化
		 */
		this.redisManager.init();
	}

	/**
	 * Returns the Redis session keys prefix.
	 * 
	 * @return The prefix
	 */
	public String getKeyPrefix() {
		return keyPrefix;
	}

	/**
	 * Sets the Redis sessions key prefix.
	 * 
	 * @param keyPrefix
	 *            The prefix
	 */
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}
}
