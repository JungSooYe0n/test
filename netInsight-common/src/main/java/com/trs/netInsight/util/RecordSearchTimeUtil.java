package com.trs.netInsight.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RecordSearchTimeUtil {

    /** 本地线程实例 */
    private  ThreadLocal<ThreadContext> localData = new ThreadLocal<ThreadContext>() {
        @Override
        protected ThreadContext initialValue() {
            return new ThreadContext();
        }
    };

    /**
     * 内部类： 存储信息
     */
    private class ThreadContext {
        Long searchTime_start;
        Long searchTime_end;
        Long searchTime_total = 0L;
        String hybase_ip;

    }

    public static void printSearchTime(long searchTime, String trsl, int connectTime, String db,
                                       String linked){
        if(searchTime > 5000){ //时长大于5s
            Environment env = SpringUtil.getBean(Environment.class);
            String userName = env.getProperty("hybase.database.user");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            String finishBakFormat = simpleDateFormat.format(new Date());
            StringBuffer sb = new StringBuffer();
            sb.append("记录查询hybase超时问题。")
                    .append("日期：").append(finishBakFormat).append("；")
                    .append("服务器ip：").append(linked).append("；")
                    .append("检索用户名：").append(userName).append("；")
                    .append("检索时长：").append(searchTime).append("；")
                    .append("链接hybase库时长：").append(connectTime).append("；")
                    .append("检索的hybase库：").append(db).append("。");
            log.error("error类型。"+sb.toString());
        }

    }



    public  void printSearchTimeLog(){
        Object object = localData.get();
        Environment env = SpringUtil.getBean(Environment.class);
        String userName = env.getProperty("hybase.database.user");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        String finishBakFormat = simpleDateFormat.format(new Date());
        Long totalTime = Long.parseLong(localData.get().searchTime_total.toString());
        StringBuffer sb = new StringBuffer();
        sb.append("记录查询hybase超时问题。")
                .append("日期：").append(finishBakFormat).append("；")
                .append("服务器ip：").append(localData.get().hybase_ip).append("；")
                .append("检索时长：").append(totalTime).append("；")
                .append("检索用户名：").append(userName).append("。");

        log.error("error类型。"+sb.toString());

    }



    public  void setHybaseServerInfo(String hybaseServerInfo){
        Object object = localData.get();
        localData.get().hybase_ip =hybaseServerInfo;
    }

    public  void setTime(long startHybaseTime,long endHybaseTime){
        localData.get().searchTime_start=startHybaseTime;
        localData.get().searchTime_end=endHybaseTime;
        localData.get().searchTime_total=endHybaseTime-startHybaseTime;
    }
    public  void setSearchStartTime(long startHybaseTime){
        localData.get().searchTime_start=startHybaseTime;
    }
    public  void setSearchEndTime(long endHybaseTime){
        Object map=  localData.get();
        localData.get().searchTime_end=endHybaseTime;
        if(localData.get().searchTime_start != null){
            long startHybaseTime = localData.get().searchTime_start;
            localData.get().searchTime_total=endHybaseTime-startHybaseTime;
        }else{
            localData.get().searchTime_total=0L;
        }
    }

}
