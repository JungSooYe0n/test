package com.trs.netInsight.support.cache;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.trs.netInsight.util.ObjectUtil;

/**
 * 永久存储池
 * 存储专题的最后修改时间，作为缓存的key值，
 * 以保证专题改动后不会取到旧数据
 *
 * Created by yan.changjiang on 2017/8/16.
 */
public final class PerpetualPool {

    private static Map<String, Object> pool = Collections.synchronizedMap(new HashMap<>());

    public static void put(String key, Object value) {
        pool.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        Object o = pool.get(key);
        return ObjectUtil.isNotEmpty(o) ? (T) o : null;
    }

    public static void remove(String key) {
        pool.remove(key);
    }

}
