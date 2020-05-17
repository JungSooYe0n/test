package com.trs.netInsight.config.constant;

import com.trs.netInsight.handler.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lilyy
 * @date 2020/2/5 17:40
 * 图片配置虚拟路径,本地图片可以直接访问
 * http://localhost:8018/netInsight/pdfpath/png/invoice_f8f5041b-8973-4768-9f8f-d991f9971e09.png
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

//    @Value("${pdfpathVirtualPath}")
//    private String virtualPath;

    @Value("${pdf.file.path}")
    private String filePath;

    @Override
    public void addViewControllers( ViewControllerRegistry registry ) {
        registry.addViewController( "/" ).setViewName( "forward:/index.html" );
        registry.setOrder( Ordered.HIGHEST_PRECEDENCE );
        super.addViewControllers( registry );
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {

        registry.addConverter(new DateConverter());
    }

    @Bean
    GlobalExceptionHandler getGlobalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //其中OTA表示访问的前缀。"file:D:/OTA/"是文件真实的存储路径
        String virtualPath = "pdfpath";
        registry.addResourceHandler("/" + virtualPath + "/**").addResourceLocations("file:" + filePath+ "/");

    }
}

class DateConverter implements Converter<String,Date> {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @Override
    public Date convert(String s) {
        if ("".equals(s) || s == null) {
            return null;
        }
        try {
            return simpleDateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
