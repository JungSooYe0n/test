package com.trs.netInsight.support.redis;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author:拓尔思信息股份有限公司
 * @Description:
 * @Date:Created in  2020/3/6 18:06
 * @Created By yangyanyan
 */

@Component
public class RedisOperator {
    @Autowired
    @Qualifier("customRedisTemplate")
    private RedisTemplate redisTemplate;



    public  RedisTemplate getRedisTemplate(int index){
        return  getTemplate(redisTemplate,index);
    }

    public RedisTemplate getTemplate(RedisTemplate redisTemplate,int index) {
        JedisConnectionFactory jedisConnectionFactory = (JedisConnectionFactory) redisTemplate.getConnectionFactory();
        jedisConnectionFactory.setDatabase(index);
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        return redisTemplate;
    }

    public <T>  void set(int index, String key, List<T>  value, long expire, TimeUnit unit) {
//        ValueOperations<String, List<Object>> opsForValue = getRedisTemplate(index).opsForValue();
//        opsForValue.set(key, (List<Object>) value);
//        getRedisTemplate(index).expire(key, expire, unit);
//        getRedisTemplate(index).opsForValue().set(key,value);

        ValueOperations<String, List<Object>> opsForValue = getRedisTemplate(index).opsForValue();
        opsForValue.set(key, (List<Object>) value);
        getRedisTemplate(index).expire(key, expire, unit);
    }


    public <T> List<T>  getObject(int index,String key,Class<T> t) {
        ValueOperations<String, List<Object>> opsForValue = getRedisTemplate(index).opsForValue();
        return (List<T>)opsForValue.get(key);
    }



}
