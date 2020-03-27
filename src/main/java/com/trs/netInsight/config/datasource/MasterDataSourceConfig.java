package com.trs.netInsight.config.datasource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Map;

/**
 * @author lilyy
 * @date 2019/12/20 12:36
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef="entityManagerFactoryPrimary",//配置连接工厂 entityManagerFactory
        transactionManagerRef="transactionManagerPrimary", //配置 事物管理器  transactionManager
        basePackages = {"com.trs.netInsight.widget.*",
                "com.trs.netInsight.support.api.*",
                "com.trs.netInsight.support.appApi.*",
                "com.trs.netInsight.support.autowork.*",
                "com.trs.netInsight.support.bigscreen.*",
                "com.trs.netInsight.support.ckm.*",
                "com.trs.netInsight.support.excel.*",
                "com.trs.netInsight.support.fts.*",
                "com.trs.netInsight.support.kafka.*",
                "com.trs.netInsight.support.mongo.*",
                "com.trs.netInsight.support.report.*",
                "com.trs.netInsight.support.storage.*",
                "com.trs.netInsight.support.hybaseShard.*",
                "com.trs.netInsight.support.knowledgeBase.*"})//设置Repository所在位置
public class MasterDataSourceConfig {

    @Autowired
    @Qualifier("primaryDataSource")
    private DataSource primaryDataSource;

    @Primary
    @Bean(name = "entityManagerPrimary")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryPrimary(builder).getObject().createEntityManager();
    }
    @Resource
    private JpaProperties jpaProperties;
//    @Resource
//    private HibernateProperties properties;

    @Primary
    @Bean(name = "entityManagerFactoryPrimary")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryPrimary (EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(primaryDataSource)
//                .properties(properties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings()))
                .properties(getVendorProperties(primaryDataSource))
                .packages("com.trs.netInsight.widget.*",
                        "com.trs.netInsight.handler.*",
                        "com.trs.netInsight.support.api.*",
                        "com.trs.netInsight.support.appApi.*",
                        "com.trs.netInsight.support.autowork.*",
                        "com.trs.netInsight.support.bigscreen.*",
                        "com.trs.netInsight.support.ckm.*",
                        "com.trs.netInsight.support.excel.*",
                        "com.trs.netInsight.support.fts.*",
                        "com.trs.netInsight.support.kafka.*",
                        "com.trs.netInsight.support.mongo.*",
                        "com.trs.netInsight.support.report.*",
                        "com.trs.netInsight.support.ssh.*",
                        "com.trs.netInsight.support.storage.*",
                        "com.trs.netInsight.support.template.*",
                        "com.trs.netInsight.support.hybaseShard.*",
                        "com.trs.netInsight.support.knowledgeBase.*"
                ) //设置实体类所在位置
                .persistenceUnit("primaryPersistenceUnit")
                .build();
    }

    private Map<String, String> getVendorProperties(DataSource dataSource) {
//        jpaProperties.getHibernate().getNaming().setPhysicalStrategy("org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        return jpaProperties.getHibernateProperties(dataSource);
    }

    @Primary
    @Bean(name = "transactionManagerPrimary")
    public PlatformTransactionManager transactionManagerPrimary(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryPrimary(builder).getObject());
    }

}
