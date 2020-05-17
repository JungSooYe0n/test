package com.trs.netInsight.support.fts.model.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 词云 通用类 分类统计结果详情
 * Created by yangyanyan on 2018/5/24.
 */
@Data
@AllArgsConstructor
public class GroupWordInfo implements Comparable<GroupWordInfo>, Serializable {

    private static final long serialVersionUID = 2140177662518881151L;
    /**
     * 字段值
     */
    private String fieldValue;

    /**
     * 数量
     */
    private long count;

    /**
     * 区分 某词 所属分类
     */
    private String entityType;

    @Override
    public int compareTo(GroupWordInfo o) {
        return o.getCount() > this.count ? 1 : (this.count == o.getCount() ? 0 : -1);
    }
}
