package com.trs.netInsight.widget.special.entity.enums;

import lombok.Getter;

/**
 * 默认检索字段
 *
 * Created by ChangXiaoyang on 2017/4/27.
 */
public enum SearchScope {

    TITLE(new String[]{"IR_URLTITLE"}),//,"IR_TAG_TXT"
    //微信检索IR_TAG_TXT  相当于之前的 IR_URLTITLE
//    IR_TAG_TXT(new String[]{"IR_TAG_TXT"}),
    TITLE_CONTENT(new String[]{"IR_URLTITLE", "IR_CONTENT"}),
    TITLE_ABSTRACT(new String[]{"IR_URLTITLE","IR_ABSTRACT"});

    @Getter
    String[] field;

    SearchScope(String []field) {
        this.field = field;
    }

}
