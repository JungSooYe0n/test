package com.trs.netInsight.util;

import com.trs.netInsight.support.fts.entity.FtsRankListHtb;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
@Slf4j
public class SortListHtb implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        FtsRankListHtb t1 = (FtsRankListHtb) o1;
        FtsRankListHtb t2 = (FtsRankListHtb) o2;
        String d1, d2;
        try {
            d1 = t1.getReadNum();
            d2 = t2.getReadNum();
        } catch (Exception e) {
            // 解析出错，则不进行排序
            log.info("解析时间出错");
            return 0;
        }
        if (ObjectUtil.isEmpty(d1) || ObjectUtil.isEmpty(d2)) return -1;
        if (Long.parseLong(d1) > Long.parseLong(d2)) {
            return -1;
        } else {
            return 1;
        }
    }
}
