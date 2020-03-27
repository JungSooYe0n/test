package com.trs.netInsight.support.api.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.trs.netInsight.support.api.utils.constance.ApiMethod;

/**
 * 需认证Api
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月2日
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Api {

	/**
	 * api名称
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @return
	 * @Return : String
	 */
	@Deprecated
	String name() default "";

	/**
	 * api名称
	 * 
	 * @since changjiang @ 2018年7月2日
	 * @return
	 * @Return : String
	 */
	String value() default "";

	/**
	 * api method
	 * @since changjiang @ 2018年7月3日
	 * @return
	 * @Return : ApiMethod
	 */
	ApiMethod method ();
}
