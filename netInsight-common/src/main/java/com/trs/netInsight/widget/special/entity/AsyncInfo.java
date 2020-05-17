package com.trs.netInsight.widget.special.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@NoArgsConstructor
public class AsyncInfo {

    private AsyncDocument asyncDocument;
    /**
     * 文档md5Tag
     */
    private String md5;
    /**
     * 当前条md5查询相似文章时查询的库
     */
    private String database;
    /**
     * 当前条小心对应的数据源
     */
    private String groupName;

}
