package com.trs.netInsight.support.hybaseRedis;

import com.trs.netInsight.support.api.entity.ApiAccessToken;
import com.trs.netInsight.support.api.exception.ApiException;
import com.trs.netInsight.support.api.handler.Api;
import com.trs.netInsight.support.api.result.ApiCommonResult;
import com.trs.netInsight.support.api.result.ApiResultType;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.model.result.IQueryBuilder;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author lilyy
 * @date 2020/6/23 17:20
 */
@Aspect
@Order(-99)
@Component
@Slf4j
public class HybaseReadAround {

    ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 拦截被Api标注的方法
     * @param point
     * @param hybaseRead
     * @throws Throwable
     * @Return : Object
     * 缓存中有数据,请求10s还没请求到数据,返回缓存中的数据
     * 缓存中没数据,一直请求海贝数据 -- 使用hash值算
     */
    @Around("@annotation(hybaseRead)")
    public Object before(ProceedingJoinPoint point, HybaseRead hybaseRead) throws Throwable {
        Object result = null;
        try {
            MethodSignature methodSign = (MethodSignature)point.getSignature();
            Class returnClazz = methodSign.getReturnType();
            // 获取参数列表及参数值
            Object[] paramValues = point.getArgs();
            //参数必须注意,否则可能缓存无效
            String paramsStr = getParamsStr(paramValues);
            Integer redisKeyHash = paramsStr.hashCode();
            String redisKey = "hybaseRedis_"+redisKeyHash;
            String redisKeyAddTime = "hybaseRedisAddTime_"+redisKeyHash;
            Object rt = RedisUtil.getObject(redisKey);
            String addTime = RedisUtil.getString(redisKeyAddTime);
            //key存放redis中的时间(分)
            long alreadyAddMin = 1000l;
            if(addTime != null) alreadyAddMin = DateUtil.getDateTimeMin(addTime,DateUtil.formatCurrentTime("yyyy-MM-dd HH:mm:ss"),"min");
//          // redis有数据并且小于10分钟直接去redis数据
            if(rt != null && alreadyAddMin<10){
                log.info("从redis获取该信息----------");
                if(returnClazz == java.lang.Long.class ){
                    rt = Long.parseLong(rt.toString());
                }
                return rt;
            }else if(rt != null){ //redis存在数据,10s获取不到海贝数据,则使用redis中数据
                if(returnClazz == java.lang.Long.class ){
                    rt = Long.parseLong(rt.toString());
                }
                CallableThread callableThread = new CallableThread(point,paramValues);
                Future<Object> future = executor.submit(callableThread);
                Object resultHybase = null;
                try{
                    // 同步结果，并且设置超时时间
                    resultHybase = future.get(10,TimeUnit.SECONDS);
                    RedisUtil.setObject(redisKey,resultHybase);
                    RedisUtil.setString(redisKeyAddTime,DateUtil.formatCurrentTime("yyyy-MM-dd HH:mm:ss"));
                } catch (TimeoutException e) { //请求超时或者异常出错 使用缓存数据
                    e.printStackTrace();
                    log.error("请求时间大于10s,继续走缓存");
                    resultHybase = rt;
                }  catch (Exception e) {
                    e.printStackTrace();
                    resultHybase = rt;
                }
                return resultHybase;
            }
            result = point.proceed(paramValues);// 方法运行
            if(result != null){
                RedisUtil.setObject(redisKey,result);
                RedisUtil.setString(redisKeyAddTime,DateUtil.formatCurrentTime("yyyy-MM-dd HH:mm:ss"));
            }

        } catch (Exception e) {
            log.error("Api调用失败,请返回重试或联系管理员!e=[" + e.getMessage() + "]", e);
        }
        return result;
    }
    public String getParamsStr(Object[] paramValues){
        StringBuilder sb = new StringBuilder();
        for(Object obj: paramValues){
            if(obj instanceof QueryBuilder){
                QueryBuilder queryBuilder = (QueryBuilder)obj;
                sb.append(queryBuilder.asTRSL());
            }else if (obj instanceof QueryCommonBuilder) {
                QueryCommonBuilder commonBuilder = (QueryCommonBuilder) obj;
                sb.append(commonBuilder.asTRSL());
            }else if (obj instanceof ChartResultField) {
                ChartResultField chartResultField = (ChartResultField) obj;
                sb.append(chartResultField.toString());
            }else{
                sb.append(obj);
            }
        }
        //由于IR_URLTIME 使key失效,把IR_URLTIME 去掉
        String res = sb.toString();
        if(res.contains("IR_URLTIME")){
            int t1 = res.indexOf("IR_URLTIME");
            res = res.substring(0,t1)+res.substring(t1+45);
        }
        return res;
    }

    /**
     * 此方法为了确认两次hash相同则字符串相同,两次比较,暂时不用
     * @param key
     * @return
     */
    public int toHash(String key){
        int arraySize = 11113; // 数组大小一般取质数
        int hashCode = 0;
        for (int i = 0; i < key.length(); i++) { // 从字符串的左边开始计算
            int letterValue = key.charAt(i) - 96;// 将获取到的字符串转换成数字，比如a的码值是97，则97-96=1
            // 就代表a的值，同理b=2；
            hashCode = ((hashCode << 5) + letterValue) % arraySize;// 防止编码溢出，对每步结果都进行取模运算
        }
        return hashCode;
    }

    public static void main(String args[]){
        String res = "(((IR_URLTIME:[20200618000000 TO 20200624112148]) AND ((IR_URLTITLE:((\"北京\"))))))falsefalsetrue新闻网站;微信;微博;新闻app;论坛;博客;电子报;境外columnname;value;null;";
        System.out.println("======"+res.hashCode());
        System.out.println("IR_URLTIME:[20200618000000 TO 20200624112148]".length());
        int t1 = res.indexOf("IR_URLTIME");
        String ff = res.substring(0,t1)+res.substring(t1+45);
        System.out.println("======"+ff);

        long alreadyAddMin = DateUtil.getDateTimeMin("2020-06-24 09:12:12",DateUtil.formatCurrentTime("yyyy-MM-dd HH:mm:ss"),"min");
        System.out.println("======"+alreadyAddMin);
    }

}
// 内部类 接口
class CallableThread implements Callable<Object> {
    ProceedingJoinPoint point;
    Object[] paramValues;

    public CallableThread(ProceedingJoinPoint point,Object[] paramValues){
        this.paramValues = paramValues;
        this.point = point;
    }
    @Override
    public Object call() {
        try {
            return point.proceed(paramValues);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
}
