package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.support.fts.model.result.IDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Created by yangyanyan on 2018/5/7.
 */
@Setter
@Getter
@NoArgsConstructor
public class Topic extends IDocument {

    /**
     * 词字
     */
    private String name;

    /**
     * 出现次数
     */
    private long count;

    private List<Topic> children;

    public Topic(String name, long count) {
        this.name = name;
        this.count = count;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}
