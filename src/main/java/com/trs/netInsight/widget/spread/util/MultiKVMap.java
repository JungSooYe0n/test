package com.trs.netInsight.widget.spread.util;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.LinkedMultiValueMap;

/**
 * 多键多值Map
 *
 * Created by ChangXiaoyang on 2017/3/10.
 */
public class MultiKVMap<K, V> extends LinkedMultiValueMap<K, V> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2374072348089220915L;
	/**
     * 替补key
     */
    private Map<K, Set<K>> subKey;

    public MultiKVMap() {
        super();
        this.subKey = new LinkedHashMap<>();
    }

    public void add(K key, K subKey, V value) {
        this.subKey.computeIfAbsent(key, k -> new HashSet<>());
        if (!this.subKey.get(key).contains(subKey)) {
            this.subKey.get(key).add(subKey);
            super.add(key, value);
        }
    }

    @Override
    public List<V> remove(Object key) {
        subKey.remove(key);
        return super.remove(key);
    }

    /**
     * 排序
     */
    public void sort() {
        this.keySet().forEach(k -> {
            List<V> vs = super.get(k);
            vs.sort((v1, v2) -> {
                Set<K> ks1 = subKey.get(v1.toString());
                Set<K> ks2 = subKey.get(v2.toString());
                int v11 = ks1 == null ? 0 : ks1.size();
                int v12 = ks2 == null ? 0 : ks2.size();
                return v12 - v11;
            });
        });
    }

}
