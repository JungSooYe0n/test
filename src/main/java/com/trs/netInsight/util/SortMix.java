package com.trs.netInsight.util;

import java.util.Comparator;
import java.util.Date;

import com.trs.netInsight.support.fts.entity.FtsDocumentChaos;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class SortMix implements Comparator<Object>{

	@Override
	public int compare(Object arg0, Object arg1) {
		FtsDocumentChaos t1 = (FtsDocumentChaos) arg0;
		FtsDocumentChaos t2 = (FtsDocumentChaos) arg1;
        Date d1, d2;
        try {
            d1 = t1.getUrlTime();
            d2 = t2.getUrlTime();
        } catch (Exception e) {
            // 解析出错，则不进行排序
        	log.info("解析时间出错");
            return 0;
        }
        if (d1.before(d2)) {
            return -1;
        } else {
            return 1;
        }
	}

}
