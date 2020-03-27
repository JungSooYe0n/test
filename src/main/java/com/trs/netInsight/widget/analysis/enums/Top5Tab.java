package com.trs.netInsight.widget.analysis.enums;

import lombok.Getter;

/**
 * top5排序方式
 *
 * Create by yan.changjiang on 2017年11月21日
 */
public enum Top5Tab {

	NEWEST("IR_URLTIME"),
	//NEWEST("IR_LOADTIME"),
	COMMENT("IR_COMMTCOUNT"),

	FORWARD("IR_RTTCOUNT"),

	LIKE("IR_APPROVE_COUNT");

	@Getter
	String field;

	Top5Tab(String field) {
		this.field = field;
	}

}
