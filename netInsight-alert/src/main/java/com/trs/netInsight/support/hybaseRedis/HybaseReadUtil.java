package com.trs.netInsight.support.hybaseRedis;

import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.user.entity.User;

public class HybaseReadUtil {

    public static void remaveHybaseReadRedisKey(User user){
        /**
         * 只需要删除对应的时间key，这时只有在查询时间超长时才会取缓存
         */
        if(user!= null && StringUtil.isNotEmpty(user.getId())){
            String redisKey = "hybaseRedis_"+user.getId()+"_";
            String redisKeyAddTime = "hybaseRedisAddTime_"+user.getId()+"_";
            RedisUtil.deleteKeyForFuzzy(redisKey);
            RedisUtil.deleteKeyForFuzzy(redisKeyAddTime);
        }
    }

}
