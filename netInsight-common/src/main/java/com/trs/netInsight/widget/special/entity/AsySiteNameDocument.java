package com.trs.netInsight.widget.special.entity;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 信息列表相似文章相关网站信息
 *
 * Created by ChangXiaoyang on 2017/5/8.
 */
@Getter
@Data
@NoArgsConstructor
public class AsySiteNameDocument {
    /**
     * 文档SID
     */
    private String id;

    /**
     * 文档md5Tag
     */
    private String md5;
    /**
     * 相似文章数量
     */
    private Long simNum;

    /**
     * 相似文章对应网站信息
     */
    private Object sitenames;


}
