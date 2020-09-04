package com.trs.netInsight.support.fts.hybaselog;

import com.trs.hybase.client.TRSConnection;
import com.trs.netInsight.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class HybaseExceptionLog {

    public static void printSearchTime(Exception e, TRSConnection connection) {
        String message = e.getMessage();
        if (message.contains("timed out")) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            String finishBakFormat = simpleDateFormat.format(new Date());
            StringBuffer sb = new StringBuffer();
            sb.append("记录查询hybase查询中报错问题。")
                    .append("日期：").append(finishBakFormat).append("；")
                    .append("服务器ip：").append(connection.getURL()).append("；")
                    .append("错误信息：").append(message);
            log.error("error类型。" + sb.toString());
        }
    }

}
