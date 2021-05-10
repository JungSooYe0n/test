package com.trs.netInsight.support.log.handler;

import com.trs.netInsight.support.log.entity.enums.SearchLogType;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 检索时间超长日志注解
 * @author 马加鹏
 * @date 2021/4/21 15:57
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SearchLog {

    /**
     * 模块类型
     * @return
     */
    SearchLogType searchLogType();

}
