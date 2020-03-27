package com.trs.netInsight.util;

import java.util.Comparator;
import java.util.Date;

import com.trs.netInsight.widget.alert.entity.AlertEntity;

public class SortAlert implements Comparator<Object> {

	@Override
	public int compare(Object arg0, Object arg1) {
		AlertEntity t1=(AlertEntity)arg0;
		AlertEntity t2=(AlertEntity)arg1;
		Date d1,d2;
		try{
			d1=t1.getCreatedTime();
			d2=t2.getCreatedTime();
		}catch(Exception e){
			return 0;
		}
		if(d1.before(d2)){
			return 1;
		}else{
			return -1;
		}
	}

}
