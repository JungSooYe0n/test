package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * ES检索对应实体
 *
 * Created by ChangXiaoyang on 2017/5/2.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.TRADITIONAL)
public class ESDocumentNew extends IDocument{

    @FtsField("IR_SID")
    private String id;

    @FtsField("IR_URLTITLE")
    private String title;

    private long count;
    
    @FtsField("MD5TAG")
    private String md5;
}
