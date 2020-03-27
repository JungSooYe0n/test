package com.trs.netInsight.support.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启Redis缓存注解
 *
 * Created by yan.changjiang on 2017/7/28.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableRedis {

    /**
     * 缓存失效性 分钟
     *
     * @return int
     */
    int cacheMinutes() default 30;

    String poolId() default "special_id";

}