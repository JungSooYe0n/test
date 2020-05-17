package com.trs.netInsight.support.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trs.netInsight.util.SpringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Author:拓尔思信息股份有限公司
 * @Description:
 * @Date:Created in  2020/3/6 17:52
 * @Created By yangyanyan
 */
@Configuration
public class RedisConfig {

    @Bean(name = "customRedisTemplate")
    public RedisTemplate redisTemplate() {
        RedisTemplate template = new RedisTemplate();
        return  getRedisTemplate(template);
    }


    public RedisTemplate getRedisTemplate(RedisTemplate template){
        template.setConnectionFactory(connectionFactory());
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //序列化 值时使用此序列化方法
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    public RedisConnectionFactory connectionFactory() {
        JedisConnectionFactory jedis = new JedisConnectionFactory();
        jedis.setHostName("192.168.202.202");
        jedis.setPort(6379);

        jedis.setDatabase(14);

        jedis.setPoolConfig(poolCofig());
        // 初始化连接pool
        jedis.afterPropertiesSet();
        RedisConnectionFactory factory = jedis;
        return factory;
    }

    public JedisPoolConfig poolCofig() {
        JedisPoolConfig poolCofig = new JedisPoolConfig();
        poolCofig.setMaxIdle(10000);
        poolCofig.setMaxTotal(30000);
        poolCofig.setMaxWaitMillis(1500);
        poolCofig.setTestOnBorrow(true);
        poolCofig.setTestOnReturn(true);
        return poolCofig;
    }
}
