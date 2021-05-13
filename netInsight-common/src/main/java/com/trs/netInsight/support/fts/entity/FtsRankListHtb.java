package com.trs.netInsight.support.fts.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.WEIBO_HTB)
public class FtsRankListHtb extends IDocument {

    /**
     * 发布时间
     */
    @FtsField("IR_LASTTIME")
    private Date lastTime;
    /**
     * 标题
     */
    @FtsField(value = "IR_URLTITLE")
    private String title;

    /**
     * 频道
     */
    @FtsField(value = "IR_CHANNEL")
    private String channel;


    @FtsField(value = "IR_READNUM")
    private String readNum;


}
