package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 网友观点 实体
 * Created by yangyanyan on 2018/5/9.
 */
@Setter
@Getter
@NoArgsConstructor
public class ViewEntity extends IDocument {

    /**
     * 观点
     */
    private String view;

    /**
     * 所占百分比
     */
    private long count;

    /**
     * 情感分类
     */
    private String appraise;
}
