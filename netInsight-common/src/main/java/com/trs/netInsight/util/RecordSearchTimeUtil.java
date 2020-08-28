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

}
