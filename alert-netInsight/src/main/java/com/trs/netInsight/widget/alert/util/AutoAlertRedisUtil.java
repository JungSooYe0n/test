package com.trs.netInsight.widget.alert.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trs.netInsight.util.StringUtil;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;


public class AutoAlertRedisUtil {

    private static RedisTemplate<String, Object> redisTemplate;

    static {
        try {

            redisTemplate = createRedisTemplate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    private static RedisTemplate createRedisTemplate() {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory());

        // 使用Jackson2JsonRedisSerialize 替换默认序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        // 设置value的序列化规则和 key的序列化规则
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;

    }

    private static RedisConnectionFactory connectionFactory() {
        JedisConnectionFactory jedis = new JedisConnectionFactory();
        jedis.setHostName("192.168.201.177");
        jedis.setPort(6379);
        jedis.setPassword("trsadmin123.");
        jedis.setDatabase(9);

        jedis.setPoolConfig(poolCofig());
        // 初始化连接pool
        jedis.afterPropertiesSet();
        RedisConnectionFactory factory = jedis;
        return factory;
    }

    private static JedisPoolConfig poolCofig() {
        JedisPoolConfig poolCofig = new JedisPoolConfig();
        poolCofig.setMaxIdle(10000);
        poolCofig.setMaxTotal(30000);
        poolCofig.setMaxWaitMillis(1500);
        poolCofig.setTestOnBorrow(true);
        poolCofig.setTestOnReturn(true);
        return poolCofig;
    }

    public static Long getSizeForList(String key){
        if(StringUtil.isEmpty(key)){
            return 0L;
        }
        return redisTemplate.opsForList().size(key);
    }

    public static Object getOneDataForList(String key){
        if(StringUtil.isEmpty(key)){
            return null;
        }
        return redisTemplate.opsForList().rightPop(key);
    }
    public static void removeAllDataForlist(String key){
        if(StringUtil.isNotEmpty(key)){
            redisTemplate.opsForList().trim(key,0,0);
        }
    }

    public static Object getOneDataForHash(String hashName,String hashKey){
        if(StringUtil.isEmpty(hashName) || StringUtil.isEmpty(hashKey)){
            return null;
        }
        return redisTemplate.opsForHash().get(hashName, hashKey);
    }



}
