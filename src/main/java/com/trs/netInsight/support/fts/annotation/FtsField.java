package com.trs.netInsight.support.fts.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 全文检索库字段标识
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FtsField {

    /**
     * 字段名
     */
    String value() default "";

    /**
     * 是否高亮显示
     */
    boolean highLight() default false;

}
