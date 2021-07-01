package com.trs.netInsight.util;

import com.trs.netInsight.support.fts.entity.FtsRankListRsb;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
@Slf4j
public class SortListRsb implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        FtsRankListRsb t1 = (FtsRankListRsb) o1;
        FtsRankListRsb t2 = (FtsRankListRsb) o2;
        String d1, d2;
        try {
            d1 = t1.getHeat();
            d2 = t2.getHeat();
        } catch (Exception e) {
            // 解析出错，则不进行排序
            log.info("解析时间出错");
            return 0;
        }
        if (ObjectUtil.isEmpty(d1) || ObjectUtil.isEmpty(d2)) return -1;
        if (Integer.valueOf(d1) > Integer.valueOf(d2)) {
            return -1;
        } else {
            return 1;
        }
    }
}
