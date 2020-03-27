package com.trs.netInsight.util;

import java.util.Comparator;
import java.util.Date;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatusLoad implements Comparator<Object>{

	@Override
	public int compare(Object arg0, Object arg1) {
		FtsDocumentStatus t1 = (FtsDocumentStatus) arg0;
		FtsDocumentStatus t2 = (FtsDocumentStatus) arg1;
		Date d1, d2;
		try {
			d1 = t1.getLoadTime();
			d2 = t2.getLoadTime();
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

