/*
 * Project: netInsight
 * 
 * File Created at 2018年3月2日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.ckm.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.trs.ckm.soap.SegDictWord;

/**
 * @Desc 分词实体
 * @author changjiang
 * @date 2018年3月2日 下午5:01:34
 * @version
 */
public class Word extends SegDictWord{
	
	public Word(){
        super();
    }

    public Word(SegDictWord w){
        super();
        if(w != null){
            this.setcate(w.getcate());
            this.setword(w.getword());
        }
    }

    public Word(String text, String category) {
        super(text, category);
    }

    /**
     * 词性
     *
     * @return
     * @since changjiang @ 2018年3月2日
     */
    public String getLexicalCategory(){
        return this.getcate();
    }

    public void setLexicalCategory(String category){
        this.setcate(category);
    }

    /**
     * 文本内容
     *
     * @return
     * @since changjiang @ 2018年3月2日
     */
    public String getText(){
        return this.getword();
    }

    public void setText(String text){
        this.setword(text);
    }

    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if(false == (o instanceof Word)){
            return false;
        }

        Word w = (Word) o;

        return new EqualsBuilder().append(getcate(), w.getcate()).append(getword(), w.getword()).isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder().append(getcate()).append(getword()).toHashCode();
    }

    public String toString(){
        return new ToStringBuilder(this).append("text", getword())
                .append("lexicalcategory", getcate()).toString();
    }
	

}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年3月2日 changjiang creat
 */