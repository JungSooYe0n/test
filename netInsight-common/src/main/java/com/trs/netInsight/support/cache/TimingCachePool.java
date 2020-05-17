package com.trs.netInsight.support.cache;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 全局对象缓存池
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class TimingCachePool {

	/**
	 * 缓存池
	 */
	private static Map<String, SoftReference> pool = Collections.synchronizedMap(new HashMap<>());

	private static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

	/**
	 * 获取对象
	 *
	 * @param id
	 *            对象的id
	 * @param <T>
	 *            对象类型
	 * @return 对象
	 */
	public static <T> T get(String id) {
		SoftReference reference = pool.get(id);
		if (reference == null) {
			return null;
		}
		return (T) reference.get();
	}

	/**
	 * 存放全局变量
	 *
	 * @param id
	 *            对象的id
	 * @param <T>
	 *            对象类型
	 * @return 对象
	 */
	public static <T> T put(String id, T value) {
		return put(id, value, 10);
	}

	/**
	 * 存放全局变量
	 *
	 * @param id
	 *            对象的id
	 * @param <T>
	 *            对象类型
	 * @return 对象
	 */
	public static <T> T put(String id, T value, int active) {
		SoftReference old = pool.put(id, new SoftReference(value));
		scheduledThreadPool.schedule(() -> clear(id), active, TimeUnit.MINUTES);
		return old == null ? null : (T) old.get();
	}

	/**
	 * 清空全局变量
	 */
	public static void clear(String key) {
		pool.remove(key);
	}

	/**
	 * 清空全局变量
	 */
	public static void clear() {
		pool.clear();
	}
}
