package com.trs.netInsight.support.fts.annotation;

import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.annotation.enums.FtsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 全文检索库连接信息
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface FtsClient {

	String indices()default "";

	FtsHybaseType hybaseType() default FtsHybaseType.TRADITIONAL;

	FtsSource source() default FtsSource.HYBASE8;
}
