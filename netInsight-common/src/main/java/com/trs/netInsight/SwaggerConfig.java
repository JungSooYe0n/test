package com.trs.netInsight;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Swagger Config
 *
 * Create by yan.changjiang on 2017年11月24日
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@SuppressWarnings("unchecked")
	@Bean
	public Docket specialDocket() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("special")
				.genericModelSubstitutes(DeferredResult.class).useDefaultResponseMessages(false)
				.forCodeGeneration(false).select()
				.paths(or(regex("/special/.*"), regex("/list/.*"), regex("/spread/*"), regex("/analysis/chart.*")))
				.build().apiInfo(apiInfo("专项检测"));
	}

	@SuppressWarnings("unchecked")
	@Bean
	public Docket reportDocket() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("src/main/resources/report").genericModelSubstitutes(DeferredResult.class)
				.useDefaultResponseMessages(false).forCodeGeneration(false).select()
				.paths(or(regex("/report/.*"), regex("/export/.*"), regex("/kafka/.*"),regex("/system/qrcode/.*"))).build()
				.apiInfo(apiInfo("报告模块"));
	}

	@SuppressWarnings("unchecked")
	@Bean
	public Docket columnDocket() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("column").genericModelSubstitutes(DeferredResult.class)
				.useDefaultResponseMessages(false).forCodeGeneration(false).select().paths(or(regex("/column/.*")))
				.build().apiInfo(apiInfo("报告模块"));
	}
	@SuppressWarnings("unchecked")
	@Bean
	public Docket userDocket() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("user").genericModelSubstitutes(DeferredResult.class)
				.useDefaultResponseMessages(false).forCodeGeneration(false).select().paths(or(regex("/user/.*|/organization/.*|/role/.*")))
				.build().apiInfo(apiInfo("用户模块"));
	}

	@Bean
	public Docket allDocket() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("all").genericModelSubstitutes(DeferredResult.class)
				.useDefaultResponseMessages(false).forCodeGeneration(false).select().build()
				.apiInfo(apiInfo("框架内外全部接口"));
	}

	@SuppressWarnings("unchecked")
	@Bean
	public Docket squareDocket() {
		return new Docket(DocumentationType.SWAGGER_2).groupName("square/alert")
				.genericModelSubstitutes(DeferredResult.class).useDefaultResponseMessages(false)
				.forCodeGeneration(false).select().paths(or(regex("/analysis/relation/.*"), regex("/s.*"),
						regex("/alert/.*"), regex("/account/.*"), regex("/rule/.*"), regex("/home/.*")))
				.build().apiInfo(apiInfo("应用广场"));
	}

	private ApiInfo apiInfo(String description) {
		return new ApiInfoBuilder().title("netInsight 1.0.1 API").description(String.format("网察项目%s接口.", description))
				.version("1.0").termsOfServiceUrl("http://www.trs.com.cn/")
				.contact(new Contact("大数据服务部", "http://dc.trs.org.cn/wiki/pages/viewpage.action?pageId=31464281",
						"ma.wen@trs.com.cn"))
				.license("北京拓尔思信息技术股份有限公司").licenseUrl("http://www.trs.com.cn/").build();
	}
}