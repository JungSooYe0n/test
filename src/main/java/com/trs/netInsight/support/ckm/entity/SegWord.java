package com.trs.netInsight.support.ckm.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SegWord {
    private String word;
    private String cate;
    private boolean isMain;

    public SegWord() {
        this.word = null;
        this.cate = null;
        this.isMain = false;
    }

    public SegWord(String _word, String _cate,boolean isMain) {
        this.word = _word;
        this.cate = _cate;
        this.isMain = isMain;
    }

}
