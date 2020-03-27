package com.trs.netInsight.widget.spread.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.fts.annotation.enums.FtsHybaseType;
import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 微博实体
 *
 * Created by ChangXiaoyang on 2017/3/9.
 */
@Setter
@Getter
@NoArgsConstructor
@FtsClient(hybaseType = FtsHybaseType.WEIBO)
public class Document extends IDocument {

//    private String id;

    @FtsField("IR_SCREEN_NAME")
    private String screenName;
    @FtsField("IR_URLNAME")
    private String currentUrl;

    @FtsField("IR_RETWEETED_URL")
    private String rootUrl;

    @FtsField("IR_CONTENT")
    private String content;
    
    @FtsField("IR_UID")
    private String uid;

    @FtsField("IR_MID")
    private String mid;

    @FtsField("IR_URLTIME")
    private Date createdAt;

    @FtsField("IR_RETWEETED_SCREEN_NAME")
    private List<String> retFrom;

    private List<Document> children;
}
