package com.trs.netInsight.util;

import com.trs.netInsight.support.fts.entity.FtsRankList;
import com.trs.netInsight.support.fts.entity.FtsRankListHtb;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
@Slf4j
public class SortListBangdanHeat implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        FtsRankList t1 = (FtsRankList) o1;
        FtsRankList t2 = (FtsRankList) o2;
        String d1, d2;
        try {
            d1 = t1.getHeat();
            d2 = t2.getHeat();
        } catch (Exception e) {
            // 解析出错，则不进行排序
            return 0;
        }
        if (Integer.valueOf(d1) > Integer.valueOf(d2)) {
            return -1;
        } else {
            return 1;
        }
    }
}
