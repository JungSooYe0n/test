package com.trs.netInsight.widget.apply.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {
    //正式formal，试用trial
    formal("正式"),
    trial("试用");
    private String name;
}
