package com.trs.netInsight.util;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.widget.user.entity.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginUtil {

    /**
     * 到期时间提醒
     *
     * @param user
     * @throws TRSException
     */
    public static void rangeExpiret(User user) throws TRSException {
        //权限重构后 2050-01-01 00:00:00 代表永久
        if (!UserUtils.FOREVER_DATE.equals(user.getExpireAt())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date dateExpire = sdf.parse(user.getExpireAt());
                int rangBetweenNow = DateUtil.rangBetweenNow(dateExpire);
                if (rangBetweenNow >= 0) {
//					if (rangBetweenNow <= 5) {
//						user.setRed(true);
//					} else {
//						user.setRed(false);
//					}
//					user.setExpireMessage("试用期还有" + rangBetweenNow + "天");
                    user.setRemainingTime(rangBetweenNow);
                }
            } catch (ParseException e) {
                throw new TRSException(e);
            }
        }
    }
}
