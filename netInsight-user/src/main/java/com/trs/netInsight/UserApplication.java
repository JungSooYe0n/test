
package com.trs.netInsight;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * SpringBoot工程启动入口
 *
 * Created by ChangXiaoyang on 2017/2/15.
 */
//@EnableJpaRepositories(basePackages ="com.trs.netInsight.com.trs.netInsight.support.hybaseShard.repository")
//@EntityScan(basePackages = "com.trs.netInsight.com.trs.netInsight.support.hybaseShard.entity")
@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class UserApplication {



	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(UserApplication.class, args);
	}

}
