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
@FtsClient(hybaseType = FtsHybaseType.WEIBO_RSB)
public class FtsRankListRsb extends IDocument {
    /**
     * 发布时间
     */
    @FtsField("IR_URLTIME")
    private Date urlTime;
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
     * 数据
     */
    @FtsField(value = "IR_HOTWORD")
    private String hotWord;
    /**
     * 频道
     */
    @FtsField(value = "IR_CHANNEL")
    private String channel;
    /**
     * 搜索指数
     */
    @FtsField(value = "IR_SEARCH_INDEX")
    private String searchIndex;
    /**
     * 热度指数
     */
    @FtsField(value = "IR_HEAT")
    private String heat;
    @FtsField(value = "DESC_EXTR")
    private String descExtr;
    @FtsField(value = "IR_READNUM")
    private String readNum;


}
